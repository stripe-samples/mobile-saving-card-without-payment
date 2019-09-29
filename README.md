# Saving cards without payment on mobile (iOS & Android)

This sample shows how to build a form to save a credit card without taking a payment using the [Setup Intents API](https://stripe.com/docs/api/setup_intents).

## How to run

This sample includes 5 server implementations in Node, Ruby, Python, Java, and PHP. It also includes 4 mobile client implementations, in Swift (iOS), Objective-C (iOS), Kotlin (Android), and Java (Android).

To run a sample server locally, first copy the .env.example file to your own .env file:

```
cp .env.example .env
```

You will need a Stripe account with its own set of [API keys](https://stripe.com/docs/development#api-keys). Enter your Stripe secret API key in the .env file.

Then, choose a sample server, and follow the instructions in the server's README to run locally.

Next, choose a client app, and follow the instruction in the app's README to run.

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
