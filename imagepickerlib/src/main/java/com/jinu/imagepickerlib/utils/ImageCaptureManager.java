package com.jinu.imagepickerlib.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.core.content.FileProvider;

public class ImageCaptureManager {

  private final static String CAPTURED_PHOTO_PATH_KEY = "mCurrentPhotoPath";
  public static final int REQUEST_TAKE_PHOTO = 1;

  private String mCurrentPhotoPath;
  private Context mContext;

  private boolean isNativeCamera = false;

  public ImageCaptureManager(Context mContext) {
    this.mContext = mContext;
  }

  private File createImageFile() throws IOException {
    // Create an image file name
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String imageFileName = "JPEG_" + timeStamp + "_";
    File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    if (!storageDir.exists()) {
      if (!storageDir.mkdir()) {
        throw new IOException();
      }
    }
    File image = File.createTempFile(
        imageFileName,  /* prefix */
        ".jpg",         /* suffix */
        storageDir      /* directory */
    );

    // Save a file: path for use with ACTION_VIEW intents
    mCurrentPhotoPath = image.getAbsolutePath();
    return image;
  }


  public Intent dispatchTakePictureIntent() throws IOException {
    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

    if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {

      ResolveInfo mInfo = mContext.getPackageManager().resolveActivity(takePictureIntent, 0);
      takePictureIntent.setComponent(new ComponentName(mInfo.activityInfo.packageName, mInfo.activityInfo.name));

      if(isNativeCamera){
          takePictureIntent.setAction(Intent.ACTION_MAIN);
          takePictureIntent.addCategory(Intent.CATEGORY_LAUNCHER);
      }

      File photoFile = createImageFile();
      if (photoFile != null) {
        Uri uri = FileProvider.getUriForFile(mContext, "com.yongbeam.y_photopicker.fileprovider", photoFile);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
      }
    }
    return takePictureIntent;
  }


  public void galleryAddPic() {
    String dcimPath = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DCIM).getAbsolutePath();

    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String resultImageDri = dcimPath + "/"+timeStamp+".jpg";
    copyFile(mCurrentPhotoPath , resultImageDri );

    File tempImage = new File(mCurrentPhotoPath);

    File f = new File(resultImageDri);
    Intent mediaIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
    mediaIntent.setData(Uri.fromFile(f));
    mContext.sendBroadcast(mediaIntent);
    if(tempImage.delete()){
        Log.d("Y-Photo-Picker","#### DELETE TEMP IMAGE");}
  }

  /**
   * 파일 복사
   * @param strSrc
   * @param save_file
   * @return
   */
  private boolean copyFile(String strSrc , String save_file){
    File file = new File(strSrc);

    boolean result;
    if(file!=null&&file.exists()){

      try {

        FileInputStream fis = new FileInputStream(file);
        FileOutputStream newfos = new FileOutputStream(save_file);
        int readcount=0;
        byte[] buffer = new byte[1024];

        while((readcount = fis.read(buffer,0,1024))!= -1){
          newfos.write(buffer,0,readcount);
        }
        newfos.close();
        fis.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      result = true;
    }else{
      result = false;
    }
    return result;
  }



  public String getCurrentPhotoPath() {
    return mCurrentPhotoPath;
  }


  public void onSaveInstanceState(Bundle savedInstanceState) {
    if (savedInstanceState != null && mCurrentPhotoPath != null) {
      savedInstanceState.putString(CAPTURED_PHOTO_PATH_KEY, mCurrentPhotoPath);
    }
  }

  public void onRestoreInstanceState(Bundle savedInstanceState) {
    if (savedInstanceState != null && savedInstanceState.containsKey(CAPTURED_PHOTO_PATH_KEY)) {
      mCurrentPhotoPath = savedInstanceState.getString(CAPTURED_PHOTO_PATH_KEY);
    }
  }
}
