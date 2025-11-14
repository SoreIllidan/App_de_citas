<?php
header('Content-Type: application/json; charset=utf-8');
include 'conexion.php';

$id_paciente = $_GET['id_paciente'] ?? $_POST['id_paciente'] ?? '';
if ($id_paciente === '' || !ctype_digit($id_paciente)) {
    echo json_encode(["estado" => "error", "mensaje" => "id_paciente inválido"]);
    exit();
}

$stmt = $conexion->prepare("
  SELECT h.id, h.id_cita, h.accion, h.fecha_anterior, h.hora_anterior, h.fecha_nueva, h.hora_nueva, h.created_at,
         c.id_medico, m.nombre AS medico, m.especialidad
  FROM cita_historial h
  JOIN cita c ON c.id = h.id_cita
  JOIN medico m ON m.id = c.id_medico
  WHERE h.id_paciente = ?
  ORDER BY h.created_at DESC
");
$stmt->bind_param("i", $id_paciente);
$stmt->execute();
$res = $stmt->get_result();

$data = [];
while ($r = $res->fetch_assoc()) {
    $data[] = $r;
}
echo json_encode(["estado" => "ok", "historial" => $data]);

$stmt->close();
$conexion->close();