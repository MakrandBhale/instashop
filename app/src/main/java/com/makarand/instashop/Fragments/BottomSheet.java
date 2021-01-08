package com.makarand.instashop.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.makarand.instashop.Adapters.CatalogAdapter;
import com.makarand.instashop.Adapters.ProductAdapter;
import com.makarand.instashop.Constants;
import com.makarand.instashop.Helpers.LocalStorage;
import com.makarand.instashop.Models.Product;
import com.makarand.instashop.Models.User;
import com.makarand.instashop.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class BottomSheet extends BottomSheetDialogFragment {
    private CatalogAdapter catalogAdapter;
    private RecyclerView catalogList;
    private String sellerKey = null;
    private ConstraintLayout catalogListContainer;
    private LinearLayout goToMapsLayout, distanceLayout;
    private AppCompatTextView sellerAddressTextView;
    private AppCompatTextView distanceButtonTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.view_bottom_shit, container, false);
        catalogList = v.findViewById(R.id.catalog_list);
        sellerKey = getArguments().getString("sellerId");
        Button showProductsButton = v.findViewById(R.id.view_products_button);
        AppCompatTextView sellerNameTextView = v.findViewById(R.id.seller_name_textview);
        sellerAddressTextView = v.findViewById(R.id.address_textview);
        distanceButtonTextView = v.findViewById(R.id.distance_button_textview);
        distanceLayout = v.findViewById(R.id.distance_button);
        goToMapsLayout = v.findViewById(R.id.navigation_button);

        distanceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String distance = distanceButtonTextView.getText().toString();
                if (distance.isEmpty() || distance == null) {
                    return;
                }

                Toast.makeText(getActivity().getApplicationContext(), "Approximate distance between you and seller is " + distance, Toast.LENGTH_SHORT).show();
            }
        });

        showProductsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                catalogListContainer = v.findViewById(R.id.catalog_list_container);
                catalogListContainer.setVisibility(View.VISIBLE);
                view.setVisibility(View.GONE);
            }
        });
        LocalStorage localStorage = LocalStorage.getInstance();
        User seller = localStorage.getStoredObject(Constants.LOCAL_STORAGE_KEY_USER_INFO, User.class, getActivity().getApplicationContext());
        //sellerNameTextView.setText(seller.getName());
        double[] sellerLocationArray = getArguments().getDoubleArray("sellerLocation");
        double[] myLocationArray = getArguments().getDoubleArray("myLocation");
        getAddress(sellerLocationArray[0], sellerLocationArray[1]);
        getDistance(sellerLocationArray, myLocationArray);

        goToMapsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strUri = "http://maps.google.com/maps?q=loc:" + sellerLocationArray[0] + "," + sellerLocationArray[1] + " (" + seller.getName() + ")";
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));

                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");

                startActivity(intent);
            }
        });
        populateRecyclerView();
        return v;
    }

    private void getDistance(double[] sellerLocation, double[] myLocation) {
        Location myLocationObj = new Location("myLocation");
        myLocationObj.setLatitude(myLocation[0]);
        myLocationObj.setLongitude(myLocation[1]);

        Location sellerLocationObj = new Location("sellerLocation");
        sellerLocationObj.setLatitude(sellerLocation[0]);
        sellerLocationObj.setLongitude(sellerLocation[1]);

        int distance = (int) (myLocationObj.distanceTo(sellerLocationObj));

        if(distance > 1000) {
            distanceButtonTextView.setText(distance/1000 + " km");
        } else {
            distanceButtonTextView.setText(distance + " m");
        }

    }

    private void getAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getActivity().getApplicationContext(), Locale.ENGLISH);

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address fetchedAddress = addresses.get(0);
                StringBuilder builder = new StringBuilder();
                //builder.append(fetchedAddress.getAddressLine(0)).append(" ");
                builder.append(fetchedAddress.getSubLocality()).append(" ");
                builder.append(fetchedAddress.getLocality());
                sellerAddressTextView.setText(builder.toString());

            } else {
                sellerAddressTextView.setText("Searching address...");
            }

        } catch (IOException e) {
            sellerAddressTextView.setText(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

    }

    public void populateRecyclerView() {
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference().child(Constants.ProductsTree);
        catalogList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        Query query = productsRef.orderByChild("sellerId").equalTo(sellerKey);

        FirebaseRecyclerOptions<Product> options =
                new FirebaseRecyclerOptions.Builder<Product>()
                        .setQuery(query, new SnapshotParser<Product>() {
                            @NonNull
                            @Override
                            public Product parseSnapshot(@NonNull DataSnapshot snapshot) {
                                return snapshot.getValue(Product.class);
                            }
                        })
                        .build();

        catalogAdapter = new CatalogAdapter(options);
        catalogList.setAdapter(catalogAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (catalogAdapter == null) return;
        catalogAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (catalogAdapter == null) return;
        catalogAdapter.stopListening();
    }

    @Override
    public int getTheme() {
        return R.style.CustomBottomSheetDialog;
    }

    public void test(View v) {
        Toast.makeText(getActivity().getApplicationContext(), "Test", Toast.LENGTH_SHORT).show();
    }
}
