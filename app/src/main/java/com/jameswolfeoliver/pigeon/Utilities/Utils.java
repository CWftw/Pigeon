package com.jameswolfeoliver.pigeon.Utilities;



public class Utils {
    public static int convertToPixels(double sizeInDp) {
        float scale = PigeonApplication.getAppContext().getResources().getDisplayMetrics().density;
        return (int) (sizeInDp*scale + 0.5f);
    }

    public static float convertToDp(int sizeInPixels) {
        int densityDpi = PigeonApplication.getAppContext().getResources().getDisplayMetrics().densityDpi;
        return sizeInPixels / (densityDpi / 160f);
    }
}
