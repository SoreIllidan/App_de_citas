package com.example.pruebat2moviles;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class AdminMedicosActivity extends AppCompatActivity {

    androidx.recyclerview.widget.RecyclerView rv;
    AdaptadorMedicos adapter;
    Button btnAdd, btnGenerarHorarios;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_medicos);

        rv = findViewById(R.id.rvMedicos);
        btnAdd = findViewById(R.id.btnAnadirMedico);
        btnGenerarHorarios = findViewById(R.id.btnGenerarHorarios);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdaptadorMedicos(m -> {
            Intent i = new Intent(this, AdminCitasMedicoActivity.class);
            i.putExtra("id_medico", m.id);
            i.putExtra("nombre_medico", m.nombre);
            startActivity(i);
        });
        rv.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> startActivity(new Intent(this, AdminAgregarMedicoActivity.class)));
        btnGenerarHorarios.setOnClickListener(v -> startActivity(new Intent(this, AdminGenerarHorariosActivity.class)));
    }

    @Override protected void onResume() {
        super.onResume();
        cargarMedicos();
    }

    private void cargarMedicos() {
        String url = Constantes.LISTAR_MEDICOS + "?activos=1";
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                resp -> {
                    if (!"ok".equals(resp.optString("estado"))) {
                        Toast.makeText(this, "No se pudieron cargar los médicos", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    JSONArray arr = resp.optJSONArray("medicos");
                    List<MedicoModel> list = new ArrayList<>();
                    if (arr != null) for (int i = 0; i < arr.length(); i++) list.add(MedicoModel.fromJson(arr.optJSONObject(i)));
                    adapter.setData(list);
                },
                err -> Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(req);
    }
}