package com.beetech.card_detect.network;


import com.beetech.card_detect.base.ListResponse;
import com.beetech.card_detect.entity.SearchResponse;

import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiInterface {
    @GET("search")
    @Headers({"lang: vi", "Content-Type: application/json"})
    Single<ListResponse<SearchResponse>> search(@Query("s") String keyword,
                                                @Query("page") int pageIndex);

    @POST("sign-pdf")
    @Multipart
    Single<ResponseBody> signPdfFile(@Part("signed-location") RequestBody signedLocation,
                                     @Part MultipartBody.Part pdfFile,
                                     @Part MultipartBody.Part signFile);

    @POST("android/sign-pdf")
    @Headers({"x-api-key: f445a94491ea38548fc78b72f78a90ccc0726d44"})
    @Multipart
    Single<ResponseBody> signPdfFileWithUrl(@Part("signed-location") RequestBody signedLocation,
                                     @Part MultipartBody.Part pdfFile,
                                     @Part MultipartBody.Part signFile);
}
