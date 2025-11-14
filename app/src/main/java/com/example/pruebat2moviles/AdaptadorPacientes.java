package com.example.pruebat2moviles;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorPacientes extends RecyclerView.Adapter<AdaptadorPacientes.VH> {

    private final List<PacienteModel> data = new ArrayList<>();

    public void setData(List<PacienteModel> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_paciente, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        PacienteModel p = data.get(pos);
        h.tvNombre.setText(p.nombre);
        h.tvDni.setText("DNI: " + (p.dni == null ? "" : p.dni));
        h.tvCorreo.setText(p.correo == null ? "" : p.correo);
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDni, tvCorreo;
        VH(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombrePaciente);
            tvDni = itemView.findViewById(R.id.tvDniPaciente);
            tvCorreo = itemView.findViewById(R.id.tvCorreoPaciente);
        }
    }
}