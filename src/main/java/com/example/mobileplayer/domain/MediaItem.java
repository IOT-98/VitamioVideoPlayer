package com.example.mobileplayer.domain;

import java.io.Serializable;

/**
 * @ProjectName: AS_AndroidItgiuguMyProject$
 * @Package: com.example.mobileplayer.domain$
 * @ClassName: MediaItem$
 * @Author: 周炜
 * @CreateDate: 2019/12/12$ 21:06$
 * @Version: 1.0
 * @Description: java类作用描述
 */

//代表一个视频,在Activity中传递对象，被传的对象需要序列化（不然会报错 java.lang.RuntimeException: Parcel: unable to marshal value MediaItem.....）
public class MediaItem implements Serializable{

    private String name;
    private Long duration;
    private Long size;
    private String data;

    private String coverImg;
    private String videoTitle;


    public MediaItem(String name, Long duration, Long size, String data, String coverImg, String videoTitle) {
        this.name = name;
        this.duration = duration;
        this.size = size;
        this.data = data;
        this.coverImg = coverImg;
        this.videoTitle = videoTitle;
    }

    public String getCoverImg() {
        return coverImg;
    }

    public void setCoverImg(String coverImg) {
        this.coverImg = coverImg;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    @Override
    public String toString() {
        return "MediaItem{" +
                "name='" + name + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                ", data='" + data + '\'' +
                ", coverImg='" + coverImg + '\'' +
                ", videoTitle='" + videoTitle + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }


    //这里可以不用构造方法
    //在加入集合是，先new一个MediaItem，然互其他的数值通过set设置。就像获取网络的资源
    // ，json没有视频总时间，用构造方法如果有时间参数却要设置时间,这样会带来不便
    //在播放器SystemVideoPlayer中，视频的总时间是通过VideoPlay来获取的，当VideoPlay设置的播放资源是可以得到是时间的
    public MediaItem(String name, Long duration, Long size, String data) {
        this.name = name;
        this.duration = duration;
        this.size = size;
        this.data = data;
    }

    public MediaItem() {

    }
}
