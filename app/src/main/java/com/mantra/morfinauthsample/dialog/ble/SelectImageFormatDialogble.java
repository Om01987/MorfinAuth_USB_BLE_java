package com.mantra.morfinauthsample.dialog.ble;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.TextView;

import com.mantra.morfinauthsample.R;
import com.mantra.morfinauthsample.databinding.DialogImageFormatsBinding;

@SuppressWarnings("ALL")
public class SelectImageFormatDialogble extends Dialog {
    public ViewHolder holder;

    public SelectImageFormatDialogble(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_image_formats_ble);

        setCancelable(true);
        setCanceledOnTouchOutside(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        WindowManager manager = (WindowManager) getContext().getSystemService(Activity.WINDOW_SERVICE);
        lp.copyFrom(getWindow().getAttributes());
        Point point = new Point();
        manager.getDefaultDisplay().getSize(point);
        lp.width = point.x;
        lp.height = point.y;
        getWindow().setAttributes(lp);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        holder = new ViewHolder(getWindow().getDecorView());
    }

    public static class ViewHolder {
        public TextView txtTitle;
        public RadioButton cbBmp;
        public RadioButton cbWsq;
        public RadioButton cbRaw;
        public RadioButton cbFIRV2005;
        public RadioButton cbFIRV2011;
        public TextView txtCancel;
        public TextView txtSave;

        ViewHolder(View view) {
            txtTitle = view.findViewById(R.id.txtTitle);
            cbBmp = view.findViewById(R.id.cbBmp);
            cbWsq = view.findViewById(R.id.cbWsq);
            cbRaw = view.findViewById(R.id.cbRaw);
            cbFIRV2005 = view.findViewById(R.id.cbFIR_V2005);
            cbFIRV2011 = view.findViewById(R.id.cbFIR_V2011);
            txtCancel = view.findViewById(R.id.txtCancel);
            txtSave = view.findViewById(R.id.txtSave);
        }
    }
}
