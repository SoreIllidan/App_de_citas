<?php
header('Content-Type: application/json; charset=utf-8');
include 'conexion.php';

$usuario = $_POST['usuario'] ?? '';
$contrasena = $_POST['contrasena'] ?? '';

if ($usuario === '' || $contrasena === '') {
    echo json_encode(["estado" => "error", "mensaje" => "Faltan parámetros"]);
    exit();
}

$stmt = $conexion->prepare("SELECT id, contrasena, activo FROM admin WHERE usuario = ?");
$stmt->bind_param("s", $usuario);
$stmt->execute();
$res = $stmt->get_result();

if ($row = $res->fetch_assoc()) {
    if ((int)$row['activo'] !== 1) {
        echo json_encode(["estado" => "inactivo"]);
    } elseif (password_verify($contrasena, $row['contrasena'])) {
        echo json_encode(["estado" => "ok", "id" => (int)$row['id'], "usuario" => $usuario]);
    } else {
        echo json_encode(["estado" => "contrasena_incorrecta"]);
    }
} else {
    echo json_encode(["estado" => "no_encontrado"]);
}

$stmt->close();
$conexion->close();