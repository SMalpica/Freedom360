package abaco_digital.freedom360;
/**
 * Autor: Sandra Malpica Mallo
 *
 * Fecha: 23/06/2015
 *
 * Clase: GaleriaPrincipal.java
 *
 * Comments: main activity of the app. Takes care of the inicializations
 * of the launch screen and sets different layouts depending on the device
 * used.
 */
//TODO: imagenes galeria se recortan en tablet. arreglarlo
//TODO: las imagenes not available no se ven en todos los moviles. Revisar
//TODO: guardar los dos primeros videos en res/raw y crear capturas en res/drawable
//TODO: gestionar en la descarga del video si hay espacio con getFreeSpace() y getTotalSpace() o capturar IOException si no se cuanto ocupara
//TODO: borrar videos con longclic
//TODO: descarga de videos con clic en /drawable/mas
//TODO: efecto deslizante en el scroll mas alla del ultimo elemento en cada lado. Buscar como o si es posible
//TODO: doble tapback para salir de la aplicacion?
import abaco_digital.freedom360.util.SystemUiHider;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.view.Display;
import android.widget.TextView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;


/**
 * horizontal full-screen
 *
 * @see SystemUiHider
 */
public class GaleriaPrincipal extends Activity {


    @Override
    @TargetApi(21)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auxiliar.principal = this;
        //full-screen
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //horizontal orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        final View contentView = findViewById(android.R.id.content);
        contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE);
        //change default typeface
        Typeface face= Typeface.createFromAsset(getAssets(), auxiliar.fuente);


        //use different layouts depending on the screen size
        LinearLayout inferior;
        if(auxiliar.isTablet(getApplicationContext())){ //tablet layout
            setContentView(R.layout.activity_galeria_principal);
            //Measure of the screen
            Display display = getWindowManager().getDefaultDisplay();
            DisplayMetrics outMetrics = new DisplayMetrics();
            display.getMetrics(outMetrics);
            //assign layout through id
            LinearLayout superior =(LinearLayout)findViewById(R.id.layoutSuperior);
            inferior=(LinearLayout)findViewById(R.id.layoutInferior);
            //half the space for the gallery
            superior.getLayoutParams().height=outMetrics.heightPixels/6;
            inferior.getLayoutParams().height=outMetrics.heightPixels*3/6;
            HorizontalListView galeria = (HorizontalListView)findViewById(R.id.galeria);
            galeria.setMinimumHeight(inferior.getHeight());
            //force text and image have the same height in xml
            TextView texto = (TextView)findViewById(R.id.editText);
            texto.setMaxHeight(outMetrics.heightPixels * 2 / 6); //textview max height
            //typeface serif (droid)
            texto.setTypeface(face);
            auxiliar.setFuente(texto);
            texto=(TextView)findViewById(R.id.textView);
            texto.setTypeface(face);

        }else{                                      //smartphone layout
            setContentView(R.layout.movil_galeria_principal);
            //typeface serif (droid)
            TextView texto = (TextView)findViewById(R.id.textView);
            texto.setTypeface(face);
            //make the scroll background crop instead of stretching in xml
        }

        //fill the gallery with the available videos
        ArrayList<Video> lista = fillData(getApplicationContext());
        HorizontalListView lv = (HorizontalListView)findViewById(R.id.galeria);
        Log.e("LISTA_LENGTH",String.valueOf(lista.size()));
        lv.setAdapter(new VideoAdapter(getApplicationContext(),lista));
    }

    /**/
    private ArrayList<Video> fillData(Context context){
        File f = auxiliar.directorio;
        String[] nombres = f.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.contains(auxiliar.extension);
            }
        });
        //obtain the videos
        ArrayList<Video> salida = new ArrayList<Video>();
        if(nombres!=null){
            for(String s:nombres){
                Log.d("FILL_DATA_FILES", s);
                Video aux = new Video(s,context);
                aux.setPath(f.getPath()+"/"+s);
                aux.crearFrameSample();
                salida.add(aux);
            }
        }
        //TODO: add the two sample videos
        Video aux = new Video("predef1",context);
        salida.add(aux);
        aux = new Video("predef2",context);
        salida.add(aux);
        aux = new Video("mas",context);
        salida.add(aux);
        return salida;
    }
}
