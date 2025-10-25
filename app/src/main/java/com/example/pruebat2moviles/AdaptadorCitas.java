package com.example.pruebat2moviles;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdaptadorCitas extends RecyclerView.Adapter<AdaptadorCitas.ViewHolder> {

    public interface OnCitaActionListener {
        void onReprogramar(CitaModel cita);
        void onCancelar(CitaModel cita);
    }

    private final List<CitaModel> lista;
    private final OnCitaActionListener listener;

    public AdaptadorCitas(List<CitaModel> lista, OnCitaActionListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdaptadorCitas.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cita, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorCitas.ViewHolder holder, int position) {
        CitaModel c = lista.get(position);
        holder.tvMedico.setText(c.getMedico());
        holder.tvEspecialidad.setText(c.getEspecialidad());
        holder.tvFecha.setText(c.getFecha());
        holder.tvConsultorio.setText(c.getConsultorio());

        int normal = Color.BLACK;
        holder.tvMedico.setTextColor(normal);
        holder.tvFecha.setTextColor(normal);
        holder.tvMedico.setPaintFlags(holder.tvMedico.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        holder.tvFecha.setPaintFlags(holder.tvFecha.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        holder.tvEstado.setVisibility(View.GONE);

        String estado = c.getEstado() == null ? "" : c.getEstado().toLowerCase();
        boolean cancelada = "cancelada".equals(estado);
        boolean reprogramada = estado.startsWith("reprogramad"); // reprogramado/reprogramada

        if (cancelada) {
            holder.tvEstado.setVisibility(View.VISIBLE);
            holder.tvEstado.setText("Cita cancelada");
            holder.tvEstado.setTextColor(Color.RED);

            holder.tvMedico.setTextColor(Color.RED);
            holder.tvFecha.setTextColor(Color.RED);
            holder.tvMedico.setPaintFlags(holder.tvMedico.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvFecha.setPaintFlags(holder.tvFecha.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            holder.btnReprogramar.setEnabled(false);
            holder.btnCancelar.setEnabled(false);
            holder.btnReprogramar.setAlpha(0.5f);
            holder.btnCancelar.setAlpha(0.5f);
        } else {
            holder.btnReprogramar.setEnabled(true);
            holder.btnCancelar.setEnabled(true);
            holder.btnReprogramar.setAlpha(1f);
            holder.btnCancelar.setAlpha(1f);

            if (reprogramada) {
                holder.tvEstado.setVisibility(View.VISIBLE);
                holder.tvEstado.setText("Reprogramado");
                holder.tvEstado.setTextColor(Color.parseColor("#1E88E5"));
            }
        }

        holder.btnReprogramar.setOnClickListener(v -> { if (listener != null) listener.onReprogramar(c); });
        holder.btnCancelar.setOnClickListener(v -> { if (listener != null) listener.onCancelar(c); });
    }

    @Override
    public int getItemCount() { return lista.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedico, tvEspecialidad, tvFecha, tvConsultorio, tvEstado;
        Button btnReprogramar, btnCancelar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            tvMedico = itemView.findViewById(R.id.tvMedico);
            tvEspecialidad = itemView.findViewById(R.id.tvEspecialidad);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvConsultorio = itemView.findViewById(R.id.tvConsultorio);
            btnReprogramar = itemView.findViewById(R.id.btnItemReprogramar);
            btnCancelar = itemView.findViewById(R.id.btnItemCancelar);
        }
    }
}