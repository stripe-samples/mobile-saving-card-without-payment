# Saving cards without payment on mobile (iOS & Android)

This sample shows how to build a form to save a credit card without taking a payment using the [Setup Intents API](https://stripe.com/docs/api/setup_intents).

## How to run

This sample includes 5 server implementations in Node, Ruby, Python, Java, and PHP. It also includes 4 mobile client implementations, in Swift (iOS), Objective-C (iOS), Kotlin (Android), and Java (Android).

Follow the steps below to run locally.

**1. Clone the repository:**

```
git clone https://github.com/stripe-samples/mobile-saving-card-without-payment
```

**2. Copy the .env.example to a .env file:**

```
cp .env.example .env
```

You will need a Stripe account in order to run the demo. Once you set up your account, go to the Stripe [developer dashboard](https://stripe.com/docs/development#api-keys) to find your API keys.

```
STRIPE_PUBLIC_KEY=<replace-with-your-publishable-key>
STRIPE_SECRET_KEY=<replace-with-your-secret-key>
```

`CLIENT_DIR` tells the server where the client files are located and does not need to be modified unless you move the server files.

**3. Follow the server instructions on how to run:**

Pick the [server language](server) you want and navigate to its directory (e.g. `cd server/node`).  Follow the instructions in the server folder README to run the server locally.

**4. [Optional] Run a webhook locally:**

If you want to test the integration with a local webhook on your machine, you can use the Stripe CLI to easily spin one up.

First [install the CLI](https://stripe.com/docs/stripe-cli) and [link your Stripe account](https://stripe.com/docs/stripe-cli#link-account).

```
stripe listen --forward-to localhost:4242/webhook
```

The CLI will print a webhook secret key to the console. Set `STRIPE_WEBHOOK_SECRET` to this value in your .env file.

You should see events logged in the console where the CLI is running.

When you are ready to create a live webhook endpoint, follow our guide in the docs on [configuring a webhook endpoint in the dashboard](https://stripe.com/docs/webhooks/setup#configure-webhook-settings). 

**5. Set up the client app:**

Next, choose a [client app](client), and follow the instruction in the folders' README to run.

When the app is running, use `4242424242424242` as a test card number with any CVC code + a future expiration date.

Use the `4000000000003220` test card number to trigger a 3D Secure challenge flow.

Read more about testing on Stripe at https://stripe.com/docs/testing.

## FAQ
Q: Why did you pick these frameworks?

A: We chose the most minimal framework to convey the key Stripe calls and concepts you need to understand. These demos are meant as an educational tool that helps you roadmap how to integrate Stripe within your own system independent of the framework.

Q: Can you show me how to build X?

A: We are always looking for new sample ideas, please email dev-samples@stripe.com with your suggestion!

## Author(s)
- [@yuki-stripe](https://github.com/yuki-stripe)
- [@bg-stripe](https://github.com/bg-stripe)
