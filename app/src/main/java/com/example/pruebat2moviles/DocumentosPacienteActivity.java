package com.example.pruebat2moviles;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DocumentosPacienteActivity extends AppCompatActivity {

    static class Doc {
        int id;
        int idCita;
        String tipo;        // diagnostico | descanso | receta
        String titulo;
        String descripcion;
        String fechaCita;
        String hora;
        String createdAt;
        String url;
        Integer kb;         // tamaño en KB
    }

    private androidx.recyclerview.widget.RecyclerView rv;
    private DocsAdapter adapter;
    private final List<Doc> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documentos_paciente);
        BottomNavHelper.attach(this, BottomNavHelper.Tab.DOCS);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Documentos de Citas");
        }

        rv = findViewById(R.id.rvDocs);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DocsAdapter();
        rv.setAdapter(adapter);

        cargar();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private int getIdPaciente() {
        return getSharedPreferences("session", MODE_PRIVATE).getInt("id", 0);
    }

    private void cargar() {
        int id = getIdPaciente();
        if (id == 0) {
            finish();
            return;
        }
        String url = Constantes.LISTAR_DOCS_PACIENTE + "?id_paciente=" + id;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                resp -> {
                    if (!"ok".equals(resp.optString("estado"))) {
                        Toast.makeText(this, "No se pudieron cargar documentos", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    JSONArray arr = resp.optJSONArray("documentos");
                    data.clear();
                    if (arr != null) {
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject o = arr.optJSONObject(i);
                            if (o == null) continue;
                            Doc d = new Doc();
                            d.id = o.optInt("id");
                            d.idCita = o.optInt("id_cita");
                            d.tipo = o.optString("tipo", "");
                            d.titulo = o.optString("titulo", "");
                            d.descripcion = o.optString("descripcion", "");
                            d.fechaCita = o.optString("fecha_cita", "");
                            d.hora = o.optString("hora", "");
                            d.createdAt = o.optString("created_at", "");
                            d.url = o.optString("url", "");
                            d.kb = o.has("tamano_kb") ? o.optInt("tamano_kb") : null;
                            data.add(d);
                        }
                    }
                    adapter.notifyDataSetChanged();
                },
                err -> Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(req);
    }

    class DocsAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<DocsAdapter.VH> {

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_documento_paciente, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            Doc d = data.get(pos);

            // Tipo y badge
            String tituloTipo = tipoLegible(d.tipo);
            h.tvTipo.setText(tituloTipo);
            h.tvBadge.setText(badgeTexto(d.tipo));
            h.tvBadge.setBackgroundColor(badgeColor(d.tipo));

            // Título (si no hay título usar descripción)
            String textoTitulo = (d.titulo == null || d.titulo.isEmpty())
                    ? (d.descripcion == null ? "" : d.descripcion)
                    : d.titulo;
            h.tvTitulo.setText(textoTitulo);

            // Línea cita
            String lineaCita = "Cita del " + fechaLegible(d.fechaCita)
                    + (d.hora == null || d.hora.isEmpty() ? "" : (" - " + d.hora));
            h.tvLineaCita.setText(lineaCita);

            // Línea generado
            String gen = "Generado el " + fechaLegible(d.createdAt)
                    + (d.kb == null ? "" : (" | Tamaño: " + d.kb + " KB"));
            h.tvLineaGenerado.setText(gen);

            // Ver
            h.btnVer.setOnClickListener(v -> {
                if (d.url == null || d.url.isEmpty()) {
                    Toast.makeText(DocumentosPacienteActivity.this, "URL no disponible", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW)
                            .setDataAndType(Uri.parse(d.url), "application/pdf")
                            .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(i);
                } catch (Exception e) {
                    Toast.makeText(DocumentosPacienteActivity.this, "No hay visor de PDF", Toast.LENGTH_SHORT).show();
                }
            });

            // Descargar
            h.btnDescargar.setOnClickListener(v -> {
                if (d.url == null || d.url.isEmpty()) {
                    Toast.makeText(DocumentosPacienteActivity.this, "URL no disponible", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager.Request r = new DownloadManager.Request(Uri.parse(d.url));
                    String nombreArchivo = (d.titulo == null || d.titulo.isEmpty()) ? "documento_cita" : d.titulo;
                    r.setTitle(nombreArchivo);
                    r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    r.setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS,
                            nombreArchivo + ".pdf");
                    dm.enqueue(r);
                    Toast.makeText(DocumentosPacienteActivity.this, "Descargando...", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(DocumentosPacienteActivity.this, "No se pudo descargar", Toast.LENGTH_SHORT).show();
                }
            });

            // Compartir (enlace)
            h.btnCompartir.setOnClickListener(v -> {
                if (d.url == null || d.url.isEmpty()) {
                    Toast.makeText(DocumentosPacienteActivity.this, "URL no disponible", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_SUBJECT, "Documento de cita");
                String texto = "Te comparto el documento (" + tituloTipo + "): " + d.url;
                share.putExtra(Intent.EXTRA_TEXT, texto);
                startActivity(Intent.createChooser(share, "Compartir"));
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class VH extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            TextView tvTipo, tvBadge, tvTitulo, tvLineaCita, tvLineaGenerado;
            Button btnVer, btnDescargar, btnCompartir;
            VH(@NonNull View itemView) {
                super(itemView);
                tvTipo = itemView.findViewById(R.id.tvTipo);
                tvBadge = itemView.findViewById(R.id.tvBadge);
                tvTitulo = itemView.findViewById(R.id.tvTitulo);
                tvLineaCita = itemView.findViewById(R.id.tvLineaCita);
                tvLineaGenerado = itemView.findViewById(R.id.tvLineaGenerado);
                btnVer = itemView.findViewById(R.id.btnVer);
                btnDescargar = itemView.findViewById(R.id.btnDescargar);
                btnCompartir = itemView.findViewById(R.id.btnCompartir);
            }
        }

        private String tipoLegible(String tipo) {
            if ("diagnostico".equalsIgnoreCase(tipo)) return "Diagnóstico";
            if ("descanso".equalsIgnoreCase(tipo)) return "Descanso Médico";
            return "Receta Médica";
        }

        private String badgeTexto(String tipo) {
            if ("diagnostico".equalsIgnoreCase(tipo)) return "Diagnóstico";
            if ("descanso".equalsIgnoreCase(tipo)) return "Descanso";
            return "Receta";
        }

        private int badgeColor(String tipo) {
            if ("diagnostico".equalsIgnoreCase(tipo)) return android.graphics.Color.parseColor("#1E88E5");
            if ("descanso".equalsIgnoreCase(tipo)) return android.graphics.Color.parseColor("#D32F2F");
            return android.graphics.Color.parseColor("#3C98B3");
        }

        private String fechaLegible(String iso) {
            if (iso == null || iso.isEmpty()) return "";
            // Manejo simple: tomar primeros 10 caracteres (YYYY-MM-DD)
            try { return iso.substring(0, 10); } catch (Exception ignored) { return iso; }
        }
    }
}