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
import android.widget.TextView;

import com.mantra.morfinauthsample.R;

@SuppressWarnings("ALL")
public class ShowTimerDialog extends Dialog {

    public ViewHolder holder;

    public ShowTimerDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_show_timer);

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
        public TextView txtslmode;
        public TextView txtoffmode;
        public TextView txtadvtime;
        public TextView txtSave;

        ViewHolder(View view) {
            txtslmode = view.findViewById(R.id.txtslmode);
            txtoffmode = view.findViewById(R.id.txtoffmode);
            txtadvtime = view.findViewById(R.id.txtadvtime);
            txtSave = view.findViewById(R.id.txtSave);
        }
    }
}
