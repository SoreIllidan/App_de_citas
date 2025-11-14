package com.example.pruebat2moviles;

public class FaqItem {
    public final String pregunta;
    public final String respuesta;
    public boolean expandido;
    public FaqItem(String pregunta, String respuesta) {
        this.pregunta = pregunta;
        this.respuesta = respuesta;
        this.expandido = false;
    }
}