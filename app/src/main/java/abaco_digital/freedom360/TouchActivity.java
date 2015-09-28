package abaco_digital.freedom360;
/**
 * Autor: Sandra Malpica Mallo
 *
 * Fecha: 11/09/2015
 *
 * Clase: TouchActivity.java
 *
 * Comments: one of the app's player activities. It manages touch mode. It also sets
 * up the video control view.
 */
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.method.Touch;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import org.rajawali3d.surface.RajawaliSurfaceView;
import java.util.concurrent.Semaphore;

//notTODO: enviar la informacion necesaria del video (path y titulo)
public class TouchActivity extends Activity implements SeekBar.OnSeekBarChangeListener,View.OnClickListener{
    TouchRenderer trenderer;        //openGL renderer
    RajawaliSurfaceView surface;    //openGL surface
    public static View principal;   //surface, for external access
    public static View control;     //view, control video view, for external access
    public LinearLayout view;       //view, control video view
    private TextView tiempoActual;  //textview to show the actual reproduced video time
    private TextView tiempoTotal;   //textview to show the total video time
    private Thread controller;      //updates progress in seekbar and textviews
    public static CountDownTimer timer;//timer to make the control video view invisible when inactive
    public Semaphore lock;          //stops controller thread when app is paused
    private int modo=0;             //video playback mode: touch(0), gyro(1), cardboard(2)
    private int tTotal,tActual;     //current and total video time
    private int timeSent;           //the time where the video has to start (sent from other player activity)
    private boolean tieneGiro = false;//shows if the device has a gyroscope sensor
    private boolean isPlaying;      //true if the video is not paused
    private String titulo;          //video's title
    private String path;            //video's path

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        lock=new Semaphore(1);  //initializes the semaphore
        //full-screen
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //horizontal orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //get intent information
        Intent intent = getIntent();
        modo=intent.getIntExtra("MODE",1);
        timeSent=intent.getIntExtra("TIME",0);
        isPlaying=intent.getBooleanExtra("STATUS", true);
        titulo = intent.getStringExtra("TITULO");
        path = intent.getStringExtra("PATH");
        Log.d("INFO_CHANGE","tiempo recibido en touchActivity "+timeSent);
        surface = new RajawaliSurfaceView(this);
        principal = surface;
        //set the renderer
        trenderer = new TouchRenderer(this,timeSent,path,titulo);

        // Add mSurface to your root view
        addContentView(surface, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT));
        surface.setSurfaceRenderer(trenderer);
        surface.setFrameRate(60);
        //keep the screen on
        surface.setKeepScreenOn(true);



        //inflates the video control view and adds it to current viewGroup
        LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
        ViewGroup viewGroup = (ViewGroup)getWindow().getDecorView().findViewById(android.R.id.content);
        view = (LinearLayout)layoutInflater.inflate(R.layout.player_control, null);
        view.setVerticalGravity(Gravity.BOTTOM);
        tiempoActual = (TextView)view.findViewById(R.id.tiempoTranscurrido);
        tiempoTotal = (TextView)view.findViewById(R.id.tiempoTotal);
        viewGroup.addView(view);
        //set the controls invisible when 3s pass and the user doesnt touch them.
        timer=new CountDownTimer(7000,7000){
            public void onFinish(){
                TouchActivity.this.view.setVisibility(View.INVISIBLE);
            }
            public void onTick(long l){}
        };
        timer.start();

        //set listeners to the buttons and seekbar progress control
        final ImageButton playButton = (ImageButton) view.findViewById(R.id.playbutton);
        ImageButton backButton = (ImageButton) view.findViewById(R.id.backbutton);
        final ImageButton modeButton = (ImageButton) view.findViewById(R.id.modebutton);
        final SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBar);

        //pause and play the video when playButton is pressed
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(trenderer.getMediaPlayer().isPlaying()){
                    trenderer.getMediaPlayer().pause();
                    playButton.setImageLevel(1);//change button image
                }else{
                    trenderer.getMediaPlayer().start();
                    playButton.setImageLevel(0);
                }
            }
        });

        //change video playback mode
        modeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if the device has accelerometer and gyroscope
                //if it doesnt have the needed sensors, only touch mode will be allowed
                if(tieneGiro){
                    TouchActivity.this.modo = (TouchActivity.this.modo+1)%3;
                }
                switch (TouchActivity.this.modo){
                    case 0:         //TOUCH MODE
                        modeButton.setImageLevel(0);    //change button image
                        break;
                    case 1:         //GYROSCOPE MODE
                        modeButton.setImageLevel(1);
                        Intent intent = new Intent(TouchActivity.this,GyroActivity.class);
                        intent.putExtra("TIME", trenderer.getMediaPlayer().getCurrentPosition());
                        intent.putExtra("STATUS", trenderer.getMediaPlayer().isPlaying());
                        intent.putExtra("TITULO", titulo);
                        intent.putExtra("PATH", path);
                        Log.d("INFO_CHANGE","tiempo enviado a GyroActivity "+timeSent);
                        startActivity(intent);
                        finish();
                        break;
                }
            }
        });

        //exit the video player when backButton is pressed
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.runFinalization();
                System.exit(0); //cerrar el sistema.
            }
        });

        seekBar.setOnSeekBarChangeListener(this);
        //launch a thread to update seekBar progress (---each second----)
        controller= new Thread(new Runnable() {
            private int posicion;
            boolean primera = true;
            @Override
            public void run() {
                //run while the mediaPlayer exists
                while(primera || trenderer.getMediaPlayer()!=null){
                    //acquire semaphore lock//used in onPause method
                    try{
                        lock.acquire();
                    }catch(InterruptedException ex){}
                    //wait until the mediaPlayer(prepared in the renderer) is not null
                    while (trenderer.getMediaPlayer()==null){}
                    //wait until the mediaPlayer is playing
                    while (!trenderer.getMediaPlayer().isPlaying()){}
                    //set the total video time (only one executed once)
                    if(primera){
                        tTotal=trenderer.getMediaPlayer().getDuration()/1000;
                        primera = false;
                    }
                    //wait 1 second
                    try{
                        Thread.sleep(1000);
                        //get current mediaPlayer position
                        posicion = trenderer.getMediaPlayer().getCurrentPosition();
                        tActual=posicion/1000;
                    }catch(InterruptedException ex){}
                    //sets the seekbar max to update progress with normal position
                    seekBar.setMax(trenderer.getMediaPlayer().getDuration());
                    //sends information to the UI thread
                    //UI elements can only be modified there
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            seekBar.setProgress(posicion);  //sets seekbar progress
                            //sets textviews values
                            String am = String.format("%02d", tActual / 60);
                            String as = String.format("%02d", tActual % 60);
                            tiempoActual.setText(am + ":" + as);
                            if (primera) {
                                am = String.format("%02d", tTotal / 60);
                                as = String.format("%02d", tTotal % 60);
                                tiempoTotal.setText(am + ":" + as);
                                primera = false;
                            }
                        }
                    });
                    lock.release(); //releases the semaphores lock
                }

            }
        });
        controller.start(); //starts the controller thread

        control=view;
        //checks whether the device has the needed sensors
        PackageManager pm = getPackageManager();
        tieneGiro = pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);
        tieneGiro = tieneGiro & pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER) & pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS);
        Log.d("HAS_SENSOR", "tiene acelerometro "+pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER));
        Log.d("HAS_SENSOR","tiene sensor rotacion "+pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS) );
        Log.d("HAS_SENSOR", "tiene giroscopo "+tieneGiro);
    }

    /******************************************************************************/
    /*                            Activity methods                                */
    /******************************************************************************/

    @Override
    public void onActivityResult(int requestCode,int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==19 && resultCode == 19){
            boolean isPlaying =  data.getBooleanExtra("STATUS",true);
            timeSent = data.getIntExtra("TIME",0);
            trenderer.getMediaPlayer().seekTo(timeSent);
            if(isPlaying) trenderer.getMediaPlayer().start();
        }
    }

    //called when the user clicks on the screen
    @Override
    public void onClick(View v) {
        timer.cancel();
        if (TouchActivity.this.view.getVisibility() == View.INVISIBLE) {
            TouchActivity.this.view.setVisibility(View.VISIBLE);
            timer.start();
        } else {
            TouchActivity.this.view.setVisibility(View.INVISIBLE);
        }
    }

    //called when the activity is paused (ie screen blocked or home button pressed)
    //stops the activity, the mediaplayer and the renderer, as well as the auxiliary thread
    @Override
    public void onPause() {
        super.onPause();
        surface.onPause();
        trenderer.onPause();
    }

    //called when the activity is resumed
    //resumes the activity, the mediaplayer and the renderer, as well as the auxiliary thread
    //also brings back de video controller view
    @Override
    public void onResume() {
        super.onResume();
        surface.onResume();
        if(principal!=null) trenderer.onResume();
        if(view.getVisibility()!=View.VISIBLE){
            view.setVisibility(View.VISIBLE);
            timer.cancel(); //restarts the timer
            timer.start();
        }
    }

    /********************************************************************************/
    /*                             SeekBarListener methods                          */
    /********************************************************************************/
    //called each time the seekbar progress changes
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //if the user provoked the change
        if (fromUser) {
            //change mediaPLayer position
            trenderer.getMediaPlayer().seekTo(progress);
            seekBar.setProgress(progress);  //change the seekbar progress
            progress=progress/1000;
            //change the textview accordingly to the movement
            String am = String.format("%02d", progress / 60);
            String as = String.format("%02d", progress % 60);
            tiempoActual.setText(am + ":" + as);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}
}

