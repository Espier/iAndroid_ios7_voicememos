package org.espier.voicememos7.ui;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import org.espier.voicememos7.R;
import org.espier.voicememos7.util.ScalePx;

import java.util.List;

public class AdapterOfShareListView extends BaseAdapter {

    private List<ResolveInfo> list;
    private Context context;
    private PackageManager pm;
    public AdapterOfShareListView(List<ResolveInfo> list, Context context,PackageManager pm) {
        super();
        this.list = list;
        this.context = context;
        this.pm = pm;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;

        if (null== convertView) {

            view = LayoutInflater.from(context).inflate(R.layout.itemofshare, null);
        }else {
            view = convertView;
        }
        LinearLayout.LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        lp.setMargins(ScalePx.scalePx(context, 15), ScalePx.scalePx(context, 40), ScalePx.scalePx(context, 15), ScalePx.scalePx(context, 10));
        ImageView imageView = (ImageView) view.findViewById(R.id.my_list_icon);
        imageView.setLayoutParams(lp);
        TextView textView = (TextView) view.findViewById(R.id.my_list_name);
        ResolveInfo resolveInfo = (ResolveInfo) getItem(position);
        textView.setText(resolveInfo.loadLabel(pm));

        imageView.setImageDrawable(resolveInfo.loadIcon(pm));

        return view;
    }

}
