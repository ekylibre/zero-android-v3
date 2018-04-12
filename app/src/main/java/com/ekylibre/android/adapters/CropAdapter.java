package com.ekylibre.android.adapters;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekylibre.android.R;
import com.ekylibre.android.database.pojos.Crops;

import java.util.List;


public class CropAdapter extends RecyclerView.Adapter<CropAdapter.ViewHolder> {

    private static final String TAG = CropAdapter.class.getName();

    private List<Crops> dataset;

    public CropAdapter(List<Crops> dataset) {
        this.dataset = dataset;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView arrowImageView;
        TextView nameTextView, areaTextView;
        CheckBox selectedCheckBox;
        LinearLayoutCompat cropContainer;

        ViewHolder(View itemView, View cropView) {
            super(itemView);

//            cropContainer = itemView.findViewById(R.id.crop_container);
//
//            firstNameTextView = itemView.findViewById(R.id.person_firstname);
//            lastNameTextView = itemView.findViewById(R.id.person_lastname);
//            selectedCheckBox = itemView.findViewById(R.id.person_is_driver);
//            deleteImageView = itemView.findViewById(R.id.person_delete);
//
//            deleteImageView.setOnClickListener(view -> {
//                Context context = itemView.getRootView().getContext();
//                AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                builder.setMessage("Etes-vous sûr de vouloir supprimer la personne ?");
//                builder.setNegativeButton("non", (dialog, i) -> dialog.cancel());
//                builder.setPositiveButton("oui", (dialog, i) -> {
//                    int position = getAdapterPosition();
//                    dataset.remove(position);
//                    //notifyItemRemoved(position);
//                    notifyDataSetChanged();
//                });
//                AlertDialog dialog = builder.create();
//                dialog.show();
//            });
//
//            driverSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
//                Crops person = dataset.get(getAdapterPosition());
//                person.inter.is_driver = compoundButton.isChecked();
//            });
        }

        void display(Crops item) {

//            cropContainer.addView(cropView);
//
//            iconImageView.setImageResource(R.drawable.ic_launcher_foreground);
//            firstNameTextView.setText(item.crop.get(0).first_name);
//            lastNameTextView.setText(item.crop.get(0).last_name);
//            driverSwitch.setChecked(item.inter.is_driver);
        }
    }

    @NonNull
    @Override
    public CropAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plot, parent, false);

        View cropView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_crop, parent, false);

        return new ViewHolder(view, cropView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //holder.display(dataset.get(position));
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }
}
