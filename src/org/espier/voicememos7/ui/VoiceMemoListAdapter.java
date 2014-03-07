
package org.espier.voicememos7.ui;

import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

import org.espier.voicememos7.R;
import org.espier.voicememos7.model.CheapSoundFile;
import org.espier.voicememos7.model.VoiceMemo;
import org.espier.voicememos7.util.AMRFileUtils;
import org.espier.voicememos7.util.MemosUtils;
import org.espier.voicememos7.util.Recorder;
import org.espier.voicememos7.util.ScalePx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class VoiceMemoListAdapter extends SimpleCursorAdapter {
    private List<View> list = new ArrayList<View>();
    private Context mContext;
    private int mMemoIdx;
    private int mPathIdx;
    private int mLabelIdx;
    private int mLabelTypeIdx;
    private int mDurationIdx;
    private int mCreateDateIdx;
    private int mCurrentBgColor;
    public String mCurrentPath;
    public Integer mCurrentMemoId = -1;
    private CheapSoundFile mSoundFile;
    private static final int REFRESH = 1;
    private final int MEDIA_STATE_EDIT = 1;
    private static final int DEL_REQUEST = 2;
    protected int mCurrentDuration;
    public MediaPlayer mCurrentMediaPlayer;
    private int mediaStatus = 0;
    private File mFile;
    public OnSeekBarChangeListener mSeekListener;
    public org.espier.voicememos7.util.Recorder mRecorder;
    Cursor c;
    protected View openedListViewItem = null;
    protected boolean isCollapsed = true;
    ViewHolder currentViewHolder;
    VoiceMemo currentMemo;

    public interface OnListViewChangedListener {
        public void onAChanged(Intent intent, int state);

        public void onVoiceEditClicked(CheapSoundFile mSoundFile, VoiceMemo memos);

        public void DisplayEditButton(boolean isDisplay);

        void onPlayStatusChanged(int status, long position);

        void onPlayStopFired();

    }

    OnListViewChangedListener mOnListViewChangedListener = null;

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

    public void setOnListViewChangedListener(OnListViewChangedListener listener) {
        mOnListViewChangedListener = listener;
    }

    private void setAChanged(Intent intentA, int request) {
        if (mOnListViewChangedListener != null)
            mOnListViewChangedListener.onAChanged(intentA, request);
    }

    private void DisplayEditButton(boolean isDisplay) {
        if (mOnListViewChangedListener != null)
            mOnListViewChangedListener.DisplayEditButton(isDisplay);
    }

    private void setOnVoiceEditClicked(CheapSoundFile mSoundFile, VoiceMemo memos) {

        Log.d("asdf", "in C event");
        if (mOnListViewChangedListener != null)
            mOnListViewChangedListener.onVoiceEditClicked(mSoundFile, memos);
    }

    private void notifyPlayCompletion()
    {
        if (mOnListViewChangedListener != null)
        {
            mOnListViewChangedListener.onPlayStopFired();
        }
    }

    private void setOnPlayPositionChanged(int status, long position)
    {
        if (mOnListViewChangedListener != null)
            mOnListViewChangedListener.onPlayStatusChanged(status, position);
    }

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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView==null)
//            Log.d("getView","exist view:"+String.valueOf(convertView.toString())+","+String.valueOf(position));
//        else
            Log.d("getView","new view is null"+","+String.valueOf(position));
        return super.getView(position, null, parent);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View view = super.newView(context, cursor, parent);
        //Log.d("newView","view:"+String.valueOf(view.toString()));
        ViewHolder holder = new ViewHolder();
        holder.playControl = (ImageView) view.findViewById(R.id.memos_item_play);
        holder.txtRecordName = (EditText) view.findViewById(R.id.memos_item_title);
        holder.createDate = (TextView) view.findViewById(R.id.memos_item_create_date);
        holder.duration = (TextView) view.findViewById(R.id.memos_item_duration);
        holder.id = (TextView) view.findViewById(R.id.memos_item__id);
        holder.path = (TextView) view.findViewById(R.id.memos_item_path);
        holder.bar = (SeekBar) view.findViewById(android.R.id.progress);
        holder.mCurrentRemain = (TextView) view.findViewById(R.id.current_remain);
        holder.mCurrentTime = (TextView) view.findViewById(R.id.current_positon);
        holder.cellGrayLine = (ImageView) view.findViewById(R.id.cell_GaryLine);
        holder.share = (ImageView) view.findViewById(R.id.share);
        holder.edit = (TextView) view.findViewById(R.id.edit);
        holder.del = (ImageView) view.findViewById(R.id.del);
        holder.position = cursor.getPosition();
        view.setTag(holder);
        RelativeLayout.LayoutParams lpTitle = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lpTitle.setMargins(ScalePx.scalePx(mContext, 31),
                ScalePx.scalePx(mContext, 13), 0, 0);
        holder.txtRecordName.setLayoutParams(lpTitle);

        RelativeLayout.LayoutParams lpCreateDate = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lpCreateDate.setMargins(ScalePx.scalePx(mContext, 31),
                ScalePx.scalePx(mContext, 0), 0, 0);
        lpCreateDate.addRule(RelativeLayout.BELOW, R.id.memos_item_title);
        holder.createDate.setLayoutParams(lpCreateDate);

        RelativeLayout.LayoutParams lpDuration = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lpDuration.setMargins(ScalePx.scalePx(mContext, 54),
                ScalePx.scalePx(mContext, 0), 0, 0);
        lpDuration.addRule(RelativeLayout.RIGHT_OF, R.id.memos_item_create_date);
        lpDuration.addRule(RelativeLayout.BELOW, R.id.memos_item_title);
        holder.duration.setLayoutParams(lpDuration);

        LinearLayout.LayoutParams lpPlay = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lpPlay.setMargins(ScalePx.scalePx(mContext, 36),
                ScalePx.scalePx(mContext, 13), 0, 0);
        holder.playControl.setLayoutParams(lpPlay);

        LinearLayout.LayoutParams lpLeftTime = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lpLeftTime.setMargins(ScalePx.scalePx(mContext, 36),
                0, 0, 0);
        holder.mCurrentTime.setLayoutParams(lpLeftTime);

        LinearLayout.LayoutParams lpSeekBar = new LinearLayout.LayoutParams(
                ScalePx.scalePx(mContext, 340),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lpSeekBar.setMargins(ScalePx.scalePx(mContext, 18),
                ScalePx.scalePx(mContext, 13), 0, 0);
        holder.bar.setLayoutParams(lpSeekBar);

        LinearLayout.LayoutParams lpRightTime = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lpRightTime.setMargins(ScalePx.scalePx(mContext, 18),
                0, 0, 0);
        holder.mCurrentRemain.setLayoutParams(lpRightTime);

        LinearLayout.LayoutParams lpLine = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lpLine.setMargins(ScalePx.scalePx(mContext, 36),
                ScalePx.scalePx(mContext, 38), 0, 0);
        holder.cellGrayLine.setLayoutParams(lpLine);

        LinearLayout.LayoutParams lpShare = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lpShare.setMargins(ScalePx.scalePx(mContext, 32),
                ScalePx.scalePx(mContext, 16), 0,
                ScalePx.scalePx(mContext, 24));
        lpShare.weight = 0;
        holder.share.setLayoutParams(lpShare);

        LinearLayout.LayoutParams lpEdit = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lpEdit.setMargins(0,
                ScalePx.scalePx(mContext, 16), 0, ScalePx.scalePx(mContext, 24));
        lpEdit.weight = 1;
        lpEdit.gravity = Gravity.CENTER_VERTICAL;
        holder.edit.setLayoutParams(lpEdit);

        LinearLayout.LayoutParams lpDelete = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lpDelete.setMargins(0,
                ScalePx.scalePx(mContext, 16),
                ScalePx.scalePx(mContext, 32),
                0);
        lpDelete.weight = 0;
        holder.del.setLayoutParams(lpDelete);
        list.add(view);
        
        return view;
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        final int position = cursor.getPosition();
        //Log.d("bindView", "view:" + view.toString());
        final ViewHolder holder = (ViewHolder) view.getTag();

        final String itemname = cursor.getString(mLabelIdx);
        final int secs = cursor.getInt(mDurationIdx);
        final Long date = cursor.getLong(mCreateDateIdx);
        final int labelType = cursor.getInt(mLabelTypeIdx);
        final String path = cursor.getString(mPathIdx);
        final int memoid = cursor.getInt(mMemoIdx);
        if(!AMRFileUtils.isExist(path)){
            return;
        }
        holder.path.setTag(path);
        holder.id.setTag(memoid);

        mCurrentMemoId = (Integer) view.findViewById(R.id.memos_item__id).getTag();
        mCurrentPath = (String) view.findViewById(R.id.memos_item_path).getTag();

        holder.txtRecordName.setClickable(false);
        holder.txtRecordName.setFocusable(false);
        holder.txtRecordName.setTag(itemname);
        String displayString = MemosUtils.Ellipsize(itemname);
        // TODO: do not remember to change view.toString to displayString
        holder.txtRecordName.setText(displayString);
        if (displayString.equals(itemname)) {

        }
        

        if (secs == 0) {
            holder.duration.setText("");
        } else {
            holder.duration.setText(MemosUtils.makeTimeString(context, secs / 1000));
            holder.duration.setTag(secs);
        }

        String dateFormat = mContext.getString(R.string.date_time_format);
        if (labelType == EspierVoiceMemos7.LABEL_TYPE_NONE) {
            dateFormat = mContext.getString(R.string.date_format);
        }
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        Date d = new Date(date);
        final String dd = format.format(d);
        holder.createDate.setText(dd);

        
        if (holder.bar instanceof SeekBar) {
            SeekBar seeker = (SeekBar) holder.bar;
            seeker.setOnSeekBarChangeListener(mSeekListener);
        }
        holder.bar.setMax(1000);

        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (position != holder.position)
                    return;
                // close view
                if (openedListViewItem != null && isCollapsed == false) {
                    openedListViewItem = null;
                    DisplayEditButton(true);
                    holder.txtRecordName.setTextColor(mContext.getResources().getColor(
                            R.color.black));
                    holder.createDate.setTextColor(mContext.getResources().getColor(R.color.black));
                    holder.duration.setTextColor(mContext.getResources().getColor(R.color.black));

                    setItemVisible(openedListViewItem, false);

                    isCollapsed = true;
                    holder.bar.setProgress(0);
                    holder.txtRecordName.clearFocus();
                    InputMethodManager imm = (InputMethodManager) mContext
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(holder.txtRecordName.getWindowToken(), 0);
                    for (int j = 0; j < list.size(); j++) {
                        View view = (View) list.get(j);

                        EditText etTitle = (EditText) view.findViewById(R.id.memos_item_title);
                        TextView tvCreateDate = (TextView) view
                                .findViewById(R.id.memos_item_create_date);
                        TextView tvDuration = (TextView) view
                                .findViewById(R.id.memos_item_duration);
                        etTitle.setTextColor(mContext.getResources().getColor(R.color.black));
                        tvCreateDate.setTextColor(mContext.getResources().getColor(R.color.black));
                        tvDuration.setTextColor(mContext.getResources().getColor(R.color.black));
                        view.setBackgroundColor(Color.WHITE);
                    }
                    return;
                }
                // expand the view
                DisplayEditButton(false);
                setItemVisible(v, true);

                holder.mCurrentRemain.setText("-" + holder.duration.getText());
                isCollapsed = false;
                for (int j = 0; j < list.size(); j++) {
                    View view = (View) list.get(j);
                    if (v == view) {

                        continue;
                    }

                    view.setBackgroundColor(mContext.getResources()
                            .getColor(R.color.light_gray));
                    EditText et = (EditText) view.findViewById(R.id.memos_item_title);
                    et.setTextColor(mContext.getResources().getColor(R.color.heavygray));
                    TextView tvCreateDate = (TextView) view
                            .findViewById(R.id.memos_item_create_date);
                    TextView tvDuration = (TextView) view
                            .findViewById(R.id.memos_item_duration);
                    tvCreateDate.setTextColor(mContext.getResources().getColor(
                            R.color.heavygray));
                    tvDuration
                            .setTextColor(mContext.getResources().getColor(R.color.heavygray));
                }

                openedListViewItem = v;

            }
        });

        if (!isCollapsed && view != openedListViewItem) {
//            Log.d("in not collapsed", "view id =" + view.toString());
//            Log.d("in not collapsed", "openedListViewItem id =" + openedListViewItem.toString());
            view.setBackgroundColor(mContext.getResources()
                    .getColor(R.color.light_gray));
            holder.txtRecordName.setTextColor(mContext.getResources().getColor(R.color.heavygray));
            holder.createDate.setTextColor(mContext.getResources().getColor(R.color.heavygray));
            holder.duration.setTextColor(mContext.getResources().getColor(R.color.heavygray));
        }
        holder.txtRecordName.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if (hasFocus) {
                    String strInput = (String) holder.txtRecordName.getTag();
                    holder.txtRecordName.setText(strInput);
                    holder.txtRecordName.setSelection(strInput.length());
                }
            }
        });

        // view.setBackgroundColor(mCurrentBgColor);
        holder.share.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // MemosUtils.shareMemo(mContext,
                // mCurrentPath);
                Intent intent = new Intent(mContext, MemoShare.class);
                intent.putExtra("path", path);
                context.startActivity(intent);
            }
        });
        holder.edit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            	setOnPlayPositionChanged(0, 0);
                mediaStatus = MEDIA_STATE_EDIT;
                currentViewHolder = holder;
                try {
                    mFile = new File(path);
                    mSoundFile = CheapSoundFile.create(path, null);
                    mSoundFile.ReadFile(mFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                if(currentMemo!=null&&!String.valueOf(memoid).endsWith(currentMemo.getMemId()))
                {
                	mRecorder.stopPlayback();
                }
                // int[] framGains = mSoundFile.getFrameGains();
                // int sampleRate = mSoundFile.getSampleRate();
                // int numFrames = mSoundFile.getNumFrames();
                // // double []gainHeights = computeGainHeights();
                //
                // double time = (mSoundFile.getSamplesPerFrame() *
                // numFrames)/sampleRate;
                VoiceMemo memo = new VoiceMemo();
                memo.setMemId(String.valueOf(memoid));
                memo.setMemCreatedDate(dd);
                memo.setMemName(itemname);
                memo.setMemPath(path);
                memo.setMemDuration(secs);
                currentMemo = memo;
                setOnVoiceEditClicked(mSoundFile, memo);
            }
        });

        holder.del.setEnabled(true);
        holder.del.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (mRecorder.getState() != Recorder.IDLE_STATE) {
                    mRecorder.stopPlayback();
                }
                Intent delIntent = new Intent(mContext, MemoDelete.class);
                delIntent.putExtra("mCurrentMemoId", memoid);
                delIntent.putExtra("memoname", itemname);
                delIntent.putExtra("memopath", path);
                setAChanged(delIntent, DEL_REQUEST);
                // startActivityForResult(delIntent, DEL_REQUEST);
            }
        });
        holder.playControl.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                currentViewHolder = holder;
                playVoiceInViewHolder(path, 0, 0);
            }
        });
        // }
    }

    private void collapseAllItems() {

    }

    private void setItemVisible(View itemView, boolean isVisible) {

        LinearLayout layout = (LinearLayout) itemView.findViewById(R.id.playlayout);
        LinearLayout sharelayout = (LinearLayout) itemView.findViewById(R.id.sharelayout);
        if (isVisible) {

            layout.setVisibility(View.VISIBLE);
            sharelayout.setVisibility(View.VISIBLE);
        } else {

            layout.setVisibility(View.GONE);
            sharelayout.setVisibility(View.GONE);
        }
    }

    public void playVoiceInViewHolder(String path, long from, long to)
    {
        int state = mRecorder.getState();
        if (state == Recorder.IDLE_STATE) {
            mCurrentMediaPlayer = mRecorder.createMediaPlayer(path);
            if (from > 0)
            {
                mRecorder.seekTo((int) from);
            }
            mRecorder.startPlayback();
            currentViewHolder.playControl.setImageResource(R.drawable.pause);
        } else if (state == Recorder.PLAYER_PAUSE_STATE) {
            if (from > 0)
            {
                mRecorder.seekTo((int) from);
            }
            mRecorder.startPlayback();
            currentViewHolder.playControl.setImageResource(R.drawable.pause);
        } else if (state == Recorder.PLAYING_STATE) {
            mRecorder.pausePlayback();
            currentViewHolder.playControl.setImageResource(R.drawable.play);
        }

        mCurrentMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                currentViewHolder.playControl.setImageResource(R.drawable.play);
                mRecorder.stopPlayback();
                notifyPlayCompletion();
            }
        });
        mCurrentDuration = (Integer) ((View) (currentViewHolder.duration)).getTag();
        long next = refreshNow(currentViewHolder);
        queueNextRefresh(next, currentViewHolder);
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
        setOnPlayPositionChanged(0, pos);
        if ((pos >= 0) && (mCurrentDuration > 0)) {
            view.mCurrentTime.setText(MemosUtils.makeTimeString(mContext,
                    pos / 1000));
            view.mCurrentRemain.setText("-"
                    + MemosUtils.makeTimeString(mContext,
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

    final static class ViewHolder {
        ImageView playControl;
        EditText txtRecordName;
        TextView createDate;
        TextView duration;
        TextView path;
        TextView id;
        ImageView cellGrayLine;
        TextView mCurrentTime;
        TextView mCurrentRemain;
        SeekBar bar;
        ImageView share;
        ImageView del;
        TextView edit;
        int position;
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
