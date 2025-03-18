package kr.co.uxn.agms_p;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;


public class PermissionUtil {

    @SuppressLint("InlinedApi")
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.POST_NOTIFICATIONS,
//            Manifest.permission.SCHEDULE_EXACT_ALARM,
//            Manifest.permission.USE_EXACT_ALARM,
            Manifest.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE,
            Manifest.permission.FOREGROUND_SERVICE
    };

    @SuppressLint("InlinedApi")
    private static final String[] PERMISSIONS_API_29 = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
    };

    private static final int PERMISSION_REQUEST_CODE = 1;

    @SuppressLint("BatteryLife")
    public static void requestPermission(final @NonNull Activity activity, final @NonNull ActivityResultLauncher <Intent> resultLauncher) {

        String[] permissionsToRequest = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ? PERMISSIONS : PERMISSIONS_API_29;
        ActivityCompat.requestPermissions(activity, permissionsToRequest, PERMISSION_REQUEST_CODE);

        boolean isIgnoringBatteryOptimizations = checkIgnoringBatteryOptimizations(activity);
        if (!isIgnoringBatteryOptimizations) {
            requestBatteryOptimizationExemption(activity, resultLauncher);
        }
    }

    private static boolean checkIgnoringBatteryOptimizations(Activity activity) {
        PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
        return pm.isIgnoringBatteryOptimizations(activity.getPackageName());
    }

    @SuppressLint("BatteryLife")
    private static void requestBatteryOptimizationExemption(Activity activity, ActivityResultLauncher<Intent> resultLauncher) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + activity.getPackageName()));
        resultLauncher.launch(intent);
    }

//    public static void requestBleON(Activity activity, ActivityResultLauncher<Intent> resultLauncher) {
//        Intent intent = new Intent();
//        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
//        intent.setData(Uri.parse("package:" + activity.getPackageName()));
//        resultLauncher.launch(intent);
//    }
}





