package com.serenegiant.usbcameratest7.server;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * All the APIs (Server calls through the App) are written here.
 */
public interface ApiService {

   // url changed in between request.
   String BASE_URL="https://dmbdemo.com/camera_project/";

    @Multipart
    @POST("https://dmbdemo.com/camera_project/upload.php")
    Call<JsonObject> uploadMultiFile1(@Part MultipartBody.Part[] files);

}
