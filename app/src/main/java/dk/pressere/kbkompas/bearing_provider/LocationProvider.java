package dk.pressere.kbkompas.bearing_provider;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;

import androidx.core.app.ActivityCompat;

/**
 * Created by REC-PC on 24-Aug-17.
 */

public class LocationProvider implements LocationListener {

    public static final int GPS_PROVIDER_DISABLED = 1000;
    public static final int GPS_PROVIDER_ENABLED = 1001;

    private static final long MIN_REQUIRED_ACCURACY = 100;
    private static final long ACCURACY_BASE_POINT = 10;
    private static final long MIN_TIME_INTERVAL = 1000;
    private static final float MIN_DISTANCE = 0;
    private static final float TIME_TO_ACCURACY_WEIGHT = 1000000000;

    private final LocationManager locationManager;
    private Location currentBestEstimate;

    private final Context context;
    private final Callback callback;

    public LocationProvider(Context context, Callback callback) {
        this.context = context;
        this.callback = callback;

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

    }

    public void enable() {
        // Will only enable if permission is given.
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            // Do nothing.
        } else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_INTERVAL, MIN_DISTANCE, this);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_INTERVAL, MIN_DISTANCE, this);
            onLocationChanged(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
            onLocationChanged(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));

            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                callback.statusUpdate(GPS_PROVIDER_DISABLED);
            }
        }
    }

    public void disable() {
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        // Determine best estimate.
        if (location != null) {
            if (location.getAccuracy() <= MIN_REQUIRED_ACCURACY) {
                if (currentBestEstimate == null) {
                    // If this is the fist location found we use it.
                    currentBestEstimate = location;
                    callback.onLocationChanged(currentBestEstimate);
                } else if (location.getAccuracy() < currentBestEstimate.getAccuracy() ||
                        location.getAccuracy() <= calculateRequiredAccuracyToTime(currentBestEstimate)) {
                    // If new location has better accuracy or has better accuracy than required based on the time.
                    currentBestEstimate = location;
                    callback.onLocationChanged(currentBestEstimate);
                }
            }
        }
    }

    private long calculateRequiredAccuracyToTime(Location location) {
        // Model for for calculated how good a location needs to be.
        long dt =  SystemClock.elapsedRealtimeNanos() - location.getElapsedRealtimeNanos();
        long accuracy = (long) (ACCURACY_BASE_POINT + dt/TIME_TO_ACCURACY_WEIGHT * dt/TIME_TO_ACCURACY_WEIGHT);
        if (accuracy > MIN_REQUIRED_ACCURACY) {
            accuracy = MIN_REQUIRED_ACCURACY;
        }
        return accuracy;
    }

    public Boolean isProviderEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        if (s.equals(LocationManager.GPS_PROVIDER)) {
            callback.statusUpdate(GPS_PROVIDER_ENABLED);
        }
    }

    @Override
    public void onProviderDisabled(String s) {
        if (s.equals(LocationManager.GPS_PROVIDER)) {
            callback.statusUpdate(GPS_PROVIDER_DISABLED);
        }
    }


    public interface Callback {
        void onLocationChanged(Location location);
        void statusUpdate(int code);
    }
}
