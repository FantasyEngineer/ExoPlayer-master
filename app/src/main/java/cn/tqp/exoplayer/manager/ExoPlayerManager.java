package cn.tqp.exoplayer.manager;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.tqp.exoplayer.entity.VideoInfo;
import cn.tqp.exoplayer.exoplayerui.ExoPlayerControl;
import cn.tqp.exoplayer.exoplayerui.ExoPlayerLoadControl;
import cn.tqp.exoplayer.exoplayerui.ExoPlayerView;
import cn.tqp.exoplayer.listener.EventLogger;


/**
 * Created by tangqipeng on 2018/1/25.
 */

public class ExoPlayerManager {

    private static final String TAG = "ExoPlayerManager";
    private Context mContext;
    private boolean reSet = false;
    private ExoPlayerView playerView;
    private SimpleExoPlayer exoPlayer;
    private List<VideoInfo> mVideoInfoList;
    private MediaSource[] mediaSources;
    private MediaSource[] previewMediaSources;
    private TrackSelector trackSelector;
    private EventLogger mEventLogger;
    private ExoPlayerLoadControl mLoadControl;
    private DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private Handler mainHandler = new Handler();

    public ExoPlayerManager(Context context, ExoPlayerView playerView) {
        this.mContext = context;
        this.playerView = playerView;
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        mLoadControl = new ExoPlayerLoadControl(context);
//        mEventLogger = new EventLogger((MappingTrackSelector)trackSelector, playerView);
    }

    /**
     * 一次注入多个数据
     *
     * @param videoInfoList
     */
    public void addVideoDatas(List<VideoInfo> videoInfoList, EventLogger eventLogger, int windowIndex, long position) {
        this.mVideoInfoList = videoInfoList;
        this.mEventLogger = eventLogger;
        playerView.setVideoInfoList(videoInfoList, windowIndex);
        mediaSources = new MediaSource[mVideoInfoList.size()];
        previewMediaSources = new MediaSource[mVideoInfoList.size()];
        for (int i = 0; i < mVideoInfoList.size(); i++) {
            mediaSources[i] = addPlayMediaSouce(Uri.parse(mVideoInfoList.get(i).movieUrl));
            previewMediaSources[i] = addPreviewMediaSouce(Uri.parse(mVideoInfoList.get(i).moviePreviewUrl));
        }
        if (previewMediaSources != null && previewMediaSources.length > 0) {
            ConcatenatingMediaSource concatenatedSource = new ConcatenatingMediaSource(previewMediaSources);
            playerView.addPreviewMovieUrl(concatenatedSource);
        }
        ExoPlayerControl.playPosition = position;
        createPlayers();
    }

    /**
     * 预览窗口是以图片的形式
     *
     * @param videoInfoList
     */
    public void addVideoDatasAndPreviewImages(List<VideoInfo> videoInfoList, EventLogger eventLogger, int windowIndex, long position) {
//        File file = new File(Environment.getExternalStorageDirectory() + "/demosys-4k/USB 4K/DOLBY (11).mp4");
        File file = new File(Environment.getExternalStorageDirectory() + "/demosys-4k/USB 4K/DOLBY (13).mp4");
//        Uri uri = FileProvider.getUriForFile(mContext, "cn.tqp.exoplayer", file);

        this.mVideoInfoList = videoInfoList;
        this.mEventLogger = eventLogger;
        playerView.setVideoInfoList(videoInfoList, windowIndex);
        mediaSources = new MediaSource[mVideoInfoList.size()];
        for (int i = 0; i < mVideoInfoList.size(); i++) {
//            mediaSources[i] = addPlayMediaSouce(Uri.parse(mVideoInfoList.get(i).moviePreviewUrl));
            mediaSources[i] = addPlayMediaSouce(Uri.fromFile(file));
        }
        playerView.addPreviewImagesUrl(mVideoInfoList);
        ExoPlayerControl.playPosition = position;
        createPlayers();
    }

    /**
     * 一次加入单个数据
     *
     * @param videoInfo
     */
    public void addVideoData(VideoInfo videoInfo, EventLogger eventLogger, long playPosition) {
        this.mEventLogger = eventLogger;
        mVideoInfoList = new ArrayList<>();
        mVideoInfoList.add(videoInfo);
        playerView.setVideoInfoList(mVideoInfoList, 0);
        mediaSources = new MediaSource[1];
        previewMediaSources = new MediaSource[1];
        mediaSources[0] = addPlayMediaSouce(Uri.parse(videoInfo.movieUrl));
        previewMediaSources[0] = addPlayMediaSouce(Uri.parse(videoInfo.moviePreviewUrl));
        if (previewMediaSources != null && previewMediaSources.length > 0) {
            ConcatenatingMediaSource concatenatedSource = new ConcatenatingMediaSource(previewMediaSources);
            playerView.addPreviewMovieUrl(concatenatedSource);
        }
        ExoPlayerControl.playPosition = playPosition;
        createPlayers();
    }

    public void addVideoDataAndPreviewImage(VideoInfo videoInfo, EventLogger eventLogger, long playPosition) {
        this.mEventLogger = eventLogger;
        mVideoInfoList = new ArrayList<>();
        mVideoInfoList.add(videoInfo);
        playerView.setVideoInfoList(mVideoInfoList, 0);
        mediaSources = new MediaSource[1];
        mediaSources[0] = addPlayMediaSouce(Uri.parse(videoInfo.movieUrl));
        playerView.addPreviewImagesUrl(mVideoInfoList);
        ExoPlayerControl.playPosition = playPosition;
        createPlayers();
    }

    public void onStart() {
        if (Util.SDK_INT > 23 && reSet) {
            reSet = false;
            createPlayers();
        }
    }

    public void onResume() {
        if (Util.SDK_INT <= 23 && reSet) {
            reSet = false;
            createPlayers();
        }
    }

    public void onPause() {
        if (Util.SDK_INT <= 23) {
            releasePlayers();
        }
    }

    public void onStop() {
        if (Util.SDK_INT > 23) {
            releasePlayers();
        }
    }

    private void releasePlayers() {
        if (exoPlayer != null) {
            reSet = true;
            exoPlayer.setPlayWhenReady(false);
            ExoPlayerControl.playPosition = exoPlayer.getCurrentPosition();
            Log.i(TAG, "ExoPlayerControl.playPosition1:" + ExoPlayerControl.playPosition);
            exoPlayer.stop();
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    public void destroy() {
        trackSelector = null;
        ExoPlayerControl.playPosition = 0;
        Log.i(TAG, "ExoPlayerControl.playPosition3:" + ExoPlayerControl.playPosition);
        mLoadControl.onStopped();
        mLoadControl.onReleased();
        mLoadControl = null;
        playerView.release();
    }

    private void createPlayers() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        if (mVideoInfoList != null && mVideoInfoList.size() > 0 && mediaSources != null && mediaSources.length > 0) {
            exoPlayer = createFullPlayer();
            playerView.setPlayer(exoPlayer);
            playerView.setExoPlayerActionListener(mEventLogger);
            playerView.setMovieTitle(mVideoInfoList.get(0).movieTitle);
        }
    }

    private SimpleExoPlayer createFullPlayer() {
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(playerView.getContext()), trackSelector, mLoadControl);
        ConcatenatingMediaSource concatenatedSource = new ConcatenatingMediaSource(mediaSources);
        player.prepare(concatenatedSource);
        player.setPlayWhenReady(true);
        Log.i(TAG, "ExoPlayerControl.playPosition2:" + ExoPlayerControl.playPosition);
        player.seekTo(ExoPlayerControl.playPosition);
        player.addVideoDebugListener(mEventLogger);
        player.addMetadataOutput(mEventLogger);
        player.addListener(mEventLogger);
        return player;
    }

    /**
     * Returns a new MediaSouce in player
     *
     * @param uri
     * @return
     */
    public MediaSource addPlayMediaSouce(Uri uri) {
        return buildMediaSource(uri, true, mEventLogger);
    }

    /**
     * Returns a new MediaSouce in Preview
     *
     * @param uri Play address  of Minimum resolution
     * @return
     */
    public MediaSource addPreviewMediaSouce(Uri uri) {
        return buildMediaSource(uri, false, null);
    }

    public MediaSource buildMediaSource(Uri uri, boolean useBandwidthMeter, EventLogger eventLogger) {
        @C.ContentType
        int type = Util.inferContentType(uri);
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(buildDataSourceFactory(useBandwidthMeter)),
                        buildDataSourceFactory(false)).createMediaSource(uri, mainHandler, eventLogger);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(
                        new DefaultSsChunkSource.Factory(buildDataSourceFactory(useBandwidthMeter)),
                        buildDataSourceFactory(false))
                        .createMediaSource(uri, mainHandler, eventLogger);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(buildDataSourceFactory(useBandwidthMeter))
                        .createMediaSource(uri, mainHandler, eventLogger);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource.Factory(buildDataSourceFactory(useBandwidthMeter))
                        .createMediaSource(uri, mainHandler, eventLogger);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new DataSource factory.
     */
    public DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(mContext, bandwidthMeter, buildHttpDataSourceFactory(bandwidthMeter));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(mContext, "ExoPlayer"), bandwidthMeter);
    }

}
