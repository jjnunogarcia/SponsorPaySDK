package com.sponsorpay.sdk.android.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import com.sponsorpay.sdk.android.advertiser.SponsorPayAdvertiserState;
import com.sponsorpay.sdk.android.credentials.SPCredentials;
import com.sponsorpay.sdk.android.utils.SPHttpConnection;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.StringUtils;
import com.sponsorpay.sdk.android.utils.UrlBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * User: jesus
 * Date: 19/03/15
 *
 * @author jjnunogarcia@gmail.com
 */
public abstract class AbstractService extends IntentService {
  protected static final String TAG                            = AbstractService.class.getSimpleName();
  protected static final int    SUCCESSFUL_HTTP_STATUS_CODE    = 200;
  public static final    String KEY_CREDENTIALS                = "key_credentials";
  public static final    String KEY_CUSTOM_PARAMS              = "key_custom_params";
  protected static final String INSTALL_SUBID_KEY              = "subid";
  protected static final String SUCCESSFUL_ANSWER_RECEIVED_KEY = "answer_received";
  public static final    String KEY_RESULT                     = "key_result";
  public static final    String BROADCAST_ACTION               = "com.sponsorpay.sdk.android.services.BROADCAST";

  protected SPCredentials             credentials;
  protected SponsorPayAdvertiserState advertiserState;
  protected Map<String, String>       customParams;

  protected AbstractService() {
    super(TAG);
  }

  protected AbstractService(String name) {
    super(name);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    advertiserState = new SponsorPayAdvertiserState(getApplicationContext());
    Bundle arguments = intent.getExtras();

    if (arguments != null) {
      if (arguments.containsKey(KEY_CREDENTIALS)) {
        credentials = arguments.getParcelable(KEY_CREDENTIALS);
      }

      if (arguments.containsKey(KEY_CUSTOM_PARAMS)) {
        customParams = (Map<String, String>) arguments.getSerializable(KEY_CUSTOM_PARAMS);
      }
    }

    String callbackUrl = buildUrl();
    boolean result = false;

    try {
      int responseStatusCode = SPHttpConnection.getConnection(callbackUrl).open().getResponseCode();
      SponsorPayLogger.d(TAG, "Server returned status code: " + responseStatusCode);
      result = responseStatusCode == SUCCESSFUL_HTTP_STATUS_CODE;
    } catch (Exception e) {
      SponsorPayLogger.e(TAG, "An exception occurred when trying to send advertiser callback: " + e);
    }

    processRequest(result);
  }

  private String buildUrl() {
    // Prepare HTTP request by URL-encoding the device information

    Map<String, String> params = getParams();
    if (params == null) {
      params = new HashMap<>();
    }

    if (customParams != null) {
      params.putAll(customParams);
    }

    params.put(SUCCESSFUL_ANSWER_RECEIVED_KEY, getAnswerReceived());

    String installSubId = advertiserState.getInstallSubId();

    if (StringUtils.notNullNorEmpty(installSubId)) {
      params.put(INSTALL_SUBID_KEY, installSubId);
    }

    String callbackUrl = UrlBuilder.newBuilder(getBaseUrl(), credentials).addExtraKeysValues(params).addScreenMetrics().buildUrl();

    SponsorPayLogger.d(TAG, "Callback will be sent to: " + callbackUrl);

    return callbackUrl;
  }

  protected abstract String getBaseUrl();

  protected abstract Map<String, String> getParams();

  protected abstract String getAnswerReceived();

  protected abstract void processRequest(boolean callbackWasSuccessful);
}
