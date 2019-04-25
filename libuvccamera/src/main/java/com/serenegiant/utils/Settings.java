package com.serenegiant.utils;

import android.os.Environment;

import java.io.File;

public class Settings {
    public static final String ROOT_PATH = Environment
            .getExternalStorageDirectory()
            + File.separator
            + "uvccamera"
            + File.separator;

    public static final String LOG_DIRECTORY = ROOT_PATH + "logs"
            + File.separator;
}
