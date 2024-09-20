package com.example.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.model.ConfirmSetupIntentParams;
import com.stripe.android.model.PaymentMethod;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.payments.paymentlauncher.PaymentLauncher;
import com.stripe.android.payments.paymentlauncher.PaymentResult;
import com.stripe.android.view.CardInputWidget;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class CheckoutActivityJava extends AppCompatActivity {
    /**
     * To run this app, you'll need to first run the sample server locally.
     * Follow the "How to run locally" instructions in the root directory's README.md to get started.
     * Once you've started the server, open http://localhost:4242 in your browser to check that the
     * server is running locally.
     * After verifying the sample server is running locally, build and run the app using the
     * Android emulator.
     */
    private static final String backendUrl = Common.webApiUrl();
    private OkHttpClient httpClient = new OkHttpClient();
    private String setupIntentClientSecret;
    private PaymentLauncher paymentLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        final PaymentConfiguration paymentConfiguration = PaymentConfiguration.getInstance(getApplicationContext());
        paymentLauncher = PaymentLauncher.Companion.create(
                this,
                paymentConfiguration.getPublishableKey(),
                paymentConfiguration.getStripeAccountId(),
                this::onPaymentResult
        );
        loadPage();
    }

    private void loadPage() {
        // Create a SetupIntent by calling the sample server's /create-setup-intent endpoint.
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create("", mediaType);
        Request request = new Request.Builder()
                .url(backendUrl + "create-setup-intent")
                .post(body)
                .build();
        httpClient.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(() -> {
                            displayAlert("Error", e.toString(), true);
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            runOnUiThread(() -> {
                                displayAlert("Error", response.toString(), true);
                            });
                        } else {
                            Gson gson = new Gson();
                            Type type = new TypeToken<Map<String, String>>(){}.getType();
                            Map<String, String> responseMap = gson.fromJson(response.body().string(), type);

                            setupIntentClientSecret = responseMap.get("clientSecret");
                        }
                    }
                });

        // Hook up the pay button to the card widget and stripe instance
        Button payButton = findViewById(R.id.payButton);
        payButton.setOnClickListener((View view) -> {
            // Collect card details
            CardInputWidget cardInputWidget = findViewById(R.id.cardInputWidget);
            PaymentMethodCreateParams.Card card = cardInputWidget.getPaymentMethodCard();

            // Later, you will need to attach the PaymentMethod to the Customer it belongs to.
            // This example collects the customer's email to know which customer the PaymentMethod belongs to, but your app might use an account id, session cookie, etc.
            EditText emailInput = findViewById(R.id.emailInput);
            PaymentMethod.BillingDetails billingDetails = (new PaymentMethod.BillingDetails.Builder())
                    .setEmail(emailInput.getText().toString())
                    .build();
            if (card != null) {
                // Create SetupIntent confirm parameters with the above
                PaymentMethodCreateParams paymentMethodParams = PaymentMethodCreateParams
                        .create(card, billingDetails);
                ConfirmSetupIntentParams confirmParams = ConfirmSetupIntentParams
                        .create(paymentMethodParams, setupIntentClientSecret);
                paymentLauncher.confirm(confirmParams);
            }
        });
    }

    private void onPaymentResult(PaymentResult paymentResult) {
        String title = "";
        String message = "";
        boolean restartDemo = false;
        if (paymentResult instanceof PaymentResult.Completed) {
            title = "Setup Completed";
            restartDemo = true;
        } else if (paymentResult instanceof PaymentResult.Canceled) {
            title = "Setup Canceled";
        } else if (paymentResult instanceof PaymentResult.Failed) {
            title = "Setup Failed";
            message = ((PaymentResult.Failed) paymentResult).getThrowable().getMessage();
        }
        displayAlert(title, message, restartDemo);
    }

    private void displayAlert(String title, String message, Boolean restartDemo) {
        runOnUiThread(()-> {
            final CardInputWidget cardInputWidget= findViewById(R.id.cardInputWidget);
            final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(message);
            if (restartDemo) {
                builder.setPositiveButton("Restart Demo", (v1, v2) -> {
                    cardInputWidget.clear();
                    loadPage();
                });
            } else {
                builder.setPositiveButton("Ok", null);
            }
            builder.create().show();
        });
    }
}
