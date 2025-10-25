package com.example.pruebat2moviles;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorMedicos extends RecyclerView.Adapter<AdaptadorMedicos.VH> {
    public interface OnMedicoClick {
        void onClick(MedicoModel m);
    }

    private final List<MedicoModel> data = new ArrayList<>();
    private final OnMedicoClick listener;

    public AdaptadorMedicos(OnMedicoClick l) { this.listener = l; }

    public void setData(List<MedicoModel> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medico, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        MedicoModel m = data.get(pos);
        h.tvNombre.setText("Dr.(a) " + m.nombre);
        h.tvEsp.setText(m.especialidad != null ? m.especialidad : "");
        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(m); });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvNombre, tvEsp;
        VH(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreMedico);
            tvEsp = itemView.findViewById(R.id.tvEspecialidadMedico);
        }
    }
}