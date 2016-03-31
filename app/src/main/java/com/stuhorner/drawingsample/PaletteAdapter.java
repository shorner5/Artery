package com.stuhorner.drawingsample;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Stu on 3/25/2016.
 */
public class PaletteAdapter extends RecyclerView.Adapter<PaletteAdapter.MyViewHolder>{
    private List<Integer> colors;
    private List<Integer> radii;
    private final Context mContext;

    public PaletteAdapter(List<Integer> colors, List<Integer> radii, Context context){
        this.colors = colors;
        this.radii = radii;
        this.mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.color_layout, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, int i) {
        myViewHolder.view.setColorFilter(colors.get(i));
        myViewHolder.view.setLayoutParams(new LinearLayout.LayoutParams(
                (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radii.get(i), mContext.getResources().getDisplayMetrics()),
                (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radii.get(i), mContext.getResources().getDisplayMetrics())));
    }

    public int getItemCount() {
        return colors == null ? 0 : colors.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView view;

        public MyViewHolder(View itemView) {
            super(itemView);

            view = (ImageView) itemView.findViewById(R.id.palette_color);
        }
    }

}
