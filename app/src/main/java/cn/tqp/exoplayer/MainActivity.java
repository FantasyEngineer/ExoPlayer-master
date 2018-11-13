package cn.tqp.exoplayer;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.ui.TimeBar;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import java.lang.reflect.Method;

import cn.tqp.exoplayer.manager.ExoPlayerScreenOrientation;
import cn.tqp.exoplayer.utils.AppUtil;


public class MainActivity extends AppCompatActivity implements PlaybackControlView.VisibilityListener, TimeBar.OnScrubListener {

    private ConstraintLayout constraintLayout;
    private SimpleExoPlayerView simpleExoPlayerView;
    private SimpleExoPlayerView simplePreExoPlayerView;
    private FrameLayout previewFrameLayout;
    private LinearLayout mLlPlayerTop, mLlplayerBottom;
    private DefaultTimeBar timeBar;
    private TextView txt_back;
    private ImageButton exo_next, fs;
    private SimpleExoPlayer simpleExoPlayer;
    private SimpleExoPlayer simplePreExoPlayer;
    private DefaultTrackSelector trackSelector, perTrackSelector;
//    private EventLogger eventLogger;

    private int playerWidth, playerHeight;

//    private DataSource.Factory mediaDataSourceFactory;
    private DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        //常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        constraintLayout = (ConstraintLayout) findViewById(R.id.constrainlayout);

        Log.i("FFFF", "getOrientationConfig:"+ ExoPlayerScreenOrientation.getOrientationConfig(this));

        /**
         * Create Simple Exoplayer Player
         */
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
//        eventLogger = new EventLogger(trackSelector);

        simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.exoplayerview);
        simpleExoPlayerView.setControllerVisibilityListener(this);
        simpleExoPlayerView.requestFocus();

        playerWidth = getScreenWidth(this);

        playerHeight = playerWidth * 9 / 16;

        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, playerHeight);

        simpleExoPlayerView.setLayoutParams(layoutParams);

//        simpleExoPlayerView.getOverlayFrameLayout().addView();

        simpleExoPlayerView.setPlayer(simpleExoPlayer);

//        simpleExoPlayerView.setShowShuffleButton(true);

        //--------------------------------------------------------

        TrackSelection.Factory selection = new AdaptiveTrackSelection.Factory(null);
        perTrackSelector = new DefaultTrackSelector(selection);
        LoadControl loadControl = new DefaultLoadControl();
        simplePreExoPlayer = ExoPlayerFactory.newSimpleInstance(this, perTrackSelector, loadControl);

        simplePreExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.previewPlayerView);

        simplePreExoPlayerView.setPlayer(simplePreExoPlayer);

        //----------------------------------------------------------

        /**
         * Create RTMP Data Source
         */
        RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory();
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
//        MediaSource firstSource = new ExtractorMediaSource(Uri.parse("rtmp://184.72.239.149/vod/mp4:bigbuckbunny_750.mp4"), rtmpDataSourceFactory, extractorsFactory, null, null);
        MediaSource firstSource = new ExtractorMediaSource(Uri.parse("rtmp://10.1.1.119/yqh/mylive"), rtmpDataSourceFactory, extractorsFactory, null, null);

        MediaSource secondSource1 = buildMediaSource(Uri.parse("http://cdn.ali.vcinema.com.cn/201709/xtMHgEOw/xOmtuUUGLj.m3u8"), null, true, null, null);

        MediaSource secondSource2 = buildMediaSource(Uri.parse(getResources().getString(R.string.url_hls1)), null, true, null, null);

        MediaSource secondSource6 = buildMediaSource(Uri.parse("http://cdn.ksyun.vcinema.com.cn/newEncode201707/21bbddba5a72909783c409beb4135664/480pmedia-3/stream.m3u8"), null, true, null, null);

        LoopingMediaSource loopingMediaSource = new LoopingMediaSource(secondSource1, 20);

        ConcatenatingMediaSource concatenatedSource = new ConcatenatingMediaSource(secondSource1);

        simpleExoPlayer.prepare(concatenatedSource);

//        simpleExoPlayer.setShuffleModeEnabled(true);

        simpleExoPlayer.setPlayWhenReady(true);

//        simpleExoPlayer.addListener(eventLogger);


        //---------------------------------------------------

        MediaSource secondSource3 = buildMediaSource(Uri.parse("http://cdn.ali.vcinema.com.cn/201709/xtMHgEOw/xOmtuUUGLj.m3u8"), null, false, null, null);

        MediaSource secondSource4 = buildMediaSource(Uri.parse(getResources().getString(R.string.url_hls1)), null, false, null, null);

        MediaSource secondSource5 = buildMediaSource(Uri.parse("http://cdn.ksyun.vcinema.com.cn/newEncode201707/21bbddba5a72909783c409beb4135664/480pmedia-3/stream.m3u8"), null, false, null, null);

        LoopingMediaSource loopingMediaSource1 = new LoopingMediaSource(secondSource3, 20);

        ConcatenatingMediaSource concatenatedSource2 = new ConcatenatingMediaSource(secondSource3);

        simplePreExoPlayer.prepare(concatenatedSource2);

        simplePreExoPlayer.setVolume(0f);

        simplePreExoPlayer.setPlayWhenReady(false);

        View view = simplePreExoPlayerView.getVideoSurfaceView();

        if (view instanceof SurfaceView) {
            SurfaceView surfaceView = (SurfaceView) view;
            surfaceView.setZOrderMediaOverlay(true);
            surfaceView.setZOrderOnTop(true);
            surfaceView.setVisibility(View.INVISIBLE);
        }

        //----------------------------------------------

        txt_back = (TextView) findViewById(R.id.txt_back);
        mLlPlayerTop = (LinearLayout) findViewById(R.id.ll_palyer_top);
        mLlplayerBottom = (LinearLayout) findViewById(R.id.ll_palyer_bottom);

        exo_next = (ImageButton) simpleExoPlayerView.findViewById(R.id.exo_next);

        fs = (ImageButton) simpleExoPlayerView.findViewById(R.id.exo_resize_screen);

        previewFrameLayout = (FrameLayout) simpleExoPlayerView.findViewById(R.id.previewFrameLayout);

        previewFrameLayout.setVisibility(View.INVISIBLE);

        timeBar = (DefaultTimeBar) simpleExoPlayerView.findViewById(R.id.exo_progress);

        txt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {//横屏
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    fs.setBackgroundResource(R.drawable.full);
                }else{
                    finish();
                }
            }
        });

        mLlPlayerTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        mLlplayerBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        fs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {//横屏
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                    fs.setBackgroundResource(R.drawable.full);
                } else if (MainActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {//竖屏
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                    fs.setBackgroundResource(R.drawable.lessen);
                }
            }
        });

        exo_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                simpleExoPlayer.seekTo(simpleExoPlayer.getNextWindowIndex(), 0);
                simplePreExoPlayer.seekTo(simplePreExoPlayer.getNextWindowIndex(), 0);
            }
        });

        timeBar.addListener(this);

    }

    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new DataSource factory.
     */
    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(this, bandwidthMeter, buildHttpDataSourceFactory(bandwidthMeter));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(this, "ExoPlayer"), bandwidthMeter);
    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension, boolean useBandwidthMeter, @Nullable Handler handler, @Nullable MediaSourceEventListener listener) {
        @C.ContentType
        int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri) : Util.inferContentType("." + overrideExtension);
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(buildDataSourceFactory(useBandwidthMeter)), buildDataSourceFactory(false)).createMediaSource(uri, handler, listener);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(
                        new DefaultSsChunkSource.Factory(buildDataSourceFactory(useBandwidthMeter)),
                        buildDataSourceFactory(false))
                        .createMediaSource(uri, handler, listener);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(buildDataSourceFactory(useBandwidthMeter))
                        .createMediaSource(uri, handler, listener);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource.Factory(buildDataSourceFactory(useBandwidthMeter))
                        .createMediaSource(uri, handler, listener);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public void onVisibilityChange(int visibility) {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {//横屏

            Log.e("KKKK", "22222");

            Log.i("HHHH", "ORIENTATION_LANDSCAPE");

            AppUtil.hideActionBarAndBottomUiMenu(this);

            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    if (android.os.Build.VERSION.SDK_INT > 18) {
                        constraintLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.INVISIBLE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                    } else {
                        constraintLayout.setSystemUiVisibility(View.INVISIBLE);
                    }
                }
            });

            //skin的宽高
            scaleLayout(simpleExoPlayerView, 0, 0);

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {//竖屏
            AppUtil.showActionBarAndBottomUiMenu(this);
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    constraintLayout.setSystemUiVisibility(View.VISIBLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                }
            });


            //skin的宽高
            if (playerWidth != 0 && playerHeight != 0) {
                scaleLayout(simpleExoPlayerView, playerWidth, playerHeight);
            }

            Log.i("HHHH", "ORIENTATION_PORTRAIT");
        }
    }

    //设置videoFrame的大小
    private void scaleLayout(View view, int width, int height) {
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        if (width == 0) {
//            width=outMetrics.widthPixels;
            width = getDpi(this);
        }
        if (height == 0) {
            height = outMetrics.heightPixels;
        }
        Log.i("HHHH", "width:" + width + "  height:" + height + "  getScreenHeight:" + getScreenHeight(this));
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        if (params == null) {
            params = new ConstraintLayout.LayoutParams(width, height);
        } else {
            params.height = height;
            params.width = width;
        }

        view.setLayoutParams(params);
    }

    /**
     * 隐藏虚拟按键，并且全屏
     */
    protected void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    //获取屏幕原始尺寸高度，包括虚拟功能键高度
    public static int getDpi(Context context) {
        int dpi = 0;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        @SuppressWarnings("rawtypes")
        Class c;
        try {
            c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, displayMetrics);
//            dpi=displayMetrics.heightPixels;
            dpi = displayMetrics.widthPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dpi;
    }

    /**
     * 获取 虚拟按键的高度
     *
     * @param context
     * @return
     */
    public static int getBottomStatusHeight(Context context) {
        int totalHeight = getDpi(context);

        int contentHeight = getScreenHeight(context);

        return totalHeight - contentHeight;
    }

    /**
     * 获得状态栏的高度
     *
     * @param context
     * @return
     */
    public static int getStatusHeight(Context context) {

        int statusHeight = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

    /**
     * 获得屏幕高度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    @Override
    protected void onStop() {
        super.onStop();
        simpleExoPlayer.stop();
        simplePreExoPlayer.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        simpleExoPlayer.release();
        simpleExoPlayer = null;
        trackSelector = null;
        simplePreExoPlayer.release();
        simplePreExoPlayer = null;
        perTrackSelector = null;
    }

    @Override
    public void onScrubStart(TimeBar timeBar, long position) {
        previewFrameLayout.setVisibility(View.VISIBLE);
        simpleExoPlayer.setPlayWhenReady(false);
    }

    @Override
    public void onScrubMove(TimeBar timeBar, long position) {

        Log.i("DDDD", "position:"+position);

        simplePreExoPlayer.seekTo((long) position);
        simplePreExoPlayer.setPlayWhenReady(false);
        View view = simplePreExoPlayerView.getVideoSurfaceView();
        if (view instanceof SurfaceView) {
            view.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
        simpleExoPlayer.setPlayWhenReady(true);
        View view = simplePreExoPlayerView.getVideoSurfaceView();
        if (view instanceof SurfaceView) {
            view.setVisibility(View.INVISIBLE);
        }
        simplePreExoPlayer.setPlayWhenReady(false);
        previewFrameLayout.setVisibility(View.INVISIBLE);
    }

}
