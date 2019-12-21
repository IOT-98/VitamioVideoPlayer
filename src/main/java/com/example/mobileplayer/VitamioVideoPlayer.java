package com.example.mobileplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.mobileplayer.domain.MediaItem;
import com.example.mobileplayer.uitls.NetUriUtils;
import com.example.mobileplayer.uitls.Utils;
import com.example.mobileplayer.view.VitamioVideoView;


import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.VideoView;

/**
 * @ProjectName: AS_AndroidItgiuguMyProject$
 * @Package: com.example.mobileplayer$
 * @ClassName: SystemVideoPlayer$
 * @Author: 周炜
 * @CreateDate: 2019/12/13$ 11:12$
 * @Version: 1.0
 * @Description: java类作用描述
 */
public class VitamioVideoPlayer extends Activity implements View.OnClickListener {

    private VitamioVideoView video_play;
    private LinearLayout llTop;
    private TextView tvName;
    private ImageView ivBattery;
    private TextView tvTime;
    private Button btnVoice;
    private SeekBar seekbarVoice;
    private Button btnSwitchPlayer;
    private LinearLayout llBottom;
    private TextView tvCurrentTime;
    private SeekBar seekbarVideo;
    private TextView tvDuration;
    private Button btnVideoExit;
    private Button btnVideoPre;
    private Button btnVideoStartPause;
    private Button btnVideoNext;
    private Button btnVideoSwitchScreen;

    private RelativeLayout rl_loding;
    private LinearLayout ll_buffer_loding;

    private TextView tv_buffer_netspeed;
    private TextView tv_play_netspeed;

    private Utils utils;
    private NetUriUtils netUriUtils;

    private BatteryReceiver receiver;

    private Uri uri;

    private List<MediaItem> mediaItemList;
    private int position;

    //1.定义手势识别器
    private GestureDetector gestureDetector;

    //每一秒更新控制面板SeekBar
    private static final int VIDEO_PLAY_UPDATAVIEW=0;
    //4秒后隐藏控制面板
    private static final int HIDE_MEDIACONTROLLER=1;

    //默认播放
    private static final int DEFAULT_SCREEWN=2;

    //全屏播放
    private static final int FULL_SCREEN=3;

    //每秒更新显示网速
    private static final int SHOW_SPEED =4 ;

    //屏幕的宽
    private int screenWidth;
    //屏幕的高
    private int screnHeigth;

    //是否全屏（开始是默认大小）
    private  boolean isFullSeceen=false;

    //上次的播放进度
    private int prePosition;


    private void findViews() {
        video_play=findViewById(R.id.vitamiovideo_play);
        llTop = (LinearLayout)findViewById( R.id.ll_top );
        tvName = (TextView)findViewById( R.id.tv_name );
        ivBattery = (ImageView)findViewById( R.id.iv_battery );
        tvTime = (TextView)findViewById( R.id.tv_time );
        btnVoice = (Button)findViewById( R.id.btn_voice );
        seekbarVoice = (SeekBar)findViewById( R.id.seekbar_voice );
        btnSwitchPlayer = (Button)findViewById( R.id.btn_switch_player );
        llBottom = (LinearLayout)findViewById( R.id.ll_bottom );
        tvCurrentTime = (TextView)findViewById( R.id.tv_current_time );
        seekbarVideo = (SeekBar)findViewById( R.id.seekbar_video );
        tvDuration = (TextView)findViewById( R.id.tv_duration );
        btnVideoExit = (Button)findViewById( R.id.btn_video_exit );
        btnVideoPre = (Button)findViewById( R.id.btn_video_pre );
        btnVideoStartPause = (Button)findViewById( R.id.btn_video_start_pause );
        btnVideoNext = (Button)findViewById( R.id.btn_video_next );
        btnVideoSwitchScreen = (Button)findViewById( R.id.btn_video_switch_screen );

        rl_loding=findViewById(R.id.rl_loding);
        ll_buffer_loding=findViewById(R.id.ll_buffer_loding);

        tv_buffer_netspeed=findViewById(R.id.tv_buffer_netspeed);
        tv_play_netspeed=findViewById(R.id.tv_play_netspeed);


        btnVoice.setOnClickListener( this );
        btnSwitchPlayer.setOnClickListener( this );
        btnVideoExit.setOnClickListener( this );
        btnVideoPre.setOnClickListener( this );
        btnVideoStartPause.setOnClickListener( this );
        btnVideoNext.setOnClickListener( this );
        btnVideoSwitchScreen.setOnClickListener( this );
    }


    private Handler handler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case VIDEO_PLAY_UPDATAVIEW:
                    //得到当前播放进度
                    int currentPosition = (int) video_play.getCurrentPosition();

                    //使用long（ms）转为String（00:00:00）工具类，设置控制面板中的视频当前播放时长
                    tvCurrentTime.setText(utils.stringForTime(currentPosition));

                    //设置视频SeekBar的进度（播放状态SeekBar：因为控制面板有两个控制面板，视频视频SeekBar和音量SeeBar，这里以此区分）
                    seekbarVideo.setProgress(currentPosition);

                    //更新系统时间
                    tvTime.setText(getSystemTime());

                    //设置SeekBar缓冲效果(这是一个设置缓冲的算法)
                    if(isNetUri){
                        //缓冲值
                        int bufferPercentage = video_play.getBufferPercentage();//0~100
                        //要缓冲多少
                        int totalBufferPercentage = seekbarVideo.getMax()*bufferPercentage;
                        //得到缓存的进度
                        int secondaryProgress = totalBufferPercentage / 100;
                        seekbarVideo.setSecondaryProgress(secondaryProgress);
                    }else{
                        seekbarVideo.setSecondaryProgress(0);
                    }


                    //卡顿第二种监听方案：
                    // 播放器是每秒更新一次播放进度，可以通过本次播放的进度与上次播放的进度的差值与500相比，小于500则说明卡了
                    //说明：这种在直播时没用，因为直播进度没变(开始时间，结束时间都为0)  m3u8没有用,得不到当前进度(0)
                    int buffer_num = currentPosition - prePosition;
                    if(video_play.isPlaying()){
                        if(buffer_num<500){
                            ll_buffer_loding.setVisibility(View.VISIBLE);
                        }else{
                            ll_buffer_loding.setVisibility(View.GONE);
                        }
                    }
                    prePosition=currentPosition;

                    handler.removeMessages(VIDEO_PLAY_UPDATAVIEW);//要设置移除发送消息？？？
                    handler.sendEmptyMessageDelayed(VIDEO_PLAY_UPDATAVIEW,1000);
                    break;

                    //隐藏控制面板
                case HIDE_MEDIACONTROLLER:
                    hideMediaController();
                    break;

                    //每秒更新网速
                case SHOW_SPEED:
                    String netSpeed = netUriUtils.getNetSpeed(VitamioVideoPlayer.this);
                    tv_buffer_netspeed.setText(netSpeed);
                    tv_play_netspeed.setText(netSpeed);
                    handler.sendEmptyMessageDelayed(SHOW_SPEED,1000);
                    break;
            }
        }
    };


    //得到系统时间并转换为00:00:00格式
    private String getSystemTime() {
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("HH:mm:ss");
        return simpleDateFormat.format(new Date());
    }


    //是否静音
    private boolean isMute=false;
    @Override
    public void onClick(View v) {
        if ( v == btnVoice ) {
            //静音或回到原始声量
            isMute=!isMute;
            updataVolume(currentVolume);
        } else if ( v == btnSwitchPlayer ) {
            //切换播放器
            showSwichPlayerDialog();
        } else if ( v == btnVideoExit ) {
            //退出播放
            finish();
        } else if ( v == btnVideoPre ) {
            //播放上一个视频
            setPlayPre();
        } else if ( v == btnVideoStartPause ) {
            //暂停或停止播放
            startAndpause();
        } else if ( v == btnVideoNext ) {
            //播放下一个视频
            setPlayNext();
        } else if ( v == btnVideoSwitchScreen ) {
            //默认或全屏播放
            if(isFullSeceen){
                setVideoType(DEFAULT_SCREEWN);
            }else{
                setVideoType(FULL_SCREEN);
            }
        }

        //在点击控制栏的按钮时移除隐藏状态栏的消息，移除后再次发送一个5秒后隐藏状态栏的消息
        handler.removeMessages(HIDE_MEDIACONTROLLER);
        handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER,5000);
    }

    //切换播放器
    private void showSwichPlayerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("切换到小影系统播放器");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startSystemVideoPlayer();
            }
        });
        builder.setNegativeButton("取消",null);
        builder.show();
    }

    private void startSystemVideoPlayer() {
        //释放这个播放器的资源
        if(video_play!=null){
            video_play.stopPlayback();
        }

        Toast.makeText(this,"启用小影系统播放器播放",Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, SystemVideoPlayer.class);
        if(mediaItemList!=null&&mediaItemList.size()>0){
            //传的是列表
            Bundle bundle=new Bundle();
            //在Activity中传递对象，被传的对象需要序列化（不然会报错 java.lang.RuntimeException: Parcel: unable to marshal value MediaItem.....）
            //自定义的对象通过bundle传递，前提条件是自定义类要实现Serializable或Parcelable两接口之一
            bundle.putSerializable("videolist", (Serializable) mediaItemList);//mediaItemList的成员需要序列化
            //bundle与意图关联
            intent.putExtras(bundle);
            //把点击的Item索引传到SystemVideoPlayer
            intent.putExtra("position",position);
        }else if(uri!=null){
            intent.setData(uri);
        }
        startActivity(intent);
        //关闭SystemVideoPlayer
        finish();
    }

    private void updataVolume(int currentVolume) {
        if(isMute){
            am.setStreamVolume(AudioManager.STREAM_MUSIC,0,0);
            seekbarVoice.setProgress(0);
        }else{
            am.setStreamVolume(AudioManager.STREAM_MUSIC,currentVolume,0);
            seekbarVoice.setProgress(currentVolume);

        }
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vitamio_video_player);

        findViews();
        //初始化解码器
        Vitamio.isInitialized(getApplicationContext());

        initData();
        getData();
        setData();
        setListener();
    }

    private void startAndpause() {
        //设置暂停播放与开始播放,这里直接调用VideoView的开始与暂停方法，通过逻辑判断来实现并改变按钮的状态
        if(video_play.isPlaying()){
            video_play.pause();
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_play_selector);
        }else{
            video_play.start();
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_pause_selector);
        }
    }

    private void setPlayPre() {
        if(mediaItemList!=null&&mediaItemList.size()>0){
            //播放上一个
            position--;
            if(position>=0){
                MediaItem mediaItem = mediaItemList.get(position);
                video_play.setVideoPath(mediaItem.getData());
                tvName.setText(mediaItem.getName());

                //设置上一个和下一个按钮的状态
                setButtonState();

                //显示加载布局
                rl_loding.setVisibility(View.VISIBLE);

                if(position==0){
                    Toast.makeText(VitamioVideoPlayer.this,"第一个视频",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void setPlayNext() {
        if(mediaItemList!=null&&mediaItemList.size()>0){
            position++;
            if(position<mediaItemList.size()){
                MediaItem mediaItem = mediaItemList.get(position);
                video_play.setVideoPath(mediaItem.getData());
                tvName.setText(mediaItem.getName());

                //设置上一个和下一个按钮的状态
                setButtonState();

                //显示加载布局
                rl_loding.setVisibility(View.VISIBLE);

                if(position==mediaItemList.size()-1){
                    Toast.makeText(VitamioVideoPlayer.this,"最好一个视频",Toast.LENGTH_SHORT).show();
                }
            }else{ //播放最后一个退出播放器
                finish();
            }
        }else if(uri!=null){
            //退出播放器，这里传过来的只是一个地址，无法播放下一个
            finish();
        }
    }

    private void setButtonState() {
        if(mediaItemList!=null&&mediaItemList.size()>0){
            if(position==0){//为第一个设置按钮（播放上一个的）不可点击
                btnVideoPre.setEnabled(false);
                btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
            }else if(position==mediaItemList.size()-1){//为最后一个设置按钮（播放下一个的）不可点击
                btnVideoNext.setEnabled(false);
                btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
            }else{//其余的设置按钮可点击
                btnVideoNext.setEnabled(true);
                btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
                btnVideoPre.setEnabled(true);
                btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
            }
        }else if(uri!=null){
            //播放外界资源的时候，下一个视频按钮和上一个要变灰
            btnVideoPre.setEnabled(false);
            btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
            btnVideoNext.setEnabled(false);
            btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
        }else{
            Toast.makeText(VitamioVideoPlayer.this,"没有播放地址",Toast.LENGTH_SHORT).show();
        }
    }



    //实际视频的宽和高
    private  int videoWidth;
    private  int  videoHeight;
    private void setListener() {
        //当底层解码器准备好的时候，回调这个方法
        video_play.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                //监听拖动完成
                mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                    @Override
                    public void onSeekComplete(MediaPlayer mp) {
                        Toast.makeText(VitamioVideoPlayer.this,"拖动完成",Toast.LENGTH_SHORT).show();
                    }
                });

                //得到实际视频的宽和高
                 videoWidth = mediaPlayer.getVideoWidth();
                 videoHeight = mediaPlayer.getVideoHeight();

                //设置循环播放
                // mp.setLooping(true);

                //得到视频的总时长再与视频SeekBar的最大值相关联
                int duration = (int) mediaPlayer.getDuration();//或int duration1 = video_play.getDuration();

                //设置视频SeekBar的最大值
                seekbarVideo.setMax(duration);

                //使用long（ms）转为String（00:00:00）工具类，设置控制面板中的视频总时长
                tvDuration.setText(utils.stringForTime(duration));

                //发送消息，更新界面
                handler.sendEmptyMessage(VIDEO_PLAY_UPDATAVIEW);


                //隐藏视频加载布局
                rl_loding.setVisibility(View.GONE);

                //开始播放
                video_play.start();

//                video_play.setVideoSize(300,200);

                //默认大小播放
                setVideoType(DEFAULT_SCREEWN);
            }
        });


        //当播放出错的时候回调这个方法
        video_play.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                Toast.makeText(VitamioVideoPlayer.this,"万能播放器播放出错",Toast.LENGTH_SHORT).show();

                //出错时显示AlertDialog提示
                new AlertDialog
                        .Builder(VitamioVideoPlayer.this)
                        .setTitle("提示")
                        .setMessage("播放视频出错，请检查网络!")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //确定后就退出播放器
                                 finish();
                             }
                        }).setCancelable(false).show();

                return true;//返回true则说明自己已做处理。如返回flase这播放出错时将弹出系统错误提示
            }
        });
        //当播放完成的时候回调这个方法
        video_play.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Toast.makeText(VitamioVideoPlayer.this,"播放完成",Toast.LENGTH_SHORT).show();
                //播放完后关闭播放界面
                //finish();

                //播放完后播放下一个
                setPlayNext();
            }
        });


        //设置视频SeekBar进度改变的监听
        seekbarVideo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            //当进度跟新的时候回调这个方法
            //i:当前进度  b:是否是由用于引起
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b){//参数b:判断是否是用户的操作，这里进行判断是用户操作后再执行视频的进度跳转，否者将会一直在一个画面跳动
                    // 设置视频播放进度
                    video_play.seekTo(i);
                }
            }

            //当手触碰SeekBar的时候回调这个方法
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                    handler.removeMessages(HIDE_MEDIACONTROLLER);
            }

            //当手指离开SeeKbar的时候回调这个方法
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                    handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER,4000);
            }
        });

        //设置声音SeekBar改变的监听
        seekbarVoice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //当进度跟新的时候回调这个方法
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    updataVolumeProgress(progress);
                }
            }
            //当手触碰SeekBar的时候回调这个方法
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeMessages(HIDE_MEDIACONTROLLER);
            }
            //当手指离开SeeKbar的时候回调这个方法
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER,4000);
            }
        });

        //监听视频是否卡顿（播放网络视频）
        //这种方法不灵敏度,有时会监听不到
        //思考：播放器是每秒更新一次播放进度，可以通过本次播放的进度与上次播放的进度的差值与500相比，小于500则说明卡了
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            video_play.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    switch (what){
                        case MediaPlayer.MEDIA_INFO_BUFFERING_START://开始卡顿或者拖动进度卡顿
                            ll_buffer_loding.setVisibility(View.VISIBLE);
                            break;
                        case MediaPlayer.MEDIA_INFO_BUFFERING_END://卡顿结束或者拖动进度卡顿结束
                            ll_buffer_loding.setVisibility(View.GONE);
                            break;
                    }
                    return true;
                }
            });
        }


    }



    //声音SeekBar改变时更新Seekbar
    private void updataVolumeProgress(int progress) {
            //设置系统声音
            am.setStreamVolume(AudioManager.STREAM_MUSIC,progress,0);
            currentVolume=progress;
            //通过屏幕滑动改变音量值时，要在这设置SeekBar进度条，来改变SeekBar显示的进度
            seekbarVoice.setProgress(currentVolume);
    }

    //设置视频播放屏幕大小
    private void setVideoType(int type) {
        switch (type){
            case DEFAULT_SCREEWN://默认播放
                //实际视频的宽和高
                int mVideoWidth=videoWidth;
                int mVideoHeight=videoHeight;
                //要播放的视频的宽和高
                int width=screenWidth;
                int height=screnHeigth;
                // for compatibility, we adjust size based on aspect ratio
                // 这里是VideoView的源码，这里根据实际视频宽高度，根据纵横比调整大小，来兼容不同机型，达到适应的默认大小
                if ( mVideoWidth * height  < width * mVideoHeight ) {
                    width = height * mVideoWidth / mVideoHeight;
                } else if ( mVideoWidth * height  > width * mVideoHeight ) {
                    height = width * mVideoHeight / mVideoWidth;
                }
                video_play.setVideoSize(width,height);
                isFullSeceen = false;
                btnVideoSwitchScreen.setBackgroundResource(R.drawable.btn_switch_screen_full_selector);
                break;

            case FULL_SCREEN://全屏播放
                video_play.setVideoSize(screenWidth,screnHeigth);
                isFullSeceen = true;
                btnVideoSwitchScreen.setBackgroundResource(R.drawable.btn_switch_screen_default_selector);
                break;
        }
    }


    private Boolean isNetUri;
    private void
    setData() {
        if(mediaItemList!=null&&mediaItemList.size()>0){
            MediaItem mediaItem = mediaItemList.get(position);
            video_play.setVideoPath(mediaItem.getData());
            tvName.setText(mediaItem.getName());

            //判断是否网络资源
            isNetUri=netUriUtils.isNetUri(mediaItem.getData());

        }else if(uri!=null){
            //在本应用里，在ViewPager没设置intent.setDataAndType(Uri.parse(mediaItem.getData()),"video/*")这里不会执行到
            // （如果是从文件夹里调视频播放器执行这里）
            video_play.setVideoURI(uri);
            tvName.setText(uri.toString());//???
            Log.e("uri",uri.toString());//content://media/external/video/media/1416598

            isNetUri=netUriUtils.isNetUri(uri.toString());
        }

        //点击ListView的Item进入SystemVideoPlayer设置数据播放视频时就要判断按钮的状态
        setButtonState();

        //播放时隐藏控制面板
        hideMediaController();

        //设置播放时不锁屏，VideoView有这个方法
        video_play.setKeepScreenOn(true);

        //在Activity中设置不锁屏的方法
//      getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //发送消息更新网速
        handler.sendEmptyMessage(SHOW_SPEED);
    }

    private void getData() {
        /*1.这里得到的是VideoPager传过来的一个地址
          只有在ViewPager设置 intent.setDataAndType(Uri.parse(mediaItem.getData()),"video/*");这里才能得到uri

          2.或者是在手机的文件夹里需要播放一个视频，在这里getIntent().getData()，再配置后就可以调起这个视频播放器播放视频
        */
        uri = getIntent().getData();

        //得到从VideoPager传过来播放列表
        mediaItemList = (List<MediaItem>) getIntent().getSerializableExtra("videolist");
        //得到从VideoPager传过来的点击ListView中Item的索引
        position = getIntent().getIntExtra("position", 0);
    }



    private AudioManager am;
    private  int currentVolume;
    private  int maxVolume;

    private void initData() {
        //格式化时间的工具类
        utils=new Utils();

        //判断是否网络资源的工具类
         netUriUtils = new NetUriUtils();

        //用AudioManager调声音,实例化AudioManager
         am = (AudioManager) getSystemService(AUDIO_SERVICE);
//        得到现在的声音
         currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
         //得到最大的声音值，并与声音SeekBar关联
         maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
         seekbarVoice.setMax(maxVolume);
         //设置声音SeekBar现在的进度
         seekbarVoice.setProgress(currentVolume);

        //得到屏幕的宽和高
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth=displayMetrics.widthPixels;
        screnHeigth=displayMetrics.heightPixels;


        //2.实例化手势识别器
        gestureDetector=new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){
            //长按回调这个方法
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                startAndpause();
            }

            //双击回调这个方法
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if(isFullSeceen){
                    setVideoType(DEFAULT_SCREEWN);
                }else{
                    setVideoType(FULL_SCREEN);
                }
                return super.onDoubleTap(e);

            }

            //单击回调这个方法
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if(isShowMediaController){
                    //隐藏控制栏
                    hideMediaController();

                    //手动隐藏后要移除定时的隐藏消息，不然用户再单击一次，显示了控制面板，上次定时隐藏控制面板的时间到了，显示控制面板后就会出现马上隐藏控制面板的情况
                    handler.removeMessages(HIDE_MEDIACONTROLLER);
                }else{
                    //显示控制栏
                    showMediaController();
                    handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER,4000);
                }
                return super.onSingleTapConfirmed(e);
            }
        });


        //注册监听电量的广播
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        receiver = new BatteryReceiver();
        registerReceiver(receiver,intentFilter);

    }




    //是否显示控制面板
    private Boolean isShowMediaController=false;
    //隐藏控制面板
    private void hideMediaController() {
        llBottom.setVisibility(View.GONE);
        llTop.setVisibility(View.GONE);
        isShowMediaController=false;
    }
    //显示控制面板
    private void showMediaController() {
        llBottom.setVisibility(View.VISIBLE);
        llTop.setVisibility(View.VISIBLE);
        isShowMediaController=true;
    }

    class BatteryReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            //这里在主线程
            //电量值：0~100
            int level = intent.getIntExtra("level", 0);
            //根据电量值设置控制栏电量状态
            setBattery(level);
        }
    }

    private void setBattery(int level) {
        if(level<=0){
            ivBattery.setImageResource(R.drawable.ic_battery_0);
        }else if(level <=10){
            ivBattery.setImageResource(R.drawable.ic_battery_10);
        }else if(level <=20){
            ivBattery.setImageResource(R.drawable.ic_battery_20);
        }else if(level <=40){
            ivBattery.setImageResource(R.drawable.ic_battery_40);
        }else if(level <=60){
            ivBattery.setImageResource(R.drawable.ic_battery_60);
        }else if(level <=80){
            ivBattery.setImageResource(R.drawable.ic_battery_80);
        }else if(level <=100){
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        }else{
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        }
    }


    private int mVol;
    private float startY;
    private int touchRang;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //3.把事件给手势识别器解析（不解析，无法执行手势识别器中的逻辑）
        gestureDetector.onTouchEvent(event);

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN://手指按下屏幕
                //1.按下的时候记录初始值
                 startY = event.getY();
                //得到屏幕可划动的总距离
                 touchRang = Math.min(screnHeigth, screnHeigth);//touchRang=screnHeigth,因为是横屏播放
                //得到当前音量值
                 mVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                 handler.removeMessages(HIDE_MEDIACONTROLLER);
                break;

            case MotionEvent.ACTION_MOVE://手指在屏幕上移动
                //得到新的坐标
                float endY = event.getY();

                //得到偏移量
                float distanceY = startY - endY;

                //屏幕滑动的距离：总距离=改变的声音：最大音量
                float changVolume=(distanceY/touchRang)*maxVolume;//改变的声音

                //最终的声音=原来的声音+改变的声音
                float volume=Math.min(Math.max(mVol+changVolume,0),maxVolume);

                // float volume=Math.min(Math.max(mVol+changVolume,0),maxVolume)相当于????
//                float undeterminedVolume=mVol+changVolume;//待定音量值
//                if(volume>0&&undeterminedVolume<maxVolume){
//                    volume=mVol+changVolume;//最终的声音
//                }


                if(changVolume!=0){//如改变的音量值不为0.则更新SeekBar
                    updataVolumeProgress((int) volume);
                }

                break;

            case MotionEvent.ACTION_UP://手指离开屏幕
                handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER,4000);
                break;
        }
        return super.onTouchEvent(event);
    }






    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN){
            if(currentVolume>=0){
                //这里要加判断，不然会出现到达静音时还一直按物理减音键,当要加声音时就要按多次加音键（达到静音后按了多少次减音键，就要按回多少次加音键），才能加音
                currentVolume--;
                updataVolumeProgress(currentVolume);
            }
            handler.removeMessages(HIDE_MEDIACONTROLLER);
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER,4000);
            //返回true不让系统音量状态栏                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              弹出来
            return true;
        }else if(keyCode==KeyEvent.KEYCODE_VOLUME_UP){
            if(currentVolume<=maxVolume){
                currentVolume++;
                updataVolumeProgress(currentVolume);
            }
            handler.removeMessages(HIDE_MEDIACONTROLLER);
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER,4000);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
//        这里要注意释放资源的顺序super.onDestroy()是销毁界面，这样在这个类中的所有资源都会释放，
//        先执行 super.onDestroy()在去执行其他资源的释放可能会出现空指针异常
        if(receiver!=null){
            unregisterReceiver(receiver);
            receiver=null;
        }

        //释放这个播放器的资源
        if(video_play!=null){
            video_play.stopPlayback();
        }

        super.onDestroy();
    }
}
