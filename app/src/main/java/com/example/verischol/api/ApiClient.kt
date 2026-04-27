package com.example.verischol.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    fun create(): IssuerApi {
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")   // <-- FIXED: trailing slash added
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IssuerApi::class.java)
    }
}
