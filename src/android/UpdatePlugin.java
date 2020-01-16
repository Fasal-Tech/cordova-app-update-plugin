package com.mrspark.cordova.plugin;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import android.app.Activity;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.tasks.Task;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.install.model.InstallStatus;
import static java.lang.System.out;
// Cordova-required packages
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;

public class UpdatePlugin extends CordovaPlugin {
  public int REQUEST_CODE = 7;
  @Override 
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
  }

  // @Override
  // public void onActivityResult(int requestCode, int resultCode, Intent data) {
  // if (requestCode == REQUEST_CODE) {
  //   if (resultCode != Activity.RESULT_OK) {
  //     System.out.println("Update flow failed! Result code: " + resultCode);
  //     // If the update is cancelled or fails,
  //     // you can request to start the update again.
  //     }
  //   }
  // }

  // @Override
  // protected void onResume() {
  //   super.onResume();
  //   appUpdateManager
  //     .getAppUpdateInfo()
  //     .addOnSuccessListener(
  //         appUpdateInfo -> {
  //           System.out.println("herer on resume");
  //           System.out.println(appUpdateInfo);
  //           if (appUpdateInfo.updateAvailability()
  //               == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
  //               appUpdateManager.startUpdateFlowForResult(
  //                   appUpdateInfo,
  //                   IMMEDIATE,
  //                   this,
  //                   REQUEST_CODE);
  //           }
  //       });
  // }

  @Override
  public boolean execute(String action, JSONArray args,
    final CallbackContext callbackContext) {
      // Verify that the user sent a 'show' action
      if (!action.equals("show")) {
        callbackContext.error("\"" + action + "\" is not a recognized action.");
        return false;
      }
      Context context = this.cordova.getActivity().getApplicationContext();

      AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(context);

      Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
      try {
      appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
        System.out.println(appUpdateInfo);  
        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
          && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
          try {
              appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.IMMEDIATE,
                cordova.getActivity(),
                 REQUEST_CODE
              );
            } catch(Exception e){
              e.printStackTrace();
            }
        }

        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
          && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
          try {
              appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.FLEXIBLE,
                cordova.getActivity(),
                 REQUEST_CODE
              );
             } catch(Exception e){
              e.printStackTrace();
            }
        }
        
        // if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {

        // }

        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, appUpdateInfo.toString());
        callbackContext.sendPluginResult(pluginResult);
      });
      } catch(Exception e){
          e.printStackTrace();
      }
      return true;
  }
}