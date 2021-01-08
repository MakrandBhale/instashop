package com.makarand.instashop.Listeners;

import android.location.LocationListener;

import com.firebase.geofire.GeoLocation;

public interface LocationCallback {
    void FetchLocation(LocationListener locationListener);
}
