/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


/**
 * Extracts device information from the host device in which the SDK runs.
 */
public class HostInfo {

  private static final String TAG = "HostInfo";

  /**
   * Prefix appended to the OS version to identify the Android platform.
   */
  private static final String ANDROID_OS_PREFIX                        = "Android OS ";
  private static final String SCREEN_DENSITY_CATEGORY_VALUE_LOW        = "LOW";
  private static final String SCREEN_DENSITY_CATEGORY_VALUE_MEDIUM     = "MEDIUM";
  private static final String SCREEN_DENSITY_CATEGORY_VALUE_HIGH       = "HIGH";
  private static final String SCREEN_DENSITY_CATEGORY_VALUE_EXTRA_HIGH = "EXTRA_HIGH";
  private static final String SCREEN_DENSITY_CATEGORY_VALUE_TV         = "TV";
  private static final String UNDEFINED_VALUE                          = "undefined";
  private static final String CONNECTION_TYPE_CELLULAR                 = "cellular";
  private static final String CONNECTION_TYPE_WIFI                     = "wifi";


  private static HostInfo hostInfoInstance;

  public static HostInfo getHostInfo(Context context) {
    if (hostInfoInstance == null) {
      hostInfoInstance = new HostInfo(context);
    }
    return hostInfoInstance;
  }

  /**
   * The running Android OS version (e.g. "2.1" for Android 2.1).
   */
  private String mOsVersion;

  /**
   * The device version (e.g. "HTC Nexus One").
   */
  private String mPhoneVersion;

  /**
   * Language settings (the default locale).
   */
  private String mLanguageSetting;

  private String mAdvertisingId;

  private boolean mAdvertisingIdLimitedTrackingEnabled = true;

  private DisplayMetrics mDisplayMetrics;
  private WindowManager  mWindowManager;

  private String mScreenDensityCategory;
  private int    mScreenWidth;
  private int    mScreenHeight;
  private float  mScreenDensityX;
  private float  mScreenDensityY;

  private String mCarrierCountry;
  private String mCarrierName;

  private String mConnectionType;

  private String mAppVersion;

  private LocationManager mLocationManager;

  private boolean mHasDeviceReverseOrientation = false;

  /**
   * Constructor. Requires an Android application context which will be used to retrieve
   * information from the device and the host application's Android Manifest.
   *
   * @param context Android application context
   */
  public HostInfo(final Context context) {
    if (context == null) {
      throw new RuntimeException("A context is required to initialize HostInfo");
    }

    // check if we're running in the main thread
    if (Looper.myLooper() == Looper.getMainLooper()) {
      new Thread("AdvertisingIdRetriever") {
        public void run() {
          retrieveAdvertisingId(context);
        }
      }.start();
    } else {
      retrieveAdvertisingId(context);
    }

    retrieveTelephonyManagerValues(context);
    retrieveAccessNetworkValues(context);
    retrieveDisplayMetrics(context);
    retrieveAppVersion(context);
    retrieveReverseOrientation(context);

    setupLocationManager(context);

    // Get the default locale
    mLanguageSetting = Locale.getDefault().toString();

    // Get the Android version
    mOsVersion = ANDROID_OS_PREFIX + android.os.Build.VERSION.RELEASE;

    // Get the phone model
    mPhoneVersion = android.os.Build.MANUFACTURER + "_" + android.os.Build.MODEL;
    mBundleName = context.getPackageName();
  }

  private void retrieveReverseOrientation(Context context) {
    Configuration config = context.getResources().getConfiguration();
    int rotation = getRotation();
    //check if orientation matches the rotation, if not, set device as reversed orientation
    mHasDeviceReverseOrientation = ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && config.orientation == Configuration.ORIENTATION_LANDSCAPE)
                                   || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && config.orientation == Configuration.ORIENTATION_PORTRAIT);
  }

  private void retrieveAccessNetworkValues(Context context) {
    mConnectionType = StringUtils.EMPTY_STRING;
    if (!sSimulateNoAccessNetworkState) {
      try {
        ConnectivityManager mConnectivity = (ConnectivityManager) context
            .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo info = mConnectivity.getActiveNetworkInfo();
        if (info != null) {
          int netType = info.getType();
          mConnectionType = netType == ConnectivityManager.TYPE_WIFI ? CONNECTION_TYPE_WIFI
                                                                     : CONNECTION_TYPE_CELLULAR;
        }
      } catch (RuntimeException e) {
      }
    }
  }


  private void retrieveTelephonyManagerValues(Context context) {
    mCarrierName = StringUtils.EMPTY_STRING;
    mCarrierCountry = StringUtils.EMPTY_STRING;
    if (!sSimulateNoReadPhoneStatePermission) {
      // Get access to the Telephony Services
      TelephonyManager tManager = (TelephonyManager) context
          .getSystemService(Context.TELEPHONY_SERVICE);
      try {
        mCarrierName = tManager.getNetworkOperatorName();
        mCarrierCountry = tManager.getNetworkCountryIso();
      } catch (SecurityException e) {
      }
    }
  }

  private DisplayMetrics retrieveDisplayMetrics(Context context) {
    if (mDisplayMetrics == null) {
      mDisplayMetrics = new DisplayMetrics();
      mWindowManager = (WindowManager) context
          .getSystemService(Context.WINDOW_SERVICE);
      mWindowManager.getDefaultDisplay().getMetrics(mDisplayMetrics);
    }
    return mDisplayMetrics;
  }


  private void retrieveAppVersion(Context context) {
    try {
      PackageInfo pInfo = context.getPackageManager()
                                 .getPackageInfo(context.getPackageName(), 0);
      mAppVersion = pInfo.versionName;
    } catch (NameNotFoundException e) {
      mAppVersion = StringUtils.EMPTY_STRING;
    }
  }

  /**
   * Get the running OS version
   *
   * @return the OS version
   */
  public String getOsVersion() {
    return mOsVersion;
  }

  /**
   * Get the current phone model
   *
   * @return the phone model
   */
  public String getPhoneVersion() {
    return mPhoneVersion;
  }

  /**
   * Get the default locale set by the user
   *
   * @return the default language setting
   */
  public String getLanguageSetting() {
    return mLanguageSetting;
  }


  private CountDownLatch mIdLatch = new CountDownLatch(1);

  private String mBundleName;

  private List<String> mProviders;


  protected void retrieveAdvertisingId(Context context) {
    try {
      //calling this reflexively, in case of Play Services not linked with the application
      Class<?> advertisingIdClientClass = Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient");

      Method getAdInfoMethod = advertisingIdClientClass.getMethod("getAdvertisingIdInfo", Context.class);
      Object adInfo = getAdInfoMethod.invoke(null, context);

      Method getIdMethod = adInfo.getClass().getMethod("getId");
      Method isLimitAdTrackingEnabledMethod = adInfo.getClass().getMethod("isLimitAdTrackingEnabled");

      mAdvertisingId = getIdMethod.invoke(adInfo).toString();
      mAdvertisingIdLimitedTrackingEnabled = (Boolean) isLimitAdTrackingEnabledMethod.invoke(adInfo);

    } catch (Exception e) {
      SponsorPayLogger.e(TAG, e.getLocalizedMessage(), e);
    }
    mIdLatch.countDown();
  }

  public String getAdvertisingId() {
    try {
      mIdLatch.await(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      //do nothing
    }
    return mAdvertisingId;
  }

  public Boolean isAdvertisingIdLimitedTrackingEnabled() {
    return mAdvertisingIdLimitedTrackingEnabled;
  }

  public String getScreenOrientation() {
    String[] values = {"portrait", "landscape", "portrait", "landscape", "portrait"};
    int rotation = getRotation();
    if (mHasDeviceReverseOrientation) {
      rotation += 1;
    }
    return values[rotation];
  }

  public int getRotation() {
    return mWindowManager.getDefaultDisplay().getRotation();
  }

  public boolean hasDeviceRevserseOrientation() {
    return mHasDeviceReverseOrientation;
  }

  public String getScreenDensityCategory() {
    if (mScreenDensityCategory == null) {
      int densityCategoryDpi = mDisplayMetrics.densityDpi;

      switch (densityCategoryDpi) {
        case DisplayMetrics.DENSITY_MEDIUM:
          mScreenDensityCategory = SCREEN_DENSITY_CATEGORY_VALUE_MEDIUM;
          break;
        case DisplayMetrics.DENSITY_HIGH:
          mScreenDensityCategory = SCREEN_DENSITY_CATEGORY_VALUE_HIGH;
          break;
        case DisplayMetrics.DENSITY_LOW:
          mScreenDensityCategory = SCREEN_DENSITY_CATEGORY_VALUE_LOW;
          break;
        case DisplayMetrics.DENSITY_XHIGH:
          mScreenDensityCategory = SCREEN_DENSITY_CATEGORY_VALUE_EXTRA_HIGH;
          break;
        default:
          mScreenDensityCategory = getScreenDensityCategoryFromModernAndroidDevice(densityCategoryDpi);
      }
    }
    return mScreenDensityCategory;
  }

  private String getScreenDensityCategoryFromModernAndroidDevice(int densityCategoryDpi) {
    String[] remainingScreenDensityCategoryStaticFieldNames = {"DENSITY_XHIGH", "DENSITY_TV"};
    String[] correspondingScreenDensityCategoryStringValues = {SCREEN_DENSITY_CATEGORY_VALUE_EXTRA_HIGH, SCREEN_DENSITY_CATEGORY_VALUE_TV};

    int iterationLimit = Math.min(remainingScreenDensityCategoryStaticFieldNames.length,
                                  correspondingScreenDensityCategoryStringValues.length);

    String densityCategory = null;

    for (int i = 0; i < iterationLimit; i++) {
      try {
        Field eachField = DisplayMetrics.class
            .getField(remainingScreenDensityCategoryStaticFieldNames[i]);
        int thisFieldvalue = eachField.getInt(null);
        if (densityCategoryDpi == thisFieldvalue) {
          densityCategory = correspondingScreenDensityCategoryStringValues[i];
        }
      } catch (Exception e) {
        // Assumed field doesn't exist in the version of Android the host is running
      }

      if (null != densityCategory) {
        break;
      }
    }

    return null == densityCategory ? UNDEFINED_VALUE : densityCategory;
  }

  public static boolean isDeviceSupported() {
    return Build.VERSION.SDK_INT >= 10;
  }

  public String getScreenWidth() {
    if (0 == mScreenWidth) {
      mScreenWidth = mDisplayMetrics.widthPixels;
    }
    return Integer.toString(mScreenWidth);
  }

  public String getScreenHeight() {
    if (0 == mScreenHeight) {
      mScreenHeight = mDisplayMetrics.heightPixels;
    }
    return Integer.toString(mScreenHeight);
  }

  public String getScreenDensityX() {
    if (0 == mScreenDensityX) {
      mScreenDensityX = mDisplayMetrics.xdpi;
    }
    return Integer.toString(Math.round(mScreenDensityX));
  }

  public String getScreenDensityY() {
    if (0 == mScreenDensityY) {
      mScreenDensityY = mDisplayMetrics.ydpi;
    }
    return Integer.toString(Math.round(mScreenDensityY));
  }

  public String getCarrierCountry() {
    return mCarrierCountry;
  }

  public String getCarrierName() {
    return mCarrierName;
  }

  public String getConnectionType() {
    return mConnectionType;
  }

  public String getManufacturer() {
    return Build.MANUFACTURER;
  }

  public String getAppVersion() {
    return mAppVersion;
  }

  public String getAppBundleName() {
    return mBundleName;
  }

  public LocationManager getLocationManager() {
    return mLocationManager;
  }

  public List<String> getLocationProviders() {
    return mProviders;
  }

  private void setupLocationManager(Context context) {
    //set a static boolean to avoid the continuous check
    List<String> providers = new LinkedList<String>();
    if (context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED) {
      providers.add(LocationManager.GPS_PROVIDER);
      providers.add(LocationManager.PASSIVE_PROVIDER);
    }
    if (context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        == PackageManager.PERMISSION_GRANTED) {
      providers.add(LocationManager.NETWORK_PROVIDER);
    }
    if (!providers.isEmpty()) {
      mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
      mProviders = providers;
    }
  }

  // Permission simulation
  protected static boolean sSimulateNoReadPhoneStatePermission = false;
  protected static boolean sSimulateNoAccessNetworkState       = false;

  public static void setSimulateNoReadPhoneStatePermission(boolean value) {
    sSimulateNoReadPhoneStatePermission = value;
  }

  public static void setSimulateNoAccessNetworkState(boolean value) {
    sSimulateNoAccessNetworkState = value;
  }

}
