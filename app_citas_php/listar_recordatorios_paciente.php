<?php
header('Content-Type: application/json; charset=utf-8');
include 'conexion.php';

$id_paciente = $_GET['id_paciente'] ?? $_POST['id_paciente'] ?? '';
if ($id_paciente === '' || !ctype_digit($id_paciente)) {
  echo json_encode(["estado"=>"error","mensaje"=>"id_paciente inválido"]); exit();
}
$id_paciente = (int)$id_paciente;
$items = [];

/** 1) Recordatorios de medicamentos activos (no vencidos por duración, si se indicó) */
$sql = "
  SELECT nombre, dosis, frecuencia_horas, duracion_dias, indicaciones, created_at
  FROM receta_medicamento
  WHERE id_paciente = ?
  ORDER BY created_at DESC
";
$stmt = $conexion->prepare($sql);
$stmt->bind_param("i", $id_paciente);
$stmt->execute();
$res = $stmt->get_result();
$hoy = new DateTime('now');
while ($r = $res->fetch_assoc()) {
  // Si tiene duración, verificamos que no haya vencido
  $creado = new DateTime($r['created_at']);
  $vigente = true;
  if (!is_null($r['duracion_dias'])) {
    $fin = clone $creado;
    $fin->modify('+' . intval($r['duracion_dias']) . ' day');
    $vigente = ($fin >= $hoy);
  }
  if (!$vigente) continue;

  $titulo = "Tomar Medicamento";
  $sub = $r['nombre'] . ' ' . $r['dosis'] . ' - Cada ' . intval($r['frecuencia_horas']) . ' horas';
  if (!empty($r['indicaciones'])) $sub .= ' • ' . $r['indicaciones'];

  $items[] = [
    "tipo" => "medicamento",
    "titulo" => $titulo,
    "subtitulo" => $sub,
    "color" => "#F8D7DA" // rosado suave
  ];
}
$stmt->close();

/** 2) Cita próxima */
$sql = "
  SELECT c.fecha, c.hora, m.nombre AS medico
  FROM cita c
  JOIN medico m ON m.id = c.id_medico
  WHERE c.id_paciente = ?
    AND c.estado IN ('programada','reprogramada','confirmada')
    AND CONCAT(c.fecha, ' ', IF(LENGTH(c.hora)=5, CONCAT(c.hora, ':00'), c.hora)) > NOW()
  ORDER BY c.fecha ASC, c.hora ASC
  LIMIT 1
";
$stmt = $conexion->prepare($sql);
$stmt->bind_param("i", $id_paciente);
$stmt->execute();
$r = $stmt->get_result()->fetch_assoc();
$stmt->close();

if ($r) {
  $fecha = $r['fecha'];
  $hora  = $r['hora'];
  if (strlen($hora)==8) $hora = substr($hora,0,5);

  $dtCita = DateTime::createFromFormat('Y-m-d H:i', trim($fecha . ' ' . $hora));
  if ($dtCita) {
    $diff = (new DateTime('now'))->diff($dtCita);
    $en = $diff->days > 0 ? "En " . $diff->days . " días" : ("Hoy a las " . $hora);
    $items[] = [
      "tipo" => "cita",
      "titulo" => "Cita Próxima",
      "subtitulo" => "Dr.(a) " . ($r['medico'] ?? '') . " - " . $en,
      "color" => "#CFEAFD" // celeste suave
    ];
  }
}

echo json_encode(["estado"=>"ok", "recordatorios"=>$items], JSON_UNESCAPED_UNICODE);