package com.example.mobileplayer.uitls;

import android.content.Context;
import android.net.TrafficStats;

//网络播放工具类
public class NetUriUtils {

    private long lastTotalRxBytes=0;
    private long lastTimeStamp=0;

    //判断是否网络资源
    public Boolean isNetUri(String data) {
        boolean result=false;
        if(data!=null){
            if(data.toLowerCase().startsWith("http")||
                    data.toLowerCase().startsWith("rtsp")||
                    data.toLowerCase().startsWith("mms")){
                result=true;
            }
        }
        return result;
    }


    //得到网速
    public String getNetSpeed(Context context){

        long nowTotalRxBytes = TrafficStats.getUidRxBytes(context.getApplicationInfo().uid) ==
                TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);//转为KB

        long nowTimeStamp = System.currentTimeMillis();


        long speed = (nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp);//豪秒转换

        lastTimeStamp=nowTimeStamp;
        lastTotalRxBytes=nowTotalRxBytes;

        String speedStr=String.valueOf(speed)+"kb/s";
        return speedStr;
    }

}
