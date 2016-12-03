package com.example.excameralibrary.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;


/**
 * Created by baochaoh on 2016/12/2.
 *camera 的参数工具类
 */
public class CamParaUtil {


    private static final String TAG = "CamParaUtil";
    private CameraSizeComparator sizeComparator = new CameraSizeComparator();
    private static CamParaUtil myCamPara = null;

    /**
     * 最小预览界面的分辨率
     */
    private static final int MIN_PREVIEW_PIXELS = 480 * 320;
    /**
     * 最大宽高比差
     */
    private static final double MAX_ASPECT_DISTORTION = 0.15;

    private CamParaUtil(){

    }
    public static CamParaUtil getInstance(){
        if(myCamPara == null){
            myCamPara = new CamParaUtil();
            return myCamPara;
        }
        else{
            return myCamPara;
        }
    }





    /**
     * 找出最适合的预览界面分辨率
     *
     * @return
     */
    public Camera.Size findBestPreviewResolution(Camera.Size defaultPreviewResolution, List<Camera.Size> rawSupportedSizes, com.example.excameralibrary.models.Size screenSize) {


        if (rawSupportedSizes == null) {
            return defaultPreviewResolution;
        }
        // 按照分辨率从大到小排序
        List<Camera.Size> supportedPreviewResolutions = new ArrayList<Camera.Size>(rawSupportedSizes);
        Collections.sort(supportedPreviewResolutions, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;
                if (bPixels < aPixels) {
                    return -1;
                }
                if (bPixels > aPixels) {
                    return 1;
                }
                return 0;
            }
        });

        StringBuilder previewResolutionSb = new StringBuilder();
        for (Camera.Size supportedPreviewResolution : supportedPreviewResolutions) {
            previewResolutionSb.append(supportedPreviewResolution.width).append('x').append(supportedPreviewResolution.height)
                    .append(' ');
        }
        Log.v(TAG, "Supported preview resolutions: " + previewResolutionSb);


        // 移除不符合条件的分辨率

        double screenAspectRatio = (double) screenSize.width
                / (double)screenSize.height;
        Iterator<Camera.Size> it = supportedPreviewResolutions.iterator();
        while (it.hasNext()) {
            Camera.Size supportedPreviewResolution = it.next();
            int width = supportedPreviewResolution.width;
            int height = supportedPreviewResolution.height;

            // 移除低于下限的分辨率，尽可能取高分辨率
            if (width * height < MIN_PREVIEW_PIXELS) {
                it.remove();
                continue;
            }

            // 在camera分辨率与屏幕分辨率宽高比不相等的情况下，找出差距最小的一组分辨率
            // 由于camera的分辨率是width>height，我们设置的portrait模式中，width<height
            // 因此这里要先交换然preview宽高比后在比较
            boolean isCandidatePortrait = width > height;
            int maybeFlippedWidth = isCandidatePortrait ? height : width;
            int maybeFlippedHeight = isCandidatePortrait ? width : height;
            double aspectRatio = (double) maybeFlippedWidth / (double) maybeFlippedHeight;
            double distortion = Math.abs(aspectRatio - screenAspectRatio);
            if (distortion > MAX_ASPECT_DISTORTION) {
                it.remove();
                continue;
            }

            // 找到与屏幕分辨率完全匹配的预览界面分辨率直接返回
            if (maybeFlippedWidth == screenSize.width
                    && maybeFlippedHeight ==screenSize.height) {
                return supportedPreviewResolution;
            }
        }

        // 如果没有找到合适的，并且还有候选的像素，则设置其中最大比例的，对于配置比较低的机器不太合适
        if (!supportedPreviewResolutions.isEmpty()) {
            Camera.Size largestPreview = supportedPreviewResolutions.get(0);
            return largestPreview;
        }

        // 没有找到合适的，就返回默认的

        return defaultPreviewResolution;
    }

    /***
     * 找出最合适的图片尺寸
     * @return
     */
    public Camera.Size findBestPictureResolution(  Camera.Size defaultPictureResolution,List<Camera.Size> supportedPicResolutions, com.example.excameralibrary.models.Size screenSize) {


        StringBuilder picResolutionSb = new StringBuilder();
        for (Camera.Size supportedPicResolution : supportedPicResolutions) {
            picResolutionSb.append(supportedPicResolution.width).append('x')
                    .append(supportedPicResolution.height).append(" ");
        }
        Log.d(TAG, "Supported picture resolutions: " + picResolutionSb);


        Log.d(TAG, "default picture resolution " + defaultPictureResolution.width + "x"
                + defaultPictureResolution.height);

        // 排序
        List<Camera.Size> sortedSupportedPicResolutions = new ArrayList<Camera.Size>(
                supportedPicResolutions);
        Collections.sort(sortedSupportedPicResolutions, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;
                if (bPixels < aPixels) {
                    return -1;
                }
                if (bPixels > aPixels) {
                    return 1;
                }
                return 0;
            }
        });

        // 移除不符合条件的分辨率
        double screenAspectRatio = (double) screenSize.width
                / (double) screenSize.height;
        Iterator<Camera.Size> it = sortedSupportedPicResolutions.iterator();
        while (it.hasNext()) {
            Camera.Size supportedPreviewResolution = it.next();
            int width = supportedPreviewResolution.width;
            int height = supportedPreviewResolution.height;

            // 在camera分辨率与屏幕分辨率宽高比不相等的情况下，找出差距最小的一组分辨率
            // 由于camera的分辨率是width>height，我们设置的portrait模式中，width<height
            // 因此这里要先交换然后在比较宽高比
            boolean isCandidatePortrait = width > height;
            int maybeFlippedWidth = isCandidatePortrait ? height : width;
            int maybeFlippedHeight = isCandidatePortrait ? width : height;
            double aspectRatio = (double) maybeFlippedWidth / (double) maybeFlippedHeight;
            double distortion = Math.abs(aspectRatio - screenAspectRatio);
            if (distortion > MAX_ASPECT_DISTORTION) {
                it.remove();
                continue;
            }
        }

        // 如果没有找到合适的，并且还有候选的像素，对于照片，则取其中最大比例的，而不是选择与屏幕分辨率相同的
        if (!sortedSupportedPicResolutions.isEmpty()) {
            return sortedSupportedPicResolutions.get(0);
        }

        // 没有找到合适的，就返回默认的
        return defaultPictureResolution;
    }




    /***
     * 获取预览大小
     * @param list  设备支持的预览大小列表
     * @param th     长宽比
     * @param minWidth   最小宽度
     * @return
     */
//    public  Size getPropPreviewSize(List<Camera.Size> list, float th, int minWidth){
//        Collections.sort(list, sizeComparator);
//
//        int i = 0;
//        for(Size s:list){
//            if((s.width >= minWidth) && equalRate(s, th)){
//                Log.i(TAG, "PreviewSize:w = " + s.width + "h = " + s.height);
//                break;
//            }
//            i++;
//        }
//        if(i == list.size()){
//            i = 0;//如果没找到，就选最小的size
//        }
//        return list.get(i);
//    }

    public Size getPropPreviewSize(List<Camera.Size> list, com.example.excameralibrary.models.Size   maxSize){

         int bestWidth = 0;
         int bestHeight = 0;
         int bestindex  = 0;

        if(list.size() == 1){
            //只有一个支持尺寸 ，则返回该尺寸
            return list.get(0);
        }

        //排序
        Collections.sort(list, sizeComparator);

        for(int i =0;i<list.size();i++){
            Size size = list.get(i);
            Log.i(TAG, "getPropPreviewSize: width:"+size.width+"height:"+size.height);
            if(size.width > bestWidth
                    &&size.width <= maxSize.width
                    &&size.height > bestHeight
                    &&size.height <= maxSize.height){
                //最佳尺寸 且小于最大屏幕宽度
                bestWidth = size.width;
                bestHeight = size.height;
                bestindex = i;
            }
        }

      return list.get(bestindex);


    }






    public Size getPropPictureSize(List<Camera.Size> list, float th, int minWidth){
        Collections.sort(list, sizeComparator);

        int i = 0;
        for(Size s:list){
            if((s.width >= minWidth) && equalRate(s, th)){
                Log.i(TAG, "PictureSize : w = " + s.width + "h = " + s.height);
                break;
            }
            i++;
        }
        if(i == list.size()){
            i = 0;//如果没找到，就选最小的size
        }
        return list.get(i);
    }

    public boolean equalRate(Size s, float rate){
        float r = (float)(s.width)/(float)(s.height);
        if(Math.abs(r - rate) <= 0.03)
        {
            return true;
        }
        else{
            return false;
        }
    }

    public  class CameraSizeComparator implements Comparator<Camera.Size>{
        public int compare(Size lhs, Size rhs) {
            // TODO Auto-generated method stub
            if(lhs.width == rhs.width){
                return 0;
            }
            else if(lhs.width > rhs.width){
                return 1;
            }
            else{
                return -1;
            }
        }

    }


    public   boolean isSupportedListHasMode( List<String> findeModes,String mode){
        Iterator<String> iterator = findeModes.iterator();
        while (iterator.hasNext()){
            if(iterator.next().equals(mode) ){
                return true;
            }
        }
        return false;
    }


    /**打印支持的previewSizes
     * @param params
     */
    public  void printSupportPreviewSize(Camera.Parameters params){
        List<Size> previewSizes = params.getSupportedPreviewSizes();
        for(int i=0; i< previewSizes.size(); i++){
            Size size = previewSizes.get(i);
            Log.i(TAG, "previewSizes:width = "+size.width+" height = "+size.height);
        }

    }

    /**打印支持的pictureSizes
     * @param params
     */
    public  void printSupportPictureSize(Camera.Parameters params){
        List<Size> pictureSizes = params.getSupportedPictureSizes();
        for(int i=0; i< pictureSizes.size(); i++){
            Size size = pictureSizes.get(i);
            Log.i(TAG, "pictureSizes:width = "+ size.width
                    +" height = " + size.height);
        }
    }
    /**打印支持的聚焦模式
     * @param params
     */
    public void printSupportFocusMode(Camera.Parameters params){
        List<String> focusModes = params.getSupportedFocusModes();
        for(String mode : focusModes){
            Log.i(TAG, "focusModes--" + mode);
        }
    }
}