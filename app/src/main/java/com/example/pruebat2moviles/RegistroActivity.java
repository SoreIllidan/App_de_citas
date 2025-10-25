package com.example.pruebat2moviles;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.*;

import com.android.volley.*;
import com.android.volley.toolbox.*;

import java.util.HashMap;
import java.util.Map;

public class RegistroActivity extends AppCompatActivity {

    EditText etNombre, etDniR, etCorreo, etPassR;
    Button btnRegistrar;

    private static final String URL_REGISTRO = "http://10.0.2.2/app_citas/registrar_paciente.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Registro");
        }

        etNombre = findViewById(R.id.etNombre);
        etDniR = findViewById(R.id.etDniR);
        etCorreo = findViewById(R.id.etCorreo);
        etPassR = findViewById(R.id.etPassR);
        btnRegistrar = findViewById(R.id.btnRegistrar);

        btnRegistrar.setOnClickListener(v -> registrarPaciente());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void registrarPaciente() {
        String nombre = etNombre.getText().toString().trim();
        String dni = etDniR.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String pass = etPassR.getText().toString().trim();

        if (nombre.isEmpty() || dni.isEmpty() || correo.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(Request.Method.POST, URL_REGISTRO,
                response -> {
                    Log.d("VOLLEY_RESPONSE", "Respuesta del servidor: " + response);
                    if (response.trim().equals("ok")) {
                        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                        finish();
                    } else if (response.trim().equals("existe")) {
                        Toast.makeText(this, "El DNI ya está registrado", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error al registrar: " + response, Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    String errorMsg = error.toString();
                    Log.e("VOLLEY_ERROR", "Error de registro: " + errorMsg);
                    Toast.makeText(this, "Volley Error: " + errorMsg, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("nombre", nombre);
                params.put("dni", dni);
                params.put("correo", correo);
                params.put("contrasena", pass);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}