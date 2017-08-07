package com.uzumaki.naruto.filemanager.views.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.uzumaki.naruto.filemanager.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Bhavik Bist on 06-08-2017.
 */

public class CustomAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<HashMap<String, String>> list;
    private LayoutInflater inflater;

    public CustomAdapter(Context mContext, ArrayList<HashMap<String, String>> list) {
        this.mContext = mContext;
        this.list = list;
        inflater = ((Activity) mContext).getLayoutInflater();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public long getItemId(int pos) {
        return list.get(pos).hashCode();
    }

    @Override
    public HashMap<String, String> getItem(int pos) {
        return list.get(pos);
    }

    public void updateSet(ArrayList<HashMap<String, String>> hashMapArrayList) {
        this.list = hashMapArrayList;
        this.notifyDataSetChanged();
    }

    private class ViewHolder {
        ImageView type_icon;
        TextView tv_name, tv_size;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.list_item, parent, false);

            holder.type_icon = (ImageView) convertView.findViewById(R.id.type_icon);
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            holder.tv_size = (TextView) convertView.findViewById(R.id.tv_size);

            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();


        HashMap<String, String> hash = getItem(pos);

        if (hash.get("type").equals("folder")) {
            holder.type_icon.setImageResource(R.drawable.ic_app_icon);
            holder.tv_size.setText(hash.get("count")
                    + (Integer.parseInt(hash.get("count")) <= 1 ? " file" : " files"));
        } else {
            if (hash.get("name").toLowerCase().contains(".jpg")
                    || hash.get("name").toLowerCase().contains(".png")) {

                setImage(hash.get("path"), holder.type_icon);
                holder.type_icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                holder.type_icon.setImageResource(R.drawable.ic_file);
            }
            holder.tv_size.setText(Integer.parseInt(hash.get("size")) >= 1024
                    ? (String.format("%.2f", Double.parseDouble(hash.get("size")) / 1024) + " Mb")
                    : hash.get("size") + " kb");
        }

        holder.tv_name.setText(hash.get("name"));

        return convertView;
    }

    private void setImage(String imgUrl, ImageView type_icon) {
        Glide.with(mContext.getApplicationContext())
                .load(imgUrl)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_file)
                .into(type_icon);
    }
}
