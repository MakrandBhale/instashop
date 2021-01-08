package com.makarand.instashop.Helpers;

import android.Manifest;
import android.content.Context;


import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.makarand.instashop.Listeners.PermissionCalllback;

import java.util.List;

public class PermissionHelper {

    PermissionCalllback permissionCalllback;
    public PermissionHelper(PermissionCalllback permissionCalllback) {
        this.permissionCalllback = permissionCalllback;
    }

    public void requestPermission(Context context){
        TedPermission.with(context)
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        permissionCalllback.onPermissionGranted();
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        permissionCalllback.onPermissionRejected();
                    }
                })
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .check();
    }
}
