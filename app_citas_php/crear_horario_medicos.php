<?php
header('Content-Type: application/json; charset=utf-8');
include 'conexion.php';

$id_medico = $_POST['id_medico'] ?? '';
$fecha = $_POST['fecha'] ?? '';
$horas = $_POST['horas'] ?? []; // puede venir como horas[]=09:00&horas[]=09:30 o CSV

if ($id_medico === '' || !ctype_digit($id_medico)) {
    echo json_encode(["estado" => "error", "mensaje" => "id_medico inválido"]);
    exit();
}
$dt = DateTime::createFromFormat('Y-m-d', $fecha);
if (!$dt || $dt->format('Y-m-d') !== $fecha) {
    echo json_encode(["estado" => "error", "mensaje" => "Fecha inválida"]);
    exit();
}

if (is_string($horas)) {
    $horas = array_filter(array_map('trim', explode(',', $horas)));
}
if (!is_array($horas) || empty($horas)) {
    echo json_encode(["estado" => "error", "mensaje" => "Lista de horas vacía"]);
    exit();
}

$insertados = 0;
$stmt = $conexion->prepare("INSERT IGNORE INTO horario_medico (id_medico, fecha, hora, disponible) VALUES (?, ?, ?, 1)");

foreach ($horas as $h) {
    // valida HH:MM o HH:MM:SS
    if (!preg_match('/^\d{2}:\d{2}(:\d{2})?$/', $h)) continue;
    $stmt->bind_param("iss", $id_medico, $fecha, $h);
    $stmt->execute();
    if ($stmt->affected_rows > 0) $insertados++;
}

echo json_encode(["estado" => "ok", "insertados" => $insertados]);
$stmt->close();
$conexion->close();