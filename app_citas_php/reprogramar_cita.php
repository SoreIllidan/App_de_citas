<?php
header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/conexion.php';

$id_cita  = $_POST['id_cita']  ?? '';
$id_medico= $_POST['id_medico']?? '';
$fecha    = $_POST['fecha']    ?? '';
$hora     = $_POST['hora']     ?? '';

if ($id_cita==='' || !ctype_digit($id_cita) || $id_medico==='' || !ctype_digit($id_medico) || $fecha==='' || $hora==='') {
  echo json_encode(["estado"=>"error","mensaje"=>"Parámetros inválidos"]); exit;
}

// Verificar estado actual
$stmt = $conexion->prepare("SELECT estado, id_medico FROM cita WHERE id=?");
$stmt->bind_param("i", $id_cita);
$stmt->execute();
$row = $stmt->get_result()->fetch_assoc();
$stmt->close();

if (!$row) { echo json_encode(["estado"=>"error","mensaje"=>"Cita no encontrada"]); exit; }
if ((int)$row['id_medico'] !== (int)$id_medico) { echo json_encode(["estado"=>"error","mensaje"=>"Médico inválido"]); exit; }
if (strtolower($row['estado']) === 'cancelada') { echo json_encode(["estado"=>"error","mensaje"=>"La cita está cancelada"]); exit; }
if (strtolower($row['estado']) === 'confirmada') { echo json_encode(["estado"=>"error","mensaje"=>"No se puede reprogramar una cita confirmada"]); exit; }

// Normalizar hora a HH:MM:SS
if (strlen($hora) == 5) $hora .= ':00';

// Evitar solapamiento (opcional)
$dup = $conexion->prepare("SELECT 1 FROM cita WHERE id_medico=? AND fecha=? AND hora=? AND id<>? AND estado<>'cancelada' LIMIT 1");
$dup->bind_param("issi", $id_medico, $fecha, $hora, $id_cita);
$dup->execute();
$dup->store_result();
if ($dup->num_rows > 0) { echo json_encode(["estado"=>"error","mensaje"=>"Horario no disponible"]); exit; }
$dup->close();

// Reprogramar
$upd = $conexion->prepare("UPDATE cita SET fecha=?, hora=?, estado='reprogramada' WHERE id=?");
$upd->bind_param("ssi", $fecha, $hora, $id_cita);
$upd->execute();
$ok = $upd->affected_rows > 0;
$upd->close();

echo json_encode($ok ? ["estado"=>"ok"] : ["estado"=>"error","mensaje"=>"No se pudo reprogramar"]);