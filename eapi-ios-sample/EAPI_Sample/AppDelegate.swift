//
//  AppDelegate.swift
//  EAPI_Sample
//
//  Copyright © 2020 FitnessOnDemand. All rights reserved.
//

import UIKit

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        
        // Congifure Google Analytics for tracking during the application lifecycle
        configureGoogleServices()
        return true
    }

    private func configureGoogleServices() {
        guard let gai = GAI.sharedInstance() else {
          assert(false, "Google Analytics not configured correctly")
        }
        gai.tracker(withTrackingId: GA_TRACKING_ID)
        
        // Optional: automatically report uncaught exceptions.
        gai.trackUncaughtExceptions = true
        
        // Optional: configure GA to log at a verbose level
        //  gai.logger.logLevel = .verbose;
    }
    
    
    // MARK: UISceneSession Lifecycle

    func application(_ application: UIApplication, configurationForConnecting connectingSceneSession: UISceneSession, options: UIScene.ConnectionOptions) -> UISceneConfiguration {
        return UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
    }

    func application(_ application: UIApplication, didDiscardSceneSessions sceneSessions: Set<UISceneSession>) {
    }
}

