package com.dudu.aios.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.dudu.aios.ui.fragment.base.BaseVehicleFragment;
import com.dudu.aios.ui.robbery.RobberyConstant;
import com.dudu.android.launcher.R;
import com.dudu.workflow.common.DataFlowFactory;
import com.dudu.workflow.common.ObservableFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RobberyLockFragment extends BaseVehicleFragment implements View.OnClickListener {

    private View guard_unlock_layout, guard_locked_layout;

    private TextView tvTitleCh, tvTitleEn;

    private Logger logger = LoggerFactory.getLogger("RobberyLockFragment");

    @Override
    public View getVehicleChildView() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.vehicle_guard_layout, null);
        initView(view);
        initListener();
        syncAppRobberyFlow();
        return view;
    }

    private void initListener() {
        guard_locked_layout.setOnClickListener(this);
        guard_unlock_layout.setOnClickListener(this);
    }

    private void initView(View view) {
        guard_unlock_layout = view.findViewById(R.id.vehicle_unlock_layout);
        guard_locked_layout = view.findViewById(R.id.vehicle_locked_layout);
        tvTitleCh = (TextView) view.findViewById(R.id.text_title_ch);
        tvTitleCh.setText(getResources().getString(R.string.vehicle_robbery_ch));
        tvTitleEn = (TextView) view.findViewById(R.id.text_title_en);
        tvTitleEn.setText(getResources().getString(R.string.vehicle_robbery_en));
        lock();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.vehicle_unlock_layout:
                unlock();
                transferParameters();

                break;
            case R.id.vehicle_locked_layout:
                lock();
                break;
        }
    }

    private void transferParameters() {
        VehiclePasswordSetFragment vehiclePasswordSetFragment = new VehiclePasswordSetFragment();
        Bundle bundle = new Bundle();
        bundle.putString(RobberyConstant.CATEGORY_CONSTANT, RobberyConstant.ROBBERY_CONSTANT);
        vehiclePasswordSetFragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(R.id.container, vehiclePasswordSetFragment).commit();
    }

    private void unlock() {
        guard_locked_layout.setVisibility(View.VISIBLE);
        guard_unlock_layout.setVisibility(View.GONE);
    }

    private void lock() {
        guard_locked_layout.setVisibility(View.GONE);
        guard_unlock_layout.setVisibility(View.VISIBLE);
    }

    public void syncAppRobberyFlow() {
        ObservableFactory.syncAppRobberyFlow()
                .subscribe(receiverData -> {
                    if (!receiverData.getSwitch0Value().equals("1")) {
                        DataFlowFactory.getSwitchDataFlow().saveRobberyState(true);
                        getFragmentManager().beginTransaction().replace(R.id.container, new RobberyFragment()).commit();
                    }
                });
    }
}
