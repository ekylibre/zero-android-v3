package com.ekylibre.android.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.ekylibre.android.R;
import com.ekylibre.android.adapters.SelectMaterialAdapter;
import com.ekylibre.android.database.AppDatabase;
import com.ekylibre.android.database.models.Material;
import com.ekylibre.android.services.ServiceResultReceiver;
import com.ekylibre.android.services.SyncService;
import com.ekylibre.android.utils.App;
import com.ekylibre.android.utils.PerformSyncWithFreshToken;
import com.ekylibre.android.utils.Units;

import java.util.ArrayList;
import java.util.Objects;

import static com.ekylibre.android.services.SyncService.CREATE_ARTICLE_DONE;


public class SelectMaterialFragment extends DialogFragment implements ServiceResultReceiver.Receiver {

    private static final int MIN_SEARCH_SIZE = 2;

    private Context context;

    private OnFragmentInteractionListener fragmentListener;
    private ServiceResultReceiver resultReceiver;
    private RecyclerView.Adapter adapter;
    private TextView createMaterial;

    private String searchText;
    private ArrayList<Material> dataset;

    public SelectMaterialFragment() {
    }

    public static SelectMaterialFragment newInstance() {
        return new SelectMaterialFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getActivity();
        this.dataset = new ArrayList<>();
        this.searchText = "";

        resultReceiver = new ServiceResultReceiver(new Handler());
        resultReceiver.setReceiver(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Disables AppBar
        Objects.requireNonNull(getDialog().getWindow()).requestFeature(Window.FEATURE_NO_TITLE);

        View inflatedView = inflater.inflate(R.layout.fragment_select_material, container, false);

        createMaterial = inflatedView.findViewById(R.id.material_dialog_create_new);
        SearchView searchView = inflatedView.findViewById(R.id.search_material);
        RecyclerView recyclerView = inflatedView.findViewById(R.id.material_dialog_recycler);

        createMaterial.setOnClickListener(view -> createMaterialDialog());
        searchView.setOnSearchClickListener(view -> createMaterial.setVisibility(View.GONE));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchText = query;
                new RequestDatabase(context).execute();
                createMaterial.setVisibility(View.VISIBLE);
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
            createMaterial.setVisibility(View.VISIBLE);
            return false;
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        adapter = new SelectMaterialAdapter(dataset, fragmentListener);
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new RequestDatabase(context).execute();
    }

    public void createMaterialDialog() {

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_create_material, null);

        Spinner spinner = dialogView.findViewById(R.id.create_material_unit_spinner);

        ArrayAdapter spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, Units.ALL_BASE_UNITS_L10N);
        spinner.setAdapter(spinnerAdapter);

        builder.setView(dialogView);
        builder.setNegativeButton("Annuler", (dialog, i) -> dialog.cancel());
        builder.setPositiveButton("Créer", (dialog, i) -> {
            new CreateNewMaterial(context, dialogView).execute();
            dialog.dismiss();
        });

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

        // Adjust dialog window to wrap content horizontally
        Window window = dialog.getWindow();
        if (window != null)
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    }

    class CreateNewMaterial extends AsyncTask<Void, Void, Void> {

        Context context;
        View dialogView;

        CreateNewMaterial(Context context, View dialogView) {
            this.context = context;
            this.dialogView = dialogView;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            AppDatabase database = AppDatabase.getInstance(context);

            TextInputLayout nameTextInput = dialogView.findViewById(R.id.create_material_name);
            String name = nameTextInput.getEditText().getText().toString();

//            TextInputLayout descTextInput = dialogView.findViewById(R.id.create_material_desc);
//            String desc = descTextInput.getEditText().getText().toString();

            AppCompatSpinner unitSpinner = dialogView.findViewById(R.id.create_material_unit_spinner);
            int spinner_pos = unitSpinner.getSelectedItemPosition();
            String unit = Units.ALL_BASE_UNITS.get(spinner_pos).key;

            database.dao().insert(new Material(null, name, null, unit));

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            new RequestDatabase(context).execute();

            if (App.isOnline(context))
                new PerformSyncWithFreshToken(context,
                        SyncService.CREATE_ARTICLE, resultReceiver).execute();
        }
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == CREATE_ARTICLE_DONE)
            new RequestDatabase(context).execute();
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
                dataset.addAll(database.dao().selectMaterial());
            else
                dataset.addAll(database.dao().searchMaterial("%" + searchText + "%"));

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