package abaco_digital.freedom360;

/**
 * Created by Fitur on 17/09/2015.
 */
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.concurrent.Semaphore;
//TODO: add scaleGesture Listener for zooming
//notTODO: pasar el nombre del video y el path(?)
//notTODO: cambiar el comportamiento del modo cardboard. Hacer que salte el diálogo la primera vez que se le da al play?
//TODO: en vez de cambio de modo secuencial, permitir elegir a qué modo se cambia al pulsar el botón(FloatingActionButton)
//TODO: asegurarse de que se puede bloquear y desbloquear la pantalla sin problemas durante la reproducción
//TODO: ojo, al cambiar a cardboard, si clickeas fuera durante la cuenta atras del dialogo se fastidia la operacion(comprobar)
/**
 * Created by Fitur on 11/09/2015.
 */
public class GyroActivity extends RajawaliVRActivity implements SeekBar.OnSeekBarChangeListener,View.OnClickListener{
    public static View principal;   //surface, for external access
    public static View control;     //view, control video view, for external access
    public LinearLayout view;       //view, control video view
    private RelativeLayout floatingView;
    private TextView tiempoActual;  //textview to show the actual reproduced video time
    private TextView tiempoTotal;   //textview to show the total video time
    private Thread controller;      //updates progress in seekbar and textviews
    public static CountDownTimer timer;//timer to make the control video view invisible when inactive
    public Semaphore lock;          //stops controller thread when app is paused
    private int mode;             //video playback mode: touch(0), gyro(1), cardboard(2)
    private int tTotal,tActual;     //current and total video time
    private int timeSent;
    private CRenderer mRenderer;
    private boolean status;
    private boolean muere = false;
    private AlertDialog dialog;
    private String titulo;
    private String path;
//    private FloatingActionButton f;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        lock = new Semaphore(1); //initialize the semaphore
        //full-screen
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //horizontal orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //get intent information
        Intent intent = getIntent();
        mode=intent.getIntExtra("MODE",1);
        timeSent=intent.getIntExtra("TIME",0);
        status=intent.getBooleanExtra("STATUS", true);
        titulo = intent.getStringExtra("TITULO");
        path = intent.getStringExtra("PATH");
        Log.e("INFO_CHANGE","tiempo recibido en GyroActivity "+timeSent);
        //set the renderer
        mRenderer = new CRenderer(this,timeSent,path,titulo);
        setRenderer(mRenderer);
        getSurfaceView().setKeepScreenOn(true);
        principal=getSurfaceView();
        if(mode==1) getSurfaceView().setVRModeEnabled(false);

        //inflates the video control view and adds it to current viewGroup
        LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
        ViewGroup viewGroup = (ViewGroup)getWindow().getDecorView().findViewById(android.R.id.content);
        view = (LinearLayout)layoutInflater.inflate(R.layout.player_control, null);
        floatingView = (RelativeLayout)layoutInflater.inflate(R.layout.floating_modes,null);
        view.setVerticalGravity(Gravity.BOTTOM);
        tiempoActual = (TextView)view.findViewById(R.id.tiempoTranscurrido);
        tiempoTotal = (TextView)view.findViewById(R.id.tiempoTotal);
        viewGroup.addView(view);
        //set the controls invisible when 3s pass and the user doesnt touch them.
        timer=new CountDownTimer(7000,7000){
            public void onFinish(){
                GyroActivity.this.view.setVisibility(View.INVISIBLE);
            }
            public void onTick(long l){}
        };
        timer.start();

        //set listeners to the buttons and seekbar progress control
        final ImageButton playButton = (ImageButton) view.findViewById(R.id.playbutton);
        ImageButton backButton = (ImageButton) view.findViewById(R.id.backbutton);
        final ImageButton modeButton = (ImageButton) view.findViewById(R.id.modebutton);
        final SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        modeButton.setImageLevel(1);
        //pause and play the video when playButton is pressed
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mRenderer.getMediaPlayer().isPlaying()){
                    mRenderer.getMediaPlayer().pause();
                    playButton.setImageLevel(1);//change button image
                }else{
                    mRenderer.getMediaPlayer().start();
                    playButton.setImageLevel(0);
                }
            }
        });

        //change the video playback mode
        modeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingView.setVisibility(View.VISIBLE);

                //the existence of the needed sensors has already been checked in TouchActivity
                GyroActivity.this.mode = (GyroActivity.this.mode + 1) % 3;
                switch (GyroActivity.this.mode) {
                    case 0:             //TOUCH MODE
                        getSurfaceView().setVRModeEnabled(false);
                        modeButton.setImageLevel(0);
                        Intent intent = new Intent(GyroActivity.this, TouchActivity.class);
                        intent.putExtra("TIME", mRenderer.getMediaPlayer().getCurrentPosition());
                        //startActivity(intent);
                        intent.putExtra("STATUS", mRenderer.getMediaPlayer().getCurrentPosition());
                        intent.putExtra("TITULO", titulo);
                        intent.putExtra("PATH", path);
                        mRenderer.getMediaPlayer().stop();
                        /*Intent intent = new Intent(GyroActivity.this, TouchActivity.class);
                        intent.putExtra("TIME", mRenderer.getMediaPlayer().getCurrentPosition());
                        //startActivity(intent);
                        intent.putExtra("STATUS", mRenderer.getMediaPlayer().getCurrentPosition());
                        setResult(19, intent);
                        mRenderer.stopRendering();
                        try{
                            lock.acquire();
//                            Log.e("THREAD SAFE", "lock acquired "+lock.availablePermits());
                        }catch(InterruptedException ex){}
                        muere=true;

                        mRenderer.getMediaPlayer().release();
                        lock.release();*/

//                        Log.e("THREAD SAFE", "lock released " + lock.availablePermits());
                        Log.e("INFO_CHANGE","tiempo enviado a touchActivity "+timeSent);
                        startActivity(intent);
                        finish();
                        break;
                    case 1:             //GYRO MODE
                        modeButton.setImageLevel(1);
                        getSurfaceView().setVRModeEnabled(false);
                        timer.cancel();
                        timer.start();
                        break;
                    case 2:             //CARDBOARD MODE
                        modeButton.setImageLevel(2);
                        getSurfaceView().setVRModeEnabled(true);
//                        lanzarGiro();
                        mRenderer.getMediaPlayer().pause();
                        timer.cancel();
                        playButton.setImageLevel(1);
                        playButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(GyroActivity.this);
                                builder.setTitle("Prepare Cardboard")
                                        .setMessage("Place your device inside the cardboard. ")
                                        .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog2, int which) {
                                                timer.start();
                                                AlertDialog.Builder b = new AlertDialog.Builder(GyroActivity.this);
                                                b.setMessage("The video will start in 10 seconds");
                                                dialog = b.create();
                                                dialog.show();
                                            }
                                        });
                                dialog = builder.create();
                                dialog.setCanceledOnTouchOutside(false);
                                timer=new CountDownTimer(10000,1000){
                                    public void onFinish(){
                                        mRenderer.getMediaPlayer().seekTo(0);
                                        mRenderer.getMediaPlayer().start();
                                        dialog.cancel();
                                        playButton.setImageLevel(0);}
                                    public void onTick(long l){dialog.setMessage("The video will start in "+(int)l/1000+" seconds");}
                                };
                                dialog.show();
                                GyroActivity.this.view.setVisibility(View.INVISIBLE);
                                playButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if(mRenderer.getMediaPlayer().isPlaying()){
                                            mRenderer.getMediaPlayer().pause();
                                            playButton.setImageLevel(1);//change button image
                                        }else{
                                            mRenderer.getMediaPlayer().start();
                                            playButton.setImageLevel(0);
                                        }
                                    }
                                });
                            }
                        });
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
//                while(primera || renderer.getMediaPlayer()!=null){
                while((primera || mRenderer.getMediaPlayer()!=null) && !muere){
                    try {
                        //acquire semaphore lock//used in onPause method
                        try {
                            lock.acquire();
//                            Log.e("THREAD SAFE", "lock acquired controller  "+lock.availablePermits());
                        } catch (InterruptedException ex) {
                        }
                        //wait until the mediaPlayer(prepared in the renderer) is not null
//                    while (renderer.getMediaPlayer()==null){}
                        while (!muere && mRenderer.getMediaPlayer() == null) {
                        }
                        //wait until the mediaPlayer is playing
//                    while (!renderer.getMediaPlayer().isPlaying()){}
                        while (!muere && !mRenderer.getMediaPlayer().isPlaying()) {
                        }
                        //set the total video time (only one executed once)
                        if (primera && !muere) {
//                        tTotal=renderer.getMediaPlayer().getDuration()/1000;
                            tTotal = mRenderer.getMediaPlayer().getDuration() / 1000;
                        }
                        //wait 1 second
                        try {
                            Thread.sleep(1000);
                            //get current mediaPlayer position
//                        posicion = renderer.getMediaPlayer().getCurrentPosition();
                            posicion = mRenderer.getMediaPlayer().getCurrentPosition();
                            tActual = posicion / 1000;
                            //sets the seekbar max to update progress with normal position
//                    seekBar.setMax(renderer.getMediaPlayer().getDuration());
                            seekBar.setMax(mRenderer.getMediaPlayer().getDuration());
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
                        } catch (InterruptedException ex) {
                            lock.release();
                        }
                    }catch(IllegalStateException ex2){ lock.release();}
                    lock.release(); //releases the semaphores lock
//                    Log.e("THREAD SAFE", "lock released controller "+lock.availablePermits());
                }

            }
        });
        controller.start(); //starts the controller thread

        getSurfaceView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.e("CONTROL VIEW", "surfaceview ontouch triggered");
                if(event.getAction()==MotionEvent.ACTION_UP){
                    timer.cancel();
                    if (GyroActivity.this.view.getVisibility() == View.INVISIBLE) {
                        GyroActivity.this.view.setVisibility(View.VISIBLE);
                        timer.start();
                    } else {
                        GyroActivity.this.view.setVisibility(View.INVISIBLE);
                    }
                }
                return false;
            }
        });
        control=view;
    }

    /******************************************************************************/
    /*                            Activity methods                                */
    /******************************************************************************/

    public void lanzarGiro(){
        mRenderer.getMediaPlayer().pause();
        //set listeners to the buttons and seekbar progress control
        final ImageButton playButton = (ImageButton) view.findViewById(R.id.playbutton);
        playButton.setImageLevel(1);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Prepare Cardboard")
                .setMessage("Place your device inside the cardboard. ")
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog2, int which) {
                        timer.start();
                        AlertDialog.Builder b = new AlertDialog.Builder(GyroActivity.this);
                        b.setMessage("The video will start in 10 seconds");
                        dialog = b.create();
                        dialog.show();
                    }
                });
        dialog = builder.create();
        timer=new CountDownTimer(10000,1000){
            public void onFinish(){
                mRenderer.getMediaPlayer().seekTo(0);
                mRenderer.getMediaPlayer().start();
                dialog.cancel();}
            public void onTick(long l){dialog.setMessage("The video will start in "+(int)l/1000+" seconds");}
        };
        dialog.show();
        GyroActivity.this.view.setVisibility(View.INVISIBLE);
//        timer.start();
    }

    //called when the user clicks on the screen
    @Override
    public void onClick(View v) {
        timer.cancel();
        if (GyroActivity.this.view.getVisibility() == View.INVISIBLE) {
            GyroActivity.this.view.setVisibility(View.VISIBLE);
            timer.start();
        } else {
            GyroActivity.this.view.setVisibility(View.INVISIBLE);
        }
    }

    //called when the activity is paused (ie screen blocked or home button pressed)
    //stops the activity, the mediaplayer and the renderer, as well as the auxiliary thread
    @Override
    public void onPause() {
        super.onPause();
        if(!muere){

            getSurfaceView().onPause();
            mRenderer.onPause();
        }
        /*try{
            lock.acquire();
        }catch(InterruptedException ex){}*/
        Log.e("SCREEN","onpause called");
    }

    //called when the activity is resumed
    //resumes the activity, the mediaplayer and the renderer, as well as the auxiliary thread
    //also brings back de video controller view
    @Override
    public void onResume() {
        super.onResume();
        if(!muere){

            getSurfaceView().onResume();
            mRenderer.onResume();
            if(view.getVisibility()!=View.VISIBLE){
                view.setVisibility(View.VISIBLE);
                timer.cancel(); //restarts the timer
                timer.start();
            }
        }
//        lock.release();
        Log.e("SCREEN", "onresume called");
    }
    /********************************************************************************/
    /*                             SeekBarListener methods                          */
    /********************************************************************************/
    //called each time the seekbar progress changes
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //if the user provoked the change
        if (fromUser && !muere) {
            //change mediaPLayer position
//            renderer.getMediaPlayer().seekTo(progress);
            mRenderer.getMediaPlayer().seekTo(progress);
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

