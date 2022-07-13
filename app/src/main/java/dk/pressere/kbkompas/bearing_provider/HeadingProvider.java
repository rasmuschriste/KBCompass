package dk.pressere.kbkompas.bearing_provider;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class HeadingProvider implements SensorEventListener {

    private final Callback callback;

    // Class used to keep track of phone heading.
    private final SensorManager sensorManager;

    // Used for output
    private GeomagneticField geomagneticField;
    private final float[] orientation = new float[3];
    private final float[] rotation = new float[9];
    private final float[] inclination = new float[9];
    private final float[] gravity = new float[3];
    private final float[] geomagnetic = new float[3];

    public HeadingProvider(Context context, Callback callback) {
        this.callback = callback;

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    public void enable() {
        Sensor sensorMagnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, sensorMagnetic,SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensorGravity,SensorManager.SENSOR_DELAY_GAME);
    }

    public void disable() {
        Sensor sensorMagnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.unregisterListener(this,sensorMagnetic);
        sensorManager.unregisterListener(this,sensorGravity);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        final float alpha = 0.97f;

        synchronized (this) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                // Apply low pass filter (removes niose).
                gravity[0] = alpha * gravity[0] + (1 - alpha) * sensorEvent.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * sensorEvent.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * sensorEvent.values[2];
            } else if(sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagnetic[0] = alpha * geomagnetic[0] + (1 - alpha) * sensorEvent.values[0];
                geomagnetic[1] = alpha * geomagnetic[1] + (1 - alpha) * sensorEvent.values[1];
                geomagnetic[2] = alpha * geomagnetic[2] + (1 - alpha) * sensorEvent.values[2];
            }
        }

        boolean success = SensorManager.getRotationMatrix(rotation, inclination, gravity, geomagnetic);
        if(success) {
            SensorManager.getOrientation(rotation,orientation);
            float heading = (float) Math.toDegrees(orientation[0]);
            heading = (heading +360)%360;

            callback.onHeadingChanged(heading);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    public interface Callback {
        void onHeadingChanged(float heading);
    }


}
