package com.example.pruebat2moviles;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

public class BottomNavHelper {

    public enum Tab { HOME, CALENDAR, DOCS, USER }

    public static void attach(Activity a, Tab current) {
        ImageView home = a.findViewById(R.id.btnNavHome);
        ImageView cal  = a.findViewById(R.id.btnNavCalendar);
        ImageView docs = a.findViewById(R.id.btnNavDocs);
        ImageView user = a.findViewById(R.id.btnNavUser);

        if (home == null || cal == null || docs == null || user == null) {
            // El layout no contiene el dock (nada que enganchar)
            return;
        }

        // Resalta el icono actual bajando la opacidad de los demás
        float on = 1f, off = 0.55f;
        home.setAlpha(current == Tab.HOME ? on : off);
        cal.setAlpha(current == Tab.CALENDAR ? on : off);
        docs.setAlpha(current == Tab.DOCS ? on : off);
        user.setAlpha(current == Tab.USER ? on : off);

        home.setOnClickListener(v -> {
            if (current != Tab.HOME) {
                a.startActivity(new Intent(a, PrincipalPacienteActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            }
        });
        cal.setOnClickListener(v -> {
            if (current != Tab.CALENDAR) {
                a.startActivity(new Intent(a, MisCitasActivity.class));
            }
        });
        docs.setOnClickListener(v -> {
            if (current != Tab.DOCS) {
                a.startActivity(new Intent(a, DocumentosPacienteActivity.class));
            }
        });
        user.setOnClickListener(v -> {
            if (current != Tab.USER) {
                a.startActivity(new Intent(a, PerfilPacienteActivity.class));
            }
        });
    }
}