package cn.tqp.exoplayer.manager;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.view.WindowManager;

/**
 * 系统亮度调节管理器
 * 
 * @author sunxiao
 * 
 */
public class ExoPlayerLightingManager {

    private static class VDPlayerLightingManagerINSTANCE {

        private static ExoPlayerLightingManager instance = new ExoPlayerLightingManager();
    }

    public static ExoPlayerLightingManager getInstance() {
        return VDPlayerLightingManagerINSTANCE.instance;
    }

    private float mCurrLightingNum = -1f;
    @SuppressWarnings("unused")
    private boolean mIsAutoLighting = false;
    @SuppressWarnings("unused")
    private boolean mIsFirst = true;
    private final static String TAG = "ExoPlayerLightingManager";

    /**
     * 设置当前使用的模式，不要用，没有开放
     * 
     * @deprecated
     * 
     * @param mode
     */
    public void setMode(int mode) {
        if (mode >= 0) {
            // mMode = mode;
        }
    }

    public void dragLightingTo(Context context, float curr, boolean notify) {
        mCurrLightingNum = curr;
        if (getIsAutoLightingSetting(context)) {
            setAutoLighting(context, false);
        }
        // 对于2.*系统来说，需要做一些特殊操作才能触发亮度调整
        WindowManager.LayoutParams lp = ((Activity) context).getWindow().getAttributes();
        lp.screenBrightness = curr;
        ((Activity) context).getWindow().setAttributes(lp);

//        if (notify) {
//            VDVideoViewController controller = VDVideoViewController.getInstance(context);
//            if (controller != null) {
//                controller.notifyLightingSetting(curr);
//            }
//        }
    }

    public float getCurrLightingSetting(Context context) {
        if (mCurrLightingNum < 0 && context != null) {
            try {
                mCurrLightingNum = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,
                        125);
            } catch (Exception e) {
                e.printStackTrace();
                mCurrLightingNum = 125;
            }
            mCurrLightingNum = mCurrLightingNum / 255;
        }
        return mCurrLightingNum;
    }

    public void setCurrLightingNum(Context context){
        mCurrLightingNum = getSystemBrightness(context);
    }

    private float getSystemBrightness(Context context) {
        float systemBrightness = 0;
        try {
            systemBrightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return systemBrightness / 255;
    }

    public boolean getIsAutoLightingSetting(Context context) {
        boolean ret = false;
        try {
            ret = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ret;
    }

    public void setAutoLighting(Context context, boolean isAutoLighting) {
        try {
            if (isAutoLighting) {
                Settings.System.putInt(((Activity) context).getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            } else {
                Settings.System.putInt(((Activity) context).getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void clean() {
        mCurrLightingNum = -1f;
        mIsAutoLighting = false;
        mIsFirst = true;
    }
}
