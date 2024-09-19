package com.example.mealmaestro


import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

// Singleton object for OkHttpClient to ensure only one instance of the client is used
object OkHttpSingleton {
    // Lazy initialization of OkHttpClient
    val client: OkHttpClient by lazy {
        // Build and return an OkHttpClient instance
        OkHttpClient.Builder().build()
    }
}

// Function to send a request and get a response from the OpenAI API
fun getResponse(
    question: String,           // The user's question to be sent to the OpenAI API
    apiKey: String,             // The API key for authenticating with OpenAI
    callback: (String) -> Unit, // Callback function to handle the successful response
    onFailure: (String) -> Unit // Callback function to handle errors or failures
) {
    // JSON body for the API request, including the user's question and model configuration
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

    // Build the HTTP request with headers and body
    val request = Request.Builder()
        .url("https://api.openai.com/v1/chat/completions") // OpenAI API endpoint
        .addHeader("Content-Type", "application/json")      // Specify the content type
        .addHeader("Authorization", "Bearer $apiKey")      // Add the API key for authentication
        .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull())) // Attach the request body
        .build()

    // Use the OkHttpSingleton client to make the API call asynchronously
    OkHttpSingleton.client.newCall(request).enqueue(object : Callback {
        // Handle failure in making the API call (e.g., network issues)
        override fun onFailure(call: okhttp3.Call, e: IOException) {
            // Call the onFailure callback with an error message
            onFailure("Failed to connect: ${e.message}")
        }

        // Handle the API response
        override fun onResponse(call: okhttp3.Call, response: Response) {
            // Use response body safely, closing it after processing
            response.use { resp ->
                val body = resp.body?.string() // Get the response body as a string
                // If the response is successful and the body is not null
                if (resp.isSuccessful && body != null) {
                    try {
                        // Parse the JSON response
                        val jsonObject = JSONObject(body)
                        val jsonArray = jsonObject.getJSONArray("choices")
                        // Extract the content of the response from the JSON object
                        val textResult = jsonArray.getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")
                        // Call the callback with the result text
                        callback(textResult)
                    } catch (e: Exception) {
                        // Handle JSON parsing errors
                        onFailure("Error parsing response: ${e.localizedMessage}")
                    }
                } else {
                    // Handle cases where the response is not successful
                    onFailure("Error: ${resp.message} - ${body ?: "No response body"}")
                }
            }
        }
    })
}