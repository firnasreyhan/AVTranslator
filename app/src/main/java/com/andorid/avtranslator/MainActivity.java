package com.andorid.avtranslator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.andorid.avtranslator.api.Api;
import com.andorid.avtranslator.api.ApiClient;
import com.andorid.avtranslator.api.response.LanguageResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private Api api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        api = ApiClient.getClient();
        api.getLanguage().enqueue(new Callback<LanguageResponse>() {
            @Override
            public void onResponse(Call<LanguageResponse> call, Response<LanguageResponse> response) {
                if (!response.body().error) {
                    Log.e("getLanguage", "Success");
                }
            }

            @Override
            public void onFailure(Call<LanguageResponse> call, Throwable t) {
                Log.e("getLanguage", t.getMessage());
            }
        });
    }
}
