package com.makarand.instashop.Helpers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.makarand.instashop.Listeners.LocationCallback;

public class LocationHelper implements LocationCallback {
    Context context;
    private LocationManager locationManager;
    private Criteria criteria;

    public LocationHelper(Context context) {
        this.context = context;
    }

    @Override
    public void FetchLocation(LocationListener locationListener) {
        if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this.context, "Location permission denied, please allow to continue", Toast.LENGTH_SHORT).show();
            return;
        }
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.context);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location == null){
                    Toast.makeText(context, "Please enable GPS", Toast.LENGTH_SHORT).show();
                    return;
                }
                locationListener.onLocationChanged(location);
            }
        });
        //getLocationManager().requestSingleUpdate(getCriteria(), locationListener, getLooper());
    }

    private Looper getLooper() {
        return null;
    }

    private LocationManager getLocationManager() {
        if(locationManager == null)
            locationManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
        return this.locationManager;
    }

    private Criteria getCriteria() {
        if(criteria == null) {
            this.criteria =  new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setPowerRequirement(Criteria.POWER_HIGH);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setSpeedRequired(false);
            criteria.setCostAllowed(true);
            criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
            criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
        }

        return criteria;
    }




}
