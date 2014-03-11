
package org.espier.voicememos7.ui;

import android.content.Context;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Scroller;

import org.espier.voicememos7.R;

/**
 * @blog http://blog.csdn.net/xiaanming
 * @author xiaanming
 */
public class SlideCutListView extends ListView {
    /**
     * 当前滑动的ListView　position
     */
    private int slidePosition;
    /**
     * 手指按下X的坐标
     */
    private int downY;
    /**
     * 手指按下Y的坐标
     */
    private int downX;
    /**
     * 屏幕宽度
     */
    private int screenWidth;
    /**
     * ListView的item
     */
    private View itemView;
    /**
     * 滑动类
     */
    private Scroller scroller;
    private static final int SNAP_VELOCITY = 600;
    /**
     * 速度追踪对象
     */
    private VelocityTracker velocityTracker;
    /**
     * 是否响应滑动，默认为不响应
     */
    private boolean isSlide = false;
    /**
     * 认为是用户滑动的最小距离
     */
    private int mTouchSlop;
    
    private int lastY;
    /**
     * 移除item后的回调接口
     */
    private RemoveListener mRemoveListener;
    /**
     * 用来指示item滑出屏幕的方向,向左或者向右,用一个枚举值来标记
     */
    private RemoveDirection removeDirection;

    // 滑动删除方向的枚举值
    public enum RemoveDirection {
        RIGHT, LEFT;
    }
    
    private Button hiddenButton;
    
    static boolean isDown = false;

    public SlideCutListView(Context context) {
        this(context, null);
    }

    public SlideCutListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideCutListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        screenWidth = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getWidth();
        scroller = new Scroller(context);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    /**
     * 设置滑动删除的回调接口
     * 
     * @param removeListener
     */
    public void setRemoveListener(RemoveListener removeListener) {
        this.mRemoveListener = removeListener;
    }
    



    /**
     * 分发事件，主要做的是判断点击的是那个item, 以及通过postDelayed来设置响应左右滑动事件
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {

                addVelocityTracker(event);

                // 假如scroller滚动还没有结束，我们直接返回
                if (!scroller.isFinished()) {
                    return super.dispatchTouchEvent(event);
                }
                downX = (int) event.getX();
                downY = (int) event.getY();

                slidePosition = pointToPosition(downX, downY);

                // 无效的position, 不做任何处理
                if (slidePosition == AdapterView.INVALID_POSITION) {
                    return super.dispatchTouchEvent(event);
                }

                // 获取我们点击的item view
                View itemWholeView = getChildAt(slidePosition - getFirstVisiblePosition());
                hiddenButton = (Button)itemWholeView.findViewById(R.id.hiddenDeleteButon);
                if (itemView != itemWholeView.findViewById(R.id.memos_item_visible)) {
                    restoreItem();
                    
                }
                
//                if (itemView != null &&getitemView.getScrollX()>0) {
//                    restoreItem();
//                    return false;
//                    
//                }
                itemView = itemWholeView.findViewById(R.id.memos_item_visible);
//                itemView = getChildAt(slidePosition - getFirstVisiblePosition());
                
                break;
            }
            case MotionEvent.ACTION_MOVE: {
//                if(itemView==null){
//                    return super.dispatchTouchEvent(event);
//                }
                Log.d("listview","action move:getScrollX"+String.valueOf(itemView.getScrollX()));
                Log.d("listview","action move:downX"+String.valueOf(downX));
                Log.d("listview","action move:event.getX()"+String.valueOf(event.getX()));
                if (Math.abs(getScrollVelocity()) > SNAP_VELOCITY
                        || (event.getX() - downX > mTouchSlop && itemView.getScrollX()>0 )||(downX - event.getX()> mTouchSlop && Math
                                .abs(event.getY() - downY) < mTouchSlop)) {
                    isSlide = true;
                }
//                if (Math.abs(event.getY() - downY)>mTouchSlop)
//                    return false;
                Log.d("listview","action move:isSlide="+String.valueOf(isSlide));
                
                break;
            }
            case MotionEvent.ACTION_UP:
                recycleVelocityTracker();
                break;
        }
        
        return super.dispatchTouchEvent(event);
    }

    // /**
    // * 往右滑动，getScrollX()返回的是左边缘的距离，就是以View左边缘为原点到开始滑动的距离，所以向右边滑动为负值
    // */
    // private void scrollRight() {
    // removeDirection = RemoveDirection.RIGHT;
    // final int delta = (screenWidth + itemView.getScrollX());
    // // 调用startScroll方法来设置一些滚动的参数，我们在computeScroll()方法中调用scrollTo来滚动item
    // scroller.startScroll(itemView.getScrollX(), 0, -delta, 0,
    // Math.abs(delta));
    // postInvalidate(); // 刷新itemView
    // }

    /**
     * 向左滑动，根据上面我们知道向左滑动为正值
     */
    private void scrollLeft() {
        removeDirection = RemoveDirection.LEFT;
        final int delta = (screenWidth - itemView.getScrollX());
        // 调用startScroll方法来设置一些滚动的参数，我们在computeScroll()方法中调用scrollTo来滚动item
        scroller.startScroll(itemView.getScrollX(), 0, delta, 0,
                Math.abs(delta));
        hiddenButton.setEnabled(true);
        //hiddenButton.setVisibility(View.VISIBLE);
//        View btnDelete = (View)itemView.findViewById(R.id.memos_item_delete_button);
//        btnDelete.setVisibility(View.VISIBLE);
        
        postInvalidate(); // 刷新itemView
    }

    /**
     * 根据手指滚动itemView的距离来判断是滚动到开始位置还是向左或者向右滚动
     */
    private void scrollByDistanceX() {
        // 如果向左滚动的距离大于屏幕的5分之一，就让其删除
        if (itemView.getScrollX() >= screenWidth / 10) {
            // 滚动到显示删除按钮的地方
            itemView.scrollTo(screenWidth / 5, 0);
            // scrollLeft();
            hiddenButton.setEnabled(true);
        } else {
            // 滚回到原始位置,为了偷下懒这里是直接调用scrollTo滚动
            itemView.scrollTo(0, 0);
            hiddenButton.setEnabled(false);
        }

    }
    
    public void restoreItem() {
        if (itemView!=null) {
            itemView.scrollTo(0,0);
            hiddenButton.setEnabled(false);
        }
    }

    /**
     * 处理我们拖动ListView item的逻辑
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        
        
        if (ev.getAction() == MotionEvent.ACTION_DOWN)
            lastY = (int)ev.getY();
        
         
        Log.d("adf","in onTouchEvent:");
        if (isSlide && slidePosition != AdapterView.INVALID_POSITION) {
            isDown = true;
            Log.d("adf","in if ");
            requestDisallowInterceptTouchEvent(true);
            addVelocityTracker(ev);
            final int action = ev.getAction();
            int x = (int) ev.getX();
            
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    Log.d("adf","in action down ");
                    
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.d("adf","onTouchEvent: action move");

                    MotionEvent cancelEvent = MotionEvent.obtain(ev);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                            (ev.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                    onTouchEvent(cancelEvent);

                    int deltaX = downX - x;
                    downX = x;
                    // 手指拖动itemView滚动, deltaX大于0向左滚动，小于0向右滚
                    itemView.scrollBy(deltaX, 0);
                    return true; // 拖动的时候ListView不滚动
                case MotionEvent.ACTION_UP:
                    Log.d("adf","in action up ");
                    int velocityX = getScrollVelocity();
                    if (velocityX > SNAP_VELOCITY) {
                        //scrollRight();
                    } else if (velocityX < -SNAP_VELOCITY) {
                        scrollLeft();
                    } else {
                        scrollByDistanceX();
                    }

                    recycleVelocityTracker();
                    // 手指离开的时候就不响应左右滚动
                    isSlide = false;
//                    break;
                    return super.onTouchEvent(ev);
            }
            
        }
        if (Math.abs(ev.getY() - lastY)>mTouchSlop)
            return super.onTouchEvent(ev);
//        if(itemView==null){
//            return super.onTouchEvent(ev);
//        }
        
//        if (isDown == false) {
//            restoreItem();
//            return super.onTouchEvent(ev);
//        }
//        isDown = false;
        Log.d("adsf", "getScrollx=" + String.valueOf(itemView.getScrollX()));
        Log.d("adsf", "getScrolly=" + String.valueOf(itemView.getScrollY()));
        if (itemView.getScrollX() <= 0 && ev.getAction() == MotionEvent.ACTION_UP) {
            View itemWholeView = getChildAt(slidePosition - getFirstVisiblePosition());
            itemWholeView.performClick();
        }
        // 否则直接交给ListView来处理onTouchEvent事件
        return super.onTouchEvent(ev);
    }

    @Override
    public void computeScroll() {
        // 调用startScroll的时候scroller.computeScrollOffset()返回true，
        if (scroller.computeScrollOffset()) {
            // 让ListView item根据当前的滚动偏移量进行滚动
            itemView.scrollTo(scroller.getCurrX(), scroller.getCurrY());

            postInvalidate();

            // 滚动动画结束的时候调用回调接口
            if (scroller.isFinished()) {
                if (mRemoveListener == null) {
                    throw new NullPointerException(
                            "RemoveListener is null, we should called setRemoveListener()");
                }

                itemView.scrollTo(0, 0);
                mRemoveListener.removeItem(removeDirection, slidePosition);
            }
        }
    }

    /**
     * 添加用户的速度跟踪器
     * 
     * @param event
     */
    private void addVelocityTracker(MotionEvent event) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }

        velocityTracker.addMovement(event);
    }

    /**
     * 移除用户速度跟踪器
     */
    private void recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    /**
     * 获取X方向的滑动速度,大于0向右滑动，反之向左
     * 
     * @return
     */
    private int getScrollVelocity() {
        velocityTracker.computeCurrentVelocity(1000);
        int velocity = (int) velocityTracker.getXVelocity();
        return velocity;
    }

    /**
     * 当ListView item滑出屏幕，回调这个接口 我们需要在回调方法removeItem()中移除该Item,然后刷新ListView
     * 
     * @author xiaanming
     */
    public interface RemoveListener {
        public void removeItem(RemoveDirection direction, int position);
    }

    @Override
    public void setItemChecked(int position, boolean value) {
        // TODO Auto-generated method stub
        super.setItemChecked(position, value);
    }

}
