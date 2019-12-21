package com.example.mobileplayer.pager;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.mobileplayer.base.BasePager;
/**
 * @ProjectName: AS_AndroidItgiuguMyProject$
 * @Package: com.example.mobileplayer.pager$
 * @ClassName: VideoPager$
 * @Author: 周炜
 * @CreateDate: 2019/12/12$ 9:38$
 * @Version: 1.0
 * @Description: java类作用描述
 */
public class AudioPager extends BasePager {
    public AudioPager(Context context) {
        //子类中所有的构造器默认都会访问父类中的空参数的构造器
        super(context);
    }

    private TextView textView;
    @Override
    public View initView() {
        Log.e("AudioPager","==加载界面==");
        textView = new TextView(context);
        return textView;
    }

    @Override
    public void initData() {
        super.initData();
        Log.e("AudioPager","==加载数据==");
        textView.setText("本地音频");
    }
}
