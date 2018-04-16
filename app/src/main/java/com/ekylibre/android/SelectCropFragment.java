package com.ekylibre.android;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ekylibre.android.adapters.SelectCropAdapter;
import com.ekylibre.android.database.AppDatabase;
import com.ekylibre.android.database.models.Plot;
import com.ekylibre.android.database.pojos.PlotWithCrops;

import java.util.ArrayList;
import java.util.List;


public class SelectCropFragment extends DialogFragment {

    private static final String TAG = SelectCropFragment.class.getName();

    private Context context;

    private OnFragmentInteractionListener fragmentListener;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    public static TextView totalTextView;
    private AppCompatButton validateButton;

    public ArrayList<PlotWithCrops> dataset;

    public SelectCropFragment() {
    }

    public static SelectCropFragment newInstance() {
        return new SelectCropFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getActivity();
        this.dataset = new ArrayList<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View inflatedView = inflater.inflate(R.layout.fragment_select_crop, container, false);

        validateButton = inflatedView.findViewById(R.id.button_validate);
        totalTextView = inflatedView.findViewById(R.id.crop_dialog_total);
        recyclerView = inflatedView.findViewById(R.id.crop_dialog_recycler);

        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        adapter = new SelectCropAdapter(context, dataset, fragmentListener);
        recyclerView.setAdapter(adapter);

        validateButton.setOnClickListener(view ->
                fragmentListener.onFragmentInteraction(dataset)
        );

        return inflatedView;
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
        adapter.notifyDataSetChanged();
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