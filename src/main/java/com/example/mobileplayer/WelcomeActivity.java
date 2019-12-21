package com.example.mobileplayer;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;

//欢迎界面
public class WelcomeActivity extends AppCompatActivity {
    private Handler handler=new Handler();
    Boolean oneStartMainActivity=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("WelcomeActivity","===onCreate===");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ActionBar supportActionBar = getSupportActionBar();
        supportActionBar.hide();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("WelcomeActivity","===postDelayed===");
                startMainActivity();
            }
        },1000);
    }

    private void startMainActivity() {
        if(!oneStartMainActivity){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            oneStartMainActivity=true;
            finish();
        }
    }

    //为了提供良好的用户体验，在原基础上的2秒后进入主界面，增加点击欢迎界面立即进入主界面（无需等待）。
    /*
    问题：增加了点击欢迎界面立即进入主界面会出现一个问题，即进入欢迎界面时，
    你点击界面此时立即进入主界面，然后过两秒后又会执行handler的定时任务再次进入主界面，
    这样就创建了两个实例（点击多次还会创建多个实例）。

    解决问题思路：

    1.单例模式（栈内单例模式）：在AndroidManifest.xml进行配置指定属性android:launchMode=“singleleTask”
    https://blog.csdn.net/sinat_33150417/article/details/78721056

    2.在代码中解决，即进入主界面后使其他的操作就不执行


    通过上面的方法解决了启动主界面会创建多个实例的问题，
   运行程序无论采用哪种方式进入主界面都只会创建一个实例，即创建一个界面。

   问题：由于用户的一些操作，进入欢迎界面时用户通过点击欢迎界面进入主界面
   ，然后快速退出主界面，但是当达到了定时所设置的时间，又会再次启动主界面。

   原因：handler延时2秒执行startMainActivity()，此时我们是通过点击欢迎界面进入主界面的，
   然后快速退出主界面，此时界面虽然销毁了，但handler所执行的定时任务依然还在运行，即到达时间就会执行startMainActivity()，
   启动主界面。

   解决问题思路：在WelcomeActivity的onDestroy方法中清空当前Handler队列所有消息。（避免内存泄漏）
    handler.removeCallbacksAndMessages(null);

     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        startMainActivity();
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("WelcomeActivity","===onDestroy===");
        handler.removeCallbacksAndMessages(null);
    }
}





//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        Log.e("WelcomeActivity","===onRestart===");
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        Log.e("WelcomeActivity","===onStart===");
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        Log.e("WelcomeActivity","===onResume===");
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        Log.e("WelcomeActivity","===onPause===");
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        Log.e("WelcomeActivity","===onStop===");
//    }
