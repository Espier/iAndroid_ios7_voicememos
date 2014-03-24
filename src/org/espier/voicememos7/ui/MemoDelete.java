
package org.espier.voicememos7.ui;

import org.espier.voicememos7.R;
import org.espier.voicememos7.util.AMRFileUtils;
import org.espier.voicememos7.util.MemosUtils;
import org.espier.voicememos7.util.ScalePx;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class MemoDelete extends Activity implements OnClickListener {

    public Button del, cancel;
    String memoname;
    int mCurrentMemoId;
    private String memopath;
    String index;
    int width;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memos_delete);
        Intent intent = getIntent();
        mCurrentMemoId = intent.getIntExtra("mCurrentMemoId",-1);
        memoname = intent.getStringExtra("memoname");
        memopath = intent.getStringExtra("memopath");
        index = memoname.substring(memoname.indexOf(" ")+1);
        // LinearLayout layout = (LinearLayout) findViewById(R.id.buttonlay);
        // layout.
        del = (Button) findViewById(R.id.memo_del_ok);
        del.setTypeface(MemosUtils.getIosDefaultTypeface(this));
        del.setTextSize(TypedValue.COMPLEX_UNIT_PX, ScalePx.scalePx(this,42));
        int ems = MemosUtils.getEllipsizeByViewWidth(memoname, width);
        del.setEms(ems);
        del.setHeight(ScalePx.scalePx(this, 88));
        LinearLayout.LayoutParams lay = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT);
        lay.setMargins(ScalePx.scalePx(this, 16), ScalePx.scalePx(this, 16),
                ScalePx.scalePx(this, 16), 0);
        del.setLayoutParams(lay);
        del.setOnClickListener(this);
//        if(memoname.length()>16){
//          String strDot = "...";
//          String str = itemname.substring(0, 16- strDot.length());
          
        del.setText(getResources().getString(R.string.delete)+"\""+memoname+"\"");
        cancel = (Button) findViewById(R.id.memo_del_cancel);
        cancel.setTypeface(MemosUtils.getIosDefaultTypeface(this));
        cancel.setTextSize(TypedValue.COMPLEX_UNIT_PX, ScalePx.scalePx(this,42));
        TextPaint paint = cancel.getPaint();
        paint.setFakeBoldText(true);
        
        LinearLayout.LayoutParams lay2 = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT);
        lay2.setMargins(ScalePx.scalePx(this, 16), ScalePx.scalePx(this, 16),
                ScalePx.scalePx(this, 16), ScalePx.scalePx(this, 16));
        cancel.setLayoutParams(lay2);
        cancel.setHeight(ScalePx.scalePx(this, 88));
        cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.memo_del_ok:
                delete();
                
                break;
            case R.id.memo_del_cancel:
                setResult(Activity.RESULT_CANCELED);
                finish();
                break;

            default:
                break;
        }

    }

    private void delete() {
        // TODO Auto-generated method stub
        System.out.println("delete "+ memopath);
        System.out.println(mCurrentMemoId);
        AMRFileUtils.delete(memopath);
        
        SharedPreferences sp = MemoDelete.this.getSharedPreferences("espier",
                EspierVoiceMemos7.MODE_PRIVATE);
        String indexnums = sp.getString("indexs", "");
        if(indexnums.contains(","+index+",")){
            indexnums = indexnums.replace(","+index+",", "");
            sp.edit().putString("indexs", indexnums).commit();
            
        }
        Intent in = new Intent();
        in.putExtra("mCurrentMemoId", mCurrentMemoId);
        in.putExtra("memopath", memopath);
        setResult(Activity.RESULT_OK,in);
        finish();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);
        del = (Button) findViewById(R.id.memo_del_ok);
        width = del.getWidth();
    }
    

}
