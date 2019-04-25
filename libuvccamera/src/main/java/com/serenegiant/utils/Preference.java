package com.serenegiant.utils;

import android.content.Context;
import android.content.SharedPreferences;


import com.serenegiant.uvccamera.R;

public class Preference  {
    //string_activity_settings.xml pref_default_player_version
    public static final int DEFAULT_SCHEDULE_DAY = 1;


    // window

    // displaying the player
    private int updateCheckerFrequency; // frequency for
    // checking updates, in
    // seconds

    private boolean checkUpdates; // player will not check updates if thisø
    // option is false

    // singleton instance of preference
    private static Preference _instance = new Preference();

    private int countdownTimerTune = 0;
    public static final int FIRSTCYCLEPAUSETIME_DEFAULT = 200;
    private int firstCyclePauseTime = FIRSTCYCLEPAUSETIME_DEFAULT;

    // Log levelø
    private static final int DEFAULT_LOGLEVEL = 2; // DEBUG
    private int logLevel = DEFAULT_LOGLEVEL;

    private int boxWidth = 0;
    private int boxHeight = 0;

    private String version;
    private String versionToUpgrade;

    public static final int WEBBROWSER_DEFAULT = 0;
    public static final int WEBBROWSER_WEBKIT = 1;

    private boolean isReboot;

    private transient SharedPreferences prefs = null;

    private boolean isBoxIDUpdated = false;
    private transient Context ctx;
    private int autorestart = 30;

    private Preference() {

    }

    public void setSharedPreferences(SharedPreferences prefs, Context ctx) {
        this.prefs = prefs;
        this.ctx = ctx;
        setAll();
    }

    private void setAll() {
        logLevel = getLogLevel();

    }

    public synchronized static Preference getInstance() {
        return _instance;
    }



    public int getPlayerLeft() {
        return Integer.parseInt(prefs.getString("player_left", "0"));
    }

    public int getautorestarttime() {
        return Integer.parseInt(prefs.getString("auto_restart_time", "45"));
    }

    public int getPlayerTop() {
        return Integer.parseInt(prefs.getString("player_top", "0"));
    }


    public int getLogLevel() {
        return Integer.parseInt(prefs.getString("log_level",
                ctx.getString(R.string.pref_default_log_level)));
    }



    @Override
    public String toString() {
        return "Preference [Version=" + version +
                 ", logLevel=" + logLevel + "";
    }



    public boolean isZeroPauseTime() {
        return false;
    }

    public double getScale() {
        return 1.0;
    }

    public void setVersion() {
        // this.version = version;
    }

    public String getVersionToUpgrade() {
        return versionToUpgrade;
    }

    public void setVersionToUpgrade(String versionToUpgrade) {
        this.versionToUpgrade = versionToUpgrade;
    }


    public boolean isLoadImageFromDist() {
        return false;
    }


}