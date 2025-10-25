package com.example.pruebat2moviles;

public class Constantes {
    public static final String BASE = "http://10.0.2.2/app_citas/";

    // PACIENTE
    public static final String LOGIN_PACIENTE = BASE + "login_paciente.php";
    public static final String REGISTRAR_PACIENTE = BASE + "registrar_paciente.php";
    public static final String LISTAR_CITAS_PACIENTE = BASE + "listar_citas_paciente.php";
    public static final String LISTAR_CITAS = BASE + "listar_citas.php";
    public static final String CANCELAR_CITA = BASE + "cancelar_cita.php";
    public static final String REPROGRAMAR_CITA = BASE + "reprogramar_cita.php";

    // ADMIN
    public static final String LISTAR_MEDICOS = BASE + "listar_medicos.php";             // GET ?activos=1
    public static final String REGISTRAR_MEDICO = BASE + "registrar_medico.php";
    public static final String LISTAR_PACIENTES = BASE + "listar_pacientes.php";
    public static final String LISTAR_HORAS = BASE + "listar_horarios_disponibles.php";  // GET ?id_medico=&fecha=
    public static final String CREAR_CITA = BASE + "crear_cita.php";
    public static final String CREAR_HORARIOS_RANGO = BASE + "crear_horarios_rango.php"; // NUEVO
}