package com.app.ekottel.Retrofit;

import com.app.ekottel.BuildConfig;
import com.google.gson.Gson;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    //public static final String BASE_URL="https://api.stripe.com/";
   // public static final String CREATE_PAYMENT_INTENT_BASE_URL="http://104.245.48.14:9294/api/";
    public static final String CREATE_PAYMENT_INTENT_BASE_URL="https://switch.banatelecom.com:9443/api/";


    private static Retrofit retrofit = null;

    /*public static Retrofit getClientWithAuth(final String token) {
        *//*Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                final Request request = chain.request().newBuilder().addHeader("Authorization", token).build();
                return chain.proceed(request);
            }};*//*

        AuthenticationInterceptor interceptor = new AuthenticationInterceptor(token);



        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.connectTimeout(100, TimeUnit.SECONDS);
        httpClient.readTimeout(100, TimeUnit.SECONDS);
        httpClient.addInterceptor(interceptor);
        httpClient.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        OkHttpClient client = httpClient.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(CREATE_PAYMENT_INTENT_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .build();
        return retrofit;
    }*/



    public static Retrofit getRestClient() {

//        OkHttpClient client = httpClient.build();
        OkHttpClient client = getUnsafeOkHttpClient();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(CREATE_PAYMENT_INTENT_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .build();
        return retrofit;
    }



    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectTimeout(100, TimeUnit.SECONDS);
            builder.readTimeout(100, TimeUnit.SECONDS);
            HttpLoggingInterceptor.Level logLevel;
            if(BuildConfig.DEBUG){
                logLevel = HttpLoggingInterceptor.Level.BODY;
            }else{
                logLevel = HttpLoggingInterceptor.Level.NONE;
            }
            builder.addInterceptor(new HttpLoggingInterceptor().setLevel(logLevel));
            return   builder.build();


        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }



    public static class AuthenticationInterceptor implements Interceptor {

        private String authToken;

        public AuthenticationInterceptor(String token) {
            this.authToken = token;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();

            Request.Builder builder = original.newBuilder()
                    .addHeader("Authorization", "Bearer " + authToken);
                    //.header("authorization", authToken);

            Request request = builder.build();
            return chain.proceed(request);
        }
    }
}

