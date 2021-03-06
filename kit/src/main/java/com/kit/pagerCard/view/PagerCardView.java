package com.kit.pagerCard.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.kit.guide.R;
import com.kit.guide.utils.GuideViewUtils;
import com.kit.pagerCard.adapter.ViewPagerAdapter;
import com.kit.pagerCard.adapter.CardPagerAdapter;
import com.kit.pagerCard.bean.PagerCardAttribute;
import com.kit.pagerCard.bean.PagerCardBean;
import com.kit.utils.L;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * pagerCard的容器，容器里有一个viewpager和若干个textview组成，textview用于指示器的显示
 * @author libowu
 * @date 2019/09/27
 */
public class PagerCardView extends LinearLayout implements CardPagerAdapter.ClickPagerCardListener<PagerCardBean> {
    //指示器盒子
    protected LinearLayout indicator;
    //指示器盒子中的具体指示器
    protected List<View> indicatorList;
    //上一个指示器的索引号
    protected int oldIndicatorIndex;
    //内容监听器，监听pagercard中的各种事件，比如点击，页面切换等
    private PagerCardListener pagerCardListener;
    //自定义viewpager，自定义的目的是设置正确的高度
    private SelfViewPagerView pager2;
    //分页显示的内容集合
    private List<RecyclerView> fragments;
    //页面属性
    private PagerCardAttribute attribute;
    //指示器的高度，宽度
    private int indicatorWidth,indicatorHeight;
    //选中指示器的颜色和未选中指示器的颜色
    private int seIndicatorColor, unSeIndicatorColor;
    //是否显示指示器
    private boolean needIndicator;
    //每个页面内容的列数和行数
    private int colm,row;
    //是否启用页面无限循环，只有一页没作用
    private boolean enableInfinite;
    //定时器，用于自动也换界面
    private Timer timer;
    private Handler handler;
    //自动播放页面时的时间间隔
    private int playDuration;
    //是否停止页面播放
    private boolean isPausePlay;
    //页面数据源集合
    private List<PagerCardBean> pagerCardBeans;
    private AttributeSet attributeSet;


    public PagerCardView(Context context) {
        this(context,null);
    }

    public PagerCardView(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public PagerCardView(Context context, AttributeSet attributeSet, int defStyleAttr){
        super(context, attributeSet,0);

        LayoutInflater.from(context).inflate(R.layout.cardpager_view,this,true);
        pager2 = findViewById(R.id.pagerCard);
        indicator = findViewById(R.id.pagerCardIndicator);

        this.attributeSet = attributeSet;
        pagerCardBeans = new ArrayList<>();
        initAttr(context,attributeSet,defStyleAttr);
    }

    /**
     * 获取xml属性
     * @param context
     * @param attributeSet
     * @param defStyleAttr
     */
    @SuppressLint("ResourceAsColor")
    private void initAttr(Context context, AttributeSet attributeSet, int defStyleAttr) {
        TypedArray attr = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.PagerCardView, defStyleAttr, 0);
        //获取未选中指示器颜色
        unSeIndicatorColor = attr.getColor(R.styleable.PagerCardView_unSeIndicatorColor, getContext().getResources().getColor(R.color.unSeIndicatorColor));
        //获取选中指示器颜色
        seIndicatorColor = attr.getColor(R.styleable.PagerCardView_seIndicatorColor,getContext().getResources().getColor(R.color.seIndicatorColor));
        //指示器高度
        indicatorHeight = (int) attr.getDimension(R.styleable.PagerCardView_indicatorHeight,getContext().getResources().getDimension(R.dimen.indicatorHeight));
        //指示器宽度
        indicatorWidth = (int) attr.getDimension(R.styleable.PagerCardView_indicatorWidth,getContext().getResources().getDimension(R.dimen.indicatorWidth));
        //pagerCard标题颜色
        int pagerCardTextColor = attr.getColor(R.styleable.PagerCardView_pagerCardTextColor,Color.BLACK);
        //pagerCard标题大小
        int pagerCardTextSize = (int) attr.getDimension(R.styleable.PagerCardView_pagerCardTextSize,GuideViewUtils.dip2px(context,12));
        //pagerCard图片宽度
        int imgWidht = (int) attr.getDimension(R.styleable.PagerCardView_pagerCardImgWidth,-1);
        //pagerCard图片高度
        int imgHeight = (int) attr.getDimension(R.styleable.PagerCardView_pagerCardImgHeight,-1);
        //pagerCard空点宽度
        int redPointWidht = (int) attr.getDimension(R.styleable.PagerCardView_pagerCardRedPointWidth,getContext().getResources().getDimension(R.dimen.redPointWidht));
        //pagerCard红点高度
        int redPointHeight = (int) attr.getDimension(R.styleable.PagerCardView_pagerCardRedPointHeight,getContext().getResources().getDimension(R.dimen.redPointHeight));
        //pagerCard红点颜色背景
        int redPointBackground = attr.getColor(R.styleable.PagerCardView_pagerCardRedPointTextColor,Color.RED);
        //红点字体颜色
        int redPointTextSize = (int) attr.getDimension(R.styleable.PagerCardView_pagerCardRedPointTextSize,getContext().getResources().getDimension(R.dimen.redPointTextSize));
        //获取图片类型
        int imgType = attr.getInt(R.styleable.PagerCardView_pagerCardImgType,1);
        //获取圆角矩形的圆角弧度
        int imgCorner = (int) attr.getDimension(R.styleable.PagerCardView_imgCorner,getContext().getResources().getDimension(R.dimen.imgCorner));
        //是否需要显示指示器
        needIndicator = attr.getBoolean(R.styleable.PagerCardView_needIndicator,true);
        //是否启用上下滑动
        boolean canScrollVertically = attr.getBoolean(R.styleable.PagerCardView_canScrollVertically,false);
        //获取分割线宽度
        int itemDecorationWeight = (int) attr.getDimension(R.styleable.PagerCardView_itemDecorationWeight,-1);
        //获取分割线颜色
        int itemDecorationColor = attr.getColor(R.styleable.PagerCardView_itemDecorationColor,getContext().getResources().getColor(R.color.itemDecorationColor));
        //获取左边的margin
        int itemMarginLeft = (int) attr.getDimension(R.styleable.PagerCardView_itemMarginLeft,0);
        //获取右边的margin
        int itemMarginRight = (int) attr.getDimension(R.styleable.PagerCardView_itemMarginRight,0);
        //获取顶部的margin
        int itemMarginTop = (int) attr.getDimension(R.styleable.PagerCardView_itemMarginTop,0);
        //获取底部的margin
        int itemMarginBottom = (int) attr.getDimension(R.styleable.PagerCardView_itemMarginBottom,0);
        //获取margin
        int itemMargin = (int) attr.getDimension(R.styleable.PagerCardView_itemMargin,GuideViewUtils.dip2px(context,10));
        //获取左边的padding
        int itemPaddingLeft = (int) attr.getDimension(R.styleable.PagerCardView_itemPaddingLeft,0);
        //获取右边的padding
        int itemPaddingRight = (int) attr.getDimension(R.styleable.PagerCardView_itemPaddingRight,0);
        //获取顶部的padding
        int itemPaddingTop = (int) attr.getDimension(R.styleable.PagerCardView_itemPaddingTop,0);
        //获取底部的padding
        int itemPaddingBottom = (int) attr.getDimension(R.styleable.PagerCardView_itemPaddingBottom,0);
        //获取padding
        int itemPadding = (int) attr.getDimension(R.styleable.PagerCardView_itemPadding,0);
        //获取每个宫格的背景resource资源
        Drawable itemBackgroundResource = attr.getDrawable(R.styleable.PagerCardView_itemBg);
        //获取每个宫格背景色
        int itemBackgroundColor = Color.WHITE;
        if (itemBackgroundResource == null){
            itemBackgroundColor = attr.getColor(R.styleable.PagerCardView_itemBg,Color.TRANSPARENT);
        }
        //获取是否支持无限循环
        enableInfinite = attr.getBoolean(R.styleable.PagerCardView_enableInfinite,false);
        //自动播放时间
        playDuration = attr.getInteger(R.styleable.PagerCardView_playDuration,0);
        int redBackGroundColor = attr.getColor(R.styleable.PagerCardView_pagerCardRedPointColor,Color.RED);
        attribute = new PagerCardAttribute(imgHeight,imgWidht,redPointTextSize,redPointBackground,redPointWidht,
                redPointHeight,pagerCardTextSize,pagerCardTextColor,unSeIndicatorColor,seIndicatorColor,
                10,10,imgType,imgCorner,needIndicator,canScrollVertically,itemDecorationColor,itemDecorationWeight,
                itemMarginLeft,itemMarginRight,itemMarginTop,itemMarginBottom,itemMargin,itemPadding,itemPaddingLeft,itemPaddingTop,itemPaddingRight,itemPaddingBottom,itemBackgroundColor,itemBackgroundResource,enableInfinite,playDuration,redBackGroundColor);
        timer = new Timer();
        handler = new Handler();
        attr.recycle();
    }

    /**
     * 设置页面数据
     * @param content 页面内容
     * @param fragmentManager fragment管理器
     * @param rowNum 行数
     * @param colNum 列数
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setCardContent(List<PagerCardBean> content, FragmentManager fragmentManager, int rowNum, int colNum){
        setCardContent(content,fragmentManager,rowNum,colNum,null);
    }

    /**
     * 设置每个pagerCard中的内容
     * @param content pagerCard中的内容
     * @param fragmentManager viewpager需要用到的fragment管理器
     * @param rowNum 行数
     * @param colNum 列数
     * @param pagerCardListener 内容的点击监听器
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setCardContent(List<PagerCardBean> content, FragmentManager fragmentManager, int rowNum, int colNum, final PagerCardListener pagerCardListener){
        this.row = rowNum;
        this.colm = colNum;
        this.pagerCardListener = pagerCardListener;
        if (fragmentManager == null || content == null || content.size() == 0 || rowNum == 0|| colNum == 0){
            return;
        }
        fragments = new ArrayList<>();
        indicator.removeAllViews();
        indicatorList = new ArrayList<>();
        //如果内容数量小鱼要求显示数量，就不进行分页显示了
        if (content.size() <= rowNum*colNum || rowNum == -1){
            //获取fragment
            fragments = new ArrayList<>();
            fragments.add(makeRecyclerview(content));
            //添加指示器
            if (needIndicator && rowNum != -1){
                indicatorList.add(makeIndicator());
            }
        }else {
            //更具数据源总长度和要求每页显示数量来进行分页显示
            int length;
            length = content.size()/(rowNum*colNum);
            if (content.size()%(rowNum*colNum*1.0f) > 0){
                length++;
            }
            //遍历数据源
            for (int i=0;i<length;i++){
                if (needIndicator){
                    indicatorList.add(makeIndicator());
                }
                List<PagerCardBean> result = new ArrayList<>();
                //设置遍历获取每页中的数据
                for (int j=i*rowNum*colNum;j<(i+1)*rowNum*colNum;j++){
                    if (j >= content.size()){
                        break;
                    }
                    result.add(content.get(j));
                }
                fragments.add(makeRecyclerview(result));

                if (enableInfinite){
                    //在首尾多添加一项，用于无限循环,在后面还需要调换一下位置。
                    if (i == 0 || i == length-1){
                        fragments.add(makeRecyclerview(result));
                    }
                }
            }
        }

        //设置指示器第零个为选中状态
        if (indicatorList.size() != 0){
            oldIndicatorIndex = 0;
            indicatorList.get(0).setBackgroundResource(R.drawable.indicator_bg);
            //设置drawable中的颜色，默认为粉红色
            GradientDrawable gifDrawableResource = (GradientDrawable)indicatorList.get(0).getBackground();
            gifDrawableResource.setColor(seIndicatorColor);
        }

        //对第一项和最后一项进行位置调换
        if (enableInfinite){
            if (fragments.size() > 1 && enableInfinite){
                RecyclerView one = fragments.get(0);
                fragments.set(0,fragments.get(fragments.size()-1));
                fragments.set(fragments.size()-1,one);
            }
        }

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(fragments);
        pager2.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //changeIndicator(position);
                //如果滑动距离超过一半，认为已经滑动到第二页，此时需要改变指示器状态
                if (positionOffset < 0.5f){
                    changeIndicator(position,positionOffset,positionOffsetPixels);
                }else {
                    changeIndicator(position+1,positionOffset,positionOffsetPixels);
                }



                //活动期间不允许定时器改变页面
                if (positionOffset != 0){
                    isPausePlay = true;
                }else {
                    isPausePlay = false;
                }
            }

            @Override
            public void onPageSelected(int position) {
                changeIndicator(position,0,0);
                //下面部分为无线循环页面时的代码逻辑
                if (fragments.size() > 1 && enableInfinite){
                    if (position == fragments.size()-1){
                        pager2.setCurrentItem(1,false);
                    }else if (position == 0){
                        pager2.setCurrentItem(fragments.size()-2,false);
                    }

                    if (position ==1){
                        position = 0;
                    }else if (position == fragments.size() - 1){
                        position = 0;
                    }else {
                        position = position -1;
                    }
                    if ( PagerCardView.this.pagerCardListener != null ){
                        PagerCardView.this.pagerCardListener.onPagerSelect(position);
                    }
                }else {
                    if ( PagerCardView.this.pagerCardListener != null ){
                        PagerCardView.this.pagerCardListener.onPagerSelect(position);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (PagerCardView.this.pagerCardListener != null){
                    PagerCardView.this.pagerCardListener.onPageScrollStateChanged(state);
                }
            }
        });
        //给viewpager设置数据，目的是设置正确的高度
        if (rowNum == -1){
            pager2.setRow(rowNum,colNum,content.size(),true,attribute,this);
        }else {
            pager2.setRow(rowNum,colNum,content.size(),false,attribute,this);
        }
        pager2.setAdapter(pagerAdapter);
        if (enableInfinite){
            if (fragments.size() > 1){
                pager2.setCurrentItem(1,false);
            }
        }
        pager2.setPageMargin(GuideViewUtils.dip2px(getContext(),0));

        //只有支持无限循环和页数大于1页才允许打开自动播放,且自动播放速度必须大于500毫秒
       if (enableInfinite && fragments.size() > 1 && playDuration > 500){
           //设置定时器进行自动播放
           timer.schedule(new TimerTask() {
               @Override
               public void run() {
                   handler.post(new Runnable() {
                       @Override
                       public void run() {
                           if (!isPausePlay){
                               int position  = pager2.getCurrentItem() + 1;
                               if (position == fragments.size()-1){
                                   pager2.setCurrentItem(1,false);
                               }else if (position == 0){
                                   pager2.setCurrentItem(fragments.size()-2,false);
                               }else {
                                   pager2.setCurrentItem(position,true);
                               }
                           }
                       }
                   });
               }
           },1000,playDuration);
       }
    }

    public boolean updateItemPagerCard(int pager,int itemIndex,PagerCardBean pagerCardBean){
        return ((CardPagerAdapter) (fragments.get(pager).getAdapter())).updateItemPagerCard(itemIndex,pagerCardBean);
    }

    public RecyclerView makeRecyclerview(List<PagerCardBean> list){
        RecyclerView pagerCardContent = new RecyclerView(getContext());
        CardPagerAdapter pagerContentAdapter = new CardPagerAdapter();
        GridLayoutManager layoutManager = null;
        //设置recyclerview是否可垂直滑动
        if (attribute != null){
            if (attribute.isCanScrollVertically()){
                layoutManager = new GridLayoutManager(getContext(),colm);
            }else {
                layoutManager = new GridLayoutManager(getContext(),colm){
                    @Override
                    public boolean canScrollVertically() {
                        return false;
                    }

                    @Override
                    public boolean canScrollHorizontally() {
                        return false;
                    }
                };
            }
        }
        pagerContentAdapter = new CardPagerAdapter();
        pagerContentAdapter.setPagerCardAttribute(attribute);
        pagerContentAdapter.setContent(list);
        pagerContentAdapter.setCardListener(this);
        if (attribute != null && attribute.getItemDecorationWeight() != -1){
            pagerCardContent.addItemDecoration(new PagerCardGridDivider(getContext(),attribute.getItemDecorationWeight(),attribute.getItemDecorationColor(),colm));
        }
        pagerCardContent.setLayoutManager(layoutManager);
        pagerCardContent.setAdapter(pagerContentAdapter);
        return pagerCardContent;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        timer.cancel();
    }

    /**
     * 改变指示器选中状态
     * @param position
     */
    protected void changeIndicator(int position,float positionOffset,int positionOffsetPixels){
        if (fragments.size() > 1 && enableInfinite){
            if (position ==1){
                position = 0;
            }else if (position == fragments.size() - 1){
                position = 0;
            }else {
                position = position -1;
            }
        }

        if (PagerCardView.this.pagerCardListener != null){
            PagerCardView.this.pagerCardListener.onPageScrolled(position,positionOffset,positionOffsetPixels);
        }

        if (PagerCardView.this.pagerCardListener != null){
            PagerCardView.this.pagerCardListener.onPagerSelect(position);
        }

        if (position < 0 || position >= indicatorList.size()){
            return;
        }
       indicatorList.get(position).setBackgroundResource(R.drawable.indicator_bg);
        GradientDrawable gifDrawableResource = (GradientDrawable)indicatorList.get(position).getBackground();
        gifDrawableResource.setColor(seIndicatorColor);

       if (position != oldIndicatorIndex){
           indicatorList.get(oldIndicatorIndex).setBackgroundResource(R.drawable.indicator_unselect_bg);
           GradientDrawable gif = (GradientDrawable)indicatorList.get(oldIndicatorIndex).getBackground();
           gif.setColor(unSeIndicatorColor);
       }
       oldIndicatorIndex = position;
    }

    /**
     * 设置卡片的指示器
     * @return
     */
    protected View makeIndicator(){
        View v = LayoutInflater.from(getContext()).inflate(R.layout.cardpager_view_indicator,null,false);
        GradientDrawable gifDrawableResource = (GradientDrawable)v.getBackground();
        gifDrawableResource.setColor(unSeIndicatorColor);
        LayoutParams ll = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ll.height = indicatorHeight;
        ll.width = indicatorWidth;
        ll.leftMargin = GuideViewUtils.dip2px(getContext(),3);
        ll.rightMargin = GuideViewUtils.dip2px(getContext(),3);
        v.setLayoutParams(ll);
        indicator.addView(v);
        return v;
    }


    /**
     * 条目点击事件
     * @param pagerCardBean
     * @param index
     */
    @Override
    public void onClickPagerCardListener(PagerCardBean pagerCardBean, int index) {
        if (pagerCardListener != null){
            int position = pager2.getCurrentItem();
            if (enableInfinite && fragments.size() > 1){
                if (position ==1){
                    position = 0;
                }else if (position == fragments.size() - 1){
                    position = 0;
                }else {
                    position = position -1;
                }
            }
            pagerCardListener.onItemClickListener(pagerCardBean,index,position);
        }
    }

    /**
     * pagerCard的动作监听
     * @param <T>
     */
    public interface PagerCardListener<T extends PagerCardBean>{
        void onItemClickListener(T pagerCardBean, int itemIndex, int currentPagerIndex);
        void onPagerSelect(int currentPagerIndex);
        void onPageScrollStateChanged(int state);
        void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);
    }

    /**
     * 更新某一页中的内容
     * @param pagerNum 要更新内容的所在页码
     */
    public void updatePagerContent(int pagerNum,List<PagerCardBean> updateContent){

    }

    /**
     * 设置当前的pager
     * @param pagerNum 页码
     */
    public void setCurrentPager(int pagerNum){
        setCurrentPager(pagerNum,false);
    }

    /**
     * 更新pagerNum页面的数据
     * @param pagerNum
     */
    public boolean updatePagerCardList(int pagerNum,List<PagerCardBean> contentList){
        if (enableInfinite && fragments.size() > 1){
            pagerNum = pagerNum + 1;
        }
        if (fragments == null){
            L.i("kitMessage","请在更新页面数据前设置数据，即调用setCardContent方法进行页面数据装载，目前pagerCard中不存在页面");
            return false;
        }else {
            if (pagerNum >= fragments.size()){
                L.i("kitMessage","传入的页码数大于pagerCard中的最大页码数，将会出现“越界”的情况，请检查传入参数是否正确");
                return false;
            }
            if (contentList.size() > row*colm){
                L.i("kitMessage","更新集合长度不允许超过当前页面被定义的长度，即在调用setCardContent方法时，传入的rowNum和colNum决定了页面可显示的最大内容，更新的内容长度不允许超过rowNum*colNum");
                return false;
            }
            //((PagerCardContentFragment)fragments.get(pagerNum)).updatePagerCardList(contentList);
            return true;
        }
    }

    /**
     * 获取页面下的某项
     * @param pager 页码
     * @param index 第几个对象
     * @return 第pager页的第index个对象
     */
    public PagerCardBean getPagerItem(int pager,int index){
        if (pager >= fragments.size()){
            return null;
        }
        return ((CardPagerAdapter)(fragments.get(pager).getAdapter())).getPagerItem(index);
    }

    public List<PagerCardBean> getPagerItemList(int pager){
        if (pager >= fragments.size()){
            return null;
        }
        return ((CardPagerAdapter)(fragments.get(pager).getAdapter())).getPagerItemList();
    }
    /**
     * 获取某页中的所有内容
     * @param index
     * @return
     */
 /*   public List<PagerCardBean> getPagerList(int index){
        if (fragments == null){
            L.i("kitMessage","请在获取页面数据前设置数据，即调用setCardContent方法进行页面数据装载，目前pagerCard中不存在页面");
            return null;
        }else {
            if (index >= fragments.size()){
                L.e("kitError","传入的页码数大于pagerCard中的最大页码数，将会出现“越界”的情况，请检查传入参数是否正确");
            }
            //复制一份数据出来，如果直接返回源数据，调用者就可以直接给源数据内容进行修改了，而不需要经过updatePagerCardList方法，这是不允许的，如果允许updatePagerCardList中的判断也就没有意义了。
            List<PagerCardBean> pagerList = new ArrayList<>();
            List<PagerCardBean> source = ((PagerCardContentFragment)fragments.get(index)).getPagerContentList();
            for (int i=0; i< source.size(); i++){
                pagerList.add(source.get(i));
            }
            return pagerList;
        }
    }*/

    /**
     * 获取有多少页内容
     * @return
     */
    public int getPagerSize(){
        if (fragments != null){
            return fragments.size();
        }else {
            return 0;
        }
    }

    /**
     * 设置当前的pager
     * @param pagerNum 页码
     * @param smoothScroll 是否启用滑动动画
     */
    public void setCurrentPager(int pagerNum,boolean smoothScroll){
        if (pager2 != null){
            if (enableInfinite && fragments.size() > 1){
                pager2.setCurrentItem(pagerNum+1,smoothScroll);
            }else {
                pager2.setCurrentItem(pagerNum,smoothScroll);
            }
        }else {
            L.e("KitError","PagerCard：viewpager can not be null");
        }
    }

    /**
     * 通过链式调用的方式添加内容
     * @param item
     * @return
     */
    public PagerCardView addContent(PagerCardBean item){
        pagerCardBeans.add(item);
        return this;
    }

    /**
     * 设置内容
     * @param fragmentManager
     * @param rowNum
     * @param colNum
     */
    public void build(FragmentManager fragmentManager, int rowNum, int colNum){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            setCardContent(pagerCardBeans,fragmentManager,rowNum,colNum);
        }
    }

    /**
     * 设置内容
     * @param fragmentManager
     * @param rowNum
     * @param colNum
     * @param pagerCardListener
     */
    public void build(FragmentManager fragmentManager, int rowNum, int colNum, PagerCardListener pagerCardListener){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            setCardContent(pagerCardBeans,fragmentManager,rowNum,colNum,pagerCardListener);
        }
    }
}
