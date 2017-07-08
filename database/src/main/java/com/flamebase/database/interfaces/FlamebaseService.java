package com.flamebase.database.interfaces;

import com.flamebase.database.model.request.Sync;
import com.flamebase.database.model.request.UpdateFromServer;
import com.flamebase.database.model.request.UpdateToServer;
import com.flamebase.database.model.service.SyncResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by efraespada on 08/07/2017.
 */

public interface FlamebaseService {

    @Headers("Content-Type: application/json")
    @POST("/")
    Call<SyncResponse> sync(@Body Sync sync);

    @Headers("Content-Type: application/json")
    @POST("/")
    Call<SyncResponse> refreshFromServer(@Body UpdateFromServer updateFromServer);

    @Headers("Content-Type: application/json")
    @POST("/")
    Call<SyncResponse> refreshToServer(@Body UpdateToServer updateToServer);
}