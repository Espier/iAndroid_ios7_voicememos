package org.espier.voicememos7.ui;

import android.app.Activity;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;






import org.espier.voicememos7.ui.EspierVoiceMemos7.MyTimerTask;
import org.espier.voicememos7.ui.SlideCutListView.RemoveDirection;
import org.espier.voicememos7.ui.SlideCutListView.RemoveListener;
import org.espier.voicememos7.util.Recorder;




import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class EspierVoiceMemos7 extends Activity implements RemoveListener
{
    
    Handler handler = new Handler()
    {

        /* (non-Javadoc)
         * @see android.os.Handler#handleMessage(android.os.Message)
         */
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    //waveView.invalidate();
                    TextView textView = (TextView)findViewById(R.id.textView1);
                    textView.setText(miniute+":"+second+":"+ms);
                    break;
                case 2:
                    
                    
                    break;

                default:
                    break;
            }
        }
        
        
    };
    private RelativeLayout ll1;
    private LinearLayout ll2;
    private List<String> dataSourceList = new ArrayList<String>();
    View view ;
    private SlideCutListView listview;
    ImageView start;
    protected MyTimerTask myTimerTask;
    protected TimerTask timerTask;
    private OnTouchListener startTouchListener = new View.OnTouchListener() {
        public final float[] BT_SELECTED = new float[] {1,0,0,0,-100,0,1,0,0,-100,0,0,1,0,-100,0,0,0,1,0};
        public final float[] BT_NOT_SELECTED = new float[] {1,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,1,0};
        private boolean isdown= false;
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub
            
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    start.getBackground().setColorFilter( new ColorMatrixColorFilter(BT_SELECTED));
                    start.setBackgroundDrawable(start.getBackground());

                    break;
                case MotionEvent.ACTION_MOVE:
                    
                    break;
                case MotionEvent.ACTION_UP:
                    start.getBackground().setColorFilter(new ColorMatrixColorFilter(BT_NOT_SELECTED));
                    start.setBackgroundDrawable(start.getBackground());
                   if(!isdown){
                       start.setBackgroundResource(R.drawable.start_down);
                       isdown = true;
                       //start
                       start();
                   }else{
                       start.setBackgroundResource(R.drawable.circular);
                       isdown = false;
                       //pause
                       pause();
                   }
                    
                    break;

                default:
                    break;
            }
            return true;
        }
        private void pause() {
            // TODO Auto-generated method stub
            mRecorder.stopRecording();
            Log.e("state", mRecorder.mState+"");
            if (timerTask!=null) {
                timerTask.cancel();
            }
        }
        Timer    timer = new Timer();
        private void start() {
            // TODO Auto-generated method stub
            if (mRecorder.mState == Recorder.RECORDING_STATE) {
                return;
            }
            mRecorder.startRecording(EspierVoiceMemos7.this);
            
            if (mRecorder.mState == Recorder.RECORDING_STATE) {
                //timeThread.start();
                myTimerTask = new MyTimerTask(ms, second, miniute);
                timer.schedule(myTimerTask, 10, 10);

                Log.e("state", "start ok!");
            }
            else {
                Log.e("state", "start failed!");
            }
        }
    };

    private org.espier.voicememos7.ui.SlideCutListView slideCutListView;
    private ArrayAdapter<String> adapter;
    private org.espier.voicememos7.ui.WaveView waveView;
    private org.espier.voicememos7.util.Recorder mRecorder;  
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_memo_main);
        ll1 = (RelativeLayout) findViewById(R.id.ll_1);
        ll2 = (LinearLayout) findViewById(R.id.ll_2);
        LinearLayout layout =(LinearLayout)findViewById(R.id.layout);
        start = (ImageView)findViewById(R.id.imageView2);
        
        start.setOnTouchListener(startTouchListener );
        
        view = layout.getChildAt(0);
        
        init();
    }
    
    private void init() {
        
        waveView = (WaveView)findViewById(R.id.waveView);
        waveView.setMinimumWidth(500);
        waveView.setMinimumHeight(500);
        mRecorder = new Recorder();
        waveView.setRecorder(mRecorder);
        
        slideCutListView = (SlideCutListView) findViewById(R.id.listView);
        slideCutListView.setRemoveListener(this);
        
        for(int i=0; i<20; i++){
            dataSourceList.add("滑动删除" + i); 
        }
        
        adapter = new ArrayAdapter<String>(this, R.layout.listview_item, R.id.list_item, dataSourceList);
        slideCutListView.setAdapter(adapter);
        
        slideCutListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                Toast.makeText(EspierVoiceMemos7.this, dataSourceList.get(position), Toast.LENGTH_SHORT).show();
            }
        });
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
    private boolean isTobottom = false;
    private boolean isfirstdown = true;
    public int ms;
    public int second;
    public int miniute;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        velocityTracker.addMovement(event);
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downy = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if(!isTobottom){
                    int deltaY = (int) (downy - y);
                    downy = y;
                    if(deltaY <0&& isfirstdown ){
                        return true;
                    }
                    if(deltaY>0){
                        isfirstdown = false;
                    }
                    view.scrollBy(0, deltaY);
                    System.out.println(deltaY);
                }else{
                    return true;
                }
                
                
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
                    isTobottom =true;
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

    @Override
    public void removeItem(RemoveDirection direction, int position) {
        adapter.remove(adapter.getItem(position));
        switch (direction) {
        case RIGHT:
            Toast.makeText(this, "向右删除  "+ position, Toast.LENGTH_SHORT).show();
            break;
        case LEFT:
            Toast.makeText(this, "向左删除  "+ position, Toast.LENGTH_SHORT).show();
            break;

        default:
            break;
        }
        
    }
    public class MyTimerTask extends TimerTask implements Runnable
    {
        private int m_ms;
        private int m_sec;
        private int m_min;

        public MyTimerTask(int ms,int sec,int min)
        {
            this.m_ms = ms;
            this.m_sec = sec;
            this.m_min = min;
            Log.e("time", min+":"+sec+":"+ms);
        }
        @Override
        public void run() {
            // TODO Auto-generated method stub
            m_ms+=1;
            if (m_ms==100) {
                m_ms=0;
                m_sec +=1;
                
               
            }
            if (m_sec==60) {
                m_min+=1;
                m_sec=0;
            }
            if (m_min==60) {
                m_min=0;
            }
            ms = this.m_ms;
            second = this.m_sec;
            miniute = this.m_min;
            //lastDataTime+=1;
            TimeFormat timeFormat = new TimeFormat();
            timeFormat.setMs(ms);
            timeFormat.setSecond(second);
            timeFormat.setMinute(miniute);
            waveView.setTimeFormat(timeFormat);
           
            Message msg = new Message();
            msg.what = 1;
            handler.sendMessage(msg);
        }
        
    }
}
