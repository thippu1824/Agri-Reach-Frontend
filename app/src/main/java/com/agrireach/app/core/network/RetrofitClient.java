package com.agrireach.app.core.network;

import com.agrireach.app.core.session.AuthSession;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;

    public static synchronized Retrofit getInstance() {
        if (retrofit == null) {
            // Configure HTTP client
            OkHttpClient client = new OkHttpClient.Builder()
                    // Automatically attach JWT authentication
                    .addInterceptor(chain -> {
                        Request originalRequest = chain.request();
                        String accessToken =
                                AuthSession.getAccessToken();
                        Request.Builder requestBuilder =
                                originalRequest.newBuilder()
                                        .header("Accept", "application/json");
                        if (accessToken != null &&
                                !accessToken.isBlank()) {
                            requestBuilder.header(
                                    "Authorization",
                                    "Bearer " + accessToken
                            );
                        }
                        return chain.proceed(
                                requestBuilder.build()
                        );
                    })
                    .build();
            // Create Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(ApiConfig.BASE_URL)
                    .client(client)
                    .addConverterFactory(
                            GsonConverterFactory.create()
                    )
                    .build();
        }
        return retrofit;
    }
}