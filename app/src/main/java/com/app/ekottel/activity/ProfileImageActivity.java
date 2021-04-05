package com.app.ekottel.activity;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import static com.app.ekottel.utils.GlobalVariables.LOG;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.app.ekottel.R;
import com.app.ekottel.utils.Utils;
import com.ca.Utils.CSDbFields;
import com.ca.wrapper.CSDataProvider;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class ProfileImageActivity extends AppCompatActivity {
    private LinearLayout mBackImageLayout;
    private ImageView mBackImageTv;
    private ImageView mProfileImageImg;
    private String mContactNumber = "";
    private boolean isSelfProfilePic = false;
    private String TAG = "ProfileImageActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_image);
        mBackImageLayout = findViewById(R.id.ll_profile_screen_back);
        mProfileImageImg = findViewById(R.id.profile_image_img);
        mBackImageTv = findViewById(R.id.profile_screen_back);
        mContactNumber = getIntent().getStringExtra("profileContactNumber");
        isSelfProfilePic = getIntent().getBooleanExtra("isSelfProfile", false);
        Typeface typeface = Utils.getTypeface(getApplicationContext());
        //  mBackImageTv.setTypeface(typeface);


        /*if (getIntent().getByteArrayExtra("image") != null) {
            byte[] byteArray = getIntent().getByteArrayExtra("image");
            Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            mProfileImageImg.setImageBitmap(BITMAP_RESIZER(bmp));
        } else {*/
        if (isSelfProfilePic) {
            Cursor cur = CSDataProvider.getSelfProfileCursor();
            if (cur.getCount() > 0) {
                cur.moveToNext();
                String imageid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_SELF_PROFILE_PROFILEPICID));
                Bitmap mybitmap = CSDataProvider.getImageBitmap(imageid);
                String filePath = CSDataProvider.getImageFilePath(imageid);
                if (mybitmap != null) {
                    try {
                        mProfileImageImg.setImageBitmap(null);
                       // mProfileImageImg.setImageBitmap(mybitmap);
                        mProfileImageImg.setImageBitmap(BitmapFactory.decodeFile(filePath));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            loadProfileImage();
        }

        //  }

        mBackImageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void loadProfileImage() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Cursor cur = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER, mContactNumber);
                    LOG.info("cursor count" + cur.getCount());
                    if (cur.getCount() > 0) {
                        cur.moveToNext();
                        String picid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_PROFILEPICID));
                        Bitmap mybitmap = CSDataProvider.getImageBitmap(picid);
                        String filePath = CSDataProvider.getImageFilePath(picid);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mybitmap != null) {
                                  //  mProfileImageImg.setImageBitmap(mybitmap);
                                    mProfileImageImg.setImageBitmap(BitmapFactory.decodeFile(filePath));
                                } else {
                                    Bitmap contactbitmap = Utils.getContactImage(getApplicationContext(),
                                            mContactNumber);
                                    if (contactbitmap != null) {
                                        mProfileImageImg.setImageBitmap(contactbitmap);
                                    } else {
                                        mProfileImageImg.setImageResource(R.mipmap.ic_contact_avatar);
                                    }
                                }
                            }
                        });

                    } else {
                        Bitmap contactbitmap = Utils.getContactImage(getApplicationContext(),
                                mContactNumber);
                        if (contactbitmap != null) {
                            mProfileImageImg.setImageBitmap(contactbitmap);
                        } else {
                            mProfileImageImg.setImageResource(R.mipmap.ic_contact_avatar);
                        }
                    }
                    cur.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }


            }
        }).start();
    }

    class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;

        public ImageDownloaderTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap photo;
            try {
                photo = retrieveContactPhoto(params[0]);
                if (photo == null) {
                    photo = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_contact_avatar);
                }
            } catch (Exception e) {
                photo = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_contact_avatar);
                e.printStackTrace();
            }
            return photo;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }
            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }
        }
    }

    public Bitmap BITMAP_RESIZER(Bitmap bitmap) {

        Bitmap scaledBitmap=null;
        if (bitmap != null) {
            int h = bitmap.getHeight();
            int w = bitmap.getWidth();
            int newWidth = 0;
            int newHeight = 0;

            if (h > w) {
                newWidth = 600;
                newHeight = 800;
            }

            if (w > h) {
                newWidth = 800;
                newHeight = 600;
            }

            float scaleWidth = ((float) newWidth) / w;
            float scaleHeight = ((float) newHeight) / h;


            Matrix matrix = new Matrix();
            // resize the bit map
            matrix.preScale(scaleWidth, scaleHeight);

            // resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);


             scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

            float ratioX = newWidth / (float) bitmap.getWidth();
            float ratioY = newHeight / (float) bitmap.getHeight();
            float middleX = newWidth / 2.0f;
            float middleY = newHeight / 2.0f;

            Matrix scaleMatrix = new Matrix();
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

            Canvas canvas = new Canvas(scaledBitmap);
            canvas.setMatrix(scaleMatrix);
            canvas.drawBitmap(bitmap, middleX - bitmap.getWidth() / 2, middleY - bitmap.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));


        }
        return scaledBitmap;
    }

    public Bitmap retrieveContactPhoto(String number) {
        ContentResolver contentResolver = getContentResolver();
        String contactId = null;
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID};

        Cursor cursor =
                contentResolver.query(
                        uri,
                        projection,
                        null,
                        null,
                        null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
            }
            cursor.close();
        }

        Bitmap photo = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_contact_avatar);

        try {
            InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(),
                    ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(contactId)));

            if (inputStream != null) {
                photo = BitmapFactory.decodeStream(inputStream);
            }

            assert inputStream != null;
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return photo;
    }

    public Bitmap getResizedBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap resizedBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        float scaleX = newWidth / (float) bitmap.getWidth();
        float scaleY = newHeight / (float) bitmap.getHeight();
        float pivotX = 0;
        float pivotY = 0;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX, scaleY, pivotX, pivotY);

        Canvas canvas = new Canvas(resizedBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));

        return resizedBitmap;
    }

    public Bitmap resizeBitmap(Bitmap bitmap) {
        Canvas canvas = new Canvas();
        Bitmap resizedBitmap = null;
        if (bitmap != null) {
            int h = bitmap.getHeight();
            int w = bitmap.getWidth();
            int newWidth = 0;
            int newHeight = 0;

            if (h > w) {
                newWidth = 600;
                newHeight = 800;
            }

            if (w > h) {
                newWidth = 800;
                newHeight = 600;
            }

            float scaleWidth = ((float) newWidth) / w;
            float scaleHeight = ((float) newHeight) / h;


            Matrix matrix = new Matrix();
            // resize the bit map
            matrix.preScale(scaleWidth, scaleHeight);

            resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);


            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);
            paint.setDither(true);

            canvas.drawBitmap(resizedBitmap, matrix, paint);


        }


        return resizedBitmap;
    }
}
