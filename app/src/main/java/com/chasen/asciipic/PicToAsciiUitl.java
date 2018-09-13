package com.chasen.asciipic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;

/**
 * @Author Chasen
 * @Data 2018/9/12
 *
 * 将图片转换成Ascii
 * 将Ascii转成图片
 *
 */
public class PicToAsciiUitl {

    public static final String TAG = "PicToAsciiUtil";

    /**
     * 创建一个字符Bimap
     * @param image Bitmap，原始图片
     * @param context Context
     * @return 转成Ascii图片再转成Bitmap并返回
     */
    public static Bitmap createAsciiPic(Bitmap bitmap, Context context) {
        // 字符串由复杂到简单
        final String base = "#8XOHLTI)i=+;:,.";
        StringBuilder text = new StringBuilder();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int width0 = bitmap.getWidth();
        int height0 = bitmap.getHeight();
        int width1, height1;
        int scale = 7;
        if (width0 <= width / scale) {
            width1 = width0;
            height1 = height0;
        } else {
            width1 = width / scale;
            height1 = width1 * height0 / width0;
        }
        //读取图片
        bitmap = scale(bitmap, width1, height1);
        //输出到指定文件中
        for (int y = 0; y < bitmap.getHeight(); y += 2) {
            for (int x = 0; x < bitmap.getWidth(); x++) {
                final int pixel = bitmap.getPixel(x, y);
                final int r = (pixel & 0xff0000) >> 16, g = (pixel & 0xff00) >> 8, b = pixel & 0xff;
                final float gray = 0.299f * r + 0.578f * g + 0.114f * b;
                final int index = Math.round(gray * (base.length() + 1) / 255);
                String s = index >= base.length() ? " " : String.valueOf(base.charAt(index));
                text.append(s);
            }
            text.append("\n");
        }
        return textAsBitmap(text, context);
    }

    /**
     * 缩放
     * @param bitmap 原始Bitmap
     * @param newWidth 新的宽
     * @param newHeight 新的高
     * @return 缩小后的Bitmap
     */
    public static Bitmap scale(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap ret = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        return ret;
    }

    /**
     * 将Ascii字符转成Bitmap
     * @param text Ascii字符
     * @param context Context
     * @return 生成的Ascii字符Bitmap
     */
    public static Bitmap textAsBitmap(StringBuilder text, Context context) {
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.BLACK);
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.MONOSPACE);
        textPaint.setTextSize(12);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        StaticLayout layout = new StaticLayout(text, textPaint, width,
                Layout.Alignment.ALIGN_CENTER, 1f, 0.0f, true);
        Bitmap bitmap = Bitmap.createBitmap(layout.getWidth() + 20,
                layout.getHeight() + 20, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.translate(10, 10);
        canvas.drawColor(Color.WHITE);
        layout.draw(canvas);
        Log.d("textAsBitmap", String.format("1:%d %d", layout.getWidth(), layout.getHeight()));
        return bitmap;
    }

    /**
     * 压缩Bitmap
     * @param bitmap 原始的Bitmap
     * @return 压缩过后的Bitmap
     */
    public static Bitmap zipBitmap(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inSampleSize = 2;
        Bitmap aftBm = BitmapFactory.decodeByteArray(bos.toByteArray(), 0, bos.toByteArray().length, options);
        return aftBm;
    }

}
