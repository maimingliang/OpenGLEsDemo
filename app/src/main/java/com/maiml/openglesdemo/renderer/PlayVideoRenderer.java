package com.maiml.openglesdemo.renderer;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;

import com.maiml.openglesdemo.R;
import com.maiml.openglesdemo.codec.SurfaceDecoder;
import com.maiml.openglesdemo.codec.SurfaceEncoder;
import com.maiml.openglesdemo.utils.MatrixUtils;
import com.maiml.openglesdemo.utils.ShaderUtils;
import com.maiml.openglesdemo.utils.TextureHelper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by maimingliang on 2017/10/13.
 */

public class PlayVideoRenderer implements GLSurfaceView.Renderer, MediaPlayer.OnVideoSizeChangedListener, SurfaceTexture.OnFrameAvailableListener {

    private static final String TAG = "GLRenderer";
    private Context context;

    private int programId;
    private FloatBuffer vertexBuffer;
    private final float[] vertexData = {
            1f,-1f,0f,
            -1f,-1f,0f,
            1f,1f,0f,
            -1f,1f,0f
    };

    private final float[] projectionMatrix=new float[16];


    private final float[] textureVertexData = {
            1f,0f,
            0f,0f,
            1f,1f,
            0f,1f
    };
    private FloatBuffer textureVertexBuffer;

    private int textureId;

    private SurfaceTexture surfaceTexture;
    private MediaPlayer mediaPlayer;
    private float[] mSTMatrix = new float[16];

    private int uSTMMatrixHandle;
    private int uTextureSamplerHandle;
    private int aTextureCoordHandle;
    private int aPositionHandle;
    private int uMatrixHandle;

    private boolean updateSurface;
    private boolean playerPrepared;
    private int screenWidth,screenHeight;

    public PlayVideoRenderer(Context context,String videoPath) {
        this.context = context;
        playerPrepared=false;
        synchronized(this) {
            updateSurface = false;
        }
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);

        textureVertexBuffer = ByteBuffer.allocateDirect(textureVertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureVertexData);
        textureVertexBuffer.position(0);

        mediaPlayer=new MediaPlayer();
        try{
            mediaPlayer.setDataSource(context, Uri.parse(videoPath));
        }catch (IOException e){
            e.printStackTrace();
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setLooping(true);

        mediaPlayer.setOnVideoSizeChangedListener(this);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {



        programId = ShaderUtils.createProgram(context.getResources(),"vshader/play_vetex.sh","fshader/play_fragment.sh");


        aPositionHandle= GLES20.glGetAttribLocation(programId,"aPosition");

        uMatrixHandle=GLES20.glGetUniformLocation(programId,"uMatrix");
        uSTMMatrixHandle = GLES20.glGetUniformLocation(programId, "uSTMatrix");
        uTextureSamplerHandle=GLES20.glGetUniformLocation(programId,"sTexture");
        aTextureCoordHandle=GLES20.glGetAttribLocation(programId,"aTexCoord");


        textureId = TextureHelper.createTextureID();


        surfaceTexture = new SurfaceTexture(textureId);
        surfaceTexture.setOnFrameAvailableListener(this);

        Surface surface = new Surface(surfaceTexture);
        mediaPlayer.setSurface(surface);
        surface.release();

        if (!playerPrepared){
            try {
                mediaPlayer.prepare();
                playerPrepared=true;
            } catch (IOException t) {
                Log.e(TAG, "media player prepare failed");
            }
            mediaPlayer.start();
            playerPrepared=true;
        }
//        try {
//            EncodeDecodeSurfaceWrapper.runTest(this);
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged: "+width+" "+height);
        screenWidth=width; screenHeight=height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        synchronized (this){
            if (updateSurface){
                surfaceTexture.updateTexImage();
                surfaceTexture.getTransformMatrix(mSTMatrix);
                updateSurface = false;
            }
        }
        GLES20.glUseProgram(programId);
        GLES20.glUniformMatrix4fv(uMatrixHandle,1,false,projectionMatrix,0);
        GLES20.glUniformMatrix4fv(uSTMMatrixHandle, 1, false, mSTMatrix, 0);

        vertexBuffer.position(0);
        GLES20.glEnableVertexAttribArray(aPositionHandle);
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false,
                12, vertexBuffer);

        textureVertexBuffer.position(0);
        GLES20.glEnableVertexAttribArray(aTextureCoordHandle);
        GLES20.glVertexAttribPointer(aTextureCoordHandle,2,GLES20.GL_FLOAT,false,8,textureVertexBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,textureId);

        GLES20.glUniform1i(uTextureSamplerHandle,0);
        GLES20.glViewport(0,0,screenWidth,screenHeight);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    @Override
    synchronized public void onFrameAvailable(SurfaceTexture surface) {
        updateSurface = true;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        Log.d(TAG, "onVideoSizeChanged: "+width+" "+height);
        updateProjection(width,height);
    }

    private void updateProjection(int videoWidth, int videoHeight){


        if(screenWidth == 0 || screenHeight == 0){
            return;
        }

        Log.e("tag","---- screenWidth = " +screenWidth +"  screenHeight = " +screenHeight);
        Log.e("tag","---- videoWidth = " +videoWidth +"  videoHeight = " +videoHeight);
        float screenRatio=(float)screenWidth/screenHeight;
        float videoRatio=(float)videoWidth/videoHeight;
        if (videoRatio>screenRatio){


            Log.e("tag","---- > 1");
            Matrix.orthoM(projectionMatrix,0,-1f,1f,-videoRatio/screenRatio,videoRatio/screenRatio,-1f,1f);

            MatrixUtils.scale(projectionMatrix,videoRatio/screenRatio,videoRatio/screenRatio);
        }else {

            Log.e("tag","---- > 2");
            Matrix.orthoM(projectionMatrix,0,-screenRatio/videoRatio,screenRatio/videoRatio,-1f,1f,-1f,1f);
        }


    }


    private static final boolean VERBOSE = false;           // lots of logging

    private static final int MAX_FRAMES = 400;       // stop extracting after this many

    private SurfaceDecoder SDecoder=new SurfaceDecoder();
    private SurfaceEncoder SEncoder=new SurfaceEncoder();


    private static class EncodeDecodeSurfaceWrapper implements Runnable {
        private Throwable mThrowable;
        private PlayVideoRenderer mTest;

        private EncodeDecodeSurfaceWrapper(PlayVideoRenderer test) {
            mTest = test;
        }

        @Override
        public void run() {
            try {
                mTest.Prepare();
            } catch (Throwable th) {
                mThrowable = th;
            }
        }

        /** Entry point. */
        public static void runTest(PlayVideoRenderer obj) throws Throwable {
            EncodeDecodeSurfaceWrapper wrapper = new EncodeDecodeSurfaceWrapper(obj);
            Thread th = new Thread(wrapper, "codec test");
            th.start();
            //th.join();
            if (wrapper.mThrowable != null) {
                throw wrapper.mThrowable;
            }
        }
    }

    private void Prepare() throws IOException {
        try {

            SEncoder.VideoEncodePrepare();
            SDecoder.SurfaceDecoderPrePare(SEncoder.getEncoderSurface());
            doExtract();
        } finally {
            SDecoder.release();
            SEncoder.release();
        }
    }


    void doExtract() throws IOException {
        final int TIMEOUT_USEC = 10000;
        ByteBuffer[] decoderInputBuffers = SDecoder.decoder.getInputBuffers();
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        int inputChunk = 0;
        int decodeCount = 0;
        long frameSaveTime = 0;

        boolean outputDone = false;
        boolean inputDone = false;
        while (!outputDone) {
            if (VERBOSE) Log.d(TAG, "loop");

            // Feed more data to the decoder.
            if (!inputDone) {
                int inputBufIndex = SDecoder.decoder.dequeueInputBuffer(TIMEOUT_USEC);
                if (inputBufIndex >= 0) {
                    ByteBuffer inputBuf = decoderInputBuffers[inputBufIndex];
                    int chunkSize = SDecoder.extractor.readSampleData(inputBuf, 0);
                    if (chunkSize < 0) {
                        SDecoder.decoder.queueInputBuffer(inputBufIndex, 0, 0, 0L,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        inputDone = true;
                        if (VERBOSE) Log.d(TAG, "sent input EOS");
                    } else {
                        if (SDecoder.extractor.getSampleTrackIndex() != SDecoder.DecodetrackIndex) {
                            Log.w(TAG, "WEIRD: got sample from track " +
                                    SDecoder.extractor.getSampleTrackIndex() + ", expected " + SDecoder.DecodetrackIndex);
                        }
                        long presentationTimeUs = SDecoder.extractor.getSampleTime();
                        SDecoder.decoder.queueInputBuffer(inputBufIndex, 0, chunkSize,
                                presentationTimeUs, 0 /*flags*/);
                        if (VERBOSE) {
                            Log.d(TAG, "submitted frame " + inputChunk + " to dec, size=" +
                                    chunkSize);
                        }
                        inputChunk++;
                        SDecoder.extractor.advance();
                    }
                } else {
                    if (VERBOSE) Log.d(TAG, "input buffer not available");
                }
            }

            if (!outputDone) {
                int decoderStatus = SDecoder.decoder.dequeueOutputBuffer(info, TIMEOUT_USEC);
                if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // no output available yet
                    if (VERBOSE) Log.d(TAG, "no output from decoder available");
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    // not important for us, since we're using Surface
                    if (VERBOSE) Log.d(TAG, "decoder output buffers changed");
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat newFormat = SDecoder.decoder.getOutputFormat();
                    if (VERBOSE) Log.d(TAG, "decoder output format changed: " + newFormat);
                } else if (decoderStatus < 0) {

                } else { // decoderStatus >= 0
                    if (VERBOSE) Log.d(TAG, "surface decoder given buffer " + decoderStatus +
                            " (size=" + info.size + ")");
                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        if (VERBOSE) Log.d(TAG, "output EOS");
                        outputDone = true;
                    }

                    boolean doRender = (info.size != 0);

                    SDecoder.decoder.releaseOutputBuffer(decoderStatus, doRender);
                    if (doRender) {
                        if (VERBOSE) Log.d(TAG, "awaiting decode of frame " + decodeCount);

                        if (decodeCount < MAX_FRAMES) {
                            SDecoder.outputSurface.makeCurrent(1);
                            SDecoder.outputSurface.awaitNewImage();
                            SDecoder.outputSurface.drawImage(true);

                            SEncoder.drainEncoder(false);
                            SDecoder.outputSurface.setPresentationTime(computePresentationTimeNsec(decodeCount));
                            SDecoder.outputSurface.swapBuffers();

                        }
                        decodeCount++;
                    }

                }
            }
        }

        SEncoder.drainEncoder(true);
        int numSaved = (MAX_FRAMES < decodeCount) ? MAX_FRAMES : decodeCount;
        Log.d(TAG, "Saving " + numSaved + " frames took " +
                (frameSaveTime / numSaved / 1000) + " us per frame");
    }


    private static long computePresentationTimeNsec(int frameIndex) {
        final long ONE_BILLION = 1000000000;
        return frameIndex * ONE_BILLION / 30;
    }



    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }


}
