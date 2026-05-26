package com.example.data.api

import com.example.data.model.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

interface RemoteApiService {

    @POST("auth")
    suspend fun authenticate(
        @Body request: AuthRequest
    ): AuthResponse

    @GET("system/telemetry")
    suspend fun getTelemetry(
        @Header("X-Auth-Token") token: String
    ): TelemetryResponse

    @GET("explorer/browse")
    suspend fun browseFiles(
        @Header("X-Auth-Token") token: String,
        @Query("path") path: String
    ): BrowseResponse

    @POST("vlc/open")
    suspend fun openFile(
        @Header("X-Auth-Token") token: String,
        @Body request: FileOpenRequest
    ): ActionResponse

    @POST("vlc/control")
    suspend fun controlVlc(
        @Header("X-Auth-Token") token: String,
        @Body request: ControlRequest
    ): ControlResponse

    @POST("system/command")
    suspend fun executeSystemCommand(
        @Header("X-Auth-Token") token: String,
        @Body request: SystemCommandRequest
    ): ActionResponse
}

object RetrofitClient {
    private var currentBaseUrl: String? = null
    private var apiService: RemoteApiService? = null

    /**
     * Obtains a Retrofit service instance configured dynamically for the target IP and port.
     */
    fun getService(ip: String, port: Int): RemoteApiService {
        val normalizedIp = ip.trim()
        val url = "http://$normalizedIp:$port/"
        
        if (url != currentBaseUrl || apiService == null) {
            currentBaseUrl = url
            
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(url)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            apiService = retrofit.create(RemoteApiService::class.java)
        }
        return apiService!!
    }
}
