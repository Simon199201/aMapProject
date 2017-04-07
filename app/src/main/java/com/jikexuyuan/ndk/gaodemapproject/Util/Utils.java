package com.jikexuyuan.ndk.gaodemapproject.Util;

import android.text.TextUtils;

import com.amap.api.location.AMapLocation;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by simon on 17/4/7.
 * 工具类
 */

public class Utils {
    public synchronized static String getLocationStr(AMapLocation location) {
        if (null == location) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
        if (location.getErrorCode() == 0) {
            sb.append("定位成功" + "\n");
            sb.append("定位类型: ").append(location.getLocationType()).append("\n");
            sb.append("经    度    : ").append(location.getLongitude()).append("\n");
            sb.append("纬    度    : ").append(location.getLatitude()).append("\n");
            sb.append("精    度    : ").append(location.getAccuracy()).append("米").append("\n");
            sb.append("提供者    : ").append(location.getProvider()).append("\n");

            sb.append("速    度    : ").append(location.getSpeed()).append("米/秒").append("\n");
            sb.append("角    度    : ").append(location.getBearing()).append("\n");
            // 获取当前提供定位服务的卫星个数
            sb.append("星    数    : ").append(location.getSatellites()).append("\n");
            sb.append("国    家    : ").append(location.getCountry()).append("\n");
            sb.append("省            : ").append(location.getProvince()).append("\n");
            sb.append("市            : ").append(location.getCity()).append("\n");
            sb.append("城市编码 : ").append(location.getCityCode()).append("\n");
            sb.append("区            : ").append(location.getDistrict()).append("\n");
            sb.append("区域 码   : ").append(location.getAdCode()).append("\n");
            sb.append("地    址    : ").append(location.getAddress()).append("\n");
            sb.append("兴趣点    : ").append(location.getPoiName()).append("\n");
            //定位完成的时间
            sb.append("定位时间: ").append(formatUTC(location.getTime(), "yyyy-MM-dd HH:mm:ss")).append("\n");
        } else {
            //定位失败
            sb.append("定位失败" + "\n");
            sb.append("错误码:").append(location.getErrorCode()).append("\n");
            sb.append("错误信息:").append(location.getErrorInfo()).append("\n");
            sb.append("错误描述:").append(location.getLocationDetail()).append("\n");
        }
        //定位之后的回调时间
        sb.append("回调时间: ").append(formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss")).append("\n");
        return sb.toString();
    }

    private static SimpleDateFormat sdf = null;

    private synchronized static String formatUTC(long l, String strPattern) {
        if (TextUtils.isEmpty(strPattern)) {
            strPattern = "yyyy-MM-dd HH:mm:ss";
        }
        if (sdf == null) {
            try {
                sdf = new SimpleDateFormat(strPattern, Locale.CHINA);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else {
            sdf.applyPattern(strPattern);
        }
        return sdf == null ? "NULL" : sdf.format(l);
    }
}
