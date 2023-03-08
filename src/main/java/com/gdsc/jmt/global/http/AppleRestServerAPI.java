package com.gdsc.jmt.global.http;

import com.gdsc.jmt.domain.user.apple.Keys;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface AppleRestServerAPI {
    @GET
    Call<Keys> sendAPI(@Header("content-type") String contentType, @Body String params);
}
