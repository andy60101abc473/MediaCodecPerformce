package com.example.lai.decodetest;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class Decoder {
    private static final String tag = "Decoder";
    private HashMap<Long, Debug.TimeTravel> mMap;
    private MediaCodec mMeidaCodec = null;
    private MediaExtractor mExtractor = null;
    private MediaCodec.BufferInfo mInfo = null;
    private Thread mDecodeThread = null;
    private boolean running = false;

    public Decoder(Surface surface, String videoFileName) {
        try {
            mInfo = new MediaCodec.BufferInfo();
            mExtractor = new MediaExtractor();
            mExtractor.setDataSource(videoFileName);
            for (int i = 0; i < mExtractor.getTrackCount(); i++) {
                MediaFormat format = mExtractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                Log.d(tag, "Decoder type: " + mime);
                if (mime.startsWith("video/")) {
                    mExtractor.selectTrack(i);
                    mMeidaCodec = MediaCodec.createDecoderByType(mime);
                    mMeidaCodec.configure(format, surface, null, 0);
                    mMeidaCodec.start();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        if(mDecodeThread == null) {
            running = true;
            mDecodeThread = new Thread(mHardwareDecode);
            mDecodeThread.start();
        }
    }

    public void stop() {
        running = false;

        if(mExtractor != null) {
            mExtractor.release();
            mExtractor = null;
        }

        if(mMeidaCodec != null) {
            mMeidaCodec.stop();
            mMeidaCodec.release();
            mMeidaCodec = null;
        }
    }

    public int queueFrame(Debug.TimeTravel timeTravel) {
        int inputBufferIndex = mMeidaCodec.dequeueInputBuffer(16000);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = mMeidaCodec.getInputBuffer(inputBufferIndex);
            inputBuffer.clear();
            int sampleSize = mExtractor.readSampleData(inputBuffer, 0);
            if(sampleSize > 0) {
                long sampleTime = mExtractor.getSampleTime();
                mMeidaCodec.queueInputBuffer(inputBufferIndex, 0, sampleSize, sampleTime, 0);
                mExtractor.advance();
                timeTravel.stamp("queue frame");
                mMap.put(sampleTime, timeTravel);
            } else {
                mMeidaCodec.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            }
        }
        return inputBufferIndex;
    }

    public int decodeFrame() {
        int outputBufferIndex = mMeidaCodec.dequeueOutputBuffer(mInfo, 16000);
        switch (outputBufferIndex) {
            case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                //Log.d(tag, "output index: INFO_OUTPUT_BUFFERS_CHANGED");
                break;
            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                //Log.d(tag, "output index: INFO_OUTPUT_FORMAT_CHANGED");
                break;
            case MediaCodec.INFO_TRY_AGAIN_LATER:
                //Log.d(tag, "output index: INFO_TRY_AGAIN_LATER");
                break;
            default:
                mMeidaCodec.releaseOutputBuffer(outputBufferIndex, true);
                Debug.TimeTravel timeTravel = mMap.remove(mInfo.presentationTimeUs);
                timeTravel.stamp("decode done");
                timeTravel.showTotalTime();
                break;
        }

        if ((mInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            Log.d(tag, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
            running = false;
        }

        return outputBufferIndex;
    }

    public int hardwareDecode(Debug.TimeTravel timeTravel) {
        int inputBufferIndex = mMeidaCodec.dequeueInputBuffer(1000000);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = mMeidaCodec.getInputBuffer(inputBufferIndex);
            inputBuffer.clear();
            int sampleSize = mExtractor.readSampleData(inputBuffer, 0);
            if(sampleSize > 0) {
                long sampleTime = mExtractor.getSampleTime();
                mMeidaCodec.queueInputBuffer(inputBufferIndex, 0, sampleSize, sampleTime, 0);
                mExtractor.advance();
                timeTravel.stamp("queue frame");
                mMap.put(sampleTime, timeTravel);
            } else {
                mMeidaCodec.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            }

            int outputBufferIndex = mMeidaCodec.dequeueOutputBuffer(mInfo, 1000000);
            switch (outputBufferIndex) {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    //Log.d(tag, "output index: INFO_OUTPUT_BUFFERS_CHANGED");
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    //Log.d(tag, "output index: INFO_OUTPUT_FORMAT_CHANGED");
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    //Log.d(tag, "output index: INFO_TRY_AGAIN_LATER");
                    break;
                default:
                    mMeidaCodec.releaseOutputBuffer(outputBufferIndex, true);
                    Debug.TimeTravel timeTravel2 = mMap.remove(mInfo.presentationTimeUs);
                    timeTravel2.stamp("decode done");
                    timeTravel2.showTotalTime();
                    break;
            }

            if ((mInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.d(tag, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                running = false;
            }

            return outputBufferIndex;
        }

        return inputBufferIndex;
    }

    private Runnable mHardwareDecode = new Runnable() {
        @SuppressLint("UseSparseArrays")
        @Override
        public void run() {
            mMap = new HashMap<>();
            while(running) {
                Debug.TimeTravel timeTravel = new Debug.TimeTravel();
                int queueStatus = queueFrame(timeTravel);
                if(queueStatus < 0) {
                    Log.d(tag, "Queue frame failed: " + queueStatus);
                }

                int decodeStatus = decodeFrame();
                if(decodeStatus < 0) {
                    Log.d(tag, "Decode frame failed: " + decodeStatus);
                }
            }
        }
    };
}
