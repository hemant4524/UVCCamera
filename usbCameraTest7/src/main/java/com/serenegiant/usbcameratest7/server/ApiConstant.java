package com.serenegiant.usbcameratest7.server;


import com.serenegiant.usbcameratest7.BuildConfig;

/**
 * Api Constants
 */
public class ApiConstant {
    //Server
    public static  String SERVER = BuildConfig.BASE_URL;
    public static final String BASE_URL = SERVER ;


    //Request timeouts
    public static final int CONNECTION_TIMEOUT = 60;//in sec
    public static final int READ_TIMEOUT = 60;//in sec

    //
    public static final String HEADER_NAME_AUTHORIZATION = "Authorization";
    public static final String HEADER_NAME_AUTH_TOKEN = "auth-token";
    public static final String HEADER_NAME_APP_SECRET = "app-secret";
    public static final String HEADER_NAME_APP_ID = "app-id";
    public static final String KEY_BEARER = "Bearer ";
    public static final String HEADER_NAME_TOKEN_VALUE = "eyJpdiI6IlNYYWE0RmFpSkJta0tHc1dtcmJoVFE9PSIsInZhbHVlIjoiUWpxWEFEbmp3UU5xTXFPZm1Ualk5ZU1cL1NWcVA2Tm1RS3dhRHNKaHhwUFZ4Z3hySnM4SU5YQ2xzcEFpNFhtNkRla3FPcUxXUUt1ZklxNmlkTE94Q3VVd1NVeUd4REZLMlZNc01WZE8yU1RMVktTQkVVSjdlSGNCOFNaRlVZVThISGhIQU53cVY4ZDdNNVwvdlRZVXNmR2xvVGx1V21IcFpOWkx4Y2piSGNpdUZTQ1B2NXlndjNhYndJM0R3YjVlcXkraGlZN0xcL2NXQ2Q5YUVDYisrRjhVdUt1QTVKcDlmeXh1ZmttcDEyTWJkKzNTUk5lODloa3hGSDFRRktCV25xTCIsIm1hYyI6ImU5Y2M4Y2ViMzk0YmE0Mjc3NjU2ZDEyNjk3OWRhMzZkODdmMzFkZGJjNjAyNThiZDkwODM1MGZmNmY3ZTljODIifQ==";

      //
    public static final String DEVICE_UUID = "device_uuid";
    public static final String LOCATION_ID = "location_id";

    // ZoneData
    public static final String LAYOUT_ID = "layout_id";
    public static final String ZONE_ID = "zone_id";


    // Schedule
    public static final String UUID = "uuid";

    // Presentation
    public static final String SCHEDULE_ID = "schedule_id";
    public static final String PRESENTATION_ID = "presentation_id";


}