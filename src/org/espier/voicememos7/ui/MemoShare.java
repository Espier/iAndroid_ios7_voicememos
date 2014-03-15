
package org.espier.voicememos7.ui;

import org.espier.voicememos7.R;
import org.espier.voicememos7.util.MemosUtils;
import org.espier.voicememos7.util.ScalePx;

import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class MemoShare extends Activity implements OnClickListener {
    String path;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memos_share);
        TextView text = (TextView) findViewById(R.id.memo_del_cancel);
        text.setTypeface(MemosUtils.getIosThTypeface(this));
        text.setTextSize(TypedValue.COMPLEX_UNIT_PX, ScalePx.scalePx(this,42));
        text.setOnClickListener(this);

        Intent intent = getIntent();
        path = intent.getExtras().getString("path");
        HorizontalListView listview = (HorizontalListView) findViewById(R.id.horizontalScrollView1);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                ScalePx.scalePx(this, 235));
        listview.setLayoutParams(lp);
        final List list = MemosUtils.getShareApps(this);
        AdapterOfShareListView adapter = new AdapterOfShareListView(list, this,
                this.getPackageManager());
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                ResolveInfo appInfo = (ResolveInfo) list.get(arg2);
                Uri uri = Uri.parse("file:///" + path);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setComponent(new ComponentName(appInfo.activityInfo.packageName,
                        appInfo.activityInfo.name));
                shareIntent.setType("audio/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(shareIntent);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.memo_del_cancel:
                this.finish();
                break;

            default:
                break;
        }

    }

}
