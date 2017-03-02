package com.example.excameralibrary.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by baochaoh on 2016/12/1.
 * camera 管理类
 */

public class CameraMgt {

    private  final  static  String TAG = "CameraMgt";

    /***
     * camera回调方法
     */
    public  interface  CameraMgtCallBack{
        /***
         * camer打开完毕回调方法
         */
        public  void cameraOpened();

        /***
         * 拍照完毕 回调方法
         * @param imageData
         */
        public  void cameratakePicture(byte[] imageData);

        /***
         * 回调camera 适配的最适合预览画面尺寸
         * @param previewSize
         */
        public  void cameraSupportedPreviewSize(Camera.Size  previewSize);
    }

    /***
     * Camera  实例
     */
    public Camera  camera;
    /***
     * 单例对象
     */
    private static CameraMgt cameraMgt;
    /***
     * 是否正在预览
     */
    private boolean isPreviewing = false;
    /***
     * camera 设置参数
     */
    public   Camera.Parameters  parameters;
    /***
     * 预览画面比例
     */
    public float cpreviewRate = -1f;
    /***
     * 回调接口对象
     */
    private CameraMgtCallBack myCallBack;




    public static synchronized CameraMgt getCameraMgt() {
        if(cameraMgt == null){
            cameraMgt = new CameraMgt();
        }
        return cameraMgt;
    }





    /***
     * 打开camera
     */
      public void openCamera(CameraMgtCallBack myCallBack){
          //设置回调对象
           this.myCallBack  = myCallBack;
          //由于打开camera 有点耗时
          camera = Camera.open();
          if(myCallBack != null){
              myCallBack.cameraOpened();
          }
     }





    /****
     * 开始预览画面
     * @param holder
     * @param previewRate  预览画面的长宽比
     */
    public void startPreview(SurfaceHolder  holder, float previewRate, com.example.excameralibrary.models.Size  size){

        if(isPreviewing){
            camera.stopPreview();
            return;
        }
        if(camera != null){
            //获取camera 的参数表
            parameters = camera.getParameters();
            //设置相关参数

            //设置对焦模式
            this.setCameraParameterFocus();

            //设置照片格式
            parameters.setPictureFormat(PixelFormat.JPEG);

            // 设置JPG照片的质量
            this.setImageFormat();



            //设置PreviewSize和PictureSize
            Camera.Size pictureSize = CamParaUtil.getInstance().findBestPictureResolution(parameters.getPictureSize(),parameters.getSupportedPictureSizes(),size);
            Log.i(TAG, "startPreview:pictureSize "+pictureSize.width+"height"+pictureSize.height);
            parameters.setPictureSize(pictureSize.width, pictureSize.height);

            Camera.Size previewSize =CamParaUtil.getInstance().findBestPreviewResolution(parameters.getPreviewSize(),parameters.getSupportedPreviewSizes(),size);
            Log.i(TAG, "startPreview: previewSize"+previewSize.width+"height"+previewSize.height);
            parameters.setPreviewSize(previewSize.width, previewSize.height);


            //回调适合尺寸
            if(myCallBack != null){
                myCallBack.cameraSupportedPreviewSize(previewSize);
            }
             //是否支持 缩放
            if( parameters.isZoomSupported()&&parameters.isSmoothZoomSupported()) {
                  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//1连续对焦
                    } else {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                  }
            }


            //为camera设置参数
            camera.setParameters(parameters);

            //设置camera 的显示方向为垂直
            camera.setDisplayOrientation(90);

          try {
              //显示画面到指定holder上
              camera.setPreviewDisplay(holder);
              //开始预览画面
              camera.startPreview();
              camera.cancelAutoFocus();// 2如果要实现连续的自动对焦，这一句必须加上
          } catch (IOException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
          }
            //更新values
            isPreviewing  = true;
            cpreviewRate = previewRate;
            //重新get一次
            parameters = camera.getParameters();

        }

    }





    /***
     * 停止预览
     */
    public void stopPreview(){

        if(null != camera)
        {
            cameraMgt = null;
            myCallBack = null;
            camera.setPreviewCallback(null);
            camera.stopPreview();
            isPreviewing = false;
            cpreviewRate = -1f;
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }


    /**
     * 拍照
     */
    public void doTakePicture(){
        if(isPreviewing && (camera != null)){
            //mShutterCallback 按下快门回调
            camera.takePicture(mShutterCallback, null, mJpegPictureCallback);
        }
    }



    /*为了实现拍照的快门声音及拍照保存照片需要下面三个回调变量*/
   private Camera.ShutterCallback mShutterCallback = new    Camera.ShutterCallback()
            //快门按下的回调，在这里我们可以设置类似播放“咔嚓”声之类的操作。默认的就是咔嚓。
    {
        public void onShutter() {
            // TODO Auto-generated method stub
            Log.i(TAG, "myShutterCallback:onShutter...");
        }
    };

    /***
     * 获取图片回调
     */
    private Camera.PictureCallback mJpegPictureCallback = new  Camera.PictureCallback()
            //对jpeg图像数据的回调,最重要的一个回调
    {
        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Auto-generated method stub

            Log.i(TAG, "myJpegCallback:onPictureTaken...");
//
//            Bitmap b = null;
            if(null != data){
//                b = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图
                camera.stopPreview();
                isPreviewing = false;
                //回调处理
                if(myCallBack != null){
                    myCallBack.cameratakePicture(data);
                }
            }
            //保存图片到sdcard
//            if(null != b)
//            {
//                //设置FOCUS_MODE_CONTINUOUS_VIDEO)之后，myParam.set("rotation", 90)失效。
//                //图片竟然不能旋转了，故这里要旋转下
////                Bitmap rotaBitmap = ImageUtil.getImageUtil().getRotateBitmap(b, 90.0f);
//                //回调处理
//                if(myCallBack != null){
//                    myCallBack.cameratakePicture(b);
//                }
//            }
            //再次进入预览
            camera.startPreview();
            isPreviewing = true;
        }
    };

//*******************************对焦处理**************************************************/
    /****
     * 定点对焦处理
     * @param x  对焦点 x
     * @param y  对焦点 y
     * @param screenWidth  预览view的宽度
     * @param screenHeight 预览view的高度
     */
    public void pointFocus(int x, int y,int screenWidth,int screenHeight) {
        //判断是否支持自动对焦   防止设置错误配置信息
        if(!CamParaUtil.getInstance().isSupportedListHasMode(parameters.getSupportedFocusModes(),Camera.Parameters.FOCUS_MODE_AUTO)){
            return;
        }
        Log.i(TAG, "pointFocus: parameters.getSupportedFocusModes()"+parameters.getSupportedFocusModes().toString());

        if (parameters.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> areas = new ArrayList<Camera.Area>();
            Rect area1 = new Rect(x - 100, x - 100, x + 100, x + 100);
            areas.add(new Camera.Area(area1, 600));
            Rect area2 = new Rect(0, screenWidth,0,screenHeight);
            areas.add(new Camera.Area(area2, 400));
            parameters.setMeteringAreas(areas);
            Log.i(TAG, "pointFocus: 对焦完毕");
        }
        camera.cancelAutoFocus();
        if(CamParaUtil.getInstance().isSupportedListHasMode(parameters.getSupportedFocusModes(),Camera.Parameters.FOCUS_MODE_AUTO)){
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            camera.autoFocus(mAutoFocusCallback);
            return;
        }
        camera.setParameters(parameters);
        camera.autoFocus(mAutoFocusCallback);
    }


    /***
     * 设置camera 的对焦模式
     */
    private void setCameraParameterFocus(){

        if(parameters.getSupportedFocusModes().size() == 1){
            parameters.setFocusMode(parameters.getSupportedFocusModes().get(0));
            return;
        }

        //1、设置对焦模式
        if(CamParaUtil.getInstance().isSupportedListHasMode(parameters.getSupportedFocusModes(),Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            return;
        }
        if(CamParaUtil.getInstance().isSupportedListHasMode(parameters.getSupportedFocusModes(),Camera.Parameters.FOCUS_MODE_AUTO)){
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            camera.autoFocus(mAutoFocusCallback);
            return;
        }
        if(CamParaUtil.getInstance().isSupportedListHasMode(parameters.getSupportedFocusModes(),Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)){
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            return;
        }
    }

    public Camera.AutoFocusCallback mAutoFocusCallback = new Camera.AutoFocusCallback() {

        public void onAutoFocus(boolean success, Camera camera) {
            // TODO Auto-generated method stub
            if(success){
                camera.setOneShotPreviewCallback(null);
                Log.i(TAG, "onAutoFocus: "+ "自动聚焦成功");
            }
        }
    };

    /***
     * 设置图片格式
     */
    private  void setImageFormat(){
        List<Integer> list = parameters.getSupportedPictureFormats();
        for(int i = 0 ;i<list.size();i++){
            Log.i(TAG, "setImageForma: getSupportedPictureFormats"+list.get(i).toString());
        }

        if(parameters.getSupportedPictureFormats().size() == 1){
            parameters.setPictureFormat(parameters.getSupportedPictureFormats().get(0));
            return;
        }
        //尝试设置其他格式格式

    }


    /***
     *设置支持的预览帧数的范围
     */
    private  void setPreviewFpsRange(){
        List<int[]> list =   parameters.getSupportedPreviewFpsRange();
        for(int i = 0 ;i<list.size();i++){
            int [] arr = list.get(i);
            for (int j = 0 ;j<arr.length;j++){
                Log.i(TAG, "setPreviewFpsRange: getSupportedPreviewFpsRange"+arr[j]);
            }

        }

//        if(parameters.getSupportedPictureFormats().size() == 1){
//            parameters.setPictureFormat(parameters.getSupportedPictureFormats().get(0));
//            return;
//        }
        //尝试设置其他格式格式

    }

    /***
     * 是否支持缩放
     */
    private  boolean mIsSupportZoom;
    /***
     * 是否支持缩放变焦
     * @return
     */
    public boolean isSupportZoom()
    {
        boolean isSuppport = camera.getParameters().isSmoothZoomSupported();

        Log.i(TAG, "setZoom: this.isSupportZoom()"+isSuppport);
        return isSuppport;
    }


    /***
     * 预览画面缩放
     * @param zoomRange
     */
    public void setZoom(int zoomRange)
    {
        if (this.isSupportZoom())
        {
            Log.i(TAG, "setZoom: parameters.getMaxZoom())"+parameters.getMaxZoom());
            if(zoomRange>parameters.getMaxZoom() || zoomRange < 0){
                return;
            }
            //设置缩放等级
            //此方法设置缩放等级无效
//          camera.setParameters(parameters);
//          parameters.setZoom(zoomRange);
            camera.startSmoothZoom(zoomRange);
        }
    }




}


