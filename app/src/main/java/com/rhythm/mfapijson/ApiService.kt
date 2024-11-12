package com.rhythm.mfapijson

import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("mf")
    suspend fun getAllSchemes(): List<Scheme>

    @GET("mf/{schemeCode}")
    suspend fun getSchemeDetails(@Path("schemeCode") schemeCode: Int): SchemeDetail
}