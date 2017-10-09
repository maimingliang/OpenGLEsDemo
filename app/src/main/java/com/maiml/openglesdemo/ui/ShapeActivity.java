package com.maiml.openglesdemo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.maiml.openglesdemo.R;

public class ShapeActivity extends AppCompatActivity {

    private static final String TAG = "ShapeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shape);

        findViewById(R.id.trangle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ShapeActivity.this, TrangleActivity.class));
            }
        });
        findViewById(R.id.square).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ShapeActivity.this, SquareActivity.class));
            }
        });
        findViewById(R.id.circle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ShapeActivity.this, CircleActivity.class));
            }
        });
        findViewById(R.id.cube).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ShapeActivity.this, CubeActivity.class));
            }
        });

    }
}
