package com.luza.zippy.ui.sidebarList.minecraft;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.luza.zippy.R;
import com.luza.zippy.ui.base.BaseFragment;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class MinecraftFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_minecraft, container, false);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.menu_minecraft);
    }

    @Override
    protected void initViews(View view) {

    }
}