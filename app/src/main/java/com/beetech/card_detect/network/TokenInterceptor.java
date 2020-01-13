package com.beetech.card_detect.network;


import com.beetech.card_detect.utils.Define;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TokenInterceptor implements Interceptor {


    public TokenInterceptor() {
    }

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request.Builder ongoing = chain.request().newBuilder();
        Response response = chain.proceed(ongoing.build());
        int responseCode = response.code();
        if (responseCode == Define.Api.Http.RESPONSE_CODE_ACCESS_TOKEN_EXPIRED) {
            //handle token expire here
        }
        return response;
    }
}
