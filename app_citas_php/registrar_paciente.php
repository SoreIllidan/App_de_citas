<?php
header('Content-Type: application/json; charset=utf-8');
include 'conexion.php';

$dni = $_POST['dni'] ?? '';
$nombre = $_POST['nombre'] ?? '';
$correo = $_POST['correo'] ?? '';
$contrasena = $_POST['contrasena'] ?? '';

if ($dni === '' || $nombre === '' || $correo === '' || $contrasena === '') {
    echo json_encode(["estado" => "error", "mensaje" => "Faltan parámetros"]);
    exit();
}

try {
    // Verifica si dni ya existe
    $stmt = $conexion->prepare("SELECT id FROM paciente WHERE dni = ?");
    $stmt->bind_param("s", $dni);
    $stmt->execute();
    $stmt->store_result();
    if ($stmt->num_rows > 0) {
        echo json_encode(["estado" => "existe", "mensaje" => "DNI ya registrado"]);
        $stmt->close();
        exit();
    }
    $stmt->close();

    // Inserta nuevo paciente
    $hash = password_hash($contrasena, PASSWORD_DEFAULT);
    $stmt = $conexion->prepare("INSERT INTO paciente (dni, nombre, correo, contrasena) VALUES (?, ?, ?, ?)");
    $stmt->bind_param("ssss", $dni, $nombre, $correo, $hash);
    $stmt->execute();

    echo json_encode([
        "estado" => "ok",
        "id" => $conexion->insert_id,  // DEVUELVE EL ID POR SI QUIERES LOGUEAR DIRECTO O GUARDARLO
        "mensaje" => "Paciente registrado"
    ]);
    $stmt->close();
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(["estado" => "error", "mensaje" => "No se pudo registrar"]);
} finally {
    $conexion->close();
}