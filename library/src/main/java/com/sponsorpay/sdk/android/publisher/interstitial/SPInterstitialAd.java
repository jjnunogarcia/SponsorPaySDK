/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.interstitial;

import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Class holding the information about a specific Interstitial Ad.
 * </p>
 * <p/>
 * This class is meant to be used only internally.
 */
public class SPInterstitialAd implements Parcelable {

  /**
   * The provider type of the ad
   */
  private String mProviderType;

  /**
   * SponsorPay internal id of the ad
   */
  private String mAdId;

  /**
   * The tracking parameters that are coming from the ad
   */
  private JSONObject mTrackingParameters;

  /**
   * It contains all the data that are coming from the ad except from the
   * three data above(provider type, ad ID and tracking parameters).
   */
  private Map<String, String> mContextData;

  public static final Creator<SPInterstitialAd> CREATOR;

  static {
    CREATOR = new Creator<SPInterstitialAd>() {

      @Override
      public SPInterstitialAd createFromParcel(Parcel source) {
        return new SPInterstitialAd(source);
      }

      @Override
      public SPInterstitialAd[] newArray(int size) {
        return new SPInterstitialAd[size];
      }
    };
  }

  public SPInterstitialAd() {
    mTrackingParameters = new JSONObject();
    mContextData = new HashMap<>();
  }

  private SPInterstitialAd(Parcel source) {
    this();
    readFromParcel(source);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(mProviderType);
    dest.writeString(mAdId);
    dest.writeString(mTrackingParameters.toString());
    dest.writeSerializable(new HashMap<>(mContextData));
  }

  private void readFromParcel(Parcel source) {
    mProviderType = source.readString();
    mAdId = source.readString();
    try {
      mTrackingParameters = new JSONObject(source.readString());
    } catch (JSONException e) {
      e.printStackTrace();
    }
    mContextData = (Map<String, String>) source.readSerializable();
  }

  public SPInterstitialAd(String providerType, String adId, JSONObject trackingParams) {
    mProviderType = providerType;
    mAdId = adId;
    mTrackingParameters = trackingParams;
  }

  public String getProviderType() {
    return mProviderType;
  }

  public String getAdId() {
    return mAdId;
  }

  public void setContextData(String key, String value) {
    if (mContextData == null) {
      mContextData = new HashMap<String, String>();
    }
    mContextData.put(key, value);
  }

  public Map<String, String> getContextData() {
    if (mContextData == null) {
      return Collections.emptyMap();
    }
    return mContextData;
  }

  public JSONObject getTrackingParameters() {
    return mTrackingParameters;
  }

}