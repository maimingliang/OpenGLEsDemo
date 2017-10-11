package com.maiml.openglesdemo.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.maiml.openglesdemo.R;
import com.maiml.openglesdemo.renderer.FBORenderer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class FBOActivity extends AppCompatActivity implements FBORenderer.Callback {

    private GLSurfaceView mGlSurfaceView;
    private ImageView mImageView;
    private Button mSelectPic;
    private String mImgPath;
    private int mBmpHeight;
    private int mBmpWidth;
    private FBORenderer mFboRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fbo);

        initViews();
    }

    private void initViews() {
        mGlSurfaceView = (GLSurfaceView) findViewById(R.id.surface_view);
        mImageView = (ImageView) findViewById(R.id.mImage);
        mSelectPic = (Button) findViewById(R.id.btn_select_pic);

        mSelectPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPic();
            }
        });
        mGlSurfaceView.setEGLContextClientVersion(2);

        mFboRenderer = new FBORenderer(this);
        mFboRenderer.setCallback(this);
        mGlSurfaceView.setRenderer(mFboRenderer);
        mGlSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private void selectPic() {
        //调用相册
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            mImgPath = c.getString(columnIndex);
            Log.e("wuwang","img->"+mImgPath);
            Bitmap bmp= BitmapFactory.decodeFile(mImgPath);
            mBmpWidth=bmp.getWidth();
            mBmpHeight=bmp.getHeight();
            mFboRenderer.setBitmap(bmp);
            mGlSurfaceView.requestRender();
            c.close();
        }
    }

    @Override
    public void onCall(final ByteBuffer data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e("wuwang","callback success" + data!=null?"is not null":"null");
                Bitmap bitmap=Bitmap.createBitmap(mBmpWidth,mBmpHeight, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(data);
                saveBitmap(bitmap);
                data.clear();
            }
        }).start();
    }

    //图片保存
    public void saveBitmap(final Bitmap b){

        Log.e("tag","saveBitmap");
        String path = mImgPath.substring(0,mImgPath.lastIndexOf("/")+1);
        File folder=new File(path);
        if(!folder.exists()&&!folder.mkdirs()){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(FBOActivity.this, "无法保存照片", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        long dataTake = System.currentTimeMillis();
        final String jpegName=path+ dataTake +".jpg";
        try {
            FileOutputStream fout = new FileOutputStream(jpegName);
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(FBOActivity.this, "保存成功->"+jpegName, Toast.LENGTH_SHORT).show();
                mImageView.setImageBitmap(b);
            }
        });

    }
}
