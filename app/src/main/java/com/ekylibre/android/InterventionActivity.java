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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekylibre.android.adapters.EquipmentAdapter;
import com.ekylibre.android.adapters.InputAdapter;
import com.ekylibre.android.adapters.MaterialAdapter;
import com.ekylibre.android.adapters.PersonAdapter;
import com.ekylibre.android.database.models.Crop;
import com.ekylibre.android.database.models.Intervention;
import com.ekylibre.android.database.pojos.Equipments;
import com.ekylibre.android.database.pojos.Fertilizers;
import com.ekylibre.android.database.pojos.Materials;
import com.ekylibre.android.database.pojos.Persons;
import com.ekylibre.android.database.pojos.Phytos;
import com.ekylibre.android.database.pojos.PlotWithCrops;
import com.ekylibre.android.database.pojos.Seeds;
import com.ekylibre.android.database.relations.InterventionCrop;
import com.ekylibre.android.utils.Converters;
import com.ekylibre.android.utils.DateTools;
import com.ekylibre.android.utils.SimpleDividerItemDecoration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class InterventionActivity extends AppCompatActivity implements
        SelectInputFragment.OnFragmentInteractionListener,
        SelectMaterialFragment.OnFragmentInteractionListener,
        SelectEquipmentFragment.OnFragmentInteractionListener,
        SelectPersonFragment.OnFragmentInteractionListener,
        SelectCropFragment.OnFragmentInteractionListener {

    private static final String TAG = InterventionActivity.class.getName();

    // UI components
    Button saveButton;
    Button cancelButton;
    InputMethodManager keyboardManager;

    // Crops layout
    private TextView cropSummary, cropAddLabel;
    private DialogFragment selectCropFragment;

    // Working period layout
    private Group workingPeriodDetail;
    private ImageView workingPeriodArrow;
    private TextView workingPeriodSummary, workingPeriodDurationUnit;
    private EditText workingPeriodEditDate, workingPeriodEditDuration;

    // Input layout
    private ImageView inputArrow;
    private TextView inputSummary, inputAddLabel;
    private DialogFragment selectInputFragment;
    private RecyclerView inputRecyclerView;
    private RecyclerView.Adapter inputAdapter;

    // Material layout
    private ImageView materialArrow;
    private TextView materialSummary, materialAddLabel;
    private DialogFragment selectMaterialFragment;
    private RecyclerView materialRecyclerView;
    private RecyclerView.Adapter materialAdapter;

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

    private String procedure;
    private int duration = 7;
    public static float surface = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intervention);

        procedure = getIntent().getStringExtra("procedure");
        setTitle(this.getResources().getIdentifier(procedure, "string", this.getPackageName()));

        keyboardManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);


        // ================================ LAYOUT ============================================= //

        // Crops
        ConstraintLayout includeCropLayout = findViewById(R.id.include_crops_layout);
        cropAddLabel = findViewById(R.id.crops_add_label);
        cropSummary = findViewById(R.id.crops_summary);
        cropSummary.setText(this.getString(R.string.select_crops));

        // Working period
        workingPeriodDetail = findViewById(R.id.working_period_detail);
        workingPeriodArrow = findViewById(R.id.working_period_arrow);
        workingPeriodSummary = findViewById(R.id.working_period_summary);
        workingPeriodEditDate = findViewById(R.id.working_period_edit_date);
        workingPeriodEditDuration = findViewById(R.id.working_period_edit_duration);
        workingPeriodDurationUnit = findViewById(R.id.working_period_duration_unit);

        // Inputs
        inputArrow = findViewById(R.id.input_arrow);
        inputSummary = findViewById(R.id.input_summary);
        inputAddLabel = findViewById(R.id.input_add_label);
        inputRecyclerView = findViewById(R.id.input_recycler);

        // Materials
        materialArrow = findViewById(R.id.material_arrow);
        materialSummary = findViewById(R.id.material_summary);
        materialAddLabel = findViewById(R.id.material_add_label);
        materialRecyclerView = findViewById(R.id.material_recycler);

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



        // =============================== CROPS EVENTS ======================================== //

        includeCropLayout.setOnClickListener(view -> {
            selectCropFragment = SelectCropFragment.newInstance();
            selectCropFragment.show(getFragmentTransaction(), "dialog");
        });


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

        workingPeriodSummary.setOnClickListener(workingPeriodListener);
        workingPeriodArrow.setOnClickListener(workingPeriodListener);
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
                if (inputAdapter.getItemCount() == 0) {
                    inputArrow.performClick();
                    inputArrow.setVisibility(View.GONE);
                    inputSummary.setVisibility(View.GONE);
                    inputAddLabel.setVisibility(View.VISIBLE);
                    inputRecyclerView.setVisibility(View.GONE);
                }
            }
        });
        inputRecyclerView.setAdapter(inputAdapter);


        // ============================== MATERIALS EVENTS ===================================== //

        materialAddLabel.setOnClickListener(view -> {
            selectMaterialFragment = SelectMaterialFragment.newInstance();
            selectMaterialFragment.show(getFragmentTransaction(), "dialog");
        });

        View.OnClickListener materialListener = view -> {
            if (materialRecyclerView.getVisibility() == View.GONE) {
                materialArrow.setVisibility(View.VISIBLE);
                materialArrow.setRotation(180);
                materialSummary.setVisibility(View.GONE);
                materialAddLabel.setVisibility(View.VISIBLE);
                materialRecyclerView.setVisibility(View.VISIBLE);
            } else {
                int count = materialList.size();
                materialSummary.setText(getResources().getQuantityString(R.plurals.materials, count, count));
                materialArrow.setRotation(0);
                materialSummary.setVisibility(View.VISIBLE);
                materialAddLabel.setVisibility(View.GONE);
                materialRecyclerView.setVisibility(View.GONE);
            }
        };

        materialArrow.setOnClickListener(materialListener);
        materialSummary.setOnClickListener(materialListener);

        materialRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        materialRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
        materialAdapter = new MaterialAdapter(this, materialList);
        materialAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                if (materialAdapter.getItemCount() == 0) {
                    materialArrow.performClick();
                    materialArrow.setVisibility(View.GONE);
                    materialSummary.setVisibility(View.GONE);
                    materialAddLabel.setVisibility(View.VISIBLE);
                    materialRecyclerView.setVisibility(View.GONE);
                }
            }
        });
        materialRecyclerView.setAdapter(materialAdapter);


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
        equipmentAdapter = new EquipmentAdapter(equipmentList);
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
                this, Converters.toDate(date.getTimeInMillis())).execute());

        cancelButton = findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(view -> finish());

    }

    private class SaveIntervention extends AsyncTask<Void, Void, Void> {

        Context context;
        Date date;

        SaveIntervention(Context context, Date date) {
            this.context = context;
            this.date = date;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            AppDatabase database = AppDatabase.getInstance(context);

            Intervention intervention = new Intervention(procedure,null, date, duration,
                    null,null,null,null,null,
                    null,null, null, new Date());

            int intervention_id = (int) (long) database.dao().insert(intervention);

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
            inputList.clear();
            materialList.clear();
            equipmentList.clear();
            personList.clear();
            plotList.clear();
            finish();
        }
    }

    @Override
    public void onFragmentInteraction(Object selection) {
        if (selection != null) {

            Log.e(TAG, "onFragmentInteraction --> " + selection.toString());

            if (selection instanceof Materials) {
                selectMaterialFragment.dismiss();
                materialList.add((Materials) selection);
                materialAdapter.notifyDataSetChanged();
                if (materialRecyclerView.getVisibility() == View.GONE)
                    materialArrow.performClick();

            } else if (selection instanceof Equipments) {
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

            } else {
                selectCropFragment.dismiss();
                if (cropSummaryText.equals(this.getString(R.string.no_crop_selected))) {
                    cropSummary.setVisibility(View.GONE);
                    cropAddLabel.setVisibility(View.VISIBLE);
                } else {
                    cropAddLabel.setVisibility(View.GONE);
                    cropSummary.setText(cropSummaryText);
                    cropSummary.setVisibility(View.VISIBLE);
                }
                plotList = (List<PlotWithCrops>) selection;
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
        inputList.clear();
        materialList.clear();
        equipmentList.clear();
        personList.clear();
        plotList.clear();
    }
}
