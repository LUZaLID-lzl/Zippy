package com.luza.zippy.ui.sidebarList.todo;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import com.luza.zippy.ui.sidebarList.todo.TodoItem;
import java.util.List;
import java.util.UUID;

public class TodoPreferences {
    private static final String PREF_NAME = "todo_preferences";
    private static final String KEY_TODO_LIST = "todo_list";
    private final SharedPreferences preferences;
    private final Gson gson;

    public TodoPreferences(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveTodoList(List<TodoItem> todoList) {
        String json = gson.toJson(todoList);
        preferences.edit().putString(KEY_TODO_LIST, json).apply();
    }

    public List<TodoItem> getTodoList() {
        String json = preferences.getString(KEY_TODO_LIST, "[]");
        Type type = new TypeToken<List<TodoItem>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public void clearAll() {
        preferences.edit().clear().apply();
    }
} 