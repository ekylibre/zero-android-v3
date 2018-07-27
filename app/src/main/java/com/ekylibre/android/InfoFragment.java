package com.ekylibre.android;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ekylibre.android.adapters.CropDetailAdapter;
import com.ekylibre.android.adapters.CropInfo.CropItem;
import com.ekylibre.android.database.AppDatabase;
import com.ekylibre.android.database.pojos.Interventions;
import com.ekylibre.android.utils.Converters;
import com.ekylibre.android.utils.DateTools;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


public class InfoFragment extends DialogFragment {

    private Context context;

    private OnFragmentInteractionListener fragmentListener;
    private RecyclerView.Adapter adapter;

    private CropItem cropItem;

    private List<Interventions> interventionsList;
    private List<Integer> interIDs;


    public InfoFragment() {}

    public static InfoFragment newInstance(Bundle args) {

        InfoFragment fragment = new InfoFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getActivity();
        this.interventionsList = new ArrayList<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Disables AppBar
        Objects.requireNonNull(getDialog().getWindow()).requestFeature(Window.FEATURE_NO_TITLE);

        View inflatedView = inflater.inflate(R.layout.fragment_crop_info, container, false);

        TextView cropName = inflatedView.findViewById(R.id.crop_detail_crop_name);
        TextView cropProduction = inflatedView.findViewById(R.id.crop_detail_production);
        TextView cropPeriods = inflatedView.findViewById(R.id.crop_detail_periods);
        TextView cropYield = inflatedView.findViewById(R.id.crop_detail_yield);
        TextView cropSurface = inflatedView.findViewById(R.id.crop_detail_surface);

        RecyclerView recyclerView = inflatedView.findViewById(R.id.crop_detail_recycler);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            cropItem = new CropItem();
            cropItem.setUUID(bundle.getString("uuid"));
            interIDs = bundle.getIntegerArrayList("interventionsIDs");
            String[] fullName = bundle.getString("full_name").split(" \\| ");
            cropName.setText(fullName[0]);
            Date startDate = Converters.toDate(bundle.getString("start_date"));
            Date stopDate = Converters.toDate(bundle.getString("stop_date"));
            cropProduction.setText(fullName[1]);
            cropPeriods.setText(
                    String.format("Du %s au %s", DateTools.display(startDate), DateTools.display(stopDate)));
            cropYield.setText(String.format("Rendement: %s", bundle.getString("yield", "non renseigné")));
            cropSurface.setText(String.format(MainActivity.LOCALE, "%.1f ha", bundle.getFloat("surface")));
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        adapter = new CropDetailAdapter(context, interventionsList);
        recyclerView.setAdapter(adapter);

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
        protected Void doInBackground(Void... voids) {
            AppDatabase database = AppDatabase.getInstance(context);
            interventionsList.clear();
            interventionsList.addAll(database.dao().selectInterventionsByInterIDs(interIDs));

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