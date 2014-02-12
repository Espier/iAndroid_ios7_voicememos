package org.espier.voicememos7.ui;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.espier.voicememos7.R;
import org.espier.voicememos7.ui.SlideCutListView;


import org.espier.ios7.ui.IosLikeListView;
import org.espier.ios7.ui.IosLikeScrollView;

import java.util.ArrayList;
import java.util.List;

public class EspierVoiceMemos7 extends Activity
{

    private RelativeLayout ll1;
    private LinearLayout ll2;
    private List<String> dataSourceList = new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    SlideCutListView list;
    View view ;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_main);
        ll1 = (RelativeLayout) findViewById(R.id.ll_1);
        ll2 = (LinearLayout) findViewById(R.id.ll_2);
        LinearLayout layout =(LinearLayout)findViewById(R.id.layout);
        view = layout.getChildAt(0);
        
        
         list = (SlideCutListView)findViewById(R.id.listView);
        for(int i=0; i<20; i++){
            dataSourceList.add("滑动删除" + i); 
        }
        adapter = new ArrayAdapter<String>(this, R.layout.listview_item, R.id.list_item, dataSourceList);
        list.setAdapter(adapter);
    }
    int height;
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);
        Rect rect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        int top = rect.top;//

         height = getWindowManager().getDefaultDisplay().getHeight()-top;
        int width = getWindowManager().getDefaultDisplay().getWidth();
        
        LayoutParams lp1 = ll1.getLayoutParams();
        lp1.height = (int) (height * 0.9);
        lp1.width = width;
        ll1.setLayoutParams(lp1);
        
        LayoutParams lp2 = ll2.getLayoutParams();
        lp2.height = (int) (height * 0.8);
        lp2.width = width;
        ll2.setLayoutParams(lp2);
        
    }
    
    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {}
            return false;
        }
    });
    float downy = 0;
    private VelocityTracker velocityTracker=VelocityTracker.obtain();
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        velocityTracker.addMovement(event);
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downy = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                
                int deltaY = (int) (downy - y);
                downy = y;
                view.scrollBy(0, deltaY);
                System.out.println(view.getTop());
                
                break;
            case MotionEvent.ACTION_UP:
                int velocityY = getScrollVelocity();
                if(velocityY>0){
                    System.out.println("向下 ");
                    if(view.getScrollY()<0){
                        view.scrollTo(0, 0);
                    }
                    System.out.println(view.getScrollY());
                }else if(velocityY<0){
                    System.out.println("向上");
                    System.out.println(view.getScrollY()+"　　"+height*1.7);
                    view.scrollTo(0, (int) (height*0.8));
                }else{
                    
                }
                
                break;

            default:
                break;
        }
        return super.onTouchEvent(event);
    }
    
    private int getScrollVelocity() {
        velocityTracker.computeCurrentVelocity(1000);
        int velocity = (int) velocityTracker.getYVelocity();
        return velocity;
    }
}
