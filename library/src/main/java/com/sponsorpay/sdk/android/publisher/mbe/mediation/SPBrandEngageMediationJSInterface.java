/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.mbe.mediation;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.webkit.JavascriptInterface;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SPBrandEngageMediationJSInterface implements ValueCallback<String> {

  private static final String TAG = "SPBrandEngageMediationJSInterface";

  private static final String SP_GET_OFFERS   = "Sponsorpay.MBE.SDKInterface.do_getOffer()";
  private static final String SP_TPN_JSON_KEY = "uses_tpn";

  private static final int CALLBACK_TIMEOUT = 5689;

  private ValueCallback<Boolean> mCallback;
  private Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case CALLBACK_TIMEOUT:
          SponsorPayLogger.d(TAG, "Timeout reached, returning \"false\" as default");
          onReceiveValue("false");
          break;
      }
    }
  };

  public void playThroughThirdParty(WebView webView, ValueCallback<Boolean> valueCallback) {
    if (valueCallback != null) {
      mCallback = valueCallback;
      mHandler.sendEmptyMessageDelayed(CALLBACK_TIMEOUT, 1000);
      if (Build.VERSION.SDK_INT >= 19) {
        try {
          String code = "javascript:try{ JSON.parse(" + SP_GET_OFFERS + ")."
                        + SP_TPN_JSON_KEY + ";}catch(error){false;};";
          Method evaluateMethod = webView.getClass().getMethod("evaluateJavascript", String.class, ValueCallback.class);
          evaluateMethod.invoke(webView, code, this);
        } catch (IllegalArgumentException e) {
          SponsorPayLogger.e(TAG, e.getLocalizedMessage());
        } catch (IllegalAccessException e) {
          SponsorPayLogger.e(TAG, e.getLocalizedMessage());
        } catch (InvocationTargetException e) {
          SponsorPayLogger.e(TAG, e.getLocalizedMessage());
        } catch (SecurityException e) {
          SponsorPayLogger.e(TAG, e.getLocalizedMessage());
        } catch (NoSuchMethodException e) {
          SponsorPayLogger.e(TAG, e.getLocalizedMessage());
        }
      } else {
        String code = "javascript:window." + interfaceName + ".setValue((function(){try{return JSON.parse("
                      + SP_GET_OFFERS + ")." + SP_TPN_JSON_KEY + ";}catch(js_eval_err){return false;}})());";
        // this needs to run in the UI thread
        webView.loadUrl(code);
      }
    } else {
      SponsorPayLogger.e(TAG, "There is no ValueCallback to notify. Aborting...");
    }
  }

  // HELPER methods for sync JS reply
  // http://www.gutterbling.com/blog/synchronous-javascript-evaluation-in-android-webview/#codesyntax_1
  /**
   * The javascript interface name for adding to web view.
   */
  private final String interfaceName = "SynchJS";

  /**
   * Receives the value from the javascript.
   *
   * @param value
   */
  @JavascriptInterface
  public void setValue(String value) {
    onReceiveValue(value);
  }

  /**
   * Gets the interface name
   *
   * @return
   */
  public String getInterfaceName() {
    return this.interfaceName;
  }

  @Override
  public void onReceiveValue(String value) {
    mHandler.removeMessages(CALLBACK_TIMEOUT);
    mCallback.onReceiveValue(Boolean.parseBoolean(value));
    mCallback = null;
  }

}
