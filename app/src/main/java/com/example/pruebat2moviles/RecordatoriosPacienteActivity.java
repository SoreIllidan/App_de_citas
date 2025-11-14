package com.example.pruebat2moviles;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RecordatoriosPacienteActivity extends AppCompatActivity {

    static class Rec {
        String tipo;    // medicamento | cita
        String titulo;  // "Tomar Medicamento" | "Cita Próxima"
        String subtitulo; // "Carvedilol 25mg - Cada 12 horas" | "Dr. X - En 3 días"
        String color;   // #RRGGBB
    }

    private final List<Rec> data = new ArrayList<>();
    private androidx.recyclerview.widget.RecyclerView rv;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordatorios_paciente);

        if (getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Recordatorios");
        }

        rv = findViewById(R.id.rvRecordatorios);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new Adapter());

        cargar();
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }

    private int getIdPaciente() {
        return getSharedPreferences("session", MODE_PRIVATE).getInt("id", 0);
    }

    private void cargar() {
        int id = getIdPaciente();
        if (id == 0) { finish(); return; }

        String url = Constantes.LISTAR_RECORDATORIOS + "?id_paciente=" + id;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                resp -> {
                    if (!"ok".equals(resp.optString("estado"))) {
                        Toast.makeText(this, "No se pudieron cargar recordatorios", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    JSONArray arr = resp.optJSONArray("recordatorios");
                    data.clear();
                    if (arr!=null){
                        for (int i=0;i<arr.length();i++){
                            JSONObject o = arr.optJSONObject(i);
                            Rec r = new Rec();
                            r.tipo = o.optString("tipo","");
                            r.titulo = o.optString("titulo","");
                            r.subtitulo = o.optString("subtitulo","");
                            r.color = o.optString("color","#DFEDF0");
                            data.add(r);
                        }
                    }
                    rv.getAdapter().notifyDataSetChanged();
                },
                err -> Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(req);
    }

    class Adapter extends androidx.recyclerview.widget.RecyclerView.Adapter<VH> {
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v){
            View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_recordatorio, p, false);
            return new VH(view);
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos){
            Rec r = data.get(pos);
            h.tvTitulo.setText(r.titulo);
            h.tvSub.setText(r.subtitulo);
            try { h.itemView.setBackgroundColor(android.graphics.Color.parseColor(r.color)); } catch (Exception ignored) {}
        }
        @Override public int getItemCount(){ return data.size(); }
    }
    static class VH extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        TextView tvTitulo, tvSub;
        VH(View item){ super(item); tvTitulo=item.findViewById(R.id.tvRecTitulo); tvSub=item.findViewById(R.id.tvRecSub); }
    }
}