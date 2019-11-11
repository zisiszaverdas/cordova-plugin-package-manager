package com.dataware.packagemanagerplugin;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

public class InstallReceiver extends BroadcastReceiver {


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle extras = intent.getExtras();
        int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1);
        String message = null;
        if (extras != null) {
            message = extras.getString(PackageInstaller.EXTRA_STATUS_MESSAGE);
        }
        switch (status) {
            case PackageInstaller.STATUS_PENDING_USER_ACTION:
                Intent activityIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
                context.startActivity(activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case PackageInstaller.STATUS_SUCCESS:
                    Toast.makeText(context, "Install succeeded!", Toast.LENGTH_SHORT).show();
                    break;
            case PackageInstaller.STATUS_FAILURE:
            case PackageInstaller.STATUS_FAILURE_ABORTED:
            case PackageInstaller.STATUS_FAILURE_BLOCKED:
            case PackageInstaller.STATUS_FAILURE_CONFLICT:
            case PackageInstaller.STATUS_FAILURE_INCOMPATIBLE:
            case PackageInstaller.STATUS_FAILURE_INVALID:
            case PackageInstaller.STATUS_FAILURE_STORAGE:
                Toast.makeText(context, "Install failed! " + status + ", " + message,
                        Toast.LENGTH_SHORT).show();
                break;
            default:
                    Toast.makeText(context, "Unrecognized status received from installer: " + status,
                            Toast.LENGTH_SHORT).show();
        }
    }

}
