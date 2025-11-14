<?php
// Cargar PHPMailer
use SendinBlue\Client\Api\TransactionalEmailsApi;
use SendinBlue\Client\Configuration;
use GuzzleHttp\Client;
require 'vendor/autoload.php';

include "conexion.php";

// Función para registrar errores en archivo
function registrarError($mensaje) {
    $archivo = __DIR__ . "/error_mail.txt"; // crea en la misma carpeta del script
    $fecha = date("Y-m-d H:i:s");
    file_put_contents($archivo, "[$fecha] $mensaje\n", FILE_APPEND);
}

$correo = $_POST['correo'] ?? '';

if (empty($correo)) {
    registrarError("Correo vacío recibido");
    echo json_encode(["estado"=>"no_existe"]);
    exit();
}

// 1. Verificar si el correo existe
$stmt = $conexion->prepare("SELECT id FROM paciente WHERE correo = ?");
$stmt->bind_param("s", $correo);
$stmt->execute();
$res = $stmt->get_result();

if ($res->num_rows == 0) {
    registrarError("Correo no registrado: $correo");
    echo json_encode(["estado"=>"no_existe"]);
    exit();
}

// 2. Generar token y fecha de expiración
$token = random_int(100000, 999999); // código de 6 dígitos
$expira = date("Y-m-d H:i:s", strtotime("+15 minutes"));

// Guardar token en BD
$stmt = $conexion->prepare("UPDATE paciente SET reset_token=?, reset_expira=? WHERE correo=?");
$stmt->bind_param("sss", $token, $expira, $correo);
$stmt->execute();

// -------- Enviar correo mediante API Brevo ----------
function enviarTokenCorreo($emailDestino, $token) {
    try {
        // Importar librería Brevo
        $config = SendinBlue\Client\Configuration::getDefaultConfiguration()
            

        $apiInstance = new SendinBlue\Client\Api\TransactionalEmailsApi(
            new GuzzleHttp\Client(),
            $config
        );

        // Armar email
        $sendSmtpEmail = new \SendinBlue\Client\Model\SendSmtpEmail([
            'subject' => 'Recuperación de contraseña',
            'sender' => ['name' => 'app_citas', 'email' => 'bandimar2@gmail.com'],
            'to' => [['email' => $emailDestino]],
            'htmlContent' => "
                <p>Hola,</p>
                <p>Tu código de recuperación es:</p>
                <h1 style='color:blue;'>$token</h1>
                <p>Vence en 15 minutos.</p>
            ",
        ]);

        // Enviar
        $result = $apiInstance->sendTransacEmail($sendSmtpEmail);
        return true;

    } catch (Exception $e) {
        registrarError("Error API Brevo: " . $e->getMessage());
        return false;
    }
}


// Intentar enviar correo
if (enviarTokenCorreo($correo, $token)) {
    echo json_encode(["estado"=>"ok"]);
} else {
    registrarError("Falló enviar correo a: $correo");
    echo json_encode(["estado"=>"error_correo"]);
}


