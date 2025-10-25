package com.example.pruebat2moviles;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class CambiarPasswordActivity extends AppCompatActivity {

    EditText etPass1, etPass2;
    Button btnCambiar;
    String correo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambiar_password);

        etPass1 = findViewById(R.id.etPass1);
        etPass2 = findViewById(R.id.etPass2);
        btnCambiar = findViewById(R.id.btnCambiar);

        correo = getIntent().getStringExtra("correo");

        btnCambiar.setOnClickListener(v -> {
            String p1 = etPass1.getText().toString();
            String p2 = etPass2.getText().toString();

            if(!p1.equals(p2)){
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            String URL_CAMBIAR = "http://10.0.2.2/app_citas/cambiar_password.php";

            StringRequest request = new StringRequest(Request.Method.POST, URL_CAMBIAR, response -> {
                if(response.contains("ok")){
                    Toast.makeText(this, "Contraseña actualizada", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(this, InicioActivity.class); // <--- tu pantalla de login
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Error al actualizar", Toast.LENGTH_LONG).show();
                }
            }, error -> Toast.makeText(this, "Error de servidor", Toast.LENGTH_SHORT).show())
            {
                @Override
                protected Map<String, String> getParams(){
                    Map<String, String> params = new HashMap<>();
                    params.put("correo", correo);
                    params.put("pass", p1);
                    return params;
                }
            };

            Volley.newRequestQueue(this).add(request);
        });
    }
}

