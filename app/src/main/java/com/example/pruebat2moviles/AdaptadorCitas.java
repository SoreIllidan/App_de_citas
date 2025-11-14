package com.example.pruebat2moviles;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdaptadorCitas extends RecyclerView.Adapter<AdaptadorCitas.VH> {

    public interface OnCitaActionListener {
        void onReprogramar(CitaModel c);
        void onCancelar(CitaModel c);
        void onConfirmar(CitaModel c);
    }

    private final List<CitaModel> data;
    private final OnCitaActionListener listener;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

    public AdaptadorCitas(List<CitaModel> data, OnCitaActionListener l) {
        this.data = data;
        this.listener = l;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cita_paciente, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Context ctx = h.itemView.getContext();
        CitaModel c = data.get(pos);

        // Básicos
        h.tvMedico.setText(nullToDash(c.getMedico()));
        h.tvFechaHora.setText(buildFechaHora(c));

        // Especialidad (si no hay, se oculta)
        String esp = firstNonEmpty(c.getEspecialidad(), c.getEspecialidadNombre(), c.getServicio());
        setTextOrGone(h.tvEspecialidad, esp);

        // Lugar / consultorio / piso (si no hay, se oculta)
        String consultorio = firstNonEmpty(c.getConsultorio(), c.getConsultorioNombre());
        String piso = nullToEmpty(c.getPiso());
        String lugar = firstNonEmpty(c.getLugar(), joinComma(consultorio, piso));
        setTextOrGone(h.tvLugar, lugar);

        // Estado y colores
        String est = c.getEstado() == null ? "" : c.getEstado().toLowerCase(Locale.US);
        boolean isCancelada = "cancelada".equals(est);
        boolean isConfirmada = "confirmada".equals(est);
        boolean isFutura = isFuture(c);

        h.tvEstado.setText(nullToDash(c.getEstado()));
        if (isCancelada) h.tvEstado.setTextColor(Color.RED);
        else if (isConfirmada) h.tvEstado.setTextColor(Color.parseColor("#2E7D32"));
        else h.tvEstado.setTextColor(Color.parseColor("#1E88E5"));

        boolean canReprogramar = !isCancelada && !isConfirmada && isFutura;
        boolean canCancelar    = !isCancelada && isFutura;
        boolean canConfirmar   = !isCancelada && !isConfirmada && isFutura;

        setEnabled(h.btnReprogramar, canReprogramar);
        setEnabled(h.btnCancelar, canCancelar);
        setEnabled(h.btnConfirmar, canConfirmar);

        h.btnReprogramar.setOnClickListener(v -> {
            if (!canReprogramar) {
                String msg = isConfirmada ? "No puedes reprogramar una cita confirmada"
                        : isCancelada ? "No puedes reprogramar una cita cancelada"
                        : "La cita ya pasó";
                Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
                return;
            }
            if (listener != null) listener.onReprogramar(c);
        });

        h.btnCancelar.setOnClickListener(v -> {
            if (!canCancelar) {
                Toast.makeText(ctx, "No puedes cancelar esta cita", Toast.LENGTH_SHORT).show();
                return;
            }
            if (listener != null) listener.onCancelar(c);
        });

        h.btnConfirmar.setOnClickListener(v -> {
            if (!canConfirmar) {
                Toast.makeText(ctx, isConfirmada ? "La cita ya está confirmada" : "No puedes confirmar esta cita", Toast.LENGTH_SHORT).show();
                return;
            }
            if (listener != null) listener.onConfirmar(c);
        });
    }

    private String buildFechaHora(CitaModel c) {
        String f = nullToEmpty(c.getFecha());
        String h = nullToEmpty(c.getHora());
        if (h.length() == 8) h = h.substring(0,5);
        if (!f.isEmpty() && !h.isEmpty()) return f + " - " + h;
        if (!f.isEmpty()) return f;
        return nullToDash(h);
    }

    private static String joinComma(String a, String b) {
        a = nullToEmpty(a); b = nullToEmpty(b);
        if (!a.isEmpty() && !b.isEmpty()) return a + ", " + b;
        if (!a.isEmpty()) return a;
        return b;
    }

    private static String firstNonEmpty(String... xs) {
        if (xs == null) return null;
        for (String s : xs) if (s != null && !s.trim().isEmpty()) return s.trim();
        return null;
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }
    private static String nullToDash(String s) { return s == null || s.isEmpty() ? "—" : s; }

    private boolean isFuture(CitaModel c) {
        try {
            String hora = c.getHora24();
            String fecha = c.getFechaISO();
            Date d = sdf.parse(fecha + " " + hora);
            return d != null && d.after(new Date());
        } catch (ParseException e) { return false; }
    }

    private void setEnabled(Button b, boolean enabled) {
        b.setEnabled(enabled);
        b.setAlpha(enabled ? 1f : 0.5f);
    }

    private static void setTextOrGone(TextView tv, String text) {
        if (tv == null) return;
        if (text == null || text.trim().isEmpty()) {
            tv.setVisibility(View.GONE);
        } else {
            tv.setText(text);
            tv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvMedico, tvEspecialidad, tvFechaHora, tvLugar, tvEstado;
        Button btnReprogramar, btnCancelar, btnConfirmar;
        VH(@NonNull View itemView) {
            super(itemView);
            tvEstado       = itemView.findViewById(R.id.tvEstadoItem);
            tvMedico       = itemView.findViewById(R.id.tvMedicoItem);
            tvEspecialidad = itemView.findViewById(R.id.tvEspecialidadItem); // NUEVO
            tvFechaHora    = itemView.findViewById(R.id.tvFechaHoraItem);
            tvLugar        = itemView.findViewById(R.id.tvLugarItem);         // NUEVO
            btnReprogramar = itemView.findViewById(R.id.btnReprogramar);
            btnCancelar    = itemView.findViewById(R.id.btnCancelar);
            btnConfirmar   = itemView.findViewById(R.id.btnConfirmar);
        }
    }
}