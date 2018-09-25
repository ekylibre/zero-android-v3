package com.ekylibre.android.adapters;


import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekylibre.android.InterventionActivity;
import com.ekylibre.android.R;
import com.ekylibre.android.database.pojos.Persons;

import java.util.List;


public class PersonAdapter extends RecyclerView.Adapter<PersonAdapter.ViewHolder> {

    private static final String TAG = PersonAdapter.class.getName();

    private List<Persons> dataset;

    public PersonAdapter(List<Persons> dataset) {
        this.dataset = dataset;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView iconImageView, deleteImageView;
        TextView firstNameTextView, lastNameTextView;
        SwitchCompat driverSwitch;

        ViewHolder(View itemView) {
            super(itemView);

            iconImageView = itemView.findViewById(R.id.person_icon);
            firstNameTextView = itemView.findViewById(R.id.person_firstname);
            lastNameTextView = itemView.findViewById(R.id.person_lastname);
            driverSwitch = itemView.findViewById(R.id.person_is_driver);
            deleteImageView = itemView.findViewById(R.id.person_delete);

            if (InterventionActivity.validated) {
                deleteImageView.setVisibility(View.GONE);
//                quantityEditText.setFocusable(false);
//                quantityEditText.setEnabled(false);
                driverSwitch.setEnabled(false);
            }
            else {
                deleteImageView.setOnClickListener(view -> {
                    Context context = itemView.getRootView().getContext();
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(R.string.delete_person_prompt);
                    builder.setNegativeButton(R.string.no, (dialog, i) -> dialog.cancel());
                    builder.setPositiveButton(R.string.yes, (dialog, i) -> {
                        int position = getAdapterPosition();
                        dataset.remove(position);
                        //notifyItemRemoved(position);
                        notifyDataSetChanged();
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                });

                driverSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
                    Persons person = dataset.get(getAdapterPosition());
                    person.inter.is_driver = compoundButton.isChecked();
                });
            }
        }

        void display(Persons item) {
            //iconImageView.setImageResource(R.drawable.ic_launcher_foreground);
            firstNameTextView.setText(item.person.get(0).first_name);
            lastNameTextView.setText(item.person.get(0).last_name);
            driverSwitch.setChecked(item.inter.is_driver);
        }
    }

    @NonNull
    @Override
    public PersonAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_person_in_intervention, parent, false);

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
