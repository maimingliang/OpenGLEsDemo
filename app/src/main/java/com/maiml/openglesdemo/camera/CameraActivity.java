package com.maiml.openglesdemo.camera;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.maiml.openglesdemo.R;

public class CameraActivity extends AppCompatActivity {


    private CameraView mCameraView;
    private Button mBtnSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        initViews();
    }


    private void initViews() {
        mCameraView = (CameraView) findViewById(R.id.surface_view);
        mBtnSwitch = (Button) findViewById(R.id.btn_swicth);

        mBtnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraView.switchCamera();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mCameraView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mCameraView.onPause();
    }
}
