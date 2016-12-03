package com.example.excameralibrary.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by baochaoh on 2016/12/2.
 * 图片处理工具类
 */

public class ImageUtil {


    private  static   ImageUtil  imageUtil;

    public static ImageUtil getImageUtil() {
        if(imageUtil == null){
            imageUtil = new ImageUtil();
        }
        return imageUtil;
    }


    /**
     * 获取指定路径图片文件的方向
     * @param filepath
     * @return
     */
    public  int getExifOrientation(String filepath) {
        int degree = 0;
        //图片元数据
        ExifInterface exif = null;

        try {
            exif = new ExifInterface(filepath);
        } catch (IOException ex) {
            // MmsLog.e(ISMS_TAG, "getExifOrientation():", ex);
        }

        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                // We only recognize a subset of orientation tag values.
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                    default:
                        break;
                }
            }
        }

        return degree;
    }




    /***
     * 根据图片大小与显示大小 按比率获取图片
     * 用于显示缩略图
     * @param displayWidth
     * @param displayHeight
     * @param file   图片路径
     * @return
     */
    public Bitmap getBitmapfromFileOption(int displayWidth, int displayHeight, File file){
        Bitmap resultBitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        //设置读取图片的参数 为只读取图片大小 而不解码图片
        options.inJustDecodeBounds = true;
        //解码文件   options 保存着图片文件的相关信息
        resultBitmap =  BitmapFactory.decodeFile(file.getPath(),options);
        //计算图片的大小与显示的大小的比率
        int heightRatio = (int)Math.ceil(options.outHeight/(float)displayHeight);
        int widthRatio = (int)Math.ceil(options.outWidth/(float)displayWidth);
        //设置采样率
        if(heightRatio > 1 && widthRatio > 1){
            if(heightRatio > widthRatio){
                options.inSampleSize = heightRatio;
            }else {
                options.inSampleSize = widthRatio;
            }
        }
        //取消只读取图片大小的属性
        options.inJustDecodeBounds = false;
        //读取图片
        resultBitmap  = BitmapFactory.decodeFile(file.getPath(),options);
        //判断图片的方向
        int degree = getExifOrientation(file.getPath());   //获取图片的方向
        if(degree == 90 || degree == 180 || degree == 270){  //旋转图片
            //Roate preview icon according to exif orientation
            Matrix matrix = new Matrix();
            matrix.postRotate(degree);
            //旋转指定的角度
            return Bitmap.createBitmap(resultBitmap, 0, 0, resultBitmap.getWidth(), resultBitmap.getHeight(), matrix, true);
        }
        //do not need roate the icon,default
        return resultBitmap;
    }


    /***
     * 获取指定图片文件的二进制数组
     * @param imageFile  图片文件
     * @param width   分辨率
     * @param height  分辨率
     * @param compression   压缩率   ，100 代表不压缩
     * @return
     */
    public    byte[] getImageToByteArray(File  imageFile,int  width,int height, int compression ){

        try {
//               //获取 1280 *720大小的图片
//               Bitmap tempBitmap  = this.getBitmapfromFileOption(1280,720,this.imageFile);

            //获取原图 1/2大小的Bitmap
//               BitmapFactory.Options  options1 = new BitmapFactory.Options();
//               options1.inSampleSize = 2;
//               Bitmap tempBitmap  =   BitmapFactory.decodeFile(this.imageFile.getPath(),options1);

//               压缩位图  比较清晰
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            //获取指定分辨率的图片 加载到内存中
            Bitmap  tempBitmap  =   this.ratio(imageFile.getPath(),width,height);
            //判断图片的方向
            int degree = getExifOrientation(imageFile.getPath());   //获取图片的方向
            if(degree == 90 || degree == 180 || degree == 270){  //旋转图片
                //Roate preview icon according to exif orientation
                Matrix matrix = new Matrix();
                matrix.postRotate(degree);
                //旋转指定的角度
                tempBitmap =  Bitmap.createBitmap(tempBitmap, 0, 0, tempBitmap.getWidth(), tempBitmap.getHeight(), matrix, true);
            }
            //压缩图片
            tempBitmap.compress(Bitmap.CompressFormat.JPEG,compression,out);
            //输出byte数据
            byte[] data =  out.toByteArray();
            out.close();
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }



    /***
     * 保存图片数据到指定文件路径上
     * @param bitmap
     * @param filePath
     */
    public    void saveImageToFile(Bitmap bitmap,String filePath){
        //保存图片至本地
        File file = new File(filePath);
        FileOutputStream outputStream;
        try {
            //保存至本地
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
        } catch (Exception e){
            System.out.print(e.toString());
        }

    }


    /***
     * 保存图片数据到指定文件路径上
     * @param imageData
     * @param filePath
     */
    public    void saveImageToFile(byte[] imageData,String filePath){
        //保存图片至本地
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        File file = new File(filePath);
        FileOutputStream outputStream;
        try {
            //保存至本地
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
        } catch (Exception e){
            System.out.print(e.toString());
        }

    }

    /**
     * 旋转Bitmap
     * @param b
     * @param rotateDegree
     * @return
     */
    public  Bitmap getRotateBitmap(Bitmap b, float rotateDegree){
        Matrix matrix = new Matrix();
        matrix.postRotate((float)rotateDegree);
        Bitmap rotaBitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, false);
        return rotaBitmap;
    }




    /**
     *根据图片路径获取图片
     *
     * @param imgPath
     * @return
     */
    public Bitmap getBitmap(String imgPath) {
        // Get bitmap through image path
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = false;
        newOpts.inPurgeable = true;
        newOpts.inInputShareable = true;
        // Do not compress
        newOpts.inSampleSize = 1;
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeFile(imgPath, newOpts);
    }

    /**
     *存储图片到指定路径上
     *
     * @param bitmap
     * @param outPath
     * @throws FileNotFoundException
     */
    public void storeImage(Bitmap bitmap, String outPath) throws FileNotFoundException {
        FileOutputStream os = new FileOutputStream(outPath);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
    }

    /**
     * Compress image by pixel, this will modify image width/height.
     * Used to get thumbnail
     *
     * @param imgPath image path
     * @param pixelW target pixel of width
     * @param pixelH target pixel of height
     * @return
     */
    public Bitmap ratio(String imgPath, float pixelW, float pixelH) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true，即只读边不读内容
        newOpts.inJustDecodeBounds = true;
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        // Get bitmap info, but notice that bitmap is null now
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath,newOpts);

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 想要缩放的目标尺寸
        float hh = pixelH;// 设置高度为240f时，可以明显看到图片缩小了
        float ww = pixelW;// 设置宽度为120f，可以明显看到图片缩小了
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0) be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        // 开始压缩图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(imgPath, newOpts);
        // 压缩好比例大小后再进行质量压缩
//        return compress(bitmap, maxSize); // 这里再进行质量压缩的意义不大，反而耗资源，删除
        return bitmap;
    }

    /**
     * Compress image by size, this will modify image width/height.
     * Used to get thumbnail
     *
     * @param image
     * @param pixelW target pixel of width
     * @param pixelH target pixel of height
     * @return
     */
    public Bitmap ratio(Bitmap image, float pixelW, float pixelH) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, os);
        if( os.toByteArray().length / 1024>1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            os.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 50, os);//这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeStream(is, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        float hh = pixelH;// 设置高度为240f时，可以明显看到图片缩小了
        float ww = pixelW;// 设置宽度为120f，可以明显看到图片缩小了
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0) be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        is = new ByteArrayInputStream(os.toByteArray());
        bitmap = BitmapFactory.decodeStream(is, null, newOpts);
        //压缩好比例大小后再进行质量压缩
//	    return compress(bitmap, maxSize); // 这里再进行质量压缩的意义不大，反而耗资源，删除
        return bitmap;
    }

    /**
     * Compress by quality,  and generate image to the path specified
     *
     * @param image
     * @param outPath
     * @param maxSize target will be compressed to be smaller than this size.(kb)
     * @throws IOException
     */
    public void compressAndGenImage(Bitmap image, String outPath, int maxSize) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        // scale
        int options = 100;
        // Store the bitmap into output stream(no compress)
        image.compress(Bitmap.CompressFormat.JPEG, options, os);
        // Compress by loop
        while ( os.toByteArray().length / 1024 > maxSize) {
            // Clean up os
            os.reset();
            // interval 10
            options -= 10;
            image.compress(Bitmap.CompressFormat.JPEG, options, os);
        }

        // Generate compressed image file
        FileOutputStream fos = new FileOutputStream(outPath);
        fos.write(os.toByteArray());
        fos.flush();
        fos.close();
    }

    /**
     * Compress by quality,  and generate image to the path specified
     *
     * @param imgPath
     * @param outPath
     * @param maxSize target will be compressed to be smaller than this size.(kb)
     * @param needsDelete Whether delete original file after compress
     * @throws IOException
     */
    public void compressAndGenImage(String imgPath, String outPath, int maxSize, boolean needsDelete) throws IOException {
        compressAndGenImage(getBitmap(imgPath), outPath, maxSize);

        // Delete original file
        if (needsDelete) {
            File file = new File (imgPath);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * Ratio and generate thumb to the path specified
     *
     * @param image
     * @param outPath
     * @param pixelW target pixel of width
     * @param pixelH target pixel of height
     * @throws FileNotFoundException
     */
    public void ratioAndGenThumb(Bitmap image, String outPath, float pixelW, float pixelH) throws FileNotFoundException {
        Bitmap bitmap = ratio(image, pixelW, pixelH);
        storeImage( bitmap, outPath);
    }

    /**
     * Ratio and generate thumb to the path specified
     *
     * @param outPath
     * @param pixelW target pixel of width
     * @param pixelH target pixel of height
     * @param needsDelete Whether delete original file after compress
     * @throws FileNotFoundException
     */
    public void ratioAndGenThumb(String imgPath, String outPath, float pixelW, float pixelH, boolean needsDelete) throws FileNotFoundException {
        Bitmap bitmap = ratio(imgPath, pixelW, pixelH);
        storeImage( bitmap, outPath);

        // Delete original file
        if (needsDelete) {
            File file = new File (imgPath);
            if (file.exists()) {
                file.delete();
            }
        }
    }


    /***
     * 获取图片大小
     * @param bitmap
     * @return
     */
    public int getBitmapSize(Bitmap bitmap){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){    //API 19
            return bitmap.getAllocationByteCount();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1){//API 12
            return bitmap.getByteCount();
        }
        return bitmap.getRowBytes() * bitmap.getHeight();                //earlier version
    }

}
