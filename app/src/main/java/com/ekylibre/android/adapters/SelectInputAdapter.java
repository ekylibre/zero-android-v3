package com.ekylibre.android.adapters;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.Group;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekylibre.android.InterventionActivity;
import com.ekylibre.android.SelectInputFragment;
import com.ekylibre.android.R;
import com.ekylibre.android.database.AppDatabase;
import com.ekylibre.android.database.models.Fertilizer;
import com.ekylibre.android.database.models.Seed;
import com.ekylibre.android.database.models.Phyto;
import com.ekylibre.android.database.pojos.Fertilizers;
import com.ekylibre.android.database.pojos.Phytos;
import com.ekylibre.android.database.pojos.Seeds;
import com.ekylibre.android.database.relations.InterventionFertilizer;
import com.ekylibre.android.database.relations.InterventionPhytosanitary;
import com.ekylibre.android.database.relations.InterventionSeed;
import com.ekylibre.android.services.ServiceResultReceiver;
import com.ekylibre.android.services.SyncService;
import com.ekylibre.android.utils.Enums;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

import static com.ekylibre.android.utils.PhytosanitaryMiscibility.mixIsAuthorized;


public class SelectInputAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ServiceResultReceiver.Receiver {

    private static final String TAG = "SelectInputAdapter";
    public static final int SEED = 0, PHYTO = 1, FERTI = 2;

    private Context context;
    private List<Object> inputList;
    private List<Integer> phytoList;
    private SelectInputFragment.OnFragmentInteractionListener fragmentListener;

    public SelectInputAdapter(List<Object> inputList, Context context, SelectInputFragment.OnFragmentInteractionListener fragmentListener) {
        this.inputList = inputList;
        this.context = context;
        this.fragmentListener = fragmentListener;
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == SyncService.DONE) {

            int type = resultData.getInt("type", 0);
            int local_id = resultData.getInt("local_id", 0);
            int remote_id = resultData.getInt("remote_id", 0);

            new SetInputId(context, type, local_id, remote_id).execute();

            for (Object phyto : inputList) {
                if (phyto instanceof Phyto)
                    if (((Phyto) phyto).id == local_id) {
                        ((Phyto) phyto).eky_id = remote_id;
                        break;
                    }
            }
        }
    }

    class SetInputId extends AsyncTask<Void, Void, Void> {

        Context context;
        int ekyId;
        int id;
        int type;

        SetInputId(Context context, int type, int id, int ekyId) {
            this.context = context;
            this.type = type;
            this.id = id;
            this.ekyId = ekyId;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            AppDatabase database = AppDatabase.getInstance(context);

            switch (type) {
                case PHYTO:
                    database.dao().setPhytoEkyId(ekyId, id);
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            notifyDataSetChanged();
        }
    }

    class SeedViewHolder extends RecyclerView.ViewHolder {

        private TextView seedSpecie, seedVariety;
        private ImageView seedFavorite;
        private Seed seed;

        SeedViewHolder(View itemView) {
            super(itemView);
            seedSpecie = itemView.findViewById(R.id.seed_specie);
            seedVariety = itemView.findViewById(R.id.seed_variety);
            seedFavorite = itemView.findViewById(R.id.icon_favorite);
            itemView.setOnClickListener(v -> {
                Seeds seedInput = new Seeds();
                seedInput.seed = Collections.singletonList(seed);
                seedInput.inter = new InterventionSeed(seed.id);
                fragmentListener.onFragmentInteraction(seedInput);
            });
        }
    }

    class PhytoViewHolder extends RecyclerView.ViewHolder {

        private AppCompatTextView phytoName, phythoCompany, phytoAmm, phytoDelay;
        private ImageView phytoFavorite;
        private Phyto phyto;
        private Group phytoMixWarning;

        PhytoViewHolder(View itemView) {
            super(itemView);
            phytoName = itemView.findViewById(R.id.phyto_name);
            phythoCompany = itemView.findViewById(R.id.phyto_company);
            phytoAmm = itemView.findViewById(R.id.phyto_amm);
            phytoDelay = itemView.findViewById(R.id.phyto_delay);
            phytoMixWarning = itemView.findViewById(R.id.item_mix_warning);
            phytoFavorite = itemView.findViewById(R.id.icon_favorite);
            itemView.setOnClickListener(v -> {
                // Get Max dose before sending phyto to adapter
                //phyto.dose_max = AppDatabase.getInstance(context).dao().getMaxDose(phyto.id);
                Phytos phytoInput = new Phytos();
                phytoInput.phyto = Collections.singletonList(phyto);
                phytoInput.inter = new InterventionPhytosanitary(phyto.id);
                fragmentListener.onFragmentInteraction(phytoInput);
            });
        }
    }

    class FertiViewHolder extends RecyclerView.ViewHolder {

        private TextView fertiName, fertiType;
        private ImageView fertiFavorite;
        private Fertilizer fertilizer;

        FertiViewHolder(View itemView) {
            super(itemView);
            fertiName = itemView.findViewById(R.id.fertilizer_name);
            fertiType = itemView.findViewById(R.id.fertilizer_type);
            fertiFavorite = itemView.findViewById(R.id.icon_favorite);
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
                Timber.e("default viewHolder --> pas normal");
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
                String specie = seed.variety;
                if (seed.specie != null) {
                    if (Enums.SPECIE_TYPES.contains(seed.specie.toUpperCase())) {
                        int res = context.getResources().getIdentifier(seed.specie.toUpperCase(), "string", context.getPackageName());
                        specie = context.getString(res);
                    }
                }
                seedViewHolder.seedSpecie.setText(specie);
                seedViewHolder.seedVariety.setText(seed.variety);
                seedViewHolder.seedFavorite.setVisibility((seed.eky_id != null) ? View.VISIBLE : View.GONE);
                seedViewHolder.seed = seed;
                break;

            case PHYTO:
                PhytoViewHolder phytoViewHolder = (PhytoViewHolder) holder;
                Phyto phyto = (Phyto) inputList.get(position);


//                phytoList = new ArrayList<>();
//                for (Object item : InterventionActivity.inputList) {
//                    if (item instanceof Phytos)
//                        phytoList.add((((Phytos) item).phyto).get(0).id);
//                }
//
//                boolean display = false;
//                if (phytoList.isEmpty())
//                    display = true;
//                else if (!phytoList.contains(phyto.id))
//                    display = true;

                phytoViewHolder.phytoName.setText(phyto.name);
                phytoViewHolder.phythoCompany.setText(phyto.firm_name);
                phytoViewHolder.phytoAmm.setText((phyto.maaid != null) ? phyto.maaid : context.getString(R.string.unspecified));
                if (phyto.in_field_reentry_delay != null)
                    phytoViewHolder.phytoDelay.setText(context.getResources().getQuantityString(R.plurals.x_hours, phyto.in_field_reentry_delay, phyto.in_field_reentry_delay));
                else
                    phytoViewHolder.phytoDelay.setText(R.string.unspecified);

                phytoViewHolder.phytoFavorite.setVisibility((phyto.eky_id != null) ? View.VISIBLE : View.GONE);
                phytoViewHolder.phytoMixWarning.setVisibility(View.GONE);

                if (phyto.mix_category_code != null) {
                    Timber.e("Current item mix_category_code %s", phyto.mix_category_code);

                    List<Integer> codes = new ArrayList<>();
                    for (Object input : InterventionActivity.inputList) {
                        if (input instanceof Phytos) {
                            Phyto currentPhyto = ((Phytos) input).phyto.get(0);
                            if (currentPhyto != null)
                                if (currentPhyto.mix_category_code != null)
                                    codes.add(currentPhyto.mix_category_code);
                        }
                    }
                    codes.add(phyto.mix_category_code);

                    Timber.e(codes.toString());

                    if (codes.size() >= 2) {
                        if (!mixIsAuthorized(codes))
                            phytoViewHolder.phytoMixWarning.setVisibility(View.VISIBLE);
                    }
                }
                phytoViewHolder.phyto = phyto;
                break;

            case FERTI:
                FertiViewHolder fertiViewHolder = (FertiViewHolder) holder;
                Fertilizer fertilizer = (Fertilizer) inputList.get(position);
                fertiViewHolder.fertiName.setText(fertilizer.label_fra);
                String type = "";
                if (fertilizer.nature != null) {
                    int res = context.getResources().getIdentifier(fertilizer.nature, "string", context.getPackageName());
                    type = context.getString(res);
                }
                fertiViewHolder.fertiType.setText(type);
                fertiViewHolder.fertiFavorite.setVisibility((fertilizer.eky_id != null) ? View.VISIBLE : View.GONE);
                fertiViewHolder.fertilizer = fertilizer;
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {

        if (inputList.get(position) instanceof Seed) {
            return SEED;
        } else if (inputList.get(position) instanceof Phyto) {
            return PHYTO;
        } else if (inputList.get(position) instanceof Fertilizer) {
            return FERTI;
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return inputList.size();
    }

}