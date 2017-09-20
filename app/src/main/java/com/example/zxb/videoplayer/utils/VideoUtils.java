package com.example.zxb.videoplayer.utils;

import android.util.Log;

import com.example.zxb.videoplayer.bean.VideoInfo;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by zouxiaobang on 17-9-20.
 */

public class VideoUtils {

    public static void getAllVideoList(final List<VideoInfo> allVideos, final File file){

        file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String name = pathname.getName();

                int i = name.indexOf(".");
                if (i != -1){
                    String suffix = name.substring(i);

                    if (suffix.equalsIgnoreCase(".mp4")
                            || suffix.equalsIgnoreCase(".3gp")
                            || suffix.equalsIgnoreCase(".wmv")
                            || suffix.equalsIgnoreCase(".ts")
                            || suffix.equalsIgnoreCase(".rmvb")
                            || suffix.equalsIgnoreCase(".mov")
                            || suffix.equalsIgnoreCase(".m4v")
                            || suffix.equalsIgnoreCase(".avi")
                            || suffix.equalsIgnoreCase(".m3u8")
                            || suffix.equalsIgnoreCase(".3gpp2")
                            || suffix.equalsIgnoreCase(".mkv")
                            || suffix.equalsIgnoreCase(".flv")
                            || suffix.equalsIgnoreCase(".divx")
                            || suffix.equalsIgnoreCase(".f4v")
                            || suffix.equalsIgnoreCase(".rm")
                            || suffix.equalsIgnoreCase(".asf")
                            || suffix.equalsIgnoreCase(".ram")
                            || suffix.equalsIgnoreCase(".mpg")
                            || suffix.equalsIgnoreCase(".v8")
                            || suffix.equalsIgnoreCase(".swf")
                            || suffix.equalsIgnoreCase(".m2v")
                            || suffix.equalsIgnoreCase(".asx")
                            || suffix.equalsIgnoreCase(".ra")
                            || suffix.equalsIgnoreCase(".ndivx")
                            || suffix.equalsIgnoreCase(".xvid")){

                        VideoInfo info = new VideoInfo();
                        info.setName(name);
                        long timeLong = pathname.lastModified();
                        Date date = new Date(timeLong);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        String time = format.format(date);
                        info.setTime(time);
                        info.setPath(pathname.getAbsolutePath());

                        allVideos.add(info);
                        Log.i("utils", "accept: file = " + name);
                        return true;
                    }
                } else if (pathname.isDirectory()){
                    getAllVideoList(allVideos, pathname);
                }

                return false;
            }
        });
    }
}
