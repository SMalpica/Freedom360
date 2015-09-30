package abaco_digital.freedom360;
/**
 * Autor: Sandra Malpica Mallo
 *
 * Fecha: 22/06/2015
 *
 * Clase: auxiliar.java
 *
 * Comments: aux. class, used to contain useful or test methods.
 */
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class auxiliar {

    protected final static String fuente = "fonts/DroidSerif-Regular.ttf";  //font used in the app's text
    protected final static String carpeta = "videos360";                    //app folder name
    protected final static File directorio = encontrarDirectorio();         //app's directory
    protected final static String extension = ".mp4";                       //videos extension
    protected static Activity principal=null;                               //reference to main activity

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
     * This method creates a frame sample from the video named <img> and then stores
     * that frame in the drawable folder as a jpeg.
     * @param video video
     * @param context context
     */
    public static void crearImagen(Video video, Context context){
        File file = new File(directorio,video.getImagen());
        Log.d("FRAME_SAMPLE", "path del video " + file.getPath());
        FFmpegMediaMetadataRetriever md = new FFmpegMediaMetadataRetriever();
        Bitmap bmp = null;

        try{
            //set the manager's data source
            md.setDataSource(file.getAbsolutePath());
            //take frame, second 2, closest sync frame found
            bmp = md.getFrameAtTime(2000000, FFmpegMediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            md.release();   //release the manager
        }catch(IllegalArgumentException ex){Log.e("METADATA","illegal argument");}

        //if frame could be taken
        if(bmp != null){
            //get the aproximated size of the image we want
            DisplayMetrics dm = new DisplayMetrics();
            Display display = principal.getWindowManager().getDefaultDisplay();
            display.getMetrics(dm);
            int height = (int)dm.density*dm.heightPixels*5/6;
            int width = height*2/3;
            try {
                //rescale the frame sample
                Bitmap background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                float originalWidth = bmp.getWidth(), originalHeight = bmp.getHeight();
                Canvas canvas = new Canvas(background);
                float scale = width/originalWidth;
                float xTranslation = 0.0f, yTranslation = (height - originalHeight * scale)/2.0f;
                Matrix transformation = new Matrix();
                transformation.postTranslate(xTranslation, yTranslation);
                transformation.preScale(scale, scale);
                Paint paint = new Paint();
                paint.setFilterBitmap(true);
                canvas.drawBitmap(bmp, transformation, paint);
                try{//store the bitmap in the pictures folder
                    //obtain a file to store the image
                    File files = obtenerArchivoImagen(video.getImagen());
                    FileOutputStream out = new FileOutputStream(files);
                    //create the bitmap
                    Bitmap mitad = Bitmap.createBitmap(bmp,(int)(originalWidth/2 - originalHeight/2),0,(int)originalHeight,(int)originalHeight);
                    //scale the bitmap
                    Bitmap escalado = Bitmap.createScaledBitmap(mitad,width,height,false);
                    escalado.compress(Bitmap.CompressFormat.JPEG, 50, out);
                    out.flush();
                    out.close(); //close the stream
                    //store the image
                    MediaStore.Images.Media.insertImage(context.getContentResolver(), files.getAbsolutePath(), files.getName(), files.getName());
                }catch(Exception ex){
                    Log.e("CAUSA_FILE",ex.getMessage());ex.printStackTrace();}
            } catch (IllegalArgumentException ex) {
                Log.e("FRAME_SAMPLE", "error illegalArgument");
            } catch (RuntimeException ex) {
                Log.e("FRAME_SAMPLE", "error runtimeException");
            }finally {
                try {
                    md.release();   //release the manager
                } catch (RuntimeException ignored) {}
            }
        }
    }

    /**
     * This method returns the file where the video frameSample should be stored, if exists.
     * @param nombreVideo
     * @return
     */
    public static File obtenerArchivoImagen(String nombreVideo){
        File directorio;
        //obtain the external or internal available directory
        if(auxiliar.isExternalStorageWritable()){
            directorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        }else{
            directorio = Environment.getDataDirectory();
        }
        //create directory
        File f = new File(directorio.getPath()+"/"+auxiliar.carpeta);
        if(!f.exists()){
            f.mkdirs();
        }
        if(nombreVideo.contains(auxiliar.extension)){
            nombreVideo = nombreVideo.substring(0,nombreVideo.indexOf("."));
        }
        File file = new File(f,nombreVideo+".jpeg");
        return file;
    }

    /**
     * This method checks whether the video frame sample has been created or not.
     * @param nombreVideo
     * @return
     */
    public static int existeImagen(String nombreVideo){
        if(nombreVideo.contains(auxiliar.extension)){
            nombreVideo = nombreVideo.substring(0,nombreVideo.indexOf("."))+".jpeg";
        }
        File directorio;
        //obtain the external or internal available directory
        if(auxiliar.isExternalStorageWritable()){
            directorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        }else{
            directorio = Environment.getDataDirectory();
        }
        //create the file
        File f = new File(directorio.getPath()+"/"+auxiliar.carpeta);
        if(!f.exists()){
            f.mkdirs();
        }
        String[] nombres = f.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.contains("video_");
            }
        });
        if (nombres!=null){
            for(int i=0; i<nombres.length; i++){
                if (nombres[i].equalsIgnoreCase(nombreVideo)){
                    return 1;
                }
            }
        }
        return 0;
    }

    /**
     *
     * @return name for a new downloaded video
     */
    public static String getNombreVideo(){
        int ultimo = 1;
        File directorio = encontrarDirectorio();
        String[] nombres = directorio.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.contains(auxiliar.extension);
            }
        });
        if(nombres!=null){
            for(int i=0; i<nombres.length; i++){
                int slash = nombres[i].lastIndexOf("_");
                int dot = nombres[i].lastIndexOf(".");
                int aux=0;
                if(slash!=-1 && dot!=-1){
                    try {
                        aux = Integer.parseInt(nombres[i].substring(slash + 1, dot));
                    }catch(NumberFormatException ex){
                        Log.e("ON_CLICK","nombre de video no valido");
                    }
                }
                if(aux > ultimo){
                    ultimo = aux;
                }
            }
        }
        return "video_"+(ultimo+1)+auxiliar.extension;
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * returns the directory where the app is going to store videos
     * @return
     */
    public static File encontrarDirectorio(){
        File directorio;
        //obtain the external or internal available directory
        if(auxiliar.isExternalStorageWritable()){
            Log.d("TABLET","external storage is writable");
            Log.d("TABLET","media state "+Environment.getExternalStorageState());
            directorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        }else{
            Log.d("TABLET","external storage is not writable");
            directorio = Environment.getDataDirectory();
        }
        File f = new File(directorio.getPath()+"/"+auxiliar.carpeta);
        if(!f.exists()){
            f.mkdirs();
        }
        return f;
    }
}
