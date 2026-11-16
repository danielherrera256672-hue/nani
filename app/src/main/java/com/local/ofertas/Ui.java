package com.local.ofertas;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.NumberFormat;
import java.util.Locale;

public final class Ui {
    public static final int NAVY = Color.rgb(20, 42, 70);
    public static final int GREEN = Color.rgb(18, 135, 77);
    public static final int RED = Color.rgb(184, 45, 45);
    public static final int AMBER = Color.rgb(178, 109, 0);
    private Ui() {}
    public static int dp(Context c, int v) { return (int) (v * c.getResources().getDisplayMetrics().density + .5f); }
    public static LinearLayout column(Context c, int pad) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(c,pad), dp(c,pad), dp(c,pad), dp(c,pad));
        return l;
    }
    public static TextView label(Context c, String text, int size, boolean bold) {
        TextView t = new TextView(c); t.setText(text); t.setTextSize(size); t.setTextColor(NAVY);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setPadding(0, dp(c,7), 0, dp(c,3)); return t;
    }
    public static EditText decimal(Context c, String value) {
        EditText e = new EditText(c);
        e.setSingleLine(true);
        e.setTextSize(16);
        e.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        e.setText(value); e.setSelectAllOnFocus(true);
        return e;
    }
    public static double number(EditText e) {
        try { return Double.parseDouble(e.getText().toString().trim().replace(',', '.')); }
        catch (Exception ex) { return Double.NaN; }
    }
    public static boolean valid(double n) { return Double.isFinite(n) && n >= 0; }
    public static Button button(Context c, String text) {
        Button b = new Button(c); b.setText(text); b.setAllCaps(false); return b;
    }
    public static GradientDrawable background(int color, int radius, Context c) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(c,radius)); return d;
    }
    public static String money(double n) {
        NumberFormat f = NumberFormat.getNumberInstance(new Locale("es", "CO"));
        f.setMaximumFractionDigits(0); f.setMinimumFractionDigits(0);
        return "$ " + f.format(n);
    }
    public static String one(double n) {
        return String.format(new Locale("es", "CO"), "%.1f", n);
    }
}
