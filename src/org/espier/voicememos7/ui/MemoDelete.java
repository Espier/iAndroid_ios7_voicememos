
package org.espier.voicememos7.ui;

import org.espier.voicememos7.R;
import org.espier.voicememos7.util.AMRFileUtils;
import org.espier.voicememos7.util.ScalePx;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class MemoDelete extends Activity implements OnClickListener {

    public static final String MEMO_PATH = "memo_path";
    public static final String MEMO_ID = "memo_id";
    public Button del, cancel;
    String memoname;
    private String memopath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memos_delete);
        Intent intent = getIntent();
        memoname = intent.getStringExtra("memoname");
        memopath = intent.getStringExtra("memopath");
        // LinearLayout layout = (LinearLayout) findViewById(R.id.buttonlay);
        // layout.
        del = (Button) findViewById(R.id.memo_del_ok);
        del.setHeight(ScalePx.scalePx(this, 88));
        LinearLayout.LayoutParams lay = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT);
        lay.setMargins(ScalePx.scalePx(this, 16), ScalePx.scalePx(this, 16),
                ScalePx.scalePx(this, 16), 0);
        del.setLayoutParams(lay);
        del.setOnClickListener(this);
        del.setText(getResources().getString(R.string.delete) + "\"" + memoname + "\"");
        cancel = (Button) findViewById(R.id.memo_del_cancel);
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
                AMRFileUtils.delete(memopath);
                setResult(Activity.RESULT_OK);
                finish();
                break;
            case R.id.memo_del_cancel:
                setResult(Activity.RESULT_CANCELED);
                finish();
                break;

            default:
                break;
        }

    }

}
