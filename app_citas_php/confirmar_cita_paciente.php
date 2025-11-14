<?php
header('Content-Type: application/json; charset=utf-8');
include 'conexion.php';

$id_cita = $_POST['id_cita'] ?? '';
$id_paciente = $_POST['id_paciente'] ?? '';

if ($id_cita === '' || !ctype_digit($id_cita) || $id_paciente === '' || !ctype_digit($id_paciente)) {
    echo json_encode(["estado" => "error", "mensaje" => "Parámetros inválidos"]);
    exit();
}

try {
    $conexion->begin_transaction();

    // Verifica propiedad y estado
    $stmt = $conexion->prepare("SELECT id_paciente, estado, fecha, hora FROM cita WHERE id = ? FOR UPDATE");
    $stmt->bind_param("i", $id_cita);
    $stmt->execute();
    $cita = $stmt->get_result()->fetch_assoc();

    if (!$cita) {
        $conexion->rollback();
        echo json_encode(["estado" => "no_encontrada"]);
        exit();
    }
    if ((int)$cita['id_paciente'] !== (int)$id_paciente) {
        $conexion->rollback();
        echo json_encode(["estado" => "no_autorizado"]);
        exit();
    }
    if (strtolower($cita['estado']) === 'cancelada') {
        $conexion->rollback();
        echo json_encode(["estado" => "ya_cancelada"]);
        exit();
    }
    // Opcional: impedir confirmar si ya pasó
    // if (strtotime($cita['fecha'].' '.$cita['hora']) < time()) { ... }

    // Marca confirmada
    $stmt = $conexion->prepare("UPDATE cita SET estado='confirmada', confirmed_at=NOW() WHERE id=?");
    $stmt->bind_param("i", $id_cita);
    $stmt->execute();

    $conexion->commit();
    echo json_encode(["estado" => "ok"]);
} catch (Exception $e) {
    $conexion->rollback();
    http_response_code(500);
    echo json_encode(["estado" => "error", "mensaje" => "No se pudo confirmar"]);
} finally {
    $conexion->close();
}