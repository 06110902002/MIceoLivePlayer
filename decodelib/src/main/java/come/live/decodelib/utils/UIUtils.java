package come.live.decodelib.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;

/**
 * Copyright@NIO Since 2014
 * CreateTime  : 2022/10/18 16:07
 * Author      : rambo.liu
 * Description :
 */
public class UIUtils {

    public static int getScreenWidth(Activity context) {
        WindowManager manager = context.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    public static int getScreenHeight(Activity context) {
        WindowManager manager = context.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }


    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
    
    public static boolean isPad(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
        double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
        double screenInches = Math.sqrt(x + y);
        return screenInches >= 7.0;
    }



    public static Bitmap drawable2bitmap(Drawable drawable){
        return ((BitmapDrawable) drawable).getBitmap();
    }

    public Drawable bitmap2Drawable(Context context,Bitmap bitmap){
        return new BitmapDrawable(context.getResources(), bitmap);
    }

    public static Bitmap bytes2Bitmap(byte[] bytes, BitmapFactory.Options opts) {
        if (null != opts) {
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
        } else {
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
    }

    public static byte[] bitmap2Bytes(Bitmap bitmap) {
        if (null != bitmap) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);    //注意压缩png和jpg的格式和质量
            return baos.toByteArray();
        }
        return null;
    }

    public static String byte2Base64(byte[] buffer) {
        return Base64.encodeToString(buffer, Base64.DEFAULT);
    }

    public static String getAppIconBase64String(Drawable iconDrawable) {
        if (iconDrawable != null) {
            Bitmap bitmap = Bitmap.createBitmap(iconDrawable.getIntrinsicWidth(),
                    iconDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            final Canvas canvas = new Canvas(bitmap);
            iconDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            iconDrawable.draw(canvas);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            bitmap.recycle();
            return byteArray != null && byteArray.length > 0 ? Base64.encodeToString(byteArray, Base64.DEFAULT) : null;
        }
        return null;

    }


    public static Bitmap base642Bitmap(String encodedImage) {
        if (encodedImage != null) {
            byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        }
        return null;
    }

}
