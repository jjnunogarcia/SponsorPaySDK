package com.sponsorpay.sdk.android.mediation;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import com.sponsorpay.sdk.android.credentials.SPCredentials;
import com.sponsorpay.sdk.android.utils.*;

import java.util.Map;
import java.util.Map.Entry;

/**
 * <p>
 * Requests and loads a resource using the HTTP GET method in the background by using
 * the AsyncTaskRequester doInBackground method (Uses the Android {@link AsyncTask}
 * mechanism.). Will save the response on the shared preferences and will update
 * the SPMediationConfigurator's Map<String, Map<String, Object>> mConfigurations.
 * </p>
 */
public class SPMediationConfigurationRequester extends SignedResponseRequester<SignedServerResponse> {

  public static final  String TAG                        = "ConfigurationRequester";
  private static final String SERVER_SIDE_CONFIG_URL_KEY = "config";

  private Activity mActivity;
  private String   mSecurityToken;


  public static void requestConfig(SPCredentials credentials, Activity activity) {

    UrlBuilder urlBuilder = UrlBuilder.newBuilder(getBaseUrl(), credentials).addSignature();

    new SPMediationConfigurationRequester(activity, credentials.getSecurityToken()).execute(urlBuilder);
  }

  private static String getBaseUrl() {
    return SponsorPayBaseUrlProvider.getBaseUrl(SERVER_SIDE_CONFIG_URL_KEY);
  }

  private SPMediationConfigurationRequester(Activity activity, String securityToken) {
    mActivity = activity;
    mSecurityToken = securityToken;
  }


  /**
   * @param result - the body from the HTTP request.
   */
  @Override
  protected void onPostExecute(SignedServerResponse result) {
    // even though this runs inside UI thread, the method below will spawn a new background thread
    SPMediationCoordinator.INSTANCE.startMediationAdapters(mActivity);
  }


  @Override
  protected String getTag() {
    return TAG;
  }

  /**
   * Check if the response body retrieved from the HTTP request
   * on AsyncTaskRequester is empty or null. In case that isn't,
   * saves the response as a shared preference. Finally we've logged
   * if the shared preference commit was successful or not. This
   * process is taking place on a background thread.
   *
   * @param signedServerResponse - the body from the HTTP request.
   */
  @Override
  protected SignedServerResponse parsedSignedResponse(SignedServerResponse signedServerResponse) {
    String json = StringUtils.EMPTY_STRING;
    SharedPreferences sharedpreferences = mActivity.getSharedPreferences(TAG, Context.MODE_PRIVATE);
    if (signedServerResponse != null && !hasErrorStatusCode(signedServerResponse.getStatusCode())) {

      if (verifySignature(signedServerResponse, mSecurityToken)) {
        SponsorPayLogger.d(TAG, "The signature is valid, proceeding...");

        String responseBody = signedServerResponse.getResponseBody();

        if (StringUtils.notNullNorEmpty(responseBody)) {

          Editor editor = sharedpreferences.edit();
          editor.putString(TAG, responseBody);

          if (editor.commit()) {
            SponsorPayLogger.d(TAG, "Server Side Configuration has been saved successfully.");
          } else {
            SponsorPayLogger.d(TAG, "Failed to save Server Side Configuration.");
          }

          json = responseBody;
        }
      } else {
        SponsorPayLogger.d(TAG, "Invalid signature, those configs will not be used.");
      }
    }
    if (StringUtils.nullOrEmpty(json)) {
      SponsorPayLogger.d(TAG, "No configs from the server, fallback to cached version.");
      // retrieve info from the store preferencs, if any
      json = sharedpreferences.getString(TAG, StringUtils.EMPTY_STRING);
      if (StringUtils.nullOrEmpty(json)) {
        SponsorPayLogger.d(TAG, "There were no cached version to use.");
      } else {
        SponsorPayLogger.d(TAG, "Using cached json file");
      }
    }
    overrideConfig(json);

    return signedServerResponse;
  }

  private void overrideConfig(String json) {
    if (StringUtils.notNullNorEmpty(json)) {
      Map<String, Map<String, Object>> settingsMapFromResponseBody = SPMediationConfigurator
          .parseConfiguration(json);

      // iterate on all server side configurations
      for (Entry<String, Map<String, Object>> entry : settingsMapFromResponseBody
          .entrySet()) {

        String network = entry.getKey();
        Map<String, Object> serverConfigs = entry.getValue();
        Map<String, Object> localConfigs = SPMediationConfigurator.INSTANCE
            .getConfigurationForAdapter(network);

        if (localConfigs != null) {
          serverConfigs.putAll(localConfigs);
        }
        SPMediationConfigurator.INSTANCE.setConfigurationForAdapter(network,
                                                                    serverConfigs);
      }
    } else {
      SponsorPayLogger.d(TAG, "There were no credentials to override");
    }
  }

  @Override
  protected SignedServerResponse noConnectionResponse(Throwable t) {
    //do nothing
    return null;
  }

}