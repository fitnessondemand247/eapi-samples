//
//  GoogleAnalyticsClient.swift
//  EAPI_Sample
//
//  Copyright © 2020 FitnessOnDemand. All rights reserved.
//

import Foundation

public extension GAI {

    static func fod_trackPlaybackEvent(action: String, videoName: String, seekTimeInMillis: Int) {
        GAI.fod_trackEvent(GA_CATEGORY_VIDEO, action: action, label: videoName, value: seekTimeInMillis)
    }
    
    static func fod_trackErrorEvent(message: String, videoId: Int) {
        GAI.fod_trackEvent(GA_CATEGORY_VIDEO, action: GA_ACTION_VIDEO_PLAYER_ERROR, label: message, value: videoId)
    }
    
    fileprivate static func fod_trackEvent(_ category: String, action: String, label: String? = nil, value: Int? = nil) {
        
        let tracker = GAI.sharedInstance().defaultTracker
        
        var number: NSNumber? = nil
        if let value = value {
            number = NSNumber(value: value as Int)
        }
        
        if let builder = GAIDictionaryBuilder.createEvent(withCategory: category, action: action, label: label, value: number) {
            tracker?.send(builder.build() as [NSObject: AnyObject])
        } else {
            assertionFailure("analytics builder failed")
        }
    }
}
