package com.example.pruebat2moviles;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

public class AdminActivity extends AppCompatActivity {

    private TextView tvAdminTitulo;
    private Button btnAdminMedicos, btnAdminPacientes, btnAdminCrearCita, btnAdminDashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Panel administrador");
        }

        tvAdminTitulo = findViewById(R.id.tvAdminTitulo);
        btnAdminMedicos = findViewById(R.id.btnAdminMedicos);
        btnAdminPacientes = findViewById(R.id.btnAdminPacientes);
        btnAdminCrearCita = findViewById(R.id.btnAdminCrearCita);
        btnAdminDashboard = findViewById(R.id.btnDashboard); // NUEVO (opcional en el layout)

        if (tvAdminTitulo != null) {
            tvAdminTitulo.setText("Panel administrador");
        }

        if (btnAdminMedicos != null) {
            btnAdminMedicos.setOnClickListener(v ->
                    startActivity(new Intent(this, AdminMedicosActivity.class)));
        }

        if (btnAdminPacientes != null) {
            btnAdminPacientes.setOnClickListener(v ->
                    startActivity(new Intent(this, AdminPacientesActivity.class)));
        }

        if (btnAdminCrearCita != null) {
            btnAdminCrearCita.setOnClickListener(v ->
                    startActivity(new Intent(this, AdminCrearCitaActivity.class)));
        }

        // Abrir Dashboard (si agregaste el botón en el layout)
        if (btnAdminDashboard != null) {
            btnAdminDashboard.setOnClickListener(v ->
                    startActivity(new Intent(this, AdminDashboardActivity.class)));
        }
    }

    // Alternativa: Menú de overflow para abrir el Dashboard aunque no exista el botón en el layout
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_dashboard) {
            startActivity(new Intent(this, AdminDashboardActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}