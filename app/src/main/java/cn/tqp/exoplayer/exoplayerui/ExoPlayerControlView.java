package cn.tqp.exoplayer.exoplayerui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.target.Target;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ControlDispatcher;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.ExoPlayerLibraryInfo;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.ui.TimeBar;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.RepeatModeUtil;
import com.google.android.exoplayer2.util.Util;

import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import cn.tqp.exoplayer.R;
import cn.tqp.exoplayer.entity.PreviewImage;
import cn.tqp.exoplayer.entity.VideoInfo;
import cn.tqp.exoplayer.glide.GlideApp;
import cn.tqp.exoplayer.glide.GlideThumbnailTransformation;
import cn.tqp.exoplayer.listener.ExoPlayerListener;
import cn.tqp.exoplayer.manager.ExoPlayerScreenOrientation;
import cn.tqp.exoplayer.utils.ExoPlayerUtils;
import cn.tqp.exoplayer.utils.NetworkUtils;
import cn.tqp.exoplayer.utils.ScreenUtils;
import cn.tqp.exoplayer.utils.ToastUtils;

/**
 * Created by tangqipeng on 2018/1/25.
 */
public class ExoPlayerControlView extends FrameLayout {

    static {
        ExoPlayerLibraryInfo.registerModule("goog.exo.ui");
    }

    /**
     * @deprecated Use {@link com.google.android.exoplayer2.ControlDispatcher}.
     */
    @Deprecated
    public interface ExoControlDispatcher extends ControlDispatcher {
    }

    /**
     * Listener to be notified about changes of the visibility of the UI control.
     */
    public interface VisibilityListener {

        /**
         * Called when the visibility changes.
         *
         * @param visibility The new visibility. Either {@link View#VISIBLE} or {@link View#GONE}.
         */
        void onVisibilityChange(int visibility);

    }

    private static final class DefaultControlDispatcher extends com.google.android.exoplayer2.DefaultControlDispatcher implements ExoControlDispatcher {
    }

    /**
     * @deprecated Use {@link com.google.android.exoplayer2.DefaultControlDispatcher}.
     */
    @Deprecated
    public static final ExoControlDispatcher DEFAULT_CONTROL_DISPATCHER = new DefaultControlDispatcher();

    /**
     * The default fast forward increment, in milliseconds.
     */
    public static final int DEFAULT_FAST_FORWARD_MS = 15000;
    /**
     * The default rewind increment, in milliseconds.
     */
    public static final int DEFAULT_REWIND_MS = 5000;
    /**
     * The default show timeout, in milliseconds.
     */
    public static final int DEFAULT_SHOW_TIMEOUT_MS = 5000;
    /**
     * The default repeat toggle modes.
     */
    public static final @RepeatModeUtil.RepeatToggleModes
    int DEFAULT_REPEAT_TOGGLE_MODES =
            RepeatModeUtil.REPEAT_TOGGLE_MODE_NONE;

    /**
     * The maximum number of windows that can be shown in a multi-window time bar.
     */
    public static final int MAX_WINDOWS_FOR_MULTI_WINDOW_TIME_BAR = 100;

    private static final long MAX_POSITION_FOR_SEEK_TO_PREVIOUS = 3000;

    private Context mContext;
    private final ComponentListener componentListener;
    private final View topContainer;
    private final View bottomContainer;
    private final View backButton;
    private final TextView txtTitle;
    private final TextView txtDate;
    private final ImageView mImageBattery;
    private final TextView mTxtBattery;
    private final View previousButton;
    private final View nextButton;
    private final View playButton;
    private final View pauseButton;
    private final View fastForwardButton;
    private final View rewindButton;
    private final ImageView repeatToggleButton;
    private final View shuffleButton;
    private final View scrrenButton;
    private final ImageView lockButton;
    private final TextView durationView;
    private final TextView positionView;
    private FrameLayout previewLayout;
    private SimpleExoPlayerView preView;
    private ImageView mImageView;
    private ExoPlayerSeekView seekView;
    private final TimeBar timeBar;
    private final StringBuilder formatBuilder;
    private final Formatter formatter;
    private final Timeline.Period period;
    private final Timeline.Window window;

    private View netView;
    private ImageView imageNetBack;
    private TextView txtTip;
    private TextView btnReplay;

    private final Drawable repeatOffButtonDrawable;
    private final Drawable repeatOneButtonDrawable;
    private final Drawable repeatAllButtonDrawable;
    private final String repeatOffButtonContentDescription;
    private final String repeatOneButtonContentDescription;
    private final String repeatAllButtonContentDescription;

    private SimpleExoPlayer preExoPlayer;
    private DefaultTrackSelector perTrackSelector;
    private LoadControl mLoadControl;
    private MediaSource mPreviewMediaSource;
    private List<VideoInfo> mPreviewVideo;

    private Player player;
    private ExoControlDispatcher controlDispatcher;
    private VisibilityListener visibilityListener;

    private boolean isAttachedToWindow;
    private boolean showMultiWindowTimeBar;
    private boolean multiWindowTimeBar;
    private boolean scrubbing;
    private int rewindMs;
    private int fastForwardMs;
    private int showTimeoutMs;
    private int fullScreenButton = R.drawable.full;
    private int smalllScreenButton = R.drawable.lessen;
    private int topContainerBackgyound = R.color.translucence;
    private int bottomContainerBackgyound = R.color.translucence;
    private @RepeatModeUtil.RepeatToggleModes
    int repeatToggleModes;
    private boolean showShuffleButton;
    private boolean showPreviewButton;
    private long hideAtMs;
    private long[] adGroupTimesMs;
    private boolean[] playedAdGroups;
    private long[] extraAdGroupTimesMs;
    private boolean[] extraPlayedAdGroups;

    private final Runnable updateProgressAction = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };

    private final Runnable hideAction = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    public ExoPlayerControlView(Context context) {
        this(context, null);
    }

    public ExoPlayerControlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExoPlayerControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, attrs);
    }

    public ExoPlayerControlView(Context context, AttributeSet attrs, int defStyleAttr, AttributeSet controlAttrs) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        int controllerLayoutId = R.layout.exoplayer_control_view;
        rewindMs = DEFAULT_REWIND_MS;
        fastForwardMs = DEFAULT_FAST_FORWARD_MS;
        showTimeoutMs = DEFAULT_SHOW_TIMEOUT_MS;
        repeatToggleModes = DEFAULT_REPEAT_TOGGLE_MODES;
        showShuffleButton = false;
        showPreviewButton = true;
        if (controlAttrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(controlAttrs, R.styleable.ExoPlayerControlView, 0, 0);
            try {
                rewindMs = a.getInt(R.styleable.ExoPlayerControlView_control_rewind_increment, rewindMs);
                fastForwardMs = a.getInt(R.styleable.ExoPlayerControlView_control_fastforward_increment, fastForwardMs);
                showTimeoutMs = a.getInt(R.styleable.ExoPlayerControlView_control_show_timeout, showTimeoutMs);
                controllerLayoutId = a.getResourceId(R.styleable.ExoPlayerControlView_control_controller_layout_id, controllerLayoutId);
                repeatToggleModes = getRepeatToggleModes(a, repeatToggleModes);
                showShuffleButton = a.getBoolean(R.styleable.ExoPlayerControlView_control_show_shuffle_button, showShuffleButton);
                showPreviewButton = a.getBoolean(R.styleable.ExoPlayerControlView_control_show_preview_button, showPreviewButton);
                fullScreenButton = a.getResourceId(R.styleable.ExoPlayerControlView_control_full_scrren_drawable, fullScreenButton);
                smalllScreenButton = a.getResourceId(R.styleable.ExoPlayerControlView_control_small_scrren_drawable, smalllScreenButton);
                topContainerBackgyound = a.getResourceId(R.styleable.ExoPlayerControlView_control_top_container_background, topContainerBackgyound);
                bottomContainerBackgyound = a.getResourceId(R.styleable.ExoPlayerControlView_control_top_container_background, bottomContainerBackgyound);
            } finally {
                a.recycle();
            }
        }
        period = new Timeline.Period();
        window = new Timeline.Window();
        formatBuilder = new StringBuilder();
        formatter = new Formatter(formatBuilder, Locale.getDefault());
        adGroupTimesMs = new long[0];
        playedAdGroups = new boolean[0];
        extraAdGroupTimesMs = new long[0];
        extraPlayedAdGroups = new boolean[0];
        componentListener = new ComponentListener();
        controlDispatcher = new DefaultControlDispatcher();

        LayoutInflater.from(context).inflate(controllerLayoutId, this);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

        topContainer = findViewById(R.id.top_container);
        bottomContainer = findViewById(R.id.bottom_container);
        if (topContainer != null) {
            topContainer.setBackgroundResource(topContainerBackgyound);
            topContainer.setOnClickListener(componentListener);
        }
        if (bottomContainer != null) {
            bottomContainer.setBackgroundResource(bottomContainerBackgyound);
            bottomContainer.setOnClickListener(componentListener);
        }

        backButton = findViewById(R.id.image_back);
        if (backButton != null) {
            backButton.setOnClickListener(componentListener);
        }
        txtTitle = findViewById(R.id.txt_title);
        txtDate = findViewById(R.id.txt_date);
        mImageBattery = findViewById(R.id.image_battery);
        mTxtBattery = findViewById(R.id.txt_battery);

        if (mImageBattery != null || mTxtBattery != null) {
            context.registerReceiver(mBatterReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }

        durationView = findViewById(R.id.exo_duration);
        positionView = findViewById(R.id.exo_position);
        timeBar = findViewById(R.id.exo_progress);
        if (timeBar != null) {
            timeBar.addListener(componentListener);
        }
        playButton = findViewById(R.id.exo_play);
        if (playButton != null) {
            playButton.setOnClickListener(componentListener);
        }
        pauseButton = findViewById(R.id.exo_pause);
        if (pauseButton != null) {
            pauseButton.setOnClickListener(componentListener);
        }
        previousButton = findViewById(R.id.exo_prev);
        if (previousButton != null) {
            previousButton.setVisibility(GONE);
            previousButton.setOnClickListener(componentListener);
        }
        nextButton = findViewById(R.id.exo_next);
        if (nextButton != null) {
            nextButton.setOnClickListener(componentListener);
        }
        rewindButton = findViewById(R.id.exo_rew);
        if (rewindButton != null) {
            rewindButton.setVisibility(GONE);
            rewindButton.setOnClickListener(componentListener);
        }
        fastForwardButton = findViewById(R.id.exo_ffwd);
        if (fastForwardButton != null) {
            fastForwardButton.setVisibility(GONE);
            fastForwardButton.setOnClickListener(componentListener);
        }
        repeatToggleButton = findViewById(R.id.exo_repeat_toggle);
        if (repeatToggleButton != null) {
            repeatToggleButton.setVisibility(GONE);
            repeatToggleButton.setOnClickListener(componentListener);
        }
        shuffleButton = findViewById(R.id.exo_shuffle);
        if (shuffleButton != null) {
            shuffleButton.setVisibility(GONE);
            shuffleButton.setOnClickListener(componentListener);
        }
        scrrenButton = findViewById(R.id.exo_resize_screen);
        if (scrrenButton != null) {
            scrrenButton.setOnClickListener(componentListener);
        }
        lockButton = findViewById(R.id.image_lock);
        if (lockButton != null) {
            lockButton.setImageResource(R.mipmap.orientation_lock_close);
            Log.i("FFFF", "lockButton componentListener:");
            lockButton.setOnClickListener(componentListener);
        }

        if (showPreviewButton) {
            previewLayout = new FrameLayout(context);
            previewLayout.setBackgroundResource(R.drawable.translucence_round_bg);
            int screenWidth = ScreenUtils.getScreenWidth(context);
            int previewWidth = screenWidth / 3;
            int previewHeight = previewWidth * 9 / 16;
            if (screenWidth <= 1080) {
                previewWidth = (int) context.getResources().getDimension(R.dimen.base_dimen_320);
                previewHeight = (int) previewWidth * 9 / 16;
            }
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(previewWidth, previewHeight);
            layoutParams.gravity = Gravity.CENTER;
            addView(previewLayout, layoutParams);
            previewLayout.setVisibility(GONE);
        }

        Resources resources = context.getResources();
        repeatOffButtonDrawable = resources.getDrawable(com.google.android.exoplayer2.ui.R.drawable.exo_controls_repeat_off);
        repeatOneButtonDrawable = resources.getDrawable(com.google.android.exoplayer2.ui.R.drawable.exo_controls_repeat_one);
        repeatAllButtonDrawable = resources.getDrawable(com.google.android.exoplayer2.ui.R.drawable.exo_controls_repeat_all);
        repeatOffButtonContentDescription = resources.getString(com.google.android.exoplayer2.ui.R.string.exo_controls_repeat_off_description);
        repeatOneButtonContentDescription = resources.getString(com.google.android.exoplayer2.ui.R.string.exo_controls_repeat_one_description);
        repeatAllButtonContentDescription = resources.getString(com.google.android.exoplayer2.ui.R.string.exo_controls_repeat_all_description);

        setSomeButtonVisible();

        //网络异常页
        netView = LayoutInflater.from(context).inflate(R.layout.net_error_layout, null);
        imageNetBack = (ImageView) netView.findViewById(R.id.image_net_back);
        txtTip = (TextView) netView.findViewById(R.id.txt_tip);
        btnReplay = (TextView) netView.findViewById(R.id.btn_replay);
        if (NetworkUtils.isOnlyMobileType(context)) {
            txtTip.setText(R.string.mobile_net_tip);
        } else if (!NetworkUtils.isNetworkAvalidate(context)){
            txtTip.setText(R.string.no_net_tip);
        }
        LayoutParams netLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(netView, netLayoutParams);
        netView.setVisibility(GONE);
        netView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        btnReplay.setOnClickListener(componentListener);
        imageNetBack.setOnClickListener(componentListener);

    }

    @SuppressWarnings("ResourceType")
    private static @RepeatModeUtil.RepeatToggleModes
    int getRepeatToggleModes(TypedArray a, @RepeatModeUtil.RepeatToggleModes int repeatToggleModes) {
        return a.getInt(R.styleable.ExoPlayerControlView_control_repeat_toggle_modes, repeatToggleModes);
    }

    public void setSomeButtonVisible(){
        if (ExoPlayerScreenOrientation.getIsPortrait(mContext)){
            if (txtDate != null){
                txtDate.setVisibility(INVISIBLE);
            }
            if (mImageBattery != null){
                mImageBattery.setVisibility(INVISIBLE);
            }
            if (mTxtBattery != null){
                mTxtBattery.setVisibility(INVISIBLE);
            }
            if (lockButton != null) {
                lockButton.setVisibility(INVISIBLE);
            }
        }else if (ExoPlayerScreenOrientation.getIsLandscape(mContext)){
            if (txtDate != null){
                txtDate.setVisibility(VISIBLE);
            }
            if (mImageBattery != null){
                mImageBattery.setVisibility(VISIBLE);
            }
            if (mTxtBattery != null){
                mTxtBattery.setVisibility(VISIBLE);
            }
            if (lockButton != null) {
                lockButton.setVisibility(VISIBLE);
            }
        }
    }

    /**
     * add preview mediaSource
     *
     * @param mediaSource
     */
    public void addPreviewMediaSouces(MediaSource mediaSource) {
        this.mPreviewMediaSource = mediaSource;
        if (showPreviewButton && previewLayout != null) {
            if (preView != null) {
                if (preExoPlayer != null) {
                    preExoPlayer.release();
                    preExoPlayer = null;
                    perTrackSelector = null;
                }
            } else {
                preView = new SimpleExoPlayerView(mContext);
            }
            if (previewLayout.getChildCount() > 0) {
                previewLayout.removeAllViews();
            }

            FrameLayout.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(2, 1, 2, 1);
            previewLayout.addView(preView, layoutParams);

            preView.setUseArtwork(false);
            preView.setControllerAutoShow(false);
            preView.setUseController(false);

            View view = preView.getVideoSurfaceView();
            if (view instanceof SurfaceView) {
                SurfaceView surfaceView = (SurfaceView) view;
                surfaceView.setZOrderMediaOverlay(true);
                surfaceView.setZOrderOnTop(true);
                surfaceView.setVisibility(View.INVISIBLE);
            }

            TrackSelection.Factory selection = new AdaptiveTrackSelection.Factory(null);
            perTrackSelector = new DefaultTrackSelector(selection);
            mLoadControl = new DefaultLoadControl();
            preExoPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(mContext), perTrackSelector, mLoadControl);
            preExoPlayer.setPlayWhenReady(false);
            preExoPlayer.setVolume(0f);
            preExoPlayer.prepare(mPreviewMediaSource);
            preView.setPlayer(preExoPlayer);
        }
    }

    /**
     * add preview images
     *
     * @param videoInfos
     */
    public void addPreviewImageview(List<VideoInfo> videoInfos) {
        this.mPreviewVideo = videoInfos;
        if (showPreviewButton && previewLayout != null) {
            if (null != videoInfos && videoInfos.size() > 0 && null != mPreviewVideo.get(0).previewImagesList && mPreviewVideo.get(0).previewImagesList.size() > 0) {
                mImageView = new ImageView(mContext);
                if (previewLayout.getChildCount() > 0) {
                    previewLayout.removeAllViews();
                }

                FrameLayout.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.setMargins(2, 1, 2, 1);
                previewLayout.addView(mImageView, layoutParams);
                for (PreviewImage image : mPreviewVideo.get(0).previewImagesList) {
                    GlideApp.with(mImageView).load(image.imagePreviewUrl).into(mImageView);
                }
            }else{
                seekView = new ExoPlayerSeekView(mContext);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.CENTER;
                addView(seekView, layoutParams);
                seekView.setVisibility(GONE);
            }
        }
    }

    /**
     * set movie title
     *
     * @param title
     */
    public void setMovieTitle(String title) {
        if (txtTitle != null) {
            txtTitle.setText(title);
        }
    }

    /**
     * Set the display and hide of the netView.
     * @param isShow
     * @param tag 标记
     */
    public void notifyNetViewVisible(boolean isShow, int tag){
        if (isShow){
            netView.setVisibility(VISIBLE);
            if (tag == ExoPlayerControl.MOBILE_NETWORK) {
                txtTip.setText(R.string.mobile_net_tip);
                btnReplay.setText(R.string.mobile_button_to_play);
            } else if (tag == ExoPlayerControl.NO_NETWORK){
                txtTip.setText(R.string.no_net_tip);
                btnReplay.setText(R.string.no_net_button_to_play);
            }
        }else{
            netView.setVisibility(GONE);
        }
    }

    /**
     * Returns the {@link Player} currently being controlled by this view, or null if no player is
     * set.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Sets the {@link Player} to control.
     *
     * @param player The {@link Player} to control.
     */
    public void setPlayer(Player player) {
        if (this.player == player) {
            return;
        }
        if (this.player != null) {
            this.player.removeListener(componentListener);
        }
        this.player = player;
        if (player != null) {
            player.addListener(componentListener);
        }
        updateAll();
    }

    /**
     * Sets whether the time bar should show all windows, as opposed to just the current one. If the
     * timeline has a period with unknown duration or more than
     * {@link #MAX_WINDOWS_FOR_MULTI_WINDOW_TIME_BAR} windows the time bar will fall back to showing a
     * single window.
     *
     * @param showMultiWindowTimeBar Whether the time bar should show all windows.
     */
    public void setShowMultiWindowTimeBar(boolean showMultiWindowTimeBar) {
        this.showMultiWindowTimeBar = showMultiWindowTimeBar;
        updateTimeBarMode();
    }

    /**
     * Sets the millisecond positions of extra ad markers relative to the start of the window (or
     * timeline, if in multi-window mode) and whether each extra ad has been played or not. The
     * markers are shown in addition to any ad markers for ads in the player's timeline.
     *
     * @param extraAdGroupTimesMs The millisecond timestamps of the extra ad markers to show, or
     *                            {@code null} to show no extra ad markers.
     * @param extraPlayedAdGroups Whether each ad has been played, or {@code null} to show no extra ad
     *                            markers.
     */
    public void setExtraAdGroupMarkers(@Nullable long[] extraAdGroupTimesMs,
                                       @Nullable boolean[] extraPlayedAdGroups) {
        if (extraAdGroupTimesMs == null) {
            this.extraAdGroupTimesMs = new long[0];
            this.extraPlayedAdGroups = new boolean[0];
        } else {
            Assertions.checkArgument(extraAdGroupTimesMs.length == extraPlayedAdGroups.length);
            this.extraAdGroupTimesMs = extraAdGroupTimesMs;
            this.extraPlayedAdGroups = extraPlayedAdGroups;
        }
        updateProgress();
    }

    /**
     * Sets the {@link VisibilityListener}.
     *
     * @param listener The listener to be notified about visibility changes.
     */
    public void setVisibilityListener(VisibilityListener listener) {
        this.visibilityListener = listener;
    }

    /**
     * Sets the {@link com.google.android.exoplayer2.ControlDispatcher}.
     *
     * @param controlDispatcher The {@link com.google.android.exoplayer2.ControlDispatcher}, or null
     *                          to use {@link com.google.android.exoplayer2.DefaultControlDispatcher}.
     */
    public void setControlDispatcher(@Nullable ExoControlDispatcher controlDispatcher) {
        this.controlDispatcher = controlDispatcher == null ? new DefaultControlDispatcher() : controlDispatcher;
    }

    /**
     * Sets the rewind increment in milliseconds.
     *
     * @param rewindMs The rewind increment in milliseconds. A non-positive value will cause the
     *                 rewind button to be disabled.
     */
    public void setRewindIncrementMs(int rewindMs) {
        this.rewindMs = rewindMs;
        updateNavigation();
    }

    /**
     * Sets the fast forward increment in milliseconds.
     *
     * @param fastForwardMs The fast forward increment in milliseconds. A non-positive value will
     *                      cause the fast forward button to be disabled.
     */
    public void setFastForwardIncrementMs(int fastForwardMs) {
        this.fastForwardMs = fastForwardMs;
        updateNavigation();
    }

    /**
     * Returns the playback controls timeout. The playback controls are automatically hidden after
     * this duration of time has elapsed without user input.
     *
     * @return The duration in milliseconds. A non-positive value indicates that the controls will
     * remain visible indefinitely.
     */
    public int getShowTimeoutMs() {
        return showTimeoutMs;
    }

    /**
     * Sets the playback controls timeout. The playback controls are automatically hidden after this
     * duration of time has elapsed without user input.
     *
     * @param showTimeoutMs The duration in milliseconds. A non-positive value will cause the controls
     *                      to remain visible indefinitely.
     */
    public void setShowTimeoutMs(int showTimeoutMs) {
        this.showTimeoutMs = showTimeoutMs;
    }

    /**
     * Returns which repeat toggle modes are enabled.
     *
     * @return The currently enabled {@link RepeatModeUtil.RepeatToggleModes}.
     */
    public @RepeatModeUtil.RepeatToggleModes
    int getRepeatToggleModes() {
        return repeatToggleModes;
    }

    /**
     * Sets which repeat toggle modes are enabled.
     *
     * @param repeatToggleModes A set of {@link RepeatModeUtil.RepeatToggleModes}.
     */
    public void setRepeatToggleModes(@RepeatModeUtil.RepeatToggleModes int repeatToggleModes) {
        this.repeatToggleModes = repeatToggleModes;
        if (player != null) {
            @Player.RepeatMode int currentMode = player.getRepeatMode();
            if (repeatToggleModes == RepeatModeUtil.REPEAT_TOGGLE_MODE_NONE
                    && currentMode != Player.REPEAT_MODE_OFF) {
                controlDispatcher.dispatchSetRepeatMode(player, Player.REPEAT_MODE_OFF);
            } else if (repeatToggleModes == RepeatModeUtil.REPEAT_TOGGLE_MODE_ONE
                    && currentMode == Player.REPEAT_MODE_ALL) {
                controlDispatcher.dispatchSetRepeatMode(player, Player.REPEAT_MODE_ONE);
            } else if (repeatToggleModes == RepeatModeUtil.REPEAT_TOGGLE_MODE_ALL
                    && currentMode == Player.REPEAT_MODE_ONE) {
                controlDispatcher.dispatchSetRepeatMode(player, Player.REPEAT_MODE_ALL);
            }
        }
    }

    /**
     * Returns whether the shuffle button is shown.
     */
    public boolean getShowShuffleButton() {
        return showShuffleButton;
    }

    /**
     * Sets whether the scrrenButton button is shown.
     *
     * @param isShow true show. false dismiss
     */
    public void setResizeScreenButton(boolean isShow) {
        if (scrrenButton != null) {
            if (isShow) {
                scrrenButton.setVisibility(VISIBLE);
            } else {
                scrrenButton.setVisibility(GONE);
            }
        }
    }

    /**
     * Sets whether the shuffle button is shown.
     *
     * @param showShuffleButton Whether the shuffle button is shown.
     */
    public void setShowShuffleButton(boolean showShuffleButton) {
        this.showShuffleButton = showShuffleButton;
        updateShuffleButton();
    }

    /**
     * Shows the playback controls. If {@link #getShowTimeoutMs()} is positive then the controls will
     * be automatically hidden after this duration of time has elapsed without user input.
     */
    public void show() {
        if (!isVisible()) {
            setVisibility(VISIBLE);
            if (visibilityListener != null) {
                visibilityListener.onVisibilityChange(getVisibility());
            }
            updateAll();
            requestPlayPauseFocus();

            if (ExoPlayerScreenOrientation.mIsScreenLock){
                if (bottomContainer != null){
                    bottomContainer.setVisibility(GONE);
                }
                if (topContainer != null){
                    topContainer.setVisibility(GONE);
                }
            }else{
                if (bottomContainer != null){
                    bottomContainer.setVisibility(VISIBLE);
                }
                if (topContainer != null){
                    topContainer.setVisibility(VISIBLE);
                }
            }

        }
        // Call hideAfterTimeout even if already visible to reset the timeout.
        hideAfterTimeout();
    }

    /**
     * Hides the controller.
     */
    public void hide() {
        if (isVisible()) {
            setVisibility(GONE);
            if (visibilityListener != null) {
                visibilityListener.onVisibilityChange(getVisibility());
            }
            removeCallbacks(updateProgressAction);
            removeCallbacks(hideAction);
            hideAtMs = C.TIME_UNSET;
        }
    }

    /**
     * Returns whether the controller is currently visible.
     */
    public boolean isVisible() {
        return getVisibility() == VISIBLE;
    }

    private void hideAfterTimeout() {
        removeCallbacks(hideAction);
        if (showTimeoutMs > 0) {
            hideAtMs = SystemClock.uptimeMillis() + showTimeoutMs;
            if (isAttachedToWindow) {
                postDelayed(hideAction, showTimeoutMs);
            }
        } else {
            hideAtMs = C.TIME_UNSET;
        }
    }

    private void updateAll() {
        updatePlayPauseButton();
        updateNavigation();
        updateRepeatModeButton();
        updateShuffleButton();
        updateProgress();
    }

    private void updatePlayPauseButton() {
        if (!isVisible() || !isAttachedToWindow) {
            return;
        }
        boolean requestPlayPauseFocus = false;
        boolean playing = player != null && player.getPlayWhenReady();
        if (playButton != null) {
            requestPlayPauseFocus |= playing && playButton.isFocused();
            playButton.setVisibility(playing ? View.GONE : View.VISIBLE);
        }
        if (pauseButton != null) {
            requestPlayPauseFocus |= !playing && pauseButton.isFocused();
            pauseButton.setVisibility(!playing ? View.GONE : View.VISIBLE);
        }
        if (requestPlayPauseFocus) {
            requestPlayPauseFocus();
        }
    }

    private void updateNavigation() {
        if (!isVisible() || !isAttachedToWindow) {
            return;
        }
        Timeline timeline = player != null ? player.getCurrentTimeline() : null;
        boolean haveNonEmptyTimeline = timeline != null && !timeline.isEmpty();
        boolean isSeekable = false;
        boolean enablePrevious = false;
        boolean enableNext = false;
        if (haveNonEmptyTimeline && !player.isPlayingAd()) {
            int windowIndex = player.getCurrentWindowIndex();
            timeline.getWindow(windowIndex, window);
            isSeekable = window.isSeekable;
            enablePrevious = isSeekable || !window.isDynamic
                    || player.getPreviousWindowIndex() != C.INDEX_UNSET;
            enableNext = window.isDynamic || player.getNextWindowIndex() != C.INDEX_UNSET;
        }
        setButtonEnabled(enablePrevious, previousButton);
        setButtonEnabled(enableNext, nextButton, true);
        setButtonEnabled(fastForwardMs > 0 && isSeekable, fastForwardButton);
        setButtonEnabled(rewindMs > 0 && isSeekable, rewindButton);
        if (timeBar != null) {
            timeBar.setEnabled(isSeekable);
        }
    }

    private void updateRepeatModeButton() {
        if (!isVisible() || !isAttachedToWindow || repeatToggleButton == null) {
            return;
        }
        if (repeatToggleModes == RepeatModeUtil.REPEAT_TOGGLE_MODE_NONE) {
            repeatToggleButton.setVisibility(View.GONE);
            return;
        }
        if (player == null) {
            setButtonEnabled(false, repeatToggleButton);
            return;
        }
        setButtonEnabled(true, repeatToggleButton);
        switch (player.getRepeatMode()) {
            case Player.REPEAT_MODE_OFF:
                repeatToggleButton.setImageDrawable(repeatOffButtonDrawable);
                repeatToggleButton.setContentDescription(repeatOffButtonContentDescription);
                break;
            case Player.REPEAT_MODE_ONE:
                repeatToggleButton.setImageDrawable(repeatOneButtonDrawable);
                repeatToggleButton.setContentDescription(repeatOneButtonContentDescription);
                break;
            case Player.REPEAT_MODE_ALL:
                repeatToggleButton.setImageDrawable(repeatAllButtonDrawable);
                repeatToggleButton.setContentDescription(repeatAllButtonContentDescription);
                break;
        }
        repeatToggleButton.setVisibility(View.VISIBLE);
    }

    private void updateShuffleButton() {
        if (!isVisible() || !isAttachedToWindow || shuffleButton == null) {
            return;
        }
        if (!showShuffleButton) {
            shuffleButton.setVisibility(View.GONE);
        } else if (player == null) {
            setButtonEnabled(false, shuffleButton);
        } else {
            shuffleButton.setAlpha(player.getShuffleModeEnabled() ? 1f : 0.3f);
            shuffleButton.setEnabled(true);
            shuffleButton.setVisibility(View.VISIBLE);
        }
    }

    private void updateTimeBarMode() {
        if (player == null) {
            return;
        }
        multiWindowTimeBar = showMultiWindowTimeBar
                && canShowMultiWindowTimeBar(player.getCurrentTimeline(), window);
    }

    private void updateProgress() {
        if (!isVisible() || !isAttachedToWindow) {
            return;
        }

        long position = 0;
        long bufferedPosition = 0;
        long duration = 0;
        if (player != null) {
            long currentWindowTimeBarOffsetUs = 0;
            long durationUs = 0;
            int adGroupCount = 0;
            Timeline timeline = player.getCurrentTimeline();
            if (!timeline.isEmpty()) {
                int currentWindowIndex = player.getCurrentWindowIndex();
                int firstWindowIndex = multiWindowTimeBar ? 0 : currentWindowIndex;
                int lastWindowIndex =
                        multiWindowTimeBar ? timeline.getWindowCount() - 1 : currentWindowIndex;
                for (int i = firstWindowIndex; i <= lastWindowIndex; i++) {
                    if (i == currentWindowIndex) {
                        currentWindowTimeBarOffsetUs = durationUs;
                    }
                    timeline.getWindow(i, window);
                    if (window.durationUs == C.TIME_UNSET) {
                        Assertions.checkState(!multiWindowTimeBar);
                        break;
                    }
                    for (int j = window.firstPeriodIndex; j <= window.lastPeriodIndex; j++) {
                        timeline.getPeriod(j, period);
                        int periodAdGroupCount = period.getAdGroupCount();
                        for (int adGroupIndex = 0; adGroupIndex < periodAdGroupCount; adGroupIndex++) {
                            long adGroupTimeInPeriodUs = period.getAdGroupTimeUs(adGroupIndex);
                            if (adGroupTimeInPeriodUs == C.TIME_END_OF_SOURCE) {
                                if (period.durationUs == C.TIME_UNSET) {
                                    // Don't show ad markers for postrolls in periods with unknown duration.
                                    continue;
                                }
                                adGroupTimeInPeriodUs = period.durationUs;
                            }
                            long adGroupTimeInWindowUs = adGroupTimeInPeriodUs + period.getPositionInWindowUs();
                            if (adGroupTimeInWindowUs >= 0 && adGroupTimeInWindowUs <= window.durationUs) {
                                if (adGroupCount == adGroupTimesMs.length) {
                                    int newLength = adGroupTimesMs.length == 0 ? 1 : adGroupTimesMs.length * 2;
                                    adGroupTimesMs = Arrays.copyOf(adGroupTimesMs, newLength);
                                    playedAdGroups = Arrays.copyOf(playedAdGroups, newLength);
                                }
                                adGroupTimesMs[adGroupCount] = C.usToMs(durationUs + adGroupTimeInWindowUs);
                                playedAdGroups[adGroupCount] = period.hasPlayedAdGroup(adGroupIndex);
                                adGroupCount++;
                            }
                        }
                    }
                    durationUs += window.durationUs;
                }
            }
            duration = C.usToMs(durationUs);
            position = C.usToMs(currentWindowTimeBarOffsetUs);
            bufferedPosition = position;
            if (player.isPlayingAd()) {
                position += player.getContentPosition();
                bufferedPosition = position;
            } else {
                position += player.getCurrentPosition();
                bufferedPosition += player.getBufferedPosition();
            }
            if (timeBar != null) {
                int extraAdGroupCount = extraAdGroupTimesMs.length;
                int totalAdGroupCount = adGroupCount + extraAdGroupCount;
                if (totalAdGroupCount > adGroupTimesMs.length) {
                    adGroupTimesMs = Arrays.copyOf(adGroupTimesMs, totalAdGroupCount);
                    playedAdGroups = Arrays.copyOf(playedAdGroups, totalAdGroupCount);
                }
                System.arraycopy(extraAdGroupTimesMs, 0, adGroupTimesMs, adGroupCount, extraAdGroupCount);
                System.arraycopy(extraPlayedAdGroups, 0, playedAdGroups, adGroupCount, extraAdGroupCount);
                timeBar.setAdGroupTimesMs(adGroupTimesMs, playedAdGroups, totalAdGroupCount);
            }
        }
        if (durationView != null) {
            durationView.setText(Util.getStringForTime(formatBuilder, formatter, duration));
        }
        if (positionView != null && !scrubbing) {
            positionView.setText(Util.getStringForTime(formatBuilder, formatter, position));
        }
        if (timeBar != null && !scrubbing) {
            timeBar.setPosition(position);
            timeBar.setBufferedPosition(bufferedPosition);
            timeBar.setDuration(duration);
        }

        if (txtDate != null) {
            android.text.format.Time localTime = new android.text.format.Time("Asia/Hong_Kong");
            localTime.setToNow();
            String date = localTime.format("%H:%M");
            txtDate.setText(date);
        }

        // Cancel any pending updates and schedule a new one if necessary.
        removeCallbacks(updateProgressAction);
        int playbackState = player == null ? Player.STATE_IDLE : player.getPlaybackState();
        if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {
            long delayMs;
            if (player.getPlayWhenReady() && playbackState == Player.STATE_READY) {
                float playbackSpeed = player.getPlaybackParameters().speed;
                if (playbackSpeed <= 0.1f) {
                    delayMs = 1000;
                } else if (playbackSpeed <= 5f) {
                    long mediaTimeUpdatePeriodMs = 1000 / Math.max(1, Math.round(1 / playbackSpeed));
                    long mediaTimeDelayMs = mediaTimeUpdatePeriodMs - (position % mediaTimeUpdatePeriodMs);
                    if (mediaTimeDelayMs < (mediaTimeUpdatePeriodMs / 5)) {
                        mediaTimeDelayMs += mediaTimeUpdatePeriodMs;
                    }
                    delayMs = playbackSpeed == 1 ? mediaTimeDelayMs
                            : (long) (mediaTimeDelayMs / playbackSpeed);
                } else {
                    delayMs = 200;
                }
            } else {
                delayMs = 1000;
            }
            postDelayed(updateProgressAction, delayMs);
        }
    }

    private void requestPlayPauseFocus() {
        boolean playing = player != null && player.getPlayWhenReady();
        if (!playing && playButton != null) {
            playButton.requestFocus();
        } else if (playing && pauseButton != null) {
            pauseButton.requestFocus();
        }
    }

    private void setButtonEnabled(boolean enabled, View view) {
        setButtonEnabled(enabled, view, false);
    }

    private void setButtonEnabled(boolean enabled, View view, boolean isVisible) {
        if (view == null) {
            return;
        }
        view.setEnabled(enabled);
        view.setAlpha(enabled ? 1f : 0.3f);
        if (view == nextButton) {
            view.setVisibility(enabled ? VISIBLE : GONE);
        } else {
            view.setVisibility(isVisible ? VISIBLE : GONE);
        }
    }

    private void previous() {
        Timeline timeline = player.getCurrentTimeline();
        if (timeline.isEmpty()) {
            return;
        }
        int windowIndex = player.getCurrentWindowIndex();
        timeline.getWindow(windowIndex, window);
        int previousWindowIndex = player.getPreviousWindowIndex();
        if (previousWindowIndex != C.INDEX_UNSET
                && (player.getCurrentPosition() <= MAX_POSITION_FOR_SEEK_TO_PREVIOUS
                || (window.isDynamic && !window.isSeekable))) {
            seekTo(previousWindowIndex, C.TIME_UNSET);
        } else {
            seekTo(0);
        }
    }

    private void next() {
        Timeline timeline = player.getCurrentTimeline();
        if (timeline.isEmpty()) {
            return;
        }
        int windowIndex = player.getCurrentWindowIndex();
        int nextWindowIndex = player.getNextWindowIndex();
        if (nextWindowIndex != C.INDEX_UNSET) {
            seekTo(nextWindowIndex, C.TIME_UNSET);
        } else if (timeline.getWindow(windowIndex, window, false).isDynamic) {
            seekTo(windowIndex, C.TIME_UNSET);
        }
    }

    private void rewind() {
        if (rewindMs <= 0) {
            return;
        }
        seekTo(Math.max(player.getCurrentPosition() - rewindMs, 0));
    }

    private void fastForward() {
        if (fastForwardMs <= 0) {
            return;
        }
        long durationMs = player.getDuration();
        long seekPositionMs = player.getCurrentPosition() + fastForwardMs;
        if (durationMs != C.TIME_UNSET) {
            seekPositionMs = Math.min(seekPositionMs, durationMs);
        }
        seekTo(seekPositionMs);
    }

    private void seekTo(long positionMs) {
        seekTo(player.getCurrentWindowIndex(), positionMs);
    }

    private void seekTo(int windowIndex, long positionMs) {
        boolean dispatched = controlDispatcher.dispatchSeekTo(player, windowIndex, positionMs);
        if (!dispatched) {
            // The seek wasn't dispatched. If the progress bar was dragged by the user to perform the
            // seek then it'll now be in the wrong position. Trigger a progress update to snap it back.
            updateProgress();
        }
        if (mSwitchoverWindow != null) {
            mSwitchoverWindow.changeWindowIndex(windowIndex);
        }
    }

    private void seekToTimeBarPosition(long positionMs) {
        int windowIndex;
        Timeline timeline = player.getCurrentTimeline();
        if (multiWindowTimeBar && !timeline.isEmpty()) {
            int windowCount = timeline.getWindowCount();
            windowIndex = 0;
            while (true) {
                long windowDurationMs = timeline.getWindow(windowIndex, window).getDurationMs();
                if (positionMs < windowDurationMs) {
                    break;
                } else if (windowIndex == windowCount - 1) {
                    // Seeking past the end of the last window should seek to the end of the timeline.
                    positionMs = windowDurationMs;
                    break;
                }
                positionMs -= windowDurationMs;
                windowIndex++;
            }
        } else {
            windowIndex = player.getCurrentWindowIndex();
        }
        seekTo(windowIndex, positionMs);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachedToWindow = true;
        if (hideAtMs != C.TIME_UNSET) {
            long delayMs = hideAtMs - SystemClock.uptimeMillis();
            if (delayMs <= 0) {
                hide();
            } else {
                postDelayed(hideAction, delayMs);
            }
        }
        updateAll();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttachedToWindow = false;
        removeCallbacks(updateProgressAction);
        removeCallbacks(hideAction);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return dispatchMediaKeyEvent(event) || super.dispatchKeyEvent(event);
    }

    /**
     * Called to process media key events. Any {@link KeyEvent} can be passed but only media key
     * events will be handled.
     *
     * @param event A key event.
     * @return Whether the key event was handled.
     */
    public boolean dispatchMediaKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (player == null || !isHandledMediaKey(keyCode)) {
            return false;
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
                fastForward();
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_REWIND) {
                rewind();
            } else if (event.getRepeatCount() == 0) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        controlDispatcher.dispatchSetPlayWhenReady(player, !player.getPlayWhenReady());
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                        controlDispatcher.dispatchSetPlayWhenReady(player, true);
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                        controlDispatcher.dispatchSetPlayWhenReady(player, false);
                        break;
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        next();
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        previous();
                        break;
                    default:
                        break;
                }
            }
        }
        return true;
    }

    @SuppressLint("InlinedApi")
    private static boolean isHandledMediaKey(int keyCode) {
        return keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD
                || keyCode == KeyEvent.KEYCODE_MEDIA_REWIND
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE
                || keyCode == KeyEvent.KEYCODE_MEDIA_NEXT
                || keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS;
    }

    /**
     * Returns whether the specified {@code timeline} can be shown on a multi-window time bar.
     *
     * @param timeline The {@link Timeline} to check.
     * @param window   A scratch {@link Timeline.Window} instance.
     * @return Whether the specified timeline can be shown on a multi-window time bar.
     */
    private static boolean canShowMultiWindowTimeBar(Timeline timeline, Timeline.Window window) {
        if (timeline.getWindowCount() > MAX_WINDOWS_FOR_MULTI_WINDOW_TIME_BAR) {
            return false;
        }
        int windowCount = timeline.getWindowCount();
        for (int i = 0; i < windowCount; i++) {
            if (timeline.getWindow(i, window).durationUs == C.TIME_UNSET) {
                return false;
            }
        }
        return true;
    }

    /**
     * 当前滑动到的位置
     */
    private long seekPosition = 0;

    private long lastSeekPosition = 0;

    private long lastSeekTime = 0;
    private long currentSeekTime = 0;

    /**
     * To start fast-forward, set the preview window.
     */
    public void seekStartPreview(){
        removeCallbacks(hideAction);
        scrubbing = true;
        if (showPreviewButton && previewLayout != null && preView != null && mPreviewMediaSource != null) {
            if (player != null && player instanceof SimpleExoPlayer) {
                player.setPlayWhenReady(false);
                seekPosition = player.getCurrentPosition();
                preExoPlayer.seekTo(player.getCurrentPosition());
                preExoPlayer.setPlayWhenReady(false);
                View view = preView.getVideoSurfaceView();
                if (view instanceof SurfaceView) {
                    view.setVisibility(View.VISIBLE);
                }
            }
            previewLayout.setVisibility(View.VISIBLE);
        }

        if (showPreviewButton && previewLayout != null && mImageView != null && mPreviewVideo != null){
            player.setPlayWhenReady(false);
            seekPosition = player.getCurrentPosition();
            previewLayout.setVisibility(View.VISIBLE);
        }

        if (showPreviewButton && seekView != null && mPreviewVideo != null){
            player.setPlayWhenReady(false);
            seekPosition = player.getCurrentPosition();
            seekView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Fast-forward to change progress.
     * @param position
     */
    public void seekChangePreview(long position){
        if (positionView != null) {
            positionView.setText(Util.getStringForTime(formatBuilder, formatter, position));
        }
        if (showPreviewButton && previewLayout != null && preView != null && mPreviewMediaSource != null) {
            if (timeBar != null){
                timeBar.setPosition(position);
            }
            seekPosition = position;
            currentSeekTime = SystemClock.elapsedRealtime();
            if (currentSeekTime - lastSeekTime > 3000 || seekPosition - lastSeekPosition > 10000) {//这里是两种显示，第一种是上次seek的时间与这次相隔3秒，或者滑动的距离超过10s
                preExoPlayer.seekTo(position);
                preExoPlayer.setPlayWhenReady(false);
                View view = preView.getVideoSurfaceView();
                if (view instanceof SurfaceView) {
                    view.setVisibility(View.VISIBLE);
                }
                lastSeekTime = currentSeekTime;
                lastSeekPosition = seekPosition;
            }
        }

        if (showPreviewButton && previewLayout != null && mImageView != null && mPreviewVideo != null){
            if (timeBar != null){
                timeBar.setPosition(position);
            }
            seekPosition = position;
            previewLayout.setVisibility(View.VISIBLE);

            long thumbnails_each_time = (long)(player.getDuration() / mPreviewVideo.get(player.getCurrentWindowIndex()).imageCount);

            int count = 0;

            int index = 0;

            for (int i = 0; i < mPreviewVideo.get(player.getCurrentWindowIndex()).previewImagesList.size(); i++){
                count = count + mPreviewVideo.get(player.getCurrentWindowIndex()).previewImagesList.get(i).imageSize;
                if (position / thumbnails_each_time <= count){
                    index = i;
                }
            }

            Log.i("PPPP", "index:"+index + "   position:"+position+"  thumbnails_each_time:"+thumbnails_each_time + "   player.getDuration():"+player.getDuration());

            if (index < mPreviewVideo.get(player.getCurrentWindowIndex()).previewImagesList.size()) {
                String imageUrl = mPreviewVideo.get(player.getCurrentWindowIndex()).previewImagesList.get(index).imagePreviewUrl;

                GlideApp.with(mImageView)
                        .load(imageUrl)
                        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .transform(new GlideThumbnailTransformation(position, ((int) thumbnails_each_time),
                                mPreviewVideo.get(player.getCurrentWindowIndex()).previewImagesList.get(index).lines, mPreviewVideo.get(player.getCurrentWindowIndex()).previewImagesList.get(index).colums))
                        .into(mImageView);
            }
        }

        if (showPreviewButton && seekView != null && mPreviewVideo != null){
            if (timeBar != null){
                timeBar.setPosition(position);
            }
            seekPosition = position;
            seekView.setVisibility(View.VISIBLE);
            seekView.setIconAndPosition(position);
        }
    }

    public long getPreViewCurrentPosition(){
//        return preExoPlayer.getCurrentPosition();
        return seekPosition;
    }

    /**
     * Fast forward to end
     * @param position
     * @param canceled
     */
    public void seekEndPreview(long position, boolean canceled){
        scrubbing = false;
        if (!canceled && player != null) {
            seekToTimeBarPosition(position);
        }
        hideAfterTimeout();
        if (showPreviewButton && previewLayout != null && preView != null && mPreviewMediaSource != null) {
            if (player != null && player instanceof SimpleExoPlayer) {
                player.setPlayWhenReady(true);
            }
            View view = preView.getVideoSurfaceView();
            if (view instanceof SurfaceView) {
                view.setVisibility(View.INVISIBLE);
            }
            preExoPlayer.setPlayWhenReady(false);
            previewLayout.setVisibility(View.GONE);
        }

        if (showPreviewButton && previewLayout != null && mImageView != null && mPreviewVideo != null){
            if (player != null && player instanceof SimpleExoPlayer) {
                player.setPlayWhenReady(true);
            }
            previewLayout.setVisibility(View.GONE);
        }

        if (showPreviewButton && seekView != null && mPreviewVideo != null){
            if (player != null && player instanceof SimpleExoPlayer) {
                player.setPlayWhenReady(true);
            }
            seekView.setVisibility(View.GONE);
        }
    }

    private final class ComponentListener extends Player.DefaultEventListener implements
            TimeBar.OnScrubListener, OnClickListener {

        @Override
        public void onScrubStart(TimeBar timeBar, long position) {
            seekStartPreview();
        }

        @Override
        public void onScrubMove(TimeBar timeBar, long position) {
            seekChangePreview(position);
        }

        @Override
        public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
            seekEndPreview(position, canceled);
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            updatePlayPauseButton();
            updateProgress();
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            updateRepeatModeButton();
            updateNavigation();
        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
            updateShuffleButton();
            updateNavigation();
        }

        @Override
        public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason) {
            updateNavigation();
            updateProgress();
            /**
             * 这里的预览窗口要同步切换
             */
            if (showPreviewButton && previewLayout != null && preView != null && mPreviewMediaSource != null && ExoPlayerUtils.getDiscontinuityReasonString(reason).equals("PERIOD_TRANSITION")) {
                preExoPlayer.seekTo(preExoPlayer.getNextWindowIndex(), 0);
            }

            if (mSwitchoverWindow != null && ExoPlayerUtils.getDiscontinuityReasonString(reason).equals("PERIOD_TRANSITION")) {
                mSwitchoverWindow.changeWindowIndex(player.getCurrentPeriodIndex());
            }

        }

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
            updateNavigation();
            updateTimeBarMode();
            updateProgress();
        }

        @Override
        public void onClick(View view) {
            if (player != null) {
                if (backButton == view || imageNetBack == view) {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && ExoPlayerScreenOrientation.mIsCanResize) {//横屏
                        ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        scrrenButton.setBackgroundResource(fullScreenButton);
                    } else {
                        ((Activity) mContext).finish();
                    }
                } else if (nextButton == view) {
                    next();
                    /**
                     * 这里的预览窗口要同步切换
                     */
                    if (showPreviewButton && previewLayout != null && preView != null && mPreviewMediaSource != null) {
                        preExoPlayer.seekTo(preExoPlayer.getNextWindowIndex(), 0);
                    }
                } else if (previousButton == view) {
                    previous();
                } else if (fastForwardButton == view) {
                    fastForward();
                } else if (rewindButton == view) {
                    rewind();
                } else if (playButton == view) {
                    controlDispatcher.dispatchSetPlayWhenReady(player, true);
                    notifyPlayTap();
                } else if (pauseButton == view) {
                    controlDispatcher.dispatchSetPlayWhenReady(player, false);
                    notifyPauseTap();
                } else if (repeatToggleButton == view) {
                    controlDispatcher.dispatchSetRepeatMode(player, RepeatModeUtil.getNextRepeatMode(
                            player.getRepeatMode(), repeatToggleModes));
                } else if (shuffleButton == view) {
                    controlDispatcher.dispatchSetShuffleModeEnabled(player, !player.getShuffleModeEnabled());
                } else if (scrrenButton == view) {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {//横屏
                        setOrientationChangeToPortrait();
                    } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {//竖屏
                        setOrientationChangeToLandscape();
                    }
                } else if (lockButton == view){
                    if (ExoPlayerScreenOrientation.mIsScreenLock){
                        ExoPlayerScreenOrientation.mIsScreenLock = false;
                        lockButton.setImageResource(R.mipmap.orientation_lock_close);
                        if (bottomContainer != null){
                            bottomContainer.setVisibility(VISIBLE);
                        }
                        if (topContainer != null){
                            topContainer.setVisibility(VISIBLE);
                        }
                    }else{
                        ExoPlayerScreenOrientation.mIsScreenLock = true;
                        lockButton.setImageResource(R.mipmap.orientation_lock_open);
                        if (bottomContainer != null){
                            bottomContainer.setVisibility(GONE);
                        }
                        if (topContainer != null){
                            topContainer.setVisibility(GONE);
                        }
                    }
                }else if (btnReplay == view){
                    if (NetworkUtils.isNetworkAvalidate(mContext)) {
                        player.setPlayWhenReady(true);
                        notifyNetViewVisible(false, ExoPlayerControl.MOBILE_NETWORK);
                        ExoPlayerControl.mobileNetPlay = true;
                    }else{
                        ToastUtils.showToast(mContext, R.string.no_net_tip);
                    }
                }
            }
            hideAfterTimeout();
        }
    }

    public void setOrientationChangeToPortrait(){
        ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        if (scrrenButton != null)
            scrrenButton.setBackgroundResource(fullScreenButton);
    }

    public void setOrientationChangeToLandscape(){
        ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        if (scrrenButton != null)
            scrrenButton.setBackgroundResource(smalllScreenButton);
    }

    /**
     * 电池状态即电量变化广播接收器
     */
    private BroadcastReceiver mBatterReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                    BatteryManager.BATTERY_STATUS_UNKNOWN);
            if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                // 充电中
                if (mImageBattery != null) {
                    mImageBattery.setImageResource(R.mipmap.battery_charging);
                }
                if (mTxtBattery != null) {
                    int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                    int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
                    int percentage = (int) (((float) level / scale) * 100);
                    mTxtBattery.setText(percentage + "%");
                }
            } else if (status == BatteryManager.BATTERY_STATUS_FULL) {
                // 充电完成
                if (mImageBattery != null) {
                    mImageBattery.setImageResource(R.mipmap.battery_full);
                }
                if (mTxtBattery != null) {
                    mTxtBattery.setText("100%");
                }
            } else {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
                int percentage = (int) (((float) level / scale) * 100);
                if (mImageBattery != null) {
                    if (percentage <= 10) {
                        mImageBattery.setImageResource(R.mipmap.battery_10);
                    } else if (percentage <= 20) {
                        mImageBattery.setImageResource(R.mipmap.battery_20);
                    } else if (percentage <= 50) {
                        mImageBattery.setImageResource(R.mipmap.battery_50);
                    } else if (percentage <= 80) {
                        mImageBattery.setImageResource(R.mipmap.battery_80);
                    } else if (percentage <= 100) {
                        mImageBattery.setImageResource(R.mipmap.battery_100);
                    }
                }
                if (mTxtBattery != null) {
                    mTxtBattery.setText(percentage + "%");
                }
            }
        }
    };


    public ExoPlayerListener.SwitchoverWindow mSwitchoverWindow;

    public void setSwitchoverWindow(ExoPlayerListener.SwitchoverWindow switchoverWindow) {
        this.mSwitchoverWindow = switchoverWindow;
    }

    public ExoPlayerListener.PlayerActionListener mPlayerActionListener;

    public void setPlayerActionListener(ExoPlayerListener.PlayerActionListener playerActionListener){
        this.mPlayerActionListener = playerActionListener;
    }

    public void notifyPlayTap(){
        if (mPlayerActionListener != null)
            mPlayerActionListener.playTap();
    }

    public void notifyPauseTap(){
        if (mPlayerActionListener != null)
            mPlayerActionListener.pauseTap();
    }

    public void release() {
        if (mImageBattery != null || mTxtBattery != null) {
            mContext.unregisterReceiver(mBatterReceiver);
        }
        if (preExoPlayer != null) {
            preExoPlayer.release();
            preExoPlayer = null;
            perTrackSelector = null;
            mLoadControl.onReleased();
            mLoadControl = null;
        }
    }

}