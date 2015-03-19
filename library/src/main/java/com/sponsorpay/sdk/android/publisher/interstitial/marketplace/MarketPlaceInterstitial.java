/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.interstitial.marketplace;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import com.sponsorpay.sdk.android.mediation.SPMediationUserActivityListener;
import com.sponsorpay.sdk.android.mediation.marketplace.MarketPlaceAdapter;
import com.sponsorpay.sdk.android.publisher.interstitial.SPInterstitialActivity;
import com.sponsorpay.sdk.android.publisher.interstitial.SPInterstitialAd;
import com.sponsorpay.sdk.android.publisher.interstitial.marketplace.view.InterstitialCloseButtonRelativeLayout;
import com.sponsorpay.sdk.android.publisher.interstitial.mediation.SPInterstitialMediationAdapter;
import com.sponsorpay.sdk.android.utils.HostInfo;
import com.sponsorpay.sdk.android.utils.SPWebClient;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.StringUtils;

public class MarketPlaceInterstitial extends SPInterstitialMediationAdapter<MarketPlaceAdapter> implements
                                                                                                SPMediationUserActivityListener, OnClickListener {

  private static final String TAG = "MarketPlaceInterstitial";

  protected static final int CREATE_WEBVIEW = 0;
  protected static final int LOAD_HTML      = 1;

  private Handler       mMainHandler;
  private WebView       mWebView;
  private WebViewClient mWebClient;

  private FrameLayout mMainLayout;
  private Activity    mActivity;
  private String      mOrientation;
  private String      mRotation;

  private String mHtmlContent;

  private InterstitialCloseButtonRelativeLayout mCloseButtonLayout;

  public MarketPlaceInterstitial(MarketPlaceAdapter adapter) {
    super(adapter);

    mMainHandler = new Handler(Looper.getMainLooper()) {
      @Override
      public void handleMessage(Message msg) {
        switch (msg.what) {
          case CREATE_WEBVIEW:

            Context context = (Context) msg.obj;

            mWebView = new WebView(context);

            createMainLayout(context);

            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.setWebViewClient(getWebClient());
            break;

          case LOAD_HTML:

            if (StringUtils.notNullNorEmpty(mHtmlContent)) {
              mWebView.loadDataWithBaseURL(null, mHtmlContent, null, "UTF-8", null);
            }

            break;

          default:
            break;
        }
      }
    };
  }

  @Override
  public boolean isAdAvailable(Context context, SPInterstitialAd ad) {
    removeAttachedLayout();

    mHtmlContent = ad.getContextData().get("html");
    boolean hasHtml = StringUtils.notNullNorEmpty(mHtmlContent);

    mOrientation = ad.getContextData().get("orientation");
    mRotation = ad.getContextData().get("rotation");

    if (hasHtml) {
      if (mWebView == null) {
        Message msg = Message.obtain(mMainHandler);
        msg.what = CREATE_WEBVIEW;
        msg.obj = context;
        msg.sendToTarget();
      }
      setAdAvailable();
    }
    return hasHtml;
  }

  @Override
  protected boolean show(Activity parentActivity) {
    loadHtml();
    mActivity = parentActivity;
    setOrientation();

    if (mActivity instanceof SPInterstitialActivity) {
      ((SPInterstitialActivity) mActivity).setMarketPlaceInterstitialListener(MarketPlaceInterstitial.this);
    }

    FrameLayout.LayoutParams layoutparams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                                                                         ViewGroup.LayoutParams.FILL_PARENT);
    parentActivity.setContentView(mMainLayout, layoutparams);

    fireImpressionEvent();

    return true;
  }

  private void loadHtml() {
    Message msg = Message.obtain(mMainHandler);
    msg.what = LOAD_HTML;
    msg.sendToTarget();
  }

  @Override
  protected void checkForAds(Context context) {
    // do nothing
  }

  private WebViewClient getWebClient() {
    if (mWebClient == null) {

      mWebClient = new SPWebClient(null) {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
          boolean shouldOverrideUrlLoading = super.shouldOverrideUrlLoading(view, url);
          if (!shouldOverrideUrlLoading) {
            fireClickAndOpenURL(url);
          }
          return true;
        }

        @Override
        protected void onSponsorPayExitScheme(int resultCode, String targetUrl) {
          fireClickAndOpenURL(targetUrl);
        }

        private void fireClickAndOpenURL(String targetUrl) {
          Activity hostActivity = getHostActivity();

          if (null == hostActivity) {
            return;
          }

          fireClickEvent();
          launchActivityWithUrl(targetUrl);
        }

        @Override
        protected Activity getHostActivity() {
          return mActivity;
        }

        @Override
        protected void processSponsorPayScheme(String host, Uri uri) {
          // nothing more to do, everything is done by super class
        }

        @Override
        protected void onTargetActivityStart(String targetUrl) {
          // nothing to do
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

          String errorMessage = String.format("Interstitials WebView triggered an error. "
                                              + "Error code: %d, error description: %s. Failing URL: %s", errorCode, description,
                                              failingUrl);

          SponsorPayLogger.e(TAG, errorMessage);

          fireShowErrorEvent(errorMessage);
        }
      };
    }

    return mWebClient;
  }

  private void createMainLayout(Context context) {
    // main FrameLayout which will host the webview and the close button
    mMainLayout = new FrameLayout(context);

    // Instance of the close button relative layout, which will be generated
    // dynamically
    mCloseButtonLayout = new InterstitialCloseButtonRelativeLayout(context);

    // the webview
    mWebView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                                                          ViewGroup.LayoutParams.FILL_PARENT));

    // attach on the main layout the webview and the close button.
    mMainLayout.addView(mWebView);

    mMainLayout.addView(mCloseButtonLayout);

    // Set a listener for the close button
    mCloseButtonLayout.setOnClickListener(this);
  }

  /**
   * Is removing all the views from the dynamically generated marketplace
   * interstitials.
   */
  private void removeAttachedLayout() {
    if (mMainLayout != null) {
      ViewGroup parentViewGroup = (ViewGroup) mMainLayout.getParent();
      if (parentViewGroup != null) {
        parentViewGroup.removeAllViews();
      }
    }
    mHtmlContent = null;
  }

  @Override
  public void notifyOnBackPressed() {
    fireCloseEventAndRemoveAttachedView();
  }

  @Override
  public void notifyOnHomePressed() {
    fireCloseEventAndRemoveAttachedView();
  }

  /**
   * Listener which will be called when the close button will be clicked.
   */
  @Override
  public void onClick(View v) {
    fireCloseEventAndRemoveAttachedView();
  }

  private void fireCloseEventAndRemoveAttachedView() {
    fireCloseEvent();
    removeAttachedLayout();
  }

  /**
   * Method which is setting the orientation according to the one that has
   * been provided via the server's JSON response. If the JSON doesn't have
   * that response, then set the existing orientation. In both occasions we
   * lock the screen orientation, so no orientation changes can take place.
   */
  private void setOrientation() {

    int rotationAsInt = Integer.parseInt(mRotation);
    boolean hasDeviceReverseOrientation = HostInfo.getHostInfo(null).hasDeviceRevserseOrientation();

    if (mOrientation.equalsIgnoreCase("portrait")) {

      if (hasDeviceReverseOrientation) {

        if (rotationAsInt == Surface.ROTATION_90) {
          lockWithProvidedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        } else {
          lockWithProvidedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

      } else {

        // if the rotation is equal to 180 degrees, then we have
        // the reverse portrait orientation.
        if (rotationAsInt == Surface.ROTATION_180) {
          lockWithProvidedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        } else {
          lockWithProvidedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
      }

    } else if (mOrientation.equalsIgnoreCase("landscape")) {

      if (hasDeviceReverseOrientation) {

        if (rotationAsInt == Surface.ROTATION_180) {
          lockWithProvidedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        } else {
          lockWithProvidedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

      } else {

        // if the rotation is equal to 270 degrees, then we have
        // the reverse landscape orientation.
        if (rotationAsInt == Surface.ROTATION_270) {
          lockWithProvidedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        } else {
          lockWithProvidedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
      }
    }
  }

  /**
   * Method which is sets and locks the provided orientation.
   *
   * @param providedOrientation
   */
  private void lockWithProvidedOrientation(int providedOrientation) {
    mActivity.setRequestedOrientation(providedOrientation);
  }

}
