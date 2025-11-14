package com.example.pruebat2moviles;

import org.json.JSONObject;

public class PerfilPacienteModel {
    public int id;
    public String dni;
    public String nombre;
    public String correo;
    public String telefono;
    public String fechaNacimiento;
    public String ocupacion;
    public String emergenciaNombre;
    public String emergenciaRelacion;
    public String emergenciaTelefono;
    public String tipoSangre;
    public String alergias;
    public String historialMedico;
    public String aseguradora;
    public String poliza;
    public String direccion;

    public static PerfilPacienteModel fromJson(JSONObject o) {
        PerfilPacienteModel p = new PerfilPacienteModel();
        p.id = o.optInt("id", 0);
        p.dni = o.optString("dni","");
        p.nombre = o.optString("nombre","");
        p.correo = o.optString("correo","");
        p.telefono = o.optString("telefono","");
        p.fechaNacimiento = o.optString("fecha_nacimiento","");
        p.ocupacion = o.optString("ocupacion","");
        p.emergenciaNombre = o.optString("emergencia_nombre","");
        p.emergenciaRelacion = o.optString("emergencia_relacion","");
        p.emergenciaTelefono = o.optString("emergencia_telefono","");
        p.tipoSangre = o.optString("tipo_sangre","");
        p.alergias = o.optString("alergias","");
        p.historialMedico = o.optString("historial_medico","");
        p.aseguradora = o.optString("aseguradora","");
        p.poliza = o.optString("poliza","");
        p.direccion = o.optString("direccion","");
        return p;
    }
}