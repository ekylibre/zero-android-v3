package com.ekylibre.android.adapters;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekylibre.android.R;
import com.ekylibre.android.database.pojos.Equipments;

import java.util.Arrays;
import java.util.List;


public class EquipmentAdapter extends RecyclerView.Adapter<EquipmentAdapter.ViewHolder> {

    private static final String TAG = EquipmentAdapter.class.getName();

    private List<Equipments> dataset;
    private List equipmentValues;
    private List equipmentKeys;
    private Context context;

    public EquipmentAdapter(Context context, List<Equipments> dataset) {
        this.dataset = dataset;
        this.context = context;
        this.equipmentValues = Arrays.asList(context.getResources().getStringArray(R.array.equipment_values));
        this.equipmentKeys = Arrays.asList(context.getResources().getStringArray(R.array.equipment_keys));
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView iconImageView, deleteImageView;
        TextView nameTextView, typeTextView;

        ViewHolder(View itemView) {
            super(itemView);

            iconImageView = itemView.findViewById(R.id.equipment_icon);
            nameTextView = itemView.findViewById(R.id.equipment_name);
            typeTextView = itemView.findViewById(R.id.equipment_type);
            deleteImageView = itemView.findViewById(R.id.equipment_delete);

            deleteImageView.setOnClickListener(view -> {
                Context context = itemView.getRootView().getContext();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Etes-vous sûr de vouloir supprimer l'outil ?");
                builder.setNegativeButton("non", (dialog, i) -> dialog.cancel());
                builder.setPositiveButton("oui", (dialog, i) -> {
                    int position = getAdapterPosition();
                    dataset.remove(position);
                    //notifyItemRemoved(position);
                    notifyDataSetChanged();
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            });
        }

        void display(Equipments item) {
            //iconImageView.setImageResource(R.drawable.ic_launcher_foreground);
            nameTextView.setText(item.equipment.get(0).name);
            if (!item.equipment.get(0).type.isEmpty()) {
                String equipment_type = (String) equipmentValues.get(equipmentKeys.indexOf(item.equipment.get(0).type.toLowerCase()));
                typeTextView.setText(equipment_type);
            }
            iconImageView.setImageResource(context.getResources().getIdentifier("tool_" + item.equipment.get(0).type.toLowerCase(), "drawable", context.getPackageName()));

        }
    }

    @NonNull
    @Override
    public EquipmentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_equipment_in_intervention, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.display(dataset.get(position));
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }
}
