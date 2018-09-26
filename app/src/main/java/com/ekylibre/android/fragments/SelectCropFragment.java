package com.ekylibre.android.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ekylibre.android.InterventionActivity;
import com.ekylibre.android.R;
import com.ekylibre.android.adapters.SelectCropAdapter;
import com.ekylibre.android.database.pojos.CropsByPlot;

import java.util.ArrayList;
import java.util.List;


public class SelectCropFragment extends DialogFragment {

    private Context context;

    private OnFragmentInteractionListener fragmentListener;
    public static TextView totalTextView;

    public List<CropsByPlot> dataset;

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

        dataset = InterventionActivity.cropList;

        // Disables AppBar
        if (getDialog().getWindow() != null)
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

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

        if (InterventionActivity.validated) {
            validateButton.setText(R.string.ok);
            Toolbar toolbar = inflatedView.findViewById(R.id.toolbar);
            toolbar.setTitle(getString(R.string.crop_list));
        }

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