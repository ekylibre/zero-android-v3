package com.ekylibre.android;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ekylibre.android.adapters.MainAdapter;
import com.ekylibre.android.database.AppDatabase;
import com.ekylibre.android.database.pojos.Interventions;
import com.ekylibre.android.services.SyncResultReceiver;
import com.ekylibre.android.services.SyncService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements SyncResultReceiver.Receiver {

    private static final String TAG = "MainActivity";

    public static Locale LOCALE;
    public int TYPE;
    public static final int STARTING = 0;
    public static final int FINISHING = 1;

    // Procedures statics
    public static final String CARE = "CARE";
    public static final String CROP_PROTECTION = "CROP_PROTECTION";
    public static final String FERTILIZATION = "FERTILIZATION";
    public static final String GROUND_WORK = "GROUND_WORK";
    public static final String HARVEST = "HARVEST";
    public static final String IMPLANTATION = "IMPLANTATION";
    public static final String IRRIGATION = "IRRIGATION";

    // Filters statics
    public static final String FILTER_MY_INTERVENTIONS = "filter_my_interventions";
    public static final String FILTER_ALL_INTERVENTIONS = "filter_all_interventions";

    // UI components
    private View darkMask;
    private ConstraintLayout procedureChoiceLayout;
    private TextView menuTitle;
    private Button startingButton;
    private Button finishingButton;
    private TextView filterAll;
    private TextView filterMine;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private TextView emptyRecyclerView;
    private RecyclerView.Adapter adapter;
    public List<Interventions> interventionsList = new ArrayList<>();

    // Activity variables
    private AppDatabase database;
    private SharedPreferences sharedPreferences;
    SyncResultReceiver resultReceiver;

    // Farm id
    public static String currentFarmId;
    public static Date lastSyncTime;
    public static final SimpleDateFormat LAST_SYNC = new SimpleDateFormat( "yyyy-MM-dd HH:mm");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set locale one time for the app
        LOCALE = getResources().getConfiguration().locale;

        // Get shared preferences and set title
        sharedPreferences = this.getSharedPreferences("prefs", Context.MODE_PRIVATE);

        setTitle(sharedPreferences.getString("current-farm-name", "No name"));
        currentFarmId = sharedPreferences.getString("current-farm-id", "");

        if (!sharedPreferences.getBoolean("initial_data_loaded", false)) {
            new LoadInitialData(this).execute();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("initial_data_loaded", true);
            editor.apply();
        }

        resultReceiver = new SyncResultReceiver(new Handler());
        resultReceiver.setReceiver(this);

        // Layout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        darkMask = findViewById(R.id.dark_mask);
        procedureChoiceLayout = findViewById(R.id.nav_procedure_choice);
        menuTitle = findViewById(R.id.nav_message);
        startingButton = findViewById(R.id.button_starting);
        finishingButton = findViewById(R.id.button_finishing);
        ImageButton careButton = findViewById(R.id.button_care);
        ImageButton cropProtectionButton = findViewById(R.id.button_crop_protection);
        ImageButton fertilizationButton = findViewById(R.id.button_fertilization);
        ImageButton groundWorkButton = findViewById(R.id.button_ground_work);
        ImageButton harvestButton = findViewById(R.id.button_harvest);
        ImageButton implantationButton = findViewById(R.id.button_implantation);
        ImageButton irrigationButton = findViewById(R.id.button_irrigation);

//        filterAll = findViewById(R.id.filter_all_interventions);
//        filterMine = findViewById(R.id.filter_my_interventions);

        // The interventionEntity list
        emptyRecyclerView = findViewById(R.id.empty_recyclerview);
        recyclerView = findViewById(R.id.intervention_recycler);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapter = new MainAdapter(this, interventionsList);

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                if (adapter.getItemCount() == 0) {
                    recyclerView.setVisibility(View.GONE);
                    emptyRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    emptyRecyclerView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        });

        recyclerView.setAdapter(adapter);


        // All button events
        careButton.setOnClickListener(view -> onProcedureChoice(CARE));
        cropProtectionButton.setOnClickListener(view -> onProcedureChoice(CROP_PROTECTION));
        fertilizationButton.setOnClickListener(view -> onProcedureChoice(FERTILIZATION));
        groundWorkButton.setOnClickListener(view -> onProcedureChoice(GROUND_WORK));
        harvestButton.setOnClickListener(view -> onProcedureChoice(HARVEST));
        implantationButton.setOnClickListener(view -> onProcedureChoice(IMPLANTATION));
        irrigationButton.setOnClickListener(view -> onProcedureChoice(IRRIGATION));
        finishingButton.setOnClickListener(view -> onInterventionTypeSelected(FINISHING));
        startingButton.setOnClickListener(view -> {
            //onInterventionTypeSelected(STARTING);
            //showInputDialog();
            //new TestCrop(this).execute();
            Toast toast = Toast.makeText(this, "Fonctionnalité bientôt disponible", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM, 0, 200);
            toast.show();
        });


        swipeRefreshLayout.setOnRefreshListener(() -> {
            Intent intent = new Intent(this, SyncService.class);
            intent.setAction(SyncService.ACTION_SYNC_PULL);
            intent.putExtra("receiver", resultReceiver);
            startService(intent);
        });


        //        if (!sharedPreferences.getBoolean("showcase-passed", false)) {
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putBoolean("showcase-passed", true);
//            editor.apply();
//            new FancyShowCaseView.Builder(this)
//                    .focusOn(menuLayout).title("Pour démarrer, c'est ici !")
//                    .focusShape(FocusShape.ROUNDED_RECTANGLE).roundRectRadius(1)
//                    .disableFocusAnimation().build().show();}
    }

    @Override
    protected void onResume() {
        super.onResume();

        String date = sharedPreferences.getString("last-sync-time", "2018-01-01 12:00");
        try {
            lastSyncTime = LAST_SYNC.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Set Farm name as page title
        setTitle(sharedPreferences.getString("current-farm-name", "Synchronisation..."));

        // Get list filter
        String filter = sharedPreferences.getString("filter", FILTER_ALL_INTERVENTIONS);

        // Update main list
        new UpdateList(this, filter).execute();

    }

    @Override
    public void onBackPressed() {
        deployMenu(false);
        //super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sharedPreferences.edit().putString("last-sync-time", MainActivity.LAST_SYNC.format(lastSyncTime)).apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.appbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_logout:
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("is_authenticated", false);
                editor.remove("access_token");
                editor.apply();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {

        swipeRefreshLayout.setRefreshing(false);

        if (resultCode == SyncService.DONE) {
            Log.e(TAG, "Synchronization done");
            new UpdateList(this, FILTER_ALL_INTERVENTIONS).execute();
        } else if (resultCode == SyncService.FAILED) {
            Toast toast = Toast.makeText(this, "Echec de la synchronisation...", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM, 0, 200);
            toast.show();
        }
    }

    public class UpdateList extends AsyncTask<Void, Void, Void> {

        Context context;
        String filter;

        UpdateList(Context context, String filter) {
            this.context = context;
            this.filter = filter;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.e(TAG, "Updating interventions recyclerView...");
            database = AppDatabase.getInstance(context);
            interventionsList.clear();
            switch (filter) {

                case FILTER_ALL_INTERVENTIONS:
                    interventionsList.addAll(database.dao().selectInterventions(currentFarmId));
                    break;

                case FILTER_MY_INTERVENTIONS:
                    //interventionsList = database.interventionDAO().selectInterventions();
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter.notifyDataSetChanged();
        }
    }

    private void onInterventionTypeSelected(int type) {
        TYPE = type;
        if (type == STARTING)
            menuTitle.setText(R.string.starting_intervention_text);
        else if (type == FINISHING)
            menuTitle.setText(R.string.finishing_intervention_text);
        deployMenu(true);
    }

    private void onProcedureChoice(String procedure) {
        Intent intent = new Intent(this, InterventionActivity.class);
        intent.putExtra("type", TYPE);
        intent.putExtra("procedure", procedure);
        startActivity(intent);
        deployMenu(false);
    }

    private Boolean deployMenu(Boolean state) {
        if (!state && procedureChoiceLayout.getVisibility() == View.VISIBLE) {
            darkMask.setVisibility(View.GONE);
            procedureChoiceLayout.setVisibility(View.GONE);
            menuTitle.setText(R.string.register_an_intervention);
            startingButton.setVisibility(View.VISIBLE);
            finishingButton.setVisibility(View.VISIBLE);
            return true;
        } else if (state && procedureChoiceLayout.getVisibility() == View.GONE) {
            startingButton.setVisibility(View.GONE);
            finishingButton.setVisibility(View.GONE);
            darkMask.setVisibility(View.VISIBLE);
            darkMask.bringToFront();
            procedureChoiceLayout.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }

    public class LoadInitialData extends AsyncTask<Void, Void, Void> {
        Context context;

        LoadInitialData(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            database = AppDatabase.getInstance(context);
            database.populateInitialData(context);
            return null;
        }
    }

}
