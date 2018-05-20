package com.rotor.chappy.services;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.rotor.chappy.App;
import com.rotor.chappy.activities.login.LoginGoogleInterface;
import com.rotor.chappy.model.BasePresenter;
import com.rotor.chappy.model.ReferenceView;
import com.rotor.chappy.model.RelationView;
import com.rotor.chappy.model.User;
import com.rotor.database.Database;
import com.rotor.database.abstr.Reference;

import java.util.HashMap;
import java.util.Map;

public class Data {

    private static User user;
    private static final Map<String, RelationView> map = new HashMap<>();

    public Data() {

    }


    public <T> void listen(final String path, final BasePresenter presenter, final ReferenceView<T> referenceView, Class<T> clazz) {

        if (map.containsKey(path)) {
            RelationView<T> relationView = map.get(path);
            relationView.addView(presenter, referenceView);
            map.put(path, relationView);
        } else {
            RelationView<T> relationView = new RelationView<>(path);
            relationView.addView(presenter, referenceView);
            map.put(path, relationView);
        }

        Database.listen(App.databaseName, path, new Reference<T>(clazz) {
            @Override
            public void onCreate() {
                RelationView<T> rV = map.get(path);
                ReferenceView<T> rF = rV.activeView();
                if (rF != null) {
                    rF.onCreateReference();
                }
            }

            @Override
            public void onChanged(@NonNull T value) {
                RelationView<T> rV = map.get(path);
                ReferenceView<T> rF = rV.activeView();
                if (rF != null) {
                    rF.onReferenceChanged(value);
                }
            }

            @Nullable
            @Override
            public T onUpdate() {
                RelationView<T> rV = map.get(path);
                ReferenceView<T> rF = rV.activeView();
                if (rF != null) {
                    return rF.onUpdateReference();
                } else {
                    return null;
                }
            }

            @Override
            public void onDestroy() {
                RelationView<T> rV = map.get(path);
                ReferenceView<T> rF = rV.activeView();
                rF.onDestroyReference();
            }

            @Override
            public void progress(int i) {
                RelationView<T> rV = map.get(path);
                ReferenceView<T> rF = rV.activeView();
                rF.progress(i);
            }
        });
    }

    public void sync(String id) {
        Database.sync(id);
    }

    public void remove(String id) {
        Database.remove(id);
    }

    public static void defineUser(User user) {
        Data.user = user;
    }

    public static User getUser() {
        return user;
    }
}
