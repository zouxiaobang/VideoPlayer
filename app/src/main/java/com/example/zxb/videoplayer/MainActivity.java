package com.example.zxb.videoplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.zxb.videoplayer.bean.VideoInfo;
import com.example.zxb.videoplayer.utils.VideoUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE_RECORDING = 101;
    private RecyclerView mRecyclerView;
    private List<VideoInfo> mVideoInfos = new ArrayList<>();
    private HomeAdapter mAdapter;
    private List<Bitmap> mBitmaps = new ArrayList<>();
    private List<Bitmap> mViewPagerBitmaps = new ArrayList<>();
    private String filePath = "/videoplayer";

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            setViewPagerImage();
            mAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initView();

        int readPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> permissionStrings = new ArrayList<String>();
        boolean isPermission = false;

        if (readPermission != PackageManager.PERMISSION_GRANTED){
            permissionStrings.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            isPermission = true;
        }
        if (writePermission != PackageManager.PERMISSION_GRANTED) {
            permissionStrings.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            isPermission = true;
        }

        if (isPermission) {
            String[] mPermissionList = new String[permissionStrings.size()];
            mPermissionList = permissionStrings.toArray(mPermissionList);
            ActivityCompat.requestPermissions(this, mPermissionList, PERMISSION_REQUEST_CODE_RECORDING);
            return;
        } else{
            bindData();
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean granted = true;

        for (int counter = 0; counter < permissions.length; counter++) {
            if (grantResults.length > 0
                    && grantResults[counter] == PackageManager.PERMISSION_GRANTED){
                granted = true;
            } else {
                granted = false;
            }
        }
        if (granted) {
            bindData();
        }
    }

    private void bindData() {
        createFile();

        new Thread(){
            @Override
            public void run() {
                //        getVideoInVideoPlayer();
                getAllVideos();
                mHandler.sendEmptyMessage(1);
            }
        }.start();




        LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(manager);

        mAdapter = new HomeAdapter(this, mVideoInfos, mBitmaps, mViewPagerBitmaps);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter.notifyDataSetChanged();
//
        mAdapter.setOnClickListener(new HomeAdapter.OnClickListener() {
            @Override
            public void onClicked(View view, int position) {
                Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                intent.putExtra("path", mVideoInfos.get(position).getPath());
                startActivity(intent);
            }
        });
    }

    /**
     * create the file(videoplayer) when you first open
     */
    private void createFile() {
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED){
            String path = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + filePath;
            File file = new File(path);
            if (!file.exists()){
                file.mkdirs();
            }
        }
    }

    /**
     * get the video in path:/sdcard/videoplayer
     */
    private void getVideoInVideoPlayer(){
        VideoUtils.getAllVideoList(mVideoInfos,
                new File(Environment.getExternalStorageDirectory().getAbsolutePath() + filePath));

    }

    /**
     * get all videos in your phone
     */
    private void getAllVideos(){
        VideoUtils.getAllVideoList(mVideoInfos,
                new File(Environment.getExternalStorageDirectory().getAbsolutePath()));
        Log.i(TAG, "getAllVideos: " + mVideoInfos.size());
    }

    /**
     * set the image in the view pager
     */
    private void setViewPagerImage(){
        mBitmaps.clear();
        mViewPagerBitmaps.clear();
        Log.i(TAG, "setViewPagerImage: " + mVideoInfos.size());
        if (mVideoInfos.size() == 0 ){
            mBitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.defaul_img));
        } else {
            Log.i(TAG, "setViewPagerImage: ");

            for (int i = 0;i < mVideoInfos.size();i ++){
                String path = mVideoInfos.get(i).getPath();
                MediaMetadataRetriever media = new MediaMetadataRetriever();
                media.setDataSource(path);
                Bitmap bitmap = media.getFrameAtTime();

                mBitmaps.add(bitmap);
                if (i < 5){
                    mViewPagerBitmaps.add(bitmap);
                }
            }
        }
    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_content);

    }

}
