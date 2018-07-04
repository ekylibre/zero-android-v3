package com.ekylibre.android.adapters;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;

import com.ekylibre.android.R;
import com.ekylibre.android.database.models.Harvest;
import com.ekylibre.android.utils.Enums;
import com.ekylibre.android.utils.Unit;
import com.ekylibre.android.utils.Units;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;


public class OutputAdapter extends RecyclerView.Adapter<OutputAdapter.ViewHolder> {

    private static final String TAG = "OutputAdapter";

    private Context context;
    private List<Harvest> dataset;

    public OutputAdapter(Context context, List<Harvest> dataset) {
        this.context = context;
        this.dataset = dataset;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView deleteImageView;
        EditText quantityEditText, numberEditText;
        AppCompatSpinner unitSpinner, storageSpinner;

        boolean globalOutput = false;

        ViewHolder(View itemView) {
            super(itemView);

            quantityEditText = itemView.findViewById(R.id.harvest_quantity_edit);
            unitSpinner = itemView.findViewById(R.id.harvest_unit_spinner);
            storageSpinner = itemView.findViewById(R.id.harvest_storage_spinner);
            deleteImageView = itemView.findViewById(R.id.harvest_delete);
            numberEditText = itemView.findViewById(R.id.harvest_number_edit);

            deleteImageView.setOnClickListener(view -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(R.string.delete_harvest_prompt);
                builder.setNegativeButton(R.string.no, (dialog, i) -> dialog.cancel());
                builder.setPositiveButton(R.string.yes, (dialog, i) -> {
                    dataset.remove(getAdapterPosition());
                    notifyDataSetChanged();  // notifyItemRemoved(position);
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            });

            quantityEditText.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable editable) {
                    if (!editable.toString().equals("0") && editable.length() != 0) {
                        dataset.get(getAdapterPosition()).quantity = Float.valueOf(editable.toString());
                    }
                }
            });

            unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onNothingSelected(AdapterView<?> parent) {}
                @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    List<Unit> units = globalOutput ? Units.GLOBAL_OUTPUT_UNITS : Units.LOAD_OUTPUT_UNITS;
                    dataset.get(getAdapterPosition()).unit = units.get(position).key;
                }
            });

            storageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onNothingSelected(AdapterView<?> parent) {}
                @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Timber.i("position %s", position);
                    if (position != 0) {
                        dataset.get(getAdapterPosition()).id_storage = Enums.STORAGE_LIST.get(position - 1).id;
                        Timber.i("Storage id --> %s", Enums.STORAGE_LIST.get(position - 1).id);
                    } else {
                        dataset.get(getAdapterPosition()).id_storage = null;
                    }
                }
            });

            numberEditText.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable editable) {
                    if (editable.length() > 0) {
                        dataset.get(getAdapterPosition()).number = editable.toString();
                    }
                }
            });
        }

        void display(Harvest item) {

            // Quantity field
            if (item.quantity != null)
                quantityEditText.setText(String.valueOf(item.quantity));

            // Quantity unit selector
            Unit currentUnit = Units.getUnit(item.unit);
            List<String> unitList;
            if (Units.GLOBAL_OUTPUT_UNITS.contains(currentUnit)) {
                unitList = Units.GLOBAL_OUTPUT_UNITS_L10N;
                globalOutput = true;
            } else {
                unitList = Units.LOAD_OUTPUT_UNITS_L10N;
                globalOutput = false;
            }

            ArrayAdapter unitSpinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, unitList);
            unitSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            unitSpinner.setAdapter(unitSpinnerAdapter);
            if (currentUnit != null)
                unitSpinner.setSelection(unitList.indexOf(currentUnit.name));

            // Storage selector
            ArrayAdapter storageSpinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, Enums.STORAGE_LIST_NAMES);
            storageSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            storageSpinner.setAdapter(storageSpinnerAdapter);
            Timber.i("storage #%s", item.id_storage);
            if (item.id_storage != null) {
                storageSpinner.setSelection(Enums.getIndex(item.id_storage) + 1);
            }

            numberEditText.setText(item.number);
        }
    }

    @NonNull
    @Override
    public OutputAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_harvest, parent, false);
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
