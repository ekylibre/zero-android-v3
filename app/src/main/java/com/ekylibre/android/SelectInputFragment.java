package com.ekylibre.android;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ekylibre.android.adapters.SelectInputAdapter;
import com.ekylibre.android.database.models.Fertilizer;
import com.ekylibre.android.database.models.Phyto;
import com.ekylibre.android.database.models.Seed;

import java.util.ArrayList;


public class SelectInputFragment extends DialogFragment {

    private static final String TAG = SelectInputFragment.class.getName();

    private static final int SEED = 0, PHYTO = 1, FERTI = 2;
    private static final int MIN_SEARCH_SIZE = 2;

    private Context context;

    private OnFragmentInteractionListener fragmentListener;
    private RecyclerView.Adapter adapter;
    private TextView createInput;

    private int currentTab;
    private String searchText;
    private ArrayList<Object> selectedList;

    public SelectInputFragment() {
    }

    public static SelectInputFragment newInstance() {
        return new SelectInputFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getActivity();
        this.selectedList = new ArrayList<>();
        this.searchText = "";
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView = inflater.inflate(R.layout.fragment_select_input, container, false);

        TabLayout tabLayout = inflatedView.findViewById(R.id.input_dialog_tabs);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.seeds));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.phytos));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.fertilizers));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                switch (currentTab) {
                    case PHYTO: createInput.setText(R.string.create_new_phyto); break;
                    case SEED: createInput.setText(R.string.create_new_seed); break;
                    case FERTI: createInput.setText(R.string.create_new_ferti); break;
                }
                new RequestDatabase(context).execute();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        createInput = inflatedView.findViewById(R.id.input_dialog_create_input);
        SearchView searchView = inflatedView.findViewById(R.id.search_input);
        RecyclerView recyclerView = inflatedView.findViewById(R.id.input_dialog_recycler);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchText = query;
                new RequestDatabase(context).execute();
                createInput.setVisibility(View.VISIBLE);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                searchText = newText;
                if (newText.length() > 2) new RequestDatabase(context).execute();
                return false;
            }
        });

        searchView.setOnCloseListener(() -> {
            new RequestDatabase(context).execute();
            createInput.setVisibility(View.VISIBLE);
            return false;
        });

        searchView.setOnSearchClickListener(view -> createInput.setVisibility(View.GONE));

        createInput.setOnClickListener(view -> createInputDialog());

        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        adapter = new SelectInputAdapter(selectedList, context, fragmentListener);
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
        Log.e(TAG, "OnViewCreated...");

        new RequestDatabase(context).execute();
    }

    /**
     * Dialog for new input creation
     */
    public void createInputDialog() {

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);

        int layoutResId = 0;

        switch (currentTab) {
            case SEED: layoutResId = R.layout.dialog_create_seed; break;
            case PHYTO: layoutResId = R.layout.dialog_create_phyto; break;
            case FERTI: layoutResId = R.layout.dialog_create_ferti; break;
        }

        View dialogView = getActivity().getLayoutInflater().inflate(layoutResId, null);

        //builder.setMessage("Remplissez le formulaire puis validez pour créer un intrant");

//        AppCompatSpinner typeSpinner = dialogView.findViewById(R.id.new_input_type);
//        List<String> typeList = new ArrayList<>();
//        typeList.add(context.getResources().getString(R.string.seeds));
//        typeList.add(context.getResources().getString(R.string.fertilizers));
//        ArrayAdapter<String> inputAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, typeList);
//        typeSpinner.setAdapter(inputAdapter);

        builder.setView(dialogView);
        builder.setNegativeButton("Annuler", (dialog, i) -> dialog.cancel());
        builder.setPositiveButton("Créer", (dialog, i) -> {
            new CreateInput(context, dialogView).execute();
            dialog.dismiss();
        });

        android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();

        // Adjust dialog window to wrap content horizontally
        Window window = dialog.getWindow();
        if (window != null)
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

//        LayoutInflater dialogInflater = LayoutInflater.from(finalContext);
//        View dialogView = dialogInflater.inflate(R.layout.dialog_create_seed, null);
//        AlertDialog.Builder dialog = new AlertDialog.Builder(finalContext);

//        // set the custom dialog components - text, image and button
//        AppCompatSpinner typeSpinner = dialog.findViewById(R.id.new_input_type);
//
//        Button createInput = dialog.findViewById(R.id.button_create_input);
//        createInput.setOnClickListener(v -> {
//            String type = typeSpinner.getSelectedItem().toString();
//            //if (type == )
//            dialog.dismiss();
//
//        });

        //dialog.show();

        //AppDatabase db = AppDatabase.getInstance(this.context);
        //db.seedDAO().insert(seed);

    }

    /**
     * The asynchrone input creation task
     */
    class CreateInput extends AsyncTask<Void, Void, Void> {

        Context context;
        View dialogView;

        CreateInput(Context context, View dialogView) {
            this.context = context;
            this.dialogView = dialogView;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            AppDatabase database = AppDatabase.getInstance(context);
            Integer newId;

            switch (currentTab) {

                case SEED:

                    AppCompatSpinner specieSpinner = dialogView.findViewById(R.id.create_seed_spinner);
                    int spinner_pos = specieSpinner.getSelectedItemPosition();
                    String specie = getResources().getStringArray(R.array.species_keys)[spinner_pos];

                    TextInputLayout varietyWrapper = dialogView.findViewById(R.id.create_seed_variety);
                    String variety = varietyWrapper.getEditText().getText().toString();

                    newId = database.dao().lastSeedId();
                    newId = (newId != null) ? ++newId : 1;

                    database.dao().insert(new Seed(newId, specie, variety, false, true));
                    break;

                case PHYTO:

                    TextInputLayout wrapper = dialogView.findViewById(R.id.create_phyto_name);
                    String name = wrapper.getEditText().getText().toString();

                    wrapper = dialogView.findViewById(R.id.create_phyto_brand);
                    String brand = wrapper.getEditText().getText().toString();

                    wrapper = dialogView.findViewById(R.id.create_phyto_maaid);
                    String maaid = wrapper.getEditText().getText().toString();

                    wrapper = dialogView.findViewById(R.id.create_phyto_reentry);
                    String reentry = wrapper.getEditText().getText().toString();
                    int reentryInt = (!reentry.isEmpty()) ? Integer.parseInt(reentry) : -1;

                    newId = database.dao().lastPhytosanitaryId();
                    newId = (newId != null) ? ++newId : 50000;

                    Phyto phyto = new Phyto(newId, name, null, maaid, -1, reentryInt, brand, false, true);
                    database.dao().insert(phyto);
                    break;

                case FERTI:

                    AppCompatSpinner natureSpinner = dialogView.findViewById(R.id.create_ferti_nature_spinner);
                    int natureSpinnerPos = natureSpinner.getSelectedItemPosition();
                    String nature = (natureSpinnerPos == 0) ? "organic" : "mineral";

                    TextInputLayout nameWrapper = dialogView.findViewById(R.id.create_ferti_name);
                    String fertiName = nameWrapper.getEditText().getText().toString();

                    newId = database.dao().lastFertilizerId();
                    newId = (newId != null) ? ++newId : 1000;

                    database.dao().insert(new Fertilizer(newId, fertiName, fertiName, null, null,
                            null, nature, null,
                            null, null, null, false, true));
                    break;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new RequestDatabase(context).execute();
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
            adapter.notifyItemRangeRemoved(0, selectedList.size());
        }

        @Override
        protected Void doInBackground(Void... voids) {
            AppDatabase database = AppDatabase.getInstance(this.context);
            selectedList.clear();
            switch (currentTab) {

                case SEED:
                    if (searchText.length() < MIN_SEARCH_SIZE)
                        selectedList.addAll(database.dao().selectSeed());
                    else
                        selectedList.addAll(database.dao().searchSeedVariety("%" + searchText + "%"));
                    break;

                case PHYTO:
                    if (searchText.length() < MIN_SEARCH_SIZE)
                        selectedList.addAll(database.dao().selectPhytosanitary());
                    else
                        selectedList.addAll(database.dao().searchPhytosanitary("%" + searchText + "%"));
                    break;

                case FERTI:
                    if (searchText.length() < MIN_SEARCH_SIZE)
                        selectedList.addAll(database.dao().selectFertilizer());
                    else
                        selectedList.addAll(database.dao().searchFertilizer("%" + searchText + "%"));
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.e(TAG, "REQUESTDATABASE --> onPostExecute()");
            // inputAdapter.notifyItemRangeRemoved(0, currentSize);
            // inputAdapter = new SelectInputAdapter(selectedList);
            // inputRecyclerView.setAdapter(inputAdapter);
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