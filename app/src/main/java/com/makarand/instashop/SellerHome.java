package com.makarand.instashop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.makarand.instashop.Fragments.StoreFragment;

public class SellerHome extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    FrameLayout fragmentsContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_home);
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        fragmentsContainer = findViewById(R.id.fragments_container);
        loadFragment(new StoreFragment());
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                switch (item.getItemId()){
                    case R.id.store:
                        fragment = new StoreFragment();
                        //Toast.makeText(SellerHome.this, "Store", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.sellerCamera:
                        startActivity(new Intent(getApplicationContext(), SellerCameraActivity.class));
                        //Toast.makeText(SellerHome.this, "Camera", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.profile:
                        Toast.makeText(SellerHome.this, "Profile", Toast.LENGTH_SHORT).show();
                        break;
                }
                return loadFragment(fragment);
            }
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if(fragment == null) return false;

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragments_container, fragment)
                .commit();
        return true;
    }


}