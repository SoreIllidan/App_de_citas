<?php
header('Content-Type: application/json; charset=utf-8');
include 'conexion.php';

$id_paciente = $_GET['id_paciente'] ?? $_POST['id_paciente'] ?? '';
if ($id_paciente === '' || !ctype_digit($id_paciente)) {
  echo json_encode(["estado"=>"error","mensaje"=>"id_paciente inválido"]); exit();
}

$sql = "
SELECT d.id, d.id_cita, d.tipo, d.titulo, d.descripcion, d.file_name, d.file_path, d.file_size, d.mime_type, d.created_at,
       c.fecha AS fecha_cita, c.hora, m.nombre AS medico, m.especialidad
FROM cita_documento d
JOIN cita c ON c.id = d.id_cita
JOIN medico m ON m.id = d.id_medico
WHERE d.id_paciente = ?
ORDER BY d.created_at DESC
";
$stmt = $conexion->prepare($sql);
$stmt->bind_param("i", $id_paciente);
$stmt->execute();
$res = $stmt->get_result();

$base = (isset($_SERVER['REQUEST_SCHEME']) ? $_SERVER['REQUEST_SCHEME'] : 'http') . '://' . $_SERVER['HTTP_HOST'] .
        rtrim(dirname($_SERVER['SCRIPT_NAME']), '/\\') . '/';

$data = [];
while ($r = $res->fetch_assoc()) {
  if (!empty($r['hora']) && strlen($r['hora']) == 8) $r['hora'] = substr($r['hora'],0,5);
  $r['url'] = $base . $r['file_path'];
  $r['tamano_kb'] = $r['file_size'] ? round($r['file_size']/1024) : null;
  $data[] = $r;
}
echo json_encode(["estado"=>"ok","documentos"=>$data]);
$stmt->close();
$conexion->close();