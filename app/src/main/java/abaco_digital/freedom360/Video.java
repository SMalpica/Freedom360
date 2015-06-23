package abaco_digital.freedom360;

import android.content.Context;
import android.media.MediaMetadataRetriever;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Autor: Sandra Malpica Mallo
 *
 * Fecha: 23/06/2015
 *
 * Clase: Video.java
 *
 * Comments: abstraction of a video. Used in the listview adapter to take
 * the information needed in the row views
 */
public class Video {
    private String imagen;

    public Video(String img, Context context){
        this.imagen = img;
        //make sure that the image exists. If not, create one with the first video image
        int imgId = context.getResources().getIdentifier(img, "drawable", context.getPackageName());
        if(imgId == 0){ //img was not found
            auxiliar.crearImagen(img,context);//create image sample
        }
    }

    public String getImagen(){
        return this.imagen;
    }

    public void setImagen(String nuevaImg){
        this.imagen= nuevaImg;
    }
}
