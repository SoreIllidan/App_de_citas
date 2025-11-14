<?php
header('Content-Type: application/json; charset=utf-8');
include 'conexion.php';

$id_medico = $_GET['id_medico'] ?? $_POST['id_medico'] ?? '';
if ($id_medico === '' || !ctype_digit($id_medico)) {
    echo json_encode(["estado" => "error", "mensaje" => "id_medico inválido"]);
    exit();
}

$sql = "
  SELECT
    c.id, c.id_paciente, c.id_medico, c.fecha, c.hora, c.consultorio, c.estado, c.confirmed_at,
    p.nombre AS paciente, p.dni, p.correo
  FROM cita c
  JOIN paciente p ON p.id = c.id_paciente
  WHERE c.id_medico = ?
  ORDER BY c.fecha DESC, c.hora DESC
";

$stmt = $conexion->prepare($sql);
$stmt->bind_param("i", $id_medico);
$stmt->execute();
$res = $stmt->get_result();

$ahora = new DateTime('now');
$hoy = $ahora->format('Y-m-d');

$citas = [];
while ($r = $res->fetch_assoc()) {
    $hora = $r['hora'];
    if (!empty($hora) && strlen($hora) == 8) $hora = substr($hora, 0, 5);
    $r['hora'] = $hora;

    $r['id'] = (int)$r['id'];
    $r['id_paciente'] = (int)$r['id_paciente'];
    $r['id_medico'] = (int)$r['id_medico'];

    // Cálculo de banderas
    $fechaHoraStr = trim(($r['fecha'] ?? '').' '.($r['hora'] ?? '00:00'));
    $dt = DateTime::createFromFormat('Y-m-d H:i', $fechaHoraStr) ?: null;

    $es_pasada = 0;
    $es_hoy = 0;
    if ($dt) {
        $es_pasada = ($dt < $ahora) ? 1 : 0;
        $es_hoy = ((substr($fechaHoraStr, 0, 10) === $hoy) ? 1 : 0);
    }
    $confirmada = (strtolower($r['estado']) === 'confirmada') ? 1 : 0;
    $por_confirmar = (in_array(strtolower($r['estado']), ['programada','reprogramada']) && !$es_pasada) ? 1 : 0;

    $r['es_pasada'] = $es_pasada;
    $r['es_hoy'] = $es_hoy;
    $r['confirmada'] = $confirmada;
    $r['por_confirmar'] = $por_confirmar;

    $citas[] = $r;
}
echo json_encode(["estado" => "ok", "citas" => $citas]);

$stmt->close();
$conexion->close();