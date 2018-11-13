package cn.tqp.exoplayer.utils;

import android.os.SystemClock;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.trackselection.TrackSelection;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by tangqipeng on 2018/1/25.
 */

public class ExoPlayerUtils {

    private static final NumberFormat TIME_FORMAT;

    static {
        TIME_FORMAT = NumberFormat.getInstance(Locale.US);
        TIME_FORMAT.setMinimumFractionDigits(2);
        TIME_FORMAT.setMaximumFractionDigits(2);
        TIME_FORMAT.setGroupingUsed(false);
    }

    /**
     * 播放状态
     * @param state
     * @return
     */
    public static String getStateString(int state) {
        switch (state) {
            case Player.STATE_BUFFERING:
                return "B";
            case Player.STATE_ENDED:
                return "E";
            case Player.STATE_IDLE:
                return "I";
            case Player.STATE_READY:
                return "R";
            default:
                return "?";
        }
    }

    /**
     * 滑动进度，切换播放地址，初始化等状态
     * @param reason
     * @return
     */
    public static String getDiscontinuityReasonString(@Player.DiscontinuityReason int reason) {
        switch (reason) {
            case Player.DISCONTINUITY_REASON_PERIOD_TRANSITION:
                return "PERIOD_TRANSITION";
            case Player.DISCONTINUITY_REASON_SEEK:
                return "SEEK";
            case Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT:
                return "SEEK_ADJUSTMENT";
            case Player.DISCONTINUITY_REASON_INTERNAL:
                return "INTERNAL";
            default:
                return "?";
        }
    }

    public static String getFormatSupportString(int formatSupport) {
        switch (formatSupport) {
            case RendererCapabilities.FORMAT_HANDLED:
                return "YES";
            case RendererCapabilities.FORMAT_EXCEEDS_CAPABILITIES:
                return "NO_EXCEEDS_CAPABILITIES";
            case RendererCapabilities.FORMAT_UNSUPPORTED_DRM:
                return "NO_UNSUPPORTED_DRM";
            case RendererCapabilities.FORMAT_UNSUPPORTED_SUBTYPE:
                return "NO_UNSUPPORTED_TYPE";
            case RendererCapabilities.FORMAT_UNSUPPORTED_TYPE:
                return "NO";
            default:
                return "?";
        }
    }

    public static String getAdaptiveSupportString(int trackCount, int adaptiveSupport) {
        if (trackCount < 2) {
            return "N/A";
        }
        switch (adaptiveSupport) {
            case RendererCapabilities.ADAPTIVE_SEAMLESS:
                return "YES";
            case RendererCapabilities.ADAPTIVE_NOT_SEAMLESS:
                return "YES_NOT_SEAMLESS";
            case RendererCapabilities.ADAPTIVE_NOT_SUPPORTED:
                return "NO";
            default:
                return "?";
        }
    }

    // Suppressing reference equality warning because the track group stored in the track selection
    // must point to the exact track group object to be considered part of it.
    @SuppressWarnings("ReferenceEquality")
    public static String getTrackStatusString(TrackSelection selection, TrackGroup group,
                                               int trackIndex) {
        return getTrackStatusString(selection != null && selection.getTrackGroup() == group
                && selection.indexOf(trackIndex) != C.INDEX_UNSET);
    }

    public static String getTrackStatusString(boolean enabled) {
        return enabled ? "[X]" : "[ ]";
    }

    public static String getRepeatModeString(@Player.RepeatMode int repeatMode) {
        switch (repeatMode) {
            case Player.REPEAT_MODE_OFF:
                return "OFF";
            case Player.REPEAT_MODE_ONE:
                return "ONE";
            case Player.REPEAT_MODE_ALL:
                return "ALL";
            default:
                return "?";
        }
    }

    public static String getTimeString(long timeMs) {
        return timeMs == C.TIME_UNSET ? "?" : TIME_FORMAT.format((timeMs) / 1000f);
    }

    public static String getSessionTimeString(long startTimeMs) {
        return ExoPlayerUtils.getTimeString(SystemClock.elapsedRealtime() - startTimeMs);
    }

}
