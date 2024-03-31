package com.serveterdogan.javamaps.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.serveterdogan.javamaps.databinding.ReceyclerRowBinding;
import com.serveterdogan.javamaps.model.Place;
import com.serveterdogan.javamaps.view.MapsActivity;

import java.util.List;

public class PlaceAdaptor  extends RecyclerView.Adapter<PlaceAdaptor.PlaceHolder> {

    List<Place> placeList;

    public PlaceAdaptor(List<Place> placeList){

        this.placeList = placeList;
    }

    @NonNull
    @Override
    public PlaceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ReceyclerRowBinding receyclerRowBinding = ReceyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new PlaceHolder(receyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.receyclerRowBinding.recyclerText.setText(placeList.get(position).name);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(), MapsActivity.class);
                intent.putExtra("info","old");

                intent.putExtra("place",placeList.get(position));
                holder.itemView.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public static class PlaceHolder extends  RecyclerView.ViewHolder{

        ReceyclerRowBinding receyclerRowBinding;
        public PlaceHolder(ReceyclerRowBinding receyclerRowBinding) {
            super(receyclerRowBinding.getRoot());
           this.receyclerRowBinding = receyclerRowBinding;

        }
    }

}
