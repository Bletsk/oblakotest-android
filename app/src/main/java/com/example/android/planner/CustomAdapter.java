package com.example.android.planner;

import android.graphics.Paint;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import java.util.ArrayList;
import java.util.TreeSet;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.google.gson.JsonObject;

class CustomAdapter extends BaseAdapter {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;

    private ArrayList<Todo> mData = new ArrayList<Todo>();
    private TreeSet<Integer> sectionHeader = new TreeSet<Integer>();

    private LayoutInflater mInflater;

    CompoundButton.OnCheckedChangeListener cbListener;

    public CustomAdapter(Context context) {
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addItem(Todo _todo) {
        mData.add(_todo);
        notifyDataSetChanged();
    }

    public void addSectionHeaderItem(String item) {
        mData.add(new Todo(-1, item, false, -1));
        sectionHeader.add(mData.size() - 1);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return sectionHeader.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Todo getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        Todo todo;
        int rowType = getItemViewType(position);

        if (convertView == null) {
            holder = new ViewHolder();
            switch (rowType) {
                case TYPE_SEPARATOR:
                    convertView = mInflater.inflate(R.layout.list_header, null);
                    holder.textView = (TextView) convertView.findViewById(R.id.textSeparator);
                    break;
                case TYPE_ITEM:
                    convertView = mInflater.inflate(R.layout.list_cell, null);
                    holder.textView = (TextView) convertView.findViewById(R.id.text);
                    holder.cb = (CheckBox) convertView.findViewById(R.id.cb);

                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            holder.cb.toggle();
                        }
                    });

                    holder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            JsonObject params;
                            final Todo todoInFocus = (Todo) buttonView.getTag();

                            if (todoInFocus.isCompleted == isChecked) return;

                            todoInFocus.isCompleted = !todoInFocus.isCompleted;
                            buttonView.setTag(todoInFocus);
                            if(todoInFocus.isCompleted) holder.textView.setPaintFlags(holder.textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            else holder.textView.setPaintFlags(holder.textView.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));

                            params = new JsonObject();
                            //params.addProperty("id", todoInFocus.id);
                            //params.addProperty("isCompleted", todoInFocus.isCompleted);
                            Ion.with(Index.context).load("PUT",Index.context.getResources().getString(R.string.server_address) + "todos/" + todoInFocus.id)
                                    .setJsonObjectBody(params)
                                    .asJsonObject()
                                    .setCallback(new FutureCallback<JsonObject>() {
                                        @Override
                                        public void onCompleted(Exception e, JsonObject result) {
                                            Log.i("Sending JSON", Index.context.getResources().getString(R.string.server_address) + "todos/" + todoInFocus.id + " - Complete");
                                    }
                                    });
                        }
                    });
                    break;
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        todo = mData.get(position);
        holder.textView.setText(todo.text);
        if(holder.cb != null) {
            holder.cb.setTag(todo);
            holder.cb.setChecked(todo.isCompleted);
            if(todo.isCompleted) holder.textView.setPaintFlags(holder.textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            else holder.textView.setPaintFlags(holder.textView.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
        }

        return convertView;
    }

    public static class ViewHolder {
        public TextView textView;
        public CheckBox cb;
    }
}