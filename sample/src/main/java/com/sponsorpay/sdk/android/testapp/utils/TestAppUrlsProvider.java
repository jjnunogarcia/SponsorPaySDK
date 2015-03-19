package com.sponsorpay.sdk.android.testapp.utils;

import com.sponsorpay.sdk.android.utils.SPUrlProvider;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.StringUtils;

import java.util.Properties;

public class TestAppUrlsProvider implements SPUrlProvider {

  public static        TestAppUrlsProvider INSTANCE = new TestAppUrlsProvider();
  private static final String              TAG      = "TestAppUrlsProvider";

  private Properties mStagingUrls;
  private String     mOverridingUrl;
  private boolean mUseStaging        = false;
  private boolean mUsePlainHttp      = false;
  private boolean isStagingAvailable = true;

  private TestAppUrlsProvider() {
    mStagingUrls = new Properties();
    try {
      mStagingUrls.load(this.getClass().getResourceAsStream("/staging.properties"));
    } catch (Exception e) {
      SponsorPayLogger.e(TAG, "An error happened while initializing url provider", e);
      isStagingAvailable = false;
    }
  }

  @Override
  public String getBaseUrl(String product) {
    if (StringUtils.notNullNorEmpty(mOverridingUrl)) {
      return mOverridingUrl;
    }
    if (isStagingAvailable && (mUseStaging || mUsePlainHttp)) {
      product = (mUsePlainHttp ? "plain-" : StringUtils.EMPTY_STRING)
                + (mUseStaging ? StringUtils.EMPTY_STRING : "prod-")
                + product;
      return mStagingUrls.getProperty(product);
    }
    return null;
  }

  public void setOverridingUrl(String mOverrideUrl) {
    this.mOverridingUrl = mOverrideUrl;
  }

  public void shouldUseStaging(boolean useStaging) {
    this.mUseStaging = useStaging;
  }

  public void shouldUsePlainHttp(boolean usePlainHttp) {
    this.mUsePlainHttp = usePlainHttp;
  }

  public boolean isStagingAvailble() {
    return isStagingAvailable;
  }

}
