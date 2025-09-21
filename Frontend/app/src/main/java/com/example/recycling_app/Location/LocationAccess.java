package com.example.recycling_app.Location;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class LocationAccess {

    private static final String TAG = "LocationAccess";
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String[] LOCATION_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private final Activity activity;
    private final PermissionListener listener;

    // 권한 요청 결과를 전달
    public interface PermissionListener {
        void LocationOnPermissionGranted();
        void LocationOnPermissionDenied();

        // 위치 바뀔 때마다 콜백
        void onLocationChanged(@NonNull Location location);
    }

    public LocationAccess(Activity activity, PermissionListener listener) {
        this.activity = activity;
        this.listener = listener;
    }

    // 위치 권한 확인 및 요청
    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            listener.LocationOnPermissionGranted();
        } else {
            ActivityCompat.requestPermissions(activity, LOCATION_PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // 권한 요청 결과
    public void handleRequestPermissionsResult(int requestCode, int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                listener.LocationOnPermissionGranted();
            } else {
                showPermissionDeniedDialog();
                listener.LocationOnPermissionDenied();
            }
        }
    }

    // 권한 거부 시
    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(activity)
                .setTitle("권한 필요")
                .setMessage("위치 권한이 필요합니다. 이 기능을 사용하려면 설정에서 권한을 허용해주세요.")
                .setPositiveButton("설정으로 이동", (dialog, which) -> {
                    try {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                .setData(Uri.parse("package:" + activity.getPackageName()));
                        activity.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Log.e(TAG, "Settings activity not found", e);
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                        activity.startActivity(intent);
                    }
                    activity.finish();
                })
                .setNegativeButton("앱 종료", (dialog, which) -> activity.finish())
                .setCancelable(false)
                .show();
    }
}
