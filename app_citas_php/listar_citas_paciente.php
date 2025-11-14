<?php
header('Content-Type: application/json; charset=utf-8');
include 'conexion.php';

$id_paciente = $_POST['id_paciente'] ?? '';
if ($id_paciente === '' || !ctype_digit($id_paciente)) {
    echo json_encode(["estado" => "error", "mensaje" => "id_paciente inválido"]);
    exit();
}

$stmt = $conexion->prepare("
    SELECT c.id, c.fecha, c.hora, c.estado, c.consultorio,
           m.id AS id_medico, m.nombre AS medico, m.especialidad
    FROM cita c
    JOIN medico m ON m.id = c.id_medico
    WHERE c.id_paciente = ? AND c.estado = 'programada'
    ORDER BY c.fecha ASC, c.hora ASC
");
$stmt->bind_param("i", $id_paciente);
$stmt->execute();
$res = $stmt->get_result();

$citas = [];
while ($r = $res->fetch_assoc()) {
    $r['id'] = (int)$r['id'];
    $r['id_medico'] = (int)$r['id_medico'];
    $citas[] = $r;
}
echo json_encode(["estado" => "ok", "citas" => $citas]);

$stmt->close();
$conexion->close();