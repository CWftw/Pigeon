package com.jameswolfeoliver.pigeon.Server.Rest;


import com.jameswolfeoliver.pigeon.Server.Models.Requests.VersionResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RestService {
    @GET("versionInfo.txt")
    Call<VersionResponse> getLoginVersions();

    @GET("{pagePath}")
    Call<String> getPage(@Path(value = "pagePath", encoded = true) String pagePath);
}
