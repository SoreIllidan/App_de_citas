<?php
header('Content-Type: application/json; charset=utf-8');
include 'conexion.php';
require_once __DIR__ . '/send_fcm.php';

// Espera: POST id_cita, tipo (diagnostico|descanso|receta), titulo, descripcion, archivo (multipart), (opcional) meds_json
$id_cita = $_POST['id_cita'] ?? '';
$tipo    = strtolower(trim($_POST['tipo'] ?? ''));
$titulo  = $_POST['titulo'] ?? null;
$desc    = $_POST['descripcion'] ?? null;
$medsJson= $_POST['meds_json'] ?? null;

$tiposValidos = ['diagnostico','descanso','receta'];
if ($id_cita === '' || !ctype_digit($id_cita)) { echo json_encode(["estado"=>"error", "mensaje"=>"id_cita inválido"]); exit; }
if (!in_array($tipo, $tiposValidos, true)) { echo json_encode(["estado"=>"error", "mensaje"=>"tipo inválido"]); exit; }
if (!isset($_FILES['archivo']) || $_FILES['archivo']['error'] !== UPLOAD_ERR_OK) { echo json_encode(["estado"=>"error", "mensaje"=>"Archivo no recibido"]); exit; }

$stmt = $conexion->prepare("SELECT id_paciente, id_medico, fecha, hora FROM cita WHERE id=?");
$stmt->bind_param("i", $id_cita);
$stmt->execute();
$cita = $stmt->get_result()->fetch_assoc();
$stmt->close();
if (!$cita) { echo json_encode(["estado"=>"no_encontrada"]); exit; }

$id_paciente = (int)$cita['id_paciente'];
$id_medico   = (int)$cita['id_medico'];

$maxBytes = 10 * 1024 * 1024;
if ((int)$_FILES['archivo']['size'] > $maxBytes) { echo json_encode(["estado"=>"error","mensaje"=>"Archivo demasiado grande (máx 10MB)"]); exit; }
$finfo = new finfo(FILEINFO_MIME_TYPE);
$mime  = $finfo->file($_FILES['archivo']['tmp_name']);
$extOk = strtolower(pathinfo($_FILES['archivo']['name'], PATHINFO_EXTENSION)) === 'pdf';
$mimeOk = in_array($mime, ['application/pdf','application/x-pdf','application/acrobat','applications/pdf'], true);
if (!$extOk || !$mimeOk) { echo json_encode(["estado"=>"error","mensaje"=>"Solo se acepta PDF"]); exit; }

$relDir = 'uploads/documentos/' . $id_cita;
$absDir = __DIR__ . '/' . $relDir;
if (!is_dir($absDir)) { @mkdir($absDir, 0775, true); }

$ts    = date('Ymd_His');
$rand  = substr(md5(random_int(1, PHP_INT_MAX)), 0, 6);
$fname = "doc_{$tipo}_{$ts}_{$rand}.pdf";
$relPath = $relDir . '/' . $fname;
$absPath = $absDir . '/' . $fname;
if (!move_uploaded_file($_FILES['archivo']['tmp_name'], $absPath)) { echo json_encode(["estado"=>"error","mensaje"=>"No se pudo guardar el archivo"]); exit; }

$stmt = $conexion->prepare("
  INSERT INTO cita_documento (id_cita, id_paciente, id_medico, tipo, titulo, descripcion, file_name, file_path, file_size, mime_type)
  VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
");
$size = (int)$_FILES['archivo']['size'];
$stmt->bind_param("iiisssssis", $id_cita, $id_paciente, $id_medico, $tipo, $titulo, $desc, $fname, $relPath, $size, $mime);
$stmt->execute();
$docId = $conexion->insert_id;
$stmt->close();

$base = (isset($_SERVER['REQUEST_SCHEME']) ? $_SERVER['REQUEST_SCHEME'] : 'http') . '://' . $_SERVER['HTTP_HOST'] .
        rtrim(dirname($_SERVER['SCRIPT_NAME']), '/\\') . '/';
$url  = $base . $relPath;

/** Guardar medicamentos si es receta */
if ($tipo === 'receta' && $medsJson) {
  $arr = json_decode($medsJson, true);
  if (is_array($arr)) {
    $ins = $conexion->prepare("INSERT INTO receta_medicamento (id_cita, id_paciente, nombre, dosis, frecuencia_horas, duracion_dias, indicaciones) VALUES (?,?,?,?,?,?,?)");
    foreach ($arr as $m) {
      $nombre = trim($m['nombre'] ?? '');
      $dosis  = trim($m['dosis'] ?? '');
      $freq   = intval($m['frecuencia_horas'] ?? 0);
      $dur    = isset($m['duracion_dias']) ? intval($m['duracion_dias']) : null;
      $ind    = isset($m['indicaciones']) ? trim($m['indicaciones']) : null;
      if ($nombre !== '' && $dosis !== '' && $freq > 0) {
        $ins->bind_param("iississ", $id_cita, $id_paciente, $nombre, $dosis, $freq, $dur, $ind);
        $ins->execute();
      }
    }
    $ins->close();
  }
}

/** Notificar al paciente (opcional, si configuraste FCM) */
try {
  $tk = $conexion->prepare("SELECT token FROM paciente_token WHERE id_paciente=?");
  $tk->bind_param("i", $id_paciente);
  $tk->execute();
  $rs = $tk->get_result();
  $tokens = [];
  while ($row = $rs->fetch_assoc()) $tokens[] = $row['token'];
  $tk->close();

  if (!empty($tokens)) {
    $fechaCita = $cita['fecha'] ?? '';
    $horaCita  = $cita['hora'] ?? '';
    if (strlen($horaCita) === 8) $horaCita = substr($horaCita, 0, 5);
    $title = "Resultado disponible";
    $body  = $tipo === 'receta'
      ? "Se cargó una receta médica para tu cita del $fechaCita $horaCita"
      : "Se cargó un documento para tu cita del $fechaCita $horaCita";
    $data  = ["tipo"=>"resultado","id_cita"=>$id_cita,"url"=>$url];
    @send_push($tokens, $title, $body, $data);
  }
} catch (Exception $e) {}

echo json_encode([
  "estado"=>"ok",
  "documento"=>[
    "id"=>$docId,
    "id_cita"=>(int)$id_cita,
    "id_paciente"=>$id_paciente,
    "id_medico"=>$id_medico,
    "tipo"=>$tipo,
    "titulo"=>$titulo,
    "descripcion"=>$desc,
    "file_name"=>$fname,
    "file_size"=>$size,
    "mime_type"=>$mime,
    "url"=>$url
  ]
]);