package com.example.pruebat2moviles;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PerfilPacienteActivity extends AppCompatActivity {

    // Texto fijo
    private TextView tvNombreCompleto, tvDni, tvTituloPerfil, tvAyuda;
    // Editables
    private EditText etCorreo, etTelefono, etFechaNacimiento, etOcupacion;
    private EditText etEmergenciaNombre, etEmergenciaRelacion, etEmergenciaTelefono;
    private EditText etTipoSangre, etAlergias, etHistorialMedico;
    private EditText etAseguradora, etPoliza, etDireccion;

    private Button btnEditarTodo, btnGuardarTodo, btnCerrarSesion;

    private PerfilPacienteModel perfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_paciente);
        BottomNavHelper.attach(this, BottomNavHelper.Tab.USER);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Perfil");
        }

        bindViews();
        setListeners();
        cargarPerfil();
    }

    private void bindViews() {
        tvTituloPerfil       = findViewById(R.id.tvTituloPerfil);
        tvNombreCompleto     = findViewById(R.id.tvNombreCompleto);
        tvDni                = findViewById(R.id.tvDni);
        tvAyuda              = findViewById(R.id.tvAyuda); // NUEVO

        etCorreo             = findViewById(R.id.etCorreoPerfil);
        etTelefono           = findViewById(R.id.etTelefonoPerfil);
        etFechaNacimiento    = findViewById(R.id.etFechaNacimiento);
        etOcupacion          = findViewById(R.id.etOcupacion);

        etEmergenciaNombre   = findViewById(R.id.etEmergenciaNombre);
        etEmergenciaRelacion = findViewById(R.id.etEmergenciaRelacion);
        etEmergenciaTelefono = findViewById(R.id.etEmergenciaTelefono);

        etTipoSangre         = findViewById(R.id.etTipoSangre);
        etAlergias           = findViewById(R.id.etAlergias);
        etHistorialMedico    = findViewById(R.id.etHistorialMedico);

        etAseguradora        = findViewById(R.id.etAseguradora);
        etPoliza             = findViewById(R.id.etPoliza);
        etDireccion          = findViewById(R.id.etDireccion);

        btnEditarTodo        = findViewById(R.id.btnEditarTodo);
        btnGuardarTodo       = findViewById(R.id.btnGuardarTodo);
        btnCerrarSesion      = findViewById(R.id.btnCerrarSesionPerfil);
    }

    private void setListeners() {
        btnEditarTodo.setOnClickListener(v -> habilitarEdicion(true));
        btnGuardarTodo.setOnClickListener(v -> guardarCambios());
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());

        // Abrir Ayuda y soporte (NUEVO)
        if (tvAyuda != null) {
            tvAyuda.setOnClickListener(v ->
                    startActivity(new Intent(PerfilPacienteActivity.this, AyudaActivity.class))
            );
        }

        // Date picker para fecha nacimiento
        etFechaNacimiento.setOnClickListener(v -> {
            if (!etFechaNacimiento.isEnabled()) return;
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, d) -> {
                String mm = String.format("%02d", m + 1);
                String dd = String.format("%02d", d);
                etFechaNacimiento.setText(y + "-" + mm + "-" + dd);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private int getIdPaciente() {
        SharedPreferences sp = getSharedPreferences("session", MODE_PRIVATE);
        return sp.getInt("id", 0);
    }

    private void cargarPerfil() {
        int id = getIdPaciente();
        if (id == 0) {
            Toast.makeText(this, "Sesión inválida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String url = Constantes.OBTENER_PERFIL_PACIENTE + "?id_paciente=" + id;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                resp -> {
                    if (!"ok".equals(resp.optString("estado"))) {
                        Toast.makeText(this, "No se pudo cargar perfil", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    JSONObject p = resp.optJSONObject("perfil");
                    if (p == null) {
                        Toast.makeText(this, "Perfil vacío", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    perfil = PerfilPacienteModel.fromJson(p);
                    renderPerfil();
                },
                err -> Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(req);
    }

    private void renderPerfil() {
        if (perfil == null) return;
        tvNombreCompleto.setText("Nombre: " + n(perfil.nombre));
        tvDni.setText("DNI: " + n(perfil.dni));
        etCorreo.setText(n(perfil.correo));
        etTelefono.setText(n(perfil.telefono));
        etFechaNacimiento.setText(n(perfil.fechaNacimiento));
        etOcupacion.setText(n(perfil.ocupacion));
        etEmergenciaNombre.setText(n(perfil.emergenciaNombre));
        etEmergenciaRelacion.setText(n(perfil.emergenciaRelacion));
        etEmergenciaTelefono.setText(n(perfil.emergenciaTelefono));
        etTipoSangre.setText(n(perfil.tipoSangre));
        etAlergias.setText(n(perfil.alergias));
        etHistorialMedico.setText(n(perfil.historialMedico));
        etAseguradora.setText(n(perfil.aseguradora));
        etPoliza.setText(n(perfil.poliza));
        etDireccion.setText(n(perfil.direccion));
        habilitarEdicion(false);
    }

    private String n(String s) { return (s == null || s.isEmpty() || "null".equalsIgnoreCase(s)) ? "" : s; }

    private void habilitarEdicion(boolean editar) {
        etCorreo.setEnabled(editar);
        etTelefono.setEnabled(editar);
        etFechaNacimiento.setEnabled(editar);
        etOcupacion.setEnabled(editar);

        etEmergenciaNombre.setEnabled(editar);
        etEmergenciaRelacion.setEnabled(editar);
        etEmergenciaTelefono.setEnabled(editar);

        etTipoSangre.setEnabled(editar);
        etAlergias.setEnabled(editar);
        etHistorialMedico.setEnabled(editar);

        etAseguradora.setEnabled(editar);
        etPoliza.setEnabled(editar);
        etDireccion.setEnabled(editar);

        btnGuardarTodo.setEnabled(editar);
        btnEditarTodo.setEnabled(!editar);
        btnGuardarTodo.setAlpha(editar ? 1f : 0.5f);
        btnEditarTodo.setAlpha(!editar ? 1f : 0.5f);
    }

    private void guardarCambios() {
        int id = getIdPaciente();
        if (id == 0) return;

        String correo = etCorreo.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();

        if (correo.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest req = new StringRequest(Request.Method.POST, Constantes.ACTUALIZAR_PERFIL_PACIENTE,
                resp -> {
                    try {
                        JSONObject j = new JSONObject(resp);
                        String est = j.optString("estado","");
                        if ("ok".equals(est)) {
                            Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                            getSharedPreferences("session", MODE_PRIVATE)
                                    .edit().putString("correo", correo).apply();
                            habilitarEdicion(false);
                        } else if ("sin_cambios".equals(est)) {
                            Toast.makeText(this, "Sin cambios", Toast.LENGTH_SHORT).show();
                            habilitarEdicion(false);
                        } else {
                            Toast.makeText(this, j.optString("mensaje","Error al actualizar"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        if (resp.trim().equalsIgnoreCase("ok")) {
                            Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                            habilitarEdicion(false);
                        } else {
                            Toast.makeText(this, "Respuesta inválida", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                err -> Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show()
        ) {
            @Override protected Map<String,String> getParams() {
                Map<String,String> p = new HashMap<>();
                p.put("id_paciente", String.valueOf(id));
                p.put("correo", correo);
                p.put("telefono", telefono);
                putIfNotEmpty(p, "fecha_nacimiento", etFechaNacimiento.getText().toString().trim());
                putIfNotEmpty(p, "ocupacion", etOcupacion.getText().toString().trim());
                putIfNotEmpty(p, "emergencia_nombre", etEmergenciaNombre.getText().toString().trim());
                putIfNotEmpty(p, "emergencia_relacion", etEmergenciaRelacion.getText().toString().trim());
                putIfNotEmpty(p, "emergencia_telefono", etEmergenciaTelefono.getText().toString().trim());
                putIfNotEmpty(p, "tipo_sangre", etTipoSangre.getText().toString().trim());
                putIfNotEmpty(p, "alergias", etAlergias.getText().toString().trim());
                putIfNotEmpty(p, "historial_medico", etHistorialMedico.getText().toString().trim());
                putIfNotEmpty(p, "aseguradora", etAseguradora.getText().toString().trim());
                putIfNotEmpty(p, "poliza", etPoliza.getText().toString().trim());
                putIfNotEmpty(p, "direccion", etDireccion.getText().toString().trim());
                return p;
            }
        };
        Volley.newRequestQueue(this).add(req);
    }

    private void putIfNotEmpty(Map<String,String> map, String key, String value) {
        if (!value.isEmpty()) map.put(key, value);
    }

    private void cerrarSesion() {
        getSharedPreferences("session", MODE_PRIVATE).edit().clear().apply();
        Intent i = new Intent(this, InicioActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home){ finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}