package com.ekylibre.android.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ekylibre.android.MainActivity;
import com.ekylibre.android.R;
import com.ekylibre.android.adapters.CropDetailAdapter;
import com.ekylibre.android.adapters.CropInfo.CropItem;
import com.ekylibre.android.database.AppDatabase;
import com.ekylibre.android.database.pojos.Interventions;
import com.ekylibre.android.database.converters.Converters;
import com.ekylibre.android.utils.DateTools;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


public class InfoFragment extends DialogFragment {

    private Context context;

    private OnFragmentInteractionListener fragmentListener;
    private RecyclerView.Adapter adapter;

    public static List<Interventions> interventionsList;
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
            interIDs = bundle.getIntegerArrayList("interventionsIDs");
            //String[] fullName = bundle.getString("full_name").split(" \\| ");
            //Timber.i(fullName.toString());
            cropName.setText(bundle.getString("full_name"));
            Date startDate = Converters.toDate(bundle.getString("start_date"));
            Date stopDate = Converters.toDate(bundle.getString("stop_date"));
            cropProduction.setText(bundle.getString("production"));
            cropPeriods.setText(
                    String.format("Du %s au %s", DateTools.STANDARD_DISPLAY.format(startDate), DateTools.STANDARD_DISPLAY.format(stopDate)));
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