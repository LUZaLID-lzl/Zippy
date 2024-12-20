package com.luza.zippy.ui.sidebarList.todo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import com.luza.zippy.R;
import java.util.function.Consumer;

public class TodoDialogFragment extends DialogFragment {
    private TodoItem todoItem;
    private Consumer<TodoItem> callback;
    private EditText titleEdit;
    private EditText contentEdit;

    public static TodoDialogFragment newInstance(TodoItem item) {
        TodoDialogFragment fragment = new TodoDialogFragment();
        fragment.todoItem = item;
        return fragment;
    }

    public void setCallback(Consumer<TodoItem> callback) {
        this.callback = callback;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_todo, null);
        titleEdit = view.findViewById(R.id.edit_title);
        contentEdit = view.findViewById(R.id.edit_content);

        if (todoItem != null) {
            titleEdit.setText(todoItem.getTitle());
            contentEdit.setText(todoItem.getContent());
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setTitle(todoItem == null ? getString(R.string.todo_add) : getString(R.string.todo_edit))
            .setView(view)
            .setPositiveButton(getString(R.string.save), null)
            .setNegativeButton(getString(R.string.cancel), null)
            .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                String title = titleEdit.getText().toString().trim();
                if (title.isEmpty()) {
                    Toast.makeText(getContext(), getString(R.string.todo_title_empty), Toast.LENGTH_SHORT).show();
                    return;
                }

                TodoItem item = todoItem == null ? new TodoItem() : todoItem;
                item.setTitle(title);
                item.setContent(contentEdit.getText().toString().trim());

                if (callback != null) {
                    callback.accept(item);
                }
                dialog.dismiss();
            });
        });

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            positiveButton.setTextColor(getResources().getColor(R.color.blue_500));
            Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            negativeButton.setTextColor(getResources().getColor(R.color.blue_500));
        }
    }
} 