<?php
header('Content-Type: application/json; charset=utf-8');
include 'conexion.php';

$id = $_POST['id'] ?? '';
$nombre = $_POST['nombre'] ?? null;
$especialidad = $_POST['especialidad'] ?? null;
$correo = $_POST['correo'] ?? null;
$telefono = $_POST['telefono'] ?? null;

if ($id === '' || !ctype_digit($id)) {
    echo json_encode(["estado" => "error", "mensaje" => "id inválido"]);
    exit();
}

$campos = [];
$vals = [];
$types = '';

if ($nombre !== null) { $campos[] = "nombre = ?"; $vals[] = $nombre; $types .= 's'; }
if ($especialidad !== null) { $campos[] = "especialidad = ?"; $vals[] = $especialidad; $types .= 's'; }
if ($correo !== null) { $campos[] = "correo = ?"; $vals[] = $correo; $types .= 's'; }
if ($telefono !== null) { $campos[] = "telefono = ?"; $vals[] = $telefono; $types .= 's'; }

if (empty($campos)) {
    echo json_encode(["estado" => "sin_cambios"]);
    exit();
}

$sql = "UPDATE medico SET " . implode(", ", $campos) . " WHERE id = ?";
$types .= 'i';
$vals[] = (int)$id;

$stmt = $conexion->prepare($sql);
$stmt->bind_param($types, ...$vals);
$stmt->execute();

echo json_encode(["estado" => $stmt->affected_rows > 0 ? "ok" : "sin_cambios"]);
$stmt->close();
$conexion->close();