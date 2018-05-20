package com.rotor.chappy.model;

import java.util.HashMap;
import java.util.Map;

public class RelationView<T> {

    private String path;

    private Map<BasePresenter, ReferenceView<T>> map;

    public RelationView(String path) {
        this.path = path;
        this.map = new HashMap<>();
    }

    public void addView(BasePresenter presenter, ReferenceView<T> view) {
        map.put(presenter, view);
    }

    public ReferenceView<T> activeView() {
        ReferenceView<T> activeView = null;
        for (Map.Entry<BasePresenter, ReferenceView<T>> entry : map.entrySet()) {
            if (entry.getKey().isVisible()) {
                activeView = entry.getValue();
                break;
            }
        }
        return activeView;
    }
}
