package com.libowu.guideview.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.libowu.guideview.bean.GuideBean;
import com.libowu.guideview.callBack.GuideViewClickCallBack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author libowu
 * @date 2019/07/30
 * guideview的核心代码
 */
public class GuideView extends View {
    private Paint paint;
    private int width, height;
    private Bitmap srcBm, dstBm;
    private PorterDuffXfermode porterDuffXfermode;
    private Rect rect;
    private List<GuideBean> guideBeans;
    private GuideBean guideBean;
    private int guideIndex;
    private GuideViewClickCallBack guideViewClickCallBack;

    public GuideView(Context context){
        this(context,null);
    }

    public GuideView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        paint = new Paint();
        paint.setColor(Color.RED);
        porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
        rect = new Rect(0,0,0,0);
        guideBeans = new ArrayList<>();
    }

    /**
     * 设置点击事件接口
     * @param guideViewClickCallBack
     */
    public void setGuideViewClickCallBack(GuideViewClickCallBack guideViewClickCallBack){
        this.guideViewClickCallBack = guideViewClickCallBack;
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = right - left;
        height = bottom - top;
        srcBm = createSrcBitmap(width, height,rect);
        dstBm = createDstBitmap(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (guideBeans == null || guideBeans.size() == 0){
            this.setVisibility(GONE);
            return;
        }
       if (Config.OPENMORE){
           canvas.drawBitmap(createDstBitmap(width,height), 0, 0, paint);
           for (int i=0; i<guideBeans.size(); i++){
               //是否是绘制简单的集合图像，不是就绘制要说明view的图像
               if (!guideBeans.get(i).isSimpleShape()){
                   drawBuyView(canvas,guideBeans.get(i));
               }else {
                  drawSimpleShapeView(canvas,guideBeans.get(i));
               }
           }
       }else {
           if (!guideBean.isSimpleShape()){
               drawBuyView(canvas,guideBean);
           }else {
               canvas.drawBitmap(createDstBitmap(width,height), 0, 0, paint);
               drawSimpleShapeView(canvas,guideBean);
           }
       }
    }


    /**
     * 绘制高亮区及说明图片
     * @param canvas
     * @param guideBean
     */
    public void drawBuyView(Canvas canvas,GuideBean guideBean){
        if (guideBean == null){
            return;
        }
        //如果是一屏显示多个控件说明，遮罩层在循环前绘制一边即可
        if (!Config.OPENMORE){
            canvas.drawBitmap(createDstBitmap(width,height), 0, 0, paint);
        }
        //绘制view的图像
        if (guideBean.getViewBitmap() != null){
            canvas.drawBitmap(guideBean.getViewBitmap(),guideBean.getRect().left,guideBean.getRect().top,paint);
        }
        //说明控件的中线坐标
        int centerLine = guideBean.getRect().left+(guideBean.getRect().right-guideBean.getRect().left)/2;
        //说明图片的左边距离
        int targetCenter = centerLine - guideBean.getBitmap().getWidth()/2;
        if (guideBean.getBitmap() != null){
            if (guideBean.isTop()){
                canvas.drawBitmap(guideBean.getBitmap(),targetCenter+guideBean.getMarginLeft(),guideBean.getRect().top+guideBean.getMarginBottom()-guideBean.getBitmap().getHeight()+guideBean.getMarginBottom(),paint);
            }else {
                canvas.drawBitmap(guideBean.getBitmap(),targetCenter+guideBean.getMarginLeft(),guideBean.getRect().bottom+guideBean.getMarginTop()+guideBean.getMarginBottom(),paint);
            }
        }
    }


    /**
     * 高亮区使用基本几何图形绘制
     * @param canvas
     * @param guideBean
     */
    public void drawSimpleShapeView(Canvas canvas,GuideBean guideBean){
        //int layerID = canvas.saveLayer(0, 0, getWidth(), getHeight(), paint, Canvas.ALL_SAVE_FLAG);
        paint.setXfermode(porterDuffXfermode);
        canvas.drawBitmap(createSrcBitmap(width,height,guideBean.getRect()), 0, 0, paint);
        paint.setXfermode(null);
        //canvas.restoreToCount(layerID);
        //说明控件的中线坐标
        int centerLine = guideBean.getRect().left+(guideBean.getRect().right-guideBean.getRect().left)/2;
        //说明图片的左边距离
        int targetCenter = centerLine - guideBean.getBitmap().getWidth()/2;
        if (guideBean.getBitmap() != null){
            if (guideBean.isTop()){
                canvas.drawBitmap(guideBean.getBitmap(),targetCenter+guideBean.getMarginLeft(),guideBean.getRect().top+guideBean.getMarginBottom()-guideBean.getBitmap().getHeight()+guideBean.getMarginBottom(),paint);
            }else {
                canvas.drawBitmap(guideBean.getBitmap(),targetCenter+guideBean.getMarginLeft(),guideBean.getRect().bottom+guideBean.getMarginTop()+guideBean.getMarginBottom(),paint);
            }
        }
    }

    /**
     * 关闭引导图
     */
    public void closeGuide(){
        this.setVisibility(GONE);
        if (guideViewClickCallBack != null){
            guideViewClickCallBack.guideEndCallback();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if (guideBeans == null || guideBeans.size() == 0){
                    return true;
                }

                //如果是一屏显示多个控件说明，则点击后直接隐藏view
               if (Config.OPENMORE){
                   closeGuide();
                   if (guideViewClickCallBack != null){
                       guideViewClickCallBack.guideMoreClick(guideBeans);
                   }
               }else {
                   if (Config.CLICK_EXACT){
                       if (guideBean.getRect().contains((int)event.getX(),(int)event.getY())){
                           //抬起手指时显示下一张引导，当索引值大于集合长度时，索引归零，并隐藏引导
                           guideIndex++;
                       }else {
                           return true;
                       }
                   }else {
                       guideIndex++;
                   }
                   if (guideIndex >= guideBeans.size()){
                       closeGuide();
                       guideIndex = 0;
                       return true;
                   }
                   guideBean = guideBeans.get(guideIndex);
                   if (guideViewClickCallBack != null){
                       guideViewClickCallBack.guideClick(guideBean,guideIndex);
                   }
                   invalidate();
               }
                break;
            default:
                break;
        }
        return true;
    }

    public void showGuide(){
        this.setVisibility(VISIBLE);
        if (guideBeans != null && guideBeans.size() !=0){
            guideBean = guideBeans.get(0);
        }
        invalidate();
    }

    /**
     * 绘制遮罩层
     * @param width
     * @param height
     * @return
     */
    public Bitmap createDstBitmap(int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint dstPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dstPaint.setColor(Config.COLOR);
        canvas.drawRect(0,0,getWidth(),getHeight(), dstPaint);
        return bitmap;
    }

    public void setGuideBeans(List<GuideBean> guideBeans){
        this.guideBeans = guideBeans;
        invalidate();
    }

    /**
     * 绘制高亮区
     * @param width
     * @param height
     * @param rect
     * @return
     */
    public Bitmap createSrcBitmap(int width, int height,Rect rect) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint scrPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        scrPaint.setColor(Color.parseColor("#ec6941"));
        canvas.drawRect(rect, scrPaint);
        return bitmap;
    }

    /**
     * view的配置文件，务必在调用showGuide方法前配置完guideview
     */
    public static class Config{
        /**
         * 配置遮罩层颜色
         */
        public static int COLOR = Color.parseColor("#99000000");

        /**
         * 是否打开一个界面显示多个控件说明,默认是关闭的
         */
        public static boolean OPENMORE = false;

        /**
         * 点击到对应的控件时，才执行点击
         * 一屏多个控件说明的设置这个值无效
         */
        public static boolean CLICK_EXACT = false;
    }
}
