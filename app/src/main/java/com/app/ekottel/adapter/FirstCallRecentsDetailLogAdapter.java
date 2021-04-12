package com.app.ekottel.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;

import androidx.cursoradapter.widget.CursorAdapter;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.ekottel.R;
import com.app.ekottel.utils.Utils;
import com.ca.Utils.CSDbFields;
import com.ca.wrapper.CSDataProvider;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * This activity is used to display call logs details.
 *
 * @author Ramesh U
 * @version 2017
 */
public class FirstCallRecentsDetailLogAdapter extends CursorAdapter {

    private Context context;
    private long mLastClickTime = 0;
    private String callLogNumber;
    public FirstCallRecentsDetailLogAdapter(Context context, Cursor c, int flags,String calllLogNumber) {
        super(context, c, flags);
        this.context = context;
        this.callLogNumber=calllLogNumber;
    }

    @Override
    public void bindView(View convertView, Context context, Cursor cursor) {
        try {

            TextView title = (TextView) convertView.findViewById(R.id.text1);
            TextView secondary = (TextView) convertView.findViewById(R.id.text2);
            TextView dateView = (TextView) convertView.findViewById(R.id.dateView);
            TextView callTypeImg = convertView.findViewById(R.id.call_type_img);
            TextView durationTv = convertView.findViewById(R.id.call_duration_tv);
            LinearLayout dateLayout = convertView.findViewById(R.id.date_layout);
            RelativeLayout parentLayout = convertView.findViewById(R.id.linearLayout1);
            Typeface typeface = Utils.getTypeface(context);
            callTypeImg.setTypeface(typeface);
            String number = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_DIR));

            //  title.setText(number.toLowerCase());

            String prevDate = "";

            Long dateStr = cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_TIME));
            String currentDate = getFormattedDate(dateStr);
            if (cursor.getPosition() > 0 && cursor.moveToPrevious()) {
                prevDate = getFormattedDate(cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_TIME)));
                cursor.moveToNext();
            }


            if (number.contains(context.getString(R.string.call_logs_out_going_message))) {
                if (number.contains("VIDEO")) {
                    callTypeImg.setText(context.getResources().getString(R.string.video_call_icon));
                    callTypeImg.setTextColor(context.getResources().getColor(R.color.recents_outgoing_call));
                    title.setText("outgoing video call");
                } else {
                    callTypeImg.setText(context.getResources().getString(R.string.dialpad_call));
                    callTypeImg.setTextColor(context.getResources().getColor(R.color.recents_outgoing_call));
                    if (number.contains("OUTGOING PSTN CALL")) {
                        title.setText("outgoing pstn call");
                    } else {
                        title.setText("outgoing app call");
                    }
                    //title.setText(context.getString(R.string.call_logs_details_outgoing_audio));
                }


            } else if (number.contains(context.getString(R.string.call_logs_in_coming_message))) {
                if (number.contains("Video")) {
                    callTypeImg.setText(context.getResources().getString(R.string.video_call_icon));
                    callTypeImg.setTextColor(context.getResources().getColor(R.color.recents_incoming_call));
                    title.setText("incoming video call");
                } else {
                    callTypeImg.setText(context.getResources().getString(R.string.dialpad_call));
                    callTypeImg.setTextColor(context.getResources().getColor(R.color.recents_incoming_call));
                    title.setText("incoming app call");
                }

            } else if (number.contains("MISSED")) {
                if (number.contains("VIDEO")) {
                    callTypeImg.setText(context.getResources().getString(R.string.video_call_icon));
                    callTypeImg.setTextColor(context.getResources().getColor(R.color.recents_missed_call));
                    title.setText("missed video call");
                } else {
                    callTypeImg.setText(context.getResources().getString(R.string.dialpad_call));
                    callTypeImg.setTextColor(context.getResources().getColor(R.color.recents_missed_call));
                    title.setText("missed app call");
                }

            } else {
                if (number.contains("VIDEO")) {
                    callTypeImg.setText(context.getResources().getString(R.string.video_call_icon));
                    callTypeImg.setTextColor(context.getResources().getColor(R.color.recents_outgoing_call));
                    title.setText("outgoing video call");
                } else {
                    callTypeImg.setText(context.getResources().getString(R.string.dialpad_call));
                    callTypeImg.setTextColor(context.getResources().getColor(R.color.recents_outgoing_call));
                    if (number.contains("OUTGOING PSTN CALL")) {
                        title.setText("outgoing pstn call");
                    } else {
                        title.setText("outgoing app call");
                    }
                }
            }




           /* if (number.toLowerCase().contains("incoming")) {
                callTypeImg.setImageResource((R.drawable.incomingorange));
            } else if (number.toLowerCase().contains("outgoing")) {
                callTypeImg.setImageResource((R.drawable.outgoingcall));
            } else if (number.toLowerCase().contains("missed")) {
                callTypeImg.setImageResource((R.drawable.missedcall));
            }*/
            if (prevDate == null || !prevDate.equals(currentDate)) {
                dateView.setVisibility(View.VISIBLE);
                dateLayout.setVisibility(View.VISIBLE);
                if (DateUtils.isToday(dateStr)) {
                    dateView.setText("Today");
                } else if (isYesterday(dateStr)) {
                    dateView.setText("Yesterday");
                } else {
                    dateView.setText(currentDate);
                }
            } else {
                dateView.setVisibility(View.GONE);
                dateLayout.setVisibility(View.GONE);
            }


            String date = "";
            String time = "";
            String Duration = "";
            String cost = "";

            String formattedDate = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_TIME));
            long timme = Long.valueOf(formattedDate);

            date = new SimpleDateFormat("dd/MM/yyyy").format(timme);
            time = new SimpleDateFormat("hh:mm a").format(timme);

	/*
	if(DateUtils.isToday(timme)) {
		date = "Today";
	} else if(isYesterday(timme)) {
		date = "Yesterday";
	}
*/

            String ds1 = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_DURATION));
            int du1 = Integer.parseInt(ds1);

            int mins = (du1) / 60;
            int sec = (du1) % 60;

            if (mins == 0 && sec == 0) {
                Duration = "";
            } else if (mins == 0 && sec > 0) {
                if (sec == 1) {
                    Duration = sec + " second";
                } else {
                    Duration = sec + " seconds";
                }
            } else if (mins > 0 && sec == 0) {
                if (mins == 1) {
                    Duration = mins + " minute";
                } else {
                    Duration = mins + " minutes";
                }
            } else if (mins > 0 && sec > 0) {
                if (mins > 1 && sec == 1) {
                    Duration = mins + " minutes " + sec + " second";
                } else if (mins == 1 && sec > 1) {
                    Duration = mins + " minute " + sec + " seconds";
                } else {
                    Duration = mins + " minutes " + sec + " seconds";
                }
            }


            //String mysecondary = date + " " + time + " " + Duration;// + "|" + callcost;
            String mysecondary = time;


            secondary.setText(mysecondary);
            durationTv.setText(Duration);

            try {
                //String direction = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_DIR));
                //if(direction.contains("MISSED")) {
                int isalreadynotified = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_MISSEDCALL_NOTIFIED));
                if (isalreadynotified != 1) {
                    String callid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_CALLID));
                   // CSDataProvider.UpdateCallLogbyCallidAndFilter(callid, CSDbFields.KEY_CALLLOG_MISSEDCALL_NOTIFIED, 1);
                    CSDataProvider.updateCallLogbyCallidAndFilter(callid, CSDbFields.KEY_CALLLOG_MISSEDCALL_NOTIFIED, 1);
                        //CSDataProvider.updateCallLogbyCallidAndFilter(callid, CSDbFields.KEY_CALLLOG_MISSEDCALL_NOTIFIED, 1);
                }
                //}
            } catch (Exception ex) {

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View convertView = inflater.inflate(R.layout.firstcall_contact_row_layout1, parent, false);

        return convertView;
    }

    public static boolean isYesterday(long date) {
        Calendar now = Calendar.getInstance();
        Calendar cdate = Calendar.getInstance();
        cdate.setTimeInMillis(date);
        now.add(Calendar.DATE, -1);
        return now.get(Calendar.YEAR) == cdate.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == cdate.get(Calendar.MONTH)
                && now.get(Calendar.DATE) == cdate.get(Calendar.DATE);
    }

    private String getFormattedDate(long dateStr) {
        try {
            return new SimpleDateFormat("dd-MM-yyyy").format(dateStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getFormattedTime1(long dateStr) {

        try {
            return new SimpleDateFormat("hh:mm a").format(dateStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}