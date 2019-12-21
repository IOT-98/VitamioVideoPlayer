package com.example.mobileplayer.pager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.mobileplayer.MainActivity;
import com.example.mobileplayer.R;
import com.example.mobileplayer.SystemVideoPlayer;
import com.example.mobileplayer.base.BasePager;
import com.example.mobileplayer.domain.MediaItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.common.util.LogUtil;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.Serializable;
import java.nio.DoubleBuffer;
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
public class NetVideoPager extends BasePager  {

    private TextView tv_nomedia;
    private ProgressBar pb_loding;
    private ListView lv_video_pager;

    private List<MediaItem> mediaItemList=new ArrayList<>();

    public NetVideoPager(Context context) {
        //子类中所有的构造器默认都会访问父类中的空参数的构造器
        super(context);
    }

    @Override
    public View initView() {
        Log.e("NetVideoPager","==加载界面==");
        View inflateView = View.inflate(context, R.layout.net_video_pager, null);
        tv_nomedia=inflateView.findViewById(R.id.tv_nomedia);
        pb_loding=inflateView.findViewById(R.id.pb_loding);
        lv_video_pager=inflateView.findViewById(R.id.lv_video_pager);

        //设置点击事件
        lv_video_pager.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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

    class NetVideoAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mediaItemList.size();
        }
        @Override
        public Object getItem(int position) {
            return null;
        }
        @Override
        public long getItemId(int position) {
            return 0;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder ;
            if(convertView==null){
                convertView=View.inflate(context,R.layout.net_video_item,null);
                viewHolder=new ViewHolder();
                viewHolder.iv_icon=convertView.findViewById(R.id.iv_icon);
                viewHolder.tv_video_name=convertView.findViewById(R.id.tv_video_name);
                viewHolder.tv_video_title=convertView.findViewById(R.id.tv_video_title);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }
            MediaItem mediaItem = mediaItemList.get(position);
            viewHolder.tv_video_name.setText(mediaItem.getName());
            viewHolder.tv_video_title.setText(mediaItem.getVideoTitle());

            //使用XUtils3请求图片
            //思考：这里在JSON中得到的是一个图片地址，这里直接可以把这个布局绑定一个图片
//            x.image().bind(viewHolder.iv_icon,mediaItem.getCoverImg());

            //使用Glide请求图片
            Glide.with(context).load(mediaItem.getCoverImg())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)//图片的缓存
                    .placeholder(R.drawable.my_icon)//加载过程中的图片
                    .error(R.drawable.my_icon)//加载失败显示的图片
                    .into(viewHolder.iv_icon);//请求成功后把图片设置到控件上

            return convertView;
        }
    }

    static class ViewHolder{
        private ImageView iv_icon;
        private TextView tv_video_name,tv_video_title;
    }


    @Override
    public void initData() {
        super.initData();
        Log.e("NetVideoPager","==加载数据==");
        //联网请求数据
        getDataFromNet();
    }

    //联网请求数据
    private void getDataFromNet() {
        RequestParams params = new RequestParams("http://api.m.mtime.cn/PageSubArea/TrailerList.api");
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                LogUtil.e("联网请求成功=="+result);
                //解析数据
                processData(result);

                if(mediaItemList!=null&&mediaItemList.size()>0){
                    NetVideoAdapter netVideoAdapter = new NetVideoAdapter();
                    lv_video_pager.setAdapter(netVideoAdapter);
                }else{
                    tv_nomedia.setVisibility(View.VISIBLE);
                }
                pb_loding.setVisibility(View.GONE);

            }
            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.e("联网请求失败=="+isOnCallback);
            }
            @Override
            public void onCancelled(CancelledException cex) {
                LogUtil.e("onCancelled");
            }
            @Override
            public void onFinished() {
                LogUtil.e("onFinished");
            }
        });
    }

    //解析数据
    private void processData(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray trailersArray = jsonObject.optJSONArray("trailers");
            for (int i=0;i<trailersArray.length();i++){
                MediaItem mediaItem = new MediaItem();
                JSONObject jsonObject1 = trailersArray.optJSONObject(i);
                String coverImg = jsonObject1.optString("coverImg");
                mediaItem.setCoverImg(coverImg);
                String hightUrl = jsonObject1.optString("hightUrl");
                mediaItem.setData(hightUrl);
                String movieName = jsonObject1.optString("movieName");
                mediaItem.setName(movieName);
                String videoTitle = jsonObject1.optString("videoTitle");
                mediaItem.setVideoTitle(videoTitle);
                mediaItemList.add(mediaItem);
            }
            LogUtil.e(mediaItemList.size()+"");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
