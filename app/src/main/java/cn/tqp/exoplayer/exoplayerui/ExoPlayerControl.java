package cn.tqp.exoplayer.exoplayerui;

/**
 * Created by tangqipeng on 2018/2/6.
 */

public class ExoPlayerControl {

    public static long playPosition = 0;

    public static final int WIFI_NETWORK = 0;
    public static final int MOBILE_NETWORK = 1;
    public static final int NO_NETWORK = 2;

//    public static boolean isPrepared = false;//是否准备完成（这里加上这个字段是因为，如果一进入播放页在移动网络的情况下直接设置不缓存，再打开缓存没有办法进行缓存了）

    public static boolean isNetError = false;//是否网络异常

    public static boolean needBuffering = true;//控制需要继续缓存的开关

    public static boolean mobileNetPlay = false;//是否允许移动网络播放

}
