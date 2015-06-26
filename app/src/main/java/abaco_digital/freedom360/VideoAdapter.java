package abaco_digital.freedom360;
/**
 * Autor: Sandra Malpica Mallo
 *
 * Fecha: 23/06/2015
 *
 * Clase: MyAdapter.java
 *
 * Comments: manages the elements within the listview of the app's main
 * screen. Inflates the rowView and fills each element with data.
 */
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.List;


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
    public View getView(int posicion, View convertView, ViewGroup parent){
        //get the data item for this position
        Video video = (Video)getItem(posicion);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(contexto).inflate(R.layout.list_item, parent, false);
        }
        // Lookup view for data population
        ImageView item = (ImageView) convertView.findViewById(R.id.miniatura);
        // Populate the data into the template view using the data object
        Log.e("VIDEO_ADAPTER_GETVIEW", video.getImagen());
        int punto = video.getImagen().indexOf(".");
        if(punto != -1){
            video.setImagen(video.getImagen().substring(0, punto));
        }
        int id = contexto.getResources().getIdentifier(video.getImagen(), "drawable", contexto.getPackageName());
        if(id!=0){
            item.setImageResource(id);
        }
        item.setScaleType(ImageView.ScaleType.FIT_CENTER);

        /*WindowManager wm = (WindowManager)contexto.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        int altura = (outMetrics.heightPixels/2)-30;
        convertView.setMinimumHeight(altura);
        item.setMaxHeight(altura);
        item.setMinimumHeight(altura);*/
        //item.setScaleType(ImageView.ScaleType.FIT_CENTER);
        // Return the completed view to render on screen
        return convertView;
    }
}
