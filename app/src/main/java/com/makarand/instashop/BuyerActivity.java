package com.makarand.instashop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.makarand.instashop.Fragments.BottomSheet;
import com.makarand.instashop.Fragments.MapsFragment;
import com.makarand.instashop.Helpers.LocationHelper;
import com.makarand.instashop.Helpers.PermissionHelper;
import com.makarand.instashop.Listeners.PermissionCalllback;

import java.util.ArrayList;
import java.util.HashMap;

public class BuyerActivity extends AppCompatActivity implements MapsFragment.MapCallback{
    FrameLayout fragmentsContainer;
    Location location;
    MapsFragment mapsFragment;
    HashMap<String, GeoLocation> productList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer);
        checkPermission();
        fragmentsContainer = findViewById(R.id.fragments_container);
        loadMaps();
        productList = new HashMap<>();
    }

    private void loadMaps() {
        mapsFragment = new MapsFragment();
        loadFragment(mapsFragment, Constants.MAPS_FRAGMENT_TAG);
    }

    private void checkPermission() {
        PermissionHelper ph = new PermissionHelper(new PermissionCalllback() {
            @Override
            public void onPermissionRejected() {

            }

            @Override
            public void onPermissionGranted() {

            }
        });
        ph.requestPermission(getApplicationContext());
    }

    private void getLocation() {
        LocationHelper locationHelper = new LocationHelper(this);

        locationHelper.FetchLocation(new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location currentLocation) {
                location = currentLocation;
                getList(location);

            }
        });
    }

    private void getList(Location location) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Locations");
        GeoFire geoFire = new GeoFire(dbRef);
        GeoQuery query = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), 50);
        query.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                productList.put(key, location);
                MapsFragment mapsFragment = (MapsFragment) getSupportFragmentManager().findFragmentByTag(Constants.MAPS_FRAGMENT_TAG);
                if(mapsFragment != null){
                    mapsFragment.addMarker(key, location);
                }
                //Toast.makeText(BuyerActivity.this, key, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onKeyExited(String key) {
                Toast.makeText(BuyerActivity.this, key + " LEFT", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Toast.makeText(BuyerActivity.this, key + " moved", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onGeoQueryReady() {

                //Toast.makeText(BuyerActivity.this, "GeoqueryReady", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Toast.makeText(BuyerActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragments_container, fragment, tag);
        fragmentTransaction.commit();
    }

    @Override
    public void OnMapReady() {
        getLocation();
    }

    @Override
    public void OnMarkerClicked(String key) {
        BottomSheet bottomSheet = new BottomSheet();
        Bundle bundle = new Bundle();
        bundle.putString("sellerId", key);
        GeoLocation sellerLocation = productList.get(key);
        bundle.putDoubleArray("myLocation", new double[]{location.getLatitude(), location.getLongitude()});
        bundle.putDoubleArray("sellerLocation", new double[]{sellerLocation.latitude, sellerLocation.longitude});
        bottomSheet.setArguments(bundle);

        bottomSheet.show(getSupportFragmentManager(), "test");
    }


}