package com.ekylibre.android;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ekylibre.android.adapters.SelectPersonAdapter;
import com.ekylibre.android.database.AppDatabase;
import com.ekylibre.android.database.models.Person;
import com.ekylibre.android.services.SyncResultReceiver;
import com.ekylibre.android.services.SyncService;

import java.util.ArrayList;
import java.util.Objects;


public class SelectPersonFragment extends DialogFragment implements SyncResultReceiver.Receiver{

    private static final String TAG = SelectPersonFragment.class.getName();

    private static final int MIN_SEARCH_SIZE = 2;

    private Context context;

    private OnFragmentInteractionListener fragmentListener;
    private RecyclerView.Adapter adapter;
    private TextView createPerson;

    private String searchText;
    private ArrayList<Person> dataset;

    public SelectPersonFragment() {
    }

    public static SelectPersonFragment newInstance() {
        return new SelectPersonFragment();
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

        View inflatedView = inflater.inflate(R.layout.fragment_select_person, container, false);

        createPerson = inflatedView.findViewById(R.id.person_dialog_create_new);
        SearchView searchView = inflatedView.findViewById(R.id.search_person);
        RecyclerView recyclerView = inflatedView.findViewById(R.id.person_dialog_recycler);

        createPerson.setOnClickListener(view -> createPersonDialog());
        searchView.setOnSearchClickListener(view -> createPerson.setVisibility(View.GONE));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchText = query;
                new RequestDatabase(context).execute();
                createPerson.setVisibility(View.VISIBLE);
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
            createPerson.setVisibility(View.VISIBLE);
            return false;
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        adapter = new SelectPersonAdapter(dataset, fragmentListener);
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

    public void createPersonDialog() {

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_create_person, null);

        builder.setView(dialogView);
        builder.setNegativeButton("Annuler", (dialog, i) -> dialog.cancel());
        builder.setPositiveButton("Créer", (dialog, i) -> {
            new CreateNewPerson(context, dialogView).execute();
            dialog.dismiss();
        });

        android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();

        // Adjust dialog window to wrap content horizontally
        Window window = dialog.getWindow();
        if (window != null)
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
//        if (resultCode == SyncService.DONE) {
//            int remote_id = resultData.getInt("remote_id", 0);
//
//            new SetEquipmentId(context, name, remote_id).execute();
//
//            for (Equipment equipment : dataset) {
//                if (equipment.name.equals(name)) {
//                    equipment.eky_id = remote_id;
//                    break;
//                }
//            }
//        }
    }

    class CreateNewPerson extends AsyncTask<Void, Void, Void> {

        Context context;
        View dialogView;

        CreateNewPerson(Context context, View dialogView) {
            this.context = context;
            this.dialogView = dialogView;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            TextInputLayout textInputLayout = dialogView.findViewById(R.id.create_person_firstname);
            String firstName = textInputLayout.getEditText().getText().toString();

            textInputLayout = dialogView.findViewById(R.id.create_person_lastname);
            String lastName = textInputLayout.getEditText().getText().toString();

//            textInputLayout = dialogView.findViewById(R.id.create_person_description);
//            String description = textInputLayout.getEditText().getText().toString();

            AppDatabase database = AppDatabase.getInstance(context);
            database.dao().insert(new Person(null, firstName, lastName, MainActivity.FARM_ID));

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new RequestDatabase(context).execute();
            Intent intent = new Intent(context, SyncService.class);
            intent.setAction(SyncService.ACTION_CREATE_PERSON_AND_EQUIPMENT);
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
                dataset.addAll(database.dao().selectPerson());
            else
                dataset.addAll(database.dao().searchPerson("%" + searchText + "%"));

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