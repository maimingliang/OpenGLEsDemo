package com.maiml.openglesdemo.codec;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 类       名:
 * 说       明:
 * version   0.1
 * date   2017/10/12
 * author   maimingliang
 */


public class AudioRecorderUtil {

    private static AudioRecorderUtil sAudioRecorder;


    // 音频源：音频输入-麦克风
    private final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
    // 采样率
    // 44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    // 采样频率一般共分为22.05KHz、44.1KHz、48KHz三个等级
    private final static int AUDIO_SAMPLE_RATE = 16000;
    // 音频通道 单声道
    private final static int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    // 音频格式：PCM编码
    private final static int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;


    // 录音状态
    private Status status = Status.STATUS_NO_READY;
    private int mBufferSize;
    private AudioRecord mAudioRecord;
    private String fileName;
    // 录音文件集合
    private List<String> filesName = new ArrayList<>();

    private AudioRecorderUtil() {
    }

    //单例模式
    public static AudioRecorderUtil getInstance() {
        if (sAudioRecorder == null) {
            sAudioRecorder = new AudioRecorderUtil();
        }
        return sAudioRecorder;
    }


    /**
     * 创建录音对象
     * @param fileName
     * @param audioSource
     * @param sampleRateInHz
     * @param channelConfig
     * @param audioFormat
     */
    public void createAudio(String fileName,int audioSource,
                            int sampleRateInHz,int channelConfig,int audioFormat){

        //获取缓冲区字节大小
        mBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        mAudioRecord = new AudioRecord(audioSource,sampleRateInHz,channelConfig,audioFormat,mBufferSize);
        this.fileName = fileName;
    }

    /**
     * 创建默认录音对象
     * @param fileName
     */
    private void createDefaultAudio(String fileName) {

        // 获得缓冲区字节大小
        mBufferSize = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE,
                AUDIO_CHANNEL, AUDIO_ENCODING);
        mAudioRecord = new AudioRecord(AUDIO_INPUT, AUDIO_SAMPLE_RATE, AUDIO_CHANNEL, AUDIO_ENCODING, mBufferSize);
        this.fileName = fileName;
        status = Status.STATUS_READY;
    }


    /**
     * 停止录音
     */
    public void stopRecord() {
        Log.d("AudioRecorder","===stopRecord===");
        if (status == Status.STATUS_NO_READY || status == Status.STATUS_READY) {
            throw new IllegalStateException("录音尚未开始");
        } else {
            mAudioRecord.stop();
            status = Status.STATUS_STOP;
            release();
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        Log.d("AudioRecorder","===release===");
        //假如有暂停录音
        try {
            if (filesName.size() > 0) {
                List<String> filePaths = new ArrayList<>();
                for (String fileName : filesName) {
                    filePaths.add(fileName);
                }
                //清除
                filesName.clear();
                //将多个pcm文件转化为wav文件
//                mergePCMFilesToWAVFile(filePaths);

            } else {
                //这里由于只要录音过filesName.size都会大于0,没录音时fileName为null
                //会报空指针 NullPointerException
                // 将单个pcm文件转化为wav文件
                //Log.d("AudioRecorder", "=====makePCMFileToWAVFile======");
                //makePCMFileToWAVFile();
            }
        } catch (IllegalStateException e) {
            throw new IllegalStateException(e.getMessage());
        }

        if (mAudioRecord != null) {
            mAudioRecord.release();
            mAudioRecord = null;
        }
        status = Status.STATUS_NO_READY;
    }




    /**
     * 取消录音
     */
    public void canel() {
        filesName.clear();
        fileName = null;
        if (mAudioRecord != null) {
            mAudioRecord.release();
            mAudioRecord = null;
        }
        status = Status.STATUS_NO_READY;
    }



    public void startRecoder(final RecordStreamListener listener){

        if (status == Status.STATUS_NO_READY || TextUtils.isEmpty(fileName)) {
            throw new RuntimeException("录音尚未初始化,请检查录音权限");
        }

        if(status == Status.STATUS_START){
            throw new RuntimeException("正在录音");
        }

        Log.e("tag","start recording " + mAudioRecord.getState());
        mAudioRecord.startRecording();

        new Thread(new Runnable() {
            @Override
            public void run() {

                writeDataTOFile(listener);
            }
        }).start();


    }

    /**
     * 将音频信息写入文件
     * @param listener
     */
    private void writeDataTOFile(RecordStreamListener listener) {
        //new 一个byte数组用来存放字节数据，大小为缓冲区大小

        byte[] audiodata = new byte[mBufferSize];

        FileOutputStream fos = null;

        int readsize =0 ;
        try {
            String currFileName = fileName;
            if(status == Status.STATUS_PAUSE){
                //假如是暂停录音 将文件名后面加个数字,防止重名文件内容被覆盖
                currFileName += filesName.size();
            }

            filesName.add(currFileName);
            File file = new File(currFileName);
            if(file.exists()){
                file.delete();
            }

            fos = new FileOutputStream(file);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("AudioRecorder", e.getMessage());
            throw new IllegalStateException(e.getMessage());
        }

        //将录音状态改为正在录音
        status = Status.STATUS_START;
        while (status == Status.STATUS_START) {
           readsize = mAudioRecord.read(audiodata,0,mBufferSize);
            if (AudioRecord.ERROR_INVALID_OPERATION != readsize && fos != null) {
                try {
                    fos.write(audiodata);
                    if(listener != null){
                        listener.onStreamListener();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            if (fos != null) {
                fos.close();// 关闭写入流
            }
        } catch (IOException e) {
            Log.e("AudioRecorder", e.getMessage());
        }


    }


    public enum Status{

        STATUS_NO_READY(0,"未准备"),
        STATUS_START(1,"开始录音"),
        STATUS_READY(2,"准备"),
        STATUS_PAUSE(3,"暂停"),
        STATUS_STOP(4,"停止");

        Status(int statusCode, String message) {

        }
    }

    public interface RecordStreamListener{
        void onStreamListener();
    }

}
