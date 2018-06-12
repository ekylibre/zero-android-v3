package com.ekylibre.android.network;

import com.ekylibre.android.network.pojos.AccessToken;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;


public interface EkylibreAPI {

    @FormUrlEncoded
    @POST("/oauth/token")
    Call<AccessToken> getNewAccessToken(
            @Field("client_id") String clientId,
            @Field("client_secret") String clientSecret,
            @Field("grant_type") String grantType,
            @Field("username") String username,
            @Field("password") String password,
            @Field("scope") String scope);
    //@Field("code") String code,

    @FormUrlEncoded
    @POST("/oauth/token")
    Call<AccessToken> getRefreshAccessToken(
            @Field("client_id") String clientId,
            @Field("client_secret") String clientSecret,
            @Field("refresh_token") String refreshToken,
            @Field("grant_type") String grantType);

}
