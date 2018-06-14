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
import com.ekylibre.android.InterventionActivity;
import com.ekylibre.android.MainActivity;
import com.ekylibre.android.ProfileQuery;
import com.ekylibre.android.PullQuery;
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
import com.ekylibre.android.utils.Enums;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;


import java.util.ArrayList;
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
                getAllData();
                break;

            case ACTION_SYNC_ALL:

                // Mutations (order is important)
                pushDeleteIntervention();
                pushUpdateIntervention();
                if (MainActivity.ITEMS_TO_SYNC) pushCreatePersonsAndEquipments();
                pushCreateIntervention();

                // Queries
                //getAllData();

                // Action done
                //receiver.send(DONE, new Bundle());

                break;
        }

    }


    /**
     * delete intervention mutation
     */
    private void pushDeleteIntervention() {

        List<Interventions> deletableIntervention = database.dao().getDeletableInterventions();

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
                                if (error.message() != null && error.message().contains("does not exist"))
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

        List<Interventions> updatableInterventions = database.dao().getUpdatableInterventions();

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
                            PushPersonMutation.CreatePerson mutation = response.data().createPerson();
                            if (!mutation.person().id().equals("")) {
                                database.dao().setPersonEkyId(person.id, Integer.valueOf(mutation.person().id()));
                                if (BuildConfig.DEBUG)
                                    Log.i(TAG, "Person #" + mutation.person().id() + " successfully created !");
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
                            }
                            if (ACTION.equals(ACTION_CREATE_ARTICLES))
                                receiver.send(DONE, new Bundle());
                        }
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                        ERROR = true;
                    }
                });
            }
        } else if (BuildConfig.DEBUG) Log.i(TAG, "No new Equipment to push");

        if (ERROR) {
            MainActivity.ITEMS_TO_SYNC = true;
            receiver.send(FAILED, new Bundle());
        }

    }


    /**
     * create intervention mutation
     */
    private void pushCreateIntervention() {

        List<Interventions> interventions = database.dao().getSyncableInterventions();

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
                    Log.i(TAG, "Create remote intervention #" + createInter.intervention.eky_id);

                targets = new ArrayList<>();
                workingDays = new ArrayList<>();
                inputs = new ArrayList<>();
                tools = new ArrayList<>();
                outputs = new ArrayList<>();
                loads = new ArrayList<>();
                operators = new ArrayList<>();
                weatherInput = null;

                for (Crops crop : createInter.crops)
                    targets.add(InterventionTargetAttributes.builder()
                            .cropID(crop.inter.crop_id)
                            .workAreaPercentage(crop.inter.work_area_percentage)
                            .build());

                for (InterventionWorkingDay wd : createInter.workingDays)
                    workingDays.add(InterventionWorkingDayAttributes.builder()
                            .executionDate(wd.execution_date)
                            .hourDuration((long) wd.hour_duration)
                            .build());

                for (Persons person : createInter.persons)
                    operators.add(InterventionOperatorAttributes.builder()
                            .personId(String.valueOf(person.person.get(0).eky_id))
                            .role((person.inter.is_driver) ? OperatorRoleEnum.DRIVER : OperatorRoleEnum.OPERATOR)
                            .build());

                for (Equipments equipment : createInter.equipments)
                    tools.add(InterventionToolAttributes.builder()
                            .equipmentId(String.valueOf(equipment.equipment.get(0).eky_id))
                            .build());

                for (Harvest harvest : createInter.harvests)
                    loads.add(HarvestLoadAttributes.builder()
                            .number(harvest.number)
                            .quantity(harvest.quantity)
                            .netQuantity(harvest.quantity)
                            .unit(HarvestLoadUnitEnum.valueOf(harvest.unit))
                            .storageID(String.valueOf(harvest.id_storage)).build());
                if (!loads.isEmpty())
                    outputs.add(InterventionOutputAttributes.builder()
                            .nature(InterventionOutputTypeEnum.safeValueOf(createInter.harvests.get(0).type))
                            .loads(loads).build());

                for (Phytos phyto : createInter.phytos)
                    if (phyto.phyto.get(0).eky_id == null) // TODO warning ! may be one maaid for several products
                        // Create new article
                        inputs.add(InterventionInputAttributes.builder()
                                //.marketingAuthorizationNumber(phyto.phyto.get(0).maaid)
                                .article(ArticleAttributes.builder()
                                        .referenceID(String.valueOf(phyto.phyto.get(0).id))
                                        .type(ArticleTypeEnum.PHYTOSANITARY).build())
                                .quantity(phyto.inter.quantity)
                                .unit(ArticleAllUnitEnum.safeValueOf(phyto.inter.unit))
                                .build());
                    else
                        // Use existing article
                        inputs.add(InterventionInputAttributes.builder()
                                .article(ArticleAttributes.builder().id(String.valueOf(phyto.phyto.get(0).eky_id)).build())
                                .quantity(phyto.inter.quantity)
                                .unit(ArticleAllUnitEnum.safeValueOf(phyto.inter.unit))
                                .build());

                for (Seeds seed : createInter.seeds)
                    if (seed.seed.get(0).eky_id == null)
                        // Create new article
                        inputs.add(InterventionInputAttributes.builder()
                                .article(ArticleAttributes.builder()
                                        .referenceID(String.valueOf(seed.seed.get(0).id))
                                        .type(ArticleTypeEnum.SEED).build())
                                .quantity(seed.inter.quantity)
                                .unit(ArticleAllUnitEnum.safeValueOf(seed.inter.unit)).build());
                    else
                        // Use existing article
                        inputs.add(InterventionInputAttributes.builder()
                                .article(ArticleAttributes.builder().id(String.valueOf(seed.seed.get(0).eky_id)).build())
                                .quantity(seed.inter.quantity)
                                .unit(ArticleAllUnitEnum.safeValueOf(seed.inter.unit)).build());

                for (Fertilizers fertilizer : createInter.fertilizers)
                    if (fertilizer.fertilizer.get(0).eky_id == null)
                        // Create new article
                        inputs.add(InterventionInputAttributes.builder()
                                .article(ArticleAttributes.builder()
                                        .referenceID(String.valueOf(fertilizer.fertilizer.get(0).id))
                                        .type(ArticleTypeEnum.FERTILIZER).build())
                                .quantity(fertilizer.inter.quantity)
                                .unit(ArticleAllUnitEnum.safeValueOf(fertilizer.inter.unit)).build());
                    else
                        // Use existing article
                        inputs.add(InterventionInputAttributes.builder()
                                .article(ArticleAttributes.builder()
                                        .id(String.valueOf(fertilizer.fertilizer.get(0).eky_id)).build())
                                .quantity(fertilizer.inter.quantity)
                                .unit(ArticleAllUnitEnum.safeValueOf(fertilizer.inter.unit)).build());

                for (Weather weather : createInter.weather)
                    weatherInput = WeatherAttributes.builder()
                            .description(weather.description != null ? WeatherEnum.valueOf(weather.description) : null)
                            .temperature(weather.temperature != null ? Double.valueOf(weather.temperature) : null)
                            .windSpeed(weather.wind_speed != null ? Double.valueOf(weather.wind_speed) : null).build();

                PushInterMutation pushIntervention = PushInterMutation.builder()
                        .farmId(createInter.intervention.farm)
                        .procedure(InterventionTypeEnum.safeValueOf(createInter.intervention.type))
                        .cropList(targets)
                        .workingDays(workingDays)
                        .inputs(inputs)
                        .outputs(outputs)
                        .tools(tools)
                        .operators(operators)
                        .weather(weatherInput)
                        .waterQuantity((createInter.intervention.water_quantity != null) ? (long) createInter.intervention.water_quantity : null)
                        .waterUnit((createInter.intervention.water_unit != null) ? ArticleVolumeUnitEnum.safeValueOf(createInter.intervention.water_unit) : null)
                        .build();

                apolloClient.mutate(pushIntervention).enqueue(new ApolloCall.Callback<PushInterMutation.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<PushInterMutation.Data> response) {
                        if (!response.hasErrors()) {
                            PushInterMutation.CreateIntervention mutation = response.data().createIntervention();
                            if (!mutation.intervention().id().equals("")) {
                                Log.e(TAG, "eky_id attributed");
                                database.dao().setInterventionEkyId(createInter.intervention.id, Integer.valueOf(mutation.intervention().id()));
                            } else {
                                Log.e(TAG, "Error while attributing id");
                            }
                            getAllData();
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
            getAllData();
        }
    }


    /**
     * get intervention query
     */
    private void getAllData() {

        List<Integer> interventionEkyIdList = database.dao().interventionsEkiIdList();

        // We always get the full article list from server
        List<Integer> personEkyIdList = database.dao().personEkiIdList();
        //List<Integer> phytoEkyIdList = new ArrayList<>();
        //List<Integer> seedEkyIdList = new ArrayList<>();
        //List<Integer> fertilizerEkyIdList = new ArrayList<>();

        apolloClient.query(PullQuery.builder().build())
                .enqueue(new ApolloCall.Callback<PullQuery.Data>() {

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        Log.e(TAG, e.getMessage(), e);
                        Bundle bundle = new Bundle();
                        bundle.putString("message", e.getLocalizedMessage());
                        receiver.send(FAILED, bundle);
                    }

                    @Override
                    public void onResponse(@Nonnull Response<PullQuery.Data> response) {

                        PullQuery.Data data = response.data();
                        if (data != null && data.farms() != null) {

                            //Log.i(TAG, "Nombre de fermes: " + data.farms().size());
                            // TODO: Farm selector

                            // Saving first farm (only one for now)
                            PullQuery.Farm farm = data.farms().get(0);
                            Farm newFarm = new Farm(farm.id(), farm.label());
                            database.dao().insert(newFarm);

                            // Saving current farm in SharedPreferences
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("current-farm-id", farm.id());
                            editor.putString("current-farm-name", farm.label());
                            editor.apply();

                            List<Integer> remoteInterventionList = new ArrayList<>();

                            for (PullQuery.Intervention remoteInter : farm.interventions()) {
                                remoteInterventionList.add(Integer.valueOf(remoteInter.id()));
                            }

                            for (Integer localInterventionEkyId : interventionEkyIdList) {
                                if (!remoteInterventionList.contains(localInterventionEkyId)) {
                                    database.dao().deleteIntervention(localInterventionEkyId);
                                }
                            }

                            // Processing crops and associated plots
                            if (!farm.crops().isEmpty()) {
                                if (BuildConfig.DEBUG) Log.i(TAG, "Fetching crops...");

                                editor.putBoolean("no-crop", false);
                                editor.apply();

                                for (PullQuery.Crop crop : farm.crops()) {

                                    // Symplify crop name
                                    String name = crop.name().replace(crop.plot().name() + " | ", "");

                                    // Saving crop
                                    Crop newCrop = new Crop(
                                            crop.uuid(), name, crop.specie().rawValue(), crop.productionNature().specie().rawValue(),
                                            crop.productionMode(), null, null, null,
                                            Float.valueOf(crop.surfaceArea().split(" ")[0]), null,
                                            crop.startDate(), crop.stopDate(), crop.plot().uuid(),
                                            farm.id());
                                    database.dao().insert(newCrop);

                                    // Saving plot
                                    Plot newPlot = new Plot(crop.plot().uuid(), crop.plot().name(), null,
                                            Float.valueOf(crop.plot().surfaceArea().split(" ")[0]), null, null, null, farm.id());
                                    database.dao().insert(newPlot);
                                }
                                // TODO: delete crop & plot if deleted on server
                            } else {
                                editor.putBoolean("no-crop", true);
                                editor.apply();
                            }

                            // Processing people
                            if (!farm.people().isEmpty()) {
                                if (BuildConfig.DEBUG) Log.i(TAG, "Fetching people...");

                                for (PullQuery.person person : farm.people()) {

                                    String firstName = (person.firstName() != null) ? person.firstName() : "";

                                    // Save or update Person
                                    if (personEkyIdList.contains(Integer.valueOf(person.id()))) {
                                        database.dao().updatePerson(firstName, person.lastName(), person.id());
                                    } else {
                                        if (BuildConfig.DEBUG) Log.d(TAG, "\tCreate person #" + person.id());
                                        database.dao().insert(new Person(Integer.valueOf(person.id()), firstName, person.lastName(), farm.id()));
                                    }
                                }
                                // TODO: delete person if deleted on server --> or mark status deleted ?
                            }

                            // Processing equipments
                            if (!farm.equipments().isEmpty()) {
                                if (BuildConfig.DEBUG) Log.i(TAG, "Fetching equipments...");
                                for (PullQuery.Equipment equipment : farm.equipments()) {

                                    int result = database.dao().setEquipmentEkyId(Integer.valueOf(equipment.id()), equipment.name());

                                    if (result != 1) {
                                        if (BuildConfig.DEBUG) Log.i(TAG, "\tCreate equipment #" + equipment.id());
                                        database.dao().insert(new Equipment(Integer.valueOf(equipment.id()),
                                                equipment.name(), equipment.type().rawValue(), equipment.number(), farm.id()));
                                    }
                                }
                            }

                            // Processing storages
                            if (!farm.storages().isEmpty()) {
                                Log.i(TAG, "Fetching storages...");
                                for (PullQuery.Storage storage : farm.storages()) {
                                    if (BuildConfig.DEBUG) Log.d(TAG, "\tCreate/update storage #" + storage.id());
                                    database.dao().insert(new Storage(
                                            Integer.valueOf(storage.id()),
                                            storage.name(),
                                            storage.type().rawValue()));
                                }
                                Enums.STORAGE_LIST = database.dao().getStorages();
                                Enums.generateStorages(getBaseContext());
                            }

                            // Processing articles
                            if (!farm.articles().isEmpty()) {
                                if (BuildConfig.DEBUG) Log.i(TAG, "Fetching articles...");

                                for (PullQuery.Article article : farm.articles()) {

                                    if (article.type() == ArticleTypeEnum.PHYTOSANITARY) {
                                        long result = database.dao().setPhytoEkyId(Integer.valueOf(article.id()), article.referenceId(), article.name().split(" - ")[0] + "%");
                                        if (result != 1) {
                                            if (BuildConfig.DEBUG) Log.d(TAG, "\tCreate phyto #" + article.id());
                                            Phyto phyto = new Phyto(Integer.valueOf(article.referenceId()), Integer.valueOf(article.id()), article.name(),
                                                    null, article.referenceId(), null,
                                                    null, null, false, true, "LITER");
                                            database.dao().insert(phyto);
                                        }
                                        //phytoEkyIdList.add(Integer.valueOf(article.id()));
                                    }

                                    if (article.type() == ArticleTypeEnum.SEED) {
                                        long result = database.dao().setSeedEkyId(Integer.valueOf(article.id()), article.referenceId());
                                        if (result != 1) {
                                            if (BuildConfig.DEBUG) Log.d(TAG, "\tCreate seed #" + article.id());
                                            Seed seed = new Seed(Integer.valueOf(article.referenceId()), Integer.valueOf(article.id()), article.name(),
                                                    null, false, true, "KILOGRAM");
                                            database.dao().insert(seed);
                                        }
                                        //seedEkyIdList.add(Integer.valueOf(article.id()));
                                    }

                                    if (article.type() == ArticleTypeEnum.FERTILIZER) {
                                        if (database.dao().setFertilizerEkyId(Integer.valueOf(article.id()), article.referenceId()) != 1) {
                                            if (BuildConfig.DEBUG) Log.d(TAG, "\tCreate fertilizer #" + article.id());
                                            Fertilizer fertilizer = new Fertilizer(Integer.valueOf(article.referenceId()), Integer.valueOf(article.id()), null,
                                                    article.name(), null, null, null, null, null,
                                                    null, null, null, null, true, "KILOGRAM");
                                            database.dao().insert(fertilizer);
                                        }
                                        //fertilizerEkyIdList.add(Integer.valueOf(article.id()));
                                    }
                                    // TODO: implement Materials when ready in API
                                }
                            }

                            // Processing interventions
                            if (!farm.interventions().isEmpty()) {
                                if (BuildConfig.DEBUG) Log.i(TAG, "Fetching interventions...");

                                // Intevention index in response query dataset
                                int index = 0;
//                                List<Integer> remoteInterventionList = new ArrayList<>();
//
//                                Log.e(TAG, "local interventions ids --> " + interventionEkyIdList.toString());
//                                Log.e(TAG, "remote interventions ids --> " + remoteInterventionList.toString());

                                for (PullQuery.Intervention inter : farm.interventions()) {

                                    // Add remote intervention id to list for comparison and deletion
                                    //remoteInterventionList.add(Integer.valueOf(inter.id()));

                                    // Save as new intervention if not existing locally
                                    if (!interventionEkyIdList.contains(Integer.valueOf(inter.id()))) {
                                        if (BuildConfig.DEBUG) Log.d(TAG, "\tSave new intervention #" + inter.id());

                                        // Building Intervention object
                                        Intervention newInter = new Intervention();
                                        newInter.setEky_id(Integer.valueOf(inter.id()));
                                        newInter.setType(inter.type().toString());
                                        if (inter.waterQuantity() != null) {
                                            newInter.setWater_quantity((int) (long) inter.waterQuantity());
                                            newInter.setWater_unit(inter.waterUnit().toString());
                                        }
                                        newInter.setFarm(farm.id());

                                        // Set status
                                        String status = (inter.validatedAt() != null) ? InterventionActivity.VALIDATED : InterventionActivity.SYNCED;
                                        newInter.setStatus(status);

                                        // Write Intervention and get fallback id
                                        int newInterId = (int) (long) database.dao().insert(newInter);

                                        // Saving WorkingDays
                                        for (PullQuery.WorkingDay wd : farm.interventions().get(index).workingDays()) {
                                            InterventionWorkingDay interventionWD =
                                                    new InterventionWorkingDay(newInterId, wd.executionDate(), (int) (long) wd.hourDuration());
                                            database.dao().insert(interventionWD);
                                        }

                                        // Saving Crops (targets)
                                        for (PullQuery.Target target : farm.interventions().get(index).targets()) {
                                            InterventionCrop interventionCrop =
                                                    new InterventionCrop(newInterId, target.crop().uuid(), 100);
                                            database.dao().insert(interventionCrop);
                                        }

                                        // Saving Operators
                                        for (PullQuery.Operator operator : farm.interventions().get(index).operators()) {

                                            if (operator.person() != null) {
                                                boolean isDiver = operator.role() == OperatorRoleEnum.DRIVER;
                                                int personId = database.dao().getPersonId(Integer.valueOf(operator.person().id()));
                                                database.dao().insert(new InterventionPerson(newInterId, personId, isDiver));
                                            }
                                        }

                                        // Saving Weather
                                        PullQuery.Weather weather = farm.interventions().get(index).weather();
                                        if (weather != null) {
                                            if (weather.temperature() != null || weather.windSpeed() != null || weather.description() != null) {
                                                Float temp = weather.temperature() != null ? weather.temperature().floatValue() : null;
                                                Float wind = weather.windSpeed() != null ? weather.windSpeed().floatValue() : null;
                                                String description = weather.description() != null ? weather.description().rawValue() : null;
                                                database.dao().insert(new Weather(newInterId, temp, wind, description));
                                            }
                                        }

                                        // Saving Equipments
                                        for (PullQuery.Tool tool : farm.interventions().get(index).tools()) {
                                            if (tool.equipment() != null) {
                                                int equipmentId = database.dao().getEquipmentId(Integer.valueOf(tool.equipment().id()));
                                                database.dao().insert(new InterventionEquipment(newInterId, equipmentId));
                                            }
                                        }

                                        // Saving Inputs
                                        for (PullQuery.Input input : farm.interventions().get(index).inputs()) {

                                            if (input.article() != null) {

                                                if (input.article().type().equals(ArticleTypeEnum.PHYTOSANITARY)) {
                                                    //int phytoId = database.dao().getPhytoId(Integer.valueOf(input.article().id()));
                                                    InterventionPhytosanitary interventionPhyto =
                                                            new InterventionPhytosanitary(input.quantityValue(), input.unit().toString(), newInterId, Integer.valueOf(input.article().referenceID()));
                                                    database.dao().insert(interventionPhyto);

                                                } else if (input.article().type().equals(ArticleTypeEnum.SEED)) {
                                                    //int seedId = database.dao().getSeedId(Integer.valueOf(input.article().id()));
                                                    InterventionSeed interventionSeed =
                                                            new InterventionSeed(input.quantityValue(), input.unit().toString(), newInterId, Integer.valueOf(input.article().referenceID()));
                                                    database.dao().insert(interventionSeed);

                                                } else if (input.article().type().equals(ArticleTypeEnum.FERTILIZER)) {
                                                    //int fertiId = database.dao().getFertilizerId(Integer.valueOf(input.article().id()));
                                                    InterventionFertilizer interventionFertilizer =
                                                            new InterventionFertilizer(input.quantityValue(), input.unit().toString(), newInterId, Integer.valueOf(input.article().referenceID()));
                                                    database.dao().insert(interventionFertilizer);
                                                }
                                            }
                                        }

                                        // Saving Outputs (harvests)
                                        if (farm.interventions().get(index).outputs().size() > 0) {
                                            int outputIndex = 0;
                                            for (PullQuery.Output output : farm.interventions().get(index).outputs()) {
                                                for (PullQuery.Load load : farm.interventions().get(index).outputs().get(outputIndex).loads()) {
                                                    Harvest harvest =
                                                            new Harvest(newInterId, (float) load.quantity(), load.unit().toString(), Integer.valueOf(load.storage().id()), load.number(), output.nature().toString());
                                                    database.dao().insert(harvest);
                                                }
                                                ++outputIndex;
                                            }
                                        }

                                    } else {

                                        // Update existing intervention
                                        if (BuildConfig.DEBUG) Log.d(TAG, "|-- update intervention #" + inter.id());
                                        Interventions existingInter = database.dao().getIntervention(Integer.valueOf(inter.id()));

                                        if (existingInter.intervention.water_quantity != null) {
                                            existingInter.intervention.water_quantity = ((int) (long) inter.waterQuantity());
                                            existingInter.intervention.water_unit = inter.waterUnit().toString();
                                        }
                                        if (inter.validatedAt() != null)
                                            existingInter.intervention.status = InterventionActivity.VALIDATED;
                                        else
                                            existingInter.intervention.status = InterventionActivity.SYNCED;
                                        database.dao().insert(existingInter.intervention);

                                        // Cleaning non unique primary key relations
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

                                        // Saving WorkingDays
                                        for (PullQuery.WorkingDay wd : farm.interventions().get(index).workingDays()) {
                                            InterventionWorkingDay interventionWD =
                                                    new InterventionWorkingDay(existingInter.intervention.id, wd.executionDate(), (int) (long) wd.hourDuration());
                                            database.dao().insert(interventionWD);
                                        }

                                        // Saving Crops (targets)
                                        for (PullQuery.Target target : farm.interventions().get(index).targets()) {
                                            InterventionCrop interventionCrop =
                                                    new InterventionCrop(existingInter.intervention.id, target.crop().uuid(), 100);
                                            database.dao().insert(interventionCrop);
                                        }

                                        // Saving Operators
                                        for (PullQuery.Operator operator : farm.interventions().get(index).operators()) {

                                            if (operator.person() != null) {
                                                boolean isDiver = operator.role() == OperatorRoleEnum.DRIVER;
                                                int personId = database.dao().getPersonId(Integer.valueOf(operator.person().id()));
                                                database.dao().insert(new InterventionPerson(existingInter.intervention.id, personId, isDiver));
                                            }
                                        }

                                        // Saving Weather
                                        PullQuery.Weather weather = farm.interventions().get(index).weather();
                                        if (weather != null) {
                                            if (weather.temperature() != null || weather.windSpeed() != null || weather.description() != null) {
                                                Float temp = weather.temperature() != null ? weather.temperature().floatValue() : null;
                                                Float wind = weather.windSpeed() != null ? weather.windSpeed().floatValue() : null;
                                                String description = weather.description() != null ? weather.description().rawValue() : null;
                                                database.dao().insert(new Weather(existingInter.intervention.id, temp, wind, description));
                                            }
                                        }

                                        // Saving Equipments
                                        for (PullQuery.Tool tool : farm.interventions().get(index).tools()) {
                                            if (tool.equipment() != null) {
                                                int equipmentId = database.dao().getEquipmentId(Integer.valueOf(tool.equipment().id()));
                                                database.dao().insert(new InterventionEquipment(existingInter.intervention.id, equipmentId));
                                            }
                                        }

                                        // Saving Inputs
                                        for (PullQuery.Input input : farm.interventions().get(index).inputs()) {

                                            if (input.article() != null) {

                                                if (input.article().type().equals(ArticleTypeEnum.PHYTOSANITARY)) {
                                                    //int phytoId = database.dao().getPhytoId(Integer.valueOf(input.article().id()));
                                                    InterventionPhytosanitary interventionPhyto =
                                                            new InterventionPhytosanitary(input.quantityValue(), input.unit().toString(),
                                                                    existingInter.intervention.id, Integer.valueOf(input.article().referenceID()));
                                                    database.dao().insert(interventionPhyto);

                                                } else if (input.article().type().equals(ArticleTypeEnum.SEED)) {
                                                    //int seedId = database.dao().getSeedId(Integer.valueOf(input.article().id()));
                                                    InterventionSeed interventionSeed =
                                                            new InterventionSeed(input.quantityValue(), input.unit().toString(), existingInter.intervention.id, Integer.valueOf(input.article().referenceID()));
                                                    database.dao().insert(interventionSeed);

                                                } else if (input.article().type().equals(ArticleTypeEnum.FERTILIZER)) {
                                                    //int fertiId = database.dao().getFertilizerId(Integer.valueOf(input.article().id()));
                                                    InterventionFertilizer interventionFertilizer =
                                                            new InterventionFertilizer(input.quantityValue(), input.unit().toString(), existingInter.intervention.id, Integer.valueOf(input.article().referenceID()));
                                                    database.dao().insert(interventionFertilizer);
                                                }
                                            }
                                        }

                                        // Saving Outputs (harvests)
                                        if (farm.interventions().get(index).outputs().size() > 0) {
                                            int outputIndex = 0;
                                            for (PullQuery.Output output : farm.interventions().get(index).outputs()) {
                                                for (PullQuery.Load load : farm.interventions().get(index).outputs().get(outputIndex).loads()) {
                                                    Harvest harvest =
                                                            new Harvest(existingInter.intervention.id, (float) load.quantity(), load.unit().toString(), Integer.valueOf(load.storage().id()), load.number(), output.nature().toString());
                                                    database.dao().insert(harvest);
                                                }
                                                ++outputIndex;
                                            }
                                        }

                                    }
                                    ++index;
                                }
                            }
                        }

                        if (response.hasErrors()) {
                            Bundle bundle = new Bundle();
                            bundle.putString("message", response.errors().get(0).message());
                            receiver.send(FAILED, bundle);
                        }

                        // Finally go back to activity and refresh recycler
                        receiver.send(DONE, new Bundle());
                    }
                });
    }
}