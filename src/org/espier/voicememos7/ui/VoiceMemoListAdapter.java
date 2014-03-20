
package org.espier.voicememos7.ui;

import android.R.integer;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

import org.espier.voicememos7.R;
import org.espier.voicememos7.model.CheapSoundFile;
import org.espier.voicememos7.model.VoiceMemo;
import org.espier.voicememos7.ui.VoiceMemoListAdapter.ViewHolder;
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
    
    protected int durationAllTime;
    public MediaPlayer mCurrentMediaPlayer;
    private int mediaStatus = 0;
    private File mFile;
    public org.espier.voicememos7.util.Recorder mRecorder;
    private Cursor cursor;
    private int expandedPosition = -1;
    private int EditPosition = 0;
    protected boolean isCollapsed = true;
    ViewHolder currentViewHolder;
    VoiceMemo currentMemo;
    private long currentPos = 0;
    Drawable thumb_gray;
    public boolean canExpanding = true;

    public interface OnListViewChangedListener {
        
        public void onDeleteItem(Intent intent, int state);

        public void onVoiceEditClicked(CheapSoundFile mSoundFile, VoiceMemo memos);

        public void DisplayEditButton(boolean isDisplay);

        void onPlayStatusChanged(int status, long position);

        void onPlayStopFired();
        void changeTextViewColorGray();
        void changeTextViewColorBlue();
        void changSoundColorGray();
        void changSoundColorBlue();
        
        void onSlideItem(View view);
        
        void setItemScroll(boolean canScroll);

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
    public ViewHolder currentHolder;

    public void setOnListViewChangedListener(OnListViewChangedListener listener) {
        mOnListViewChangedListener = listener;
    }

    private void deleteItem(Intent intentA, int request) {
        if (mOnListViewChangedListener != null)
            mOnListViewChangedListener.onDeleteItem(intentA, request);
    }

    private void DisplayEditButton(boolean isDisplay) {
        if (mOnListViewChangedListener != null)
            mOnListViewChangedListener.DisplayEditButton(isDisplay);
    }

    private void setOnVoiceEditClicked(CheapSoundFile mSoundFile, VoiceMemo memos) {
        if (mOnListViewChangedListener != null)
            mOnListViewChangedListener.onVoiceEditClicked(mSoundFile, memos);
    }

    private void notifyPlayCompletion() {
        if (mOnListViewChangedListener != null) {
            mOnListViewChangedListener.onPlayStopFired();
        }
    }

    private void setOnPlayPositionChanged(int status, long position) {
        if (mOnListViewChangedListener != null)
            mOnListViewChangedListener.onPlayStatusChanged(status, position);
    }

    private void SlideItem(View view) {
        if (mOnListViewChangedListener != null)
            mOnListViewChangedListener.onSlideItem(view);
    }
    
    private void setItemScroll(boolean canScroll) {
        if (mOnListViewChangedListener != null)
            mOnListViewChangedListener.setItemScroll(canScroll);
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
    
    public MediaPlayer getCurrentMediaPlayer()
    {
        return mCurrentMediaPlayer;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View view = super.newView(context, cursor, parent);
        ViewHolder holder = new ViewHolder();
        thumb_gray =context.getResources().getDrawable(R.drawable.thumb_gray);
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
        holder.btnHiddenDelete = (Button)view.findViewById(R.id.hiddenDeleteButon);
        holder.bgView = view.findViewById(R.id.memos_item_bg);
        
        
        holder.txtRecordNameEditable.setOnKeyListener(new View.OnKeyListener() {
            
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
             // TODO Auto-generated method stub  
             if(keyCode == KeyEvent.KEYCODE_ENTER){  
                 InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);  
                 if(imm.isActive()){  
                     imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0 );  
                 }  
                     return true;  
                 }  
                 return false;  
            }
        });
        
        
        LinearLayout.LayoutParams llp = new android.widget.LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,ScalePx.scalePx(mContext, 108));
        
        holder.bgView.setLayoutParams(llp);
        RelativeLayout.LayoutParams lpButton = new RelativeLayout.LayoutParams(
                ScalePx.scalePx(mContext, 130),
                ScalePx.scalePx(mContext, 108));
        lpButton.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,RelativeLayout.TRUE);
        holder.btnHiddenDelete.setLayoutParams(lpButton);
        
        RelativeLayout.LayoutParams lpTitle = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lpTitle.setMargins(ScalePx.scalePx(mContext, 31),
                ScalePx.scalePx(mContext, 13), ScalePx.scalePx(mContext, 31), 0);
        holder.txtRecordName.setLayoutParams(lpTitle);

        holder.txtRecordNameEditable.setVisibility(View.GONE);
        
        RelativeLayout.LayoutParams lpCreateDate = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lpCreateDate.setMargins(ScalePx.scalePx(mContext, 31),
                ScalePx.scalePx(mContext, 0), 0, 0);
        lpCreateDate.addRule(RelativeLayout.BELOW, R.id.titlelay);
        holder.createDate.setLayoutParams(lpCreateDate);

        RelativeLayout.LayoutParams lpDuration = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lpDuration.setMargins(ScalePx.scalePx(mContext, 54),
                ScalePx.scalePx(mContext, 0), 0, 0);
        lpDuration.addRule(RelativeLayout.RIGHT_OF, R.id.memos_item_create_date);
        lpDuration.addRule(RelativeLayout.BELOW, R.id.titlelay);
        holder.duration.setLayoutParams(lpDuration);

        LinearLayout.LayoutParams lpPlay = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lpPlay.setMargins(ScalePx.scalePx(mContext, 36),
                ScalePx.scalePx(mContext, 13), 0, 0);
        holder.playControl.setLayoutParams(lpPlay);

        RelativeLayout.LayoutParams lpLeftTime = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lpLeftTime.setMargins(ScalePx.scalePx(mContext, 36),
                0, 0, 0);
        lpLeftTime.addRule(RelativeLayout.ALIGN_PARENT_LEFT,RelativeLayout.TRUE);
        holder.mCurrentTime.setLayoutParams(lpLeftTime);
        
        RelativeLayout.LayoutParams lpSeekBar = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        System.out.println("lay  width  "+lpSeekBar.width);
        lpSeekBar.setMargins(ScalePx.scalePx(mContext, 18),
                ScalePx.scalePx(mContext, 13), 0, 0);
        lpSeekBar.addRule(RelativeLayout.RIGHT_OF,R.id.current_positon);
        lpSeekBar.addRule(RelativeLayout.LEFT_OF,R.id.current_remain);
        holder.bar.setLayoutParams(lpSeekBar);

        RelativeLayout.LayoutParams lpRightTime = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lpRightTime.setMargins(ScalePx.scalePx(mContext, 18),
                0, ScalePx.scalePx(mContext, 33), 0);
        lpRightTime.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,RelativeLayout.TRUE);
        holder.mCurrentRemain.setLayoutParams(lpRightTime);

        LinearLayout.LayoutParams lpLine = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lpLine.setMargins(ScalePx.scalePx(mContext, 36),
                ScalePx.scalePx(mContext, 38), 0, 0);
        holder.cellGrayLine.setLayoutParams(lpLine);

        RelativeLayout.LayoutParams lpShare = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lpShare.setMargins(ScalePx.scalePx(mContext, 32),
                ScalePx.scalePx(mContext, 16), 0,
                ScalePx.scalePx(mContext, 24));
        lpShare.addRule(RelativeLayout.ALIGN_PARENT_LEFT,RelativeLayout.TRUE);
        holder.share.setLayoutParams(lpShare);

        RelativeLayout.LayoutParams lpEdit = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lpEdit.setMargins(0,
                ScalePx.scalePx(mContext, 16), 0, ScalePx.scalePx(mContext, 24));
        lpEdit.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE);
        holder.edit.setLayoutParams(lpEdit);

        RelativeLayout.LayoutParams lpDelete = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lpDelete.setMargins(0,
                ScalePx.scalePx(mContext, 16),
                ScalePx.scalePx(mContext, 32),
                ScalePx.scalePx(mContext, 24));
        lpDelete.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,RelativeLayout.TRUE);
        holder.del.setLayoutParams(lpDelete);
        RelativeLayout memos_item_delete_hidden = (RelativeLayout)view.findViewById(R.id.memos_item_delete_hidden);
        RelativeLayout.LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT,ScalePx.scalePx(mContext, 108));
        memos_item_delete_hidden.setLayoutParams(lp);
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
        String viewAddr = view.toString().substring(27, view.toString().length())+"@";
        holder.txtRecordName.setText(displayString);
        if (displayString.equals(itemname)) {

        }
        holder.txtRecordNameEditable.setText(displayString);

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

        view.setOnClickListener(new OnClickItem(cursor.getPosition()));
        view.setClickable(false);
        holder.txtRecordName.setOnFocusChangeListener(new OnClickRecordName(holder));
        holder.share.setOnClickListener(new OnClickShare(context, path));
        holder.edit.setOnClickListener(new OnClickEdit(path, secs, holder, holder.txtRecordName.getText().toString(), strDate,
                memoid,view));
        holder.del.setEnabled(true);
        holder.btnHiddenDelete.setOnClickListener(new OnClickDelete(path, holder.txtRecordName.getText().toString(), memoid,holder,MemosUtils.DELETE_WITHOUT_CONFIRM));
        holder.del.setOnClickListener(new OnClickDelete(path, holder.txtRecordName.getText().toString(), memoid,holder,MemosUtils.DELETE_WITH_CONFIRM));
        holder.playControl.setOnClickListener(new OnClickPlay(holder, path));
        
        if (isCollapsed) {
//            view.setBackgroundColor(mContext.getResources()
//                    .getColor(R.color.white));
            holder.bgView.setBackgroundResource(R.color.white);
            holder.txtRecordName.setTextColor(mContext.getResources().getColor(R.color.black));
            holder.createDate.setTextColor(mContext.getResources().getColor(R.color.black));
            holder.duration.setTextColor(mContext.getResources().getColor(R.color.black));
        } else {
            if (cursor.getPosition() == expandedPosition) {

                view.setBackgroundColor(mContext.getResources()
                        .getColor(R.color.white));
                holder.bgView.setBackgroundResource(R.color.white);
                holder.txtRecordName.setTextColor(mContext.getResources().getColor(R.color.black));
                holder.createDate.setTextColor(mContext.getResources().getColor(R.color.black));
                holder.duration.setTextColor(mContext.getResources().getColor(R.color.black));
                setItemVisible(view, true);
            } else {
                holder.bgView.setBackgroundColor(mContext.getResources().getColor(R.color.light_gray));
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

    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {

        public void onStartTrackingTouch(SeekBar bar) {
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser)
                return;
            int pos = durationAllTime * progress / 1000;
            mRecorder.seekTo(pos);
        }

        public void onStopTrackingTouch(SeekBar bar) {
        }
    };
    
    public void collapseAllItems() {
        for (int j = 0; j < list.size(); j++) {
            
            View view = (View) list.get(j);
            setItemVisible(view, false);
            ViewHolder itemHolder = (ViewHolder) view.getTag();
            itemHolder.txtRecordName.setTextColor(mContext.getResources().getColor(R.color.black));
            itemHolder.createDate.setTextColor(mContext.getResources().getColor(R.color.black));
            itemHolder.duration.setTextColor(mContext.getResources().getColor(R.color.black));
            itemHolder.bgView.setBackgroundColor(mContext.getResources().getColor(R.color.white));
            view.setBackgroundColor(Color.WHITE);
        }
        expandedPosition = -1;
        isCollapsed = true;
        mRecorder.stopPlayback();
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
            itemHolder.bgView.setBackgroundColor(mContext.getResources().getColor(R.color.light_gray));
//            view.setBackgroundColor(mContext.getResources()
//                    .getColor(R.color.light_gray));
        }
    }

    private void setItemVisible(View itemView, boolean isVisible) {

        LinearLayout layout = (LinearLayout) itemView.findViewById(R.id.playlayout);
        LinearLayout sharelayout = (LinearLayout) itemView.findViewById(R.id.sharelayout);
        if (isVisible) {
            
            ImageView del = (ImageView)itemView.findViewById(R.id.del);
            del.setImageResource(R.drawable.trash);
            ImageView share = (ImageView)itemView.findViewById(R.id.share);
            share.setImageResource(R.drawable.action);
            ImageView play = (ImageView)itemView.findViewById(R.id.memos_item_play);
            play.setImageResource(R.drawable.play);
            SeekBar bar = (SeekBar)itemView.findViewById(android.R.id.progress);
            bar.setThumb(mContext.getResources().getDrawable(R.drawable.thumb));
            TextView textview = (TextView)itemView.findViewById(R.id.edit);
            textview.setTextColor(mContext.getResources().getColor(R.color.font_color));
            TextView title = (TextView)itemView.findViewById(R.id.memos_item_title);
            EditText editText = (EditText)itemView.findViewById(R.id.memos_item_title_editable);
            RelativeLayout.LayoutParams lpTitle = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.FILL_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            lpTitle.setMargins(ScalePx.scalePx(mContext, 31),
                    ScalePx.scalePx(mContext, 13), ScalePx.scalePx(mContext, 31), 0);
            editText.setLayoutParams(lpTitle);
            editText.setSelection(editText.getText().toString().length());
            title.setVisibility(View.GONE);
            editText.setVisibility(View.VISIBLE);
            
            layout.setVisibility(View.VISIBLE);
            sharelayout.setVisibility(View.VISIBLE);
        } else {
            TextView title = (TextView)itemView.findViewById(R.id.memos_item_title);
            EditText editText = (EditText)itemView.findViewById(R.id.memos_item_title_editable);
            RelativeLayout.LayoutParams lpTitle = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.FILL_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            lpTitle.setMargins(ScalePx.scalePx(mContext, 31),
                    ScalePx.scalePx(mContext, 13), ScalePx.scalePx(mContext, 31), 0);
            title.setLayoutParams(lpTitle);
            title.setVisibility(View.VISIBLE);
            title.setText(editText.getText().toString());
            editText.setVisibility(View.GONE);
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
            if (holder.bgView.getScrollX()<0)
                return;

            if (expandedPosition == position)
                return;
            
            if (canExpanding == false)
                return;

            // if status is not collapsed(expanded), and the item clicked is not
            // the expanded one,
            // then collapse it, and restore the color to white and black.
            if (expandedPosition >= 0 && isCollapsed == false ) {
                DisplayEditButton(true);
                collapseAllItems();
                if(currentHolder!=null)
                {
                    currentHolder.bar.setProgress(0);
                    currentHolder.playControl.setImageResource(R.drawable.play);
                    currentHolder.mCurrentRemain.setText("-" + currentHolder.duration.getText());
                    currentHolder.mCurrentTime.setText("0:00");
                    mRecorder.stopPlayback();
                }
                expandedPosition = -1;
                isCollapsed = true;
                setItemScroll(true);
                return;
            }
            
            currentHolder = holder;
            // expand the view
            DisplayEditButton(false);
            setItemVisible(v, true);
            holder.mCurrentRemain.setText("-" + holder.duration.getText());
            isCollapsed = false;
            expandItem(v);
            expandedPosition =  position;
            setItemScroll(false);
        }

       
    }
    
    public void ExitCurrentEditMode() {
        if(currentHolder!=null)
        {
            collapseAllItems();
        }
        else {
            return;
        }
        if(mRecorder.getState() == Recorder.IDLE_STATE || mRecorder.getState() == Recorder.PLAYING_STATE || mRecorder.getState() == Recorder.PLAYER_PAUSE_STATE)
        {
            currentHolder.bar.setProgress(0);
            currentHolder.playControl.setImageResource(R.drawable.play);
            currentHolder.mCurrentRemain.setText("-" + currentHolder.duration.getText());
            currentHolder.mCurrentTime.setText("0:00");
            mRecorder.stopPlayback();
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
        private final ViewHolder holder;
        private int message;
        
        private OnClickDelete(String path, String itemname, int memoid,ViewHolder holder,int message) {
            this.path = path;
            this.holder= holder;
            this.itemname = itemname;
            this.memoid = memoid;
            this.message = message;
        }

        @SuppressLint("ResourceAsColor")
        @Override
        public void onClick(View arg0) {
            Log.d("adf","click Delete view="+arg0.toString());
            holder.del.setImageResource(R.drawable.trash_gray);
            holder.share.setImageResource(R.drawable.action_gray);
            holder.playControl.setImageResource(R.drawable.play_gray);
            if(arg0.getId()!=R.id.hiddenDeleteButon){
                mOnListViewChangedListener.changSoundColorGray();
                mOnListViewChangedListener.changeTextViewColorGray();
            }
            holder.edit.setTextColor(R.color.gray);
            
            holder.bar.setThumb(thumb_gray);
            
            if (mRecorder.getState() != Recorder.IDLE_STATE) {
                mRecorder.stopPlayback();
            }
            Intent delIntent = new Intent(mContext, MemoDelete.class);
            delIntent.putExtra("mCurrentMemoId", memoid);
            delIntent.putExtra("memoname", holder.txtRecordName.getText().toString());
            delIntent.putExtra("memopath", path);
            deleteItem(delIntent, this.message);
        }
    }

    private final class OnClickEdit implements View.OnClickListener {
        private final String path;
        private final int secs;
        private final ViewHolder holder;
        private final String itemname;
        private final String dd;
        private final int memoid;
        private View view;

        private OnClickEdit(String path, int secs, ViewHolder holder, String itemname, String dd,
                int memoid,View view) {
            this.path = path;
            this.secs = secs;
            this.holder = holder;
            this.itemname = itemname;
            this.dd = dd;
            this.memoid = memoid;
            this.view = view;
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
            memo.setMemName(holder.txtRecordName.getText().toString());
            memo.setMemPath(path);
            memo.setMemDuration(secs);
            currentMemo = memo;
            
            setOnVoiceEditClicked(null, memo);
//            LinearLayout layout = (LinearLayout) view.findViewById(R.id.playlayout);
//            LinearLayout sharelayout = (LinearLayout) view.findViewById(R.id.sharelayout);
//            layout.setVisibility(View.GONE);
//            sharelayout.setVisibility(View.GONE);
//            DisplayEditButton(true);
//            EditPosition = -1;
            
            setItemVisible(view, false);
            collapseAllItems();
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
            if (mRecorder.mState == Recorder.PLAYING_STATE)
                mRecorder.pausePlayback();
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
                currentViewHolder.bar.setProgress(1000);
            }
        });
        durationAllTime = (Integer) ((View) (currentViewHolder.duration)).getTag();
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
    
    public void showOrHiddenDelete(boolean isShow) {
        //textview changeto editView
        if (isShow) {
            canExpanding = false;
            // show delete image textview show finish
            // textViewEdit.setText(getResources().getString(R.string.finish));
//            for (int i = 0;i<this.getCount();i++) {
            for (final View item : list) {
//                final View item = this.getChildAt(i);
                // for (int i = 0; i < slideCutListView.getCount(); i++) {
                // View item = slideCutListView.getChildAt(i);
                ImageView delete = (ImageView) item.findViewById(R.id.deleteimage);
                delete.setVisibility(View.VISIBLE);
                delete.setOnClickListener(new View.OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        SlideItem(item);
                        
                    }
                });
                RelativeLayout.LayoutParams lpTitle = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.FILL_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                lpTitle.setMargins(ScalePx.scalePx(mContext, 31),
                        ScalePx.scalePx(mContext, 13), ScalePx.scalePx(mContext, 31), 0);
                final TextView itemname = (TextView) item.findViewById(R.id.memos_item_title);
                final EditText itemtitle = (EditText) item
                        .findViewById(R.id.memos_item_title_editable);
                itemtitle.setLayoutParams(lpTitle);
                final String title = itemname.getText().toString();
                final TextView idtextview = (TextView) item.findViewById(R.id.memos_item__id);
                itemname.setVisibility(View.GONE);
//                itemtitle.setTextSize(17);
                int ems = MemosUtils.getEllipsizeByViewWidths(title, item.getWidth()-ScalePx.scalePx(mContext, 350));
               System.out.println("  width "+ems);
//               itemtitle.setEnabled(false);
//                itemtitle.setEms(ems);
                itemtitle.setText(title);
                itemtitle.setVisibility(View.VISIBLE);
                itemtitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {

                    private int time = 0;

                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        // TODO Auto-generated method stub
                        if (!hasFocus) {
//                              InputMethodManager mInputMethodManager=(InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
//                              mInputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                            String newname = itemtitle.getText().toString();
                            if (!title.equals(newname)) {
                                // memeoname nodified update momeinfo
                                System.out.println("(Integer)   " + (Integer) idtextview.getTag());
                                MemosUtils.updateVoiceName(mContext, newname,
                                        (Integer) idtextview.getTag());
                                itemname.setText(newname);
                            }
                        }
                    }
                });
            }
        } else {
            canExpanding = true;
            //
            // textViewEdit.setText(getResources().getString(R.string.edit));
            for (View item : list) {
                // View item = slideCutListView.getChildAt(i);
                ImageView delete = (ImageView) item.findViewById(R.id.deleteimage);
                delete.setVisibility(View.GONE);
                delete.setOnClickListener(null);
                TextView itemname = (TextView) item.findViewById(R.id.memos_item_title);
                EditText itemtitle = (EditText) item.findViewById(R.id.memos_item_title_editable);
                String title = itemtitle.getText().toString();
                itemtitle.setVisibility(View.INVISIBLE);
                itemname.setText(title);
                itemname.setVisibility(View.VISIBLE);

            }
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
            view.mCurrentTime.setText("0:00");
            view.bar.setProgress(1000);
            currentPos = durationAllTime;
            return 40;
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
        if ((pos >= 0) && (durationAllTime > 0)) {
            view.mCurrentTime.setText(MemosUtils.makeTimeString(mContext,
                    pos / 1000));
            view.mCurrentRemain.setText("-"
                    + MemosUtils.makeTimeString(mContext,
                            ((durationAllTime - pos) / 1000)));
            int progress = (int) (1000 * pos / durationAllTime);
            view.bar.setProgress(progress);

        } else {
            view.mCurrentTime.setText("0:00");
            view.bar.setProgress(1000);
        }
        long remaining = 1000 - (pos % 1000);

        int width = view.bar.getWidth();
        if (width == 0)
            width = 320;
        long smoothrefreshtime = durationAllTime / width;

        if (smoothrefreshtime > remaining)
            return 40;
        if (smoothrefreshtime < 20)
            return 20;
        return 50;
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
        Button btnHiddenDelete;
        int position;
        View bgView;
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
    
    public void RemoveItem(String path) {
        List<View> listNew = new ArrayList<View>();
        
        for(View v:list) {
            Log.d("adf","view="+v.toString()+",remove file path="+path);
            ViewHolder vh = (ViewHolder)v.getTag();
            if (!((String)vh.path.getTag()).equals(path)) {
                listNew.add(v);
                Log.d("asdf","add v"+v.toString()+",vh.path in list ="+ vh.path.getTag().toString());
            }
        }
        list = listNew;
        
        for(View v:list) {
            final ViewHolder holder = (ViewHolder) v.getTag();

            Log.d("asdf",holder.path.getTag().toString());
            final String itemname = cursor.getString(mLabelIdx);
            final int secs = cursor.getInt(mDurationIdx);
            final Long date = cursor.getLong(mCreateDateIdx);
            EditText ee = (EditText)v.findViewById(R.id.memos_item_title_editable);
            ee.setText(itemname);
            Log.d("asdf","itemname="+itemname);
            TextView et = (TextView)v.findViewById(R.id.memos_item_title);
            et.setText(itemname);
        }
    }

}
