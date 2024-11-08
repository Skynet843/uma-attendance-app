package in.umaenterprise.attendancemanagement.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import in.umaenterprise.attendancemanagement.service.TrackingService;
import in.umaenterprise.attendancemanagement.utils.CommonMethods;
import in.umaenterprise.attendancemanagement.utils.ConstantData;
import in.umaenterprise.attendancemanagement.utils.SharePreferences;

public class AutoStartReceiver extends BroadcastReceiver {

    public static final String START_TRACKING = "START_TRACKING";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            if (SharePreferences.getBool(SharePreferences.KEY_IS_TRACKING_ENABLE,
                    SharePreferences.DEFAULT_BOOLEAN)) {
                Intent myIntent = new Intent(context, TrackingService.class);
                myIntent.setAction(ConstantData.START_LOCATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(myIntent);
                } else {
                    context.startService(myIntent);
                }
            }
        } else if (intent.getAction().equals(START_TRACKING)) {
            if (!CommonMethods.isMyServiceRunning(context, TrackingService.class.getName())) {
                if (SharePreferences.getBool(SharePreferences.KEY_IS_TRACKING_ENABLE,
                        SharePreferences.DEFAULT_BOOLEAN)) {
                    Intent myIntent = new Intent(context, TrackingService.class);
                    myIntent.setAction(ConstantData.START_LOCATION_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(myIntent);
                    } else {
                        context.startService(myIntent);
                    }
                }
            }
        }
    }


}