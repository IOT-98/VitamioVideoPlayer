package com.example.mobileplayer.pager;

        import android.content.ContentResolver;
        import android.content.Context;
        import android.content.Intent;
        import android.database.Cursor;
        import android.net.Uri;
        import android.os.Build;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.Message;
        import android.os.SystemClock;
        import android.provider.MediaStore;
        import android.text.format.Formatter;
        import android.util.Log;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.AdapterView;
        import android.widget.BaseAdapter;
        import android.widget.ListView;
        import android.widget.ProgressBar;
        import android.widget.TextView;
        import androidx.annotation.NonNull;
        import com.example.mobileplayer.R;
        import com.example.mobileplayer.SystemVideoPlayer;
        import com.example.mobileplayer.base.BasePager;
        import com.example.mobileplayer.domain.MediaItem;
        import com.example.mobileplayer.uitls.Utils;
        import java.io.Serializable;
        import java.util.ArrayList;
        import java.util.List;

/**
 * @ProjectName: AS_AndroidItgiuguMyProject$
 * @Package: com.example.mobileplayer.pager$
 * @ClassName: VideoPager$
 * @Author: 周炜
 * @CreateDate: 2019/12/12$ 9:38$
 * @Version: 1.0
 * @Description: java类作用描述
 */
public class VideoPager extends BasePager {

    public VideoPager(Context context) {
        //子类中所有的构造器默认都会访问父类中的空参数的构造器
        super(context);
    }

    private TextView tv_nomedia;
    private ProgressBar pb_loding;
    private ListView lv_video_pager;
    private Utils utils;

    private List<MediaItem> mediaItemList=new ArrayList<>();
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(mediaItemList!=null&&mediaItemList.size()>0){
                tv_nomedia.setVisibility(View.GONE);
                pb_loding.setVisibility(View.GONE);
                lv_video_pager.setAdapter(new VideoAdapter());
            }else{
                pb_loding.setVisibility(View.GONE);
                tv_nomedia.setVisibility(View.VISIBLE);
            }

        }
    };

    @Override
    public View initView() {
        Log.e("VideoPager","==加载界面==");
//        View inflateView = View.inflate(context, R.layout.net_video_pager, null);

        View inflateView = View.inflate(context, R.layout.video_pager, null);
        tv_nomedia=inflateView.findViewById(R.id.tv_nomedia);
        pb_loding=inflateView.findViewById(R.id.pb_loding);
        lv_video_pager=inflateView.findViewById(R.id.lv_video_pager);

        //设置点击事件
        lv_video_pager.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                MediaItem mediaItem = mediaItemList.get(position);

                //调用系统播放器   隐身意图，通过匹配调用合适的Activity
//                Intent intent = new Intent();
//                intent.setDataAndType(Uri.parse(mediaItem.getData()),"video/*");
//                context.startActivity(intent);

                //调用自己手写的播放器  这里是把一个视频地址传到SystemVideoPlayer
//                Intent intent = new Intent(context, SystemVideoPlayer.class);
//                intent.setDataAndType(Uri.parse(mediaItem.getData()),"video/*");//匹配类型
//                context.startActivity(intent);

                //传的是列表
                Intent intent = new Intent(context, SystemVideoPlayer.class);
                Bundle bundle=new Bundle();
                //在Activity中传递对象，被传的对象需要序列化（不然会报错 java.lang.RuntimeException: Parcel: unable to marshal value MediaItem.....）
                //自定义的对象通过bundle传递，前提条件是自定义类要实现Serializable或Parcelable两接口之一
                bundle.putSerializable("videolist", (Serializable) mediaItemList);//mediaItemList的成员需要序列化
                //bundle与意图关联
                intent.putExtras(bundle);
                //把点击的Item索引传到SystemVideoPlayer
                intent.putExtra("position",position);
                context.startActivity(intent);
            }
        });
        return inflateView;
    }

    @Override
    public void initData() {
        super.initData();
        Log.e("VideoPager","==加载数据==");
        utils = new Utils();
        getData();
    }

    private void getData() {
        //加载数据耗时，需要在分线程加载数据
        new Thread(){
            @Override
            public void run() {
                super.run();
                SystemClock.sleep(1000);

                //内容提供者？？？
                ContentResolver contentResolver = context.getContentResolver();

                Uri uri= MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

                String[] projection={
                        MediaStore.Video.Media.DISPLAY_NAME,
                        MediaStore.Video.Media.DURATION,
                        MediaStore.Video.Media.SIZE,
                        MediaStore.Video.Media.DATA//????
                };
                Cursor query = contentResolver.query(uri, projection, null, null, null);
                if(query!=null){
                    while (query.moveToNext()){
                        String name = query.getString(0);
                        long duration = query.getLong(1);
                        long size = query.getLong(2);
                        String data = query.getString(3);
////                        data="http://vfx.mtime.cn/Video/2016/05/20/mp4/160520104417280900_480.mp4";
//                        data="http://cctvcncw.v.wscdns.com/live/cctv1_1_2000/playlist.m3u8?wsHost=cctv.schls.8686c.com&wsApp=HLS";
//                        data="http://cctvaliw.v.myalicdn.com/live/cctv6_1td.m3u8";
                        mediaItemList.add(new MediaItem(name,duration,size,data));


                    }
                    query.close();
                }
                handler.sendEmptyMessage(0);
            }
        }.start();
    }


    class VideoAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mediaItemList.size();
        }

        @Override
        public Object getItem(int i) {
            return mediaItemList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if(view==null){
                view = View.inflate(context, R.layout.video_item, null);
                viewHolder=new ViewHolder();
                viewHolder.tv_name=view.findViewById(R.id.tv_name);
                viewHolder.tv_time=view.findViewById(R.id.tv_time);
                viewHolder.tv_size=view.findViewById(R.id.tv_size);
                view.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) view.getTag();
            }
            MediaItem mediaItem = mediaItemList.get(i);
            viewHolder.tv_name.setText(mediaItem.getName());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                viewHolder.tv_time.setText(utils.stringForTime(Math.toIntExact(mediaItem.getDuration())));
            }
//            viewHolder.tv_time.setText(utils.stringForTime((int)mediaItem.getDuration()));????
            viewHolder.tv_size.setText(Formatter.formatFileSize(context,mediaItem.getSize()));
            return view;
        }

        class ViewHolder{
            private TextView tv_name,tv_time,tv_size;
        }
    }
}
