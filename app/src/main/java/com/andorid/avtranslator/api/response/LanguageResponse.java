package com.andorid.avtranslator.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class LanguageResponse extends BaseResponse {
    @SerializedName("data")
    public ArrayList<LanguageModel> data;

    public static class LanguageModel {
        @SerializedName("code")
        public String code;

        @SerializedName("country")
        public String country;

        @Override
        public String toString() {
            return country;
        }
    }
}
