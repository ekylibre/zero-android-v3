package com.ekylibre.android.adapters;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekylibre.android.SelectInputFragment;
import com.ekylibre.android.R;
import com.ekylibre.android.database.models.Fertilizer;
import com.ekylibre.android.database.models.Seed;
import com.ekylibre.android.database.models.Phyto;
import com.ekylibre.android.database.pojos.Fertilizers;
import com.ekylibre.android.database.pojos.Phytos;
import com.ekylibre.android.database.pojos.Seeds;
import com.ekylibre.android.database.relations.InterventionFertilizer;
import com.ekylibre.android.database.relations.InterventionPhytosanitary;
import com.ekylibre.android.database.relations.InterventionSeed;

import java.util.Collections;
import java.util.List;


public class SelectInputAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "SelectInputAdapter";
    private final int SEED = 0, PHYTO = 1, FERTI = 2;

    private Context context;
    private List<Object> inputList;
    private SelectInputFragment.OnFragmentInteractionListener fragmentListener;

    public SelectInputAdapter(List<Object> inputList, Context context, SelectInputFragment.OnFragmentInteractionListener fragmentListener) {
        this.inputList = inputList;
        this.context = context;
        this.fragmentListener = fragmentListener;
    }

    class SeedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView seedSpecie, seedVariety;
        private ImageView seedFavorite;
        private Seed seed;

        SeedViewHolder(View itemView) {
            super(itemView);
            seedSpecie = itemView.findViewById(R.id.seed_specie);
            seedVariety = itemView.findViewById(R.id.seed_variety);
            seedFavorite = itemView.findViewById(R.id.seed_favorite);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.e(TAG, "SeedViewHolder onClick()");
            Seeds seedInput = new Seeds();
            seedInput.seed = Collections.singletonList(seed);
            seedInput.inter = new InterventionSeed(seed.id);
            Log.e(TAG, seedInput.toString());
            Log.e(TAG, seedInput.seed.get(0).specie);
            fragmentListener.onFragmentInteraction(seedInput);
        }
    }

    class PhytoViewHolder extends RecyclerView.ViewHolder {

        AppCompatTextView phytoName, phythoCompany, phytoAmm, phytoDelay;
        Phyto phyto;

        PhytoViewHolder(View itemView) {
            super(itemView);
            phytoName = itemView.findViewById(R.id.phyto_name);
            phythoCompany = itemView.findViewById(R.id.phyto_company);
            phytoAmm = itemView.findViewById(R.id.phyto_amm);
            phytoDelay = itemView.findViewById(R.id.phyto_delay);
            itemView.setOnClickListener(v -> {
                Phytos phytoInput = new Phytos();
                phytoInput.phyto = Collections.singletonList(phyto);
                phytoInput.inter = new InterventionPhytosanitary(phyto.id);
                fragmentListener.onFragmentInteraction(phytoInput);
            });
        }
    }

    class FertiViewHolder extends RecyclerView.ViewHolder {

        TextView fertiName, fertiType;
        //ImageView fertiFavorite;
        Fertilizer fertilizer;

        FertiViewHolder(View itemView) {
            super(itemView);
            fertiName = itemView.findViewById(R.id.fertilizer_name);
            fertiType = itemView.findViewById(R.id.fertilizer_type);
            itemView.setOnClickListener(v -> {
                Fertilizers fertiInput = new Fertilizers();
                fertiInput.fertilizer = Collections.singletonList(fertilizer);
                fertiInput.inter = new InterventionFertilizer(fertilizer.id);
                fragmentListener.onFragmentInteraction(fertiInput);
            });
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {

            case SEED:
                View seedView = inflater.inflate(R.layout.item_seed, parent, false);
                viewHolder = new SeedViewHolder(seedView);
                break;

            case PHYTO:
                View phytoView = inflater.inflate(R.layout.item_phyto, parent, false);
                viewHolder = new PhytoViewHolder(phytoView);
                break;

            case FERTI:
                View fertiView = inflater.inflate(R.layout.item_fertilizer, parent, false);
                viewHolder = new FertiViewHolder(fertiView);
                break;

            default:
                Log.e(TAG, "default viewHolder --> pas normal");
                View defaultView = inflater.inflate(R.layout.item_seed, parent, false);
                viewHolder = new SeedViewHolder(defaultView);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        switch (holder.getItemViewType()) {

            case SEED:
                SeedViewHolder seedViewHolder = (SeedViewHolder) holder;
                Seed seed = (Seed) inputList.get(position);
                int res = context.getResources().getIdentifier(seed.specie, "string", context.getPackageName());
                seedViewHolder.seedSpecie.setText(context.getString(res));
                seedViewHolder.seedVariety.setText(seed.variety);
                seedViewHolder.seedFavorite.setVisibility((!seed.registered) ? View.VISIBLE : View.GONE);
                seedViewHolder.seed = seed;
                break;

            case PHYTO:
                PhytoViewHolder phytoViewHolder = (PhytoViewHolder) holder;
                Phyto phyto = (Phyto) inputList.get(position);
                phytoViewHolder.phytoName.setText(phyto.name);
                phytoViewHolder.phythoCompany.setText(phyto.firm_name);
                phytoViewHolder.phytoAmm.setText((!phyto.maaid.isEmpty()) ? phyto.maaid : context.getString(R.string.unspecified));
                if (phyto.in_field_reentry_delay != -1)
                    phytoViewHolder.phytoDelay.setText(context.getResources().getQuantityString(R.plurals.x_hours, phyto.in_field_reentry_delay, phyto.in_field_reentry_delay));
                else
                    phytoViewHolder.phytoDelay.setText(R.string.unspecified);
                phytoViewHolder.phyto = phyto;
                break;

            case FERTI:
                FertiViewHolder fertiViewHolder = (FertiViewHolder) holder;
                Fertilizer fertilizer = (Fertilizer) inputList.get(position);
                fertiViewHolder.fertiName.setText(fertilizer.label_fra);
                res = context.getResources().getIdentifier(fertilizer.nature, "string", context.getPackageName());
                fertiViewHolder.fertiType.setText(context.getString(res));
                fertiViewHolder.fertilizer = fertilizer;
                break;

            default:
                SeedViewHolder defaultViewHolder = (SeedViewHolder) holder;
                Seed defaultItem = (Seed) inputList.get(position);
                defaultViewHolder.seedSpecie.setText(defaultItem.specie);
                defaultViewHolder.seedVariety.setText(defaultItem.variety);
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {

        if (inputList.get(position) instanceof Seed) {
            return SEED;
        }
        else if (inputList.get(position) instanceof Phyto) {
            return PHYTO;
        }
        else if (inputList.get(position) instanceof Fertilizer) {
            return FERTI;
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return inputList.size();
    }
}