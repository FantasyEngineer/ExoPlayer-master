package cn.tqp.exoplayer.listener;

import android.graphics.PointF;
import android.view.MotionEvent;

/**
 * Created by tangqipeng on 2018/1/26.
 */

public class ExoPlayerListener {


    public interface SwitchoverWindow{

        void changeWindowIndex(int windowIndex);

    }

    public interface PlayerControlListener{

        void singleTouch(MotionEvent ev);

        void doubleTouch(MotionEvent e);

        void notifySoundVisible(boolean isShow);

        void notifySoundChanged(float curr);

        void notifyLightingVisible(boolean isShow);

        void notifyLightingSetting(float curr);

        void notifyPanelSeekStart();

        void notifyPanelSeekChange(PointF point1, PointF point2);

        void notifyPanelSeekEnd();

    }

    public interface PlayerGravitySensorListener {

        void notifyOrientationChangeToPortrait();

        void notifyOrientationChangeToLandscape();

    }

    public interface PlayerActionListener {//都是行为的监听

        void pauseTap();//手动暂停

        void playTap();//手动开始

    }

}
