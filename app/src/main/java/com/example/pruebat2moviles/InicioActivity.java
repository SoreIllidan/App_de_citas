package com.example.pruebat2moviles;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.*;
import com.android.volley.*;
import com.android.volley.toolbox.*;

import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class InicioActivity extends AppCompatActivity {

    EditText etDni, etPass;
    Button btnEntrar;
    TextView tvRegistro, tvOlvide;

    private static final String URL_LOGIN = "http://10.0.2.2/app_citas/login_paciente.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // flecha atrás
            getSupportActionBar().setTitle("Iniciar sesión");
        }

        etDni = findViewById(R.id.etDni);
        etPass = findViewById(R.id.etPass);
        btnEntrar = findViewById(R.id.btnEntrar);
        tvRegistro = findViewById(R.id.tvRegistro);
        tvOlvide = findViewById(R.id.tvOlvide);

        btnEntrar.setOnClickListener(v -> autenticarUsuario());
        tvRegistro.setOnClickListener(v -> startActivity(new Intent(this, RegistroActivity.class)));
        tvOlvide.setOnClickListener(v -> startActivity(new Intent(this, RecuperarActivity.class)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void autenticarUsuario() {
        String dni = etDni.getText().toString().trim();
        String contrasena = etPass.getText().toString().trim();

        if (dni.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(Request.Method.POST, URL_LOGIN,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        String estado = json.optString("estado", "");
                        if (estado.equals("ok")) {
                            int id = json.optInt("id", 0);
                            String nombre = json.optString("nombre", "");
                            String correo = json.optString("correo", "");
                            if (id == 0) {
                                Toast.makeText(this, "Falta 'id' en la respuesta del login. Actualiza el PHP.", Toast.LENGTH_LONG).show();
                                return;
                            }
                            SharedPreferences sp = getSharedPreferences("session", MODE_PRIVATE);
                            sp.edit()
                                    .putInt("id", id)
                                    .putString("nombre", nombre)
                                    .putString("correo", correo)
                                    .apply();

                            startActivity(new Intent(this, PrincipalPacienteActivity.class));
                            finish();
                        } else if (estado.equals("contrasena_incorrecta")) {
                            Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error al procesar respuesta", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error de conexión con el servidor", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("dni", dni);
                params.put("contrasena", contrasena);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}