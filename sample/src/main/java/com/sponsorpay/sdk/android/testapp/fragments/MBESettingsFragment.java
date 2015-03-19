package com.sponsorpay.sdk.android.testapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import com.sponsorpay.sdk.android.SponsorPay;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher;
import com.sponsorpay.sdk.android.publisher.currency.SPCurrencyServerErrorResponse;
import com.sponsorpay.sdk.android.publisher.currency.SPCurrencyServerListener;
import com.sponsorpay.sdk.android.publisher.currency.SPCurrencyServerSuccessfulResponse;
import com.sponsorpay.sdk.android.publisher.mbe.SPBrandEngageClient;
import com.sponsorpay.sdk.android.publisher.mbe.SPBrandEngageRequestListener;
import com.sponsorpay.sdk.android.testapp.R;
import com.sponsorpay.sdk.android.testapp.SponsorpayAndroidTestAppActivity;
import com.sponsorpay.sdk.android.testapp.utils.TextViewLogger;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.StringUtils;

public class MBESettingsFragment extends AbstractSettingsFragment implements SPBrandEngageRequestListener {

  private static final String TAG = "MBESettingsFragment";

  private static final String MBE_CHECK_VCS                = "MBE_CHECK_VCS";
  private static final String MBE_SHOW_REWARD_NOTIFICATION = "MBE_SHOW_REWARD_NOTIFICATION";
  private static final String PREFERENCES_FILE_NAME        = "SponsorPayTestAppState";

  private Intent   mIntent;
  private CheckBox mVCSCheckbox;
  private CheckBox mNotificationfCheckbox;
  private Button   btnStart;
  private Button   btnRequest;

  private boolean mAddVCSListener;
  private boolean mShowNotification;

  private String currencyId;

  private SponsorpayAndroidTestAppActivity mMainActivity;

  private SPCurrencyServerListener mVCSListener = new SPCurrencyServerListener() {

    @Override
    public void onSPCurrencyServerError(
        SPCurrencyServerErrorResponse response) {
      Log.e(TAG, "VCS error received - " + response.getErrorMessage());
    }

    @Override
    public void onSPCurrencyDeltaReceived(
        SPCurrencyServerSuccessfulResponse response) {
      Log.d(TAG, "VCS coins received - " + response.getDeltaOfCoins());
    }
  };

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    mMainActivity = (SponsorpayAndroidTestAppActivity) this.getActivity();

    View view = inflater.inflate(R.layout.fragment_settings_mbe, container, false);

    btnStart = ((Button) view.findViewById(R.id.mbe_start));
    btnRequest = ((Button) view.findViewById(R.id.mbe_request_offers));

    btnRequest.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {
        mMainActivity.fetchValuesFromFields();
        TextViewLogger.INSTANCE.reset();
        SponsorPayLogger.d(TAG, "Requesting MBE offers...");
        v.setEnabled(false);
        btnStart.setEnabled(false);
        Context context = getActivity();
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        String currencyName = sharedPref.getString(CurrencyFragment.VCS_CURRENCY_NAME, StringUtils.EMPTY_STRING);
        requestOffers(currencyName);
      }
    });

    btnStart.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {
        mMainActivity.fetchValuesFromFields();

        SponsorPayLogger.d(TAG, "Starting engagement");
        startEngament();
        v.setEnabled(false);
      }
    });

    return view;
  }

  @Override
  protected String getFragmentTitle() {
    return getResources().getString(R.string.mbe);
  }

  @Override
  protected void setValuesInFields() {
    mVCSCheckbox.setChecked(mAddVCSListener);
    mNotificationfCheckbox.setChecked(mShowNotification);
  }

  @Override
  protected void bindViews() {
    mVCSCheckbox = (CheckBox) findViewById(R.id.mbe_add_vcs_listener_checkbox);
    mNotificationfCheckbox = (CheckBox) findViewById(R.id.mbe_show_notification_checkbox);
  }

  @Override
  protected void fetchValuesFromFields() {
    mAddVCSListener = mVCSCheckbox.isChecked();
    mShowNotification = mNotificationfCheckbox.isChecked();
  }

  @Override
  protected void readPreferences(SharedPreferences prefs) {
    mAddVCSListener = prefs.getBoolean(MBE_CHECK_VCS, true);
    mShowNotification = prefs.getBoolean(MBE_SHOW_REWARD_NOTIFICATION, true);
    currencyId = prefs.getString(CurrencyFragment.VCS_CURRENCY_ID, StringUtils.EMPTY_STRING);
  }

  @Override
  protected void storePreferences(Editor prefsEditor) {
    prefsEditor.putBoolean(MBE_CHECK_VCS, mAddVCSListener);
    prefsEditor.putBoolean(MBE_SHOW_REWARD_NOTIFICATION, mShowNotification);
  }

  public void requestOffers(String currencyName) {
    fetchValuesFromFields();
    try {
      String credentialsToken = SponsorPay.getCurrentCredentials().getCredentialsToken();

      SPBrandEngageClient.INSTANCE.setShowRewardsNotification(mShowNotification);

      SPCurrencyServerListener vcsListener = mAddVCSListener ? mVCSListener : null;
      SponsorPayPublisher.getIntentForMBEActivity(credentialsToken, getActivity(), this,
                                                  currencyName, null, vcsListener, currencyId, SponsorpayAndroidTestAppActivity.mPlacementId);


    } catch (RuntimeException ex) {
      btnRequest.setEnabled(true);
      btnStart.setEnabled(false);
      showCancellableAlertBox("Exception from SDK", ex.getMessage());
      Log.e(SponsorpayAndroidTestAppActivity.class.toString(), "SponsorPay SDK Exception: ",
            ex);
    }
  }

  public void startEngament() {
    if (mIntent != null && SPBrandEngageClient.INSTANCE.canStartEngagement()) {
      SponsorPayLogger.d(TAG, "Starting MBE engagement...");
      startActivity(mIntent);
      mIntent = null;
    }
  }

  @Override
  public void onSPBrandEngageOffersAvailable(Intent spBrandEngageActivity) {
    btnRequest.setEnabled(true);
    btnStart.setEnabled(true);
    SponsorPayLogger.d(TAG, "SPBrandEngage - offers are available");
    mIntent = spBrandEngageActivity;
  }

  @Override
  public void onSPBrandEngageOffersNotAvailable() {
    btnRequest.setEnabled(true);
    btnStart.setEnabled(false);
    SponsorPayLogger.d(TAG, "SPBrandEngage - no offers for the moment");
  }

  @Override
  public void onSPBrandEngageError(String errorMessage) {
    btnRequest.setEnabled(true);
    btnStart.setEnabled(false);
    SponsorPayLogger.e(TAG, "SPBrandEngage - an error occured:\n" + errorMessage);
  }

}
