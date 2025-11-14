<?php
header('Content-Type: application/json; charset=utf-8');

require_once __DIR__ . '/_db.php'; // $cn = new mysqli(...)

function jerr($msg){ echo json_encode(['estado'=>'error','mensaje'=>$msg]); exit; }

$cn->set_charset('utf8mb4');

/**
 * KPIs: total, confirmadas, canceladas
 * Tabla real: cita (id, id_paciente, id_medico, fecha, hora, consultorio, estado, ...)
 */
$q1 = "SELECT 
          COUNT(*) AS total,
          SUM(estado='confirmada') AS confirmadas,
          SUM(estado='cancelada') AS canceladas
       FROM cita";
$r1 = $cn->query($q1);
$kpis = $r1 ? $r1->fetch_assoc() : ['total'=>0,'confirmadas'=>0,'canceladas'=>0];

/**
 * Distribución por estado
 */
$q2 = "SELECT estado, COUNT(*) AS total
       FROM cita
       GROUP BY estado
       ORDER BY total DESC";
$r2 = $cn->query($q2);
$por_estado = [];
if ($r2) while ($row = $r2->fetch_assoc()) $por_estado[] = $row;

/**
 * Últimos 7 días (por fecha)
 * Usamos la columna fecha (DATE). Rellenamos días sin registros con 0.
 */
$q3 = "SELECT fecha, COUNT(*) AS total
       FROM cita
       WHERE fecha >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
       GROUP BY fecha
       ORDER BY fecha ASC";
$r3 = $cn->query($q3);
$map = [];
if ($r3) while ($row = $r3->fetch_assoc()) $map[$row['fecha']] = (int)$row['total'];

$ultimos_7 = [];
$today = new DateTime();
for ($i=6; $i>=0; $i--) {
  $d = (clone $today)->sub(new DateInterval("P{$i}D"))->format('Y-m-d');
  $ultimos_7[] = ['fecha'=>$d, 'total'=> ($map[$d] ?? 0)];
}

/**
 * Últimos 12 meses (por mes)
 */
$q4 = "SELECT DATE_FORMAT(fecha, '%Y-%m') AS mes, COUNT(*) AS total
       FROM cita
       WHERE fecha >= DATE_SUB(DATE_FORMAT(CURDATE(),'%Y-%m-01'), INTERVAL 11 MONTH)
       GROUP BY DATE_FORMAT(fecha, '%Y-%m')
       ORDER BY mes ASC";
$r4 = $cn->query($q4);
$mapm = [];
if ($r4) while ($row = $r4->fetch_assoc()) $mapm[$row['mes']] = (int)$row['total'];

$start = new DateTime(date('Y-m-01'));
$start->sub(new DateInterval('P11M'));
$ult_12 = [];
for ($i=0; $i<12; $i++) {
  $m = $start->format('Y-m');
  $ult_12[] = ['mes'=>$m, 'total'=> ($mapm[$m] ?? 0)];
  $start->add(new DateInterval('P1M'));
}

echo json_encode([
  'estado' => 'ok',
  'kpis' => [
    'total' => (int)$kpis['total'],
    'confirmadas' => (int)$kpis['confirmadas'],
    'canceladas' => (int)$kpis['canceladas'],
  ],
  'por_estado' => $por_estado,
  'ultimos_7_dias' => $ultimos_7,
  'ultimos_12_meses' => $ult_12
], JSON_UNESCAPED_UNICODE);