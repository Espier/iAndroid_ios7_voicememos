
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
import android.view.View;
import android.view.ViewGroup;
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
import java.util.ArrayList;
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
    private Cursor cursor;
    private int expandedPosition = -1;
    protected boolean isCollapsed = true;
    ViewHolder currentViewHolder;
    VoiceMemo currentMemo;
    private long currentPos = 0;

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
        this.cursor = c;
        setupColumnIndices(c);
    }
    
//    
//
//    @Override
//    public void changeCursor(Cursor c) {
//        // TODO Auto-generated method stub
//        super.changeCursor(c);
//        list.clear();
//    }

    @Override
    public int getCount() {

        return cursor.getCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        return super.getView(position, convertView, parent);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View view = super.newView(context, cursor, parent);
        ViewHolder holder = new ViewHolder();
        holder.playControl = (ImageView) view.findViewById(R.id.memos_item_play);
        holder.txtRecordName = (TextView) view.findViewById(R.id.memos_item_title);
        holder.txtRecordNameEditable = (EditText) view.findViewById(R.id.memos_item_title_editable);
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

        RelativeLayout.LayoutParams lpTitle = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lpTitle.setMargins(ScalePx.scalePx(mContext, 31),
                ScalePx.scalePx(mContext, 13), 0, 0);
        holder.txtRecordName.setLayoutParams(lpTitle);

        holder.txtRecordNameEditable.setVisibility(View.GONE);
        
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

        view.setTag(holder);
        Log.d("add view to list","view:"+String.valueOf(view.toString()));
        list.add(view);
        return view;
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        final ViewHolder holder = (ViewHolder) view.getTag();

        final String itemname = cursor.getString(mLabelIdx);
        final int secs = cursor.getInt(mDurationIdx);
        final Long date = cursor.getLong(mCreateDateIdx);
        final int labelType = cursor.getInt(mLabelTypeIdx);
        final String path = cursor.getString(mPathIdx);
        final int memoid = cursor.getInt(mMemoIdx);
        if (!AMRFileUtils.isExist(path)) {
            return;
        }
        holder.path.setTag(path);
        holder.id.setTag(memoid);

        mCurrentMemoId = (Integer) view.findViewById(R.id.memos_item__id).getTag();
        mCurrentPath = (String) view.findViewById(R.id.memos_item_path).getTag();

        holder.txtRecordName.setClickable(false);
        holder.txtRecordName.setFocusable(false);
        holder.txtRecordName.setTag(itemname);
        String displayString = MemosUtils.Ellipsize(itemname,context);
//        String viewAddr = view.toString().substring(27, view.toString().length())+"@";
        holder.txtRecordName.setText(displayString);
        if (displayString.equals(itemname)) {

        }

        if (secs == 0) {
            holder.duration.setText("");
        } else {
            holder.duration.setText(MemosUtils.makeTimeString(context, secs / 1000));
            holder.duration.setTag(secs);
        }

        final String strDate = MemosUtils.makeDateString(context, labelType, date);
        holder.createDate.setText(strDate);

        if (holder.bar instanceof SeekBar) {
            SeekBar seeker = (SeekBar) holder.bar;
            seeker.setOnSeekBarChangeListener(mSeekListener);
        }
        holder.bar.setMax(1000);

//        view.setOnClickListener(new OnClickItem(cursor.getPosition()));
//        holder.txtRecordName.setOnFocusChangeListener(new OnClickRecordName(holder));
        holder.share.setOnClickListener(new OnClickShare(context, path));
        holder.edit.setOnClickListener(new OnClickEdit(path, secs, holder, itemname, strDate,
                memoid));
        holder.del.setEnabled(true);
        holder.del.setOnClickListener(new OnClickDelete(path, itemname, memoid));
        holder.playControl.setOnClickListener(new OnClickPlay(holder, path));
        
        if (isCollapsed) {
            view.setBackgroundColor(mContext.getResources()
                    .getColor(R.color.white));
            holder.txtRecordName.setTextColor(mContext.getResources().getColor(R.color.black));
            holder.createDate.setTextColor(mContext.getResources().getColor(R.color.black));
            holder.duration.setTextColor(mContext.getResources().getColor(R.color.black));
        } else {
            if (cursor.getPosition() == expandedPosition) {

                view.setBackgroundColor(mContext.getResources()
                        .getColor(R.color.white));
                holder.txtRecordName.setTextColor(mContext.getResources().getColor(R.color.black));
                holder.createDate.setTextColor(mContext.getResources().getColor(R.color.black));
                holder.duration.setTextColor(mContext.getResources().getColor(R.color.black));
                setItemVisible(view, true);
            } else {
                view.setBackgroundColor(mContext.getResources()
                        .getColor(R.color.light_gray));
                holder.txtRecordName.setTextColor(mContext.getResources().getColor(
                        R.color.heavygray));
                holder.createDate.setTextColor(mContext.getResources().getColor(R.color.heavygray));
                holder.duration.setTextColor(mContext.getResources().getColor(R.color.heavygray));
                setItemVisible(view, false);
            }
        }

    }

    public void collapseAllItems() {
        for (int j = 0; j < list.size(); j++) {
            
            View view = (View) list.get(j);
            setItemVisible(view, false);
            ViewHolder itemHolder = (ViewHolder) view.getTag();
            itemHolder.txtRecordName.setTextColor(mContext.getResources().getColor(R.color.black));
            itemHolder.createDate.setTextColor(mContext.getResources().getColor(R.color.black));
            itemHolder.duration.setTextColor(mContext.getResources().getColor(R.color.black));
            view.setBackgroundColor(Color.WHITE);
        }
        expandedPosition = -1;
        isCollapsed = true;
    }

    private void expandItem(View v) {
        for (int j = 0; j < list.size(); j++) {
            View view = (View) list.get(j);
            if (v == view) {
                continue;
            }
            ViewHolder itemHolder = (ViewHolder) view.getTag();
            itemHolder.txtRecordName.setTextColor(mContext.getResources().getColor(
                    R.color.heavygray));
            itemHolder.createDate.setTextColor(mContext.getResources().getColor(R.color.heavygray));
            itemHolder.duration.setTextColor(mContext.getResources().getColor(R.color.heavygray));

            view.setBackgroundColor(mContext.getResources()
                    .getColor(R.color.light_gray));
        }
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

    private final class OnClickRecordName implements View.OnFocusChangeListener {
        private final ViewHolder holder;

        private OnClickRecordName(ViewHolder holder) {
            this.holder = holder;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                String strInput = (String) holder.txtRecordName.getTag();
                holder.txtRecordName.setText(strInput);
                //holder.txtRecordName.setSelection(strInput.length());
            }
        }
    }

    private final class OnClickItem implements View.OnClickListener {
        
        private int position;
        public OnClickItem(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            // if click the expanded item, ignore the click operation
            ViewHolder holder = (ViewHolder) v.getTag();
            if (expandedPosition == position)
                return;

            // if status is not collapsed(expanded), and the item clicked is not
            // the expanded one,
            // then collapse it, and restore the color to white and black.
            if (expandedPosition >= 0 && isCollapsed == false) {
                DisplayEditButton(true);
                collapseAllItems();
                holder.bar.setProgress(0);
                expandedPosition = -1;
                isCollapsed = true;
                return;
            }
            // expand the view
            DisplayEditButton(false);
            setItemVisible(v, true);
            holder.mCurrentRemain.setText("-" + holder.duration.getText());
            isCollapsed = false;
            expandItem(v);
            expandedPosition =  position;
        }
    }

    private final class OnClickPlay implements View.OnClickListener {
        private final ViewHolder holder;
        private final String path;

        private OnClickPlay(ViewHolder holder, String path) {
            this.holder = holder;
            this.path = path;
        }

        @Override
        public void onClick(View arg0) {
            currentViewHolder = holder;
            playVoiceInViewHolder(path, 0, 0);
        }
    }

    private final class OnClickDelete implements View.OnClickListener {
        private final String path;
        private final String itemname;
        private final int memoid;

        private OnClickDelete(String path, String itemname, int memoid) {
            this.path = path;
            this.itemname = itemname;
            this.memoid = memoid;
        }

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
//            startActivityForResult(delIntent, DEL_REQUEST);
        }
    }

    private final class OnClickEdit implements View.OnClickListener {
        private final String path;
        private final int secs;
        private final ViewHolder holder;
        private final String itemname;
        private final String dd;
        private final int memoid;

        private OnClickEdit(String path, int secs, ViewHolder holder, String itemname, String dd,
                int memoid) {
            this.path = path;
            this.secs = secs;
            this.holder = holder;
            this.itemname = itemname;
            this.dd = dd;
            this.memoid = memoid;
        }

        @Override
        public void onClick(View v) {
            currentPos = 0;
            setOnPlayPositionChanged(0, 0);
            mediaStatus = MEDIA_STATE_EDIT;
            currentViewHolder = holder;
            

            if (currentMemo != null && !String.valueOf(memoid).endsWith(currentMemo.getMemId()))
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
            setOnVoiceEditClicked(null, memo);
        }
    }

    private final class OnClickShare implements View.OnClickListener {
        private final Context context;
        private final String path;

        private OnClickShare(Context context, String path) {
            this.context = context;
            this.path = path;
        }

        @Override
        public void onClick(View v) {
            // MemosUtils.shareMemo(mContext,
            // mCurrentPath);
            Intent intent = new Intent(mContext, MemoShare.class);
            intent.putExtra("path", path);
            context.startActivity(intent);
        }
    }

    public void playVoiceInViewHolder(String path, long from, long to)
    {
        int state = mRecorder.getState();
        if (state == Recorder.IDLE_STATE) {
            mCurrentMediaPlayer = mRecorder.createMediaPlayer(path);
            currentPos = 0;
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
        int durationAllTime = mCurrentMediaPlayer.getDuration();
        long pos = mCurrentMediaPlayer.getCurrentPosition();
        if(currentPos >= durationAllTime)
        {
            currentPos = durationAllTime;
        }
        else if(currentPos <= durationAllTime && (durationAllTime - pos) <=200)
        {
            currentPos += 40;
        }
        else
        {
            currentPos = pos;
        }
        setOnPlayPositionChanged(0, currentPos);
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
        TextView txtRecordName;
        EditText txtRecordNameEditable;
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
