package com.sponsorpay.sdk.android.services;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import com.sponsorpay.sdk.android.utils.SponsorPayBaseUrlProvider;

import java.util.Map;

/**
 * User: jesus
 * Date: 19/03/15
 *
 * @author jjnunogarcia@gmail.com
 */
public class InstallService extends AbstractService {
  public static final  String TAG              = InstallService.class.getSimpleName();
  private static final String INSTALLS_URL_KEY = "installs";

  public InstallService() {
    super(TAG);
  }

  public InstallService(String name) {
    super(name);
  }

  @Override
  protected String getBaseUrl() {
    return SponsorPayBaseUrlProvider.getBaseUrl(INSTALLS_URL_KEY);
  }

  @Override
  protected String getAnswerReceived() {
    return advertiserState.getCallbackReceivedSuccessfulResponse(null);
  }

  @Override
  protected Map<String, String> getParams() {
    return null;
  }

  @Override
  protected void processRequest(boolean callbackWasSuccessful) {
//    advertiserState.setCallbackReceivedSuccessfulResponse(null, true); // TODO shouldn't we pass 'callbackWasSuccessful' ??
    advertiserState.setCallbackReceivedSuccessfulResponse(null, callbackWasSuccessful);

    Intent resultIntent = new Intent(BROADCAST_ACTION);
    Bundle returnExtras = new Bundle();
    returnExtras.putBoolean(KEY_RESULT, callbackWasSuccessful);
    resultIntent.putExtras(returnExtras);
    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(resultIntent);
  }
}
