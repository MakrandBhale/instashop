package com.makarand.instashop.Helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.makarand.instashop.Constants;

public class LocalStorage {
    private SharedPreferences.Editor editor;
    private  SharedPreferences sharedPreferences;
    private static LocalStorage localStorage;

    public static LocalStorage getInstance() {
        if(localStorage == null)
            localStorage = new LocalStorage();
        return localStorage;
    }



    public void storeObject(String key, Object object, Context context){
        sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREF_ID, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        String json = new Gson().toJson(object);
        editor.putString(key, json);
        editor.apply();
    }

    public <T> T getStoredObject(String key, Class<T> type, Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREF_ID, Context.MODE_PRIVATE);
        String jsonString = sharedPreferences.getString(key, null);
        if(jsonString == null) return null;
        return new Gson().fromJson(jsonString, type);
    }
}
