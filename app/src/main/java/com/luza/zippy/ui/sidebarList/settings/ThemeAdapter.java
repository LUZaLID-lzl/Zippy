package com.luza.zippy.ui.sidebarList.settings;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.luza.zippy.MainActivity;
import com.luza.zippy.R;
import com.luza.zippy.setting.ShardPerfenceSetting;

import java.util.List;

public class ThemeAdapter extends RecyclerView.Adapter<ThemeAdapter.ThemeViewHolder>{
    private List<ThemeItem> themes;
    private int selectedPosition = 0; // 默认选中第一项

    private Activity mActivity;
    private ShardPerfenceSetting shardPerfenceSetting;

    ThemeAdapter(List<ThemeItem> themes,Activity activity) {
        this.themes = themes;
        this.mActivity = activity;
        this.shardPerfenceSetting = ShardPerfenceSetting.getInstance(mActivity.getBaseContext());

        // 设置shardPerfence开启状态
        for(ThemeItem item:themes){
            if (item.description.equals(shardPerfenceSetting.getHomeTheme())){
                item.isEnabled = true;
                break;
            }
        }
    }

    @NonNull
    @Override
    public ThemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_theme_card, parent, false);
        return new ThemeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThemeViewHolder holder, int position) {
        ThemeItem theme = themes.get(position);
        holder.bind(theme);
    }

    @Override
    public int getItemCount() {
        return themes.size();
    }

    class ThemeViewHolder extends RecyclerView.ViewHolder {
        private TextView nameText;
        private TextView descriptionText;
        private ImageView themeImage;
        private CardView cardView;
        private Switch themeSwitch;

        ThemeViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.theme_name);
            descriptionText = itemView.findViewById(R.id.theme_description);
            themeImage = itemView.findViewById(R.id.theme_image);
            cardView = (CardView) itemView;
            themeSwitch = itemView.findViewById(R.id.theme_switch);

            // 为整个卡片添加点击事件
            cardView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    ThemeItem theme = themes.get(position);
                    if (!theme.isEnabled) {
                        // 关闭之前选中的项
                        int oldSelectedPosition = selectedPosition;
                        themes.get(oldSelectedPosition).isEnabled = false;

                        // 更新新的选中项
                        selectedPosition = position;
                        theme.isEnabled = true;

                        // 保存主题设置
                        shardPerfenceSetting.setHomeTheme(theme.description);

                        // 显示加载对话框
                        Dialog loadingDialog = new Dialog(mActivity, R.style.LoadingDialog);
                        loadingDialog.setContentView(R.layout.loading_dialog);
                        loadingDialog.setCancelable(false);
                        loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                        loadingDialog.show();

                        // 延迟一小段时间后重新创建Activity
                        new Handler().postDelayed(() -> {
                            loadingDialog.dismiss();
                            if (mActivity instanceof MainActivity) {
                                // 重新创建Activity以应用新主题
                                Intent intent = mActivity.getIntent();
                                mActivity.finish();
                                mActivity.startActivity(intent);
                                mActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            }
                        }, 500);

                        Toast.makeText(mActivity, mActivity.getBaseContext().getString(R.string.theme_tips), Toast.LENGTH_SHORT).show();

                        // 刷新列表显示
                        notifyItemChanged(oldSelectedPosition);
                        notifyItemChanged(selectedPosition);
                    }
                }
            });
        }

        void bind(ThemeItem theme) {
            nameText.setText(theme.name);
            descriptionText.setText(theme.description);
            themeImage.setImageResource(theme.imageRes);
            cardView.setBackgroundResource(theme.backgroundColor);

            // 只需要更新开关状态，不需要添加监听器
            themeSwitch.setChecked(theme.isEnabled);
        }
    }
}
