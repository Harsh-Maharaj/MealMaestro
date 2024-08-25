package com.example.mealmaestro


import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

object OkHttpSingleton {
    val client: OkHttpClient by lazy {
        OkHttpClient.Builder().build()
    }
}

fun getResponse(
    question: String,
    apiKey: String,
    callback: (String) -> Unit,
    onFailure: (String) -> Unit
) {
    val requestBody = """
        {
            "model": "gpt-3.5-turbo",
            "messages": [
                {"role": "user", "content": "$question"}
            ],
            "max_tokens": 500,
            "temperature": 0.7
        }
    """.trimIndent()

    val request = Request.Builder()
        .url("https://api.openai.com/v1/chat/completions")
        .addHeader("Content-Type", "application/json")
        .addHeader("Authorization", "Bearer sk-k6HaNpMmUXydD-iwATLJyWulEBq-C2fqJVzZfvrakCT3BlbkFJk_DNOug9WOHHdjWTzAePIuyHu2oIvZrOpgITnFOzIA")
        .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
        .build()

    OkHttpSingleton.client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: okhttp3.Call, e: IOException) {
            onFailure("Failed to connect: ${e.message}")
        }

        override fun onResponse(call: okhttp3.Call, response: Response) {
            response.use { resp ->
                val body = resp.body?.string()
                if (resp.isSuccessful && body != null) {
                    try {
                        val jsonObject = JSONObject(body)
                        val jsonArray = jsonObject.getJSONArray("choices")
                        val textResult = jsonArray.getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")
                        callback(textResult)
                    } catch (e: Exception) {
                        onFailure("Error parsing response: ${e.localizedMessage}")
                    }
                } else {
                    onFailure("Error: ${resp.message} - ${body ?: "No response body"}")
                }
            }
        }
    })
}


