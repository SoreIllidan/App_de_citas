package com.example.pruebat2moviles;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.style.StrikethroughSpan;
import android.text.SpannableString;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class AdminCrearCitaActivity extends AppCompatActivity {

    Spinner spPacientes, spMedicos;
    Button btnFecha, btnVerHoras, btnCrear;
    TextView tvFecha, tvHora, etConsultorio;

    private final List<PacienteModel> pacientes = new ArrayList<>();
    private final List<MedicoModel> medicos = new ArrayList<>();
    private String fechaSel = null;
    private String horaSel = null;

    static class HourSlot {
        String hora; boolean disponible;
        HourSlot(String h, boolean d){ hora=h; disponible=d; }
    }
    private final List<HourSlot> horasSlots = new ArrayList<>();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_crear_cita);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Crear Cita");
        }

        spPacientes = findViewById(R.id.spPacientes);
        spMedicos = findViewById(R.id.spMedicos);
        btnFecha = findViewById(R.id.btnFecha);
        btnVerHoras = findViewById(R.id.btnVerHoras);
        btnCrear = findViewById(R.id.btnCrear);
        tvFecha = findViewById(R.id.tvFecha);
        tvHora = findViewById(R.id.tvHora);
        etConsultorio = findViewById(R.id.etConsultorio);

        cargarPacientes();
        cargarMedicos();

        btnFecha.setOnClickListener(v -> pickFecha());
        btnVerHoras.setOnClickListener(v -> cargarHoras());
        btnCrear.setOnClickListener(v -> crearCita());
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }

    private void cargarPacientes() {
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, Constantes.LISTAR_PACIENTES, null,
                resp -> {
                    JSONArray arr = resp.optJSONArray("pacientes");
                    pacientes.clear();
                    if (arr != null) for (int i = 0; i < arr.length(); i++) pacientes.add(PacienteModel.fromJson(arr.optJSONObject(i)));
                    ArrayAdapter<PacienteModel> ad = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, pacientes);
                    spPacientes.setAdapter(ad);
                },
                err -> Toast.makeText(this, "Error cargando pacientes", Toast.LENGTH_SHORT).show()
        );
        req.setRetryPolicy(new DefaultRetryPolicy(12000, 1, 1f));
        Volley.newRequestQueue(this).add(req);
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
                    Log.e("CREAR_CITA", detalle, err);
                    Toast.makeText(this, detalle, Toast.LENGTH_LONG).show();
                }
        );
        req.setRetryPolicy(new DefaultRetryPolicy(12000, 1, 1f));
        Volley.newRequestQueue(this).add(req);
    }
    private void pickFecha() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dp = new DatePickerDialog(this, (view, y, m, d) -> {
            String mm = String.format(Locale.US, "%02d", m + 1);
            String dd = String.format(Locale.US, "%02d", d);
            fechaSel = y + "-" + mm + "-" + dd;
            tvFecha.setText(fechaSel);
            horaSel = null;
            tvHora.setText("—");

            Calendar sel = Calendar.getInstance();
            sel.set(y, m, d, 0, 0, 0);
            int dow = sel.get(Calendar.DAY_OF_WEEK); // 1=Dom, 7=Sáb
            if (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY) {
                Toast.makeText(this, "Es fin de semana. El generador L–V no crea horarios para Sáb/Dom.", Toast.LENGTH_LONG).show();
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dp.getDatePicker().setMinDate(System.currentTimeMillis()); // no días pasados
        dp.show();
    }

    private void cargarHoras() {
        if (fechaSel == null) {
            Toast.makeText(this, "Elige una fecha", Toast.LENGTH_SHORT).show();
            return;
        }
        if (medicos.isEmpty() || spMedicos.getSelectedItemPosition() < 0) {
            Toast.makeText(this, "Selecciona un médico", Toast.LENGTH_SHORT).show();
            return;
        }
        int idMedico = medicos.get(spMedicos.getSelectedItemPosition()).id;
        String url = Constantes.LISTAR_HORAS + "?id_medico=" + idMedico + "&fecha=" + fechaSel;
        android.util.Log.d("CREAR_CITA", "GET " + url);

        com.android.volley.toolbox.JsonObjectRequest req = new com.android.volley.toolbox.JsonObjectRequest(
                com.android.volley.Request.Method.GET, url, null,
                resp -> {
                    if (!"ok".equals(resp.optString("estado"))) {
                        Toast.makeText(this, "Respuesta no válida al listar horas", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    org.json.JSONArray arr = resp.optJSONArray("horas");
                    if (arr == null || arr.length() == 0) {
                        Toast.makeText(this, "Sin horarios para esa fecha. Verifica que generaste L–V y la fecha sea entre el rango.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    horasSlots.clear();
                    for (int i = 0; i < arr.length(); i++) {
                        org.json.JSONObject o = arr.optJSONObject(i);
                        if (o != null) {
                            String h = o.optString("hora");
                            boolean disp = o.optInt("disponible", 0) == 1;
                            if (h != null && h.length() == 8) h = h.substring(0,5);
                            horasSlots.add(new HourSlot(h, disp));
                        } else {
                            String h = arr.optString(i, null);
                            if (h != null && h.length() == 8) h = h.substring(0,5);
                            if (h != null) horasSlots.add(new HourSlot(h, true));
                        }
                    }
                    mostrarDialogoHoras();
                },
                err -> {
                    String detalle = "Error cargando horas";
                    if (err.networkResponse != null) {
                        String body = "";
                        try { body = new String(err.networkResponse.data, java.nio.charset.StandardCharsets.UTF_8); } catch (Exception ignored) {}
                        detalle = "HTTP " + err.networkResponse.statusCode + ": " + body;
                    }
                    android.util.Log.e("CREAR_CITA", detalle, err);
                    Toast.makeText(this, detalle, Toast.LENGTH_LONG).show();
                }
        );
        req.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(12000, 1, 1f));
        com.android.volley.toolbox.Volley.newRequestQueue(this).add(req);
    }
    private void mostrarDialogoHoras() {
        ListAdapter adapter = new BaseAdapter() {
            @Override public int getCount() { return horasSlots.size(); }
            @Override public Object getItem(int i) { return horasSlots.get(i); }
            @Override public long getItemId(int i) { return i; }
            @Override public boolean areAllItemsEnabled() { return false; }
            @Override public boolean isEnabled(int position) { return horasSlots.get(position).disponible; }
            @Override public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                TextView tv = (TextView) (convertView != null ? convertView :
                        getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false));
                HourSlot s = horasSlots.get(position);
                String label = s.hora + (s.disponible ? "" : " (ocupado)");
                if (s.disponible) {
                    tv.setText(label);
                    tv.setEnabled(true);
                    tv.setPaintFlags(tv.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                } else {

                    SpannableString ss = new SpannableString(label);
                    ss.setSpan(new StrikethroughSpan(), 0, label.length(), 0);
                    tv.setText(ss);
                    tv.setEnabled(false);
                }
                return tv;
            }
        };

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Selecciona hora")
                .setAdapter(adapter, (dialog, which) -> {
                    HourSlot s = horasSlots.get(which);
                    if (!s.disponible) {
                        Toast.makeText(this, "Horario ocupado", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    horaSel = s.hora;
                    tvHora.setText(horaSel);
                })
                .show();
    }

    private void crearCita() {
        if (pacientes.isEmpty() || medicos.isEmpty() || fechaSel == null || horaSel == null) {
            Toast.makeText(this, "Completa paciente, médico, fecha y hora", Toast.LENGTH_SHORT).show();
            return;
        }
        final int idPacienteSel = pacientes.get(spPacientes.getSelectedItemPosition()).id;
        final int idMedicoSel = medicos.get(spMedicos.getSelectedItemPosition()).id;
        final String fechaSelLocal = fechaSel;
        final String horaSelLocal = horaSel;
        final String consultorioFinal = (etConsultorio != null) ? etConsultorio.getText().toString().trim() : "";

        StringRequest req = new StringRequest(Request.Method.POST, Constantes.CREAR_CITA,
                resp -> {
                    try {
                        JSONObject j = new JSONObject(resp);
                        if ("ok".equals(j.optString("estado"))) {
                            Toast.makeText(this, "Cita creada", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "No se pudo crear", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        if ("ok".equals(resp.trim())) {
                            Toast.makeText(this, "Cita creada", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Respuesta inválida", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                err -> {
                    String detalle = "Error creando cita";
                    if (err.networkResponse != null) {
                        String body = "";
                        try { body = new String(err.networkResponse.data, java.nio.charset.StandardCharsets.UTF_8); } catch (Exception ignored) {}
                        detalle = "HTTP " + err.networkResponse.statusCode + ": " + body;
                    }
                    Log.e("CREAR_CITA", detalle, err);
                    Toast.makeText(this, detalle, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("id_paciente", String.valueOf(idPacienteSel));
                p.put("id_medico", String.valueOf(idMedicoSel));
                p.put("fecha", fechaSelLocal);
                p.put("hora", horaSelLocal);
                p.put("consultorio", consultorioFinal);
                return p;
            }
        };
        req.setRetryPolicy(new DefaultRetryPolicy(12000, 1, 1f));
        Volley.newRequestQueue(this).add(req);
    }
}