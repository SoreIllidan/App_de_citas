<?php
header('Content-Type: application/json; charset=utf-8');
include 'conexion.php';

$nombre = $_POST['nombre'] ?? '';
$especialidad = $_POST['especialidad'] ?? '';
$correo = $_POST['correo'] ?? null;
$telefono = $_POST['telefono'] ?? null;

if ($nombre === '' || $especialidad === '') {
    echo json_encode(["estado" => "error", "mensaje" => "Faltan parámetros"]);
    exit();
}

try {
    $stmt = $conexion->prepare("INSERT INTO medico (nombre, especialidad, correo, telefono, activo) VALUES (?, ?, ?, ?, 1)");
    $stmt->bind_param("ssss", $nombre, $especialidad, $correo, $telefono);
    $stmt->execute();

    echo json_encode(["estado" => "ok", "id" => $conexion->insert_id]);
    $stmt->close();
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(["estado" => "error", "mensaje" => "No se pudo registrar"]);
} finally {
    $conexion->close();
}