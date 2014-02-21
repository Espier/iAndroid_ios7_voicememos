package org.espier.voicememos7.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SeekBar.OnSeekBarChangeListener;






import org.espier.voicememos7.R;
import org.espier.voicememos7.model.VoiceMemo;
import org.espier.voicememos7.ui.SlideCutListView.RemoveDirection;
import org.espier.voicememos7.ui.SlideCutListView.RemoveListener;
import org.espier.voicememos7.util.MemosUtils;
import org.espier.voicememos7.util.Recorder;




import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class EspierVoiceMemos7 extends Activity implements RemoveListener,OnItemClickListener,OnClickListener
{
    
    public static final int REFRESH = 1;
    public static int LABEL_TYPE_NONE = 0;
    private MediaPlayer mCurrentMediaPlayer;
    TextView finished;
    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    break;
                case 2:
                    
                    
                    break;

                default:
                    break;
            }
        }
        
        
    };
    private RelativeLayout ll1;
    private boolean isCurrentPosition = false;
    private LinearLayout ll2;
    private List<String> dataSourceList = new ArrayList<String>();
    View view ;
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
            mRecorder.pauseRecording();
            waveView.pause();
        }
      
        Timer    timer = new Timer();
        private void start() {
            // TODO Auto-generated method stub
            if (mRecorder.mState == Recorder.RECORDING_STATE) {
                return;
            }
            mRecorder.startRecording(EspierVoiceMemos7.this);
            waveView.start();
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
    private org.espier.voicememos7.ui.VoiceWaveView waveView;
    private org.espier.voicememos7.util.Recorder mRecorder;
    private org.espier.voicememos7.ui.EspierVoiceMemos7.VoiceMemoListAdapter mVoiceMemoListAdapter;  
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
        
        waveView = (VoiceWaveView)findViewById(R.id.waveView);
        waveView.setMinimumWidth(500);
        waveView.setMinimumHeight(500);
        mRecorder = new Recorder();
        waveView.setRecorder(mRecorder);
        finished = (TextView)findViewById(R.id.finished);
        finished.setOnClickListener(this);
        slideCutListView = (SlideCutListView) findViewById(R.id.listView);
        slideCutListView.setRemoveListener(this);
        slideCutListView.setOnItemClickListener(this);
        listViewaddData();
    }
    
    private void listViewaddData() {
        // TODO Auto-generated method stub
        Cursor cs = managedQuery(VoiceMemo.Memos.CONTENT_URI, null, null, null, null);
        mVoiceMemoListAdapter =
            new VoiceMemoListAdapter(EspierVoiceMemos7.this, R.layout.listview_item, cs, new String[] {},
                new int[] {});
        slideCutListView.setAdapter(mVoiceMemoListAdapter);
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
    
    
    float downy = 0;
    private VelocityTracker velocityTracker=VelocityTracker.obtain();
    private boolean isTobottom = false;
    private boolean isfirstdown = true;
    public int ms;
    public int second;
    public int miniute;
    public int mCurrentPosition=-1;
    protected int mCurrentDuration;
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
                    //control page scroll to top limit
                    if (view.getScrollY()+deltaY<0)
                        deltaY = -view.getScrollY();
                        
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
           
            Message msg = new Message();
            msg.what = 1;
            handler.sendMessage(msg);
        }
        
    }
    
    class VoiceMemoListAdapter extends SimpleCursorAdapter {

        private Context mContext;
        private int mMemoIdx;
        private int mPathIdx;
        private int mLabelIdx;
        private int mLabelTypeIdx;
        private int mDurationIdx;
        private int mCreateDateIdx;
        private int mCurrentBgColor;
        private final Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
              switch (msg.what) {
                case REFRESH:
                  long next = refreshNow((ViewHolder) msg.obj);
                  queueNextRefresh(next,(ViewHolder) msg.obj);
                  break;
              }
            }
          };

        public VoiceMemoListAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
          super(context, layout, c, from, to);
          mContext = context;
          mCurrentBgColor = Color.WHITE;
          setupColumnIndices(c);
        }
        
        public View getView(int position, View convertView, ViewGroup parent) {
          Log.d("memo", "getView, mCurrentPosition:" + mCurrentPosition);
          if (mCurrentPosition == position) {
            isCurrentPosition = true;
          } else {
            isCurrentPosition = false;
          }
          return super.getView(position, convertView, parent);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
          View v = super.newView(context, cursor, parent);

          ViewHolder vh = new ViewHolder();
          vh.playControl = (ImageView) v.findViewById(R.id.memos_item_play);
          vh.tag = (TextView) v.findViewById(R.id.memos_item_title);
          vh.createDate = (TextView) v.findViewById(R.id.memos_item_create_date);
          vh.duration = (TextView) v.findViewById(R.id.memos_item_duration);
          vh.id = (TextView) v.findViewById(R.id.memos_item__id);
          vh.path = (TextView) v.findViewById(R.id.memos_item_path);
          vh.bar = (SeekBar)v.findViewById(android.R.id.progress);
          vh.mCurrentRemain = (TextView)v.findViewById(R.id.current_remain);
          vh.mCurrentTime = (TextView)v.findViewById(R.id.current_positon);
         // mCurrentDuration = (Integer)v.findViewById(R.id.memos_item_duration).getTag();
          if (vh.bar instanceof SeekBar) {
              SeekBar seeker = (SeekBar) vh.bar;
              seeker.setOnSeekBarChangeListener(mSeekListener);
            }
          vh.bar.setMax(1000);
          v.setTag(vh);
          v.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                LinearLayout layout = (LinearLayout)v.findViewById(R.id.playlayout);
                if(layout.getVisibility() == View.GONE){
                    layout.setVisibility(View.VISIBLE);
                }else{
                    layout.setVisibility(View.GONE);
                }
                RelativeLayout sharelayout = (RelativeLayout)v.findViewById(R.id.sharelayout);
                if(sharelayout.getVisibility() == View.GONE){
                    sharelayout.setVisibility(View.VISIBLE);
                }else{
                    sharelayout.setVisibility(View.GONE);
                }
                
                System.out.println("item click");
            }
        });
          return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

          final ViewHolder vh = (ViewHolder) view.getTag();
          
          
          vh.tag.setText(cursor.getString(mLabelIdx));

          int secs = cursor.getInt(mDurationIdx);
          if (secs == 0) {
            vh.duration.setText("");
          } else {
            vh.duration.setText(MemosUtils.makeTimeString(context, secs / 1000));
            vh.duration.setTag(secs);
          }

          Long date = cursor.getLong(mCreateDateIdx);
          String dateFormat = getString(R.string.date_time_format);
          int labelType = cursor.getInt(mLabelTypeIdx);
          if (labelType == EspierVoiceMemos7.LABEL_TYPE_NONE ) {
            dateFormat = getString(R.string.date_format);
          }
          SimpleDateFormat format = new SimpleDateFormat(dateFormat);
          Date d = new Date(date);
          String dd = format.format(d);
          vh.createDate.setText(dd);

          final String path = cursor.getString(mPathIdx);
          final Integer id = cursor.getInt(mMemoIdx);
          vh.path.setTag(path);
          vh.id.setTag(id);
         

          File file = new File(path);
          if (!file.exists()) {
            if (isCurrentPosition) {
              view.setBackgroundColor(mCurrentBgColor);
              vh.playControl.setVisibility(View.VISIBLE);
              vh.tag.setTextColor(Color.WHITE);
              vh.createDate.setTextColor(Color.WHITE);
              vh.duration.setTextColor(Color.WHITE);
            }else{
              view.setBackgroundColor(Color.LTGRAY);
              vh.playControl.setVisibility(View.VISIBLE);
              vh.tag.setTextColor(Color.BLACK);
              vh.createDate.setTextColor(Color.GRAY);
              vh.duration.setTextColor(Color.BLUE);
            }
          } else {
            if (isCurrentPosition) {
              vh.playControl.setVisibility(View.VISIBLE);
              vh.tag.setTextColor(Color.WHITE);
              vh.createDate.setTextColor(Color.WHITE);
              vh.duration.setTextColor(Color.WHITE);
            } else {
              vh.tag.setTextColor(Color.BLACK);
              vh.createDate.setTextColor(Color.GRAY);
              vh.duration.setTextColor(Color.BLUE);
              vh.playControl.setImageResource(R.drawable.play);
            }
            
            view.setBackgroundColor(mCurrentBgColor);
           

            vh.playControl.setOnClickListener(new View.OnClickListener() {

              @Override
              public void onClick(View arg0) {
                int state = mRecorder.getState();
//                if (state == Recorder.PLAYING_STATE) {
//                  mRecorder.stopPlayback();
//                }
                if (state == Recorder.IDLE_STATE) {
                  mCurrentMediaPlayer = mRecorder.createMediaPlayer(path);
                  mRecorder.startPlayback();
                  vh.playControl.setImageResource(R.drawable.pause);
                } else if (state == Recorder.PLAYER_PAUSE_STATE) {
                  mRecorder.startPlayback();
                  vh.playControl.setImageResource(R.drawable.pause);
                } else if (state == Recorder.PLAYING_STATE) {
                  mRecorder.pausePlayback();
                  vh.playControl.setImageResource(R.drawable.play);
                }

                mCurrentMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

                  @Override
                  public void onCompletion(MediaPlayer mp) {
                    vh.playControl.setImageResource(R.drawable.play);
                    mRecorder.stopPlayback();
                  }
                });
                mCurrentDuration = (Integer) ((View)(vh.duration)).getTag();
                long next = refreshNow(vh);
                queueNextRefresh(next,vh);

              }
            });
          }
        }

        protected void queueNextRefresh(long delay,ViewHolder vh) {
            if (mRecorder.getState() == Recorder.PLAYING_STATE) {
                Message msg =new Message();
                msg.what = 1;
                msg.obj = vh;
                mHandler.removeMessages(REFRESH);
                mHandler .sendMessageDelayed(msg, delay);
              }
            }

        protected long refreshNow(ViewHolder view) {
            if (mCurrentMediaPlayer == null || mRecorder.getState() != Recorder.PLAYING_STATE) {

                return 500;
              }

              // try {
              long pos = mCurrentMediaPlayer.getCurrentPosition();
              if ((pos >= 0) && (mCurrentDuration > 0)) {
                  view.mCurrentTime.setText(MemosUtils.makeTimeString(EspierVoiceMemos7.this, pos / 1000));
                  view.mCurrentRemain.setText("-"
                    + MemosUtils.makeTimeString(EspierVoiceMemos7.this, ((mCurrentDuration - pos) / 1000)));
                int progress = (int) (1000 * pos / mCurrentDuration);
                view.bar.setProgress(progress);

              } else {
                  view. mCurrentTime.setText("0:00");
                  view.bar.setProgress(1000);
              }
              // calculate the number of milliseconds until the next full second,
              // so
              // the counter can be updated at just the right time
              long remaining = 1000 - (pos % 1000);

              // approximate how often we would need to refresh the slider to
              // move it smoothly
              int width = view.bar.getWidth();
              if (width == 0) width = 320;
              long smoothrefreshtime = mCurrentDuration / width;

              if (smoothrefreshtime > remaining) return remaining;
              if (smoothrefreshtime < 20) return 20;
              return smoothrefreshtime;
              // return 500;
            }

        @Override
        public void changeCursor(Cursor cursor) {

        }

        class ViewHolder {
          ImageView playControl;
          TextView tag;
          TextView createDate;
          TextView duration;
          TextView path;
          TextView id;
          TextView mCurrentTime;
          TextView mCurrentRemain;
          SeekBar bar;
        }

        private void setupColumnIndices(Cursor cursor) {
          if (cursor != null) {
            mLabelIdx = cursor.getColumnIndexOrThrow(VoiceMemo.Memos.LABEL);
            mLabelTypeIdx = cursor.getColumnIndexOrThrow(VoiceMemo.Memos.LABEL_TYPE);
            mDurationIdx = cursor.getColumnIndexOrThrow(VoiceMemo.Memos.DURATION);
            mCreateDateIdx = cursor.getColumnIndexOrThrow(VoiceMemo.Memos.CREATE_DATE);
            mMemoIdx = cursor.getColumnIndexOrThrow(VoiceMemo.Memos._ID);
            mPathIdx = cursor.getColumnIndexOrThrow(VoiceMemo.Memos.DATA);
          }else{
              System.out.println("cursor is null");
          }
        }

      }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub
        System.out.println(arg2+"SSSSSSSS");
    }
    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {}

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
          if (!fromuser) return;
          int pos = mCurrentDuration * progress / 1000;
          mRecorder.seekTo(pos);
        }

        public void onStopTrackingTouch(SeekBar bar) {}
      };
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.finished:
                stop();
                break;
            case R.id.imageView2:
                System.out.println("image be clicked");
                break;
            default:
                break;
        }
    }

    private void stop() {
        // TODO Auto-generated method stub
        mRecorder.stopRecording();
        insertVoiceMemo();
        mVoiceMemoListAdapter.notifyDataSetChanged();
    }
    private void insertVoiceMemo() {
        // TODO Auto-generated method stub

        Resources res = getResources();
        ContentValues cv = new ContentValues();
        long current = System.currentTimeMillis();
        File file = mRecorder.sampleFile();
        long modDate = file.lastModified();
        Date date = new Date(current);
        SimpleDateFormat formatter = new SimpleDateFormat(res.getString(R.string.time_format));
        String title = formatter.format(date);
        // long sampleLengthMillis = mRecorder.sampleLength() * 1000L;
        String filepath = file.getAbsolutePath();
        MediaPlayer mediaPlayer = mRecorder.createMediaPlayer(filepath);
        int duration = mediaPlayer.getDuration();
        mRecorder.stopPlayback();
        if(duration < 1000){
          return;
        }

        cv.put(VoiceMemo.Memos.DATA, filepath);
        cv.put(VoiceMemo.Memos.LABEL, title);
        cv.put(VoiceMemo.Memos.LABEL_TYPE, LABEL_TYPE_NONE);
        cv.put(VoiceMemo.Memos.CREATE_DATE, current);
        cv.put(VoiceMemo.Memos.MODIFICATION_DATE, (int) (modDate / 1000));
        cv.put(VoiceMemo.Memos.DURATION, duration);
        getContentResolver().insert(VoiceMemo.Memos.CONTENT_URI, cv);
      
    }
}
