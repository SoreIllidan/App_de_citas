<?php
include 'conexion.php';

$id = $_GET['id'] ?? '';
if ($id === '' || !ctype_digit($id)) { http_response_code(400); echo "id inválido"; exit; }

$stmt = $conexion->prepare("SELECT file_name, file_path, mime_type FROM cita_documento WHERE id=?");
$stmt->bind_param("i", $id);
$stmt->execute();
$r = $stmt->get_result()->fetch_assoc();
$stmt->close();
if (!$r) { http_response_code(404); echo "no encontrado"; exit; }

$absPath = __DIR__ . '/' . $r['file_path'];
if (!is_file($absPath)) { http_response_code(404); echo "archivo no existe"; exit; }

$mime = $r['mime_type'] ?: 'application/pdf';
header('Content-Type: '.$mime);
header('Content-Disposition: inline; filename="'.basename($r['file_name']).'"');
header('Content-Length: '.filesize($absPath));
readfile($absPath);