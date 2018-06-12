package com.ekylibre.android;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ekylibre.android.adapters.SelectEquipmentAdapter;
import com.ekylibre.android.database.AppDatabase;
import com.ekylibre.android.database.models.Equipment;
import com.ekylibre.android.services.SyncService;
import com.ekylibre.android.utils.Enums;

import java.util.ArrayList;
import java.util.Objects;


public class SelectEquipmentFragment extends DialogFragment {

    private static final String TAG = "SelectEquipmentFragment";

    private static final int MIN_SEARCH_SIZE = 2;

    private Context context;

    private OnFragmentInteractionListener fragmentListener;
    private RecyclerView.Adapter adapter;
    private TextView createEquipment;

    private String searchText;
    private ArrayList<Equipment> dataset;

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

        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        adapter = new SelectEquipmentAdapter(context, dataset, fragmentListener);
        recyclerView.setAdapter(adapter);

        return inflatedView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Window window = getDialog().getWindow();
        if (window != null)
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new RequestDatabase(context).execute();
    }

    public void createEquipmentDialog() {

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_create_equipment, null);

        builder.setView(dialogView);
        builder.setNegativeButton(R.string.cancel, (dialog, i) -> dialog.cancel());
        builder.setPositiveButton(R.string.create, (dialog, i) -> {
            new CreateNewEquipment(context, dialogView).execute();
            dialog.dismiss();
        });

        android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();

        // Adjust dialog window to wrap content horizontally
        Window window = dialog.getWindow();
        if (window != null)
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        AppCompatSpinner spinner = dialogView.findViewById(R.id.create_equipment_type_spinner);
        ArrayAdapter spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, Enums.EQUIMPMENT_NAMES);
        spinner.setAdapter(spinnerAdapter);
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
            database.dao().insert(new Equipment(null, name, type, number, MainActivity.FARM_ID));

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new RequestDatabase(context).execute();
            Intent intent = new Intent(context, SyncService.class);
            intent.setAction(SyncService.ACTION_CREATE_ARTICLES);
            context.startService(intent);
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