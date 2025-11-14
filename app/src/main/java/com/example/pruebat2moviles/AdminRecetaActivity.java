package com.example.pruebat2moviles;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;

public class AdminRecetaActivity extends AppCompatActivity {

    private LinearLayout contenedor;
    private Button btnAgregar, btnListo, btnCancelar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_receta);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Receta • Medicamentos");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        contenedor = findViewById(R.id.contenedorMedicamentos);
        btnAgregar = findViewById(R.id.btnAgregarMed);
        btnListo   = findViewById(R.id.btnListoReceta);
        btnCancelar= findViewById(R.id.btnCancelarReceta);

        // Primera fila por defecto
        agregarFila();

        btnAgregar.setOnClickListener(v -> agregarFila());
        btnCancelar.setOnClickListener(v -> { setResult(RESULT_CANCELED); finish(); });
        btnListo.setOnClickListener(v -> devolverJSON());
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }

    private void agregarFila() {
        View fila = getLayoutInflater().inflate(R.layout.item_receta_medicamento, contenedor, false);
        ImageButton btnDel = fila.findViewById(R.id.btnDel);
        btnDel.setOnClickListener(v -> contenedor.removeView(fila));
        contenedor.addView(fila);
    }

    private void devolverJSON() {
        JSONArray arr = new JSONArray();
        for (int i = 0; i < contenedor.getChildCount(); i++) {
            View fila = contenedor.getChildAt(i);
            EditText etNombre = fila.findViewById(R.id.etMedNombre);
            EditText etDosis  = fila.findViewById(R.id.etMedDosis);
            EditText etFreq   = fila.findViewById(R.id.etMedFrecuencia);
            EditText etDias   = fila.findViewById(R.id.etMedDuracion);
            EditText etInd    = fila.findViewById(R.id.etMedIndicaciones);

            String nombre = etNombre.getText().toString().trim();
            String dosis = etDosis.getText().toString().trim();
            String freqStr = etFreq.getText().toString().trim();
            String diasStr = etDias.getText().toString().trim();
            String ind = etInd.getText().toString().trim();

            if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(dosis) || TextUtils.isEmpty(freqStr)) {
                Toast.makeText(this, "Completa nombre, dosis y frecuencia", Toast.LENGTH_SHORT).show();
                return;
            }
            int freq = Integer.parseInt(freqStr);
            Integer dur = TextUtils.isEmpty(diasStr) ? null : Integer.parseInt(diasStr);

            JSONObject o = new JSONObject();
            try {
                o.put("nombre", nombre);
                o.put("dosis", dosis);
                o.put("frecuencia_horas", freq);
                if (dur != null) o.put("duracion_dias", dur);
                if (!TextUtils.isEmpty(ind)) o.put("indicaciones", ind);
                arr.put(o);
            } catch (Exception ignored) {}
        }

        Intent data = new Intent();
        data.putExtra("meds_json", arr.toString());
        setResult(RESULT_OK, data);
        finish();
    }
}