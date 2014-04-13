package org.mainproject.sixthsense;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class Gesture extends Fragment implements View.OnClickListener{
	
	Button Gesture;
	public static final int RESULT_GALLERY = 0;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		Gesture=(Button) getActivity().findViewById(R.id.gesture);
		Gesture.setOnClickListener(this);
		
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		

	}	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.gesture_buttons,container, true);
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onClick(View v) {
		
		if(v==Gesture)
		{
			
			y();
		}
		
	}
	
	public void x(){
		Intent galleryIntent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
startActivityForResult(galleryIntent , RESULT_GALLERY );
	}
	
	public void y()
	{
	new Handler().postDelayed(new Runnable() {

		@Override
		public void run() {
			x();
		}
	}, 5000);// 5 seconds
}

}
