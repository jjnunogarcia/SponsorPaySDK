package com.sponsorpay.sdk.android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.sponsorpay.sdk.android.services.AbstractService;

/**
 * User: jesus
 * Date: 19/03/15
 *
 * @author jjnunogarcia@gmail.com
 */
public class CallbackResponseReceiver extends BroadcastReceiver {
  public CallbackResponseReceiver() {}

  @Override
  public void onReceive(Context context, Intent intent) {
    Bundle extras = intent.getExtras();

    if (extras != null && extras.containsKey(AbstractService.KEY_RESULT)) {
      boolean result = extras.getBoolean(AbstractService.KEY_RESULT);

      // TODO do something with the result??
      Toast.makeText(context, "The callback result is: " + result, Toast.LENGTH_SHORT).show();
    }
  }
}
