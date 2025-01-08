package com.luza.zippy.ui.sidebarList.todo;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.luza.zippy.R;
import com.luza.zippy.ui.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

public class TodoFragment extends BaseFragment {
    private RecyclerView recyclerView;
    private TodoAdapter adapter;
    private TodoPreferences todoPreferences;
    private List<TodoItem> todoList = new ArrayList<>();

    @Override
    protected String getTitle() {
        return  getString(R.string.menu_todo);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_todo, container, false);
    }


    @Override
    protected void initViews(View view) {
        // 设置Toolbar

        todoPreferences = new TodoPreferences(requireContext());
        setupRecyclerView(view);
        setupButtons(view);
        loadTodoItems();
    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TodoAdapter(
            todoList,
            this::editTodoItem,
            this::deleteTodoItem,
            this::completeTodoItem
        );
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new TodoItemAnimator());
    }

    private void setupButtons(View view) {
        view.findViewById(R.id.fab_add).setOnClickListener(v -> showAddDialog());
        view.findViewById(R.id.fab_import).setOnClickListener(v -> importTodoItems());
        view.findViewById(R.id.fab_clear).setOnClickListener(v -> showClearDialog());
    }

    private void showAddDialog() {
        TodoDialogFragment dialog = TodoDialogFragment.newInstance(null);
        dialog.setCallback(this::addTodoItem);
        dialog.show(getChildFragmentManager(), "add_todo");
    }

    private void addTodoItem(TodoItem item) {
        todoList.add(0, item);
        adapter.notifyItemInserted(0);
        recyclerView.smoothScrollToPosition(0);
        saveTodoItems();
    }

    private void editTodoItem(int position, TodoItem item) {
        TodoDialogFragment dialog = TodoDialogFragment.newInstance(item);
        dialog.setCallback(newItem -> {
            todoList.set(position, newItem);
            adapter.notifyItemChanged(position);
            saveTodoItems();
        });
        dialog.show(getChildFragmentManager(), "edit_todo");
    }

    private void deleteTodoItem(int position) {
        TodoItem item = todoList.get(position);
        todoList.remove(position);
        adapter.notifyItemRemoved(position);
        saveTodoItems();
        showUndoSnackbar(item, position);
    }

    private void showUndoSnackbar(TodoItem item, int position) {
        Snackbar.make(recyclerView, getString(R.string.todo_deleted), Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.undo), v -> {
                todoList.add(position, item);
                adapter.notifyItemInserted(position);
                saveTodoItems();
            })
            .show();
    }

    private void importTodoItems() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_import_todo, null);
        EditText importEdit = view.findViewById(R.id.edit_import);

        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setTitle(getString(R.string.todo_import))
            .setView(view)
            .setPositiveButton(getString(R.string.confirm), null)
            .setNegativeButton(getString(R.string.cancel), null)
            .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                String input = importEdit.getText().toString().trim();
                if (input.isEmpty()) {
                    Toast.makeText(getContext(), getString(R.string.todo_input_empty), Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    String[] lines = input.split("\n");
                    int importCount = 0;
                    
                    // 跳过表头行
                    boolean isFirstLine = true;
                    
                    for (String line : lines) {
                        // 跳过表头和分隔符行
                        if (isFirstLine || line.contains("-")) {
                            isFirstLine = false;
                            continue;
                        }
                        
                        // 处理每一行数据
                        line = line.trim();
                        if (line.startsWith("|") && line.endsWith("|")) {
                            // 移除首尾的 |
                            line = line.substring(1, line.length() - 1);
                            // 按 | 分割并去除空格
                            String[] parts = line.split("\\|");
                            if (parts.length == 2) {
                                String title = parts[0].trim();
                                String content = parts[1].trim();
                                
                                if (!title.isEmpty()) {
                                    TodoItem item = new TodoItem();
                                    item.setTitle(title);
                                    item.setContent(content);
                                    todoList.add(0, item);
                                    importCount++;
                                }
                            }
                        }
                    }

                    if (importCount > 0) {
                        adapter.notifyDataSetChanged();
                        saveTodoItems();
                        dialog.dismiss();
                        Snackbar.make(recyclerView, 
                            getString(R.string.todo_import_success, importCount), 
                            Snackbar.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), getString(R.string.todo_import_invalid_format), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getContext(), getString(R.string.todo_import_failed), Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void showClearDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.todo_clear))
            .setMessage(getString(R.string.todo_clear_confirm))
            .setPositiveButton(getString(R.string.confirm), (dialog, which) -> {
                todoList.clear();
                adapter.notifyDataSetChanged();
                todoPreferences.clearAll();
            })
            .setNegativeButton(getString(R.string.cancel), null)
            .show();
    }

    private void loadTodoItems() {
        todoList.clear();
        todoList.addAll(todoPreferences.getTodoList());
        adapter.notifyDataSetChanged();
    }

    private void saveTodoItems() {
        todoPreferences.saveTodoList(todoList);
    }

    private void completeTodoItem(TodoItem item) {
        saveTodoItems();
        Snackbar.make(recyclerView, getString(R.string.todo_completed), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);  // 启用选项菜单
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_todo, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_import) {
            importTodoItems();
            return true;
        } else if (id == R.id.action_clear) {
            showClearDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 