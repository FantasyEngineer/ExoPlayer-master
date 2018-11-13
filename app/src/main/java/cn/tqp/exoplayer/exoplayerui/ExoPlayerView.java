package cn.tqp.exoplayer.exoplayerui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ControlDispatcher;
import com.google.android.exoplayer2.DefaultControlDispatcher;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.id3.ApicFrame;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.RepeatModeUtil;
import com.google.android.exoplayer2.util.Util;

import java.util.List;

import cn.tqp.exoplayer.R;
import cn.tqp.exoplayer.entity.VideoInfo;
import cn.tqp.exoplayer.listener.ExoPlayerListener;
import cn.tqp.exoplayer.manager.ExoPlayerGravitySensorManager;
import cn.tqp.exoplayer.manager.ExoPlayerScreenOrientation;
import cn.tqp.exoplayer.utils.AppUtil;
import cn.tqp.exoplayer.utils.ScreenUtils;

/**
 * Created by tangqipeng on 2018/1/25.
 */
/**
 * A high level view for {@link SimpleExoPlayer} media playbacks. It displays video, subtitles and
 * album art during playback, and displays playback controls using a {@link ExoPlayerControlView}.
 *
 * <p>A SimpleExoPlayerView can be customized by setting attributes (or calling corresponding
 * methods), overriding the view's layout file or by specifying a custom view layout file, as
 * outlined below.
 *
 * <h3>Attributes</h3>
 *
 * The following attributes can be set on a SimpleExoPlayerView when used in a layout XML file:
 *
 * <p>
 *
 * <ul>
 *   <li><b>{@code view_use_artwork}</b> - Whether artwork is used if available in audio streams.
 *       <ul>
 *         <li>Corresponding method: {@link #setUseArtwork(boolean)}
 *         <li>Default: {@code true}
 *       </ul>
 *   <li><b>{@code view_default_artwork}</b> - Default artwork to use if no artwork available in audio
 *       streams.
 *       <ul>
 *         <li>Corresponding method: {@link #setDefaultArtwork(Bitmap)}
 *         <li>Default: {@code null}
 *       </ul>
 *   <li><b>{@code view_use_controller}</b> - Whether the playback controls can be shown.
 *       <ul>
 *         <li>Corresponding method: {@link #setUseController(boolean)}
 *         <li>Default: {@code true}
 *       </ul>
 *   <li><b>{@code view_hide_on_touch}</b> - Whether the playback controls are hidden by touch events.
 *       <ul>
 *         <li>Corresponding method: {@link #setControllerHideOnTouch(boolean)}
 *         <li>Default: {@code true}
 *       </ul>
 *   <li><b>{@code view_auto_show}</b> - Whether the playback controls are automatically shown when
 *       playback starts, pauses, ends, or fails. If set to false, the playback controls can be
 *       manually operated with {@link #showController()} and {@link #hideController()}.
 *       <ul>
 *         <li>Corresponding method: {@link #setControllerAutoShow(boolean)}
 *         <li>Default: {@code true}
 *       </ul>
 *   <li><b>{@code view_hide_during_ads}</b> - Whether the playback controls are hidden during ads.
 *       Controls are always shown during ads if they are enabled and the player is paused.
 *       <ul>
 *         <li>Corresponding method: {@link #setControllerHideDuringAds(boolean)}
 *         <li>Default: {@code true}
 *       </ul>
 *   <li><b>{@code view_resize_mode}</b> - Controls how video and album art is resized within the view.
 *       Valid values are {@code fit}, {@code fixed_width}, {@code fixed_height} and {@code fill}.
 *       <ul>
 *         <li>Corresponding method: {@link #setResizeMode(int)}
 *         <li>Default: {@code fit}
 *       </ul>
 *   <li><b>{@code view_surface_type}</b> - The type of surface view used for video playbacks. Valid
 *       values are {@code surface_view}, {@code texture_view} and {@code none}. Using {@code none}
 *       is recommended for audio only applications, since creating the surface can be expensive.
 *       Using {@code surface_view} is recommended for video applications.
 *       <ul>
 *         <li>Corresponding method: None
 *         <li>Default: {@code surface_view}
 *       </ul>
 *   <li><b>{@code view_shutter_background_color}</b> - The background color of the {@code exo_shutter}
 *       view.
 *       <ul>
 *         <li>Corresponding method: {@link #setShutterBackgroundColor(int)}
 *         <li>Default: {@code unset}
 *       </ul>
 *   <li><b>{@code view_player_layout_id}</b> - Specifies the id of the layout to be inflated. See below
 *       for more details.
 *       <ul>
 *         <li>Corresponding method: None
 *         <li>Default: {@code R.id.exo_simple_player_view}
 *       </ul>
 *   <li><b>{@code control_controller_layout_id}</b> - Specifies the id of the layout resource to be
 *       inflated by the child {@link ExoPlayerControlView}. See below for more details.
 *       <ul>
 *         <li>Corresponding method: None
 *         <li>Default: {@code R.id.exo_playback_control_view}
 *       </ul>
 *   <li>All attributes that can be set on a {@link ExoPlayerControlView} can also be set on a
 *       SimpleExoPlayerView, and will be propagated to the inflated {@link ExoPlayerControlView}
 *       unless the layout is overridden to specify a custom {@code exo_controller} (see below).
 * </ul>
 *
 * <h3>Overriding the layout file</h3>
 *
 * To customize the layout of SimpleExoPlayerView throughout your app, or just for certain
 * configurations, you can define {@code exo_simple_player_view.xml} layout files in your
 * application {@code res/layout*} directories. These layouts will override the one provided by the
 * ExoPlayer library, and will be inflated for use by SimpleExoPlayerView. The view identifies and
 * binds its children by looking for the following ids:
 *
 * <p>
 *
 * <ul>
 *   <li><b>{@code exo_content_frame}</b> - A frame whose aspect ratio is resized based on the video
 *       or album art of the media being played, and the configured {@code resize_mode}. The video
 *       surface view is inflated into this frame as its first child.
 *       <ul>
 *         <li>Type: {@link AspectRatioFrameLayout}
 *       </ul>
 *   <li><b>{@code exo_shutter}</b> - A view that's made visible when video should be hidden. This
 *       view is typically an opaque view that covers the video surface view, thereby obscuring it
 *       when visible.
 *       <ul>
 *         <li>Type: {@link View}
 *       </ul>
 *   <li><b>{@code exo_subtitles}</b> - Displays subtitles.
 *       <ul>
 *         <li>Type: {@link SubtitleView}
 *       </ul>
 *   <li><b>{@code exo_artwork}</b> - Displays album art.
 *       <ul>
 *         <li>Type: {@link ImageView}
 *       </ul>
 *   <li><b>{@code exo_controller_placeholder}</b> - A placeholder that's replaced with the inflated
 *       {@link ExoPlayerControlView}. Ignored if an {@code exo_controller} view exists.
 *       <ul>
 *         <li>Type: {@link View}
 *       </ul>
 *   <li><b>{@code exo_controller}</b> - An already inflated {@link ExoPlayerControlView}. Allows use
 *       of a custom extension of {@link ExoPlayerControlView}. Note that attributes such as {@code
 *       rewind_increment} will not be automatically propagated through to this instance. If a view
 *       exists with this id, any {@code exo_controller_placeholder} view will be ignored.
 *       <ul>
 *         <li>Type: {@link ExoPlayerControlView}
 *       </ul>
 *   <li><b>{@code exo_overlay}</b> - A {@link FrameLayout} positioned on top of the player which
 *       the app can access via {@link #getOverlayFrameLayout()}, provided for convenience.
 *       <ul>
 *         <li>Type: {@link FrameLayout}
 *       </ul>
 * </ul>
 *
 * <p>All child views are optional and so can be omitted if not required, however where defined they
 * must be of the expected type.
 *
 * <h3>Specifying a custom layout file</h3>
 *
 * Defining your own {@code exo_simple_player_view.xml} is useful to customize the layout of
 * SimpleExoPlayerView throughout your application. It's also possible to customize the layout for a
 * single instance in a layout file. This is achieved by setting the {@code player_layout_id}
 * attribute on a SimpleExoPlayerView. This will cause the specified layout to be inflated instead
 * of {@code exo_simple_player_view.xml} for only the instance on which the attribute is set.
 */
@TargetApi(16)
public class ExoPlayerView extends FrameLayout implements ExoPlayerListener.SwitchoverWindow, ExoPlayerListener.PlayerControlListener, ExoPlayerListener.PlayerGravitySensorListener {

    private static final String TAG = "ExoPlayerView";
    private static final int SURFACE_TYPE_NONE = 0;
    private static final int SURFACE_TYPE_SURFACE_VIEW = 1;
    private static final int SURFACE_TYPE_TEXTURE_VIEW = 2;

    private Context mContext;
    private ViewGroup exoPlayerViewContainer;
    private final AspectRatioFrameLayout contentFrame;
    private final View shutterView;
    private final View surfaceView;
    private final ImageView artworkView;
    private final SubtitleView subtitleView;
    private final ExoPlayerControlView controller;
    private final ComponentListener componentListener;
    private final FrameLayout overlayFrameLayout;
    private ExoPlayerControlPanelView controlPanelView;
    private ExoPlayerLightAndSoundView mLightView;
    private ExoPlayerLoadingView mLoadingView;

    private SimpleExoPlayer player;
    private boolean useController;
    private boolean useArtwork;
    private Bitmap defaultArtwork;
    private int controllerShowTimeoutMs;
    private boolean controllerAutoShow;
    private boolean controllerHideDuringAds;
    private boolean controllerHideOnTouch;
    private boolean mIsCachePath;//是否是本地缓存片

    private ExoPlayerGravitySensorManager sensorManager;

    private List<VideoInfo> mVideoInfoList;

    public ExoPlayerView(Context context) {
        this(context, null);
    }

    public ExoPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        if (isInEditMode()) {
            contentFrame = null;
            shutterView = null;
            surfaceView = null;
            artworkView = null;
            subtitleView = null;
            controller = null;
            componentListener = null;
            overlayFrameLayout = null;
            ImageView logo = new ImageView(context);
            if (Util.SDK_INT >= 23) {
                configureEditModeLogoV23(getResources(), logo);
            } else {
                configureEditModeLogo(getResources(), logo);
            }
            addView(logo);
            return;
        }

        boolean shutterColorSet = false;
        int shutterColor = 0;
        int playerLayoutId = R.layout.exo_simple_player_view;
        boolean useArtwork = true;
        int defaultArtworkId = 0;
        boolean useController = true;
        int surfaceType = SURFACE_TYPE_SURFACE_VIEW;
        int resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT;
        int controllerShowTimeoutMs = PlaybackControlView.DEFAULT_SHOW_TIMEOUT_MS;
        boolean controllerHideOnTouch = true;
        boolean controllerAutoShow = true;
        boolean controllerHideDuringAds = true;
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ExoPlayerView, 0, 0);
            try {
                shutterColorSet = a.hasValue(R.styleable.ExoPlayerView_view_shutter_background_color);
                shutterColor = a.getColor(R.styleable.ExoPlayerView_view_shutter_background_color, shutterColor);
                playerLayoutId = a.getResourceId(R.styleable.ExoPlayerView_view_player_layout_id, playerLayoutId);
                useArtwork = a.getBoolean(R.styleable.ExoPlayerView_view_use_artwork, useArtwork);
                defaultArtworkId = a.getResourceId(R.styleable.ExoPlayerView_view_default_artwork, defaultArtworkId);
                useController = a.getBoolean(R.styleable.ExoPlayerView_view_use_controller, useController);
                surfaceType = a.getInt(R.styleable.ExoPlayerView_view_surface_type, surfaceType);
                resizeMode = a.getInt(R.styleable.ExoPlayerView_view_resize_mode, resizeMode);
                controllerShowTimeoutMs = a.getInt(R.styleable.ExoPlayerView_view_show_timeout, controllerShowTimeoutMs);
                controllerHideOnTouch = a.getBoolean(R.styleable.ExoPlayerView_view_hide_on_touch, controllerHideOnTouch);
                controllerAutoShow = a.getBoolean(R.styleable.ExoPlayerView_view_auto_show, controllerAutoShow);
                controllerHideDuringAds = a.getBoolean(R.styleable.ExoPlayerView_view_hide_during_ads, controllerHideDuringAds);
            } finally {
                a.recycle();
            }
        }

        LayoutInflater.from(context).inflate(playerLayoutId, this);
        componentListener = new ComponentListener();
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

        // Content frame.
        contentFrame = findViewById(com.google.android.exoplayer2.ui.R.id.exo_content_frame);
        if (contentFrame != null) {
            setResizeModeRaw(contentFrame, resizeMode);
        }

        // Shutter view.
        shutterView = findViewById(com.google.android.exoplayer2.ui.R.id.exo_shutter);
        if (shutterView != null && shutterColorSet) {
            shutterView.setBackgroundColor(shutterColor);
        }

        // Create a surface view and insert it into the content frame, if there is one.
        if (contentFrame != null && surfaceType != SURFACE_TYPE_NONE) {
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            surfaceView = surfaceType == SURFACE_TYPE_TEXTURE_VIEW ? new TextureView(context)
                    : new SurfaceView(context);
            surfaceView.setLayoutParams(params);
            contentFrame.addView(surfaceView, 0);
        } else {
            surfaceView = null;
        }

        // Overlay frame layout.
        overlayFrameLayout = findViewById(com.google.android.exoplayer2.ui.R.id.exo_overlay);

        // Artwork view.
        artworkView = findViewById(com.google.android.exoplayer2.ui.R.id.exo_artwork);
        this.useArtwork = useArtwork && artworkView != null;
        if (defaultArtworkId != 0) {
            defaultArtwork = BitmapFactory.decodeResource(context.getResources(), defaultArtworkId);
        }

        // Subtitle view.
        subtitleView = findViewById(com.google.android.exoplayer2.ui.R.id.exo_subtitles);
        if (subtitleView != null) {
            subtitleView.setUserDefaultStyle();
            subtitleView.setUserDefaultTextSize();
        }

        //Desktop controller
        controlPanelView = new ExoPlayerControlPanelView(context);
        FrameLayout.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(30, 100, 30, 100);
        addView(controlPanelView, layoutParams);
        controlPanelView.setPlayerControlListener(this);

        mLightView = new ExoPlayerLightAndSoundView(context);
        FrameLayout.LayoutParams lightParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lightParams.gravity = Gravity.CENTER;
        addView(mLightView, lightParams);
        mLightView.setVisibility(GONE);

        mLoadingView = new ExoPlayerLoadingView(context);
        FrameLayout.LayoutParams loadParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadParams.gravity = Gravity.CENTER;
        addView(mLoadingView, loadParams);
        mLoadingView.showLoading();

        ExoPlayerControlView customController = new ExoPlayerControlView(context, null, 0, attrs);
        if (customController != null) {
            this.controller = customController;
        }else{
            this.controller = new ExoPlayerControlView(context, null, 0, attrs);
        }
        FrameLayout.LayoutParams controlParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(customController, controlParams);

        this.controllerShowTimeoutMs = controller != null ? controllerShowTimeoutMs : 0;
        this.controllerHideOnTouch = controllerHideOnTouch;
        this.controllerAutoShow = controllerAutoShow;
        this.controllerHideDuringAds = controllerHideDuringAds;
        this.useController = useController && controller != null;
        hideController();
        if (this.controller != null){
            this.controller.setSwitchoverWindow(this);
        }

        if (ExoPlayerScreenOrientation.getIsCanSensor(mContext)){
            sensorManager = new ExoPlayerGravitySensorManager();
            sensorManager.register(mContext);
            sensorManager.setPlayerGravitySensorListener(this);
            ExoPlayerScreenOrientation.mIsNeedSensor = true;
            ExoPlayerScreenOrientation.mIsCanResize = true;
            if (this.controller != null)
                this.controller.setResizeScreenButton(true);
        }else{
            ExoPlayerScreenOrientation.mIsNeedSensor = false;
            if (ExoPlayerScreenOrientation.getIsPortrait(mContext)) {
                ExoPlayerScreenOrientation.mIsCanResize = true;
                if (this.controller != null)
                    this.controller.setResizeScreenButton(true);
            } else {
                ExoPlayerScreenOrientation.mIsCanResize = false;
                if (this.controller != null)
                    this.controller.setResizeScreenButton(false);
            }
        }
    }

    /**
     * ORIENTATION_LANDSCAPE or ORIENTATION_PORTRAIT change must set it [(ViewGroup) exoPlayerView.getParent()]
     * @param playerViewContainer
     */
    public void setExoPlayerViewContainer(ViewGroup playerViewContainer){
        this.exoPlayerViewContainer = playerViewContainer;
    }

    /**
     * add preview mediasouce
     * @param mediaSource
     */
    public void addPreviewMovieUrl(MediaSource mediaSource){
        if (this.controller != null){
            this.controller.addPreviewMediaSouces(mediaSource);
        }
    }

    /**
     * add preview images
     * @param videoInfoList
     */
    public void addPreviewImagesUrl(List<VideoInfo> videoInfoList){
        if (this.controller != null){
            this.controller.addPreviewImageview(videoInfoList);
        }
    }

    /**
     * set movie title
     */
    public void setMovieTitle(String name){
        if (this.controller != null){
            this.controller.setMovieTitle(name);
        }
    }

    public void setVideoInfoList(List<VideoInfo> videoInfoList, int windowIndex){
        this.mVideoInfoList = videoInfoList;
        if (windowIndex < videoInfoList.size()) {
            setIsCachePath(videoInfoList.get(windowIndex).isCache);
        }
    }

    public List<VideoInfo> getVideoList(){
        return mVideoInfoList;
    }

    public boolean isCachePath() {
        return mIsCachePath;
    }

    public void setIsCachePath(boolean mIsCachePath) {
        this.mIsCachePath = mIsCachePath;
    }

    /**
     * Set the display and hide of the netView.
     * @param isShow
     */
    public void notifyNetViewIsVisible(boolean isShow, int tag){
        if (this.controller != null)
            this.controller.notifyNetViewVisible(isShow, tag);
    }

    /**
     * Switches the view targeted by a given {@link SimpleExoPlayer}.
     *
     * @param player The player whose target view is being switched.
     * @param oldPlayerView The old view to detach from the player.
     * @param newPlayerView The new view to attach to the player.
     */
    public static void switchTargetView(@NonNull SimpleExoPlayer player, @Nullable SimpleExoPlayerView oldPlayerView, @Nullable SimpleExoPlayerView newPlayerView) {
        if (oldPlayerView == newPlayerView) {
            return;
        }
        // We attach the new view before detaching the old one because this ordering allows the player
        // to swap directly from one surface to another, without transitioning through a state where no
        // surface is attached. This is significantly more efficient and achieves a more seamless
        // transition when using platform provided video decoders.
        if (newPlayerView != null) {
            newPlayerView.setPlayer(player);
        }
        if (oldPlayerView != null) {
            oldPlayerView.setPlayer(null);
        }
    }

    /**
     * Returns the player currently set on this view, or null if no player is set.
     */
    public SimpleExoPlayer getPlayer() {
        return player;
    }

    /**
     * Set the {@link SimpleExoPlayer} to use.
     * <p>
     * To transition a {@link SimpleExoPlayer} from targeting one view to another, it's recommended to
     * use {@link #switchTargetView(SimpleExoPlayer, SimpleExoPlayerView, SimpleExoPlayerView)} rather
     * than this method. If you do wish to use this method directly, be sure to attach the player to
     * the new view <em>before</em> calling {@code setPlayer(null)} to detach it from the old one.
     * This ordering is significantly more efficient and may allow for more seamless transitions.
     *
     * @param player The {@link SimpleExoPlayer} to use.
     */
    public void setPlayer(SimpleExoPlayer player) {
        if (this.player == player) {
            return;
        }
        if (this.player != null) {
            this.player.removeListener(componentListener);
            this.player.removeTextOutput(componentListener);
            this.player.removeVideoListener(componentListener);
            if (surfaceView instanceof TextureView) {
                this.player.clearVideoTextureView((TextureView) surfaceView);
            } else if (surfaceView instanceof SurfaceView) {
                this.player.clearVideoSurfaceView((SurfaceView) surfaceView);
            }
        }
        this.player = player;
        if (useController) {
            controller.setPlayer(player);
        }
        if (shutterView != null) {
            shutterView.setVisibility(VISIBLE);
        }
        if (subtitleView != null) {
            subtitleView.setCues(null);
        }
        if (player != null) {
            if (surfaceView instanceof TextureView) {
                player.setVideoTextureView((TextureView) surfaceView);
            } else if (surfaceView instanceof SurfaceView) {
                player.setVideoSurfaceView((SurfaceView) surfaceView);
            }
            player.addVideoListener(componentListener);
            player.addTextOutput(componentListener);
            player.addListener(componentListener);
            maybeShowController(false);
            updateForCurrentTrackSelections();
        } else {
            hideController();
            hideArtwork();
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (surfaceView instanceof SurfaceView) {
            // Work around https://github.com/google/ExoPlayer/issues/3160.
            surfaceView.setVisibility(visibility);
        }
    }

    /**
     * Sets the resize mode.
     *
     * @param resizeMode The resize mode.
     */
    public void setResizeMode(@AspectRatioFrameLayout.ResizeMode int resizeMode) {
        Assertions.checkState(contentFrame != null);
        contentFrame.setResizeMode(resizeMode);
    }

    /**
     * Returns whether artwork is displayed if present in the media.
     */
    public boolean getUseArtwork() {
        return useArtwork;
    }

    /**
     * Sets whether artwork is displayed if present in the media.
     *
     * @param useArtwork Whether artwork is displayed.
     */
    public void setUseArtwork(boolean useArtwork) {
        Assertions.checkState(!useArtwork || artworkView != null);
        if (this.useArtwork != useArtwork) {
            this.useArtwork = useArtwork;
            updateForCurrentTrackSelections();
        }
    }

    /**
     * Returns the default artwork to display.
     */
    public Bitmap getDefaultArtwork() {
        return defaultArtwork;
    }

    /**
     * Sets the default artwork to display if {@code useArtwork} is {@code true} and no artwork is
     * present in the media.
     *
     * @param defaultArtwork the default artwork to display.
     */
    public void setDefaultArtwork(Bitmap defaultArtwork) {
        if (this.defaultArtwork != defaultArtwork) {
            this.defaultArtwork = defaultArtwork;
            updateForCurrentTrackSelections();
        }
    }

    /**
     * Returns whether the playback controls can be shown.
     */
    public boolean getUseController() {
        return useController;
    }

    /**
     * Sets whether the playback controls can be shown. If set to {@code false} the playback controls
     * are never visible and are disconnected from the player.
     *
     * @param useController Whether the playback controls can be shown.
     */
    public void setUseController(boolean useController) {
        Assertions.checkState(!useController || controller != null);
        if (this.useController == useController) {
            return;
        }
        this.useController = useController;
        if (useController) {
            controller.setPlayer(player);
        } else if (controller != null) {
            controller.hide();
            controller.setPlayer(null);
        }
    }

    public void hideLoadingView(){
        mLoadingView.hideLoading();
    }


    public void setExoPlayerActionListener(ExoPlayerListener.PlayerActionListener playerActionListener){
        if (this.controller != null)
            this.controller.setPlayerActionListener(playerActionListener);
    }

    /**
     * Sets the background color of the {@code exo_shutter} view.
     *
     * @param color The background color.
     */
    public void setShutterBackgroundColor(int color) {
        if (shutterView != null) {
            shutterView.setBackgroundColor(color);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (player != null && player.isPlayingAd()) {
            // Focus any overlay UI now, in case it's provided by a WebView whose contents may update
            // dynamically. This is needed to make the "Skip ad" button focused on Android TV when using
            // IMA [Internal: b/62371030].
            overlayFrameLayout.requestFocus();
            return super.dispatchKeyEvent(event);
        }
        boolean isDpadWhenControlHidden = isDpadKey(event.getKeyCode()) && useController
                && !controller.isVisible();
        maybeShowController(true);
        return isDpadWhenControlHidden || dispatchMediaKeyEvent(event) || super.dispatchKeyEvent(event);
    }

    /**
     * Called to process media key events. Any {@link KeyEvent} can be passed but only media key
     * events will be handled. Does nothing if playback controls are disabled.
     *
     * @param event A key event.
     * @return Whether the key event was handled.
     */
    public boolean dispatchMediaKeyEvent(KeyEvent event) {
        return useController && controller.dispatchMediaKeyEvent(event);
    }

    /**
     * Shows the playback controls. Does nothing if playback controls are disabled.
     *
     * <p>The playback controls are automatically hidden during playback after
     * {{@link #getControllerShowTimeoutMs()}}. They are shown indefinitely when playback has not
     * started yet, is paused, has ended or failed.
     */
    public void showController() {
        showController(shouldShowControllerIndefinitely());
    }

    /**
     * Hides the playback controls. Does nothing if playback controls are disabled.
     */
    public void hideController() {
        if (controller != null) {
            controller.hide();
        }
    }

    /**
     * The position of the fast forward point.
     */
    public long getSeekEndPosition(){
        if (this.controller != null) {
            return this.controller.getPreViewCurrentPosition();
        }
        return 0;
    }

    /**
     * Returns the playback controls timeout. The playback controls are automatically hidden after
     * this duration of time has elapsed without user input and with playback or buffering in
     * progress.
     *
     * @return The timeout in milliseconds. A non-positive value will cause the controller to remain
     *     visible indefinitely.
     */
    public int getControllerShowTimeoutMs() {
        return controllerShowTimeoutMs;
    }

    /**
     * Sets the playback controls timeout. The playback controls are automatically hidden after this
     * duration of time has elapsed without user input and with playback or buffering in progress.
     *
     * @param controllerShowTimeoutMs The timeout in milliseconds. A non-positive value will cause
     *     the controller to remain visible indefinitely.
     */
    public void setControllerShowTimeoutMs(int controllerShowTimeoutMs) {
        Assertions.checkState(controller != null);
        this.controllerShowTimeoutMs = controllerShowTimeoutMs;
    }

    /**
     * Returns whether the playback controls are hidden by touch events.
     */
    public boolean getControllerHideOnTouch() {
        return controllerHideOnTouch;
    }

    /**
     * Sets whether the playback controls are hidden by touch events.
     *
     * @param controllerHideOnTouch Whether the playback controls are hidden by touch events.
     */
    public void setControllerHideOnTouch(boolean controllerHideOnTouch) {
        Assertions.checkState(controller != null);
        this.controllerHideOnTouch = controllerHideOnTouch;
    }

    /**
     * Returns whether the playback controls are automatically shown when playback starts, pauses,
     * ends, or fails. If set to false, the playback controls can be manually operated with {@link
     * #showController()} and {@link #hideController()}.
     */
    public boolean getControllerAutoShow() {
        return controllerAutoShow;
    }

    /**
     * Sets whether the playback controls are automatically shown when playback starts, pauses, ends,
     * or fails. If set to false, the playback controls can be manually operated with {@link
     * #showController()} and {@link #hideController()}.
     *
     * @param controllerAutoShow Whether the playback controls are allowed to show automatically.
     */
    public void setControllerAutoShow(boolean controllerAutoShow) {
        this.controllerAutoShow = controllerAutoShow;
    }

    /**
     * Sets whether the playback controls are hidden when ads are playing. Controls are always shown
     * during ads if they are enabled and the player is paused.
     *
     * @param controllerHideDuringAds Whether the playback controls are hidden when ads are playing.
     */
    public void setControllerHideDuringAds(boolean controllerHideDuringAds) {
        this.controllerHideDuringAds = controllerHideDuringAds;
    }

    /**
     * Set the {@link PlaybackControlView.VisibilityListener}.
     *
     * @param listener The listener to be notified about visibility changes.
     */
    public void setControllerVisibilityListener(ExoPlayerControlView.VisibilityListener listener) {
        Assertions.checkState(controller != null);
        controller.setVisibilityListener(listener);
    }

    /**
     * Sets the {@link ControlDispatcher}.
     *
     * @param controlDispatcher The {@link ControlDispatcher}, or null to use
     *     {@link DefaultControlDispatcher}.
     */
    public void setControlDispatcher(@Nullable ExoPlayerControlView.ExoControlDispatcher controlDispatcher) {
        Assertions.checkState(controller != null);
        controller.setControlDispatcher(controlDispatcher);
    }

    /**
     * Sets the rewind increment in milliseconds.
     *
     * @param rewindMs The rewind increment in milliseconds. A non-positive value will cause the
     *     rewind button to be disabled.
     */
    public void setRewindIncrementMs(int rewindMs) {
        Assertions.checkState(controller != null);
        controller.setRewindIncrementMs(rewindMs);
    }

    /**
     * Sets the fast forward increment in milliseconds.
     *
     * @param fastForwardMs The fast forward increment in milliseconds. A non-positive value will
     *     cause the fast forward button to be disabled.
     */
    public void setFastForwardIncrementMs(int fastForwardMs) {
        Assertions.checkState(controller != null);
        controller.setFastForwardIncrementMs(fastForwardMs);
    }

    /**
     * Sets which repeat toggle modes are enabled.
     *
     * @param repeatToggleModes A set of {@link RepeatModeUtil.RepeatToggleModes}.
     */
    public void setRepeatToggleModes(@RepeatModeUtil.RepeatToggleModes int repeatToggleModes) {
        Assertions.checkState(controller != null);
        controller.setRepeatToggleModes(repeatToggleModes);
    }

    /**
     * Sets whether the shuffle button is shown.
     *
     * @param showShuffleButton Whether the shuffle button is shown.
     */
    public void setShowShuffleButton(boolean showShuffleButton) {
        Assertions.checkState(controller != null);
        controller.setShowShuffleButton(showShuffleButton);
    }

    /**
     * Sets whether the time bar should show all windows, as opposed to just the current one.
     *
     * @param showMultiWindowTimeBar Whether to show all windows.
     */
    public void setShowMultiWindowTimeBar(boolean showMultiWindowTimeBar) {
        Assertions.checkState(controller != null);
        controller.setShowMultiWindowTimeBar(showMultiWindowTimeBar);
    }

    /**
     * Gets the view onto which video is rendered. This is a:
     * <ul>
     *   <li>{@link SurfaceView} by default, or if the {@code surface_type} attribute is set to
     *   {@code surface_view}.</li>
     *   <li>{@link TextureView} if {@code surface_type} is {@code texture_view}.</li>
     *   <li>{@code null} if {@code surface_type} is {@code none}.</li>
     * </ul>
     *
     * @return The {@link SurfaceView}, {@link TextureView} or {@code null}.
     */
    public View getVideoSurfaceView() {
        return surfaceView;
    }

    /**
     * Gets the overlay {@link FrameLayout}, which can be populated with UI elements to show on top of
     * the player.
     *
     * @return The overlay {@link FrameLayout}, or {@code null} if the layout has been customized and
     *     the overlay is not present.
     */
    public FrameLayout getOverlayFrameLayout() {
        return overlayFrameLayout;
    }

    /**
     * Gets the {@link SubtitleView}.
     *
     * @return The {@link SubtitleView}, or {@code null} if the layout has been customized and the
     *     subtitle view is not present.
     */
    public SubtitleView getSubtitleView() {
        return subtitleView;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!useController || player == null || ev.getActionMasked() != MotionEvent.ACTION_DOWN) {
            return false;
        }
        if (!controller.isVisible()) {
            maybeShowController(true);
        } else if (controllerHideOnTouch) {
            controller.hide();
        }
        Log.i("5555", "onTouchEvent");
        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (!useController || player == null) {
            return false;
        }
        maybeShowController(true);
        return true;
    }

    /**
     * Shows the playback controls, but only if forced or shown indefinitely.
     */
    private void maybeShowController(boolean isForced) {
        if (isPlayingAd() && controllerHideDuringAds) {
            return;
        }
        if (useController) {
            boolean wasShowingIndefinitely = controller.isVisible() && controller.getShowTimeoutMs() <= 0;
            boolean shouldShowIndefinitely = shouldShowControllerIndefinitely();
            if (isForced || wasShowingIndefinitely || shouldShowIndefinitely) {
                showController(shouldShowIndefinitely);
            }
        }
    }

    private boolean shouldShowControllerIndefinitely() {
        if (player == null) {
            return true;
        }
        int playbackState = player.getPlaybackState();
        return controllerAutoShow && (playbackState == Player.STATE_IDLE
                || playbackState == Player.STATE_ENDED || !player.getPlayWhenReady());
    }

    private void showController(boolean showIndefinitely) {
        if (!useController) {
            return;
        }
        controller.setShowTimeoutMs(showIndefinitely ? 0 : controllerShowTimeoutMs);
        controller.show();
    }

    private boolean isPlayingAd() {
        return player != null && player.isPlayingAd() && player.getPlayWhenReady();
    }

    private void updateForCurrentTrackSelections() {
        if (player == null) {
            return;
        }
        TrackSelectionArray selections = player.getCurrentTrackSelections();
        for (int i = 0; i < selections.length; i++) {
            if (player.getRendererType(i) == C.TRACK_TYPE_VIDEO && selections.get(i) != null) {
                // Video enabled so artwork must be hidden. If the shutter is closed, it will be opened in
                // onRenderedFirstFrame().
                hideArtwork();
                return;
            }
        }
        // Video disabled so the shutter must be closed.
        if (shutterView != null) {
            shutterView.setVisibility(VISIBLE);
        }
        // Display artwork if enabled and available, else hide it.
        if (useArtwork) {
            for (int i = 0; i < selections.length; i++) {
                TrackSelection selection = selections.get(i);
                if (selection != null) {
                    for (int j = 0; j < selection.length(); j++) {
                        Metadata metadata = selection.getFormat(j).metadata;
                        if (metadata != null && setArtworkFromMetadata(metadata)) {
                            return;
                        }
                    }
                }
            }
            if (setArtworkFromBitmap(defaultArtwork)) {
                return;
            }
        }
        // Artwork disabled or unavailable.
        hideArtwork();
    }

    private boolean setArtworkFromMetadata(Metadata metadata) {
        for (int i = 0; i < metadata.length(); i++) {
            Metadata.Entry metadataEntry = metadata.get(i);
            if (metadataEntry instanceof ApicFrame) {
                byte[] bitmapData = ((ApicFrame) metadataEntry).pictureData;
                Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
                return setArtworkFromBitmap(bitmap);
            }
        }
        return false;
    }

    private boolean setArtworkFromBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            if (bitmapWidth > 0 && bitmapHeight > 0) {
                if (contentFrame != null) {
                    contentFrame.setAspectRatio((float) bitmapWidth / bitmapHeight);
                }
                artworkView.setImageBitmap(bitmap);
                artworkView.setVisibility(VISIBLE);
                return true;
            }
        }
        return false;
    }

    private void hideArtwork() {
        if (artworkView != null) {
            artworkView.setImageResource(android.R.color.transparent); // Clears any bitmap reference.
            artworkView.setVisibility(INVISIBLE);
        }
    }

    //设置videoFrame的大小
    private void scaleLayout(int width, int height) {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        if (width == 0) {
            width = ScreenUtils.getDpi(mContext);
        }
        if (height == 0) {
            height = outMetrics.heightPixels;
        }
        ViewGroup.LayoutParams params = this.getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(width, height);
        } else {
            params.height = height;
            params.width = width;
        }

        setLayoutParams(params);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {//横屏
            AppUtil.hideActionBarAndBottomUiMenu(mContext);

            if (exoPlayerViewContainer != null){
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (android.os.Build.VERSION.SDK_INT > 18) {
                            exoPlayerViewContainer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.INVISIBLE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                        } else {
                            exoPlayerViewContainer.setSystemUiVisibility(View.INVISIBLE);
                        }
                    }
                });
            }

            scaleLayout(0, 0);

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {//竖屏
            AppUtil.showActionBarAndBottomUiMenu(mContext);
            if (exoPlayerViewContainer != null){
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        exoPlayerViewContainer.setSystemUiVisibility(View.VISIBLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                    }
                });
            }


            scaleLayout(ScreenUtils.getScreenWidth(mContext), ScreenUtils.getScreenWidth(mContext) * 9 / 16);

        }

        if (this.controller != null){
            this.controller.setSomeButtonVisible();
        }
    }

    @TargetApi(23)
    private static void configureEditModeLogoV23(Resources resources, ImageView logo) {
        logo.setImageDrawable(resources.getDrawable(com.google.android.exoplayer2.ui.R.drawable.exo_edit_mode_logo, null));
        logo.setBackgroundColor(resources.getColor(com.google.android.exoplayer2.ui.R.color.exo_edit_mode_background_color, null));
    }

    @SuppressWarnings("deprecation")
    private static void configureEditModeLogo(Resources resources, ImageView logo) {
        logo.setImageDrawable(resources.getDrawable(com.google.android.exoplayer2.ui.R.drawable.exo_edit_mode_logo));
        logo.setBackgroundColor(resources.getColor(com.google.android.exoplayer2.ui.R.color.exo_edit_mode_background_color));
    }

    @SuppressWarnings("ResourceType")
    private static void setResizeModeRaw(AspectRatioFrameLayout aspectRatioFrame, int resizeMode) {
        aspectRatioFrame.setResizeMode(resizeMode);
    }

    @SuppressLint("InlinedApi")
    private boolean isDpadKey(int keyCode) {
        return keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_UP_RIGHT
                || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_DOWN_RIGHT
                || keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_DOWN_LEFT
                || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_UP_LEFT
                || keyCode == KeyEvent.KEYCODE_DPAD_CENTER;
    }

    private final class ComponentListener extends Player.DefaultEventListener implements TextOutput, SimpleExoPlayer.VideoListener {

        // TextOutput implementation

        @Override
        public void onCues(List<Cue> cues) {
            if (subtitleView != null) {
                subtitleView.onCues(cues);
            }
        }

        // SimpleExoPlayer.VideoInfoListener implementation

        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
            if (contentFrame != null) {
                float aspectRatio = height == 0 ? 1 : (width * pixelWidthHeightRatio) / height;
                contentFrame.setAspectRatio(aspectRatio);
            }
        }

        @Override
        public void onRenderedFirstFrame() {
            if (shutterView != null) {
                shutterView.setVisibility(INVISIBLE);
            }
        }

        @Override
        public void onTracksChanged(TrackGroupArray tracks, TrackSelectionArray selections) {
            updateForCurrentTrackSelections();
        }

        // Player.EventListener implementation

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (playbackState == Player.STATE_IDLE){
                if (mLoadingView != null)
                    mLoadingView.showLoading();
            } else if (playbackState == Player.STATE_READY){
                if (mLoadingView != null)
                    mLoadingView.hideLoading();
            } else if (playbackState == Player.STATE_BUFFERING){
                if (mLoadingView != null)
                    mLoadingView.showLoading();
            } else if (playbackState == Player.STATE_ENDED){
                if (mLoadingView != null)
                    mLoadingView.hideLoading();
            } else {
                Log.i(TAG, "?");
            }

            if (isPlayingAd() && controllerHideDuringAds) {
                hideController();
            } else {
                maybeShowController(false);
            }
        }

        @Override
        public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason) {
            if (isPlayingAd() && controllerHideDuringAds) {
                hideController();
            }
        }
    }

    @Override
    public void changeWindowIndex(int windowIndex) {
        if (windowIndex < getVideoList().size()){
            setMovieTitle(getVideoList().get(windowIndex).movieTitle);
        }
    }

    @Override
    public void singleTouch(MotionEvent ev) {
        onTouchEvent(ev);
    }

    @Override
    public void doubleTouch(MotionEvent ev) {
        if (this.controller != null  && null != this.controller.getPlayer() && !this.controller.getPlayer().isPlayingAd()){
            if (this.controller.getPlayer().getPlayWhenReady()){
                this.controller.getPlayer().setPlayWhenReady(false);
                this.controller.notifyPauseTap();
            }else{
                this.controller.getPlayer().setPlayWhenReady(true);
                this.controller.notifyPlayTap();
            }
            maybeShowController(true);
        }
    }

    @Override
    public void notifySoundVisible(boolean isShow) {
        mLightView.setIconType(ExoPlayerLightAndSoundView.SOUND_TYPE);
        if (isShow){
            mLightView.setVisibility(VISIBLE);
        }else {
            mLightView.setVisibility(GONE);
        }
    }

    @Override
    public void notifySoundChanged(float curr) {
        mLightView.setLightPercentage(curr);
    }

    @Override
    public void notifyLightingVisible(boolean isShow) {
        mLightView.setIconType(ExoPlayerLightAndSoundView.LIGHT_TYPE);
        if (isShow){
            mLightView.setVisibility(VISIBLE);
        }else {
            mLightView.setVisibility(GONE);
        }
    }

    @Override
    public void notifyLightingSetting(float curr) {
        mLightView.setLightPercentage(curr);
    }

    @Override
    public void notifyPanelSeekStart() {
        if (this.controller != null && controlPanelView != null){
            controlPanelView.setProgressRate(player.getDuration());
            this.controller.seekStartPreview();
        }
    }

    @Override
    public void notifyPanelSeekChange(PointF point1, PointF point2) {
        if (this.controller != null && controlPanelView != null){
            float curr = controlPanelView.getCurrTimeFromEvent(point1, point2, this.controller.getPreViewCurrentPosition(), player.getDuration());
            long seekTo = (long) (curr * player.getDuration());
            this.controller.seekChangePreview(seekTo);
        }
    }

    @Override
    public void notifyPanelSeekEnd() {
        if (this.controller != null && controlPanelView != null){
            this.controller.seekEndPreview(this.controller.getPreViewCurrentPosition(), false);
        }
    }

    /**
     * change Orientation to portrait
     */
    @Override
    public void notifyOrientationChangeToPortrait() {
        if (this.controller != null)
            this.controller.setOrientationChangeToPortrait();
    }

    /**
     * change Orientation to landscape
     */
    @Override
    public void notifyOrientationChangeToLandscape() {
        if (this.controller != null)
            this.controller.setOrientationChangeToLandscape();
    }

    public void release(){
        if (this.controller != null){
            this.controller.release();
        }
        if (sensorManager != null){
            sensorManager.release();
        }
    }
}
