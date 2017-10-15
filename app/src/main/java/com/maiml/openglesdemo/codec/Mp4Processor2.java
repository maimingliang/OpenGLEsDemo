package com.maiml.openglesdemo.codec;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;

import com.maiml.openglesdemo.egl.OesFilter2;
import com.maiml.openglesdemo.utils.EGLHelper2;
import com.maiml.openglesdemo.utils.TextureHelper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

/**
 * Created by maimingliang on 2017/10/15.
 */

public class Mp4Processor2 implements SurfaceTexture.OnFrameAvailableListener {


    private final int TIME_OUT=1000;
    private final EGLHelper2 eglHelper;
    private final MediaCodec.BufferInfo videoDecoderBufferInfo; //用于存储当前帧的视频解码信息
    private final MediaCodec.BufferInfo videoEncoderBufferInfo; //用于存储当前帧的视频编码信息
    private final MediaCodec.BufferInfo audioDecoderBufferInfo; //用于存储当前帧的音频解码信息
    private final MediaCodec.BufferInfo audioEncoderBufferInfo; //用于存储当前帧的音频编码信息

    private String inputPath;
    private String outputPath;

    private Context mContext;
    private int inputVideoWidth = 0;//视频输入宽度
    private int inputVideoHeight = 0;//视频输入高度

    private int outputVideoWidth = 0;//视频输出宽度
    private int outputVideoHeight =0; //视频输出高度

    private CompleteListener completeListener;
    private final Object PROCESS_LOCK=new Object();
    private final Object Extractor_LOCK=new Object();
    private final Object MUX_LOCK=new Object();
    private MediaExtractor mediaExtractor;
    private MediaCodec videoDecoder;
    private int videoTextureId;
    private SurfaceTexture videoSurfaceTexture;

    private boolean isRenderToWindowSurface;        //是否渲染到用户设置的WindowBuffer上，用于测试
    private MediaCodec videoEncoder;
    private Surface outputSurface;
    private MediaMuxer muxer;

    private int audioDecoderTrack = -1;
    private int audioEncoderTrack = -1;
    private int mVideoEncoderTrack = -1;
    private int videoDecoderTrack = -1;

    private boolean isStarted;
    private boolean isUserWantToStop;
    private boolean isVideoExtractorEnd;
    private boolean mGLThreadFlag;
    private Thread glThread;
    private boolean mCodecFlag;
    private Thread mDecoderThread;
    private Semaphore mSem;
    private Semaphore mDecoderSem;
    private OesFilter2 mRenderer;
    private long mVideoStopTimeStamp;
    private boolean isAudioExtractorEnd;


    public void setCompleteListener(CompleteListener completeListener) {
        this.completeListener = completeListener;
    }

    public void setVideoSize(int width, int height){
        outputVideoWidth = width;
        outputVideoHeight = height;
    }


    /**
     * 视频输入路径
     * @param inputPath
     */
    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    /**
     * 设置视频输入路径
     * @param outputPath
     */
    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public Mp4Processor2(Context context) {
        mContext = context;
        eglHelper = new EGLHelper2();
        videoDecoderBufferInfo = new MediaCodec.BufferInfo();
        videoEncoderBufferInfo = new MediaCodec.BufferInfo();
        audioDecoderBufferInfo = new MediaCodec.BufferInfo();
        audioEncoderBufferInfo = new MediaCodec.BufferInfo();
    }


    public boolean prepare() throws IOException {
        //获取视频旋转信息，并作出相应的处理
        synchronized (PROCESS_LOCK){
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(inputPath);
            //分离音视频
            mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(inputPath);
            int count = mediaExtractor.getTrackCount();
            int videoRattion = 0;
            for(int i =0 ;i < count;i++){
                MediaFormat format = mediaExtractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                Log.e("tag","---- extractor format-->"+mediaExtractor.getTrackFormat(i));

                if(mime.startsWith("audio/")){
                    audioDecoderTrack = i;
                }else if(mime.startsWith("video/")){
                    //5.0 一下不能解析mp4v-es
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP&&mime.equals(MediaFormat.MIMETYPE_VIDEO_MPEG4)) {
                        return false;
                    }

                    videoDecoderTrack = i;
                    String rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);


                    if(rotation != null){
                        videoRattion = Integer.valueOf(rotation);
                    }
                    //判断方向
                    if(videoRattion == 90 || videoRattion == 270){
                        inputVideoHeight = format.getInteger(MediaFormat.KEY_WIDTH);
                        inputVideoWidth = format.getInteger(MediaFormat.KEY_HEIGHT);
                    }else{
                        inputVideoWidth = format.getInteger(MediaFormat.KEY_WIDTH);
                        inputVideoHeight = format.getInteger(MediaFormat.KEY_HEIGHT);
                    }

                    Log.e("tag","--create decoder");
                    videoDecoder = MediaCodec.createDecoderByType(mime);
                    Log.e("tag","--create decoder end");

                    videoTextureId = TextureHelper.createTextureID();
                    videoSurfaceTexture = new SurfaceTexture(videoTextureId);
                    videoSurfaceTexture.setOnFrameAvailableListener(this);
                    videoDecoder.configure(format,new Surface(videoSurfaceTexture),null,0);

                    if(!isRenderToWindowSurface){
                        if(outputVideoWidth == 0 || outputVideoHeight == 0){
                            outputVideoWidth = inputVideoWidth;
                            outputVideoHeight = inputVideoHeight;
                        }

                        MediaFormat videoFormat = MediaFormat.createVideoFormat(mime,outputVideoWidth,outputVideoHeight);
                        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
                        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE,outputVideoWidth * outputVideoHeight * 5);
                        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE,24);
                        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,1);
                        videoEncoder = MediaCodec.createEncoderByType(mime);
                        videoEncoder.configure(videoFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);

                        outputSurface = videoEncoder.createInputSurface();
                        Bundle bundle = new Bundle();
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                            bundle.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE,outputVideoWidth * outputVideoHeight * 5);
                            videoEncoder.setParameters(bundle);
                        }

                    }


                }//if end
            }// for end

            if(!isRenderToWindowSurface){
                //如果用户没有设置渲染到指定Surface，就需要导出视频，暂时不对音频做处理
                muxer = new MediaMuxer(outputPath,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                Log.e("tag","--- video ratation = " + videoRattion);
                //如果有音轨
                if(audioDecoderTrack >= 0){
                    MediaFormat format = mediaExtractor.getTrackFormat(audioDecoderTrack);
                    Log.e("tag","----audio track = " + format.toString());

                    muxer.addTrack(format);
                }

            }

        }


        return true;
    }


    public boolean start() throws IOException {

        synchronized (PROCESS_LOCK){
            if(!isStarted){
                if(!prepare()){
                    Log.e("tag","----preprare fail");
                    return false;
                }

                isUserWantToStop=false;
                isVideoExtractorEnd=false;

                mGLThreadFlag=true;
                videoDecoder.start();
                if(!isRenderToWindowSurface){
                    videoEncoder.start();
                }

                glThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        glRunnable();
                    }
                });

                glThread.start();

                mCodecFlag = true;

                mDecoderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //视频处理
                        if(videoDecoderTrack >= 0){
                            Log.e("tag","video decoder step start");
                            while (mCodecFlag && !videoDecoderStep());
                            Log.e("tag","video decoder step end");
                            mGLThreadFlag = false;

                            try {
                                mSem.release();
                                glThread.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            //将原视频中的音频复制到新的视频中
                            if(audioDecoderTrack >= 0&& mVideoEncoderTrack >=0){
                                ByteBuffer buffer= ByteBuffer.allocate(1024*32);
                                while (mCodecFlag&&!audioDecodeStep(buffer));
                                buffer.clear();
                            }

                            Log.e("tag","codec thread_finish");
                            mCodecFlag = false;
                            avStop();

                            if(completeListener != null){
                                completeListener.onComplete(outputPath);
                            }
                        }
                    }
                });
                mDecoderThread.start();
                isStarted = true;

            }
        }
        return true;
    }

    private void glRunnable() {
        mSem = new Semaphore(0);
        mDecoderSem = new Semaphore(1);
        eglHelper.setSurface(outputSurface);
        boolean ret = eglHelper.createGLES(outputVideoWidth,outputVideoHeight);

        if(!ret){
            return;
        }

        if(mRenderer == null){
            mRenderer = new OesFilter2(mContext);
        }

        mRenderer.create();
        mRenderer.sizeChanged(outputVideoWidth,outputVideoHeight,inputVideoWidth,inputVideoHeight);
        while (mGLThreadFlag){
            try {

                Log.e("tag","---mSem.acquire");
                mSem.acquire();
                Log.e("tag","--- mSem.acquire end");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(mGLThreadFlag){
                videoSurfaceTexture.updateTexImage();
                videoSurfaceTexture.getTransformMatrix(mRenderer.getTextureMatrix());
                mRenderer.draw(videoTextureId);
                eglHelper.setPresentationTime(videoDecoderBufferInfo.presentationTimeUs*1000);
                if(!isRenderToWindowSurface){
                    videoEncoderStep(false);
                }
                eglHelper.swapBuffers();
            }

            mDecoderSem.release();
        }

        if(!isRenderToWindowSurface){
            videoEncoderStep(true);
        }
        eglHelper.destroyGLES();
        mRenderer.destroy();


    }

    private boolean videoEncoderStep(boolean isEnd) {

        if(isEnd){
            videoEncoder.signalEndOfInputStream();
        }

        while (true){
            int outputIndex = videoEncoder.dequeueOutputBuffer(videoEncoderBufferInfo, TIME_OUT);

            if(outputIndex >= 0){
                ByteBuffer outputBuffer = getOutputBuffer(videoEncoder, outputIndex);
                if(videoEncoderBufferInfo.size > 0){
                    muxer.writeSampleData(mVideoEncoderTrack,outputBuffer,videoDecoderBufferInfo);
                }

                videoEncoder.releaseOutputBuffer(outputIndex,false);
            }else if(outputIndex== MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                MediaFormat format=videoEncoder.getOutputFormat();
                Log.d("tag","video format -->"+format.toString());
                mVideoEncoderTrack=muxer.addTrack(format);
                muxer.start();
                synchronized (MUX_LOCK){
                    MUX_LOCK.notifyAll();
                }
            }else if(outputIndex== MediaCodec.INFO_TRY_AGAIN_LATER){
                break;
            }

        }

        return false;
    }

    private boolean audioDecodeStep(ByteBuffer buffer) {

        boolean isTimeEnd = false;
        buffer.clear();

        synchronized (Extractor_LOCK){
            mediaExtractor.selectTrack(audioDecoderTrack);
            int lenght = mediaExtractor.readSampleData(buffer, 0);
            if(lenght != -1){
                int flag = mediaExtractor.getSampleFlags();
                audioDecoderBufferInfo.size = lenght;
                audioDecoderBufferInfo.flags = flag;
                audioDecoderBufferInfo.presentationTimeUs = mediaExtractor.getSampleTime();
                audioDecoderBufferInfo.offset = 0;
                isTimeEnd = mediaExtractor.getSampleTime()>=mVideoStopTimeStamp;
                muxer.writeSampleData(audioDecoderTrack,buffer,audioDecoderBufferInfo);

            }

            isAudioExtractorEnd = !mediaExtractor.advance();
        }

        return isAudioExtractorEnd || isTimeEnd;
    }

    //视频解码到SurfaceTexture上，以供后续处理。返回值为是否是最后一帧视频
    private boolean videoDecoderStep() {

        int inputIndex = videoDecoder.dequeueInputBuffer(TIME_OUT);

        if(inputIndex >= 0){
            ByteBuffer inputBuffer = getInputBuffer(videoDecoder, inputIndex);
            inputBuffer.clear();
            synchronized (Extractor_LOCK){
                mediaExtractor.selectTrack(videoDecoderTrack);
                int ret = mediaExtractor.readSampleData(inputBuffer, 0);
                if(ret != -1){
                    mVideoStopTimeStamp = mediaExtractor.getSampleTime();
                    videoDecoder.queueInputBuffer(inputIndex,0,ret,mVideoStopTimeStamp,mediaExtractor.getSampleFlags());
                }
                isVideoExtractorEnd = !mediaExtractor.advance();
            }
        }


        while (true){
            int outputIndex = videoDecoder.dequeueOutputBuffer(videoDecoderBufferInfo, TIME_OUT);
            if(outputIndex >= 0){

                if(!isUserWantToStop){
                    try {
                        Log.e("tag"," mDecodeSem.acquire ");
                        mDecoderSem.acquire();
                        Log.e("tag"," mDecodeSem.acquire end");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

                videoDecoder.releaseOutputBuffer(outputIndex,true);
            }else if(outputIndex== MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                //MediaFormat format=mVideoDecoder.getOutputFormat();
            }else if(outputIndex== MediaCodec.INFO_TRY_AGAIN_LATER){
                break;
            }
        }



        return isVideoExtractorEnd || isUserWantToStop;
    }



    private void avStop() {

        if(isStarted){
            if(videoDecoder != null){
                videoDecoder.stop();
                videoDecoder.release();
                videoDecoder = null;
            }

            if(!isRenderToWindowSurface && videoEncoder != null){
                videoEncoder.stop();
                videoEncoder.release();
                videoEncoder = null;
            }

            if(!isRenderToWindowSurface){

                if(muxer != null&&mVideoEncoderTrack>=0){
                    try {
                        muxer.stop();
                        muxer.release();
                        muxer = null;
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

            }

            if(mediaExtractor != null){
                mediaExtractor.release();
            }

            isStarted = false;

            audioDecoderTrack = -1;
            videoDecoderTrack = -1;
            mVideoEncoderTrack = -1;
            audioEncoderTrack = -1;

        }

    }

    public boolean stop() throws InterruptedException {

        synchronized (PROCESS_LOCK){
            if(isStarted){
                if(mCodecFlag){
                    mDecoderSem.release();
                    isUserWantToStop = true;
                    if(mDecoderThread != null&&mDecoderThread.isAlive()){
                        mDecoderThread.join();
                    }

                    isUserWantToStop = false;
                }
            }

        }
        return true;
    }

    public boolean release() throws InterruptedException {
        synchronized (PROCESS_LOCK){
            if(mCodecFlag){
                stop();
            }
        }
        return true;
    }



    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        Log.e("tag","----msem release");
        mSem.release();
    }


    private ByteBuffer getInputBuffer(MediaCodec codec, int index){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return codec.getInputBuffer(index);
        }else{
            return codec.getInputBuffers()[index];
        }
    }

    private ByteBuffer getOutputBuffer(MediaCodec codec, int index){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return codec.getOutputBuffer(index);
        }else{
            return codec.getOutputBuffers()[index];
        }
    }

    public interface CompleteListener{
        void onComplete(String path);
    }


}
