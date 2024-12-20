package com.luza.zippy.ui.sidebarList.deviceInformation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.luza.zippy.MainActivity;
import com.luza.zippy.R;
import java.util.ArrayList;
import java.util.List;

public class InfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    
    private List<String> data = new ArrayList<>();
    private String headerText;

    public void setData(List<String> newData) {
        this.data.clear();
        if (newData != null) {
            this.data.addAll(newData);
        }
        notifyDataSetChanged();
    }

    public void setHeaderText(String text) {
        this.headerText = text;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (headerText != null && position == 0) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
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
                .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ItemViewHolder(view);
        }
    }

    private boolean isHeader(String text) {
        return text.equals(MainActivity.mContext.getString(R.string.device_info_basic)) ||
               text.equals(MainActivity.mContext.getString(R.string.device_info_system)) ||
               text.equals(MainActivity.mContext.getString(R.string.device_info_hardware)) ||
               text.equals(MainActivity.mContext.getString(R.string.device_info_network));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        try {
            if (holder instanceof HeaderViewHolder && headerText != null) {
                ((HeaderViewHolder) holder).textView.setText(headerText);
            } else if (holder instanceof ItemViewHolder) {
                int dataPosition = headerText != null ? position - 1 : position;
                if (dataPosition >= 0 && dataPosition < data.size()) {
                    String text = data.get(dataPosition);
                    ItemViewHolder itemHolder = (ItemViewHolder) holder;
                    itemHolder.textView.setText(text);
                    
                    // 设置标题行样式
                    if (isHeader(text)) {
                        itemHolder.textView.setTextSize(20); // 更大的字体
                        itemHolder.textView.setTextColor(itemHolder.textView.getContext()
                            .getResources().getColor(R.color.blue_500)); // 蓝色文字
                        itemHolder.textView.setTypeface(null, android.graphics.Typeface.BOLD); // 加粗
                        itemHolder.textView.setBackgroundResource(R.drawable.bg_info_header);
                        itemHolder.textView.setPadding(32, 24, 32, 24); // 更大的内边距
                    } else {
                        itemHolder.textView.setTextSize(16); // 普通字体大小
                        itemHolder.textView.setTextColor(itemHolder.textView.getContext()
                            .getResources().getColor(R.color.text_light)); // 使用text_light替代text_color
                        itemHolder.textView.setTypeface(null, android.graphics.Typeface.NORMAL); // 正常字重
                        itemHolder.textView.setBackgroundResource(0);
                        itemHolder.textView.setPadding(32, 16, 32, 16); // 普通内边距
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        int headerCount = headerText != null ? 1 : 0;
        return data.size() + headerCount;
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        HeaderViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.header_text);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ItemViewHolder(View view) {
            super(view);
            textView = view.findViewById(android.R.id.text1);
        }
    }
} 