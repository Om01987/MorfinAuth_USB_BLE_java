package com.mantra.morfinauthsample.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mantra.morfinauth.ble.model.MorfinBleDevice;
import com.mantra.morfinauthsample.R;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(MorfinBleDevice device);
    }

    private List<MorfinBleDevice> devices;
    private OnItemClickListener listener;

    public RecyclerViewAdapter(List<MorfinBleDevice> devices, OnItemClickListener listener) {
        this.devices = devices;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.btle_device_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MorfinBleDevice device = devices.get(position);
        Log.e("onBindViewHolder","onBindViewHolder : "+device.name+"");
        holder.txtName.setText(device.name); // Or any info you want to show
        holder.itemView.setOnClickListener(v -> listener.onItemClick(device));
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.Devicename_tv);
        }
    }
}
