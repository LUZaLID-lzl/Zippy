package com.luza.zippy.ui.sidebarList.test;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.luza.zippy.R;
import com.luza.zippy.ui.base.BaseFragment;
import com.luza.zippy.ui.sidebarList.settings.Util;

import java.util.Arrays;
import java.util.List;

public class TestFragment extends BaseFragment {

    @Override
    protected String getTitle() {
        return getString(R.string.menu_test);
    }

    @Override
    protected void initViews(View view) {

    }

}