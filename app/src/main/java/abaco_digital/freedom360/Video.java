package abaco_digital.freedom360;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;

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
    private String url;
    private Uri uri;

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
        int imgId = auxiliar.existeImagen(this.imagen);
        Log.e("FRAMSE_SAMPLE", "img id " + imgId);
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

    public void setURL (String url){
        this.url = url;
    }

    public String getURL (){ return this.url;}

    public void setUri(){

    }
}
