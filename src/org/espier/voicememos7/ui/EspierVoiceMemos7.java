
package org.espier.voicememos7.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.espier.voicememos7.R;
import org.espier.voicememos7.model.VoiceMemo;
import org.espier.voicememos7.ui.SlideCutListView.RemoveDirection;
import org.espier.voicememos7.ui.SlideCutListView.RemoveListener;
import org.espier.voicememos7.util.MemosUtils;
import org.espier.voicememos7.util.Recorder;
import org.espier.voicememos7.util.ScalePx;
import org.espier.voicememos7.util.StorageUtil;

import java.io.File;
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

    private AlertDialog dialog;

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

    public final float[] BT_SELECTED = new float[] {
            1, 0, 0, 0, -100, 0,
            1, 0, 0, -100, 0, 0, 1, 0, -100, 0, 0, 0, 1, 0
    };
    public final float[] BT_NOT_SELECTED = new float[] {
            1, 0, 0, 0, 0, 0,
            1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0
    };

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

        VoiceWaveView waveView = (VoiceWaveView) findViewById(R.id.waveView);
        RelativeLayout.LayoutParams rlpWaveView = new RelativeLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        rlpWaveView.setMargins(0, ScalePx.scalePx(this, 40), 0, 0);
        rlpWaveView.addRule(RelativeLayout.BELOW, R.id.txtMainTitle);
        rlpWaveView.height = ScalePx.scalePx(this, 465);
        waveView.setLayoutParams(rlpWaveView);

        TextView txtRecordName = (TextView) findViewById(R.id.txtRecordName);
        RelativeLayout.LayoutParams rlpRecordName = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlpRecordName.setMargins(ScalePx.scalePx(this, 31),
                ScalePx.scalePx(this, 13), 0, 0);
        rlpRecordName.addRule(RelativeLayout.BELOW, R.id.waveView);
        txtRecordName.setLayoutParams(rlpRecordName);
        memoName = txtRecordName.getText().toString();
        TextView txtDate = (TextView) findViewById(R.id.txtDate);
        RelativeLayout.LayoutParams rlpDate = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlpDate.setMargins(ScalePx.scalePx(this, 31),
                ScalePx.scalePx(this, 11), 0, 0);
        rlpDate.addRule(RelativeLayout.BELOW, R.id.txtRecordName);
        txtDate.setLayoutParams(rlpDate);

//         LinearLayout buttonLayout =
//         (LinearLayout)findViewById(R.id.buttonLayout);
//         RelativeLayout.LayoutParams rlpButton = new
//         RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
//         LayoutParams.WRAP_CONTENT);
//         rlpButton.setMargins(
//         0,
//         ScalePx.scalePx(this, 40), 0, 0);
//         rlpButton.addRule(RelativeLayout.BELOW, R.id.txtDate);
//        
//         buttonLayout.setLayoutParams(rlpButton);

//        TextView txtFinish = (TextView) findViewById(R.id.finished);
//        RelativeLayout.LayoutParams rlpFinish = new RelativeLayout.LayoutParams(
//                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//        rlpFinish.setMargins(ScalePx.scalePx(this, 60),0, 0, 0);
//        rlpFinish.addRule(RelativeLayout.RIGHT_OF, R.id.buttonLayout);
//        rlpFinish.addRule(RelativeLayout.BELOW, R.id.txtDate);
//        rlpFinish.addRule(RelativeLayout.CENTER_VERTICAL);
//        txtFinish.setLayoutParams(rlpFinish);
        
        
        mainLayout = (LinearLayout) findViewById(R.id.mainlayout);
        aboveLayout = (RelativeLayout) findViewById(R.id.aboveLayout);
        belowLayout = (RelativeLayout) findViewById(R.id.belowLayout);
        hiddenView = (Button) findViewById(R.id.hiddenView);
        start = (ImageView) findViewById(R.id.redButton);
        start.setOnClickListener(this);
        start.setOnTouchListener(startTouchListener);

        init();
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
        LinearLayout ll = (LinearLayout) findViewById(R.id.buttonLayout);
        mainLayout.scrollTo(0, ll.getTop());
        hiddenView.setVisibility(View.INVISIBLE);

        finished.setVisibility(View.INVISIBLE);

    }

    private void ScrollDown() {
        mainLayout.scrollTo(0, 0);
        if (hiddenView.getVisibility() != View.VISIBLE) {
            hiddenView.setVisibility(View.VISIBLE);
        }

        finished.setVisibility(View.VISIBLE);

    }

    private int GetScreenCenter() {
        return this.getResources().getDisplayMetrics().heightPixels;
    }

    private void init() {

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

            final ViewHolder vh = new ViewHolder();
            vh.playControl = (ImageView) v.findViewById(R.id.memos_item_play);
            vh.tag = (TextView) v.findViewById(R.id.memos_item_title);
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
            // mCurrentDuration =
            // (Integer)v.findViewById(R.id.memos_item_duration).getTag();
            if (vh.bar instanceof SeekBar) {
                SeekBar seeker = (SeekBar) vh.bar;
                seeker.setOnSeekBarChangeListener(mSeekListener);
            }
            vh.bar.setMax(1000);
            v.setTag(vh);
            v.setOnClickListener(new View.OnClickListener() {

                @SuppressLint("ResourceAsColor")
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (lastView != null && isClose) {
                        LinearLayout layout = (LinearLayout) lastView.findViewById(R.id.playlayout);
                        layout.setVisibility(View.GONE);
                        RelativeLayout sharelayout = (RelativeLayout) lastView
                                .findViewById(R.id.sharelayout);
                        sharelayout.setVisibility(View.GONE);
                        isClose = false;
                        vh.bar.setProgress(0);
                        int i = list.size();
                        for (int j = 0; j < i; j++) {
                            View view = (View) list.get(j);

                            view.setBackgroundColor(Color.WHITE);
                        }
                        return;
                    }
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
                    }

                    lastView = v;

                }
            });
            list.add(v);
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
            if (labelType == EspierVoiceMemos7.LABEL_TYPE_NONE) {
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

            mCurrentMemoId = (Integer) view.findViewById(R.id.memos_item__id).getTag();
            mCurrentPath = (String) view.findViewById(R.id.memos_item_path).getTag();
            view.setBackgroundColor(mCurrentBgColor);
            vh.share.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    MemosUtils.shareMemo(EspierVoiceMemos7.this, mCurrentPath);
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
                    delIntent.putExtra("memoname", vh.tag.getText());
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
            TextView tag;
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
        } else {
            return;
        }
        AlertDialog.Builder builder = new Builder(EspierVoiceMemos7.this);
        final View view = this.getLayoutInflater().inflate(R.layout.items, null);
        EditText text = (EditText) view.findViewById(R.id.memoname);
        text.setText(memoName);
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

                insertVoiceMemo(name);
                waveView.clearData();
                mVoiceMemoListAdapter.notifyDataSetChanged();
                dialogdismiss.sendEmptyMessage(1);
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
                deleteMemo(mCurrentMemoId);
                mVoiceMemoListAdapter.notifyDataSetChanged();
                mCurrentDuration = 0;
                // resetPlayer();
            }
        }
    }

    private void deleteMemo(int memoId) {
        // TODO Auto-generated method stub
        Uri memoUri = ContentUris.withAppendedId(VoiceMemo.Memos.CONTENT_URI,
                memoId);
        getContentResolver().delete(memoUri, null, null);
        File file = new File(mCurrentPath);
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
