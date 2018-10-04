package com.ekylibre.android.adapters;


import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ekylibre.android.InterventionActivity;
import com.ekylibre.android.MainActivity;
import com.ekylibre.android.R;
import com.ekylibre.android.database.models.Harvest;
import com.ekylibre.android.database.models.Seed;
import com.ekylibre.android.database.pojos.Crops;
import com.ekylibre.android.database.pojos.Equipments;
import com.ekylibre.android.database.pojos.Fertilizers;
import com.ekylibre.android.database.pojos.Interventions;
import com.ekylibre.android.database.pojos.Phytos;
import com.ekylibre.android.database.pojos.Seeds;
import com.ekylibre.android.utils.App;
import com.ekylibre.android.utils.DateTools;
import com.ekylibre.android.utils.Units;
import com.ekylibre.android.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;


public class CropDetailAdapter extends RecyclerView.Adapter<CropDetailAdapter.ViewHolder> {

    private List<Interventions> interventionsList;
    private Context context;

    private static SimpleDateFormat SIMPLE_DATE = new SimpleDateFormat("HH:mm", MainActivity.LOCALE);


    public CropDetailAdapter(Context context, List<Interventions> interventionsList) {
        this.interventionsList = interventionsList;
        this.context = context;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final AppCompatImageView itemIcon, itemSynchronized;
        private final TextView itemProcedure, itemDate, itemCrops, itemInfos;
        private final View itemBackground;

        ViewHolder(final View itemView, int viewType) {
            super(itemView);

            itemBackground = itemView.findViewById(R.id.intervention_item_layout);
            itemIcon = itemView.findViewById(R.id.item_icon);
            itemProcedure = itemView.findViewById(R.id.item_procedure);
            itemDate = itemView.findViewById(R.id.item_date);
            itemCrops = itemView.findViewById(R.id.item_cultures);
            itemInfos = itemView.findViewById(R.id.item_infos);
            itemSynchronized = itemView.findViewById(R.id.item_synchronized);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), InterventionActivity.class);
                intent.putExtra("intervention_id", getAdapterPosition());
                intent.putExtra("edition", true);
                itemView.getContext().startActivity(intent);
            });
        }
    }

    @NonNull
    @Override
    public CropDetailAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_intervention, parent, false);

        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (position %2 == 1) {
            holder.itemBackground.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
        } else {
            holder.itemBackground.setBackgroundColor(ContextCompat.getColor(context, R.color.another_light_grey));
        }

        Interventions current = interventionsList.get(position);
        holder.itemIcon.setImageResource(Utils.getResId(context,"procedure_" + current.intervention.type.toLowerCase(), "drawable"));
        holder.itemProcedure.setText(Utils.getTranslation(context, current.intervention.type));
        holder.itemDate.setText(DateTools.display(current.workingDays.get(0).execution_date));

        switch (current.intervention.status) {
            case InterventionActivity.SYNCED:
                holder.itemSynchronized.setImageResource(R.drawable.icon_check);
                ImageViewCompat.setImageTintList(holder.itemSynchronized, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.success)));
                break;
            case InterventionActivity.VALIDATED:
                holder.itemSynchronized.setImageResource(R.drawable.icon_check_validated);
                ImageViewCompat.setImageTintList(holder.itemSynchronized, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.success)));
                break;
            default:
                holder.itemSynchronized.setImageResource(R.drawable.icon_sync);
                ImageViewCompat.setImageTintList(holder.itemSynchronized, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.warning)));
                break;
        }

        // Count parcels and surface
        float total = 0;
        int count = 0;
        for (Crops crop : current.crops) {
            total +=  crop.crop.get(0).surface_area * crop.inter.work_area_percentage / 100;
            ++count;
        }

        String cropCount = context.getResources().getQuantityString(R.plurals.crops, count, count);
        String totalString = String.format(MainActivity.LOCALE, "%s • %.1f ha", cropCount, total);
        holder.itemCrops.setText(totalString);

        // Display input by nature
        StringBuilder sb = new StringBuilder();
        switch (current.intervention.type) {

            case App.CROP_PROTECTION:
                for (Phytos p : current.phytos) {
                    sb.append(p.phyto.get(0).name).append(" • ");
                    sb.append(String.format(MainActivity.LOCALE, "%.1f", p.inter.quantity)).append(" ");
                    sb.append(Objects.requireNonNull(Units.getUnit(p.inter.unit)).name);
                    if (current.phytos.indexOf(p) + 1 != current.phytos.size()) sb.append("\n");
                }
                break;

            case App.IMPLANTATION:
                for (Seeds s : current.seeds) {
                    Seed seed = s.seed.get(0);
                    String specie;
                    if (seed.specie != null)
                        specie = Utils.getTranslation(context, seed.specie.toUpperCase());
                    else
                        specie = seed.variety;
                    sb.append(specie).append(" • ");
                    sb.append(String.format(MainActivity.LOCALE, "%.1f", s.inter.quantity)).append(" ");
                    sb.append(Objects.requireNonNull(Units.getUnit(s.inter.unit)).name);
                    if (current.seeds.indexOf(s) + 1 != current.seeds.size()) sb.append("\n");
                }
                break;

            case App.FERTILIZATION:
                for (Fertilizers f : current.fertilizers) {
                    sb.append(f.fertilizer.get(0).label_fra).append(" • ");
                    sb.append(String.format(MainActivity.LOCALE, "%.1f", f.inter.quantity)).append(" ");
                    sb.append(Objects.requireNonNull(Units.getUnit(f.inter.unit)).name);
                    if (current.fertilizers.indexOf(f) + 1 != current.fertilizers.size()) sb.append("\n");
                }
                break;

//            case MainActivity.CARE:
//                for (Materials m : current.materials) {
//                    sb.append(m.material.get(0).name).append(" • ");
//                    sb.append(m.inter.quantity).append(" ");
//                    sb.append(unitValues.get(unitKeys.indexOf(m.inter.unit)));
//                    if (current.materials.indexOf(m) + 1 != current.materials.size()) sb.append("\n");
//                }
//                break;

            case App.CARE:
            case App.GROUND_WORK:
                for (Equipments e : current.equipments) {
                    sb.append(e.equipment.get(0).name);
                    if (current.equipments.indexOf(e) + 1 != current.equipments.size()) sb.append("\n");
                }
                break;

            case App.IRRIGATION:
                if (current.intervention.water_quantity != null) {
                    sb.append("Volume • ").append(current.intervention.water_quantity).append(" ");
                    sb.append(Objects.requireNonNull(Units.getUnit(current.intervention.water_unit)).name);
                    break;
                }

            case App.HARVEST:
                if (current.harvests.size() > 0) {
                    for (Harvest harvest : current.harvests) {
                        sb.append(Utils.getTranslation(context, harvest.type)).append(" • ");
                        sb.append(String.format(MainActivity.LOCALE, "%.1f %s", harvest.quantity, Objects.requireNonNull(Units.getUnit(harvest.unit)).name));
                        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
                        if (current.harvests.indexOf(harvest) + 1 != current.harvests.size()) sb.append("\n");
                    }
                }

                break;
        }
        holder.itemInfos.setText(sb.toString());
    }

    @Override
    public int getItemCount() {
        return interventionsList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? 1 : 2;
    }
}
