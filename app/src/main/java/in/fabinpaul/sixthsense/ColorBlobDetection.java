package in.fabinpaul.sixthsense;

import android.app.Activity;
import android.app.Fragment;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.List;

public class ColorBlobDetection extends Fragment implements OnTouchListener,
		CvCameraViewListener2 {

	private static final String TAG = "SixthSense: ColorBlobdetector";

	private boolean[] mIsColorSelected = { false, false, false, false };
	private Mat mRgba;
	private Scalar[] mBlobColorRgba = new Scalar[4];
	private Scalar[] mBlobColorHsv = new Scalar[4];
	private ColorBlobDetector[] mDetector = new ColorBlobDetector[4];
	private int count = -1;
	private Scalar CONTOUR_COLOR;
	private boolean colorMarkerSet = false;
	private Communicator comm;
	private CameraView mOpenCvCameraView;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.i(TAG, "onActivityCreated");
		mOpenCvCameraView = (CameraView) getActivity().findViewById(
				R.id.color_blob_detection_activity_surface_view);
		mOpenCvCameraView.setCvCameraViewListener(this);
		comm = (Communicator) getActivity();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.color_blob_detection_surface_view,
				container, true);
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		mRgba = new Mat(height, width, CvType.CV_8UC4);
		for (int i = 0; i < 4; i++) {
			mDetector[i] = new ColorBlobDetector();
			mBlobColorRgba[i] = new Scalar(255);
			mBlobColorHsv[i] = new Scalar(255);
		}
		CONTOUR_COLOR = new Scalar(255, 0, 0, 255);

	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		mRgba = inputFrame.rgba();
		Mat[] colorLabel = new Mat[4];
		org.opencv.core.Point markersXY[]=new org.opencv.core.Point[4];
		for (int i = 0; i < 4; i++) {

			colorLabel[i] = new Mat();

			if (mIsColorSelected[i]) {
				mDetector[i].process(mRgba);
				List<MatOfPoint> contours = mDetector[i].getContours();
				Log.e(TAG, "Contours count: " + contours.size());
				Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);
				markersXY[i]=new org.opencv.core.Point();
				markersXY[i]=mDetector[i].getXY();
				Log.i(TAG, "Point:X"+markersXY[i].x+" Y:"+markersXY[i].y);
				switch (i) {
				case 0:
					colorLabel[i] = mRgba.submat(4, 68, 4, 68);
					break;
				case 1:
					colorLabel[i] = mRgba.submat(4, 68, mRgba.cols() - 68,
							mRgba.cols() - 4);
					break;
				case 2:
					colorLabel[i] = mRgba.submat(mRgba.rows() - 68,
							mRgba.rows() - 4, 4, 68);
					break;
				case 3:
					colorLabel[i] = mRgba.submat(mRgba.rows() - 68,
							mRgba.rows() - 4, mRgba.cols() - 68,
							mRgba.cols() - 4);
					break;
				}
				colorLabel[i].setTo(mBlobColorRgba[i]);
			}
		}

		return mRgba;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			count++;
			if (count > 3)
				count = 0;

			if (count == 3) {
				colorMarkerSet = true;
				comm.saveButtonVisibility();
			}

			int cols = mRgba.cols();
			int rows = mRgba.rows();

			int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
			int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;
			Log.i(TAG,
					"x coordinates" + event.getX() + "y coordinates"
							+ event.getY());
			Log.i(TAG, "View width" + mOpenCvCameraView.getWidth()
					+ "View Height" + mOpenCvCameraView.getHeight());

			int x = (int) event.getX() - xOffset;
			int y = (int) event.getY() - yOffset;

			if ((x < 0) || (y < 0) || (x > cols) || (y > rows))
				return false;

			Rect touchedRect = new Rect();

			touchedRect.x = (x > 4) ? x - 4 : 0;
			touchedRect.y = (y > 4) ? y - 4 : 0;

			touchedRect.width = (x + 4 < cols) ? x - 1 - touchedRect.x : cols
					- touchedRect.x;
			touchedRect.height = (y + 4 < rows) ? y - 1 - touchedRect.y : rows
					- touchedRect.y;

			Log.i(TAG, "Width" + touchedRect.width + " Height"
					+ touchedRect.height);
			Log.i(TAG, "Column" + cols + " Rows" + rows);
			Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

			Mat touchedRegionRgba = mRgba.submat(touchedRect);

			Mat touchedRegionHsv = new Mat();
			Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv,
					Imgproc.COLOR_RGB2HSV_FULL);

			// Calculate average color of touched region
			mBlobColorHsv[count] = Core.sumElems(touchedRegionHsv);
			int pointCount = touchedRect.width * touchedRect.height;
			for (int i = 0; i < mBlobColorHsv[count].val.length; i++)
				mBlobColorHsv[count].val[i] /= pointCount;

			mBlobColorRgba[count] = converScalarHsv2Rgba(mBlobColorHsv[count]);

			Log.i(TAG, "Before" + mBlobColorHsv[count].val[0] + " "
					+ mBlobColorHsv[count].val[1] + " "
					+ mBlobColorHsv[count].val[2]);
			Log.i(TAG, "After" + mBlobColorRgba[count].val[0] + " "
					+ mBlobColorRgba[count].val[1] + " "
					+ mBlobColorRgba[count].val[2]);

			Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba[count].val[0]
					+ ", " + mBlobColorRgba[count].val[1] + ", "
					+ mBlobColorRgba[count].val[2] + ", "
					+ mBlobColorRgba[count].val[3] + ")");

			// mDetector[count].setHsvColor(mBlobColorHsv[count]);
			setHSV(count);

			mIsColorSelected[count] = true;

			touchedRegionRgba.release();
			touchedRegionHsv.release();
		}
		return true; // don't need subsequent touch events
	}

	public float getHSV(String channel, int id) {
		if (channel.equalsIgnoreCase("hue"))
			return (float) mBlobColorHsv[id].val[0];
		else if (channel.equalsIgnoreCase("saturation"))
			return (float) mBlobColorHsv[id].val[1];
		else if (channel.equalsIgnoreCase("value"))
			return (float) mBlobColorHsv[id].val[2];
		else
			return (float) mBlobColorHsv[id].val[3];
	}

	public void setHSV(String channel, int id, float value) {

		if (channel.equalsIgnoreCase("hue")) {
			mBlobColorHsv[id].val[0] = value;
			Log.i(TAG, "Hue"+id+"," + value);
		}

		else if (channel.equalsIgnoreCase("saturation")) {
			mBlobColorHsv[id].val[1] = value;
			Log.i(TAG, "Saturataion" + value);
		}

		else if (channel.equalsIgnoreCase("value")) {
			mBlobColorHsv[id].val[2] = value;
			Log.i(TAG, "Value" + value);
		}

		else if (channel.equalsIgnoreCase("temp")) {
			mBlobColorHsv[id].val[3] = value;
			Log.i(TAG, "Temp" + value);
		}
	}

	public void setHSV(int id) {
		mDetector[id].setHsvColor(mBlobColorHsv[id]);
	}

	public void clearMarkers() {
		for (int i = 0; i < 4; i++) {
			mIsColorSelected[i] = false;
		}
	}

	public float getRGBA(String color, int id) {

		if (color.equalsIgnoreCase("red"))
			return (float) mBlobColorRgba[id].val[0];
		else if (color.equalsIgnoreCase("green"))
			return (float) mBlobColorRgba[id].val[1];
		else if (color.equalsIgnoreCase("blue"))
			return (float) mBlobColorRgba[id].val[2];
		else if (color.equalsIgnoreCase("alpha"))
			return (float) mBlobColorRgba[id].val[3];
		else
			return 0;
	}

	public void setRGBA(String color, int id, float value) {

		if (color.equalsIgnoreCase("red")) 
			mBlobColorRgba[id].val[0] = value;

		else if (color.equalsIgnoreCase("green"))
			mBlobColorRgba[id].val[1] = value;

		else if (color.equalsIgnoreCase("blue"))
			mBlobColorRgba[id].val[2] = value;

		else if (color.equalsIgnoreCase("alpha"))
			mBlobColorRgba[id].val[3] = value;

	}

	public void setColorSelected(int id) {
		mIsColorSelected[id] = true;
	}

	private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
		Mat pointMatRgba = new Mat();
		Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
		Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL,
				4);

		return new Scalar(pointMatRgba.get(0, 0));
	}

	@Override
	public void onCameraViewStopped() {
		mRgba.release();

	}

	public void initialize() {
		Log.i(TAG, "OpenCV loaded successfully");
		Log.i(TAG, "" + mOpenCvCameraView);
		mOpenCvCameraView.enableView();
		mOpenCvCameraView.setOnTouchListener(ColorBlobDetection.this);

	}

	public void toggleVisibilityOn() {
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
	}

	public void toggleVisibilityGone() {
		mOpenCvCameraView.setVisibility(SurfaceView.GONE);
	}

	public void disableView() {
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	public List<Size> getResolutionList() {
		return mOpenCvCameraView.getResolutionList();
	}

	public void setResolution(Size resolution) {
		mOpenCvCameraView.setResolution(resolution);
	}

	public Size getResolution() {
		return mOpenCvCameraView.getResolution();
	}

	public LayoutParams getLayoutParams() {
		return mOpenCvCameraView.getLayoutParams();
	}

	public boolean isColorMarkerSet() {
		return colorMarkerSet;
	}

}
