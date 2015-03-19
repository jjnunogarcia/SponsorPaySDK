/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */
package com.sponsorpay.sdk.android.mediation;

import com.sponsorpay.sdk.android.publisher.interstitial.SPInterstitialActivity;

/**
 * <p>
 * Interface that defines the methods to be implemented in order to get notifications
 * for the cases of back or home buttons clicked.
 * </p>
 * <p/>
 * Used internally by the {@link SPInterstitialActivity} in order to be notified
 * when the onBackPressed and the onUserLeaveHint methods are called accordingly.
 */
public interface SPMediationUserActivityListener {

  /**
   * Invoked when the back button clicked.
   */
  public void notifyOnBackPressed();

  /**
   * Invoked when the home button is clicked.
   */
  public void notifyOnHomePressed();

}
