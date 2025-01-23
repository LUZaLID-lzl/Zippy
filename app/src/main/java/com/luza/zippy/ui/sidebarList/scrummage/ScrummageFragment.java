package com.luza.zippy.ui.sidebarList.scrummage;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.luza.zippy.R;
import com.luza.zippy.ui.base.BaseFragment;
import com.luza.zippy.ui.sidebarList.scrummage.data.entity.ScrummageRecord;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class ScrummageFragment extends BaseFragment {
    private ScrummageViewModel viewModel;
    private ScrummageTypeViewModel typeViewModel;
    private RecyclerView recyclerView;
    private TextView emptyText;
    private ScrummageAdapter adapter;
    private List<String> scrummageTypes = new ArrayList<>(Arrays.asList("结婚", "住房", "白事"));
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_scrummage, container, false);
        initViews(rootView);
        return rootView;
    }

    @Override
    protected String getTitle() {
        return getString(R.string.menu_scrummage);
    }

    @Override
    protected void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_scrummage);
        emptyText = view.findViewById(R.id.text_empty);

        // 初始化左按钮（添加记录）
        Button btnLeft = view.findViewById(R.id.btn_scrummage_left);
        btnLeft.setText(R.string.scrummage_add);
        btnLeft.setOnClickListener(v -> showAddDialog());

        // 初始化中间按钮（过滤）
        Button btnMiddle = view.findViewById(R.id.btn_scrummage_center);
        btnMiddle.setVisibility(View.VISIBLE);
        btnMiddle.setText(R.string.scrummage_filter);
        btnMiddle.setOnClickListener(v -> showFilterDialog());

        // 初始化右按钮（设置）
        Button btnRight = view.findViewById(R.id.btn_scrummage_right);
        btnRight.setText(R.string.settings);
        btnRight.setOnClickListener(v -> {
            // 跳转到设置页面
            getParentFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.content_frame, new ScrummageSettingFragment())
                .addToBackStack(null)
                .commit();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ScrummageAdapter();
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ScrummageViewModel.class);
        typeViewModel = new ViewModelProvider(requireActivity()).get(ScrummageTypeViewModel.class);

        viewModel.getAllRecords().observe(getViewLifecycleOwner(), records -> {
            adapter.submitList(records);
            emptyText.setVisibility(records.isEmpty() ? View.VISIBLE : View.GONE);
        });

        adapter.setOnItemClickListener(new ScrummageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ScrummageRecord record) {
                showEditDialog(record);
            }

            @Override
            public void onItemLongClick(ScrummageRecord record) {
                showDeleteDialog(record);
            }
        });

        updateFilterStatus();
    }

    private void updateFilterStatus() {
        Button btnFilter = rootView.findViewById(R.id.btn_scrummage_center);
        View cardFilterStatus = rootView.findViewById(R.id.card_filter_status);
        TextView filterStatus = rootView.findViewById(R.id.text_filter_status);
        
        boolean isFiltering = viewModel.getCurrentTypeFilter() != null ||
                viewModel.getCurrentNameFilter() != null ||
                viewModel.getCurrentMinAmount() != null ||
                viewModel.getCurrentMaxAmount() != null ||
                viewModel.getCurrentStartDate() != null ||
                viewModel.getCurrentEndDate() != null;

        if (isFiltering) {
            // 构建过滤提示文本
            StringBuilder filterInfo = new StringBuilder("当前过滤：");
            if (viewModel.getCurrentTypeFilter() != null) {
                filterInfo.append("类型(").append(viewModel.getCurrentTypeFilter()).append(") ");
            }
            if (viewModel.getCurrentNameFilter() != null) {
                filterInfo.append("名字(").append(viewModel.getCurrentNameFilter()).append(") ");
            }
            if (viewModel.getCurrentMinAmount() != null || viewModel.getCurrentMaxAmount() != null) {
                filterInfo.append("金额(");
                if (viewModel.getCurrentMinAmount() != null) {
                    filterInfo.append(viewModel.getCurrentMinAmount());
                    if (viewModel.getCurrentMaxAmount() != null) {
                        filterInfo.append("到");
                    }
                }
                if (viewModel.getCurrentMaxAmount() != null) {
                    filterInfo.append(viewModel.getCurrentMaxAmount());
                }
                filterInfo.append(") ");
            }
            if (viewModel.getCurrentStartDate() != null || viewModel.getCurrentEndDate() != null) {
                filterInfo.append("日期(");
                if (viewModel.getCurrentStartDate() != null) {
                    filterInfo.append(viewModel.getCurrentStartDate());
                    if (viewModel.getCurrentEndDate() != null) {
                        filterInfo.append("到");
                    }
                }
                if (viewModel.getCurrentEndDate() != null) {
                    filterInfo.append(viewModel.getCurrentEndDate());
                }
                filterInfo.append(")");
            }
            
            // 显示过滤提示
            filterStatus.setText(filterInfo.toString());
            cardFilterStatus.setVisibility(View.VISIBLE);
        } else {
            // 隐藏过滤提示
            cardFilterStatus.setVisibility(View.GONE);
        }
    }

    private void showAddDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_scrummage_record, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.RoundedDialog);
        builder.setView(dialogView);

        TextView titleView = dialogView.findViewById(R.id.dialog_title);
        titleView.setText(R.string.scrummage_add);

        // 设置类型下拉框
        Spinner typeSpinner = dialogView.findViewById(R.id.spinner_type);
        List<String> types = typeViewModel.getScrummageTypes().getValue();
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
            requireContext(),
            R.layout.item_spinner,
            types != null ? types : new ArrayList<>()
        );
        spinnerAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        typeSpinner.setAdapter(spinnerAdapter);

        TextInputEditText amountEdit = dialogView.findViewById(R.id.edit_amount);
        TextInputEditText payerEdit = dialogView.findViewById(R.id.edit_payer);
        Button positiveButton = dialogView.findViewById(R.id.btn_positive);
        Button negativeButton = dialogView.findViewById(R.id.btn_negative);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        positiveButton.setOnClickListener(v -> {
            String type = typeSpinner.getSelectedItem().toString();
            String amountStr = amountEdit.getText().toString().trim();
            String payer = payerEdit.getText().toString().trim();

            if (amountStr.isEmpty()) {
                Toast.makeText(requireContext(), R.string.scrummage_amount_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            if (payer.isEmpty()) {
                Toast.makeText(requireContext(), R.string.scrummage_payer_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(new Date());

            ScrummageRecord record = new ScrummageRecord(type, amount, date, "", payer, "");
            viewModel.insert(record);
            Toast.makeText(requireContext(), R.string.scrummage_add_success, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        negativeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showEditDialog(ScrummageRecord record) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_scrummage_record, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.RoundedDialog);
        builder.setView(dialogView);

        TextView titleView = dialogView.findViewById(R.id.dialog_title);
        titleView.setText(R.string.scrummage_edit);

        // 设置类型下拉框
        Spinner typeSpinner = dialogView.findViewById(R.id.spinner_type);
        List<String> types = typeViewModel.getScrummageTypes().getValue();
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
            requireContext(),
            R.layout.item_spinner,
            types != null ? types : new ArrayList<>()
        );
        spinnerAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        typeSpinner.setAdapter(spinnerAdapter);
        
        // 设置当前选中的类型
        int typePosition = scrummageTypes.indexOf(record.getTitle());
        if (typePosition >= 0) {
            typeSpinner.setSelection(typePosition);
        }

        TextInputEditText amountEdit = dialogView.findViewById(R.id.edit_amount);
        TextInputEditText payerEdit = dialogView.findViewById(R.id.edit_payer);
        Button positiveButton = dialogView.findViewById(R.id.btn_positive);
        Button negativeButton = dialogView.findViewById(R.id.btn_negative);

        // 填充现有数据
        amountEdit.setText(String.format(Locale.getDefault(), "%.2f", record.getAmount()));
        payerEdit.setText(record.getPayer());

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        positiveButton.setOnClickListener(v -> {
            String type = typeSpinner.getSelectedItem().toString();
            String amountStr = amountEdit.getText().toString().trim();
            String payer = payerEdit.getText().toString().trim();

            if (amountStr.isEmpty()) {
                Toast.makeText(requireContext(), R.string.scrummage_amount_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            if (payer.isEmpty()) {
                Toast.makeText(requireContext(), R.string.scrummage_payer_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);

            record.setTitle(type);
            record.setAmount(amount);
            record.setPayer(payer);

            viewModel.update(record);
            Toast.makeText(requireContext(), R.string.scrummage_edit_success, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        negativeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showDeleteDialog(ScrummageRecord record) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_delete_confirm, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.RoundedDialog);
        builder.setView(dialogView);

        TextView titleView = dialogView.findViewById(R.id.dialog_title);
        TextView messageView = dialogView.findViewById(R.id.dialog_message);
        Button positiveButton = dialogView.findViewById(R.id.btn_positive);
        Button negativeButton = dialogView.findViewById(R.id.btn_negative);

        titleView.setText(R.string.scrummage_delete);
        messageView.setText(R.string.scrummage_delete_confirm);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        positiveButton.setOnClickListener(v -> {
            viewModel.delete(record);
            Toast.makeText(requireContext(), R.string.scrummage_delete_success, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        negativeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showFilterDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_scrummage_filter);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        RadioGroup radioGroup = dialog.findViewById(R.id.radio_group_filter);
        View layoutFilterType = dialog.findViewById(R.id.layout_filter_type);
        View layoutFilterName = dialog.findViewById(R.id.layout_filter_name);
        View layoutFilterAmount = dialog.findViewById(R.id.layout_filter_amount);
        View layoutFilterDate = dialog.findViewById(R.id.layout_filter_date);

        // 初始化所有过滤器为隐藏状态
        layoutFilterType.setVisibility(View.GONE);
        layoutFilterName.setVisibility(View.GONE);
        layoutFilterAmount.setVisibility(View.GONE);
        layoutFilterDate.setVisibility(View.GONE);

        // 设置单选框切换监听器
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            layoutFilterType.setVisibility(View.GONE);
            layoutFilterName.setVisibility(View.GONE);
            layoutFilterAmount.setVisibility(View.GONE);
            layoutFilterDate.setVisibility(View.GONE);

            if (checkedId == R.id.radio_type) {
                layoutFilterType.setVisibility(View.VISIBLE);
                setupTypeSpinner(dialog);
            } else if (checkedId == R.id.radio_name) {
                layoutFilterName.setVisibility(View.VISIBLE);
                setupNameSpinner(dialog);
            } else if (checkedId == R.id.radio_amount) {
                layoutFilterAmount.setVisibility(View.VISIBLE);
                EditText editMinAmount = dialog.findViewById(R.id.edit_min_amount);
                EditText editMaxAmount = dialog.findViewById(R.id.edit_max_amount);
                editMinAmount.setText(viewModel.getCurrentMinAmount() != null ? 
                    String.valueOf(viewModel.getCurrentMinAmount()) : "");
                editMaxAmount.setText(viewModel.getCurrentMaxAmount() != null ? 
                    String.valueOf(viewModel.getCurrentMaxAmount()) : "");
            } else if (checkedId == R.id.radio_date) {
                layoutFilterDate.setVisibility(View.VISIBLE);
                setupDatePickers(dialog);
            }
        });

        // 设置按钮点击事件
        dialog.findViewById(R.id.btn_clear).setOnClickListener(v -> {
            viewModel.clearFilter();
            updateFilterStatus();
            dialog.dismiss();
        });

        dialog.findViewById(R.id.btn_positive).setOnClickListener(v -> {
            int checkedId = radioGroup.getCheckedRadioButtonId();
            if (checkedId == R.id.radio_type) {
                Spinner spinnerType = dialog.findViewById(R.id.spinner_type);
                String selectedType = spinnerType.getSelectedItem().toString();
                viewModel.filterByType(selectedType);
            } else if (checkedId == R.id.radio_name) {
                Spinner spinnerName = dialog.findViewById(R.id.spinner_name);
                String selectedName = spinnerName.getSelectedItem().toString();
                viewModel.filterByName(selectedName);
            } else if (checkedId == R.id.radio_amount) {
                EditText editMinAmount = dialog.findViewById(R.id.edit_min_amount);
                EditText editMaxAmount = dialog.findViewById(R.id.edit_max_amount);
                String minAmountStr = editMinAmount.getText().toString();
                String maxAmountStr = editMaxAmount.getText().toString();
                
                Double minAmount = minAmountStr.isEmpty() ? null : Double.parseDouble(minAmountStr);
                Double maxAmount = maxAmountStr.isEmpty() ? null : Double.parseDouble(maxAmountStr);
                
                viewModel.filterByAmount(minAmount, maxAmount);
            } else if (checkedId == R.id.radio_date) {
                EditText editStartDate = dialog.findViewById(R.id.edit_start_date);
                EditText editEndDate = dialog.findViewById(R.id.edit_end_date);
                String startDate = editStartDate.getText().toString();
                String endDate = editEndDate.getText().toString();
                
                viewModel.filterByDate(startDate, endDate);
            }
            updateFilterStatus();
            dialog.dismiss();
        });

        // 根据当前过滤状态选择默认选项
        if (viewModel.getCurrentTypeFilter() != null) {
            radioGroup.check(R.id.radio_type);
        } else if (viewModel.getCurrentNameFilter() != null) {
            radioGroup.check(R.id.radio_name);
        } else if (viewModel.getCurrentMinAmount() != null || viewModel.getCurrentMaxAmount() != null) {
            radioGroup.check(R.id.radio_amount);
        } else if (viewModel.getCurrentStartDate() != null || viewModel.getCurrentEndDate() != null) {
            radioGroup.check(R.id.radio_date);
        } else {
            radioGroup.check(R.id.radio_type);
        }

        dialog.show();
    }

    private void setupTypeSpinner(Dialog dialog) {
        Spinner spinnerType = dialog.findViewById(R.id.spinner_type);
        List<String> types = typeViewModel.getScrummageTypes().getValue();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            requireContext(),
            R.layout.item_spinner,
            types != null ? types : new ArrayList<>()
        );
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerType.setAdapter(adapter);

        String currentType = viewModel.getCurrentTypeFilter();
        if (currentType != null) {
            int position = adapter.getPosition(currentType);
            if (position != -1) {
                spinnerType.setSelection(position);
            }
        }
    }

    private void setupNameSpinner(Dialog dialog) {
        Spinner spinnerName = dialog.findViewById(R.id.spinner_name);
        viewModel.getAllUniquePayerNames().observe(getViewLifecycleOwner(), names -> {
            if (names != null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    R.layout.item_spinner,
                    names
                );
                adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
                spinnerName.setAdapter(adapter);

                String currentName = viewModel.getCurrentNameFilter();
                if (currentName != null) {
                    int position = adapter.getPosition(currentName);
                    if (position != -1) {
                        spinnerName.setSelection(position);
                    }
                }
            }
        });
    }

    private void setupDatePickers(Dialog dialog) {
        EditText editStartDate = dialog.findViewById(R.id.edit_start_date);
        EditText editEndDate = dialog.findViewById(R.id.edit_end_date);

        // 设置当前日期
        String currentStartDate = viewModel.getCurrentStartDate();
        String currentEndDate = viewModel.getCurrentEndDate();
        editStartDate.setText(currentStartDate != null ? currentStartDate : "");
        editEndDate.setText(currentEndDate != null ? currentEndDate : "");

        // 设置开始日期选择器
        editStartDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            if (currentStartDate != null) {
                try {
                    Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .parse(currentStartDate);
                    calendar.setTime(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String date = String.format(Locale.getDefault(), "%d-%02d-%02d",
                        year, month + 1, dayOfMonth);
                    editStartDate.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // 设置结束日期选择器
        editEndDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            if (currentEndDate != null) {
                try {
                    Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .parse(currentEndDate);
                    calendar.setTime(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String date = String.format(Locale.getDefault(), "%d-%02d-%02d",
                        year, month + 1, dayOfMonth);
                    editEndDate.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
    }
}