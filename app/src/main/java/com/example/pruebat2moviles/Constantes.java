package com.example.pruebat2moviles;

public class Constantes {

    // Base del backend (emulador) o reemplaza por la IP de tu PC en dispositivo físico
    public static final String BASE = "http://10.0.2.2/app_citas/";

    // PACIENTE - Autenticación y citas
    public static final String LOGIN_PACIENTE              = BASE + "login_paciente.php";
    public static final String REGISTRAR_PACIENTE          = BASE + "registrar_paciente.php";
    public static final String LISTAR_CITAS_PACIENTE       = BASE + "listar_citas_paciente.php";
    public static final String LISTAR_CITAS_PACIENTE_TODAS = BASE + "listar_citas_paciente_todas.php";
    public static final String LISTAR_CITAS                = BASE + "listar_citas.php";
    public static final String CANCELAR_CITA               = BASE + "cancelar_cita.php";
    public static final String REPROGRAMAR_CITA            = BASE + "reprogramar_cita.php";
    public static final String CONFIRMAR_CITA_PACIENTE     = BASE + "confirmar_cita_paciente.php";

    // ADMIN
    public static final String LISTAR_MEDICOS              = BASE + "listar_medicos.php";
    public static final String REGISTRAR_MEDICO            = BASE + "registrar_medico.php";
    public static final String LISTAR_PACIENTES            = BASE + "listar_pacientes.php";
    public static final String LISTAR_HORAS                = BASE + "listar_horarios_disponibles.php";
    public static final String CREAR_CITA                  = BASE + "crear_cita.php";
    public static final String CREAR_HORARIOS_RANGO        = BASE + "crear_horarios_rango.php";
    public static final String LISTAR_CITAS_MEDICO         = BASE + "listar_citas_medico.php";
    public static final String CONFIRMAR_CITA              = BASE + "confirmar_cita.php";

    // PERFIL
    public static final String OBTENER_PERFIL_PACIENTE     = BASE + "obtener_perfil_paciente.php";
    public static final String ACTUALIZAR_PERFIL_PACIENTE  = BASE + "actualizar_perfil_paciente.php";

    // DOCUMENTOS DE CITA
    public static final String SUBIR_DOC_CITA              = BASE + "upload_cita_documento.php";
    public static final String LISTAR_DOCS_PACIENTE        = BASE + "listar_documentos_paciente.php";
    public static final String LISTAR_DOCS_CITA            = BASE + "listar_documentos_cita.php";
    public static final String DESCARGAR_DOC               = BASE + "download_cita_documento.php";

    // Soporte / WhatsApp
    public static final String SOPORTE_WHATSAPP            = "51933905250";

    // Dashboard
    public static final String ESTADISTICAS_CITAS          = BASE + "estadisticas_citas.php";

    // Notificaciones push (si lo usas)
    public static final String REGISTRAR_TOKEN             = BASE + "registrar_token.php";

    // Recordatorios
    public static final String LISTAR_RECORDATORIOS        = BASE + "listar_recordatorios_paciente.php";
}