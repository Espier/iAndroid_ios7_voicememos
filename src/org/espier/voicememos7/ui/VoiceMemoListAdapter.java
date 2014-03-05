package org.espier.voicememos7.ui;

import android.annotation.SuppressLint;
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
        private List list = new ArrayList<View>();
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
        
        public interface OnListViewChangedListener {
            public void onAChanged(Intent intent,int state);
            public void onBChanged();
            public void onVoiceEditClicked();
            public void DisplayEditButton(boolean isDisplay);
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
        protected View lastView = null;
        protected boolean isClose = false;

        public void setOnListViewChangedListener(OnListViewChangedListener listener) {
            mOnListViewChangedListener = listener;
          }
        
        private void setAChanged(Intent intentA,int request) {
            if (mOnListViewChangedListener != null) mOnListViewChangedListener.onAChanged(intentA,request);
          }

        private void setBChanged() {
            if (mOnListViewChangedListener != null) mOnListViewChangedListener.onBChanged();
          }
        
        private void DisplayEditButton(boolean isDisplay) {
            if (mOnListViewChangedListener != null) mOnListViewChangedListener.DisplayEditButton(isDisplay);
        }
        
        private void setOnVoiceEditClicked() {
            Log.d("asdf","in C event");
            if (mOnListViewChangedListener != null) mOnListViewChangedListener.onVoiceEditClicked();
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
            vh.cellGrayLine = (ImageView)v.findViewById(R.id.cell_GaryLine);
            vh.share = (ImageView) v.findViewById(R.id.share);
            
            vh.edit = (TextView) v.findViewById(R.id.edit);
            vh.del = (ImageView) v.findViewById(R.id.del);
            
            
            
            RelativeLayout.LayoutParams lpTitle = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.FILL_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            lpTitle.setMargins(ScalePx.scalePx(mContext, 31), 
                    ScalePx.scalePx(mContext, 13), 0, 0);
            vh.tag.setLayoutParams(lpTitle);
            
            RelativeLayout.LayoutParams lpCreateDate = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            lpCreateDate.setMargins(ScalePx.scalePx(mContext, 31), 
                    ScalePx.scalePx(mContext, 0), 0, 0);
            lpCreateDate.addRule(RelativeLayout.BELOW,R.id.memos_item_title);
            vh.createDate.setLayoutParams(lpCreateDate);
            
            RelativeLayout.LayoutParams lpDuration = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            lpDuration.setMargins(ScalePx.scalePx(mContext, 54), 
                    ScalePx.scalePx(mContext, 0), 0, 0);
            lpDuration.addRule(RelativeLayout.RIGHT_OF, R.id.memos_item_create_date);
            lpDuration.addRule(RelativeLayout.BELOW,R.id.memos_item_title);
            vh.duration.setLayoutParams(lpDuration);
            
            LinearLayout.LayoutParams lpPlay = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lpPlay.setMargins(ScalePx.scalePx(mContext, 36), 
                    ScalePx.scalePx(mContext, 13), 0, 0);
            vh.playControl.setLayoutParams(lpPlay);
            
            LinearLayout.LayoutParams lpLeftTime = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lpLeftTime.setMargins(ScalePx.scalePx(mContext, 36), 
                    0, 0, 0);
            vh.mCurrentTime.setLayoutParams(lpLeftTime);
            
            LinearLayout.LayoutParams lpSeekBar = new LinearLayout.LayoutParams(
                    ScalePx.scalePx(mContext, 340),
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lpSeekBar.setMargins(ScalePx.scalePx(mContext, 18), 
                    ScalePx.scalePx(mContext, 13), 0, 0);
            vh.bar.setLayoutParams(lpSeekBar);
            
            LinearLayout.LayoutParams lpRightTime = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lpRightTime.setMargins(ScalePx.scalePx(mContext, 18), 
                    0, 0, 0);
            vh.mCurrentRemain.setLayoutParams(lpRightTime);
            
            LinearLayout.LayoutParams lpLine = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.FILL_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lpLine.setMargins(ScalePx.scalePx(mContext, 36), 
                    ScalePx.scalePx(mContext, 38), 0, 0);
            vh.cellGrayLine.setLayoutParams(lpLine);
            
            
            LinearLayout.LayoutParams lpShare = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lpShare.setMargins(ScalePx.scalePx(mContext, 32), 
                    ScalePx.scalePx(mContext, 16), 0, 
                    ScalePx.scalePx(mContext, 24));
            lpShare.weight = 0;
            vh.share.setLayoutParams(lpShare);
            
            
            LinearLayout.LayoutParams lpEdit = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lpEdit.setMargins(0, 
                    ScalePx.scalePx(mContext, 16), 0, ScalePx.scalePx(mContext, 24));
            lpEdit.weight = 1;
            lpEdit.gravity = Gravity.CENTER_VERTICAL;
            vh.edit.setLayoutParams(lpEdit);
            
            
            LinearLayout.LayoutParams lpDelete = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lpDelete.setMargins(0, 
                    ScalePx.scalePx(mContext, 16), 
                    ScalePx.scalePx(mContext, 32), 
                    0);
            lpDelete.weight = 0;
            vh.del.setLayoutParams(lpDelete);
            
            
            if (vh.bar instanceof SeekBar) {
                SeekBar seeker = (SeekBar) vh.bar;
                seeker.setOnSeekBarChangeListener(mSeekListener);
            }
            vh.bar.setMax(1000);


//            vh.tag.setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    vh.tag.getParent().
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

                //@SuppressLint("ResourceAsColor")
                @Override
                public void onClick(View v) {
                    //close view
                    if (lastView != null && isClose) {
                        DisplayEditButton(true);
                        vh.tag.setTextColor(mContext.getResources().getColor(R.color.black));
                        vh.createDate.setTextColor(mContext.getResources().getColor(R.color.black));
                        vh.duration.setTextColor(mContext.getResources().getColor(R.color.black));
                        LinearLayout layout = (LinearLayout) lastView.findViewById(R.id.playlayout);
                        layout.setVisibility(View.GONE);
                        LinearLayout sharelayout = (LinearLayout) lastView
                                .findViewById(R.id.sharelayout);
                        sharelayout.setVisibility(View.GONE);
                        isClose = false;
                        vh.bar.setProgress(0);
                        vh.tag.clearFocus();
                        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(vh.tag.getWindowToken(), 0);
                        int i = list.size();
                        for (int j = 0; j < i; j++) {
                            View view = (View) list.get(j);
                            
                            EditText etTitle = (EditText) view.findViewById(R.id.memos_item_title);
                            TextView tvCreateDate = (TextView)view.findViewById(R.id.memos_item_create_date);
                            TextView tvDuration = (TextView)view.findViewById(R.id.memos_item_duration);
                            etTitle.setTextColor(mContext.getResources().getColor(R.color.black));
                            tvCreateDate.setTextColor(mContext.getResources().getColor(R.color.black));
                            tvDuration.setTextColor(mContext.getResources().getColor(R.color.black));
                            view.setBackgroundColor(Color.WHITE);
                        }
                        return;
                    } else {
                    //expand the view
                        DisplayEditButton(false);
                    LinearLayout layout = (LinearLayout) v.findViewById(R.id.playlayout);
                    layout.setVisibility(View.VISIBLE);

                    LinearLayout sharelayout = (LinearLayout) v.findViewById(R.id.sharelayout);
                    sharelayout.setVisibility(View.VISIBLE);

                    vh.mCurrentRemain.setText("-" + vh.duration.getText());
                    isClose = true;
                    int i = list.size();
                    for (int j = 0; j < i; j++) {
                        View view = (View) list.get(j);
                        if (v == view) {
                            
                            continue;
                        }
                        

                        view.setBackgroundColor(mContext.getResources().getColor(R.color.light_gray));
                        EditText et = (EditText) view.findViewById(R.id.memos_item_title);
                        et.setTextColor(mContext.getResources().getColor(R.color.heavygray));
                        TextView tvCreateDate = (TextView)view.findViewById(R.id.memos_item_create_date);
                        TextView tvDuration = (TextView)view.findViewById(R.id.memos_item_duration);
                        tvCreateDate.setTextColor(mContext.getResources().getColor(R.color.heavygray));
                        tvDuration.setTextColor(mContext.getResources().getColor(R.color.heavygray));
                    }

                    lastView = v;
                    }

                }
            });
            vh.tag.setClickable(false);
            vh.tag.setFocusable(false);
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
            String dateFormat = mContext.getString(R.string.date_time_format);
            int labelType = cursor.getInt(mLabelTypeIdx);
            if (labelType == EspierVoiceMemos7.LABEL_TYPE_NONE) {
                dateFormat = mContext.getString(R.string.date_format);
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
                    // MemosUtils.shareMemo(mContext,
                    // mCurrentPath);
                    Intent intent = new Intent(mContext, MemoShare.class);
                    intent.putExtra("path", mCurrentPath);
                    context.startActivity(intent);
                }
            });
            vh.edit.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mediaStatus = MEDIA_STATE_EDIT;
                    Log.d("adf","click edit button");
//                    RelativeLayout editLayout = (RelativeLayout)findViewById(R.id.editlayout);
//                    editLayout.setVisibility(View.VISIBLE);
//                    
//                    RelativeLayout playLayout = (RelativeLayout)findViewById(R.id.playlayout);
//                    playLayout.setVisibility(View.GONE);
                    
                    setOnVoiceEditClicked();
                    refreshNow(vh);
                    
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
                    //setBChanged();
                }
            });

            vh.del.setEnabled(true);
            vh.del.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (mRecorder.getState() != Recorder.IDLE_STATE) {
                        mRecorder.stopPlayback();
                    }
                    Intent delIntent = new Intent(mContext, MemoDelete.class);
                    delIntent.putExtra("mCurrentMemoId", memoid);
                    delIntent.putExtra("memoname", itemname);
                    delIntent.putExtra("memopath", path);
                    setAChanged(delIntent,DEL_REQUEST);
                    //startActivityForResult(delIntent, DEL_REQUEST);
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
            ImageView cellGrayLine;
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
