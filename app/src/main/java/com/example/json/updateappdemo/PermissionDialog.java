package com.example.json.updateappdemo;

import android.app.Activity;
import android.app.Dialog;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;


/**
 * Created by Administrator on 2017/5/18.
 */

public class PermissionDialog extends Dialog {

    private Activity mContext;
    private OnPermissionClickListener mListener;

    public PermissionDialog(Activity activity) {
        super(activity, R.style.common_alert_dialog);
        this.mContext = activity;
    }


    public void setOnButtonClickListener(OnPermissionClickListener listener) {
        mListener = listener;
    }

    public interface OnPermissionClickListener {
        void cancel();

        void confirm();
    }

    public  void showPermissionDialog() {
        final Dialog dialog = new Dialog(mContext, R.style.LodingDialog);
        dialog.setContentView(R.layout.dialog_permission);
        setCanceledOnTouchOutside(false);
        TextView tvCancel = (TextView) dialog.findViewById(R.id.tv_cancel);
        TextView tvConfirm = (TextView) dialog.findViewById(R.id.tv_confirm);

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.cancel();
            }
        });
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                mListener.confirm();

            }
        });
        Display defaultDisplay = mContext.getWindowManager().getDefaultDisplay();
        int width = (int) (defaultDisplay.getWidth() * 0.7);
        android.view.WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = width;
        dialog.getWindow().setGravity(Gravity.CENTER_HORIZONTAL);
        dialog.getWindow().setAttributes(params);

        dialog.show();
    }

}
