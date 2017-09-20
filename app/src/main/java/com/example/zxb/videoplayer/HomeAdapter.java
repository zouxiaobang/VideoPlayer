package com.example.zxb.videoplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zouxiaobang on 17-9-20.
 */

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder>{
    private Context mContext;
    private List<VideoInfo> mVideoInfos = new ArrayList<>();
    private List<Bitmap> mBitmaps = new ArrayList<>();
    private List<Bitmap> mViewPagerBitmaps = new ArrayList<>();
    private OnClickListener mListener;
    private ViewPagerAdapter mAdapter = new ViewPagerAdapter();

    public HomeAdapter(Context context, List<VideoInfo> videoInfos,
                       List<Bitmap> bitmaps, List<Bitmap> viewPagerBitmaps){
        mContext = context;
        mVideoInfos = videoInfos;
//        for (int i = 0;i < (mBitmaps.size() > 5?5:mBitmaps.size());i ++){
//            mBitmaps.add(bitmaps.get(i));
//        }
        mBitmaps = bitmaps;
        mViewPagerBitmaps = viewPagerBitmaps;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.home_cycle_view_item, null);
        MyViewHolder myViewHolder = new MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        if (position == 0){
            holder.rlViewPager.setVisibility(View.VISIBLE);
            int height = mContext.getResources().getDisplayMetrics().heightPixels;
            int vpHeight = height*3/7;

            ViewGroup.LayoutParams layoutParams = holder.viewPager.getLayoutParams();
            layoutParams.height = vpHeight;
            holder.viewPager.setLayoutParams(layoutParams);
            ViewGroup.LayoutParams layoutParams1 = holder.cpPoint.getLayoutParams();
            layoutParams1.height = vpHeight;
            holder.cpPoint.setLayoutParams(layoutParams1);

            holder.rlContent.setVisibility(View.GONE);
            holder.viewPager.setAdapter(mAdapter);
            holder.viewPager.setPageTransformer(true, new CubeOutTransformer());
            holder.viewPager.setCurrentItem(mViewPagerBitmaps.size()*100);
            holder.cpPoint.setViewPager(mViewPagerBitmaps.size(), holder.viewPager, vpHeight - 80);
        } else {
            holder.rlViewPager.setVisibility(View.GONE);
            holder.rlContent.setVisibility(View.VISIBLE);

            holder.tvName.setText(mVideoInfos.get(position-1).getName());
            holder.tvTime.setText(mVideoInfos.get(position-1).getTime());
            try {
                Bitmap bitmap = mBitmaps.get(position-1);
                if (bitmap != null)
                    holder.imageView.setImageBitmap(bitmap);
            } catch (Exception e){
                Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.defaul_img);
                holder.imageView.setImageBitmap(bitmap);
            }

            holder.rlContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener !=null){
                        mListener.onClicked(v, position-1);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mVideoInfos.size() + 1;
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        ViewPager viewPager;
        RelativeLayout rlContent;
        TextView tvName;
        TextView tvTime;
        ImageView imageView;
        RelativeLayout rlViewPager;
        CircleViewPager cpPoint;

        public MyViewHolder(View itemView) {
            super(itemView);
            viewPager = (ViewPager) itemView.findViewById(R.id.vp_display);
            rlContent = (RelativeLayout) itemView.findViewById(R.id.rl_content);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvTime = (TextView) itemView.findViewById(R.id.tv_time);
            imageView = (ImageView) itemView.findViewById(R.id.iv_image);
            rlViewPager = (RelativeLayout) itemView.findViewById(R.id.rl_viewpager);
            cpPoint = (CircleViewPager) itemView.findViewById(R.id.circle_point);
        }
    }


    private class ViewPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (mViewPagerBitmaps.size() != 0)
                position = position%mViewPagerBitmaps.size();

            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.home_view_pager_item, null);
            ImageView ivFrame = (ImageView) view.findViewById(R.id.iv_frame);
            if (mViewPagerBitmaps.size() != 0)
                ivFrame.setImageBitmap(mViewPagerBitmaps.get(position));

            container.addView(view);

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

        }
    }

    interface OnClickListener{
        void onClicked(View view, int position);
    }

    public void setOnClickListener(OnClickListener listener){
        this.mListener = listener;
    }
}
