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
import com.ekylibre.android.utils.App;
import com.ekylibre.android.utils.SpinnerLists;
import com.ekylibre.android.utils.Unit;
import com.ekylibre.android.utils.Units;

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
    public static boolean NO_CROP = false;

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
    public static List<Interventions> interventionsList = new ArrayList<>();

    private SharedPreferences sharedPreferences;
    private SyncResultReceiver resultReceiver;

    public static String FARM_ID;
    public static Date lastSyncTime;
    public static final SimpleDateFormat LAST_SYNC = new SimpleDateFormat( "yyyy-MM-dd HH:mm");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Generates localized string lists for spinners
        generateUnitLists();
        SpinnerLists.generate(this);

        // Get shared preferences and set title
        sharedPreferences = this.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        setTitle(sharedPreferences.getString("current-farm-name", "No name"));

        // Get current farm_id
        FARM_ID = sharedPreferences.getString("current-farm-id", "");

        // Get locale one time for the app
        LOCALE = getResources().getConfiguration().locale;

        resultReceiver = new SyncResultReceiver(new Handler());
        resultReceiver.setReceiver(this);

        // Get layout refs
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
        careButton.setOnClickListener(view -> onProcedureChoice(App.CARE));
        cropProtectionButton.setOnClickListener(view -> onProcedureChoice(App.CROP_PROTECTION));
        fertilizationButton.setOnClickListener(view -> onProcedureChoice(App.FERTILIZATION));
        groundWorkButton.setOnClickListener(view -> onProcedureChoice(App.GROUND_WORK));
        harvestButton.setOnClickListener(view -> onProcedureChoice(App.HARVEST));
        implantationButton.setOnClickListener(view -> onProcedureChoice(App.IMPLANTATION));
        irrigationButton.setOnClickListener(view -> onProcedureChoice(App.IRRIGATION));
        finishingButton.setOnClickListener(view -> onInterventionTypeSelected(FINISHING));
        startingButton.setOnClickListener(view -> {
            Toast toast = Toast.makeText(this, "Fonctionnalité bientôt disponible !", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM, 0, 200);
            toast.show();
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            Intent intent = new Intent(this, SyncService.class);
            intent.setAction(SyncService.ACTION_SYNC_PULL);
            intent.putExtra("receiver", resultReceiver);
            startService(intent);
        });

        Intent intent = new Intent(this, SyncService.class);
        intent.setAction(SyncService.ACTION_SYNC_PULL);
        intent.putExtra("receiver", resultReceiver);
        startService(intent);

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

        try {
            lastSyncTime = LAST_SYNC.parse(sharedPreferences.getString("last-sync-time", "2018-01-01 12:00"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Set Farm name as page title
        setTitle(sharedPreferences.getString("current-farm-name", "Synchronisation..."));
        FARM_ID = sharedPreferences.getString("current-farm-id", "");

        // Get list filter and update list
        String filter = sharedPreferences.getString("filter", FILTER_ALL_INTERVENTIONS);
        new UpdateList(this, filter).execute();

    }

    @Override
    public void onBackPressed() {
        if (!deployMenu(false))
            super.onBackPressed();
            AppDatabase.revokeInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add the menubar (top right) with disconnect option
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
                editor.remove("is_authenticated");
                editor.remove("access_token");
                editor.remove("refresh_token");
                editor.remove("current-farm-name");
                editor.remove("current-farm-id");
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
            if (BuildConfig.DEBUG) Log.e(TAG, "Synchronization done");
            lastSyncTime = new Date();
            sharedPreferences.edit().putString("last-sync-time", LAST_SYNC.format(lastSyncTime)).apply();
            new UpdateList(this, FILTER_ALL_INTERVENTIONS).execute();
        } else if (resultCode == SyncService.FAILED) {
            Toast toast = Toast.makeText(this, R.string.sync_failure, Toast.LENGTH_LONG);
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
            if (BuildConfig.DEBUG) Log.e(TAG, "Updating interventions recyclerView...");
            AppDatabase database = AppDatabase.getInstance(context);
            interventionsList.clear();
            switch (filter) {

                case FILTER_ALL_INTERVENTIONS:
                    interventionsList.addAll(database.dao().selectInterventions(FARM_ID));
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
        if (sharedPreferences.getBoolean("no-crop", true)) {
            Toast toast = Toast.makeText(this, "Vous n'avez aucune parcelle assolée. Rendez-vous en ligne pour commencer !", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM, 0, 200);
            toast.show();
        } else {
            TYPE = type;
            if (type == STARTING)
                menuTitle.setText(R.string.starting_intervention_text);
            else if (type == FINISHING)
                menuTitle.setText(R.string.finishing_intervention_text);
            deployMenu(true);
        }
    }

    private void onProcedureChoice(String procedure) {
        Intent intent = new Intent(this, InterventionActivity.class);
        intent.putExtra("nature", TYPE);
        intent.putExtra("procedure", procedure);
        intent.putExtra("edition", false);
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

    /**
     * Creates units lists based on current locale
     */
    private void generateUnitLists() {
        setUnitsName(Units.IRRIGATION_UNITS, Units.IRRIGATION_UNITS_L10N);
        setUnitsName(Units.OUTPUT_UNITS, Units.OUTPUT_UNITS_L10N);
        setUnitsName(Units.VOLUME_UNITS, Units.VOLUME_UNITS_L10N);
        setUnitsName(Units.MASS_UNITS, Units.MASS_UNITS_L10N);
    }

    private void setUnitsName(List<Unit> unitList, List<String> unitListString) {
        for (Unit unit : unitList) {
            String name = getString(getResources().getIdentifier(unit.key, "string", getPackageName()));
            String quantity_name_only = (unit.surface_factor != 0) ? getString(getResources().getIdentifier(unit.quantity_key_only, "string", getPackageName())) : null;
            unitListString.add(name);
            unit.setName(name, quantity_name_only);
        }
    }
}
