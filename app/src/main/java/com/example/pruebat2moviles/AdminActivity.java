package com.example.pruebat2moviles;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;

public class AdminActivity extends AppCompatActivity {
    TextView tvAdminTitulo;
    Button btnAdminMedicos, btnAdminPacientes, btnAdminCrearCita;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        tvAdminTitulo = findViewById(R.id.tvAdminTitulo);
        btnAdminMedicos = findViewById(R.id.btnAdminMedicos);
        btnAdminPacientes = findViewById(R.id.btnAdminPacientes);
        btnAdminCrearCita = findViewById(R.id.btnAdminCrearCita);

        tvAdminTitulo.setText("Panel administrador");

        btnAdminMedicos.setOnClickListener(v ->
                startActivity(new Intent(this, AdminMedicosActivity.class)));

        btnAdminPacientes.setOnClickListener(v ->
                startActivity(new Intent(this, AdminPacientesActivity.class)));

        btnAdminCrearCita.setOnClickListener(v ->
                startActivity(new Intent(this, AdminCrearCitaActivity.class)));
    }
}