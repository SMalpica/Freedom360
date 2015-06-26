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
//TODO: las imagenes not available no se ven en todos los moviles. Arreglarlo.
import abaco_digital.freedom360.util.SystemUiHider;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.view.Display;
import android.widget.TextView;

import java.io.File;
import java.io.FileDescriptor;
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
        //full-screen
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //horizontal orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        final View contentView = findViewById(android.R.id.content);
        contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE);
        //change default typeface
        Typeface face= Typeface.createFromAsset(getAssets(), "fonts/DroidSerif-Regular.ttf");


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
        ArrayList<Video> salida = new ArrayList<Video>();
            Field[] fields = R.raw.class.getFields();
            for(Field f:fields){
                Log.d("FILL_DATA_FILES", f.getName());
                    Video aux = new Video(f.getName(),context);
                    int id = getApplicationContext().getResources().getIdentifier(f.getName(), "raw", getApplicationContext().getPackageName());
                    if(id!=0){
                        aux.setID(id);
                        aux.crearFrameSample();
                    }
                    salida.add(aux);
            }
            Video aux = new Video("mas",context);
            salida.add(aux);
            return salida;
    }
}
