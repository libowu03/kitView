package com.kit.ActivityUiFrame;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kit.guide.R;
import com.kit.utils.GetActionBarHeight;

import java.util.ArrayList;
import java.util.List;

/**
 * 主界面框架view
 * @author libowu
 * @date 2019/12/08
 */
public class MainUIFrameView extends LinearLayout implements View.OnClickListener {
    private Activity activity;
    private DrawerLayout mainUiBox;
    private LinearLayout toolbar;
    private ImageView mainuiOpenMenu;
    private ImageView mainuiRightBtn;
    private TextView mainuiToolbarTitle;
    private List<TabContent> tabContentList;
    private LinearLayout defaultTab;


    public MainUIFrameView(Context context) {
        this(context, null);
    }

    public MainUIFrameView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MainUIFrameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getObject(context);
        initView();
        initListener();
    }


    /**
     * 获取非的实例
     * @param context
     */
    private void getObject(Context context) {
        activity = (Activity) context;
        tabContentList = new ArrayList<>();
    }


    /**
     * 初始化监听器
     */
    private void initListener() {
        //用户自定义状态了后这两个控件可能为空值
        if (mainuiOpenMenu != null){
            mainuiOpenMenu.setOnClickListener(this);
        }
        if (mainuiRightBtn != null){
            mainuiRightBtn.setOnClickListener(this);
        }

    }


    /**
     * 设置fragment
     * @param tabContents
     */
    public void setFragmentsList(final List<TabContent> tabContents){
        this.tabContentList = tabContents;
        for (final TabContent tab:tabContents){
            View tabView = LayoutInflater.from(getContext()).inflate(R.layout.mainui_view_tab,null);
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
            llp.height = LayoutParams.WRAP_CONTENT;
            llp.weight = 1;
            tabView.setLayoutParams(llp);
            TextView name = tabView.findViewById(R.id.mainuiTabName);
            ImageView tabIcon = tabView.findViewById(R.id.mainuiTabIcon);
            if (tab.getTabName() != null && !tab.getTabName().isEmpty()){
                name.setTag(tab.getTabName());
                name.setText(tab.getTabName());
            }else {
                name.setVisibility(GONE);
            }
            if (tab.getTabIcon() == null){
                if (tab.getTabIcon() instanceof String){
                    String tabIconUrl = (String) tab.getTabIcon();
                    if (tabIconUrl == null || tabIconUrl.isEmpty()){
                        tabIcon.setVisibility(GONE);
                    }
                }

            }
            tabView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                   Log.e("日志","执行点击："+tab.getFragment().getTag());
                }
            });
            defaultTab.addView(tabView);
        }
    }


/*    *//**
     * 添加fragment
     * @param tabContent
     * @return
     *//*
    public List<TabContent> addFragment(TabContent tabContent){
        tabContentList.add(tabContent);
        return tabContentList;
    }*/


    /**
     * 初始化界面
     */
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.mainui_view_main,this,true);

        mainUiBox = findViewById(R.id.mainUiBox);
        toolbar = findViewById(R.id.defaultToolbar);
        defaultTab = findViewById(R.id.defaultTab);

        //添加默认工具栏
        View defaultToolbar = LayoutInflater.from(getContext()).inflate(R.layout.mainui_default_main_toolbar,null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        lp.width = LayoutParams.MATCH_PARENT;
        lp.height = LayoutParams.WRAP_CONTENT;
        toolbar.addView(defaultToolbar,lp);

        //隐藏app默认状态栏
        if (activity instanceof AppCompatActivity){
            AppCompatActivity appCompatActivity = (AppCompatActivity) activity;
            if (appCompatActivity.getSupportActionBar() != null){
                appCompatActivity.getSupportActionBar().hide();
            }
            transparentStatusBar();
        }

        //默认顶部工具栏内部控件
        mainuiOpenMenu = findViewById(R.id.mainuiOpenMenu);
        mainuiRightBtn = findViewById(R.id.mainuiRightBtn);
        mainuiToolbarTitle = findViewById(R.id.mainuiToolbarTitle);
    }


    /**
     * 使状态栏透明
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void transparentStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = activity.getWindow();
                View decorView = window.getDecorView();
                //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
                int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                decorView.setSystemUiVisibility(option);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                //设置状态栏为透明，否则在部分手机上会呈现系统默认的浅灰色
                window.setStatusBarColor(Color.TRANSPARENT);
                //导航栏颜色也可以考虑设置为透明色
                window.setNavigationBarColor(Color.TRANSPARENT);
            } else {
                Window window = activity.getWindow();
                WindowManager.LayoutParams attributes = window.getAttributes();
                int flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
                int flagTranslucentNavigation = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
                attributes.flags |= flagTranslucentStatus;
                window.setAttributes(attributes);
            }
        }
        //设置工具栏距离状态栏的margin及设置根布局的背景色
        if (toolbar.getChildCount() > 0){
            View toolbarChild = toolbar.getChildAt(0);
            GetActionBarHeight.setMarginsNoneTop(toolbar);
            Drawable toolbarBackground = toolbarChild.getBackground();
            if (toolbarBackground instanceof ColorDrawable){
                mainUiBox.setBackgroundColor(((ColorDrawable)toolbarBackground).getColor());
            }
        }

    }


    /**
     * 设置顶部工具栏，这个方法的属性都是自适应的，要自定义属性，可以使用{@link MainUIFrameView#setToolbar(View, LayoutParams)}
     * @param view 工具栏
     */
    public void setToolbar(View view){
        if (view == null){
            return;
        }
        toolbar.removeAllViews();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        lp.width = LayoutParams.MATCH_PARENT;
        lp.height = LayoutParams.WRAP_CONTENT;
        toolbar.addView(view,lp);
    }


    /**
     * 设置顶部工具栏，这个方法需要自定义工具栏属性，自适应属性可以使用{@link MainUIFrameView#setToolbar(View)}
     * @param view 工具栏
     * @param layoutParams 工具栏属性
     */
    public void setToolbar(View view,LayoutParams layoutParams){
        if (view == null){
            return;
        }
        toolbar.removeAllViews();
        if (layoutParams == null){
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
            lp.width = LayoutParams.MATCH_PARENT;
            lp.height = LayoutParams.WRAP_CONTENT;
            layoutParams = lp;
        }
        toolbar.addView(view,layoutParams);
    }


    /**
     * 是否显示顶部工具栏
     * @param isShow
     */
    public void showToolbar(boolean isShow){
        if (isShow){
            toolbar.setVisibility(VISIBLE);
        }else {
            toolbar.setVisibility(GONE);
        }
    }


    /**
     * 获取侧滑动
     * @return
     */
    public DrawerLayout getDrawerLayout(){
        return mainUiBox;
    }


    @Override
    public void onClick(View view) {
        if (view == mainuiOpenMenu){
            mainUiBox.openDrawer(Gravity.START);
        }else if (view == mainuiRightBtn){
            Toast.makeText(getContext(),"点击右边按钮",Toast.LENGTH_SHORT).show();
        }
    }
}
