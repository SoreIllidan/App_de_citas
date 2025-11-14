<?php
header('Content-Type: application/json; charset=utf-8');
include 'conexion.php';

$id_paciente = $_POST['id_paciente'] ?? '';
$id_medico = $_POST['id_medico'] ?? '';
$fecha = $_POST['fecha'] ?? '';
$hora = $_POST['hora'] ?? '';
$consultorio = $_POST['consultorio'] ?? null;

if (!ctype_digit($id_paciente) || !ctype_digit($id_medico)) {
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

    // Verifica y bloquea el slot
    $stmt = $conexion->prepare("SELECT id, disponible FROM horario_medico WHERE id_medico = ? AND fecha = ? AND hora = ? FOR UPDATE");
    $stmt->bind_param("iss", $id_medico, $fecha, $hora);
    $stmt->execute();
    $slot = $stmt->get_result()->fetch_assoc();
    if (!$slot || (int)$slot['disponible'] !== 1) {
        $conexion->rollback();
        echo json_encode(["estado" => "no_disponible"]);
        exit();
    }

    // Inserta cita
    $stmt = $conexion->prepare("INSERT INTO cita (id_paciente, id_medico, fecha, hora, consultorio, estado) VALUES (?, ?, ?, ?, ?, 'programada')");
    $stmt->bind_param("iisss", $id_paciente, $id_medico, $fecha, $hora, $consultorio);
    $stmt->execute();
    $id_cita = $conexion->insert_id;

    // Marca slot como no disponible
    $stmt = $conexion->prepare("UPDATE horario_medico SET disponible = 0 WHERE id = ?");
    $stmt->bind_param("i", $slot['id']);
    $stmt->execute();

    $conexion->commit();
    echo json_encode(["estado" => "ok", "id_cita" => $id_cita]);
} catch (Exception $e) {
    $conexion->rollback();
    http_response_code(500);
    echo json_encode(["estado" => "error", "mensaje" => "No se pudo crear la cita"]);
} finally {
    $conexion->close();
}