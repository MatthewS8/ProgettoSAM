package com.github.matthews8.placeswishlist.utils


import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object RetrofitImpl {
    val geocodeApi: geocodingAPI by lazy {
        Retrofit.Builder()
            .baseUrl(geocodingAPI.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(geocodingAPI::class.java)
    }
}
interface geocodingAPI{
    @GET("/maps/api/geocode/json")
    fun getGeocoding(
        @Query("latlng") latLng: String,
        @Query("key") key: String
    ): Call<GeocoderResponse?>?

    companion object{
        const val BASE_URL = "https://maps.googleapis.com"
    }
}