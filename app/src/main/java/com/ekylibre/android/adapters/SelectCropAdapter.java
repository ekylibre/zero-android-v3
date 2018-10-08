package com.ekylibre.android.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekylibre.android.InterventionActivity;
import com.ekylibre.android.MainActivity;
import com.ekylibre.android.R;
import com.ekylibre.android.database.pojos.CropsByPlot;
import com.ekylibre.android.fragments.SelectCropFragment;
import com.ekylibre.android.database.models.Crop;
import com.mapbox.api.staticmap.v1.MapboxStaticMap;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;

import timber.log.Timber;

import static com.mapbox.api.staticmap.v1.StaticMapCriteria.LIGHT_STYLE;


public class SelectCropAdapter extends RecyclerView.Adapter<SelectCropAdapter.ViewHolder> {

    private Context context;
    private List<CropsByPlot> dataset;

    public SelectCropAdapter(Context context, List<CropsByPlot> dataset, SelectCropFragment.OnFragmentInteractionListener fragmentListener) {
        this.context = context;
        this.dataset = dataset;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView plotArea;
        CheckBox plotCheckBox;
        ImageView plotArrow;
        LinearLayoutCompat cropContainer;
        LayoutInflater inflater;

        ViewHolder(View itemView) {
            super(itemView);

            plotArea = itemView.findViewById(R.id.plot_area);
            plotCheckBox = itemView.findViewById(R.id.plot_checkbox);
            plotArrow = itemView.findViewById(R.id.plot_arrow);
            cropContainer = itemView.findViewById(R.id.crop_container);

            inflater = (LayoutInflater) itemView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        void display(CropsByPlot plot) {

            Timber.i("Plot %s", plot);

            plotCheckBox.setText(plot.name);
            plotCheckBox.setChecked(plot.is_checked);
            plotArea.setText(String.format(MainActivity.LOCALE, "%.1f ha", plot.getSurface(false)));

            // Display all crops associated to a plot
            displayCrops(plot);
            updateTotal();

            // Accordion control
            itemView.setOnClickListener(view -> {
                if (cropContainer.getVisibility() == View.GONE) {
                    plotArrow.setRotation(180);
                    cropContainer.setVisibility(View.VISIBLE);
                } else {
                    plotArrow.setRotation(0);
                    cropContainer.setVisibility(View.GONE);
                }
            });

            // Plot CheckBox onChecked
            View.OnClickListener listener = view -> {

                boolean isChecked = plotCheckBox.isChecked();

                // Save action (checked/unchecked) to dataset item plot and crops
                plot.is_checked = isChecked;

                for (Crop crop : plot.crops)
                    crop.is_checked = isChecked;

                // Updates crops display
                displayCrops(plot);

                // Updates surface area total
                updateTotal();
            };

            if (InterventionActivity.editIntervention == null) {
                plotCheckBox.setOnClickListener(listener);
            } else {
                if (InterventionActivity.validated) {
                    if (cropContainer.getVisibility() == View.GONE) {
                        itemView.performClick();
                        itemView.setOnClickListener(null);
                    }
                    plotCheckBox.setClickable(false);
                }
            }
        }

        void displayCrops(CropsByPlot plot) {

            // First remove all childs view
            cropContainer.removeAllViews();

            int index = 0;
            for (Crop crop : plot.crops) {

                // Inflate crom item layout
                View child = inflater.inflate(R.layout.item_crop, null);

                // Set bottom border if not latest element
                if (++index < plot.crops.size())
                    child.setBackgroundResource(R.drawable.border_bottom_lightgrey);

                ConstraintLayout cropLayout = child.findViewById(R.id.item_crop_layout);
                CheckBox cropCheckBox = child.findViewById(R.id.crop_checkbox);
                TextView cropName = child.findViewById(R.id.crop_name);
                TextView cropArea = child.findViewById(R.id.crop_area);
                ImageView cropMap = child.findViewById(R.id.crop_map);

                StringBuilder sb = new StringBuilder();
                sb.append(crop.production_nature);
                if (crop.production_mode.equals("Agriculture biologique"))
                    sb.append(" bio");
                Calendar cal = Calendar.getInstance();
                cal.setTime(crop.stop_date);
                sb.append(" ").append(cal.get(Calendar.YEAR));
                cropName.setText(sb);

                cropArea.setText(String.format(MainActivity.LOCALE, "%.1f ha travaillés", crop.surface_area));
                cropCheckBox.setChecked(crop.is_checked);

                MapboxStaticMap staticImage = MapboxStaticMap.builder()
                        .accessToken(context.getString(R.string.mapbox_token))
                        .width(128)
                        .height(128)
                        .retina(true)
                        .geoJson(crop.shape)
                        .cameraAuto(true)
                        .attribution(false)
                        .logo(false)
                        .styleId(LIGHT_STYLE)
                        .build();
                String imageUrl = staticImage.url().toString();
                Timber.i(imageUrl);
                Picasso.get().load(imageUrl).into(cropMap);  // .placeholder(R.drawable.icon_parcel)

                CheckBox.OnCheckedChangeListener checkListener = ((buttonView, isChecked) -> {
                    // Save action (checked/unchecked) to dataset item crop and update total
                    crop.is_checked = isChecked;
                    int cropSelected = 0;
                    for (Crop mCrop : plot.crops) {
                        if (mCrop.is_checked)
                            cropSelected++;
                    }
                    if (cropSelected > 0) {
                        plotCheckBox.setChecked(true);
                        plot.is_checked = true;
                    }
                    else {
                        plotCheckBox.setChecked(false);
                        plot.is_checked = false;
                    }
                    updateTotal();
                });

                View.OnClickListener cropListener = (view -> {
                    if (crop.is_checked) {
                        cropCheckBox.setChecked(false);
                    } else {
                        cropCheckBox.setChecked(true);
                    }
                });

                if (InterventionActivity.editIntervention == null) {
                    cropCheckBox.setOnCheckedChangeListener(checkListener);
                    cropLayout.setOnClickListener(cropListener);
                } else {
                    if (InterventionActivity.validated) {
                        cropCheckBox.setVisibility(View.GONE);
                    }
                }

                // Add rendered view to container layout
                cropContainer.addView(child);
            }
        }

        void updateTotal() {

            float total = 0;
            int count = 0;

            for (CropsByPlot plot : dataset)
                for (Crop crop : plot.crops)
                    if (crop.is_checked) {
                        total += crop.surface_area;
                        ++count;
                    }

            String totalString;
            String cropCount;
            if (total > 0) {
                cropCount = context.getResources().getQuantityString(R.plurals.crops, count, count);
                totalString = String.format(MainActivity.LOCALE, "%s • %.1f ha", cropCount, total);
            }
            else {
                totalString = context.getString(R.string.no_crop_selected);
            }
            SelectCropFragment.totalTextView.setText(totalString);
            InterventionActivity.cropSummaryText = totalString;
            InterventionActivity.surface = total;

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