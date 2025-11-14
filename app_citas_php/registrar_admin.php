<?php
header('Content-Type: application/json; charset=utf-8');
include 'conexion.php';

$usuario = $_POST['usuario'] ?? '';
$contrasena = $_POST['contrasena'] ?? '';

if ($usuario === '' || $contrasena === '') {
    echo json_encode(["estado" => "error", "mensaje" => "Faltan parámetros"]);
    exit();
}

$hash = password_hash($contrasena, PASSWORD_DEFAULT);
$stmt = $conexion->prepare("INSERT INTO admin (usuario, contrasena, activo) VALUES (?, ?, 1)");
$stmt->bind_param("ss", $usuario, $hash);
$stmt->execute();

echo json_encode(["estado" => "ok", "id" => $conexion->insert_id]);
$stmt->close();
$conexion->close();