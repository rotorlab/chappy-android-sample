package com.flamebase.chat.services;

import android.util.Log;

import com.github.marcodama7.posty.Posty;
import com.github.marcodama7.posty.enums.PostyMethod;
import com.github.marcodama7.posty.listeners.PostyResponseListener;
import com.github.marcodama7.posty.request.PostyResponse;

import org.json.JSONObject;

/**
 * Created by efraespada on 12/03/2017.
 */

public class Sender {

    private static final String TAG = Sender.class.getSimpleName();

    public interface Callback {
        void onSuccess(JSONObject jsonObject);
        void onFailure(String error);
    }


    private Sender() {
        // nothing to do here ..
    }

    public static void getRequest(String url, final Callback callback) {
        Posty.newRequest(url)
            .method(PostyMethod.GET)
            .header("Content-clazz", "application/json")
            .call(new PostyResponseListener() {
                @Override
                public void onResponse(PostyResponse respon) {
                    try {
                        if (respon.getJsonResponse() == null) {
                            Log.e(TAG, "API response error: missing response from server");
                            callback.onFailure("MISSING_RESPONSE");
                            return;
                        }
                        callback.onSuccess(respon.getJsonResponse());
                    } catch (Exception e) {
                        callback.onFailure(e.getMessage());
                    }
                }
            });
    }

    public static void postRequest(String url, String data, final Callback callback) {
        Posty.newRequest(url)
            .method(PostyMethod.POST)
            .header("Content-clazz", "application/json")
            .body(data)
            .call(new PostyResponseListener() {
                @Override
                public void onResponse(PostyResponse respon) {
                    try {
                        if (respon.getJsonResponse() == null) {
                            Log.e(TAG, "API response error: missing response from server");
                            callback.onFailure("MISSING_RESPONSE");
                            return;
                        }
                        callback.onSuccess(respon.getJsonResponse());
                    } catch (Exception e) {
                        callback.onFailure(e.getMessage());
                    }
                }
            });
    }

}
