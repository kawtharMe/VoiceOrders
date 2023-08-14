package com.example.mousa3idi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.Log;

import androidx.annotation.RequiresApi;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

public class RestartBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {

        Intent inten = new Intent(context, LaunchService.class);
        inten.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);


        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(inten);
            } else {
                context.startService(inten);
            }
        }
        else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {

            context.stopService(inten);
        }

    }
}

