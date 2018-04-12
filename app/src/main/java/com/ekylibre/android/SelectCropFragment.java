package com.ekylibre.android;

import android.app.DialogFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;

import com.ekylibre.android.adapters.SelectCropAdapter;
import com.ekylibre.android.database.models.Plot;
import com.ekylibre.android.database.pojos.PlotWithCrops;

import java.util.ArrayList;
import java.util.List;


public class SelectCropFragment extends DialogFragment {

    private static final String TAG = SelectCropFragment.class.getName();

    private Context context;

    private OnFragmentInteractionListener fragmentListener;
    private RecyclerView.Adapter adapter;

    private ArrayList<PlotWithCrops> dataset;

    public SelectCropFragment() {
    }

    public static SelectCropFragment newInstance() {

        SelectCropFragment fragment = new SelectCropFragment();
        //fragment.setStyle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getActivity();
        this.dataset = new ArrayList<>();

        Log.e(TAG, "onCreate()");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().setTitle("Sélectionnez des cultures");

        View inflatedView = inflater.inflate(R.layout.fragment_select_crop, container, false);

        RecyclerView recyclerView = inflatedView.findViewById(R.id.crop_dialog_recycler);

        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        adapter = new SelectCropAdapter(dataset, fragmentListener);
        recyclerView.setAdapter(adapter);

        return inflatedView;
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.e(TAG, "onResume()");
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.e(TAG, "onStart()");

        Window window = getDialog().getWindow();
        if (window != null)
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (dataset.isEmpty())
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
            //dataset.clear();
            //dataset.addAll(database.dao().listCropWithPlots(new Date().getTime()));

            List<Plot> plotList = database.dao().plotList();


            for (Plot plot : plotList) {
                PlotWithCrops plotWithCrops = new PlotWithCrops(plot);
                plotWithCrops.crops = database.dao().cropsByPlotUuid(plot.uuid);  //new Date().getTime()
                dataset.add(plotWithCrops);
            }

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