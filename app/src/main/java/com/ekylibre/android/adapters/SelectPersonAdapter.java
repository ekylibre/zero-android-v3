package com.ekylibre.android.adapters;

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

    private static final String TAG = SelectPersonAdapter.class.getName();

    private List<Person> dataset;
    private SelectPersonFragment.OnFragmentInteractionListener fragmentListener;

    public SelectPersonAdapter(List<Person> dataset, SelectPersonFragment.OnFragmentInteractionListener fragmentListener) {
        this.dataset = dataset;
        this.fragmentListener = fragmentListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView firstNameTextView, lastNameTextView, descTextView;
        Person person;

        ViewHolder(View itemView) {
            super(itemView);

            firstNameTextView = itemView.findViewById(R.id.person_firstname);
            lastNameTextView = itemView.findViewById(R.id.person_lastname);
            //descTextView = itemView.findViewById(R.id.person_description);

            itemView.setOnClickListener(v -> {
                Persons selection = new Persons();
                selection.person = Collections.singletonList(person);
                selection.inter = new InterventionPerson(person.id);
                fragmentListener.onFragmentInteraction(selection);
            });
        }

        void display(Person item) {
            person = item;
            firstNameTextView.setText(item.first_name);
            lastNameTextView.setText(item.last_name);
            //descTextView.setText(item.role);
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