<?php
header('Content-Type: application/json; charset=utf-8');
include 'conexion.php';

$stmt = $conexion->prepare("SELECT id, dni, nombre, correo FROM paciente ORDER BY nombre ASC");
$stmt->execute();
$res = $stmt->get_result();

$pacientes = [];
while ($r = $res->fetch_assoc()) {
    $r['id'] = (int)$r['id'];
    $pacientes[] = $r;
}
echo json_encode(["estado" => "ok", "pacientes" => $pacientes]);

$stmt->close();
$conexion->close();