package org.mainproject.sixthsense;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class MarkerButtons extends Fragment implements View.OnClickListener {
	
	private Button newMarker;
	private Button saveMarker;
	private Button loadMarker;
	private Button clearMarker;
	private Communicator comm;
	private String TAG="MarkerButtons";

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		newMarker=(Button) getActivity().findViewById(R.id.newMarker);
		newMarker.setOnClickListener(this);
		saveMarker=(Button) getActivity().findViewById(R.id.saveMarker);
		saveMarker.setOnClickListener(this);
		loadMarker=(Button) getActivity().findViewById(R.id.loadMarker);
		loadMarker.setOnClickListener(this);
		clearMarker=(Button) getActivity().findViewById(R.id.clearMarker);
		clearMarker.setOnClickListener(this);
		comm=(Communicator) getActivity();
		
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.marker_button_view ,container , true);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		Log.i(TAG, "Inside onClick");
		switch(v.getId()){
		case R.id.newMarker:
			comm.respond(R.id.newMarker);
			setClearVisible();
			loadMarker.setVisibility(View.VISIBLE);
			break;
		case R.id.saveMarker:
			comm.respond(R.id.saveMarker);
			break;
		case R.id.clearMarker:
			comm.respond(R.id.clearMarker);
			break;
		case R.id.loadMarker:
			comm.respond(R.id.loadMarker);
		}
		
	}
	
	public void setSaveVisible(){
		saveMarker.setVisibility(View.VISIBLE);
	}
	
	public boolean isSaveVisible(){
		if(saveMarker.getVisibility()==View.VISIBLE)
			return true;
		else
			return false;
	}
	
	public void setClearVisible(){
		clearMarker.setVisibility(View.VISIBLE);
	}

}
