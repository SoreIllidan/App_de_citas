package com.example.pruebat2moviles;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class MisCitasActivity extends AppCompatActivity {

    RecyclerView recycler;
    AdaptadorCitas adaptador;
    List<CitaModel> lista = new ArrayList<>();

    private static final String URL_LISTAR_CITAS = "http://10.0.2.2/app_citas/listar_citas.php";
    private static final String URL_CANCELAR_CITA = "http://10.0.2.2/app_citas/cancelar_cita.php";
    private static final String URL_REPROGRAMAR_CITA = "http://10.0.2.2/app_citas/reprogramar_cita.php";
    private static final String URL_LISTAR_HORAS = "http://10.0.2.2/app_citas/listar_horarios_disponibles.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_citas);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mis citas");
        }

        recycler = findViewById(R.id.recyclerCitas);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adaptador = new AdaptadorCitas(lista, new AdaptadorCitas.OnCitaActionListener() {
            @Override public void onReprogramar(CitaModel c) { reprogramar(c); }
            @Override public void onCancelar(CitaModel c) { cancelar(c); }
        });
        recycler.setAdapter(adaptador);

        cargarCitas();
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }

    private int getIdPaciente() {
        SharedPreferences sp = getSharedPreferences("session", MODE_PRIVATE);
        return sp.getInt("id", 0);
    }

    private void cargarCitas() {
        int idPaciente = getIdPaciente();
        if (idPaciente == 0) {
            Toast.makeText(this, "Sesión no válida. Inicia sesión nuevamente.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        StringRequest req = new StringRequest(Request.Method.POST, URL_LISTAR_CITAS,
                resp -> {
                    try {
                        JSONObject obj = new JSONObject(resp);
                        if (!"ok".equals(obj.optString("estado"))) {
                            lista.clear();
                            adaptador.notifyDataSetChanged();
                            return;
                        }
                        JSONArray arr = obj.optJSONArray("citas");
                        lista.clear();
                        if (arr != null) {
                            for (int i = 0; i < arr.length(); i++) {
                                lista.add(CitaModel.fromJson(arr.getJSONObject(i)));
                            }
                        }
                        adaptador.notifyDataSetChanged();
                    } catch (Exception e) {
                        Toast.makeText(this, "Error al parsear citas", Toast.LENGTH_SHORT).show();
                    }
                },
                err -> Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show()
        ) {
            @Override protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("id_paciente", String.valueOf(idPaciente));
                return p;
            }
        };

        com.android.volley.toolbox.Volley.newRequestQueue(this).add(req);
    }

    private void cancelar(CitaModel c) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cancelar")
                .setMessage("¿Cancelar la cita seleccionada?")
                .setPositiveButton("Sí", (d, w) -> {
                    StringRequest req = new StringRequest(Request.Method.POST, URL_CANCELAR_CITA,
                            resp -> {
                                boolean ok = "ok".equals(resp.trim());
                                if (!ok) {
                                    try { ok = "ok".equals(new JSONObject(resp).optString("estado")); } catch (Exception ignored) {}
                                }
                                if (ok) {
                                    // Actualiza estado en la lista (tachado rojo)
                                    c.setEstado("cancelada");
                                    adaptador.notifyDataSetChanged();
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
                        if (!"ok".equals(jr.optString("estado"))) {
                            Toast.makeText(this, "No hay horarios disponibles", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        JSONArray horas = jr.optJSONArray("horas");
                        if (horas == null || horas.length() == 0) {
                            Toast.makeText(this, "Sin horarios para esa fecha", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        List<String> listaHoras = new ArrayList<>();
                        for (int i = 0; i < horas.length(); i++) {
                            JSONObject o = horas.optJSONObject(i);
                            if (o != null) {
                                String h = o.optString("hora");
                                boolean disp = o.optInt("disponible", 0) == 1;
                                if (h != null && h.length() == 8) h = h.substring(0,5);
                                if (disp) listaHoras.add(h);
                            } else {
                                String h2 = horas.optString(i, null);
                                if (h2 != null) listaHoras.add(h2.length()==8 ? h2.substring(0,5) : h2);
                            }
                        }
                        if (listaHoras.isEmpty()) {
                            Toast.makeText(this, "Sin horarios libres ese día", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String[] arr = listaHoras.toArray(new String[0]);

                        new androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("Selecciona hora")
                                .setItems(arr, (dialog, which) -> {
                                    String horaSel = arr[which];

                                    StringRequest req = new StringRequest(Request.Method.POST, URL_REPROGRAMAR_CITA,
                                            resp -> {
                                                boolean ok = false;
                                                try { ok = "ok".equals(new JSONObject(resp).optString("estado")); }
                                                catch (Exception e) { ok = "ok".equals(resp.trim()); }
                                                if (ok) {
                                                    c.setFecha(nuevaFecha);
                                                    c.setHora(horaSel);
                                                    c.setEstado("reprogramado");
                                                    adaptador.notifyDataSetChanged();
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