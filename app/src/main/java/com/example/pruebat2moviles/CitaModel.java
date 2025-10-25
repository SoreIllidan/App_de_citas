package com.example.pruebat2moviles;

import org.json.JSONObject;

public class CitaModel {
    private String medico;
    private String especialidad;
    private String fecha;       // YYYY-MM-DD
    private String consultorio;

    private int id;
    private int idMedico;
    private String hora;        // HH:MM ya normalizada
    private String estado;      // creada/programada/reprogramada/cancelada

    public CitaModel(String medico, String especialidad, String fecha, String consultorio) {
        this.medico = medico;
        this.especialidad = especialidad;
        this.fecha = fecha;
        this.consultorio = consultorio;
    }

    public static CitaModel fromJson(JSONObject o) {
        CitaModel c = new CitaModel(
                o.optString("medico", "—"),
                o.optString("especialidad", ""),
                o.optString("fecha", ""),
                o.optString("consultorio", "")
        );
        c.id = o.optInt("id", 0);
        c.idMedico = o.optInt("id_medico", 0);
        String h = o.optString("hora", "");
        if (h != null && h.length() == 8) h = h.substring(0, 5);
        c.hora = h;
        c.estado = o.optString("estado", "creada");
        return c;
    }

    public String getMedico() { return medico; }
    public String getEspecialidad() { return especialidad; }
    public String getConsultorio() { return consultorio; }
    public int getId() { return id; }
    public int getIdMedico() { return idMedico; }
    public String getHora() { return hora; }
    public String getEstado() { return estado; }


    public String getFecha() { return (hora != null && !hora.isEmpty()) ? (fecha + " - " + hora) : fecha; }


    public String getFechaISO() { return fecha; }
    public String getHora24() { return (hora == null || hora.isEmpty()) ? "00:00" : hora; } // HH:MM

    public void setFecha(String fecha) { this.fecha = fecha; }
    public void setHora(String hora) { this.hora = hora; }
    public void setEstado(String estado) { this.estado = estado; }
}