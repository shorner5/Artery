package com.stuhorner.drawingsample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

/**
 * Created by Stu on 3/29/2016.
 */
public class CardAdapter extends BaseAdapter {
    private List<OtherUser> users;
    List<Integer> drawings, userNames, userAges;
    Context context;
    private static LayoutInflater inflater;

    public CardAdapter(Context context, List<OtherUser> users) {
        this.users = users;
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public int getCount(){
        return userNames.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class Holder {
        TextView name;
        ImageView img;
    }

    @Override
    public View getView(final int position, View v, ViewGroup parent) {
        View view;

        if (v == null) { // if it's not recycled, initialize some attributes
            view = new View(context);
            view = inflater.inflate(R.layout.card_layout, parent, false);
        } else {
            view = (View) v;
        }
        Holder holder = new Holder();
        holder.name = (TextView) view.findViewById(R.id.card_name);
        holder.img = (ImageView) view.findViewById(R.id.card_drawing);
        holder.name.setText(String.format(context.getResources().getString(R.string.card_title), userNames.get(position), Integer.toString(userAges.get(position))));
        holder.img.setImageResource(R.drawable.example_drawing);

        return view;
    }
}