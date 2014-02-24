//package org.espier.voicememos7.ui;
//
//import android.content.Context;
//import android.content.Intent;
//import android.database.Cursor;
//import android.graphics.Color;
//import android.media.MediaPlayer;
//import android.media.MediaPlayer.OnCompletionListener;
//import android.os.Handler;
//import android.os.Message;
//import android.util.Log;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
//import android.widget.SeekBar;
//import android.widget.SimpleCursorAdapter;
//import android.widget.TextView;
//
//import org.espier.voicememos7.R;
//import org.espier.voicememos7.model.VoiceMemo;
//import org.espier.voicememos7.ui.EspierVoiceMemos7.VoiceMemoListAdapter.ViewHolder;
//import org.espier.voicememos7.util.MemosUtils;
//import org.espier.voicememos7.util.Recorder;
//
//import java.io.File;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//class VoiceMemoListAdapter extends SimpleCursorAdapter {
//
//    private Context mContext;
//    private int mMemoIdx;
//    private int mPathIdx;
//    private int mLabelIdx;
//    private int mLabelTypeIdx;
//    private int mDurationIdx;
//    private int mCreateDateIdx;
//    private int mCurrentBgColor;
//    private final Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//          switch (msg.what) {
//            case 1:
//              long next = refreshNow((ViewHolder) msg.obj);
//              queueNextRefresh(next,(ViewHolder) msg.obj);
//              break;
//          }
//        }
//      };
//
//    public VoiceMemoListAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
//      super(context, layout, c, from, to);
//      mContext = context;
//      mCurrentBgColor = Color.WHITE;
//      setupColumnIndices(c);
//    }
//    
//    public View getView(int position, View convertView, ViewGroup parent) {
//      Log.d("memo", "getView, mCurrentPosition:" + mCurrentPosition);
//      if (mCurrentPosition == position) {
//        isCurrentPosition = true;
//      } else {
//        isCurrentPosition = false;
//      }
//      return super.getView(position, convertView, parent);
//    }
//
//    @Override
//    public View newView(Context context, Cursor cursor, ViewGroup parent) {
//      View v = super.newView(context, cursor, parent);
//
//      ViewHolder vh = new ViewHolder();
//      vh.playControl = (ImageView) v.findViewById(R.id.memos_item_play);
//      vh.tag = (TextView) v.findViewById(R.id.memos_item_title);
//      vh.createDate = (TextView) v.findViewById(R.id.memos_item_create_date);
//      vh.duration = (TextView) v.findViewById(R.id.memos_item_duration);
//      vh.id = (TextView) v.findViewById(R.id.memos_item__id);
//      vh.path = (TextView) v.findViewById(R.id.memos_item_path);
//      vh.bar = (SeekBar)v.findViewById(android.R.id.progress);
//      vh.mCurrentRemain = (TextView)v.findViewById(R.id.current_remain);
//      vh.mCurrentTime = (TextView)v.findViewById(R.id.current_positon);
//      vh.share = (ImageView)v.findViewById(R.id.share);
//      vh.del = (ImageView)v.findViewById(R.id.del);
//      vh.edit = (TextView)v.findViewById(R.id.edit);
//     // mCurrentDuration = (Integer)v.findViewById(R.id.memos_item_duration).getTag();
//      if (vh.bar instanceof SeekBar) {
//          SeekBar seeker = (SeekBar) vh.bar;
//          seeker.setOnSeekBarChangeListener(mSeekListener);
//        }
//      vh.bar.setMax(1000);
//      v.setTag(vh);
//      v.setOnClickListener(new View.OnClickListener() {
//        
//        @Override
//        public void onClick(View v) {
//            // TODO Auto-generated method stub
//            LinearLayout layout = (LinearLayout)v.findViewById(R.id.playlayout);
//            if(layout.getVisibility() == View.GONE){
//                layout.setVisibility(View.VISIBLE);
//            }else{
//                layout.setVisibility(View.GONE);
//            }
//            RelativeLayout sharelayout = (RelativeLayout)v.findViewById(R.id.sharelayout);
//            if(sharelayout.getVisibility() == View.GONE){
//                sharelayout.setVisibility(View.VISIBLE);
//            }else{
//                sharelayout.setVisibility(View.GONE);
//            }
//            
//            System.out.println("item click");
//        }
//    });
//      return v;
//    }
//
//    @Override
//    public void bindView(View view, Context context, Cursor cursor) {
//
//      final ViewHolder vh = (ViewHolder) view.getTag();
//      
//      
//      vh.tag.setText(cursor.getString(mLabelIdx));
//
//      int secs = cursor.getInt(mDurationIdx);
//      if (secs == 0) {
//        vh.duration.setText("");
//      } else {
//        vh.duration.setText(MemosUtils.makeTimeString(context, secs / 1000));
//        vh.duration.setTag(secs);
//      }
//
//      Long date = cursor.getLong(mCreateDateIdx);
//      String dateFormat = getString(R.string.date_time_format);
//      int labelType = cursor.getInt(mLabelTypeIdx);
//      if (labelType == EspierVoiceMemos7.LABEL_TYPE_NONE ) {
//        dateFormat = getString(R.string.date_format);
//      }
//      SimpleDateFormat format = new SimpleDateFormat(dateFormat);
//      Date d = new Date(date);
//      String dd = format.format(d);
//      vh.createDate.setText(dd);
//
//      final String path = cursor.getString(mPathIdx);
//      final Integer id = cursor.getInt(mMemoIdx);
//      vh.path.setTag(path);
//      vh.id.setTag(id);
//     
//
//      File file = new File(path);
//      if (!file.exists()) {
//        if (isCurrentPosition) {
//          view.setBackgroundColor(mCurrentBgColor);
//          vh.playControl.setVisibility(View.VISIBLE);
//          vh.tag.setTextColor(Color.WHITE);
//          vh.createDate.setTextColor(Color.WHITE);
//          vh.duration.setTextColor(Color.WHITE);
//        }else{
//          view.setBackgroundColor(Color.LTGRAY);
//          vh.playControl.setVisibility(View.VISIBLE);
//          vh.tag.setTextColor(Color.BLACK);
//          vh.createDate.setTextColor(Color.GRAY);
//          vh.duration.setTextColor(Color.BLUE);
//        }
//      } else {
//        if (isCurrentPosition) {
//          vh.playControl.setVisibility(View.VISIBLE);
//          vh.tag.setTextColor(Color.WHITE);
//          vh.createDate.setTextColor(Color.WHITE);
//          vh.duration.setTextColor(Color.WHITE);
//        } else {
//          vh.tag.setTextColor(Color.BLACK);
//          vh.createDate.setTextColor(Color.GRAY);
//          vh.duration.setTextColor(Color.BLUE);
//          vh.playControl.setImageResource(R.drawable.play);
//        }
//       // mCurrentDuration = (Integer) view.findViewById(R.id.memos_item_duration).getTag();
//        mCurrentMemoId = (Integer) view.findViewById(R.id.memos_item__id).getTag();
//        mCurrentPath = (String) view.findViewById(R.id.memos_item_path).getTag();
//        view.setBackgroundColor(mCurrentBgColor);
//        vh.share.setOnClickListener(new View.OnClickListener() {
//            
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                MemosUtils.shareMemo(EspierVoiceMemos7.this, mCurrentPath);
//            }
//        });
//    
//        vh.del.setEnabled(true);
//        vh.del.setOnClickListener(new View.OnClickListener() {
//
//          @Override
//          public void onClick(View arg0) {
//              if (mRecorder.getState() != Recorder.IDLE_STATE) {
//                  mRecorder.stopPlayback();
//                }
//                Intent delIntent = new Intent(EspierVoiceMemos7.this, MemoDelete.class);
//                delIntent.putExtra("memoname", vh.tag.getText());
//                startActivityForResult(delIntent, DEL_REQUEST);
//          }
//        });
//        vh.playControl.setOnClickListener(new View.OnClickListener() {
//
//          @Override
//          public void onClick(View arg0) {
//            int state = mRecorder.getState();
////            if (state == Recorder.PLAYING_STATE) {
////              mRecorder.stopPlayback();
////            }
//            if (state == Recorder.IDLE_STATE) {
//              mCurrentMediaPlayer = mRecorder.createMediaPlayer(path);
//              mRecorder.startPlayback();
//              vh.playControl.setImageResource(R.drawable.pause);
//            } else if (state == Recorder.PLAYER_PAUSE_STATE) {
//              mRecorder.startPlayback();
//              vh.playControl.setImageResource(R.drawable.pause);
//            } else if (state == Recorder.PLAYING_STATE) {
//              mRecorder.pausePlayback();
//              vh.playControl.setImageResource(R.drawable.play);
//            }
//
//            mCurrentMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
//
//              @Override
//              public void onCompletion(MediaPlayer mp) {
//                vh.playControl.setImageResource(R.drawable.play);
//                mRecorder.stopPlayback();
//              }
//            });
//            mCurrentDuration = (Integer) ((View)(vh.duration)).getTag();
//            long next = refreshNow(vh);
//            queueNextRefresh(next,vh);
//
//          }
//        });
//      }
//    }
//
//    protected void queueNextRefresh(long delay,ViewHolder vh) {
//        if (mRecorder.getState() == Recorder.PLAYING_STATE) {
//            Message msg =new Message();
//            msg.what = 1;
//            msg.obj = vh;
//            mHandler.removeMessages(REFRESH);
//            mHandler .sendMessageDelayed(msg, delay);
//          }
//        }
//
//    protected long refreshNow(ViewHolder view) {
//        if (mCurrentMediaPlayer == null || mRecorder.getState() != Recorder.PLAYING_STATE) {
//
//            return 500;
//          }
//
//          // try {
//          long pos = mCurrentMediaPlayer.getCurrentPosition();
//          if ((pos >= 0) && (mCurrentDuration > 0)) {
//              view.mCurrentTime.setText(MemosUtils.makeTimeString(EspierVoiceMemos7.this, pos / 1000));
//              view.mCurrentRemain.setText("-"
//                + MemosUtils.makeTimeString(EspierVoiceMemos7.this, ((mCurrentDuration - pos) / 1000)));
//            int progress = (int) (1000 * pos / mCurrentDuration);
//            view.bar.setProgress(progress);
//
//          } else {
//              view. mCurrentTime.setText("0:00");
//              view.bar.setProgress(1000);
//          }
//          // calculate the number of milliseconds until the next full second,
//          // so
//          // the counter can be updated at just the right time
//          long remaining = 1000 - (pos % 1000);
//
//          // approximate how often we would need to refresh the slider to
//          // move it smoothly
//          int width = view.bar.getWidth();
//          if (width == 0) width = 320;
//          long smoothrefreshtime = mCurrentDuration / width;
//
//          if (smoothrefreshtime > remaining) return remaining;
//          if (smoothrefreshtime < 20) return 20;
//          return smoothrefreshtime;
//          // return 500;
//        }
//
//    @Override
//    public void changeCursor(Cursor cursor) {
//
//    }
//
//    class ViewHolder {
//      ImageView playControl;
//      TextView tag;
//      TextView createDate;
//      TextView duration;
//      TextView path;
//      TextView id;
//      TextView mCurrentTime;
//      TextView mCurrentRemain;
//      SeekBar bar;
//      ImageView share;
//      ImageView del;
//      TextView edit;
//    }
//
//    private void setupColumnIndices(Cursor cursor) {
//      if (cursor != null) {
//        mLabelIdx = cursor.getColumnIndexOrThrow(VoiceMemo.Memos.LABEL);
//        mLabelTypeIdx = cursor.getColumnIndexOrThrow(VoiceMemo.Memos.LABEL_TYPE);
//        mDurationIdx = cursor.getColumnIndexOrThrow(VoiceMemo.Memos.DURATION);
//        mCreateDateIdx = cursor.getColumnIndexOrThrow(VoiceMemo.Memos.CREATE_DATE);
//        mMemoIdx = cursor.getColumnIndexOrThrow(VoiceMemo.Memos._ID);
//        mPathIdx = cursor.getColumnIndexOrThrow(VoiceMemo.Memos.DATA);
//      }else{
//          System.out.println("cursor is null");
//      }
//    }
//
//  }
