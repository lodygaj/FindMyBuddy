package com.lodygaj.findmybuddy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Joey Laptop on 1/4/2016.
 */
public class ContactAdapter extends BaseAdapter {
    Context context;
    String[] contacts;
    LayoutInflater inflater;

    public ContactAdapter(Context context, String[] contacts) {
        this.context = context;
        this.contacts = contacts;
        inflater = (LayoutInflater) this.context.getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return contacts.length;
    }

    public Object getItem(int position) {
        return contacts[position];
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.user_view_item, null);
            viewHolder = new ViewHolder();
            viewHolder.textViewUser = (TextView) convertView.findViewById(R.id.textViewUser);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.textViewUser.setText(contacts[position]);
        return convertView;
    }

    private static class ViewHolder {
        public TextView textViewUser;
    }
}
