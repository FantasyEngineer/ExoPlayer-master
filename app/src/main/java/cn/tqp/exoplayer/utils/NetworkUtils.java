package cn.tqp.exoplayer.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by tangqipeng on 2018/2/5.
 */

public class NetworkUtils {

    /**
     * 是否开启飞行模式
     *
     * @param context
     * @return
     */
    public static boolean isAirplaneModeOn(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }

    /**
     * 网络是否已连接
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        try {
            NetworkInfo networkInfo = ((ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE))
                    .getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 检查网络是否可用
     *
     * @return true可用,false不可用
     */
    public static boolean isNetworkAvalidate(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null
                && networkInfo.isConnected()
                && networkInfo.getState() == NetworkInfo.State.CONNECTED
                && networkInfo.isAvailable();
    }

    /**
     * Wifi网络是否已连接
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnectedByWifi(Context context) {
        NetworkInfo networkInfo = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE))
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return networkInfo != null && networkInfo.isConnected();
    }


    /**
     * 网络是否手机网络连接
     *
     * @param context
     * @return
     */
    public static boolean isOnlyMobileType(Context context) {
        NetworkInfo.State wifiState = null;
        NetworkInfo.State mobileState = null;
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        try {
            networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (networkInfo != null) {
            wifiState = networkInfo.getState();
        }
        try {
            networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (networkInfo != null) {
            mobileState = networkInfo.getState();
        }
        if (wifiState != null && mobileState != null
                && NetworkInfo.State.CONNECTED != wifiState
                && NetworkInfo.State.CONNECTED == mobileState) {
            // 手机网络连接成功
            return true;
        }
        return false;
    }

    /**
     * Cmwap网络是否已连接
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnectedByCmwap(Context context) {
        NetworkInfo networkInfo = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE))
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return networkInfo != null && networkInfo.isConnected()
                && networkInfo.getExtraInfo() != null
                && networkInfo.getExtraInfo().toLowerCase().contains("cmwap");
    }

    /**
     * 连接的是否是2G网络
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnectedBy2G(Context context) {
        NetworkInfo networkInfo = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE))
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (networkInfo != null && networkInfo.isConnected()) {
            int subtype = networkInfo.getSubtype();
            if (subtype == TelephonyManager.NETWORK_TYPE_GPRS
                    || subtype == TelephonyManager.NETWORK_TYPE_EDGE
                    || subtype == TelephonyManager.NETWORK_TYPE_CDMA) {// 移动和联通2G
                return true;
            }
        }
        return false;
    }

    /**
     * 连接的是否是3G网络
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnectedBy3G(Context context) {
        NetworkInfo networkInfo = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE))
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (networkInfo != null && networkInfo.isConnected()) {
            int subtype = networkInfo.getSubtype();
            if (subtype == TelephonyManager.NETWORK_TYPE_EVDO_A
                    || subtype == TelephonyManager.NETWORK_TYPE_EVDO_0
                    || subtype == TelephonyManager.NETWORK_TYPE_UMTS
                    || subtype == TelephonyManager.NETWORK_TYPE_HSPA) {// 电信或联通3G
                return true;
            }
        }
        return false;
    }

    /**
     * 获取本地Ip地址
     *
     * @param context
     * @return
     */
    public static String getLocalIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null && wifiInfo.getIpAddress() != 0) {
            return android.text.format.Formatter.formatIpAddress(wifiInfo
                    .getIpAddress());
        } else {
            try {
                Enumeration<NetworkInterface> en = NetworkInterface
                        .getNetworkInterfaces();
                while (en.hasMoreElements()) {
                    NetworkInterface intf = en.nextElement();
                    Enumeration<InetAddress> enumIpAddr = intf
                            .getInetAddresses();
                    while (enumIpAddr.hasMoreElements()) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()
                                && inetAddress.getHostAddress().indexOf(":") == -1) {
                            String ipAddress = inetAddress.getHostAddress();
                            if (!TextUtils.isEmpty(ipAddress)
                                    && !ipAddress.contains(":")) {
                                return ipAddress;
                            }
                        }
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


}
