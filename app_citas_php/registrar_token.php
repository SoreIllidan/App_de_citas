<?php
header('Content-Type: application/json; charset=utf-8');
include 'conexion.php';

$id_paciente = $_POST['id_paciente'] ?? '';
$token = $_POST['token'] ?? '';

if ($id_paciente === '' || !ctype_digit($id_paciente) || $token === '') {
  echo json_encode(["estado"=>"error","mensaje"=>"parámetros inválidos"]); exit;
}

try {
  // Inserta o ignora si ya existe
  $stmt = $conexion->prepare("INSERT INTO paciente_token (id_paciente, token) VALUES (?, ?) ON DUPLICATE KEY UPDATE id_paciente=VALUES(id_paciente)");
  $stmt->bind_param("is", $id_paciente, $token);
  $stmt->execute();
  echo json_encode(["estado"=>"ok"]);
} catch (Exception $e) {
  http_response_code(500);
  echo json_encode(["estado"=>"error"]);
} finally {
  $conexion->close();
}