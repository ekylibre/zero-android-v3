package com.ekylibre.android.network;


import androidx.annotation.NonNull;

import com.apollographql.apollo.ApolloClient;
import com.ekylibre.android.BuildConfig;
import com.ekylibre.android.network.helpers.ApolloAdapters;
import com.ekylibre.android.type.CustomType;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import timber.log.Timber;


public class GraphQLClient {

    private static final int TIME_OUT = 120000;  // 30 sec timout !
    private static OkHttpClient okHttpClient;

    // get the instance of apollo client with all the headers and correct url
    public static ApolloClient getApolloClient(String authToken) {

        if (okHttpClient == null) {
            okHttpClient = getOkHttpClient(authToken);
        }

        return ApolloClient.builder()
                .serverUrl(BuildConfig.API_URL + "/v1/graphql")
                .okHttpClient(okHttpClient)
                .addCustomTypeAdapter(CustomType.DATE, ApolloAdapters.customDateAdapter)
                .addCustomTypeAdapter(CustomType.POLYGON, ApolloAdapters.customPolygonAdapter)
                .addCustomTypeAdapter(CustomType.POINT, ApolloAdapters.customPointAdapter)
                .build();
    }

    private static OkHttpClient getOkHttpClient(String authToken) {

//        ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
//                .tlsVersions(TlsVersion.TLS_1_2)
//                .cipherSuites(
//                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
//                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
//                        CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
//                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
//                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
//                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
//                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
//                        CipherSuite.TLS_ECDHE_ECDSA_WITH_RC4_128_SHA,
//                        CipherSuite.TLS_ECDHE_RSA_WITH_RC4_128_SHA,
//                        CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
//                        CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA,
//                        CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA)
//                .build();

        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        // set the timeouts
        builder.connectTimeout(TIME_OUT, TimeUnit.MILLISECONDS);
        builder.readTimeout(TIME_OUT, TimeUnit.MILLISECONDS);
        builder.writeTimeout(TIME_OUT, TimeUnit.MILLISECONDS);

        addLoggingInterceptor(builder);
        builder.addInterceptor(new RequestInterceptor(authToken));

        //builder.connectionSpecs(Collections.singletonList(spec));

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
            Timber.d("Bearer %s", authToken);
            Request request = chain.request();
            Request.Builder requestBuilder = request.newBuilder();
            requestBuilder.addHeader("Authorization", "Bearer " + authToken);

            return chain.proceed(requestBuilder.build());
        }
    }
}
