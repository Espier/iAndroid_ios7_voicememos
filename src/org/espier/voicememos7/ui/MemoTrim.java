
package org.espier.voicememos7.ui;

import android.R.integer;
import android.app.Activity;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import org.espier.voicememos7.R;
import org.espier.voicememos7.model.VoiceMemo;
import org.espier.voicememos7.util.MemosUtils;
import org.espier.voicememos7.util.ScalePx;

public class MemoTrim extends Activity implements OnClickListener {
    private Button btnSaveAsNew, btnTrimOrigin, btnCancel;
    private ImageView imageViewGreyLine;
    long voice_trim_from;
    long voice_trim_right;
    VoiceMemo voiceMemo;
    public final float[] BT_SELECTED = new float[] {
            1, 0, 0, 0, -100, 0,
            1, 0, 0, -100, 0, 0, 1, 0, -100, 0, 0, 0, 1, 0
    };
    public final float[] BT_NOT_SELECTED = new float[] {
            1, 0, 0, 0, 0, 0,
            1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0
    };
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
                
            }
            break;
            
            case R.id.trim_save_new:
            {
                
            }
            break;
            
            case R.id.trim_cancel:
            {
                finish();
            }
            break;
        }

    }

}
