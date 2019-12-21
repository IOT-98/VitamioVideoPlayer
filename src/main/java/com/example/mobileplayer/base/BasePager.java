package com.example.mobileplayer.base;

import android.content.Context;
import android.view.View;
import android.widget.BaseAdapter;

/**
 * @ProjectName: AS_AndroidItgiuguMyProject$
 * @Package: com.example.mobileplayer.base$
 * @ClassName: BasePager$
 * @Author: 周炜
 * @CreateDate: 2019/12/12$ 9:15$
 * @Version: 1.0
 * @Description: java类作用描述
 */
public abstract class BasePager {
    //上下文
    public Context context;
    //各个子页面实例化的结果
    public View rootView;

    //判断是否已初始化数据
    public boolean isInitData=false;

    public BasePager(Context context){
        this.context=context;
        rootView=initView();//调用的是子页面的initView（）;
    }

    //强制子页面实现该方法，实现特定的效果
    public abstract View initView();

    //当子页面需要初始化数据的时候，重写该方法，用于请求数据或者显示数据
    public void initData(){

    }
}
