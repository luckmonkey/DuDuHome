package com.dudu.android.launcher.ui.dialog;

import android.app.Activity;
import android.app.Dialog;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.dudu.android.launcher.R;

/**
 * Created by ZACK on 2015/11/16.
 * 没有蓝牙是弹出的dialog
 */
public class BluetoothAlertDialog extends Dialog implements View.OnClickListener{
    private Activity mActivity;
    public BluetoothAlertDialog(Activity activity) {
        super(activity, R.style.BlueTeethPromptDialog);
        this.mActivity=activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blue_teeth_error_prompt);
       LinearLayout linearLayout=(LinearLayout)findViewById(R.id.layout_blue_teeth);
        setCanceledOnTouchOutside(false);
       //linearLayout.getBackground().setAlpha(120);//180为透明的比率
       linearLayout.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.layout_blue_teeth:
               // dismiss();
                break;
        }
    }

}