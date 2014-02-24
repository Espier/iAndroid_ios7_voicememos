
package org.espier.voicememos7.util;

import android.content.Context;
import android.util.DisplayMetrics;

public class ScalePx {
    private static int IPHONE4_WIDTH_PX = 640;
    private static int IPHONE4_HEIGHT_PX = 960;
    private static int IPHONE4_DPI = 326;
    private static float IPHONE4_DENSITY = 2.0f; // IPHONE4_DPI / 160

    private static int LOCAL_WIDTH_PX = 0;
    private static int LOCAL_HEIGHT_PX = 0;
    private static int LOCAL_DPI = 0;
    private static float LOCAL_DENSITY = 0.0f;

    private static boolean USE_SCALE_PX = false;

    // scale = DisplayMetrics.density
    public static int dip2px(float dipValue, float scale) {
        return (int) (dipValue * scale + 0.5f);
    }

    public static int px2dip(float pxValue, float scale) {
        return (int) (pxValue / scale + 0.5f);
    }

    public static void setScaleAlgorithm(boolean use_scale_px) {
        USE_SCALE_PX = use_scale_px;
    }

    public static int scalePx(Context context, int px) {
        if (LOCAL_DPI == 0) {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            if (dm.widthPixels > dm.heightPixels) {
                LOCAL_WIDTH_PX = dm.heightPixels;
                LOCAL_HEIGHT_PX = dm.widthPixels;
            } else {
                LOCAL_WIDTH_PX = dm.widthPixels;
                LOCAL_HEIGHT_PX = dm.heightPixels;
            }
            LOCAL_DPI = dm.densityDpi;
            LOCAL_DENSITY = dm.density;
        }

        return USE_SCALE_PX ? scalePxByIphonePx(px) : scalePxByIphneDensity(px);
    }

    public static int scalePxByIphonePx(int px) {
        return (int) ((float) px / IPHONE4_WIDTH_PX * LOCAL_WIDTH_PX);
    }

    public static int scalePxByIphneDensity(int px) {
        return dip2px(px2dip(px, IPHONE4_DENSITY), LOCAL_DENSITY);
    }
}
