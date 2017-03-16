package in.fabinpaul.sixthsense;

import android.content.Context;
import android.hardware.Camera.Size;
import android.util.AttributeSet;

import org.opencv.android.JavaCameraView;

import java.util.List;

public class CameraView extends JavaCameraView {


    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //mCamera.setDisplayOrientation(90);
    }


    public List<Size> getResolutionList() {
        if (mCamera == null)
            return null;
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
