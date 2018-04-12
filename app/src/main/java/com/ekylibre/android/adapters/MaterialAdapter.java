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
import com.ekylibre.android.database.pojos.Materials;
import com.ekylibre.android.utils.QuantityCalculs;

import java.util.Arrays;
import java.util.List;


public class MaterialAdapter extends RecyclerView.Adapter<MaterialAdapter.ViewHolder> {

    private static final String TAG = MaterialAdapter.class.getName();

    private Context context;
    private List<Materials> dataset;
    private InputMethodManager keyboardManager;
    private float surface = 17.3f;

    public MaterialAdapter(Context context, List<Materials> dataset) {
        this.context = context;
        this.dataset = dataset;
        this.keyboardManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView iconImageView, deleteImageView;
        TextView nameTextView, descTextView, totalTextView;
        EditText quantityEditText;
        AppCompatSpinner unitSpinner;
        SwitchCompat approximativeSwitch;

        ViewHolder(View itemView) {
            super(itemView);

            iconImageView = itemView.findViewById(R.id.item_icon);
            nameTextView = itemView.findViewById(R.id.item_name);
            descTextView = itemView.findViewById(R.id.item_desc);
            quantityEditText = itemView.findViewById(R.id.item_quantity_edit);
            unitSpinner = itemView.findViewById(R.id.item_unit_spinner);
            totalTextView = itemView.findViewById(R.id.item_total);
            approximativeSwitch = itemView.findViewById(R.id.item_approximated);
            deleteImageView = itemView.findViewById(R.id.item_delete);

            deleteImageView.setOnClickListener(view -> {
                //Context context = itemView.getRootView().getContext();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Etes-vous sûr de vouloir supprimer le matériau ?");
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

            quantityEditText.setOnEditorActionListener((view, actionId, event) -> {
                String value = quantityEditText.getText().toString();
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    if (value.isEmpty())
                        return true;
                    else {
                        totalTextView.setText(calculTotal(value));
                        keyboardManager.hideSoftInputFromWindow(quantityEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        quantityEditText.clearFocus();
                    }
                }
                return false;
            });

            quantityEditText.setOnFocusChangeListener((v, hasFocus) -> {
                String value = quantityEditText.getText().toString();
                if (!hasFocus) {
                    if (value.isEmpty()) {
                        quantityEditText.requestFocus();
                    } else {
                        totalTextView.setText(calculTotal(value));
                    }
                }
            });

            unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    calculTotal(quantityEditText.getText().toString());
                    // TODO: update quantity on unit change
                }
                @Override
                public void onNothingSelected(AdapterView<?> parentView) {}
            });

            approximativeSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
                Materials person = dataset.get(getAdapterPosition());
                person.inter.approximative_value = compoundButton.isChecked();
            });
        }

        void display(Materials item) {

            String quantityAsString = String.format("%s", item.inter.quantity);
            iconImageView.setImageResource(R.drawable.icon_fertilizer);
            nameTextView.setText(item.material.get(0).name);
            if (!item.material.get(0).description.isEmpty())
                descTextView.setText(item.material.get(0).description);
            quantityEditText.setText(quantityAsString);
            approximativeSwitch.setChecked(item.inter.approximative_value);
            totalTextView.setText(calculTotal(quantityAsString));

            ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(context, R.array.unity_unit_values, android.R.layout.simple_spinner_dropdown_item);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            unitSpinner.setAdapter(spinnerAdapter);

            List unitKeys = Arrays.asList(context.getResources().getStringArray(R.array.unity_unit_values));
            unitSpinner.setSelection(unitKeys.indexOf(item.inter.unit));
        }

        String calculTotal(String value) {

            List unitKeys;
            String unitBefore;
            String unitAfter;
            String text = "";
            int quantity = Integer.parseInt(value);
            int pos = unitSpinner.getSelectedItemPosition();

            if (quantity > 0) {

                Materials material = dataset.get(getAdapterPosition());
                unitKeys = Arrays.asList(context.getResources().getStringArray(R.array.unity_unit_keys));
                unitBefore = material.inter.unit;
                unitAfter = unitKeys.get(pos).toString();
                material.inter.unit = unitAfter;
                material.inter.quantity = quantity;
                text = QuantityCalculs.getText(quantity, unitBefore, unitAfter, surface);

                totalTextView.setVisibility(View.VISIBLE);

            } else {
                totalTextView.setVisibility(View.GONE);
            }
            return text;
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
