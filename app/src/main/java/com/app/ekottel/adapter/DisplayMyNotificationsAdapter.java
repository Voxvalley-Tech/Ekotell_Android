package com.app.ekottel.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import androidx.cursoradapter.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.ekottel.R;
import com.ca.Utils.CSDbFields;


public class DisplayMyNotificationsAdapter extends CursorAdapter {

    private Context context;

    public DisplayMyNotificationsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        this.context = context;

    }

    @Override
    public void bindView(View convertView, Context context, Cursor cursor) {
        TextView title = (TextView) convertView.findViewById(R.id.comment_title);

        title.setText(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_PROMOTIONAL_MESSAGEMESSAGE)));
        String message_read=cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_PROMOTIONAL_MESSAGEREAD));

        if(message_read!=null && message_read.equalsIgnoreCase("1")){
            title.setTextColor(Color.parseColor("#b2b2b2"));
        }else{
            title.setTextColor(Color.parseColor("#B3000000"));
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.promotions, parent, false);
    }


}