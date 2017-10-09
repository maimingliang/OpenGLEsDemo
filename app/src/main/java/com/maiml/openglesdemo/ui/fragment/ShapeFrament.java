package com.maiml.openglesdemo.ui.fragment;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.maiml.openglesdemo.R;
import com.maiml.openglesdemo.renderer.TrangleRenderer;


public class ShapeFrament extends Fragment {

    private GLSurfaceView mGlSurfaceView;

    public ShapeFrament() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shape_frament, container, false);

        initViews(view);

        return view;
    }

    private void initViews(View view) {
        mGlSurfaceView = (GLSurfaceView) view.findViewById(R.id.surface_view);
        mGlSurfaceView.setEGLContextClientVersion(2);
        mGlSurfaceView.setRenderer(new TrangleRenderer(getActivity()));
        mGlSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public void onResume() {
        super.onResume();
        mGlSurfaceView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mGlSurfaceView.onPause();
    }
}
