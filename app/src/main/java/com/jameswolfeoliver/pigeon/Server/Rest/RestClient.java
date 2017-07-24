package com.jameswolfeoliver.pigeon.Server.Rest;

import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestClient {
    private static final String BASE_URL = "https://raw.githubusercontent.com/jameswolfeoliver/pigeon-web/master/";
    private static RestClient instance;
    private RestService restService;
    private RestService stringResponseRestService;

    private RestClient() {
    }

    public static RestClient getInstance() {
        if (instance == null) {
            instance = new RestClient();
        }
        return instance;
    }

    private RestService buildRestService(Converter.Factory factory) {
        return this.restService = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(factory)
                .build().create(RestService.class);
    }

    public RestService getRestService() {
        if (restService == null) {
            restService = buildRestService(GsonConverterFactory.create(PigeonApplication.getGson()));
        }
        return restService;
    }

    public RestService getStringResponseRestService() {
        if (stringResponseRestService == null) {
            stringResponseRestService = buildRestService(new StringConverterFactory());
        }
        return stringResponseRestService;
    }

    private class StringConverterFactory extends Converter.Factory {
        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type,
                                                                Annotation[] annotations,
                                                                Retrofit retrofit) {
            if (String.class.equals(type)) {
                return new Converter<ResponseBody, Object>() {

                    @Override
                    public Object convert(ResponseBody responseBody) throws IOException {
                        return responseBody.string();
                    }
                };
            }

            return null;
        }

        @Override
        public Converter<?, RequestBody> requestBodyConverter(Type type,
                                                              Annotation[] parameterAnnotations,
                                                              Annotation[] methodAnnotations,
                                                              Retrofit retrofit) {
            if (String.class.equals(type)) {
                return new Converter<String, RequestBody>() {

                    @Override
                    public RequestBody convert(String value) throws IOException {
                        return RequestBody.create(MediaType.parse("text/plain"), value);
                    }
                };
            }
            return null;
        }
    }
}
