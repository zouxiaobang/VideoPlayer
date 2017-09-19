package com.example.zxb.videoplayer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.BoolRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
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
    private FrameLayout mFlProgress;
    private CircleProgress mCpProgress;

    private AudioManager mAudioManager;
    private boolean isFullScreen;
    private boolean isAdjustLight = false;
    private boolean isAdjustProgress = false;
    private float threshold = 100;
    private int mScreenWidth, mScreenHeight;
    private float lastX = 0, lastY = 0;
    private int mCurrentVolume = 0;
    private float mCurrentLight = 0;
    private float mCurrentProgress = 0;
    private boolean isTouch = false;

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
        mFlProgress = (FrameLayout) findViewById(R.id.fl_progress);
        mCpProgress = (CircleProgress) findViewById(R.id.cp_progress);

        mCpProgress.setVisibility(View.GONE);
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

        try {
            int light = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.screenBrightness = light*1.0f/255;
            getWindow().setAttributes(lp);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

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
                    mIvScreen.setImageResource(R.drawable.upstep);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    mIvScreen.setImageResource(R.drawable.reduce);
                }
            }
        });

        mLlController.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
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
                        mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

                        WindowManager.LayoutParams attributes = getWindow().getAttributes();
                        mCurrentLight = attributes.screenBrightness;

                        mCurrentProgress = mVideoView.getCurrentPosition();

                        if (x < mScreenWidth/2){
                            mCpProgress.setProgress((int) (mCurrentLight * 360));
                            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.light);
                            mCpProgress.setBitmap(bitmap);
                        } else {
                            mCpProgress.setProgress(mCurrentVolume * 360 /15);
                            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sound);
                            mCpProgress.setBitmap(bitmap);
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float detlaX = x - lastX;
                        float detalY = y - lastY;
                        float absdetlaX = Math.abs(detlaX);
                        float absdetlaY = Math.abs(detalY);

                        if (absdetlaX > threshold && absdetlaY > threshold){
                            if (absdetlaX < absdetlaY){
                                //adjust the light or sound
                                isAdjustLight = true;
                            } else {
                                //adjust the progress
                                isAdjustProgress = true;
                            }
                        } else if (absdetlaX < threshold && absdetlaY > threshold){
                            isAdjustLight = true;
                        } else if (absdetlaX > threshold && absdetlaY < threshold){
                            isAdjustProgress = true;
                        }

                        if (isAdjustLight && !isAdjustProgress){
                            mCpProgress.setVisibility(View.VISIBLE);
                            if (x < mScreenWidth/2){
                                changeLight(-detalY, mCurrentLight);
                            } else {
                                changeVolume(-detalY, mCurrentVolume);
                            }
                        }
                        if (isAdjustProgress && !isAdjustLight){
                            mUiHandler.sendEmptyMessage(UPDATE_UI);
                            changeProgress(detlaX, mCurrentProgress);
                        }

                        break;
                    case MotionEvent.ACTION_UP:
                        mCpProgress.setVisibility(View.GONE);
                        isAdjustLight = false;
                        isAdjustProgress = false;
                        break;
                }

                return true;
            }
        });
    }

    private void changeVolume(float detlaY, int current){
        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float radio = detlaY/mScreenHeight;
        int progress = (int) (radio*360+current*360/max);

        int volume = (int) (radio*max + current);

        if (progress > 360){
            progress = 360;
        } else if (progress < 0){
            progress = 0;
        }

        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        mSbSound.setProgress(volume);

        mCpProgress.setProgress(progress);
    }

    private void changeLight(float detlaY, float current){
        float radio = detlaY/mScreenHeight;
        current += radio;
        if (current > 1.0f){
            current = 1.0f;
        } else if (current < 0.001f){
            current = 0.01f;
        }

        int progress = (int) (current*360);

        mCpProgress.setProgress(progress);

        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.screenBrightness = current;
        getWindow().setAttributes(attributes);

    }

    private void changeProgress(float detlaX, float current){
        float radio = detlaX/mScreenWidth;
        int progress = (int) (radio * mVideoView.getDuration() + current);
        mVideoView.seekTo(progress);
        mSbProgress.setProgress(progress);
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
