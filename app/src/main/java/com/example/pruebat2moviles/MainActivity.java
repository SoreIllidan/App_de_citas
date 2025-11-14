package com.example.pruebat2moviles;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button btnIrLogin, btnIrAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnIrLogin = findViewById(R.id.btnIrLogin);
        btnIrAdmin = findViewById(R.id.btnIrAdmin);

        btnIrLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, InicioActivity.class));
        });

        btnIrAdmin.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminActivity.class));
        });
    }
}
