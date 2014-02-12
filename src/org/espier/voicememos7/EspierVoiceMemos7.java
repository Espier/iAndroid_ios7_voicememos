package org.espier.voicememos7;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_main);
        ll1 = (RelativeLayout) findViewById(R.id.ll_1);
        ll2 = (LinearLayout) findViewById(R.id.ll_2);
        IosLikeListView list = (IosLikeListView)findViewById(R.id.listView);
        for(int i=0; i<20; i++){
            dataSourceList.add("滑动删除" + i); 
        }
        adapter = new ArrayAdapter<String>(this, R.layout.listview_item, R.id.list_item, dataSourceList);
        list.setAdapter(adapter);
//        sv_base_view = (IosLikeScrollView) findViewById(R.id.sv_base_view);
//        sv_base_view.setOnTouchListener(scroolListener);
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
    
    /**
     * ScrollView��OnTouch�¼�
     */
    private OnTouchListener scroolListener = new OnTouchListener() {
        private float y;
        private boolean isScroll2Top= false;
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                y = event.getY();     
                break;
            case MotionEvent.ACTION_UP:
                if(event.getY()<y&&(y-event.getY())>height/2){
                        scroll2Buttom();
                    
                }else{
                    scroll2Top();
                }

                break;
            case MotionEvent.ACTION_MOVE:
//                System.out.println(event.getY());
//                scrollTo((int) (event.getY()-y));
                if(isScroll2Top){
                    scroll2Top();
                    isScroll2Top = false;
                }
                break;
            }
            return false;
        }
        private void scroll2Top() {
            // TODO Auto-generated method stub
//            sv_base_view.scrollTo(0, 0);
            isScroll2Top = true;
        }
        private void scroll2Buttom() {
            // TODO Auto-generated method stub
//            sv_base_view.scrollTo(0, (int) (height*0.9));
        }
    };
   
    

    

    

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            // TODO Auto-generated method stub

            return false;
        }
    });

}
