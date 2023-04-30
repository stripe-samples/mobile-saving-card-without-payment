package com.example.app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.stripe.android.PaymentConfiguration
import com.stripe.android.model.ConfirmSetupIntentParams
import com.stripe.android.model.PaymentMethod
import com.stripe.android.model.PaymentMethodCreateParams
import com.stripe.android.payments.paymentlauncher.PaymentLauncher
import com.stripe.android.payments.paymentlauncher.PaymentResult
import com.stripe.android.view.CardInputWidget
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException


class CheckoutActivityKotlin : AppCompatActivity() {

    /**
     * To run this app, you'll need to first run the sample server locally.
     * Follow the "How to run locally" instructions in the root directory's README.md to get started.
     * Once you've started the server, open http://localhost:4242 in your browser to check that the
     * server is running locally.
     * After verifying the sample server is running locally, build and run the app using the
     * Android emulator.
     */
    private val backendUrl = Common.webApiUrl()
    private val httpClient = OkHttpClient()
    private lateinit var setupIntentClientSecret: String

    private lateinit var paymentLauncher: PaymentLauncher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        val paymentConfiguration = PaymentConfiguration.getInstance(applicationContext)
        paymentLauncher = PaymentLauncher.Companion.create(
            this,
            paymentConfiguration.publishableKey,
            paymentConfiguration.stripeAccountId,
            ::onPaymentResult
        )
        loadPage()
    }

    private fun loadPage() {
        // Create a SetupIntent by calling the sample server's /create-setup-intent endpoint.
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = "".toRequestBody(mediaType)
        val request = Request.Builder()
            .url(backendUrl + "create-setup-intent")
            .post(body)
            .build()

        httpClient.newCall(request)
            .enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        displayAlert("Error", e.toString(), true)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        runOnUiThread {
                            displayAlert("Error", response.toString(), true)
                        }
                    } else {
                        val responseData = response.body?.string()
                        val json = JSONObject(responseData)
                        setupIntentClientSecret = json.getString("clientSecret")
                    }
                }
            })

        // Hook up the pay button to the card widget and stripe instance
        val payButton: Button = findViewById(R.id.payButton)
        payButton.setOnClickListener {
            // Collect card details
            val cardInputWidget =
                findViewById<CardInputWidget>(R.id.cardInputWidget)
            val paymentMethodCard = cardInputWidget.paymentMethodCard

            // Later, you will need to attach the PaymentMethod to the Customer it belongs to.
            // This example collects the customer's email to know which customer the PaymentMethod belongs to, but your app might use an account id, session cookie, etc.
            val emailInput = findViewById<EditText>(R.id.emailInput)
            val billingDetails = PaymentMethod.BillingDetails.Builder()
                .setEmail((emailInput.text ?: "").toString())
                .build()

            // Create SetupIntent confirm parameters with the above
            if (paymentMethodCard != null) {
                val paymentMethodParams = PaymentMethodCreateParams
                    .create(paymentMethodCard, billingDetails, null)
                val confirmParams = ConfirmSetupIntentParams
                    .create(paymentMethodParams, setupIntentClientSecret)
                lifecycleScope.launch {
                    paymentLauncher.confirm(confirmParams)
                }
            }
        }

    }

    private fun onPaymentResult(paymentResult: PaymentResult) {
        var title = ""
        var message = ""
        var restartDemo = false
        when (paymentResult) {
            is PaymentResult.Completed -> {
                title = "Setup Completed"
                restartDemo = true
            }
            is PaymentResult.Canceled -> {
                title = "Setup Canceled"
            }
            is PaymentResult.Failed -> {
                title = "Setup Failed"
                message = paymentResult.throwable.message!!
            }
        }
        displayAlert(title, message, restartDemo)
    }

    private fun displayAlert(
        title: String,
        message: String,
        restartDemo: Boolean = false
    ) {
        runOnUiThread {
            val cardInputWidget =
                findViewById<CardInputWidget>(R.id.cardInputWidget)
            val builder = AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
            if (restartDemo) {
                builder.setPositiveButton("Restart demo") { _, _ ->
                    cardInputWidget.clear()
                    loadPage()
                }
            }
            else {
                builder.setPositiveButton("Ok", null)
            }
            builder
                .create()
                .show()
        }
    }
}
