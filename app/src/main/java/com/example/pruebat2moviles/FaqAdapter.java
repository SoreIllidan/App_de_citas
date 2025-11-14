package com.example.pruebat2moviles;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FaqAdapter extends RecyclerView.Adapter<FaqAdapter.VH> {

    private final List<FaqItem> data;

    public FaqAdapter(List<FaqItem> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_faq, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        FaqItem f = data.get(pos);
        h.tvQ.setText(f.pregunta);
        h.tvA.setText(f.respuesta);
        h.tvA.setVisibility(f.expandido ? View.VISIBLE : View.GONE);
        h.ivArrow.setRotation(f.expandido ? 180f : 0f);

        h.itemView.setOnClickListener(v -> {
            f.expandido = !f.expandido;
            notifyItemChanged(h.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvQ, tvA;
        ImageView ivArrow;
        VH(@NonNull View itemView) {
            super(itemView);
            tvQ = itemView.findViewById(R.id.tvPregunta);
            tvA = itemView.findViewById(R.id.tvRespuesta);
            ivArrow = itemView.findViewById(R.id.ivArrow);
        }
    }
}