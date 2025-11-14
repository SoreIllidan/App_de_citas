<?php
header('Content-Type: application/json; charset=utf-8');
include 'conexion.php';

$id_paciente = $_POST['id_paciente'] ?? '';
if ($id_paciente === '' || !ctype_digit($id_paciente)) {
    echo json_encode(["estado"=>"error","mensaje"=>"id_paciente inválido"]);
    exit();
}

$editable = [
    'correo','telefono','fecha_nacimiento','ocupacion',
    'emergencia_nombre','emergencia_relacion','emergencia_telefono',
    'tipo_sangre','alergias','historial_medico','aseguradora','poliza','direccion'
];

$campos = [];
$vals = [];
$types = '';

foreach ($editable as $campo) {
    if (!array_key_exists($campo, $_POST)) continue; // sólo si fue enviado
    $valor = trim($_POST[$campo]);

    // Validaciones puntuales
    if ($campo === 'correo') {
        if ($valor === '' || !filter_var($valor, FILTER_VALIDATE_EMAIL)) {
            echo json_encode(["estado"=>"error","mensaje"=>"Correo inválido"]);
            exit();
        }
        // Unicidad de correo
        $chk = $conexion->prepare("SELECT id FROM paciente WHERE correo=? AND id<>?");
        $chk->bind_param("si", $valor, $id_paciente);
        $chk->execute();
        $chk->store_result();
        if ($chk->num_rows > 0) {
            echo json_encode(["estado"=>"error","mensaje"=>"Correo ya registrado"]);
            $chk->close();
            exit();
        }
        $chk->close();
    }

    if ($campo === 'fecha_nacimiento' && $valor !== '') {
        $dt = DateTime::createFromFormat('Y-m-d', $valor);
        if (!$dt || $dt->format('Y-m-d') !== $valor) {
            echo json_encode(["estado"=>"error","mensaje"=>"Fecha nacimiento inválida"]);
            exit();
        }
    }

    // Permitir limpiar campo (si cadena vacía -> guardar NULL)
    if ($valor === '') {
        $campos[] = "$campo = NULL";
    } else {
        $campos[] = "$campo = ?";
        $vals[] = $valor;
        $types .= 's';
    }
}

if (empty($campos)) {
    echo json_encode(["estado"=>"sin_cambios"]);
    exit();
}

$sql = "UPDATE paciente SET ".implode(", ", $campos)." WHERE id = ?";
$types .= 'i';
$vals[] = (int)$id_paciente;

$stmt = $conexion->prepare($sql);
$stmt->bind_param($types, ...$vals);
$stmt->execute();

echo json_encode(["estado" => $stmt->affected_rows > 0 ? "ok" : "sin_cambios"]);
$stmt->close();
$conexion->close();