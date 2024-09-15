package com.example.mealmaestro

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.fragment.app.Fragment
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

        // Configure Remote Config settings
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600 // 1 hour interval
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(mapOf("openai_api_key" to ""))

        // Fetch the API key from Remote Config
        fetchApiKey()

        etQuestion.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                val question = etQuestion.text.toString().trim()
                if (question.isNotEmpty()) {
                    txtResponse.text = "Please wait..."
                    getResponse(question) { response ->
                        activity?.runOnUiThread { txtResponse.text = response }
                    }
                }
                true
            } else false
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

    private fun getResponse(question: String, callback: (String) -> Unit) {
        idTVQuestion.text = question
        etQuestion.setText("")

        if (!::apiKey.isInitialized || apiKey.isEmpty()) {
            callback("API Key not found.")
            return
        }

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
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback("Failed to connect: ${e.message}")
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
                            callback(textResult)
                        } catch (e: Exception) {
                            callback("Error parsing response: ${e.localizedMessage}")
                            Log.e("API_ERROR", "Response parsing failed", e)
                        }
                    } else {
                        callback("Error: ${resp.message} - ${body ?: "No response body"}")
                        Log.e("API_ERROR", "Server responded with error: ${resp.message} - ${body ?: "No response body"}")
                    }
                }
            }
        })
    }
}
