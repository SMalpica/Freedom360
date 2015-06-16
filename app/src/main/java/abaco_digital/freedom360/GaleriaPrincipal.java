package abaco_digital.freedom360;

import abaco_digital.freedom360.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.Display;
import android.widget.TextView;
import android.graphics.Point;
import android.graphics.Bitmap;


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

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = false;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_FULLSCREEN;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_galeria_principal);
            getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        /*super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_galeria_principal);*/

        //final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(android.R.id.content);


        contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE);
        // This example uses decor view, but you can use any visible view.

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        //mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        //mSystemUiHider.setup();
        /*mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                      /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible) {
                            // Schedule a hide().
                            delayedHide(1);
                            //delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });*/

        // Set up the user interaction to manually show or hide the system UI.
//        contentView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (TOGGLE_ON_CLICK) {
//                    mSystemUiHider.hide();
//                } else {
//                    //mSystemUiHider.show();
//                    mSystemUiHider.hide();
//                }
//            }
//        });

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
        TextView texto = (TextView)findViewById(R.id.editText);
        texto.setMaxHeight(outMetrics.heightPixels * 2 / 6);
        Drawable editTextDrawable = ((TextView) findViewById(R.id.editText)).getCompoundDrawables()[0];

        //editTextDrawable.setBounds(0, 0, editTextDrawable.getIntrinsicWidth(), editTextDrawable.getIntrinsicHeight());
        //ImageView camaras = (ImageView)findViewById(R.id.imageView);
        //BitmapDrawable imagen = (BitmapDrawable)camaras.getDrawable();
        //int altura = imagen.getBitmap().getHeight();
        //texto.getLayoutParams().height=altura;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

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
