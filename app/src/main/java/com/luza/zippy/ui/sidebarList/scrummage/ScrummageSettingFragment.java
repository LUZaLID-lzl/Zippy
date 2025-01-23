package com.luza.zippy.ui.sidebarList.scrummage;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.luza.zippy.R;
import com.luza.zippy.ui.base.BaseFragment;

public class ScrummageSettingFragment extends BaseFragment {
    private RecyclerView recyclerView;
    private ScrummageTypeAdapter adapter;
    private ScrummageTypeViewModel typeViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scrummage_setting, container, false);
        typeViewModel = new ViewModelProvider(requireActivity()).get(ScrummageTypeViewModel.class);
        initViews(view);
        return view;
    }

    @Override
    protected String getTitle() {
        return getString(R.string.menu_scrummage_setting);
    }

    @Override
    protected void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_types);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new ScrummageTypeAdapter(typeViewModel.getScrummageTypes().getValue());
        recyclerView.setAdapter(adapter);

        typeViewModel.getScrummageTypes().observe(getViewLifecycleOwner(), types -> {
            adapter = new ScrummageTypeAdapter(types);
            recyclerView.setAdapter(adapter);
            setupAdapterListeners();
        });

        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add_type);
        fabAdd.setOnClickListener(v -> showAddTypeDialog());

        setupAdapterListeners();
    }

    private void setupAdapterListeners() {
        adapter.setOnItemClickListener(new ScrummageTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, String type) {
                showEditTypeDialog(position, type);
            }

            @Override
            public void onItemLongClick(int position, String type) {
                showDeleteTypeDialog(position, type);
            }
        });
    }

    private void showAddTypeDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_type, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.RoundedDialog);
        builder.setView(dialogView);

        TextView titleView = dialogView.findViewById(R.id.dialog_title);
        EditText typeEdit = dialogView.findViewById(R.id.edit_type);
        Button positiveButton = dialogView.findViewById(R.id.btn_positive);
        Button negativeButton = dialogView.findViewById(R.id.btn_negative);

        titleView.setText(R.string.scrummage_setting_add_type);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        positiveButton.setOnClickListener(v -> {
            String type = typeEdit.getText().toString().trim();
            if (type.isEmpty()) {
                Toast.makeText(requireContext(), R.string.scrummage_setting_type_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            if (typeViewModel.getScrummageTypes().getValue().contains(type)) {
                Toast.makeText(requireContext(), R.string.scrummage_setting_type_exists, Toast.LENGTH_SHORT).show();
                return;
            }

            typeViewModel.addType(type);
            dialog.dismiss();
            Toast.makeText(requireContext(), R.string.scrummage_setting_add_success, Toast.LENGTH_SHORT).show();
        });

        negativeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showEditTypeDialog(int position, String type) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_type, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.RoundedDialog);
        builder.setView(dialogView);

        TextView titleView = dialogView.findViewById(R.id.dialog_title);
        EditText typeEdit = dialogView.findViewById(R.id.edit_type);
        Button positiveButton = dialogView.findViewById(R.id.btn_positive);
        Button negativeButton = dialogView.findViewById(R.id.btn_negative);

        titleView.setText(R.string.scrummage_setting_edit_type);
        typeEdit.setText(type);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        positiveButton.setOnClickListener(v -> {
            String newType = typeEdit.getText().toString().trim();
            if (newType.isEmpty()) {
                Toast.makeText(requireContext(), R.string.scrummage_setting_type_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            if (typeViewModel.getScrummageTypes().getValue().contains(newType) && !newType.equals(type)) {
                Toast.makeText(requireContext(), R.string.scrummage_setting_type_exists, Toast.LENGTH_SHORT).show();
                return;
            }

            typeViewModel.updateType(position, newType);
            dialog.dismiss();
            Toast.makeText(requireContext(), R.string.scrummage_setting_edit_success, Toast.LENGTH_SHORT).show();
        });

        negativeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showDeleteTypeDialog(int position, String type) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_delete_confirm, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.RoundedDialog);
        builder.setView(dialogView);

        TextView titleView = dialogView.findViewById(R.id.dialog_title);
        TextView messageView = dialogView.findViewById(R.id.dialog_message);
        Button positiveButton = dialogView.findViewById(R.id.btn_positive);
        Button negativeButton = dialogView.findViewById(R.id.btn_negative);

        titleView.setText(R.string.scrummage_setting_delete_type);
        messageView.setText(getString(R.string.scrummage_setting_delete_confirm, type));

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        positiveButton.setOnClickListener(v -> {
            typeViewModel.deleteType(position);
            dialog.dismiss();
            Toast.makeText(requireContext(), R.string.scrummage_setting_delete_success, Toast.LENGTH_SHORT).show();
        });

        negativeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}