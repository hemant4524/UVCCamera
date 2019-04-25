package com.serenegiant.usbcameratest7.xlog.printer.file.clean;

import java.io.File;

/**
 * Never Limit the file life.
 */
public class NeverCleanStrategy implements CleanStrategy {

  @Override
  public boolean shouldClean(File file) {
    return false;
  }
}
