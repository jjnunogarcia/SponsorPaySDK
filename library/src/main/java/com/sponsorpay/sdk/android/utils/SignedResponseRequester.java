/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.utils;

import java.util.List;
import java.util.Locale;

import android.os.AsyncTask;

/**
 * The SignedResponseRequester extends the AsyncTask android class 
 * and we are performing HTTP requests. We are creating with 
 * the response an CurrencyAndMediationServerResponse object
 * by passing into the later constructor the HTTP's 
 * status code, response body and the response signature.
 * Also this class is providing static method for
 * verifying the signature, to check the if there is an
 * error status code and to accept the language header value. 
 */
public abstract class SignedResponseRequester<V> extends AsyncTask<UrlBuilder, Void, V>{
	
	public static String TAG = "SignedResponseRequester";
	
	/**
	 * Key of the User-Agent header sent on background requests.
	 */
	private static String USER_AGENT_HEADER_NAME = "User-Agent";

	/**
	 * Key of the Accept-Language header sent on background requests.
	 */
	private static String ACCEPT_LANGUAGE_HEADER_NAME = "Accept-Language";

	/**
	 * Value of the User-Agent header sent on background requests.
	 */
	private static String USER_AGENT_HEADER_VALUE = "Android";

	/**
	 * Custom SponsorPay HTTP header containing the signature of the response.
	 */
	private static final String SIGNATURE_HEADER = "X-Sponsorpay-Response-Signature";
	

	@Override
	protected V doInBackground(UrlBuilder... params) {

		SignedServerResponse signedServerResponse = null;
		
		Thread.currentThread().setName(getTag());
		
		String requestUrl = params[0].buildUrl();
		
		SponsorPayLogger.d(TAG, "Request will be sent to URL + params: "
				+ requestUrl);
		
		try {
			SPHttpConnection connection = SPHttpConnection.getConnection(requestUrl)
					.addHeader(USER_AGENT_HEADER_NAME, USER_AGENT_HEADER_VALUE)
					.addHeader(ACCEPT_LANGUAGE_HEADER_NAME, makeAcceptLanguageHeaderValue())
					.open();
			int statusCode = connection.getResponseCode();
			String responseBody = connection.getBodyContent();
			List<String> responseSignatureHeaders = connection.getHeader(SIGNATURE_HEADER);
			String responseSignature = responseSignatureHeaders != null && responseSignatureHeaders.size() > 0 
					? responseSignatureHeaders.get(0)
					: StringUtils.EMPTY_STRING;
					
			SponsorPayLogger.d(TAG, String.format(
					"Server Response, status code: %d, response body: %s, signature: %s",
					statusCode, responseBody, responseSignature));
			
			signedServerResponse = new SignedServerResponse(statusCode, responseBody, responseSignature); 
			
			
		} catch (Throwable t) {
			SponsorPayLogger.e(TAG, "Exception triggered when executing request: " + t);
			return noConnectionResponse(t);
		}
		return parsedSignedResponse(signedServerResponse);
	}
	
	abstract protected V noConnectionResponse(Throwable t);

	abstract protected V parsedSignedResponse(SignedServerResponse signedServerResponse);

	/**
	 * Verify calculate the signature of the response with the provided security token and compare
	 * it against the server-provided response signature.
	 * @param responseBody 
	 * @param responseSignature 
	 * 
	 * @param securityToken
	 *            Security token which will be used to calculate the signature.
	 * @return true if the calculated signature matches the server-provided signature. false
	 *         otherwise.
	 */
	protected boolean verifySignature(SignedServerResponse signedServerResponse, String mSecurityToken) {
		String generatedSignature = SignatureTools.generateSignatureForString(signedServerResponse.getResponseBody(), mSecurityToken);
		return generatedSignature.equals(signedServerResponse.getResponseSignature());
	}
	
	/**
	 * Returns true if the response contains an HTTP status code out of the 200s.
	 * @param responseStatusCode 
	 * 
	 * @return false if HTTP status code is between 200 and 299. True otherwise.
	 */
	protected boolean hasErrorStatusCode(int responseStatusCode) {
		return responseStatusCode < 200 || responseStatusCode > 299;
	}
	
	/**
	 * Returns a value for the HTTP Accept-Language header based on the current locale set up for
	 * the device.
	 */
	protected String makeAcceptLanguageHeaderValue() {
		String preferredLanguage = Locale.getDefault().getLanguage();

		String acceptLanguageLocaleValue = preferredLanguage;
		final String englishLanguageCode = Locale.ENGLISH.getLanguage();

		if (StringUtils.nullOrEmpty(preferredLanguage)) {
			acceptLanguageLocaleValue = englishLanguageCode;
		} else if (!englishLanguageCode.equals(preferredLanguage)) {
			acceptLanguageLocaleValue += String.format(", %s;q=0.8", englishLanguageCode);
		}
		return acceptLanguageLocaleValue;
	}


	/**
	 * 
	 * @return The tag of the class that is implementing it
	 */
	protected abstract String getTag();
}