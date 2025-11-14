<?php
header('Content-Type: application/json; charset=utf-8');
include 'conexion.php';

$id_cita = $_POST['id_cita'] ?? '';

if ($id_cita === '' || !ctype_digit($id_cita)) {
    echo json_encode(["estado" => "error", "mensaje" => "id_cita inválido"]);
    exit();
}

try {
    $conexion->begin_transaction();

    // Busca cita
    $stmt = $conexion->prepare("SELECT id_paciente, id_medico, fecha, hora, estado FROM cita WHERE id = ? FOR UPDATE");
    $stmt->bind_param("i", $id_cita);
    $stmt->execute();
    $cita = $stmt->get_result()->fetch_assoc();
    if (!$cita) { $conexion->rollback(); echo json_encode(["estado" => "no_encontrada"]); exit(); }
    if ($cita['estado'] === 'cancelada') { $conexion->rollback(); echo json_encode(["estado" => "sin_cambios"]); exit(); }

    // Cancela
    $stmt = $conexion->prepare("UPDATE cita SET estado='cancelada' WHERE id = ?");
    $stmt->bind_param("i", $id_cita);
    $stmt->execute();

    // Libera slot
    $stmt = $conexion->prepare("UPDATE horario_medico SET disponible = 1 WHERE id_medico = ? AND fecha = ? AND hora = ?");
    $stmt->bind_param("iss", $cita['id_medico'], $cita['fecha'], $cita['hora']);
    $stmt->execute();

    // Historial
    $stmt = $conexion->prepare("INSERT INTO cita_historial (id_cita, id_paciente, id_medico, accion, fecha_anterior, hora_anterior) VALUES (?, ?, ?, 'cancelada', ?, ?)");
    $stmt->bind_param("iiiss", $id_cita, $cita['id_paciente'], $cita['id_medico'], $cita['fecha'], $cita['hora']);
    $stmt->execute();

    $conexion->commit();
    echo json_encode(["estado" => "ok"]);
} catch (Exception $e) {
    $conexion->rollback();
    http_response_code(500);
    echo json_encode(["estado" => "error", "mensaje" => "No se pudo cancelar"]);
} finally {
    $conexion->close();
}