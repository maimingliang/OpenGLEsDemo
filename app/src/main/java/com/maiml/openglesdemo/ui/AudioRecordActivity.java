package com.maiml.openglesdemo.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.maiml.openglesdemo.R;
import com.maiml.openglesdemo.codec.AudioDecoder;
import com.maiml.openglesdemo.codec.AudioEncoder;
import com.maiml.openglesdemo.utils.PCMPlayer;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class AudioRecordActivity extends AppCompatActivity {

    private Button btnRecord;
    private Button btnStop;
    private Button btnPlay;
    private PCMPlayer pcmPlayer;

    private boolean isEncode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_record);

        initViews();
    }

    private void initViews() {
        btnRecord = (Button) findViewById(R.id.btn_record);
        btnStop = (Button) findViewById(R.id.btn_stop);
        btnPlay = (Button) findViewById(R.id.btn_play);

        final AudioEncoder audioEncoder = new AudioEncoder();

        final AudioDecoder audioDecoder = new AudioDecoder("/sdcard/mm.aac");

        audioEncoder.setmSavePath("/sdcard/mm.aac");

        try {
            audioEncoder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("tag","----record");
                isEncode = true;
                try {
                    audioEncoder.start();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });


        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isEncode){
                    audioEncoder.stop();
                }else{
                    audioDecoder.stop();
                }

            }
        });


        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEncode = false;
                audioDecoder.start();
            }
        });



    }

    private void play() {


        pcmPlayer = new PCMPlayer();

        byte[] bytes = readFile2Bytes(new File("/sdcard/mm.pcm"));

        pcmPlayer.write(bytes);





    }

    /**
     * 指定编码按行读取文件到字符数组中
     *
     * @param file 文件
     * @return StringBuilder对象
     */
    public static byte[] readFile2Bytes(File file) {
        if (file == null) return null;
        try {
            return inputStream2Bytes(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * inputStream转byteArr
     *
     * @param is 输入流
     * @return 字节数组
     */
    public static byte[] inputStream2Bytes(InputStream is) {
        return input2OutputStream(is).toByteArray();
    }

    /**
     * inputStream转outputStream
     *
     * @param is 输入流
     * @return outputStream子类
     */
    public static ByteArrayOutputStream input2OutputStream(InputStream is) {
        if (is == null) return null;
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int len;
            while ((len = is.read(b, 0, 1024)) != -1) {
                os.write(b, 0, len);
            }
            return os;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            closeIO(is);
        }
    }


    /**
     * 关闭IO
     *
     * @param closeables closeable
     */
    public static void closeIO(Closeable... closeables) {
        if (closeables == null) return;
        try {
            for (Closeable closeable : closeables) {
                if (closeable != null) {
                    closeable.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
