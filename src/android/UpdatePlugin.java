package com.mrspark.cordova.plugin;

import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.tasks.Task;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.R;
import android.widget.FrameLayout;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import static java.lang.System.out;

public class UpdatePlugin extends CordovaPlugin {
 public int REQUEST_CODE = 1;
 private static boolean IN_APP_UPDATE = false;
 private static String IN_APP_UPDATE_TYPE = "FLEXIBLE";
 private static AppUpdateManager appUpdateManager;
 private static InstallStateUpdatedListener listener;
 private FrameLayout layout;

 @Override
 public void initialize(CordovaInterface cordova, CordovaWebView webView) {
  super.initialize(cordova, webView);
  layout = (FrameLayout) webView.getView().getParent();
 }

 @Override
 public void onActivityResult(int requestCode, int resultCode, Intent data) {
  if (requestCode == REQUEST_CODE && IN_APP_UPDATE) {
   if (resultCode != Activity.RESULT_OK) {
    System.out.println("Update failed");
    // If the update is cancelled or fails,
    // you can request to start the update again.
   }
  }
 }

 public void onStateUpdate(InstallState state) {
  if (state.installStatus() == InstallStatus.DOWNLOADED) {
   // After the update is downloaded, show a notification
   // and request user confirmation to restart the app.
   popupSnackbarForCompleteUpdate();
  }
 };
 /* Displays the snackbar notification and call to action. */
 private void popupSnackbarForCompleteUpdate() {
  Snackbar snackbar =
   Snackbar.make(
    layout,
    "An update has just been downloaded.",
    Snackbar.LENGTH_INDEFINITE);
  snackbar.setAction("RESTART", view -> appUpdateManager.completeUpdate());
  snackbar.show();
 }

 @Override
 public boolean execute(String action, JSONArray args,
  final CallbackContext callbackContext) {
  // Verify that the user sent a "show" action
  if (!action.equals("show")) {
   callbackContext.error("\"" + action + "\" is not a recognized action.");
   return false;
  }
  IN_APP_UPDATE = true;
  try {
   System.out.println(args.getJSONObject(0));
   JSONObject argument = args.getJSONObject(0);
   IN_APP_UPDATE_TYPE = argument.getString("updateType");
   System.out.println("came here");
   System.out.println(IN_APP_UPDATE_TYPE);
  } catch (JSONException e) {
   e.printStackTrace();
  }
  Context context = this.cordova.getActivity().getApplicationContext();
  fakeAppUpdateManager =  FakeAppUpdateManager(context);
  fakeAppUpdateManager.partiallyAllowedUpdateType = AppUpdateType.FLEXIBLE;
  fakeAppUpdateManager.setUpdateAvailable(2);

  System.out.println(fakeAppUpdateManager.isConfirmationDialogVisible);

  fakeAppUpdateManager.userAcceptsUpdate();

  fakeAppUpdateManager.downloadStarts();

  fakeAppUpdateManager.downloadCompletes();
  
  fakeAppUpdateManager.installCompletes();
  System.out.println(fakeAppUpdateManager.isInstallSplashScreenVisible);
  appUpdateManager = AppUpdateManagerFactory.create(context);

  Task < AppUpdateInfo > appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

  try {
   appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
    System.out.println(appUpdateInfo);
    if (IN_APP_UPDATE && IN_APP_UPDATE_TYPE == "IMMEDIATE" && appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
     appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
     try {
      appUpdateManager.startUpdateFlowForResult(
       appUpdateInfo,
       AppUpdateType.IMMEDIATE,
       cordova.getActivity(),
       REQUEST_CODE
      );
     } catch (Exception e) {
      e.printStackTrace();
     }
    } else if (IN_APP_UPDATE && IN_APP_UPDATE_TYPE == "FLEXIBLE" && appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
     appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
     listener = state -> {
      onStateUpdate(state);
     };
     appUpdateManager.registerListener(listener);
     try {
      appUpdateManager.startUpdateFlowForResult(
       appUpdateInfo,
       AppUpdateType.FLEXIBLE,
       cordova.getActivity(),
       REQUEST_CODE
      );
     } catch (Exception e) {
      e.printStackTrace();
     }
    }

    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, appUpdateInfo.toString());
    callbackContext.sendPluginResult(pluginResult);
   });
  } catch (Exception e) {
   e.printStackTrace();
  }
  return true;
 }

 @Override
 public void onResume(boolean multitasking) {
  super.onResume(multitasking);
  appUpdateManager
   .getAppUpdateInfo()
   .addOnSuccessListener(
    appUpdateInfo -> {

     if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
      popupSnackbarForCompleteUpdate();
      appUpdateManager.unregisterListener(listener);
     }

     if (appUpdateInfo.updateAvailability() ==
      UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
      // If an in-app update is already running, resume the update.
      try {
       appUpdateManager.startUpdateFlowForResult(
        appUpdateInfo,
        AppUpdateType.IMMEDIATE,
        cordova.getActivity(),
        REQUEST_CODE);
      } catch (Exception e) {
       e.printStackTrace();
      }
     }
    });
 }
}