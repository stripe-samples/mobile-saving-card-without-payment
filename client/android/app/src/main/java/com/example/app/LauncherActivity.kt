package com.example.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.stripe.android.PaymentConfiguration
import kotlinx.android.synthetic.main.activity_launcher.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class LauncherActivity : AppCompatActivity() {
    // 10.0.2.2 is the Android emulator's alias to localhost
    private val backendUrl = "http://10.0.2.2:4242/"
    private val httpClient = OkHttpClient()
    private lateinit var publishableKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        fetchPublishableKey()

        launch_checkout_kotlin.setOnClickListener {
            launchActivity(CheckoutActivityKotlin::class.java)
        }
        launch_checkout_java.setOnClickListener {
            launchActivity(CheckoutActivityJava::class.java)
        }
    }
    private fun launchActivity(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass))
    }

    private fun displayAlert(
        title: String,
        message: String
    ) {
        runOnUiThread {
            val builder = AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)

            builder.setPositiveButton("Ok", null)
            builder
                .create()
                .show()
        }
    }

    private fun fetchPublishableKey() {
        // Create a SetupIntent by calling the sample server's /create-setup-intent endpoint.
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = "".toRequestBody(mediaType)
        val request = Request.Builder()
            .url(backendUrl + "create-setup-intent")
            .post(body)
            .build()

        httpClient.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                displayAlert("Failed to load page", "Error: $e")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    displayAlert("Failed to load page", "Error: $response"
                    )
                } else {
                    val responseData = response.body?.string()
                    val responseJson =
                        responseData?.let { JSONObject(it) } ?: JSONObject()
                    // For added security, our sample app gets the publishable key
                    // from the server.
                    publishableKey = responseJson.getString("publishableKey")

                    // Set up PaymentConfiguration with your Stripe publishable key
                    PaymentConfiguration.init(applicationContext, publishableKey)
                }
            }
        })
    }
}