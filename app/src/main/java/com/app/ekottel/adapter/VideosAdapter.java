package com.app.ekottel.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.ekottel.R;
import com.app.ekottel.interfaces.ImageInterFace;
import com.app.ekottel.model.ImageModel;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

public class VideosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context mContext;
    ArrayList<ImageModel> imageList = new ArrayList<>();
    ImageInterFace mImageInterFace;
    String TAG = "VideosAdapter";

    public VideosAdapter(Context mContext, ArrayList<ImageModel> imageList) {
        this.mContext = mContext;
        this.imageList = imageList;
        mImageInterFace = (ImageInterFace) mContext;
    }

    private class DisplayViewHolder extends RecyclerView.ViewHolder {
        ImageView displayImg;
        RelativeLayout parentLayout;
        TextView durationTv;

        public DisplayViewHolder(View itemView) {
            super(itemView);
            displayImg = itemView.findViewById(R.id.display_img);
            parentLayout = itemView.findViewById(R.id.image_parent_layout);
            durationTv = itemView.findViewById(R.id.duration_tv);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_video_display, parent, false);
        return new DisplayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DisplayViewHolder) {
            final DisplayViewHolder viewHolder = (DisplayViewHolder) holder;
            final ImageModel imageModel = imageList.get(position);
            viewHolder.displayImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(mContext)
                    .load(new File(imageModel.getFilePath()))
                    .into(viewHolder.displayImg);
            viewHolder.durationTv.setText(imageModel.getDuration());
            viewHolder.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mImageInterFace.sendVideoFile(imageModel.getFilePath());
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }


}