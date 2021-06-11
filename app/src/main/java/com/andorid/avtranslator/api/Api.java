package com.andorid.avtranslator.api;

import com.andorid.avtranslator.api.response.LanguageResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface Api {
    @GET("show_language.php")
    Call<LanguageResponse> getLanguage();
}
