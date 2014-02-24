package org.espier.voicememos7.ui;

import android.R.integer;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.espier.voicememos7.util.Recorder;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class VoiceWaveView extends View {

    long time;
    public float time_axix_len;
    public static int invalidate_rate = 20;
    public static float cicle_radius = 5;
    public static float num_margin_right = 10;
    Timer timer;
    TimerTask timerTask;
    Paint voiceLinePaint;
    Paint slideLinePaint;
    Paint timeTextPaint;
    Paint grayLinePaint;
    Paint voicedbPaint;
    Recorder recorder;
    Handler handler;

    int w;
    float v;
    float slide_line_top_margin = 70;
    float slide_line_bottom_margin = 120;
    List<Integer> voice_list = new ArrayList<Integer>();
    List<Integer> time_list = new ArrayList<Integer>();
   
    String[] voice_db_list = new String[]{"0","-1","-2","-3","-5","-7","-10"};

    int second_index;

    public float margin_lef_init = cicle_radius + 1;

    Context context;

    /**
     * @param recorder the recorder to set
     */
    public void setRecorder(Recorder recorder) {
        this.recorder = recorder;
    }

    public VoiceWaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    public VoiceWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        init();
    }

    public VoiceWaveView(Context context) {
        super(context);
        this.context = context;

        init();
    }

    private void init()
    {
        
        voiceLinePaint = new Paint();
        voiceLinePaint.setStrokeWidth(3.0f);
        voiceLinePaint.setColor(Color.WHITE);
        slideLinePaint = new Paint();
        slideLinePaint.setColor(Color.BLUE);
        slideLinePaint.setStrokeWidth(2.0f);
        timeTextPaint = new Paint();
        timeTextPaint.setTextSize(30);
        timeTextPaint.setColor(Color.WHITE);
        grayLinePaint = new Paint();
        grayLinePaint.setColor(Color.GRAY);
        voicedbPaint = new Paint();
        voicedbPaint.setColor(Color.GRAY);
        voicedbPaint.setTextAlign(Align.RIGHT);

        handler = new Handler()
        {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        invalidate();
                        break;

                    default:
                        break;
                }
                super.handleMessage(msg);

            }

        };

    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        // long t1 = System.currentTimeMillis();

        float x = 0;
        w = getWidth();
        time_axix_len = w / 60;

        x = w * time / (time_axix_len * 1000) + margin_lef_init;
        if (x >= w / 2) {
            x = w / 2;
        }
        else {
            time_list.clear();
            for (int i = 0; i <= time_axix_len; i++)
            {
                time_list.add(i);
            }
        }
        v = w / (time_axix_len * 1000);
        float start_move_time_textview = 60;
        float q = 0;

        if (x < start_move_time_textview) {
            q = 0;
        }
        else {
            q = x - start_move_time_textview;

        }

        float mid_y = (getHeight() - slide_line_bottom_margin + slide_line_top_margin) / 2;

        try {
            drawSlideLine(canvas, x, mid_y);
            drawVoice(canvas, x, mid_y);
            drawTimeTextView(canvas, q);
            drawXAxis(canvas);
            drawYAxis(canvas, mid_y);
        } catch (Exception e) {
            // TODO: handle exception
        }
        

        // long t2 = System.currentTimeMillis() - t1;
        // Log.e(" draw time", t2+"");
    }

    private void drawSlideLine(Canvas canvas, float offset, float mid_y)
    {
        float x = offset;

        canvas.drawLine(x, slide_line_top_margin, x, getHeight()
                - slide_line_bottom_margin, slideLinePaint);
        canvas.drawCircle(x, slide_line_top_margin - cicle_radius, cicle_radius, slideLinePaint);
        canvas.drawCircle(x, getHeight() - slide_line_bottom_margin + cicle_radius, cicle_radius,
                slideLinePaint);

        canvas.drawLine(0, slide_line_top_margin, getWidth(), slide_line_top_margin, grayLinePaint);
        canvas.drawLine(0, getHeight() - slide_line_bottom_margin, getWidth(), getHeight()
                - slide_line_bottom_margin, grayLinePaint);

        canvas.drawLine(0, mid_y, getWidth(), mid_y, grayLinePaint);

    }

    private void drawVoice(Canvas canvas, float offset, float mid_y)
    {
        int n = voice_list.size();
//        float[] points = new float[n * 4];
//
//        for (int i = 0; i < n; i++)
//        {
//            float x = (offset / n) * i;
//            points[i * 4] = x;
//            points[i * 4 + 1] = mid_y - voice_list.get(i);
//            points[i * 4 + 2] = x;
//            points[i * 4 + 3] = mid_y + voice_list.get(i);
//
//        }
//        canvas.drawLines(points, voiceLinePaint);
        
        for(int i=0;i<voice_list.size();i++)
        {
            float x = (offset / n) * i;
            canvas.drawLine(x, mid_y-voice_list.get(i), x, mid_y+voice_list.get(i), voiceLinePaint);
        }
        

    }

    private void drawXAxis(Canvas canvas)
    {
        for (int i = 0; i < time_list.size(); i++)
        {
            float x = i * w / time_axix_len - v
                    * invalidate_rate * second_index;
            canvas.drawText(timeAxisFormat(time_list.get(i)), x, 50, voiceLinePaint);
        }
    }
    
    private void drawYAxis(Canvas canvas,float midy)
    
    {
        float H = midy - slide_line_top_margin - 20;
        float h = H/voice_db_list.length;
        for(int i=0;i<voice_db_list.length;i++)
        {
            canvas.drawText(voice_db_list[i], getWidth()-num_margin_right, midy +(i+1)*h+grayLinePaint.getTextSize(), voicedbPaint);
            canvas.drawText(voice_db_list[i], getWidth()-num_margin_right, midy -(i+1)*h, voicedbPaint);
        }
        
        
    }

    private void drawTimeTextView(Canvas canvas, float offset)
    {
        canvas.drawText(timeFormat(time), offset, getHeight() - 70, timeTextPaint);
    }

    private String timeAxisFormat(int t)
    {
        long minute = t / 60;
        long second = t % 60;
        StringBuffer ret = new StringBuffer();

        if (minute < 10) {
            ret.append(0);

        }
        ret.append(minute);
        ret.append(":");
        if (second < 10) {
            ret.append(0);
        }
        ret.append(second);

        return ret.toString();
    }

    private String timeFormat(long t)
    {
        long minute = (t % (1000 * 60 * 60)) / (1000 * 60);
        long second = (t % (1000 * 60)) / 1000;
        long ms = (t % 1000) / 10;
        StringBuffer ret = new StringBuffer();

        if (minute < 10) {
            ret.append(0);
        }
        ret.append(minute);
        ret.append(":");
        if (second < 10) {
            ret.append(0);
        }
        ret.append(second);
        ret.append(".");
        if (ms < 10) {
            ret.append(0);
        }
        ret.append(ms);

        return ret.toString();
    }

    private int calculateDB(int amp)
    {
        if (amp == 0) {
            return 0;
        }
        int amp_ref = 32768;
        int db = (int) (20 * Math.log10(amp / amp_ref));
        Log.e("db", db + "");
        return db;
    }

    public void start()
    {
        timer = new Timer();
        timerTask = new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub

                try {
                    time += invalidate_rate;
                    if (time >= time_axix_len * 1000 / 2) {
                        // Log.e("size", voice_list.size()+"");
                        // voice_list.remove(0);

                        second_index++;
                        if (1000 / invalidate_rate == second_index) {
                            second_index = 0;
                            time_list.remove(0);
                            time_list.add(time_list.get(time_list.size() - 1) + 1);
                        }
                    }

                    // voice_list.add(recorder.getMaxAmplitude() / 300);

                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    Log.e("get amp err:", e.toString());
                }

            }
        };

        TimerTask getAmpTask = new TimerTask() {

            @Override
            public void run() {
                try {
                    if (time >= time_axix_len * 1000 / 2)
                    {
                        voice_list.remove(0);

                    }
                    voice_list.add(recorder.getMaxAmplitude() / 300);
                } catch (Exception e) {

                }

            }
        };
        timer.schedule(timerTask, invalidate_rate, invalidate_rate);
        timer.schedule(getAmpTask, 0, 10);

    }

    public void pause()
    {
        if (timer != null) {
            timer.cancel();
        }

    }

    public void stop()
    {
        if (timer != null) {
            timer.cancel();
        }
    }

    public void clearData()
    {
        voice_list.removeAll(voice_list);  
        time_list.removeAll(time_list);
        time = 0;
        invalidate();
    }

}
