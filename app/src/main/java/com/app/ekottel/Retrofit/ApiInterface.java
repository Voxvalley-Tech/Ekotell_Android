package com.app.ekottel.Retrofit;


import com.app.ekottel.RetroModels.CreatePaymentIntentRes;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface ApiInterface {


    @POST("v1/payment_intents")
    @FormUrlEncoded
    Call<CreatePaymentIntentRes> createPaymentIntentReq(@Field("amount") String username,
                                                        @Field("currency") String password
    );

    @POST()
    @FormUrlEncoded
    Call<JsonElement> createStripIntent(@Url String url,@Field("username") String userName,
                                        @Field("emailid") String email,
                                        @Field("amount") String amount,
                                        @Field("currency") String currency);

    @POST()
    @FormUrlEncoded
    Call<JSONObject> verifyStripe(@Url String url,@Field("username") String userName,
                                  @Field("emailid") String email,
                                  @Field("amount") String amount,
                                  @Field("paymentid") String paymentid);
    @GET()
    Call<JSONObject> verifypaypal(@Url String url,@Field("paymentid") String paymentid,
                                  @Field("username") String username,
                                  @Field("emailid") String emailid,
                                  @Field("environment") String environment);



    @GET()
    Call<JsonObject> getStripePublishableKey(@Url String url);

    @GET
    Call<String> verifyPaypal(@Url String url, @Query("paymentid") String paymentid, @Query("username") String userName,
                              @Query("emailid") String email,
                              @Query("environment") String environment);


      /*

    {
        "id": "pi_1FM1HoHjMNmZQQqsPhVWOMar",
            "object": "payment_intent",
            "allowed_source_types": [
        "card"
  ],
        "amount": 50,
            "amount_capturable": 0,
            "amount_received": 0,
            "application": null,
            "application_fee_amount": null,
            "canceled_at": null,
            "cancellation_reason": null,
            "capture_method": "automatic",
            "charges": {
        "object": "list",
                "data": [

    ],
        "has_more": false,
                "total_count": 0,
                "url": "/v1/charges?payment_intent=pi_1FM1HoHjMNmZQQqsPhVWOMar"
    },
        "client_secret": "pi_1FM1HoHjMNmZQQqsPhVWOMar_secret_YUJvxU04SofAK20YyTxY6zRuL",
            "confirmation_method": "automatic",
            "created": 1569282584,
            "currency": "usd",
            "customer": null,
            "description": null,
            "invoice": null,
            "last_payment_error": null,
            "livemode": true,
            "metadata": {
    },
        "next_action": null,
            "next_source_action": null,
            "on_behalf_of": null,
            "payment_method": null,
            "payment_method_options": {
        "card": {
            "request_three_d_secure": "automatic"
        }
    },
        "payment_method_types": [
        "card"
  ],
        "receipt_email": null,
            "review": null,
            "setup_future_usage": null,
            "shipping": null,
            "source": null,
            "statement_descriptor": null,
            "statement_descriptor_suffix": null,
            "status": "requires_source",
            "transfer_data": null,
            "transfer_group": null
    }

    */
}
