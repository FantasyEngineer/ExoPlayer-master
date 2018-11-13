package cn.tqp.exoplayer.manager;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import cn.tqp.exoplayer.listener.ExoPlayerListener;

/**
 * 从传感器中，获得相应的值 保证在横竖屏时候，左右摆动10度左右，能解除锁屏限制
 */
public class ExoPlayerGravitySensorManager {

    public static final int ScreenOreintationX = 1;
    public static final int ScreenOreintationY = 2;
    public static final int ScreenOreintationZ = 3;
    private int lastScreenOreintation = -1;
    private int currentScreenOreintation = 0;

    private class VDGravitySensorEventListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (!ExoPlayerScreenOrientation.mIsNeedSensor || ExoPlayerScreenOrientation.mIsScreenLock) {
                return;
            }
            float x = event.values[0]; // X轴
            float y = event.values[1]; // Y轴
            float z = event.values[2]; // Z轴
            if (Math.abs(y) > Math.abs(x) && Math.abs(y) > Math.abs(z)) {
                currentScreenOreintation = ScreenOreintationY;
            } else if (Math.abs(y) < Math.abs(x) && Math.abs(y) < Math.abs(x)) {
                currentScreenOreintation = ScreenOreintationX;
            }

            if (Math.abs(x) <= 3F && Math.abs(y) >= 7F && Math.abs(z) <= 6F) {
                if (lastScreenOreintation != currentScreenOreintation) {
                    lastScreenOreintation = currentScreenOreintation;
                    Log.i("ExoPlayerGravity", "onSensorChanged Portrait:  横屏 显示lockview");

                    if (mPlayerGravitySensorListener != null){
                        mPlayerGravitySensorListener.notifyOrientationChangeToPortrait();
                    }

                }
            } else if (Math.abs(x) >= 7.2F && Math.abs(y) <= 3.5F && (double) Math.abs(z) <= 6F) {
                if (lastScreenOreintation != currentScreenOreintation) {
                    lastScreenOreintation = currentScreenOreintation;
                    Log.i("ExoPlayerGravity", "onSensorChanged Landscape:  竖屏 显示lockview");

                    if (mPlayerGravitySensorListener != null){
                        mPlayerGravitySensorListener.notifyOrientationChangeToLandscape();
                    }
                }
            }
            if (ExoPlayerScreenOrientation.getIsPortrait(mContext)) {
                // 竖屏
                if (Math.abs(x) <= 3F && Math.abs(y) >= 7F && Math.abs(z) <= 6F) {
                    if (ExoPlayerScreenOrientation.getOrientationConfig(mContext) == ActivityInfo.SCREEN_ORIENTATION_SENSOR) {
                        return;
                    }

                    // mIsInSensor = true;
                }
            } else {
                if (Math.abs(x) >= 7.2F && Math.abs(y) <= 3.5F && (double) Math.abs(z) <= 6F) {
                    if (ExoPlayerScreenOrientation.getOrientationConfig(mContext) == ActivityInfo.SCREEN_ORIENTATION_SENSOR) {
                        return;
                    }
                    // 横屏
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }

    }

    private final static String TAG = "ExoPlayerGravitySensorManager";
    private SensorManager mSensorManager = null;
    private Sensor mSensor = null;
    private VDGravitySensorEventListener mEventListener = null;
    private Context mContext = null;

    public ExoPlayerGravitySensorManager() {
        super();
    }

    /**
     * 注册监听
     */
    public void register(Context context) {
        if (context == null) {
            return;
        }
        mContext = context;
        try {
            if (mEventListener == null) {
                mEventListener = new VDGravitySensorEventListener();
            }
            mSensorManager = (SensorManager) ((Activity) context).getSystemService(Context.SENSOR_SERVICE);
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            mSensorManager.registerListener(mEventListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ExoPlayerListener.PlayerGravitySensorListener mPlayerGravitySensorListener;

    public void setPlayerGravitySensorListener(ExoPlayerListener.PlayerGravitySensorListener playerGravitySensorListener){
        this.mPlayerGravitySensorListener = playerGravitySensorListener;
    }

    /**
     * 取消监听
     */
    public void release() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mEventListener);
        }
    }

}
