<?php
include "conexion.php";

$correo = $_POST['correo'] ?? '';
$token = $_POST['token'] ?? '';
$nueva = $_POST['nueva'] ?? '';

$hash = password_hash($nueva, PASSWORD_BCRYPT);

$stmt = $conexion->prepare("SELECT id FROM paciente WHERE correo=? AND reset_token=? AND reset_expira > NOW()");
$stmt->bind_param("ss", $correo, $token);
$stmt->execute();
$res = $stmt->get_result();

if ($res->num_rows == 0) {
    echo json_encode(["estado"=>"expirado"]);
    exit();
}

$stmt = $conexion->prepare("UPDATE paciente SET contrasena=?, reset_token=NULL, reset_expira=NULL WHERE correo=?");
$stmt->bind_param("ss", $hash, $correo);
$stmt->execute();

echo json_encode(["estado"=>"ok"]);
