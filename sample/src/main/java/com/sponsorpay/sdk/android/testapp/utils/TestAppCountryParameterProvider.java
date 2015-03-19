package com.sponsorpay.sdk.android.testapp.utils;

import com.sponsorpay.sdk.android.utils.SPParametersProvider;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;

import java.util.HashMap;
import java.util.Map;

public class TestAppCountryParameterProvider implements SPParametersProvider {

  public static        TestAppCountryParameterProvider INSTANCE = new TestAppCountryParameterProvider();
  private static final String                          TAG      = "TestAppCountryParameterProvider";

  private Map<String, String> mParameters = new HashMap<String, String>();

  private TestAppCountryParameterProvider() {
  }

  @Override
  public Map<String, String> getParameters() {
    return mParameters;
  }

  public void useCountry(String value) {
    if (value.equals("")) {
      mParameters.clear();
    } else {
      SponsorPayLogger.d(TAG, "Country set to " + value);
      mParameters.put("country_code", value);
      mParameters.put("godmode", "neverletmedown");
    }
  }

}
