package com.rotor.chappy.services;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.rotor.chappy.App;
import com.rotor.chappy.model.Location;
import com.rotor.chappy.model.User;
import com.rotor.chappy.model.mpv.BasePresenter;
import com.rotor.chappy.model.mpv.MapReferenceView;
import com.rotor.chappy.model.mpv.ProfilePresenter;
import com.rotor.chappy.model.mpv.ProfileView;
import com.rotor.chappy.model.mpv.ProfilesView;
import com.rotor.chappy.model.mpv.ReferenceView;
import com.rotor.chappy.model.mpv.RelationProfilesView;
import com.rotor.chappy.model.mpv.RelationView;
import com.rotor.database.Database;
import com.rotor.database.abstr.Reference;

import java.util.HashMap;
import java.util.Map;

public class ProfileRepository {

    private static final Map<String, User> users = new HashMap<>();
    private static final Map<String, RelationProfilesView> map = new HashMap<>();

    public ProfileRepository() {
        // nothing to do here
    }


    public void listen(final String path, final ProfilePresenter presenter, final ProfilesView referenceView) {

        if (map.containsKey(path)) {
            RelationProfilesView relationView = map.get(path);
            relationView.addView(presenter, referenceView);
            map.put(path, relationView);
        } else {
            RelationProfilesView relationView = new RelationProfilesView(path);
            relationView.addView(presenter, referenceView);
            map.put(path, relationView);
        }

        implListen(path);
    }

    public <T> void listen(final String path, final ProfilePresenter presenter, final ProfileView referenceView) {

        if (map.containsKey(path)) {
            RelationProfilesView relationView = map.get(path);
            relationView.addView(presenter, referenceView);
            map.put(path, relationView);
        } else {
            RelationProfilesView relationView = new RelationProfilesView(path);
            relationView.addView(presenter, referenceView);
            map.put(path, relationView);
        }

        implListen(path);
    }

    private void implListen(final String path) {
        Database.listen(App.databaseName, path, new Reference<User>(User.class) {

            @Override
            public void onCreate() {
                RelationProfilesView rV = map.get(path);
                ProfileView rF = rV.activeView();
                if (rF != null) {
                    rF.onCreateUser();
                }
                ProfilesView rFM = rV.activeMapView();
                if (rFM != null) {
                    rFM.onCreateUser(path);
                }
            }

            @Override
            public void onChanged(@NonNull User user) {
                users.put("/users/" + user.getUid(), user);
                RelationProfilesView rV = map.get(path);
                ProfileView rF = rV.activeView();
                if (rF != null) {
                    rF.onUserChanged(user);
                }
                ProfilesView rFM = rV.activeMapView();
                if (rFM != null) {
                    rFM.onUserChanged(path, user);
                }
            }


            @Nullable
            @Override
            public User onUpdate() {
                RelationProfilesView rV = map.get(path);
                ProfileView rF = rV.activeView();
                if (rF != null) {
                    return rF.onUpdateUser();
                } else {
                    ProfilesView rFM = rV.activeMapView();
                    if (rFM != null) {
                        return rFM.onUpdateUser(path);
                    } else {
                        return users.get(path);
                    }
                }
            }

            @Override
            public void onDestroy() {
                RelationProfilesView rV = map.get(path);
                ProfileView rF = rV.activeView();
                if (rF != null) {
                    rF.onDestroyUser();
                }

                ProfilesView rFM = rV.activeMapView();
                if (rFM != null) {
                    rFM.onDestroyUser(path);
                }
            }

            @Override
            public void progress(int i) {
                RelationProfilesView rV = map.get(path);
                ProfileView rF = rV.activeView();
                if (rF != null) {
                    rF.userProgress(i);
                }
                ProfilesView rFM = rV.activeMapView();
                if (rFM != null) {
                    rFM.userProgress(path, i);
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

    public static void addLocationTo(String id, Location location) {
        users.get(id).getLocations().put(location.getId(), location);
        Database.sync(id);
    }

    public static User getUser(String id) {
        if (map.containsKey(id)) {
            RelationProfilesView rV = map.get(id);
            ProfileView rF = rV.activeView();
            if (rF != null) {
                return rF.onUpdateUser();
            } else {
                ProfilesView rFM = rV.activeMapView();
                if (rFM != null) {
                    return rFM.onUpdateUser(id);
                } else {
                    return users.get(id);
                }
            }
        } else return null;
    }

    public static void setUser(String id, User user) {
        users.put(id, user);
        if (map.containsKey(id)) {
            RelationProfilesView rV = map.get(id);
            ProfileView rF = rV.activeView();
            if (rF != null) {
                rF.onUserChanged(user);
            } else {
                ProfilesView rFM = rV.activeMapView();
                if (rFM != null) {
                    rFM.onUserChanged(id, user);
                }
            }
        }
    }

}
