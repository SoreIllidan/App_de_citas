<?php
// Habilita errores de mysqli como excepciones para depurar mejor
mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);

try {
    $conexion = new mysqli("localhost", "root", "", "bd_citas_medicas");
    $conexion->set_charset("utf8mb4");
} catch (Exception $e) {
    http_response_code(500);
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode(["estado" => "error", "mensaje" => "Error de conexión a BD"]);
    exit;
}