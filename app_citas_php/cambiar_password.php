<?php
include "conexion.php";

$correo = $_POST['correo'];
$pass = password_hash($_POST['pass'], PASSWORD_BCRYPT);

$query = $conexion->prepare("UPDATE paciente SET contrasena=?, reset_token=NULL, reset_expira=NULL WHERE correo=?");
$query->bind_param("ss", $pass, $correo);

if($query->execute()){
    echo "ok";
} else {
    echo "error";
}
?>