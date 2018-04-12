package com.ekylibre.android.adapters;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekylibre.android.R;
import com.ekylibre.android.database.models.Person;
import com.ekylibre.android.database.pojos.Materials;
import com.ekylibre.android.database.pojos.Persons;
import com.ekylibre.android.utils.QuantityCalculs;

import java.util.Arrays;
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

            deleteImageView.setOnClickListener(view -> {
                Context context = itemView.getRootView().getContext();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Etes-vous sûr de vouloir supprimer la personne ?");
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

            driverSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
                Persons person = dataset.get(getAdapterPosition());
                person.inter.is_driver = compoundButton.isChecked();
            });
        }

        void display(Persons item) {
            iconImageView.setImageResource(R.drawable.ic_launcher_foreground);
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
