package com.twinone.locker.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

@SuppressLint("NewApi")
public class CameraManager {

	private static final String TAG = "CameraManager";
	private final Context mContext;

	private Camera mCamera;

	public CameraManager(Context c) {
		mContext = c;
	}

	public void takePicture() {
		int id = getFrontCameraId();
		if (id == -1) {
			Log.w(TAG, "No front camera available");
			return;
		}
		try {
			Log.d(TAG, "trying id" + id);
			mCamera = Camera.open(id);
			setCameraDisplayOrientation(mContext, id, mCamera);
			mCamera.startPreview();
			mCamera.autoFocus(null);
			mCamera.takePicture(null, null, null, new PictureSaver(mContext));
		} catch (Exception e) {
			Log.w(TAG, "Failed to take picture", e);
			close();
		}
	}

	public void close() {
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}

	public boolean hasFrontCamera() {
		return getFrontCameraId() != -1;
	}

	/**
	 * Returns the id of the first front facing camera or -1 if no front facing
	 * camera exists<br>
	 * Will only work on API level 9+
	 * 
	 * @return
	 */
	@SuppressLint("NewApi")
	private int getFrontCameraId() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
			return -1;
		}
		if (!mContext.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA_FRONT)) {
			return -1;
		}
		CameraInfo ci = new CameraInfo();
		for (int id = 0; id < Camera.getNumberOfCameras(); id++) {
			Camera.getCameraInfo(id, ci);
			if (ci.facing == CameraInfo.CAMERA_FACING_FRONT)
				return id;
		}
		return -1;
	}

	public static void setCameraDisplayOrientation(Context c, int cameraId,
			android.hardware.Camera camera) {
		CameraInfo info = new CameraInfo();
		Camera.getCameraInfo(cameraId, info);
		WindowManager wm = (WindowManager) c
				.getSystemService(Context.WINDOW_SERVICE);
		int rotation = wm.getDefaultDisplay().getRotation();
		int degrees = rotation == Surface.ROTATION_0 ? 0
				: rotation == Surface.ROTATION_90 ? 90
						: rotation == Surface.ROTATION_180 ? 180 : 270;

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);
	}
}
