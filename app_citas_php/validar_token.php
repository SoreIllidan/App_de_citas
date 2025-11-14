<?php
include "conexion.php";

$correo = $_POST['correo'];
$token = $_POST['token'];

$query = $conexion->prepare("SELECT reset_token, reset_expira FROM paciente WHERE correo=?");
$query->bind_param("s", $correo);
$query->execute();
$res = $query->get_result();

if($res->num_rows > 0){
    $row = $res->fetch_assoc();

    if($row['reset_token'] == $token && $row['reset_expira'] > date("Y-m-d H:i:s")){
        echo "valido";
    } else {
        echo "invalido";
    }

} else {
    echo "no_existe";
}
?>
