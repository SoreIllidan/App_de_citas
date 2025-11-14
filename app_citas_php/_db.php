<?php
// Ajusta estos datos a tu entorno
$host = '127.0.0.1';
$user = 'root';          // o tu usuario de BD
$pass = '';              // contraseña si configuraste
$db   = 'bd_citas_medicas';
$port = 3306;

$cn = @new mysqli($host, $user, $pass, $db, $port);
if ($cn->connect_errno) {
    http_response_code(500);
    die(json_encode(['estado'=>'error','mensaje'=>'BD no disponible']));
}