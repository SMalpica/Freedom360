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
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;

public class auxiliar {

    protected final static String fuente = "fonts/DroidSerif-Regular.ttf";
    protected final static String carpeta = "videos360";
    protected final static File directorio = encontrarDirectorio();
    protected final static String extension = ".mp4";
    protected static Activity principal=null;

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
     * @param context context
     * @return inches
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
    public static void setFuente (TextView textView){
        //obtain the views size
        textView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Rect out = new Rect();
        textView.getDrawingRect(out);

    }

    /**
     * This method creates a frame sample from the video named <img> and then stores
     * that frame in the drawable folder as a jpeg.
     * @param video video
     * @param context context
     */
    public static void crearImagen(Video video, Context context){
        Log.e("FRAME_SAMPLE","en crear imagen");
        MediaMetadataRetriever md = new MediaMetadataRetriever();
        String path = video.getPath();  //get the video path
        Log.e("FRAME_SAMPLE","path del video "+video.getPath());
        int width =204;
        int height = 300;
        try {
            Uri uri = Uri.parse(path);
            md.setDataSource(context, uri);
            Bitmap bmp = md.getFrameAtTime(2000);   //get the image
            Log.e("FRAME_SAMPLE","aqui no llego");
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
            Log.e("FRAME_SAMPLE", "fin redimensionado");
            try{//store the bitmap in the drawable folder
                Log.e("FRAME_SAMPLE","nombre del video "+video.getImagen());

                File file = obtenerArchivoImagen(video.getImagen());
                Log.e("FRAME_SAMPLE",file.getPath());
//                File file = new File (path);
                Log.d("CREAR_IMAGEN3","file creado");
                FileOutputStream out = new FileOutputStream(file);
                Log.d("CREAR_IMAGEN3","outputStream creado");
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, out); // saving the Bitmap to a file compressed as a JPEG with 100% compression rate
                Log.d("CREAR_IMAGEN3", "bmp compressed");
                out.flush();
                out.close(); //close the stream
                Log.d("CREAR_IMAGEN3", "outputStream closed");
                MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
                Log.d("CREAR_IMAGEN3", "imagen guardada");
            }catch(Exception ex){
                Log.e("CAUSA_FILE",ex.getMessage());ex.printStackTrace();}
        } catch (IllegalArgumentException ex) {
            Log.e("FRAME_SAMPLE","error illegalArgument");
            System.out.println("causa fd ilegal");ex.printStackTrace();
        } catch (RuntimeException ex) {
            System.out.println("causa fd runtime");ex.printStackTrace();
            Log.e("FRAME_SAMPLE", "error runtimeException");
        }finally {
            try {
                md.release();
            } catch (RuntimeException ignored) {}
        }


    }

    public static File obtenerArchivoImagen(String nombreVideo){
        File directorio;
        //obtain the external or internal available directory
        if(auxiliar.isExternalStorageWritable()){
            directorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        }else{
            directorio = Environment.getDataDirectory();
        }
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
        File f = new File(directorio.getPath()+"/"+auxiliar.carpeta);
        if(!f.exists()){
            f.mkdirs();
        }
        Log.e("FRAME_SAMPLE","directorio imagen "+f.getPath());
        Log.e("FRAME_SAMPLE","nombre imagen "+nombreVideo);
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
     * @return name of a new video
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
            directorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        }else{
            directorio = Environment.getDataDirectory();
        }
        File f = new File(directorio.getPath()+"/"+auxiliar.carpeta);
        if(!f.exists()){
            f.mkdirs();
        }
        return f;
    }
}
