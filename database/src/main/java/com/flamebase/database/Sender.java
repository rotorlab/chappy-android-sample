package com.flamebase.database;

import com.google.gson.JsonObject;

/**
 * Created by efraespada on 12/03/2017.
 */

public class Sender {

    private static final String TAG = Sender.class.getSimpleName();

    public interface FlamebaseResponse {
        void onSuccess(JsonObject jsonObject);
        void onFailure(String error);
    }


    private Sender() {
        // nothing to do here ..
    }

}
