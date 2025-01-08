package com.luza.zippy.ui.sidebarList.calorie;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.luza.zippy.R;
import com.luza.zippy.ui.base.BaseFragment;

public class CalorieFragment extends BaseFragment {
    private EditText energyPer100gEdit;
    private EditText gramsEdit;
    private TextView resultText;
    private TextView caloriesText;  // 显示转换后的卡路里
    private static final float KJ_TO_KCAL = 0.239f;  // 千焦转千卡系数

    @Override
    protected String getTitle() {
        return getString(R.string.menu_calorie);
    }

    @Override
    protected void initViews(View view) {
        energyPer100gEdit = view.findViewById(R.id.edit_energy_per_100g);
        gramsEdit = view.findViewById(R.id.edit_grams);
        resultText = view.findViewById(R.id.text_result);
        caloriesText = view.findViewById(R.id.text_calories);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                calculateEnergy();
            }
        };

        energyPer100gEdit.addTextChangedListener(watcher);
        gramsEdit.addTextChangedListener(watcher);
    }

    private void calculateEnergy() {
        try {
            String energyPer100gStr = energyPer100gEdit.getText().toString();
            String gramsStr = gramsEdit.getText().toString();
            
            if (!energyPer100gStr.isEmpty() && !gramsStr.isEmpty()) {
                float energyPer100g = Float.parseFloat(energyPer100gStr);
                float grams = Float.parseFloat(gramsStr);
                
                float totalEnergy = (energyPer100g * grams) / 100f;
                float totalCalories = totalEnergy * KJ_TO_KCAL;
                
                resultText.setText(String.format(getString(R.string.calorie_total_energy), totalEnergy));
                caloriesText.setText(String.format(getString(R.string.calorie_total_calories), totalCalories));
            } else {
                resultText.setText(getString(R.string.calorie_enter_value));
                caloriesText.setText("");
            }
        } catch (NumberFormatException e) {
            resultText.setText(getString(R.string.calorie_enter_valid));
            caloriesText.setText("");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calorie, container, false);
    }

} 