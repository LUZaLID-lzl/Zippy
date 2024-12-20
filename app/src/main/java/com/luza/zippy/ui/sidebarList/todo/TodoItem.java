package com.luza.zippy.ui.sidebarList.todo;

import java.util.UUID;

public class TodoItem {
    private String id;
    private String title;
    private String content;
    private long createTime;
    private boolean completed;

    public TodoItem() {
        this.id = UUID.randomUUID().toString();
        this.createTime = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
} 