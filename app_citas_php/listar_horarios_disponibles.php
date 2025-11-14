<?php
header('Content-Type: application/json; charset=utf-8');
include 'conexion.php';

$id_medico = $_GET['id_medico'] ?? '';
$fecha = $_GET['fecha'] ?? '';

if (!ctype_digit($id_medico)) {
    echo json_encode(["estado" => "error", "mensaje" => "id_medico inválido"]);
    exit();
}
$dt = DateTime::createFromFormat('Y-m-d', $fecha);
if (!$dt || $dt->format('Y-m-d') !== $fecha) {
    echo json_encode(["estado" => "error", "mensaje" => "Fecha inválida"]);
    exit();
}

try {
    // Trae todos los slots generados para ese día, con su bandera disponible (1 libre, 0 ocupado)
    $stmt = $conexion->prepare("SELECT hora, disponible FROM horario_medico WHERE id_medico = ? AND fecha = ? ORDER BY hora ASC");
    $stmt->bind_param("is", $id_medico, $fecha);
    $stmt->execute();
    $res = $stmt->get_result();

    $horas = [];
    while ($r = $res->fetch_assoc()) {
        // Normaliza a HH:MM
        $hora = $r['hora'];
        if (strlen($hora) == 8) $hora = substr($hora, 0, 5);
        $horas[] = [
            "hora" => $hora,
            "disponible" => intval($r['disponible'])
        ];
    }

    echo json_encode(["estado" => "ok", "horas" => $horas]);
    $stmt->close();
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(["estado" => "error", "mensaje" => "No se pudieron listar horarios"]);
} finally {
    $conexion->close();
}