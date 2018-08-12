package com.rotor.chappy.model.map;

import com.google.maps.android.clustering.ClusterItem;

public interface RClusterItem extends ClusterItem {

    boolean shouldBeOutOfCluster();

}
