<?php
header('Content-Type: application/json; charset=utf-8');
include 'conexion.php';

$dni = $_POST['dni'] ?? '';
$contrasena = $_POST['contrasena'] ?? '';

if ($dni === '' || $contrasena === '') {
    echo json_encode(["estado" => "error", "mensaje" => "Faltan parámetros"]);
    exit();
}

$stmt = $conexion->prepare("SELECT id, nombre, correo, contrasena FROM paciente WHERE dni = ?");
$stmt->bind_param("s", $dni);
$stmt->execute();
$result = $stmt->get_result();

if ($row = $result->fetch_assoc()) {
    if (password_verify($contrasena, $row['contrasena'])) {
        echo json_encode([
            "estado" => "ok",
            "id" => (int)$row['id'],         // DEVUELVE EL ID PARA USARLO EN LAS SIGUIENTES LLAMADAS
            "nombre" => $row['nombre'],
            "correo" => $row['correo']
        ]);
    } else {
        echo json_encode(["estado" => "contrasena_incorrecta"]);
    }
} else {
    echo json_encode(["estado" => "no_encontrado"]);
}

$stmt->close();
$conexion->close();