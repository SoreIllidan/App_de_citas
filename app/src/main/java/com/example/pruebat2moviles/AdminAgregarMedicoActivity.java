package com.example.pruebat2moviles;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class AdminAgregarMedicoActivity extends AppCompatActivity {

    EditText etNombre, etEsp, etCorreo, etTel;
    Button btnGuardar;

    private static final String TAG = "AGREGAR_MEDICO";

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_agregar_medico);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Añadir médico");
        }

        etNombre = findViewById(R.id.etNombreMedico);
        etEsp = findViewById(R.id.etEspecialidad);
        etCorreo = findViewById(R.id.etCorreoMedico);
        etTel = findViewById(R.id.etTelefonoMedico);
        btnGuardar = findViewById(R.id.btnGuardarMedico);

        btnGuardar.setOnClickListener(v -> guardar());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void guardar() {
        String nombre = etNombre.getText().toString().trim();
        String esp = etEsp.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String tel = etTel.getText().toString().trim();

        if (nombre.isEmpty() || esp.isEmpty()) {
            Toast.makeText(this, "Nombre y especialidad son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest req = new StringRequest(Request.Method.POST, Constantes.REGISTRAR_MEDICO,
                resp -> {
                    try {
                        if ("ok".equals(resp.trim())) {
                            Toast.makeText(this, "Médico agregado", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        JSONObject j = new JSONObject(resp);
                        if ("ok".equals(j.optString("estado"))) {
                            Toast.makeText(this, "Médico agregado", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String msj = j.optString("mensaje", "No se pudo guardar");
                            Toast.makeText(this, msj, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Respuesta no OK: " + resp);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Excepción parseando respuesta: " + resp, e);
                        Toast.makeText(this, "Respuesta inválida del servidor", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    String detalle = "Error de red";
                    if (error.networkResponse != null) {
                        int status = error.networkResponse.statusCode;
                        String body = "";
                        try { body = new String(error.networkResponse.data, StandardCharsets.UTF_8); } catch (Exception ignored) {}
                        detalle = "HTTP " + status + ": " + body;
                    }
                    Log.e(TAG, detalle, error);
                    Toast.makeText(this, detalle, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("nombre", nombre);
                p.put("especialidad", esp);
                p.put("correo", correo);
                p.put("telefono", tel);
                return p;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(this).add(req);
    }
}