package com.example.pruebat2moviles;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class AdminGenerarHorariosActivity extends AppCompatActivity {

    private Spinner spMedicos;
    private EditText etFechaIni, etFechaFin, etHoraIni, etHoraFin, etIntervalo;
    private Button btnFechaIni, btnFechaFin, btnHoraIni, btnHoraFin, btnCrear;

    private final List<MedicoModel> medicos = new ArrayList<>();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_generar_horarios);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Generar horarios (L–V)");
        }

        spMedicos = findViewById(R.id.spMedicos);
        etFechaIni = findViewById(R.id.etFechaInicio);
        etFechaFin = findViewById(R.id.etFechaFin);
        etHoraIni = findViewById(R.id.etHoraInicio);
        etHoraFin = findViewById(R.id.etHoraFin);
        etIntervalo = findViewById(R.id.etIntervalo);

        btnFechaIni = findViewById(R.id.btnPickFechaInicio);
        btnFechaFin = findViewById(R.id.btnPickFechaFin);
        btnHoraIni = findViewById(R.id.btnPickHoraInicio);
        btnHoraFin = findViewById(R.id.btnPickHoraFin);
        btnCrear = findViewById(R.id.btnCrearHorarios);

        btnFechaIni.setOnClickListener(v -> pickFecha(etFechaIni));
        btnFechaFin.setOnClickListener(v -> pickFecha(etFechaFin));
        btnHoraIni.setOnClickListener(v -> pickHora(etHoraIni));
        btnHoraFin.setOnClickListener(v -> pickHora(etHoraFin));
        btnCrear.setOnClickListener(v -> crearHorarios());

        cargarMedicos();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void pickFecha(EditText target) {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dp = new DatePickerDialog(this, (view, y, m, d) -> {
            String mm = String.format(Locale.US, "%02d", m + 1);
            String dd = String.format(Locale.US, "%02d", d);
            target.setText(y + "-" + mm + "-" + dd);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dp.getDatePicker().setMinDate(System.currentTimeMillis()); // no días pasados
        dp.show();
    }

    private void pickHora(EditText target) {
        Calendar c = Calendar.getInstance();
        int h = c.get(Calendar.HOUR_OF_DAY);
        int m = c.get(Calendar.MINUTE);
        new TimePickerDialog(this, (view, hour, min) -> {
            String hh = String.format(Locale.US, "%02d", hour);
            String mm = String.format(Locale.US, "%02d", min);
            target.setText(hh + ":" + mm);
        }, h, m, true).show();
    }

    private void cargarMedicos() {
        String url = Constantes.LISTAR_MEDICOS + "?activos=1";
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                resp -> {
                    JSONArray arr = resp.optJSONArray("medicos");
                    medicos.clear();
                    if (arr != null) for (int i = 0; i < arr.length(); i++) medicos.add(MedicoModel.fromJson(arr.optJSONObject(i)));
                    ArrayAdapter<MedicoModel> ad = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, medicos);
                    spMedicos.setAdapter(ad);
                },
                err -> {
                    String detalle = "Error cargando médicos";
                    if (err.networkResponse != null) detalle = "HTTP " + err.networkResponse.statusCode;
                    Log.e("GEN_HORARIOS", detalle, err);
                    Toast.makeText(this, detalle, Toast.LENGTH_LONG).show();
                }
        );
        req.setRetryPolicy(new DefaultRetryPolicy(12000, 1, 1f));
        Volley.newRequestQueue(this).add(req);
    }

    private void crearHorarios() {
        if (medicos.isEmpty() || spMedicos.getSelectedItemPosition() < 0) {
            Toast.makeText(this, "Selecciona un médico", Toast.LENGTH_SHORT).show();
            return;
        }
        String fIni = etFechaIni.getText().toString().trim();
        String fFin = etFechaFin.getText().toString().trim();
        String hIni = etHoraIni.getText().toString().trim();
        String hFin = etHoraFin.getText().toString().trim();
        String inter = etIntervalo.getText().toString().trim();

        if (fIni.isEmpty() || fFin.isEmpty() || hIni.isEmpty() || hFin.isEmpty() || inter.isEmpty()) {
            Toast.makeText(this, "Completa rango de fechas/horas e intervalo", Toast.LENGTH_SHORT).show();
            return;
        }

        final int idMedico = medicos.get(spMedicos.getSelectedItemPosition()).id;

        StringRequest req = new StringRequest(Request.Method.POST, Constantes.CREAR_HORARIOS_RANGO,
                resp -> {
                    try {
                        JSONObject j = new JSONObject(resp);
                        if ("ok".equals(j.optString("estado"))) {
                            int n = j.optInt("insertados", 0);
                            Toast.makeText(this, "Horarios L–V creados: " + n, Toast.LENGTH_LONG).show();
                            if (n == 0) {
                                Toast.makeText(this, "Si elegiste fines de semana o hora_inicio >= hora_fin, no se crean slots.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(this, "No se pudieron crear horarios", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        if ("ok".equals(resp.trim())) {
                            Toast.makeText(this, "Horarios creados", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Respuesta inválida al crear horarios", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                err -> {
                    String detalle = "Error creando horarios";
                    if (err.networkResponse != null) {
                        String body = "";
                        try { body = new String(err.networkResponse.data, StandardCharsets.UTF_8); } catch (Exception ignored) {}
                        detalle = "HTTP " + err.networkResponse.statusCode + ": " + body;
                    }
                    Log.e("GEN_HORARIOS", detalle, err);
                    Toast.makeText(this, detalle, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("id_medico", String.valueOf(idMedico));
                p.put("fecha_inicio", fIni);
                p.put("fecha_fin", fFin);
                p.put("hora_inicio", hIni);
                p.put("hora_fin", hFin);
                p.put("intervalo", inter);
                return p;
            }
        };
        req.setRetryPolicy(new DefaultRetryPolicy(15000, 1, 1f));
        Volley.newRequestQueue(this).add(req);
    }
}