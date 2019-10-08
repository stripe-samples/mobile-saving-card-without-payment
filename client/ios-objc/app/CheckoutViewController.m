//
//  CheckoutViewController.m
//  app
//
//  Created by Ben Guo on 9/29/19.
//  Copyright © 2019 stripe-samples. All rights reserved.
//

#import "CheckoutViewController.h"
#import <Stripe/Stripe.h>

/**
* To run this app, you'll need to first run the sample server locally.
* Follow the "How to run locally" instructions in the root directory's README.md to get started.
* Once you've started the server, open http://localhost:4242 in your browser to check that the
* server is running locally.
* After verifying the sample server is running locally, build and run the app using the iOS simulator.
*/
NSString *const BackendUrl = @"http://127.0.0.1:4242/";

@interface CheckoutViewController ()  <STPAuthenticationContext>

@property (nonatomic, weak) STPPaymentCardTextField *cardTextField;
@property (nonatomic, weak) UIButton *payButton;
@property (nonatomic, weak) UITextField *emailTextField;
@property (nonatomic, copy) NSString *setupIntentClientSecret;

@end

@implementation CheckoutViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor whiteColor];
    
    UITextField *emailTextField = [UITextField new];
    emailTextField.borderStyle = UITextBorderStyleRoundedRect;
    emailTextField.placeholder = @"Enter your email";
    self.emailTextField = emailTextField;

    STPPaymentCardTextField *cardTextField = [[STPPaymentCardTextField alloc] init];
    self.cardTextField = cardTextField;

    UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];
    button.layer.cornerRadius = 5;
    button.backgroundColor = [UIColor systemBlueColor];
    button.titleLabel.font = [UIFont systemFontOfSize:22];
    [button setTitle:@"Save" forState:UIControlStateNormal];
    [button addTarget:self action:@selector(pay) forControlEvents:UIControlEventTouchUpInside];
    self.payButton = button;

    UIStackView *stackView = [[UIStackView alloc] initWithArrangedSubviews:@[emailTextField, cardTextField, button]];
    stackView.axis = UILayoutConstraintAxisVertical;
    stackView.translatesAutoresizingMaskIntoConstraints = NO;
    stackView.spacing = 20;
    [self.view addSubview:stackView];

    [NSLayoutConstraint activateConstraints:@[
        [stackView.leftAnchor constraintEqualToSystemSpacingAfterAnchor:self.view.leftAnchor multiplier:2],
        [self.view.rightAnchor constraintEqualToSystemSpacingAfterAnchor:stackView.rightAnchor multiplier:2],
        [stackView.topAnchor constraintEqualToSystemSpacingBelowAnchor:self.view.topAnchor multiplier:2],
    ]];

    [self startCheckout];
}

- (void)startCheckout {
    // Create a SetupIntent by calling the sample server's /create-setup-intent endpoint.
    NSURL *url = [NSURL URLWithString:[NSString stringWithFormat:@"%@create-setup-intent", BackendUrl]];
    NSMutableURLRequest *request = [[NSURLRequest requestWithURL:url] mutableCopy];
    [request setHTTPMethod:@"POST"];
    [request setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    NSURLSessionTask *task = [[NSURLSession sharedSession] dataTaskWithRequest:request completionHandler:^(NSData *data, NSURLResponse *response, NSError *requestError) {
        NSError *error = requestError;
        NSDictionary *json = [NSJSONSerialization JSONObjectWithData:data options:0 error:&error];
        NSHTTPURLResponse *httpResponse = (NSHTTPURLResponse *)response;
        if (error != nil || httpResponse.statusCode != 200 || json[@"publishableKey"] == nil) {
            dispatch_async(dispatch_get_main_queue(), ^{
                NSString *message = error.localizedDescription ?: @"";
                UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"Error loading page" message:message preferredStyle:UIAlertControllerStyleAlert];
                [alert addAction:[UIAlertAction actionWithTitle:@"OK" style:UIAlertActionStyleCancel handler:nil]];
                [self presentViewController:alert animated:YES completion:nil];
            });
        }
        else {
            NSLog(@"Created Setupintent");
            self.setupIntentClientSecret = json[@"clientSecret"];
            NSString *stripePublishableKey = json[@"publishableKey"];
            [Stripe setDefaultPublishableKey:stripePublishableKey];
        }
    }];
    [task resume];
}

- (void)pay {
    if (!self.setupIntentClientSecret) {
        NSLog(@"SetupIntent hasn't been created");
        return;
    }

    // Collect card details
    STPPaymentMethodCardParams *cardParams = self.cardTextField.cardParams;
    
    // Later, you will need to attach the PaymentMethod to the Customer it belongs to.
    // This example collects the customer's email to know which customer the PaymentMethod belongs to, but your app might use an account id, session cookie, etc.
    STPPaymentMethodBillingDetails *billingDetails = [STPPaymentMethodBillingDetails new];
    billingDetails.email = self.emailTextField.text;
    
    // Create SetupIntent confirm parameters with the above
    STPPaymentMethodParams *paymentMethodParams = [STPPaymentMethodParams paramsWithCard:cardParams billingDetails:billingDetails metadata:nil];
    STPSetupIntentConfirmParams *setupIntentParams = [[STPSetupIntentConfirmParams alloc] initWithClientSecret:self.setupIntentClientSecret];
    setupIntentParams.paymentMethodParams = paymentMethodParams;

    // Complete the setup
    STPPaymentHandler *paymentHandler = [STPPaymentHandler sharedHandler];
    [paymentHandler confirmSetupIntent:setupIntentParams withAuthenticationContext:self completion:^(STPPaymentHandlerActionStatus status, STPSetupIntent *setupIntent, NSError *error) {
        dispatch_async(dispatch_get_main_queue(), ^{
            switch (status) {
                case STPPaymentHandlerActionStatusFailed: {
                    NSString *message = error.localizedDescription ?: @"";
                    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"Setup failed" message:message preferredStyle:UIAlertControllerStyleAlert];
                    [alert addAction:[UIAlertAction actionWithTitle:@"OK" style:UIAlertActionStyleCancel handler:nil]];
                    [self presentViewController:alert animated:YES completion:nil];
                    break;
                }
                case STPPaymentHandlerActionStatusCanceled: {
                    NSString *message = error.localizedDescription ?: @"";
                    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"Setup canceled" message:message preferredStyle:UIAlertControllerStyleAlert];
                    [alert addAction:[UIAlertAction actionWithTitle:@"OK" style:UIAlertActionStyleCancel handler:nil]];
                    [self presentViewController:alert animated:YES completion:nil];
                    break;
                }
                case STPPaymentHandlerActionStatusSucceeded: {
                    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"Setup succeeded" message:setupIntent.description preferredStyle:UIAlertControllerStyleAlert];
                    [alert addAction:[UIAlertAction actionWithTitle:@"Restart demo" style:UIAlertActionStyleCancel handler:^(UIAlertAction *action) {
                        [self.cardTextField clear];
                        self.emailTextField.text = nil;
                        [self startCheckout];
                    }]];
                    [self presentViewController:alert animated:YES completion:nil];
                    break;
                }
                default:
                    break;
            }
        });
    }];
}

# pragma mark STPAuthenticationContext
- (UIViewController *)authenticationPresentingViewController {
    return self;
}

@end
