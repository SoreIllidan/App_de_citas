<?php
header('Content-Type: application/json; charset=utf-8');
include 'conexion.php';

$id_paciente = $_GET['id_paciente'] ?? $_POST['id_paciente'] ?? '';

if ($id_paciente === '' || !ctype_digit($id_paciente)) {
    echo json_encode(["estado"=>"error","mensaje"=>"id_paciente inválido"]);
    exit();
}

$stmt = $conexion->prepare("
  SELECT id, dni, nombre, correo, telefono, fecha_nacimiento, ocupacion,
         emergencia_nombre, emergencia_relacion, emergencia_telefono,
         tipo_sangre, alergias, historial_medico, aseguradora, poliza, direccion
  FROM paciente
  WHERE id = ?
  LIMIT 1
");
$stmt->bind_param("i", $id_paciente);
$stmt->execute();
$res = $stmt->get_result();

if ($fila = $res->fetch_assoc()) {
    $fila['id'] = (int)$fila['id'];
    echo json_encode(["estado"=>"ok","perfil"=>$fila]);
} else {
    echo json_encode(["estado"=>"no_encontrado"]);
}

$stmt->close();
$conexion->close();