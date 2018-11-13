/**
 * 全屏部分的变量暂存以及调整当前屏幕方向等等的函数包
 * 
 * @author sunxiao
 */

package cn.tqp.exoplayer.manager;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

public class ExoPlayerScreenOrientation {

    public static boolean mIsNeedSensor = false;//可自由转化屏幕 （这个主要用于重力感应是否启动）

    public static boolean mIsCanResize = false;//可变屏幕（这个主要用于全部的 可转屏幕大小的）

    public static boolean mIsScreenLock = false;//是否锁屏


    public static int getOrientationConfig(Context context) {
        if (context != null) {
            return ((Activity) context).getRequestedOrientation();
        }
        return -1;
    }

    /**
     * 目前是否横屏
     * @param context
     * @return
     */
    public static boolean getIsLandscape(Context context) {
        if (context != null) {
            return ((Activity) context).getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        }
        return false;
    }

    /**
     * 目前是否竖屏
     * @param context
     * @return
     */
    public static boolean getIsPortrait(Context context) {
        if (context != null) {
            return ((Activity) context).getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        }
        return false;
    }

    /**
     * 只能横屏
     * @param context
     * @return
     */
    public static boolean getIsOnlyLandscape(Context context){
        if (context != null) {
            return ((Activity) context).getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        }
        return false;
    }

    /**
     * 可以开启重力感应横竖屏切换模式
     * @param context
     * @return
     */
    public static boolean getIsCanSensor(Context context){
        if (context != null) {
            return ((Activity) context).getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        }
        return false;
    }

    public static void setPortrait(Context context) {
        mIsNeedSensor = true;
        if (context != null) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= 9) {
                    ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                } else {
                    ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            } catch (Exception e) {
                // do nothing
            }
        }
    }

    public static void setLandscape(Context context) {
        mIsNeedSensor = true;
        if (context != null) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= 9) {
                    ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                } else {
                    ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            } catch (Exception e) {
                // do nothing
            }
        }
    }

}
