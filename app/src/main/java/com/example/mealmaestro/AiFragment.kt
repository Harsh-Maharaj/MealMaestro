package com.example.mealmaestro

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.firebase.ktx.Firebase
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class AiFragment : Fragment() {

    private val client = OkHttpClient()
    private lateinit var txtResponse: TextView
    private lateinit var idTVQuestion: TextView
    private lateinit var etQuestion: TextInputEditText
    private lateinit var aiImageResponse: ImageView
    private lateinit var btnSubmitQuestion: MaterialButton // Button to trigger the AI request
    private lateinit var apiKey: String

    // Initialize Firebase Remote Config
    private val remoteConfig: FirebaseRemoteConfig by lazy {
        Firebase.remoteConfig
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ai, container, false)

        etQuestion = view.findViewById(R.id.etQuestion)
        idTVQuestion = view.findViewById(R.id.idTVQuestion)
        txtResponse = view.findViewById(R.id.txtResponse)
        aiImageResponse = view.findViewById(R.id.ai_image_response)
        btnSubmitQuestion = view.findViewById(R.id.btnSubmitQuestion)

        // Introduce Maestro when the user enters the page
        txtResponse.text = "Hi I'm Maestro, your virtual food AI assistant"

        // Configure Remote Config settings
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600 // 1 hour interval
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(mapOf("openai_api_key" to ""))

        // Fetch the API key from Remote Config
        fetchApiKey()

        // Submit the query when pressing the "send" button on the keyboard
        etQuestion.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                handleQuerySubmit()
                true
            } else false
        }

        // Submit the query when clicking the "Ask AI" button
        btnSubmitQuestion.setOnClickListener {
            handleQuerySubmit()
        }

        return view
    }

    // Fetch API key from Firebase Remote Config
    private fun fetchApiKey() {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    apiKey = remoteConfig.getString("openai_api_key")
                    Log.d("AiFragment", "API Key fetched successfully: $apiKey")
                } else {
                    Log.e("AiFragment", "Failed to fetch API key from Firebase")
                    txtResponse.text = "Failed to fetch API key."
                }
            }
    }

    // Function to handle submitting a query
    private fun handleQuerySubmit() {
        val question = etQuestion.text.toString().trim()

        // Filter out non-food-related queries
        if (question.isEmpty()) {
            txtResponse.text = "Please ask something about food."
            return
        } else if (!isFoodRelated(question)) {
            txtResponse.text = "I'm sorry, I can only help with food-related questions."
            return
        }

        txtResponse.text = "Please wait..."
        getResponse(question) { response, imageUrl ->
            activity?.runOnUiThread {
                txtResponse.text = response
                if (imageUrl != null) {
                    aiImageResponse.visibility = View.VISIBLE
                    Glide.with(this@AiFragment).load(imageUrl).into(aiImageResponse)
                } else {
                    aiImageResponse.visibility = View.GONE
                }
            }
        }
    }

    // Function to determine if the query is food-related
    private fun isFoodRelated(question: String): Boolean {
        val foodKeywords = listOf("food", "recipe", "meal", "ingredient", "dish", "cooking", "nutrition", "restaurant", "diet", "calories")
        return foodKeywords.any { question.contains(it, ignoreCase = true) }
    }

    // Function to request AI response
    private fun getResponse(question: String, callback: (String, String?) -> Unit) {
        idTVQuestion.text = question
        etQuestion.setText("")

        if (!::apiKey.isInitialized || apiKey.isEmpty()) {
            callback("API Key not found.", null)
            return
        }

        val requestBody = """
            {
                "model": "gpt-4o",
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
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback("Failed to connect: ${e.message}", null)
                Log.e("API_ERROR", "API call failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { resp ->
                    val body = resp.body?.string()
                    if (resp.isSuccessful && body != null) {
                        try {
                            val jsonObject = JSONObject(body)
                            val jsonArray = jsonObject.getJSONArray("choices")
                            val textResult = jsonArray.getJSONObject(0).getJSONObject("message").getString("content")

                            // Example: Check for image URL in response (adjust based on actual response format)
                            val imageUrl = if (textResult.contains("http")) {
                                val start = textResult.indexOf("http")
                                val end = textResult.indexOf(" ", start).takeIf { it > start } ?: textResult.length
                                textResult.substring(start, end)
                            } else {
                                null
                            }

                            callback(textResult, imageUrl)
                        } catch (e: Exception) {
                            callback("Error parsing response: ${e.localizedMessage}", null)
                            Log.e("API_ERROR", "Response parsing failed", e)
                        }
                    } else {
                        callback("Error: ${resp.message} - ${body ?: "No response body"}", null)
                        Log.e("API_ERROR", "Server responded with error: ${resp.message} - ${body ?: "No response body"}")
                    }
                }
            }
        })
    }
}
