package com.ekylibre.android;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ekylibre.android.adapters.SelectInputAdapter;
import com.ekylibre.android.database.AppDatabase;
import com.ekylibre.android.database.models.Equipment;
import com.ekylibre.android.database.models.Fertilizer;
import com.ekylibre.android.database.models.Phyto;
import com.ekylibre.android.database.models.Seed;
import com.ekylibre.android.database.pojos.Fertilizers;
import com.ekylibre.android.database.pojos.Phytos;
import com.ekylibre.android.database.pojos.Seeds;
import com.ekylibre.android.services.SyncResultReceiver;
import com.ekylibre.android.services.SyncService;
import com.ekylibre.android.utils.App;
import com.ekylibre.android.utils.Enums;

import java.util.ArrayList;
import java.util.Objects;


public class SelectInputFragment extends DialogFragment implements SyncResultReceiver.Receiver{

    private static final String TAG = SelectInputFragment.class.getName();

    private static final int SEED = 0, PHYTO = 1, FERTI = 2;
    private static final int MIN_SEARCH_SIZE = 2;

    private Context context;
    private AppDatabase database;

    private SyncResultReceiver resultReceiver;
    private OnFragmentInteractionListener fragmentListener;
    private RecyclerView.Adapter adapter;
    private TextView createInput;
    private TabLayout tabLayout;

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

        resultReceiver = new SyncResultReceiver(new Handler());
        resultReceiver.setReceiver(this);

        switch (InterventionActivity.procedure) {

            case App.IMPLANTATION:
                this.currentTab = SEED; break;

            case App.CROP_PROTECTION:
                this.currentTab = PHYTO; break;

            case App.FERTILIZATION:
                this.currentTab = FERTI; break;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Disables AppBar
        Objects.requireNonNull(getDialog().getWindow()).requestFeature(Window.FEATURE_NO_TITLE);

        View inflatedView = inflater.inflate(R.layout.fragment_select_input, container, false);

        tabLayout = inflatedView.findViewById(R.id.input_dialog_tabs);
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TabLayout.Tab tab = tabLayout.getTabAt(currentTab);
        if (tab != null) {
            switch (currentTab) {
                case PHYTO: createInput.setText(R.string.create_new_phyto); break;
                case SEED: createInput.setText(R.string.create_new_seed); break;
                case FERTI: createInput.setText(R.string.create_new_ferti); break;
            }
            tab.select();
        }

        database = AppDatabase.getInstance(context);

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
        builder.setNegativeButton(R.string.cancel, (dialog, i) -> dialog.cancel());
        builder.setPositiveButton(R.string.create, (dialog, i) -> {
            new CreateInput(context, dialogView).execute();
            dialog.dismiss();
        });

        android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();

        // Adjust dialog window to wrap content horizontally
        Window window = dialog.getWindow();
        if (window != null)
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        if (currentTab == SEED) {
            AppCompatSpinner spinner = dialogView.findViewById(R.id.create_seed_spinner);
            ArrayAdapter spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, Enums.SPECIE_NAMES);
            spinner.setAdapter(spinnerAdapter);
        }

//        LayoutInflater dialogInflater = LayoutInflater.from(finalContext);
//        View dialogView = dialogInflater.inflate(R.layout.dialog_create_seed, null);
//        AlertDialog.Builder dialog = new AlertDialog.Builder(finalContext);

//        // set the custom dialog components - text, image and button
//        AppCompatSpinner typeSpinner = dialog.findViewById(R.id.new_input_type);
//
//        Button createInput = dialog.findViewById(R.id.button_create_input);
//        createInput.setOnClickListener(v -> {
//            String nature = typeSpinner.getSelectedItem().toString();
//            //if (nature == )
//            dialog.dismiss();
//
//        });

        //dialog.show();

        //AppDatabase db = AppDatabase.getInstance(this.context);
        //db.seedDAO().insert(seed);

    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {

//        if (resultCode == SyncService.DONE) {
//            int local_id = resultData.getInt("local_id", 0);
//            int remote_id = resultData.getInt("remote_id", 0);
//            database.dao().setPhytoEkyId(remote_id, local_id);
//        }

        new RequestDatabase(context).execute();
    }

    /**
     * The asynchrone input creation task
     */
    class CreateInput extends AsyncTask<Void, Void, Void> {

        Context context;
        View dialogView;
        long createdPhytoId;

        CreateInput(Context context, View dialogView) {
            this.context = context;
            this.dialogView = dialogView;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            Integer newId;

            switch (currentTab) {

                case SEED:

                    AppCompatSpinner specieSpinner = dialogView.findViewById(R.id.create_seed_spinner);
                    String specie = Enums.SPECIE_TYPES.get(specieSpinner.getSelectedItemPosition());

                    TextInputLayout varietyWrapper = dialogView.findViewById(R.id.create_seed_variety);
                    String variety = varietyWrapper.getEditText().getText().toString();

                    newId = database.dao().lastSeedId();
                    newId = (newId != null) ? ++newId : 1;

                    database.dao().insert(new Seed(newId, null, specie, variety, false, true, "KILOGRAM"));
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
                    Integer reentryInt = (!reentry.isEmpty()) ? Integer.parseInt(reentry) : null;

                    newId = database.dao().lastPhytosanitaryId();
                    newId = (newId != null) ? ++newId : 100000;

                    Phyto phyto = new Phyto(newId, null, name, null, maaid, null, reentryInt, brand, false, true, "LITER");
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

                    database.dao().insert(new Fertilizer(newId, null, null, fertiName, null, null,
                            null, nature, null,null,
                            null, null, false, true, "KILOGRAM"));
                    break;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new RequestDatabase(context).execute();
            Intent intent = new Intent(context, SyncService.class);
            intent.setAction(SyncService.ACTION_CREATE_ARTICLE);
            intent.putExtra("receiver", resultReceiver);
            //intent.putExtra("createdPhytoId", createdPhytoId);
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
            adapter.notifyItemRangeRemoved(0, selectedList.size());
        }

        @Override
        protected Void doInBackground(Void... voids) {
            selectedList.clear();
            switch (currentTab) {

                case SEED:
                    if (searchText.length() < MIN_SEARCH_SIZE)
                        selectedList.addAll(database.dao().selectSeed());
                    else
                        selectedList.addAll(database.dao().searchSeedVariety("%" + searchText + "%"));

                    // Remove current items
                    for (Object item : InterventionActivity.inputList) {
                        if (item instanceof Seeds) {
                            Seed seed = ((Seeds) item).seed.get(0);
                            for (Object object : selectedList) {
                                if (((Seed) object).id.equals(seed.id)) {
                                    selectedList.remove(object);
                                    break;
                                }
                            }
                        }
                    }
                    break;

                case PHYTO:
                    if (searchText.length() < MIN_SEARCH_SIZE)
                        selectedList.addAll(database.dao().selectPhytosanitary());
                    else
                        selectedList.addAll(database.dao().searchPhytosanitary("%" + searchText + "%"));

                    // Remove current items
                    for (Object item : InterventionActivity.inputList) {
                        if (item instanceof Phytos) {
                            Phyto phyto = ((Phytos) item).phyto.get(0);
                            for (Object object : selectedList) {
                                if (((Phyto) object).id.equals(phyto.id)) {
                                    selectedList.remove(object);
                                    break;
                                }
                            }
                        }
                    }
                    break;

                case FERTI:
                    if (searchText.length() < MIN_SEARCH_SIZE)
                        selectedList.addAll(database.dao().selectFertilizer());
                    else
                        selectedList.addAll(database.dao().searchFertilizer("%" + searchText + "%"));

                    // Remove current items
                    for (Object item : InterventionActivity.inputList) {
                        if (item instanceof Fertilizers) {
                            Fertilizer fertilizer = ((Fertilizers) item).fertilizer.get(0);
                            for (Object object : selectedList) {
                                if (((Fertilizer) object).id.equals(fertilizer.id)) {
                                    selectedList.remove(object);
                                    break;
                                }
                            }
                        }
                    }
                    break;
            }



            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (BuildConfig.DEBUG) Log.e(TAG, "REQUESTDATABASE --> onPostExecute()");
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