package com.luza.zippy.ui.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.luza.zippy.R;
import com.luza.zippy.setting.ShardPerfenceSetting;
import com.luza.zippy.ui.utils.ColorCalibration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ColorDrawable;
import androidx.cardview.widget.CardView;

public class BottomMenuAdapter extends RecyclerView.Adapter<BottomMenuAdapter.MenuViewHolder> {

    public static class MenuItem {
        public int id;
        public int iconRes;
        public String title;
        public boolean isFavorite;

        public MenuItem(int id, int iconRes, String title) {
            this.id = id;
            this.iconRes = iconRes;
            this.title = title;
            this.isFavorite = false;
        }
    }

    private final List<MenuItem> menuItems;
    private final OnMenuItemClickListener listener;
    private final Context context;
    private final ShardPerfenceSetting shardPerfenceSetting;

    public interface OnMenuItemClickListener {
        void onMenuItemClick(MenuItem item);
    }

    public BottomMenuAdapter(Context context, List<MenuItem> menuItems, OnMenuItemClickListener listener) {
        this.context = context;
        this.menuItems = new ArrayList<>(menuItems);
        this.listener = listener;
        this.shardPerfenceSetting = ShardPerfenceSetting.getInstance(context);
        
        loadFavoriteStates();
        sortMenuItems();
    }

    private void loadFavoriteStates() {
        for (MenuItem item : menuItems) {
            item.isFavorite = shardPerfenceSetting.isMenuItemFavorite(item.id);
        }
    }

    private void sortMenuItems() {
        Collections.sort(menuItems, (item1, item2) -> {
            if (item1.isFavorite && !item2.isFavorite) return -1;
            if (!item1.isFavorite && item2.isFavorite) return 1;
            return 0;
        });
    }

    @Override
    public int getItemViewType(int position) {
        return shardPerfenceSetting.isBottom_menu_bkg() ? 1 : 0;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            // 卡片式布局
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_bottom_menu_card, parent, false);
        } else {
            // 普通布局
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_bottom_menu_normal, parent, false);
        }
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        MenuItem item = menuItems.get(position);
        holder.icon.setImageResource(item.iconRes);
        holder.title.setText(item.title);

        // 动态设置菜单项大小
        int menuItemSize = shardPerfenceSetting.getBottomMenuItemSize();
        android.util.Log.d("liziluo", "BottomMenuAdapter menuItemSize: " + menuItemSize);
        
        // 设置卡片大小
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        int dpSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, menuItemSize, context.getResources().getDisplayMetrics());
        layoutParams.width = dpSize;
        layoutParams.height = dpSize;
        holder.itemView.setLayoutParams(layoutParams);
        
        // 根据菜单项大小调整图标大小 (20-48dp 对应 60-120dp)
        int iconSize = (int) (20 + (menuItemSize - 60) * 0.467f); // 线性映射到20-48dp
        ViewGroup.LayoutParams iconParams = holder.icon.getLayoutParams();
        int iconSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, iconSize, context.getResources().getDisplayMetrics());
        iconParams.width = iconSizePx;
        iconParams.height = iconSizePx;
        holder.icon.setLayoutParams(iconParams);
        
        // 根据菜单项大小调整文字大小 (10-16sp 对应 60-120dp)
        float textSize = 10 + (menuItemSize - 60) * 0.1f; // 线性映射到10-16sp
        holder.title.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);

        String theme = shardPerfenceSetting.getHomeTheme();
        String themeColor = ColorCalibration.getThemeColor(theme);
        
        if (item.isFavorite) {

            int color = Color.parseColor(themeColor);
            
            float[] hsv = new float[3];
            Color.colorToHSV(color, hsv);
            hsv[2] = hsv[2] * 0.7f;
            int darkerColor = Color.HSVToColor(hsv);

            if (getItemViewType(position) == 1) {
                // 卡片式布局的收藏项样式
                ((CardView) holder.itemView).setCardBackgroundColor(Color.parseColor(ColorCalibration.getThemeColorType(theme,3)));
            }
            
            holder.icon.setColorFilter(darkerColor);
            holder.title.setTextColor(darkerColor);
            holder.icon.setAlpha(1.0f);
            holder.title.setAlpha(1.0f);
        } else {
            if (getItemViewType(position) == 1) {
                // 卡片式布局的普通项样式
                ((CardView) holder.itemView).setCardBackgroundColor(Color.parseColor(ColorCalibration.getThemeColorType(theme,4)));
            }
            
            holder.icon.setColorFilter(null);
            holder.title.setTextColor(context.getResources().getColor(android.R.color.tab_indicator_text));
            holder.icon.setAlpha(0.8f);
            holder.title.setAlpha(0.8f);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMenuItemClick(item);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            showFavoritePopup(v, item);
            return true;
        });
    }

    private void showFavoritePopup(View anchorView, MenuItem item) {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popup_favorite, null);
        TextView popupText = popupView.findViewById(R.id.popup_text);
        
        String themeColor = ColorCalibration.getThemeColor(shardPerfenceSetting.getHomeTheme());
        int color = Color.parseColor(themeColor);
        
        // 创建带边框的背景
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setColor(Color.WHITE); //弹出收藏按钮的气泡背景颜色
        shape.setCornerRadius(context.getResources().getDimension(R.dimen.card_corner_radius));
        shape.setStroke(
            (int) context.getResources().getDimension(R.dimen.card_stroke_width),
            color
        );
        
        // 设置阴影背景
        popupView.setBackground(shape);
        popupView.setElevation(context.getResources().getDimension(R.dimen.card_elevation_selected));
        
        String text = item.isFavorite ? 
            context.getString(R.string.remove_from_favorites) : 
            context.getString(R.string.add_to_favorites);
        popupText.setText(text);
        
        int iconRes = item.isFavorite ? R.drawable.ic_star_filled : R.drawable.ic_star_border;
        popupText.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0);
        popupText.setCompoundDrawablePadding(16);
        popupText.setTextColor(color);
        Drawable[] drawables = popupText.getCompoundDrawables();
        if (drawables[0] != null) {
            drawables[0].setTint(color);
        }

        popupView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        
        PopupWindow popupWindow = new PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        );
        
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(16f);
        }

        int[] location = new int[2];
        anchorView.getLocationInWindow(location);
        
        int xOffset = (anchorView.getWidth() - popupView.getMeasuredWidth()) / 2;
        // 向上偏移更多距离
        int yOffset = -anchorView.getHeight() - popupView.getMeasuredHeight() - 32;
        
        popupView.setOnClickListener(v -> {
            item.isFavorite = !item.isFavorite;
            shardPerfenceSetting.setMenuItemFavorite(item.id, item.isFavorite);
            sortMenuItems();
            notifyDataSetChanged();
            popupWindow.dismiss();
        });

        popupWindow.showAsDropDown(anchorView, xOffset, yOffset);
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    static class MenuViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;

        MenuViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.menu_icon);
            title = itemView.findViewById(R.id.menu_title);
        }
    }
} 