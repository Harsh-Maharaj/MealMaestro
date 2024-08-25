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
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.FileInputStream
import java.io.IOException
import java.util.Properties
import org.json.JSONObject

class AiFragment : Fragment() {

    private val client = OkHttpClient()
    private lateinit var txtResponse: TextView
    private lateinit var idTVQuestion: TextView
    private lateinit var etQuestion: TextInputEditText
    private lateinit var apiKey: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ai, container, false)

        // Load the API key from the properties file
        loadApiKey()

        etQuestion = view.findViewById(R.id.etQuestion)
        idTVQuestion = view.findViewById(R.id.idTVQuestion)
        txtResponse = view.findViewById(R.id.txtResponse)

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

    private fun loadApiKey() {
        val properties = Properties()
        val fileInputStream = FileInputStream("path/to/api_keys.properties")
        properties.load(fileInputStream)
        apiKey = properties.getProperty("OPENAI_API_KEY")
        fileInputStream.close()
    }

    private fun getResponse(question: String, callback: (String) -> Unit) {
        idTVQuestion.text = question
        etQuestion.setText("")

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
