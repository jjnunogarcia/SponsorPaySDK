/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.utils;

import android.text.TextUtils;
import com.sponsorpay.sdk.android.user.SPUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;

public class SPHttpConnection {

  private static final String TAG = "SPHttpConnection";

  private static final String USER_SEGMENTATION_HEADER_NAME = "X-User-Data";
  private static final String SET_COOKIES_HEADER            = "Set-Cookie";
  private static final String SET_COOKIES_HEADER2           = "Set-Cookie2";
  private static final String COOKIE_HEADER                 = "Cookie";

  private static CookieStore COOKIE_STORE;

  public static void setCookieStore(CookieStore store) {
    COOKIE_STORE = store;
  }

  public static SPHttpConnection getConnection(String url) throws MalformedURLException {
    return new SPHttpConnection(url);
  }

  public static SPHttpConnection getConnection(UrlBuilder builder) throws MalformedURLException {
    return new SPHttpConnection(builder.buildUrl());
  }

  private String                    mBody;
  private URL                       mUrl;
  private Map<String, List<String>> mHeaders;
  private int                       mResponseCode;
  private List<Header>              mHeadersToAdd;
  private boolean mOpen = false;

  private SPHttpConnection(String url) throws MalformedURLException {
    mUrl = new URL(url);
  }

  public SPHttpConnection addHeader(String header, String headerValue) {
    if (mHeadersToAdd == null) {
      mHeadersToAdd = new LinkedList<Header>();
    }
    mHeadersToAdd.add(new Header(header, headerValue));
    return this;
  }

  public SPHttpConnection open() {
    HttpURLConnection urlConnection = null;
    try {
      urlConnection = (HttpURLConnection) mUrl.openConnection();
      if (mHeadersToAdd != null) {
        for (Header header : mHeadersToAdd) {
          urlConnection.addRequestProperty(header.key, header.value);
        }
      }

      //If the developer has passed data for user segmentation,
      //then add it in the headers
      String userSegmentationData = SPUser.mapToString();
      if (StringUtils.notNullNorEmpty(userSegmentationData)) {
        urlConnection.addRequestProperty(USER_SEGMENTATION_HEADER_NAME, userSegmentationData);
      }

      addCookies(urlConnection);

      InputStream is = null;
      try {
        is = urlConnection.getInputStream();
      } catch (IOException exception) {
        is = urlConnection.getErrorStream();
      }
      mBody = readStream(is);
      mResponseCode = urlConnection.getResponseCode();
      mHeaders = Collections.unmodifiableMap(urlConnection.getHeaderFields());

      storeCookies();
    } catch (Exception e) {
      SponsorPayLogger.e(TAG, e.getLocalizedMessage(), e);
    } finally {
      if (urlConnection != null) {
        urlConnection.disconnect();
      }
      mOpen = true;
    }
    return this;
  }

  private String readStream(InputStream inputStream) throws IOException {

    ByteArrayOutputStream into = new ByteArrayOutputStream();
    byte[] buf = new byte[1024];
    for (int n; 0 < (n = inputStream.read(buf)); ) {
      into.write(buf, 0, n);
    }
    into.close();
    return new String(into.toByteArray(), "UTF-8");
  }

  public String getBodyContent() throws IOException {
    if (!mOpen) {
      throw new IOException("The connection has not been opened yet.");
    }
    return mBody;
  }

  public Map<String, List<String>> getHeaders() throws IOException {
    if (!mOpen) {
      throw new IOException("The connection has not been opened yet.");
    }
    return mHeaders;
  }

  public List<String> getHeader(String header) throws IOException {
    if (!mOpen) {
      throw new IOException("The connection has not been opened yet.");
    }
    return mHeaders.get(header);
  }

  public int getResponseCode() throws IOException {
    if (!mOpen) {
      throw new IOException("The connection has not been opened yet.");
    }
    return mResponseCode;
  }

  public static Map<String, String> createUserSegmentationMapForHeaders() {
    Map<String, String> headers = new HashMap<String, String>();
    headers.put(USER_SEGMENTATION_HEADER_NAME, SPUser.mapToString());
    return headers;
  }

  //Cookies
  private void addCookies(HttpURLConnection urlConnection) {
    if (COOKIE_STORE != null) {
      synchronized (COOKIE_STORE) {
        try {
          List<HttpCookie> cookies = COOKIE_STORE.get(mUrl.toURI());
          if (cookies.size() > 0) {
            urlConnection.addRequestProperty(COOKIE_HEADER,
                                             TextUtils.join(";", cookies));
          }
        } catch (URISyntaxException e) {
          SponsorPayLogger.d(TAG, e.getMessage());
        }
      }
    }
  }

  private synchronized void storeCookies() {
    if (COOKIE_STORE != null) {
      synchronized (COOKIE_STORE) {
        storeCookiesFromHeader(SET_COOKIES_HEADER);
        storeCookiesFromHeader(SET_COOKIES_HEADER2);
      }
    }
  }

  private void storeCookiesFromHeader(String header) {
    List<String> cookiesHeader = mHeaders.get(header);
    if (cookiesHeader != null) {
      for (String cookieString : cookiesHeader) {
        try {
          List<HttpCookie> cookies = HttpCookie.parse(cookieString);
          for (HttpCookie cookie : cookies) {
            if (StringUtils.nullOrEmpty(cookie.getDomain())) {
              cookie.setDomain(mUrl.getHost());
            }
            COOKIE_STORE.add(mUrl.toURI(), cookie);
          }
        } catch (URISyntaxException e) {
          SponsorPayLogger.d(TAG, e.getMessage());
        }
      }
    }
  }

  // helper class
  private class Header {

    String key;
    String value;

    public Header(String key, String value) {
      this.key = key;
      this.value = value;
    }

  }

}