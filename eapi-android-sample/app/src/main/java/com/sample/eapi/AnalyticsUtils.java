package com.sample.eapi;

public class AnalyticsUtils {

    // Category (all analytics logged to video category)
    public static String GA_CATEGORY_VIDEO = "Video";

    // Action
    public static String GA_ACTION_VIDEO_PLAYBACK_BILLABLE_PLAY = "Billable Play";
    public static String GA_ACTION_VIDEO_PLAYBACK_FINISHED = "Video Play Finished";
    public static String GA_ACTION_VIDEO_PLAYBACK_PLAY = "Video Play Started";
    public static String GA_ACTION_VIDEO_PLAYER_ERROR = "Video Error";

    // Label (all analytics are logged labeled with the current video's name: "[video name]",
    // except error, where we want to log the error message)
    public static String GA_LABEL_ERROR_INVALID_ID = "Brightcove Android Player Error: Could not find video";
    public static String GA_LABEL_ERROR_INITIALIZATION = "Brightcove Android Player Error: Initialization Failed";
    public static String GA_LABEL_ERROR_UNKNOWN = "Brightcove Android Player Error: Unknown Error";

    // Value (all values are logged with current seek time of video, in milliseconds, or video id integer for errors)

    // Other
    public static float GA_OTHER_BILLABLE_THRESHOLD_PERCENT = .25f;
}