package com.ekylibre.android.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ekylibre.android.InterventionActivity;
import com.ekylibre.android.MainActivity;
import com.ekylibre.android.R;
import com.ekylibre.android.adapters.SelectEquipmentAdapter;
import com.ekylibre.android.database.AppDatabase;
import com.ekylibre.android.database.models.Equipment;
import com.ekylibre.android.database.pojos.Equipments;
import com.ekylibre.android.services.ServiceResultReceiver;
import com.ekylibre.android.services.SyncService;
import com.ekylibre.android.utils.App;
import com.ekylibre.android.utils.Enums;
import com.ekylibre.android.utils.PerformSyncWithFreshToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

import static com.ekylibre.android.services.SyncService.DONE;


public class SelectEquipmentFragment extends DialogFragment implements ServiceResultReceiver.Receiver{

    private static final int MIN_SEARCH_SIZE = 2;

    private Context context;

    private OnFragmentInteractionListener fragmentListener;
    private RecyclerView.Adapter adapter;
    private TextView createEquipment;
    private ServiceResultReceiver resultReceiver;

    private String searchText;
    private ArrayList<Equipment> dataset;
    private List<String> equipmentNames;

    public SelectEquipmentFragment() {
    }

    public static SelectEquipmentFragment newInstance() {
        return new SelectEquipmentFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getActivity();
        this.dataset = new ArrayList<>();
        this.equipmentNames = new ArrayList<>();
        this.searchText = "";
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Disables AppBar
        Objects.requireNonNull(getDialog().getWindow()).requestFeature(Window.FEATURE_NO_TITLE);

        View inflatedView = inflater.inflate(R.layout.fragment_select_equipment, container, false);

        createEquipment = inflatedView.findViewById(R.id.equipment_dialog_create_new);
        SearchView searchView = inflatedView.findViewById(R.id.search_equipment);
        RecyclerView recyclerView = inflatedView.findViewById(R.id.equipment_dialog_recycler);

        createEquipment.setOnClickListener(view -> createEquipmentDialog());
        searchView.setOnSearchClickListener(view -> createEquipment.setVisibility(View.GONE));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchText = query;
                new RequestDatabase(context).execute();
                createEquipment.setVisibility(View.VISIBLE);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                searchText = newText;
                if (newText.length() > 2)
                    new RequestDatabase(context).execute();
                return false;
            }
        });

        searchView.setOnCloseListener(() -> {
            new RequestDatabase(context).execute();
            createEquipment.setVisibility(View.VISIBLE);
            return false;
        });


        List<Integer> selectedEquipments = new ArrayList<>();
        for (Equipments equipments : InterventionActivity.equipmentList) {
            selectedEquipments.add(equipments.equipment.get(0).id);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        adapter = new SelectEquipmentAdapter(context, dataset, selectedEquipments, fragmentListener);
        recyclerView.setAdapter(adapter);

        return inflatedView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Window window = getDialog().getWindow();
        if (window != null)
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        resultReceiver = new ServiceResultReceiver(new Handler());
        resultReceiver.setReceiver(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new RequestDatabase(context).execute();
    }

    public void createEquipmentDialog() {

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_create_equipment, null);

        TextInputLayout nameTextInput = dialogView.findViewById(R.id.create_equipment_name);
        AppCompatImageView typeIcon = dialogView.findViewById(R.id.create_equipment_icon);

        builder.setView(dialogView);
        builder.setNegativeButton(R.string.cancel, (dialog, i) -> dialog.cancel());
        builder.setPositiveButton(R.string.create, (dialog, i) -> {
            // pass
        });

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = nameTextInput.getEditText().getText().toString();
            if (equipmentNames.contains(name)) {
                nameTextInput.setError(context.getString(R.string.equipment_name_not_available));
            } else if (name.isEmpty()) {
                nameTextInput.setError(context.getString(R.string.equipment_name_is_empty));
            } else {
                new CreateNewEquipment(context, dialogView).execute();
                dialog.dismiss();
            }
        });

        // Adjust dialog window to wrap content horizontally
        Window window = dialog.getWindow();
        if (window != null)
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        AppCompatSpinner spinner = dialogView.findViewById(R.id.create_equipment_type_spinner);
        ArrayAdapter spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, Enums.EQUIMPMENT_NAMES);
        spinner.setAdapter(spinnerAdapter);

        Timber.i("Equipment type = tool_%s", Enums.EQUIMPMENT_TYPES.get(0).toLowerCase());
        typeIcon.setImageResource(getResources().getIdentifier(
                "tool_" + Enums.EQUIMPMENT_TYPES.get(0).toLowerCase(), "drawable", context.getPackageName()));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                typeIcon.setImageResource(getResources().getIdentifier(
                        "tool_" + Enums.EQUIMPMENT_TYPES.get(pos).toLowerCase(),
                        "drawable", context.getPackageName()));
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {

        if (resultCode == DONE) {
            String name = resultData.getString("name", null);
            int remote_id = resultData.getInt("remote_id", 0);

            new SetEquipmentId(context, name, remote_id).execute();

            for (Equipment equipment : dataset) {
                if (equipment.name.equals(name)) {
                    equipment.eky_id = remote_id;
                    break;
                }
            }
        }
    }

    class SetEquipmentId extends AsyncTask<Void, Void, Void> {

        Context context;
        String name;
        int id;

        SetEquipmentId(Context context, String name, int id) {
            this.context = context;
            this.name = name;
            this.id = id;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            AppDatabase database = AppDatabase.getInstance(context);
            database.dao().setEquipmentEkyId(id, name);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter.notifyDataSetChanged();
        }
    }

    class CreateNewEquipment extends AsyncTask<Void, Void, Void> {

        Context context;
        View dialogView;

        CreateNewEquipment(Context context, View dialogView) {
            this.context = context;
            this.dialogView = dialogView;
        }

        @Override
        protected Void doInBackground(Void... voids) {


            AppCompatSpinner typeSpinner = dialogView.findViewById(R.id.create_equipment_type_spinner);
            String type = Enums.EQUIMPMENT_TYPES.get(typeSpinner.getSelectedItemPosition());

            TextInputLayout nameTextInput = dialogView.findViewById(R.id.create_equipment_name);
            String name = nameTextInput.getEditText().getText().toString();

            TextInputLayout numberTextInput = dialogView.findViewById(R.id.create_equipment_number);
            String number = numberTextInput.getEditText().getText().toString();

            AppDatabase database = AppDatabase.getInstance(context);
            database.dao().insert(new Equipment(null, name, type, number, MainActivity.FARM_ID, null, null));

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            new RequestDatabase(context).execute();

            if (App.isOnline(context))
                new PerformSyncWithFreshToken(context,
                        SyncService.CREATE_EQUIPMENT, resultReceiver).execute();
        }
    }

    /**
     * The asynchrone request task
     */
    private class RequestDatabase extends AsyncTask<Void, Void, Void> {

        Context context;

        RequestDatabase(final Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            adapter.notifyItemRangeRemoved(0, dataset.size());
        }

        @Override
        protected Void doInBackground(Void... voids) {

            AppDatabase database = AppDatabase.getInstance(this.context);
            dataset.clear();

            if (searchText.length() < MIN_SEARCH_SIZE)
                dataset.addAll(database.dao().selectEquipment());
            else
                dataset.addAll(database.dao().searchEquipment("%" + searchText + "%"));

            equipmentNames.addAll(database.dao().selectEquipmentNames());

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            fragmentListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Object selection);
    }
}