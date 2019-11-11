package com.dataware.packagemanagerplugin;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.PluginResult;

public class PackageManagerPlugin extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        try {

            if (action.equals("install")) {
                if (args.length() != 1) {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
                    return false;
                }

                // Parse the arguments
                final CordovaResourceApi resourceApi = webView.getResourceApi();
                JSONObject obj = args.getJSONObject(0);
                String packageName = obj.has("packageName") ? obj.getString("packageName") : null;
                Uri uri = obj.has("url") ? resourceApi.remapUri(Uri.parse(obj.getString("url"))) : null;
                if (uri == null) {
                    callbackContext.error("Expected non-empty uri string argument.");
                    return false;
                }
                try {
                    InputStream inputStream = cordova.getContext().getContentResolver().openInputStream(uri);
                    if (inputStream == null) {
                        callbackContext.error("Failed to resolve uri to inputStream.");
                        return false;
                    }
                    try {
                        installPackage(cordova.getContext(), "PackageManagerPluginSession", packageName, inputStream);
                    } catch (IOException e) {
                        e.printStackTrace();
                        callbackContext.error("installPackage IOException.");
                        return false;
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    callbackContext.error("inputStream FileNotFoundException.");
                    return false;
                }
//                startActivity(obj.getString("action"), uri, type, extrasMap);
//                //return new PluginResult(PluginResult.Status.OK);
//                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
                return true;
            }
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            String errorMessage = e.getMessage();
            //return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION, errorMessage));
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void installPackage(Context context, String installSessionId,
                                       String packageName,
                                       InputStream apkStream) throws IOException {
        PackageManager packageManger = context.getPackageManager();
        android.content.pm.PackageInstaller packageInstaller =
                packageManger.getPackageInstaller();

        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        params.setAppPackageName(packageName);
        android.content.pm.PackageInstaller.Session session = null;
        try {
            int sessionId = packageInstaller.createSession(params);
            session = packageInstaller.openSession(sessionId);
            OutputStream out = session.openWrite(installSessionId, 0, -1);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = apkStream.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
            session.fsync(out);
            out.close();

            Intent intent = new Intent(context, InstallReceiver.class);

            session.commit(PendingIntent.getBroadcast(context, sessionId,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT).getIntentSender());
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
