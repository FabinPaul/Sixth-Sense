/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package in.fabinpaul.sixthsense;

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
    private String TAG = MarkerButtons.class.getSimpleName();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        newMarker = (Button) getActivity().findViewById(R.id.newMarker);
        newMarker.setOnClickListener(this);
        saveMarker = (Button) getActivity().findViewById(R.id.saveMarker);
        saveMarker.setOnClickListener(this);
        loadMarker = (Button) getActivity().findViewById(R.id.loadMarker);
        loadMarker.setOnClickListener(this);
        clearMarker = (Button) getActivity().findViewById(R.id.clearMarker);
        clearMarker.setOnClickListener(this);
        comm = (Communicator) getActivity();

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
        return inflater.inflate(R.layout.marker_button_view, container, true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        Log.i(TAG, "Inside onClick");
        switch (v.getId()) {
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

    public void setSaveVisible() {
        saveMarker.setVisibility(View.VISIBLE);
    }

    public boolean isSaveVisible() {
        if (saveMarker.getVisibility() == View.VISIBLE)
            return true;
        else
            return false;
    }

    public void setClearVisible() {
        clearMarker.setVisibility(View.VISIBLE);
    }

}
