package com.example.pruebat2moviles;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvKpiTotal, tvKpiConfirmadas, tvKpiCanceladas;
    private PieChart chartEstados;
    private BarChart chartUltimos7;
    private LineChart chart12Meses;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin • Dashboard");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        bind();
        cargarDashboard();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void bind() {
        tvKpiTotal = findViewById(R.id.tvKpiTotal);
        tvKpiConfirmadas = findViewById(R.id.tvKpiConfirmadas);
        tvKpiCanceladas = findViewById(R.id.tvKpiCanceladas);
        chartEstados = findViewById(R.id.chartEstados);
        chartUltimos7 = findViewById(R.id.chartUltimos7);
        chart12Meses = findViewById(R.id.chart12Meses);

        disableDesc(chartEstados);
        disableDesc(chartUltimos7);
        disableDesc(chart12Meses);
    }

    private void disableDesc(com.github.mikephil.charting.charts.Chart<?> c) {
        Description d = new Description();
        d.setText("");
        c.setDescription(d);
        c.getLegend().setEnabled(true);
    }

    private void cargarDashboard() {
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                Constantes.ESTADISTICAS_CITAS,
                null,
                resp -> {
                    if (!"ok".equals(resp.optString("estado"))) {
                        Toast.makeText(this, "No se pudieron cargar estadísticas", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // KPIs
                    JSONObject kpis = resp.optJSONObject("kpis");
                    if (kpis != null) {
                        tvKpiTotal.setText(String.valueOf(kpis.optInt("total", 0)));
                        tvKpiConfirmadas.setText(String.valueOf(kpis.optInt("confirmadas", 0)));
                        tvKpiCanceladas.setText(String.valueOf(kpis.optInt("canceladas", 0)));
                    }

                    // Estados (pie)
                    JSONArray estados = resp.optJSONArray("por_estado");
                    if (estados != null) {
                        List<PieEntry> entries = new ArrayList<>();
                        List<Integer> colors = new ArrayList<>();
                        for (int i = 0; i < estados.length(); i++) {
                            JSONObject o = estados.optJSONObject(i);
                            String est = o.optString("estado", "otros");
                            int total = o.optInt("total", 0);
                            if (total <= 0) continue;
                            entries.add(new PieEntry(total, est));
                            colors.add(colorForEstado(est));
                        }
                        PieDataSet ds = new PieDataSet(entries, "");
                        ds.setColors(colors);
                        ds.setValueTextColor(Color.BLACK);
                        ds.setValueTextSize(12f);
                        chartEstados.setData(new PieData(ds));
                        chartEstados.invalidate();
                    }

                    // Últimos 7 días (bar)
                    JSONArray ult7 = resp.optJSONArray("ultimos_7_dias");
                    if (ult7 != null) {
                        List<BarEntry> bars = new ArrayList<>();
                        List<String> labels = new ArrayList<>();
                        for (int i = 0; i < ult7.length(); i++) {
                            JSONObject o = ult7.optJSONObject(i);
                            bars.add(new BarEntry(i, o.optInt("total", 0)));
                            labels.add(o.optString("fecha", ""));
                        }
                        BarDataSet ds = new BarDataSet(bars, "Citas por día");
                        ds.setColor(Color.parseColor("#3C98B3"));
                        ds.setValueTextColor(Color.BLACK);
                        BarData data = new BarData(ds);
                        data.setBarWidth(0.9f);
                        chartUltimos7.setData(data);
                        chartUltimos7.getXAxis().setGranularity(1f);
                        chartUltimos7.getXAxis().setLabelCount(labels.size());
                        chartUltimos7.getAxisRight().setEnabled(false);
                        chartUltimos7.setFitBars(true);
                        chartUltimos7.invalidate();
                    }

                    // Últimos 12 meses (line)
                    JSONArray m12 = resp.optJSONArray("ultimos_12_meses");
                    if (m12 != null) {
                        List<Entry> pts = new ArrayList<>();
                        List<String> labels = new ArrayList<>();
                        for (int i = 0; i < m12.length(); i++) {
                            JSONObject o = m12.optJSONObject(i);
                            pts.add(new Entry(i, o.optInt("total", 0)));
                            labels.add(o.optString("mes", ""));
                        }
                        LineDataSet ds = new LineDataSet(pts, "Citas por mes");
                        ds.setColor(Color.parseColor("#2D8CAE"));
                        ds.setCircleColor(Color.parseColor("#2D8CAE"));
                        ds.setValueTextColor(Color.BLACK);
                        ds.setLineWidth(2f);
                        ds.setCircleRadius(3f);
                        chart12Meses.setData(new LineData(ds));
                        chart12Meses.getAxisRight().setEnabled(false);
                        chart12Meses.invalidate();
                    }
                },
                err -> Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(req);
    }

    private int colorForEstado(String e) {
        String x = e.toLowerCase();
        if (x.contains("confirm")) return Color.parseColor("#2E7D32");
        if (x.contains("cancel")) return Color.parseColor("#C62828");
        if (x.contains("reprogram")) return Color.parseColor("#F57C00");
        if (x.contains("complet")) return Color.parseColor("#1565C0");
        if (x.contains("program")) return Color.parseColor("#6D4C41");
        return Color.GRAY;
    }
}