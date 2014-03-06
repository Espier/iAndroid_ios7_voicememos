
package org.espier.voicememos7.ui;


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
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.sax.TextElementListener;
import android.text.format.DateFormat;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import org.espier.voicememos7.R;
import org.espier.voicememos7.model.CheapSoundFile;
import org.espier.voicememos7.model.VoiceMemo;
import org.espier.voicememos7.ui.SlideCutListView.RemoveDirection;
import org.espier.voicememos7.ui.SlideCutListView.RemoveListener;
import org.espier.voicememos7.util.Recorder;
import org.espier.voicememos7.util.ScalePx;
import org.espier.voicememos7.util.StorageUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

public class EspierVoiceMemos7 extends Activity implements RemoveListener,
        OnClickListener,VoiceMemoListAdapter.OnListViewChangedListener {
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
    private org.espier.voicememos7.ui.VoiceMemoListAdapter mVoiceMemoListAdapter;
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
    
    //Voice Edit Layout
    private TextView textVoiceNameInEditMode;
    private TextView textVoiceTimeInEditMode;
    private ImageView imageViewVoicePlayInEditMode;
    private ImageView imageViewVoiceCropInEditMode;
    private TextView textViewVoiceEditFinishInEditMode;
    
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
        init();
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
       
    }

    private void initEditLayout()
    {
    	textVoiceNameInEditMode = (TextView)findViewById(R.id.edittxtRecordName);
        textVoiceTimeInEditMode = (TextView)findViewById(R.id.edittxtDate);
        imageViewVoiceCropInEditMode = (ImageView)findViewById(R.id.editimage);
        imageViewVoicePlayInEditMode = (ImageView)findViewById(R.id.editredButton);
        textViewVoiceEditFinishInEditMode = (TextView)findViewById(R.id.editfinished);
        
        imageViewVoicePlayInEditMode.setOnClickListener(new View.OnClickListener() {
        
            @Override
            public void onClick(View v) {
            if(mediaStatus == VoiceWaveView.VIEW_STATUS_EDIT)
            {
            	
            }
         }
       });
    }
    
    private CharSequence getRecordName() {
        // TODO Auto-generated method stub
        SharedPreferences sp = this.getSharedPreferences("espier", this.MODE_PRIVATE);
        String exitindexs = sp.getString("indexs", "");
        if (exitindexs.equals("")||mVoiceMemoListAdapter.c.getCount()==0) {
            memo_name = this.getResources().getString(R.string.record_name).toString();
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
                    if(txtRecordName.getText().toString().equals("")){
                        txtRecordName.setText(getRecordName());
                    }
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
    	RelativeLayout editLayout = (RelativeLayout)findViewById(R.id.editlayout);
        editLayout.setVisibility(View.GONE);
        
        RelativeLayout playLayout = (RelativeLayout)findViewById(R.id.playlayout);
        playLayout.setVisibility(View.VISIBLE);
        
        TextView text = (TextView)findViewById(R.id.txtRecordName);
        mainLayout.scrollTo(0, playLayout.getTop() );
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
        slideCutListView = (SlideCutListView) findViewById(R.id.listView);
        listViewaddData();
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
        lp1.setMargins(0, ScalePx.scalePx(this, 28), 0, ScalePx.scalePx(this, 36));
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
        
        slideCutListView.setRemoveListener(this);
        date = (TextView) findViewById(R.id.txtDate);
        String datetime = (String) DateFormat.format("yy-M-dd", System.currentTimeMillis());
        date.setText(datetime);
       
        mVoiceMemoListAdapter.setOnListViewChangedListener(this);
        mVoiceMemoListAdapter.mRecorder = mRecorder;
        
        this.initEditLayout();
         
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
                txtRecordName.setText("");
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

    @Override
    public void onAChanged(Intent intent, int state) {
        startActivityForResult(intent, state);
        
    }

    @Override
    public void onBChanged() {
        
        
    }

    @Override

    public void onVoiceEditClicked(CheapSoundFile mSoundFile) {

        ScrollDown();
        RelativeLayout editLayout = (RelativeLayout)findViewById(R.id.editlayout);
        editLayout.setVisibility(View.VISIBLE);
        
        RelativeLayout playLayout = (RelativeLayout)findViewById(R.id.playlayout);
        playLayout.setVisibility(View.GONE);
        
        waveView.setViewStatus(VoiceWaveView.VIEW_STATUS_TO_EDIT);
        waveView.setCheapSoundFile(mSoundFile);
    }
}
