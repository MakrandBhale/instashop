package com.makarand.instashop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.view.CameraView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makarand.instashop.Helpers.LocalStorage;
import com.makarand.instashop.Helpers.LocationHelper;
import com.makarand.instashop.Helpers.PermissionHelper;
import com.makarand.instashop.Listeners.PermissionCalllback;
import com.makarand.instashop.Models.Product;
import com.makarand.instashop.Models.User;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

import static com.makarand.instashop.Constants.FILENAME_FORMAT;

public class SellerCameraActivity extends AppCompatActivity {
    LinearLayout permissionRefusedContainer, backdropLayout;
    RelativeLayout imagePreviewContainer, cameraViewContainer;
    CameraView cameraView;
    AppCompatImageButton captureButton;
    AppCompatImageView imagePreviewImageView;
    MaterialButton postAdButton;
    AppCompatEditText productName, productPrice;
    ImageButton storeImageButton;
    LocationListener locationListener;
    Location currentLocation;
    LocationManager locationManager;
    Criteria criteria;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_camera);
        permissionRefusedContainer = findViewById(R.id.permission_refused_container);
        cameraView = findViewById(R.id.cameraView);
        cameraViewContainer = findViewById(R.id.camera_preview_container);
        imagePreviewContainer = findViewById(R.id.image_preview_container);
        captureButton = findViewById(R.id.captureButton);
        imagePreviewImageView = findViewById(R.id.image_preview_imageview);
        backdropLayout = findViewById(R.id.backdrop_container);
        postAdButton = findViewById(R.id.post_ad_button);
        productName = findViewById(R.id.product_name);
        productPrice = findViewById(R.id.price);
        storeImageButton = findViewById(R.id.store_image_button);
        postAdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validate()) return;
                hideKeyboard();
                v.setEnabled(false);
                getLocation();
            }
        });
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
            }
        });
        initCamera();
    }

    private void getLocation() {
        LocationHelper locationHelper = new LocationHelper(this);
        locationHelper.FetchLocation(new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                currentLocation = location;
                uploadImage();
            }
        });
        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission denied, please allow to continue", Toast.LENGTH_SHORT).show();
            return;
        }
        setUpLocationListener();
        locationManager.requestSingleUpdate(criteria, locationListener, null);*/
    }

    private void setUpLocationListener() {
        if(locationListener != null && locationManager != null) return;
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
                uploadImage();
            }
        };
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Looper looper = null;

    }

    private boolean validate() {
        String name = productName.getText().toString().trim();
        String price = productPrice.getText().toString().trim();

        if(name.length() < 3) {
            Toast.makeText(this, "Please enter name of product you are selling", Toast.LENGTH_SHORT).show();
            return false;
        }
        double productPrice = 0;
        try {
            productPrice = Double.parseDouble(price);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void hideKeyboard() {
        try {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadImage() {
        imagePreviewImageView.setDrawingCacheEnabled(true);
        imagePreviewImageView.buildDrawingCache();
        Bitmap imageToUpload = ((BitmapDrawable) imagePreviewImageView.getDrawable()).getBitmap();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imageToUpload.compress(Bitmap.CompressFormat.JPEG, 10, baos);
                byte[] data = baos.toByteArray();
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                StorageReference storageReference = FirebaseStorage.getInstance().getReference(Constants.ProductImageFolder + uid + "/" + getFileName());
                storageReference.putBytes(data)
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SellerCameraActivity.this, "Error Occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                resetImagePreview();
                                retakeImage(null);
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                                result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        User user = LocalStorage.getInstance()
                                                .getStoredObject(Constants.LOCAL_STORAGE_KEY_USER_INFO, User.class, getApplicationContext());
                                        String sellerName = "";
                                        if(user != null) sellerName = user.getName();

                                        Product product = new Product(
                                                productName.getText().toString().trim(),
                                                productPrice.getText().toString().trim(),
                                                uri.toString(),
                                                uid,
                                                sellerName,
                                                currentLocation.getLatitude(),
                                                currentLocation.getLongitude()
                                        );
                                        sendToDatabase(product);
                                        resetImagePreview();
                                        retakeImage(null);
                                        Toast.makeText(SellerCameraActivity.this, "Image uploaded: ", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        });
            }
        });
        thread.start();
    }

    private void sendToDatabase(Product product) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String productId = database.getReference().child(Constants.ProductsTree).push().getKey();
        product.setProductId(productId);


        database.getReference().child(Constants.ProductsTree).child(productId).setValue(product).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful()) {
                    Toast.makeText(SellerCameraActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                } else {
                    DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference().child("Locations");
                    GeoFire geoFire = new GeoFire(locationRef);
                    geoFire.setLocation(product.getSellerId(), new GeoLocation(product.getLatitude(), product.getLongitude()));
                }
            }
        });
        database.getReference(Constants.UserTree + product.getSellerId() + "/products/").push().setValue(productId);
    }

    private void resetImagePreview() {
        postAdButton.setEnabled(true);
        imagePreviewImageView.setImageBitmap(null);
        imagePreviewImageView.destroyDrawingCache();
        productPrice.setText(null);
        productName.setText(null);
    }
    private void captureImage() {
        cameraView.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                showImagePreview(imageProxyToBitmap(image));
                super.onCaptureSuccess(image);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                super.onError(exception);
            }
        });
//        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(imageFile).build();
//        cameraView.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
//            @Override
//            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
//                Uri uri = Uri.fromFile(imageFile);
//                showImagePreview(uri);
//            }
//
//            @Override
//            public void onError(@NonNull ImageCaptureException exception) {
//                Toast.makeText(SellerCameraActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    private void showImagePreview(Bitmap bitmap){
        storeImageButton.setVisibility(View.GONE);
        imagePreviewContainer.setVisibility(View.VISIBLE);
        imagePreviewImageView.setImageBitmap(bitmap);

        slideUp(backdropLayout);

        cameraViewContainer.setVisibility(View.GONE);
    }

    public void openStore(View v) {
        startActivity(new Intent(this, SellerHome.class));
        finish();
    }

    public void retakeImage(View view) {
        storeImageButton.setVisibility(View.VISIBLE);
        imagePreviewContainer.setVisibility(View.GONE);
        cameraViewContainer.setVisibility(View.VISIBLE);
    }

    private void initCamera() {
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            checkPermission();
            return;
        }
        cameraView.bindToLifecycle(this);
    }
    private void checkPermission() {
        PermissionHelper ph = new PermissionHelper(new PermissionCalllback() {
            @Override
            public void onPermissionRejected() {
                //Toast.makeText(SellerCameraActivity.this, "Granted", Toast.LENGTH_SHORT).show();
                permissionRefusedContainer.setVisibility(View.VISIBLE);

            }

            @Override
            public void onPermissionGranted() {
                //Toast.makeText(SellerCameraActivity.this, "Granted", Toast.LENGTH_SHORT).show();
                permissionRefusedContainer.setVisibility(View.GONE);
                initCamera();
            }
        });
        ph.requestPermission(getApplicationContext());
    }

    private String getFileName(){
        return new SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg";
    }
    private File getOutputDirectory(){
        return new File(
                getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                new SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg");

    }

    private Bitmap imageProxyToBitmap(ImageProxy image)
    {
        ImageProxy.PlaneProxy planeProxy = image.getPlanes()[0];
        ByteBuffer buffer = planeProxy.getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @Override
    public void onBackPressed() {
        if(imagePreviewContainer.getVisibility() == View.VISIBLE) {
            retakeImage(null);
        } else {
            super.onBackPressed();
        }
    }

    public void slideUp(View view){
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                view.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(200);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    // slide the view from its current position to below itself
    public void slideDown(View view){
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,                 // fromYDelta
                view.getHeight()); // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }


}
