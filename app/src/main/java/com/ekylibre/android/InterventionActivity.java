package com.ekylibre.android;


import android.app.DatePickerDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Group;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekylibre.android.adapters.EquipmentAdapter;
import com.ekylibre.android.adapters.InputAdapter;
import com.ekylibre.android.adapters.PersonAdapter;
import com.ekylibre.android.database.AppDatabase;
import com.ekylibre.android.database.models.Crop;
import com.ekylibre.android.database.models.Intervention;
import com.ekylibre.android.database.models.Phyto;
import com.ekylibre.android.database.pojos.Equipments;
import com.ekylibre.android.database.pojos.Fertilizers;
import com.ekylibre.android.database.pojos.Materials;
import com.ekylibre.android.database.pojos.Persons;
import com.ekylibre.android.database.pojos.Phytos;
import com.ekylibre.android.database.pojos.PlotWithCrops;
import com.ekylibre.android.database.pojos.Seeds;
import com.ekylibre.android.database.relations.InterventionCrop;
import com.ekylibre.android.database.relations.InterventionWorkingDay;
import com.ekylibre.android.utils.DateTools;
import com.ekylibre.android.utils.SimpleDividerItemDecoration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.ekylibre.android.utils.PhytosanitaryMiscibility.mixIsAuthorized;


public class InterventionActivity extends AppCompatActivity implements
        SelectInputFragment.OnFragmentInteractionListener,
        SelectMaterialFragment.OnFragmentInteractionListener,
        SelectEquipmentFragment.OnFragmentInteractionListener,
        SelectPersonFragment.OnFragmentInteractionListener,
        SelectCropFragment.OnFragmentInteractionListener {

    private static final String TAG = InterventionActivity.class.getName();
    public static final String CREATED = "created";
    public static final String SYNCED = "synced";
    public static final String VALIDATED = "validated";

    // UI components
    Button saveButton;
    Button cancelButton;
    InputMethodManager keyboardManager;

    // Crops layout
    private TextView cropSummary, cropAddLabel;
    private DialogFragment selectCropFragment;

    private Group irrigationDetail;
    private ImageView irrigationArrow;
    private TextView irrigationSummary;
    private EditText irrigationQuantityEdit;
    private AppCompatSpinner irrigationUnitSpinner;

    // Working period layout
    private Group workingPeriodDetail;
    private ConstraintLayout workingPeriodLayout;
    private ImageView workingPeriodArrow;
    private TextView workingPeriodSummary, workingPeriodDurationUnit;
    private EditText workingPeriodEditDate, workingPeriodEditDuration;

    private ImageView inputArrow;
    private TextView inputSummary, inputAddLabel;
    private DialogFragment selectInputFragment;
    private RecyclerView inputRecyclerView;
    private RecyclerView.Adapter inputAdapter;
    private Group phytoMixWarning;

//    private ImageView materialArrow;
//    private TextView materialSummary, materialAddLabel;
//    private DialogFragment selectMaterialFragment;
//    private RecyclerView materialRecyclerView;
//    private RecyclerView.Adapter materialAdapter;

    // Equipment layout
    private ImageView equipmentArrow;
    private TextView equipmentSummary, equipmentAddLabel;
    private DialogFragment selectEquipmentFragment;
    private RecyclerView equipmentRecyclerView;
    private RecyclerView.Adapter equipmentAdapter;

    // Person layout
    private ImageView personArrow;
    private TextView personSummary, personAddLabel;
    private DialogFragment selectPersonFragment;
    private RecyclerView personRecyclerView;
    private RecyclerView.Adapter personAdapter;

    // Current Intervention values
    public static List<Object> inputList = new ArrayList<>();
    public static List<Materials> materialList = new ArrayList<>();
    public static List<Equipments> equipmentList = new ArrayList<>();
    public static List<Persons> personList = new ArrayList<>();
    public static List<PlotWithCrops> plotList = new ArrayList<>();
    public static String cropSummaryText;

    private Calendar today = Calendar.getInstance();
    private Calendar date = Calendar.getInstance();

    public static String procedure;
    private int duration = 7;
    public static float surface = 0f;
    private List volumeUnitKeys;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intervention);

        procedure = getIntent().getStringExtra("procedure");
        Log.e(TAG, "procedure " + procedure);
        setTitle(this.getResources().getIdentifier(procedure, "string", this.getPackageName()));

        keyboardManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        volumeUnitKeys = Arrays.asList(getResources().getStringArray(R.array.volume_unit_keys));



        // ================================ LAYOUT ============================================= //

        // Crops
        ConstraintLayout includeCropLayout = findViewById(R.id.include_crops_layout);
        cropAddLabel = findViewById(R.id.crops_add_label);
        cropSummary = findViewById(R.id.crops_summary);
        cropSummary.setText(this.getString(R.string.select_crops));

        // Irrigation
        ConstraintLayout irrigationLayout = findViewById(R.id.irrigation_layout);
        irrigationDetail = findViewById(R.id.irrigation_detail);
        irrigationArrow = findViewById(R.id.irrigation_arrow);
        irrigationQuantityEdit = findViewById(R.id.irrigation_quantity_edit);
        irrigationUnitSpinner = findViewById(R.id.irrigation_unit_spinner);

        // Working period
        workingPeriodLayout = findViewById(R.id.working_period_layout);
        workingPeriodDetail = findViewById(R.id.working_period_detail);
        workingPeriodArrow = findViewById(R.id.working_period_arrow);
        workingPeriodSummary = findViewById(R.id.working_period_summary);
        workingPeriodEditDate = findViewById(R.id.working_period_edit_date);
        workingPeriodEditDuration = findViewById(R.id.working_period_edit_duration);
        workingPeriodDurationUnit = findViewById(R.id.working_period_duration_unit);

        // Inputs
        ConstraintLayout inputLayout = findViewById(R.id.input_layout);
        inputArrow = findViewById(R.id.input_arrow);
        inputSummary = findViewById(R.id.input_summary);
        inputAddLabel = findViewById(R.id.input_add_label);
        inputRecyclerView = findViewById(R.id.input_recycler);
        phytoMixWarning = findViewById(R.id.phyto_mix_warning);

        // Materials
//        ConstraintLayout materialLayout = findViewById(R.id.material_layout);
//        materialArrow = findViewById(R.id.material_arrow);
//        materialSummary = findViewById(R.id.material_summary);
//        materialAddLabel = findViewById(R.id.material_add_label);
//        materialRecyclerView = findViewById(R.id.material_recycler);

        // Equipments
        equipmentArrow = findViewById(R.id.equipment_arrow);
        equipmentSummary = findViewById(R.id.equipment_summary);
        equipmentAddLabel = findViewById(R.id.equipment_add_label);
        equipmentRecyclerView = findViewById(R.id.equipment_recycler);

        // Persons
        personArrow = findViewById(R.id.person_arrow);
        personSummary = findViewById(R.id.person_summary);
        personAddLabel = findViewById(R.id.person_add_label);
        personRecyclerView = findViewById(R.id.person_recycler);


        // ================================ UI SETTINGS ======================================== //

        irrigationLayout.setVisibility(View.GONE);
        inputLayout.setVisibility(View.VISIBLE); // TODO: hide input in some case
        // materialLayout.setVisibility(View.GONE);


        switch (procedure) {
            case MainActivity.IRRIGATION:
                irrigationLayout.setVisibility(View.VISIBLE);
                break;
//            case MainActivity.CARE:
//                materialLayout.setVisibility(View.VISIBLE);
//                break;
//            case MainActivity.GROUND_WORK:
//                materialLayout.setVisibility(View.GONE);
//                break;
        }

        // =============================== CROPS EVENTS ======================================== //

        includeCropLayout.setOnClickListener(view ->
            selectCropFragment.show(getFragmentTransaction(), "dialog")
        );

        // =============================== IRRIGATION EVENTS =================================== //

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.volume_unit_values, android.R.layout.simple_spinner_dropdown_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        irrigationUnitSpinner.setAdapter(spinnerAdapter);

//        irrigationUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                String string = itemQuantityEdit.getText().toString();
//                if (!string.isEmpty()) {
//                    itemTotal.setText(calculTotal(Float.valueOf(string)));
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });


        // ========================== WORKING PERIOD EVENTS ==================================== //

        View.OnClickListener workingPeriodListener = view -> {
            if (workingPeriodSummary.getVisibility() == View.VISIBLE) {
                workingPeriodArrow.setRotation(180);
                workingPeriodSummary.setVisibility(View.GONE);
                workingPeriodDetail.setVisibility(View.VISIBLE);
            } else {
                workingPeriodSummary.setText(
                        String.format("%s • %s h", DateTools.display(date.getTime()), duration));
                workingPeriodArrow.setRotation(0);
                workingPeriodSummary.setVisibility(View.VISIBLE);
                workingPeriodDetail.setVisibility(View.GONE);
            }
        };

//        workingPeriodSummary.setOnClickListener(workingPeriodListener);
//        workingPeriodArrow.setOnClickListener(workingPeriodListener);
        workingPeriodLayout.setOnClickListener(workingPeriodListener);
        workingPeriodEditDate.setOnClickListener(view -> datePicker());

        workingPeriodEditDuration.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE){
                String editText = workingPeriodEditDuration.getText().toString();
                if (editText.isEmpty())
                    return true;
                else {
                    duration = Integer.parseInt(editText);
                    workingPeriodDurationUnit.setText(getResources().getQuantityString(R.plurals.hours, duration));
                    keyboardManager.hideSoftInputFromWindow(workingPeriodEditDuration.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    workingPeriodEditDuration.clearFocus();
                }
            }
            return false;
        });


        // ============================== INPUTS EVENTS ======================================== //

        inputAddLabel.setOnClickListener(view -> {
            selectInputFragment = SelectInputFragment.newInstance();
            selectInputFragment.show(getFragmentTransaction(), "dialog");
        });

        View.OnClickListener inputListener = view -> {
            if (inputRecyclerView.getVisibility() == View.GONE) {
                inputArrow.setVisibility(View.VISIBLE);
                inputArrow.setRotation(180);
                inputSummary.setVisibility(View.GONE);
                inputAddLabel.setVisibility(View.VISIBLE);
                inputRecyclerView.setVisibility(View.VISIBLE);
            } else {
                int count = inputList.size();
                inputSummary.setText(getResources().getQuantityString(R.plurals.inputs, count, count));
                inputArrow.setRotation(0);
                inputSummary.setVisibility(View.VISIBLE);
                inputAddLabel.setVisibility(View.GONE);
                inputRecyclerView.setVisibility(View.GONE);
            }
        };

        inputArrow.setOnClickListener(inputListener);
        inputSummary.setOnClickListener(inputListener);

        inputRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        inputRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
        inputAdapter = new InputAdapter(this, inputList);
        inputAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {

                Log.e(TAG, "inputList onChange()");

                phytoMixWarning.setVisibility(View.GONE);

                if (inputAdapter.getItemCount() == 0) {
                    inputArrow.performClick();
                    inputArrow.setVisibility(View.GONE);
                    inputSummary.setVisibility(View.GONE);
                    inputAddLabel.setVisibility(View.VISIBLE);
                    inputRecyclerView.setVisibility(View.GONE);
                } else if (inputAdapter.getItemCount() >= 2) {

                    Log.e(TAG, "inputList >= 2");

//                    List<Integer> phytoMixCodeList = new ArrayList<>();
//                    for (Object input : inputList) {
//                        if (input instanceof Phytos) {
//                            phytoMixCodeList.add(((Phytos) input).phyto.get(0).mix_category_code);
//                        }
//                    }
                    List<Integer> codes = new ArrayList<>();
                    for (Object input : inputList) {
                        if (input instanceof Phytos) {
                            Phyto phyto = ((Phytos) input).phyto.get(0);
                            if (phyto != null)
                                codes.add(phyto.mix_category_code);
                        }
                    }

                    if (codes.size() >= 2) {

                        Log.e(TAG, "phytos >= 2");

                        if (!mixIsAuthorized(codes))
                            phytoMixWarning.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        inputRecyclerView.setAdapter(inputAdapter);

        // ============================== MATERIALS EVENTS ===================================== //

//        materialAddLabel.setOnClickListener(view -> {
//            selectMaterialFragment = SelectMaterialFragment.newInstance();
//            selectMaterialFragment.show(getFragmentTransaction(), "dialog");
//        });
//
//        View.OnClickListener materialListener = view -> {
//            if (materialRecyclerView.getVisibility() == View.GONE) {
//                materialArrow.setVisibility(View.VISIBLE);
//                materialArrow.setRotation(180);
//                materialSummary.setVisibility(View.GONE);
//                materialAddLabel.setVisibility(View.VISIBLE);
//                materialRecyclerView.setVisibility(View.VISIBLE);
//            } else {
//                int count = materialList.size();
//                materialSummary.setText(getResources().getQuantityString(R.plurals.materials, count, count));
//                materialArrow.setRotation(0);
//                materialSummary.setVisibility(View.VISIBLE);
//                materialAddLabel.setVisibility(View.GONE);
//                materialRecyclerView.setVisibility(View.GONE);
//            }
//        };
//
//        materialArrow.setOnClickListener(materialListener);
//        materialSummary.setOnClickListener(materialListener);
//
//        materialRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        materialRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
//        materialAdapter = new MaterialAdapter(this, materialList);
//        materialAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
//            @Override
//            public void onChanged() {
//                if (materialAdapter.getItemCount() == 0) {
//                    materialArrow.performClick();
//                    materialArrow.setVisibility(View.GONE);
//                    materialSummary.setVisibility(View.GONE);
//                    materialAddLabel.setVisibility(View.VISIBLE);
//                    materialRecyclerView.setVisibility(View.GONE);
//                }
//            }
//        });
//        materialRecyclerView.setAdapter(materialAdapter);


        // ============================= EQUIPMENTS EVENTS ===================================== //

        equipmentAddLabel.setOnClickListener(view -> {
            selectEquipmentFragment = SelectEquipmentFragment.newInstance();
            selectEquipmentFragment.show(getFragmentTransaction(), "dialog");
        });

        View.OnClickListener equipmentListener = view -> {
            if (equipmentRecyclerView.getVisibility() == View.GONE) {
                equipmentArrow.setVisibility(View.VISIBLE);
                equipmentArrow.setRotation(180);
                equipmentSummary.setVisibility(View.GONE);
                equipmentAddLabel.setVisibility(View.VISIBLE);
                equipmentRecyclerView.setVisibility(View.VISIBLE);
            } else {
                int count = equipmentList.size();
                equipmentSummary.setText(getResources().getQuantityString(R.plurals.equipments, count, count));
                equipmentArrow.setRotation(0);
                equipmentSummary.setVisibility(View.VISIBLE);
                equipmentAddLabel.setVisibility(View.GONE);
                equipmentRecyclerView.setVisibility(View.GONE);
            }
        };

        equipmentArrow.setOnClickListener(equipmentListener);
        equipmentSummary.setOnClickListener(equipmentListener);

        equipmentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        equipmentRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
        equipmentAdapter = new EquipmentAdapter(this, equipmentList);
        equipmentAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                if (equipmentAdapter.getItemCount() == 0) {
                    equipmentArrow.performClick();
                    equipmentArrow.setVisibility(View.GONE);
                    equipmentSummary.setVisibility(View.GONE);
                    equipmentAddLabel.setVisibility(View.VISIBLE);
                    equipmentRecyclerView.setVisibility(View.GONE);
                }
            }
        });
        equipmentRecyclerView.setAdapter(equipmentAdapter);


        // =============================== PERSONS EVENTS ====================================== //

        personAddLabel.setOnClickListener(view -> {
            selectPersonFragment = SelectPersonFragment.newInstance();
            selectPersonFragment.show(getFragmentTransaction(), "dialog");
        });

        View.OnClickListener personListener = view -> {
            if (personRecyclerView.getVisibility() == View.GONE) {
                personArrow.setVisibility(View.VISIBLE);
                personArrow.setRotation(180);
                personSummary.setVisibility(View.GONE);
                personAddLabel.setVisibility(View.VISIBLE);
                personRecyclerView.setVisibility(View.VISIBLE);
            } else {
                int count = personList.size();
                personSummary.setText(getResources().getQuantityString(R.plurals.persons, count, count));
                personArrow.setRotation(0);
                personSummary.setVisibility(View.VISIBLE);
                personAddLabel.setVisibility(View.GONE);
                personRecyclerView.setVisibility(View.GONE);
            }
        };

        personArrow.setOnClickListener(personListener);
        personSummary.setOnClickListener(personListener);

        personRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        personRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
        personAdapter = new PersonAdapter(personList);
        personAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                if (personAdapter.getItemCount() == 0) {
                    personArrow.performClick();
                    personArrow.setVisibility(View.GONE);
                    personSummary.setVisibility(View.GONE);
                    personAddLabel.setVisibility(View.VISIBLE);
                    personRecyclerView.setVisibility(View.GONE);
                }
            }
        });
        personRecyclerView.setAdapter(personAdapter);


        // ================================ BOTTOM BAR ========================================= //



        saveButton = findViewById(R.id.button_save);
        saveButton.setOnClickListener(view -> new SaveIntervention(
                this, date).execute());

        cancelButton = findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(view -> {
            clearDatasets();
            finish();
        });

        // Launch crop selector
        selectCropFragment = SelectCropFragment.newInstance();
        selectCropFragment.show(getFragmentTransaction(), "dialog");

    }

    private class SaveIntervention extends AsyncTask<Void, Void, Void> {

        Context context;
        Date date;

        SaveIntervention(Context context, Calendar date) {
            this.context = context;
            this.date = date.getTime(); // Converters.toDate(date.getTimeInMillis())
        }

        @Override
        protected Void doInBackground(Void... voids) {

            AppDatabase database = AppDatabase.getInstance(context);

            Intervention intervention = new Intervention();
            intervention.setType(procedure);
            if (procedure.equals(MainActivity.IRRIGATION)) {
                intervention.setWater_quantity(Integer.valueOf(irrigationQuantityEdit.getText().toString()));
                intervention.setWater_unit(volumeUnitKeys.get(irrigationUnitSpinner.getSelectedItemPosition()).toString());
            }
            intervention.setStatus(CREATED);
            intervention.setFarm(MainActivity.currentFarmId);

            // Save intervention and get returning id
            int intervention_id = (int) (long) database.dao().insert(intervention);

            InterventionWorkingDay workingDay = new InterventionWorkingDay(intervention_id, date, duration);
            Log.e(TAG, workingDay.toString());
            database.dao().insert(workingDay);

            for (Object item : inputList) {
                if (item instanceof Seeds) {
                    Seeds seed = (Seeds) item;
                    seed.inter.intervention_id =  intervention_id;
                    database.dao().insert(seed.inter);
                }
                else if (item instanceof Phytos) {
                    Phytos phyto = (Phytos) item;
                    phyto.inter.intervention_id = intervention_id;
                    database.dao().insert(phyto.inter);
                }
                else if (item instanceof Fertilizers) {
                    Fertilizers ferti = (Fertilizers) item;
                    ferti.inter.intervention_id = intervention_id;
                    database.dao().insert(ferti.inter);
                }
            }

            for (Materials item : materialList) {
                item.inter.intervention_id = intervention_id;
                database.dao().insert(item.inter);
            }

            for (Equipments item : equipmentList) {
                item.inter.intervention_id = intervention_id;
                database.dao().insert(item.inter);
            }

            for (Persons item : personList) {
                item.inter.intervention_id = intervention_id;
                database.dao().insert(item.inter);
            }

            for (PlotWithCrops plot : plotList) {
                    for (Crop crop : plot.crops)
                        if (crop.is_checked)
                            database.dao().insert(new InterventionCrop(intervention_id, crop.uuid, crop.work_area_percentage));
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            clearDatasets();
            finish();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onFragmentInteraction(Object selection) {
        if (selection != null) {

            Log.e(TAG, "onFragmentInteraction --> " + selection.toString());

//            if (selection instanceof Materials) {
//                selectMaterialFragment.dismiss();
//                materialList.add((Materials) selection);
//                materialAdapter.notifyDataSetChanged();
//                if (materialRecyclerView.getVisibility() == View.GONE)
//                    materialArrow.performClick();

            if (selection instanceof Equipments) {
                selectEquipmentFragment.dismiss();
                equipmentList.add((Equipments) selection);
                equipmentAdapter.notifyDataSetChanged();
                if (equipmentRecyclerView.getVisibility() == View.GONE)
                    equipmentArrow.performClick();

            } else if (selection instanceof Persons) {
                selectPersonFragment.dismiss();
                personList.add((Persons) selection);
                personAdapter.notifyDataSetChanged();
                if (personRecyclerView.getVisibility() == View.GONE)
                    personArrow.performClick();

            } else if (selection instanceof Seeds || selection instanceof Phytos || selection instanceof Fertilizers) {
                selectInputFragment.dismiss();
                inputList.add(selection);
                inputAdapter.notifyDataSetChanged();
                if (inputRecyclerView.getVisibility() == View.GONE)
                    inputArrow.performClick();
                if (selection instanceof Phytos) {
                    new GetMaxDose((Phytos) selection).execute();
                }

            } else if (selectCropFragment.isVisible()){
                selectCropFragment.dismiss();
                List<PlotWithCrops> plotWithCropsList = (List<PlotWithCrops>) selection;
                if (!plotWithCropsList.isEmpty()) {
                    if (cropSummaryText.equals(this.getString(R.string.no_crop_selected))) {
                        cropSummary.setVisibility(View.GONE);
                        cropAddLabel.setVisibility(View.VISIBLE);
                    } else {
                        cropAddLabel.setVisibility(View.GONE);
                        cropSummary.setText(cropSummaryText);
                        cropSummary.setVisibility(View.VISIBLE);
                    }
                    // Erase plotList with new one from selection
                    plotList = plotWithCropsList;
                }
            }
        }
    }

    private FragmentTransaction getFragmentTransaction() {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        return ft;
    }

    private void datePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, day) -> {
            date.set(year, month, day);
            workingPeriodEditDate.setText(DateTools.display(date.getTime()));
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        clearDatasets();
    }

    void clearDatasets() {
        inputList.clear();
        materialList.clear();
        equipmentList.clear();
        personList.clear();
        plotList.clear();
    }

    private class GetMaxDose extends AsyncTask<Void, Void, Void> {

        Phytos phyto;
        Float dose_max;

        GetMaxDose(Phytos phyto) {
            this.phyto = phyto;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            dose_max = AppDatabase.getInstance(getBaseContext()).dao().getMaxDose(phyto.phyto.get(0).id);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ((Phytos) inputList.get(inputList.indexOf(phyto))).phyto.get(0).dose_max = dose_max;
            Log.e(TAG, "dose max = " + ((Phytos) inputList.get(inputList.indexOf(phyto))).phyto.get(0).dose_max);
        }
    }
}
