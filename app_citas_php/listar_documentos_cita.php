<?php
header('Content-Type: application/json; charset=utf-8');
include 'conexion.php';

$id_cita = $_GET['id_cita'] ?? $_POST['id_cita'] ?? '';
if ($id_cita === '' || !ctype_digit($id_cita)) {
  echo json_encode(["estado"=>"error","mensaje"=>"id_cita inválido"]); exit();
}

$sql = "
SELECT d.id, d.id_cita, d.tipo, d.titulo, d.descripcion, d.file_name, d.file_path, d.file_size, d.mime_type, d.created_at
FROM cita_documento d
WHERE d.id_cita = ?
ORDER BY d.created_at DESC
";
$stmt = $conexion->prepare($sql);
$stmt->bind_param("i", $id_cita);
$stmt->execute();
$res = $stmt->get_result();

$base = (isset($_SERVER['REQUEST_SCHEME']) ? $_SERVER['REQUEST_SCHEME'] : 'http') . '://' . $_SERVER['HTTP_HOST'] .
        rtrim(dirname($_SERVER['SCRIPT_NAME']), '/\\') . '/';

$list = [];
while ($r = $res->fetch_assoc()) {
  $r['url'] = $base . $r['file_path'];
  $r['tamano_kb'] = $r['file_size'] ? round($r['file_size']/1024) : null;
  $list[] = $r;
}
echo json_encode(["estado"=>"ok","documentos"=>$list]);
$stmt->close();
$conexion->close();