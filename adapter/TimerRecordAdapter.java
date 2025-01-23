@Override
public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    TimerRecord record = records.get(position);
    
    // 设置类型图标
    if (record.isOption1()) {
        holder.typeImage.setImageResource(R.drawable.ic_spark);
    } else if (record.isOption2()) {
        holder.typeImage.setImageResource(R.drawable.ic_leaf);
    } else if (record.isOption3()) {
        holder.typeImage.setImageResource(R.drawable.ic_peach);
    }

    // 格式化时长
    long millis = record.getDuration();
    long hours = millis / (1000 * 60 * 60);
    long minutes = (millis % (1000 * 60 * 60)) / (1000 * 60);
    long seconds = (millis % (1000 * 60)) / 1000;
    long milliseconds = millis % 1000;

    StringBuilder timeBuilder = new StringBuilder();
    if (hours > 0) {
        timeBuilder.append(hours).append("小时");
    }
    if (minutes > 0 || hours > 0) {
        timeBuilder.append(minutes).append("分");
    }
    if (seconds > 0 || minutes > 0 || hours > 0) {
        timeBuilder.append(seconds).append("秒");
    }
    timeBuilder.append(String.format(Locale.getDefault(), "%03d", milliseconds)).append("毫秒");
    
    holder.durationText.setText(timeBuilder.toString());
    holder.durationText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10); // 修改字体大小

    // 格式化时间戳
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    holder.timestampText.setText(sdf.format(record.getTimestamp()));
    holder.timestampText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10); // 修改字体大小

    // 设置删除按钮点击事件
    holder.deleteButton.setOnClickListener(v -> {
        if (deleteListener != null) {
            deleteListener.onDeleteClick(record);
        }
    });
}