package com.sponsorpay.sdk.android.testapp.fragments;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import com.sponsorpay.sdk.android.publisher.SponsorPayPublisher;
import com.sponsorpay.sdk.android.publisher.currency.SPCurrencyServerErrorResponse;
import com.sponsorpay.sdk.android.publisher.currency.SPCurrencyServerListener;
import com.sponsorpay.sdk.android.publisher.currency.SPCurrencyServerSuccessfulResponse;
import com.sponsorpay.sdk.android.testapp.R;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.StringUtils;

public class CurrencyFragment extends AbstractSettingsFragment implements OnClickListener, SPCurrencyServerListener {

  private static final String TAG = "CurrencyFragment";

  public static final String VCS_CURRENCY_NAME = "VCS_NAME";
  public static final String VCS_CURRENCY_ID   = "VCS_ID";

  private String mCurrencyName;
  private String mCurrencyId;

  private EditText mCurrencyNameField;
  private EditText mCurrencyIdField;
  private Button   mRequestCoinsButton;


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_settings_vcs, container, false);
    mCurrencyNameField = (EditText) view.findViewById(R.id.currency_name_field);
    mCurrencyIdField = (EditText) view.findViewById(R.id.currency_id_field);
    mRequestCoinsButton = (Button) view.findViewById(R.id.request_new_coins_button);
    mRequestCoinsButton.setOnClickListener(this);
    return view;
  }

  @Override
  protected void setValuesInFields() {
    mCurrencyNameField.setText(mCurrencyName);
    mCurrencyIdField.setText(mCurrencyId);
  }

  @Override
  protected String getFragmentTitle() {
    return getResources().getString(R.string.vcs);
  }

  @Override
  protected void bindViews() {
    mCurrencyNameField = (EditText) findViewById(R.id.currency_name_field);
    mCurrencyIdField = (EditText) findViewById(R.id.currency_id_field);
  }

  @Override
  protected void fetchValuesFromFields() {
    mCurrencyName = mCurrencyNameField.getText().toString();
    mCurrencyId = mCurrencyIdField.getText().toString();
  }

  @Override
  protected void readPreferences(SharedPreferences prefs) {
    mCurrencyName = prefs.getString(VCS_CURRENCY_NAME, StringUtils.EMPTY_STRING);
    mCurrencyId = prefs.getString(VCS_CURRENCY_ID, StringUtils.EMPTY_STRING);
  }

  @Override
  protected void storePreferences(Editor prefsEditor) {
    prefsEditor.putString(VCS_CURRENCY_NAME, mCurrencyName);
    prefsEditor.putString(VCS_CURRENCY_ID, mCurrencyId);
  }

  /**
   * Triggered when the user clicks on the Request New Coins button. Will send
   * a request for delta of coins to the currency server and register a
   * callback object to show the result in a dialog box. Uses the values
   * entered for User ID, App ID and Security Token.
   *
   * @param v
   */
  @Override
  public void onClick(View arg0) {
    fetchValuesFromFields();

    try {
      SponsorPayPublisher.requestNewCoins(getApplicationContext(), mCurrencyId, this, mCurrencyName);
    } catch (RuntimeException ex) {
      showCancellableAlertBox("Exception from SDK", ex.getMessage());
      SponsorPayLogger.e(TAG, "SponsorPay SDK Exception: ", ex);
    }
  }

  @Override
  public void onSPCurrencyServerError(SPCurrencyServerErrorResponse response) {
    showCancellableAlertBox(
        "Response or Request Error",
        String.format("%s\n%s\n%s\n",
                      response.getErrorType(),
                      response.getErrorCode(),
                      response.getErrorMessage()));
  }

  @Override
  public void onSPCurrencyDeltaReceived(SPCurrencyServerSuccessfulResponse response) {
    showCancellableAlertBox("Response From Currency Server",
                            String.format("Delta of Coins: %s\n\n"
                                          + "Returned Latest Transaction ID: %s\n\n",
                                          response.getDeltaOfCoins(),
                                          response.getLatestTransactionId()));

  }

}
