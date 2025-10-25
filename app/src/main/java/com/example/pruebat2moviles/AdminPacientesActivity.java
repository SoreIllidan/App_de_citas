package com.example.pruebat2moviles;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class AdminPacientesActivity extends AppCompatActivity {

    androidx.recyclerview.widget.RecyclerView rv;
    AdaptadorPacientes adapter;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_pacientes);

        rv = findViewById(R.id.rvPacientes);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdaptadorPacientes();
        rv.setAdapter(adapter);

        cargarPacientes();
    }

    private void cargarPacientes() {
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, Constantes.LISTAR_PACIENTES, null,
                resp -> {
                    if (!"ok".equals(resp.optString("estado"))) {
                        Toast.makeText(this, "No se pudieron cargar los pacientes", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    JSONArray arr = resp.optJSONArray("pacientes");
                    List<PacienteModel> list = new ArrayList<>();
                    if (arr != null) {
                        for (int i = 0; i < arr.length(); i++) {
                            list.add(PacienteModel.fromJson(arr.optJSONObject(i)));
                        }
                    }
                    adapter.setData(list);
                },
                err -> Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(req);
    }
}