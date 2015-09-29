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
//TODO: gestionar en la descarga del video si hay espacio con getFreeSpace() y getTotalSpace() o capturar IOException si no se cuanto ocupara con dialog
//notTODO: borrar videos con longclic.
//notTODO: actualizar la galeria al borrar un video
// ojo con las url al descargar. control. si no tiene protocolo anyadir http y a correr. A correr hecho. http hecho
//notTODO: gestionar errores con dialog y mensajes al usuario. En ello. Falta el de tamano video
//notTODO: efecto deslizante en el scroll mas alla del ultimo elemento en cada lado. Buscar como o si es posible
//notTODO: doble tapback para salir de la aplicacion? Uno vale tambien
// keyboard shows in tablet but not in smartphone (dialog editText). Arreglado a lo bestia
//notTODO: asegurarse de que la pantalla no se bloquea al reproducir un video
// ahora en el movil no se ve el fondo del horizontallistview. Arreglado. Faltaban las carpetas drawable dpi
// eliminar texto del dialog cuando el usuario pulsa sobre el. HECHO
// coger la imagen para los videos que no son predefinidos. Hecho
//notTODO: en la tablet no se cargan las imagenes de los videos descargados ni se puede acceder a los videos descargados
//notTODO: imagen de videos descargados es demasiado ancha //notTODO: ahora en el movil se ve demasiado estrecho
//notTODO: funcionar de forma distinta dependiendo del tamaño para arreglarlo
//notTODO: eventos en el drag no funcionan
//notTODO: probar que en la cadena de conexion esta el http
//notTODO: no se pueden borrar los videos (?)
//notTODO: esconder el teclado al salir de la descarga
//notTODO: dejar más espacio entre el borde del videoControlView y los elementos(botones y tiempo), alinear tiempo y botones
//notTODO: borrar imagenes(del almacenamiento del dispositivo) al borrar el video
//nottodo: copiar clicklistener de mas al onlongclicklistener
//TODO: eliminar codigo muerto, aligerar la aplicacion
//TODO: utilizar SDcard si se encuentra disponible
//notTODO: no permitir descargas vacías
//TODO: dar opcion de cancelar en las descargas. No necesaria
//notTODO: hacer que el icono de cambio de modo sea mas grande
//TODO: eliminar fotos intermedias que se generan en /pictures

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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.Display;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * horizontal full-screen
 */
public class GaleriaPrincipal extends Activity {

    private VideoAdapter videoAdapter;              //listview adapter.
    private AsyncVideoDownloader videoDownloader;   //asynchronous video downloader
//    private ArrayList<Video> lista;                 //list with the gallery's video
    private ListView lv;                            //listview with the gallery elements
    public boolean esTablet;                        //true if the device is a tablet (i.e: screen is bigger than 5')

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
        esTablet=auxiliar.isTablet(getApplicationContext());
        if(esTablet){                         //tablet layout
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
           ListView galeria = (ListView)findViewById(R.id.galeria);
            galeria.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
            galeria.setMinimumHeight(inferior.getHeight());
            //force text and image have the same height in xml
            TextView texto = (TextView)findViewById(R.id.editText);
            texto.setMaxHeight(outMetrics.heightPixels * 2 / 6); //textview max height
            //typeface serif (droid)
            texto.setTypeface(face);
            texto=(TextView)findViewById(R.id.textView);
            texto.setTypeface(face);

        }else{                                                                  //smartphone layout
            setContentView(R.layout.movil_galeria_principal);
            //typeface serif (droid)
            TextView texto = (TextView)findViewById(R.id.textView);
            texto.setTypeface(face);
            //make the scroll background crop instead of stretching in xml
            ListView galeria = (ListView)findViewById(R.id.galeria);
            galeria.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        }

        //fill the gallery with the available videos
        ArrayList<Video> lista = fillData(getApplicationContext());
        lv = (ListView)findViewById(R.id.galeria);
        lv.setPadding(0, 0, 0, 0);
        lv.setVerticalScrollBarEnabled(false);
        lv.setDivider(null);
        /*//set each item's click listener
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("MOTION", "child clicked");
                Video video = lista.get(position);
                if (!video.getImagen().equalsIgnoreCase("mas")) {
                    Intent intent = new Intent(GaleriaPrincipal.this, TouchActivity.class);
                    //send the URI
                    intent.putExtra("TITULO", video.getImagen());
                    intent.putExtra("PATH", video.getPath());
                    intent.putExtra("TIME", 0);
                    intent.putExtra("STATUS", true);
                    intent.putExtra("MODE", 0);
                    startActivity(intent);
                } else {
                    LayoutInflater layoutInflater = LayoutInflater.from(GaleriaPrincipal.this);
                    //inflate the dialog view
                    View promptView = layoutInflater.inflate(R.layout.popup, null);

                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GaleriaPrincipal.this);

                    // set prompts.xml to be the layout file of the alertdialog builder
                    alertDialogBuilder.setView(promptView);

                    final EditText enlace = (EditText) promptView.findViewById(R.id.enlaceDescarga);
//                  used:    http://stackoverflow.com/questions/5105354/how-to-show-soft-keyboard-when-edittext-is-focused
                    enlace.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (enlace.getText().toString().contains("insert video URL")) {
                                enlace.setText("");
                            }
                            Log.d("TEXTO", enlace.getText().toString());
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
                                    InputMethodManager imm = (InputMethodManager) GaleriaPrincipal.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.showSoftInput(enlace, InputMethodManager.SHOW_IMPLICIT);
                                    String path = enlace.getText().toString();
                                    try {
                                        //method for download found in http://www.insdout.com/
                                        // snippets/descargar-archivos-desde-una-url-en-nuestra-aplicacion-android.htm
                                        if (!path.startsWith("http://") && !path.startsWith("https://") && !path.startsWith("ftp://")) {
                                            path = "http://" + path;
                                        }
                                        URL url = new URL(path);
                                        videoDownloader = new AsyncVideoDownloader();
                                        videoDownloader.execute(url);
                                    } catch (MalformedURLException ex) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(GaleriaPrincipal.this);
                                        builder.setTitle("Error. Bad URL. Make sure you use a valid format (ie: \"http://url.com\", or \"url.com\")")
                                                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.cancel();
                                                        InputMethodManager imm = (InputMethodManager) GaleriaPrincipal.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                                                        if (imm.isActive()) {
                                                            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, InputMethodManager.HIDE_IMPLICIT_ONLY);
                                                        }
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
                }
            }
        });*/
        /*//set item's onLongClickListeners
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            Video video;

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int posicion = position;
                Log.d("MOTION", "child long clicked");
                video = lista.get(position);
                //item mas opens a dialog for downloads if long clicked
                if (video.getImagen().equalsIgnoreCase("mas")) {

                    return true;
                } else {  //other items open a dialog to be deleted when long clicked
                    //except for the two default videos
                    if (!video.getImagen().equalsIgnoreCase("predef1") && !video.getImagen().equalsIgnoreCase("predef2")) {
                        final File archivo = new File(auxiliar.directorio, video.getImagen());
                        if (archivo.exists()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(GaleriaPrincipal.this);
                            builder.setTitle("Are you sure you want to delete " + video.getImagen() + "?")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            lista.remove(posicion);
                                            archivo.delete();
                                            File imagen = auxiliar.obtenerArchivoImagen(video.getImagen());
                                            imagen.delete();
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
                            return true;
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(GaleriaPrincipal.this);
                            builder.setTitle("File " + video.getImagen() + " can't be deleted. Delete Manually.")
                                    .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog d = builder.create();
                            d.show();
                            Log.e("ON_LONG_CLICK", "video no encontrado");
                            return true;
                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(GaleriaPrincipal.this);
                        builder.setTitle("File " + video.getImagen() + " is a default sample video and can't be deleted")
                                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog d = builder.create();
                        d.show();
                        Log.e("ON_LONG_CLICK", "video predefinido no se puede borrar");
                        return true;
                    }
                }
            }
        });*/
        //set listview's adapter
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
        //add them to the list
        salida.add(aux);
        aux = new Video("predef2",context);
        salida.add(aux);
        aux = new Video("mas",context);
        salida.add(aux);
        return salida;
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
//        private ImageView item;

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
//            convertView=null;
            ViewHolder holder = null;
            //get the data item for this position
            final Video video = (Video)getItem(posicion);
            if(convertView==null){
                // always inflate the view so that old views do not appear twice
                convertView = LayoutInflater.from(contexto).inflate(R.layout.list_item, parent, false);
                // Lookup view for data population
                holder = new ViewHolder();
                holder.imageView = (ImageView) convertView.findViewById(R.id.miniatura);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder)convertView.getTag();
            }
            holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Log.d("MOTION", "child long clicked");
                    final Video video = list.get(posicion);
                    //item mas opens a dialog for downloads if long clicked
                    if (video.getImagen().equalsIgnoreCase("mas")) {
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
                                if (enlace.getText().toString().contains("insert video URL")) {
                                    enlace.setText("");
                                }
                                Log.e("TEXTO", enlace.getText().toString());
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
                                        InputMethodManager imm = (InputMethodManager) GaleriaPrincipal.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.showSoftInput(enlace, InputMethodManager.SHOW_IMPLICIT);
                                        String path = enlace.getText().toString();
                                        try {
                                            //method for download found in http://www.insdout.com/
                                            // snippets/descargar-archivos-desde-una-url-en-nuestra-aplicacion-android.htm
                                            if (!path.startsWith("http://") && !path.startsWith("https://") && !path.startsWith("ftp://")) {
                                                path = "http://" + path;
                                            }
                                            URL url = new URL(path);
                                            videoDownloader = new AsyncVideoDownloader();
                                            videoDownloader.execute(url);
                                        } catch (MalformedURLException ex) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(GaleriaPrincipal.this);
                                            builder.setTitle("Error. Bad URL. Make sure you use a valid format (ie: \"http://url.com\", or \"url.com\")")
                                                    .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.cancel();
                                                            InputMethodManager imm = (InputMethodManager) GaleriaPrincipal.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                                                            if (imm.isActive()) {
                                                                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, InputMethodManager.HIDE_IMPLICIT_ONLY);
                                                            }
                                                        }
                                                    });
                                            AlertDialog d = builder.create();
                                            d.show();
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
                    } else {  //other items open a dialog to be deleted when long clicked
                        //except for the two default videos
                        if (!video.getImagen().equalsIgnoreCase("predef1") && !video.getImagen().equalsIgnoreCase("predef2")) {
                            final File archivo = new File(auxiliar.directorio, video.getImagen());
                            if (archivo.exists()) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(GaleriaPrincipal.this);
                                builder.setTitle("Are you sure you want to delete " + video.getImagen() + "?")
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
//                                            lv.removeViewAt(posicion);
                                                list.remove(posicion);
                                                archivo.delete();
                                                File imagen = auxiliar.obtenerArchivoImagen(video.getImagen());
                                                imagen.delete();
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
                                return true;
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(GaleriaPrincipal.this);
                                builder.setTitle("File " + video.getImagen() + " can't be deleted. Delete Manually.")
                                        .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });
                                AlertDialog d = builder.create();
                                d.show();
                                Log.e("ON_LONG_CLICK", "video no encontrado");
                                return true;
                            }
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(GaleriaPrincipal.this);
                            builder.setTitle("File " + video.getImagen() + " is a default sample video and can't be deleted")
                                    .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog d = builder.create();
                            d.show();
                            Log.e("ON_LONG_CLICK", "video predefinido no puede borrarse desde app");
                            return true;
                        }
                    }
                }
            });
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e("MOTION", "child clicked");
                    Video video = list.get(posicion);
                    if (!video.getImagen().equalsIgnoreCase("mas")) {
//                    Intent intent = new Intent(GaleriaPrincipal.this, MainActivity.class);
                        Intent intent = new Intent(GaleriaPrincipal.this, TouchActivity.class);
                        //send the URI
                        intent.putExtra("TITULO", video.getImagen());
                        intent.putExtra("PATH", video.getPath());
                        intent.putExtra("TIME", 0);
                        intent.putExtra("STATUS", true);
                        intent.putExtra("MODE", 0);
//                        intent.putExtra("URI",video.getUri().toString());
                        Log.d("ON_CLICK", "open new activity");
                        startActivity(intent);
                    } else {
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
                                if (enlace.getText().toString().contains("insert video URL")) {
                                    enlace.setText("");
                                }
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
                                        InputMethodManager imm = (InputMethodManager) GaleriaPrincipal.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.showSoftInput(enlace, InputMethodManager.SHOW_IMPLICIT);
                                        String path = enlace.getText().toString();
                                        try {
                                            //method for download found in http://www.insdout.com/
                                            // snippets/descargar-archivos-desde-una-url-en-nuestra-aplicacion-android.htm
                                            if (!path.startsWith("http://") && !path.startsWith("https://") && !path.startsWith("ftp://")) {
                                                path = "http://" + path;
                                            }
                                            URL url = new URL(path);
                                            videoDownloader = new AsyncVideoDownloader();
                                            videoDownloader.execute(url);
                                            InputMethodManager i = (InputMethodManager) GaleriaPrincipal.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                                            if (i.isActive()) {
                                                i.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, InputMethodManager.HIDE_IMPLICIT_ONLY);
                                            }
                                        } catch (MalformedURLException ex) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(GaleriaPrincipal.this);
                                            builder.setTitle("Error. Bad URL. Make sure you use a valid format (ie: \"http://url.com\", or \"url.com\")")
                                                    .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.cancel();
                                                            InputMethodManager imm = (InputMethodManager) GaleriaPrincipal.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                                                            if (imm.isActive()) {
                                                                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, InputMethodManager.HIDE_IMPLICIT_ONLY);
                                                            }
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
                    }
                }
            });
            // Populate the data into the template view using the data object
            //parse the video name to get the image name
            int punto = video.getImagen().indexOf(".");
            String nombre=video.getImagen();
            if(punto != -1){
                nombre = video.getImagen().substring(0, punto);
            }
            //get the image id
            int id = contexto.getResources().getIdentifier(nombre, "drawable", contexto.getPackageName());
            if(id!=0){
                holder.imageView.setImageResource(id);
                holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                holder.imageView.setClickable(true);
                if(!video.getImagen().equalsIgnoreCase("mas")){
                    //take out background color
//                    holder.imageView.setBackgroundColor(Color.TRANSPARENT);
                    holder.imageView.setPadding(0, 0, 0, 0);
                }else{
                    holder.imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                    holder.imageView.setBackgroundColor();
                }
            }else{
                File archivoImagen = auxiliar.obtenerArchivoImagen(video.getImagen());
                if(archivoImagen.exists()){
                    holder.imageView.setImageURI(Uri.parse(archivoImagen.getAbsolutePath()));
                    holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                    holder.imageView.setBackgroundColor(Color.TRANSPARENT);
                    holder.imageView.setPadding(0, 0, 0, 0);
                }
            }
            //set scale type and return the view
            if(!video.getImagen().equalsIgnoreCase("mas")) holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            else holder.imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            return convertView;
        }
    }


    public static class ViewHolder{
        public ImageView imageView;
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
        private ProgressDialog progreso;
        private boolean error;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            error = false;
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
                    Log.d("BACK_DOWNLOAD", "conexion abierta");
                    //configuration
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoOutput(true);
                    urlConnection.connect();
                    Log.d("URL", "content type " + urlConnection.getContentType());
                    if(urlConnection.getContentType().contains("video/")){
                        //download and get the video
                        File f = auxiliar.directorio;
                        nombreVideo=auxiliar.getNombreVideo();

                        File file = new File(f,nombreVideo);
                        //stream to place the downloaded file
                        FileOutputStream fileOutput = new FileOutputStream(file);
                        //read data
                        InputStream inputStream = urlConnection.getInputStream();
                        //get file size
                        int totalSize = urlConnection.getContentLength();
                        int downloadedSize = 0;
                        //make buffer to store data
                        byte[] buffer = new byte[1024];
                        int bufferLength;
                        //write to file
                        while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                            fileOutput.write(buffer, 0, bufferLength);
                            downloadedSize += bufferLength;
                            int porcentaje = 100*downloadedSize/totalSize;
                            publishProgress(porcentaje);
                        }
                        Log.d("BACK_DOWNLOAD", "archivo volcado");
                        //close
                        fileOutput.close();
                        return "";
                    }else{
                        error=true;
                        progreso.cancel();
                    }

                }catch(IOException ex){
                    Log.e("ON_CLICK_ERROR",ex.getMessage());
                    error=true;
                    progreso.cancel();
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
            if(!error){
                Video video = new Video(result,GaleriaPrincipal.this);
                video.setPath(auxiliar.directorio.getPath() + "/" + result);
                video.setURL(result);
                video.crearFrameSample();
                videoAdapter.list=fillData(getApplicationContext());
                videoAdapter.list.add(0, video);
                lv.setSelection(0);
                videoAdapter.notifyDataSetChanged();
                progreso.cancel();
                AlertDialog.Builder builder = new AlertDialog.Builder(GaleriaPrincipal.this);
                builder.setTitle("Download successful. The frame sample will appear when gallery is refreshed.")
                        .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog d = builder.create();
                d.show();
                Log.d("BACK_DOWNLOAD", "downloaded video");
            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(GaleriaPrincipal.this);
                builder.setTitle("Error retrieving information. Please check the url or try again later.")
                        .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog d = builder.create();
                d.show();
                Log.e("BACK_DOWNLOAD", "error while downloading video");
                if(progreso.isShowing()){
                    progreso.cancel();
                }
            }

        }
    }
}
