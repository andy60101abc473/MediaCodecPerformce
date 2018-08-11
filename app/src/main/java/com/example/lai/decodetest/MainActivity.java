package com.example.lai.decodetest;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends AppCompatActivity {
    private String filePath = Environment.getExternalStorageDirectory().getPath() + "/sample.mp4";
    private SurfaceView mSurfaceView = null;
    private Decoder mVideoDecoder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSurfaceView = new SurfaceView(this);
        mSurfaceView.getHolder().addCallback(mSufaceCallback);
        setContentView(mSurfaceView);
    }

    private SurfaceHolder.Callback mSufaceCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if(mVideoDecoder == null) {
                mVideoDecoder = new Decoder(holder.getSurface(),filePath);
                mVideoDecoder.start();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if(mVideoDecoder != null) {
                mVideoDecoder.stop();
                mVideoDecoder = null;
            }
        }
    };
}
