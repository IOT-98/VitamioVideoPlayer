package com.example.mobileplayer.pager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.mobileplayer.R;
import com.example.mobileplayer.SystemVideoPlayer;
import com.example.mobileplayer.base.BasePager;

import org.xutils.common.Callback;
import org.xutils.common.util.LogUtil;
import org.xutils.http.RequestParams;
import org.xutils.x;

    public class LiveTVPager extends BasePager implements View.OnClickListener {

        private Button bu_play;
        public LiveTVPager(Context context) {
            //子类中所有的构造器默认都会访问父类中的空参数的构造器
            super(context);

        }
        @Override
        public View initView() {
            Log.e("NetVideoPager","==加载界面==");
            View inflate = View.inflate(context, R.layout.net_tv_pager, null);
            bu_play=inflate.findViewById(R.id.bu_play);
            bu_play.setOnClickListener(this);
            return inflate;
        }

        @Override
        public void initData() {
            super.initData();
            Log.e("NetVideoPager","==加载数据==");

        }


    @Override
    public void onClick(View v) {
        //调用自己手写的播放器  这里是把一个视频地址传到SystemVideoPlayer
        Intent intent = new Intent(context, SystemVideoPlayer.class);
        String data="http://cctvaliw.v.myalicdn.com/live/cctv6_1td.m3u8";
        intent.setData(Uri.parse(data));
        intent.setAction(Intent.ACTION_VIEW);
        context.startActivity(intent);
    }
    }

