package com.example.mobileplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * @ProjectName: AS_AndroidItgiuguMyProject$
 * @Package: com.example.mobileplayer.view$
 * @ClassName: VideoView$
 * @Author: 周炜
 * @CreateDate: 2019/12/16$ 15:13$
 * @Version: 1.0
 * @Description: java类作用描述
 */
public class VideoView extends android.widget.VideoView {

    public VideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //测量
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);//保存测量结果(全屏)
    }


    //设置视频的画面大小
    //参一：要设置视频的宽 参二：要设置视频的高
    public void setVideoSize(int width,int height){
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.width=width;
        layoutParams.height=height;
        setLayoutParams(layoutParams);
    }

}
