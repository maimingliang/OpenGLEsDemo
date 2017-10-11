package com.maiml.openglesdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.maiml.openglesdemo.camera.CameraActivity;
import com.maiml.openglesdemo.ui.FBOActivity;
import com.maiml.openglesdemo.ui.FilterImageActivity;
import com.maiml.openglesdemo.ui.ImageActivity;
import com.maiml.openglesdemo.ui.ShapeActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        findViewById(R.id.shape).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ShapeActivity.class));
            }
        });
        findViewById(R.id.pic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ImageActivity.class));
            }
        });
        findViewById(R.id.filter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, FilterImageActivity.class));
            }
        });
        findViewById(R.id.camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CameraActivity.class));
            }
        });
        findViewById(R.id.fbo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, FBOActivity.class));
            }
        });
    }
}
