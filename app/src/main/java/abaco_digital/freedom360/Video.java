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
    private String path;
    private FileDescriptor fileDescriptor;
    private Context context;
    private int id;

    public Video(String img, Context context){
        this.imagen = img;
        this.context=context;
    }

    public void setPath(String p){
        this.path=p;
    }

    public String getPath(){
        return this.path;
    }

    public void crearFrameSample(){
        //make sure that the image exists. If not, create one with the first video image
        int imgId = context.getResources().getIdentifier(this.imagen, "drawable", context.getPackageName());
        if(imgId == 0){ //img was not found
          auxiliar.crearImagen(this,context);//create image sample
        }
    }

    public String getImagen(){
        return this.imagen;
    }

    public void setImagen(String nuevaImg){
        this.imagen= nuevaImg;
    }

    public void setFD(FileDescriptor fd){ this.fileDescriptor = fd; }

    public FileDescriptor getFD(){ return this.fileDescriptor;}

    public void setID(int id){this.id = id;}

    public int getID(){return this.id;}
}
