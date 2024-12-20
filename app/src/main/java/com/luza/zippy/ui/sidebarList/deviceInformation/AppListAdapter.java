package com.luza.zippy.ui.sidebarList.deviceInformation;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.luza.zippy.MainActivity;
import com.luza.zippy.R;
import java.util.ArrayList;
import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    
    private List<ApplicationInfo> apps = new ArrayList<>();
    private PackageManager packageManager;

    public AppListAdapter(PackageManager pm) {
        this.packageManager = pm;
    }

    public void setData(List<ApplicationInfo> newData) {
        apps.clear();
        apps.addAll(newData);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).textView.setText(
                String.format(MainActivity.mContext.getString(R.string.device_do_app_num) + "：%d", apps.size()));
        } else if (holder instanceof ItemViewHolder) {
            ApplicationInfo app = apps.get(position - 1);
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            itemHolder.textView.setText(packageManager.getApplicationLabel(app));
            itemHolder.imageView.setImageDrawable(packageManager.getApplicationIcon(app));
        }
    }

    @Override
    public int getItemCount() {
        return apps.size() + 1; // +1 for header
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        HeaderViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.header_text);
    }
}

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        ItemViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.app_icon);
            textView = view.findViewById(R.id.app_name);
        }
    }
} 