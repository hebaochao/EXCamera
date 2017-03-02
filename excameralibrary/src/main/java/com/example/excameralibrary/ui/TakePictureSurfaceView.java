package com.example.excameralibrary.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
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
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
         */
        public  void TakePictureSuccessResult(String saveImageFilePath);

        /***
         * 手势触发缩放操作时的回调
         * @param zoom
         */
        public  void TakePictureCameraZoomValueChange(int zoom);

        /***
         * 已经开始预览回调
         */
        public  void startedPreview();
    }

    private  final  static  String TAG = "TakePictureSurfaceView";

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
    /***
     * 缩放对象
     */
    private ScaleGestureDetector  myScaleGestureDetector;

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
                    if(myCallBack != null){
                        myCallBack.startedPreview();
                    }
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
        this.setOnTouchListener(this);
        //创建缩放对象  并添加缩放监听回调方法
        myScaleGestureDetector = new ScaleGestureDetector(context,new MyScaleGestureListener());
    }

    /***
     * 拍照结果回调
     * @param data
     */
    @Override
    public void cameratakePicture(byte[] data) {
        //保存图片
        if(this.saveImageFilePath != null && !this.saveImageFilePath.equals("")){
            //保存图片至指定的路径上
//            ImageUtil.getImageUtil().saveImageToFile(bitmap,saveImageFilePath);
            ImageUtil.getImageUtil().saveImageToFile(data,saveImageFilePath);
        }
        //执行回调
        if(myCallBack != null){
            myCallBack.TakePictureSuccessResult(saveImageFilePath);
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
        return myScaleGestureDetector.onTouchEvent(event);
    }



    /***
     * 当前缩放等级 index
     */
    private  int scaleRange = 0;
    /***
     * 缩放最大值
     */
    private  int maxScaleRange = 0;

    /***
     *
     * 缩放监听处理回调
     */

  public   class MyScaleGestureListener  implements ScaleGestureDetector.OnScaleGestureListener{
        // 1.0->变大
        //1.0->0

        /***
         * 上一个变量因子
         */
        private  float startScaleFactor = 1;

      @Override
        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
          //计算缩放因子差值
          int factor = (int)((scaleGestureDetector.getScaleFactor() - startScaleFactor)* 10.0);
          //追加当前缩放等级
          int tempZoom = scaleRange + factor;
//          Log.i(TAG, "onScale: 临时目标缩放值tempZoom"+tempZoom );
//          //下标越界 处理
//              if(tempZoom >= getMaxScaleRange()){
//                  tempZoom =  getMaxScaleRange();
//              }
//              if(tempZoom <= -getMaxScaleRange()) {
//                  tempZoom = -getMaxScaleRange();
//              }
//          //设置缩放值
//          cameraMgt.setZoom(tempZoom);
//          scaleRange = tempZoom;
//          Log.i(TAG, "onScale: 执行缩放操作"+tempZoom);

          setCameraZoom(tempZoom);

        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            System.out.println(detector.getScaleFactor());
//            Log.i(TAG, "onScale: "+detector.getScaleFactor());
//             //设置缩放因子的变化范围不能大于2
            if(detector.getScaleFactor() >2){
//                Log.i(TAG, "onScale: 设置缩放因子的变化范围不能大于2");
                return  true;
            }
            return false;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {

            //设置初始值
            startScaleFactor = scaleGestureDetector.getScaleFactor();
            return true;
        }
    }


    /***
     * 设置缩放值
     * @param tempZoom
     */
    public void setCameraZoom(int  tempZoom){
        //下标越界 处理
        if(tempZoom >= getMaxScaleRange()){
            tempZoom =  getMaxScaleRange();
        }
        if(tempZoom <= -getMaxScaleRange()) {
            tempZoom = -getMaxScaleRange();
        }
        //设置缩放值
        cameraMgt.setZoom(tempZoom);
        scaleRange = tempZoom;
        if(myCallBack != null){
            myCallBack.TakePictureCameraZoomValueChange(tempZoom);
        }
    }

    /***
     * 获取最大缩放值
     * @return
     */
    public int getMaxScaleRange() {
        if(cameraMgt != null && cameraMgt.parameters != null){
            maxScaleRange = cameraMgt.parameters.getMaxZoom();
        }
        return maxScaleRange;
    }


}


