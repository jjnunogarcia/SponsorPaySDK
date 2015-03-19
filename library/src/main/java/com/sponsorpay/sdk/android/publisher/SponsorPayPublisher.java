/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */
package com.sponsorpay.sdk.android.publisher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.sponsorpay.sdk.android.SponsorPay;
import com.sponsorpay.sdk.android.credentials.SPCredentials;
import com.sponsorpay.sdk.android.publisher.currency.SPCurrencyServerListener;
import com.sponsorpay.sdk.android.publisher.currency.SPVirtualCurrencyConnector;
import com.sponsorpay.sdk.android.publisher.interstitial.SPInterstitialClient;
import com.sponsorpay.sdk.android.publisher.interstitial.SPInterstitialRequestListener;
import com.sponsorpay.sdk.android.publisher.mbe.SPBrandEngageClient;
import com.sponsorpay.sdk.android.publisher.mbe.SPBrandEngageRequest;
import com.sponsorpay.sdk.android.publisher.mbe.SPBrandEngageRequestListener;
import com.sponsorpay.sdk.android.publisher.ofw.SPOfferWallActivity;
import com.sponsorpay.sdk.android.utils.HostInfo;
import com.sponsorpay.sdk.android.utils.StringUtils;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides convenience calls to load and show the mobile Offer Wall, mobile BrandEnagge
 * and the mobile Interstitial.
 */
public class SponsorPayPublisher {
  public static final String PREFERENCES_FILENAME = "SponsorPayPublisherState";

  public static final String PLACEMENT_KEY = "placement_id";

  /**
   * Enumeration identifying the different messages which can be displayed in
   * the user interface.
   */
  public enum UIStringIdentifier {
    ERROR_DIALOG_TITLE, DISMISS_ERROR_DIALOG, GENERIC_ERROR, ERROR_LOADING_OFFERWALL, ERROR_LOADING_OFFERWALL_NO_INTERNET_CONNECTION, LOADING_INTERSTITIAL, LOADING_OFFERWALL, ERROR_PLAY_STORE_UNAVAILABLE, MBE_REWARD_NOTIFICATION, VCS_COINS_NOTIFICATION, VCS_DEFAULT_CURRENCY, MBE_ERROR_DIALOG_TITLE, MBE_ERROR_DIALOG_MESSAGE_DEFAULT, MBE_ERROR_DIALOG_MESSAGE_OFFLINE, MBE_ERROR_DIALOG_BUTTON_TITLE_DISMISS, MBE_FORFEIT_DIALOG_TITLE
  }

  /**
   * Messages which can be displayed in the user interface.
   */
  private static EnumMap<UIStringIdentifier, String> sUIStrings;

  /**
   * Fills {@link #sUIStrings} with the default messages.
   */
  private static void initUIStrings() {
    sUIStrings = new EnumMap<UIStringIdentifier, String>(UIStringIdentifier.class);
    sUIStrings.put(UIStringIdentifier.ERROR_DIALOG_TITLE, "Error");
    sUIStrings.put(UIStringIdentifier.DISMISS_ERROR_DIALOG, "Dismiss");
    sUIStrings.put(UIStringIdentifier.GENERIC_ERROR, "An error happened when performing this operation");
    sUIStrings.put(UIStringIdentifier.ERROR_LOADING_OFFERWALL, "An error happened when loading the offer wall");
    sUIStrings.put(UIStringIdentifier.ERROR_LOADING_OFFERWALL_NO_INTERNET_CONNECTION,
                   "An error happened when loading the offer wall (no internet connection)");
    sUIStrings.put(UIStringIdentifier.LOADING_INTERSTITIAL, "Loading...");
    sUIStrings.put(UIStringIdentifier.LOADING_OFFERWALL, "Loading...");
    sUIStrings.put(UIStringIdentifier.ERROR_PLAY_STORE_UNAVAILABLE,
                   "You don't have the Google Play Store application on your device to complete App Install offers.");
    sUIStrings.put(UIStringIdentifier.MBE_REWARD_NOTIFICATION, "Thanks! Your reward will be paid out shortly");
    sUIStrings.put(UIStringIdentifier.VCS_COINS_NOTIFICATION, "Congratulations! You've earned %.0f %s!");
    sUIStrings.put(UIStringIdentifier.VCS_DEFAULT_CURRENCY, "coins");

    sUIStrings.put(UIStringIdentifier.MBE_ERROR_DIALOG_TITLE, "Error");
    sUIStrings.put(UIStringIdentifier.MBE_ERROR_DIALOG_MESSAGE_DEFAULT,
                   "We're sorry, something went wrong. Please try again.");
    sUIStrings.put(UIStringIdentifier.MBE_ERROR_DIALOG_MESSAGE_OFFLINE,
                   "Your Internet connection has been lost. Please try again later.");
    sUIStrings.put(UIStringIdentifier.MBE_ERROR_DIALOG_BUTTON_TITLE_DISMISS, "Dismiss");
    sUIStrings.put(UIStringIdentifier.MBE_FORFEIT_DIALOG_TITLE, StringUtils.EMPTY_STRING);
  }

  /**
   * Gets a particular UI message identified by a {@link UIStringIdentifier}.
   *
   * @param identifier The identifier of the message to get.
   * @return The message string.
   */
  public static String getUIString(UIStringIdentifier identifier) {
    if (sUIStrings == null) {
      initUIStrings();
    }

    return sUIStrings.get(identifier);
  }

  /**
   * Replaces one of the UI messages with a custom text.
   *
   * @param identifier The identifier of the message to set.
   * @param message    Custom text for the message.
   */
  public static void setCustomUIString(UIStringIdentifier identifier, String message) {
    if (sUIStrings == null) {
      initUIStrings();
    }

    sUIStrings.put(identifier, message);
  }

  /**
   * Replaces one or several of the UI messages at once.
   *
   * @param messages An EnumMap mapping {@link UIStringIdentifier}s to the
   *                 respective desired texts.
   */
  public static void setCustomUIStrings(EnumMap<UIStringIdentifier, String> messages) {
    for (UIStringIdentifier condition : UIStringIdentifier.values()) {
      if (messages.containsKey(condition)) {
        setCustomUIString(condition, messages.get(condition));
      }
    }
  }

  /**
   * Replaces one of the UI messages with the text identified by an Android
   * String resource id.
   *
   * @param identifier The {@link UIStringIdentifier} of the message to replace.
   * @param message    An Android String resource identifier.
   * @param context    An Android context used to fetch the resource
   */
  public static void setCustomUIString(UIStringIdentifier identifier, int message, Context context) {
    setCustomUIString(identifier, context.getString(message));
  }

  public static void setCustomUIStrings(EnumMap<UIStringIdentifier, Integer> messages, Context context) {
    for (UIStringIdentifier condition : UIStringIdentifier.values()) {
      if (messages.containsKey(condition)) {
        setCustomUIString(condition, messages.get(condition), context);
      }
    }
  }

  // ================================================================================
  // OfferWall
  // ================================================================================

  /**
   * <p>
   * Returns an {@link Intent} that can be used to launch the
   * {@link SPOfferWallActivity}. Lets the caller specify the behavior of the
   * Offer Wall once the user gets redirected out of the application by
   * clicking on an offer.
   * </p>
   * <p/>
   * <p>
   * Will use the publisher application id and user id stored in the current
   * credentials.
   * </p>
   *
   * @param context        The publisher application context.
   * @param shouldStayOpen True if the Offer Wall should stay open after the user clicks
   *                       on an offer and gets redirected out of the app. False to close
   *                       the Offer Wall.
   * @return An Android {@link Intent} which can be used with the
   * {@link Activity} method startActivityForResult() to launch the
   * {@link SPOfferWallActivity}.Or null if the device is not
   * supported(Running Android OS version prior to Android 2.3
   * (Gingerbread).
   */
  public static Intent getIntentForOfferWallActivity(Context context, Boolean shouldStayOpen) {
    String credentialsToken = SponsorPay.getCurrentCredentials().getCredentialsToken();
    return getIntentForOfferWallActivity(credentialsToken, context, shouldStayOpen, null, null, null);
  }

  /**
   * <p>
   * Returns an {@link Intent} that can be used to launch the
   * {@link SPOfferWallActivity}. Lets the caller specify the behavior of the
   * Offer Wall once the user gets redirected out of the application by
   * clicking on an offer.
   * </p>
   * <p/>
   * <p>
   * Will use the publisher application id and user id stored in the current
   * credentials.
   * </p>
   *
   * @param context        The publisher application context.
   * @param shouldStayOpen True if the Offer Wall should stay open after the user clicks
   *                       on an offer and gets redirected out of the app. False to close
   *                       the Offer Wall.
   * @param placementId    The placement ID.
   * @return An Android {@link Intent} which can be used with the
   * {@link Activity} method startActivityForResult() to launch the
   * {@link SPOfferWallActivity}.Or null if the device is not
   * supported(Running Android OS version prior to Android 2.3
   * (Gingerbread).
   */
  public static Intent getIntentForOfferWallActivity(Context context, Boolean shouldStayOpen, String placementId) {
    String credentialsToken = SponsorPay.getCurrentCredentials().getCredentialsToken();
    return getIntentForOfferWallActivity(credentialsToken, context, shouldStayOpen, null, null, placementId);
  }

  /**
   * <p>
   * Returns an {@link Intent} that can be used to launch the
   * {@link SPOfferWallActivity}. Lets the caller specify the behavior of the
   * Offer Wall once the user gets redirected out of the application by
   * clicking on an offer.
   * </p>
   * <p/>
   * <p>
   * Will use the publisher application id and user id stored in the current
   * credentials.
   * </p>
   *
   * @param context        The publisher application context.
   * @param shouldStayOpen True if the Offer Wall should stay open after the user clicks
   *                       on an offer and gets redirected out of the app. False to close
   *                       the Offer Wall.
   * @param currencyName   The name of the currency employed by your application. Provide
   *                       null if you don't use a custom currency name.
   * @param customParams   A map of extra key/value pairs to add to the request URL.
   * @return An Android {@link Intent} which can be used with the
   * {@link Activity} method startActivityForResult() to launch the
   * {@link SPOfferWallActivity}. Or null if the device is not
   * supported(Running Android OS version prior to Android 2.3
   * (Gingerbread).
   */
  public static Intent getIntentForOfferWallActivity(Context context, Boolean shouldStayOpen, String currencyName,
                                                     HashMap<String, String> customParams) {

    String credentialsToken = SponsorPay.getCurrentCredentials().getCredentialsToken();
    return getIntentForOfferWallActivity(credentialsToken, context, shouldStayOpen, currencyName, customParams,
                                         null);
  }

  /**
   * <p>
   * Returns an {@link Intent} that can be used to launch the
   * {@link SPOfferWallActivity}. Lets the caller specify the behavior of the
   * Offer Wall once the user gets redirected out of the application by
   * clicking on an offer.
   * </p>
   * <p/>
   * <p>
   * Will use the publisher application id and user id stored in the current
   * credentials.
   * </p>
   *
   * @param context        The publisher application context.
   * @param shouldStayOpen True if the Offer Wall should stay open after the user clicks
   *                       on an offer and gets redirected out of the app. False to close
   *                       the Offer Wall.
   * @param currencyName   The name of the currency employed by your application. Provide
   *                       null if you don't use a custom currency name.
   * @param customParams   A map of extra key/value pairs to add to the request URL.
   * @param placementId    The placement ID.
   * @return An Android {@link Intent} which can be used with the
   * {@link Activity} method startActivityForResult() to launch the
   * {@link SPOfferWallActivity}. Or null if the device is not
   * supported(Running Android OS version prior to Android 2.3
   * (Gingerbread).
   */
  public static Intent getIntentForOfferWallActivity(Context context, Boolean shouldStayOpen, String currencyName,
                                                     HashMap<String, String> customParams, String placementId) {

    String credentialsToken = SponsorPay.getCurrentCredentials().getCredentialsToken();
    return getIntentForOfferWallActivity(credentialsToken, context, shouldStayOpen, currencyName, customParams,
                                         placementId);
  }

  /**
   * <p>
   * Returns an {@link Intent} that can be used to launch the
   * {@link SPOfferWallActivity}. Lets the caller specify the behavior of the
   * Offer Wall once the user gets redirected out of the application by
   * clicking on an offer.
   * </p>
   * <p/>
   * <p>
   * Will use the provided publisher application id and user id stored in the
   * credentials identified by the token id.
   * </p>
   *
   * @param credentialsToken The token id of the credentials to be used.
   * @param context          The publisher application context.
   * @param shouldStayOpen   True if the Offer Wall should stay open after the user clicks
   *                         on an offer and gets redirected out of the app. False to close
   *                         the Offer Wall.
   * @param currencyName     The name of the currency employed by your application. Provide
   *                         null if you don't use a custom currency name.
   * @param customParams     A map of extra key/value pairs to add to the request URL.
   * @return An Android {@link Intent} which can be used with the
   * {@link Activity} method startActivityForResult() to launch the
   * {@link SPOfferWallActivity}.
   */
  public static Intent getIntentForOfferWallActivity(String credentialsToken, Context context,
                                                     Boolean shouldStayOpen, String currencyName, HashMap<String, String> customParams) {

    return getIntentForOfferWallActivity(credentialsToken, context, shouldStayOpen, currencyName, customParams,
                                         null);
  }

  /**
   * <p>
   * Returns an {@link Intent} that can be used to launch the
   * {@link SPOfferWallActivity}. Lets the caller specify the behavior of the
   * Offer Wall once the user gets redirected out of the application by
   * clicking on an offer.
   * </p>
   * <p/>
   * <p>
   * Will use the provided publisher application id and user id stored in the
   * credentials identified by the token id.
   * </p>
   *
   * @param credentialsToken The token id of the credentials to be used.
   * @param context          The publisher application context.
   * @param shouldStayOpen   True if the Offer Wall should stay open after the user clicks
   *                         on an offer and gets redirected out of the app. False to close
   *                         the Offer Wall.
   * @param currencyName     The name of the currency employed by your application. Provide
   *                         null if you don't use a custom currency name.
   * @param customParams     A map of extra key/value pairs to add to the request URL.
   * @param placementId      The placement ID.
   * @return An Android {@link Intent} which can be used with the
   * {@link Activity} method startActivityForResult() to launch the
   * {@link SPOfferWallActivity}.
   */
  public static Intent getIntentForOfferWallActivity(String credentialsToken, Context context,
                                                     Boolean shouldStayOpen, String currencyName, HashMap<String, String> customParams, String placementId) {

    Intent intent = new Intent(context, SPOfferWallActivity.class);
    if (HostInfo.isDeviceSupported()) {
      customParams = (HashMap<String, String>) setParameterInMap(customParams, placementId);

      SPCredentials credentials = SponsorPay.getCredentials(credentialsToken);

      intent.putExtra(SPOfferWallActivity.EXTRA_CREDENTIALS_TOKEN_KEY, credentials.getCredentialsToken());
      intent.putExtra(SPOfferWallActivity.EXTRA_SHOULD_STAY_OPEN_KEY, shouldStayOpen);
      intent.putExtra(SPOfferWallActivity.EXTRA_CURRENCY_NAME_KEY, currencyName);
      intent.putExtra(SPOfferWallActivity.EXTRA_KEYS_VALUES_MAP_KEY, customParams);

    }
    return intent;
  }

  // ================================================================================
  // VCS
  // ================================================================================

  /**
   * Sends a request to the SponsorPay currency server to obtain the variation
   * in amount of virtual currency for a given user. Returns immediately, and
   * the answer is delivered to one of the provided listener's callback
   * methods. See {@link SPCurrencyServerListener}. The current credentials
   * will be used to get the application id and user id.
   *
   * @param context  Android application context.
   * @param listener {@link SPCurrencyServerListener} which will be notified of the
   *                 result of the request.
   */
  public static void requestNewCoins(Context context, SPCurrencyServerListener listener) {
    requestNewCoins(context, listener, null);
  }

  /**
   * Sends a request to the SponsorPay currency server to obtain the variation
   * in amount of virtual currency for a given user. Returns immediately, and
   * the answer is delivered to one of the provided listener's callback
   * methods. See {@link SPCurrencyServerListener}. The current credentials
   * will be used to get the application id and user id.
   *
   * @param context    Android application context.
   * @param currencyId Optionally, provide the ID of the currency that you want to
   *                   retrieve. If the provided ID is empty or null, then the
   *                   default currency will be requested. If an invalid currency ID
   *                   will be provided then you will receive an invalid application
   *                   error.
   * @param listener   {@link SPCurrencyServerListener} which will be notified of the
   *                   result of the request.
   */
  public static void requestNewCoins(Context context, String currencyId, SPCurrencyServerListener listener) {
    requestNewCoins(context, currencyId, listener, null);
  }

  /**
   * Sends a request to the SponsorPay currency server to obtain the variation
   * in amount of virtual currency for a given user. Returns immediately, and
   * the answer is delivered to one of the provided listener's callback
   * methods. See {@link SPCurrencyServerListener}. The current credentials
   * will be used to get the application id and user id.
   *
   * @param context        Android application context.
   * @param listener       {@link SPCurrencyServerListener} which will be notified of the
   *                       result of the request.
   * @param customCurrency A string representing the custom currency to be used by the
   *                       toast message to show the amount of coins earned.
   */
  public static void requestNewCoins(Context context, SPCurrencyServerListener listener, String customCurrency) {
    String credentialsToken = SponsorPay.getCurrentCredentials().getCredentialsToken();
    requestNewCoins(credentialsToken, context, listener, null, null, customCurrency);
  }

  /**
   * Sends a request to the SponsorPay currency server to obtain the variation
   * in amount of virtual currency for a given user. Returns immediately, and
   * the answer is delivered to one of the provided listener's callback
   * methods. See {@link SPCurrencyServerListener}. The current credentials
   * will be used to get the application id and user id.
   *
   * @param context        Android application context.
   * @param listener       {@link SPCurrencyServerListener} which will be notified of the
   *                       result of the request.
   * @param currencyId     Optionally, provide the ID of the currency that you want to
   *                       retrieve. If the provided ID is empty or null, then the
   *                       default currency will be requested. If an invalid currency ID
   *                       will be provided then you will receive an invalid application
   *                       error.
   * @param customCurrency A string representing the custom currency to be used by the
   *                       toast message to show the amount of coins earned.
   */
  public static void requestNewCoins(Context context, String currencyId, SPCurrencyServerListener listener, String customCurrency) {
    String credentialsToken = SponsorPay.getCurrentCredentials().getCredentialsToken();
    requestNewCoins(credentialsToken, context, listener, null, currencyId, null, customCurrency);
  }

  /**
   * Sends a request to the SponsorPay currency server to obtain the variation
   * in amount of virtual currency for a given user. Returns immediately, and
   * the answer is delivered to one of the provided listener's callback
   * methods. See {@link SPCurrencyServerListener}. It will use the
   * credentials identified by the provided token id.
   *
   * @param credentialsToken The token id of the credentials to be used.
   * @param context          Android application context.
   * @param listener         {@link SPCurrencyServerListener} which will be notified of the
   *                         result of the request.
   * @param transactionId    Optionally, provide the ID of the latest known transaction.
   *                         The delta of coins will be calculated from this transaction
   *                         (not included) up to the present. Leave it to null to let the
   *                         SDK use the latest transaction ID it kept track of.
   * @param customParams     A map of extra key/value pairs to add to the request URL.
   * @param customCurrency   A string representing the custom currency to be used by the
   *                         toast message to show the amount of coins earned.
   */
  public static void requestNewCoins(String credentialsToken, Context context, SPCurrencyServerListener listener,
                                     String transactionId, Map<String, String> customParams, String customCurrency) {

    requestNewCoins(credentialsToken, context, listener, transactionId, null, customParams, customCurrency);
  }

  /**
   * Sends a request to the SponsorPay currency server to obtain the variation
   * in amount of virtual currency for a given user. Returns immediately, and
   * the answer is delivered to one of the provided listener's callback
   * methods. See {@link SPCurrencyServerListener}. It will use the
   * credentials identified by the provided token id.
   *
   * @param credentialsToken The token id of the credentials to be used.
   * @param context          Android application context.
   * @param listener         {@link SPCurrencyServerListener} which will be notified of the
   *                         result of the request.
   * @param transactionId    Optionally, provide the ID of the latest known transaction.
   *                         The delta of coins will be calculated from this transaction
   *                         (not included) up to the present. Leave it to null to let the
   *                         SDK use the latest transaction ID it kept track of.
   * @param currencyId       Optionally, provide the ID of the currency that you want to
   *                         retrieve. If the provided ID is empty or null, then the
   *                         default currency will be requested. If an invalid currency ID
   *                         will be provided then you will receive an invalid application
   *                         error.
   * @param customParams     A map of extra key/value pairs to add to the request URL.
   * @param customCurrency   A string representing the custom currency to be used by the
   *                         toast message to show the amount of coins earned.
   */
  public static void requestNewCoins(String credentialsToken, Context context, SPCurrencyServerListener listener,
                                     String transactionId, String currencyId, Map<String, String> customParams, String customCurrency) {

    SPVirtualCurrencyConnector vcc = new SPVirtualCurrencyConnector(context, credentialsToken, listener);
    vcc.setCustomParameters(customParams);
    vcc.setCurrency(customCurrency);
    vcc.fetchDeltaOfCoinsForCurrentUserSinceTransactionId(transactionId, currencyId);
  }

  /**
   * Allows the configuration of the Toast notification message that display
   * the amount of coins earned after a successful request to the SponsorPay's
   * Currency server. This is ON by default.
   *
   * @param shouldShowNotification Whether the Toast notification message should be shown or not
   */
  public static void displayNotificationForSuccessfullCoinRequest(boolean shouldShowNotification) {
    SPVirtualCurrencyConnector.shouldShowToastNotification(shouldShowNotification);
  }

  // ================================================================================
  // Rewarded Videos
  // ================================================================================

  /**
   * Requests a Mobile BrandEngage Offer to the SponsorPay servers and
   * registers a listener which will be notified when a response is received.
   *
   * @param activity Calling activity.
   * @param listener {@link SPBrandEngageRequestListener} which will be notified of
   *                 the results of the request.
   * @return A boolean that indicates if the actual request has been made to
   * the server.
   */
  public static boolean getIntentForMBEActivity(Activity activity, SPBrandEngageRequestListener listener) {

    String credentialsToken = SponsorPay.getCurrentCredentials().getCredentialsToken();
    return getIntentForMBEActivity(credentialsToken, activity, listener, null, null, null);
  }

  /**
   * Requests a Mobile BrandEngage Offer to the SponsorPay servers and
   * registers a listener which will be notified when a response is received.
   *
   * @param activity    Calling activity.
   * @param listener    {@link SPBrandEngageRequestListener} which will be notified of
   *                    the results of the request.
   * @param currencyId  The custom currency ID. If the default is required then pass
   *                    null.
   * @param placementId The placement ID.
   * @return A boolean that indicates if the actual request has been made to
   * the server.
   */
  public static boolean getIntentForMBEActivity(Activity activity, SPBrandEngageRequestListener listener,
                                                String placementId) {

    String credentialsToken = SponsorPay.getCurrentCredentials().getCredentialsToken();
    return getIntentForMBEActivity(credentialsToken, activity, listener, placementId);
  }

  /**
   * Requests a Mobile BrandEngage Offer to the SponsorPay servers and
   * registers a listener which will be notified when a response is received.
   *
   * @param activity    Calling activity.
   * @param listener    {@link SPBrandEngageRequestListener} which will be notified of
   *                    the results of the request.
   * @param vcsListener The Virtual Currency Server listener that will be notified
   *                    after a successful engagement.
   * @return A boolean that indicates if the actual request has been made to
   * the server.
   */
  public static boolean getIntentForMBEActivity(Activity activity, SPBrandEngageRequestListener listener,
                                                SPCurrencyServerListener vcsListener) {
    String credentialsToken = SponsorPay.getCurrentCredentials().getCredentialsToken();
    return getIntentForMBEActivity(credentialsToken, activity, listener, null, null, vcsListener, null, null);
  }

  /**
   * Requests a Mobile BrandEngage Offer to the SponsorPay servers and
   * registers a listener which will be notified when a response is received.
   *
   * @param activity    Calling activity.
   * @param listener    {@link SPBrandEngageRequestListener} which will be notified of
   *                    the results of the request.
   * @param vcsListener The Virtual Currency Server listener that will be notified
   *                    after a successful engagement.
   * @param currencyId  The custom currency ID. If the default is required then pass
   *                    null.
   * @param placementId The placement ID.
   * @return A boolean that indicates if the actual request has been made to
   * the server.
   */
  public static boolean getIntentForMBEActivity(Activity activity, SPBrandEngageRequestListener listener,
                                                SPCurrencyServerListener vcsListener, String currencyId, String placementId) {
    String credentialsToken = SponsorPay.getCurrentCredentials().getCredentialsToken();
    return getIntentForMBEActivity(credentialsToken, activity, listener, null, null, vcsListener, currencyId,
                                   placementId);
  }

  /**
   * Requests a Mobile BrandEngage Offer to the SponsorPay servers and
   * registers a listener which will be notified when a response is received.
   *
   * @param credentialsToken The token id of the credentials to be used.
   * @param activity         Calling activity.
   * @param listener         {@link SPBrandEngageRequestListener} which will be notified of
   *                         the results of the request.
   * @return A boolean that indicates if the actual request has been made to
   * the server.
   */
  public static boolean getIntentForMBEActivity(String credentialsToken, Activity activity,
                                                SPBrandEngageRequestListener listener) {
    return getIntentForMBEActivity(credentialsToken, activity, listener, null, null, null, null, null);
  }

  /**
   * Requests a Mobile BrandEngage Offer to the SponsorPay servers and
   * registers a listener which will be notified when a response is received.
   *
   * @param credentialsToken The token id of the credentials to be used.
   * @param activity         Calling activity.
   * @param listener         {@link SPBrandEngageRequestListener} which will be notified of
   *                         the results of the request.
   * @param currencyId       The custom currency ID. If the default is required then pass * @param
   *                         placementId The placement ID.
   * @return A boolean that indicates if the actual request has been made to
   * the server.
   */
  public static boolean getIntentForMBEActivity(String credentialsToken, Activity activity,
                                                SPBrandEngageRequestListener listener, String placementId) {
    return getIntentForMBEActivity(credentialsToken, activity, listener, null, null, null, null, placementId);
  }

  /**
   * Requests a Mobile BrandEngage Offer to the SponsorPay servers and
   * registers a listener which will be notified when a response is received.
   *
   * @param credentialsToken The token id of the credentials to be used.
   * @param activity         Calling activity.
   * @param listener         {@link SPBrandEngageRequestListener} which will be notified of
   *                         the results of the request.
   * @param currencyName     The name of the currency employed by your application. Provide
   *                         null if you don't use a custom currency name.
   * @param parameters       A map of extra key/value pairs to add to the request URL.
   * @param vcsListener      The Virtual Currency Server listener that will be notified
   *                         after a successful engagement.
   * @return A boolean that indicates if the actual request has been made to
   * the server.
   */
  public static boolean getIntentForMBEActivity(String credentialsToken, Activity activity,
                                                SPBrandEngageRequestListener listener, String currencyName, Map<String, String> parameters,
                                                SPCurrencyServerListener vcsListener) {

    return getIntentForMBEActivity(credentialsToken, activity, listener, currencyName, parameters, vcsListener,
                                   null, null);
  }

  /**
   * Requests a Mobile BrandEngage Offer to the SponsorPay servers and
   * registers a listener which will be notified when a response is received.
   * {@link SPBrandEngageRequestListener} which will be notified of the
   * results of the request.
   *
   * @param currencyName The name of the currency employed by your application. Provide
   *                     null if you don't use a custom currency name.
   * @param parameters   A map of extra key/value pairs to add to the request URL.
   * @param vcsListener  The Virtual Currency Server listener that will be notified
   *                     after a successful engagement.
   * @param currencyId   The currency ID that will be used. Provide null if you don't
   *                     use a custom currency ID.
   * @return A boolean that indicates if the actual request has been made to
   * the server.
   */
  public static boolean getIntentForMBEActivity(String credentialsToken, Activity activity,
                                                SPBrandEngageRequestListener listener, String currencyName, Map<String, String> parameters,
                                                SPCurrencyServerListener vcsListener, String currencyId, String placementId) {

    SPBrandEngageClient brandEngageClient = SPBrandEngageClient.INSTANCE;
    boolean canRequestOffers = brandEngageClient.canRequestOffers();
    if (canRequestOffers) {
      parameters = setParameterInMap(parameters, placementId);

      SPCredentials credentials = SponsorPay.getCredentials(credentialsToken);
      brandEngageClient.setCurrencyName(currencyName);
      brandEngageClient.setParametersForRequest(parameters);
      brandEngageClient.setCurrencyListener(vcsListener);
      brandEngageClient.setCurrencyId(currencyId);

      SPBrandEngageRequest request = new SPBrandEngageRequest(credentials, activity, brandEngageClient, listener);
      request.askForOffers();
    }

    return canRequestOffers;
  }

  // ================================================================================
  // Interstitials
  // ================================================================================

  /**
   * Requests an Interstitial Ad to the SponsorPay servers and registers a
   * listener which will be notified when a response is received.
   *
   * @param activity Calling activity.
   * @param listener {@link SPInterstitialRequestListener} which will be notified
   *                 of the results of the request.
   * @return A boolean that indicates if the actual request has been made to
   * the server.
   */
  public static boolean getIntentForInterstitialActivity(Activity activity, SPInterstitialRequestListener listener) {

    String credentialsToken = SponsorPay.getCurrentCredentials().getCredentialsToken();
    String placementId = null;
    return getIntentForInterstitialActivity(credentialsToken, activity, listener, placementId);
  }

  /**
   * Requests an Interstitial Ad to the SponsorPay servers and registers a
   * listener which will be notified when a response is received.
   *
   * @param activity    Calling activity.
   * @param listener    {@link SPInterstitialRequestListener} which will be notified
   *                    of the results of the request.
   * @param placementId The placement ID.
   * @return A boolean that indicates if the actual request has been made to
   * the server.
   */
  public static boolean getIntentForInterstitialActivity(Activity activity, SPInterstitialRequestListener listener,
                                                         String placementId) {

    String credentialsToken = SponsorPay.getCurrentCredentials().getCredentialsToken();
    return getIntentForInterstitialActivity(credentialsToken, activity, listener, placementId);
  }

  /**
   * Requests an Interstitial Ad to the SponsorPay servers and registers a
   * listener which will be notified when a response is received.
   *
   * @param credentialsToken The token id of the credentials to be used.
   * @param activity         Calling activity.
   * @param listener         {@link SPInterstitialRequestListener} which will be notified
   *                         of the results of the request.
   * @return A boolean that indicates if the actual request has been made to
   * the server.
   */
  public static boolean getIntentForInterstitialActivity(String credentialsToken, Activity activity,
                                                         SPInterstitialRequestListener listener) {
    Map<String, String> parameters = null;
    return getIntentForInterstitialActivity(credentialsToken, activity, listener, parameters, null);
  }

  /**
   * Requests an Interstitial Ad to the SponsorPay servers and registers a
   * listener which will be notified when a response is received.
   *
   * @param credentialsToken The token id of the credentials to be used.
   * @param activity         Calling activity.
   * @param listener         {@link SPInterstitialRequestListener} which will be notified
   *                         of the results of the request.
   * @return A boolean that indicates if the actual request has been made to
   * the server.
   */
  public static boolean getIntentForInterstitialActivity(String credentialsToken, Activity activity,
                                                         SPInterstitialRequestListener listener, String placementId) {
    return getIntentForInterstitialActivity(credentialsToken, activity, listener, null, placementId);
  }

  /**
   * Requests an Interstitial Ad to the SponsorPay servers and registers a
   * listener which will be notified when a response is received.
   *
   * @param credentialsToken The token id of the credentials to be used.
   * @param activity         Calling activity.
   * @param listener         {@link SPInterstitialRequestListener} which will be notified
   *                         of the results of the request.
   * @param parameters       A map of extra key/value pairs to add to the request URL.
   * @param placementId      The placement ID.
   * @return A boolean that indicates if the actual request has been made to
   * the server.
   */
  public static boolean getIntentForInterstitialActivity(String credentialsToken, Activity activity,
                                                         SPInterstitialRequestListener listener, Map<String, String> parameters) {

    return getIntentForInterstitialActivity(credentialsToken, activity, listener, parameters, null);
  }

  /**
   * Requests an Interstitial Ad to the SponsorPay servers and registers a
   * listener which will be notified when a response is received.
   *
   * @param credentialsToken The token id of the credentials to be used.
   * @param activity         Calling activity.
   * @param listener         {@link SPInterstitialRequestListener} which will be notified
   *                         of the results of the request.
   * @param parameters       A map of extra key/value pairs to add to the request URL.
   * @param placementId      The placement ID.
   * @return A boolean that indicates if the actual request has been made to
   * the server.
   */
  public static boolean getIntentForInterstitialActivity(String credentialsToken, Activity activity,
                                                         SPInterstitialRequestListener listener, Map<String, String> parameters, String placementId) {

    SPInterstitialClient interstitialClient = SPInterstitialClient.INSTANCE;
    boolean canRequestAds = interstitialClient.canRequestAds();
    if (canRequestAds) {

      parameters = setParameterInMap(parameters, placementId);

      SPCredentials credentials = SponsorPay.getCredentials(credentialsToken);
      interstitialClient.setRequestListener(listener);
      interstitialClient.setCustomParameters(parameters);
      interstitialClient.requestAds(credentials, activity);
    }
    return canRequestAds;
  }

  private static Map<String, String> setParameterInMap(Map<String, String> parameters, String placementId) {
    if (parameters == null) {
      parameters = new HashMap<String, String>();
    }

    if (StringUtils.nullOrEmpty(placementId)) {
      placementId = "";
    }
    parameters.put(PLACEMENT_KEY, placementId);
    return parameters;
  }

}