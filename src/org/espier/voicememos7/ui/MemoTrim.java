
package org.espier.voicememos7.ui;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.ColorMatrixColorFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import org.espier.voicememos7.util.Recorder;
import org.espier.voicememos7.util.AMRFileUtils;
import org.espier.voicememos7.R;
import org.espier.voicememos7.model.VoiceMemo;
import org.espier.voicememos7.util.ScalePx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MemoTrim extends Activity implements OnClickListener {
    private Button btnSaveAsNew, btnTrimOrigin, btnCancel;
    private ImageView imageViewGreyLine;
    long voice_trim_from;
    long voice_trim_right;
    public final float[] BT_SELECTED = new float[] {
            1, 0, 0, 0, -100, 0,
            1, 0, 0, -100, 0, 0, 1, 0, -100, 0, 0, 0, 1, 0
    };
    public final float[] BT_NOT_SELECTED = new float[] {
            1, 0, 0, 0, 0, 0,
            1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0
    };
    
    private long mStartPosition;
    private long mEndPosition;
    
    private int mMemoId;
    private String mMemName;
    private String mMemPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memos_edit);
        
        imageViewGreyLine = (ImageView)findViewById(R.id.imageViewGreyLine);
        LinearLayout.LayoutParams layLine = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT);
        layLine.setMargins(ScalePx.scalePx(this, 16), ScalePx.scalePx(this, 0),
                ScalePx.scalePx(this, 16), 0);
        imageViewGreyLine.setLayoutParams(layLine);
        
        
        btnTrimOrigin = (Button) findViewById(R.id.memo_trim_origin);
        btnTrimOrigin.setHeight(ScalePx.scalePx(this, 88));
        LinearLayout.LayoutParams lay = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT);
        lay.setMargins(ScalePx.scalePx(this, 16), ScalePx.scalePx(this, 16),
                ScalePx.scalePx(this, 16), 0);
        btnTrimOrigin.setLayoutParams(lay);
        btnTrimOrigin.setOnClickListener(this);
          
        btnSaveAsNew = (Button) findViewById(R.id.trim_save_new);
        LinearLayout.LayoutParams lay2 = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT);
        lay2.setMargins(ScalePx.scalePx(this, 16), ScalePx.scalePx(this, 0),
                ScalePx.scalePx(this, 16), ScalePx.scalePx(this, 10));
        btnSaveAsNew.setLayoutParams(lay2);
        btnSaveAsNew.setHeight(ScalePx.scalePx(this, 88));
        btnSaveAsNew.setOnClickListener(this);
        
        btnCancel = (Button) findViewById(R.id.trim_cancel);
        LinearLayout.LayoutParams lay3 = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT);
        lay3.setMargins(ScalePx.scalePx(this, 16), ScalePx.scalePx(this, 10),
                ScalePx.scalePx(this, 16), ScalePx.scalePx(this, 16));
        btnCancel.setLayoutParams(lay3);
        btnCancel.setHeight(ScalePx.scalePx(this, 88));
        btnCancel.setOnClickListener(this);
        
        btnTrimOrigin.setOnTouchListener(onTouch);
        btnSaveAsNew.setOnTouchListener(onTouch);
        btnCancel.setOnTouchListener(onTouch);
        
        //Get Extra parameters
        mMemName = getIntent().getStringExtra("memoName");
        mMemoId = Integer.valueOf(getIntent().getStringExtra("memoId"));
        mMemPath = getIntent().getStringExtra("memoPath");
        mStartPosition = getIntent().getLongExtra("start", 0);
        mEndPosition = getIntent().getLongExtra("end", 0);
    }


    private OnTouchListener onTouch = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.getBackground().setColorFilter(
                            new ColorMatrixColorFilter(BT_SELECTED));
                    v.setBackgroundDrawable(v.getBackground());

                    break;

                case MotionEvent.ACTION_UP:
                    v.getBackground().setColorFilter(
                            new ColorMatrixColorFilter(BT_NOT_SELECTED));
                    v.setBackgroundDrawable(v.getBackground());
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    
    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.memo_trim_origin:
            {
                trim(false);
                Intent intent = new Intent();
                intent.putExtra("memoName", mMemName);
                intent.putExtra("memoId",mMemoId);
                intent.putExtra("memoPath", mMemPath);
                setResult(9001, intent);
                finish();
            }
            break;
            
            case R.id.trim_save_new:
            {
                trim(true);
                Intent intent = new Intent();
                intent.putExtra("memoName", mMemName);
                intent.putExtra("memoId",mMemoId);
                intent.putExtra("memoPath", mMemPath);
                setResult(9001, intent);
                finish();
            }
            break;
            
            case R.id.trim_cancel:
            {
                finish();
            }
            break;
        }

    }
    
    public void trim(Boolean isNewFile)
    {
        AMRFileUtils fileUtils = new AMRFileUtils();
        int startFrame = fileUtils.secondsToFrames(mStartPosition * 0.001);
        int endFrame = fileUtils.secondsToFrames(mEndPosition * 0.001);
        if (startFrame == 0 && endFrame == 0) 
        {
            return;
        }
        else if (mEndPosition - mStartPosition < 1000) 
        {
            return;
        }
        
        File inputFile = new File(mMemPath);
        File outputFile = Recorder.createTempFile();

        try {
          fileUtils.ReadFile(inputFile);
          fileUtils.WriteFile(outputFile, startFrame, endFrame - startFrame);

        } catch (FileNotFoundException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
        inputFile.delete();
        updateVoiceMemo(outputFile, (int)(mEndPosition - mStartPosition));
        mMemPath = outputFile.getAbsolutePath();
    }
    
    private void insertVoiceMemo(File outputFile,int duration,String memName)
    {
        if (duration < 1000) {
            return;
        }
        ContentValues cv = new ContentValues();
        long modDate = outputFile.lastModified();
        long current = System.currentTimeMillis();
        cv.put(VoiceMemo.Memos.DATA, outputFile.getAbsolutePath());
        cv.put(VoiceMemo.Memos.LABEL, memName);
        cv.put(VoiceMemo.Memos.LABEL_TYPE, 0);
        cv.put(VoiceMemo.Memos.CREATE_DATE, current);
        cv.put(VoiceMemo.Memos.MODIFICATION_DATE, (int) (modDate / 1000));
        cv.put(VoiceMemo.Memos.DURATION, duration);
        getContentResolver().insert(VoiceMemo.Memos.CONTENT_URI, cv);
    }

    private void updateVoiceMemo(File outputFile, int duration) 
    {
        if (duration < 1000) {
            return;
          }

          ContentValues cv = new ContentValues();
          long modDate = outputFile.lastModified();
          cv.put(VoiceMemo.Memos.DATA, outputFile.getAbsolutePath());
          cv.put(VoiceMemo.Memos.MODIFICATION_DATE, (int) (modDate / 1000));
          cv.put(VoiceMemo.Memos.DURATION, duration);

          if (mMemoId != -1) {
            Uri memoUri = ContentUris.withAppendedId(VoiceMemo.Memos.CONTENT_URI, mMemoId);
            getContentResolver().update(memoUri, cv, null, null);
          }
    }
}
