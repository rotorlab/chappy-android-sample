package com.flamebase.chat.services;


import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by efraespada on 19/02/2017.
 */

public class FIService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
    }
}
