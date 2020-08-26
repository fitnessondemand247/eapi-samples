package com.fod.eapi.poc;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.util.Strings;

public class GoogleAnalyticsTracker {

    static private Tracker googleTracker;

    public GoogleAnalyticsTracker(Application application) {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(application);
        googleTracker = analytics.newTracker(Config.GOOGLE_ANALYTICS_TRACKER_ID);
    }

    public static void trackPlaybackEvent(String action, String videoName, int seekTimeMillis) {
        trackEvent(AnalyticsUtils.GA_CATEGORY_VIDEO, action, videoName, seekTimeMillis);
    }

    public static void trackErrorEvent(String errorMessage, int videoId) {
        trackEvent(AnalyticsUtils.GA_CATEGORY_VIDEO, AnalyticsUtils.GA_ACTION_VIDEO_PLAYER_ERROR, errorMessage, videoId);
    }

    public static void trackEvent(String category, String action, String label, long value) {
        HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder();
        eventBuilder.setCategory(category);
        eventBuilder.setAction(action);
        if (Strings.isEmptyOrWhitespace(label)) {
            eventBuilder.setLabel(label);
        }
        if (value != -1) {
            eventBuilder.setValue(value);
        }
        googleTracker.send(eventBuilder.build());
    }
}
