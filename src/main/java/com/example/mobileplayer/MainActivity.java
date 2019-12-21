package com.example.mobileplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.example.mobileplayer.base.BasePager;
import com.example.mobileplayer.pager.AudioPager;
import com.example.mobileplayer.pager.LiveTVPager;
import com.example.mobileplayer.pager.NetAudioPager;
import com.example.mobileplayer.pager.NetVideoPager;
import com.example.mobileplayer.pager.VideoPager;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity {

    private RadioGroup radioGroup;
    private ArrayList<BasePager> basePagerArrayList=new ArrayList<>();

    //页面对应的位置
    private int position;

    //Android 6.0以上动态申请文件读写权限(A. B. C.)
    //A.读与写的权限先定义到静态字符数组中：
    //读写权限
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        radioGroup=findViewById(R.id.rg_main);
        basePagerArrayList.add(new VideoPager(this));
        basePagerArrayList.add(new NetVideoPager(this));
        basePagerArrayList.add(new LiveTVPager(this));


        //B.首先判断当前系统是否是Android6.0(对应API 23)以及以上，
        // 如果是则判断是否含有了写文件的权限，如果没有则调用动态申请权限的代码，
        // ActivityCompat.requestPermission方法的第一个参数是目标Activity,填写this即可，第二个参数是String[]字符数组类型的权限集，第三个即请求码：
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }
        }

        radioGroup.setOnCheckedChangeListener(new MyOnCheckedChangeListener());
        radioGroup.check(R.id.rb_video);
    }

    //C.回调函数，申请权限后回调onRequestPermissionResult函数，
    // 第一个参数为请求码，第二个参数是刚刚请求的权限集，第三个参数是请求结果，0表示授权成功，-1表示授权失败：
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                Log.e("EntryInterfaceActivity", "申请的权限为：" + permissions[i] + ",申请结果：" + grantResults[i]);
            }
        }
    }

    class MyOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            switch (i){
                default://默认选中
                    position=0;
                    break;
                case R.id.rb_net_video:
                    position=1;
                    break;
                case R.id.rb_net_tv:
                    position=2;
                    break;
            }
            setFragment();
        }
    }

    private void setFragment() {
        //得到FragmentManager
        FragmentManager supportFragmentManager = getSupportFragmentManager();//v4包里的
        //开启事务
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        //替换
        //参一：存放fragment的布局的id;
        fragmentTransaction.replace(R.id.fl_main,new ReplaceFragment(getBasePager()));
        //提交事务
        fragmentTransaction.commit();
    }

    public static class ReplaceFragment extends Fragment{
        private BasePager basePager;
//        报错： Unable to instantiate fragment com.example.mobileplayer.MainActivity$ReplaceFragment: could not find Fragment constructor

        /*
        注意到Unable to instantiate fragment ..... could not find Fragment constructor，
        是说fragment不能被是实例化，那么问题来了，为什么不能被实例化呢，
        首先，fragment什么时候会被实例化，当然是我们在代码中去add或者replace这个fragment的时候，
        除此以为还有什么地方去实例化fragment吗，有，宿主Activity被销毁后重新恢复的时候，
        它的fragment也会被恢复，进行重新实例化
        链接：https://www.jianshu.com/p/e8c831e9ae73
         */

        //添加这个无参还解决了一个问题，当我从SystemVideoPlayer播放器跳转到VitamioVideoPlayer播放器,
        //VitamioVideoPlayer播放器闪退，原本是会直接退出程序，添加这个无参后只是退出了这个VitamioVideoPlayer播放器.
        public  ReplaceFragment(){

        }

        public ReplaceFragment(BasePager basePager){
                this.basePager=basePager;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
              if(basePager!=null){
                  //返回各子页面的视图
                  return basePager.rootView;
              }
                return null;
            }
        }


        /*
        这样写报错：
        Caused by: java.lang.IllegalStateException: Fragment null must be a public static class to be  properly recreated from instance state.
         在这里碎片（Fragment）必须是公共静态类，才能从实例状态重新创建。
         */
//    private void setFragment() {
//        FragmentManager supportFragmentManager = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
//        fragmentTransaction.replace(R.id.fl_main,new Fragment(){
//            @Nullable
//            @Override
//            public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//              BasePager basePager=getBasePager();
//              if(basePager!=null){
//                  return basePager.rootView;
//              }
//                return null;
//            }
//        });
//        fragmentTransaction.commit();
//    }

    private BasePager getBasePager() {
        BasePager basePager = basePagerArrayList.get(position);
        if(basePager!=null&&!basePager.isInitData){
            //请求数据或者显示数据(这里是执行子类的方法)
            basePager.initData();
            basePager.isInitData=true;
        }
        return basePager;
    }

}
