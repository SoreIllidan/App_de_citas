package com.example.pruebat2moviles;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class RecuperarActivity extends AppCompatActivity {

    EditText etEmailRec;
    Button btnEnviar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Recuperar contraseña");
        }

        etEmailRec = findViewById(R.id.etEmailRec);
        btnEnviar = findViewById(R.id.btnEnviar);

        btnEnviar.setOnClickListener(v -> {
            String email = etEmailRec.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Ingrese su correo", Toast.LENGTH_SHORT).show();
                return;
            }

            // URL de tu API de recuperación
            String URL_RESET = "http://10.0.2.2/app_citas/solicitar_reset.php";

            // Petición VOLLEY
            StringRequest request = new StringRequest(Request.Method.POST, URL_RESET,
                    response -> {
                        if(response.contains("ok")){
                            Toast.makeText(this,
                                    "Se envió un código de recuperación a tu correo",
                                    Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(this, ValidarCodigoActivity.class);
                            intent.putExtra("correo", email); // pasamos el correo
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, response, Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> {
                        Toast.makeText(this, "Error de servidor", Toast.LENGTH_SHORT).show();
                    }
            ){
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("correo", email);  // <-- Lo que envías al backend
                    return params;
                }
            };

            // Inicializar queue Volley
            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(request);
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}