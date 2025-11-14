<?php
header('Content-Type: application/json; charset=utf-8');
include 'conexion.php';

$id_medico = $_POST['id_medico'] ?? '';
$fecha_inicio = $_POST['fecha_inicio'] ?? '';
$fecha_fin = $_POST['fecha_fin'] ?? '';
$hora_inicio = $_POST['hora_inicio'] ?? '';
$hora_fin = $_POST['hora_fin'] ?? '';
$intervalo = $_POST['intervalo'] ?? '';

if (!ctype_digit($id_medico)) {
    echo json_encode(["estado" => "error", "mensaje" => "id_medico inválido"]);
    exit();
}
$dtIni = DateTime::createFromFormat('Y-m-d', $fecha_inicio);
$dtFin = DateTime::createFromFormat('Y-m-d', $fecha_fin);
if (!$dtIni || !$dtFin || $dtIni > $dtFin) {
    echo json_encode(["estado" => "error", "mensaje" => "Rango de fechas inválido"]);
    exit();
}
if (!preg_match('/^\d{2}:\d{2}(:\d{2})?$/', $hora_inicio) || !preg_match('/^\d{2}:\d{2}(:\d{2})?$/', $hora_fin)) {
    echo json_encode(["estado" => "error", "mensaje" => "Formato de hora inválido"]);
    exit();
}
$intervalo = intval($intervalo);
if ($intervalo <= 0 || $intervalo > 240) {
    echo json_encode(["estado" => "error", "mensaje" => "Intervalo inválido"]);
    exit();
}
// Normaliza horas a HH:MM:SS
if (strlen($hora_inicio) == 5) $hora_inicio .= ':00';
if (strlen($hora_fin) == 5) $hora_fin .= ':00';

try {
    $insertados = 0;
    $stmt = $conexion->prepare("INSERT IGNORE INTO horario_medico (id_medico, fecha, hora, disponible) VALUES (?, ?, ?, 1)");

    $dia = clone $dtIni;
    while ($dia <= $dtFin) {
        // Lunes(1) a Viernes(5)
        $dow = intval($dia->format('N'));
        if ($dow >= 1 && $dow <= 5) {
            $hIni = DateTime::createFromFormat('H:i:s', $hora_inicio);
            $hFin = DateTime::createFromFormat('H:i:s', $hora_fin);
            if ($hIni && $hFin && $hIni < $hFin) {
                $cursor = clone $hIni;
                while ($cursor < $hFin) {
                    $fechaDia = $dia->format('Y-m-d');
                    $horaSlot = $cursor->format('H:i:s');
                    $stmt->bind_param("iss", $id_medico, $fechaDia, $horaSlot);
                    $stmt->execute();
                    if ($stmt->affected_rows > 0) $insertados++;
                    $cursor->modify("+{$intervalo} minutes");
                }
            }
        }
        $dia->modify('+1 day');
    }

    echo json_encode(["estado" => "ok", "insertados" => $insertados]);
    $stmt->close();
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(["estado" => "error", "mensaje" => "No se pudieron crear horarios"]);
} finally {
    $conexion->close();
}