package com.ekylibre.android.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ekylibre.android.R;
import com.ekylibre.android.SelectPersonFragment;
import com.ekylibre.android.database.models.Person;
import com.ekylibre.android.database.pojos.Persons;
import com.ekylibre.android.database.relations.InterventionPerson;

import java.util.Collections;
import java.util.List;


public class SelectPersonAdapter extends RecyclerView.Adapter<SelectPersonAdapter.ViewHolder> {

    private Context context;
    private List<Person> dataset;
    private List<Integer> selectedPeople;
    private SelectPersonFragment.OnFragmentInteractionListener fragmentListener;

    public SelectPersonAdapter(Context context, List<Person> dataset, List<Integer> selectedPeople,
                               SelectPersonFragment.OnFragmentInteractionListener fragmentListener) {
        this.context = context;
        this.dataset = dataset;
        this.fragmentListener = fragmentListener;
        this.selectedPeople = selectedPeople;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView firstNameTextView, lastNameTextView, descTextView;
        Person person;
        View.OnClickListener onClick;

        ViewHolder(View itemView) {
            super(itemView);

            firstNameTextView = itemView.findViewById(R.id.person_firstname);
            lastNameTextView = itemView.findViewById(R.id.person_lastname);
            //descTextView = itemView.findViewById(R.id.person_description);

            onClick = v -> {
                Persons selection = new Persons();
                selection.person = Collections.singletonList(person);
                selection.inter = new InterventionPerson(person.id);
                fragmentListener.onFragmentInteraction(selection);
            };
        }

        void display(Person item) {
            person = item;
            firstNameTextView.setText(item.first_name);
            lastNameTextView.setText(item.last_name);
            //descTextView.setText(item.role);

            int colorId;
            if (selectedPeople.contains(item.id)) {
                itemView.setOnClickListener(null);
                colorId = context.getResources().getColor(R.color.light_grey);
                itemView.setBackgroundColor(colorId);
            } else {
                itemView.setOnClickListener(onClick);
                //colorId = context.getResources().getColor(R.color.white);
            }
        }
    }

    @NonNull
    @Override
    public SelectPersonAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_person, parent, false);
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