package com.nightxstudio.omniconverter.Retrofit;

import com.google.gson.JsonObject;
import com.nightxstudio.omniconverter.MainActivity;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RetrofitInterface {
    @GET("{currency}")
    Call<JsonObject> getExchangeCurrency(@Path("currency") String currency);
}
