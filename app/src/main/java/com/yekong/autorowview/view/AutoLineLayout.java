package com.yekong.autorowview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by xigua on 2017/9/8.
 */

public class AutoLineLayout<T> extends FrameLayout implements View.OnTouchListener {

    private List<T> childList;//存放子控件
    private List<View> viewList = new ArrayList<>();

    /**
     * 被选中的View的Index
     */
    private List<T> selectViewList = new ArrayList<>();
    /**
     * 最大选中数量(默认为1个)
     */
    private int maxItem = 1;


    private Rect currentR;//记录用于比较的child的绘制空间

    private int layoutHeight,layoutWidth;//控件所占宽高

    private int drawWidth;//可用绘制宽度

    private int residueWidht;//剩余可用绘制宽度

    private int lineSpan = 15;//行间距
    private int rowSpan = 10;//左右间距

    private int paddingLeft,paddingRight,paddingTop,paddingBottom;

    private boolean canLayout = true;


    /**
     * 当只有一条时，选中后不再能够取消
     */
    private boolean isOneLock = false;
    /**
     * 锁定的条目
     */
    private int selectIndex = -1;

    public static final String SELECT_ON = "1";
    public static final String SELECT_OFF = "0";

    private OnItemClickListener listener;

    private Scroller scroller;

    public void setMaxItem(int maxItem) {
        this.maxItem = maxItem;
    }

    public void setOneLock(boolean oneLock) {
        isOneLock = oneLock;
    }

    public List<T> getSelectViewList() {
        return selectViewList;
    }

    public AutoLineLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public AutoLineLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AutoLineLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private int winHeight;
    private void init() {
        if (childList == null) {
            childList = new ArrayList<>();
        }
        setOnTouchListener(this);
        setClickable(true);
        scroller = new Scroller(getContext());
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        winHeight = wm.getDefaultDisplay().getHeight();
    }

    public void setChild(List<T> childList , int layoutId ,OnItemClickListener<T> listener) {
        this.childList = childList;
        this.listener = listener;
        for (int i = 0; i < childList.size(); i++) {
            TextView textView = (TextView) LayoutInflater.from(getContext()).inflate(layoutId, this, false);
            try {
                Class cls = Class.forName(childList.get(i).getClass().getName());
                Field f = cls.getField("_title_");
                String o = (String) f.get(childList.get(i));
                Log.e("AutoLineLayout", o);
                textView.setText(o);
                textView.setTag(SELECT_OFF);
                viewList.add(textView);
                addView(textView);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        canLayout = true;
        postInvalidate();
    }

    public interface OnItemClickListener<T>{
        void onItemClick(View view, T t, int position);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawChild();
    }

    private void drawChild(){
        if (canLayout) {
            if (null != childList) {
                for (int i = 0; i < viewList.size(); i++) {
                    View view = viewList.get(i);
                    mathChildSize(view);
                    canLayout = false;
                    final int finalI = i;
                    if (maxItem == 1) {
                        setItemClick(view, finalI);
                    }else{
                        setMutilImteClick(view,finalI);
                    }
                }
            }
        }
    }

    /**
     *
     * @param view 点击的view
     * @param finalI 下标
     */
    private void setMutilImteClick(View view ,final int finalI){
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag().equals(SELECT_ON)) {
                    //取消选中
                    view.setSelected(false);
                    view.setFocusable(false);
                    view.setPressed(false);
                    view.setTag(SELECT_OFF);
                    if (selectViewList.contains(childList.get(finalI)))
                        selectViewList.remove(childList.get(finalI));
                }else{
                    //选中
                    if (selectViewList.size() < maxItem) {
                        view.setSelected(true);
                        view.setFocusable(true);
                        view.setPressed(true);
                        if (!selectViewList.contains(childList.get(finalI)))
                            selectViewList.add(childList.get(finalI));
                        view.setTag(SELECT_ON);
                    }else{
                        //不可添加
                        Toast.makeText(getContext(), "最多可选中 "+maxItem+" 个", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
    /**
     *
     * @param view 点击的view
     * @param finalI 下标
     */
    private void setItemClick(View view ,final int finalI){
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag().equals(SELECT_ON)) {
                    //选中状态
                    if (viewList.size() == 1 && isOneLock) {
                        //保持选中
                        view.setSelected(true);
                        view.setFocusable(true);
                        view.setPressed(true);
                        if (!selectViewList.contains(childList.get(finalI)))
                            selectViewList.add(childList.get(finalI));
                        view.setTag(SELECT_ON);
                    }else{
                        view.setSelected(false);
                        view.setFocusable(false);
                        view.setPressed(false);
                        view.setTag(SELECT_OFF);
                        selectIndex = -1;
                        if (selectViewList.contains(childList.get(finalI)))
                            selectViewList.remove(childList.get(finalI));
                    }
                }else{
                    //非选中状态
                    if (selectIndex != finalI) {
                        if (selectIndex != -1) {
                            viewList.get(selectIndex).setTag(SELECT_OFF);
                            viewList.get(selectIndex).setSelected(false);
                            view.setFocusable(false);
                            view.setPressed(false);
                            if (selectViewList.contains(childList.get(finalI)))
                                selectViewList.remove(childList.get(finalI));
                        }
                        view.setSelected(true);
                        view.setFocusable(true);
                        view.setPressed(true);
                        view.setTag(SELECT_ON);
                        selectIndex = finalI;
                        if (!selectViewList.contains(childList.get(finalI)))
                            selectViewList.add(childList.get(finalI));
                    }
                }
                listener.onItemClick(view,childList.get(finalI),finalI);
            }
        });
    }

    /**
     * 计算控件绘制尺寸
     * @param view
     */
    private void mathChildSize(View view){
        int width = view.getWidth();
        int height = view.getHeight();
        if (currentR == null) {
            //绘制第一个控件
            currentR = new Rect(view.getLeft(),view.getTop(),view.getRight(),view.getBottom());
            residueWidht = drawWidth - width;
            layoutHeight = layoutHeight + height + lineSpan;
        }else{
            //绘制从第二个控件开始
            int left = currentR.left;
            int top = currentR.top;
            int right = currentR.right;
            int bottom = currentR.bottom;
            if (width + rowSpan*2 <= residueWidht) {
                //画在当前行
                view.layout(right + rowSpan ,top, right + width + rowSpan  ,top+height);
                residueWidht = residueWidht - width;
            }else{
                view.layout(0 + paddingLeft , bottom + lineSpan , width + paddingLeft , bottom + lineSpan + height);
                residueWidht = drawWidth - width;
                layoutHeight = layoutHeight + height + lineSpan;
            }
            //重新设值
            currentR.left = view.getLeft();
            currentR.top = view.getTop();
            currentR.right = view.getRight();
            currentR.bottom = view.getBottom();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        layoutWidth =  MeasureSpec.getSize(widthMeasureSpec);
        paddingLeft = getPaddingLeft();
        paddingRight = getPaddingRight();
        paddingTop = getPaddingTop();
        paddingBottom = getPaddingBottom();
        drawWidth = layoutWidth - paddingLeft - paddingRight;
//        if (layoutHeight == 0)
//            layoutHeight = paddingTop + paddingBottom - lineSpan;
        setMeasuredDimension(layoutWidth,layoutHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        layoutHeight = paddingTop + paddingBottom - lineSpan;
        drawChild();
        currentR = null;
        canLayout = true;
    }

    private float current_x,current_y;
    private float next_x,next_y;
    private float scrollYMax;


    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        final int action = motionEvent.getAction();
        if ((action == MotionEvent.ACTION_MOVE) ) { // 正在滑动中
            return true;
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                current_x = motionEvent.getX();
                current_y = motionEvent.getY();
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                next_x = motionEvent.getX();
                next_y = motionEvent.getY();
                Log.e(TAG, "scroll  currentR   :  "+(currentR.bottom + paddingTop + paddingBottom));
                Log.e(TAG, "scroll  getHeight   :  "+getHeight());
                Log.e(TAG, "scroll  winHeight   :  "+winHeight);
                Log.e(TAG, "scroll  getBottom   :  "+getBottom());


                if ( getBottom() > winHeight ) {
                    //&& (scrollType == 0 || scrollType == 2)
                    if(Math.abs(next_y - current_y) > 8 ){
                        //10个像素判断为移动
                        if (scrollYMax == 0) {
//                            scrollYMax = (currentR.bottom - getHeight() + paddingTop);
//                            scrollYMax = (currentR.bottom - winHeight + paddingTop + paddingBottom);
                            scrollYMax = (getBottom() - winHeight + paddingBottom);
                        }
                        if (next_y > current_y) {
                            //上移
                            if (getScrollY() <= 0) {
                                smoothScrollTo(getScrollX(),0);
                            } else {
                                scrollTo(getScrollX() , getScrollY() - (int) (Math.abs(next_y - current_y)));
                            }
                        } else {
                            //下移
                            //总长 - 偏移量
                            if (getScrollY() >= ( scrollYMax )) {
                                smoothScrollTo(getScrollX(), (int) scrollYMax );
                            } else {
                                scrollTo( getScrollX(), getScrollY() + (int) (Math.abs(next_y - current_y)));
                            }
                        }
                    }
                }/*else if (currentR.bottom + paddingTop  + paddingBottom > getHeight()){
                    if(Math.abs(next_y - current_y) > 8 ){
                        if (scrollYMax == 0) {
//                            scrollYMax = getBottom() -  winHeight + paddingTop + paddingBottom;
                            scrollYMax = currentR.bottom + paddingTop  + paddingBottom - getHeight();
                        }
                        if (next_y > current_y) {
                            //上移
                            if (getScrollY() <= 0) {
                                smoothScrollTo(getScrollX(),0);
                            } else {
                                scrollTo(getScrollX() , getScrollY() - (int) (Math.abs(next_y - current_y)));
                            }
                        } else {
                            //下移
                            //总长 - 偏移量
                            if (getScrollY() >= ( scrollYMax )) {
                                smoothScrollTo(getScrollX(), (int) scrollYMax );
                            } else {
                                scrollTo( getScrollX(), getScrollY() + (int) (Math.abs(next_y - current_y)));
                            }
                        }
                    }
                }*/
                current_x = next_x;
                current_y = next_y;
            }
            break;
            case MotionEvent.ACTION_UP:

                break;
        }
        return true;
    }

    /**
     * 缓慢滚动到指定位置
     * @param destX     指定滚动到的X轴位置
     * @param destY     指定滚动到的Y轴位置
     */
    private void smoothScrollTo(int destX, int destY) {
        //获取当前滚动的距离X
        int scrollX = getScrollX();
        //获取需要滚动的偏移量X
        int delta = destX - scrollX;
        //设置1000ms内滚动到delta位置，而效果就是慢慢滑动
        //获取当前滚动的距离Y
        int scrollY = getScrollY();
        //获取需要滚动的偏移量X
        int deltb = destY - scrollY;

        scroller.startScroll(scrollX, scrollY, delta, deltb , 500);
        invalidate();
    }

    /**
     * 持续滚动，实现慢慢滑动
     */
    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()){
            scrollTo(scroller.getCurrX(),scroller.getCurrY());
            postInvalidate();
        }
    }
}

