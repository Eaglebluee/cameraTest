package com.example.core_network.di

import android.util.Log
import com.example.core_network.BuildConfig
import okhttp3.Interceptor
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import okio.IOException

class LoggingInterceptor : Interceptor {

    private val TAG = LoggingInterceptor::class.java.simpleName

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val t1 = System.nanoTime()
        var requestLog = String.format("Sending request %s on %s%n%s",
            request.url,
            chain.connection(),
            request.headers)
        val response = chain.proceed(request)
        val t2 = System.nanoTime()

        val responseLog = String.format("Received response for %s in %.1fms%n%s",
            response.request.url,
            (t2 - t1) / 1e6,
            response.headers)

        val bodyString = response.body!!.string()

        if (BuildConfig.DEBUG) {
            if (request.method.compareTo("post", true) == 0) {
                requestLog = "\n" + requestLog + "\n" + requestBodyToString(request.body)
            }
            Log.d(TAG, "request\n$requestLog")
            Log.d(TAG, "response\n$responseLog\n$bodyString")
        }

        return response.newBuilder().body(bodyString.toResponseBody(response.body!!.contentType())).build()
    }

    private fun requestBodyToString(request: RequestBody?): String? {
        return try {
            val buffer = Buffer()
            if (request != null) request.writeTo(buffer) else return ""
            val ret = buffer.readUtf8()
            buffer.close()
            ret
        } catch (e: IOException) {
            "error"
        } catch (e: OutOfMemoryError) {
            "outOfMemory"
        }
    }
}