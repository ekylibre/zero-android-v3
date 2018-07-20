package com.ekylibre.android;

import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ekylibre.android.adapters.SelectMaterialAdapter;
import com.ekylibre.android.database.AppDatabase;
import com.ekylibre.android.database.models.Material;
import com.ekylibre.android.database.pojos.Crops;

import java.util.ArrayList;
import java.util.Objects;


public class InfoFragment extends DialogFragment {

    private Context context;

    private OnFragmentInteractionListener fragmentListener;
    private RecyclerView.Adapter adapter;

    private ArrayList<Crops> dataset;

    public InfoFragment() {
    }

    public static InfoFragment newInstance() {
        return new InfoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getActivity();
        this.dataset = new ArrayList<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Disables AppBar
        Objects.requireNonNull(getDialog().getWindow()).requestFeature(Window.FEATURE_NO_TITLE);

        View inflatedView = inflater.inflate(R.layout.fragment_crop_info, container, false);

//        createMaterial = inflatedView.findViewById(R.id.material_dialog_create_new);
//        SearchView searchView = inflatedView.findViewById(R.id.search_material);
//        RecyclerView recyclerView = inflatedView.findViewById(R.id.material_dialog_recycler);
//
//        createMaterial.setOnClickListener(view -> createMaterialDialog());
//        searchView.setOnSearchClickListener(view -> createMaterial.setVisibility(View.GONE));
//
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                searchText = query;
//                new RequestDatabase(context).execute();
//                createMaterial.setVisibility(View.VISIBLE);
//                return false;
//            }
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                searchText = newText;
//                if (newText.length() > 2)
//                    new RequestDatabase(context).execute();
//                return false;
//            }
//        });
//
//        searchView.setOnCloseListener(() -> {
//            new RequestDatabase(context).execute();
//            createMaterial.setVisibility(View.VISIBLE);
//            return false;
//        });
//
//        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
//        adapter = new SelectMaterialAdapter(dataset, fragmentListener);
//        recyclerView.setAdapter(adapter);

        return inflatedView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
            //dataset.addAll(database.dao().selectMaterial());

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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Object selection);
    }
}