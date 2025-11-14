<?php
header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/conexion.php';
require_once __DIR__ . '/send_fcm.php';

/**
 * Selecciona citas cuyo timestamp esté entre NOW()+24h y NOW()+25h
 * y que aún no tengan registro en cita_notif (tipo '24h').
 */
$sql = "
SELECT c.id, c.id_paciente, c.fecha, c.hora, m.nombre AS medico, m.especialidad
FROM cita c
JOIN medico m ON m.id = c.id_medico
LEFT JOIN cita_notif n ON n.id_cita = c.id AND n.tipo = '24h'
WHERE n.id IS NULL
  AND CONCAT(c.fecha, ' ', IF(LENGTH(c.hora)=5, CONCAT(c.hora, ':00'), c.hora)) 
      BETWEEN DATE_ADD(NOW(), INTERVAL 24 HOUR) AND DATE_ADD(NOW(), INTERVAL 25 HOUR)
  AND c.estado IN ('programada','reprogramada','confirmada')";

$res = $conexion->query($sql);
$pendientes = [];
while ($row = $res->fetch_assoc()) $pendientes[] = $row;

$enviadas = 0;
foreach ($pendientes as $cita) {
    $pid = (int)$cita['id_paciente'];

    // tokens del paciente
    $stmt = $conexion->prepare("SELECT token FROM paciente_token WHERE id_paciente=?");
    $stmt->bind_param("i", $pid);
    $stmt->execute();
    $rs = $stmt->get_result();
    $tokens = [];
    while ($r = $rs->fetch_assoc()) $tokens[] = $r['token'];
    $stmt->close();

    if (empty($tokens)) continue;

    $fecha = $cita['fecha'];
    $hora  = substr($cita['hora'], 0, 5);
    $med   = $cita['medico'];
    $title = "Recordatorio de cita";
    $body  = "Mañana a las $hora con Dr.(a) $med";
    $data  = ["tipo"=>"recordatorio_24h","id_cita"=>$cita['id']];

    $resPush = send_push($tokens, $title, $body, $data);
    if (($resPush['ok'] ?? 0) > 0) {
        $stmt = $conexion->prepare("INSERT INTO cita_notif (id_cita, tipo) VALUES (?, '24h')");
        $stmt->bind_param("i", $cita['id']);
        $stmt->execute();
        $stmt->close();
        $enviadas++;
    }
}

echo json_encode(["estado"=>"ok","citas_procesadas"=>count($pendientes),"enviadas"=>$enviadas]);