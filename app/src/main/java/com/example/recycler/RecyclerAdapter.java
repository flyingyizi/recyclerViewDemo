package com.example.recycler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.image.Photo;
import com.example.recyclerviewdemo.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.PhotoHolder> {
    private final ArrayList<Photo> photos;

    public RecyclerAdapter(ArrayList<Photo> photos) {
        this.photos = photos;
    }

    @NonNull
    @Override
    public PhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //负责承载每个item的布局
        View inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item_row,parent,false);
        return new PhotoHolder(inflated);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {
        //负责将每个item holder绑定数据
        Photo itemPhoto = photos.get(position);
        holder.bindPhoto(itemPhoto);
    }

    @Override
    public int getItemCount() {
        return  photos.size();
    }

    class PhotoHolder extends RecyclerView.ViewHolder {
        private  View itemView;

        public PhotoHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
        }
        public void bindPhoto(Photo photo) {
            ImageView row_itemImage = (ImageView)itemView.findViewById(R.id.row_itemImage);
            TextView row_itemDate = (TextView) itemView.findViewById(R.id.row_itemDate);
            TextView row_itemDescription = (TextView) itemView.findViewById(R.id.row_itemDescription);

            Picasso.with(itemView.getContext()).load(photo.getUrl()).into(row_itemImage);
            row_itemDate.setText( photo.getDate() );
            row_itemDescription.setText( photo.getExplanation());
        }
    }
}
