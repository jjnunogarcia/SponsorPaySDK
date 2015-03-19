/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */
package com.sponsorpay.sdk.android.publisher.currency;

import com.sponsorpay.sdk.android.publisher.currency.SPCurrencyServerRequester.SPCurrencyServerReponse;
import com.sponsorpay.sdk.android.utils.StringUtils;

public class SPCurrencyServerErrorResponse implements SPCurrencyServerReponse {

  private final SPCurrencyServerRequestErrorType mErrorType;
  private final String                           mErrorCode;
  private final String                           mErrorMessage;

  public SPCurrencyServerErrorResponse(
      SPCurrencyServerRequestErrorType errorType, String errorCode,
      String errorMessage) {
    mErrorType = errorType;
    mErrorCode = errorCode;
    mErrorMessage = errorMessage;
  }

  /**
   * Gets the error condition in which this request / response has resulted.
   *
   * @return A {@link SPCurrencyServerRequestErrorType}.
   */
  public SPCurrencyServerRequestErrorType getErrorType() {
    return mErrorType;
  }

  /**
   * Gets the error code returned by the server.
   *
   * @return
   */
  public String getErrorCode() {
    return mErrorCode;
  }

  /**
   * Gets the error message returned by the server.
   *
   * @return
   */
  public String getErrorMessage() {
    return mErrorMessage != null ? mErrorMessage : StringUtils.EMPTY_STRING;
  }

}
