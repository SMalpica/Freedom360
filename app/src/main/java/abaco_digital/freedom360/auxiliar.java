package abaco_digital.freedom360;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * Created by android on 16/06/2015.
 */
public class auxiliar {

    /*it sets whether the device is a tablet or a phone depending on the screen size
    * this method is intended to be used to switch between different layouts
    * code found in http://stackoverflow.com/questions/8148749/use-different-theme-depending-on-if-device-is-android-tablet-or-phone*/
    public static boolean isTablet(Context context) {
        try {
            // Compute screen size
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            float screenWidth  = dm.widthPixels / dm.xdpi;
            float screenHeight = dm.heightPixels / dm.ydpi;
            double size = Math.sqrt(Math.pow(screenWidth, 2) +
                    Math.pow(screenHeight, 2));
            // Tablet devices should have a screen size greater than 6 inches
            return size >= 6;
        } catch(Throwable t) {
            Log.e("LOG", "Failed to compute screen size", t);
            return false;
        }

    }
}
