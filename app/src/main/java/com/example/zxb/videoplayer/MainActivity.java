package com.example.zxb.videoplayer;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Path;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.BoolRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final int UPDATE_UI = 0;

    private CustomVideoView mVideoView;
    private LinearLayout mLlController;
    private SeekBar mSbProgress;
    private ImageView mIvPauseButton;
    private TextView mTvCurrentTime;
    private TextView mTvTotalTime;
    private ImageView mIvSound;
    private SeekBar mSbSound;
    private ImageView mIvScreen;
    private RelativeLayout mRlApp;

    private AudioManager mAudioManager;
    private boolean isFullScreen;
    private boolean isAdjust = false;
    private float threshold = 54;
    private int mScreenWidth, mScreenHeight;
    private float lastX = 0, lastY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initView();

        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            bindData();
            setPlayEvent();
        }
    }

    private void initView() {
        mVideoView = (CustomVideoView) findViewById(R.id.vv_video);
        mLlController = (LinearLayout) findViewById(R.id.ll_controllerbar);
        mSbProgress = (SeekBar) findViewById(R.id.sb_progress);
        mIvPauseButton = (ImageView) findViewById(R.id.iv_pause_img);
        mTvCurrentTime = (TextView) findViewById(R.id.tv_current_time);
        mTvTotalTime = (TextView) findViewById(R.id.tv_total_time);
        mIvSound = (ImageView) findViewById(R.id.iv_sound);
        mSbSound = (SeekBar) findViewById(R.id.sb_sound);
        mIvScreen = (ImageView) findViewById(R.id.iv_screen);
        mRlApp = (RelativeLayout) findViewById(R.id.rl_app);
    }

    private void bindData() {
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);


        /**
         * the local play
         */
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ){
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/yu.mp4";
            mVideoView.setVideoPath(path);

            MediaMetadataRetriever retr = new MediaMetadataRetriever();
            retr.setDataSource(path);
            String height = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT); // 视频高度
            String width = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH); // 视频宽度
            mVideoView.setWidthAndHeight(Integer.parseInt(width), Integer.parseInt(height));
        }

        mVideoView.start();
        mUiHandler.sendEmptyMessage(UPDATE_UI);

        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mSbSound.setMax(maxVolume);
        mSbSound.setProgress(streamVolume);
    }

    private void setPlayEvent() {
        //controll the video's play and stop
        mIvPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoView.isPlaying()){
                    //want to pause
                    mIvPauseButton.setImageResource(R.drawable.play);
                    mVideoView.pause();
                    mUiHandler.removeMessages(UPDATE_UI);
                } else {
                    mIvPauseButton.setImageResource(R.drawable.pause);
                    mVideoView.start();
                    mUiHandler.sendEmptyMessage(UPDATE_UI);
                }
            }
        });

        mSbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateTextViewWithTimeFormat(mTvCurrentTime, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mUiHandler.removeMessages(UPDATE_UI);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                //set the video's progress
                mVideoView.seekTo(progress);
                mUiHandler.sendEmptyMessage(UPDATE_UI);
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mIvPauseButton.setImageResource(R.drawable.play);
            }
        });

        mSbSound.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //set the device's volume
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        mIvScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFullScreen){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    mIvScreen.setImageResource(R.drawable.reduce);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    mIvScreen.setImageResource(R.drawable.upstep);
                }
            }
        });

        mRlApp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();
                float y = event.getY();
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        lastX = x;
                        lastY = y;
                        break;
                    case MotionEvent.ACTION_MOVE:
//                        Log.i("main", "x = " + lastX + " : y = " + lastY);
                        float detlaX = x - lastX;
                        float detalY = y - lastY;
//                        Log.i("main", "x = " + x + " : y = " + y);
                        float absdetlaX = Math.abs(detlaX);
                        float absdetlaY = Math.abs(detalY);
//                        Log.i("main", "x = " + absdetlaX + " : y = " + absdetlaY);

                        if (absdetlaX > threshold && absdetlaY > threshold){
                            if (absdetlaX < absdetlaY){
                                //adjust the light or
                                isAdjust = true;
                            } else {
                                //adjust the progress
                                isAdjust = false;
                            }
                        } else if (absdetlaX < threshold && absdetlaY > threshold){
                            isAdjust = true;
                        } else if (absdetlaX > threshold && absdetlaY < threshold){
                            isAdjust = false;
                        }

                        if (isAdjust){
                            if (x < mScreenWidth/2){

                            } else {
                                changeVolume(-detalY);
                            }
                        }

//                        lastX = x;
//                        lastY = y;

                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }

                return true;
            }
        });
    }

    private void changeVolume(float detlaY){
        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int index = (int) ((current / max * mScreenHeight + detlaY) / mScreenHeight * 15);

//        int index = (int) (detlaY/mScreenHeight*max);
        int volume = Math.max(index, 0);

        Log.i("main", " index == " + index) ;
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        mSbSound.setProgress(volume);
    }

    private Handler mUiHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == UPDATE_UI){
                //current time -- millis
                int currentPosition = mVideoView.getCurrentPosition() + 1000;
                updateTextViewWithTimeFormat(mTvCurrentTime, currentPosition);
                mSbProgress.setProgress(currentPosition);

                //total time -- millis
                int totalPosition = mVideoView.getDuration();
                updateTextViewWithTimeFormat(mTvTotalTime, totalPosition);
                mSbProgress.setMax(totalPosition);

                mUiHandler.sendEmptyMessageDelayed(0, 500L);
            }
        }
    };

    private void updateTextViewWithTimeFormat(TextView textView, int millis){
        int totalSecond = millis/1000;
        int hour = totalSecond/3600;
        int minute = totalSecond%3600/60;
        int second = totalSecond%60;

        String str = null;
        if (hour != 0){
            str = String.format("%02d:%02d:%02d", hour, minute, second);
        } else {
            str = String.format("%02d:%02d", minute, second);
        }
        textView.setText(str);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            mIvSound.setVisibility(View.VISIBLE);
            mSbSound.setVisibility(View.VISIBLE);

            isFullScreen = true;
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            mIvSound.setVisibility(View.GONE);
            mSbSound.setVisibility(View.GONE);

            isFullScreen = false;
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.seekTo(mSbProgress.getProgress());
        mVideoView.start();
        mUiHandler.sendEmptyMessage(UPDATE_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.pause();
        mUiHandler.removeMessages(UPDATE_UI);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    bindData();
                } else {
                    MainActivity.this.finish();
                }
                break;
        }
    }
}
