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
//imagenes galeria se recortan en tablet. arreglarlo. HECHO
// las imagenes not available no se ven en todos los moviles. Revisar HEDHO
// guardar los dos primeros videos en res/raw y crear capturas en res/drawable. Hecho
//TODO: gestionar en la descarga del video si hay espacio con getFreeSpace() y getTotalSpace() o capturar IOException si no se cuanto ocupara
//notTODO: borrar videos con longclic.
//TODO: actualizar la galeria al borrar un video
// ojo con las url al descargar. control. si no tiene protocolo anyadir http y a correr. A correr hecho. http hecho
//TODO: gestionar errores con dialog y mensajes al usuario. En ello. Falta el de tamano video video
//TODO: efecto deslizante en el scroll mas alla del ultimo elemento en cada lado. Buscar como o si es posible
//TODO: doble tapback para salir de la aplicacion? Uno vale tambien
// keyboard shows in tablet but not in smartphone (dialog editText). Arreglado a lo bestia
//notTODO: asegurarse de que la pantalla no se bloquea al reproducir un video
// ahora en el movil no se ve el fondo del horizontallistview. Arreglado. Faltaban las carpetas drawable dpi
// eliminar texto del dialog cuando el usuario pulsa sobre el. HECHO
// coger la imagen para los videos que no son predefinidos. Hecho

import abaco_digital.freedom360.util.SystemUiHider;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.Display;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * horizontal full-screen
 *
 * @see SystemUiHider
 */
public class GaleriaPrincipal extends Activity {

    private VideoAdapter videoAdapter;
    private AsyncVideoDownloader videoDownloader;
    private ArrayList<Video> lista;
    private HorizontalListView lv;

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
        videoDownloader = new AsyncVideoDownloader();

        //use different layouts depending on the screen size
        FrameLayout inferior;
        if(auxiliar.isTablet(getApplicationContext())){                         //tablet layout
            setContentView(R.layout.activity_galeria_principal);
            //Measure of the screen
            Display display = getWindowManager().getDefaultDisplay();
            DisplayMetrics outMetrics = new DisplayMetrics();
            display.getMetrics(outMetrics);
            //assign layout through id
            LinearLayout superior =(LinearLayout)findViewById(R.id.layoutSuperior);
            inferior=(FrameLayout)findViewById(R.id.layoutInferior);
            //half the space for the gallery
            superior.getLayoutParams().height=outMetrics.heightPixels/6;
//            inferior.getLayoutParams().height=outMetrics.heightPixels*3/6;
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

        }else{                                                                  //smartphone layout
            setContentView(R.layout.movil_galeria_principal);
            //typeface serif (droid)
            TextView texto = (TextView)findViewById(R.id.textView);
            texto.setTypeface(face);
            //make the scroll background crop instead of stretching in xml
        }

        //fill the gallery with the available videos
        lista = fillData(getApplicationContext());
        lv = (HorizontalListView)findViewById(R.id.galeria);
        Log.e("LISTA_LENGTH",String.valueOf(lista.size()));
        videoAdapter = new VideoAdapter(getApplicationContext(), lista);
        lv.setAdapter(videoAdapter);
    }

    /*searches for the downloaded videos to fill the app gallery*/
    public ArrayList<Video> fillData(Context context){
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
                aux.setPath(f.getPath() + "/" + s);
                aux.crearFrameSample();
                salida.add(aux);

            }
        }
        Video aux = new Video("predef1",context);
        salida.add(aux);
        aux = new Video("predef2",context);
        salida.add(aux);
        aux = new Video("mas",context);
        Log.e("FILL_DATA_LLAMADO", "added mas");
        salida.add(aux);
        return salida;
    }

    public void sacarTeclado(final EditText enlace){
        enlace.setFocusable(true);
        enlace.setFocusableInTouchMode(true);
        enlace.requestFocus();
//                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                            imm.showSoftInput(enlace, InputMethodManager.SHOW_IMPLICIT);
        enlace.postDelayed(new Runnable() {
            public void run() {
//              ((EditText) findViewById(R.id.et_find)).requestFocus();
//

//                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.showSoftInput(enlace, InputMethodManager.SHOW_IMPLICIT);

                enlace.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                enlace.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));

            }
        }, 300);
    }

    //http://stackoverflow.com/questions/5105354/how-to-show-soft-keyboard-when-edittext-is-focused
    public static void showKeyboard(Activity activity) {
        if (activity != null) {
            activity.getWindow()
                    .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    //http://stackoverflow.com/questions/5105354/how-to-show-soft-keyboard-when-edittext-is-focused
    public static void hideKeyboard(Activity activity) {
        if (activity != null) {
            activity.getWindow()
                    .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }
/**
 * Autor: Sandra Malpica Mallo
 *
 * Fecha: 30/06/2015
 *
 * Clase: VideoAdapter.java
 *
 * Comments: manages the elements within the listview of the app's main
 * screen. Inflates the rowView and fills each element with data.
 */
    public class VideoAdapter extends BaseAdapter {
        private Context contexto;
        private List<Video> list;

        public VideoAdapter(Context context, List<Video> lista){
            super();
            this.contexto = context;
            this.list = lista;
        }

        public long getItemId(int posicion){
            return (long)posicion;
        }

        public Object getItem(int posicion){
            return list.get(posicion);
        }

        public int getCount(){
            return list.size();
        }



        @Override
        public View getView(final int posicion, View convertView, final ViewGroup parent){
            convertView=null;
            Log.e("GET_VIEW", "posicion de view "+posicion);
            //get the data item for this position
            final Video video = (Video)getItem(posicion);
            // always inflate the view so that old views do not appear twice
            convertView = LayoutInflater.from(contexto).inflate(R.layout.list_item, parent, false);


            /*ImageView imagenFondo = (ImageView)findViewById(R.id.imageView2);
            imagenFondo.setImageResource(R.color.black_overlay);
            imagenFondo.setScaleType(ImageView.ScaleType.CENTER_CROP);*/
//            ((RelativeLayout) convertView).setGravity(Gravity.CENTER_VERTICAL);

            // Lookup view for data population
            ImageView item = (ImageView) convertView.findViewById(R.id.miniatura);
//            int width =204;
//            item.setMaxWidth(width);
            // Populate the data into the template view using the data object
            Log.e("VIDEO_ADAPTER_GETVIEW", video.getImagen());
            //parse the video name to get the image name
            int punto = video.getImagen().indexOf(".");
            String nombre=video.getImagen();
            if(punto != -1){
                nombre = video.getImagen().substring(0, punto);
            }
            //get the image id
            int id = contexto.getResources().getIdentifier(nombre, "drawable", contexto.getPackageName());
            if(id!=0){
                item.setImageResource(id);
                item.setScaleType(ImageView.ScaleType.CENTER_CROP);
                if(!video.getImagen().equalsIgnoreCase("mas")){
                    //take out background color
                    item.setBackgroundColor(Color.TRANSPARENT);
                    item.setPadding(0, 0, 0, 0);
                }else{
                    Log.e("SETEANDO_MAS", "entrando");
                    item.setClickable(true);
                    //set the actions to be done when the image is pressed
                    item.setOnLongClickListener(new View.OnLongClickListener() {
                        /*based on http://examples.javacodegeeks.com/android/core/ui/
                        alertdialog/android-prompt-user-input-dialog-example/ code*/
                        @Override
                        public boolean onLongClick(View v) {
                            Log.e("MAS_ONCLICK", "principio");
                            LayoutInflater layoutInflater = LayoutInflater.from(GaleriaPrincipal.this);
                            //inflate the dialog view
                            View promptView = layoutInflater.inflate(R.layout.popup, null);

                            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GaleriaPrincipal.this);

                            // set prompts.xml to be the layout file of the alertdialog builder
                            alertDialogBuilder.setView(promptView);

                            final EditText enlace = (EditText) promptView.findViewById(R.id.enlaceDescarga);
//                            http://stackoverflow.com/questions/5105354/how-to-show-soft-keyboard-when-edittext-is-focused
                            enlace.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(enlace.getText().toString().equals("insert video URL")){
                                        enlace.setText("");
                                    }
                                    Log.e("TEXTO",enlace.getText().toString());
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                }
                            });

                            // setup a dialog window
                            alertDialogBuilder
                                    .setTitle(R.string.titulo_popup)
                                            //start download when "ok" is pressed
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // get user input and set it to result
                                            Log.e("ON_CLICK", "principio");
                                            InputMethodManager imm = (InputMethodManager) GaleriaPrincipal.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.showSoftInput(enlace, InputMethodManager.SHOW_IMPLICIT);
                                            String path = enlace.getText().toString();
                                            Log.e("ON_CLICK", path);
                                            try {
                                                //method for download found in http://www.insdout.com/
                                                // snippets/descargar-archivos-desde-una-url-en-nuestra-aplicacion-android.htm
                                                URL url = new URL(path);
                                                if(url.getProtocol()==null){
                                                    path="http://"+path;
                                                    url = new URL(path);
                                                }
                                                videoDownloader = new AsyncVideoDownloader();
                                                videoDownloader.execute(url);

                                            } catch (MalformedURLException ex) {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(GaleriaPrincipal.this);
                                                builder.setTitle("Error. Bad URL. Make sure you use a valid format (ie: \"http://url.com\")")
                                                        .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                dialog.cancel();
                                                            }
                                                        });
                                                AlertDialog d = builder.create();
                                                d.show();
                                                Log.e("ON_CLICK", "malformedURL");
                                            }
                                        }
                                    })//dismiss the dialog when cancel is pressed
                                    .setNegativeButton("Cancel",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });

                            // create an alert dialog
                            AlertDialog alertD = alertDialogBuilder.create();
                            alertD.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                            alertD.show();
                            return true;
                        }
                    });
                    item.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }
            }else{
                Log.e("FRAME_SAMPLE","archivos que no estan dentro apk");
                File archivoImagen = auxiliar.obtenerArchivoImagen(video.getImagen());
                if(archivoImagen.exists()){
                    item.setImageURI(Uri.parse(archivoImagen.getAbsolutePath()));
                    item.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    item.setBackgroundColor(Color.TRANSPARENT);
                    item.setPadding(0, 0, 0, 0);
                }else{
                    Log.e("FRAME_SAMPLE","no encontrada imagen");
                    //set a text with the video's title instead
                    TextView texto = new TextView(getApplicationContext());
                    texto.setText(video.getImagen());
                    RelativeLayout padre = (RelativeLayout)findViewById(R.id.padre);
                    texto.setGravity(Gravity.CENTER_HORIZONTAL);
                    texto.setPadding(15,15,15,15);
                    if(padre!=null){padre.addView(texto);}
                }
            }
            //TODO: borrar imagenes tambien al borrar el video
            //Actualizar bien galeria al borrar el video
            if(!video.getImagen().equals("mas")){
                item.setOnLongClickListener(new View.OnLongClickListener(){
                    public boolean onLongClick (View v){
                        final File archivo = new File(auxiliar.directorio,video.getImagen());
                        if(archivo.exists()){
                            AlertDialog.Builder builder = new AlertDialog.Builder(GaleriaPrincipal.this);
                            builder.setTitle("Are you sure you want to delete "+video.getImagen()+"?")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            lv.removeViewAt(posicion);
                                            archivo.delete();
                                            videoAdapter.notifyDataSetChanged();
                                            dialog.cancel();
                                        }
                                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            AlertDialog d = builder.create();
                            d.show();
                            Log.e("ON_LONG_CLICK", "borrar video");
                            return true;
                        }else{
                            AlertDialog.Builder builder = new AlertDialog.Builder(GaleriaPrincipal.this);
                            builder.setTitle("File "+video.getImagen()+" not found. Delete Manually.")
                                    .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog d = builder.create();
                            d.show();
                            Log.e("ON_LONG_CLICK", "video no encontrado");
                            return false;
                        }
                    }
                });
                //launch activity player
                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent (GaleriaPrincipal.this, MainActivity.class);
                        //send the URI
                        intent.putExtra("TITULO",video.getImagen());
                        intent.putExtra("PATH",video.getPath());
//                        intent.putExtra("URI",video.getUri().toString());
                        Log.e("ON_CLICK","video clicado, abrir nueva actividad");
                        startActivity(intent);
                    }
                });
            }
//            item.setScaleType(ImageView.ScaleType.FIT_CENTER);
//            item.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return convertView;
        }
    }


    /**
     * Autor: Sandra Malpica Mallo
     *
     * Fecha: 1/07/2015
     *
     * Clase: AsyncVideoDownloader.java
     *
     * Comments: async task used to download the videos of the app.
     */
    public class AsyncVideoDownloader extends AsyncTask<URL,Integer,String>{
        //TODO: dar opcion de cancelar en las descargas. No necesaria
        private ProgressDialog progreso;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = new ProgressDialog(GaleriaPrincipal.this);
            progreso.setTitle("Downloading...");
            progreso.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progreso.setIndeterminate(false);
            progreso.show();
        }

        protected String doInBackground(URL... params){
            URL url;
            String nombreVideo="video1"+auxiliar.extension;
            for(int i=0; i<params.length; i++){
                try{
                    url = params[i];
                    //establish connection
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    Log.e("ON_CLICK","conexion abierta");
                    //configuration
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoOutput(true);
                    urlConnection.connect();
                    Log.e("ON_CLICK", "conexion establecida");
                    //download and get the video
                    File f = auxiliar.directorio;
                    Log.e("ON_CLICK", "file directorio creado");
                    nombreVideo=auxiliar.getNombreVideo();

                    File file = new File(f,nombreVideo);
                    Log.e("ON_CLICK", "file creado");
                    //stream to place the downloaded file
                    FileOutputStream fileOutput = new FileOutputStream(file);
                    //read data
                    Log.e("ON_CLICK", "outputStream creado");
                    InputStream inputStream = urlConnection.getInputStream();
                    Log.e("ON_CLICK", "getInputStream");
                    //get file size
                    int totalSize = urlConnection.getContentLength();
                    Log.e("ON_CLICK","longitud obtenida");
                    int downloadedSize = 0;
                    //make buffer to store data
                    byte[] buffer = new byte[1024];
                    int bufferLength;
                    //write to file
                    while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                        fileOutput.write(buffer, 0, bufferLength);
                        downloadedSize += bufferLength;
                        int porcentaje = 100*downloadedSize/totalSize;
//                        Log.e("ON_CLICK","completado "+porcentaje);
                        publishProgress(porcentaje);
                    }
                    Log.e("ON_CLICK","archivo volcado");
                    //close
                    fileOutput.close();
                    return "";
                }catch(IOException ex){
                    Log.e("ON_CLICK",ex.getMessage());
                }

            }
            return nombreVideo;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progreso.setProgress(values[0]);

        }

        @Override
        protected void onPostExecute(String result){
            Video video = new Video(result,GaleriaPrincipal.this);
            video.setPath(auxiliar.directorio.getPath()+"/"+result);
            video.setURL(result);
            video.crearFrameSample();
            lista.add(0,video);
            lv.setSelection(0);
            videoAdapter.notifyDataSetChanged();
            progreso.cancel();
            Log.e("GALLERY","setpath to the video");
        }
    }
}
