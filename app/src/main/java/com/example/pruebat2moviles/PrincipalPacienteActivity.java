package com.example.pruebat2moviles;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PrincipalPacienteActivity extends AppCompatActivity {

    TextView tvSaludo, tvMedico, tvFecha, tvEstadoPrincipal, tvTituloLista;
    Button btnVerCitas, btnReprogramar, btnCancelar;

    private CitaModel citaActual;
    private androidx.recyclerview.widget.RecyclerView rvCitas;
    private AdaptadorCitas adaptador;
    private final List<CitaModel> lista = new ArrayList<>();

    private static final String URL_LISTAR_CITAS_TODAS = "http://10.0.2.2/app_citas/listar_citas_paciente_todas.php";
    private static final String URL_CANCELAR_CITA = "http://10.0.2.2/app_citas/cancelar_cita.php";
    private static final String URL_REPROGRAMAR_CITA = "http://10.0.2.2/app_citas/reprogramar_cita.php";
    private static final String URL_LISTAR_HORAS = "http://10.0.2.2/app_citas/listar_horarios_disponibles.php";

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal_paciente);

        tvSaludo = findViewById(R.id.tvSaludo);
        tvMedico = findViewById(R.id.tvMedico);
        tvFecha = findViewById(R.id.tvFecha);
        tvEstadoPrincipal = findViewById(R.id.tvEstadoPrincipal);
        tvTituloLista = findViewById(R.id.tvTituloLista);
        btnVerCitas = findViewById(R.id.btnVerCitas);
        btnReprogramar = findViewById(R.id.btnReprogramar);
        btnCancelar = findViewById(R.id.btnCancelar);

        rvCitas = findViewById(R.id.rvCitasPaciente);
        rvCitas.setLayoutManager(new LinearLayoutManager(this));
        adaptador = new AdaptadorCitas(lista, new AdaptadorCitas.OnCitaActionListener() {
            @Override public void onReprogramar(CitaModel c) { reprogramar(c); }
            @Override public void onCancelar(CitaModel c) { cancelar(c); }
        });
        rvCitas.setAdapter(adaptador);

        String nombre = getSharedPreferences("session", MODE_PRIVATE).getString("nombre", null);
        tvSaludo.setText(nombre != null ? "¡Hola, " + nombre + "!" : "¡Hola!");

        btnVerCitas.setOnClickListener(v -> startActivity(new android.content.Intent(this, MisCitasActivity.class)));
        btnReprogramar.setOnClickListener(v -> { if (citaActual != null) reprogramar(citaActual); });
        btnCancelar.setOnClickListener(v -> { if (citaActual != null) cancelar(citaActual); });

        cargarTodasCitas();
    }

    private int getIdPaciente() {
        return getSharedPreferences("session", MODE_PRIVATE).getInt("id", 0);
    }

    private void cargarTodasCitas() {
        int idPaciente = getIdPaciente();
        if (idPaciente == 0) { finish(); return; }

        String url = "http://10.0.2.2/app_citas/listar_citas_paciente_todas.php?id_paciente=" + idPaciente;
        android.util.Log.d("PACIENTE", "GET " + url);

        com.android.volley.toolbox.JsonObjectRequest req = new com.android.volley.toolbox.JsonObjectRequest(
                com.android.volley.Request.Method.GET, url, null,
                resp -> {
                    try {
                        if (!"ok".equals(resp.optString("estado"))) {
                            renderSinCitaCabecera();
                            lista.clear();
                            adaptador.notifyDataSetChanged();
                            android.widget.Toast.makeText(this, "Respuesta no OK: " + resp.toString(), android.widget.Toast.LENGTH_LONG).show();
                            return;
                        }
                        org.json.JSONArray arr = resp.optJSONArray("citas");
                        lista.clear();
                        if (arr != null) {
                            for (int i = 0; i < arr.length(); i++) {
                                lista.add(CitaModel.fromJson(arr.optJSONObject(i)));
                            }
                        }
                        adaptador.notifyDataSetChanged();

                        citaActual = seleccionarProxima(lista);
                        if (citaActual == null) renderSinCitaCabecera();
                        else renderCitaCabecera();
                    } catch (Exception e) {
                        renderSinCitaCabecera();
                        android.widget.Toast.makeText(this, "Error parseando JSON: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                    }
                },
                err -> {
                    String detalle = "Error al cargar citas";
                    if (err.networkResponse != null) {
                        String body = "";
                        try { body = new String(err.networkResponse.data, java.nio.charset.StandardCharsets.UTF_8); } catch (Exception ignored) {}
                        detalle = "HTTP " + err.networkResponse.statusCode + ": " + body;
                    } else if (err.getCause() != null) {
                        detalle = err.getClass().getSimpleName() + ": " + err.getCause().getMessage();
                    } else if (err.getMessage() != null) {
                        detalle = err.getMessage();
                    }
                    android.util.Log.e("PACIENTE", detalle, err);
                    android.widget.Toast.makeText(this, detalle, android.widget.Toast.LENGTH_LONG).show();
                    renderSinCitaCabecera();
                }
        );
        req.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(15000, 1, 1f));
        com.android.volley.toolbox.Volley.newRequestQueue(this).add(req);
    }

    private Date toDate(CitaModel c) {
        try {
            return sdf.parse(c.getFechaISO() + " " + c.getHora24());
        } catch (ParseException e) {
            return null;
        }
    }

    private CitaModel seleccionarProxima(List<CitaModel> citas) {
        Date ahora = new Date();
        List<CitaModel> candidatas = new ArrayList<>();
        for (CitaModel c : citas) {
            String est = c.getEstado() == null ? "" : c.getEstado().toLowerCase(Locale.US);
            if ("cancelada".equals(est)) continue;
            Date d = toDate(c);
            if (d != null && d.after(ahora)) candidatas.add(c);
        }
        if (candidatas.isEmpty()) return null;
        candidatas.sort(Comparator.comparing(this::toDate, Comparator.nullsLast(Comparator.naturalOrder())));
        return candidatas.get(0);
    }

    private void renderSinCitaCabecera() {
        tvEstadoPrincipal.setVisibility(android.view.View.GONE);
        tvMedico.setText("—");
        tvFecha.setText("Sin próxima cita");
        tvMedico.setPaintFlags(tvMedico.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        tvFecha.setPaintFlags(tvFecha.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        tvMedico.setTextColor(Color.BLACK);
        tvFecha.setTextColor(Color.BLACK);
        btnReprogramar.setEnabled(false);
        btnCancelar.setEnabled(false);
        btnReprogramar.setAlpha(0.5f);
        btnCancelar.setAlpha(0.5f);
    }

    private void renderCitaCabecera() {
        if (citaActual == null) { renderSinCitaCabecera(); return; }
        tvMedico.setText(citaActual.getMedico());
        tvFecha.setText(citaActual.getFecha());
        tvEstadoPrincipal.setVisibility(android.view.View.GONE);
        tvMedico.setPaintFlags(tvMedico.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        tvFecha.setPaintFlags(tvFecha.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        tvMedico.setTextColor(Color.BLACK);
        tvFecha.setTextColor(Color.BLACK);
        btnReprogramar.setEnabled(true);
        btnCancelar.setEnabled(true);
        btnReprogramar.setAlpha(1f);
        btnCancelar.setAlpha(1f);
    }


    private void cancelar(CitaModel c) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cancelar")
                .setMessage("¿Cancelar la cita seleccionada?")
                .setPositiveButton("Sí", (d, w) -> {
                    StringRequest req = new StringRequest(Request.Method.POST, URL_CANCELAR_CITA,
                            resp -> {
                                boolean ok = "ok".equals(resp.trim());
                                if (!ok) { try { ok = "ok".equals(new JSONObject(resp).optString("estado")); } catch (Exception ignored) {} }
                                if (ok) {
                                    c.setEstado("cancelada");
                                    adaptador.notifyDataSetChanged();
                                    if (citaActual != null && citaActual.getId() == c.getId()) renderSinCitaCabecera();
                                    Toast.makeText(this, "Cita cancelada", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "No se pudo cancelar", Toast.LENGTH_SHORT).show();
                                }
                            },
                            err -> Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show()
                    ) {
                        @Override protected Map<String, String> getParams() {
                            Map<String, String> p = new HashMap<>();
                            p.put("id_cita", String.valueOf(c.getId()));
                            return p;
                        }
                    };
                    com.android.volley.toolbox.Volley.newRequestQueue(this).add(req);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void reprogramar(CitaModel c) {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dp = new DatePickerDialog(this, (view, y, m, d) -> {
            String mm = String.format(Locale.US, "%02d", m + 1);
            String dd = String.format(Locale.US, "%02d", d);
            String nuevaFecha = y + "-" + mm + "-" + dd;

            int idMedico = c.getIdMedico();
            String url = URL_LISTAR_HORAS + "?id_medico=" + idMedico + "&fecha=" + nuevaFecha;

            JsonObjectRequest horasReq = new JsonObjectRequest(Request.Method.GET, url, null,
                    jr -> {
                        if (!"ok".equals(jr.optString("estado"))) { Toast.makeText(this, "No hay horarios", Toast.LENGTH_SHORT).show(); return; }
                        JSONArray horas = jr.optJSONArray("horas");
                        if (horas == null || horas.length() == 0) { Toast.makeText(this, "Sin horarios para esa fecha", Toast.LENGTH_SHORT).show(); return; }

                        List<String> libres = new ArrayList<>();
                        for (int i = 0; i < horas.length(); i++) {
                            JSONObject o = horas.optJSONObject(i);
                            if (o != null && o.optInt("disponible", 0) == 1) {
                                String h = o.optString("hora");
                                if (h != null && h.length() == 8) h = h.substring(0,5);
                                libres.add(h);
                            } else if (o == null) {
                                String h2 = horas.optString(i, null);
                                if (h2 != null) libres.add(h2.length()==8 ? h2.substring(0,5) : h2);
                            }
                        }
                        if (libres.isEmpty()) { Toast.makeText(this, "Sin horarios libres", Toast.LENGTH_SHORT).show(); return; }

                        String[] arr = libres.toArray(new String[0]);
                        new androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("Selecciona hora")
                                .setItems(arr, (dialog, which) -> {
                                    String horaSel = arr[which];
                                    StringRequest req = new StringRequest(Request.Method.POST, URL_REPROGRAMAR_CITA,
                                            resp -> {
                                                boolean ok;
                                                try { ok = "ok".equals(new JSONObject(resp).optString("estado")); }
                                                catch (Exception e) { ok = "ok".equals(resp.trim()); }
                                                if (ok) {
                                                    c.setFecha(nuevaFecha);
                                                    c.setHora(horaSel);
                                                    c.setEstado("reprogramada");
                                                    adaptador.notifyDataSetChanged();
                                                    if (citaActual != null && citaActual.getId() == c.getId()) {
                                                        citaActual = c;
                                                        renderCitaCabecera();
                                                    }
                                                    Toast.makeText(this, "Cita reprogramada", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(this, "No se pudo reprogramar", Toast.LENGTH_SHORT).show();
                                                }
                                            },
                                            err -> Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show()
                                    ) {
                                        @Override protected Map<String, String> getParams() {
                                            Map<String, String> p = new HashMap<>();
                                            p.put("id_cita", String.valueOf(c.getId()));
                                            p.put("id_medico", String.valueOf(idMedico));
                                            p.put("fecha", nuevaFecha);
                                            p.put("hora", horaSel);
                                            return p;
                                        }
                                    };
                                    com.android.volley.toolbox.Volley.newRequestQueue(this).add(req);
                                })
                                .show();
                    },
                    err -> Toast.makeText(this, "Error al cargar horas", Toast.LENGTH_SHORT).show()
            );
            com.android.volley.toolbox.Volley.newRequestQueue(this).add(horasReq);

        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dp.getDatePicker().setMinDate(System.currentTimeMillis());
        dp.show();
    }
}