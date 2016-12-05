package com.example.alex.excamera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.excameralibrary.tools.SDCardUtils;
import com.example.excameralibrary.ui.TakePictureSurfaceView;

import java.io.File;
import java.util.Date;

public class MainActivity extends Activity implements TakePictureSurfaceView.TakePictureSurfaceViewCallBack{

     private  TakePictureSurfaceView   takePictureSurfaceView;

    private SeekBar  seekBar;

    private  final  static  String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        takePictureSurfaceView  = (TakePictureSurfaceView)findViewById(R.id.myTakePictureSurfaceView);
        //设置预览比例
        takePictureSurfaceView.cpreviewRate  = 0.5f;
        takePictureSurfaceView.setMyCallBack(this);

        findViewById(R.id.takeBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String  filePath = SDCardUtils.createFilePath("EXCameraTest",new Date().toString()+".png");
                takePictureSurfaceView.doTakePictureAction(filePath);
            }
        });
        Log.i(TAG, "onCreate: ");


        seekBar = (SeekBar) findViewById(R.id.seekBar2);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean flag) {

                if(flag){  //只在手动滑动进度才调用缩放方法
                    //缩放
                    takePictureSurfaceView.setCameraZoom(progress);
//                    Log.i(TAG, "onProgressChanged: b"+b);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
        //重新初始化
        takePictureSurfaceView.init(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: ");
        takePictureSurfaceView.stopPreview();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
    }

    /***
     * 拍照结果回调
     * @param saveImageFilePath
     * @param bitmap
     */
    @Override
    public void TakePictureSuccessResult(String saveImageFilePath, Bitmap bitmap) {
        Log.i(TAG, "TakePictureSuccessResult: saveImageFilePath"+saveImageFilePath );
        Log.i(TAG, "TakePictureSuccessResult: bitmap"+bitmap.getByteCount() );
        Toast.makeText(this,"TakePictureSuccessResult: saveImageFilePath"+saveImageFilePath,Toast.LENGTH_SHORT).show();
    }

    /***
     * 手势触碰改变缩放值的回调
     * @param zoom
     */
    @Override
    public void TakePictureCameraZoomValueChange(int zoom) {
           seekBar.setProgress(zoom);
    }

    @Override
    public void startedPreview() {
        //设置最范围
        seekBar.setMax(takePictureSurfaceView.getMaxScaleRange());
    }
}
