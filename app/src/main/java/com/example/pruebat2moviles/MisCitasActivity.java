package com.example.pruebat2moviles;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MisCitasActivity extends AppCompatActivity {

    private androidx.recyclerview.widget.RecyclerView recycler;
    private AdaptadorCitas adaptador;

    // Datos
    private final List<CitaModel> lista = new ArrayList<>();         // todas
    private final List<CitaModel> vista = new ArrayList<>();         // filtradas (lo que se muestra)

    // Filtros
    private AutoCompleteTextView acBuscar, acMedico, acEspecialidad;
    private TextView tvFechaFiltro;
    private Button btnElegirFecha, btnLimpiar;

    // Sugerencias
    private ArrayAdapter<String> sugBuscarAdapter, sugMedicosAdapter, sugEspecAdapter;
    private final Set<String> setMedicos = new HashSet<>();
    private final Set<String> setEspecs = new HashSet<>();
    private final Set<String> setLugares = new HashSet<>();

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
    private final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    // Estado de filtros en memoria
    private String filtroFecha = null;         // yyyy-MM-dd
    private String filtroMedico = "";
    private String filtroEspecialidad = "";
    private String filtroTexto = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_citas);

        if (getSupportActionBar()!=null) {
            getSupportActionBar().setTitle("Mis Citas");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // UI
        recycler = findViewById(R.id.recyclerCitas);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adaptador = new AdaptadorCitas(vista, new AdaptadorCitas.OnCitaActionListener() {
            @Override public void onReprogramar(CitaModel c) { reprogramar(c); }
            @Override public void onCancelar(CitaModel c)   { cancelar(c); }
            @Override public void onConfirmar(CitaModel c)  { confirmar(c); }
        });
        recycler.setAdapter(adaptador);

        acBuscar = findViewById(R.id.acBuscar);
        acMedico = findViewById(R.id.acMedico);
        acEspecialidad = findViewById(R.id.acEspecialidad);
        tvFechaFiltro = findViewById(R.id.tvFechaFiltro);
        btnElegirFecha = findViewById(R.id.btnElegirFecha);
        btnLimpiar = findViewById(R.id.btnLimpiar);

        // Adapters de sugerencias
        sugBuscarAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        sugMedicosAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        sugEspecAdapter   = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        acBuscar.setAdapter(sugBuscarAdapter);
        acMedico.setAdapter(sugMedicosAdapter);
        acEspecialidad.setAdapter(sugEspecAdapter);

        // Listeners filtros
        acBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                filtroTexto = s == null ? "" : s.toString().trim();
                actualizarSugerenciasBusqueda(filtroTexto);
                aplicarFiltros();
            }
        });

        acMedico.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                filtroMedico = s == null ? "" : s.toString().trim();
                aplicarFiltros();
            }
        });

        acEspecialidad.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                filtroEspecialidad = s == null ? "" : s.toString().trim();
                aplicarFiltros();
            }
        });

        btnElegirFecha.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            DatePickerDialog dp = new DatePickerDialog(this,
                    (view, y, m, d) -> {
                        filtroFecha = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
                        tvFechaFiltro.setText(filtroFecha);
                        tvFechaFiltro.setTextColor(0xFF111111);
                        aplicarFiltros();
                    },
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            dp.getDatePicker().setMinDate(0); // permitir elegir cualquier fecha (ajusta si quieres min = hoy)
            dp.show();
        });

        btnLimpiar.setOnClickListener(v -> {
            filtroTexto = "";
            filtroMedico = "";
            filtroEspecialidad = "";
            filtroFecha = null;
            acBuscar.setText("");
            acMedico.setText("");
            acEspecialidad.setText("");
            tvFechaFiltro.setText("Fecha (yyyy-MM-dd)");
            tvFechaFiltro.setTextColor(0xFF666666);
            aplicarFiltros();
        });

        cargarCitas();
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }

    private int getIdPaciente() {
        return getSharedPreferences("session", MODE_PRIVATE).getInt("id", 0);
    }

    private void cargarCitas() {
        int idPaciente = getIdPaciente();
        if (idPaciente == 0) { finish(); return; }

        String url = Constantes.LISTAR_CITAS_PACIENTE_TODAS + "?id_paciente=" + idPaciente;
        Log.d("MIS_CITAS","GET "+url);

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                resp -> {
                    if (!"ok".equals(resp.optString("estado"))) {
                        lista.clear();
                        vista.clear();
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
                    // Sugerencias base
                    reconstruirSugerenciasBase();
                    // Orden inicial por fecha/hora asc
                    lista.sort(Comparator.comparing(this::toDate, Comparator.nullsLast(Comparator.naturalOrder())));
                    // Primera vista = todo
                    aplicarFiltros();
                },
                err -> Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(req);
    }

    private Date toDate(CitaModel c) {
        try { return sdf.parse(c.getFechaISO() + " " + c.getHora24()); } catch (ParseException e) { return null; }
    }

    // Construye sets únicos para llenar adapters de sugerencias
    private void reconstruirSugerenciasBase() {
        setMedicos.clear(); setEspecs.clear(); setLugares.clear();

        for (CitaModel c : lista) {
            if (c.getMedico()!=null && !c.getMedico().trim().isEmpty()) setMedicos.add(c.getMedico().trim());
            String esp = firstNonEmpty(c.getEspecialidad(), c.getEspecialidadNombre(), c.getServicio());
            if (esp!=null && !esp.isEmpty()) setEspecs.add(esp);
            String consultorio = firstNonEmpty(c.getConsultorio(), c.getConsultorioNombre());
            String lugar = firstNonEmpty(c.getLugar(), joinComma(consultorio, c.getPiso()));
            if (lugar!=null && !lugar.isEmpty()) setLugares.add(lugar);
        }

        // Carga adapters
        sugMedicosAdapter.clear(); sugMedicosAdapter.addAll(setMedicos); sugMedicosAdapter.notifyDataSetChanged();
        sugEspecAdapter.clear();   sugEspecAdapter.addAll(setEspecs);   sugEspecAdapter.notifyDataSetChanged();

        // Mezcla para búsqueda general
        List<String> base = new ArrayList<>();
        base.addAll(setMedicos);
        base.addAll(setEspecs);
        base.addAll(setLugares);
        // No llenamos aquí; se actualiza en tiempo real en actualizarSugerenciasBusqueda
        sugBuscarAdapter.clear(); sugBuscarAdapter.addAll(base); sugBuscarAdapter.notifyDataSetChanged();
    }

    // Sugerencias aproximadas según lo que escriba el usuario en "Buscar"
    private void actualizarSugerenciasBusqueda(String q) {
        String nq = norm(q);
        List<String> base = new ArrayList<>();
        base.addAll(setMedicos);
        base.addAll(setEspecs);
        base.addAll(setLugares);

        if (nq.isEmpty()) {
            sugBuscarAdapter.clear();
            sugBuscarAdapter.addAll(base.subList(0, Math.min(10, base.size())));
            sugBuscarAdapter.notifyDataSetChanged();
            return;
        }

        // Aproximados: empieza con, contiene, y “parecidos” simples (distancia acotada)
        List<String> sugerencias = new ArrayList<>();
        for (String s : base) {
            String ns = norm(s);
            if (ns.startsWith(nq) || ns.contains(nq) || isLooseSimilar(ns, nq)) {
                sugerencias.add(s);
                if (sugerencias.size() >= 10) break;
            }
        }
        sugBuscarAdapter.clear();
        sugBuscarAdapter.addAll(sugerencias);
        sugBuscarAdapter.notifyDataSetChanged();
    }

    // Filtro principal: fecha, médico, especialidad y texto libre
    private void aplicarFiltros() {
        vista.clear();
        for (CitaModel c : lista) {
            if (!matchFecha(c, filtroFecha)) continue;
            if (!matchText(c.getMedico(), filtroMedico)) continue;

            String esp = firstNonEmpty(c.getEspecialidad(), c.getEspecialidadNombre(), c.getServicio());
            if (!matchText(esp, filtroEspecialidad)) continue;

            if (!matchGlobal(c, filtroTexto)) continue;

            vista.add(c);
        }

        // Ranking básico: los que “empiezan con” el texto buscado primero
        if (!filtroTexto.isEmpty()) {
            final String nq = norm(filtroTexto);
            vista.sort((a, b) -> {
                int sa = scoreMatch(a, nq);
                int sb = scoreMatch(b, nq);
                // Desc por score; si empatan, por fecha asc
                int cmp = Integer.compare(sb, sa);
                if (cmp != 0) return cmp;
                Date da = toDate(a), db = toDate(b);
                if (da == null && db == null) return 0;
                if (da == null) return 1;
                if (db == null) return -1;
                return da.compareTo(db);
            });
        } else {
            // Por fecha/hora asc si no hay texto
            vista.sort(Comparator.comparing(this::toDate, Comparator.nullsLast(Comparator.naturalOrder())));
        }

        adaptador.notifyDataSetChanged();
    }

    private boolean matchFecha(CitaModel c, String yyyyMMdd) {
        if (yyyyMMdd == null || yyyyMMdd.isEmpty()) return true;
        String f = c.getFechaISO();
        return f != null && f.equals(yyyyMMdd);
    }

    private boolean matchText(String field, String filter) {
        if (filter == null || filter.trim().isEmpty()) return true;
        if (field == null) return false;
        String nf = norm(filter), ns = norm(field);
        return ns.contains(nf) || isLooseSimilar(ns, nf);
    }

    private boolean matchGlobal(CitaModel c, String q) {
        if (q == null || q.trim().isEmpty()) return true;
        String nq = norm(q);

        String medico = norm(c.getMedico());
        String esp = norm(firstNonEmpty(c.getEspecialidad(), c.getEspecialidadNombre(), c.getServicio()));
        String consultorio = norm(firstNonEmpty(c.getConsultorio(), c.getConsultorioNombre()));
        String piso = norm(c.getPiso());
        String lugar = norm(c.getLugar());
        String fecha = norm(c.getFechaISO());
        String hora = norm(c.getHora24());

        return containsAny(nq, medico, esp, consultorio, piso, lugar, fecha, hora);
    }

    private boolean containsAny(String q, String... fields) {
        for (String f : fields) {
            if (f != null && (f.contains(q) || isLooseSimilar(f, q))) return true;
        }
        return false;
    }

    // Scoring sencillo para ordenar resultados cuando hay texto libre
    private int scoreMatch(CitaModel c, String nq) {
        int s = 0;
        String medico = norm(c.getMedico());
        String esp = norm(firstNonEmpty(c.getEspecialidad(), c.getEspecialidadNombre(), c.getServicio()));
        String lugar = norm(firstNonEmpty(c.getLugar(), firstNonEmpty(c.getConsultorio(), c.getConsultorioNombre())));
        if (medico != null) { if (medico.startsWith(nq)) s += 3; else if (medico.contains(nq)) s += 2; }
        if (esp != null)    { if (esp.startsWith(nq))    s += 2; else if (esp.contains(nq))    s += 1; }
        if (lugar != null)  { if (lugar.startsWith(nq))  s += 1; else if (lugar.contains(nq))  s += 1; }
        return s;
    }

    // Similaridad laxa: permite 1 edición o inversión de 2 letras para consultas cortas
    private boolean isLooseSimilar(String text, String query) {
        if (text == null || query == null) return false;
        if (query.length() <= 3) return false;
        // transposición simple
        if (text.contains(swapAdjacent(query))) return true;
        // distancia Levenshtein acotada a 1-2 para strings pequeños
        int d = levenshteinLimited(text, query, 2);
        return d >= 0 && d <= 2;
    }

    private static String swapAdjacent(String s) {
        if (s.length() < 2) return s;
        char[] a = s.toCharArray();
        char t = a[0]; a[0] = a[1]; a[1] = t;
        return new String(a);
    }

    // Levenshtein con límite; retorna -1 si supera el límite
    private static int levenshteinLimited(String a, String b, int limit) {
        int n = a.length(), m = b.length();
        if (Math.abs(n - m) > limit) return -1;
        int[] prev = new int[m + 1], cur = new int[m + 1];
        for (int j = 0; j <= m; j++) prev[j] = j;
        for (int i = 1; i <= n; i++) {
            cur[0] = i;
            int rowMin = cur[0];
            char ca = a.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                int cost = (ca == b.charAt(j - 1)) ? 0 : 1;
                cur[j] = Math.min(Math.min(cur[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
                if (cur[j] < rowMin) rowMin = cur[j];
            }
            if (rowMin > limit) return -1;
            int[] tmp = prev; prev = cur; cur = tmp;
        }
        return prev[m];
    }

    private static String joinComma(String a, String b) {
        a = a == null ? "" : a.trim();
        b = b == null ? "" : b.trim();
        if (!a.isEmpty() && !b.isEmpty()) return a + ", " + b;
        return !a.isEmpty() ? a : b;
    }

    private static String firstNonEmpty(String... xs) {
        if (xs == null) return null;
        for (String s : xs) if (s != null && !s.trim().isEmpty()) return s.trim();
        return null;
    }

    private static String norm(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD);
        n = n.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return n.toLowerCase(Locale.US).trim();
    }

    // ===== Acciones =====

    private boolean esFutura(CitaModel c) {
        try {
            Date d = sdf.parse(c.getFechaISO() + " " + c.getHora24());
            return d != null && d.after(new Date());
        } catch (Exception e) { return false; }
    }

    private void reprogramar(CitaModel c) {
        String est = c.getEstado() == null ? "" : c.getEstado().toLowerCase(Locale.US);
        if ("confirmada".equals(est)) { Toast.makeText(this, "No puedes reprogramar una cita confirmada", Toast.LENGTH_SHORT).show(); return; }
        if ("cancelada".equals(est))  { Toast.makeText(this, "No puedes reprogramar una cita cancelada", Toast.LENGTH_SHORT).show(); return; }
        if (!esFutura(c))             { Toast.makeText(this, "La cita ya pasó, no se puede reprogramar", Toast.LENGTH_SHORT).show(); return; }

        Calendar cal = Calendar.getInstance();
        DatePickerDialog dp = new DatePickerDialog(this, (view, y, m, d) -> {
            String nuevaFecha = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
            int idMedico = c.getIdMedico();
            String url = Constantes.LISTAR_HORAS + "?id_medico=" + idMedico + "&fecha=" + nuevaFecha;

            JsonObjectRequest horasReq = new JsonObjectRequest(Request.Method.GET, url, null,
                    jr -> {
                        if (!"ok".equals(jr.optString("estado"))) { Toast.makeText(this, "No hay horarios", Toast.LENGTH_SHORT).show(); return; }
                        JSONArray horas = jr.optJSONArray("horas");
                        List<String> libres = new ArrayList<>();
                        if (horas != null) {
                            for (int i = 0; i < horas.length(); i++) {
                                JSONObject o = horas.optJSONObject(i);
                                if (o != null) {
                                    int disp = o.optInt("disponible", 1);
                                    String h = o.optString("hora", null);
                                    if (h != null && disp == 1) {
                                        if (h.length()==8) h = h.substring(0,5);
                                        libres.add(h);
                                    }
                                } else {
                                    String h2 = horas.optString(i, null);
                                    if (h2 != null) {
                                        if (h2.length()==8) h2 = h2.substring(0,5);
                                        libres.add(h2);
                                    }
                                }
                            }
                        }
                        if (libres.isEmpty()) { Toast.makeText(this, "Sin horarios libres", Toast.LENGTH_SHORT).show(); return; }

                        String[] arr = libres.toArray(new String[0]);
                        new androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("Selecciona hora")
                                .setItems(arr, (dialog, which) -> {
                                    String horaSel = arr[which];
                                    StringRequest req2 = new StringRequest(Request.Method.POST, Constantes.REPROGRAMAR_CITA,
                                            resp -> {
                                                boolean ok; String msg = "No se pudo reprogramar";
                                                try { JSONObject j = new JSONObject(resp); ok = "ok".equals(j.optString("estado")); if (!ok) msg = j.optString("mensaje", msg); }
                                                catch (Exception e) { ok = "ok".equals(resp.trim()); }
                                                if (ok) {
                                                    c.setFecha(nuevaFecha);
                                                    c.setHora(horaSel);
                                                    c.setEstado("reprogramada");
                                                    adaptador.notifyDataSetChanged();
                                                    Toast.makeText(this, "Cita reprogramada", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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
                                    Volley.newRequestQueue(this).add(req2);
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

    private void confirmar(CitaModel c) {
        int idPaciente = getSharedPreferences("session", MODE_PRIVATE).getInt("id", 0);
        if (idPaciente == 0) { Toast.makeText(this, "Sesión inválida", Toast.LENGTH_SHORT).show(); return; }
        if ("confirmada".equalsIgnoreCase(c.getEstado())) { Toast.makeText(this, "Ya confirmada", Toast.LENGTH_SHORT).show(); return; }

        StringRequest req = new StringRequest(Request.Method.POST, Constantes.CONFIRMAR_CITA_PACIENTE,
                resp -> {
                    boolean ok = "ok".equals(resp.trim());
                    if (!ok) { try { ok = "ok".equals(new JSONObject(resp).optString("estado")); } catch (Exception ignored) {} }
                    if (ok) {
                        c.setEstado("confirmada");
                        adaptador.notifyDataSetChanged();
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
}