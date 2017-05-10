package com.melon.mylibrary.util;

import android.app.Activity;
import android.app.Application;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by admin on 2016/5/17.
 */
public class CommonUtil {
    /**
     * 启动Activity
     *
     * @param ctx   上下文
     * @param clazz 要跳转页面
     */
    public static void enterActivity(Context ctx, Class<?> clazz) {
        Intent intent = new Intent(ctx, clazz);
        if (ctx instanceof Application) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        ctx.startActivity(intent);
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int getScreenHeight(Context ctx) {
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        display.getMetrics(metrics);
        return metrics.heightPixels;
    }

    public static int getScreenWidth(Context ctx) {
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        display.getMetrics(metrics);
        return metrics.widthPixels;
    }

    //全屏
    public static void fullScreen(Activity act) {
        act.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 空判断
     */
    public static boolean isEmpty(CharSequence str) {
        if (str == null || str.length() == 0 || "null".equalsIgnoreCase(str.toString()))
            return true;
        else
            return false;
    }

    /**
     * 计算图片高度
     */
    public static int getPicHeight(Context ctx, int img) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeResource(ctx.getResources(), img, options);
        return options.outHeight;
    }

    /**
     * 复制到剪切板
     */
    public static void addToClipboard(Context ctx, String value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ClipboardManager cmb2 = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
            cmb2.setText(value);
            ToastUtil.toast(ctx, "已复制");
        }
    }

    /**
     * 隐藏软键盘
     *
     * @param activity
     * @param flag     true    隐藏
     */
    public static void hideInputMode(Activity activity, boolean flag) {
        if (flag) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            View view = activity.getCurrentFocus();
            if (view != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } else {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            View view = activity.getCurrentFocus();
            if (view != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
            }
        }
    }
}
