<?php
header('Content-Type: application/json; charset=utf-8');
include 'conexion.php';

$id_paciente = $_POST['id_paciente'] ?? '';
if ($id_paciente === '' || !ctype_digit($id_paciente)) {
    echo json_encode(["estado" => "error", "mensaje" => "id_paciente inválido"]);
    exit();
}

$stmt = $conexion->prepare("
    SELECT c.id, c.id_paciente, c.id_medico, c.fecha, c.hora, c.consultorio, c.estado,
           m.nombre AS medico, m.especialidad
    FROM cita c
    JOIN medico m ON m.id = c.id_medico
    WHERE c.id_paciente = ? AND c.estado='programada'
    ORDER BY c.fecha ASC, c.hora ASC
");
$stmt->bind_param("i", $id_paciente);
$stmt->execute();
$res = $stmt->get_result();

$citas = [];
while ($fila = $res->fetch_assoc()) {
    $fila['id'] = (int)$fila['id'];
    $fila['id_paciente'] = (int)$fila['id_paciente'];
    $fila['id_medico'] = (int)$fila['id_medico'];
    $citas[] = $fila;
}
echo json_encode(["estado" => "ok", "citas" => $citas]);

$stmt->close();
$conexion->close();