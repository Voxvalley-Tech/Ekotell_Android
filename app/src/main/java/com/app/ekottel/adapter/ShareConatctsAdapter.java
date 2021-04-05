package com.app.ekottel.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.ekottel.R;
import com.app.ekottel.activity.ContactShareActivity;
import com.app.ekottel.model.ContactsModel;
import com.app.ekottel.utils.Utils;
import com.ca.Utils.CSDbFields;
import com.ca.wrapper.CSDataProvider;


import java.lang.ref.WeakReference;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShareConatctsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<ContactsModel> mContactList = new ArrayList<>();

    public ShareConatctsAdapter(Context context, ArrayList<ContactsModel> conactsList) {
        this.mContext = context;
        this.mContactList = conactsList;
    }


    public class ContactViewHolder extends RecyclerView.ViewHolder {
        CircleImageView conatctImage;
        ImageView selectedImage;
        TextView conatctNameTv, contactNumberTv;
        LinearLayout parentLayout;

        public ContactViewHolder(View itemView) {
            super(itemView);
            conatctNameTv = itemView.findViewById(R.id.contact_name_tv);
            parentLayout = itemView.findViewById(R.id.contact_parent_layout);
            selectedImage = itemView.findViewById(R.id.selected_image);
            contactNumberTv = itemView.findViewById(R.id.contact_number_tv);
            conatctImage = itemView.findViewById(R.id.contact_image);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.adapter_contact_share, parent, false);

        return new ContactViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ContactViewHolder) {
            final ContactViewHolder contactViewHolder = (ContactViewHolder) holder;
            final ContactsModel contactsModel = mContactList.get(position);
            Cursor cur = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER, contactsModel.getContactNumber());
            if (cur.getCount() > 0) {
                cur.moveToNext();
                String picid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_PROFILEPICID));
                Bitmap mybitmap = CSDataProvider.getImageBitmap(picid);
                if (mybitmap != null) {
                   // cModel.setProfilePicAvailable(true);
                    contactViewHolder.conatctImage.setImageBitmap(mybitmap);
                } else {
                    new ImageDownloaderTask(contactViewHolder.conatctImage).execute("app", picid, contactsModel.getConatctID());
                }


                //}
            }
            if (contactsModel.isContactSelected()) {
                contactViewHolder.parentLayout.setSelected(true);
                contactsModel.setContactSelected(true);
                contactViewHolder.selectedImage.setVisibility(View.VISIBLE);
            } else {
                contactViewHolder.parentLayout.setSelected(false);
                contactsModel.setContactSelected(false);
                contactViewHolder.selectedImage.setVisibility(View.GONE);
            }
            contactViewHolder.conatctNameTv.setText(contactsModel.getContactName());
            contactViewHolder.contactNumberTv.setText(contactsModel.getContactNumber());
            contactViewHolder.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (contactsModel.isContactSelected()) {
                        if (ContactShareActivity.selectedContactsList.contains(contactsModel)) {
                            ContactShareActivity.selectedContactsList.remove(contactsModel);
                        }
                        contactViewHolder.parentLayout.setSelected(false);
                        contactsModel.setContactSelected(false);
                        contactViewHolder.selectedImage.setVisibility(View.GONE);
                    } else {
                        if (!ContactShareActivity.selectedContactsList.contains(contactsModel)) {
                            ContactShareActivity.selectedContactsList.add(contactsModel);
                        }
                        contactViewHolder.parentLayout.setSelected(true);
                        contactsModel.setContactSelected(true);
                        contactViewHolder.selectedImage.setVisibility(View.VISIBLE);
                    }
                    if (ContactShareActivity.selectedContactsList.size() == 1) {
                        ContactShareActivity.mToolbar.setSubtitle(ContactShareActivity.selectedContactsList.size() + " Contact selected");
                    } else {
                        ContactShareActivity.mToolbar.setSubtitle(ContactShareActivity.selectedContactsList.size() + " Contacts selected");
                    }

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mContactList.size();
    }

    class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        boolean scaleit = false;

        public ImageDownloaderTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap photo = null;
            try {
                if (params[0].equals("app")) {
                    photo = CSDataProvider.getImageBitmap(params[1]);
                    if (photo == null) {
                        photo = Utils.loadContactPhoto(mContext, Long.parseLong(params[2]));
                    }
                } else if (params[0].equals("native")) {
                    photo = Utils.loadContactPhoto(mContext, Long.parseLong(params[1]));
                } else if (params[0].equals("group") && photo == null) {
                    photo = CSDataProvider.getImageBitmap(params[1]);
                }

                if (photo == null) {
                    photo = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_contact_avatar);
                }
            } catch (Exception e) {
                try {
                    if (photo == null) {
                        photo = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_contact_avatar);

                    }
                } catch (Exception ex) {
                }
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

}
