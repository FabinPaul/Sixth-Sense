package in.fabinpaul.sixthsense;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.util.List;
import java.util.Map;

public class SixthSenseActivity extends AppCompatActivity implements Communicator {

    private static final int REQUEST_CAMERA_PERMISSION = 1000;
    private ColorBlobDetectionFragment mColorDetector;
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

    private boolean hasPermissions = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sixth_sense_view);
        mPreference = getPreferences(MODE_PRIVATE);
        manager = getFragmentManager();
        buttonFragment = (MarkerButtons) manager.findFragmentById(R.id.markers);
        hasPermissions = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        requestCameraPermission();
    }

    private void requestCameraPermission() {
        if (!hasPermissions) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    mColorDetector = (ColorBlobDetectionFragment) getFragmentManager()
                            .findFragmentById(R.id.colorblobdetection);
                    mColorDetector.initialize();
                    mColorDetector.toggleVisibilityGone();
                    invalidateOptionsMenu();
                    inflator = getLayoutInflater();
                    mView = inflator.inflate(R.layout.sixth_sense_logo, null);
                    Layout = (RelativeLayout) findViewById(R.id.layout);
                    Layout.addView(mView, mColorDetector.getLayoutParams());
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
        if (mColorDetector != null)
            mColorDetector.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (hasPermissions)
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
                    mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mColorDetector != null)
            mColorDetector.disableView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
                            mLoaderCallback);

                } else {

                    AlertDialog ad = new AlertDialog.Builder(this).create();
                    ad.setCancelable(false); // This blocks the 'BACK' button
                    ad.setMessage("It seems that you device does not support camera (or it is locked). Application will be closed.");
                    ad.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    ad.show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void respond(int id) {

        switch (id) {
            case R.id.newMarker:
                mView.setVisibility(View.GONE);
                mColorDetector.toggleVisibilityOn();
                invalidateOptionsMenu();
                break;
            case R.id.saveMarker:
                if (mColorDetector.isColorMarkerSet()) {
                    if (save())
                        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.clearMarker:
                mColorDetector.clearMarkers();
                break;
            case R.id.loadMarker:
                if (mView.getVisibility() == View.VISIBLE) {
                    mView.setVisibility(View.GONE);
                    mColorDetector.toggleVisibilityOn();
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
            edit.putFloat("red" + i, mColorDetector.getRGBA("red", i));
            edit.putFloat("green" + i, mColorDetector.getRGBA("green", i));
            edit.putFloat("blue" + i, mColorDetector.getRGBA("blue", i));
            edit.putFloat("alpha" + i, mColorDetector.getRGBA("alpha", i));
            edit.putFloat("hue" + i, mColorDetector.getHSV("hue", i));
            edit.putFloat("saturation" + i, mColorDetector.getHSV("saturation", i));
            edit.putFloat("value" + i, mColorDetector.getHSV("value", i));
            edit.putFloat("temp" + i, mColorDetector.getHSV("temp", i));
        }
        return edit.commit();
    }

    private void load() {

        Map<String, ?> keys = mPreference.getAll();

        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            Log.d("map values", entry.getKey() + ": "
                    + entry.getValue().toString());
            if (entry.getKey() == null) {
                Toast.makeText(this, "Not Saved", Toast.LENGTH_LONG).show();
                return;
            }
        }
        for (int i = 0; i < 4; i++) {
            mColorDetector.setRGBA("red", i, mPreference.getFloat("red" + i, 0));
            mColorDetector.setRGBA("green", i, mPreference.getFloat("green" + i, 0));
            mColorDetector.setRGBA("blue", i, mPreference.getFloat("blue" + i, 0));
            mColorDetector.setRGBA("alpha", i, mPreference.getFloat("alpha" + i, 0));
            mColorDetector.setHSV("hue", i, mPreference.getFloat("hue" + i, 0));
            mColorDetector.setHSV("saturation", i,
                    mPreference.getFloat("saturation" + i, 0));
            mColorDetector.setHSV("value", i, mPreference.getFloat("value" + i, 0));
            mColorDetector.setHSV("temp", i, mPreference.getFloat("temp" + i, 0));

            mColorDetector.setColorSelected(i);
            mColorDetector.setHSV(i);
        }
    }

    @Override
    public void saveButtonVisibility() {
        if (buttonFragment.isSaveVisible() == false)
            buttonFragment.setSaveVisible();
    }

}
