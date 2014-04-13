package org.mainproject.sixthsense;

import java.util.List;

import org.opencv.android.JavaCameraView;

import android.content.Context;
import android.hardware.Camera.Size;
import android.util.AttributeSet;

public class CameraView extends JavaCameraView {

	
	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//mCamera.setDisplayOrientation(90);
	}
	

	public List<Size> getResolutionList() {
		return mCamera.getParameters().getSupportedPreviewSizes();
	}

	public void setResolution(Size resolution) {
		disconnectCamera();
		mMaxHeight = resolution.height;
		mMaxWidth = resolution.width;
		connectCamera(mMaxWidth, mMaxHeight);
	}

	public Size getResolution() {
		return mCamera.getParameters().getPreviewSize();
	}

}
