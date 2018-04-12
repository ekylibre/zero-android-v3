package com.ekylibre.android.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ekylibre.android.R;
import com.ekylibre.android.SelectEquipmentFragment;
import com.ekylibre.android.database.models.Equipment;
import com.ekylibre.android.database.pojos.Equipments;
import com.ekylibre.android.database.relations.InterventionEquipment;

import java.util.Collections;
import java.util.List;


public class SelectEquipmentAdapter extends RecyclerView.Adapter<SelectEquipmentAdapter.ViewHolder> {

    private static final String TAG = SelectEquipmentAdapter.class.getName();

    private List<Equipment> dataset;
    private SelectEquipmentFragment.OnFragmentInteractionListener fragmentListener;

    public SelectEquipmentAdapter(List<Equipment> dataset, SelectEquipmentFragment.OnFragmentInteractionListener fragmentListener) {
        this.dataset = dataset;
        this.fragmentListener = fragmentListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView, typeTextView;
        Equipment equipment;

        ViewHolder(View itemView) {
            super(itemView);

            nameTextView = itemView.findViewById(R.id.equipment_name);
            typeTextView = itemView.findViewById(R.id.equipment_type);

            itemView.setOnClickListener(v -> {
                Equipments selection = new Equipments();
                selection.equipment = Collections.singletonList(equipment);
                selection.inter = new InterventionEquipment(equipment.id);
                fragmentListener.onFragmentInteraction(selection);
            });
        }

        void display(Equipment item) {
            equipment = item;
            nameTextView.setText(item.name);
            typeTextView.setText(item.type);
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