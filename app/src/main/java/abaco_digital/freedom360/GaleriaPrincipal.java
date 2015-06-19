package abaco_digital.freedom360;
//TODO: tamanyo de la letra dependiente del de la pantalla
//TODO: probar con distintas resoluciones para tablet y movil
import abaco_digital.freedom360.util.SystemUiHider;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.view.Display;
import android.widget.TextView;
import android.graphics.Point;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class GaleriaPrincipal extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;


    @Override
    @TargetApi(21)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        final View contentView = findViewById(android.R.id.content);
        contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE);
        Typeface face= Typeface.createFromAsset(getAssets(), "fonts/DroidSerif-Regular.ttf");
        //use different layouts depending on the screen size
        if(auxiliar.isTablet(getApplicationContext())){
            setContentView(R.layout.activity_galeria_principal);
            //MEDIMOS LA PANTALLA
            Display display = getWindowManager().getDefaultDisplay();
            DisplayMetrics outMetrics = new DisplayMetrics();
            //display.getMetrics(outMetrics);
            display.getMetrics(outMetrics);
            Point out = new Point();
            display.getSize(out);
            //ASIGNAMOS MEDIANTE ID EL LAYOUT
            //LinearLayout layoutcentral =(LinearLayout)findViewById(R.id.layoutCentral);
            LinearLayout layoutsuperior =(LinearLayout)findViewById(R.id.layoutSuperior);
            LinearLayout inferior =(LinearLayout)findViewById(R.id.horizontalScrollView);
            //2 quintos para el layout central
            //otros dos quintos para el inferior
            //layoutcentral.getLayoutParams().height=outMetrics.heightPixels*2/6;
            layoutsuperior.getLayoutParams().height=outMetrics.heightPixels/6;
            inferior.getLayoutParams().height=outMetrics.heightPixels*3/6-10;
            //forzar que la imagen y el texto del layout central sean igual de altos
            //en xml
            //tipografia tipo serif
            TextView texto = (TextView)findViewById(R.id.editText);
            texto.setMaxHeight(outMetrics.heightPixels * 2 / 6);
            texto.setTypeface(face);
            texto=(TextView)findViewById(R.id.textView);
            texto.setTypeface(face);
        }else{
            setContentView(R.layout.movil_galeria_principal);
            TextView texto = (TextView)findViewById(R.id.textView);
            texto.setTypeface(face);
            //hacer que el scroll sea del tamaño del fondo para que no se deforme
            //hecho en xml con frameLayout
            //usar distintos metodos dependiendo de la api
//            HorizontalScrollView horizontal = (HorizontalScrollView)findViewById(R.id.horizontalScrollView);
//            int sdk = android.os.Build.VERSION.SDK_INT;
//            if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
//                horizontal.setBackgroundDrawable( getResources().getDrawable(R.drawable.espacio) );
//            } else if (sdk < 22){
//                horizontal.setBackground( getResources().getDrawable(R.drawable.espacio));
//            } else{
//                Drawable d = getResources().getDrawable(R.drawable.espacio,null);
//                horizontal.setBackground(d);
//            }

        }



        /*super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_galeria_principal);*/

        //final View controlsView = findViewById(R.id.fullscreen_content_controls);




    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }



    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            //mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
