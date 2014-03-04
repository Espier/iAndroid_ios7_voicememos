
package org.espier.voicememos7.ui;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.espier.voicememos7.R;
import org.espier.voicememos7.model.CheapSoundFile;
import org.espier.voicememos7.model.VoiceMemo;
import org.espier.voicememos7.ui.SlideCutListView.RemoveDirection;
import org.espier.voicememos7.ui.SlideCutListView.RemoveListener;
import org.espier.voicememos7.util.MemosUtils;
import org.espier.voicememos7.util.Recorder;
import org.espier.voicememos7.util.ScalePx;
import org.espier.voicememos7.util.StorageUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

public class EspierVoiceMemos7 extends Activity implements RemoveListener,
        OnClickListener {
    private LinearLayout mainLayout;
    float downy = 0;
    public int ms;
    public int second;
    public int miniute;
    public int mCurrentPosition = -1;
    protected int mCurrentDuration;
    public static final int REFRESH = 1;
    public static int LABEL_TYPE_NONE = 0;
    private MediaPlayer mCurrentMediaPlayer;
    private static final int DEL_REQUEST = 2;
    TextView date;
    AudioManager audioManager;
    private AlertDialog dialog;
    boolean isSoundOn = false;
    TextView finished;
    Boolean isCurrentPosition;
    private Button hiddenView;
    private RelativeLayout aboveLayout;
    private RelativeLayout belowLayout;
    ImageView start;
    protected TimerTask timerTask;
    private org.espier.voicememos7.ui.SlideCutListView slideCutListView;
    private ArrayAdapter<String> adapter;
    private org.espier.voicememos7.ui.VoiceWaveView waveView;
    private org.espier.voicememos7.util.Recorder mRecorder;
    private org.espier.voicememos7.ui.EspierVoiceMemos7.VoiceMemoListAdapter mVoiceMemoListAdapter;
    int height;
    public String mCurrentPath;
    public Integer mCurrentMemoId = -1;
    public String memoName;
    private String memo_name;
    int indexnum;
    private CheapSoundFile mSoundFile;
    private File mFile;
    TextView txtRecordName;
    View emptyView;
    public final float[] BT_SELECTED = new float[] {
            1, 0, 0, 0, -100, 0,
            1, 0, 0, -100, 0, 0, 1, 0, -100, 0, 0, 0, 1, 0
    };
    public final float[] BT_NOT_SELECTED = new float[] {
            1, 0, 0, 0, 0, 0,
            1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0
    };
    
    private final int MEDIA_STATE_RECORDING = 0;
    private final int MEDIA_STATE_EDIT = 1;
    private int mediaStatus = 0;
    
    LinearLayout titlelayout;
    ImageView sound;
    TextView textViewEdit, textviewmemo;
    Handler dialogdismiss = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    break;

                default:
                    break;
            }
        }

    };

    private OnTouchListener startTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    start.getBackground().setColorFilter(
                            new ColorMatrixColorFilter(BT_SELECTED));
                    start.setBackgroundDrawable(start.getBackground());

                    break;

                case MotionEvent.ACTION_UP:
                    start.getBackground().setColorFilter(
                            new ColorMatrixColorFilter(BT_NOT_SELECTED));
                    start.setBackgroundDrawable(start.getBackground());
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_memo_main);

        TextView txtMainTitle = (TextView) findViewById(R.id.txtMainTitle);
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        rlp.setMargins(0, ScalePx.scalePx(this, 29), 0, 0);
        txtMainTitle.setLayoutParams(rlp);

        waveView = (VoiceWaveView) findViewById(R.id.waveView);
        RelativeLayout.LayoutParams rlpWaveView = new RelativeLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        rlpWaveView.setMargins(0, ScalePx.scalePx(this, 40), 0, 0);
        rlpWaveView.addRule(RelativeLayout.BELOW, R.id.txtMainTitle);
        rlpWaveView.height = ScalePx.scalePx(this, 465);
        waveView.setLayoutParams(rlpWaveView);

        txtRecordName = (TextView) findViewById(R.id.txtRecordName);
        RelativeLayout.LayoutParams rlpRecordName = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlpRecordName.setMargins(ScalePx.scalePx(this, 31),
                ScalePx.scalePx(this, 13), 0, 0);
        rlpRecordName.addRule(RelativeLayout.BELOW, R.id.waveView);
        txtRecordName.setLayoutParams(rlpRecordName);
        txtRecordName.setText(getRecordName());
        memoName = txtRecordName.getText().toString();
        TextView txtDate = (TextView) findViewById(R.id.txtDate);
        RelativeLayout.LayoutParams rlpDate = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlpDate.setMargins(ScalePx.scalePx(this, 31),
                ScalePx.scalePx(this, 11), 0, 0);
        rlpDate.addRule(RelativeLayout.BELOW, R.id.txtRecordName);
        txtDate.setLayoutParams(rlpDate);

        // LinearLayout buttonLayout =
        // (LinearLayout)findViewById(R.id.buttonLayout);
        // RelativeLayout.LayoutParams rlpButton = new
        // RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
        // LayoutParams.WRAP_CONTENT);
        // rlpButton.setMargins(
        // 0,
        // ScalePx.scalePx(this, 40), 0, 0);
        // rlpButton.addRule(RelativeLayout.BELOW, R.id.txtDate);
        //
        // buttonLayout.setLayoutParams(rlpButton);

        // TextView txtFinish = (TextView) findViewById(R.id.finished);
        // RelativeLayout.LayoutParams rlpFinish = new
        // RelativeLayout.LayoutParams(
        // LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        // rlpFinish.setMargins(ScalePx.scalePx(this, 60),0, 0, 0);
        // rlpFinish.addRule(RelativeLayout.RIGHT_OF, R.id.buttonLayout);
        // rlpFinish.addRule(RelativeLayout.BELOW, R.id.txtDate);
        // rlpFinish.addRule(RelativeLayout.CENTER_VERTICAL);
        // txtFinish.setLayoutParams(rlpFinish);

        mainLayout = (LinearLayout) findViewById(R.id.mainlayout);
        aboveLayout = (RelativeLayout) findViewById(R.id.aboveLayout);
        belowLayout = (RelativeLayout) findViewById(R.id.belowLayout);
        hiddenView = (Button) findViewById(R.id.hiddenView);
        start = (ImageView) findViewById(R.id.redButton);
        start.setOnClickListener(this);
        start.setOnTouchListener(startTouchListener);

        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        init();
    }

    private CharSequence getRecordName() {
        // TODO Auto-generated method stub
        SharedPreferences sp = this.getSharedPreferences("espier", this.MODE_PRIVATE);
        String exitindexs = sp.getString("indexs", "");
        if (exitindexs.equals("")) {
            memo_name = this.getResources().getString(R.string.record_name).toString() + " ";
        } else {
            for(int i=2;i<10000;i++){
                String index = ","+i+",";
                if(exitindexs.contains(index)){
                    continue;
                }else{
                    indexnum=i;
                    memo_name = this.getResources().getString(R.string.record_name).toString() + " " + i;
                    break;
                }
            }
        }
        return memo_name;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.finished:
                stop();
                break;
            case R.id.redButton:
                if (!StorageUtil.hasDiskSpace()) {
                    Toast.makeText(this, R.string.storage_is_full, Toast.LENGTH_SHORT).show();
                    return;
                } else if (!StorageUtil.isStorageMounted()) {
                    Toast.makeText(this, R.string.insert_sd_card, Toast.LENGTH_SHORT).show();
                    return;
                }

                stopMusic();
                if (mRecorder.getState() == Recorder.RECORDING_STATE) {
                    mRecorder.pauseRecording();

                    start.setBackgroundResource(R.drawable.record_red);
                    waveView.pause();
                } else {
                    mRecorder.startRecording(this);
                    start.setBackgroundResource(R.drawable.stop_red);

                    waveView.start();
                    ScrollDown();
                }

                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        hiddenView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        });
        hiddenView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                final int action = event.getAction();

                int y = (int) event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downy = event.getY();

                        break;
                    case MotionEvent.ACTION_MOVE:

                        int deltaY = (int) (downy - y);

                        if (mainLayout.getScrollY() + deltaY < 0) {
                            deltaY = -mainLayout.getScrollY();
                        }

                        mainLayout.scrollBy(0, deltaY);

                        System.out.println(deltaY);
                        break;
                    case MotionEvent.ACTION_UP:
                        int[] location = new int[2];
                        v.getLocationOnScreen(location);
                        int viewY = location[1];

                        if (viewY > GetScreenCenter() / 2) {
                            ScrollDown();
                        } else {

                            ScrollUp();
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    private void ScrollUp() {
        RelativeLayout ll = (RelativeLayout) findViewById(R.id.playlayout);
        TextView text = (TextView)findViewById(R.id.txtRecordName);
        mainLayout.scrollTo(0, ll.getTop() );
        hiddenView.setVisibility(View.INVISIBLE);
        txtRecordName.setVisibility(View.INVISIBLE);
        waveView.setVisibility(View.INVISIBLE);
        date.setVisibility(View.INVISIBLE);
        titlelayout.setVisibility(View.VISIBLE);
        finished.setVisibility(View.INVISIBLE);

    }

    private void ScrollDown() {
        mainLayout.scrollTo(0, 0);
        if (hiddenView.getVisibility() != View.VISIBLE) {
            hiddenView.setVisibility(View.VISIBLE);
        }
        txtRecordName.setVisibility(View.VISIBLE);
        date.setVisibility(View.VISIBLE);
        waveView.setVisibility(View.VISIBLE);
        titlelayout.setVisibility(View.INVISIBLE);
        finished.setVisibility(View.VISIBLE);

    }

    private int GetScreenCenter() {
        return this.getResources().getDisplayMetrics().heightPixels;
    }

    private void init() {
        titlelayout = (LinearLayout) findViewById(R.id.titlelay);
        textViewEdit = (TextView) findViewById(R.id.edititem);
        LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(ScalePx.scalePx(this, 19), ScalePx.scalePx(this, 28), 0, 0);
        lp.weight = 1;
        textViewEdit.setLayoutParams(lp);
        
        textviewmemo = (TextView) findViewById(R.id.name);
        LinearLayout.LayoutParams lp1 = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        lp1.setMargins(0, ScalePx.scalePx(this, 28), 0, ScalePx.scalePx(this, 56));
        lp1.weight = 1;
        textviewmemo.setLayoutParams(lp1);
        
        int H = textviewmemo.getHeight();
        sound = (ImageView) findViewById(R.id.sound);
        
        sound.setScaleType(ScaleType.CENTER_INSIDE);
        sound.setMaxHeight( H);
        LinearLayout.LayoutParams lp3 = new LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        
        lp3.setMargins(0, ScalePx.scalePx(this, 28), ScalePx.scalePx(this, 0), 0);
        lp3.weight = 1;
        sound.setLayoutParams(lp3);
        sound.setOnClickListener((new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                
                if(isSoundOn) {
                    audioManager.setSpeakerphoneOn(true);
                    isSoundOn = false;
                    sound.setImageResource(R.drawable.volume_blue);
            } else {
                    audioManager.setSpeakerphoneOn(false);//关闭扬声器
                    audioManager.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_EARPIECE, AudioManager.ROUTE_ALL);
                    setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
                    //把声音设定成Earpiece（听筒）出来，设定为正在通话中
                    audioManager.setMode(AudioManager.MODE_IN_CALL);
                    sound.setImageResource(R.drawable.volume_gray);
                    isSoundOn = true;
            }
            }
        }));
        
        waveView = (VoiceWaveView) findViewById(R.id.waveView);
        waveView.setMinimumWidth(500);
        waveView.setMinimumHeight(100);
        mRecorder = new Recorder();
        waveView.setRecorder(mRecorder);
        finished = (TextView) findViewById(R.id.finished);
        finished.setOnClickListener(this);
        slideCutListView = (SlideCutListView) findViewById(R.id.listView);
        slideCutListView.setRemoveListener(this);
        date = (TextView) findViewById(R.id.txtDate);
        String datetime = (String) DateFormat.format("yy-M-dd", System.currentTimeMillis());
        date.setText(datetime);
        listViewaddData();
    }

    private void listViewaddData() {
        // TODO Auto-generated method stub
        Cursor cs = managedQuery(VoiceMemo.Memos.CONTENT_URI, null, null, null, null);
        mVoiceMemoListAdapter =
                new VoiceMemoListAdapter(EspierVoiceMemos7.this, R.layout.listview_item, cs,
                        new String[] {},
                        new int[] {});
        slideCutListView.setAdapter(mVoiceMemoListAdapter);
        if (cs.getCount() == 0) {
            emptyView = getLayoutInflater().inflate(R.layout.listviewempty, null);
            TextView textview = (TextView) emptyView.findViewById(R.id.textView1);
            RelativeLayout.LayoutParams lparam = new android.widget.RelativeLayout.LayoutParams(
                    android.widget.RelativeLayout.LayoutParams.FILL_PARENT,
                    android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
            lparam.setMargins(ScalePx.scalePx(this, 30), 0, 0, 0);
            textview.setLayoutParams(lparam);

            ImageView image1 = (ImageView) emptyView.findViewById(R.id.imageView1);
            RelativeLayout.LayoutParams lp = new android.widget.RelativeLayout.LayoutParams(
                    android.widget.RelativeLayout.LayoutParams.FILL_PARENT,
                    android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(ScalePx.scalePx(this, 30), ScalePx.scalePx(this, 99), 0, 0);
            image1.setLayoutParams(lp);

            ImageView image2 = (ImageView) emptyView.findViewById(R.id.imageView2);
            RelativeLayout.LayoutParams lp2 = new android.widget.RelativeLayout.LayoutParams(
                    android.widget.RelativeLayout.LayoutParams.FILL_PARENT,
                    android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp2.setMargins(ScalePx.scalePx(this, 30), ScalePx.scalePx(this, 99), 0, 0);
            lp2.addRule(RelativeLayout.BELOW, R.id.imageView1);
            image2.setLayoutParams(lp2);

            ImageView image3 = (ImageView) emptyView.findViewById(R.id.imageView3);
            RelativeLayout.LayoutParams lp3 = new android.widget.RelativeLayout.LayoutParams(
                    android.widget.RelativeLayout.LayoutParams.FILL_PARENT,
                    android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp3.setMargins(ScalePx.scalePx(this, 30), ScalePx.scalePx(this, 99), 0, 0);
            lp3.addRule(RelativeLayout.BELOW, R.id.imageView2);
            image3.setLayoutParams(lp3);

            ImageView image4 = (ImageView) emptyView.findViewById(R.id.imageView4);
            RelativeLayout.LayoutParams lp4 = new android.widget.RelativeLayout.LayoutParams(
                    android.widget.RelativeLayout.LayoutParams.FILL_PARENT,
                    android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp4.setMargins(ScalePx.scalePx(this, 30), ScalePx.scalePx(this, 99), 0, 0);
            lp4.addRule(RelativeLayout.BELOW, R.id.imageView3);
            image4.setLayoutParams(lp4);

            ImageView image5 = (ImageView) emptyView.findViewById(R.id.imageView5);
            RelativeLayout.LayoutParams lp5 = new android.widget.RelativeLayout.LayoutParams(
                    android.widget.RelativeLayout.LayoutParams.FILL_PARENT,
                    android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp5.setMargins(ScalePx.scalePx(this, 30), ScalePx.scalePx(this, 99), 0, 0);
            lp5.addRule(RelativeLayout.BELOW, R.id.imageView4);
            image5.setLayoutParams(lp5);

            ImageView image6 = (ImageView) emptyView.findViewById(R.id.imageView6);
            RelativeLayout.LayoutParams lp6 = new android.widget.RelativeLayout.LayoutParams(
                    android.widget.RelativeLayout.LayoutParams.FILL_PARENT,
                    android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp6.setMargins(ScalePx.scalePx(this, 30), ScalePx.scalePx(this, 99), 0, 0);
            lp6.addRule(RelativeLayout.BELOW, R.id.imageView5);
            image6.setLayoutParams(lp6);

            ((ViewGroup) slideCutListView.getParent()).addView(emptyView);
            slideCutListView.setEmptyView(emptyView);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);
        Rect rect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        int top = rect.top;//

        height = getWindowManager().getDefaultDisplay().getHeight() - top;
        int width = getWindowManager().getDefaultDisplay().getWidth();

        LayoutParams lp1 = aboveLayout.getLayoutParams();
        lp1.height = (int) (height * 0.9);
        lp1.width = width;
        aboveLayout.setLayoutParams(lp1);

        LayoutParams lp2 = belowLayout.getLayoutParams();
        lp2.height = (int) (height * 0.9);
        lp2.width = width;
        belowLayout.setLayoutParams(lp2);

    }

    @Override
    public void removeItem(RemoveDirection direction, int position) {
        adapter.remove(adapter.getItem(position));
        switch (direction) {
            case RIGHT:
                Toast.makeText(this, "向右删除  " + position, Toast.LENGTH_SHORT).show();
                break;
            case LEFT:
                Toast.makeText(this, "向左删除  " + position, Toast.LENGTH_SHORT).show();
                break;

            default:
                break;
        }

    }

    class VoiceMemoListAdapter extends SimpleCursorAdapter {
        private List list = new ArrayList<View>();
        private Context mContext;
        private int mMemoIdx;
        private int mPathIdx;
        private int mLabelIdx;
        private int mLabelTypeIdx;
        private int mDurationIdx;
        private int mCreateDateIdx;
        private int mCurrentBgColor;
        Cursor c;
        private final Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case REFRESH:
                        long next = refreshNow((ViewHolder) msg.obj);
                        queueNextRefresh(next, (ViewHolder) msg.obj);
                        break;
                }
            }
        };
        protected View lastView = null;
        protected boolean isClose = false;

        public VoiceMemoListAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
            super(context, layout, c, from, to);
            mContext = context;
            mCurrentBgColor = Color.WHITE;
            this.c = c;
            setupColumnIndices(c);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return c.getCount();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
//            Log.d("memo", "getView, mCurrentPosition:" + mCurrentPosition);
//            if (mCurrentPosition == position) {
//                isCurrentPosition = true;
//            } else {
//                isCurrentPosition = false;
//            }
            return super.getView(position, convertView, parent);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = super.newView(context, cursor, parent);

            final ViewHolder vh = new ViewHolder();
            vh.playControl = (ImageView) v.findViewById(R.id.memos_item_play);
            vh.tag = (EditText) v.findViewById(R.id.memos_item_title);
            vh.createDate = (TextView) v.findViewById(R.id.memos_item_create_date);
            vh.duration = (TextView) v.findViewById(R.id.memos_item_duration);
            vh.id = (TextView) v.findViewById(R.id.memos_item__id);
            vh.path = (TextView) v.findViewById(R.id.memos_item_path);
            vh.bar = (SeekBar) v.findViewById(android.R.id.progress);
            vh.mCurrentRemain = (TextView) v.findViewById(R.id.current_remain);
            vh.mCurrentTime = (TextView) v.findViewById(R.id.current_positon);
            vh.share = (ImageView) v.findViewById(R.id.share);
            vh.del = (ImageView) v.findViewById(R.id.del);
            vh.edit = (TextView) v.findViewById(R.id.edit);
            
            RelativeLayout.LayoutParams lpTitle = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            lpTitle.setMargins(ScalePx.scalePx(EspierVoiceMemos7.this, 31), 
                    ScalePx.scalePx(EspierVoiceMemos7.this, 13), 0, 0);
            vh.tag.setLayoutParams(lpTitle);
            
            RelativeLayout.LayoutParams lpCreateDate = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            lpCreateDate.setMargins(ScalePx.scalePx(EspierVoiceMemos7.this, 31), 
                    ScalePx.scalePx(EspierVoiceMemos7.this, 13), 0, 0);
            lpCreateDate.addRule(RelativeLayout.BELOW,R.id.memos_item_title);
            vh.createDate.setLayoutParams(lpCreateDate);
            
            RelativeLayout.LayoutParams lpDuration = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            lpDuration.setMargins(ScalePx.scalePx(EspierVoiceMemos7.this, 54), 
                    ScalePx.scalePx(EspierVoiceMemos7.this, 13), 0, 0);
            lpDuration.addRule(RelativeLayout.RIGHT_OF, R.id.memos_item_create_date);
            lpDuration.addRule(RelativeLayout.BELOW,R.id.memos_item_title);
            vh.duration.setLayoutParams(lpDuration);
//            
//            LinearLayout.LayoutParams lpPlay = new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.WRAP_CONTENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT);
//            lpPlay.setMargins(ScalePx.scalePx(EspierVoiceMemos7.this, 36), 
//                    ScalePx.scalePx(EspierVoiceMemos7.this, 13), 0, 0);
//            vh.playControl.setLayoutParams(lpPlay);
//            
//            LinearLayout.LayoutParams lpLeftTime = new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.WRAP_CONTENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT);
//            lpLeftTime.setMargins(ScalePx.scalePx(EspierVoiceMemos7.this, 36), 
//                    0, 0, 0);
//            vh.mCurrentTime.setLayoutParams(lpLeftTime);
//            
//            LinearLayout.LayoutParams lpSeekBar = new LinearLayout.LayoutParams(
//                    ScalePx.scalePx(EspierVoiceMemos7.this, 340),
//                    LinearLayout.LayoutParams.WRAP_CONTENT);
//            lpSeekBar.setMargins(ScalePx.scalePx(EspierVoiceMemos7.this, 18), 
//                    ScalePx.scalePx(EspierVoiceMemos7.this, 13), 0, 0);
//            vh.bar.setLayoutParams(lpSeekBar);
//            
//            LinearLayout.LayoutParams lpRightTime = new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.WRAP_CONTENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT);
//            lpRightTime.setMargins(ScalePx.scalePx(EspierVoiceMemos7.this, 18), 
//                    0, 0, 0);
//            vh.mCurrentRemain.setLayoutParams(lpRightTime);
//            
//            RelativeLayout.LayoutParams lpLine = new RelativeLayout.LayoutParams(
//                    RelativeLayout.LayoutParams.WRAP_CONTENT,
//                    RelativeLayout.LayoutParams.WRAP_CONTENT);
//            lpLine.setMargins(ScalePx.scalePx(EspierVoiceMemos7.this, 36), 
//                    ScalePx.scalePx(EspierVoiceMemos7.this, 38), 0, 0);
//            ImageView imgLine = (ImageView)v.findViewById(R.id.line);
//            
//            imgLine.setLayoutParams(lpLine);
            
            
            
            
            // mCurrentDuration =
            // (Integer)v.findViewById(R.id.memos_item_duration).getTag();
            if (vh.bar instanceof SeekBar) {
                SeekBar seeker = (SeekBar) vh.bar;
                seeker.setOnSeekBarChangeListener(mSeekListener);
            }
            vh.bar.setMax(1000);

//            vh.tag.setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//
//                    // v.setFocusable(true);
//                    // v.requestFocus();
//                }
//            });
//            vh.tag.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//
//                @Override
//                public void onFocusChange(View v, boolean hasFocus) {
//                    // if (hasFocus) {
//                    // v.clearFocus();
//                    // InputMethodManager imm =
//                    // (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//                    // imm.hideSoftInputFromWindow(v.getWindowToken(),0);
//                    // }
//
//                }
//            });
            v.setTag(vh);
            v.setOnClickListener(new View.OnClickListener() {

                @SuppressLint("ResourceAsColor")
                @Override
                public void onClick(View v) {
                    Log.d("adf","click Cell View");
                    if (lastView != null && isClose) {
                        vh.tag.setTextColor(R.color.black);
                        vh.createDate.setTextColor(R.color.black);
                        vh.duration.setTextColor(R.color.black);
//                        sound.setImageResource(R.drawable.volume_blue);
                        LinearLayout layout = (LinearLayout) lastView.findViewById(R.id.playlayout);
                        layout.setVisibility(View.GONE);
                        RelativeLayout sharelayout = (RelativeLayout) lastView
                                .findViewById(R.id.sharelayout);
                        sharelayout.setVisibility(View.GONE);
                        isClose = false;
                        vh.bar.setProgress(0);
                        vh.tag.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(vh.tag.getWindowToken(), 0);
                        int i = list.size();
                        for (int j = 0; j < i; j++) {
                            View view = (View) list.get(j);

                            view.setBackgroundColor(Color.WHITE);
                        }
                        return;
                    }
                    //sound.setImageResource(R.drawable.volume_gray);
                    LinearLayout layout = (LinearLayout) v.findViewById(R.id.playlayout);
                    layout.setVisibility(View.VISIBLE);

                    RelativeLayout sharelayout = (RelativeLayout) v.findViewById(R.id.sharelayout);
                    sharelayout.setVisibility(View.VISIBLE);

                    vh.mCurrentRemain.setText("-" + vh.duration.getText());
                    isClose = true;
                    int i = list.size();
                    for (int j = 0; j < i; j++) {
                        View view = (View) list.get(j);
                        if (v == view) {
                            
                            continue;
                        }
                        

                        view.setBackgroundColor(getResources().getColor(R.color.light_gray));
                        EditText et = (EditText) view.findViewById(R.id.memos_item_title);
                        et.setTextColor(R.color.heavygray);
                        TextView tvCreateDate = (TextView)view.findViewById(R.id.memos_item_create_date);
                        TextView tvDuration = (TextView)view.findViewById(R.id.memos_item_duration);
                        tvCreateDate.setTextColor(R.color.heavygray);
                        tvDuration.setTextColor(R.color.heavygray);
                    }

                    lastView = v;

                }
            });
            list.add(v);

            return v;
        }

        @Override
        public void bindView(View view, final Context context, final Cursor cursor) {
            System.out.println( cursor.getString(mLabelIdx)+"    "+cursor.getString(mPathIdx));
            final ViewHolder vh = (ViewHolder) view.getTag();
            final String itemname = cursor.getString(mLabelIdx);
            vh.tag.setText(itemname);

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
            if (labelType == EspierVoiceMemos7.LABEL_TYPE_NONE) {
                dateFormat = getString(R.string.date_format);
            }
            SimpleDateFormat format = new SimpleDateFormat(dateFormat);
            Date d = new Date(date);
            String dd = format.format(d);
            vh.createDate.setText(dd);

            final String path = cursor.getString(mPathIdx);
            final int memoid = cursor.getInt(mMemoIdx);
            final Integer id = cursor.getInt(mMemoIdx);
            vh.path.setTag(path);
            vh.id.setTag(id);

            mCurrentMemoId = (Integer) view.findViewById(R.id.memos_item__id).getTag();
            mCurrentPath = (String) view.findViewById(R.id.memos_item_path).getTag();
            view.setBackgroundColor(mCurrentBgColor);
            vh.share.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    // MemosUtils.shareMemo(EspierVoiceMemos7.this,
                    // mCurrentPath);
                    Intent intent = new Intent(EspierVoiceMemos7.this, MemoShare.class);
                    intent.putExtra("path", mCurrentPath);
                    context.startActivity(intent);
                }
            });
            vh.edit.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mediaStatus = MEDIA_STATE_EDIT;
                    start.setBackgroundResource(R.drawable.trim_play);
                    refreshNow(vh);
                    ScrollDown();
                    try {
                    	mFile = new File(mCurrentPath);
						mSoundFile = CheapSoundFile.create(mCurrentPath, null);
						mSoundFile.ReadFile(mFile);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
                    
                    int[] framGains = mSoundFile.getFrameGains();
                    int sampleRate = mSoundFile.getSampleRate();
                    int numFrames = mSoundFile.getNumFrames();
                    double []gainHeights = computeGainHeights();
                }
            });

            vh.del.setEnabled(true);
            vh.del.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (mRecorder.getState() != Recorder.IDLE_STATE) {
                        mRecorder.stopPlayback();
                    }
                    Intent delIntent = new Intent(EspierVoiceMemos7.this, MemoDelete.class);
                    delIntent.putExtra("mCurrentMemoId", memoid);
                    delIntent.putExtra("memoname", itemname);
                    delIntent.putExtra("memopath", path);
                    startActivityForResult(delIntent, DEL_REQUEST);
                }
            });
            vh.playControl.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    int state = mRecorder.getState();
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
                    mCurrentDuration = (Integer) ((View) (vh.duration)).getTag();
                    long next = refreshNow(vh);
                    queueNextRefresh(next, vh);

                }
            });
            // }
        }

        protected void queueNextRefresh(long delay, ViewHolder vh) {
            if (mRecorder.getState() == Recorder.PLAYING_STATE) {
                Message msg = new Message();
                msg.what = 1;
                msg.obj = vh;
                mHandler.removeMessages(REFRESH);
                mHandler.sendMessageDelayed(msg, delay);
            }
        }

        private double[] computeGainHeights()
        {
        	int numFrames = mSoundFile.getNumFrames();
            int[] frameGains = mSoundFile.getFrameGains();
            double[] smoothedGains = new double[numFrames];
            if (numFrames == 1) {
                smoothedGains[0] = frameGains[0];
            } else if (numFrames == 2) {
                smoothedGains[0] = frameGains[0];
                smoothedGains[1] = frameGains[1];
            } else if (numFrames > 2) {
                smoothedGains[0] = (double)(
                    (frameGains[0] / 2.0) +
                    (frameGains[1] / 2.0));
                for (int i = 1; i < numFrames - 1; i++) {
                    smoothedGains[i] = (double)(
                        (frameGains[i - 1] / 3.0) +
                        (frameGains[i    ] / 3.0) +
                        (frameGains[i + 1] / 3.0));
                }
                smoothedGains[numFrames - 1] = (double)(
                    (frameGains[numFrames - 2] / 2.0) +
                    (frameGains[numFrames - 1] / 2.0));
            }

            // Make sure the range is no more than 0 - 255
            double maxGain = 1.0;
            for (int i = 0; i < numFrames; i++) {
                if (smoothedGains[i] > maxGain) {
                    maxGain = smoothedGains[i];
                }
            }
            double scaleFactor = 1.0;
            if (maxGain > 255.0) {
                scaleFactor = 255 / maxGain;
            }        

            // Build histogram of 256 bins and figure out the new scaled max
            maxGain = 0;
            int gainHist[] = new int[256];
            for (int i = 0; i < numFrames; i++) {
                int smoothedGain = (int)(smoothedGains[i] * scaleFactor);
                if (smoothedGain < 0)
                    smoothedGain = 0;
                if (smoothedGain > 255)
                    smoothedGain = 255;

                if (smoothedGain > maxGain)
                    maxGain = smoothedGain;

                gainHist[smoothedGain]++;
            }

            // Re-calibrate the min to be 5%
            double minGain = 0;
            int sum = 0;
            while (minGain < 255 && sum < numFrames / 20) {
                sum += gainHist[(int)minGain];
                minGain++;
            }

            // Re-calibrate the max to be 99%
            sum = 0;
            while (maxGain > 2 && sum < numFrames / 100) {
                sum += gainHist[(int)maxGain];
                maxGain--;
            }

            // Compute the heights
            double[] heights = new double[numFrames];
            double range = maxGain - minGain;
            for (int i = 0; i < numFrames; i++) {
                double value = (smoothedGains[i] * scaleFactor - minGain) / range;
                if (value < 0.0)
                    value = 0.0;
                if (value > 1.0)
                    value = 1.0;
                heights[i] = value * value;
            }
            return heights;
        }
        
        protected long refreshNow(ViewHolder view) {
            if (mCurrentMediaPlayer == null || mRecorder.getState() != Recorder.PLAYING_STATE) {

                return 500;
            }

            // try {
            long pos = mCurrentMediaPlayer.getCurrentPosition();
            if ((pos >= 0) && (mCurrentDuration > 0)) {
                view.mCurrentTime.setText(MemosUtils.makeTimeString(EspierVoiceMemos7.this,
                        pos / 1000));
                view.mCurrentRemain.setText("-"
                        + MemosUtils.makeTimeString(EspierVoiceMemos7.this,
                                ((mCurrentDuration - pos) / 1000)));
                int progress = (int) (1000 * pos / mCurrentDuration);
                view.bar.setProgress(progress);

            } else {
                view.mCurrentTime.setText("0:00");
                view.bar.setProgress(1000);
            }
            long remaining = 1000 - (pos % 1000);

            int width = view.bar.getWidth();
            if (width == 0)
                width = 320;
            long smoothrefreshtime = mCurrentDuration / width;

            if (smoothrefreshtime > remaining)
                return remaining;
            if (smoothrefreshtime < 20)
                return 20;
            return smoothrefreshtime;
        }

        @Override
        public void changeCursor(Cursor cursor) {

        }

        class ViewHolder {
            ImageView playControl;
            EditText tag;
            TextView createDate;
            TextView duration;
            TextView path;
            TextView id;
            TextView mCurrentTime;
            TextView mCurrentRemain;
            SeekBar bar;
            ImageView share;
            ImageView del;
            TextView edit;
        }

        private void setupColumnIndices(Cursor cursor) {
            if (cursor != null) {
                mLabelIdx = cursor.getColumnIndexOrThrow(VoiceMemo.Memos.LABEL);
                mLabelTypeIdx = cursor.getColumnIndexOrThrow(VoiceMemo.Memos.LABEL_TYPE);
                mDurationIdx = cursor.getColumnIndexOrThrow(VoiceMemo.Memos.DURATION);
                mCreateDateIdx = cursor.getColumnIndexOrThrow(VoiceMemo.Memos.CREATE_DATE);
                mMemoIdx = cursor.getColumnIndexOrThrow(VoiceMemo.Memos._ID);
                mPathIdx = cursor.getColumnIndexOrThrow(VoiceMemo.Memos.DATA);
            } else {
                System.out.println("cursor is null");
            }
        }

    }

    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser)
                return;
            int pos = mCurrentDuration * progress / 1000;
            mRecorder.seekTo(pos);
        }

        public void onStopTrackingTouch(SeekBar bar) {
        }
    };

    private void stop() {
        // TODO Auto-generated method stub
        int state = mRecorder.getState();
        start.setBackgroundResource(R.drawable.record_red);
        if (state == Recorder.RECORDING_STATE || state == Recorder.RECORDER_PAUSE_STATE) {
            mRecorder.stopRecording();
            waveView.stop();
        } else {
//            return;
        }
        AlertDialog.Builder builder = new Builder(EspierVoiceMemos7.this);
        final View view = this.getLayoutInflater().inflate(R.layout.items, null);
        RelativeLayout.LayoutParams rellay = new RelativeLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        rellay.setMargins(0, ScalePx.scalePx(this, 42), 0, 0);
        TextView title = (TextView) view.findViewById(R.id.textView1);
        title.setWidth(ScalePx.scalePx(this, 540));
        title.setLayoutParams(rellay);
        RelativeLayout.LayoutParams rellay2 = new RelativeLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        rellay2.setMargins(0, ScalePx.scalePx(this, 18), 0, 0);
        rellay2.addRule(RelativeLayout.BELOW, title.getId());
        TextView text2 = (TextView) view.findViewById(R.id.textView2);
        text2.setLayoutParams(rellay2);
        EditText text = (EditText) view.findViewById(R.id.memoname);
        memoName = txtRecordName.getText().toString();
        text.setText(memoName);
        text.setHeight(ScalePx.scalePx(this, 58));
        text.setWidth(ScalePx.scalePx(this, 478));
        RelativeLayout.LayoutParams textlay = new RelativeLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        textlay.setMargins(ScalePx.scalePx(this, 30), ScalePx.scalePx(this, 48),
                ScalePx.scalePx(this, 30), ScalePx.scalePx(this, 29));
        textlay.addRule(RelativeLayout.BELOW, R.id.textView2);
        text.setLayoutParams(textlay);
        ImageView imag = (ImageView) view.findViewById(R.id.h_line);
        imag.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, ScalePx.scalePx(this, 88)));

        TextView cancel = (TextView) view.findViewById(R.id.cancel);
        TextView ok = (TextView) view.findViewById(R.id.ok);
        cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dialogdismiss.sendEmptyMessage(1);
                return;
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String name
                = ((EditText) view.findViewById(R.id.memoname)).getText().toString();
                if (memo_name.equals(name)) {
                    SharedPreferences sp = EspierVoiceMemos7.this.getSharedPreferences("espier",
                            EspierVoiceMemos7.MODE_PRIVATE);

                    int num = sp.getInt("Counter", 1);
                    sp.edit().putInt("Counter", num + 1).commit();
                    String exitstring = sp.getString("indexs", "");
                    sp.edit().putString("indexs", exitstring+","+indexnum+",").commit();
                }
                insertVoiceMemo(name);
                waveView.clearData();
                if (emptyView != null) {
                    emptyView.setVisibility(View.GONE);
                }

                mVoiceMemoListAdapter.notifyDataSetChanged();
                dialogdismiss.sendEmptyMessage(1);
                txtRecordName.setText(getRecordName());
                ScrollUp();
            }
        });

        dialog = builder.create();
        dialog.setView(view, 0, 0, 0, 0);
        dialog.show();

    }

    private void insertVoiceMemo(String memoname) {
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
        if (mediaPlayer == null) {
            return;
        }
        int duration = mediaPlayer.getDuration();
        mRecorder.stopPlayback();
        if (duration < 1000) {
            return;
        }

        cv.put(VoiceMemo.Memos.DATA, filepath);
        cv.put(VoiceMemo.Memos.LABEL, memoname);
        cv.put(VoiceMemo.Memos.LABEL_TYPE, LABEL_TYPE_NONE);
        cv.put(VoiceMemo.Memos.CREATE_DATE, current);
        cv.put(VoiceMemo.Memos.MODIFICATION_DATE, (int) (modDate / 1000));
        cv.put(VoiceMemo.Memos.DURATION, duration);
        getContentResolver().insert(VoiceMemo.Memos.CONTENT_URI, cv);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == DEL_REQUEST) {
                int id = data.getIntExtra("mCurrentMemoId",-1);
                String memopath = data.getStringExtra("memopath");
                deleteMemo(id, memopath);
                mVoiceMemoListAdapter.notifyDataSetChanged();
                mCurrentDuration = 0;
                if(mVoiceMemoListAdapter.c.getCount() ==0){
                    if (emptyView != null) {
                        emptyView.setVisibility(View.VISIBLE);
                    }
                }
                // resetPlayer();
            }
        }
    }

    private void deleteMemo(int memoId,String path) {
        // TODO Auto-generated method stub
        Uri memoUri = ContentUris.withAppendedId(VoiceMemo.Memos.CONTENT_URI,
                memoId);
        System.out.println(memoUri.toString());
        getContentResolver().delete(memoUri, null, null);
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        mCurrentPosition = -1;

    }

    private void stopMusic() {
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        sendBroadcast(i);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        waveView.destroy();
        waveView = null;
        super.onDestroy();
    }
}
