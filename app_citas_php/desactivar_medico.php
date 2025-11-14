<?php
header('Content-Type: application/json; charset=utf-8');
include 'conexion.php';

$solo_activos = isset($_GET['activos']) ? ($_GET['activos'] === '1') : true;

if ($solo_activos) {
    $sql = "SELECT id, nombre, especialidad, correo, telefono FROM medico WHERE activo = 1 ORDER BY nombre ASC";
    $stmt = $conexion->prepare($sql);
} else {
    $sql = "SELECT id, nombre, especialidad, correo, telefono, activo FROM medico ORDER BY nombre ASC";
    $stmt = $conexion->prepare($sql);
}

$stmt->execute();
$res = $stmt->get_result();

$medicos = [];
while ($r = $res->fetch_assoc()) {
    $r['id'] = (int)$r['id'];
    if (isset($r['activo'])) $r['activo'] = (int)$r['activo'];
    $medicos[] = $r;
}
echo json_encode(["estado" => "ok", "medicos" => $medicos]);

$stmt->close();
$conexion->close();