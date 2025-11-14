package com.example.pruebat2moviles;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class AyudaActivity extends AppCompatActivity {

    private androidx.recyclerview.widget.RecyclerView rvFaq;
    private FaqAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ayuda);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        findViewById(R.id.btnWhatsApp).setOnClickListener(v -> abrirWhatsApp());

        rvFaq = findViewById(R.id.rvFaq);
        rvFaq.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FaqAdapter(crearFaqs());
        rvFaq.setAdapter(adapter);
    }

    private void abrirWhatsApp() {
        String phone = Constantes.SOPORTE_WHATSAPP; // Ej: "51999999999"
        String text = Uri.encode("Hola, necesito soporte con mi cuenta de CitaPlus.");
        String url = "https://wa.me/" + phone + "?text=" + text;
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        try {
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show();
        }
    }

    private List<FaqItem> crearFaqs() {
        List<FaqItem> list = new ArrayList<>();
        list.add(new FaqItem("No puedo iniciar sesión",
                "Verifica tu correo y contraseña, tu conexión a internet y si olvidaste la contraseña contáctanos por WhatsApp."));
        list.add(new FaqItem("¿Cómo confirmo mi asistencia a una cita?",
                "Desde la pantalla principal, pulsa Confirmar en tu próxima cita."));
        list.add(new FaqItem("¿Cómo reprogramo una cita?",
                "Pulsa Reprogramar, elige fecha y luego una hora disponible."));
        list.add(new FaqItem("¿Cómo cancelo una cita?",
                "Pulsa Cancelar y confirma la acción."));
        list.add(new FaqItem("No veo todas mis citas",
                "Usa el ícono Calendario en la barra inferior para ver la lista completa."));
        list.add(new FaqItem("¿Dónde veo mis documentos?",
                "Toca el ícono Documentos en la barra inferior."));
        list.add(new FaqItem("No se abre un PDF",
                "Instala o actualiza un visor de PDF o descarga el archivo y ábrelo con tu app de PDF."));
        list.add(new FaqItem("Compartir documentos",
                "Desde Documentos puedes compartir el enlace del PDF."));
        list.add(new FaqItem("Actualizar mis datos",
                "En Perfil edita los campos y guarda. Si un dato está bloqueado, contáctanos por WhatsApp."));
        list.add(new FaqItem("Estados de cita",
                "Programada, Confirmada, Reprogramada, Completada, Cancelada."));
        return list;
    }
}