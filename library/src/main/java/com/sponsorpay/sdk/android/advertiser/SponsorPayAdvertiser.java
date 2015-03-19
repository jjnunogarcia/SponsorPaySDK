/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.advertiser;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.sponsorpay.sdk.android.SponsorPay;
import com.sponsorpay.sdk.android.credentials.SPCredentials;
import com.sponsorpay.sdk.android.receivers.advertiser.CallbackResponseReceiver;
import com.sponsorpay.sdk.android.services.advertiser.AbstractService;
import com.sponsorpay.sdk.android.services.advertiser.ActionService;
import com.sponsorpay.sdk.android.services.advertiser.InstallService;
import com.sponsorpay.sdk.android.utils.HostInfo;
import com.sponsorpay.sdk.android.utils.SPIdException;
import com.sponsorpay.sdk.android.utils.SPIdValidator;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Provides convenience calls to run the Advertiser callback request. Manages the state of the SDK
 * determining whether a successful response to the callback request has been already received since
 * the application was installed in the host device.
 * </p>
 * <p>
 * It's implemented as a singleton, and its public methods are static.
 * </p>
 */
public class SponsorPayAdvertiser {

  private static final String TAG = "SponsorPayAdvertiser";

  /**
   * Keep track of the persisted state of the Advertiser part of the SDK
   */
  private SponsorPayAdvertiserState mPersistedState;

  /**
   * Singleton instance.
   */
  private static SponsorPayAdvertiser mInstance;

  /**
   * Constructor. Stores the received application context and loads up the shared preferences.
   *
   * @param context The host application context.
   */
  private SponsorPayAdvertiser(Context context) {
    if (context == null) {
      throw new RuntimeException("The SDK was not initialized yet. You should call SponsorPay.start method");
    }
    mPersistedState = new SponsorPayAdvertiserState(context);
  }

  private static SponsorPayAdvertiser getInstance(Context context) {
    if (mInstance == null) {
      mInstance = new SponsorPayAdvertiser(context);
      mInstance.registerCallbackReceiver(context);
    }
    return mInstance;
  }


  /**
   * This method does the actual registration at the SponsorPay backend, performing the advertiser
   * callback, and including in it a parameter to signal if a successful response has been
   * received yet.
   *
   * @param credentialsToken The token id of the credentials to be used.
   * @param customParams     A map of extra key/value pairs to add to the request URL.
   */
  private void register(String credentialsToken, Map<String, String> customParams) {
    SPCredentials credentials = SponsorPay.getCredentials(credentialsToken);

		/* Send asynchronous call to SponsorPay's API */
//    InstallCallbackSender callback = new InstallCallbackSender(credentials, mPersistedState);
//    callback.setCustomParams(customParams);
//    callback.trigger();
  }

  private void register(Context context, String credentialsToken, Map<String, String> customParams) {
    SPCredentials credentials = SponsorPay.getCredentials(credentialsToken);

    startInstallService(context, credentials, customParams);
  }

  private void registerCallbackReceiver(Context context) {
    IntentFilter intentFilter = new IntentFilter(AbstractService.BROADCAST_ACTION);
    LocalBroadcastManager.getInstance(context).registerReceiver(new CallbackResponseReceiver(), intentFilter);
  }

  private void startInstallService(Context context, SPCredentials credentials, Map<String, String> customParams) {
    Intent installServiceIntent = new Intent(context, InstallService.class);
    Bundle extras = new Bundle();
    extras.putParcelable(AbstractService.KEY_CREDENTIALS, credentials);
    if (customParams != null) {
      extras.putSerializable(AbstractService.KEY_CUSTOM_PARAMS, new HashMap<>(customParams));
    }
    installServiceIntent.putExtras(extras);
    context.startService(installServiceIntent);
  }

  private void notifyActionCompletion(String credentialsToken, String actionId, Map<String, String> customParams) {
    SPCredentials credentials = SponsorPay.getCredentials(credentialsToken);

		/* Send asynchronous call to SponsorPay's API */
//    ActionCallbackSender callback = new ActionCallbackSender(actionId, credentials, mPersistedState);
//    callback.setCustomParams(customParams);
//    callback.trigger();
  }

  private void notifyActionCompletion(Context context, String credentialsToken, String actionId, Map<String, String> customParams) {
    SPCredentials credentials = SponsorPay.getCredentials(credentialsToken);

    startActionService(context, credentials, actionId, customParams);
  }

  private void startActionService(Context context, SPCredentials credentials, String actionId, Map<String, String> customParams) {
    Intent installServiceIntent = new Intent(context, ActionService.class);
    Bundle extras = new Bundle();
    extras.putParcelable(AbstractService.KEY_CREDENTIALS, credentials);
    if (customParams == null) {
      customParams = new HashMap<>();
    }
    customParams.put(ActionService.ACTION_ID_KEY, actionId);
    extras.putSerializable(AbstractService.KEY_CUSTOM_PARAMS, new HashMap<>(customParams));
    installServiceIntent.putExtras(extras);
    context.startService(installServiceIntent);
  }

  //================================================================================
  // Actions
  //================================================================================


  /**
   * Report an Action completion. It will use the values hold on the current credentials.
   *
   * @param actionId the id of the action
   */
  public static void reportActionCompletion(String actionId) {
    String credentialsToken = SponsorPay.getCurrentCredentials().getCredentialsToken();
    reportActionCompletion(credentialsToken, actionId);
  }

  public static void reportActionCompletion(Context context, String actionId) {
    String credentialsToken = SponsorPay.getCurrentCredentials().getCredentialsToken();
    reportActionCompletion(context, credentialsToken, actionId, null);
  }

  /**
   * Report an Action completion.
   *
   * @param credentialsToken the token id of credentials
   * @param actionId         the id of the action
   */
  public static void reportActionCompletion(String credentialsToken, String actionId) {
    reportActionCompletion(credentialsToken, actionId, null);
  }

  /**
   * Report an Action completion.
   *
   * @param credentialsToken the token id of credentials
   * @param actionId         the id of the action
   * @param customParams     A map of extra key/value pairs to add to the request URL.
   */
  public static void reportActionCompletion(String credentialsToken, String actionId, Map<String, String> customParams) {
    try {
      SPIdValidator.validate(actionId);
    } catch (SPIdException e) {
      throw new RuntimeException("The provided Action ID is not valid. " + e.getLocalizedMessage());
    }
    if (HostInfo.isDeviceSupported()) {
      // The actual work is performed by the notifyActionCompletion() instance method.
      //mInstance has to exist so we can have a credentialsToken, anyway, shielding it
      if (mInstance == null) {
        throw new RuntimeException("No valid credentials object was created yet.\nYou have to execute SponsorPay.start method first.");
      }
      mInstance.notifyActionCompletion(credentialsToken, actionId, customParams);
    } else {
      outputLogMessage();
    }
  }

  public static void reportActionCompletion(Context context, String credentialsToken, String actionId, Map<String, String> customParams) {
    try {
      SPIdValidator.validate(actionId);
    } catch (SPIdException e) {
      throw new RuntimeException("The provided Action ID is not valid. " + e.getLocalizedMessage());
    }
    if (HostInfo.isDeviceSupported()) {
      // The actual work is performed by the notifyActionCompletion() instance method.
      //mInstance has to exist so we can have a credentialsToken, anyway, shielding it
      if (mInstance == null) {
        throw new RuntimeException("No valid credentials object was created yet.\nYou have to execute SponsorPay.start method first.");
      }

      mInstance.notifyActionCompletion(context, credentialsToken, actionId, customParams);
    } else {
      outputLogMessage();
    }
  }

  //================================================================================
  // Callbacks
  //================================================================================

  /**
   * Triggers the Advertiser callback. It will use the values hold on the current credentials.
   *
   * @param context Host application context.
   */
  public static void register(Context context) {
    register(context, (Map<String, String>) null);
  }

  /**
   * Triggers the Advertiser callback. It will use the values hold on the current credentials..
   *
   * @param context      Host application context.
   * @param customParams A map of extra key/value pairs to add to the request URL.
   */
  public static void register(Context context, Map<String, String> customParams) {
    String credentialsToken = SponsorPay.getCurrentCredentials().getCredentialsToken();
    register(credentialsToken, context, customParams);
  }

  /**
   * Triggers the Advertiser callback.
   *
   * @param credentialsToken the token id of credentials
   * @param context          Host application context.
   * @param customParams     A map of extra key/value pairs to add to the request URL.
   */
  public static void register(String credentialsToken, Context context, Map<String, String> customParams) {
    if (HostInfo.isDeviceSupported()) {
      getInstance(context);

      // The actual work is performed by the register() instance method.
//      mInstance.register(credentialsToken, customParams); TODO commented for the test
      mInstance.register(context, credentialsToken, customParams);
    } else {
      outputLogMessage();
    }
  }

  private static void outputLogMessage() {
    if (SponsorPayLogger.isLogging()) {
      SponsorPayLogger.i(TAG, "Only devices running Android API level 10 and above are supported");
    } else {
      Log.i(TAG, "Only devices running Android API level 10 and above are supported");
    }
  }

}
