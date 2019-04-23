package com.serenegiant.usbcameratest7.server;

import com.ihsanbal.logging.LoggingInterceptor;
import com.serenegiant.usbcameratest7.BuildConfig;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.platform.Platform;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit : Rest client
 */
public class RestClient {

    //Singleton instance
    private static RestClient instance;

    //Api service instance
    private ApiService mApiService;

    /**
     * Private constructor
     */
    private RestClient() {
//        interceptor to log request and response info
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

//        interceptor to add custom headers
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                //set request headers
                Request.Builder builder = original.newBuilder();
//                builder.header(ApiConstant.HEADER_NAME_AUTHORIZATION, ApiConstant.KEY_BEARER + ApiConstant.HEADER_NAME_TOKEN_VALUE);
//                if(PreferenceHandler.getInstance().getAuthTokenValue() != null){
//                    builder.header(ApiConstant.HEADER_NAME_AUTH_TOKEN, PreferenceHandler.getInstance().getAuthTokenValue());
//                    builder.header(ApiConstant.HEADER_NAME_APP_SECRET, PreferenceHandler.getInstance().getAppSecretValue());
//                    builder.header(ApiConstant.HEADER_NAME_APP_ID, PreferenceHandler.getInstance().getAppIdValue());
//                }
                builder.method(original.method(), original.body());

                //build request
                Request request = builder.build();
                return chain.proceed(request);
            }
        };

        //init okhttp client to handle request
        OkHttpClient client = new OkHttpClient.Builder()
//                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(new LoggingInterceptor.Builder()
                        .loggable(!BuildConfig.IS_LIVE)
                        .log(Platform.INFO)
                        .log(1)
                        .request("Request")
                        .response("Response")
                        .build())
                .addInterceptor(interceptor)
                .connectTimeout(ApiConstant.CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(ApiConstant.READ_TIMEOUT, TimeUnit.SECONDS)
                .build();

        //prepare retrofit client
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //create request; this instance will be used to make api call on server
        mApiService = retrofit.create(ApiService.class);
    }

    /**
     * Get singleton instance for RestClient class
     *
     * @return Singleton instance
     */
    public static RestClient getInstance() {
        if (instance == null) {
            instance = new RestClient();
        }
        return instance;
    }

    /**
     * Get api service
     *
     * @return ApiService
     */
    public ApiService getApiService() {
        return mApiService;
    }




}