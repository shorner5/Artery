package com.stuhorner.drawingsample;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import java.util.Random;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ItemsViewHolder>{
    int colors[] = {R.color.green, R.color.orange,  R.color.red,  R.color.purple,  R.color.blue,  R.color.deepBlue,  R.color.brown,  R.color.pink,  R.color.colorAccent};

    public static class ItemsViewHolder extends RecyclerView.ViewHolder{
        TextView personName;
        TextView subtitle;
        ImageView icon;

        ItemsViewHolder(View itemView) {
            super(itemView);
            personName = (TextView)itemView.findViewById(R.id.chat_name);
            subtitle = (TextView)itemView.findViewById(R.id.chat_subtitle);
            icon = (ImageView) itemView.findViewById(R.id.chat_icon);
        }
    }

    List<ChatRow> chatRows;
    private Context context;

    public ChatAdapter(List<ChatRow> chatRows, Context context) {
        this.chatRows = chatRows;
        this.context = context;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
    @Override
    public ItemsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_item, viewGroup, false);
        return new ItemsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ItemsViewHolder itemViewHolder, int i) {
        itemViewHolder.personName.setText(chatRows.get(i).getName());
        itemViewHolder.subtitle.setText(chatRows.get(i).getSubtitle());
        if (chatRows.get(i).isNewMessage()) {
            itemViewHolder.personName.setTypeface(null, Typeface.BOLD);
            itemViewHolder.subtitle.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        }
        else {
            itemViewHolder.personName.setTypeface(null, Typeface.NORMAL);
            itemViewHolder.subtitle.setTextColor(context.getResources().getColor(R.color.lightGray));
        }

        int index = chatRows.get(i).getName().length() % 9;
        itemViewHolder.icon.setColorFilter(context.getResources().getColor(colors[index]));
    }

    @Override
    public int getItemCount() {
        return chatRows.size();
    }

}
