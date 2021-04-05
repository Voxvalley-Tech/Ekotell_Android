package com.app.ekottel.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.app.ekottel.R;
import com.app.ekottel.activity.ViewMyPackageActivity;
import com.app.ekottel.model.MyPackagesList;

import java.util.ArrayList;
import java.util.List;

/**
 * This activity is used to display my packages.
 *
 * @author Ramesh U
 * @version 2017
 */
public class DisplayMyPackagesAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private Context context;
    List<MyPackagesList> myPackagesLists = new ArrayList<MyPackagesList>();
    public ArrayList<Boolean> itemChecked = new ArrayList<Boolean>();

    public DisplayMyPackagesAdapter(Context context, List<MyPackagesList> myPackagesLists, int flags) {

        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.myPackagesLists = myPackagesLists;

    }


    public int getCount() {
        return myPackagesLists.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        try {
            final ViewHolder holder;

            convertView = mInflater.inflate(R.layout.mypackages, null);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.comment_text);
            holder.rl_my_package_item = (RelativeLayout) convertView.findViewById(R.id.rl_my_package_item);
            holder.secondary = (TextView) convertView.findViewById(R.id.comment_title);

            holder.time = (TextView) convertView.findViewById(R.id.comment_by);

            holder.tv_max_minutes = (TextView) convertView.findViewById(R.id.tv_max_minutes);

            convertView.setTag(holder);

            String packageName = myPackagesLists.get(position).getPackageName();
            String cost = myPackagesLists.get(position).getCost();
            String expiry = myPackagesLists.get(position).getExpiry();
            String remainingMinutes = myPackagesLists.get(position).getRemainingMinutes();


            holder.secondary.setText(packageName);
            holder.title.setText("$" + cost);
            holder.time.setText(context.getString(R.string.my_packages_expiry) + " " + expiry);
            holder.tv_max_minutes.setText(context.getString(R.string.my_packages_rem_min) + " " + remainingMinutes);
            holder.rl_my_package_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    Intent intentt = new Intent(context, ViewMyPackageActivity.class);
                    intentt.putExtra("packageid", myPackagesLists.get(position).getId());
                    intentt.putExtra("packageName", myPackagesLists.get(position).getPackageName());
                    intentt.putExtra("packageCost", myPackagesLists.get(position).getCost());
                    intentt.putExtra("packageExpiry", myPackagesLists.get(position).getExpiry());
                    intentt.putExtra("packageRemMinutes", myPackagesLists.get(position).getRemainingMinutes());
                    context.startActivity(intentt);


                }
            });


        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return convertView;
    }


    static class ViewHolder {

        TextView title;
        TextView secondary;
        TextView time;
        TextView tv_max_minutes;
        RelativeLayout rl_my_package_item;
    }

}