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
import com.app.ekottel.activity.SubscriberPackagesActivity;
import com.app.ekottel.activity.ViewAndBuyPackageActivity;
import com.app.ekottel.model.SubscribePackagesList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ramesh.u on 5/3/2018.
 */

public class DisplayPackagesAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private Context context;
    List<SubscribePackagesList> subscribePackagesLists=new ArrayList<SubscribePackagesList>();
    public ArrayList<Boolean> itemChecked = new ArrayList<Boolean>();

    public DisplayPackagesAdapter(Context context, List<SubscribePackagesList> subscribePackagesLists, int flags) {

        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.subscribePackagesLists = subscribePackagesLists;
        for (int i = 0; i < this.getCount(); i++) {
            itemChecked.add(i, false); // initializes all items value with false
        }
    }


    public int getCount() {
        return subscribePackagesLists.size();
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

            convertView = mInflater.inflate(R.layout.packages, null);
            holder = new ViewHolder();


            holder.title = (TextView) convertView.findViewById(R.id.comment_text);
            holder.ll_subscribe_package_item = (RelativeLayout) convertView.findViewById(R.id.ll_subscribe_package_item);

            holder.secondary = (TextView) convertView.findViewById(R.id.comment_title);

            holder.time = (TextView) convertView.findViewById(R.id.comment_by);

            holder.tv_max_minutes = (TextView) convertView.findViewById(R.id.tv_max_minutes);
            convertView.setTag(holder);
            String packageName = subscribePackagesLists.get(position).getPackageName();
            String cost = subscribePackagesLists.get(position).getCost();
            String validity = subscribePackagesLists.get(position).getValidity();

            String maxminutes = subscribePackagesLists.get(position).getMaxMinutes();


            holder.secondary.setText(packageName);
            holder.title.setText("$" + cost);
            holder.time.setText(context.getString(R.string.subscribe_package_validity)+" " + validity + " days");
            holder.tv_max_minutes.setText(context.getString(R.string.subscribe_package_max_min)+" " + maxminutes);

            holder.ll_subscribe_package_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!SubscriberPackagesActivity.isSubscribePkgClick) {
                        SubscriberPackagesActivity.isSubscribePkgClick = true;
                        Intent intentt = new Intent(context, ViewAndBuyPackageActivity.class);
                        intentt.putExtra("packageid", subscribePackagesLists.get(position).getId());
                        intentt.putExtra("packageName", subscribePackagesLists.get(position).getPackageName());
                        intentt.putExtra("packageCost", subscribePackagesLists.get(position).getCost());
                        intentt.putExtra("packageMaxMinutes", subscribePackagesLists.get(position).getMaxMinutes());
                        intentt.putExtra("packageValidity", subscribePackagesLists.get(position).getValidity());
                        context.startActivity(intentt);
                    }

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
        RelativeLayout ll_subscribe_package_item;
    }


}
