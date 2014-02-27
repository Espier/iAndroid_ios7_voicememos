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
import android.view.View;

import org.espier.voicememos7.util.Recorder;
import org.espier.voicememos7.util.ScalePx;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class VoiceWaveView extends View {

    long time;
    //public float time_axix_len;
    public static final int invalidate_rate = 20;
    
    
    public static final float num_margin_right = 10;
   // public static final float width_per_second = 60;
    Timer timer;
    TimerTask timerTask;
    Paint voiceLinePaint;
    Paint slideLinePaint;
    Paint timeTopPaint;
    Paint timeTextPaint;
    Paint grayLinePaint;
    Paint darkGrayLinePaint;
    Paint voicedbPaint;
    Recorder recorder;
    Handler handler;

    int w;
    float v;
    float slide_line_top_margin = 0;
    float slide_line_bottom_margin = 120;
    List<Integer> voice_list = new ArrayList<Integer>();
    List<Integer> time_list = new ArrayList<Integer>();
   
    String[] voice_db_list = new String[]{"-10","-7","-5","-3","-2","-1","0"};

    int second_index = 0;

    Context context;
    private int grid_width;
    private float time_x;
    private float width_per_second = grid_width*4;;
    private float y_xaxis = 0;
    private float h_high_line = 30;
    private float h_low_line = 5;
    private float y_top_line = y_xaxis +h_high_line;
    private float h_block ;
    private float y_mid_line ;
    private float y_bottom_line ;
    private float cicle_radius ;
    private float h_bottomLine2timetext;
    private float y_time_text;
    private float h_db2midline;
    private float h_db2db;
    
    private float margin_lef_init ;
    
    private String blueColorString = "#007aff";
    private int blueColor; 
    
    private String grayColorString = "#808080";
    private int grayColor;
    
    private String darkGrayColorString = "#8c8c8c";
    private int darkGrayColor;
    
    int time_text_font_size;
    
    float x = 0;
    

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
        voiceLinePaint.setStrokeWidth(2.0f);
        voiceLinePaint.setColor(Color.WHITE);
        
        slideLinePaint = new Paint();
        blueColor = Color.parseColor(blueColorString);
        slideLinePaint.setColor(blueColor);
        slideLinePaint.setStrokeWidth(1f);
        
        timeTopPaint = new Paint();
        timeTopPaint.setTextSize(ScalePx.scalePx(context, 24));
        timeTopPaint.setColor(Color.WHITE);
        timeTopPaint.setStrokeWidth(0.5f);
        
        timeTextPaint = new Paint();
        timeTextPaint.setTextSize(ScalePx.scalePx(context, 40));
        timeTextPaint.setColor(Color.WHITE);
        timeTextPaint.setTypeface(Typeface.SANS_SERIF);
        
        grayLinePaint = new Paint();
        grayColor = Color.parseColor(grayColorString);
        grayLinePaint.setColor(grayColor);
        grayLinePaint.setStrokeWidth(2f);
        
        darkGrayLinePaint = new Paint();
        darkGrayLinePaint.setColor(grayColor);;
        darkGrayLinePaint.setStrokeWidth(1f);
        
        voicedbPaint = new Paint();
        darkGrayColor = Color.parseColor(darkGrayColorString);
        voicedbPaint.setColor(darkGrayColor);
        voicedbPaint.setTextAlign(Align.RIGHT);
        voicedbPaint.setTextSize(ScalePx.scalePx(context, 14));

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
    
    private void initView()
    {
        grid_width = ScalePx.scalePx(context, 24);
        time_x = getWidth()/(grid_width*4);
        h_block = ScalePx.scalePx(context, 176);
        cicle_radius = ScalePx.scalePx(context, 7);
        y_mid_line = y_xaxis +h_high_line+h_block;
        y_bottom_line = y_xaxis +h_high_line+h_block*2;
        h_bottomLine2timetext = ScalePx.scalePx(context, 28);
        y_time_text = y_bottom_line+h_bottomLine2timetext;
        h_db2db = ScalePx.scalePx(context, 8);
        h_db2midline = ScalePx.scalePx(context, 13);
        time_text_font_size = ScalePx.scalePx(context, 24);
        
        
        margin_lef_init = ScalePx.scalePx(context, 31);
        
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        // long t1 = System.currentTimeMillis();
        initView();

        
        w = getWidth();
        
        //time_axix_len = w / width_per_second;

        x = w * time / (time_x * 1000) + margin_lef_init;
        if (x >= w / 2) {
            x = w / 2;
        }
        else {
            time_list.clear();
            for (int i = 0; i <= time_x+1; i++)
            {
                time_list.add(i);
            }
        }
        //v = w / (time_x * 1000);
        v = grid_width*4/1000f;
        float start_move_time_textview = 80;
        
        float q  = (x<start_move_time_textview)?0:(x-start_move_time_textview);
        try {
            drawSlideLine(canvas, x);
            drawVoice(canvas, x);
            drawTimeTextView(canvas, q);
            drawXAxis(canvas,margin_lef_init);
            drawYAxis(canvas);
        } catch (Exception e) {
            
        }
        

        // long t2 = System.currentTimeMillis() - t1;
        // Log.e(" draw time", t2+"");
    }

    private void drawSlideLine(Canvas canvas, float offset)
    {
        float x = offset;

        canvas.drawLine(x, y_top_line, x, y_bottom_line, slideLinePaint);
        //canvas.drawLine(x, y_top_line, x, y_bottom_line, voiceLinePaint);
        canvas.drawCircle(x, y_top_line - cicle_radius, cicle_radius, slideLinePaint);
        canvas.drawCircle(x, y_bottom_line + cicle_radius, cicle_radius,
                slideLinePaint);

        canvas.drawLine(0, y_top_line, getWidth(), y_top_line, grayLinePaint);
        canvas.drawLine(0, y_bottom_line, getWidth(), y_bottom_line
                , grayLinePaint);

        canvas.drawLine(0, y_mid_line, getWidth(), y_mid_line, darkGrayLinePaint);

    }

    private void drawVoice(Canvas canvas, float offset)
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
            canvas.drawLine(x, y_mid_line - voice_list.get(i), 
                            x, y_mid_line + voice_list.get(i), voiceLinePaint);
            
        }
        

    }

    private void drawXAxis(Canvas canvas,float offset)
    {
        int grid_num = 4;
        //float s = width_per_second/grid_num;
        float text_offset = ScalePx.scalePx(context, 8);
        for (int i = -1; i < time_list.size(); i++)
        {
            
            float x = i * grid_width * 4 - v* invalidate_rate * second_index + offset;
            
            
            //float x = i * grid_width * 4 - grid_width*4 * (invalidate_rate/1000) * second_index + offset;
            //Log.e("index", x+"");
            float  h;
            for(int j = 0; j < grid_num; j++)
            {
                h=(j==0)?h_high_line:h_low_line;
                canvas.drawLine(x+j*grid_width, y_xaxis+h_high_line, x+j*grid_width, y_xaxis+h_high_line-h, darkGrayLinePaint);
            }
            if (i!=-1) {
                canvas.drawText(timeAxisFormat(time_list.get(i)), x+text_offset, y_xaxis+timeTopPaint.getTextSize(), timeTopPaint);

            }
        }
    }
    
    private void drawYAxis(Canvas canvas)
    
    {
//        float H = midy - slide_line_top_margin - 20;
//        float h = h_block/voice_db_list.length;
        for(int i=0;i<voice_db_list.length;i++)
        {
            canvas.drawText(voice_db_list[i], getWidth()-num_margin_right, y_mid_line+h_db2midline +i*(h_db2db+voicedbPaint.getTextSize())+voicedbPaint.getTextSize(), voicedbPaint);
            canvas.drawText(voice_db_list[i], getWidth()-num_margin_right, y_mid_line -h_db2midline-i*(h_db2db+voicedbPaint.getTextSize()), voicedbPaint);
        }
        
        
    }

    private void drawTimeTextView(Canvas canvas, float offset)
    {
        canvas.drawText(timeFormat(time), offset+margin_lef_init, y_time_text+timeTextPaint.getTextSize(), timeTextPaint);
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
                if (recorder.getState() != Recorder.RECORDING_STATE) {
                    return;
                }

                try {
                    time += invalidate_rate;
                    //if (time >= time_x * 1000 / 2) 
                    if (x >= w / 2)
                    {
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
                if (recorder.getState() != Recorder.RECORDING_STATE) {
                    return;
                }
                try {
                    if (time >= time_x * 1000 / 2)
                    {
                        voice_list.remove(0);

                    }
                    if (recorder!=null && !recorder.isReSet) {
                        voice_list.add(recorder.getMaxAmplitude() / 300);
                    }
                    
                } catch (Exception e) {

                }

            }
        };
        timer.schedule(timerTask, invalidate_rate, invalidate_rate);
        timer.schedule(getAmpTask, 20, 20);

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
        second_index = 0;
        invalidate();
    }

}
