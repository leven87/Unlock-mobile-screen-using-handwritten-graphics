package com.example.screenunlock.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Class of drawing handwritten graphics
 */
public class MyView extends View {

    private Paint mPaint;  // painting brush
    private Path mPath;      //painting Path
    private Canvas mCanvas;  //Canvas
    private Bitmap mBitmap;  //bitmap

    private float mLastX;
    private float mLastY;

//    private int clr_bg = Color.WHITE;
//    private int clr_bg = Color.parseColor("#eeeeee");
    private int clr_bg = Color.TRANSPARENT;
    private String first_sign_fname = null;


    public Path path = new Path();
    String finaldata = "X, Y, TStamp, Pres., EndPts\n" + "DataSize: 698\n";
    private int signCounter = 0;

    public MyView(Context context) {
        super(context);
        init();
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mPath = new Path();
        mPaint = new Paint();   //initial painting brush
        mPaint.setColor(Color.BLACK);
        mPaint.setAntiAlias(true); //Anti-aliasing
        mPaint.setDither(true); //Anti-dither
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND); // round at joint
        mPaint.setStrokeCap(Paint.Cap.ROUND); // round at corner
        mPaint.setStrokeWidth(5);   // width of painting brush
        this.setBackgroundColor(clr_bg);
//        this.setBackgroundResource(R.drawable.p202084182520);
//        this.getBackground().setAlpha(32);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        // initial bitmap,Canvas
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    //drawing
    @Override
    protected void onDraw(Canvas canvas) {
        drawPath();
        canvas.drawBitmap(mBitmap, 0, 0, null);//draw and show on the phone
    }

    //drawing path
    private void drawPath(){
        mCanvas.drawPath(mPath, mPaint);//Draw the specified path using the specified paint.
    }

    //event for touch screen
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();
        float xpos = event.getX();
        float ypos = event.getY();
        long t = System.nanoTime()/1000000; //ms
        int time = (int)t;
//        System.out.println ("nanoTime:" + time);
        String data;

        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
                mLastX = xpos;
                mLastY = ypos;
                mPath.moveTo(mLastX, mLastY);

                printPoints(event);
                path.moveTo(xpos, ypos);
                return true;

            case MotionEvent.ACTION_MOVE:
//                int dx = Math.abs(xpos - mLastX);
//                int dy = Math.abs(ypos - mLastY);
//                if (dx > 3 || dy > 3)
//                    mPath.lineTo(xpos, ypos);

                printPoints(event);
//                mPath.lineTo(xpos, ypos); //draw the line on the screen
//                path.lineTo(xpos, ypos); //record the path for output file


                if (Math.abs(mLastX - xpos) < 3 && Math.abs(mLastY - ypos) < 3) {//贝塞尔曲线，让画笔平滑
                } else {
                    mPath.quadTo(mLastX, mLastY, (xpos + mLastX) / 2, (ypos + mLastY) / 2);
                    path.quadTo(mLastX, mLastY, (xpos + mLastX) / 2, (ypos + mLastY) / 2);
                    mLastX = xpos ;
                    mLastY = ypos ;
                }

                mLastX = xpos;
                mLastY = ypos;
                break;

            case MotionEvent.ACTION_UP:
                printPoints(event);
                return true;

            default:
                return false;
        }

        invalidate();
        return true;
    }

    /**
     * save graphics to file as pic
     * @param filename
     * @throws FileNotFoundException
     */
    public void saveToFile(String filename) throws IOException {
        if(this.signCounter == 0){
                this.first_sign_fname = filename + ".png";
        }

        //create png and sig file
        File f_png = new File(filename + ".png");
        if(f_png.exists())
            throw new RuntimeException("file：" + filename + " already exists！");
        File f_sig = new File(filename + ".sig");
        if(f_sig.exists())
            throw new RuntimeException("file：" + filename + " already exists！");

        FileOutputStream fos = new FileOutputStream(f_png);
        FileOutputStream fos2 = new FileOutputStream(f_sig);

        fos2.write(finaldata.getBytes());
        //compress bitmap to other format (PNG)
        mBitmap.compress(Bitmap.CompressFormat.PNG, 50, fos);
        fos.close();
        fos2.close();
    }

    /**
     * clear canvas
     */
    public void clear(int signCounter) {
        this.signCounter = signCounter;
        mPath.reset();
        path.reset();
        finaldata = "X, Y, TStamp, Pres., EndPts\n" + "DataSize: 698\n";
        mCanvas.drawColor(Color.parseColor("#eeeeee"));
        mCanvas.drawColor(clr_bg);
        this.setBackgroundColor(clr_bg);
        if(signCounter >= 1) {
//            this.setBackgroundResource(R.drawable.p202084182520);
            Bitmap bitmap = BitmapFactory.decodeFile(first_sign_fname);
            Resources res = getResources();
            System.out.print(bitmap);
            BitmapDrawable bd = new BitmapDrawable(res, bitmap);
            this.setBackground(bd);
            this.getBackground().setAlpha(32);
        }
        invalidate();

    }

    /**
     * record all the points of user's graphics
     * @param ev event
     */
    void printPoints(MotionEvent ev) {
        final int historySize = ev.getHistorySize();
        final int pointerCount = ev.getPointerCount();

        int action = ev.getAction();
        String data;
        long pointTime;
        float xpos, ypos;
        switch (action) {
            case MotionEvent.ACTION_DOWN :
                pointTime =   ev.getEventTime();
                xpos = ev.getX(0);
                ypos = ev.getY(0);
                data = Float.toString(xpos) + " " + Float.toString(ypos) + " " + Long.toString(pointTime) + " " + Integer.toString(0) + " " + Integer.toString(0) + "\n";
                finaldata += data;
                break;
            case MotionEvent.ACTION_MOVE:
                //record history point data
                for (int h = 0; h < historySize; h++) {
                pointTime =   ev.getHistoricalEventTime(h);
                for (int p = 0; p < pointerCount; p++) {
                    xpos = ev.getHistoricalX(p, h);
                    ypos = ev.getHistoricalY(p, h);
                    data = Float.toString(xpos) + " " + Float.toString(ypos) + " " + Long.toString(pointTime) + " " + Integer.toString(0) + " " + Integer.toString(0) + "\n";
                    finaldata += data;
                }
            }

                pointTime =   ev.getEventTime();
                xpos = ev.getX(0);
                ypos = ev.getY(0);
                data = Float.toString(xpos) + " " + Float.toString(ypos) + " " + Long.toString(pointTime) + " " + Integer.toString(0) + " " + Integer.toString(0) + "\n";
                finaldata += data;
                break;

            case MotionEvent.ACTION_UP:
                pointTime =   ev.getEventTime();
                xpos = ev.getX(0);
                ypos = ev.getY(0);
                data = Float.toString(xpos) + " " + Float.toString(ypos) + " " + Long.toString(pointTime) + " " + Integer.toString(0) + " " + Integer.toString(1) + "\n";
                finaldata += data;
                break;
        }

        return;
    }
}
