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

import com.ekylibre.android.MainActivity;
import com.ekylibre.android.ProfileQuery;
import com.ekylibre.android.PullQuery;
import com.ekylibre.android.PushInterventionMutation;
import com.ekylibre.android.database.AppDatabase;
import com.ekylibre.android.database.models.Crop;
import com.ekylibre.android.database.models.Farm;
import com.ekylibre.android.database.models.Fertilizer;
import com.ekylibre.android.database.models.Intervention;
import com.ekylibre.android.database.models.Person;
import com.ekylibre.android.database.models.Phyto;
import com.ekylibre.android.database.models.Plot;
import com.ekylibre.android.database.models.Seed;
import com.ekylibre.android.database.pojos.Crops;
import com.ekylibre.android.database.pojos.Fertilizers;
import com.ekylibre.android.database.pojos.Interventions;
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
import com.ekylibre.android.type.InterventionInputInputObject;
import com.ekylibre.android.type.InterventionOperatorInputObject;
import com.ekylibre.android.type.InterventionToolInputObject;
import com.ekylibre.android.type.InterventionTypeEnum;
import com.ekylibre.android.type.InterventionTargetInputObject;
import com.ekylibre.android.type.InterventionWorkingDayInputObject;

import com.ekylibre.android.type.OperatorRoles;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static helper methods.
 */
public class SyncService extends IntentService {

    private static final String TAG = "SyncService";

    public static final int DONE = 10;
    public static final int FAILED = 11;

    public static final String ACTION_SYNC_PULL = "com.ekylibre.android.services.action.SYNC_PULL";
    public static final String ACTION_SYNC_PUSH = "com.ekylibre.android.services.action.SYNC_PUSH";
    public static final String ACTION_VERIFY_TOKEN = "com.ekylibre.android.services.action.VERIFY_TOKEN";

//    // TODO: Rename parameters
//    private static final String EXTRA_PARAM1 = "com.ekylibre.android.services.extra.PARAM1";
//    private static final String EXTRA_PARAM2 = "com.ekylibre.android.services.extra.PARAM2";

    private static String ACCESS_TOKEN;
    private static SharedPreferences sharedPreferences;
    private static AppDatabase database;


    public SyncService() {
        super("SyncService");
    }

//    public static void startActionVerifyToken(Context context, String param1, String param2) {
//        Intent intent = new Intent(context, SyncService.class);
//        intent.setAction(ACTION_VERIFY_TOKEN);
//        intent.putExtra(EXTRA_PARAM1, param1);
//        intent.putExtra(EXTRA_PARAM2, param2);
//        intent.putExtra("receiverTag", new SyncResultReceiver(new Handler()));
//        context.startService(intent);
//    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            ProviderInstaller.installIfNeeded(this);
        } catch (GooglePlayServicesRepairableException e) {
            Log.e(TAG, "GooglePlayServicesRepairableException");
            GoogleApiAvailability.getInstance().showErrorNotification(this, e.getConnectionStatusCode());
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.e(TAG, "GooglePlayServicesNotAvailableException");
        }

        final ResultReceiver receiver = intent.getParcelableExtra("receiver");

        sharedPreferences = this.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        ACCESS_TOKEN = sharedPreferences.getString("access_token", null);

        final String action = intent.getAction();

        if (ACTION_SYNC_PULL.equals(action))
            handleActionSyncPull(receiver);

        else if (ACTION_VERIFY_TOKEN.equals(action))
            handleActionVerifyToken();

    }


    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSyncPull(ResultReceiver receiver) {

        database = AppDatabase.getInstance(this);

        List<Integer> interventionEkyIdList = database.dao().interventionsEkiIdList();
        Log.i(TAG, "interventionEkyIdList " + interventionEkyIdList.toString());

//        List<Integer> phytoEkyIdList = database.dao().phytoEkiIdList();
//        List<Integer> seedEkyIdList = database.dao().seedEkiIdList();
//        List<Integer> fertilizerEkyIdList = database.dao().fertilizerEkiIdList();

        // We always get the full article list from server
        List<Integer> phytoEkyIdList = new ArrayList<>();
        List<Integer> seedEkyIdList = new ArrayList<>();
        List<Integer> fertilizerEkyIdList = new ArrayList<>();

        Log.e(TAG, "phytoEkyIdList " + phytoEkyIdList + " seedEkyIdList " + seedEkyIdList + " fertilizerEkyIdList " + fertilizerEkyIdList);

        ApolloClient apolloClient = GraphQLClient.getApolloClient(ACCESS_TOKEN);

        apolloClient.query(PullQuery.builder().build())
                .enqueue(new ApolloCall.Callback<PullQuery.Data>() {
            @Override
            public void onResponse(@Nonnull Response<PullQuery.Data> response) {

                PullQuery.Data data = response.data();

                if (data != null && data.farms() != null) {

                    Log.i(TAG, "Nombre de fermes: " + data.farms().size());

                    // TODO: Farms selector
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

                    if (!farm.people().isEmpty()) {
                        Log.i(TAG, "Fetching people...");

                        for (PullQuery.person person : farm.people()) {

                            Log.e(TAG, String.format("[Person] id: %s name: %s", person.id(), person.lastName()));

                            // Save or update Person
                            if (!database.dao().personEkiIdList().contains(person.id())) {
                                Log.i(TAG, "save person");
                                String firstName = (person.firstName().isEmpty()) ? person.firstName() : null;
                                        database.dao().insert(new Person(Integer.valueOf(person.id()), firstName, person.lastName()));
                            }
                            else
                                database.dao().updatePerson(person.firstName(), person.lastName(), person.id());
                        }
                        // TODO: delete person if deleted on server --> or mark status deleted ?
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

                            // TODO: materials not yet implemented
//                            if (article.type() == ArticleType.MATERIAL) {
//
//                            }
                        }
                    }

                    // Processing interventions
                    if (!farm.interventions().isEmpty()) {

                        int index = 0;
                        List<Integer> remoteInterventionList = new ArrayList<>();

                        for (PullQuery.Intervention inter : farm.interventions()) {

                            remoteInterventionList.add(Integer.valueOf(inter.id()));

                            if (!interventionEkyIdList.contains(Integer.valueOf(inter.id()))) {

                                Log.e(TAG, "Save new intervention");
                                // Save main intervention
                                Intervention newInter = new Intervention();
                                newInter.setEky_id(Integer.valueOf(inter.id()));
                                newInter.setType(inter.type().toString());
                                if (inter.waterQuantity() != null) {
                                    newInter.setWater_quantity((int) (long) inter.waterQuantity());
                                    newInter.setWater_unit(inter.waterUnit().toString());
                                }
                                newInter.setFarm(farm.id());
                                newInter.setStatus("sync");

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
                                    Log.i(TAG, "operator_id " + operator.person().id());
                                    boolean role = operator.role() == OperatorRoles.DRIVER;
                                    int personId = database.dao().getPersonId(Integer.valueOf(operator.person().id()));
                                    database.dao().insert(new InterventionPerson(newInterId, personId, role));
                                }

                                // Saving Inputs
                                for (PullQuery.Input input : farm.interventions().get(index).inputs()) {

                                    if (phytoEkyIdList.contains(Integer.valueOf(input.articleId()))) {
                                        // Phytosanitary products
                                        Integer phytoId = database.dao().getPhytoId(Integer.valueOf(input.articleId()));
                                        Log.e(TAG, "input_id --> " + input.id() + " inter_id " + newInterId + " phyto_id " + phytoId);
                                        InterventionPhytosanitary interventionPhyto =
                                                new InterventionPhytosanitary(input.quantityValue(), input.unit().toString(), newInterId, phytoId);
                                        database.dao().insert(interventionPhyto);
                                    }

                                    // Seeds
                                    if (seedEkyIdList.contains(Integer.valueOf(input.articleId()))) {
                                        Log.e(TAG, "input_id --> " + input.id());
                                        Integer seedId = database.dao().getSeedId(Integer.valueOf(input.articleId()));
                                        InterventionSeed interventionSeed =
                                                new InterventionSeed(input.quantityValue(), input.unit().toString(), newInterId, seedId);
                                        database.dao().insert(interventionSeed);
                                    }
                                    // Fertilizers
                                    if (fertilizerEkyIdList.contains(Integer.valueOf(input.articleId()))) {
                                        Log.e(TAG, "input_id --> " + input.id());
                                        Integer fertiId = database.dao().getFertilizerId(Integer.valueOf(input.articleId()));
                                        InterventionFertilizer interventionFertilizer =
                                                new InterventionFertilizer(input.quantityValue(), input.unit().toString(), newInterId, fertiId);
                                        database.dao().insert(interventionFertilizer);
                                    }
                                }

                                // Saving Equipments
                                // TODO missing equipment.id for now
//                                for (PullQuery.Tool tool : farm.interventions().get(index).tools()) {
//                                    database.dao().insert(new InterventionEquipment(newInterId, tool.equipment().id()));
//                                }
                            }
                            ++index;
                        }

                        Log.e(TAG, "local interventions ids --> " + interventionEkyIdList.toString());
                        Log.e(TAG, "remote interventions ids --> " + remoteInterventionList.toString());

                        // Delets deleted interventions
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

        Log.e(TAG, interventions.toString());

        ApolloClient apolloClient = GraphQLClient.getApolloClient(ACCESS_TOKEN);

        for (Interventions inter : interventions) {

            List<InterventionTargetInputObject> targets = new ArrayList<>();
            List<InterventionWorkingDayInputObject> workingDays = new ArrayList<>();
            List<InterventionInputInputObject> inputs = new ArrayList<>();
            List<InterventionOperatorInputObject> operators = new ArrayList<>();
            List<InterventionToolInputObject> tools = new ArrayList<>();
            // TODO InterventionOutputsInputObject

            for (Crops crop : inter.crops) {
                targets.add(InterventionTargetInputObject.builder()
                        .cropID(crop.inter.crop_id)
                        .workAreaPercentage(crop.inter.work_area_percentage)
                        .build());
            }

            for (InterventionWorkingDay wd : inter.workingDays) {
                workingDays.add(InterventionWorkingDayInputObject.builder()
                        .executionDate(wd.execution_date)
                        .hourDuration((long) wd.hour_duration)
                        .build());
            }

//            for (Operato operator : inter.op) {
//                workingDays.add(InterventionWorkingDayInputObject.builder()
//                        .executionDate(wd.execution_date)
//                        .hourDuration((long) wd.hour_duration)
//                        .build());
//            }

            for (Phytos phyto : inter.phytos) {
                if (phyto.phyto.get(0).eky_id == null) {
                    inputs.add(InterventionInputInputObject.builder()
                            // TODO warning ! may be one maaid for several products
                            .marketingAuthorizationNumber(phyto.phyto.get(0).maaid)
                            //.article(ArticleInputObject.builder()
                            //        .referenceID(phyto.phyto.get(0).maaid)
                            //        .type(ArticleType.PHYTOSANITARY).build())
                            .quantity(phyto.inter.quantity)
                            .unit(ArticleAllUnit.safeValueOf(phyto.inter.unit))
                            .build());
                } else {
                    inputs.add(InterventionInputInputObject.builder()
                            .article(ArticleInputObject.builder()
                                    .id(String.valueOf(phyto.phyto.get(0).eky_id)).build())
                            .quantity(phyto.inter.quantity)
                            .unit(ArticleAllUnit.safeValueOf(phyto.inter.unit))
                            .build());
                }
            }

            for (Seeds seed : inter.seeds) {
                if (seed.seed.get(0).eky_id == null) {
                    inputs.add(InterventionInputInputObject.builder()
                            //.marketingAuthorizationNumber(String.valueOf(seed.seed.get(0).id))
                            .article(ArticleInputObject.builder()
                                    .referenceID(String.valueOf(seed.seed.get(0).id))
                                    .type(ArticleType.SEED).build())
                            .quantity(seed.inter.quantity)
                            .unit(ArticleAllUnit.safeValueOf(seed.inter.unit))
                            .build());
                } else {
                    inputs.add(InterventionInputInputObject.builder()
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
                    inputs.add(InterventionInputInputObject.builder()
                            //.marketingAuthorizationNumber(String.valueOf(seed.seed.get(0).id))
                            .article(ArticleInputObject.builder()
                                    .referenceID(String.valueOf(fertilizer.fertilizer.get(0).id))
                                    .type(ArticleType.FERTILIZER).build())
                            .quantity(fertilizer.inter.quantity)
                            .unit(ArticleAllUnit.safeValueOf(fertilizer.inter.unit))
                            .build());
                } else {
                    inputs.add(InterventionInputInputObject.builder()
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
                    .waterQuantity((inter.intervention.water_quantity != null) ? (long) inter.intervention.water_quantity : null)
                    .waterUnit((inter.intervention.water_unit != null) ? ArticleVolumeUnit.safeValueOf(inter.intervention.water_unit) : null)
                    .build();

            apolloClient.mutate(pushIntervention).enqueue(new ApolloCall.Callback<PushInterventionMutation.Data>() {
                @Override
                public void onResponse(@Nonnull Response<PushInterventionMutation.Data> response) {
                    if (!response.hasErrors()) {
                        PushInterventionMutation.CreateInterventionMutation mutation = response.data().createInterventionMutation();
                        if (mutation.errors() == null)
                            database.dao().setInterventionEkyId(inter.intervention.id, Integer.valueOf(mutation.intervention().id()));
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
