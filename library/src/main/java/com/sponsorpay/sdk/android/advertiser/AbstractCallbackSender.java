/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.advertiser;

import android.os.AsyncTask;
import com.sponsorpay.sdk.android.credentials.SPCredentials;
import com.sponsorpay.sdk.android.utils.SPHttpConnection;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.StringUtils;
import com.sponsorpay.sdk.android.utils.UrlBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Runs the Advertiser Callback HTTP request in the background.
 */
public abstract class AbstractCallbackSender extends AsyncTask<String, Void, Boolean> {

  private static final String TAG = "AbstractCallbackSender";

  /**
   * HTTP status code that the response should have in order to determine that the API has been contacted successfully.
   */
  protected static final int SUCCESSFUL_HTTP_STATUS_CODE = 200;

  /**
   * SubID URL parameter key
   */
  private static final String INSTALL_SUBID_KEY = "subid";

  /**
   * The key for encoding the parameter corresponding to whether a previous invocation of the advertiser callback had received a successful response.
   */
  protected static final String SUCCESSFUL_ANSWER_RECEIVED_KEY = "answer_received";

  private SPCredentials mCredentials;

  protected SponsorPayAdvertiserState mState;

  /**
   * Map of custom parameters to be sent in the callback request.
   */
  private Map<String, String> mCustomParams;

  /**
   * <p>
   * Constructor.
   * </p>
   * See {@link SPCredentials} and {@link SponsorPayAdvertiserState}.
   *
   * @param credentials the credentials used for this callback
   * @param state       the advertiser state for getting information about previous callbacks
   */
  public AbstractCallbackSender(SPCredentials credentials, SponsorPayAdvertiserState state) {
    mState = state;
    mCredentials = credentials;
  }

  /**
   * Sets the map of custom parameters to be sent in the callback request.
   */
  public void setCustomParams(Map<String, String> customParams) {
    mCustomParams = customParams;
  }

  /**
   * Triggers the callback request that contacts the SponsorPay Advertiser API.
   */
  public void trigger() {
    // The final URL with parameters is built right away, to make sure that possible runtime
    // exceptions triggered from the SDK to the integrator's code --due to a missing App ID
    // value or to an invalid collection of custom parameters-- are triggered on the calling
    // thread.
    execute((String[]) null);
  }

  private String buildUrl() {
    // Prepare HTTP request by URL-encoding the device information

    Map<String, String> params = getParams();
    if (params == null) {
      params = new HashMap<String, String>();
    }

    if (mCustomParams != null) {
      params.putAll(mCustomParams);
    }

    params.put(SUCCESSFUL_ANSWER_RECEIVED_KEY, getAnswerReceived());

    String installSubId = mState.getInstallSubId();

    if (StringUtils.notNullNorEmpty(installSubId)) {
      params.put(INSTALL_SUBID_KEY, installSubId);
    }

    String callbackUrl = UrlBuilder.newBuilder(getBaseUrl(), mCredentials).addExtraKeysValues(params).addScreenMetrics().buildUrl();

    SponsorPayLogger.d(TAG, "Callback will be sent to: " + callbackUrl);

    return callbackUrl;
  }

  /**
   * <p>
   * Method overridden from {@link AsyncTask}. Executed on a background thread, runs the API
   * contact request.
   * </p>
   * <p/>
   * Encodes the host information in the request URL, runs the request, waits for the response,
   * parses its status code and lets the UI thread receive the result.
   * <p/>
   *
   * @param params Only one parameter of type {@link String} containing the request URL is expected.
   * @return True for a successful request, false otherwise. This value will be communicated to
   * the UI thread by the Android {@link AsyncTask} implementation.
   */
  @Override
  protected Boolean doInBackground(String... params) {
    Thread.currentThread().setName(TAG);
    Boolean returnValue = null;

    String callbackUrl = buildUrl();

    try {
      int responseStatusCode = SPHttpConnection.getConnection(callbackUrl).open().getResponseCode();
      returnValue = responseStatusCode == SUCCESSFUL_HTTP_STATUS_CODE;
      SponsorPayLogger.d(TAG, "Server returned status code: " + responseStatusCode);
    } catch (Exception e) {
      returnValue = false;
      SponsorPayLogger.e(TAG, "An exception occurred when trying to send advertiser callback: " + e);
    }
    return returnValue;
  }

  /**
   * This method is called by the Android {@link AsyncTask} implementation in the UI thread (or
   * the thread which invoked {@link #trigger()}) when
   * {@link #doInBackground(String...)} returns.
   *
   * @param callbackWasSuccessful true if the response has a successful status code (equal to
   *                              {@link #SUCCESSFUL_HTTP_STATUS_CODE}). false otherwise.
   */
  @Override
  protected void onPostExecute(Boolean callbackWasSuccessful) {
    processRequest(callbackWasSuccessful);
  }

  protected abstract String getBaseUrl();

  protected abstract Map<String, String> getParams();

  protected abstract String getAnswerReceived();

  protected abstract void processRequest(Boolean callbackWasSuccessful);
}
