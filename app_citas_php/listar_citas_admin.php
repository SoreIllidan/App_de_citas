<?php
header('Content-Type: application/json; charset=utf-8');
include 'conexion.php';

$estado = $_GET['estado'] ?? null; // opcional: programada/cancelada
if ($estado && !in_array($estado, ['programada', 'cancelada'])) $estado = null;

if ($estado) {
    $stmt = $conexion->prepare("
      SELECT c.id, c.fecha, c.hora, c.estado, c.consultorio,
             p.id AS id_paciente, p.nombre AS paciente, p.dni,
             m.id AS id_medico, m.nombre AS medico, m.especialidad
      FROM cita c
      JOIN paciente p ON p.id = c.id_paciente
      JOIN medico m ON m.id = c.id_medico
      WHERE c.estado = ?
      ORDER BY c.fecha DESC, c.hora DESC
    ");
    $stmt->bind_param("s", $estado);
} else {
    $stmt = $conexion->prepare("
      SELECT c.id, c.fecha, c.hora, c.estado, c.consultorio,
             p.id AS id_paciente, p.nombre AS paciente, p.dni,
             m.id AS id_medico, m.nombre AS medico, m.especialidad
      FROM cita c
      JOIN paciente p ON p.id = c.id_paciente
      JOIN medico m ON m.id = c.id_medico
      ORDER BY c.fecha DESC, c.hora DESC
    ");
}
$stmt->execute();
$res = $stmt->get_result();

$citas = [];
while ($r = $res->fetch_assoc()) {
    $r['id'] = (int)$r['id'];
    $r['id_paciente'] = (int)$r['id_paciente'];
    $r['id_medico'] = (int)$r['id_medico'];
    $citas[] = $r;
}
echo json_encode(["estado" => "ok", "citas" => $citas]);

$stmt->close();
$conexion->close();