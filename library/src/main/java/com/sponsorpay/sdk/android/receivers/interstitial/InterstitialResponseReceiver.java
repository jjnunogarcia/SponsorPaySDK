package com.sponsorpay.sdk.android.receivers.interstitial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.sponsorpay.sdk.android.services.interstitial.InterstitialService;

/**
 * User: jesus
 * Date: 19/03/15
 *
 * @author jjnunogarcia@gmail.com
 */
public class InterstitialResponseReceiver extends BroadcastReceiver {
  public InterstitialResponseReceiver() {}

  @Override
  public void onReceive(Context context, Intent intent) {
    Bundle extras = intent.getExtras();

    if (extras != null && extras.containsKey(InterstitialService.KEY_RESULT)) {
      boolean result = extras.getBoolean(InterstitialService.KEY_RESULT);

      // TODO do something with the result??
      Toast.makeText(context, "The interstitial result is: " + result, Toast.LENGTH_SHORT).show();
    }
  }
}
