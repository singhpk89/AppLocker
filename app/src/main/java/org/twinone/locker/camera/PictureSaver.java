package org.twinone.locker.camera;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.Log;

class PictureSaver implements PictureCallback {

	private static final String TAG = "PictureSaver";
	private static final String PICTURES_DIR = "pictures";
	private final Context mContext;

	public PictureSaver(Context context) {
		mContext = context;
	}

	@SuppressLint("SimpleDateFormat")
	@Override
	public void onPictureTaken(byte[] data, Camera camera) {

		Log.d(TAG, "picturesaver!");
		if (data == null) {
			return;
		}
		File dir = mContext.getDir(PICTURES_DIR, Context.MODE_PRIVATE);

		SimpleDateFormat format = new SimpleDateFormat("yyyymmdd-hhmmss");
		String date = format.format(new Date());
		String file = date + ".jpg";

		String filename = dir.getPath() + File.separator + file;

		File pictureFile = new File(filename);

		try {
			FileOutputStream fos = new FileOutputStream(pictureFile);
			fos.write(data);
			fos.close();
		} catch (Exception error) {
			Log.w(TAG, "Could not save image");
		}
		if (camera != null) {
			camera.release();
			camera = null;
		}
	}

}