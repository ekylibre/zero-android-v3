package com.ekylibre.android.adapters;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.RecyclerView;
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

import com.ekylibre.android.InterventionActivity;
import com.ekylibre.android.R;
import com.ekylibre.android.database.pojos.Fertilizers;
import com.ekylibre.android.database.pojos.Phytos;
import com.ekylibre.android.database.pojos.Seeds;
import com.ekylibre.android.utils.QuantityCalcul;

import java.util.Arrays;
import java.util.List;


public class InputAdapter extends RecyclerView.Adapter<InputAdapter.ViewHolder> {

    private static final String TAG = "InputAdapter";
    private static final int SEED = 0, PHYTO = 1, FERTI = 2;

    private Context context;
    private List<Object> inputList;
    private InputMethodManager keyboardManager;

    public InputAdapter(Context context, List<Object> inputList) {
        this.context = context;
        this.inputList = inputList;
        this.keyboardManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView itemIcon;
        TextView itemName;
        TextView itemNameMore;
        EditText itemQuantityEdit;
        AppCompatSpinner itemUnitSpinner;
        TextView itemTotal;
        ImageView itemDelete;

        ViewHolder(View itemView) {
            super(itemView);

            itemIcon = itemView.findViewById(R.id.item_icon);
            itemName = itemView.findViewById(R.id.item_name);
            itemNameMore = itemView.findViewById(R.id.item_name_more);
            itemQuantityEdit = itemView.findViewById(R.id.item_quantity_edit);
            itemUnitSpinner = itemView.findViewById(R.id.item_unit_spinner);
            itemTotal = itemView.findViewById(R.id.item_total);
            itemDelete = itemView.findViewById(R.id.item_delete);

            itemDelete.setOnClickListener(view -> {
                Context context = itemView.getRootView().getContext();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Etes-vous sûr de vouloir supprimer l'intrant ?");
                builder.setNegativeButton("non", (dialog, i) -> dialog.cancel());
                builder.setPositiveButton("oui", (dialog, i) -> {
                    int position = getAdapterPosition();
                    inputList.remove(position);
                    //notifyItemRemoved(position);
                    notifyDataSetChanged();
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            });

            itemQuantityEdit.setOnEditorActionListener((view, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    String string = itemQuantityEdit.getText().toString();
                    if (string.isEmpty())
                        return true;
                    else {
                        itemTotal.setText(calculTotal(Float.valueOf(string)));
                        keyboardManager.hideSoftInputFromWindow(itemQuantityEdit.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        itemQuantityEdit.clearFocus();
                    }
                }
                return false;
            });

            itemQuantityEdit.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    String string = itemQuantityEdit.getText().toString();
                    if (string.isEmpty()) {
                        itemQuantityEdit.requestFocus();
                    } else {
                        itemTotal.setText(calculTotal(Float.valueOf(string)));
                    }
                }
            });

            itemUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    String string = itemQuantityEdit.getText().toString();
                    if (!string.isEmpty()) {
                        calculTotal(Float.valueOf(string));
                    }

                    // TODO: update quantity on unit change
                }
                @Override
                public void onNothingSelected(AdapterView<?> parentView) {}
            });
        }

        void display(int icon, String name, String more, float quantity, String unit, int unit_dimention) {
            String quantityAsString = Float.toString(quantity);
            itemIcon.setImageResource(icon);
            itemName.setText(name);
            itemNameMore.setText(more);
            itemQuantityEdit.setText(quantityAsString);
            itemTotal.setText(calculTotal(quantity));

            int resKeys = 0, resValues = 0;
            switch (unit_dimention) {
                case FERTI:
                case SEED:
                    resKeys = R.array.mass_unit_keys;
                    resValues = R.array.mass_unit_values;
                    break;
                case PHYTO:
                    resKeys = R.array.volume_unit_keys;
                    resValues = R.array.volume_unit_values;
                    break;
            }
            ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(context, resValues, android.R.layout.simple_spinner_dropdown_item);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            itemUnitSpinner.setAdapter(spinnerAdapter);

            List unitKeys = Arrays.asList(context.getResources().getStringArray(resKeys));
            itemUnitSpinner.setSelection(unitKeys.indexOf(unit));
        }

        String calculTotal(float quantity) {

            List unitKeys;
            String unitBefore;
            String unitAfter;
            String text = "";
            int pos = itemUnitSpinner.getSelectedItemPosition();

            if (quantity > 0f) {
                switch (getItemViewType()) {

                    case SEED:
                        Seeds seed = (Seeds) inputList.get(getAdapterPosition());
                        unitKeys = Arrays.asList(context.getResources().getStringArray(R.array.mass_unit_keys));
                        unitBefore = seed.inter.unit;
                        unitAfter = unitKeys.get(pos).toString();
                        seed.inter.unit = unitAfter;
                        seed.inter.quantity = quantity;
                        text = QuantityCalcul.getText(quantity, unitBefore, unitAfter, InterventionActivity.surface);
                        break;

                    case PHYTO:
                        Phytos phyto = (Phytos) inputList.get(getAdapterPosition());
                        unitKeys = Arrays.asList(context.getResources().getStringArray(R.array.volume_unit_keys));
                        unitBefore = phyto.inter.unit;
                        unitAfter = unitKeys.get(pos).toString();
                        phyto.inter.unit = unitAfter;
                        phyto.inter.quantity = quantity;
                        text = QuantityCalcul.getText(quantity, unitBefore, unitAfter, InterventionActivity.surface);
                        break;

                    case FERTI:
                        Fertilizers ferti = (Fertilizers) inputList.get(getAdapterPosition());
                        unitKeys = Arrays.asList(context.getResources().getStringArray(R.array.mass_unit_keys));
                        unitBefore = ferti.inter.unit;
                        unitAfter = unitKeys.get(pos).toString();
                        ferti.inter.unit = unitAfter;
                        ferti.inter.quantity = quantity;
                        text = QuantityCalcul.getText(quantity, unitBefore, unitAfter, InterventionActivity.surface);
                        break;
                }
                itemTotal.setVisibility(View.VISIBLE);
            }
            else
                itemTotal.setVisibility(View.GONE);

            return text;
        }




    }

    @NonNull
    @Override
    public InputAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_input_in_intervention, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        switch (holder.getItemViewType()) {

            case SEED:
                Seeds seed = (Seeds) inputList.get(position);
                holder.display(R.drawable.icon_seed, seed.seed.get(0).fra, seed.seed.get(0).variety, seed.inter.quantity, seed.inter.unit, SEED);
                break;

            case PHYTO:
                Phytos phyto = (Phytos) inputList.get(position);
                holder.display(R.drawable.icon_fertilizer, phyto.phyto.get(0).name, phyto.phyto.get(0).firm_name, phyto.inter.quantity, phyto.inter.unit, PHYTO);
                break;

            case FERTI:
                Fertilizers ferti = (Fertilizers) inputList.get(position);
                holder.display(R.drawable.icon_fertilizer, ferti.fertilizer.get(0).label_fra, ferti.fertilizer.get(0).variety, ferti.inter.quantity, ferti.inter.unit, FERTI);
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {

        Object item = inputList.get(position);

        if (item instanceof Seeds)
            return SEED;

        else if (item instanceof Phytos)
            return PHYTO;

        else if (item instanceof Fertilizers)
            return FERTI;

        return -1;
    }

    @Override
    public int getItemCount() {
        return inputList.size();
    }
}
