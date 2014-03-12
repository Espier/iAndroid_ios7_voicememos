
package org.espier.voicememos7.ui;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.database.Cursor;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.sax.TextElementListener;
import android.text.TextPaint;
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
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import org.espier.voicememos7.R;
import org.espier.voicememos7.db.MemosProvider;
import org.espier.voicememos7.model.CheapSoundFile;
import org.espier.voicememos7.model.VoiceMemo;
import org.espier.voicememos7.ui.SlideCutListView.RemoveDirection;
import org.espier.voicememos7.ui.SlideCutListView.RemoveListener;
import org.espier.voicememos7.util.AMRFileUtils;
import org.espier.voicememos7.util.MemosUtils;
import org.espier.voicememos7.util.Recorder;
import org.espier.voicememos7.util.ScalePx;
import org.espier.voicememos7.util.StorageUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

public class EspierVoiceMemos7 extends Activity implements RemoveListener,
        OnClickListener, VoiceMemoListAdapter.OnListViewChangedListener {
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
    private static final int TRIM_REQUEST = 9000;
    private static final int TRIM_DONE = 9001;
    TextView date;
    AudioManager audioManager;
    private Dialog dialog;
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
    int height,width;
    public String mCurrentPath;
    public Integer mCurrentMemoId = -1;
    public String memoName;
    private String memo_name;
    int indexnum;
    private CheapSoundFile mSoundFile = null;
    private File mFile;
    private boolean isEditable = false;
    private boolean firstTime = true;
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
    private final int RECORDING_STATE_INIT = 1;
    private final int RECORDING_STATE_ONGOING = 2;

    private final int MEDIA_STATE_EDIT = 1;
    private final int EDIT_STATE_INIT = 2;
    private final int EDIT_STATE_CROP_REDY = 3;
    private final int EDIT_STATE_CROP_CHANGE = 4;

    private int mediaStatus = 0;
    private int recordingStatus = 1;
    private int editStatus = 2;
    private long toMSeconds = 0;

    // Voice Edit Layout
    private TextView textVoiceNameInEditMode;
    private TextView textVoiceTimeInEditMode;
    private ImageView imageViewVoicePlayInEditMode;
    private ImageView imageViewVoiceCropInEditMode;
    private TextView textViewVoiceEditFinishInEditMode;

    private RelativeLayout layoutCrop;
    private RelativeLayout layoutEdit;
    private VoiceMemo currentEditMemo;
    private TextView textViewCrop;
    private TextView textViewCropCancel;

    RelativeLayout titlelayout;
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

    private OnTouchListener onTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (v instanceof TextView)
                    {
                        TextView tvTextView = (TextView) v;
                        v.setTag(tvTextView.getCurrentTextColor());
                        tvTextView.setTextColor(getResources().getColor(R.color.gray));
                    }
                    else
                    {
                        v.getBackground().setColorFilter(
                                new ColorMatrixColorFilter(BT_SELECTED));
                        v.setBackgroundDrawable(v.getBackground());
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    if (v instanceof TextView)
                    {
                        TextView tvTextView = (TextView) v;
                        tvTextView.setTextColor(Integer.valueOf(v.getTag().toString()));
                    }
                    else
                    {
                        v.getBackground().setColorFilter(
                                new ColorMatrixColorFilter(BT_NOT_SELECTED));
                        v.setBackgroundDrawable(v.getBackground());
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    private OnTouchListener editPlayTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    imageViewVoicePlayInEditMode.getBackground().setColorFilter(
                            new ColorMatrixColorFilter(BT_SELECTED));
                    imageViewVoicePlayInEditMode.setBackgroundDrawable(imageViewVoicePlayInEditMode
                            .getBackground());

                    break;

                case MotionEvent.ACTION_UP:
                    imageViewVoicePlayInEditMode.getBackground().setColorFilter(
                            new ColorMatrixColorFilter(BT_NOT_SELECTED));
                    imageViewVoicePlayInEditMode.setBackgroundDrawable(imageViewVoicePlayInEditMode
                            .getBackground());
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
        txtRecordName.setOnClickListener(this);
        txtRecordName.setOnTouchListener(onTouchListener);
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
        start.setOnTouchListener(onTouchListener);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

    }

    private void initEditLayout()
    {
        RelativeLayout.LayoutParams rlpRecordName = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlpRecordName.setMargins(ScalePx.scalePx(this, 31),
                ScalePx.scalePx(this, 13), 0, 0);
        RelativeLayout.LayoutParams rlpRecordName2 = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlpRecordName2.setMargins(ScalePx.scalePx(this, 31),
                0, 0, 0);
        RelativeLayout.LayoutParams rlpRecordName3 = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlpRecordName3.setMargins(0,
                ScalePx.scalePx(this, 13), ScalePx.scalePx(this, 33), 0);
        rlpRecordName3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);

        textVoiceNameInEditMode = (TextView) findViewById(R.id.edittxtRecordName);
        textVoiceNameInEditMode.setLayoutParams(rlpRecordName);
        textVoiceTimeInEditMode = (TextView) findViewById(R.id.edittxtDate);
        rlpRecordName2.addRule(RelativeLayout.BELOW, R.id.edittxtRecordName);
        textVoiceTimeInEditMode.setLayoutParams(rlpRecordName2);
        imageViewVoiceCropInEditMode = (ImageView) findViewById(R.id.editimage);
        imageViewVoiceCropInEditMode.setLayoutParams(rlpRecordName3);
        imageViewVoicePlayInEditMode = (ImageView) findViewById(R.id.editredButton);
        textViewVoiceEditFinishInEditMode = (TextView) findViewById(R.id.editfinished);
        textViewVoiceEditFinishInEditMode.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        textViewVoiceEditFinishInEditMode.setTextColor(getResources().getColor(
                                R.color.finish_text_color));
                        break;
                    case MotionEvent.ACTION_UP:
                        textViewVoiceEditFinishInEditMode.setTextColor(getResources().getColor(
                                R.color.white));
                    default:
                        break;
                }
                return false;
            }
        });
        imageViewVoicePlayInEditMode.setOnTouchListener(editPlayTouchListener);
        imageViewVoicePlayInEditMode.setOnClickListener(this);
        imageViewVoiceCropInEditMode.setOnClickListener(this);
        textViewVoiceEditFinishInEditMode.setOnClickListener(this);

        layoutEdit = (RelativeLayout) findViewById(R.id.layoutwitheditimage);
        layoutCrop = (RelativeLayout) findViewById(R.id.layoutwithtextview);

        textViewCrop = (TextView) findViewById(R.id.textViewCropEdit);
        textViewCropCancel = (TextView) findViewById(R.id.textViewCropCancel);
        RelativeLayout.LayoutParams rltextViewCropCancel = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rltextViewCropCancel.setMargins(ScalePx.scalePx(this, 31),
                ScalePx.scalePx(this, 13), 0, 0);
        rltextViewCropCancel.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        textViewCropCancel.setLayoutParams(rltextViewCropCancel);

        RelativeLayout.LayoutParams rltextViewCrop = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rltextViewCrop.setMargins(0,
                ScalePx.scalePx(this, 13), ScalePx.scalePx(this, 31), 0);
        rltextViewCrop.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);

        textViewCrop.setLayoutParams(rltextViewCrop);
        textViewCrop.setOnClickListener(this);
        textViewCropCancel.setOnClickListener(this);
        textVoiceNameInEditMode.setOnClickListener(this);

        textViewCrop.setOnTouchListener(onTouchListener);
        textViewCropCancel.setOnTouchListener(onTouchListener);
        textVoiceNameInEditMode.setOnTouchListener(onTouchListener);
    }

    private CharSequence getRecordName() {
        // TODO Auto-generated method stub
        SharedPreferences sp = this.getSharedPreferences("espier", this.MODE_PRIVATE);
        String exitindexs = sp.getString("indexs", "");
        if (exitindexs.equals("") || mVoiceMemoListAdapter.getCount() == 0) {
            memo_name = this.getResources().getString(R.string.record_name).toString();
        } else {
            for (int i = 2; i < 10000; i++) {
                String index = "," + i + ",";
                if (exitindexs.contains(index)) {
                    continue;
                } else {
                    indexnum = i;
                    memo_name = this.getResources().getString(R.string.record_name).toString()
                            + " " + i;
                    break;
                }
            }
        }
        return memo_name;
    }

    private void updateEditModeButtonStatus()
    {
        int state = mRecorder.getState();
        if (state == Recorder.IDLE_STATE) {
            imageViewVoicePlayInEditMode.setBackgroundResource(R.drawable.trim_play);
            imageViewVoiceCropInEditMode.setEnabled(true);
        } else if (state == Recorder.PLAYER_PAUSE_STATE) {
            imageViewVoicePlayInEditMode.setBackgroundResource(R.drawable.trim_play);
            imageViewVoiceCropInEditMode.setEnabled(true);
            waveView.setPlayMode(false);
        } else if (state == Recorder.PLAYING_STATE) {
            imageViewVoicePlayInEditMode.setBackgroundResource(R.drawable.trim_pause);
            imageViewVoiceCropInEditMode.setEnabled(false);
            waveView.setPlayMode(true);
        }
    }

    private void updateUIByCropStatus()
    {
        if (editStatus == EDIT_STATE_INIT)
        {
            layoutCrop.setVisibility(View.GONE);
            layoutEdit.setVisibility(View.VISIBLE);
            textViewVoiceEditFinishInEditMode.setVisibility(View.VISIBLE);
        }
        else if (editStatus == EDIT_STATE_CROP_REDY || editStatus == EDIT_STATE_CROP_CHANGE)
        {
            layoutCrop.setVisibility(View.VISIBLE);
            layoutEdit.setVisibility(View.GONE);
            textViewVoiceEditFinishInEditMode.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.editredButton:// User click play button in Edit mode
            {
                if (editStatus == EDIT_STATE_INIT)
                {
                    long fromMSeconds = waveView.getFromPlayTime();
                    mVoiceMemoListAdapter.playVoiceInViewHolder(currentEditMemo.getMemPath(),
                            fromMSeconds, 0);
                    updateEditModeButtonStatus();
                }
                else {
                    long fromMSeconds = waveView.getFromPlayTime();
                    toMSeconds = waveView.getClip_right_time();
                    mVoiceMemoListAdapter.playVoiceInViewHolder(currentEditMemo.getMemPath(),
                            fromMSeconds, 0);
                    updateEditModeButtonStatus();
                }
            }
                break;
            case R.id.editimage:// User click crop button in edit mode.
            {
                waveView.setViewStatus(VoiceWaveView.VIEW_STATUS_EDIT);
                waveView.resetClipStatus();
                editStatus = EDIT_STATE_CROP_REDY;
                updateUIByCropStatus();
                waveView.invalidate();
            }
                break;
            case R.id.editfinished:// User click finish button in edit mode.
            {
                ScollToBottom();
            }
                break;
            case R.id.edittxtRecordName: {
                changeVoiceMemName();
            }
                break;
            case R.id.txtRecordName: {
                changeVoiceMemName();
            }
                break;
            case R.id.textViewCropCancel: {
                editStatus = EDIT_STATE_INIT;
                waveView.setViewStatus(VoiceWaveView.VIEW_STATUS_TO_EDIT);
                updateUIByCropStatus();
            }
                break;
            case R.id.textViewCropEdit: {
                if (waveView.isVoiceClipped())
                {
                    Intent trimIntent = new Intent(EspierVoiceMemos7.this, MemoTrim.class);
                    trimIntent.putExtra("memoPath", currentEditMemo.getMemPath());
                    trimIntent.putExtra("memoId", currentEditMemo.getMemId());
                    trimIntent.putExtra("memoName", currentEditMemo.getMemName());
                    trimIntent.putExtra("start", waveView.getClip_left_time());
                    trimIntent.putExtra("end", waveView.getClip_right_time());
                    startActivityForResult(trimIntent, TRIM_REQUEST);
                }
                else
                {
                    waveView.setViewStatus(VoiceWaveView.VIEW_STATUS_TO_EDIT);
                    editStatus = EDIT_STATE_INIT;
                    updateUIByCropStatus();
                    waveView.invalidate();
                }
            }
                break;
            case R.id.finished:
                recordingStatus = RECORDING_STATE_INIT;
                if (firstTime) {
                    ScollToBottom();
                    firstTime = false;
                } else {
                    stop();
                }
                break;
            case R.id.redButton:
                mediaStatus = MEDIA_STATE_RECORDING;
                recordingStatus = RECORDING_STATE_ONGOING;
                waveView.setViewStatus(VoiceWaveView.VIEW_STATUS_RECORD);
                firstTime = false;
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
                    if (txtRecordName.getText().toString().equals("")) {
                        txtRecordName.setText(getRecordName());
                    }
                    ScrollToTop();
                }

                break;
            case R.id.editButton:
                // in edit status
                TextView tv = (TextView)findViewById(R.id.editButton);
                if (isEditable) {
                    tv.setText(R.string.edit);
                    mVoiceMemoListAdapter.showOrHiddenDelete(false);
                    isEditable = false;
                } else {
                // in normal status
                    tv.setText(R.string.finish);
                    mVoiceMemoListAdapter.showOrHiddenDelete(true);
                    isEditable = true;
                }
                
//                showOrHiddenDelete();

                break;
            default:
                break;
        }
    }

//    private void showOrHiddenDelete() {
//        // TODO Auto-generated method stub textview changeto editView
//        if (textViewEdit.getText().toString().equals(getResources().getString(R.string.edit))) {
//            // show delete image textview show finish
//            textViewEdit.setText(getResources().getString(R.string.finish));
//            for (int i = 0; i < slideCutListView.getCount(); i++) {
//                View item = slideCutListView.getChildAt(i);
//                ImageView delete = (ImageView) item.findViewById(R.id.deleteimage);
//                delete.setVisibility(View.VISIBLE);
//                RelativeLayout.LayoutParams lpTitle = new RelativeLayout.LayoutParams(
//                        RelativeLayout.LayoutParams.FILL_PARENT,
//                        RelativeLayout.LayoutParams.WRAP_CONTENT);
//                lpTitle.setMargins(ScalePx.scalePx(EspierVoiceMemos7.this, 31),
//                        0, 0, 0);
//                TextView itemname = (TextView) item.findViewById(R.id.memos_item_title);
//                EditText itemtitle = (EditText) item.findViewById(R.id.memos_item_title_editable);
//                itemtitle.setLayoutParams(lpTitle);
//                String title = itemname.getText().toString();
//                itemname.setVisibility(View.INVISIBLE);
//                itemtitle.setTextSize(itemname.getTextSize());
//                itemtitle.setText(title);
//                itemtitle.setVisibility(View.VISIBLE);
//
//            }
//        } else {
//            //
//            textViewEdit.setText(getResources().getString(R.string.edit));
//            for (int i = 0; i < slideCutListView.getCount(); i++) {
//                View item = slideCutListView.getChildAt(i);
//                ImageView delete = (ImageView) item.findViewById(R.id.deleteimage);
//                delete.setVisibility(View.GONE);
//
//                TextView itemname = (TextView) item.findViewById(R.id.memos_item_title);
//                EditText itemtitle = (EditText) item.findViewById(R.id.memos_item_title_editable);
//                String title = itemtitle.getText().toString();
//                itemtitle.setVisibility(View.INVISIBLE);
//                itemname.setText(title);
//                itemname.setVisibility(View.VISIBLE);
//
//            }
//        }
//    }

    @Override
    protected void onResume() {
        super.onResume();

        hiddenView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mediaStatus == MEDIA_STATE_RECORDING
                        && recordingStatus == RECORDING_STATE_ONGOING)
                {
                    return;
                }
                // ScollToBottom();
            }
        });
        hiddenView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mediaStatus == MEDIA_STATE_RECORDING
                        && recordingStatus == RECORDING_STATE_ONGOING)
                {
                    return false;
                }

                if (mediaStatus == MEDIA_STATE_EDIT && editStatus != EDIT_STATE_INIT)
                {
                    return false;
                }

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
                            ScrollToTop();
                        } else {

                            ScollToBottom();
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    private void ScollToBottom() {
        RelativeLayout editLayout = (RelativeLayout) findViewById(R.id.editlayout);
        RelativeLayout playLayout = (RelativeLayout) findViewById(R.id.playlayout);

        editLayout.setVisibility(View.GONE);
        playLayout.setVisibility(View.VISIBLE);

        TextView text = (TextView) findViewById(R.id.txtRecordName);
        mainLayout.scrollTo(0, playLayout.getTop());

        hiddenView.setVisibility(View.INVISIBLE);
        txtRecordName.setVisibility(View.INVISIBLE);
        waveView.setVisibility(View.INVISIBLE);
        date.setVisibility(View.INVISIBLE);
        titlelayout.setVisibility(View.VISIBLE);
        finished.setVisibility(View.INVISIBLE);

    }

    private void ScrollToTop() {
        RelativeLayout editLayout = (RelativeLayout) findViewById(R.id.editlayout);
        RelativeLayout playLayout = (RelativeLayout) findViewById(R.id.playlayout);

        if (mediaStatus == MEDIA_STATE_EDIT)
        {
            editLayout.setVisibility(View.VISIBLE);
            playLayout.setVisibility(View.GONE);
        }
        else if (mediaStatus == MEDIA_STATE_RECORDING)
        {
            editLayout.setVisibility(View.GONE);
            playLayout.setVisibility(View.VISIBLE);
        }

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
        titlelayout = (RelativeLayout) findViewById(R.id.titlelay);

        textViewEdit = (TextView) findViewById(R.id.editButton);
        RelativeLayout.LayoutParams lp = new android.widget.RelativeLayout.LayoutParams(
                android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT,
                android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(ScalePx.scalePx(this, 19), ScalePx.scalePx(this, 28), 0, 0);
        textViewEdit.setLayoutParams(lp);
        textViewEdit.setOnClickListener(this);

        textviewmemo = (TextView) findViewById(R.id.name);
        RelativeLayout.LayoutParams lp1 = new android.widget.RelativeLayout.LayoutParams(
                android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT,
                android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp1.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        lp1.setMargins(ScalePx.scalePx(this, 25), ScalePx.scalePx(this, 28), 0,
                ScalePx.scalePx(this, 36));

        textviewmemo.setLayoutParams(lp1);

        int H = textviewmemo.getHeight();
        sound = (ImageView) findViewById(R.id.sound);

        sound.setScaleType(ScaleType.CENTER_INSIDE);
        sound.setMaxHeight(H);
        RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(
                android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT,
                android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);

        lp3.setMargins(0, ScalePx.scalePx(this, 28), ScalePx.scalePx(this, 45), 0);
        lp3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        sound.setLayoutParams(lp3);
        sound.setOnClickListener((new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (isSoundOn) {
                    audioManager.setMode(AudioManager.STREAM_MUSIC);
                    audioManager.setSpeakerphoneOn(true);
                    isSoundOn = false;
                    sound.setImageResource(R.drawable.volume_blue);
                } else {
                    audioManager.setMode(AudioManager.STREAM_MUSIC);
                    audioManager.setSpeakerphoneOn(false);
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
        finished.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        finished.setTextColor(getResources().getColor(R.color.finish_text_color));
                        break;
                    case MotionEvent.ACTION_UP:
                        finished.setTextColor(getResources().getColor(R.color.white));
                    default:
                        break;
                }
                return false;
            }
        });
        slideCutListView.setRemoveListener(this);
        slideCutListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                // TODO: deal with onItemClick
                Log.d("adf", "inonitemclick");
            }

        });
        date = (TextView) findViewById(R.id.txtDate);
        String datetime = (String) DateFormat.format("yy-M-dd", System.currentTimeMillis());
        date.setText(datetime);

        mVoiceMemoListAdapter.setOnListViewChangedListener(this);
        mVoiceMemoListAdapter.mRecorder = mRecorder;

        this.initEditLayout();

    }

    private void listViewaddData() {
        // TODO Auto-generated method stub
        Cursor cs1 = managedQuery(VoiceMemo.Memos.CONTENT_URI, null, null, null, null);
        while (cs1.moveToNext()) {
            String path = cs1.getString(cs1.getColumnIndexOrThrow("data"));
            if (!AMRFileUtils.isExist(path)) {
                int id = cs1.getInt(cs1.getColumnIndexOrThrow("_id"));
                Uri memoUri = ContentUris.withAppendedId(VoiceMemo.Memos.CONTENT_URI,
                        id);
                getContentResolver().delete(memoUri, null, null);
            }
        }
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
            LinearLayout.LayoutParams llp = new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.FILL_PARENT,
                    android.widget.LinearLayout.LayoutParams.FILL_PARENT);
            emptyView.setLayoutParams(llp);

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
        width = getWindowManager().getDefaultDisplay().getWidth();
        RelativeLayout.LayoutParams relP = new android.widget.RelativeLayout.LayoutParams(
                android.widget.RelativeLayout.LayoutParams.FILL_PARENT,
                (int) (height * 0.9 * 4 / 7 - 30));
        relP.addRule(RelativeLayout.BELOW, R.id.txtMainTitle);
        waveView.setLayoutParams(relP);
        RelativeLayout.LayoutParams relP2 = new android.widget.RelativeLayout.LayoutParams(
                android.widget.RelativeLayout.LayoutParams.FILL_PARENT,
                (int) (height * 0.9 * 2.5 / 7));
        RelativeLayout playLayout = (RelativeLayout) findViewById(R.id.playlayout);
        relP2.addRule(RelativeLayout.BELOW, R.id.waveView);
        playLayout.setLayoutParams(relP2);
        LayoutParams lp1 = aboveLayout.getLayoutParams();
        lp1.height = (int) (height * 8 / 9);
        lp1.width = width;
        aboveLayout.setLayoutParams(lp1);

        LayoutParams lp2 = belowLayout.getLayoutParams();
        lp2.height = (int) (height * 1.2);
        lp2.width = width;
        belowLayout.setLayoutParams(lp2);

        RelativeLayout.LayoutParams lp_list = new android.widget.RelativeLayout.LayoutParams(
                android.widget.RelativeLayout.LayoutParams.FILL_PARENT,
                android.widget.RelativeLayout.LayoutParams.FILL_PARENT);
        lp_list.height = (int) (height * 7 / 10.3 - 10);
        System.out.println("height-playLayout.getHeight() " + (height - playLayout.getHeight()));
        // // lp_list.addRule(RelativeLayout.BELOW,R.id.hiddenView);
        slideCutListView = (SlideCutListView) findViewById(R.id.listView);
        slideCutListView.setLayoutParams(lp_list);

    }

    @Override
    public void removeItem(RemoveDirection direction, int position) {
        adapter.remove(adapter.getItem(position));
        switch (direction) {
            case RIGHT:
                Toast.makeText(this, "?????冲?????  " + position, Toast.LENGTH_SHORT).show();
                break;
            case LEFT:
                Toast.makeText(this, "???宸???????  " + position, Toast.LENGTH_SHORT).show();
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

    private void changeVoiceMemName()
    {
        String voiceMemName = "";
        if (mediaStatus == MEDIA_STATE_EDIT)
        {
            voiceMemName = currentEditMemo.getMemName();
        }
        else if (mediaStatus == MEDIA_STATE_RECORDING)
        {
            voiceMemName = txtRecordName.getText().toString();
        }

        final View view = this.getLayoutInflater().inflate(R.layout.items, null);
        view.setPadding(0, 0, 0, 0);
        // view.setBackgroundColor(getResources().getColor(R.color.blue));
        view.setBackgroundDrawable(getResources().getDrawable(R.drawable.radius));
        RelativeLayout.LayoutParams rellay = new RelativeLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        rellay.setMargins(0, ScalePx.scalePx(this, 30), 0, 0);
        TextView title = (TextView) view.findViewById(R.id.textView1);
        title.setPadding(0, 0, 0, 0);
        title.setWidth(ScalePx.scalePx(this, 540));
        title.setLayoutParams(rellay);

        RelativeLayout.LayoutParams rellay2 = new RelativeLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        rellay2.setMargins(0, ScalePx.scalePx(this, 12), 0, 0);
        rellay2.addRule(RelativeLayout.BELOW, title.getId());
        TextView text2 = (TextView) view.findViewById(R.id.textView2);
        text2.setLayoutParams(rellay2);
        text2.setPadding(0, 0, 0, 0);

        final EditText text = (EditText) view.findViewById(R.id.memoname);
        memoName = voiceMemName;
        text.setText(voiceMemName);
        text.setHeight(ScalePx.scalePx(this, 58));
        text.setWidth(ScalePx.scalePx(this, 478));
        RelativeLayout.LayoutParams textlay = new RelativeLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        textlay.setMargins(ScalePx.scalePx(this, 30), ScalePx.scalePx(this, 30),
                ScalePx.scalePx(this, 30), ScalePx.scalePx(this, 24));
        textlay.addRule(RelativeLayout.BELOW, R.id.textView2);
        text.setLayoutParams(textlay);
        text.setTextSize(ScalePx.scalePx(this, 18));
        text.setPadding(ScalePx.scalePx(this, 16),
                ScalePx.scalePx(this, 16),
                ScalePx.scalePx(this, 16), ScalePx.scalePx(this, 16));
        text.setSingleLine(true);
        text.setSelection(voiceMemName.length());
        text.setBackgroundDrawable(getResources().getDrawable(R.drawable.memoseditteststyle));
        ImageView imag = (ImageView) view.findViewById(R.id.h_line);
        imag.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, ScalePx.scalePx(this, 88)));

        TextView cancel = (TextView) view.findViewById(R.id.cancel);
        TextView ok = (TextView) view.findViewById(R.id.ok);
        cancel.setOnTouchListener(onTouchListener);
        ok.setOnTouchListener(onTouchListener);
        cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                dialogdismiss.sendEmptyMessage(1);
            }
        });

        ok.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (text.getText().toString().equals(""))
                {
                    return;
                }
                if (mediaStatus == MEDIA_STATE_EDIT)
                {
                    textVoiceNameInEditMode.setText(text.getText().toString());
                    MemosUtils.updateVoiceName(getApplicationContext(), text.getText().toString(),
                            Integer.valueOf(currentEditMemo.getMemId()));
                    currentEditMemo = MemosUtils.getMemoByID(getApplicationContext(),
                            currentEditMemo.getMemId());
                }

                if (mediaStatus == MEDIA_STATE_RECORDING)
                {
                    txtRecordName.setText(text.getText().toString());
                }
                dialogdismiss.sendEmptyMessage(1);
            }
        });

        dialog = new Dialog(EspierVoiceMemos7.this, R.style.dialog);
        // dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        RelativeLayout.LayoutParams viewrl = new RelativeLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        viewrl.setMargins(0, 0, 0, 0);
        dialog.setContentView(view, viewrl);
        // dialog.setView(view, 0, 0, 0, 0);
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = ScalePx.scalePx(this, 540);
        lp.height = ScalePx.scalePx(this, 344);
        dialogWindow.setAttributes(lp);
        dialog.show();
    }

    private void stop() {
        int state = mRecorder.getState();
        start.setBackgroundResource(R.drawable.record_red);
        if (state == Recorder.RECORDING_STATE || state == Recorder.RECORDER_PAUSE_STATE) {
            mRecorder.stopRecording();
            waveView.stop();
        } else {
            // return;
        }

        final View view = this.getLayoutInflater().inflate(R.layout.items, null);
        view.setPadding(0, 0, 0, 0);
        // view.setBackgroundColor(getResources().getColor(R.color.blue));
        view.setBackgroundDrawable(getResources().getDrawable(R.drawable.radius));
        RelativeLayout.LayoutParams rellay = new RelativeLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        rellay.setMargins(0, ScalePx.scalePx(this, 30), 0, 0);
        TextView title = (TextView) view.findViewById(R.id.textView1);
        title.setPadding(0, 0, 0, 0);
        title.setWidth(ScalePx.scalePx(this, 540));
        title.setLayoutParams(rellay);

        RelativeLayout.LayoutParams rellay2 = new RelativeLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        rellay2.setMargins(0, ScalePx.scalePx(this, 12), 0, 0);
        rellay2.addRule(RelativeLayout.BELOW, title.getId());
        TextView text2 = (TextView) view.findViewById(R.id.textView2);
        text2.setLayoutParams(rellay2);
        text2.setPadding(0, 0, 0, 0);

        EditText text = (EditText) view.findViewById(R.id.memoname);
        memoName = txtRecordName.getText().toString();
        text.setText(memoName);
        text.setHeight(ScalePx.scalePx(this, 58));
        text.setWidth(ScalePx.scalePx(this, 478));
        RelativeLayout.LayoutParams textlay = new RelativeLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        textlay.setMargins(ScalePx.scalePx(this, 30), ScalePx.scalePx(this, 30),
                ScalePx.scalePx(this, 30), ScalePx.scalePx(this, 24));
        textlay.addRule(RelativeLayout.BELOW, R.id.textView2);
        text.setLayoutParams(textlay);
        text.setTextSize(ScalePx.scalePx(this, 18));
        text.setPadding(ScalePx.scalePx(this, 16),
                ScalePx.scalePx(this, 16),
                ScalePx.scalePx(this, 16), ScalePx.scalePx(this, 16));
        text.setSingleLine(true);
        text.setSelection(txtRecordName.getText().length());
        text.setBackgroundDrawable(getResources().getDrawable(R.drawable.memoseditteststyle));
        ImageView imag = (ImageView) view.findViewById(R.id.h_line);
        imag.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, ScalePx.scalePx(this, 88)));

        TextView cancel = (TextView) view.findViewById(R.id.cancel);
        TextView ok = (TextView) view.findViewById(R.id.ok);
        // ok.setBackgroundColor(getResources().getColor(R.color.red));
        cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dialogdismiss.sendEmptyMessage(1);
                ScollToBottom();
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
                    sp.edit().putString("indexs", exitstring + "," + indexnum + ",").commit();
                }
                insertVoiceMemo(name);
                waveView.clearData();
                if (emptyView != null) {
                    emptyView.setVisibility(View.GONE);
                }
                mVoiceMemoListAdapter.notifyDataSetChanged();
                dialogdismiss.sendEmptyMessage(1);
                txtRecordName.setText("");
                ScollToBottom();
            }
        });
        // AlertDialog.Builder builder = new Builder(EspierVoiceMemos7.this);
        dialog = new Dialog(EspierVoiceMemos7.this, R.style.dialog);
        // dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        RelativeLayout.LayoutParams viewrl = new RelativeLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        viewrl.setMargins(0, 0, 0, 0);
        dialog.setContentView(view, viewrl);
        // dialog.setView(view, 0, 0, 0, 0);
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = ScalePx.scalePx(this, 540);
        lp.height = ScalePx.scalePx(this, 344);
        dialogWindow.setAttributes(lp);
        dialog.show();

    }

    private void insertVoiceMemo(String memoname) {
        // TODO Auto-generated method stub
        System.out.println("insert 1");
        Resources res = getResources();
        ContentValues cv = new ContentValues();
        long current = System.currentTimeMillis();
        File file = mRecorder.sampleFile();
        Date date = new Date(current);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String title = formatter.format(date);
        long modDate = file.lastModified();

        // long sampleLengthMillis = mRecorder.sampleLength() * 1000L;
        String filepath = file.getAbsolutePath();
        String path = filepath.substring(0, filepath.lastIndexOf("/") + 1);
        String newname = path + memoname + "-" + title + ".amr";
        AMRFileUtils.rename(filepath, newname);
        MediaPlayer mediaPlayer = mRecorder.createMediaPlayer(newname);
        if (mediaPlayer == null) {
            return;
        }
        int duration = mediaPlayer.getDuration();
        mRecorder.stopPlayback();
        if (duration < 10) {
            return;
        }
        cv.put(VoiceMemo.Memos.DATA, newname);
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

        if (requestCode == TRIM_REQUEST)
        {
            if (resultCode == TRIM_DONE)
            {
                // Get Extra parameters

                String mMemPath = data.getStringExtra("memoPath");
                currentEditMemo = MemosUtils.getMemoByPath(EspierVoiceMemos7.this, mMemPath);

                onVoiceEditClicked(null, currentEditMemo);

                editStatus = EDIT_STATE_INIT;
                waveView.setViewStatus(VoiceWaveView.VIEW_STATUS_TO_EDIT);
                waveView.setTime_to_edit(0);
                updateUIByCropStatus();
                mVoiceMemoListAdapter.notifyDataSetChanged();
            }
        }
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == DEL_REQUEST) {
                int id = data.getIntExtra("mCurrentMemoId", -1);
                String memopath = data.getStringExtra("memopath");
                deleteMemo(id, memopath);
                mVoiceMemoListAdapter.notifyDataSetChanged();
                mVoiceMemoListAdapter.collapseAllItems();
                slideCutListView.restoreItem();
                mCurrentDuration = 0;
                if (mVoiceMemoListAdapter.getCount() == 0) {
                    if (emptyView != null) {
                        emptyView.setVisibility(View.VISIBLE);
                    }
                }
                TextView tv = (TextView)findViewById(R.id.editButton);
                tv.setVisibility(View.VISIBLE);
                // resetPlayer();
            }
        }
    }

    private void deleteMemo(int memoId, String path) {
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

    final CheapSoundFile.ProgressListener listener =
            new CheapSoundFile.ProgressListener() {
                public boolean reportProgress(double fractionComplete) {
//                    Log.d("Fraction", String.valueOf(fractionComplete));
                    return true;
                }
            };
            
            
    public CheapSoundFile generateSoundFile(String memPath)
    {
        try {
            
            mSoundFile = CheapSoundFile.create(memPath, listener);
            SoundReadTask readTask = new SoundReadTask();
            readTask.execute(memPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mSoundFile;
    }

    class SoundReadTask extends AsyncTask<String, Void, Void>
    {

        @Override
        protected Void doInBackground(String... params) {
            String memPath = params[0];
            File mFile1 = new File(memPath);
            try {
                mSoundFile.ReadFile(mFile1);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mediaStatus = MEDIA_STATE_EDIT;
            RelativeLayout editLayout = (RelativeLayout) findViewById(R.id.editlayout);
            editLayout.setVisibility(View.VISIBLE);

            RelativeLayout playLayout = (RelativeLayout) findViewById(R.id.playlayout);
            playLayout.setVisibility(View.GONE);
            updateEditModeButtonStatus();
            ScrollToTop();
            waveView.setViewStatus(VoiceWaveView.VIEW_STATUS_TO_EDIT);
            waveView.setCheapSoundFile(mSoundFile);
            super.onPostExecute(result);
        }
        
        
        
    }
    @Override
    public void onVoiceEditClicked(CheapSoundFile mSoundFil1e, VoiceMemo memo) {
        CheapSoundFile mSoundFile = generateSoundFile(memo.getMemPath());
        if (mSoundFile == null)
            return;
        

        // int sampleRate = mSoundFile.getSampleRate();
        // int numFrames = mSoundFile.getNumFrames();
        // int totalTime = numFrames *
        // mSoundFile.getSamplesPerFrame()/sampleRate;
        currentEditMemo = memo;
        textVoiceTimeInEditMode.setVisibility(View.VISIBLE);
        textVoiceTimeInEditMode.setText(memo.getMemCreatedDate());
        textVoiceNameInEditMode.setText(memo.getMemName());
    }

    @Override
    public void DisplayEditButton(boolean isDisplay) {
        TextView tvEdit = (TextView) findViewById(R.id.editButton);
        if (isDisplay)
            tvEdit.setVisibility(View.VISIBLE);
        else
            tvEdit.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onPlayStatusChanged(int status, long position)
    {
        if (editStatus == EDIT_STATE_CROP_REDY || editStatus == EDIT_STATE_CROP_CHANGE)
        {
            if (position >= toMSeconds)
            {
                mRecorder.stopPlayback();
                // mRecorder.pausePlayback();
                waveView.setPlayMode(false);
                updateEditModeButtonStatus();
            }
        }
        waveView.setTime_to_edit(position);
        waveView.invalidate();
    }

    @Override
    public void onPlayStopFired()
    {
        editStatus = EDIT_STATE_INIT;
        updateEditModeButtonStatus();
        waveView.setPlayMode(false);
        waveView.setTime_to_end();
        waveView.invalidate();
    }
}
