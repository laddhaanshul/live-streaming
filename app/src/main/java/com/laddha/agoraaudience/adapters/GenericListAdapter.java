package com.laddha.agoraaudience.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.laddha.agoraaudience.R;
import com.laddha.agoraaudience.model.CommentData;

import java.util.List;

public class GenericListAdapter extends BaseAdapter {

    private Context context;
    private List<CommentData> data;

    public GenericListAdapter(Context context, List<CommentData> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        CommentData commentData = data.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).
                    inflate(R.layout.layout_list, parent, false);
        }

        TextView textViewItemName = (TextView)
                convertView.findViewById(R.id.data_txt);

        textViewItemName.setText(commentData.getUserName() + " : " + commentData.getCommentMsg());

        return convertView;
    }
}
