package com.ekylibre.android.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ekylibre.android.R;
import com.ekylibre.android.fragments.SelectEquipmentFragment;
import com.ekylibre.android.database.models.Equipment;
import com.ekylibre.android.database.pojos.Equipments;
import com.ekylibre.android.database.relations.InterventionEquipment;
import com.ekylibre.android.utils.Enums;

import java.util.Collections;
import java.util.List;


public class SelectEquipmentAdapter extends RecyclerView.Adapter<SelectEquipmentAdapter.ViewHolder> {

    private static final String TAG = "SelectEquipmentAdapter";

    private List<Integer> selectedEquipments;
    private List<Equipment> dataset;
    private Context context;

    private SelectEquipmentFragment.OnFragmentInteractionListener fragmentListener;


    public SelectEquipmentAdapter(Context context, List<Equipment> dataset, List<Integer> selectedEquipments,
                                  SelectEquipmentFragment.OnFragmentInteractionListener fragmentListener) {
        this.dataset = dataset;
        this.selectedEquipments = selectedEquipments;
        this.context = context;
        this.fragmentListener = fragmentListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView, typeTextView;
        AppCompatImageView typeImageView;
        Equipment equipment;
        View.OnClickListener onClick;

        ViewHolder(View itemView) {
            super(itemView);

            nameTextView = itemView.findViewById(R.id.equipment_name);
            typeTextView = itemView.findViewById(R.id.equipment_type);
            typeImageView = itemView.findViewById(R.id.equipment_icon);

            onClick = v -> {
                Equipments selection = new Equipments();
                selection.equipment = Collections.singletonList(equipment);
                selection.inter = new InterventionEquipment(equipment.id);
                fragmentListener.onFragmentInteraction(selection);
            };
        }

        void display(Equipment item) {
            // Set data
            equipment = item;
            nameTextView.setText(item.name);
            StringBuilder sb = new StringBuilder();
            sb.append(Enums.EQUIMPMENT_NAMES.get(Enums.EQUIMPMENT_TYPES.indexOf(item.type)));
            if (item.number != null && !item.number.isEmpty())
                sb.append(String.format(" #%s", item.number));
            typeTextView.setText(sb);
            Integer iconRessource = context.getResources().getIdentifier("tool_" + item.type.toLowerCase(), "drawable", context.getPackageName());
            if (iconRessource != 0)
                typeImageView.setImageResource(iconRessource);

            // Adapt layout according to selected or not
            if (selectedEquipments.contains(item.id)) {
                itemView.setOnClickListener(null);
                //itemView.setBackground(context.getResources().getDrawable(R.drawable.border_bottom_disabled));
                nameTextView.setTextColor(context.getResources().getColor(R.color.grey));
                typeTextView.setTextColor(context.getResources().getColor(R.color.grey));
                typeImageView.setColorFilter(context.getResources().getColor(R.color.grey));
                typeImageView.setBackgroundResource(R.drawable.background_white);
            } else {
                itemView.setOnClickListener(onClick);
                //itemView.setBackground(context.getResources().getDrawable(R.drawable.border_bottom));
                nameTextView.setTextColor(context.getResources().getColor(R.color.black));
                typeTextView.setTextColor(context.getResources().getColor(R.color.secondary_text));
                typeImageView.clearColorFilter();
                typeImageView.setBackgroundResource(R.drawable.background_grey);
            }
        }
    }

    @NonNull
    @Override
    public SelectEquipmentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_equipment, parent, false);

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