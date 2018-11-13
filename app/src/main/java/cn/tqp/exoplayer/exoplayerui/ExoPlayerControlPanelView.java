package cn.tqp.exoplayer.exoplayerui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import cn.tqp.exoplayer.R;
import cn.tqp.exoplayer.listener.ExoPlayerListener;
import cn.tqp.exoplayer.manager.ExoPlayerLightingManager;
import cn.tqp.exoplayer.manager.ExoPlayerScreenOrientation;
import cn.tqp.exoplayer.manager.ExoPlayerSoundManager;
import cn.tqp.exoplayer.utils.ScreenUtils;


/**
 * 控制面板显示反馈布局 1. 快进\快退 2. 双击暂停\播放 3. 亮度调整 4. 音量调整
 *
 * @author seven
 */
public class ExoPlayerControlPanelView extends View {

    @SuppressWarnings("unused")
    private Context mContext = null;

    private GestureDetector mGestureDetector = null;
    private VDVideoControlPanelGesture mVDVideoControlPanelGesture = null;
    private final static String TAG = "VDVideoControlPanelLayout";
    private int mLevel = -1;
    private boolean mIsVertical = false;
    private boolean mIsHorinzontal = false;
    private boolean mIsScrolling = false;
    private PointF mPrePoint = new PointF();

    private Handler mHandler =  new Handler();

    private boolean mOperationExecuting = false;

    private float mTmpStreamLevel;

    /**
     * 当前播放进度（100%）
     */
    public float mProgressRate;

    private eVerticalScrollTouchListener eFlag;


    public enum eVerticalScrollTouchListener {
        eTouchListenerVerticalScrollStart, eTouchListenerVerticalScroll, eTouchListenerVerticalScrollSound, eTouchListenerVerticalScrollLighting, eTouchListenerVerticalScrollEnd,
    }

    public enum eHorizonScrollTouchListener {
        eTouchListenerHorizonScrollStart, eTouchListenerHorizonScroll, eTouchListenerHorizonScrollEnd,
    }

    public final int GESTURELEVELSINGLETAP = 1;
    public final int GESTURELEVELDOUBLETAP = 2;
    public final int GESTURELEVELHORIZONSCROLL = 4;
    public final int GESTURELEVELVERTICALSCROLL = 8;
    public final int GESTURELEVELHORIZONSCROLLLIGHTING = 16;
    public final int GESTURELEVELHORIZONSCROLLSOUND = 32;

    public ExoPlayerControlPanelView(Context context) {
        super(context);
        // 直接使用控件的话，就默认全支持
        init(context,
                (GESTURELEVELVERTICALSCROLL | GESTURELEVELHORIZONSCROLL
                        | GESTURELEVELDOUBLETAP | GESTURELEVELSINGLETAP
                        | GESTURELEVELHORIZONSCROLLLIGHTING | GESTURELEVELHORIZONSCROLLSOUND));
    }

    public ExoPlayerControlPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);

        int level = GESTURELEVELSINGLETAP | GESTURELEVELHORIZONSCROLL | GESTURELEVELVERTICALSCROLL | GESTURELEVELDOUBLETAP;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ControlPanelContainer);
        for (int i = 0; i < typedArray.getIndexCount(); i++) {
            if (typedArray.getIndex(i) == R.styleable.ControlPanelContainer_gestureLevel) {
                level = typedArray.getInt(i, -1);
            }
        }
        typedArray.recycle();

        init(context, level);
    }

    public void mergeLevel(int level) {
        mLevel |= level;
    }

    private boolean checkLevel(int level) {
        if (mLevel < 0) {
            return false;
        }
        return ((mLevel & level) == level);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // TODO Auto-generated method stub
        return super.dispatchKeyEvent(event);
    }

    private void init(Context context, int level) {
        mContext = context;
        mLevel = level;

        mVDVideoControlPanelGesture = new VDVideoControlPanelGesture(context,
                level);
        mGestureDetector = new GestureDetector(context,
                mVDVideoControlPanelGesture);
        mGestureDetector.setIsLongpressEnabled(false);
    }

    private class VDVideoControlPanelGesture extends SimpleOnGestureListener {

        private Context mContext;

        public VDVideoControlPanelGesture(Context context, int level) {
            mContext = context;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            mPrePoint = new PointF();
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (ExoPlayerScreenOrientation.mIsScreenLock)
                return false;
            handleDoubleTap(e);
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            handleSingleTap(e);
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (ExoPlayerScreenOrientation.getIsPortrait(mContext) || ExoPlayerScreenOrientation.mIsScreenLock)
                return false;
            if (mPrePoint.equals(0.f, 0.f)) {
                mPrePoint.set(e1.getRawX(), e1.getRawY());
            }
            if (!mOperationExecuting) { // 滑动操作正在执行锁定其方向判断
                int fromDownX = (int) (e2.getX() - e1.getX());
                int fromDownY = (int) (e1.getY() - e2.getY());
                if (Math.abs(fromDownY) > 10F
                        && Math.abs(fromDownY) > Math.abs(fromDownX)) {
                    mIsVertical = true;
                    mOperationExecuting = true;
                    if (checkLevel(GESTURELEVELVERTICALSCROLL)) {
                        notifyScreenVerticalScrollTouch(new PointF(mPrePoint.x, mPrePoint.y),
                                new PointF(e2.getRawX(), e2.getRawY()),
                                new PointF(e1.getRawX(), e1.getRawY()),
                                eVerticalScrollTouchListener.eTouchListenerVerticalScrollStart, distanceY);
                    }
                    if (checkLevel(GESTURELEVELVERTICALSCROLL)) {
                        // 竖直滑动
                        eFlag = eVerticalScrollTouchListener.eTouchListenerVerticalScroll;
                    } else if (checkLevel(GESTURELEVELHORIZONSCROLLLIGHTING)) {
                        // 亮度调整
                        eFlag = eVerticalScrollTouchListener.eTouchListenerVerticalScrollLighting;
                    } else if (checkLevel(GESTURELEVELHORIZONSCROLLSOUND)) {
                        // 声音调整
                        eFlag = eVerticalScrollTouchListener.eTouchListenerVerticalScrollSound;
                    }
                } else if (Math.abs(fromDownX) > 10F && Math.abs(fromDownX) > Math.abs(fromDownY)) {
                    mIsHorinzontal = true;
                    mOperationExecuting = true;
                    if (checkLevel(GESTURELEVELHORIZONSCROLL)) {
                        notifyScreenHorizonScrollTouch(new PointF(mPrePoint.x, mPrePoint.y), new PointF(e2.getRawX(), e2.getRawY()), new PointF(e1.getRawX(), e1.getRawY()),
                                eHorizonScrollTouchListener.eTouchListenerHorizonScrollStart);
                    }
                }
            } else if (mIsVertical) {
                handleVerticalScroll(new PointF(mPrePoint.x, mPrePoint.y),
                        new PointF(e2.getRawX(), e2.getRawY()),
                        new PointF(e1.getRawX(), e1.getRawY()), distanceY);
            } else if (mIsHorinzontal) {
                handleHorizonScroll(new PointF(mPrePoint.x, mPrePoint.y),
                        new PointF(e2.getRawX(), e2.getRawY()),
                        new PointF(e1.getRawX(), e1.getRawY()));
            }
            mIsScrolling = true;
            mPrePoint.set(e2.getRawX(), e2.getRawY());

            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (mIsScrolling) {
                if (mIsVertical) {
                    handleVerticalScrollFinish(null, null,
                            new PointF(event.getRawX(), event.getRawY()), 0);
                } else if (mIsHorinzontal) {
                    handleHorizonScrollFinish(new PointF(mPrePoint.x,
                                    mPrePoint.y),
                            new PointF(event.getRawX(), event.getRawY()),
                            new PointF(event.getRawX(), event.getRawY()));
                }
                mIsScrolling = false;
            }
            mIsVertical = false;
            mIsHorinzontal = false;
            mOperationExecuting = false;
        }
        return true;
    }

    private void handleSingleTap(MotionEvent e) {
        if (mPlayerControlListener != null){
            mPlayerControlListener.singleTouch(e);
        }
    }

    private void handleDoubleTap(MotionEvent e) {
        if (mPlayerControlListener != null){
            mPlayerControlListener.doubleTouch(e);
        }
    }

    private void handleVerticalScroll(final PointF point1, final PointF point2,
                                      final PointF beginPoint, float distansY) {
        if (checkLevel(GESTURELEVELVERTICALSCROLL)) {
            notifyScreenVerticalScrollTouch(point1, point2, beginPoint, eFlag, distansY);
        }
    }

    private void handleVerticalScrollFinish(final PointF point1, final PointF point2, final PointF beginPoint, float distansY) {
        if (checkLevel(GESTURELEVELVERTICALSCROLL)) {
            notifyScreenVerticalScrollTouch(point1, point2, beginPoint, eVerticalScrollTouchListener.eTouchListenerVerticalScrollEnd, distansY);
        }
    }

    private void handleHorizonScroll(final PointF point1, final PointF point2, final PointF beginPoint) {
        if (checkLevel(GESTURELEVELHORIZONSCROLL)) {
            notifyScreenHorizonScrollTouch(point1, point2, beginPoint, eHorizonScrollTouchListener.eTouchListenerHorizonScroll);
        }
    }

    private void handleHorizonScrollFinish(final PointF point1, final PointF point2, final PointF beginPoint) {
        if (checkLevel(GESTURELEVELHORIZONSCROLL)) {
            notifyScreenHorizonScrollTouch(point1, point2, beginPoint, eHorizonScrollTouchListener.eTouchListenerHorizonScrollEnd);
        }
    }


    public void notifyScreenVerticalScrollTouch(final PointF point1, final PointF point2, final PointF beginPoint, final eVerticalScrollTouchListener flag, final float distansY) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                float curr = -1;
                boolean isSoundVisible = false;
                if (flag == eVerticalScrollTouchListener.eTouchListenerVerticalScrollEnd) {
                    curr = 0;
                } else if (flag == eVerticalScrollTouchListener.eTouchListenerVerticalScrollSound || getIsRight(beginPoint)) {
                    // 调节音量
                    isSoundVisible = true;
                    if (point1 != null && point2 != null) {
                        curr = getCurrSoundFromEvent(beginPoint, point2, distansY);
                    }
                } else if (flag == eVerticalScrollTouchListener.eTouchListenerVerticalScrollLighting
                        || !getIsRight(beginPoint)) {
                    // 调节亮度··
                    isSoundVisible = false;
                    if (point1 != null && point2 != null) {
                        curr = getCurrLightingFromEvent(point1, point2);
                    }
                }
                if (curr == -1) {
                    return;
                }
                switch (flag) {
                    case eTouchListenerVerticalScrollStart:
                        if (isSoundVisible) {
                            if (mPlayerControlListener != null){
                                mPlayerControlListener.notifySoundVisible(true);
                            }
                            mTmpStreamLevel = ExoPlayerSoundManager.getCurrSoundVolume(mContext);
                        } else {
                            if (mPlayerControlListener != null){
                                mPlayerControlListener.notifyLightingVisible(true);
                            }
                        }
                        break;

                    case eTouchListenerVerticalScrollLighting:
                        dragLightingTo(curr, true);

                        break;

                    case eTouchListenerVerticalScrollSound:
                        ExoPlayerSoundManager.dragSoundSeekTo(mContext, (int) curr);
                        if (mPlayerControlListener != null){
                            mPlayerControlListener.notifySoundChanged((int) curr);
                        }
                        break;

                    case eTouchListenerVerticalScroll:
                        // 判断是右边还是左边？
                        if (getIsRight(beginPoint)) {
                            ExoPlayerSoundManager.dragSoundSeekTo(mContext, (int) curr);
                            if (mPlayerControlListener != null){
                                mPlayerControlListener.notifySoundChanged((int) curr);
                            }
                        } else {
                            dragLightingTo(curr, true);
                        }
                        break;

                    case eTouchListenerVerticalScrollEnd:
                        if (mPlayerControlListener != null){
                            mPlayerControlListener.notifySoundVisible(false);
                            mPlayerControlListener.notifyLightingVisible(false);
                        }
                        break;

                    default:
                        break;
                }
            }
        });
    }

    private boolean getIsRight(final PointF point) {
        int width = ScreenUtils.getScreenWidth(mContext);
        boolean isRight = false;
        if (point.x > ((float) width / 2)) {
            // 在右边屏幕位置
            isRight = true;
        }

        return isRight;
    }

    /**
     * 使用坐标点方式得到[0-1]的音量取值范围
     *
     * @param point1
     * @param point2
     * @return
     */
    private float getCurrSoundFromEvent(final PointF point1, final PointF point2, float distansY) {
        int maxVolume = ExoPlayerSoundManager.getMaxSoundVolume(mContext);
        int currVolume = ExoPlayerSoundManager.getCurrSoundVolume(mContext);
        float degree = (float) distansY / ScreenUtils.getScreenHeight(mContext);
        mTmpStreamLevel += (degree * maxVolume);
        if (mTmpStreamLevel < 0) {
            mTmpStreamLevel = 0;
        } else if (mTmpStreamLevel > maxVolume) {
            mTmpStreamLevel = maxVolume;
        }
        return mTmpStreamLevel;
    }

    /**
     * 使用坐标点方式得到[0-1]的亮度取值范围
     *
     * @param point1
     * @param point2
     * @return
     */
    private float getCurrLightingFromEvent(final PointF point1,
                                           final PointF point2) {
        float ret = 1.0f;
        float y1 = point1.y;
        float y2 = point2.y;
        int height = ScreenUtils.getScreenHeight(mContext);
        float distance = y1 - y2;

        float degree = distance / height;

        float curNum = ExoPlayerLightingManager.getInstance().getCurrLightingSetting(mContext);

        ret = curNum + degree;

        if (ret >= 1.0) {
            ret = 1.0f;
        } else if (ret <= 0.1) { // 不能小于10%亮度
            ret = 0.1f;
        }

        return ret;
    }

    /**
     * 调整系统亮度 修改为 调整屏幕亮度
     *
     * @param curr
     */
    public void dragLightingTo(float curr, boolean notify) {

        ExoPlayerLightingManager.getInstance().dragLightingTo(mContext, curr, notify);
        try {
            final WindowManager.LayoutParams attrs = ((Activity) mContext).getWindow().getAttributes();
            attrs.screenBrightness = curr;
            ((Activity) mContext).getWindow().setAttributes(attrs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (notify) {
            if (mPlayerControlListener != null){
                mPlayerControlListener.notifyLightingSetting(curr);
            }
        }
    }

    public void notifyScreenHorizonScrollTouch(final PointF point1, final PointF point2, final PointF beginPoint, final eHorizonScrollTouchListener flag) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                switch (flag) {
                    case eTouchListenerHorizonScrollStart:
                        if (mPlayerControlListener != null){
                            mPlayerControlListener.notifyPanelSeekStart();
                        }
                        break;
                    case eTouchListenerHorizonScroll:
                        if (mPlayerControlListener != null){
                            mPlayerControlListener.notifyPanelSeekChange(point1, point2);
                        }
                        break;

                    case eTouchListenerHorizonScrollEnd:
                        if (mPlayerControlListener != null){
                            mPlayerControlListener.notifyPanelSeekEnd();
                        }
                        break;

                    default:
                        break;
                }
            }
        });
    }

    /**
     * 使用坐标点方式得到[0-1]的滑动取值范围
     *
     * @param point1
     * @param point2
     * @return
     */
    public float getCurrTimeFromEvent(final PointF point1, final PointF point2, long current, long duration) {
        int width = ScreenUtils.getScreenWidth(mContext);
        int distance = (int) (point2.x - point1.x);
        float rate = mProgressRate;
        float ret = ((float) current / duration) + ((float) distance / (float) width) * rate;
        if (ret < 0) {
            ret = 0;
        } else if (ret > 1) {
            ret = 1;
        }

        return ret;
    }

    /**
     * 设置进度显示比例
     */
    public void setProgressRate(long duration) {
        boolean isPortrait = (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
        Log.i("5555", "isPortrait:"+isPortrait);
        if (duration < 10 * 60 * 1000) {
            if (isPortrait) {
                mProgressRate = 60 * 1000f / duration;
            } else {
                mProgressRate = 90 * 1000f / duration;
            }

        } else if (duration < 20 * 60 * 1000) {
            if (isPortrait) {
                mProgressRate = 2 * 60 * 1000f / duration;
            } else {
                mProgressRate = 150 * 1000f / duration;
            }
        } else {
            if (isPortrait) {
                mProgressRate = 5 * 60 * 1000f / duration;
            } else {
                mProgressRate = 460 * 1000f / duration;
            }
        }

        Log.i("5555", "isPortrait:"+isPortrait);
    }

    public ExoPlayerListener.PlayerControlListener mPlayerControlListener;

    public void setPlayerControlListener(ExoPlayerListener.PlayerControlListener playerControlListenerc){
        this.mPlayerControlListener = playerControlListenerc;
    }

}
