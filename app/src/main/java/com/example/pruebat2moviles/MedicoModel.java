package com.example.pruebat2moviles;

import org.json.JSONObject;

public class MedicoModel {
    public int id;
    public String nombre;
    public String especialidad;
    public String correo;
    public String telefono;
    public Integer activo;

    public static MedicoModel fromJson(JSONObject o) {
        MedicoModel m = new MedicoModel();
        m.id = o.optInt("id", 0);
        m.nombre = o.optString("nombre", "");
        m.especialidad = o.optString("especialidad", "");
        m.correo = o.optString("correo", "");
        m.telefono = o.optString("telefono", "");
        if (o.has("activo")) m.activo = o.optInt("activo", 1);
        return m;
    }

    @Override public String toString() {
        // Útil para Spinner: "Dr. Nombre (Especialidad)"
        String pref = nombre != null && !nombre.toLowerCase().startsWith("dr.") ? "Dr. " : "";
        return pref + (nombre == null ? "" : nombre) + (especialidad == null || especialidad.isEmpty() ? "" : (" (" + especialidad + ")"));
    }
}