package com.example.pruebat2moviles;

import org.json.JSONObject;

public class PacienteModel {
    public int id;
    public String dni;
    public String nombre;
    public String correo;

    public static PacienteModel fromJson(JSONObject o) {
        PacienteModel p = new PacienteModel();
        p.id = o.optInt("id", 0);
        p.dni = o.optString("dni", "");
        p.nombre = o.optString("nombre", "");
        p.correo = o.optString("correo", "");
        return p;
    }

    @Override public String toString() {
        // Útil para Spinner: "Nombre (DNI 12345678)"
        return (nombre == null ? "" : nombre) + (dni == null || dni.isEmpty() ? "" : (" (DNI " + dni + ")"));
    }
}