package com.app.ekottel.activity;

import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import static com.app.ekottel.utils.GlobalVariables.LOG;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.app.ekottel.R;
import com.app.ekottel.adapter.AdapterDisplayImages;
import com.app.ekottel.adapter.VideosAdapter;
import com.app.ekottel.interfaces.ImageInterFace;
import com.app.ekottel.model.ImageModel;
import com.app.ekottel.utils.Utils;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class ShowImagesActivity extends AppCompatActivity implements ImageInterFace {
    private RecyclerView mImageRecyclerView, mVideosRecyclerView;
    private ArrayList<ImageModel> imageList = new ArrayList<>();
    private ArrayList<ImageModel> videosList = new ArrayList<>();
    private AdapterDisplayImages adapterDisplayImages;
    private VideosAdapter videosAdapter;
    public static TextView mDoneTv, mImagesTv, mVideosTv;
    private ArrayList<ImageModel> selectedList = new ArrayList<>();
    private String TAG = "ShowImagesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_images);
        mImageRecyclerView = findViewById(R.id.images_recycler_view);
        mVideosRecyclerView=findViewById(R.id.videos_recycler_view);
        mDoneTv = findViewById(R.id.done_button_tv);
        mImagesTv = findViewById(R.id.images_tv);
        mVideosTv = findViewById(R.id.videos_tv);
        mImagesTv.setSelected(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        mImageRecyclerView.setLayoutManager(gridLayoutManager);
        GridLayoutManager gridLayoutManager1 = new GridLayoutManager(getApplicationContext(), 3);
        mVideosRecyclerView.setLayoutManager(gridLayoutManager1);
        imageList.clear();
        selectedList.clear();
        new getImage().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new getVideos().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        mDoneTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Utils.getNetwork(ShowImagesActivity.this)) {
                    ArrayList<String> filePaths = new ArrayList<>();
                    for (int i = 0; i < selectedList.size(); i++) {
                        filePaths.add(selectedList.get(i).getFilePath());
                    }
                    Intent intent = new Intent();
                    intent.putExtra("selectedList", filePaths);
                    intent.putExtra("isImageData", true);
                    setResult(RESULT_OK, intent);
                    finish();
                }else {
                    Toast.makeText(ShowImagesActivity.this, getString(R.string.network_unavail_message), Toast.LENGTH_LONG).show();
                }
            }
        });
        mImagesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mImagesTv.setSelected(true);
                mVideosTv.setSelected(false);
                mVideosRecyclerView.setVisibility(View.GONE);
                mImageRecyclerView.setVisibility(View.VISIBLE);
            }
        });
        mVideosTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mImagesTv.setSelected(false);
                mVideosTv.setSelected(true);
                mVideosRecyclerView.setVisibility(View.VISIBLE);
                mImageRecyclerView.setVisibility(View.GONE);
            }
        });
    }

    public void finsshScreen(View view) {
        finish();
    }

    @Override
    public void updateImageSelection(ImageModel fileData, boolean update) {
        if (update) {
            selectedList.add(fileData);
        } else {
            selectedList.remove(fileData);
        }
        LOG.info("updateImageSelection: selected list size " + selectedList.size());
    }

    @Override
    public void sendVideoFile(String filePath) {
          if(mVideosRecyclerView.getVisibility()==View.VISIBLE){
              Intent intent = new Intent();
              intent.putExtra("selectedList", filePath);
              intent.putExtra("isImageData", false);
              setResult(RESULT_OK, intent);
              finish();
          }
    }


    private class getImage extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            imageList = getAllShownImagesPath();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapterDisplayImages = new AdapterDisplayImages(ShowImagesActivity.this, imageList);
            mImageRecyclerView.setAdapter(adapterDisplayImages);
        }
    }

    private class getVideos extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            videosList = getAllShownVideosPath();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            videosAdapter = new VideosAdapter(ShowImagesActivity.this, videosList);
            mVideosRecyclerView.setAdapter(videosAdapter);
        }
    }


    private ArrayList<ImageModel> getAllShownImagesPath() {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<ImageModel> listOfAllImages = new ArrayList<>();
        String absolutePathOfImage = null;
        String fileName = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        cursor = getApplicationContext().getContentResolver().query(uri, projection, null,
                null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            fileName = cursor.getString(column_index_folder_name);
            ImageModel imageModel = new ImageModel();
            imageModel.setFilePath(absolutePathOfImage);
            imageModel.setFileName(fileName);
            listOfAllImages.add(imageModel);
        }
        return listOfAllImages;
    }

    private ArrayList<ImageModel> getAllShownVideosPath() {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<ImageModel> listOfAllImages = new ArrayList<>();
        String absolutePathOfImage = null;
        String fileName = null;
        uri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME};

        cursor = getApplicationContext().getContentResolver().query(uri, projection, null,
                null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            fileName = cursor.getString(column_index_folder_name);
            ImageModel imageModel = new ImageModel();
            imageModel.setFilePath(absolutePathOfImage);
            imageModel.setFileName(fileName);
            imageModel.setDuration(getDurationOfAudio(absolutePathOfImage));
            listOfAllImages.add(imageModel);
        }
        return listOfAllImages;
    }
    /**
     * This method gets the duration of the audio
     *
     * @param path
     * @return duration of audio
     */
    public String getDurationOfAudio(String path) {
        String durationTime = "";
        try {
            LOG.info("Audio local path: " + path);
            MediaPlayer mp = new MediaPlayer();
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mp.setDataSource(getApplicationContext(), Uri.parse(path));
            mp.prepare();
            int duration = mp.getDuration();
            durationTime = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes((long) duration),
                    TimeUnit.MILLISECONDS.toSeconds((long) duration) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) duration)));
            mp.release();
            LOG.info("audio duration: " + durationTime);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return durationTime;
    }
}
