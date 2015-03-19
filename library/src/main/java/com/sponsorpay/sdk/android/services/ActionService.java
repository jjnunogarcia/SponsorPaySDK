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
public class ActionService extends AbstractService {
  public static final  String TAG             = ActionService.class.getSimpleName();
  private static final String ACTIONS_URL_KEY = "actions";
  public static final  String ACTION_ID_KEY   = "action_id";

  public ActionService() {
    super(TAG);
  }

  public ActionService(String name) {
    super(name);
  }

  @Override
  protected String getAnswerReceived() {
    return advertiserState.getCallbackReceivedSuccessfulResponse(customParams.get(ACTION_ID_KEY));
  }

  @Override
  protected String getBaseUrl() {
    return SponsorPayBaseUrlProvider.getBaseUrl(ACTIONS_URL_KEY);
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
