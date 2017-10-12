package com.maiml.openglesdemo.utils;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * Created by maimingliang on 2017/10/12.
 */

public class PCMPlayer {

    private AudioTrack audioTrack;

    private int bufferSize;

    public int getBufferSize() {
        return bufferSize;
    }


    //默认播放

    private int sampleRate = 4000; //   c采样率4000 每秒采集4000个点
    private int channel = AudioFormat.CHANNEL_OUT_MONO;//声道 1 个 单声道
    private int format = AudioFormat.ENCODING_PCM_8BIT;//每个采样率8位量化

    public PCMPlayer(){
        initPlayer();
    }
    public PCMPlayer(int sampleRate, int channel, int format) {
        this.sampleRate = sampleRate;
        this.channel = channel;
        this.format = format;
        initPlayer();
    }


    private void initPlayer() {


        /**
         * AudioTrack 支持4K-48K c采样率
         * (sampleRateInHz< 4000) || (sampleRateInHz > 48000) )
         */
        //获取缓冲流大小
        int minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channel, format);


        /**
         * 参数:
         * 1.streamType
         *   STREAM_ALARM：警告声
         *   STREAM_MUSCI：音乐声，例如music等
         *   STREAM_RING：铃声
         *   STREAM_SYSTEM：系统声音
         *   STREAM_VOCIE_CALL：电话声音
         *
         * 2.采样率
         * 3.声道数
         * 4.采样精度
         * 5.每次播放的数据大小
         * 6.AudioTrack中有MODE_STATIC和MODE_STREAM两种分类。
         *   STREAM的意思是由用户在应用程序通过write方式把数据一次一次得写到audiotrack中。
         *   意味着你只需要开启播放后 后续使用write方法(AudioTrack的方法)写入buffer就行
         *
         *   STATIC的意思是一开始创建的时候，就把音频数据放到一个固定的buffer，然后直接传给audiotrack，
         *   后续就不用一次次得write了。AudioTrack会自己播放这个buffer中的数据。
         *   这种方法对于铃声等内存占用较小，延时要求较高的声音来说很适用。
         */

        //初始化AudioTrack
         audioTrack =  new AudioTrack(AudioManager.STREAM_MUSIC,sampleRate,channel,format,
                format,AudioTrack.MODE_STREAM);

        /**
         * 播放 后续z直接wwrite数据就行
         */
        audioTrack.play();
    }


    // 写入数据就能播放 但是要先初始化 开启播放器
    public void write(byte[] buffer) {
        /**
         * 1.要播放的buffer
         * 2.播放需要的位移
         * 3.播放的数据长度
         */
        audioTrack.write(buffer, 0, buffer.length);
        audioTrack.stop();
    }

    /**
     * 释放资源
     */
    public void destoryPlay() {
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }
    }



}
