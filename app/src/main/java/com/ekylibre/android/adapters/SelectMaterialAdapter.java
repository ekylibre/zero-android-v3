package com.ekylibre.android.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ekylibre.android.R;
import com.ekylibre.android.SelectMaterialFragment;
import com.ekylibre.android.database.models.Material;
import com.ekylibre.android.database.pojos.Materials;
import com.ekylibre.android.database.relations.InterventionMaterial;

import java.util.Collections;
import java.util.List;


public class SelectMaterialAdapter extends RecyclerView.Adapter<SelectMaterialAdapter.ViewHolder> {

    private static final String TAG = SelectMaterialAdapter.class.getName();

    private static final int EMPTY_VIEW_TYPE = 0;
    private static final int REGULAR_VIEW_TYPE = 1;

    private List<Material> dataset;
    private SelectMaterialFragment.OnFragmentInteractionListener fragmentListener;

    public SelectMaterialAdapter(List<Material> dataset, SelectMaterialFragment.OnFragmentInteractionListener fragmentListener) {
        this.dataset = dataset;
        this.fragmentListener = fragmentListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView, descTextView;
        Material material;

        ViewHolder(View itemView) {
            super(itemView);

            nameTextView = itemView.findViewById(R.id.material_name);
            descTextView = itemView.findViewById(R.id.material_desc);

            itemView.setOnClickListener(v -> {
                Materials selection = new Materials();
                selection.material = Collections.singletonList(material);
                selection.inter = new InterventionMaterial(material.id);
                fragmentListener.onFragmentInteraction(selection);
            });
        }

        void display(Material item) {
            material = item;
            nameTextView.setText(item.name);
            descTextView.setText(item.description);
        }
    }

    @NonNull
    @Override
    public SelectMaterialAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        int layoutId = R.layout.item_material;

//        if (viewType == EMPTY_VIEW_TYPE)
//            layoutId = R.layout.item_material_empty;

        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);

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

//    @Override
//    public int getItemViewType(int position) {
//        if (dataset.isEmpty())
//            return EMPTY_VIEW_TYPE;
//        else
//            return REGULAR_VIEW_TYPE;
//    }

}