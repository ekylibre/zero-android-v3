package com.ekylibre.android.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ekylibre.android.InterventionActivity;
import com.ekylibre.android.R;
import com.ekylibre.android.adapters.SelectCropAdapter;
import com.ekylibre.android.database.pojos.Plots;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class SelectCropFragment extends DialogFragment {

    private Context context;

    private OnFragmentInteractionListener fragmentListener;
    public static TextView totalTextView;

    public List<Plots> dataset;

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

        dataset = InterventionActivity.plotList;

        // Disables AppBar
        Objects.requireNonNull(getDialog().getWindow()).requestFeature(Window.FEATURE_NO_TITLE);

        View inflatedView = inflater.inflate(R.layout.fragment_select_crop, container, false);

        AppCompatButton validateButton = inflatedView.findViewById(R.id.button_validate);
        totalTextView = inflatedView.findViewById(R.id.crop_dialog_total);
        RecyclerView recyclerView = inflatedView.findViewById(R.id.crop_dialog_recycler);

        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        RecyclerView.Adapter adapter = new SelectCropAdapter(context, dataset, fragmentListener);
        recyclerView.setAdapter(adapter);

        validateButton.setOnClickListener(view -> {
            fragmentListener.onFragmentInteraction(dataset);
            adapter.notifyItemRangeRemoved(0, dataset.size());
        });

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
     * Interface unused in this case
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Object selection);
    }

}