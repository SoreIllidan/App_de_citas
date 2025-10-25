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

public class ValidarCodigoActivity extends AppCompatActivity {

    EditText etCodigo;
    Button btnValidar;
    String correo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validar_codigo);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Validar código");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etCodigo = findViewById(R.id.etCodigo);
        btnValidar = findViewById(R.id.btnValidar);

        correo = getIntent().getStringExtra("correo");

        btnValidar.setOnClickListener(v -> {
            String codigo = etCodigo.getText().toString().trim();

            if(codigo.isEmpty()){
                Toast.makeText(this, "Ingrese el código", Toast.LENGTH_SHORT).show();
                return;
            }

            String URL_VALIDAR = "http://10.0.2.2/app_citas/validar_token.php";

            StringRequest request = new StringRequest(Request.Method.POST, URL_VALIDAR, response -> {

                if(response.contains("valido")){
                    Intent intent = new Intent(this, CambiarPasswordActivity.class);
                    intent.putExtra("correo", correo);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Código incorrecto o vencido", Toast.LENGTH_SHORT).show();
                }

            }, error -> Toast.makeText(this, "Error de servidor", Toast.LENGTH_SHORT).show()){
                @Override
                protected Map<String, String> getParams(){
                    Map<String, String> params = new HashMap<>();
                    params.put("correo", correo);
                    params.put("token", codigo);
                    return params;
                }
            };

            Volley.newRequestQueue(this).add(request);
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

