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
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import com.ekylibre.android.InterventionActivity;
import com.ekylibre.android.MainActivity;
import com.ekylibre.android.ProfileQuery;
import com.ekylibre.android.PullQuery;
import com.ekylibre.android.PushInterventionMutation;
import com.ekylibre.android.database.AppDatabase;
import com.ekylibre.android.database.models.Crop;
import com.ekylibre.android.database.models.Equipment;
import com.ekylibre.android.database.models.Farm;
import com.ekylibre.android.database.models.Fertilizer;
import com.ekylibre.android.database.models.Intervention;
import com.ekylibre.android.database.models.Person;
import com.ekylibre.android.database.models.Phyto;
import com.ekylibre.android.database.models.Plot;
import com.ekylibre.android.database.models.Seed;
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

import com.ekylibre.android.type.ArticleAllUnit;
import com.ekylibre.android.type.ArticleInputObject;
import com.ekylibre.android.type.ArticleType;
import com.ekylibre.android.type.ArticleVolumeUnit;
import com.ekylibre.android.type.CreateInterventionInputInputObject;
import com.ekylibre.android.type.CreateInterventionOperatorInputObject;
import com.ekylibre.android.type.CreateInterventionTargetInputObject;
import com.ekylibre.android.type.CreateInterventionToolInputObject;
import com.ekylibre.android.type.CreateInterventionWorkingDayInputObject;
import com.ekylibre.android.type.InterventionTypeEnum;
import com.ekylibre.android.type.OperatorRoles;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;


public class SyncService extends IntentService {

    private static final String TAG = "SyncService";

    public static final int DONE = 10;
    public static final int FAILED = 11;

    public static final String ACTION_SYNC_PULL = "com.ekylibre.android.services.action.SYNC_PULL";
    public static final String ACTION_VERIFY_TOKEN = "com.ekylibre.android.services.action.VERIFY_TOKEN";

    private static String ACCESS_TOKEN;
    private static SharedPreferences sharedPreferences;
    private static AppDatabase database;


    public SyncService() {
        super("SyncService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // Handle Handshake Errors
        try {
            ProviderInstaller.installIfNeeded(this);
        } catch (GooglePlayServicesRepairableException e) {
            Log.e(TAG, "GooglePlayServicesRepairableException");
            GoogleApiAvailability.getInstance().showErrorNotification(this, e.getConnectionStatusCode());
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.e(TAG, "GooglePlayServicesNotAvailableException");
        }

        sharedPreferences = this.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        ACCESS_TOKEN = sharedPreferences.getString("access_token", null);

        // Get ResultReceiver from intent
        final ResultReceiver receiver = intent.getParcelableExtra("receiver");

        // Route action to function
        switch (intent.getAction()) {

            case ACTION_SYNC_PULL:
                handleActionSyncPull(receiver);
                break;

            case ACTION_VERIFY_TOKEN:
                handleActionVerifyToken();
                break;
        }
    }


    private void handleActionSyncPull(ResultReceiver receiver) {

        database = AppDatabase.getInstance(this);

        List<Integer> interventionEkyIdList = database.dao().interventionsEkiIdList();

        // We always get the full article list from server
        List<Integer> personEkyIdList = database.dao().personEkiIdList();
        List<Integer> phytoEkyIdList = new ArrayList<>();
        List<Integer> seedEkyIdList = new ArrayList<>();
        List<Integer> fertilizerEkyIdList = new ArrayList<>();

        ApolloClient apolloClient = GraphQLClient.getApolloClient(ACCESS_TOKEN);
        apolloClient.query(PullQuery.builder().build())
                .enqueue(new ApolloCall.Callback<PullQuery.Data>() {

            @Override
            public void onResponse(@Nonnull Response<PullQuery.Data> response) {

                PullQuery.Data data = response.data();
                if (data != null && data.farms() != null) {

                    Log.i(TAG, "Nombre de fermes: " + data.farms().size());

                    // TODO: Farm selector

                    // Saving first farm (only one for now)
                    PullQuery.Farm farm = data.farms().get(0);
                    Farm newFarm = new Farm(farm.id(), farm.label());
                    database.dao().insert(newFarm);

                    // Saving current farm in SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("current-farm-id", farm.id());
                    editor.putString("current-farm-name", farm.label());
                    editor.apply();

                    // Processing crops and associated plots
                    if (!farm.crops().isEmpty()) {
                        Log.i(TAG, "Fetching crops...");

                        for (PullQuery.Crop crop : farm.crops()) {

                            // Symplify crop name
                            String name = crop.name().replace(crop.plot().name() + " | ", "");

                            // Saving crop
                            Crop newCrop = new Crop(
                                    crop.uuid(), name, crop.specie(), crop.productionNature(),
                                    crop.productionMode(), null, null, null,
                                    Float.valueOf(crop.surfaceArea().split(" ")[0]), null,
                                    crop.startDate(), crop.stopDate(), crop.plot().uuid(),
                                    null, farm.id());
                            database.dao().insert(newCrop);

                            // Saving plot
                            Plot newPlot = new Plot(crop.plot().uuid(), crop.plot().name(), null,
                                    Float.valueOf(crop.plot().surfaceArea().split(" ")[0]), null, null, null, farm.id());
                            database.dao().insert(newPlot);
                        }
                        // TODO: delete crop & plot if deleted on server
                    }

                    // Processing people
                    if (!farm.people().isEmpty()) {
                        Log.i(TAG, "Fetching people...");

                        for (PullQuery.person person : farm.people()) {

                            String firstName = (person.firstName() != null) ? person.firstName() : "";

                            // Save or update Person
                            if (personEkyIdList.contains(Integer.valueOf(person.id()))) {
                                Log.i(TAG, "update person " + person.id());
                                database.dao().updatePerson(firstName, person.lastName(), person.id());
                            } else {
                                Log.i(TAG, "save person " + person.id());
                                database.dao().insert(new Person(Integer.valueOf(person.id()), firstName, person.lastName()));
                            }
                        }
                        // TODO: delete person if deleted on server --> or mark status deleted ?
                    }

                    // Processing equipments
                    if (!farm.equipments().isEmpty()) {
                        Log.i(TAG, "Fetching equipments...");
                        for (PullQuery.Equipment equipment : farm.equipments()) {

                            int result = database.dao().setEquipmentEkyId(Integer.valueOf(equipment.id()), equipment.name());
                            if (result != 1) {
                                Log.i(TAG, "Creating equipment");
                                database.dao().insert(new Equipment(Integer.valueOf(equipment.id()),
                                        equipment.name(), equipment.nature(), equipment.number(), farm.id()));
                            }
                        }
                    }

                    // Processing articles
                    if (!farm.articles().isEmpty()) {
                        Log.i(TAG, "Fetching articles...");

                        for (PullQuery.Article article : farm.articles()) {

                            if (article.type() == ArticleType.PHYTOSANITARY) {
                                Log.e(TAG, "phyto " + article.name() + " eky_id " + article.id() + " product_id " + article.referenceId());
                                long result = database.dao().setPhytoEkyId(Integer.valueOf(article.id()), article.referenceId());
                                Log.e(TAG, "result = " + result);
                                if (result != 1) {
                                    Phyto phyto = new Phyto(Integer.valueOf(article.referenceId()), Integer.valueOf(article.id()), article.name(),
                                            null, article.referenceId(), null,
                                            null, null, false, true, "LITER");
                                    database.dao().insert(phyto);
                                }
                                phytoEkyIdList.add(Integer.valueOf(article.id()));
                            }

                            if (article.type() == ArticleType.SEED) {
                                long result = database.dao().setSeedEkyId(Integer.valueOf(article.id()), article.referenceId());
                                if (result != 1) {
                                    Seed seed = new Seed(Integer.valueOf(article.referenceId()), Integer.valueOf(article.id()), article.name(),
                                            null, false, true, "KILOGRAM");
                                    database.dao().insert(seed);
                                }
                                seedEkyIdList.add(Integer.valueOf(article.id()));
                            }

                            if (article.type() == ArticleType.FERTILIZER) {
                                if (database.dao().setFertilizerEkyId(Integer.valueOf(article.id()), article.referenceId()) != 1) {
                                    Fertilizer fertilizer = new Fertilizer(Integer.valueOf(article.referenceId()), Integer.valueOf(article.id()), null,
                                            article.name(), null, null, null, null, null,
                                            null, null, null, null, true,"KILOGRAM");
                                    database.dao().insert(fertilizer);
                                }
                                fertilizerEkyIdList.add(Integer.valueOf(article.id()));
                            }
                            // TODO: implement Materials when ready in API
                        }
                    }

                    // Processing interventions
                    if (!farm.interventions().isEmpty()) {
                        Log.i(TAG, "Fetching interventions...");

                        // Intevention index in response query dataset
                        int index = 0;
                        List<Integer> remoteInterventionList = new ArrayList<>();

                        for (PullQuery.Intervention inter : farm.interventions()) {

                            // Add remote intervention id to list for comparison and deletion
                            remoteInterventionList.add(Integer.valueOf(inter.id()));

                            // Save as new intervention if not existing locally
                            if (!interventionEkyIdList.contains(Integer.valueOf(inter.id()))) {
                                Log.e(TAG, "Save new intervention");

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
                                        boolean isDiver = operator.role() == OperatorRoles.DRIVER;
                                        int personId = database.dao().getPersonId(Integer.valueOf(operator.person().id()));
                                        database.dao().insert(new InterventionPerson(newInterId, personId, isDiver));
                                    }
                                }

                                // Saving Equipments
                                for (PullQuery.Tool tool : farm.interventions().get(index).tools()) {
                                    if (tool.equipment() != null) {
                                        Log.e(TAG, "Equipment eky_id --> " + tool.equipment().id());
                                        database.dao().insert(new InterventionEquipment(newInterId, Integer.valueOf(tool.equipment().id())));
                                    }
                                }

                                // Saving Inputs
                                for (PullQuery.Input input : farm.interventions().get(index).inputs()) {

                                    if (input.article() != null) {

                                        if (input.article().type().equals(ArticleType.PHYTOSANITARY)) {
                                            int phytoId = database.dao().getPhytoId(Integer.valueOf(input.article().id()));
                                            Log.e(TAG, "PhytoId = " + phytoId);
                                            InterventionPhytosanitary interventionPhyto =
                                                    new InterventionPhytosanitary(input.quantityValue(), input.unit().toString(), newInterId, phytoId);
                                            database.dao().insert(interventionPhyto);

                                        } else if (input.article().type().equals(ArticleType.SEED)) {
                                            int seedId = database.dao().getSeedId(Integer.valueOf(input.article().id()));
                                            InterventionSeed interventionSeed =
                                                    new InterventionSeed(input.quantityValue(), input.unit().toString(), newInterId, seedId);
                                            database.dao().insert(interventionSeed);

                                        } else if (input.article().type().equals(ArticleType.FERTILIZER)) {
                                            int fertiId = database.dao().getFertilizerId(Integer.valueOf(input.article().id()));
                                            InterventionFertilizer interventionFertilizer =
                                                    new InterventionFertilizer(input.quantityValue(), input.unit().toString(), newInterId, fertiId);
                                            database.dao().insert(interventionFertilizer);
                                        }
                                    }
                                }

                            } else {
                                if (inter.validatedAt() != null) {
                                    Log.i(TAG, String.format("Intervention #%s validated", inter.id()));
                                    database.dao().updateInterventionStatus(Integer.valueOf(inter.id()), InterventionActivity.VALIDATED);
                                } else {
                                    Log.i(TAG, String.format("Intervention #%s synced", inter.id()));
                                    database.dao().updateInterventionStatus(Integer.valueOf(inter.id()), InterventionActivity.SYNCED);
                                }
                            }
                            ++index;
                        }

                        Log.e(TAG, "local interventions ids --> " + interventionEkyIdList.toString());
                        Log.e(TAG, "remote interventions ids --> " + remoteInterventionList.toString());

                        // Delets local intervention if remote deleted
                        for (Integer localInterventionEkyId : interventionEkyIdList) {
                            if (!remoteInterventionList.contains(localInterventionEkyId)) {
                                database.dao().deleteIntervention(localInterventionEkyId);
                            }
                        }
                    }
                    MainActivity.lastSyncTime = new Date();
//                    receiver.send(DONE, new Bundle());

                    handleActionSyncPush(receiver);
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, e.getMessage(), e);
                receiver.send(FAILED, new Bundle());


            }
        });
        //AppDatabase database = AppDatabase.getInstance(this);
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionVerifyToken() {

        ApolloClient apolloClient = GraphQLClient.getApolloClient(ACCESS_TOKEN);
        ProfileQuery profileQuery = ProfileQuery.builder().build();
        ApolloCall<ProfileQuery.Data> profileCall = apolloClient.query(profileQuery);

        profileCall.enqueue(new ApolloCall.Callback<ProfileQuery.Data>() {

            @Override
            public void onResponse(@Nonnull Response<ProfileQuery.Data> response) {
                ProfileQuery.Data data = response.data();
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {

            }

        });

    }

    private void handleActionSyncPush(ResultReceiver receiver) {

        database = AppDatabase.getInstance(this);

        List<Interventions> interventions = database.dao().getSyncableInterventions();

        ApolloClient apolloClient = GraphQLClient.getApolloClient(ACCESS_TOKEN);

        for (Interventions inter : interventions) {

            List<CreateInterventionTargetInputObject> targets = new ArrayList<>();
            List<CreateInterventionWorkingDayInputObject> workingDays = new ArrayList<>();
            List<CreateInterventionInputInputObject> inputs = new ArrayList<>();
            List<CreateInterventionOperatorInputObject> operators = new ArrayList<>();
            List<CreateInterventionToolInputObject> tools = new ArrayList<>();
            // TODO InterventionOutputsInputObject

            for (Crops crop : inter.crops) {
                targets.add(CreateInterventionTargetInputObject.builder()
                        .cropID(crop.inter.crop_id)
                        .workAreaPercentage(crop.inter.work_area_percentage)
                        .build());
            }

            for (InterventionWorkingDay wd : inter.workingDays) {
                workingDays.add(CreateInterventionWorkingDayInputObject.builder()
                        .executionDate(wd.execution_date)
                        .hourDuration((long) wd.hour_duration)
                        .build());
            }

            for (Persons person : inter.persons) {
                operators.add(CreateInterventionOperatorInputObject.builder()
                        .personId(String.valueOf(person.person.get(0).eky_id))
                        .role((person.inter.is_driver) ? OperatorRoles.DRIVER : OperatorRoles.OPERATOR)
                        .build());
            }

            for (Equipments equipment : inter.equipments) {
                if (equipment.equipment.get(0).eky_id == null) {

                } else {
                    tools.add(CreateInterventionToolInputObject.builder()
                            .equipmentId(String.valueOf(equipment.equipment.get(0).eky_id))
                            .build());
                }
            }

            for (Phytos phyto : inter.phytos) {
                if (phyto.phyto.get(0).eky_id == null) {
                    inputs.add(CreateInterventionInputInputObject.builder()
                            // TODO warning ! may be one maaid for several products
                            .marketingAuthorizationNumber(phyto.phyto.get(0).maaid)
                            //.article(ArticleInputObject.builder()
                            //        .referenceID(phyto.phyto.get(0).maaid)
                            //        .nature(ArticleType.PHYTOSANITARY).build())
                            .quantity(phyto.inter.quantity)
                            .unit(ArticleAllUnit.safeValueOf(phyto.inter.unit))
                            .build());
                } else {
                    inputs.add(CreateInterventionInputInputObject.builder()
                            .article(ArticleInputObject.builder()
                                    .id(String.valueOf(phyto.phyto.get(0).eky_id)).build())
                            .quantity(phyto.inter.quantity)
                            .unit(ArticleAllUnit.safeValueOf(phyto.inter.unit))
                            .build());
                }
            }

            for (Seeds seed : inter.seeds) {
                if (seed.seed.get(0).eky_id == null) {
                    inputs.add(CreateInterventionInputInputObject.builder()
                            //.marketingAuthorizationNumber(String.valueOf(seed.seed.get(0).id))
                            .article(ArticleInputObject.builder()
                                    .referenceID(String.valueOf(seed.seed.get(0).id))
                                    .type(ArticleType.SEED).build())
                            .quantity(seed.inter.quantity)
                            .unit(ArticleAllUnit.safeValueOf(seed.inter.unit))
                            .build());
                } else {
                    inputs.add(CreateInterventionInputInputObject.builder()
                            .article(ArticleInputObject.builder()
                                    .id(String.valueOf(seed.seed.get(0).eky_id)).build())
                            .quantity(seed.inter.quantity)
                            .unit(ArticleAllUnit.safeValueOf(seed.inter.unit))
                            .build());
                }
            }

            // TODO check with @aquaj for mutation
            for (Fertilizers fertilizer : inter.fertilizers) {
                if (fertilizer.fertilizer.get(0).eky_id == null) {
                    inputs.add(CreateInterventionInputInputObject.builder()
                            //.marketingAuthorizationNumber(String.valueOf(seed.seed.get(0).id))
                            .article(ArticleInputObject.builder()
                                    .referenceID(String.valueOf(fertilizer.fertilizer.get(0).id))
                                    .type(ArticleType.FERTILIZER).build())
                            .quantity(fertilizer.inter.quantity)
                            .unit(ArticleAllUnit.safeValueOf(fertilizer.inter.unit))
                            .build());
                } else {
                    inputs.add(CreateInterventionInputInputObject.builder()
                            .article(ArticleInputObject.builder()
                                    .id(String.valueOf(fertilizer.fertilizer.get(0).eky_id)).build())
                            .quantity(fertilizer.inter.quantity)
                            .unit(ArticleAllUnit.safeValueOf(fertilizer.inter.unit))
                            .build());
                }
            }

            PushInterventionMutation pushIntervention = PushInterventionMutation.builder()
                    .farmId(inter.intervention.farm)
                    .procedure(InterventionTypeEnum.safeValueOf(inter.intervention.type))
                    .cropList(targets)
                    .workingDays(workingDays)
                    .inputs(inputs)
                    .tools(tools)
                    .operators(operators)
                    .waterQuantity((inter.intervention.water_quantity != null) ? (long) inter.intervention.water_quantity : null)
                    .waterUnit((inter.intervention.water_unit != null) ? ArticleVolumeUnit.safeValueOf(inter.intervention.water_unit) : null)
                    .build();

            apolloClient.mutate(pushIntervention).enqueue(new ApolloCall.Callback<PushInterventionMutation.Data>() {
                @Override
                public void onResponse(@Nonnull Response<PushInterventionMutation.Data> response) {
                    if (!response.hasErrors()) {
                        PushInterventionMutation.CreateInterventionMutation mutation = response.data().createInterventionMutation();
                        if (mutation.errors() == null) {
                            Log.e(TAG, "eky_id attributed");
                            database.dao().setInterventionEkyId(inter.intervention.id, Integer.valueOf(mutation.intervention().id()));
                        }
                    }
                    receiver.send(DONE, new Bundle());
                }

                @Override
                public void onFailure(@Nonnull ApolloException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    receiver.send(FAILED, new Bundle());
                }
            });
        }
        receiver.send(DONE, new Bundle());
    }
}
