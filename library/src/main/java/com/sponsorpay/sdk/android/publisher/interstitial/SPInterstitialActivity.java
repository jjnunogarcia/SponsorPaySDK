/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.interstitial;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import com.sponsorpay.sdk.android.mediation.SPMediationUserActivityListener;

/**
 * <p>
 * Activity that is responsible for showing an interstitial ad
 * </p>
 * <p/>
 * When closed, it returns the ad status as a bundle extra with
 * the key {@link SPInterstitialActivity#SP_AD_STATUS}
 */
public class SPInterstitialActivity extends Activity implements SPInterstitialAdListener {

  public final static String SP_AD_STATUS = "AD_STATUS";

  public final static String SP_ERROR_MESSAGE = "ERROR_MESSAGE";

  private SPMediationUserActivityListener mActivityListener;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);

    SPInterstitialClient.INSTANCE.setAdStateListener(SPInterstitialActivity.this);
    SPInterstitialClient.INSTANCE.showInterstitial(SPInterstitialActivity.this);
  }

  // SPInterstitialAdListener
  @Override
  public void onSPInterstitialAdShown() {
    //do nothing
  }

  @Override
  public void onSPInterstitialAdClosed(SPInterstitialAdCloseReason reason) {
    Intent intent = new Intent();
    intent.putExtra(SP_AD_STATUS, reason);
    setResult(RESULT_OK, intent);
    finish();
  }

  @Override
  public void onSPInterstitialAdError(String error) {
    Intent intent = new Intent();
    intent.putExtra(SP_AD_STATUS, SPInterstitialAdCloseReason.ReasonError);
    intent.putExtra(SP_ERROR_MESSAGE, error);
    setResult(RESULT_OK, intent);
    finish();
  }

  public void setMarketPlaceInterstitialListener(SPMediationUserActivityListener listener) {
    if (mActivityListener == null) {
      mActivityListener = listener;
    }
  }

  @Override
  protected void onUserLeaveHint() {
    if (mActivityListener != null) {
      mActivityListener.notifyOnHomePressed();
    }
    super.onUserLeaveHint();
  }


  @Override
  public void onBackPressed() {
    if (mActivityListener != null) {
      mActivityListener.notifyOnBackPressed();
    }
    super.onBackPressed();
  }

}
