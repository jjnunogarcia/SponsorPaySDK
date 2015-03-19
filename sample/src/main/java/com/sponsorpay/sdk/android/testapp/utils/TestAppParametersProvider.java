package com.sponsorpay.sdk.android.testapp.utils;

import com.sponsorpay.sdk.android.utils.SPParametersProvider;

import java.util.HashMap;
import java.util.Map;

public class TestAppParametersProvider implements SPParametersProvider {

  public static TestAppParametersProvider INSTANCE = new TestAppParametersProvider();

  private Map<String, String> mParameters = new HashMap<String, String>();

  private TestAppParametersProvider() {
  }

  @Override
  public Map<String, String> getParameters() {
    return mParameters;
  }

  public void setParameters(Map<String, String> parameters) {
    mParameters = parameters;
  }

  public void put(String key, String value) {
    mParameters.put(key, value);
  }

  public void clear() {
    mParameters.clear();
  }


}
