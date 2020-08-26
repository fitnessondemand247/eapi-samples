package com.sample.eapi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.brightcove.player.edge.Catalog;
import com.brightcove.player.edge.CatalogError;
import com.brightcove.player.edge.VideoListener;
import com.brightcove.player.event.EventEmitter;
import com.brightcove.player.event.EventType;
import com.brightcove.player.mediacontroller.ThumbnailComponent;
import com.brightcove.player.model.DeliveryType;
import com.brightcove.player.model.Video;
import com.brightcove.player.view.BaseVideoView;

import com.google.android.gms.common.util.Strings;

import java.util.List;
import java.util.Locale;

public class BrightcovePlayerActivity extends AppCompatActivity {
    private String TAG = BrightcovePlayerActivity.class.getName();
    private BaseVideoView baseVideoView;
    private EventEmitter eventEmitter;
    private ProgressBar loadingVideo;
    private String videoId = "";

    // Tracking of billable analytic
    private boolean billablePlay = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brightcove_player);
        videoId = "yourVideoId";
        baseVideoView = findViewById(R.id.brightcove_video_view);
        loadingVideo = findViewById(R.id.loading_video);
        playVideo();
    }

    protected void playVideo() {
        if (baseVideoView != null) {
            eventEmitter = baseVideoView.getEventEmitter();
            Catalog catalog = new Catalog.Builder(eventEmitter, Config.BRIGHTCOVE_ACCOUNT_ID)
                    .setPolicy(Config.BRIGHTCOVE_PLAYER_POLICY_KEY)
                    .build();
            try {
                catalog.findVideoByReferenceID(videoId, new VideoListener() {

                    @Override
                    public void onVideo(Video video) {
                        if (baseVideoView != null) {
                            if (loadingVideo != null) {
                                loadingVideo.setVisibility(View.GONE);
                            }

                            // Specify HLS as preferred delivery type
                            // Note: chromecast cannot use HLS; edit this as needed for such instances
                            sourceSelectionFilter(DeliveryType.HLS, video);

                            // Add the video found to the queue with add().
                            // Start playback of the video with start().
                            baseVideoView.add(video);
                            baseVideoView.start();

                            // Initiate full screen immediately
                            eventEmitter.emit(EventType.ENTER_FULL_SCREEN);

                            // Configure thumbnail scrubbing or trickplay
                            configureThumbnailScrubber();

                            // Log Analytics for Play
                            GoogleAnalyticsTracker.trackPlaybackEvent(AnalyticsUtils.GA_ACTION_VIDEO_PLAYBACK_PLAY, getVideoName(), getSeekTime());
                        } else {
                            Log.w(TAG, "Unable to start playback: baseVideoView was null");
                            GoogleAnalyticsTracker.trackErrorEvent(AnalyticsUtils.GA_LABEL_ERROR_INITIALIZATION, getReferenceIdInt());
                        }
                    }

                    @Override
                    public void onError(@NonNull List<CatalogError> errors) {
                        super.onError(errors);

                        // Parse errors and write each to log and analytics
                        onCatalogError(errors);
                    }

                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to set Brightcove videoId, likely caused by null brightcoveReferenceId");
                GoogleAnalyticsTracker.trackErrorEvent(AnalyticsUtils.GA_LABEL_ERROR_INVALID_ID, getReferenceIdInt());
            }

            eventEmitter.on(EventType.BUFFERING_STARTED, event -> {
                if (loadingVideo != null) {
                    loadingVideo.setVisibility(View.VISIBLE);
                }
            });
            eventEmitter.on(EventType.BUFFERING_COMPLETED, event -> {
                if (loadingVideo != null) {
                    loadingVideo.setVisibility(View.GONE);
                }
            });

            eventEmitter.on(EventType.PROGRESS, event -> {
                if (!billablePlay && getPercentViewed() > AnalyticsUtils.GA_OTHER_BILLABLE_THRESHOLD_PERCENT) {
                    GoogleAnalyticsTracker.trackPlaybackEvent(AnalyticsUtils.GA_ACTION_VIDEO_PLAYBACK_BILLABLE_PLAY, getVideoName(), getSeekTime());
                    billablePlay = true;
                }
            });
        } else {
            Log.w(TAG, "Failed to set Brightcove video player, Likely caused by null baseVideoView, brightcoveReferenceId");
            GoogleAnalyticsTracker.trackErrorEvent(AnalyticsUtils.GA_LABEL_ERROR_INITIALIZATION, getReferenceIdInt());
        }
    }

    /**
     * Method to set default deliver type
     *
     * @param deliveryTypeFilter filters deliveryTypes down to only the one provided
     * @param video the video currently being displayed and/or played
     */
    public void sourceSelectionFilter(@NonNull DeliveryType deliveryTypeFilter, @NonNull Video video) {
        if (video.getSourceCollections().containsKey(deliveryTypeFilter)) {
            // Only remove the other delivery types if the desired type is in our Source collections
            for (DeliveryType deliveryType : DeliveryType.values()) {
                if (!deliveryType.equals(deliveryTypeFilter)) {
                    video.getSourceCollections().remove(deliveryType);
                }
            }
        }
    }

    // Parse catalog errors which are buried so they can be relayed
    private void onCatalogError(List<CatalogError> catalogErrorList) {
        if (catalogErrorList != null && !catalogErrorList.isEmpty()) {
            for (CatalogError catalogError : catalogErrorList) {
                String errMsg = catalogError.getMessage().isEmpty() ? catalogError.getCatalogErrorCode() : catalogError.getMessage();
                String msg = String.format(Locale.getDefault(), "Brightcove Player Error: video: %s, %s, %s", videoId, catalogError.getErrorCode(), errMsg);
                Log.e(TAG, msg);
                GoogleAnalyticsTracker.trackErrorEvent(msg, getReferenceIdInt());
            }
        } else {
            String msg = String.format(Locale.getDefault(), "Unknown Brightcove player error");
            Log.e(TAG, msg);
            GoogleAnalyticsTracker.trackErrorEvent(AnalyticsUtils.GA_LABEL_ERROR_UNKNOWN, getReferenceIdInt());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (baseVideoView != null) {
            baseVideoView.pause();
        } else {
            Log.w(TAG, "Unable to pause video: baseVideoView was null");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (baseVideoView != null) {
            // Log all playback via destroy to hopefully not miss any that might stop for unexpected reasons.
            // Tracking the COMPLETED and DID_STOP events on the emitter can also provide similar touchpoints.
            GoogleAnalyticsTracker.trackPlaybackEvent(AnalyticsUtils.GA_ACTION_VIDEO_PLAYBACK_FINISHED, getVideoName(), getSeekTime());

            // Tear down player as much as possible
            baseVideoView.removeAllViews();
            baseVideoView.removeListeners();
            baseVideoView.removeAllViewsInLayout();
            baseVideoView = null;
        }
    }

    //Thumbnail Scrubber while seeking up/down the video duration bar
    private void configureThumbnailScrubber() {
        try {
            ThumbnailComponent thumbnailComponent = new ThumbnailComponent(baseVideoView);
            thumbnailComponent.setupPreviewThumbnailController();
        } catch (Exception e) {
            Log.e(TAG, "Failed to set Thumbnail Scrubber");
        }
    }

    // Google Analytics Helper Functions

    private String getVideoName() {
        if (baseVideoView != null && baseVideoView.getCurrentVideo() != null) {
            return baseVideoView.getCurrentVideo().getName();
        }
        return "Unknown Video";
    }

    private int getSeekTime() {
        return baseVideoView != null ? baseVideoView.getCurrentPosition() : 0;
    }

    private float getPercentViewed() {
        if (baseVideoView != null) {
            return (float)getSeekTime() / (float)baseVideoView.getDuration();
        }
        return 0.0f;
    }

    // Convert any reference id (including preview) to it's integer form, to make use of the "value" part of google analytics
    private int getReferenceIdInt() {
        if (Strings.isEmptyOrWhitespace(videoId)) {
            return -1;
        }

        try {
            // Account for previews which start with "p"
            if (videoId.contains("p")) {
                return Integer.parseInt(videoId.substring(1));
            }
            return Integer.parseInt(videoId);
        } catch (NumberFormatException nfe) {
            Log.e(TAG, "Could not parse " + nfe);
            return -1;
        }
    }
}
