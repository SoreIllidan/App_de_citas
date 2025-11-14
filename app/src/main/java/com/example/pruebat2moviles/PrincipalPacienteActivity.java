package com.example.pruebat2moviles;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PrincipalPacienteActivity extends AppCompatActivity {

    TextView tvSaludo, tvMedico, tvFecha, tvEstadoPrincipal, tvTituloLista;
    Button btnReprogramar, btnCancelar, btnConfirmar, btnPerfil;

    // Campana (nuevo)
    private ImageButton btnCampana;
    private TextView tvBadgeCount;

    // Recordatorios en home
    private View cardRecordatorios;
    private RecyclerView rvRecordatoriosHome;
    private Button btnVerRecordatorios;
    private final List<Rec> recs = new ArrayList<>();
    private RecHomeAdapter recAdapter = null;

    private CitaModel citaActual;
    private RecyclerView rvCitas;
    private AdaptadorCitas adaptador;
    private final List<CitaModel> lista = new ArrayList<>();

    private static final String URL_LISTAR_TODAS = Constantes.LISTAR_CITAS_PACIENTE_TODAS;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal_paciente);

        BottomNavHelper.attach(this, BottomNavHelper.Tab.HOME);

        tvSaludo = findViewById(R.id.tvSaludo);
        tvMedico = findViewById(R.id.tvMedico);
        tvFecha = findViewById(R.id.tvFecha);
        tvEstadoPrincipal = findViewById(R.id.tvEstadoPrincipal);
        tvTituloLista = findViewById(R.id.tvTituloLista);

        btnReprogramar = findViewById(R.id.btnReprogramar);
        btnCancelar = findViewById(R.id.btnCancelar);
        btnConfirmar = findViewById(R.id.btnConfirmar);
        btnPerfil = findViewById(R.id.btnPerfil);

        // Campana
        btnCampana = findViewById(R.id.btnCampana);
        tvBadgeCount = findViewById(R.id.tvBadgeCount);
        btnCampana.setOnClickListener(v ->
                startActivity(new Intent(this, RecordatoriosPacienteActivity.class)));

        // Recordatorios (Home)
        cardRecordatorios = findViewById(R.id.cardRecordatorios);
        rvRecordatoriosHome = findViewById(R.id.rvRecordatoriosHome);
        btnVerRecordatorios = findViewById(R.id.btnVerRecordatorios);
        rvRecordatoriosHome.setLayoutManager(new LinearLayoutManager(this));
        recAdapter = new RecHomeAdapter();
        rvRecordatoriosHome.setAdapter(recAdapter);
        btnVerRecordatorios.setOnClickListener(v ->
                startActivity(new Intent(this, RecordatoriosPacienteActivity.class)));

        rvCitas = findViewById(R.id.rvCitasPaciente);
        rvCitas.setLayoutManager(new LinearLayoutManager(this));
        adaptador = new AdaptadorCitas(lista, new AdaptadorCitas.OnCitaActionListener() {
            @Override public void onReprogramar(CitaModel c) { reprogramar(c); }
            @Override public void onCancelar(CitaModel c) { cancelar(c); }
            @Override public void onConfirmar(CitaModel c) { confirmar(c); }
        });
        rvCitas.setAdapter(adaptador);

        String nombre = getSharedPreferences("session", MODE_PRIVATE).getString("nombre", null);
        tvSaludo.setText(nombre != null ? "¡Hola, " + nombre + "!" : "¡Hola!");

        btnReprogramar.setOnClickListener(v -> { if (citaActual != null) reprogramar(citaActual); });
        btnCancelar.setOnClickListener(v -> { if (citaActual != null) cancelar(citaActual); });
        btnConfirmar.setOnClickListener(v -> { if (citaActual != null) confirmar(citaActual); });
        btnPerfil.setOnClickListener(v -> startActivity(new Intent(this, PerfilPacienteActivity.class)));

        cargarTodasCitas();
        cargarRecordatoriosHome();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarRecordatoriosHome(); // Actualiza badge al volver
    }

    private int getIdPaciente() {
        return getSharedPreferences("session", MODE_PRIVATE).getInt("id", 0);
    }

    private void cargarRecordatoriosHome() {
        int id = getIdPaciente();
        if (id == 0) { cardRecorditoriosOff(); return; }

        String url = Constantes.LISTAR_RECORDATORIOS + "?id_paciente=" + id;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                resp -> {
                    int total = 0;
                    if ("ok".equals(resp.optString("estado"))) {
                        JSONArray arr = resp.optJSONArray("recordatorios");
                        recs.clear();
                        if (arr != null) {
                            total = arr.length();
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject o = arr.optJSONObject(i);
                                if (o == null) continue;
                                Rec r = new Rec();
                                r.tipo = o.optString("tipo", "");
                                r.titulo = o.optString("titulo", "");
                                r.subtitulo = o.optString("subtitulo", "");
                                r.color = o.optString("color", "#E5F6F8");
                                recs.add(r);
                            }
                        }
                        recAdapter.notifyDataSetChanged();
                        cardRecordatorios.setVisibility(recs.isEmpty() ? View.GONE : View.VISIBLE);
                    } else {
                        cardRecorditoriosOff();
                    }
                    // Badge
                    if (total > 0) {
                        tvBadgeCount.setText(total > 9 ? "9+" : String.valueOf(total));
                        tvBadgeCount.setVisibility(View.VISIBLE);
                    } else {
                        tvBadgeCount.setVisibility(View.GONE);
                    }
                },
                err -> cardRecorditoriosOff()
        );
        Volley.newRequestQueue(this).add(req);
    }

    private void cardRecorditoriosOff() {
        cardRecordatorios.setVisibility(View.GONE);
        tvBadgeCount.setVisibility(View.GONE);
    }

    private void cargarTodasCitas() {
        int idPaciente = getIdPaciente();
        if (idPaciente == 0) { finish(); return; }

        String url = URL_LISTAR_TODAS + "?id_paciente=" + idPaciente;
        Log.d("PACIENTE", "GET " + url);

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET, url, null,
                resp -> {
                    try {
                        if (!"ok".equals(resp.optString("estado"))) {
                            renderSinCitaCabecera();
                            lista.clear();
                            adaptador.notifyDataSetChanged();
                            return;
                        }
                        JSONArray arr = resp.optJSONArray("citas");
                        lista.clear();
                        if (arr != null) {
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject o = arr.optJSONObject(i);
                                if (o != null) lista.add(CitaModel.fromJson(o));
                            }
                        }
                        adaptador.notifyDataSetChanged();

                        citaActual = seleccionarProxima(lista);
                        if (citaActual == null) renderSinCitaCabecera(); else renderCitaCabecera();
                    } catch (Exception e) {
                        renderSinCitaCabecera();
                    }
                },
                err -> renderSinCitaCabecera()
        );
        req.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(15000, 1, 1f));
        Volley.newRequestQueue(this).add(req);
    }

    private Date toDate(CitaModel c) {
        try { return sdf.parse(c.getFechaISO() + " " + c.getHora24()); } catch (ParseException e) { return null; }
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
        tvEstadoPrincipal.setVisibility(View.GONE);
        tvMedico.setText("—");
        tvFecha.setText("Sin próxima cita");
        tvMedico.setPaintFlags(tvMedico.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        tvFecha.setPaintFlags(tvFecha.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        tvMedico.setTextColor(Color.BLACK);
        tvFecha.setTextColor(Color.BLACK);
        btnReprogramar.setEnabled(false);
        btnCancelar.setEnabled(false);
        btnConfirmar.setEnabled(false);
        btnReprogramar.setAlpha(0.5f);
        btnCancelar.setAlpha(0.5f);
        btnConfirmar.setAlpha(0.5f);
    }

    private void renderCitaCabecera() {
        if (citaActual == null) { renderSinCitaCabecera(); return; }
        tvMedico.setText(citaActual.getMedico());
        tvFecha.setText(citaActual.getFecha());

        String est = citaActual.getEstado() == null ? "" : citaActual.getEstado().toLowerCase(Locale.US);
        boolean confirmada = "confirmada".equals(est);
        boolean cancelada = "cancelada".equals(est);
        boolean futura = esFutura(citaActual);
        boolean pendiente = ("programada".equals(est) || est.startsWith("reprogramad")) && futura && !confirmada;

        if (confirmada) {
            tvEstadoPrincipal.setVisibility(View.VISIBLE);
            tvEstadoPrincipal.setText("Confirmada");
            tvEstadoPrincipal.setTextColor(Color.parseColor("#2E7D32"));
        } else if (pendiente) {
            tvEstadoPrincipal.setVisibility(View.VISIBLE);
            tvEstadoPrincipal.setText("Pendiente de confirmación");
            tvEstadoPrincipal.setTextColor(Color.parseColor("#F57C00"));
        } else {
            tvEstadoPrincipal.setVisibility(View.GONE);
        }

        tvMedico.setPaintFlags(tvMedico.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        tvFecha.setPaintFlags(tvFecha.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        tvMedico.setTextColor(Color.BLACK);
        tvFecha.setTextColor(Color.BLACK);

        // Habilitar botones según reglas
        boolean enableReprogramar = futura && !confirmada && !cancelada;
        boolean enableCancelar = futura && !cancelada;
        boolean enableConfirmar = futura && !confirmada && !cancelada;

        setEnabled(btnReprogramar, enableReprogramar);
        setEnabled(btnCancelar, enableCancelar);
        setEnabled(btnConfirmar, enableConfirmar);
    }

    private void setEnabled(Button b, boolean enabled) {
        b.setEnabled(enabled);
        b.setAlpha(enabled ? 1f : 0.5f);
    }

    private boolean esFutura(CitaModel c) {
        Date d = toDate(c);
        return d != null && d.after(new Date());
    }

    private void cancelar(CitaModel c) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cancelar")
                .setMessage("¿Cancelar la cita seleccionada?")
                .setPositiveButton("Sí", (d, w) -> {
                    StringRequest req = new StringRequest(Request.Method.POST, Constantes.CANCELAR_CITA,
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
                    Volley.newRequestQueue(this).add(req);
                })
                .setNegativeButton("No", null)
                .show();
    }

    // REPROGRAMAR con validaciones: no confirmada, no cancelada y futura
    private void reprogramar(CitaModel c) {
        String est = c.getEstado() == null ? "" : c.getEstado().toLowerCase(Locale.US);
        if ("confirmada".equals(est)) {
            Toast.makeText(this, "No puedes reprogramar una cita confirmada", Toast.LENGTH_SHORT).show();
            return;
        }
        if ("cancelada".equals(est)) {
            Toast.makeText(this, "No puedes reprogramar una cita cancelada", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!esFutura(c)) {
            Toast.makeText(this, "La cita ya pasó, no se puede reprogramar", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar cal = Calendar.getInstance();
        DatePickerDialog dp = new DatePickerDialog(this, (view, y, m, d) -> {
            String nuevaFecha = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);

            int idMedico = c.getIdMedico();
            String url = Constantes.LISTAR_HORAS + "?id_medico=" + idMedico + "&fecha=" + nuevaFecha;

            JsonObjectRequest horasReq = new JsonObjectRequest(Request.Method.GET, url, null,
                    jr -> {
                        if (!"ok".equals(jr.optString("estado"))) {
                            Toast.makeText(this, "No hay horarios", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        JSONArray horas = jr.optJSONArray("horas");
                        if (horas == null || horas.length() == 0) {
                            Toast.makeText(this, "Sin horarios para esa fecha", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        List<String> libres = new ArrayList<>();
                        for (int i = 0; i < horas.length(); i++) {
                            JSONObject o = horas.optJSONObject(i);
                            if (o != null) {
                                int disp = o.optInt("disponible", 1);
                                String h = o.optString("hora", null);
                                if (h != null && disp == 1) {
                                    if (h.length() == 8) h = h.substring(0, 5); // HH:mm:ss -> HH:mm
                                    libres.add(h);
                                }
                            } else {
                                String h2 = horas.optString(i, null);
                                if (h2 != null) {
                                    if (h2.length() == 8) h2 = h2.substring(0, 5);
                                    libres.add(h2);
                                }
                            }
                        }
                        if (libres.isEmpty()) {
                            Toast.makeText(this, "Sin horarios libres", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String[] arr = libres.toArray(new String[0]);
                        new androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("Selecciona hora")
                                .setItems(arr, (dialog, which) -> {
                                    String horaSel = arr[which]; // HH:mm

                                    StringRequest req = new StringRequest(Request.Method.POST, Constantes.REPROGRAMAR_CITA,
                                            resp -> {
                                                boolean ok;
                                                String msgError = "No se pudo reprogramar";
                                                try {
                                                    JSONObject j = new JSONObject(resp);
                                                    ok = "ok".equals(j.optString("estado"));
                                                    if (!ok) msgError = j.optString("mensaje", msgError);
                                                } catch (Exception e) {
                                                    ok = "ok".equals(resp.trim());
                                                }

                                                if (ok) {
                                                    c.setFecha(nuevaFecha);
                                                    c.setHora(horaSel);
                                                    c.setEstado("reprogramada");
                                                    if (adaptador != null) adaptador.notifyDataSetChanged();
                                                    if (citaActual != null && citaActual.getId() == c.getId()) {
                                                        citaActual = c;
                                                        renderCitaCabecera();
                                                    }
                                                    Toast.makeText(this, "Cita reprogramada", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(this, msgError, Toast.LENGTH_SHORT).show();
                                                }
                                            },
                                            err -> {
                                                String detalle = "Error de red";
                                                if (err != null && err.networkResponse != null)
                                                    detalle = "HTTP " + err.networkResponse.statusCode;
                                                Toast.makeText(this, detalle, Toast.LENGTH_SHORT).show();
                                            }
                                    ) {
                                        @Override
                                        protected Map<String, String> getParams() {
                                            Map<String, String> p = new HashMap<>();
                                            p.put("id_cita", String.valueOf(c.getId()));
                                            p.put("id_medico", String.valueOf(idMedico));
                                            p.put("fecha", nuevaFecha);
                                            p.put("hora", horaSel); // backend normaliza a HH:mm:ss si lo requiere
                                            return p;
                                        }
                                    };
                                    Volley.newRequestQueue(this).add(req);
                                })
                                .show();
                    },
                    err -> Toast.makeText(this, "Error al cargar horas", Toast.LENGTH_SHORT).show()
            );
            Volley.newRequestQueue(this).add(horasReq);

        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dp.getDatePicker().setMinDate(System.currentTimeMillis());
        dp.show();
    }

    private void confirmar(CitaModel c) {
        int idPaciente = getIdPaciente();
        if (idPaciente == 0) { Toast.makeText(this, "Sesión inválida", Toast.LENGTH_SHORT).show(); return; }

        if ("confirmada".equalsIgnoreCase(c.getEstado())) {
            Toast.makeText(this, "Ya confirmada", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest req = new StringRequest(Request.Method.POST, Constantes.CONFIRMAR_CITA_PACIENTE,
                resp -> {
                    boolean ok = "ok".equals(resp.trim());
                    if (!ok) {
                        try { ok = "ok".equals(new JSONObject(resp).optString("estado")); } catch (Exception ignored) {}
                    }
                    if (ok) {
                        c.setEstado("confirmada");
                        adaptador.notifyDataSetChanged();
                        if (citaActual != null && citaActual.getId() == c.getId()) {
                            citaActual = c;
                            renderCitaCabecera();
                        }
                        Toast.makeText(this, "Asistencia confirmada", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No se pudo confirmar", Toast.LENGTH_SHORT).show();
                    }
                },
                err -> Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show()
        ) {
            @Override protected Map<String,String> getParams() {
                Map<String,String> p = new HashMap<>();
                p.put("id_cita", String.valueOf(c.getId()));
                p.put("id_paciente", String.valueOf(idPaciente));
                return p;
            }
        };
        Volley.newRequestQueue(this).add(req);
    }

    // ====== Recordatorios (Home) ======
    static class Rec {
        String tipo;    // medicamento | cita
        String titulo;
        String subtitulo;
        String color;   // #RRGGBB
    }

    class RecHomeAdapter extends RecyclerView.Adapter<RecHomeAdapter.VH> {

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_recordatorio, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Rec r = recs.get(position);
            holder.tvTitulo.setText(r.titulo);
            holder.tvSub.setText(r.subtitulo);
            try { holder.itemView.setBackgroundColor(Color.parseColor(r.color)); } catch (Exception ignored) {}
        }

        @Override
        public int getItemCount() {
            return Math.min(recs.size(), 2);
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvTitulo, tvSub;
            VH(@NonNull View itemView) {
                super(itemView);
                tvTitulo = itemView.findViewById(R.id.tvRecTitulo);
                tvSub    = itemView.findViewById(R.id.tvRecSub);
            }
        }
    }
}