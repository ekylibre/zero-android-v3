package com.ekylibre.android.adapters;


import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekylibre.android.InterventionActivity;
import com.ekylibre.android.R;
import com.ekylibre.android.database.pojos.Equipments;
import com.ekylibre.android.utils.Enums;

import java.util.List;


public class EquipmentAdapter extends RecyclerView.Adapter<EquipmentAdapter.ViewHolder> {

    private static final String TAG = "EquipmentAdapter";

    private List<Equipments> dataset;
    private Context context;

    public EquipmentAdapter(Context context, List<Equipments> dataset) {
        this.dataset = dataset;
        this.context = context;
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

            if (InterventionActivity.validated) {
                deleteImageView.setVisibility(View.GONE);
            } else {
                deleteImageView.setOnClickListener(view -> {
                    Context context = itemView.getRootView().getContext();
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(R.string.delete_equipment_prompt);
                    builder.setNegativeButton(R.string.no, (dialog, i) -> dialog.cancel());
                    builder.setPositiveButton(R.string.yes, (dialog, i) -> {
                        dataset.remove(getAdapterPosition());
                        notifyDataSetChanged();  //notifyItemRemoved(position);
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                });
            }
        }

        void display(Equipments item) {
            nameTextView.setText(item.equipment.get(0).name);
            StringBuilder sb = new StringBuilder();
            sb.append(Enums.EQUIMPMENT_NAMES.get(Enums.EQUIMPMENT_TYPES.indexOf(item.equipment.get(0).type)));
            if (item.equipment.get(0).number != null && !item.equipment.get(0).number.isEmpty())
                sb.append(String.format(" #%s", item.equipment.get(0).number));
            typeTextView.setText(sb);
            Integer iconRessource = context.getResources().getIdentifier("tool_" + item.equipment.get(0).type.toLowerCase(), "drawable", context.getPackageName());
            if (iconRessource != 0)
                iconImageView.setImageResource(iconRessource);
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
