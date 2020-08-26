//
//  AnalyticsLabels.swift
//  EAPI_Sample
//
//  Copyright © 2020 Forgeahead. All rights reserved.
//

import Foundation

public let kViewControllerPlaybackServicePolicyKey = "{your brightcove policy key"
public let kViewControllerAccountID = "{your brightcove account id}"

public let GA_TRACKING_ID = "{your EAPI Google Analytics tracking id}"

// Category (all analytics logged to video category)
public let GA_CATEGORY_VIDEO = "Video"

// Action
public let GA_ACTION_VIDEO_PLAYBACK_BILLABLE_PLAY = "Billable Play"
public let GA_ACTION_VIDEO_PLAYBACK_FINISHED = "Video Play Finished"
public let GA_ACTION_VIDEO_PLAYBACK_PLAY = "Video Play Started"
public let GA_ACTION_VIDEO_PLAYER_ERROR = "Video Error"

// Label (all analytics are logged labeled with the current video's name: "[video name]",
// except error, where we want to log the error message)
public let GA_LABEL_ERROR_INVALID_ID = "Brightcove iOS Player Error: Could not find video"
public let GA_LABEL_ERROR_INITIALIZATION = "Brightcove iOS Player Error: Initialization Failed"
public let GA_LABEL_ERROR_UNKNOWN = "Brightcove iOS Player Error: Unknown Error"

// Value (all values are logged with current seek time of video, in milliseconds, or video id integer for errors)

// Other
public let GA_OTHER_BILLABLE_THRESHOLD_PERCENT = 0.25
