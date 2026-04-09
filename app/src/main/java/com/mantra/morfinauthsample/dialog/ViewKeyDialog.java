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
import android.widget.TextView;

import com.mantra.morfinauthsample.R;

@SuppressWarnings("ALL")
public class ViewKeyDialog extends Dialog {
    public ViewHolder holder;

    public ViewKeyDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_view_key);

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
        public TextView txtKey;
        public TextView txtSave;
        public TextView txtCancel;

        ViewHolder(View view) {
            txtTitle = view.findViewById(R.id.txtTitle);
            txtKey = view.findViewById(R.id.txtKey);
            txtSave = view.findViewById(R.id.txtSave);
            txtCancel = view.findViewById(R.id.txtCancel);
        }
    }
}
