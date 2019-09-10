package com.cqsynet.swifi.scaner;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Author: sayaki
 * Date: 2017/4/27
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private Camera camera;
    private SurfaceHolder surfaceHolder;

    public CameraPreview(Context context) {
        this(context, null);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
    }

    public SurfaceHolder getSurfaceHolder() {
        return surfaceHolder;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i("CameraPreview", "@@@@surfaceCreated");
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i("CameraPreview", "@@@@surfaceChanged");
        if (holder.getSurface() == null || camera == null) {
            return;
        }
        camera.stopPreview();
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i("CameraPreview", "@@@@surfaceDestroyed");
    }
}
