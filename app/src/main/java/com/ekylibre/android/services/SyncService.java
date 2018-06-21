package com.ekylibre.android.services;


import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import com.ekylibre.android.BuildConfig;
import com.ekylibre.android.DeleteInterMutation;
import com.ekylibre.android.FarmQuery;
import com.ekylibre.android.InterventionActivity;
import com.ekylibre.android.InterventionQuery;
import com.ekylibre.android.MainActivity;
import com.ekylibre.android.PushEquipmentMutation;
import com.ekylibre.android.PushInterMutation;
import com.ekylibre.android.PushPersonMutation;
import com.ekylibre.android.UpdateInterMutation;
import com.ekylibre.android.database.AppDatabase;
import com.ekylibre.android.database.models.Crop;
import com.ekylibre.android.database.models.Equipment;
import com.ekylibre.android.database.models.Farm;
import com.ekylibre.android.database.models.Fertilizer;
import com.ekylibre.android.database.models.Harvest;
import com.ekylibre.android.database.models.Intervention;
import com.ekylibre.android.database.models.Person;
import com.ekylibre.android.database.models.Phyto;
import com.ekylibre.android.database.models.Plot;
import com.ekylibre.android.database.models.Seed;
import com.ekylibre.android.database.models.Storage;
import com.ekylibre.android.database.models.Weather;
import com.ekylibre.android.database.pojos.Crops;
import com.ekylibre.android.database.pojos.Equipments;
import com.ekylibre.android.database.pojos.Fertilizers;
import com.ekylibre.android.database.pojos.Interventions;
import com.ekylibre.android.database.pojos.Persons;
import com.ekylibre.android.database.pojos.Phytos;
import com.ekylibre.android.database.pojos.Seeds;
import com.ekylibre.android.database.relations.InterventionCrop;

import com.ekylibre.android.database.relations.InterventionEquipment;
import com.ekylibre.android.database.relations.InterventionFertilizer;
import com.ekylibre.android.database.relations.InterventionPerson;
import com.ekylibre.android.database.relations.InterventionPhytosanitary;
import com.ekylibre.android.database.relations.InterventionSeed;
import com.ekylibre.android.database.relations.InterventionWorkingDay;
import com.ekylibre.android.network.GraphQLClient;

import com.ekylibre.android.type.ArticleAllUnitEnum;
import com.ekylibre.android.type.ArticleAttributes;
import com.ekylibre.android.type.ArticleTypeEnum;
import com.ekylibre.android.type.ArticleVolumeUnitEnum;
import com.ekylibre.android.type.HarvestLoadAttributes;
import com.ekylibre.android.type.HarvestLoadUnitEnum;
import com.ekylibre.android.type.InterventionInputAttributes;
import com.ekylibre.android.type.InterventionOperatorAttributes;
import com.ekylibre.android.type.InterventionOutputAttributes;
import com.ekylibre.android.type.InterventionOutputTypeEnum;
import com.ekylibre.android.type.InterventionTargetAttributes;
import com.ekylibre.android.type.InterventionToolAttributes;
import com.ekylibre.android.type.InterventionWorkingDayAttributes;
import com.ekylibre.android.type.EquipmentTypeEnum;
import com.ekylibre.android.type.InterventionTypeEnum;
import com.ekylibre.android.type.OperatorRoleEnum;
import com.ekylibre.android.type.WeatherAttributes;
import com.ekylibre.android.type.WeatherEnum;
import com.ekylibre.android.utils.App;
import com.ekylibre.android.utils.Enums;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;


public class SyncService extends IntentService {

    private static final String TAG = "SyncService";

    public static final int DONE = 10;
    public static final int FAILED = 11;

    public static final String ACTION_SYNC_ALL = "com.ekylibre.android.services.action.SYNC_PULL";
    public static final String FIRST_TIME_SYNC = "com.ekylibre.android.services.action.FIRST_TIME_SYNC";
    public static final String ACTION_CREATE_ARTICLES = "com.ekylibre.android.services.action.CREATE_ARTICLES";

    private boolean ERROR = false;
    public static String ACCESS_TOKEN;
    private static SharedPreferences prefs;
    private AppDatabase database;
    private ApolloClient apolloClient;
    private ResultReceiver receiver;
    private String ACTION;


    public SyncService() {
        super("SyncService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (BuildConfig.DEBUG) Log.i(TAG, "Starting SyncService");

        // Handle Handshake Errors
        try {
            ProviderInstaller.installIfNeeded(this);
        } catch (GooglePlayServicesRepairableException e) {
            Log.e(TAG, "GooglePlayServicesRepairableException");
            GoogleApiAvailability.getInstance().showErrorNotification(this, e.getConnectionStatusCode());
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.e(TAG, "GooglePlayServicesNotAvailableException");
        }

        prefs = this.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        ACCESS_TOKEN = prefs.getString("access_token", null);

        if (App.API_URL == null) {
            App.API_URL = getString(getResources().getIdentifier("api_url", "string", getPackageName()));
            App.OAUTH_CLIENT_ID = getString(getResources().getIdentifier("client_id", "string", getPackageName()));
            App.OAUTH_CLIENT_SECRET = getString(getResources().getIdentifier("client_secret", "string", getPackageName()));
        }

        // Get ResultReceiver from intent
        receiver = intent.getParcelableExtra("receiver");
        database = AppDatabase.getInstance(this);
        apolloClient = GraphQLClient.getApolloClient(ACCESS_TOKEN);
        ACTION = Objects.requireNonNull(intent.getAction());

        // Route action to function
        switch (ACTION) {

            case ACTION_CREATE_ARTICLES:
                pushCreatePersonsAndEquipments();
                break;

            case FIRST_TIME_SYNC:
                getFarm();
                break;

            case ACTION_SYNC_ALL:

                // Mutations (order is important)
                pushCreatePersonsAndEquipments();
                pushDeleteIntervention();
                pushUpdateIntervention();
                pushCreateIntervention();

                // Queries
                //getFarm();   is call in code

                // Action done
                //receiver.send(DONE, new Bundle());  called in code
                break;
        }
    }


    /**
     * delete intervention mutation
     */
    private void pushDeleteIntervention() {

        List<Interventions> deletableIntervention = database.dao().getDeletableInterventions(MainActivity.FARM_ID);

        if (!deletableIntervention.isEmpty()) {
            for (Interventions deletableInter : deletableIntervention) {

                DeleteInterMutation deleteInter = DeleteInterMutation.builder()
                        .id(String.valueOf(deletableInter.intervention.eky_id)).build();

                apolloClient.mutate(deleteInter).enqueue(new ApolloCall.Callback<DeleteInterMutation.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<DeleteInterMutation.Data> response) {

                        boolean alreadyDeleted = false;
                        if (response.hasErrors()) {
                            for (Error error : response.errors()) {
                                String message = error.message();
                                if (message != null && message.contains("does not exist"))
                                    alreadyDeleted = true;
                            }
                        }

                        if (!response.hasErrors() || alreadyDeleted) {
                            Log.i(TAG, String.format("Intervention #%s deleted", deletableInter.intervention.id));
                            database.dao().delete(deletableInter.intervention);
                        }
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                });
            }
        } else if (BuildConfig.DEBUG) Log.d(TAG, "No intervention to delete");
    }


    /**
     * update intervention mutation
     */
    private void pushUpdateIntervention() {

        List<Interventions> updatableInterventions = database.dao().getUpdatableInterventions(MainActivity.FARM_ID);

        if (!updatableInterventions.isEmpty()) {

            List<InterventionTargetAttributes> targetUpdates;
            List<InterventionWorkingDayAttributes> workingDayUpdate;
            List<InterventionInputAttributes> inputUpdates;
            List<InterventionOperatorAttributes> operatorUpdates;
            List<InterventionToolAttributes> toolUpdates;
            List<HarvestLoadAttributes> loadUpdates;
            List<InterventionOutputAttributes> outputUpdate;
            WeatherAttributes weatherUpdate;

            for (Interventions updatableInter : updatableInterventions) {

                if (BuildConfig.DEBUG)
                    Log.i(TAG, "Updating remote intervention #" + updatableInter.intervention.eky_id);

                targetUpdates = new ArrayList<>();
                workingDayUpdate = new ArrayList<>();
                inputUpdates = new ArrayList<>();
                loadUpdates = new ArrayList<>();
                outputUpdate = new ArrayList<>();
                operatorUpdates = new ArrayList<>();
                toolUpdates = new ArrayList<>();
                weatherUpdate = null;

                for (Crops crop : updatableInter.crops)
                    targetUpdates.add(InterventionTargetAttributes.builder()
                            .cropID(crop.inter.crop_id)
                            .workAreaPercentage(crop.inter.work_area_percentage).build());

                for (InterventionWorkingDay wd : updatableInter.workingDays)
                    workingDayUpdate.add(InterventionWorkingDayAttributes.builder()
                            .executionDate(wd.execution_date)
                            .hourDuration((long) wd.hour_duration).build());

                for (Persons person : updatableInter.persons)
                    operatorUpdates.add(InterventionOperatorAttributes.builder()
                            .personId(String.valueOf(person.person.get(0).eky_id))
                            .role((person.inter.is_driver) ? OperatorRoleEnum.DRIVER : OperatorRoleEnum.OPERATOR).build());

                for (Equipments equipment : updatableInter.equipments)
                    toolUpdates.add(InterventionToolAttributes.builder()
                            .equipmentId(String.valueOf(equipment.equipment.get(0).eky_id)).build());

                for (Phytos phyto : updatableInter.phytos) {
                    inputUpdates.add(InterventionInputAttributes.builder()
                            .article(ArticleAttributes.builder().id(String.valueOf(phyto.phyto.get(0).eky_id)).build())
                            .quantity(phyto.inter.quantity)
                            .unit(ArticleAllUnitEnum.safeValueOf(phyto.inter.unit)).build());
                }

                for (Seeds seed : updatableInter.seeds)
                    inputUpdates.add(InterventionInputAttributes.builder()
                            .article(ArticleAttributes.builder().id(String.valueOf(seed.seed.get(0).eky_id)).build())
                            .quantity(seed.inter.quantity)
                            .unit(ArticleAllUnitEnum.safeValueOf(seed.inter.unit)).build());

                for (Fertilizers fertilizer : updatableInter.fertilizers)
                    inputUpdates.add(InterventionInputAttributes.builder()
                            .article(ArticleAttributes.builder().id(String.valueOf(fertilizer.fertilizer.get(0).eky_id)).build())
                            .quantity(fertilizer.inter.quantity)
                            .unit(ArticleAllUnitEnum.safeValueOf(fertilizer.inter.unit)).build());

                for (Weather weather : updatableInter.weather)
                    weatherUpdate = WeatherAttributes.builder()
                            .description(weather.description != null ? WeatherEnum.valueOf(weather.description) : null)
                            .temperature(weather.temperature != null ? Double.valueOf(weather.temperature) : null)
                            .windSpeed(weather.wind_speed != null ? Double.valueOf(weather.wind_speed) : null).build();

                for (Harvest harvest : updatableInter.harvests) {
                    loadUpdates.add(HarvestLoadAttributes.builder()
                            .number(harvest.number)
                            .quantity(harvest.quantity)
                            .netQuantity(harvest.quantity)
                            .unit(HarvestLoadUnitEnum.valueOf(harvest.unit))
                            .storageID(String.valueOf(harvest.id_storage)).build());
                }
                if (!loadUpdates.isEmpty()) {
                    outputUpdate.add(InterventionOutputAttributes.builder()
                            .nature(InterventionOutputTypeEnum.safeValueOf(updatableInter.harvests.get(0).type))
                            .loads(loadUpdates).build());
                }

                // Whould be cleaner if the API were accepting null value in this cases
//                if (inputUpdates.isEmpty()) inputUpdates = null;
//                if (outputUpdate.isEmpty()) outputUpdate = null;
//                if (operatorUpdates.isEmpty()) operatorUpdates = null;
//                if (toolUpdates.isEmpty()) toolUpdates = null;

                UpdateInterMutation updateIntervention = UpdateInterMutation.builder()
                        .farmId(updatableInter.intervention.farm)
                        .interventionId(String.valueOf(updatableInter.intervention.eky_id))
                        .procedure(InterventionTypeEnum.safeValueOf(updatableInter.intervention.type))
                        .cropList(targetUpdates)
                        .workingDays(workingDayUpdate)
                        .inputs(inputUpdates)
                        .outputs(outputUpdate)
                        .tools(toolUpdates)
                        .operators(operatorUpdates)
                        .weather(weatherUpdate)
                        .waterQuantity((updatableInter.intervention.water_quantity != null) ? (long) updatableInter.intervention.water_quantity : null)
                        .waterUnit((updatableInter.intervention.water_unit != null) ? ArticleVolumeUnitEnum.safeValueOf(updatableInter.intervention.water_unit) : null)
                        .build();

                // Do the mutation and register callback
                apolloClient.mutate(updateIntervention).enqueue(new ApolloCall.Callback<UpdateInterMutation.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<UpdateInterMutation.Data> response) {
                        if (!response.hasErrors()) {
                            database.dao().setInterventionSynced(updatableInter.intervention.id);
                            if (BuildConfig.DEBUG)
                                Log.d(TAG, "\tIntervention #" + updatableInter.intervention.eky_id + " successfully updated !");
                        } else {
                            Log.e(TAG, "Error while updating intervention #" + updatableInter.intervention.id);
                        }
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        Log.e(TAG, "Error on update " + e.getLocalizedMessage());
                        //receiver.send(FAILED, new Bundle());
                    }
                });
                // Hack avoiding false positive error TODO: correct this
                database.dao().setInterventionSynced(updatableInter.intervention.id);
            }
        }
    }


    /**
     * create person and equipment mutation
     */
    private void pushCreatePersonsAndEquipments() {

        List<Equipment> newEquipments = database.dao().getEquipmentWithoutEkyId();
        List<Person> newPersons = database.dao().getPersonsWithoutEkyId();

        if (!newPersons.isEmpty()) {
            for (Person person : newPersons) {

                PushPersonMutation pushPerson = PushPersonMutation.builder()
                        .farmId(person.farm_id)
                        .firstName(person.first_name)
                        .lastName(person.last_name).build();

                apolloClient.mutate(pushPerson).enqueue(new ApolloCall.Callback<PushPersonMutation.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<PushPersonMutation.Data> response) {
                        if (!response.hasErrors()) {
                            PushPersonMutation.Data data = response.data();
                            if (data != null) {
                                PushPersonMutation.CreatePerson mutation = data.createPerson();
                                if (mutation != null) {
                                    PushPersonMutation.Person pers = mutation.person();
                                    if (pers != null && !pers.id().equals("")) {
                                        database.dao().setPersonEkyId(person.id, Integer.valueOf(pers.id()));
                                        if (BuildConfig.DEBUG) Log.i(TAG, "Person #" + pers.id() + " successfully created !");
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                        ERROR = true;
                    }
                });
            }
        } else if (BuildConfig.DEBUG) Log.i(TAG, "No new Person to push");

        if (!newEquipments.isEmpty()) {
            for (Equipment equipment : newEquipments) {

                PushEquipmentMutation pushEquipment = PushEquipmentMutation.builder()
                        .farmId(equipment.farmId)
                        .type(EquipmentTypeEnum.safeValueOf(equipment.type))
                        .name(equipment.name)
                        .number(equipment.number).build();

                apolloClient.mutate(pushEquipment).enqueue(new ApolloCall.Callback<PushEquipmentMutation.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<PushEquipmentMutation.Data> response) {
                        if (!response.hasErrors()) {
                            PushEquipmentMutation.CreateEquipment mutation = response.data().createEquipment();
                            if (!mutation.equipment().id().equals("")) {
                                database.dao().setEquipmentEkyId(equipment.id, mutation.equipment().id());
                                if (BuildConfig.DEBUG)
                                    Log.i(TAG, "Equipment #" + mutation.equipment().id() + " successfully created");
                                if (ACTION.equals(ACTION_CREATE_ARTICLES)) {
                                    Log.e(TAG, "Return Equipment id " + mutation.equipment().id());
                                    Bundle bundle = new Bundle();
                                    bundle.putString("name", equipment.name);
                                    bundle.putInt("remote_id", Integer.valueOf(mutation.equipment().id()));
                                    receiver.send(DONE, bundle);
                                }
                            }

                        }
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                        ERROR = true;
                    }
                });
            }
        } else {
            if (BuildConfig.DEBUG) Log.i(TAG, "No new Equipment to push");
        }

        if (ERROR) {
            MainActivity.ITEMS_TO_SYNC = true;
            receiver.send(FAILED, new Bundle());
        }

    }


    /**
     * create intervention mutation
     */
    private void pushCreateIntervention() {

        List<Interventions> interventions = database.dao().getSyncableInterventions(MainActivity.FARM_ID);

        if (interventions.size() > 0) {
            List<InterventionTargetAttributes> targets;
            List<InterventionWorkingDayAttributes> workingDays;
            List<InterventionInputAttributes> inputs;
            List<HarvestLoadAttributes> loads;
            List<InterventionOutputAttributes> outputs;
            List<InterventionOperatorAttributes> operators;
            List<InterventionToolAttributes> tools;
            WeatherAttributes weatherInput;

            for (Interventions createInter : interventions) {

                if (BuildConfig.DEBUG)
                    Log.i(TAG, "Create remote intervention");

                targets = new ArrayList<>();
                workingDays = new ArrayList<>();
                inputs = new ArrayList<>();
                tools = new ArrayList<>();
                outputs = new ArrayList<>();
                loads = new ArrayList<>();
                operators = new ArrayList<>();
                weatherInput = null;
                boolean globalOutputs = true;

                for (Crops crop : createInter.crops)
                    targets.add(InterventionTargetAttributes.builder()
                            .cropID(crop.inter.crop_id)
                            .workAreaPercentage(crop.inter.work_area_percentage).build());

                for (InterventionWorkingDay wd : createInter.workingDays)
                    workingDays.add(InterventionWorkingDayAttributes.builder()
                            .executionDate(wd.execution_date)
                            .hourDuration((long) wd.hour_duration).build());

                for (Persons person : createInter.persons)
                    operators.add(InterventionOperatorAttributes.builder()
                            .personId(String.valueOf(person.person.get(0).eky_id))
                            .role((person.inter.is_driver) ? OperatorRoleEnum.DRIVER : OperatorRoleEnum.OPERATOR).build());

                for (Equipments equipment : createInter.equipments)
                    tools.add(InterventionToolAttributes.builder()
                            .equipmentId(String.valueOf(equipment.equipment.get(0).eky_id)).build());

                for (Harvest harvest : createInter.harvests) {
                    HarvestLoadAttributes.Builder loadBuilder = HarvestLoadAttributes.builder()
                            .quantity(harvest.quantity)
                            .netQuantity(harvest.quantity)
                            .unit(HarvestLoadUnitEnum.valueOf(harvest.unit));
                    if (harvest.number != null) loadBuilder.number(harvest.number);
                    if (harvest.id_storage != null) loadBuilder.storageID(String.valueOf(harvest.id_storage));
                    loads.add(loadBuilder.build());
                }
                if (!loads.isEmpty()) {
                    globalOutputs = false;
                    outputs.add(InterventionOutputAttributes.builder()
                            .nature(InterventionOutputTypeEnum.safeValueOf(createInter.harvests.get(0).type))
                            .loads(loads).build());
                }

                for (Phytos phyto : createInter.phytos) {
                    ArticleAttributes.Builder articleBuilder = ArticleAttributes.builder().type(ArticleTypeEnum.CHEMICAL);

                    if (phyto.phyto.get(0).eky_id == null)  // Create new article
                        if (phyto.phyto.get(0).registered)
                            articleBuilder.referenceID(String.valueOf(phyto.phyto.get(0).id));

                        else  // Use existing article
                            articleBuilder.id(String.valueOf(phyto.phyto.get(0).eky_id));

                    inputs.add(InterventionInputAttributes.builder()
                            .article(articleBuilder.build())
                            .quantity(phyto.inter.quantity)
                            .unit(ArticleAllUnitEnum.safeValueOf(phyto.inter.unit)).build());
                }

                for (Seeds seed : createInter.seeds) {
                    ArticleAttributes.Builder articleBuilder = ArticleAttributes.builder().type(ArticleTypeEnum.SEED);

                    if (seed.seed.get(0).eky_id == null)  // Create new article
                        if (seed.seed.get(0).registered)
                            articleBuilder.referenceID(String.valueOf(seed.seed.get(0).id));

                        else  // Use existing article
                            articleBuilder.id(String.valueOf(seed.seed.get(0).eky_id));

                    inputs.add(InterventionInputAttributes.builder()
                            .article(articleBuilder.build())
                            .quantity(seed.inter.quantity)
                            .unit(ArticleAllUnitEnum.safeValueOf(seed.inter.unit)).build());
                }

                for (Fertilizers fertilizer : createInter.fertilizers) {
                    ArticleAttributes.Builder articleBuilder = ArticleAttributes.builder().type(ArticleTypeEnum.FERTILIZER);

                    if (fertilizer.fertilizer.get(0).eky_id == null)  // Create new article
                        if (fertilizer.fertilizer.get(0).registered)
                            articleBuilder.referenceID(String.valueOf(fertilizer.fertilizer.get(0).id));

                        else  // Use existing article
                            articleBuilder.id(String.valueOf(fertilizer.fertilizer.get(0).eky_id));

                    inputs.add(InterventionInputAttributes.builder()
                            .article(articleBuilder.build())
                            .quantity(fertilizer.inter.quantity)
                            .unit(ArticleAllUnitEnum.safeValueOf(fertilizer.inter.unit)).build());
                }

                for (Weather weather : createInter.weather)
                    weatherInput = WeatherAttributes.builder()
                            .description(weather.description != null ? WeatherEnum.valueOf(weather.description) : null)
                            .temperature(weather.temperature != null ? Double.valueOf(weather.temperature) : null)
                            .windSpeed(weather.wind_speed != null ? Double.valueOf(weather.wind_speed) : null).build();

                // Build the mutation
                PushInterMutation.Builder pushIntervention = PushInterMutation.builder()
                        .farmId(createInter.intervention.farm)
                        .procedure(InterventionTypeEnum.safeValueOf(createInter.intervention.type))
                        .cropList(targets)
                        .workingDays(workingDays)
                        .globalOutputs(globalOutputs);

                if (createInter.intervention.water_quantity != null)
                    pushIntervention.waterQuantity((long) createInter.intervention.water_quantity);
                if (createInter.intervention.water_unit != null)
                    pushIntervention.waterUnit(ArticleVolumeUnitEnum.safeValueOf(createInter.intervention.water_unit));
                if (weatherInput != null) pushIntervention.weather(weatherInput);

                if (!inputs.isEmpty()) pushIntervention.inputs(inputs);
                if (!outputs.isEmpty()) pushIntervention.outputs(outputs);
                if (!tools.isEmpty()) pushIntervention.tools(tools);
                if (!operators.isEmpty()) pushIntervention.operators(operators);

                apolloClient.mutate(pushIntervention.build()).enqueue(new ApolloCall.Callback<PushInterMutation.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<PushInterMutation.Data> response) {
                        if (!response.hasErrors()) {
                            PushInterMutation.CreateIntervention mutation = response.data().createIntervention();
                            if (!mutation.intervention().id().equals("")) {
                                if (BuildConfig.DEBUG) Log.i(TAG, "|--> eky_id #" + mutation.intervention().id() + " attributed");
                                database.dao().setInterventionEkyId(createInter.intervention.id, Integer.valueOf(mutation.intervention().id()));
                            } else {
                                Log.e(TAG, "Error while attributing id");
                            }
                            // Continue to global sync
                            getFarm();

                        } else {
                            Bundle bundle = new Bundle();
                            bundle.putString("message", response.errors().get(0).message());
                            receiver.send(FAILED, bundle);
                        }

                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                        receiver.send(FAILED, new Bundle());
                    }
                });
            }
        } else {
            // Continue to global sync
            getFarm();
        }
    }


    /**
     * get intervention query
     */
    private void getFarm() {

        List<Integer> personEkyIdList = database.dao().personEkiIdList();

        ApolloCall.Callback<FarmQuery.Data> farmCallback = new ApolloCall.Callback<FarmQuery.Data>() {

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, e.getMessage(), e);
                Bundle bundle = new Bundle();
                bundle.putString("message", e.getLocalizedMessage());
                receiver.send(FAILED, bundle);
            }

            @Override
            public void onResponse(@Nonnull Response<FarmQuery.Data> response) {

                FarmQuery.Data data = response.data();

                if (data != null) {

                    // TODO: improve following

                    // Saving first farm (only one for now)
                    FarmQuery.Farm farm = data.farms().get(0);
                    Farm newFarm = new Farm(farm.id(), farm.label());
                    database.dao().insert(newFarm);

                    // Saving current farm in SharedPreferences
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("current-farm-id", farm.id());
                    editor.putString("current-farm-name", farm.label());
                    editor.apply();


                    // Processing plots
                    List<FarmQuery.Plot> plots = farm.plots();
                    if (plots != null) {
                        if (BuildConfig.DEBUG) Log.i(TAG, "Fetching plots...");
                        for (FarmQuery.Plot plot : plots) {
                            Plot newPlot = new Plot(plot.uuid(), plot.name(), null,
                                    Float.valueOf(plot.surfaceArea().split(" ")[0]), null, null, null, farm.id());
                            database.dao().insert(newPlot);
                        }
                    }

                    // Processing crops and associated plots
                    List<FarmQuery.Crop> crops = farm.crops();
                    if (crops != null) {
                        if (BuildConfig.DEBUG) Log.i(TAG, "Fetching crops...");

                        editor.putBoolean("no-crop", false);
                        editor.apply();

                        Calendar cal = Calendar.getInstance();

                        for (FarmQuery.Crop crop : crops) {

                            cal.setTime(crop.stopDate());
                            String name = crop.productionNature().name() + " " + cal.get(Calendar.YEAR);

                            // Saving crop
                            Crop newCrop = new Crop(
                                    crop.uuid(), name, crop.specie().rawValue(), crop.productionNature().specie().rawValue(),
                                    crop.productionMode(), null, null, null,
                                    Float.valueOf(crop.surfaceArea().split(" ")[0]), null,
                                    crop.startDate(), crop.stopDate(), crop.plot().uuid(),
                                    farm.id());
                            database.dao().insert(newCrop);
                        }
                        // TODO: delete crop & plot if deleted on server
                    } else {
                        editor.putBoolean("no-crop", true);
                        editor.apply();
                    }

                    // Processing people
                    List<FarmQuery.person> people = farm.people();
                    if (people != null) {
                        if (BuildConfig.DEBUG) Log.i(TAG, "Fetching people...");

                        for (FarmQuery.person person : people) {

                            String firstName = (person.firstName() != null) ? person.firstName() : "";

                            // Save or update Person
                            if (personEkyIdList.contains(Integer.valueOf(person.id()))) {
                                database.dao().updatePerson(firstName, person.lastName(), person.id());
                            } else {
                                if (BuildConfig.DEBUG)
                                    Log.d(TAG, "\tCreate person #" + person.id());
                                database.dao().insert(new Person(Integer.valueOf(person.id()), firstName, person.lastName(), farm.id()));
                            }
                        }
                        // TODO: delete person if deleted on server --> or mark status deleted ?
                    }

                    // Processing equipments
                    List<FarmQuery.Equipment> equipments = farm.equipments();
                    if (equipments != null) {
                        if (BuildConfig.DEBUG) Log.i(TAG, "Fetching equipments...");
                        for (FarmQuery.Equipment equipment : equipments) {

                            int result = database.dao().setEquipmentEkyId(Integer.valueOf(equipment.id()), equipment.name());

                            if (result != 1) {
                                if (BuildConfig.DEBUG)
                                    Log.i(TAG, "\tCreate equipment #" + equipment.id());
                                database.dao().insert(new Equipment(Integer.valueOf(equipment.id()),
                                        equipment.name(), equipment.type() != null ? equipment.type().rawValue() : null, equipment.number(), farm.id()));
                            }
                        }
                    }

                    // Processing storages
                    List<FarmQuery.Storage> storages = farm.storages();
                    if (storages != null) {
                        Log.i(TAG, "Fetching storages...");

                        for (FarmQuery.Storage storage : storages) {
                            if (BuildConfig.DEBUG)
                                Log.d(TAG, "\tCreate/update storage #" + storage.id());
                            database.dao().insert(new Storage(
                                    Integer.valueOf(storage.id()),
                                    storage.name(),
                                    storage.type().rawValue()));
                        }
                        Enums.STORAGE_LIST = database.dao().getStorages();
                        Enums.generateStorages(getBaseContext());
                    }

                    // Processing articles
                    List<FarmQuery.Article> articles = farm.articles();
                    if (articles != null) {
                        if (BuildConfig.DEBUG) Log.i(TAG, "Fetching articles...");

                        for (FarmQuery.Article article : articles) {

                            if (article.type() == ArticleTypeEnum.CHEMICAL) {
                                long result = database.dao().setPhytoEkyId(Integer.valueOf(article.id()), article.referenceID(), article.name().split(" - ")[0] + "%");
                                if (result != 1) {
                                    if (BuildConfig.DEBUG)
                                        Log.d(TAG, "\tCreate phyto #" + article.id());
                                    database.dao().insert(new Phyto(Integer.valueOf(article.referenceID()), Integer.valueOf(article.id()), article.name(),
                                            null, article.referenceID(), null,
                                            null, null, false, true, "LITER"));
                                }
                            }

                            if (article.type() == ArticleTypeEnum.SEED) {
                                long result = database.dao().setSeedEkyId(Integer.valueOf(article.id()), article.referenceID());
                                if (result != 1) {
                                    if (BuildConfig.DEBUG)
                                        Log.d(TAG, "\tCreate seed #" + article.id());
                                    database.dao().insert(new Seed(Integer.valueOf(article.referenceID()), Integer.valueOf(article.id()), article.name(),
                                            null, false, true, "KILOGRAM"));
                                }
                            }

                            if (article.type() == ArticleTypeEnum.FERTILIZER) {
                                if (database.dao().setFertilizerEkyId(Integer.valueOf(article.id()), article.referenceID()) != 1) {
                                    if (BuildConfig.DEBUG) Log.d(TAG, "\tCreate fertilizer #" + article.id());
                                    database.dao().insert(new Fertilizer(Integer.valueOf(article.referenceID()), Integer.valueOf(article.id()), null,
                                            article.name(), null, null, null, null, null,
                                            null, null, null, null, true, "KILOGRAM"));
                                }
                            }
                        }
                    }
                    getInterventions();
                }
            }
        };

        apolloClient.query(FarmQuery.builder().build()).enqueue(farmCallback);
    }

    /**
     * Fetch Interventions with relations from server
     */
    private void getInterventions() {

        List<Integer> interventionEkyIdList = database.dao().interventionsEkiIdList();
        List<Integer> remoteInterventionList = new ArrayList<>();

        ApolloCall.Callback<InterventionQuery.Data> interventionCallback = new ApolloCall.Callback<InterventionQuery.Data>() {

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, e.getMessage(), e);
                Bundle bundle = new Bundle();
                bundle.putString("message", e.getLocalizedMessage());
                receiver.send(FAILED, bundle);
            }

            @Override
            public void onResponse(@Nonnull Response<InterventionQuery.Data> response) {

                InterventionQuery.Data data = response.data();

                if (data != null) {

                    InterventionQuery.Farm farm = data.farms().get(0);
                    List<InterventionQuery.Intervention> interventions = farm.interventions();

                    if (interventions != null) {

                        if (BuildConfig.DEBUG) Log.i(TAG, "Fetching interventions...");

                        // Building list of remote intervention IDs
                        for (InterventionQuery.Intervention inter : interventions)
                            remoteInterventionList.add(Integer.valueOf(inter.id()));

                        // Deletes local Intervention not present online
                        for (Integer ekyId : interventionEkyIdList)
                            if (!remoteInterventionList.contains(ekyId))
                                database.dao().deleteIntervention(ekyId);

                        int id;

                        for (InterventionQuery.Intervention inter : interventions) {

                            // Check intervention doesn't exists locally
                            if (!interventionEkyIdList.contains(Integer.valueOf(inter.id()))) {

                                if (BuildConfig.DEBUG) Log.d(TAG, "|-- save new intervention #" + inter.id());

                                Intervention newInter = new Intervention();

                                // Set general data
                                newInter.setFarm(farm.id());
                                newInter.setEky_id(Integer.valueOf(inter.id()));
                                newInter.setType(inter.type().toString());

                                // Set status
                                String status = inter.validatedAt() != null ? InterventionActivity.VALIDATED : InterventionActivity.SYNCED;
                                newInter.setStatus(status);

                                // Setting water values if present
                                Long waterQuantity = inter.waterQuantity();
                                ArticleVolumeUnitEnum waterUnit = inter.waterUnit();
                                if (waterQuantity != null && waterUnit != null) {
                                    newInter.setWater_quantity(waterQuantity.intValue());
                                    newInter.setWater_unit(waterUnit.rawValue());
                                }

                                // Write Intervention and get fallback id
                                id = (int) database.dao().insert(newInter);

                            } else {

                                if (BuildConfig.DEBUG) Log.d(TAG, "|-- update intervention #" + inter.id());

                                // Get actual local Intervention and proceed update
                                Interventions existingInter = database.dao().getIntervention(Integer.parseInt(inter.id()));

                                Date validatedAt = inter.validatedAt();
                                existingInter.intervention.status = validatedAt != null ? InterventionActivity.VALIDATED : InterventionActivity.SYNCED;

                                Long waterQuantity = inter.waterQuantity();
                                ArticleVolumeUnitEnum waterUnit = inter.waterUnit();
                                if (waterQuantity != null && waterUnit != null) {
                                    existingInter.intervention.water_quantity = waterQuantity.intValue();
                                    existingInter.intervention.water_unit = waterUnit.rawValue();
                                }

                                // Upsert modified Intervention in database
                                database.dao().insert(existingInter.intervention);

                                // Set id for next use
                                id = existingInter.intervention.id;

                                // Cleaning non unique primary key relations
                                if (!existingInter.workingDays.isEmpty())
                                    database.dao().delete(existingInter.workingDays.get(0));
                                for (Crops crop : existingInter.crops)
                                    database.dao().delete(crop.inter);
                                for (Persons person : existingInter.persons)
                                    database.dao().delete(person.inter);
                                for (Phytos phyto : existingInter.phytos)
                                    database.dao().delete(phyto.inter);
                                for (Seeds seed : existingInter.seeds)
                                    database.dao().delete(seed.inter);
                                for (Fertilizers fertilizer : existingInter.fertilizers)
                                    database.dao().delete(fertilizer.inter);
                                for (Equipments equipment : existingInter.equipments)
                                    database.dao().delete(equipment.inter);
                                for (Harvest harvest : existingInter.harvests)
                                    database.dao().delete(harvest);
                            }

                            //////////////////////
                            // Saving WorkingDays
                            //////////////////////
                            for (InterventionQuery.WorkingDay workingDay : inter.workingDays()) {
                                Date executionDate = workingDay.executionDate();
                                Long hourDuration = workingDay.hourDuration();
                                if (executionDate != null && hourDuration != null)
                                    database.dao().insert(new InterventionWorkingDay(id, executionDate, hourDuration.intValue()));
                            }

                            //////////////////////////
                            // Saving Crops (targets)
                            //////////////////////////
                            for (InterventionQuery.Target target : inter.targets()) {
                                Long workingPercentage = target.workingPercentage();
                                database.dao().insert(new InterventionCrop(id, target.crop().uuid(), workingPercentage.intValue()));
                            }

                            ////////////////////
                            // Saving Operators
                            ////////////////////
                            List<InterventionQuery.Operator> operators = inter.operators();
                            if (operators != null) {
                                for (InterventionQuery.Operator operator : operators) {
                                    InterventionQuery.Person person = operator.person();
                                    if (person != null) {
                                        boolean isDiver = operator.role() == OperatorRoleEnum.DRIVER;
                                        int personId = database.dao().getPersonId(Integer.valueOf(person.id()));
                                        database.dao().insert(new InterventionPerson(id, personId, isDiver));
                                    }
                                }
                            }

                            //////////////////
                            // Saving Weather
                            //////////////////
                            InterventionQuery.Weather weather = inter.weather();
                            if (weather != null) {
                                Double temperature = weather.temperature();
                                Float temp = temperature != null ? temperature.floatValue() : null;
                                Double windSpeed = weather.windSpeed();
                                Float wind = windSpeed != null ? windSpeed.floatValue() : null;
                                WeatherEnum description = weather.description();
                                String desc = description != null ? description.rawValue() : null;
                                if (temp != null || wind != null || desc != null)
                                    database.dao().insert(new Weather(id, temp, wind, desc));
                            }

                            /////////////////////
                            // Saving Equipments
                            /////////////////////
                            List<InterventionQuery.Tool> tools = inter.tools();
                            if (tools != null) {
                                for (InterventionQuery.Tool tool : tools) {
                                    if (tool.equipment() != null) {
                                        int equipmentId = database.dao().getEquipmentId(Integer.valueOf(tool.equipment().id()));
                                        database.dao().insert(new InterventionEquipment(id, equipmentId));
                                    }
                                }
                            }

                            /////////////////
                            // Saving Inputs
                            /////////////////
                            List<InterventionQuery.Input> inputs = inter.inputs();
                            if (inputs != null) {
                                for (InterventionQuery.Input input : inputs) {

                                    InterventionQuery.Article article = input.article();
                                    if (article != null) {

                                        Log.i(TAG, "article " + article.id());

                                        String referenceId = article.referenceID();
                                        int articleId;

                                        if (article.type().equals(ArticleTypeEnum.CHEMICAL)) {
                                            if (referenceId != null) // TODO: waiting for API to support null
                                                articleId = Integer.valueOf(referenceId);
                                            else
                                                articleId = database.dao().getPhytoId(Integer.valueOf(input.article().id()));

                                            Double quantity = input.quantity();
                                            float qtt = quantity != null ? quantity.floatValue() : 0;
                                            database.dao().insert(new InterventionPhytosanitary(qtt, input.unit().toString(), id, articleId));

                                        } else if (article.type().equals(ArticleTypeEnum.SEED)) {
                                            //int seedId = database.dao().getSeedId(Integer.valueOf(input.article().id()));
                                            database.dao().insert(new InterventionSeed(input.quantity().floatValue(), input.unit().toString(), id, Integer.valueOf(article.referenceID())));

                                        } else if (article.type().equals(ArticleTypeEnum.FERTILIZER)) {
                                            //int fertiId = database.dao().getFertilizerId(Integer.valueOf(input.article().id()));
                                            InterventionFertilizer interventionFertilizer =
                                                    new InterventionFertilizer(input.quantity().floatValue(), input.unit().toString(), id, Integer.valueOf(article.referenceID()));
                                            database.dao().insert(interventionFertilizer);
                                        }
                                    }
                                }
                            }

                            /////////////////////////////
                            // Saving Outputs (harvests)
                            /////////////////////////////
                            List<InterventionQuery.Output> outputs = inter.outputs();
                            Boolean globalOutputs = inter.globalOutputs();
                            if (outputs != null) {
                                if (globalOutputs != null && !globalOutputs) {
                                    for (InterventionQuery.Output output : outputs) {
                                        List<InterventionQuery.Load> loads = output.loads();
                                        if (loads != null) {
                                            for (InterventionQuery.Load load : loads) {
                                                if (load.quantity() > 0) {
                                                    InterventionQuery.Storage storage = load.storage();
                                                    Integer storageId = storage != null ? Integer.valueOf(storage.id()) : null;
                                                    HarvestLoadUnitEnum unit = load.unit();
                                                    String quantityUnit = unit != null ? unit.toString() : null;
                                                    database.dao().insert(new Harvest(id, (float) load.quantity(), quantityUnit, storageId, load.number(), output.nature().toString()));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // Finally go back to activity and refresh recycler
                receiver.send(DONE, new Bundle());
            }
        };
        apolloClient.query(InterventionQuery.builder().build()).enqueue(interventionCallback);
    }
}