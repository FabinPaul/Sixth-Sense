package org.mainproject.sixthsense;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class SixthSenseActivity extends Activity implements Communicator {

	private ColorBlobDetection Detector;
	private List<android.hardware.Camera.Size> mResolutionList;
	private MenuItem[] mResolutionMenuItems;
	private SubMenu mResolutionMenu;
	private String TAG = "SixthSenseActivity";
	private View mView;
	private LayoutInflater inflator;
	private RelativeLayout Layout;
	private FragmentManager manager;
	private SharedPreferences mPreference;
	private MarkerButtons buttonFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sixth_sense_view);
		mPreference = getPreferences(MODE_PRIVATE);
		manager = getFragmentManager();
		buttonFragment = (MarkerButtons) manager.findFragmentById(R.id.markers);
	}

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

		@Override
		public void onManagerConnected(int status) {
			Detector = (ColorBlobDetection) getFragmentManager()
					.findFragmentById(R.id.colorblobdetection);
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Detector.initialize();
				Detector.toggleVisibilityGone();
				inflator = getLayoutInflater();
				mView = inflator.inflate(R.layout.sixth_sense_logo, null);
				Layout = (RelativeLayout) findViewById(R.id.layout);
				Layout.addView(mView, Detector.getLayoutParams());
				mView.setVisibility(View.VISIBLE);
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	@Override
	public void onPause() {
		super.onPause();
		Detector.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
		Detector.disableView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		mResolutionMenu = menu.addSubMenu("Resolution");
		mResolutionList = Detector.getResolutionList();
		mResolutionMenuItems = new MenuItem[mResolutionList.size()];
		Log.i(TAG, "Menu created");

		ListIterator<android.hardware.Camera.Size> resolutionItr = mResolutionList
				.listIterator();
		int idx = 0;
		while (resolutionItr.hasNext()) {
			android.hardware.Camera.Size element = resolutionItr.next();
			mResolutionMenuItems[idx] = mResolutionMenu.add(2, idx, Menu.NONE,
					Integer.valueOf(element.width).toString() + "x"
							+ Integer.valueOf(element.height).toString());
			idx++;
		}

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
		int id = item.getItemId();
		android.hardware.Camera.Size resolution = mResolutionList.get(id);
		Detector.setResolution(resolution);
		resolution = Detector.getResolution();
		String caption = Integer.valueOf(resolution.width).toString() + "x"
				+ Integer.valueOf(resolution.height).toString();
		Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();

		return true;
	}

	@Override
	public void respond(int id) {

		switch (id) {
		case R.id.newMarker:
			mView.setVisibility(View.GONE);
			Detector.toggleVisibilityOn();
			break;
		case R.id.saveMarker:
			if (Detector.isColorMarkerSet()) {
				if (save())
					Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.clearMarker:
			Detector.clearMarkers();
			break;
		case R.id.loadMarker:
			if (mView.getVisibility() == View.VISIBLE) {
				mView.setVisibility(View.GONE);
				Detector.toggleVisibilityOn();
				buttonFragment.setClearVisible();
				saveButtonVisibility();
			}
			load();
			Toast.makeText(this, "Loaded", Toast.LENGTH_SHORT).show();
			break;
		}

	}

	private boolean save() {
		Editor edit = mPreference.edit();
		for (int i = 0; i < 4; i++) {
			edit.putFloat("red" + i, Detector.getRGBA("red", i));
			edit.putFloat("green" + i, Detector.getRGBA("green", i));
			edit.putFloat("blue" + i, Detector.getRGBA("blue", i));
			edit.putFloat("alpha" + i, Detector.getRGBA("alpha", i));
			edit.putFloat("hue" + i, Detector.getHSV("hue", i));
			edit.putFloat("saturation" + i, Detector.getHSV("saturation", i));
			edit.putFloat("value" + i, Detector.getHSV("value", i));
			edit.putFloat("temp" + i, Detector.getHSV("temp", i));
		}
		return edit.commit();
	}

	private void load() {

		Map<String, ?> keys = mPreference.getAll();

		for (Map.Entry<String, ?> entry : keys.entrySet()) {
			Log.d("map values", entry.getKey() + ": "
					+ entry.getValue().toString());
			if(entry.getKey()==null){
				Toast.makeText(this, "Not Saved", Toast.LENGTH_LONG).show();
				return;
			}
		}
		for (int i = 0; i < 4; i++) {
			Detector.setRGBA("red", i, mPreference.getFloat("red" + i, 0));
			Detector.setRGBA("green", i, mPreference.getFloat("green" + i, 0));
			Detector.setRGBA("blue", i, mPreference.getFloat("blue" + i, 0));
			Detector.setRGBA("alpha", i, mPreference.getFloat("alpha" + i, 0));
			Detector.setHSV("hue", i, mPreference.getFloat("hue" + i, 0));
			Detector.setHSV("saturation", i,
					mPreference.getFloat("saturation" + i, 0));
			Detector.setHSV("value", i, mPreference.getFloat("value" + i, 0));
			Detector.setHSV("temp", i, mPreference.getFloat("temp" + i, 0));

			Detector.setColorSelected(i);
			Detector.setHSV(i);
		}
	}

	@Override
	public void saveButtonVisibility() {
		if (buttonFragment.isSaveVisible() == false)
			buttonFragment.setSaveVisible();
	}

}
