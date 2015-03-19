/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.utils;

import android.util.Log;

import java.util.HashSet;
import java.util.Set;

public class SponsorPayLogger {

  public enum Level {
    VERBOSE,
    DEBUG,
    INFO,
    WARNING,
    ERROR,
  }

  private static boolean logging = false;

  private static final String prefix = "[SP] ";

  public static boolean toggleLogging() {
    logging = !logging;
    return logging;
  }

  public static boolean isLogging() {
    return logging;
  }

  public static boolean enableLogging(boolean shouldLog) {
    logging = shouldLog;
    return logging;
  }

  public static void e(String tag, String message) {
    if (shouldLog()) {
      Log.e(prefix + tag, message);
      INSTANCE.log(Level.ERROR, tag, message, null);
    }
  }

  public static void e(String tag, String message, Exception exception) {
    if (shouldLog()) {
      Log.w(prefix + tag, message, exception);
      INSTANCE.log(Level.ERROR, tag, message, exception);
    }
  }

  public static void d(String tag, String message) {
    if (shouldLog()) {
      Log.d(prefix + tag, message);
      INSTANCE.log(Level.DEBUG, tag, message, null);
    }
  }

  public static void i(String tag, String message) {
    if (shouldLog()) {
      Log.i(prefix + tag, message);
      INSTANCE.log(Level.INFO, tag, message, null);
    }
  }

  public static void v(String tag, String message) {
    if (shouldLog()) {
      Log.v(prefix + tag, message);
      INSTANCE.log(Level.VERBOSE, tag, message, null);
    }
  }

  public static void w(String tag, String message) {
    if (shouldLog()) {
      Log.w(prefix + tag, message);
      INSTANCE.log(Level.WARNING, tag, message, null);
    }
  }

  public static void w(String tag, String message, Exception exception) {
    if (shouldLog()) {
      Log.w(prefix + tag, message, exception);
      INSTANCE.log(Level.WARNING, tag, message, exception);
    }
  }

  //

  private static boolean shouldLog() {
    return logging || Log.isLoggable("SponsorPay", Log.VERBOSE);
  }

  private static SponsorPayLogger INSTANCE = new SponsorPayLogger();

  private Set<SPLoggerListener> listeners = new HashSet<SPLoggerListener>();

  private SponsorPayLogger() {
  }

  public void log(final Level level, final String tag, final String message,
                  final Exception exception) {
    if (!listeners.isEmpty()) {
      // this should have been taken care with thread, looper and handler,
      // but then we need to destroy it
      new Thread(new Runnable() {
        @Override
        public void run() {
          for (SPLoggerListener listener : listeners) {
            listener.log(level, tag, message, exception);
          }
        }
      }).start();
    }
  }

  public static boolean addLoggerListener(SPLoggerListener newListener) {
    return INSTANCE.listeners.add(newListener);
  }

  public static boolean removeLoggerListener(SPLoggerListener listener) {
    return INSTANCE.listeners.remove(listener);
  }

}
