package com.stuhorner.drawingsample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Stu on 3/24/2016.
 */
public class CustomView extends View {
    private Path drawPath;
    private Paint canvasPaint;
    private Paint drawPaint;
    private int paintColor;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private int currentBrushSize;
    private LinkedHashMap<Path, Paint> paths = new LinkedHashMap<Path,Paint>();
    private LinkedHashMap<Path, Paint> undonePaths = new LinkedHashMap<Path, Paint>();
    private List<Integer> brushSizes = new ArrayList<Integer>();
    private List<Integer> undoneBrushSizes = new ArrayList<Integer>();
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
    private boolean mDrawPoint;

    private void init(){
        currentBrushSize = getResources().getInteger(R.integer.medium_size);
        drawPath = new Path();
        paintColor = Color.parseColor("#7DBF43");
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    private void initPaint() {
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(currentBrushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        initPaint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int i = 0;
        for (Map.Entry<Path,Paint> path : paths.entrySet()) {
            path.getValue().setStrokeWidth(brushSizes.get(i++));
            canvas.drawPath(path.getKey(), path.getValue());
        }
        drawPaint.setStrokeWidth(currentBrushSize);
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w,h,oldw,oldh);
        Log.d("DIMEN", Integer.toString(w) + ", " + Integer.toString(h));
        canvasBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(touchX,touchY);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(touchX, touchY);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
            default:
                return false;
        }
        return true;
    }

    private void touch_start(float x, float y) {
        mDrawPoint = true;
        undonePaths.clear();
        drawPath.reset();
        drawPath.moveTo(x, y);
        mX = x;
        mY = y;
    }
    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y- mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mDrawPoint = false;
            drawPath.quadTo(mX, mY, (x + mX)/2, (y+mY)/2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {
        if (mDrawPoint){
            drawPath.quadTo(mX, mY, mX + 1, mY);
        }
        else {
            drawPath.lineTo(mX, mY);
        }
        drawCanvas.drawPath(drawPath, drawPaint);
        paths.put(drawPath, drawPaint);
        brushSizes.add(currentBrushSize);
        drawPath = new Path();
        initPaint();

    }

    public void onClickUndo() {
        Path lastElement = null;
        Iterator<Path> iterator = paths.keySet().iterator();
        while (iterator.hasNext()) { lastElement = iterator.next(); }
        if (paths.size() > 0) {
            undonePaths.put(lastElement, paths.get(lastElement));
            paths.remove(lastElement);
            undoneBrushSizes.add(brushSizes.get(brushSizes.size() - 1));
            brushSizes.remove(brushSizes.size()-1);
            invalidate();
        }
    }

    public void onClickRedo() {
        Path lastElement = null;
        Iterator<Path> iterator = undonePaths.keySet().iterator();
        while (iterator.hasNext()) { lastElement = iterator.next(); }
        if (undonePaths.size() > 0) {
            paths.put(lastElement, undonePaths.get(lastElement));
            undonePaths.remove(lastElement);
            brushSizes.add(undoneBrushSizes.get(undoneBrushSizes.size()-1));
            undoneBrushSizes.remove(undoneBrushSizes.size()-1);
            invalidate();
        }
    }

    public void setPaintColor(int color) {
        drawPaint.setColor(color);
        paintColor = color;
    }
    public int getPaintColor() {
        return drawPaint.getColor();
    }

    public void setBrushSize(int newSize) {
        currentBrushSize = newSize * 2;
        canvasPaint.setStrokeWidth(currentBrushSize);
    }

    public float getBrushSize() {
        return currentBrushSize;
    }

    public void eraseAll() {
        paths.clear();
        brushSizes.clear();
        drawPath = new Path();
        initPaint();
        drawCanvas.drawColor(Color.WHITE);
        invalidate();
    }

}

