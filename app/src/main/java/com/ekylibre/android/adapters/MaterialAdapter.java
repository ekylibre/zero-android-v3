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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekylibre.android.R;
import com.ekylibre.android.database.models.Material;
import com.ekylibre.android.database.pojos.Materials;
import com.ekylibre.android.utils.Units;

import java.util.List;


public class MaterialAdapter extends RecyclerView.Adapter<MaterialAdapter.ViewHolder> {

    private Context context;
    private List<Materials> dataset;
    private InputMethodManager keyboardManager;

    public MaterialAdapter(Context context, List<Materials> dataset) {
        this.context = context;
        this.dataset = dataset;
        this.keyboardManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView deleteImageView;
        TextView nameTextView;
        EditText quantityEditText;
        AppCompatSpinner unitSpinner;
        SwitchCompat approximativeSwitch;

        ViewHolder(View itemView) {
            super(itemView);

            nameTextView = itemView.findViewById(R.id.item_name);
//            descTextView = itemView.findViewById(R.id.item_desc);
            quantityEditText = itemView.findViewById(R.id.item_quantity_edit);
            unitSpinner = itemView.findViewById(R.id.item_unit_spinner);
            approximativeSwitch = itemView.findViewById(R.id.item_approximated);
            deleteImageView = itemView.findViewById(R.id.item_delete);

            deleteImageView.setOnClickListener(view -> {
                //Context context = itemView.getRootView().getContext();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(R.string.delete_material_prompt);
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

            quantityEditText.setOnEditorActionListener((view, actionId, event) -> {
                String value = quantityEditText.getText().toString();
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    if (value.isEmpty())
                        return true;
                    else {
                        keyboardManager.hideSoftInputFromWindow(quantityEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        quantityEditText.clearFocus();
                    }
                }
                return false;
            });

            quantityEditText.setOnFocusChangeListener((v, hasFocus) -> {
                String value = quantityEditText.getText().toString();
                if (!hasFocus) {
                    if (value.isEmpty())
                        quantityEditText.requestFocus();
                    else {
                        Materials interMaterial = dataset.get(getLayoutPosition());
                        interMaterial.inter.quantity = Integer.valueOf(quantityEditText.getText().toString());
                    }
                }
            });

            approximativeSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
                Materials interMaterial = dataset.get(getAdapterPosition());
                interMaterial.inter.approximative_value = compoundButton.isChecked();
            });

            unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    dataset.get(getLayoutPosition()).inter.unit = Units.ALL_BASE_UNITS.get(position).key;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        void display(Materials item) {

            String quantityAsString = String.valueOf(item.inter.quantity);
            nameTextView.setText(item.material.get(0).name);
//            if (item.material.get(0).description != null)
//                descTextView.setText(item.material.get(0).description);
            quantityEditText.setText(quantityAsString);
            approximativeSwitch.setChecked(item.inter.approximative_value);

            ArrayAdapter spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, Units.ALL_BASE_UNITS_L10N);
            unitSpinner.setAdapter(spinnerAdapter);
            unitSpinner.setSelection(Units.ALL_BASE_UNITS.indexOf(Units.getUnit(item.material.get(0).unit)));

        }
    }

    @NonNull
    @Override
    public MaterialAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_material_in_intervention, parent, false);

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
