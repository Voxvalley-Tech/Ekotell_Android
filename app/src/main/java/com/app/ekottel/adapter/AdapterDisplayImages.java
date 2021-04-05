package com.app.ekottel.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.app.ekottel.R;
import com.app.ekottel.activity.ShowImagesActivity;
import com.app.ekottel.interfaces.ImageInterFace;
import com.app.ekottel.model.ImageModel;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

public class AdapterDisplayImages extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context mContext;
    ArrayList<ImageModel> imageList = new ArrayList<>();
    int selectedCount = 0;
    ImageInterFace mImageInterFace;

    public AdapterDisplayImages(Context mContext, ArrayList<ImageModel> imageList) {
        this.mContext = mContext;
        this.imageList = imageList;
        mImageInterFace = (ImageInterFace) mContext;
    }

    private class DisplayViewHolder extends RecyclerView.ViewHolder {
        ImageView displayImg;
        RelativeLayout imageCheckBox, parentLayout;

        public DisplayViewHolder(View itemView) {
            super(itemView);
            displayImg = itemView.findViewById(R.id.display_img);
            imageCheckBox = itemView.findViewById(R.id.selected_img);
            parentLayout = itemView.findViewById(R.id.image_parent_layout);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_image_display, parent, false);
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
            if (imageModel.isChecked()) {
                viewHolder.imageCheckBox.setVisibility(View.VISIBLE);
            } else {
                viewHolder.imageCheckBox.setVisibility(View.GONE);
            }
            viewHolder.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (selectedCount < 10 || imageModel.isChecked()) {
                        if (imageModel.isChecked()) {
                            viewHolder.imageCheckBox.setVisibility(View.GONE);
                            imageModel.setChecked(false);
                            selectedCount--;
                            mImageInterFace.updateImageSelection(imageModel,false);
                        } else {
                            viewHolder.imageCheckBox.setVisibility(View.VISIBLE);
                            imageModel.setChecked(true);
                            selectedCount++;
                            mImageInterFace.updateImageSelection(imageModel,true);
                        }
                        if (selectedCount > 0) {
                            ShowImagesActivity.mDoneTv.setVisibility(View.VISIBLE);
                        } else {
                            ShowImagesActivity.mDoneTv.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(mContext, "You can not send more than 10 files at a time", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }
}
