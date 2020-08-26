EapiBrightCoveAndroidPOC
======================

Provides sample apps for the Brightcove Player SDK and Plugins for Android.

Learn more about the [Brightcove Native Player SDKs](https://support.brightcove.com/native-player-sdks).

The Android sample app projects in this repository can be inserted directly into Android Studio and subsequently executed or simulated.
This version of the sample apps supports the latest Brightcove SDK and plugins.  The following sample apps are included:

## Brightcove ExoPlayer 2 sample apps:

### Important Notes about the ExoPlayer 2 sample apps
1. The ExoPlayer sample apps on the master branch only support Google ExoPlayer 2. If you want to run ExoPlayer 1 versions of the ExoPlayer sample apps, please `git checkout ExoPlayerV1` and run them on that branch.
2. The Brightcove SDK using ExoPlayer v2 supports all features previously supported by ExoPlayer v1, including the following:
   * VOD video formats: Mpeg-DASH, HLS, HLSe, MP4
   * WebVTT Closed Captions
   * Multiple Audio Track videos
   * Client-side advertising with Google IMA and FreeWheel
   * Live video, and Live video with DVR
   * Server-side Ad Insertion
   * DRM with Widevine Modular
   * Offline playback of clear and DRM-protected content
   * Playback in Android TV and Fire TV.
   * Video 360 playback
   * Omniture analytics.

3. Please visit [Migrating Exoplayer 2 framework](https://support.brightcove.com/migrating-exoplayer-2-framework) for more information.

4. What this sample contain:
   a. BrightcoveplayerActivity: This file contains brightcove video player
   b. Config file: This file contanis Brightcove accountId and policy Key
   
   Reference : https://github.com/BrightcoveOS/android-player-samples
5. Google Analytics implementation
    We have GoogleAnalyticsTracker.java where we are initializing the Google Analytics and one method to 
    log the screen name and 3 overridden method to log the events and AnalyticsUtils.java is the 
    utility class where we have listed all the category and action.
    
    a. Get the tracker id by creating the property from Google analytics account for an individual project/app. 
    Please visit https://support.google.com/analytics/answer/7476135?hl=en
    b. After getting the tracker_id we have to initialize the Google Analytics SDK like (Same as we did in GoogleAnalyticsTracker.java):
       GoogleAnalytics analytics = GoogleAnalytics.getInstance(application);
       googleTracker = analytics.newTracker(Config.GOOGLE_ANALYTICS_TRACKER_ID);
    c. We can log event with category, action and label where category and action are compulsory and label is optional and in label we are 
       logging the video name also value param is not in use for our case.  
   Note: Need to initialize GoogleAnalyticsTracker.java from application class
   

