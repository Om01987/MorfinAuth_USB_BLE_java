package com.mantra.morfinauthsample.dialog;

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


@SuppressWarnings("ALL")
public class SelectTemplateFormatDialog extends Dialog {
    public ViewHolder holder;

    public SelectTemplateFormatDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_template_formats);

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
        public RadioButton cbFMRV2005;
        public RadioButton cbFMRV2011;
        public RadioButton cbANSIV378;
        public TextView txtCancel;
        public TextView txtSave;

        ViewHolder(View view) {
            txtTitle = view.findViewById(R.id.txtTitle);
            cbFMRV2005 = view.findViewById(R.id.cbFMR_V2005);
            cbFMRV2011 = view.findViewById(R.id.cbFMR_V2011);
            cbANSIV378 = view.findViewById(R.id.cbANSI_V378);
            txtCancel = view.findViewById(R.id.txtCancel);
            txtSave = view.findViewById(R.id.txtSave);
        }
    }
}
