package com.efraespada.motiondetector;

import android.location.Location;

/**
 * Created by efraespada on 16/07/2017.
 */

public interface Listener {

    void locationChanged(Location location);

    void accelerationChanged(float acceleration);

    void locatedStep();

    void notLocatedStep();

    void type(String type);

}
