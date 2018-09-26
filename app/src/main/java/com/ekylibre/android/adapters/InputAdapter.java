package com.ekylibre.android.adapters;


import android.content.Context;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.RecyclerView;
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
import com.ekylibre.android.database.models.Seed;
import com.ekylibre.android.database.pojos.Fertilizers;
import com.ekylibre.android.database.pojos.Phytos;
import com.ekylibre.android.database.pojos.Seeds;
import com.ekylibre.android.utils.QuantityConverter;
import com.ekylibre.android.utils.Unit;
import com.ekylibre.android.utils.Units;

import java.util.List;

import static com.ekylibre.android.utils.Utils.decimalFormat;
import static com.ekylibre.android.utils.Utils.getEditTextToFloat;


public class InputAdapter extends RecyclerView.Adapter<InputAdapter.ViewHolder> {

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

        ImageView itemIcon, itemDelete;
        TextView itemName, itemNameMore, itemTotal;
        EditText itemQuantityEdit;
        AppCompatSpinner itemUnitSpinner;
        Group itemDoseMax;

        int displayDoseWarning = View.GONE;

        List<String> unitsLabel;
        List<Unit> unitsKey;
        Unit currentUnit;

        ViewHolder(View itemView) {
            super(itemView);

            itemIcon = itemView.findViewById(R.id.item_icon);
            itemName = itemView.findViewById(R.id.item_name);
            itemNameMore = itemView.findViewById(R.id.item_name_more);
            itemQuantityEdit = itemView.findViewById(R.id.item_quantity_edit);
            itemUnitSpinner = itemView.findViewById(R.id.item_unit_spinner);
            itemTotal = itemView.findViewById(R.id.item_total);
            itemDelete = itemView.findViewById(R.id.item_delete);
            itemDoseMax = itemView.findViewById(R.id.item_dose_warning);

            if (getItemViewType() == PHYTO)
                displayDoseWarning(getEditTextToFloat(itemQuantityEdit));

            if (InterventionActivity.validated) {
                itemDelete.setVisibility(View.GONE);
                itemQuantityEdit.setFocusable(false);
                itemQuantityEdit.setEnabled(false);
                itemUnitSpinner.setEnabled(false);
            } else {
                itemDelete.setOnClickListener(view -> {
                    Context context = itemView.getRootView().getContext();
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(R.string.delete_input_prompt);
                    builder.setNegativeButton(R.string.no, (dialog, i) -> dialog.cancel());
                    builder.setPositiveButton(R.string.yes, (dialog, i) -> {
                        int position = getAdapterPosition();
                        inputList.remove(position);
                        notifyDataSetChanged();
                        displayDoseWarning = View.GONE;
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                });

                itemQuantityEdit.setOnEditorActionListener((view, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        Float floatValue = getEditTextToFloat(itemQuantityEdit);
                        if (floatValue == null)
                            return true;
                        else {
                            switch (getItemViewType()) {
                                case PHYTO:
                                    ((Phytos) inputList.get(getLayoutPosition())).inter.quantity = floatValue;
                                    break;
                                case SEED:
                                    ((Seeds) inputList.get(getLayoutPosition())).inter.quantity = floatValue;
                                    break;
                                case FERTI:
                                    ((Fertilizers) inputList.get(getLayoutPosition())).inter.quantity = floatValue;
                                    break;
                            }
                            updateTotal();
                            itemTotal.setTextColor(context.getResources().getColor(R.color.secondary_text));
                            keyboardManager.hideSoftInputFromWindow(itemQuantityEdit.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                            itemQuantityEdit.clearFocus();
                            if (getItemViewType() == PHYTO)
                                displayDoseWarning(floatValue);
                        }
                    }
                    return false;
                });

                itemUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                    }

                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        switch (getItemViewType()) {
                            case PHYTO:
                                displayDoseWarning(getEditTextToFloat(itemQuantityEdit));
                                ((Phytos) inputList.get(getLayoutPosition())).inter.unit = Units.VOLUME_UNITS.get(position).key;
                                break;
                            case SEED:
                                ((Seeds) inputList.get(getLayoutPosition())).inter.unit = Units.MASS_UNITS.get(position).key;
                                break;
                            case FERTI:
                                ((Fertilizers) inputList.get(getLayoutPosition())).inter.unit = Units.MASS_UNITS.get(position).key;
                                break;
                        }
                        updateTotal();
                    }
                });
            }
        }

        void displayDoseWarning(Float quantity) {
            if (quantity != null) {
                if (quantity != 0f) {
                    Phytos currentPhytos = (Phytos) inputList.get(getAdapterPosition());
                    Float dose_max = currentPhytos.phyto.get(0).dose_max;
                    if (dose_max != null) {
                        float dose;
                        if (currentUnit.surface_factor == 0)
                            dose = quantity * currentUnit.quantity_factor / InterventionActivity.surface;
                        else
                            dose = quantity * currentUnit.surface_factor;
                        if (dose > dose_max)
                            displayDoseWarning = View.VISIBLE;
                        else
                            displayDoseWarning = View.GONE;
                    }
                } else {
                    displayDoseWarning = View.GONE;
                }
                itemDoseMax.setVisibility(displayDoseWarning);

            }
        }

        void display(int icon, String name, String more, float quantity, String unit) {

            currentUnit = Units.getUnit(unit);

            itemIcon.setImageResource(icon);
            itemName.setText(name);
            itemNameMore.setText(more);
            itemQuantityEdit.setText(decimalFormat.format(quantity));
            itemDoseMax.setVisibility(displayDoseWarning);

            if (getItemViewType() == FERTI)
                itemNameMore.setVisibility(View.GONE);
            if (getItemViewType() == PHYTO) {
                unitsLabel = Units.VOLUME_UNITS_L10N;
                unitsKey = Units.VOLUME_UNITS;
                displayDoseWarning(getEditTextToFloat(itemQuantityEdit));
            }
            else {
                unitsLabel = Units.MASS_UNITS_L10N;
                unitsKey = Units.MASS_UNITS;
            }

            ArrayAdapter spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, unitsLabel);
            itemUnitSpinner.setAdapter(spinnerAdapter);
            itemUnitSpinner.setSelection(unitsKey.indexOf(currentUnit));

            if (quantity > 0)
                updateTotal();

        }

        void updateTotal() {

            Float quantity = getEditTextToFloat(itemQuantityEdit);
            currentUnit = unitsKey.get(itemUnitSpinner.getSelectedItemPosition());
            String text = "Nothing to show";

            if (quantity > 0f) {

                switch (getItemViewType()) {

                    case SEED:
                        Seeds seed = (Seeds) inputList.get(getLayoutPosition());
                        seed.inter.setInter(quantity, currentUnit.key);
                        text = QuantityConverter.getText(quantity, currentUnit);
                        break;

                    case PHYTO:
                        Phytos phyto = (Phytos) inputList.get(getLayoutPosition());
                        phyto.inter.setInter(quantity, currentUnit.key);
                        text = QuantityConverter.getText(quantity, currentUnit);
                        break;

                    case FERTI:
                        Fertilizers fertilizer = (Fertilizers) inputList.get(getLayoutPosition());
                        fertilizer.inter.setInter(quantity, currentUnit.key);
                        text = QuantityConverter.getText(quantity, currentUnit);
                        break;
                }
                itemTotal.setText(text);
                itemTotal.setVisibility(View.VISIBLE);
            }
            else {
                itemTotal.setVisibility(View.GONE);
            }
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
                Seeds item = (Seeds) inputList.get(position);
                Seed seed = item.seed.get(0);
                String speciL10n = "";
                if (seed.specie != null)
                    speciL10n = context.getString(context.getResources().getIdentifier(seed.specie.toUpperCase(), "string", context.getPackageName()));
                holder.display(R.drawable.icon_seed, speciL10n, seed.variety, item.inter.quantity, item.inter.unit);
                break;

            case PHYTO:
                Phytos phyto = (Phytos) inputList.get(position);
                holder.itemDoseMax.setVisibility(View.GONE);
                holder.display(R.drawable.icon_phytosanitary, phyto.phyto.get(0).name, phyto.phyto.get(0).firm_name, phyto.inter.quantity, phyto.inter.unit);
                break;

            case FERTI:
                Fertilizers ferti = (Fertilizers) inputList.get(position);
                holder.display(R.drawable.icon_fertilizer, ferti.fertilizer.get(0).label_fra, ferti.fertilizer.get(0).variety, ferti.inter.quantity, ferti.inter.unit);
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
