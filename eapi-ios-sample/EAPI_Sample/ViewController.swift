//
//  ViewController.swift
//  EAPI_Sample
//
//  Copyright © 2020 FitnessOnDemand. All rights reserved.
//

import AVKit
import UIKit
import BrightcovePlayerSDK

class ViewController: UIViewController {
    private lazy var playbackService : BCOVPlaybackService = {
        return BCOVPlaybackService(accountId: kViewControllerAccountID, policyKey: kViewControllerPlaybackServicePolicyKey)
    }()
    
    private lazy var playbackController: BCOVPlaybackController? = {
        guard let pc = BCOVPlayerSDKManager.shared()?.createPlaybackController() else {
            return nil
        }
        pc.delegate = self
        pc.isAutoAdvance = true
        pc.isAutoPlay = true
        return pc
    }()
    
    // Track events that have been logged already
    var billablePlayEventLogged = false
    var finishedEventLogged = false
    
    // Track video metadata (if not already available)
    var brightcovePlayhead = 0
    var playbackSession : BCOVPlaybackSession?
    
    @IBOutlet weak var videoContainerView: UIView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Set up our player view and options. Create with a standard VOD layout.
        let options = BCOVPUIPlayerViewOptions()
        options.showPictureInPictureButton = false
        
        guard let playerView = BCOVPUIPlayerView(playbackController: self.playbackController, options: options, controlsView: BCOVPUIBasicControlView.withVODLayout()) else {
            return
        }
        playerView.delegate = self
        
        // Install in the container view and match its size.
        self.videoContainerView.addSubview(playerView)
        playerView.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            playerView.topAnchor.constraint(equalTo: self.videoContainerView.topAnchor),
            playerView.rightAnchor.constraint(equalTo: self.videoContainerView.rightAnchor),
            playerView.leftAnchor.constraint(equalTo: self.videoContainerView.leftAnchor),
            playerView.bottomAnchor.constraint(equalTo: self.videoContainerView.bottomAnchor)
        ])
        
        // Associate the playerView with the playback controller.
        playerView.playbackController = playbackController
        requestContentFromPlaybackService(with: "{your FOD video id}")
        
        // Listen for background event so that Video Play Finished event can be logged
        NotificationCenter.default.addObserver(self, selector: #selector(self.willResignActive), name: UIApplication.willResignActiveNotification, object: nil)
    }
    
    @objc private func willResignActive() {
        // Log the finished event if it hasn't already
        if !finishedEventLogged {
            GAI.fod_trackPlaybackEvent(action: GA_ACTION_VIDEO_PLAYBACK_FINISHED, videoName: playbackSession?.videoName() ?? "", seekTimeInMillis: brightcovePlayhead)
            finishedEventLogged = true
        }
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        NotificationCenter.default.removeObserver(self, name: UIApplication.willResignActiveNotification, object: nil)
        super.viewWillDisappear(animated)
    }
    
    func requestContentFromPlaybackService(with videoId: String) {
        playbackService.findVideo(withReferenceID: videoId, parameters: [:]) { [weak self] (video: BCOVVideo?, jsonResponse: [AnyHashable: Any]?, error: Error?) -> Void in
            guard let strongSelf = self else { return }
            
            if let video = video {
                strongSelf.playbackController?.setVideos([video] as NSArray)
            } else {
                GAI.fod_trackErrorEvent(message: GA_LABEL_ERROR_INVALID_ID, videoId: videoId.videoIdInt())
            }
        }
    }

    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        segue.destination.modalPresentationStyle = .overFullScreen
    }
}

extension ViewController: BCOVPlaybackControllerDelegate, BCOVPUIPlayerViewDelegate {

    func playbackController(_ controller: BCOVPlaybackController!, didAdvanceTo session: BCOVPlaybackSession!) {
        // Capture session for metadata reference throughout playback
        playbackSession = session
    }
    
    func playbackController(_ controller: BCOVPlaybackController!, playbackSession session: BCOVPlaybackSession!, didProgressTo progress: TimeInterval) {
        if progress.isInfinite {
            // progress value sets to inf at the beginning and after the video playback ends.
            return
        }
        
        // Record seek position constantly for any analytics that need to be written; convert to milliseconds
        brightcovePlayhead = Int(progress * 1000)
        
        // Log the Billable Play event upon reaching the threshold (25%) for the first time in a video
        if !billablePlayEventLogged,
            let duration = session.player.currentItem?.duration,
            progress >= GA_OTHER_BILLABLE_THRESHOLD_PERCENT * CMTimeGetSeconds(duration) {
            
            GAI.fod_trackPlaybackEvent(action: GA_ACTION_VIDEO_PLAYBACK_BILLABLE_PLAY, videoName: playbackSession?.videoName() ?? "", seekTimeInMillis: brightcovePlayhead)
            billablePlayEventLogged = true
        }
    }
    
    func playbackController(_ controller: BCOVPlaybackController!, playbackSession session:
     BCOVPlaybackSession!, didReceive lifecycleEvent: BCOVPlaybackSessionLifecycleEvent!) {
        if lifecycleEvent.eventType == kBCOVPlaybackSessionLifecycleEventReady {
            GAI.fod_trackPlaybackEvent(action: GA_ACTION_VIDEO_PLAYBACK_PLAY, videoName: session.videoName(), seekTimeInMillis: brightcovePlayhead)
        }
        if !finishedEventLogged,
            lifecycleEvent.eventType == kBCOVPlaybackSessionLifecycleEventEnd {
            GAI.fod_trackPlaybackEvent(action: GA_ACTION_VIDEO_PLAYBACK_FINISHED, videoName: session.videoName(), seekTimeInMillis: brightcovePlayhead)
            finishedEventLogged = true
        }
        if lifecycleEvent.eventType == kBCOVPlaybackSessionLifecycleEventFail {
            GAI.fod_trackErrorEvent(message: session.video.errorMessage, videoId: session.videoIdInt())
        }
    }
}

fileprivate extension BCOVPlaybackSession {
    func videoName() -> String {
        return self.video.properties["name"] as? String ?? ""
    }
    
    
    // Convert any reference id (including preview) to it's integer form, to make use of the "value" part of google analytics
    func videoIdInt() -> Int {
        if let videoId = self.video.properties["reference_id"] as? String {
            return videoId.videoIdInt()
        }
        return -1
    }
}

fileprivate extension String {
    func videoIdInt() -> Int {
        if self.isEmpty {
            return -1
        }
        
        if self.contains("p") {
            return Int(self.suffix(1)) ?? -1
        }
        return Int(self) ?? -1
    }
}
