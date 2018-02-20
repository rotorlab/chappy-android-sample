package com.flamebase.database.interfaces;

import com.google.gson.JsonObject;

/**
 * Created by efrainespada on 20/02/2018.
 */

public interface FlamebaseResponse {
    void onSuccess(JsonObject jsonObject);
    void onFailure(String error);
}