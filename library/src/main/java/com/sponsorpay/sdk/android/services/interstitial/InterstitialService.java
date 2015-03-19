package com.sponsorpay.sdk.android.services.interstitial;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import com.sponsorpay.sdk.android.credentials.SPCredentials;
import com.sponsorpay.sdk.android.publisher.interstitial.SPInterstitialAd;
import com.sponsorpay.sdk.android.publisher.interstitial.SPInterstitialClient;
import com.sponsorpay.sdk.android.publisher.interstitial.SPInterstitialEvent;
import com.sponsorpay.sdk.android.utils.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * User: jesus
 * Date: 19/03/15
 *
 * @author j.nuno@klara.com
 */
public class InterstitialService extends IntentService {
  public static final  String   TAG                         = InterstitialService.class.getSimpleName();
  public static final  String   BROADCAST_ACTION            = "com.sponsorpay.sdk.android.services.interstitial.BROADCAST";
  public static final  String   KEY_CREDENTIALS             = "key_credentials";
  public static final  String   KEY_REQUEST_ID              = "key_request_id";
  public static final  String   KEY_AD                      = "key_ad";
  public static final  String   KEY_EVENT                   = "key_event";
  public static final  String   KEY_RESULT                  = "key_result";
  private static final String   TRACKERL_URL_KEY            = "tracker";
  private static final int      SUCCESSFUL_HTTP_STATUS_CODE = 200;
  private static       String[] additionalParamKey          = {"ad_format", "rewarded"};
  private static       String[] additionalParamValues       = {"interstitial", "0"};
  private SPCredentials       credentials;
  private String              requestId;
  private SPInterstitialAd    ad;
  private SPInterstitialEvent event;

  public InterstitialService() {
    super(TAG);
  }

  public InterstitialService(String name) {
    super(name);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Bundle arguments = intent.getExtras();

    if (arguments != null) {
      if (arguments.containsKey(KEY_CREDENTIALS)) {
        credentials = arguments.getParcelable(KEY_CREDENTIALS);
      }

      if (arguments.containsKey(KEY_REQUEST_ID)) {
        requestId = arguments.getString(KEY_REQUEST_ID);
      }

      if (arguments.containsKey(KEY_AD)) {
        ad = arguments.getParcelable(KEY_AD);
      }

      if (arguments.containsKey(KEY_EVENT)) {
        event = (SPInterstitialEvent) arguments.getSerializable(KEY_EVENT);
      }
    }

    if (credentials == null || StringUtils.nullOrEmpty(requestId) || event == null) {
      SponsorPayLogger.d(TAG, "The event cannot be sent, a required field is missing.");
    } else {
      if (ad != null) {
        SponsorPayLogger.d(TAG, String.format("Notifying tracker of event=%s with request_id=%s for ad_id=%s and provider_type=%s ", event, requestId, ad.getAdId(), ad.getProviderType()));
      } else {
        SponsorPayLogger.d(TAG, String.format("Notifying tracker of event=%s with request_id=%s", event, requestId));
      }

      boolean returnValue = false;

      String url = getUrlBuilder(credentials, requestId, ad, event).buildUrl();

      SponsorPayLogger.d(TAG, "Sending event to " + url);

      try {
        returnValue = SPHttpConnection.getConnection(url).open().getResponseCode() == SUCCESSFUL_HTTP_STATUS_CODE;
      } catch (Exception e) {
        SponsorPayLogger.e(TAG, "An exception occurred when trying to send advertiser callback: " + e);
      }

      handleResult(returnValue);
    }
  }

  private static UrlBuilder getUrlBuilder(SPCredentials credentials, String requestId, SPInterstitialAd ad, SPInterstitialEvent event) {
    UrlBuilder builder = UrlBuilder.newBuilder(getBaseUrl(), credentials)
                                   .addKeyValue(SPInterstitialClient.SP_REQUEST_ID_PARAMETER_KEY, requestId)
                                   .addKeyValue("event", event.toString())
                                   .addExtraKeysValues(UrlBuilder.mapKeysToValues(additionalParamKey, additionalParamValues))
                                   .addScreenMetrics();

    if (ad != null) {
      builder.addKeyValue("ad_id", ad.getAdId()).addKeyValue("provider_type", ad.getProviderType());

      JSONObject trackingParameters = ad.getTrackingParameters();
      if (trackingParameters != null) {
        appendTrackingParametersToURL(builder, trackingParameters);
      }

    }
    return builder;
  }

  private static void appendTrackingParametersToURL(UrlBuilder builder, JSONObject trackingParameters) {
    Iterator<?> keys = trackingParameters.keys();
    while (keys.hasNext()) {
      String key = (String) keys.next();
      Object value = null;
      try {
        value = trackingParameters.get(key);
        if (value != null) {
          builder.addKeyValue(key, value.toString());
        }
      } catch (JSONException exception) {
        SponsorPayLogger.e(TAG, exception.getMessage());
      }
    }
  }

  private static String getBaseUrl() {
    return SponsorPayBaseUrlProvider.getBaseUrl(TRACKERL_URL_KEY);
  }

  private void handleResult(boolean returnValue) {
    Intent resultIntent = new Intent(BROADCAST_ACTION);
    Bundle returnExtras = new Bundle();
    returnExtras.putBoolean(KEY_RESULT, returnValue);
    resultIntent.putExtras(returnExtras);
    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(resultIntent);
  }
}
