package com.ekylibre.android.network;


import android.support.annotation.NonNull;

import com.apollographql.apollo.ApolloClient;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;


public class GraphQLClient {

    private static final int TIME_OUT = 5000;
    private static final String BASE_URL = "https://ekylibre-test.com/v1/graphql";
    private static OkHttpClient okHttpClient;

    // get the instance of apollo client with all the headers and correct url
    public static ApolloClient getApolloClient(String authToken) {

        if (okHttpClient == null) {
            okHttpClient = getOkHttpClient(authToken);
        }

        return ApolloClient.builder()
                .serverUrl(BASE_URL)
                .okHttpClient(okHttpClient)
                .build();
    }

    private static OkHttpClient getOkHttpClient(String authToken) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        // set the timeouts
        builder.connectTimeout(TIME_OUT, TimeUnit.MILLISECONDS);
        builder.readTimeout(TIME_OUT, TimeUnit.MILLISECONDS);
        builder.writeTimeout(TIME_OUT, TimeUnit.MILLISECONDS);

        addLoggingInterceptor(builder);
        builder.addInterceptor(new RequestInterceptor(authToken));

        return builder.build();
    }

    private static void addLoggingInterceptor(OkHttpClient.Builder builder) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(loggingInterceptor);
    }

    private static class RequestInterceptor implements Interceptor {

        private String authToken;

        public RequestInterceptor(String authToken) {
            this.authToken = authToken;
        }

        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request request = chain.request();
            Request.Builder requestBuilder = request.newBuilder();
            requestBuilder.addHeader("Authorization", "Bearer " + authToken);


            return chain.proceed(requestBuilder.build());
        }
    }
}
