/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.tqp.exoplayer.listener;

import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.MetadataOutput;
import com.google.android.exoplayer2.metadata.emsg.EventMessage;
import com.google.android.exoplayer2.metadata.id3.ApicFrame;
import com.google.android.exoplayer2.metadata.id3.CommentFrame;
import com.google.android.exoplayer2.metadata.id3.GeobFrame;
import com.google.android.exoplayer2.metadata.id3.Id3Frame;
import com.google.android.exoplayer2.metadata.id3.PrivFrame;
import com.google.android.exoplayer2.metadata.id3.TextInformationFrame;
import com.google.android.exoplayer2.metadata.id3.UrlLinkFrame;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.io.IOException;

import cn.tqp.exoplayer.exoplayerui.ExoPlayerControl;
import cn.tqp.exoplayer.exoplayerui.ExoPlayerView;
import cn.tqp.exoplayer.utils.ExoPlayerUtils;
import cn.tqp.exoplayer.utils.NetworkUtils;


/**
 * Logs player events using {@link Log}.
 */
/* package */
public class EventLogger implements Player.EventListener, MetadataOutput, AudioRendererEventListener,
        VideoRendererEventListener, MediaSourceEventListener, AdsMediaSource.EventListener,
        DefaultDrmSessionManager.EventListener, ExoPlayerListener.PlayerActionListener {

    private static final String TAG = "EventLogger";
    private static final int MAX_TIMELINE_ITEM_LINES = 3;

    private final ExoPlayerView mExoPlayerView;
    private final Timeline.Window window;
    private final Timeline.Period period;
    private final long startTimeMs;
    private long lastTimeMs;
    private long currentTimeMs;
    private boolean isPrepare = true;//预加载
    private boolean isSeek = false;//是否seek
    private boolean isBuffer = false;//是否在预加载
    private boolean isPause = false;//是否暂停
    private long pausePosition = 0;
    private long playCountTime = 0;

    public EventLogger(ExoPlayerView exoPlayerView) {
        this.mExoPlayerView = exoPlayerView;
        window = new Timeline.Window();
        period = new Timeline.Period();
        startTimeMs = SystemClock.elapsedRealtime();
        lastTimeMs = SystemClock.elapsedRealtime();
    }

    // Player.EventListener

    /**
     * 这个是影片缓冲的开始与结束
     *
     * @param isLoading true 开始缓冲下一个阶段的ts片 false 缓冲完成结束
     */
    @Override
    public void onLoadingChanged(boolean isLoading) {
        Log.d(TAG, "loading [" + isLoading + "]");
        if (NetworkUtils.isOnlyMobileType(mExoPlayerView.getContext()) && !ExoPlayerControl.mobileNetPlay && !mExoPlayerView.isCachePath()){
            ExoPlayerControl.isNetError = true;
            ExoPlayerControl.needBuffering = false;
            mExoPlayerView.hideLoadingView();
            mExoPlayerView.getPlayer().setPlayWhenReady(false);
            mExoPlayerView.notifyNetViewIsVisible(true, ExoPlayerControl.MOBILE_NETWORK);
        }
    }

    /**
     * 起播准备，初始化加载数据，出现buffer, 暂停，播放  都会调用这个接口
     *
     * @param playWhenReady true 播放 false 暂停
     * @param state         Player.STATE_IDLE  Player.STATE_READY  Player.STATE_BUFFERING  Player.STATE_ENDED
     */
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int state) {
        Log.d(TAG, "state [" + ExoPlayerUtils.getSessionTimeString(startTimeMs) + ", " + playWhenReady + ", " + ExoPlayerUtils.getStateString(state) + "]");

        currentTimeMs = SystemClock.elapsedRealtime();

        if (playWhenReady && ExoPlayerUtils.getStateString(state).equals("R")) {//准备完成正在播放
            if (isPrepare) {//初始化准备完成进入buffer
                isPrepare = false;
                isPause = false;
                Log.e(TAG, "state [" + ExoPlayerUtils.getStateString(state) + "] after prepare bufferTimeMs [" + (currentTimeMs - lastTimeMs) + "]");
            } else {
                if (isSeek) {//seek后，buffer结束
                    if (isBuffer) {
                        isSeek = false;
                        isBuffer = false;
                        isPause = false;
                        Log.e(TAG, "state [" + ExoPlayerUtils.getStateString(state) + "] after seek bufferTimeMs [" + (currentTimeMs - lastTimeMs) + "]");
                    } else {//拖动用时 seek松开手
                        Log.e(TAG, "state [" + ExoPlayerUtils.getStateString(state) + "] seekTimeMs [" + (currentTimeMs - lastTimeMs) + "] seekEndPosition [" + mExoPlayerView.getPlayer().getCurrentPosition() + "]");
                    }
                }else{
                    if (isPause){
                        isPause = false;
                        ExoPlayerControl.isNetError = false;
                        Log.e(TAG, "state [" + ExoPlayerUtils.getStateString(state) + "] pauseTimeMs [" + (currentTimeMs - lastTimeMs) + "]");
                    }
                }
            }
            lastTimeMs = currentTimeMs;
        } else if (!playWhenReady && ExoPlayerUtils.getStateString(state).equals("R")) {//准备完成正暂停中
            isPause = true;
            pausePosition = mExoPlayerView.getPlayer().getCurrentPosition();
            if (ExoPlayerControl.isNetError){//网络异常导致暂停
                Log.e(TAG, "state [" + ExoPlayerUtils.getStateString(state) + "] NetError playedTimeMs [" + (currentTimeMs - lastTimeMs) + "] pausePositon [" + pausePosition + "]");
            }else{//主动暂停
                Log.e(TAG, "state [" + ExoPlayerUtils.getStateString(state) + "] playedTimeMs [" + (currentTimeMs - lastTimeMs) + "] pausePositon [" + pausePosition + "]");
            }
            playCountTime = playCountTime + (currentTimeMs - lastTimeMs);
            lastTimeMs = currentTimeMs;
        } else if (playWhenReady && ExoPlayerUtils.getStateString(state).equals("B")) {//准备完成正缓冲中
            if (isPrepare) {//初始化准备
                Log.e(TAG, "state [" + ExoPlayerUtils.getStateString(state) + "] prepareTimeMs [" + (currentTimeMs - lastTimeMs) + "]");
                lastTimeMs = currentTimeMs;
            } else {
                if (isSeek) {//seek结束，开始buffer
                    isBuffer = true;
                }
            }
        } else if (playWhenReady && ExoPlayerUtils.getStateString(state).equals("I")){
            if (ExoPlayerControl.isNetError){
                Log.e(TAG, "state [" + ExoPlayerUtils.getStateString(state) + "] NetError playedTimeMs [" + (currentTimeMs - lastTimeMs) + "]");
                lastTimeMs = currentTimeMs;
                mExoPlayerView.getPlayer().setPlayWhenReady(false);
            }
        } else if (playWhenReady && ExoPlayerUtils.getStateString(state).equals("E")){//播放完成当前播放器中所有的地址
            pausePosition = mExoPlayerView.getPlayer().getCurrentPosition();
            Log.e(TAG, "state [" + ExoPlayerUtils.getStateString(state) + "] Completed playedTimeMs [" + (currentTimeMs - lastTimeMs) + "] endPositon [" + mExoPlayerView.getPlayer().getCurrentPosition() + "]");
            playCountTime = playCountTime + (currentTimeMs - lastTimeMs);
            Log.w(TAG, "playCountTime:"+playCountTime);
            lastTimeMs = currentTimeMs;
            //todo 这里传入结束日志
            playCountTime = 0;
        }


    }

    @Override
    public void onRepeatModeChanged(@Player.RepeatMode int repeatMode) {
        Log.d(TAG, "repeatMode [" + ExoPlayerUtils.getRepeatModeString(repeatMode) + "]");
    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
        Log.d(TAG, "shuffleModeEnabled [" + shuffleModeEnabled + "]");
    }

    /**
     * 当注入多个播放地址时  手动切换，或自动（播放完成）切换到下一个地址时调用
     *
     * @param reason
     */
    @Override
    public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason) {
        Log.d(TAG, "positionDiscontinuity [" + ExoPlayerUtils.getDiscontinuityReasonString(reason) + "]");
        if (ExoPlayerUtils.getDiscontinuityReasonString(reason).equals("SEEK")) {//seek开始
            isSeek = true;
            Log.e(TAG, "positionDiscontinuity [" + ExoPlayerUtils.getDiscontinuityReasonString(reason) + "] seekStartPosition [" + pausePosition + "]");
        }else if (ExoPlayerUtils.getDiscontinuityReasonString(reason).equals("PERIOD_TRANSITION")){
            currentTimeMs = SystemClock.elapsedRealtime();
            Log.e(TAG, "positionDiscontinuity [" + ExoPlayerUtils.getDiscontinuityReasonString(reason) + "] Completed playedTimeMs [" + (currentTimeMs - lastTimeMs) + "] pausePositon [" + pausePosition + "]");
            playCountTime = playCountTime + (currentTimeMs - lastTimeMs);
            Log.w(TAG, "playCountTime:"+playCountTime);
            lastTimeMs = currentTimeMs;
            //todo 这里传入结束日志
            playCountTime = 0;
        }
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
        Log.d(TAG, "playbackParameters " + String.format("[speed=%.2f, pitch=%.2f]", playbackParameters.speed, playbackParameters.pitch));
    }

    /**
     * 像播放器注入数据的时候会调用
     *
     * @param timeline
     * @param manifest
     */
    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        int periodCount = timeline.getPeriodCount();
        int windowCount = timeline.getWindowCount();
        Log.d(TAG, "sourceInfo [periodCount=" + periodCount + ", windowCount=" + windowCount);
        for (int i = 0; i < Math.min(periodCount, MAX_TIMELINE_ITEM_LINES); i++) {
            timeline.getPeriod(i, period);
            Log.d(TAG, "  " + "period [" + ExoPlayerUtils.getTimeString(period.getDurationMs()) + "]");
        }
        if (periodCount > MAX_TIMELINE_ITEM_LINES) {
            Log.d(TAG, "  ...");
        }
        for (int i = 0; i < Math.min(windowCount, MAX_TIMELINE_ITEM_LINES); i++) {
            timeline.getWindow(i, window);
            Log.d(TAG, "  " + "window [" + ExoPlayerUtils.getTimeString(window.getDurationMs()) + ", "
                    + window.isSeekable + ", " + window.isDynamic + "]");
        }
        if (windowCount > MAX_TIMELINE_ITEM_LINES) {
            Log.d(TAG, "  ...");
        }
        Log.d(TAG, "]");
    }

    /**
     * 播放失败后
     * @param e
     */
    @Override
    public void onPlayerError(ExoPlaybackException e) {
        Log.e(TAG, "playerFailed [" + ExoPlayerUtils.getSessionTimeString(startTimeMs) + "]", e);
    }

    /**
     * 播放新地址一开始会调用
     *
     * @param ignored
     * @param trackSelections
     */
    @Override
    public void onTracksChanged(TrackGroupArray ignored, TrackSelectionArray trackSelections) {
//        MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
//        if (mappedTrackInfo == null) {
//            Log.d(TAG, "Tracks []");
//            return;
//        }
//        Log.d(TAG, "Tracks [");
//        // Log tracks associated to renderers.
//        for (int rendererIndex = 0; rendererIndex < mappedTrackInfo.length; rendererIndex++) {
//            TrackGroupArray rendererTrackGroups = mappedTrackInfo.getTrackGroups(rendererIndex);
//            TrackSelection trackSelection = trackSelections.get(rendererIndex);
//            if (rendererTrackGroups.length > 0) {
//                Log.d(TAG, "  Renderer:" + rendererIndex + " [");
//                for (int groupIndex = 0; groupIndex < rendererTrackGroups.length; groupIndex++) {
//                    TrackGroup trackGroup = rendererTrackGroups.get(groupIndex);
//                    String adaptiveSupport = ExoPlayerUtils.getAdaptiveSupportString(trackGroup.length,
//                            mappedTrackInfo.getAdaptiveSupport(rendererIndex, groupIndex, false));
//                    Log.d(TAG, "    Group:" + groupIndex + ", adaptive_supported=" + adaptiveSupport + " [");
//                    for (int trackIndex = 0; trackIndex < trackGroup.length; trackIndex++) {
//                        String status = ExoPlayerUtils.getTrackStatusString(trackSelection, trackGroup, trackIndex);
//                        String formatSupport = ExoPlayerUtils.getFormatSupportString(
//                                mappedTrackInfo.getTrackFormatSupport(rendererIndex, groupIndex, trackIndex));
//                        Log.d(TAG, "      " + status + " Track:" + trackIndex + ", "
//                                + Format.toLogString(trackGroup.getFormat(trackIndex))
//                                + ", supported=" + formatSupport);
//                    }
//                    Log.d(TAG, "    ]");
//                }
//                // Log metadata for at most one of the tracks selected for the renderer.
//                if (trackSelection != null) {
//                    for (int selectionIndex = 0; selectionIndex < trackSelection.length(); selectionIndex++) {
//                        Metadata metadata = trackSelection.getFormat(selectionIndex).metadata;
//                        if (metadata != null) {
//                            Log.d(TAG, "    Metadata [");
//                            printMetadata(metadata, "      ");
//                            Log.d(TAG, "    ]");
//                            break;
//                        }
//                    }
//                }
//                Log.d(TAG, "  ]");
//            }
//        }
//        // Log tracks not associated with a renderer.
//        TrackGroupArray unassociatedTrackGroups = mappedTrackInfo.getUnassociatedTrackGroups();
//        if (unassociatedTrackGroups.length > 0) {
//            Log.d(TAG, "  Renderer:None [");
//            for (int groupIndex = 0; groupIndex < unassociatedTrackGroups.length; groupIndex++) {
//                Log.d(TAG, "    Group:" + groupIndex + " [");
//                TrackGroup trackGroup = unassociatedTrackGroups.get(groupIndex);
//                for (int trackIndex = 0; trackIndex < trackGroup.length; trackIndex++) {
//                    String status = ExoPlayerUtils.getTrackStatusString(false);
//                    String formatSupport = ExoPlayerUtils.getFormatSupportString(
//                            RendererCapabilities.FORMAT_UNSUPPORTED_TYPE);
//                    Log.d(TAG, "      " + status + " Track:" + trackIndex + ", "
//                            + Format.toLogString(trackGroup.getFormat(trackIndex))
//                            + ", supported=" + formatSupport);
//                }
//                Log.d(TAG, "    ]");
//            }
//            Log.d(TAG, "  ]");
//        }
//        Log.d(TAG, "]");
    }

    /**
     * seek后调用
     */
    @Override
    public void onSeekProcessed() {
        Log.d(TAG, "seekProcessed");
    }

    // MetadataOutput

    @Override
    public void onMetadata(Metadata metadata) {
        Log.d(TAG, "onMetadata [");
        printMetadata(metadata, "  ");
        Log.d(TAG, "]");
    }

    // AudioRendererEventListener

    @Override
    public void onAudioEnabled(DecoderCounters counters) {
        Log.d(TAG, "audioEnabled [" + ExoPlayerUtils.getSessionTimeString(startTimeMs) + "]");
    }

    @Override
    public void onAudioSessionId(int audioSessionId) {
        Log.d(TAG, "audioSessionId [" + audioSessionId + "]");
    }

    @Override
    public void onAudioDecoderInitialized(String decoderName, long elapsedRealtimeMs, long initializationDurationMs) {
        Log.d(TAG, "audioDecoderInitialized [" + ExoPlayerUtils.getSessionTimeString(startTimeMs) + ", " + decoderName + "]");
    }

    @Override
    public void onAudioInputFormatChanged(Format format) {
        Log.d(TAG, "audioFormatChanged [" + ExoPlayerUtils.getSessionTimeString(startTimeMs) + ", " + Format.toLogString(format) + "]");
    }

    @Override
    public void onAudioDisabled(DecoderCounters counters) {
        Log.d(TAG, "audioDisabled [" + ExoPlayerUtils.getSessionTimeString(startTimeMs) + "]");
    }

    @Override
    public void onAudioSinkUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {
        printInternalError("audioTrackUnderrun [" + bufferSize + ", " + bufferSizeMs + ", " + elapsedSinceLastFeedMs + "]", null);
    }

    // VideoRendererEventListener

    @Override
    public void onVideoEnabled(DecoderCounters counters) {
        Log.d(TAG, "videoEnabled [" + ExoPlayerUtils.getSessionTimeString(startTimeMs) + "]");
    }

    @Override
    public void onVideoDecoderInitialized(String decoderName, long elapsedRealtimeMs, long initializationDurationMs) {
//        ExoPlayerControl.isPrepared = true;
        Log.d(TAG, "videoDecoderInitialized [" + ExoPlayerUtils.getSessionTimeString(startTimeMs) + ", " + decoderName + "]");
    }

    @Override
    public void onVideoInputFormatChanged(Format format) {
        Log.d(TAG, "videoFormatChanged [" + ExoPlayerUtils.getSessionTimeString(startTimeMs) + ", " + Format.toLogString(format) + "]");
    }

    @Override
    public void onVideoDisabled(DecoderCounters counters) {
        Log.d(TAG, "videoDisabled [" + ExoPlayerUtils.getSessionTimeString(startTimeMs) + "]");
    }

    @Override
    public void onDroppedFrames(int count, long elapsed) {
        Log.d(TAG, "droppedFrames [" + ExoPlayerUtils.getSessionTimeString(startTimeMs) + ", " + count + "]");
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        Log.d(TAG, "videoSizeChanged [" + width + ", " + height + "]");
    }

    @Override
    public void onRenderedFirstFrame(Surface surface) {
        Log.d(TAG, "renderedFirstFrame [" + surface + "]");
    }

    // DefaultDrmSessionManager.EventListener

    @Override
    public void onDrmSessionManagerError(Exception e) {
        printInternalError("drmSessionManagerError", e);
    }

    @Override
    public void onDrmKeysRestored() {
        Log.d(TAG, "drmKeysRestored [" + ExoPlayerUtils.getSessionTimeString(startTimeMs) + "]");
    }

    @Override
    public void onDrmKeysRemoved() {
        Log.d(TAG, "drmKeysRemoved [" + ExoPlayerUtils.getSessionTimeString(startTimeMs) + "]");
    }

    @Override
    public void onDrmKeysLoaded() {
        Log.d(TAG, "drmKeysLoaded [" + ExoPlayerUtils.getSessionTimeString(startTimeMs) + "]");
    }

    // MediaSourceEventListener

    @Override
    public void onLoadStarted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs) {
        Log.d(TAG, "onLoadStarted dataSpec [" + dataSpec.uri + "] dataType [" + dataType + "] trackType [" + trackType + "] trackFormat [" + trackFormat + "] trackSelectionReason [" +
                trackSelectionReason + "] trackSelectionData [" + trackSelectionData + "] mediaStartTimeMs [" + mediaStartTimeMs + "] mediaEndTimeMs [" + mediaEndTimeMs + "] elapsedRealtimeMs [" + elapsedRealtimeMs + "]");
    }

    @Override
    public void onLoadError(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded, IOException error, boolean wasCanceled) {
        if (NetworkUtils.isOnlyMobileType(mExoPlayerView.getContext()) || !NetworkUtils.isNetworkAvalidate(mExoPlayerView.getContext())) {
            ExoPlayerControl.isNetError = true;
            ExoPlayerControl.needBuffering = false;
            mExoPlayerView.getPlayer().setPlayWhenReady(false);

            if (NetworkUtils.isOnlyMobileType(mExoPlayerView.getContext())){
                mExoPlayerView.notifyNetViewIsVisible(true, ExoPlayerControl.MOBILE_NETWORK);
            }

            if (!NetworkUtils.isNetworkAvalidate(mExoPlayerView.getContext())){
                mExoPlayerView.notifyNetViewIsVisible(true, ExoPlayerControl.NO_NETWORK);
            }
        }
        printInternalError("loadError", error);
        Log.d(TAG, "onLoadError dataSpec [" + dataSpec.uri + "] dataType [" + dataType + "] trackType [" + trackType + "] trackFormat [" + trackFormat + "] trackSelectionReason [" + trackSelectionReason + "] trackSelectionData [" + trackSelectionData +
                "] mediaStartTimeMs [" + mediaStartTimeMs + "] mediaEndTimeMs [" + mediaEndTimeMs + "] elapsedRealtimeMs [" + elapsedRealtimeMs + "] loadDurationMs [" + loadDurationMs + "] bytesLoaded [" + bytesLoaded + "] error [" + error.getMessage() + "] wasCanceled [" + wasCanceled + "]");
    }

    @Override
    public void onLoadCanceled(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {
        Log.d(TAG, "onLoadCanceled dataSpec [" + (null == dataSpec.uri? "0" : dataSpec.uri) + "] dataType [" + dataType + "] trackType [" + trackType + "] trackFormat [" + trackFormat + "] trackSelectionReason [" + trackSelectionReason + "] trackSelectionData [" + trackSelectionData +
                "] mediaStartTimeMs [" + mediaStartTimeMs + "] mediaEndTimeMs [" + mediaEndTimeMs + "] elapsedRealtimeMs [" + elapsedRealtimeMs + "] loadDurationMs [" + loadDurationMs + "] bytesLoaded [" + bytesLoaded + "]");
    }

    @Override
    public void onLoadCompleted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {
        if (ExoPlayerControl.isNetError && NetworkUtils.isNetworkConnectedByWifi(mExoPlayerView.getContext())) {
            ExoPlayerControl.isNetError = false;
            mExoPlayerView.getPlayer().setPlayWhenReady(true);
            mExoPlayerView.notifyNetViewIsVisible(false, ExoPlayerControl.WIFI_NETWORK);
        }
        Log.d(TAG, "onLoadCompleted dataSpec [" + dataSpec.uri + "] dataType [" + dataType + "] trackType [" + trackType + "] trackFormat [" + trackFormat + "] trackSelectionReason [" + trackSelectionReason + "] trackSelectionData [" + trackSelectionData +
                "] mediaStartTimeMs [" + mediaStartTimeMs + "] mediaEndTimeMs [" + mediaEndTimeMs + "] elapsedRealtimeMs [" + elapsedRealtimeMs + "] loadDurationMs [" + loadDurationMs + "] bytesLoaded [" + bytesLoaded + "]");
    }

    @Override
    public void onUpstreamDiscarded(int trackType, long mediaStartTimeMs, long mediaEndTimeMs) {
        Log.e(TAG, "onUpstreamDiscarded trackType [" + trackType + "] mediaStartTimeMs [" + mediaStartTimeMs + "] mediaEndTimeMs [" + mediaEndTimeMs + "]");
    }

    @Override
    public void onDownstreamFormatChanged(int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaTimeMs) {
        Log.e(TAG, "onDownstreamFormatChanged [" + isPrepare + "]");
        Log.d(TAG, "onDownstreamFormatChanged trackType [" + trackType + "] trackFormat [" + trackFormat + "] trackSelectionReason [" + trackSelectionReason + "] trackSelectionData [" + trackSelectionData + "] mediaTimeMs [" + mediaTimeMs + "]");
    }

    // AdsMediaSource.EventListener

    @Override
    public void onAdLoadError(IOException error) {
        printInternalError("adLoadError", error);
    }

    @Override
    public void onAdClicked() {
        // Do nothing.
    }

    @Override
    public void onAdTapped() {
        // Do nothing.
    }

    // PlayerActionListener methods

    @Override
    public void pauseTap() {

    }

    @Override
    public void playTap() {

    }


    // Internal methods

    private void printInternalError(String type, Exception e) {
        Log.e(TAG, "internalError [" + ExoPlayerUtils.getSessionTimeString(startTimeMs) + ", " + type + "]", e);
    }

    private void printMetadata(Metadata metadata, String prefix) {
        for (int i = 0; i < metadata.length(); i++) {
            Metadata.Entry entry = metadata.get(i);
            if (entry instanceof TextInformationFrame) {
                TextInformationFrame textInformationFrame = (TextInformationFrame) entry;
                Log.d(TAG, prefix + String.format("%s: value=%s", textInformationFrame.id,
                        textInformationFrame.value));
            } else if (entry instanceof UrlLinkFrame) {
                UrlLinkFrame urlLinkFrame = (UrlLinkFrame) entry;
                Log.d(TAG, prefix + String.format("%s: url=%s", urlLinkFrame.id, urlLinkFrame.url));
            } else if (entry instanceof PrivFrame) {
                PrivFrame privFrame = (PrivFrame) entry;
                Log.d(TAG, prefix + String.format("%s: owner=%s", privFrame.id, privFrame.owner));
            } else if (entry instanceof GeobFrame) {
                GeobFrame geobFrame = (GeobFrame) entry;
                Log.d(TAG, prefix + String.format("%s: mimeType=%s, filename=%s, description=%s",
                        geobFrame.id, geobFrame.mimeType, geobFrame.filename, geobFrame.description));
            } else if (entry instanceof ApicFrame) {
                ApicFrame apicFrame = (ApicFrame) entry;
                Log.d(TAG, prefix + String.format("%s: mimeType=%s, description=%s",
                        apicFrame.id, apicFrame.mimeType, apicFrame.description));
            } else if (entry instanceof CommentFrame) {
                CommentFrame commentFrame = (CommentFrame) entry;
                Log.d(TAG, prefix + String.format("%s: language=%s, description=%s", commentFrame.id,
                        commentFrame.language, commentFrame.description));
            } else if (entry instanceof Id3Frame) {
                Id3Frame id3Frame = (Id3Frame) entry;
                Log.d(TAG, prefix + String.format("%s", id3Frame.id));
            } else if (entry instanceof EventMessage) {
                EventMessage eventMessage = (EventMessage) entry;
                Log.d(TAG, prefix + String.format("EMSG: scheme=%s, id=%d, value=%s",
                        eventMessage.schemeIdUri, eventMessage.id, eventMessage.value));
            }
        }
    }

}
