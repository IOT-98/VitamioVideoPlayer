package com.example.mobileplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.mobileplayer.R;

/**
 * @ProjectName: AS_AndroidItgiuguMyProject$
 * @Package: com.example.mobileplayer.view$
 * @ClassName: TitleBar$
 * @Author: 周炜
 * @CreateDate: 2019/12/12$ 20:33$
 * @Version: 1.0
 * @Description: java类作用描述
 */
public class TitleBar extends LinearLayout implements View.OnClickListener {
    private View iv_history;
    private View tv_seache;

    private Context context;

   /* Android.View.InflateException: Binary XML File Line #异常的解决(没写这个构造器报错)

    https://www.cnblogs.com/awkflf11/p/5362927.html
    https://blog.csdn.net/hahaha_1107/article/details/78926814
     */
    public TitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
    }

    @Override
    protected void onFinishInflate() {
       super.onFinishInflate();
        tv_seache = getChildAt(1);
        tv_seache = getChildAt(2);

        iv_history=findViewById(R.id. iv_history);
       tv_seache=findViewById(R.id.tv_seache);

        iv_history.setOnClickListener(this);
        tv_seache.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.iv_history:
                Toast.makeText(context,"历史",Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_seache:
                Toast.makeText(context,"搜索",Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
