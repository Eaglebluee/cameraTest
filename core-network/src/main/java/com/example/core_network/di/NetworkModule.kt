package com.example.core_network.di

import android.util.Log
import com.example.core_network.adapter.FlowCallAdapterFactory
import com.example.core_network.service.ImageService
import com.example.core_network.service.ReportService
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.Retrofit.Builder
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {


    @Singleton
    @Provides
    fun getHeaderInterceptor() = Interceptor { chain ->
        val builder = chain.request().newBuilder().apply {
            addHeader("Content-Type", "application/json;charset=UTF-8")
        }
        chain.proceed(builder.build())
    }

    @Singleton
    @Provides
    fun getBodyInterceptor() = Interceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)
        val bodyString = response.body!!.string()
        val body = bodyString.toResponseBody(response.body!!.contentType())

        Log.d("123123123", "request : $request")
        Log.d("123123123", "response : $response")
        Log.d("123123123", "response body : ${bodyString}")
        return@Interceptor response.newBuilder().body(body).build()
    }

    @Singleton
    @Provides
    fun providesOkHttpClient(): OkHttpClient {
        val timeout = 2L
        val timeUnit = TimeUnit.MINUTES

        val okHttpClientBuilder = OkHttpClient.Builder()
            .addInterceptor(getBodyInterceptor())
            .connectTimeout(timeout, timeUnit)
            .writeTimeout(timeout, timeUnit)
            .readTimeout(timeout, timeUnit)
            .retryOnConnectionFailure(true)

        return okHttpClientBuilder.build()
    }

    @Singleton
    @Provides
    fun providesRetrofitBuilder(
        okHttpClient: OkHttpClient
    ): Retrofit.Builder =
        Retrofit.Builder()
            .client(okHttpClient)
            .addCallAdapterFactory(FlowCallAdapterFactory())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(Gson()))


    @Singleton
    @Provides
    fun providesImageApiService(
        retrofit: Retrofit.Builder
    ): ImageService = retrofit.baseUrl(providesImageUrl()).build().create(ImageService::class.java)

    @Singleton
    @Provides
    fun providesReportApiService(
        retrofit: Builder
    ): ReportService = retrofit.baseUrl(providesReportUrl()).build().create(ReportService::class.java)
}