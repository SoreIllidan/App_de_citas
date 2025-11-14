<?php
header('Content-Type: application/json; charset=utf-8');
include 'conexion.php';

$id_cita = $_POST['id_cita'] ?? '';
$id_medico = $_POST['id_medico'] ?? '';
$fecha = $_POST['fecha'] ?? '';
$hora = $_POST['hora'] ?? '';

if (!ctype_digit($id_cita) || !ctype_digit($id_medico)) {
    echo json_encode(["estado" => "error", "mensaje" => "IDs inválidos"]);
    exit();
}
$dt = DateTime::createFromFormat('Y-m-d', $fecha);
if (!$dt || $dt->format('Y-m-d') !== $fecha) {
    echo json_encode(["estado" => "error", "mensaje" => "Fecha inválida"]);
    exit();
}
if (!preg_match('/^\d{2}:\d{2}(:\d{2})?$/', $hora)) {
    echo json_encode(["estado" => "error", "mensaje" => "Hora inválida"]);
    exit();
}

try {
    $conexion->begin_transaction();

    // Trae cita actual y bloquea
    $stmt = $conexion->prepare("SELECT id_medico, fecha, hora, estado FROM cita WHERE id = ? FOR UPDATE");
    $stmt->bind_param("i", $id_cita);
    $stmt->execute();
    $actual = $stmt->get_result()->fetch_assoc();
    if (!$actual) {
        $conexion->rollback();
        echo json_encode(["estado" => "no_encontrada"]);
        exit();
    }
    if ($actual['estado'] !== 'programada') {
        $conexion->rollback();
        echo json_encode(["estado" => "no_permitido", "mensaje" => "La cita no está programada"]);
        exit();
    }

    // Reserva nuevo slot
    $stmt = $conexion->prepare("SELECT id, disponible FROM horario_medico WHERE id_medico = ? AND fecha = ? AND hora = ? FOR UPDATE");
    $stmt->bind_param("iss", $id_medico, $fecha, $hora);
    $stmt->execute();
    $slot_nuevo = $stmt->get_result()->fetch_assoc();
    if (!$slot_nuevo || (int)$slot_nuevo['disponible'] !== 1) {
        $conexion->rollback();
        echo json_encode(["estado" => "no_disponible"]);
        exit();
    }

    // Libera slot anterior (si existía en horario_medico)
    $stmt = $conexion->prepare("UPDATE horario_medico SET disponible = 1 WHERE id_medico = ? AND fecha = ? AND hora = ?");
    $stmt->bind_param("iss", $actual['id_medico'], $actual['fecha'], $actual['hora']);
    $stmt->execute(); // si no existía, no pasa nada