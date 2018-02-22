package com.flamebase.database.model;

import org.json.JSONObject;

/**
 * Created by efraespada on 03/01/2018.
 */

public interface CallbackIO {

    void received(JSONObject jsonObject);
}
