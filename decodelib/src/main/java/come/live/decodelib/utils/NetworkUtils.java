package come.live.decodelib.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author hengyang.lxb
 * @date 2020/12/19
 * @Version: 1.0
 * @Description:
 */
public class NetworkUtils {

    private static String wifiSSID = "";


    /**
     * 获取外网IP地址
     * @return 外网ip
     */
    public static String getNetIp(){
        String ip = "0";
        URL infoUrl = null;
        InputStream inStream = null;
        String line = "";
        try {
            infoUrl = new URL("http://pv.sohu.com/cityjson?ie=utf-8");
            URLConnection connection = infoUrl.openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection)connection;
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inStream = httpConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "utf-8"));
                StringBuilder tmpStr = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    tmpStr.append(line + "\n");
                }
                inStream.close();
                int start = tmpStr.indexOf("{");
                int end = tmpStr.indexOf("}");
                String json = tmpStr.substring(start, end + 1);
                if (json != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        line = jsonObject.optString("cip");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                ip = line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ip;
    }

    /**
     * 获取IP
     *
     * @param context
     * @return
     */
    public static  String getLocalWifiIp(Context context) {
        String ip = "0.0.0.0";
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return ip;
        }
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info == null) {
            return ip;
        }
        int type = info.getType();
        if (type == ConnectivityManager.TYPE_ETHERNET) {
            ip = getEtherNetIP();
            wifiSSID = "yx_wired";
        } else if (type == ConnectivityManager.TYPE_WIFI) {
            ip = getWifiIP(context);
        }
        return ip;
    }

    /**
     * 获取有线地址
     *
     * @return
     */
    public static String getEtherNetIP() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                    .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                        && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("getEtherNetIP ", ex.toString());
        }
        return "0.0.0.0";
    }

    /**
     * 获取wifiIP地址
     *
     * @param context
     * @return
     */
    public static String getWifiIP(Context context) {
        android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context.getSystemService(android.content.Context.WIFI_SERVICE);
        if (wifi == null) {
            return "0.0.0.0";
        }
        WifiInfo wifiinfo = wifi.getConnectionInfo();
        if (wifiinfo == null) {
            return "0.0.0.0";
        }
        try {
            wifiSSID = wifiinfo.getSSID().replace("\"", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        int intaddr = wifiinfo.getIpAddress();
        byte[] byteaddr = new byte[]{
            (byte) (intaddr & 0xff),
            (byte) (intaddr >> 8 & 0xff),
            (byte) (intaddr >> 16 & 0xff),
            (byte) (intaddr >> 24 & 0xff)};
        InetAddress addr = null;
        try {
            addr = InetAddress.getByAddress(byteaddr);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        if (addr != null) {
            return addr.getHostAddress();
        }
        return "0.0.0.0";
    }
}
