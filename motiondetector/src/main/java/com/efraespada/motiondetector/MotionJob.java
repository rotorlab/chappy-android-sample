package com.efraespada.motiondetector;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;

import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.rotor.core.RJob;
import com.rotor.core.Rotor;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.SENSOR_SERVICE;

public class MotionJob implements SensorEventListener, RJob {

    private static final String TAG = MotionJob.class.getSimpleName();

    public static final String SIT = "sit";
    public static final String WALK = "walking";
    public static final String JOGGING = "jogging";
    public static final String RUN = "running";
    public static final String BIKE = "biking";
    public static final String CAR = "car";
    public static final String MOTO = "moto";
    public static final String METRO = "metro";
    public static final String PLANE = "plane";

    private static final String[] ORDER = new String[]{SIT, WALK, JOGGING, RUN, BIKE, METRO, CAR, MOTO, PLANE};

    private static Date lastTypeTime;
    private static final long maxInterval = 5 * 60 * 1000; // 5 min - millis

    private static MotionJob.Properties SIT_PROPERTIES;
    private static MotionJob.Properties WALK_PROPERTIES;
    private static MotionJob.Properties JOGGING_PROPERTIES;
    private static MotionJob.Properties RUN_PROPERTIES;
    private static MotionJob.Properties BIKE_PROPERTIES;
    private static MotionJob.Properties CAR_PROPERTIES;
    private static MotionJob.Properties MOTO_PROPERTIES;
    private static MotionJob.Properties METRO_PROPERTIES;
    private static MotionJob.Properties PLANE_PROPERTIES;

    @Override
    public void onCreate() {
        MotionDetector.sensorMan = (SensorManager) Rotor.getContext().getSystemService(SENSOR_SERVICE);
        MotionDetector.accelerometer = MotionDetector.sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        MotionDetector.mAccel = 0.00f;
        MotionDetector.accelerationTimes = 0;
        MotionDetector.mAccelCurrent = SensorManager.GRAVITY_EARTH;
        MotionDetector.mAccelLast = SensorManager.GRAVITY_EARTH;

        SIT_PROPERTIES = new MotionJob.Properties(0, 0.5f, 1, 2, 1.4f);
        WALK_PROPERTIES = new MotionJob.Properties(0.5001f, 4.99f, 3, 4, 1.4f);
        JOGGING_PROPERTIES = new MotionJob.Properties(5, 9.99f, 5, 6, 1.85f);
        RUN_PROPERTIES = new MotionJob.Properties(10, 19.99f, 7, 8, 3.47f);
        BIKE_PROPERTIES = new MotionJob.Properties(10, 29.99f, 8, 9, 2.7f);
        CAR_PROPERTIES = new MotionJob.Properties(10, 249.99f, 9, 15, 2);
        MOTO_PROPERTIES = new MotionJob.Properties(10, 249.99f, 18, 23, 1.4f);
        METRO_PROPERTIES = new MotionJob.Properties(10, 50, 2, 4, 1.4f);
        PLANE_PROPERTIES = new MotionJob.Properties(150, 1200, 60, 70, 1.4f);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            MotionDetector.mGravity = event.values.clone();

            float x = MotionDetector.mGravity[0];
            float y = MotionDetector.mGravity[1];
            float z = MotionDetector.mGravity[2];
            MotionDetector.mAccelLast = MotionDetector.mAccelCurrent;
            MotionDetector.mAccelCurrent = (float) Math.sqrt(x * x + y * y + z * z);
            float delta = MotionDetector.mAccelCurrent - MotionDetector.mAccelLast;
            MotionDetector.mAccel = MotionDetector.mAccel * 0.9f + delta;

            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
            symbols.setDecimalSeparator('.');
            DecimalFormat decimalFormat = new DecimalFormat("#.###", symbols);
            float twoDigitsF = Float.valueOf(decimalFormat.format(MotionDetector.mAccel));

            checkAcceleration(twoDigitsF);
            if (!MotionDetector.deviceIsMoving && twoDigitsF > 1) {
                startService();
                MotionDetector.deviceIsMoving = true;
                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        MotionDetector.deviceIsMoving = false;
                    }
                };
                handler.postDelayed(runnable, 3000);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void stopService() {
        if (MotionDetector.initialized) {
            LocationManager locationManager = (LocationManager) MotionDetector.context.getSystemService(Context.LOCATION_SERVICE);
            locationManager.removeUpdates(MotionDetector.locationListener);
            MotionDetector.initialized = false;
        }
    }

    public void startService() {
        if (!MotionDetector.initialized) {
            if (ContextCompat.checkSelfPermission(MotionDetector.context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MotionDetector.context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            MotionDetector.initialized = true;

            MotionDetector.locationManager = (LocationManager) MotionDetector.context.getSystemService(Context.LOCATION_SERVICE);
            MotionDetector.locationManager.requestLocationUpdates(MotionDetector.getProviderName(), MotionDetector.minTime, MotionDetector.minDistance, MotionDetector.locationListener);

            if (MotionDetector.currentLocation == null) {
                MotionDetector.currentLocation = MotionDetector.locationManager.getLastKnownLocation(MotionDetector.getProviderName());
            }
        }
    }

    private void checkAcceleration(float mAccel) {
        MotionDetector.listener.accelerationChanged(mAccel);
        if (Math.abs(mAccel) > 0.01) {
            if (mAccel > 0 && MotionDetector.isPositive) {
                MotionDetector.accelerationTimes++;
            } else if (mAccel > 0 && !MotionDetector.isPositive) {
                MotionDetector.isPositive = true;
                MotionDetector.accelerationTimes = 0;
                if (mAccel > 0.5 && MotionDetector.currentLocation != null && (MotionDetector.currentLocation.getSpeed() * 3.6f) <= RUN_PROPERTIES.getMaxSpeed() ) {
                    MotionDetector.listener.locatedStep();
                } else if (mAccel > 0.5) {
                    MotionDetector.listener.notLocatedStep();

                }
            } else if (mAccel < 0 && MotionDetector.isPositive) {
                MotionDetector.isPositive = false;
                MotionDetector.accelerationTimes = 0;
            } else if (mAccel < 0 && !MotionDetector.isPositive) {
                MotionDetector.accelerationTimes++;
            }

            if (MotionDetector.currentLocation != null) {
                if (MotionDetector.accelerationTimes >= SIT_PROPERTIES.getMinAccelerationTimes()
                        && MotionDetector.accelerationTimes <= SIT_PROPERTIES.getMaxAccelerationTimes()
                        && MotionDetector.currentLocation.getSpeed() * 3.6f < SIT_PROPERTIES.getMaxSpeed()
                        && MotionDetector.currentLocation.getSpeed() * 3.6f >= SIT_PROPERTIES.getMinSpeed()) {

                    if (priority(SIT)) {
                        MotionDetector.currentType = SIT;
                        lastTypeTime = new Date();
                        MotionDetector.listener.type(MotionDetector.currentType);
                    }

                } else if (MotionDetector.accelerationTimes >= WALK_PROPERTIES.getMinAccelerationTimes()
                        && MotionDetector.accelerationTimes <= WALK_PROPERTIES.getMaxAccelerationTimes()
                        && MotionDetector.currentLocation.getSpeed() * 3.6f < WALK_PROPERTIES.getMaxSpeed()
                        && MotionDetector.currentLocation.getSpeed() * 3.6f >= WALK_PROPERTIES.getMinSpeed()) {

                    if (priority(WALK)) {
                        MotionDetector.currentType = WALK;
                        lastTypeTime = new Date();
                        MotionDetector.listener.type(MotionDetector.currentType);
                    }

                } else if (MotionDetector.accelerationTimes >= JOGGING_PROPERTIES.getMinAccelerationTimes()
                        && MotionDetector.accelerationTimes <= JOGGING_PROPERTIES.getMaxAccelerationTimes()
                        && MotionDetector.currentLocation.getSpeed() * 3.6f < JOGGING_PROPERTIES.getMaxSpeed()
                        && MotionDetector.currentLocation.getSpeed() * 3.6f >= JOGGING_PROPERTIES.getMinSpeed()) {

                    if (priority(JOGGING)) {
                        MotionDetector.currentType = JOGGING;
                        lastTypeTime = new Date();
                        MotionDetector.listener.type(MotionDetector.currentType);
                    }

                } else if (MotionDetector.accelerationTimes >= RUN_PROPERTIES.getMinAccelerationTimes()
                        && MotionDetector.accelerationTimes <= RUN_PROPERTIES.getMaxAccelerationTimes()
                        && MotionDetector.currentLocation.getSpeed() * 3.6f < RUN_PROPERTIES.getMaxSpeed()
                        && MotionDetector.currentLocation.getSpeed() * 3.6f >= RUN_PROPERTIES.getMinSpeed()) {

                    if (priority(RUN)) {
                        MotionDetector.currentType = RUN;
                        lastTypeTime = new Date();
                        MotionDetector.listener.type(MotionDetector.currentType);
                    }

                } else if (MotionDetector.accelerationTimes >= BIKE_PROPERTIES.getMinAccelerationTimes()
                        && MotionDetector.accelerationTimes <= BIKE_PROPERTIES.getMaxAccelerationTimes()
                        && MotionDetector.currentLocation.getSpeed() * 3.6f < BIKE_PROPERTIES.getMaxSpeed()
                        && MotionDetector.currentLocation.getSpeed() * 3.6f >= BIKE_PROPERTIES.getMinSpeed()) {

                    if (priority(BIKE)) {
                        MotionDetector.currentType = BIKE;
                        lastTypeTime = new Date();
                        MotionDetector.listener.type(MotionDetector.currentType);
                    }

                } else if (MotionDetector.accelerationTimes >= MOTO_PROPERTIES.getMinAccelerationTimes()
                        && MotionDetector.accelerationTimes <= MOTO_PROPERTIES.getMaxAccelerationTimes()
                        && MotionDetector.currentLocation.getSpeed() * 3.6f < MOTO_PROPERTIES.getMaxSpeed()
                        && MotionDetector.currentLocation.getSpeed() * 3.6f >= MOTO_PROPERTIES.getMinSpeed()) {

                    if (priority(MOTO)) {
                        MotionDetector.currentType = MOTO;
                        lastTypeTime = new Date();
                        MotionDetector.listener.type(MotionDetector.currentType);
                    }

                } else if (MotionDetector.accelerationTimes >= METRO_PROPERTIES.getMinAccelerationTimes()
                        && MotionDetector.accelerationTimes <= METRO_PROPERTIES.getMaxAccelerationTimes()
                        && MotionDetector.currentLocation.getSpeed() * 3.6f < METRO_PROPERTIES.getMaxSpeed()
                        && MotionDetector.currentLocation.getSpeed() * 3.6f >= METRO_PROPERTIES.getMinSpeed()) {

                    if (priority(METRO)) {
                        MotionDetector.currentType = METRO;
                        lastTypeTime = new Date();
                        MotionDetector.listener.type(MotionDetector.currentType);
                    }

                } else if (MotionDetector.accelerationTimes >= CAR_PROPERTIES.getMinAccelerationTimes()
                        && MotionDetector.accelerationTimes <= CAR_PROPERTIES.getMaxAccelerationTimes()
                        && MotionDetector.currentLocation.getSpeed() * 3.6f < CAR_PROPERTIES.getMaxSpeed()
                        && MotionDetector.currentLocation.getSpeed() * 3.6f >= CAR_PROPERTIES.getMinSpeed()) {

                    if (priority(CAR)) {
                        MotionDetector.currentType = CAR;
                        lastTypeTime = new Date();
                        MotionDetector.listener.type(MotionDetector.currentType);
                    }

                } else if (MotionDetector.accelerationTimes >= PLANE_PROPERTIES.getMinAccelerationTimes()
                        && MotionDetector.accelerationTimes <= PLANE_PROPERTIES.getMaxAccelerationTimes()
                        && MotionDetector.currentLocation.getSpeed() * 3.6f < PLANE_PROPERTIES.getMaxSpeed()
                        && MotionDetector.currentLocation.getSpeed() * 3.6f >= PLANE_PROPERTIES.getMinSpeed()) {

                    if (priority(PLANE)) {
                        MotionDetector.currentType = PLANE;
                        lastTypeTime = new Date();
                        MotionDetector.listener.type(MotionDetector.currentType);
                    }

                }
            }
        }
    }

    private static boolean priority(String value) {
        if (MotionDetector.currentType == null) {
            return true;
        }

        if (value.equals(MotionDetector.currentType)) {
            return true;
        }

        if ((SIT.equals(MotionDetector.currentType) || WALK.equals(MotionDetector.currentType) || JOGGING.equals(MotionDetector.currentType) || RUN.equals(MotionDetector.currentType))
                && (SIT.equals(value) || WALK.equals(value) || JOGGING.equals(value) || RUN.equals(value))) {
            return true;
        }

        if (lastTypeTime != null && (lastTypeTime.getTime() - (new Date().getTime())) >= maxInterval) {
            return true;
        }

        for (String val : ORDER) {
            if (val.equals(MotionDetector.currentType)) {
                return true;
            } else if (val.equals(value)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void startJob() {
        startService();
        MotionDetector.sensorMan.registerListener(this, MotionDetector.accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void stopJob() {
        MotionDetector.sensorMan.unregisterListener(this);
        stopService();
    }

    class Properties {

        private float maxSpeed;
        private float minSpeed;
        private int minAccelerationTimes;
        private int maxAccelerationTimes;
        private float maxSteps;

        public Properties() {
            // nothing to do here
        }

        public Properties(float minSpeed, float maxSpeed, int minAccelerationTimes, int maxAccelerationTimes, float maxSteps) {
            this.maxSpeed = maxSpeed;
            this.minSpeed = minSpeed;
            this.minAccelerationTimes = minAccelerationTimes;
            this.maxAccelerationTimes = maxAccelerationTimes;
            this.maxSteps = maxSteps;
        }

        public float getMaxSpeed() {
            return maxSpeed;
        }

        public void setMaxSpeed(float maxSpeed) {
            this.maxSpeed = maxSpeed;
        }

        public float getMinSpeed() {
            return minSpeed;
        }

        public void setMinSpeed(float minSpeed) {
            this.minSpeed = minSpeed;
        }

        public float getMaxSteps() {
            return maxSteps;
        }

        public void setMaxSteps(float maxSteps) {
            this.maxSteps = maxSteps;
        }

        public int getMinAccelerationTimes() {
            return minAccelerationTimes;
        }

        public void setMinAccelerationTimes(int minAccelerationTimes) {
            this.minAccelerationTimes = minAccelerationTimes;
        }

        public int getMaxAccelerationTimes() {
            return maxAccelerationTimes;
        }

        public void setMaxAccelerationTimes(int maxAccelerationTimes) {
            this.maxAccelerationTimes = maxAccelerationTimes;
        }
    }
}
