package com.ekylibre.android.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekylibre.android.MainActivity;
import com.ekylibre.android.R;
import com.ekylibre.android.SelectCropFragment;
import com.ekylibre.android.database.models.Crop;
import com.ekylibre.android.database.pojos.CropWithPlots;
import com.ekylibre.android.database.pojos.Crops;
import com.ekylibre.android.database.pojos.PlotWithCrops;
import com.ekylibre.android.database.relations.InterventionCrop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SelectCropAdapter extends RecyclerView.Adapter<SelectCropAdapter.ViewHolder> {

    private static final String TAG = SelectCropAdapter.class.getName();

    private List<PlotWithCrops> dataset;
    private SelectCropFragment.OnFragmentInteractionListener fragmentListener;

    public SelectCropAdapter(List<PlotWithCrops> dataset, SelectCropFragment.OnFragmentInteractionListener fragmentListener) {
        this.dataset = dataset;
        this.fragmentListener = fragmentListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView plotArea;
        CheckBox plotCheckBox;
        ImageView plotArrow;
        LinearLayoutCompat cropContainer;
        LayoutInflater inflater;

        //List<Crop> crop = new ArrayList<>();

        ViewHolder(View itemView) {
            super(itemView);

            plotArea = itemView.findViewById(R.id.plot_area);
            plotCheckBox = itemView.findViewById(R.id.plot_checkbox);
            plotArrow = itemView.findViewById(R.id.plot_arrow);
            cropContainer = itemView.findViewById(R.id.crop_container);

            inflater = (LayoutInflater) itemView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        void display(PlotWithCrops item) {

            int index = 0;
            for (Crop crop : item.crops) {

                View child = inflater.inflate(R.layout.item_crop, null);

                if (++index < item.crops.size())
                    child.setBackgroundResource(R.drawable.border_bottom_lightgrey);

                CheckBox cropCheckBox = child.findViewById(R.id.crop_checkbox);
                TextView cropName = child.findViewById(R.id.crop_name);
                TextView cropArea = child.findViewById(R.id.crop_area);
                cropName.setText(crop.name);
                cropArea.setText(String.format(MainActivity.LOCALE, "%.1f ha travaillés", crop.surface_area));

                cropCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    crop.is_checked = isChecked;
                    updateTotal();
                });

                cropContainer.addView(child);
            }

            plotCheckBox.setText(item.plot.name);
            plotArea.setText(String.format(MainActivity.LOCALE, "%.1f ha", item.plot.surface_area));

            plotCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

                item.plot.is_checked = isChecked;

                for (Crop crop : item.crops)
                    crop.is_checked = isChecked;

                notifyDataSetChanged();

                updateTotal();
            });
        }

        void updateTotal() {

        }
    }

    @NonNull
    @Override
    public SelectCropAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plot, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.display(dataset.get(position));

    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

}