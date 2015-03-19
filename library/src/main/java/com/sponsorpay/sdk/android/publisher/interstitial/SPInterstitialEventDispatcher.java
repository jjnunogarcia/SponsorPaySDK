/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.interstitial;

import android.os.AsyncTask;
import com.sponsorpay.sdk.android.credentials.SPCredentials;
import com.sponsorpay.sdk.android.utils.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * <p>
 * Internal class dispatches the interstitial events.
 * </p>
 * <p/>
 * This class is not meant to be used directly.
 * It is used by {@link SPInterstitialClient}.
 */
public class SPInterstitialEventDispatcher extends AsyncTask<UrlBuilder, Void, Boolean> {
  private static final String TAG                         = "SPInterstitialEventDispatcher";
  private static final String TRACKERL_URL_KEY            = "tracker";
  private static final int    SUCCESSFUL_HTTP_STATUS_CODE = 200;

  // those values are hardcoded for now
  private static String[] additionalParamKey    = {"ad_format", "rewarded"};
  private static String[] additionalParamValues = {"interstitial", "0"};

  public static void trigger(SPCredentials credentials, String requestId, SPInterstitialAd ad, SPInterstitialEvent event) {
    if (credentials == null || StringUtils.nullOrEmpty(requestId) || event == null) {
      SponsorPayLogger.d(TAG, "The event cannot be sent, a required field is missing.");
    } else {
      if (ad != null) {
        SponsorPayLogger.d(TAG, String.format("Notifiying tracker of event=%s with request_id=%s for ad_id=%s and provider_type=%s ", event, requestId, ad.getAdId(), ad.getProviderType()));
      } else {
        SponsorPayLogger.d(TAG, String.format("Notifiying tracker of event=%s with request_id=%s", event, requestId));
      }
      new SPInterstitialEventDispatcher().execute(getUrlBuilder(credentials, requestId, ad, event));
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

  private SPInterstitialEventDispatcher() {}

  @Override
  protected Boolean doInBackground(UrlBuilder... params) {
    Thread.currentThread().setName(TAG);
    Boolean returnValue = false;

    String url = params[0].buildUrl();

    SponsorPayLogger.d(TAG, "Sending event to " + url);

    try {
      returnValue = SPHttpConnection.getConnection(url).open().getResponseCode() == SUCCESSFUL_HTTP_STATUS_CODE;
    } catch (Exception e) {
      SponsorPayLogger.e(TAG, "An exception occurred when trying to send advertiser callback: " + e);
    }

    return returnValue;
  }

}
