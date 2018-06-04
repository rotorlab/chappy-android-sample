package com.rotor.chappy.model.mpv;

import java.util.HashMap;
import java.util.Map;

public class RelationProfilesView {

    private String path;

    private Map<ProfilePresenter, ProfileView> map;

    private Map<ProfilePresenter, ProfilesView> mapMap;

    public RelationProfilesView(String path) {
        this.path = path;
        this.map = new HashMap<>();
        this.mapMap = new HashMap<>();
    }

    public void addView(ProfilePresenter presenter, ProfileView view) {
        map.put(presenter, view);
    }

    public void addView(ProfilePresenter presenter, ProfilesView view) {
        mapMap.put(presenter, view);
    }

    public ProfileView activeView() {
        ProfileView activeView = null;
        for (Map.Entry<ProfilePresenter, ProfileView> entry : map.entrySet()) {
            if (entry.getKey().isVisible()) {
                activeView = entry.getValue();
                break;
            }
        }
        return activeView;
    }

    public ProfilesView activeMapView() {
        ProfilesView activeView = null;
        for (Map.Entry<ProfilePresenter, ProfilesView> entry : mapMap.entrySet()) {
            if (entry.getKey().isVisible()) {
                activeView = entry.getValue();
                break;
            }
        }
        return activeView;
    }
}
