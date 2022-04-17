#import "UpdatePlugin.h"
#import <Cordova/CDVPlugin.h>
#import "SystemConfiguration/SystemConfiguration.h"

@interface Updater:NSObject

@property NSString *alertTitle;
@property NSString *alertMessage;
@property NSString *alertUpdateButtonTitle;
@property NSString *alertCancelButtonTitle;

@end


@implementation Updater

- (id)init
{
    if (self) {
        self.alertTitle = @"New Version";
        self.alertMessage = @"Version %@ is available on the AppStore.";
        self.alertUpdateButtonTitle = @"Update";
        self.alertCancelButtonTitle = @"Not Now";
    }
    return self;
}

- (BOOL)hasConnection
{
    const char *host = "itunes.apple.com";
    BOOL reachable;
    BOOL success;
   
    // Link SystemConfiguration.framework! <SystemConfiguration/SystemConfiguration.h>
    SCNetworkReachabilityRef reachability = SCNetworkReachabilityCreateWithName(NULL, host);
    SCNetworkReachabilityFlags flags;
    success = SCNetworkReachabilityGetFlags(reachability, &flags);
    reachable = success && (flags & kSCNetworkFlagsReachable) && !(flags & kSCNetworkFlagsConnectionRequired);
    CFRelease(reachability);
    return reachable;
}

NSString *appStoreURL = nil;

+ (NSInteger)daysBetweenDate:(NSDate*)fromDateTime endDate:(NSDate*)toDateTime
{
    NSDate *fromDate;
    NSDate *toDate;

    NSCalendar *calendar = [NSCalendar currentCalendar];

    [calendar rangeOfUnit:NSCalendarUnitDay startDate:&fromDate
        interval:NULL forDate:fromDateTime];
    [calendar rangeOfUnit:NSCalendarUnitDay startDate:&toDate
        interval:NULL forDate:toDateTime];

    NSDateComponents *difference = [calendar components:NSCalendarUnitDay
        fromDate:fromDate toDate:toDate options:0];

    return [difference day];
}


- (void)checkNewAppVersion:(void(^)(BOOL newVersion, NSString *version,NSInteger days))completion
{
    NSDictionary *bundleInfo = [[NSBundle mainBundle] infoDictionary];
    NSString *bundleIdentifier = bundleInfo[@"CFBundleIdentifier"];
    NSString *currentVersion = bundleInfo[@"CFBundleShortVersionString"];
   
    NSLocale *currentLocale = [NSLocale currentLocale];
    NSString *countryCode = [currentLocale objectForKey:NSLocaleCountryCode];
    NSURL *lookupURL = [NSURL URLWithString:[NSString stringWithFormat:@"https://itunes.apple.com/lookup?bundleId=%@&t=%f&country=%@",
                                             bundleIdentifier, [NSDate.date timeIntervalSince1970],countryCode]];
   
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_HIGH, 0), ^(void) {
       
        NSData *lookupResults = [NSData dataWithContentsOfURL:lookupURL];
        if (!lookupResults) {
            completion(NO, nil, 0);
            return;
        }
       
        NSDictionary *jsonResults = [NSJSONSerialization JSONObjectWithData:lookupResults options:0 error:nil];
       
        dispatch_async(dispatch_get_main_queue(), ^(void) {
            NSUInteger resultCount = [jsonResults[@"resultCount"] integerValue];
            if (resultCount){
                NSDictionary *appDetails = [jsonResults[@"results"] firstObject];
                NSString *appItunesUrl = [appDetails[@"trackViewUrl"] stringByReplacingOccurrencesOfString:@"&uo=4" withString:@""];
                NSString *latestVersion = appDetails[@"version"];
                NSString *currentVersionReleaseDate = appDetails [@"currentVersionReleaseDate"];
                NSDateFormatter * dateFormatter = [NSDateFormatter new];
                [dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ssZ"];
                NSDate *releaseDate = [dateFormatter dateFromString:currentVersionReleaseDate];
                
                if ([latestVersion compare:currentVersion options:NSNumericSearch] == NSOrderedDescending) {
                    appStoreURL = appItunesUrl;
                    NSInteger daysDiff = [Updater daysBetweenDate:releaseDate endDate:[NSDate date]];
                    completion(YES, latestVersion, daysDiff);
                } else {
                    completion(NO, nil, 0);
                }
            } else {
                completion(NO, nil, 0);
            }
        });
    });
}

- (void)alertUpdateForVersion:(NSString *)version withForce:(BOOL)force
{
    UIWindow *foundWindow = nil;
    NSArray *windows = [[UIApplication sharedApplication] windows];

    for (UIWindow *window in windows) {
        if (window.isKeyWindow) {
            foundWindow = window;
            break;
        }
    }
    if (foundWindow)
    {
        NSString *alertMessage = [NSString stringWithFormat:self.alertMessage, version];
        UIAlertController *alert = [UIAlertController alertControllerWithTitle:self.alertTitle message:alertMessage preferredStyle:UIAlertControllerStyleAlert];

        UIAlertAction *updateAction = [UIAlertAction actionWithTitle:self.alertUpdateButtonTitle style:UIAlertActionStyleDefault handler:^(UIAlertAction * action) {
            [[UIApplication sharedApplication] openURL:[NSURL URLWithString:appStoreURL] options:@{} completionHandler:nil];
            if(force){
                [foundWindow.rootViewController presentViewController:alert animated:YES completion:nil];
            }
        }];
        [alert addAction:updateAction];

        if (!force) {
            UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:self.alertCancelButtonTitle style:UIAlertActionStyleCancel handler:nil];
            [alert addAction:cancelAction];
        }
        
        [foundWindow.rootViewController presentViewController:alert animated:YES completion:nil];
    }
}

@end



@implementation CDVUpdatePlugin

- (void)update:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    NSDictionary *args = [command.arguments objectAtIndex:0];
    NSInteger flexibleUpdateStalenessDays = [args[@"flexibleUpdateStalenessDays"] integerValue];
    NSInteger immediateUpdateStalenessDays = [args[@"immediateUpdateStalenessDays"] integerValue];
    Updater *updater = [Updater new];
    BOOL hasConnection = [updater hasConnection];
    if (hasConnection) {
        [updater checkNewAppVersion:^(BOOL newVersion, NSString *version, NSInteger days) {
            if(newVersion){
                if(days >= immediateUpdateStalenessDays){
                    [updater alertUpdateForVersion:version withForce:YES];
                }
                else if(days >= flexibleUpdateStalenessDays){
                    [updater alertUpdateForVersion:version withForce:NO];
                }
            }
        }];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"success"];
    }
    else
    {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"failure"];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

@end



 
