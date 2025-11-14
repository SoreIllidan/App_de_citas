package com.example.pruebat2moviles;

import org.json.JSONObject;

public class CitaModel {
    private int id;
    private int idMedico;
    private String medico;
    private String estado;
    private String fecha;      // visual
    private String hora;       // visual
    private String fechaISO;   // yyyy-MM-dd
    private String hora24;     // HH:mm

    // NUEVOS
    private String especialidad;
    private String especialidadNombre;
    private String servicio;
    private String consultorio;
    private String consultorioNombre;
    private String piso;
    private String lugar;

    public static CitaModel fromJson(JSONObject o) {
        CitaModel c = new CitaModel();
        c.id        = o.optInt("id", 0);
        c.idMedico  = o.optInt("id_medico", 0);
        c.medico    = o.optString("medico", o.optString("doctor", ""));
        c.estado    = o.optString("estado", "");

        c.fecha     = o.optString("fecha_str", o.optString("fecha", ""));
        c.hora      = o.optString("hora_str",  o.optString("hora", ""));
        c.fechaISO  = o.optString("fecha", "");
        c.hora24    = o.optString("hora", "");

        // Posibles nombres de campo según backend
        c.especialidad       = o.optString("especialidad", "");
        c.especialidadNombre = o.optString("especialidad_nombre", o.optString("especialidadName",""));
        c.servicio           = o.optString("servicio", "");

        c.consultorio        = o.optString("consultorio", "");
        c.consultorioNombre  = o.optString("consultorio_nombre", "");
        c.piso               = o.optString("piso", o.optString("piso_texto",""));
        c.lugar              = o.optString("lugar", o.optString("ubicacion",""));

        return c;
    }

    public int getId() { return id; }
    public int getIdMedico() { return idMedico; }
    public String getMedico() { return medico; }
    public String getEstado() { return estado; }
    public String getFecha() { return fecha; }
    public String getHora() { return hora; }
    public String getFechaISO() { return fechaISO == null || fechaISO.isEmpty() ? fecha : fechaISO; }
    public String getHora24() { return hora24 == null || hora24.isEmpty() ? hora : (hora24.length()==5 ? hora24 + ":00" : hora24); }

    // NUEVOS getters
    public String getEspecialidad() { return especialidad; }
    public String getEspecialidadNombre() { return especialidadNombre; }
    public String getServicio() { return servicio; }
    public String getConsultorio() { return consultorio; }
    public String getConsultorioNombre() { return consultorioNombre; }
    public String getPiso() { return piso; }
    public String getLugar() { return lugar; }

    // setters usados en reprogramación
    public void setFecha(String f) { this.fecha = f; this.fechaISO = f; }
    public void setHora(String h) { this.hora = h; this.hora24 = h; }
    public void setEstado(String e) { this.estado = e; }
}