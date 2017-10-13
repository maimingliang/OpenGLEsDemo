package com.maiml.openglesdemo.codec;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 *
 * 对本地的aac文件用MediaCodec解码出来，就是pcm编码的音频数据了，然后直接将pcm数据写进AudioTrack进行播放。
 * Created by maimingliang on 2017/10/12.
 */

public class AudioDecoder {



    private static final String TAG = "AudioDecoder";
    public static final int KEY_CHANNEL_COUNT = 0;

    private Worker mWorker;

    private String path;//aac文件的路径。

    public AudioDecoder(String path) {
        this.path = path;
    }

    public void start() {
        if (mWorker == null) {
            mWorker = new Worker();
            mWorker.setRunning(true);
            mWorker.start();
        }
    }

    public void stop() {
        if (mWorker != null) {
            mWorker.setRunning(false);
            mWorker = null;
        }

    }

    private class Worker extends Thread{


        private static final int KEY_SAMPLE_RATE = 0;

        private boolean isRunning = false;
        private AudioTrack mPlayer;
        private MediaCodec mDecoder;
        private MediaExtractor extractor;

        public void setRunning(boolean running) {
            isRunning = running;
        }


        /**
         * 等待客户端连接，初始化解码器
         *
         * @return 初始化失败返回false，成功返回true
         */
        public boolean prepare() {
            // 等待客户端

            mPlayer = new AudioTrack(AudioManager.STREAM_MUSIC,8000, AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,2048,AudioTrack.MODE_STREAM);

            mPlayer.play();

            try {
                mDecoder = MediaCodec.createDecoderByType("audio/mp4a-latm");

                String encodeFile = path;

                extractor = new MediaExtractor();
                extractor.setDataSource(encodeFile);

                MediaFormat mediaFormat = null;

                for(int i = 0 ;i < extractor.getTrackCount();i++){

                    MediaFormat format = extractor.getTrackFormat(i);
                    String mime = format.getString(MediaFormat.KEY_MIME);
                    if(mime.startsWith("audio/")){
                        extractor.selectTrack(i);
                        mediaFormat = format;
                        break;
                    }
                }


                mediaFormat.setString(MediaFormat.KEY_MIME,"audio/mp4a-latm");
                mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT,KEY_CHANNEL_COUNT);
                mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE,KEY_SAMPLE_RATE);
                mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE,24000);
                mediaFormat.setInteger(MediaFormat.KEY_IS_ADTS,1);
                mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,0);

                mDecoder.configure(mediaFormat,null,null,0);



            } catch (IOException e) {
                e.printStackTrace();
            }


            if(mDecoder == null){
                Log.e(TAG, "create mediaDecode failed");
                return false;
            }


            mDecoder.start();

            return true;
        }


        @Override
        public void run() {
            super.run();

            if(!prepare()){

                isRunning = false;

                Log.e(TAG,"---- 音频解码器初始化失败");
            }


            while (isRunning){
                decoder();
            }


            release();

        }

        private ByteBuffer getInputBuffer(int index){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return mDecoder.getInputBuffer(index);
            }else{
                return mDecoder.getInputBuffers()[index];
            }
        }



        /**
         * 释放资源
         */
        private void release() {

            if (mDecoder != null) {
                mDecoder.stop();
                mDecoder.release();

            }
            if (mPlayer != null) {
                mPlayer.stop();
                mPlayer.release();
                mPlayer = null;
            }
            if (extractor != null) {
                extractor.release();
                extractor = null;
            }
        }
        /**
         * acc 解码 + 播放
         */
        private void decoder() {

            ByteBuffer[] codecInputBuffers = mDecoder.getInputBuffers();
            ByteBuffer[] codecOutputBuffers = mDecoder.getOutputBuffers();


            long kTimeOutUs = 5000;

            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

            boolean sawInputEOS = false;
            boolean sawOutputEOS = false;

            int totalRawSize = 0;

            try {


                while (!sawOutputEOS){

                    if(!sawInputEOS){
                        int inputBufIndex = mDecoder.dequeueInputBuffer(kTimeOutUs);

                        if(inputBufIndex >= 0){
                            ByteBuffer dstBuf = codecInputBuffers[inputBufIndex];
                           int sampleSize = extractor.readSampleData(dstBuf,0);

                            if(sampleSize < 0){
                                Log.i("TAG", "saw input EOS.");
                                sawInputEOS = true;
                                mDecoder.queueInputBuffer(inputBufIndex,0,0,0,MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            }else{
                                long presentationTimeUs = extractor.getSampleTime();
                                mDecoder.queueInputBuffer(inputBufIndex,0,sampleSize,presentationTimeUs,0);
                                extractor.advance();
                            }

                        }
                    }


                    int res = mDecoder.dequeueOutputBuffer(info, kTimeOutUs);

                    if(res >= 0){

                        int outputBufIndex = res;
                        // Simply ignore codec config buffers.
                        if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                            Log.i("TAG", "audio encoder: codec config buffer");
                            mDecoder.releaseOutputBuffer(outputBufIndex, false);
                            continue;
                        }


                        if(info.size != 0){

                            ByteBuffer outBuf = codecOutputBuffers[outputBufIndex];
                            outBuf.position(info.offset);
                            outBuf.limit(info.offset + info.size);

                            byte[] data = new byte[info.size];

                            outBuf.get(data);
                            totalRawSize += data.length;

                            // fosDecoder.write(data);
                            // 播放音乐
                            mPlayer.write(data, 0, info.size);
                        }

                        mDecoder.releaseOutputBuffer(outputBufIndex, false);
                        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            Log.i("TAG", "saw output EOS.");
                            sawOutputEOS = true;
                        }

                    }else if(res == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED){
                        codecOutputBuffers = mDecoder.getOutputBuffers();
                        Log.i("TAG", "output buffers have changed.");
                    }else if(res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                        MediaFormat oformat = mDecoder.getOutputFormat();
                        Log.i("TAG", "output format has changed to " + oformat);
                    }

                }
            }finally {
                // fosDecoder.close();
                extractor.release();
            }
        }




    }


}
