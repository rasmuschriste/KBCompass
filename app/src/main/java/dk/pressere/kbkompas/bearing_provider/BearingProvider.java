package dk.pressere.kbkompas.bearing_provider;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.location.Location;

public class BearingProvider implements HeadingProvider.Callback, LocationProvider.Callback{

    public static final int LOCATION_MISSING = 0;
    public static final int LOCATION_FOUND = 1;
    public static final int LOCATION_GPS_PROVIDER_DISABLED = 1000;
    public static final int LOCATION_GPS_PROVIDER_ENABLED = 1001;

    private Location targetLocation;
    private Location currentLocation;

    private float currentHeading = 0;
    private float declination = 0;

    private final Callback callback;
    private final HeadingProvider headingProvider;
    private final LocationProvider locationProvider;

    private boolean isLocationMissing = true;

    public BearingProvider(Context context, Callback callback) {
        this.callback = callback;

        headingProvider = new HeadingProvider(context, this);
        locationProvider = new LocationProvider(context, this);
    }

    public void statusUpdate(int code) {
        switch (code) {
            case LocationProvider.GPS_PROVIDER_DISABLED:
                callback.statusUpdate(LOCATION_GPS_PROVIDER_DISABLED);
                break;
            case LocationProvider.GPS_PROVIDER_ENABLED:
                callback.statusUpdate(LOCATION_GPS_PROVIDER_ENABLED);
                break;
        }
    }

    public Boolean isGPSProviderEnabled() {
        return locationProvider.isProviderEnabled();
    }

    public void setTargetLocation(Location location) {
        targetLocation = location;
        callback.onBearingChanged(getBearing());
    }

    public void enable() {
        // This will only be relevant the first time it is called.
        if (isLocationMissing) {
            callback.statusUpdate(LOCATION_MISSING);
        }
        headingProvider.enable();
        locationProvider.enable();
    }

    public void disable() {
        headingProvider.disable();
        locationProvider.disable();
    }

    @Override
    public void onHeadingChanged(float heading) {
        currentHeading = heading + declination;
        callback.onBearingChanged(getBearing());
    }

    @Override
    public void onLocationChanged(Location location) {
        if (isLocationMissing) {
            callback.statusUpdate(LOCATION_FOUND);
            isLocationMissing = false;
        }
        currentLocation = location;
        GeomagneticField geomagneticField = new GeomagneticField((float)currentLocation.getLatitude(), (float)currentLocation.getLongitude(), 0, System.currentTimeMillis());
        declination = geomagneticField.getDeclination();
        callback.onBearingChanged(getBearing());
        callback.onLocationChanged(location);
    }

    public float getDistance() {
        if (targetLocation != null) {
            return getDistance(targetLocation);
        } else {
            return 0;
        }
    }

    public float getDistance(Location location) {
        if (currentLocation != null) {
            return currentLocation.distanceTo(location);
        } else {
            return 0;
        }
    }

    private float getBearing() {
        if (targetLocation != null && currentLocation != null) {
            return (currentLocation.bearingTo(targetLocation) - currentHeading + 720) % 360;
        } else {
            return 0;
        }
    }


    public interface Callback {
        void onBearingChanged(float bearing);
        void statusUpdate(int value);
        void onLocationChanged(Location location);
    }
}
