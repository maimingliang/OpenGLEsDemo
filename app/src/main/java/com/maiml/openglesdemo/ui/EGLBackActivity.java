package com.maiml.openglesdemo.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.maiml.openglesdemo.R;
import com.maiml.openglesdemo.egl.GLES20BackEnv;
import com.maiml.openglesdemo.egl.GrayFilter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.maiml.openglesdemo.R.id.mImage;

public class EGLBackActivity extends AppCompatActivity {

    private ImageView mImageView;
    private String mImgPath;
    private int mBmpWidth;
    private int mBmpHeight;
    private GLES20BackEnv mBackEnv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eglback);

        initViews();
    }

    private void initViews() {
        mImageView = (ImageView) findViewById(mImage);
        findViewById(R.id.btn_select_pic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPic();
            }
        });
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
            mBackEnv=new GLES20BackEnv(mBmpWidth,mBmpHeight);
            mBackEnv.setThreadOwner(getMainLooper().getThread().getName());
            mBackEnv.setGrayFilter(new GrayFilter(this));
            mBackEnv.setBitmap(bmp);
            saveBitmap(mBackEnv.getBitmap());
            c.close();
        }
    }

    //图片保存
    public void saveBitmap(final Bitmap b){
        String path = mImgPath.substring(0,mImgPath.lastIndexOf("/")+1);
        File folder=new File(path);
        if(!folder.exists()&&!folder.mkdirs()){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(EGLBackActivity.this, "无法保存照片", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(EGLBackActivity.this, "保存成功->"+jpegName, Toast.LENGTH_SHORT).show();
                mImageView.setImageBitmap(b);
            }
        });

    }

}
