package abaco_digital.freedom360;
/**
 * Autor: Sandra Malpica Mallo
 *
 * Fecha: 22/06/2015
 *
 * Clase: GaleriaPrincipal.java
 *
 * Comments: aux. class, used to contain useful or test methods.
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;

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

    /**
     * returns the device screen's inches
     * @param context
     * @return
     */
    public static double getPulgadas (Context context){
        try{
            //compute screen size
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            float screenWidth = dm.widthPixels / dm.xdpi;
            float screenHeight = dm.heightPixels / dm.ydpi;
            double size = Math.sqrt(Math.pow(screenWidth,2)+Math.pow(screenHeight,2));
            return size;
        }catch(Throwable t){
            Log.e("LOG", "Failed to compute screen size", t);
            return -1;
        }
    }

    public static double getCentimetros (Context context){
        double pulg = getPulgadas(context);
        return pulg*2.54;
    }

/*  No funciona!!
    public static double getFuente (Context context){
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        float screenWidth = dm.widthPixels / dm.xdpi;
        float screenHeight = dm.heightPixels / dm.ydpi;
        double menor;
        if(screenWidth <= screenHeight){
            menor = screenWidth;
        }else{
            menor=screenHeight;
        }
        //obtener taman fuente
        return menor*50/1760;
    }*/
    public static void setFuente (TextView textView){
        //obtain the views size
        textView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int widht = textView.getMeasuredWidth();
        int height = textView.getMeasuredHeight();
        Rect out = new Rect();
        textView.getDrawingRect(out);

    }

    /**
     * This method creates a frame sample from the video named <img> and then stores
     * that frame in the drawable folder as a jpeg.
     * @param img
     * @param context
     */
    public static void crearImagen(String img, Context context){
        //obtain the path
        String path = "videos/"+img;
        FileDescriptor fd = null;

        try{//take the file descriptor of the video
            fd = context.getAssets().openFd(path).getFileDescriptor();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        MediaMetadataRetriever md = new MediaMetadataRetriever();
        try {//take a frame sample for the video
            md.setDataSource(fd);
            Bitmap bmp = md.getFrameAtTime();
            try{//store the bitmap in the drawable folder
                File file = new File (context.getPackageResourcePath()+"/drawable/"+img);
                FileOutputStream out = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, out); // saving the Bitmap to a file compressed as a JPEG with 100% compression rate
                out.flush();
                out.close(); //close the stream
                MediaStore.Images.Media.insertImage(context.getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
            }catch(Exception ex){ex.printStackTrace();}
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        } finally {
            try {
                md.release();
            } catch (RuntimeException ex) {
            }
        }


    }
}
