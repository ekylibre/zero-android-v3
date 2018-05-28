package com.ekylibre.android.adapters;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekylibre.android.R;
import com.ekylibre.android.database.pojos.Persons;

import java.util.List;


public class HarvestAdapter extends RecyclerView.Adapter<HarvestAdapter.ViewHolder> {

    private static final String TAG = HarvestAdapter.class.getName();

    private List<Persons> dataset;

    public HarvestAdapter(List<Persons> dataset) {
        this.dataset = dataset;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView deleteImageView;
        AppCompatEditText quantityEditText, numberEditText;
        AppCompatSpinner unitSpinner, storageSpinner;

        ViewHolder(View itemView) {
            super(itemView);

            quantityEditText = itemView.findViewById(R.id.harvest_quantity_edit);
            numberEditText = itemView.findViewById(R.id.harvest_number_edit);
            deleteImageView = itemView.findViewById(R.id.harvest_delete);
            storageSpinner = itemView.findViewById(R.id.stock_place_spinner);
            unitSpinner = itemView.findViewById(R.id.harvest_unit_spinner);

            deleteImageView.setOnClickListener(view -> {
                Context context = itemView.getRootView().getContext();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Etes-vous sûr de vouloir supprimer la récolte ?");
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

        void display(Persons item) {
            //iconImageView.setImageResource(R.drawable.ic_launcher_foreground);
//            firstNameTextView.setText(item.person.get(0).first_name);
//            lastNameTextView.setText(item.person.get(0).last_name);
//            driverSwitch.setChecked(item.inter.is_driver);
        }
    }

    @NonNull
    @Override
    public HarvestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_harvest, parent, false);

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
