
package org.espier.voicememos7.ui;

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
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
//import android.view.View;

import android.view.View;

import org.espier.voicememos7.model.CheapSoundFile;
import org.espier.voicememos7.util.Recorder;
import org.espier.voicememos7.util.ScalePx;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class VoiceWaveView extends View implements OnGestureListener {

    long time;
    long time_to_edit = 0;

    // public float time_axix_len;
    public static final int invalidate_rate = 20;

    public static final float num_margin_right = 10;
    // public static final float width_per_second = 60;
    Timer timer;
    TimerTask timerTask;
    Paint voiceLinePaint;
    Paint slideLinePaint;
    Paint editBarPaint;
    Paint timeTopPaint;
    Paint timeTextPaint;
    Paint grayLinePaint;
    Paint darkGrayLinePaint;
    Paint voicedbPaint;
    Paint maskPaint;
    Recorder recorder;
    Handler handler;

    int w;
    float v;
    float slide_line_top_margin = 0;
    float slide_line_bottom_margin = 120;
    List<Float> voice_list = new ArrayList<Float>();
    List<Integer> time_list = new ArrayList<Integer>();

    String[] voice_db_list = new String[] {
            "-10", "-7", "-5", "-3", "-2", "-1", "0"
    };

    int second_index = 0;
    int left_distance_time = 0;

    Context context;
    private int grid_width;
    private float time_x;
    private float time_per_pixel;
    // private float width_per_second = grid_width*4;;
    private float y_xaxis = 0;
    private float h_high_line = 30;
    private float h_low_line = 5;
    private float y_top_line = y_xaxis + h_high_line;
    private float h_block;
    private float y_mid_line;
    private float y_bottom_line;
    private float cicle_radius;
    private float h_bottomLine2timetext;
    private float y_time_text;
    private float h_db2midline;
    private float h_db2db;

    private float margin_lef_init;

    private String blueColorString = "#007aff";
    private int blueColor;

    private String grayColorString = "#808080";
    private int grayColor;

    private String darkGrayColorString = "#8c8c8c";
    private int darkGrayColor;

    int time_text_font_size;

    float x = 0;

    float down_x;
    GestureDetector gestureDetector;

    long time_voice_all;
    float voice_distance;
    

    /***
     * view status
     */
    int viewStatus;
    public static final int VIEW_STATUS_RECORD = 0;
    public static final int VIEW_STATUS_TO_EDIT = 1;
    public static final int VIEW_STATUS_EDIT = 2;

    CheapSoundFile cheapSoundFile;

    int frameGains[];
    int sampleRate;
    int numFrames;
    float factor;
    int display_num;
    float step_width;
    int currentFramPos;
    float timePerFrame;

    float edit_margin_left = 15;
    float edit_margin_right = 35;

    float left_edit_bar_pos;
    float right_edit_bar_pos;
    boolean isZoomLeft;
    boolean isZoomRight;
    
    long clip_left_time;
    long clip_right_time;
    long clip_time;
    int zoomLevel = 3;
    int num_dis;

    /**
     * @return the cheapSoundFile
     */
    public CheapSoundFile getCheapSoundFile() {
        return cheapSoundFile;
    }

    /**
     * @param cheapSoundFile the cheapSoundFile to set
     */
    public void setCheapSoundFile(CheapSoundFile cheapSoundFile) {
        this.cheapSoundFile = cheapSoundFile;
        if (cheapSoundFile != null) {
            frameGains = cheapSoundFile.getFrameGains();
            sampleRate = cheapSoundFile.getSampleRate();
            numFrames = cheapSoundFile.getNumFrames();

            // 计算比例
            int max = getMax(frameGains);
            float height = (y_bottom_line - y_top_line) / 2;
            factor = height * 0.9f / max;

            // 帧间距
            int mSamplePerFrame = cheapSoundFile.getSamplesPerFrame();
            timePerFrame = mSamplePerFrame * 1000 / sampleRate;
            step_width = w * timePerFrame / (time_x * 1000);
            // double time = (mSamplePerFrame * numFrames)/sampleRate;
            time_voice_all = (long)(numFrames * timePerFrame);

            // 能显示的帧数
            display_num = (int) (time_x * 1000 / timePerFrame);
            
            clip_left_time = 0;
            clip_right_time = (int) time_voice_all;
            clip_time = clip_right_time - clip_left_time;

        }

    }

    /**
     * @return the viewStatus
     */
    public int getViewStatus() {
        return viewStatus;
    }

    /**
     * @param viewStatus the viewStatus to set
     */
    public void setViewStatus(int viewStatus) {
        this.viewStatus = viewStatus;
        invalidate();
    }

    /**
     * @return the time_to_edit
     */
    public long getTime_to_edit() {
        return time_to_edit;
    }

    /**
     * @param time_to_edit the time_to_edit to set
     */
    public void setTime_to_edit(long time_to_edit) {
        this.time_to_edit = time_to_edit;
    }

    // List<Long> t_list = new ArrayList<Long>();

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
        // startPlay();
        gestureDetector = new GestureDetector(this);
        viewStatus = VIEW_STATUS_RECORD;
        voiceLinePaint = new Paint();
        voiceLinePaint.setStrokeWidth(2.0f);
        voiceLinePaint.setColor(Color.WHITE);

        slideLinePaint = new Paint();
        blueColor = Color.parseColor(blueColorString);
        slideLinePaint.setColor(blueColor);
        slideLinePaint.setStrokeWidth(1f);

        editBarPaint = new Paint();
        editBarPaint.setColor(Color.RED);
        editBarPaint.setStrokeWidth(1f);

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
        
        maskPaint = new Paint();
        maskPaint.setColor(Color.BLUE);
        maskPaint.setStrokeWidth(0);
        maskPaint.setAlpha(60);

        darkGrayLinePaint = new Paint();
        darkGrayLinePaint.setColor(grayColor);
        ;
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
        time_x = getWidth() / (grid_width * 4);
        time_per_pixel = time_x * 1000 / getWidth();
        h_block = ScalePx.scalePx(context, 176);
        cicle_radius = ScalePx.scalePx(context, 7);
        y_mid_line = y_xaxis + h_high_line + h_block;
        y_bottom_line = y_xaxis + h_high_line + h_block * 2;
        h_bottomLine2timetext = ScalePx.scalePx(context, 28);
        y_time_text = y_bottom_line + h_bottomLine2timetext;
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
        v = grid_width * 4 / 1000f;

        switch (viewStatus) {
            case VIEW_STATUS_RECORD:
                drawRecordView(canvas);
                break;

            case VIEW_STATUS_TO_EDIT:
                drawToEditView(canvas);
                break;

            case VIEW_STATUS_EDIT:
                drawEditView(canvas);
                break;

            default:
                break;
        }
    }

    private void drawEditView(Canvas canvas)
    {
        float s = w - edit_margin_left - edit_margin_right;
        float s_v = numFrames * step_width;
        float fac = s_v/s;
        int _factor = (int) (fac + 0.5);
        _factor =1;
        float start_pos;
        float end_pos;
        float delt_x;
        num_dis = numFrames / _factor;
        float s_dis = num_dis*step_width;
        
        if (fac>1) {
            delt_x = s/numFrames;
            s_v = s;
        }
        else {
            delt_x = step_width;
            s_v = numFrames*step_width;
        }
        voice_distance = s_v;

        
        
        
        start_pos = (s - s_v) / 2 + edit_margin_left;
        end_pos = start_pos+s_v;
        //left_edit_bar_pos = start_pos;
        if (isZoomLeft || isZoomRight) {
            end_pos = start_pos+s_dis*zoomLevel;
        }else {
            left_edit_bar_pos = start_pos;
            right_edit_bar_pos = end_pos;
        }
        //right_edit_bar_pos = end_pos;
        

        float start_move_time_textview = 80;
        float q = (w / 2 - start_move_time_textview);
        try {
            drawSlideLine(canvas, x);
            drawEditBar(canvas);
            drawVoiceEdit(canvas, start_pos,s);
            drawTimeTextViewEdit(canvas, q);
            drawXAxisEdit(canvas, margin_lef_init);
            drawYAxis(canvas);
        } catch (Exception e) {
            Log.e("draw err", e.toString());
        }
    }

    private void drawToEditView(Canvas canvas)
    {
        x = w / 2;
        float start_move_time_textview = 80;
        float q = (w / 2 - start_move_time_textview);
        try {
            drawSlideLine(canvas, x);

            drawTimeTextViewToEdit(canvas, q);
            drawXAxisToEdit(canvas, margin_lef_init);
            drawYAxis(canvas);
            drawVoiceToEdit(canvas, x, margin_lef_init);
        } catch (Exception e) {
            Log.e("drawToEdit err", e.toString());
        }
    }

    private void drawRecordView(Canvas canvas)
    {
        x = w * time / (time_x * 1000) + margin_lef_init;
        x = margin_lef_init + time * v;
        if (x >= w / 2) {
            x = w / 2;
        }
        else {
            time_list.clear();
            for (int i = 0; i <= time_x + 1; i++)
            {
                time_list.add(i);
            }
        }
        // v = w / (time_x * 1000);

        float start_move_time_textview = 80;

        float q = (x < start_move_time_textview) ? 0 : (x - start_move_time_textview);
        try {
            drawSlideLine(canvas, x);
            drawVoice(canvas, x, margin_lef_init);
            drawTimeTextView(canvas, q);
            drawXAxis(canvas, margin_lef_init);
            drawYAxis(canvas);
        } catch (Exception e) {

        }
    }

    private void drawSlideLine(Canvas canvas, float offset)
    {
        float x = offset;
        if (viewStatus != VIEW_STATUS_EDIT) {
            canvas.drawLine(x, y_top_line, x, y_bottom_line, slideLinePaint);
            // canvas.drawLine(x, y_top_line, x, y_bottom_line, voiceLinePaint);
            canvas.drawCircle(x, y_top_line - cicle_radius, cicle_radius, slideLinePaint);
            canvas.drawCircle(x, y_bottom_line + cicle_radius, cicle_radius,
                    slideLinePaint);
        }
        canvas.drawLine(0, y_top_line, getWidth(), y_top_line, grayLinePaint);
        canvas.drawLine(0, y_bottom_line, getWidth(), y_bottom_line
                , grayLinePaint);

        canvas.drawLine(0, y_mid_line, getWidth(), y_mid_line, darkGrayLinePaint);

    }

    private void drawEditBar(Canvas canvas)
    {
        //left
        canvas.drawLine(left_edit_bar_pos, y_top_line, left_edit_bar_pos, y_bottom_line, editBarPaint);
        // canvas.drawLine(x, y_top_line, x, y_bottom_line, voiceLinePaint);
        canvas.drawCircle(left_edit_bar_pos, y_top_line - cicle_radius, cicle_radius, editBarPaint);

        canvas.drawText(timeFormat(clip_left_time), left_edit_bar_pos, y_xaxis
                + timeTopPaint.getTextSize(), timeTopPaint);
       
        //right
        canvas.drawLine(right_edit_bar_pos, y_top_line, right_edit_bar_pos, y_bottom_line, editBarPaint);
        // canvas.drawLine(x, y_top_line, x, y_bottom_line, voiceLinePaint);
        canvas.drawCircle(right_edit_bar_pos, y_bottom_line + cicle_radius, cicle_radius,
                editBarPaint);
        canvas.drawText(timeFormat(clip_right_time), right_edit_bar_pos, y_xaxis
                + timeTopPaint.getTextSize(), timeTopPaint);
        
        //mask
        canvas.drawRect(left_edit_bar_pos, y_top_line, right_edit_bar_pos, y_bottom_line, maskPaint);
    
    }
    
    

    private void drawVoice(Canvas canvas, float s, float offset)
    {
        int n = voice_list.size();
        // float[] points = new float[n * 4];
        //
        // for (int i = 0; i < n; i++)
        // {
        // float x = (offset / n) * i;
        // points[i * 4] = x;
        // points[i * 4 + 1] = mid_y - voice_list.get(i);
        // points[i * 4 + 2] = x;
        // points[i * 4 + 3] = mid_y + voice_list.get(i);
        //
        // }
        // canvas.drawLines(points, voiceLinePaint);

        for (int i = 0; i < voice_list.size(); i++)
        {
            float x_;
            if (x >= w / 2) {

                if (time < time_x / 2 * 1000) {
                    float ss = offset - left_distance_time * v;
                    x_ = (s - ss) / n * i + ss;
                }
                else {
                    x_ = (s / n) * i;
                    // x_ = v*invalidate_rate*i;
                }

            }
            else {
                // x_ = (s-offset)/n*i+offset;
                x_ = offset + v * invalidate_rate * i;
            }
            canvas.drawLine(x_, y_mid_line - voice_list.get(i),
                    x_, y_mid_line + voice_list.get(i), voiceLinePaint);

        }

    }

    private void drawVoiceToEdit(Canvas canvas, float s, float offset)
    {
        if (cheapSoundFile != null) {
            // 计算当前时间帧位置
            currentFramPos = (int) (time_to_edit / timePerFrame);
            float x_ = 0;

            for (int i = currentFramPos, j = display_num / 2; i > 0 && j > 0; i--, j--)
            {
                x_ = w / 2 - (currentFramPos - i) * step_width;
                canvas.drawLine(x_, y_mid_line - (float) frameGains[i] * factor,
                        x_, y_mid_line + (float) frameGains[i] * factor, voiceLinePaint);
            }
            for (int i = currentFramPos, j = 0; i < numFrames && j < display_num / 2; i++, j++)
            {
                x_ = w / 2 + (i - currentFramPos) * step_width;
                canvas.drawLine(x_, y_mid_line - (float) frameGains[i] * factor,
                        x_, y_mid_line + (float) frameGains[i] * factor, voiceLinePaint);
                if (x_ > w - num_margin_right * 3) {
                    break;
                }
            }

        }

    }

    private void drawVoiceEdit(Canvas canvas, float start_pos, float s)
    {
        int pos_l = getFramePositionByTime(clip_left_time);
        int pos_r = getFramePositionByTime(clip_right_time);
        int clip_num = pos_r - pos_l;
        Log.e("l-r", pos_l+","+pos_r);
        float delt_x = s/clip_num;
        float zoom =(isZoomLeft || isZoomRight)?zoomLevel:1;
        
            
//            for (int i = 0; i < numFrames; i++) {
//
//                canvas.drawLine(start_pos + i * delt_x, y_mid_line - (float) frameGains[i] * factor,
//                        start_pos + i * delt_x, y_mid_line + (float) frameGains[i] * factor,
//                        voiceLinePaint);
//                
//                left_edit_bar_pos = start_pos;
//                right_edit_bar_pos = start_pos + numFrames*delt_x;
//            }
            
          //left data
            for (int i = pos_l; i >0; i--) {
                canvas.drawLine(start_pos - (pos_l-i) * delt_x*zoom, y_mid_line - (float) frameGains[i] * factor,
                        start_pos - (pos_l-i) * delt_x*zoom, y_mid_line + (float) frameGains[i] * factor,
                        voiceLinePaint);
            }
            //clip data
            for (int i = 0; i < clip_num; i++) {
                canvas.drawLine(start_pos + i * delt_x*zoom, y_mid_line - (float) frameGains[pos_l+i] * factor,
                        start_pos + i * delt_x*zoom, y_mid_line + (float) frameGains[pos_l+i] * factor,
                        voiceLinePaint);
            }
            //right data
            float clip_width = clip_num*delt_x;
            for (int i = 0; i < numFrames-pos_r; i++) {
                canvas.drawLine(start_pos+clip_width +i * delt_x*zoom, y_mid_line - (float) frameGains[pos_r+i] * factor,
                        start_pos+clip_width +i * delt_x*zoom, y_mid_line + (float) frameGains[pos_r+i] * factor,
                        voiceLinePaint);
            }
       
        
        

    }

  
    private void drawXAxis(Canvas canvas, float offset)
    {
        int grid_num = 4;
        // float s = width_per_second/grid_num;
        float text_offset = ScalePx.scalePx(context, 8);
        for (int i = -1; i < time_list.size(); i++)
        {

            float x = i * grid_width * 4 - v * invalidate_rate * second_index + offset;

            // float x = i * grid_width * 4 - grid_width*4 *
            // (invalidate_rate/1000) * second_index + offset;
            // Log.e("index", x+"");
            float h;
            for (int j = 0; j < grid_num; j++)
            {
                h = (j == 0) ? h_high_line : h_low_line;
                canvas.drawLine(x + j * grid_width, y_xaxis + h_high_line, x + j * grid_width,
                        y_xaxis + h_high_line - h, darkGrayLinePaint);
            }
            if (i != -1) {
                canvas.drawText(timeAxisFormat(time_list.get(i)), x + text_offset, y_xaxis
                        + timeTopPaint.getTextSize(), timeTopPaint);

            }
        }
    }

    private void drawXAxisToEdit(Canvas canvas, float offset)
    {

        int time_ms = (int) (time_to_edit % 1000);
        Log.e("ms", time_ms + "");

        float text_offset = ScalePx.scalePx(context, 8);
        // 当前时间整数点

        float x0 = w / 2 - time_ms / time_per_pixel;
        int index_s = 0;
        for (int i = 0; i < time_x * 4 / 2 + 8; i++)
        {
            float x1 = x0 + i * grid_width;
            float x2 = x0 - i * grid_width;
            float h;
            h = (i % 4 == 0) ? h_high_line : h_low_line;
            canvas.drawLine(x1, y_xaxis + h_high_line, x1, y_xaxis + h_high_line - h,
                    darkGrayLinePaint);
            canvas.drawLine(x2, y_xaxis + h_high_line, x2, y_xaxis + h_high_line - h,
                    darkGrayLinePaint);

            if (i % 4 == 0) {

                int t1 = (int) (time_to_edit / 1000 + index_s);
                int t2 = (int) (time_to_edit / 1000 - index_s);

                index_s++;
                canvas.drawText(timeAxisFormat(t1), x1 + text_offset,
                        y_xaxis + timeTopPaint.getTextSize(), timeTopPaint);
                canvas.drawText(timeAxisFormat(t2), x2 + text_offset,
                        y_xaxis + timeTopPaint.getTextSize(), timeTopPaint);

            }

        }

    }

    private void drawXAxisEdit(Canvas canvas, float offset)
    {

        for (int i = 0; i < time_x * 4 + 1; i++) {
            float h;
            h = (i % 4 == 0) ? h_high_line : h_low_line;
            canvas.drawLine(i * grid_width, y_xaxis + h_high_line, i * grid_width, y_xaxis
                    + h_high_line - h,
                    darkGrayLinePaint);
        }

    }

    private void drawYAxis(Canvas canvas)

    {

        for (int i = 0; i < voice_db_list.length; i++)
        {
            canvas.drawText(voice_db_list[i], w - num_margin_right,
                    y_mid_line + h_db2midline + i * (h_db2db + voicedbPaint.getTextSize())
                            + voicedbPaint.getTextSize(), voicedbPaint);
            canvas.drawText(voice_db_list[i], w - num_margin_right, y_mid_line
                    - h_db2midline - i * (h_db2db + voicedbPaint.getTextSize()), voicedbPaint);
        }

    }

    private void drawTimeTextView(Canvas canvas, float offset)
    {
        canvas.drawText(timeFormat(time), offset + margin_lef_init,
                y_time_text + timeTextPaint.getTextSize(), timeTextPaint);
    }

    private void drawTimeTextViewToEdit(Canvas canvas, float offset)
    {
        canvas.drawText(timeFormat(time_to_edit), offset + margin_lef_init, y_time_text
                + timeTextPaint.getTextSize(), timeTextPaint);
    }
    
    private void drawTimeTextViewEdit(Canvas canvas, float offset)
    {
        canvas.drawText(timeFormat(clip_right_time-clip_left_time), offset + margin_lef_init, y_time_text
                + timeTextPaint.getTextSize(), timeTextPaint);
    }

    private String timeAxisFormat(int t)
    {
        if (t < 0) {
            return "";
        }
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
        if (t <= 0) {
            return "00:00.00";
        }
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
        if (viewStatus == VIEW_STATUS_RECORD) {
            timer = new Timer();
            timerTask = new TimerTask() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    if (viewStatus != VIEW_STATUS_RECORD || recorder == null
                            || recorder.getState() != Recorder.RECORDING_STATE) {
                        return;
                    }

                    try {
                        // Log.e("task", "running...");
                        time += invalidate_rate;
                        // t_list.add(System.currentTimeMillis());
                        // Log.e("duration",
                        // t_list.get(t_list.size()-1)-t_list.get(t_list.size()-2)+"");
                        // if (time >= time_x * 1000 / 2)
                        if (x >= w / 2)
                        {
                            if (time < time_x / 2 * 1000) {
                                left_distance_time += invalidate_rate;
                            }

                            second_index++;
                            if (1000 / invalidate_rate == second_index) {
                                second_index = 0;
                                time_list.remove(0);
                                time_list.add(time_list.get(time_list.size() - 1) + 1);
                            }
                            voice_list.remove(0);
                        }
                        if (recorder != null && !recorder.isReSet) {
                            int amp = recorder.getMaxAmplitude();
                            voice_list.add(amp / 300f);

                        }

                        // voice_list.add(recorder.getMaxAmplitude() / 300);

                        Message msg = new Message();
                        msg.what = 1;
                        handler.sendMessage(msg);
                    } catch (Exception e) {
                        Log.e("task err:", e.toString());
                    }

                }
            };
            timer.schedule(timerTask, invalidate_rate, invalidate_rate);

        }

    }

    public void pause()
    {
        if (timer != null) {
            timer.cancel();

            timer = null;
        }

    }

    public void stop()
    {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    public void startPlay()
    {
        // if (viewStatus == VIEW_STATUS_TO_EDIT) {
        timer = new Timer();
        timerTask = new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (viewStatus != VIEW_STATUS_TO_EDIT || recorder == null
                        || recorder.getState() != Recorder.RECORDING_STATE) {
                    return;
                }

                try {
                    time_to_edit += invalidate_rate;

                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    Log.e("task err:", e.toString());
                }

            }
        };
        timer.schedule(timerTask, invalidate_rate, invalidate_rate);

        // }
    }

    public void clearData()
    {
        voice_list.removeAll(voice_list);
        time_list.removeAll(time_list);
        time = 0;
        second_index = 0;
        left_distance_time = 0;
        v = 0;
        invalidate();
    }

    public void destroy()
    {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (recorder != null) {
            if (recorder.getState() == Recorder.RECORDING_STATE) {
                recorder.stopRecording();
            }
            recorder = null;
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        // TODO Auto-generated method stub
        
        super.onScrollChanged(l, t, oldl, oldt);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getViewStatus() == VIEW_STATUS_TO_EDIT || getViewStatus()==VIEW_STATUS_EDIT) {
            if(event.getAction()==MotionEvent.ACTION_UP)
            {
               isZoomLeft = false;
               isZoomRight = false;
               invalidate();
               Log.e("up","up");
            }
            return gestureDetector.onTouchEvent(event);

            
        }
        
        if (getViewStatus()==VIEW_STATUS_EDIT)
        {
            if(event.getAction()==MotionEvent.ACTION_UP)
            {
               isZoomLeft = false;
               isZoomRight = false;
               invalidate();
               Log.e("up","up");
            }
        }
        
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        if (viewStatus==VIEW_STATUS_EDIT) {
            if (Math.abs(e.getX()-left_edit_bar_pos)<10) {
                isZoomLeft = true;
                
                invalidate();
            }
            if (Math.abs(e.getX()-right_edit_bar_pos)<10) {
                isZoomRight = true;
                
                invalidate();
            }
        }
        return true;
    }
    
    

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        /*
         * final int FLING_MIN_DISTANCE = 100, FLING_MIN_VELOCITY = 200; if
         * (Math.abs(e1.getX() - e2.getX()) > FLING_MIN_DISTANCE &&
         * Math.abs(velocityX) > FLING_MIN_VELOCITY) { // Fling left //
         * Log.i("MyGesture", "Fling left"); float v =(velocityX)/1000 ; float a
         * = 0.05f; float t = Math.abs(v/a); Log.e("t", t+""); float intval =
         * 10; while (t>0) { float s = v*intval-a*intval*intval/2; v =
         * v-a*intval; try { Thread.sleep(10); } catch (InterruptedException e)
         * { // TODO Auto-generated catch block e.printStackTrace(); }
         * t-=intval; time_to_edit += (int)(s*time_per_pixel); if
         * (time_to_edit<0) { time_to_edit = 0; } Log.e("time",
         * time_to_edit+""); invalidate(); } } else if (e2.getX() - e1.getX() >
         * FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) { //
         * Fling right //Log.i("MyGesture", "Fling right"); }
         */
        return true;

    }

    @Override
    public void onLongPress(MotionEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        
        if (viewStatus == VIEW_STATUS_TO_EDIT) {
            int t = (int) (distanceX * time_per_pixel);
            time_to_edit += t;
            if (time_to_edit < 0) {
                time_to_edit = 0;
            }
            if (time_to_edit > time_voice_all) {
                time_to_edit =  time_voice_all;
            }

            invalidate();
        }
        
        if (viewStatus == VIEW_STATUS_EDIT)
        {
            if (isZoomLeft) {
               // long temp = clip_left_time;
                left_edit_bar_pos -= distanceX;
//                if (right_edit_bar_pos -left_edit_bar_pos<20) {
//                    left_edit_bar_pos = right_edit_bar_pos-20;
//                }
                clip_left_time -= distanceX*time_voice_all/(zoomLevel*voice_distance);
                if (clip_right_time-clip_left_time<=1000) {
                    clip_left_time = clip_right_time-1000;
                    return true;
                }
            }
            
            if (isZoomRight) {
                // long temp = clip_left_time;
                 right_edit_bar_pos -= distanceX;
//                 if (right_edit_bar_pos -left_edit_bar_pos<20) {
//                     left_edit_bar_pos = right_edit_bar_pos-20;
//                 }
                 clip_right_time -= distanceX*time_voice_all/(zoomLevel*voice_distance);
                 if (clip_right_time-clip_left_time<=1000) {
                     clip_right_time = clip_right_time+1000;
                     return true;
                 }
             }
            
            invalidate();
        }

        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // TODO Auto-generated method stub
        return true;
    }
    
    

    private int getMax(int[] arr) {

        int max = arr[0];

        for (int x = 1; x < arr.length; x++) {
            if (arr[x] > max)
                max = arr[x];

        }
        return max;

    }
    
    private int getFramePositionByTime(long t)
    {
        int pos = (int) (t / timePerFrame);
        return pos;
    }

}
