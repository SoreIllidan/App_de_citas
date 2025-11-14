package com.example.pruebat2moviles;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;
import android.provider.OpenableColumns;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminCitasMedicoActivity extends AppCompatActivity {

    TextView tvMedico;
    EditText etBuscar;
    androidx.recyclerview.widget.RecyclerView rv;
    ProgressBar progress;

    int idMedico;
    String nombreMedico;

    static class CitaRow {
        int id;
        int idPaciente;
        String paciente;
        String dni;
        String correo;
        String fecha;
        String hora;
        String consultorio;
        String estado;
        int es_pasada;
        int es_hoy;
        int confirmada;
        int por_confirmar;
    }

    final List<CitaRow> data = new ArrayList<>();
    final List<CitaRow> filtered = new ArrayList<>();
    CitasAdapter adapter;

    // Campos para subir PDF
    private CitaRow citaSeleccionadaParaUpload = null;
    private String tipoSeleccionado = null;
    private ActivityResultLauncher<String> pickPdfLauncher;

    // NUEVO: flujo para "Receta Médica" (capturar medicamentos antes del PDF)
    private ActivityResultLauncher<Intent> recetaLauncher;
    private String medsJsonSeleccionado = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_citas_medico);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Citas del médico");
        }

        idMedico = getIntent().getIntExtra("id_medico", 0);
        nombreMedico = getIntent().getStringExtra("nombre_medico");

        tvMedico = findViewById(R.id.tvNombreMedicoSel);
        etBuscar = findViewById(R.id.etBuscar);
        rv = findViewById(R.id.rvCitasMedico);
        progress = findViewById(R.id.progress);

        tvMedico.setText("Dr.(a) " + (nombreMedico == null ? "" : nombreMedico));

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CitasAdapter();
        rv.setAdapter(adapter);

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { applyFilter(s.toString()); }
        });

        // Registrar lanzador para seleccionar PDF
        pickPdfLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && citaSeleccionadaParaUpload != null && tipoSeleccionado != null) {
                        subirPdfCita(citaSeleccionadaParaUpload, tipoSeleccionado, uri);
                    } else {
                        Toast.makeText(this, "Operación cancelada", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // NUEVO: lanzador para capturar medicamentos de receta
        recetaLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                res -> {
                    if (res.getResultCode() == RESULT_OK && res.getData() != null) {
                        medsJsonSeleccionado = res.getData().getStringExtra("meds_json");
                        // Luego de capturar medicamentos, seleccionar PDF
                        pickPdfLauncher.launch("application/pdf");
                    } else {
                        Toast.makeText(this, "Receta cancelada", Toast.LENGTH_SHORT).show();
                        citaSeleccionadaParaUpload = null;
                        tipoSeleccionado = null;
                    }
                }
        );

        cargar();
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void setLoading(boolean b) {
        if (progress != null) progress.setVisibility(b ? View.VISIBLE : View.GONE);
        etBuscar.setEnabled(!b);
    }

    private void cargar() {
        if (idMedico <= 0) {
            Toast.makeText(this, "Médico inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setLoading(true);
        String url = Constantes.LISTAR_CITAS_MEDICO + "?id_medico=" + idMedico;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                resp -> {
                    setLoading(false);
                    if (!"ok".equals(resp.optString("estado"))) {
                        Toast.makeText(this, "No se pudieron cargar las citas", Toast.LENGTH_SHORT).show();
                        data.clear(); filtered.clear(); adapter.notifyDataSetChanged();
                        return;
                    }
                    JSONArray arr = resp.optJSONArray("citas");
                    data.clear();
                    if (arr != null) {
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject o = arr.optJSONObject(i);
                            if (o == null) continue;
                            CitaRow r = new CitaRow();
                            r.id            = o.optInt("id", 0);
                            r.idPaciente    = o.optInt("id_paciente", 0);
                            r.paciente      = o.optString("paciente", "");
                            r.dni           = o.optString("dni", "");
                            r.correo        = o.optString("correo", "");
                            r.fecha         = o.optString("fecha", "");
                            r.hora          = o.optString("hora", "");
                            r.consultorio   = o.optString("consultorio", "");
                            r.estado        = o.optString("estado", "");
                            r.es_pasada     = o.optInt("es_pasada", 0);
                            r.es_hoy        = o.optInt("es_hoy", 0);
                            r.confirmada    = o.optInt("confirmada", 0);
                            r.por_confirmar = o.optInt("por_confirmar", 0);
                            data.add(r);
                        }
                    }
                    applyFilter(etBuscar.getText() == null ? "" : etBuscar.getText().toString());
                },
                err -> {
                    setLoading(false);
                    String detalle = "Error de red";
                    if (err.networkResponse != null && err.networkResponse.data != null) {
                        try {
                            detalle = "HTTP " + err.networkResponse.statusCode + ": " +
                                    new String(err.networkResponse.data, StandardCharsets.UTF_8);
                        } catch (Exception ignored) {}
                    }
                    Toast.makeText(this, detalle, Toast.LENGTH_LONG).show();
                }
        );
        Volley.newRequestQueue(this).add(req);
    }

    private void applyFilter(String q) {
        String query = q == null ? "" : q.trim().toLowerCase(Locale.US);
        filtered.clear();
        if (query.isEmpty()) {
            filtered.addAll(data);
        } else {
            for (CitaRow r : data) {
                if (
                        (r.paciente != null && r.paciente.toLowerCase(Locale.US).contains(query)) ||
                                (r.dni != null && r.dni.toLowerCase(Locale.US).contains(query)) ||
                                (r.correo != null && r.correo.toLowerCase(Locale.US).contains(query)) ||
                                (r.estado != null && r.estado.toLowerCase(Locale.US).contains(query)) ||
                                (r.fecha != null && r.fecha.toLowerCase(Locale.US).contains(query)) ||
                                (r.hora != null && r.hora.toLowerCase(Locale.US).contains(query)) ||
                                (r.consultorio != null && r.consultorio.toLowerCase(Locale.US).contains(query))
                ) {
                    filtered.add(r);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    class CitasAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<CitasAdapter.VH> {
        @NonNull
        @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_cita_medico, parent, false);
            return new VH(v);
        }

        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            CitaRow c = filtered.get(pos);
            h.tvPaciente.setText(c.paciente + (c.dni == null || c.dni.isEmpty() ? "" : " (DNI " + c.dni + ")"));
            h.tvCorreo.setText(c.correo == null ? "" : c.correo);
            h.tvFechaHora.setText(c.fecha + (c.hora == null || c.hora.isEmpty() ? "" : " - " + c.hora));
            h.tvConsultorio.setText(c.consultorio == null ? "" : c.consultorio);

            String etiqueta = c.estado == null ? "" : c.estado;
            int color = Color.parseColor("#1E88E5");

            if ("cancelada".equalsIgnoreCase(c.estado)) {
                etiqueta = "Cancelada";
                color = Color.RED;
            } else if ("confirmada".equalsIgnoreCase(c.estado)) {
                etiqueta = "Confirmada";
                color = Color.parseColor("#2E7D32");
            } else if (c.por_confirmar == 1) {
                etiqueta = "Pendiente de confirmación";
                color = Color.parseColor("#F57C00");
            } else if ("reprogramada".equalsIgnoreCase(c.estado)) {
                etiqueta = "Reprogramada";
                color = Color.parseColor("#1565C0");
            } else if (c.es_pasada == 1) {
                etiqueta = "Pasada";
                color = Color.DKGRAY;
            }

            h.tvEstado.setText("Estado: " + etiqueta);
            h.tvEstado.setTextColor(color);

            // Subir PDF: elegir tipo y lanzar selector correspondiente
            h.btnSubirPdf.setOnClickListener(v -> {
                final String[] tipos = {"Diagnóstico", "Descanso Médico", "Receta Médica"};
                new AlertDialog.Builder(AdminCitasMedicoActivity.this)
                        .setTitle("Tipo de documento")
                        .setItems(tipos, (d, which) -> {
                            String t = tipos[which].toLowerCase(Locale.US);
                            citaSeleccionadaParaUpload = c;
                            medsJsonSeleccionado = null;

                            if (t.startsWith("diag")) {
                                tipoSeleccionado = "diagnostico";
                                pickPdfLauncher.launch("application/pdf");
                            } else if (t.startsWith("desc")) {
                                tipoSeleccionado = "descanso";
                                pickPdfLauncher.launch("application/pdf");
                            } else {
                                // Receta Médica: primero capturar medicamentos
                                tipoSeleccionado = "receta";
                                Intent i = new Intent(AdminCitasMedicoActivity.this, AdminRecetaActivity.class);
                                recetaLauncher.launch(i);
                            }
                        })
                        .show();
            });
        }

        @Override public int getItemCount() { return filtered.size(); }

        class VH extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            TextView tvPaciente, tvCorreo, tvFechaHora, tvConsultorio, tvEstado;
            Button btnSubirPdf;
            VH(View itemView) {
                super(itemView);
                tvPaciente   = itemView.findViewById(R.id.tvPacNombre);
                tvCorreo     = itemView.findViewById(R.id.tvPacCorreo);
                tvFechaHora  = itemView.findViewById(R.id.tvFechaHora);
                tvConsultorio= itemView.findViewById(R.id.tvConsultorio);
                tvEstado     = itemView.findViewById(R.id.tvEstadoCita);
                btnSubirPdf  = itemView.findViewById(R.id.btnSubirPdf);
            }
        }
    }

    // SUBIR PDF a la cita (usa VolleyMultipartRequest)
    private void subirPdfCita(CitaRow c, String tipo, Uri uri) {
        try {
            ContentResolver cr = getContentResolver();
            String fname = "documento.pdf";
            String mime = cr.getType(uri);

            // Intentar obtener nombre del archivo desde el proveedor
            try (android.database.Cursor cursor = cr.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIdx >= 0) fname = cursor.getString(nameIdx);
                }
            }

            byte[] bytes;
            try (java.io.InputStream is = cr.openInputStream(uri)) {
                bytes = VolleyMultipartRequest.readAllBytes(is);
            }

            Map<String,String> params = new HashMap<>();
            params.put("id_cita", String.valueOf(c.id));
            params.put("tipo", tipo);
            // Opcionales
            params.put("titulo", tipo.equals("receta") ? "Receta Médica" : tipo.equals("diagnostico") ? "Diagnóstico" : "Descanso Médico");
            params.put("descripcion", "Documento de la cita del " + c.fecha + (c.hora==null?"":(" " + c.hora)));

            // Adjuntar medicamentos si es receta y se capturaron
            if ("receta".equalsIgnoreCase(tipo) && medsJsonSeleccionado != null && !medsJsonSeleccionado.isEmpty()) {
                params.put("meds_json", medsJsonSeleccionado);
            }

            VolleyMultipartRequest req = new VolleyMultipartRequest(
                    com.android.volley.Request.Method.POST,
                    Constantes.SUBIR_DOC_CITA,
                    params,
                    "archivo", fname, mime, bytes,
                    resp -> {
                        boolean ok = resp != null && resp.contains("\"estado\":\"ok\"");
                        Toast.makeText(this, ok ? "Documento subido" : "Error al subir", Toast.LENGTH_LONG).show();
                    },
                    err -> {
                        String detalle = "Error de red";
                        if (err.networkResponse != null) detalle = "HTTP " + err.networkResponse.statusCode;
                        Toast.makeText(this, detalle, Toast.LENGTH_LONG).show();
                    }
            );
            Volley.newRequestQueue(this).add(req);
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo leer el archivo", Toast.LENGTH_LONG).show();
        } finally {
            citaSeleccionadaParaUpload = null;
            tipoSeleccionado = null;
            medsJsonSeleccionado = null;
        }
    }
}