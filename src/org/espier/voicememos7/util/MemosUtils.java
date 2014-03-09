/*
 * Copyright (C) 2011 The Android Open Source Project Copyright (C) 2013
 * robin.pei(webfanren@gmail.com)
 * 
 * The code is developed under sponsor from Beijing FMSoft Tech. Co. Ltd(http://www.fmsoft.cn)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.espier.voicememos7.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextPaint;

import org.espier.voicememos7.R;
import org.espier.voicememos7.model.VoiceMemo;
import org.espier.voicememos7.ui.EspierVoiceMemos7;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class MemosUtils {
    private static StringBuilder sFormatBuilder = new StringBuilder();
    private static Formatter sFormatter = new Formatter(sFormatBuilder, Locale.getDefault());
    private static final Object[] sTimeArgs = new Object[5];

    public static String makeTimeString(Context context, long secs) {
        String durationformat =
                context.getString(secs < 3600 ? R.string.durationformatshort
                        : R.string.durationformatlong);

        /*
         * Provide multiple arguments so the format can be changed easily by
         * modifying the xml.
         */
        sFormatBuilder.setLength(0);

        final Object[] timeArgs = sTimeArgs;
        timeArgs[0] = secs / 3600;
        timeArgs[1] = secs / 60;
        timeArgs[2] = (secs / 60) % 60;
        timeArgs[3] = secs;
        timeArgs[4] = secs % 60;

        return sFormatter.format(durationformat, timeArgs).toString();
    }

    public static void shareMemo(Context context, String path) {
        Uri uri = Uri.parse("file:///" + path);
        Intent it = new Intent(Intent.ACTION_SEND);
        it.putExtra(Intent.EXTRA_STREAM, uri);
        it.setType("audio/*");
        context.startActivity(Intent.createChooser(it, context.getString(R.string.list_share_title)));
    }

    public static List<ResolveInfo> getShareApps(Context context) {
        List<ResolveInfo> mApps = new ArrayList<ResolveInfo>();
        Intent intent = new Intent(Intent.ACTION_SEND, null);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType("audio/*");
        PackageManager pManager = context.getPackageManager();
        mApps = pManager.queryIntentActivities(intent,
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
        return mApps;
    }

    public static String Ellipsize(String str,Context activity) {
        TextPaint paint = new TextPaint();
      int  width = ((Activity)activity).getWindowManager().getDefaultDisplay().getWidth();
        float size = paint.measureText(str);
        float charsize = size/str.length();
        final int LENGTH = (int) (width/charsize);
        if (str.length() > LENGTH) {
            return str.substring(0, LENGTH) + "...";
        } else {
            return str;
        }
    }
    
    public static String makeDateString(Context context, int type, long timestamp) {
        String dateFormat = context.getString(R.string.date_time_format);
        if (type == EspierVoiceMemos7.LABEL_TYPE_NONE) {
            dateFormat = context.getString(R.string.date_format);
        }
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        Date d = new Date(timestamp);
        return format.format(d);
    }
    
    public static VoiceMemo getMemoByID(Context context,String id){
        Cursor cs1 =  context.getContentResolver().query(VoiceMemo.Memos.CONTENT_URI, new String[]{"_id","label","data","created","modified"}, "_id=?", new String[]{id}, null);
        VoiceMemo memo = new VoiceMemo();
        if(cs1.getCount()==1){
            cs1.moveToNext();
            String _id = cs1.getString(cs1.getColumnIndexOrThrow("_id"));
            String path = cs1.getString(cs1.getColumnIndexOrThrow("data"));
            long created = cs1.getLong(cs1.getColumnIndexOrThrow("created"));
            String modified = cs1.getString(cs1.getColumnIndexOrThrow("modified"));
            String memoname = cs1.getString(cs1.getColumnIndexOrThrow("label"));
            memo.setMemName(memoname);
            memo.setMemId(_id);
            SimpleDateFormat f = new SimpleDateFormat("yy-MM-dd");
            memo.setMemPath(path);
            memo.setMemCreatedDate(f.format(new Date(created)));
            memo.setModifiedDate(modified);
        }
        return memo;
    }
    
    public static VoiceMemo getMemoByPath(Context context,String path){
        Cursor cs1 =  context.getContentResolver().query(VoiceMemo.Memos.CONTENT_URI, new String[]{"_id","label","data","created","modified"}, "data=?", new String[]{path}, null);
        VoiceMemo memo = new VoiceMemo();
        if(cs1.getCount()==1){
            cs1.moveToNext();
            String _id = cs1.getString(cs1.getColumnIndexOrThrow("_id"));
            String memopath = cs1.getString(cs1.getColumnIndexOrThrow("data"));
            long created = cs1.getLong(cs1.getColumnIndexOrThrow("created"));
            String modified = cs1.getString(cs1.getColumnIndexOrThrow("modified"));
            String memoname = cs1.getString(cs1.getColumnIndexOrThrow("label"));
            memo.setMemName(memoname);
            memo.setMemId(_id);
            SimpleDateFormat f = new SimpleDateFormat("yy-MM-dd");
            memo.setMemPath(memopath);
            memo.setMemCreatedDate(f.format(new Date(created)));
            memo.setModifiedDate(modified);
        }
        return memo;
    }
    
}
