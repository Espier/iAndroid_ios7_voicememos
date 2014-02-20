
package org.espier.voicememos7.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import org.espier.voicememos7.util.Recorder;

import java.util.ArrayList;
import java.util.List;

public class WaveView extends View {

    // private int amplitude;
    Paint paint = new Paint();
    List<AudioData> audioDatas = new ArrayList<AudioData>();
    List<TimeFormat> timeDatas = new ArrayList<TimeFormat>();
    Rect mRect;
    float[] mPoints = null;
    Recorder mRecorder;
    int power_per_pixel;
    TimeFormat timeFormat;
    boolean isMiddle;
    Paint greenPaint = new Paint();

    float all_time;
    long get_amp_interval = 20;
    int time_offset;
    boolean isInitTimeData;

    Handler handler;
    Runnable runnable;

    /**
     * @return the timeFormat
     */
    public TimeFormat getTimeFormat() {
        return timeFormat;
    }

    /**
     * @param timeFormat the timeFormat to set
     */
    public void setTimeFormat(TimeFormat timeFormat) {
        this.timeFormat = timeFormat;
    }

    /**
     * @return the mRecorder
     */
    public Recorder getRecorder() {
        return mRecorder;
    }

    /**
     * @param mRecorder the mRecorder to set
     */
    public void setRecorder(Recorder mRecorder) {
        this.mRecorder = mRecorder;
    }

    /**
     * @return the audioDatas
     */
    public List<AudioData> getAudioDatas() {
        return audioDatas;
    }

    /**
     * @param audioDatas the audioDatas to set
     */
    public void setAudioDatas(List<AudioData> audioDatas) {
        this.audioDatas = audioDatas;
    }

    public WaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WaveView(Context context) {
        super(context);
        init();
    }

    private void init()
    {
        // all_time = get_amp_interval*getWidth()/1000;
        // Log.e("all time", all_time+"");
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2.0f);
        
        greenPaint.setColor(Color.GREEN);
        greenPaint.setStrokeWidth(3.0f);

        

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (true) {

                    while (mRecorder != null && mRecorder.getState() == Recorder.RECORDING_STATE) {
                        try {
                            int m = mRecorder.getMaxAmplitude();
                            AudioData data = new AudioData();
                            data.setAmplitude(m);
                            if (audioDatas.size() == getWidth() / 2) {

                                isMiddle = true;

                            }
                            if (isMiddle) {
                                audioDatas.remove(0);
                                time_offset++;

                            }

                            audioDatas.add(data);
                            try {
                                Thread.sleep(get_amp_interval);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            // TODO: handle exception
                        }

                    }
                }
            }
        });
        thread.start();
    }

    /*
     * (non-Javadoc)
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // canvas.drawLine(0, 0, getWidth(), getHeight(), paint);
        // canvas.drawLine(0, 0, getWidth(), 0, paint);
        // canvas.drawLine(0, getHeight()-100, getWidth(), getHeight(), paint);
        // power_per_pixel = 32768/getHeight();

        // Log.e("w,h",getWidth()+","+getHeight());
        power_per_pixel = 65535 / getHeight();
        // Log.e("power_per_pixel", power_per_pixel+"");
        drawTime(canvas, 0);
        
        

        if (audioDatas == null || audioDatas.size()==0) {
          //draw green line 
            canvas.drawLine(0, 70, 0, getHeight()-100, greenPaint);
            return;
        }
 

        for (int i = 0; i < audioDatas.size(); i++)
        {
            // Log.e(""+i,audioDatas.get(i).getAmplitude()/300+"" );
            // long time = System.currentTimeMillis();
            float x;
            x = i;

            // float amp = audioDatas.get(i).getAmplitude()/3;
            float amp = 0;
            // amp = audioDatas.get(i).getDb();
            amp = audioDatas.get(i).getAmplitude() / (power_per_pixel * 2);

            // Log.e("db:", amp+"");
            // amp*=20;
            // amp = (float)Math.sqrt(amp);
            float y_top = getHeight() / 2 - amp;
            float y_bottom = getHeight() / 2 + amp;
            canvas.drawLine(x, y_top, x, y_bottom, paint);
           
            // drawSlideLine(canvas);
            if (i == audioDatas.size() - 1) {
                
                canvas.drawLine(i, 70, i, getHeight()-100, greenPaint);
            }

        }

        if (audioDatas != null && mRecorder.getState() == Recorder.RECORDING_STATE) {

            postInvalidateDelayed(get_amp_interval);

        }

    }

    private void drawTime(Canvas canvas, int offset)
    {
        all_time = get_amp_interval * getWidth() / 1000;
        // float time_per_pixel = all_time/getWidth();
        // Log.e("qqqqqqqq", all_time+"");
        int grid_num = (int) all_time;
        if (!isInitTimeData) {
            for (int i = 0; i < grid_num + 1; i++)
            {
                TimeFormat t = new TimeFormat();
                t.setMinute(0);
                t.setSecond(i);
                timeDatas.add(t);
            }
            isInitTimeData = true;
        }

        if (isMiddle) {
            if (time_offset >= 1000 / get_amp_interval) {

                timeDatas.remove(0);
                TimeFormat t = new TimeFormat();
                t.setMinute(timeDatas.get(timeDatas.size() - 1).getMinute());
                t.setSecond(timeDatas.get(timeDatas.size() - 1).getSecond() + 1);
                if (t.getSecond() == 60) {
                    t.setSecond(0);
                    t.setMinute(t.getMinute() + 1);
                    ;
                }
                timeDatas.add(t);
                time_offset = 0;

            }
        }

        int w = getWidth();
        int s = grid_num;
        float v = w / s;

        for (int i = 0; i < timeDatas.size(); i++)
        {
            canvas.drawText(timeDatas.get(i).getMinute() + ":" + timeDatas.get(i).getSecond(),
                    i * v - time_offset, 50, paint);

        }

        // if (isMiddle) {
        // for(int i=0;i<timeDatas.size();i++)
        // {
        // canvas.drawText(timeDatas.get(i).getMinute()+":"+timeDatas.get(i).getSecond(),
        // i*v-time_offset, 80, paint);
        //
        // }
        // }
        // else {
        // for(int i=0;i<timeDatas.size();i++)
        // {
        // canvas.drawText(timeDatas.get(i).getMinute()+":"+timeDatas.get(i).getSecond(),
        // i*v, 80, paint);
        //
        // }
        // }

    }
    
    //draw time  textview
    private void drawTimeText(Canvas canvas)
    {
        
    }

    // private void drawSlideLine(Canvas canvas)
    // {
    // if (timeFormat==null) {
    // return;
    // }
    // int w = getWidth();
    // int s = 6;
    // float v = w/s;
    // greenPaint.setColor(Color.GREEN);
    // greenPaint.setStrokeWidth(2.0f);
    // if (!isMiddle) {
    //
    // //canvas.drawText(timeFormat.getMinute()+":"+timeFormat.getSecond(),
    // (timeFormat.getSecond()+timeFormat.getMs()/60)*v, 20, paint);
    // canvas.drawLine((timeFormat.getSecond()+timeFormat.getMs()/60)*v, 0,
    // (timeFormat.getSecond()+timeFormat.getMs()/60)*v, getHeight(),
    // greenPaint);
    // }
    // else {
    // canvas.drawLine(getWidth()/2, 0, getWidth()/2, getHeight(), greenPaint);
    // }
    //
    //
    // }

    // private int calculateDB(int amp)
    // {
    // if (amp==0) {
    // return 0;
    // }
    // int amp_ref = 32768;
    // return (int)(20 * Math.log10(amp / amp_ref));
    // }

}
