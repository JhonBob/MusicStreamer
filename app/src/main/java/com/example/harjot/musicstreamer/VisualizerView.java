package com.example.harjot.musicstreamer;

/**
 * Created by Harjot on 01-May-16.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class VisualizerView extends View {

    private byte[] mBytes;
    private float[] mPoints;
    private float[] mCirclePoints;
    private float[] mCirclePoints1;
    private Rect mRect = new Rect();
    private Paint mForePaint = new Paint();
    private Paint mForePaint1 = new Paint();

    private float width, height, angle, color, lnDataDistance, distance, size, volume, power, outerRadius, alpha;
    private int time;
    private float normalizedPosition;

    private List<Pair<Float, Float>> pts;
    private List<Pair<Float, Pair<Integer, Integer>>> ptPaint;

    double LOG_MAX = Math.log(128);
    double TAU = Math.PI * 2;
    double MAX_DOT_SIZE = 0.5;
    double BASE = Math.log(4) / LOG_MAX;

    public VisualizerView(Context context) {
        super(context);
        init();
    }

    public VisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VisualizerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mBytes = null;
        mForePaint.setStrokeWidth(1f);
        mForePaint.setAntiAlias(true);
        mForePaint.setColor(Color.rgb(0, 128, 255));
        mForePaint1.setStrokeWidth(1f);
        mForePaint1.setAntiAlias(true);
        mForePaint1.setColor(Color.rgb(255, 128, 0));
        pts = new ArrayList<>();
        ptPaint = new ArrayList<>();
        /*ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
        exec.scheduleAtFixedRate(new Runnable() {
            public void run() {
                invalidate();
            }
        }, 0, 10, TimeUnit.MILLISECONDS);*/
    }

    public void updateVisualizer(byte[] bytes) {
        mBytes = bytes;
        postInvalidate();
    }

//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//        if (mBytes == null) {
//            return;
//        }
//        if (mPoints == null || mPoints.length < mBytes.length * 4) {
//            mPoints = new float[mBytes.length * 4];
//            mCirclePoints = new float[mBytes.length * 4];
//            mCirclePoints1 = new float[mBytes.length * 4];
//        }
//        mRect.set(0, 0, getWidth(), getHeight());
//        float centerX = mRect.width() / 2;
//        float centerY = mRect.height() / 2;
//        float theta = (float) (360.0 / (float) (mBytes.length - 1));
//        //Log.d("BYTES","" +  (mBytes.length - 1));
//        float RADIUS = Math.min(centerX / 2, centerY / 2);
//        //float RADIUS = 100;
//        for (int i = 0; i < mBytes.length - 1; i++) {
//            mPoints[i * 4] = mRect.width() * i / (mBytes.length - 1);
//            mPoints[i * 4 + 1] = ((byte) (mBytes[i] + 128)) * (mRect.height() / 2) / 512;
//            mPoints[i * 4 + 2] = mRect.width() * (i + 1) / (mBytes.length - 1);
//            mPoints[i * 4 + 3] = ((byte) (mBytes[i + 1] + 128)) * (mRect.height() / 2) / 512;
//
//            mCirclePoints[i * 4] = (float) (centerX + (RADIUS + mPoints[i * 4 + 1]) * Math.sin((i * theta *Math.PI)/ (180)));
//            mCirclePoints[i * 4 + 1] = (float) (centerY + (RADIUS + mPoints[i * 4 + 1]) * Math.cos((i * theta *Math.PI)/ (180)));
//            mCirclePoints[i * 4 + 2] = (float) (centerX + (RADIUS + mPoints[i * 4 + 3]) * Math.sin(((i+1) * theta *Math.PI)/ (180)));
//            mCirclePoints[i * 4 + 3] = (float) (centerY + (RADIUS + mPoints[i * 4 + 3]) * Math.cos(((i+1) * theta *Math.PI)/ (180)));
//
//            mCirclePoints1[i * 4] = (float) (centerX - (RADIUS + mPoints[i * 4 + 1]) * Math.sin((i * theta *Math.PI)/ (180)));
//            mCirclePoints1[i * 4 + 1] = (float) (centerY - (RADIUS + mPoints[i * 4 + 1]) * Math.cos((i * theta *Math.PI)/ (180)));
//            mCirclePoints1[i * 4 + 2] = (float) (centerX - (RADIUS + mPoints[i * 4 + 3]) * Math.sin(((i+1) * theta *Math.PI)/ (180)));
//            mCirclePoints1[i * 4 + 3] = (float) (centerY - (RADIUS + mPoints[i * 4 + 3]) * Math.cos(((i+1) * theta *Math.PI)/ (180)));
//
//        }
//        canvas.drawLines(mCirclePoints, mForePaint);
//        canvas.drawLines(mCirclePoints1, mForePaint1);
//    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        width = canvas.getWidth();
        height = canvas.getHeight();
        outerRadius = (float) (Math.min(width, height) * 0.44);
         normalizedPosition = ((float) (System.currentTimeMillis() - MainActivity.startTime)) / (float) (MainActivity.durationInMilliSec);
        //normalizedPosition = (float) (Math.PI - ((float) (360 * z) / (float) (MainActivity.durationInMilliSec)));
        //normalizedPosition = (float) (Math.PI - ((float) time / (float) (MainActivity.durationInMilliSec)));
        if (mBytes == null) {
            return;
        }
        angle = (float) (Math.PI - normalizedPosition * TAU);
        Log.d("ANGLE", angle + "");
        color = 0;
        lnDataDistance = 0;
        distance = 0;
        size = 0;
        volume = 0;
        power = 0;

        float x = (float) Math.sin(angle);
        float y = (float) Math.cos(angle);

        int midx = (int) (width / 2);
        int midy = (int) (height / 2);

        for (int i = 0; i < pts.size(); i++) {
            mForePaint.setColor(ptPaint.get(i).second.first);
            mForePaint.setAlpha(ptPaint.get(i).second.second);
            canvas.drawCircle(pts.get(i).first, pts.get(i).second, ptPaint.get(i).first, mForePaint);
        }

        for (int a = 16; a < mBytes.length/2; a++) {
            Log.d("BYTE", mBytes[a] + "");
            /*float amp = mBytes[(a*2) + 0]*mBytes[(a*2) + 0] + mBytes[(a*2) + 1]*mBytes[(a*2) + 1];
            Log.d("AMP", amp + "");*/
            volume = ((float) (mBytes[a] + 128)) / (float) 255;
            //volume = ((float) amp) / (float) 32768.0;
            Log.d("VOLUME", volume + "");
            x = (float) Math.sin(angle);
            y = (float) Math.cos(angle);
            if (volume < 0.73) {
                continue;
            }
            color = (float) (normalizedPosition - 0.12 + Math.random() * 0.24);
            color = Math.round(color * 360);

            lnDataDistance = (float) ((Math.log(a - 4) / LOG_MAX) - BASE);
            Log.d("LNDIS", lnDataDistance + "");

            distance = lnDataDistance * outerRadius;
            size = (float) (1.5 * volume * MAX_DOT_SIZE + Math.random() * 2);


            if (Math.random() > 0.995) {
                size *= ((mBytes[a] + 128) * 0.2) * Math.random();
                volume *= Math.random() * 0.25;
            }

            Log.d("SIZE", size + "");

            alpha = (float) (volume * 0.09);
            Log.d("ALPHA2", alpha + "");
            x = x * distance;
            y = y * distance;

            float[] hsv = new float[3];
            hsv[0] = color;
            hsv[1] = (float) 0.8;
            hsv[2] = (float) 0.5;

            // mForePaint.setAlpha((int) (155 - alpha * 1000));

            mForePaint.setColor(Color.HSVToColor(hsv));

            /*if (size > 2.5) {
                mForePaint.setAlpha(0);
            } else {
                mForePaint.setAlpha(248);
            }*/

            mForePaint.setAlpha((int) (alpha * 900));
            Log.d("ALPHA3", mForePaint.getAlpha() + "");

            canvas.drawCircle(midx + x, midy + y, size, mForePaint);
            pts.add(Pair.create(midx + x, midy + y));
            ptPaint.add(Pair.create(size, Pair.create(mForePaint.getColor(), mForePaint.getAlpha())));
        }

    }
}
