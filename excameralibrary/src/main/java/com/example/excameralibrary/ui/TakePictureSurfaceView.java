package com.example.excameralibrary.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.example.excameralibrary.models.Size;
import com.example.excameralibrary.tools.CameraMgt;
import com.example.excameralibrary.tools.DisplayUtil;
import com.example.excameralibrary.tools.ImageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by baochaoh on 2016/12/2.
 *
 * 拍照SurfaceView
 */

public class TakePictureSurfaceView extends SurfaceView  implements SurfaceHolder.Callback,CameraMgt.CameraMgtCallBack,View.OnTouchListener{

    /***
     * 回调接口
     */
    public  interface TakePictureSurfaceViewCallBack{
        /***
         * 拍照完成回调接口
         * @param saveImageFilePath
         * @param bitmap
         */
        public  void TakePictureSuccessResult(String saveImageFilePath,Bitmap bitmap);
    }


    /***
     * SurfaceView 管理者
     * SurfaceHolder
     */
    public SurfaceHolder surfaceHolder;
    /***
     * camera管理类
     */
    public CameraMgt cameraMgt;
    /***
     * 预览画面比例
     */
    public float cpreviewRate = -1f;
    /***
     * 图片保存路径
     */
    private String saveImageFilePath = null;
    /***
     * 接口回调对象
     */
    private  TakePictureSurfaceViewCallBack myCallBack;

    private  Context context;
    /***
     * previewSize
     */
    private Size  previewSize  ;


    public TakePictureSurfaceView(Context context) {

        super(context);
        init(context);
    }

    public TakePictureSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TakePictureSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /***
     * 初始化
     */
    public   void  init(Context context){
        this.context = context;
        cameraMgt = CameraMgt.getCameraMgt();
        surfaceHolder   = this.getHolder();
        //translucent半透明 transparent透明
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //SurfaceHolder 的 回调接口
        surfaceHolder.addCallback(this);
          //默认全屏的比例预览
        cpreviewRate = DisplayUtil.getScreenRate(context);
        this.setOnTouchListener(this);
    }



    //实现SurfaceHolder.Callback

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        //开启线程 打开 Camera
        HandlerThread  handlerThread  = new HandlerThread("openCameraThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper(),null);
        //执行耗时操作
        handler.post(new Runnable() {
            @Override
            public void run() {
                 if(cameraMgt != null){
                     //打开camera
                     cameraMgt.openCamera(TakePictureSurfaceView.this);
                 }
            }
        });

    }


    Handler  handler;

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        stopPreview();
    }



    /***
     * 停止预览
     */
    public void stopPreview(){
        if(cameraMgt != null){
            //SurfaceHolder 的 回调接口
            surfaceHolder.removeCallback(this);
            //停止预览
            cameraMgt.stopPreview();
            cameraMgt = null;
        }
    }



    //实现CameraMgt.CameraMgtCallBack

    /***
     * camera 打开完毕回调
     */
    @Override
    public void cameraOpened() {
          //打开预览
        this.post(new Runnable() {
            @Override
            public void run() {
                if(cameraMgt != null){
                    startPreview();
                }
            }
        });
    }

    /***
     * 预览
     */
    private  void startPreview(){
        //开始预览
        //默认全屏尺寸
        Point  point =  DisplayUtil.getScreenMetrics(context);
        previewSize = new Size( point.x,  point.y);
        cameraMgt.startPreview(surfaceHolder,cpreviewRate,previewSize);
    }

    /***
     * 拍照结果回调
     * @param bitmap
     */
    @Override
    public void cameratakePicture(Bitmap bitmap) {
        //保存图片
        if(this.saveImageFilePath != null && !this.saveImageFilePath.equals("")){
            //保存图片至指定的路径上
            ImageUtil.getImageUtil().saveImageToFile(bitmap,saveImageFilePath);
        }
        //执行回调
        if(myCallBack != null){
            myCallBack.TakePictureSuccessResult(saveImageFilePath,bitmap);
        }

    }

    /***
     * 更新适合camera 预览画面的尺寸
     * @param previewSize
     */
    @Override
    public void cameraSupportedPreviewSize(Camera.Size previewSize) {
//        ViewGroup.LayoutParams  params =   this.getLayoutParams();
//        params.width  = previewSize.width;
//        params.height = previewSize.height;
//        this.setLayoutParams(params);
    }

    /**
     * 拍摄照片
     */
    public void doTakePictureAction(String saveImagePath){
        this.saveImageFilePath  = saveImagePath;
        cameraMgt.doTakePicture();
    }

    public void setMyCallBack(TakePictureSurfaceViewCallBack myCallBack) {
        this.myCallBack = myCallBack;
    }

    public String getSaveImageFilePath() {
        return saveImageFilePath;
    }




    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN: //单击 处理  对焦
                cameraMgt.pointFocus((int)event.getX(),(int)event.getY(),previewSize.width,previewSize.height);
                break;
        }
        return false;
    }




}


