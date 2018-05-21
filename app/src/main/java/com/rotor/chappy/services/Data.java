package com.rotor.chappy.services;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.rotor.chappy.App;
import com.rotor.chappy.model.mpv.BasePresenter;
import com.rotor.chappy.model.mpv.MapReferenceView;
import com.rotor.chappy.model.mpv.ReferenceView;
import com.rotor.chappy.model.mpv.RelationView;
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


    public <T> void listen(final String path, final BasePresenter presenter, final MapReferenceView<T> referenceView, Class<T> clazz) {

        if (map.containsKey(path)) {
            RelationView<T> relationView = map.get(path);
            relationView.addView(presenter, referenceView);
            map.put(path, relationView);
        } else {
            RelationView<T> relationView = new RelationView<>(path);
            relationView.addView(presenter, referenceView);
            map.put(path, relationView);
        }

        implListen(path, clazz);
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

        implListen(path, clazz);
    }

    private <T> void implListen(final String path, Class<T> clazz) {
        Database.listen(App.databaseName, path, new Reference<T>(clazz) {
            @Override
            public void onCreate() {
                RelationView<T> rV = map.get(path);
                ReferenceView<T> rF = rV.activeView();
                if (rF != null) {
                    rF.onCreateReference();
                }
                MapReferenceView<T> rFM = rV.activeMapView();
                if (rFM != null) {
                    rFM.onCreateReference(path);
                }
            }

            @Override
            public void onChanged(@NonNull T value) {
                RelationView<T> rV = map.get(path);
                ReferenceView<T> rF = rV.activeView();
                if (rF != null) {
                    rF.onReferenceChanged(value);
                }
                MapReferenceView<T> rFM = rV.activeMapView();
                if (rFM != null) {
                    rFM.onReferenceChanged(path, value);
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
                    MapReferenceView<T> rFM = rV.activeMapView();
                    if (rFM != null) {
                        return rFM.onUpdateReference(path);
                    } else {
                        return null;
                    }
                }
            }

            @Override
            public void onDestroy() {
                RelationView<T> rV = map.get(path);
                ReferenceView<T> rF = rV.activeView();
                if (rF != null) {
                    rF.onDestroyReference();
                }

                MapReferenceView<T> rFM = rV.activeMapView();
                if (rFM != null) {
                    rFM.onDestroyReference(path);
                }
            }

            @Override
            public void progress(int i) {
                RelationView<T> rV = map.get(path);
                ReferenceView<T> rF = rV.activeView();
                if (rF != null) {
                    rF.progress(i);
                }
                MapReferenceView<T> rFM = rV.activeMapView();
                if (rFM != null) {
                    rFM.progress(path, i);
                }
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
