package abaco_digital.freedom360;

/**
 * Created by Fitur on 17/09/2015.
 */
import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.StreamingTexture;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.RajawaliRenderer;

import java.io.File;
import java.io.IOException;

/**
 * Created by Fitur on 16/09/2015.
 */
public class TouchRenderer extends RajawaliRenderer{
    private Context context;        //renderer context
    private Sphere earthSphere;     //sphere where the video will be displayed
    private MediaPlayer mMediaPlayer;   //mediaPLayer that holds the video
    StreamingTexture video;         //video texture to project on the sphere
    public int pausedPosition;         //video length in ms
    private int timeSet;
    private String path;            //path del video
    private String titulo;          //titulo del video
    /*********************************************************************************************/
    private GestureDetector detector;
    private ScaleGestureDetector scaleDetector;
    private GestureListener gListener;
    private ScaleListener sListener;
    private View.OnTouchListener touchListener;
    private boolean isRotating;
    private boolean isScaling;
    private float xInicial,yInicial;
    private double yaw,pitch, yawAcumulado=0, pitchAcumulado=0, yawAcumuladoR=0, pitchAcumuladoR=0;
    private double reduccion;
    private final double gradosPorBarridoX=120, gradosPorBarridoY=90;
    private final double gradosPorPixelYaw, gradosPorPixelPitch;
    /*********************************************************************************************/

    /**Renderer constructor, initializes its main values*/
    public TouchRenderer(Context context, int time, String path, String titulo) {
        super(context);
        this.context = context;
        this.timeSet=time;
        setFrameRate(60);   //sets the renderer frame rate
        DisplayMetrics outMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        gradosPorPixelPitch = gradosPorBarridoY / outMetrics.heightPixels;
        gradosPorPixelYaw = gradosPorBarridoX / outMetrics.widthPixels;
        Log.e("NUEVO RENDERER","gradosyaw "+gradosPorPixelYaw+" gradospitch "+gradosPorPixelPitch);
        addListeners();
        /*Log.e("NUEVO RENDERER","vheight "+getViewportHeight()+" viewport width "+getViewportWidth());
        gradosPorPixelYaw = gradosPorBarridoX / getViewportWidth();
        gradosPorPixelPitch = gradosPorBarridoY / getViewportHeight();*/
        this.path=path;
        this.titulo=titulo;
    }

    //from Rajawali ArcballCamera class
    private void addListeners(){
        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gListener = new GestureListener();
                sListener = new ScaleListener();
                detector = new GestureDetector(context, gListener);
                scaleDetector = new ScaleGestureDetector(context, sListener);
                touchListener = new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        scaleDetector.onTouchEvent(event); //see if it is a scale event
                        //if not, check whether it is a scroll
                        if (!isScaling) {
                            detector.onTouchEvent(event);
                            //or an up motion
                            if (event.getAction() == MotionEvent.ACTION_UP) {
                                if (!isRotating) {
                                    //change video control view's visibility
                                    TouchActivity.timer.cancel();
                                    if (TouchActivity.control.getVisibility() == View.INVISIBLE) {
                                        TouchActivity.control.setVisibility(View.VISIBLE);
                                        TouchActivity.timer.start(); //timer is restarted
                                    } else {
                                        TouchActivity.control.setVisibility(View.INVISIBLE);
                                    }
                                } else {
                                    isRotating = false;   //cancel rotation
                                }
                            }
                        }
                        return true;
                    }
                };
                Log.e("NUEVO_RENDERER","principal "+(TouchActivity.principal!=null));
                TouchActivity.principal.setOnTouchListener(touchListener);
            }
        });
    }

    public void initScene(){
        //create a 100 segment sphere
        earthSphere = new Sphere(1, 100, 100);

        //try to set the mediaPLayer data source
        mMediaPlayer = new MediaPlayer();
        try{
            if(path==null){          //case: default app video
                int id;
                if(titulo.equalsIgnoreCase("predef1")){
                    id=context.getResources().getIdentifier("formigal","raw",context.getPackageName());
                }else{
                    id=context.getResources().getIdentifier("pyrex","raw",context.getPackageName());
                }
                mMediaPlayer.setDataSource(context, Uri.parse("android.resource://" + context.getPackageName() + "/" + id));
            }else{                  //case: downloaded video
                mMediaPlayer.setDataSource(Uri.fromFile(new File(path)).getPath());
            }
//            mMediaPlayer.setDataSource(context, Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.pyrex));
        }catch(IOException ex){
            Log.e("ERROR", "couldn attach data source to the media player");
        }
        mMediaPlayer.setLooping(true);  //enable video looping
        video = new StreamingTexture("pyrex",mMediaPlayer); //create video texture
        mMediaPlayer.prepareAsync();    //prepare the player (asynchronous)
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
//                mp.start(); //start the player only when it is prepared
                mp.seekTo(timeSet);
                mp.start();
            }
        });
        //add textture to a new material
        Material material = new Material ();
        material.setColorInfluence(0f);
        try{
            material.addTexture(video);
        }catch(ATexture.TextureException ex){
            Log.e("ERROR","texture error when adding video to material");
        }
        //set the material to the sphere
        earthSphere.setMaterial(material);
        earthSphere.setPosition(0, 0, 0);
        //add the sphere to the rendering scene
        getCurrentScene().addChild(earthSphere);
        Log.e("ESFERA", "posicion " + earthSphere.getPosition());
        Log.e("ESFERA", "camara " + getCurrentCamera().getPosition());
        //invert the sphere (to display the video on the inside of the sphere)
        earthSphere.setScaleX(1.15);
        earthSphere.setScaleY(1.15);
        earthSphere.setScaleZ(-1.15);

        Log.e("ESFERA", "camara mirando a " + getCurrentCamera().getLookAt());
        Log.e("ESFERA", "camara en cero? " + getCurrentCamera().getPosition());
        Log.e("ESFERA", "camara mirando a " + getCurrentCamera().getLookAt());

        //create the arcball camera and target the sphere
//        arcballCamera = new CamaraActualizada(context,TouchActivity.principal,earthSphere);
        Log.e("CAMARA", "camara creada");
        Log.e("CAMARA", "camara movida");
        //switch cameras
//        getCurrentScene().replaceAndSwitchCamera(getCurrentCamera(), arcballCamera);
//        arcballCamera.setPosition(0, 0, 0.5);
        getCurrentCamera().setPosition(0,0,0.5);
        Log.e("CAMARA", "switch camara");
    }


    /*update the video texture on rendering*/
    @Override
    public void onRender(final long elapsedTime, final double deltaTime) {
        super.onRender(elapsedTime, deltaTime);
        if (video != null) {
            video.update();
        }
        //if the screen is off, pause the mediaPlayer and store the current position for later restoring
        PowerManager mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (!mPowerManager.isScreenOn()) {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                pausedPosition = mMediaPlayer.getCurrentPosition();
            }
        }
//        if(isRotating) {
        yawAcumuladoR = (yawAcumulado) * 0.04;
        pitchAcumuladoR = (pitchAcumulado) * 0.04;
        Log.e("NUEVO_RENDERER", "yaw acumulado " + yaw + " pitch acumulado " + pitch);
        Quaternion q = new Quaternion();
        q.fromEuler(yawAcumuladoR, pitchAcumuladoR, 0);
        earthSphere.setOrientation(q);
//    }
        /*else{
            double yawaux = yawAcumulado * reduccion;
            double pitchaux = pitchAcumulado * reduccion;
            Quaternion q = new Quaternion();
            q.fromEuler(yawAcumulado * 0.05, pitchAcumulado * 0.05, 0);
            earthSphere.setOrientation(q);
            reduccion*=0.99;
            Log.e("NUEVO_RENDERER","reduccion "+reduccion);
        }*/
//        earthSphere.setRotY(pitchAcumulado);
//        earthSphere.setRotX(yawAcumulado);
//        getCurrentCamera().setCameraPitch(Math.toRadians(pitchAcumulado));
//        getCurrentCamera().setCameraYaw(Math.toRadians(yawAcumulado));
    }

    //returns this renderer media player
    public MediaPlayer getMediaPlayer(){
        return this.mMediaPlayer;
    }

    //called when the renderer is paused. it pauses the renderer and the mediaPlayer
    //storing its position
    @Override
    public void onPause(){
        super.onPause();
        if (mMediaPlayer!= null && mMediaPlayer.isPlaying())
        {    mMediaPlayer.pause();
            pausedPosition=mMediaPlayer.getCurrentPosition();
        }
    }

    //called when the renderer is resumed from a pause.
    //resumes mediaPlayer state
    @Override
    public void onResume(){
        super.onResume();
        if(mMediaPlayer!=null){
            mMediaPlayer.seekTo(pausedPosition);
            Log.e("INTENT INFO","on resume called "+pausedPosition);
        }
    }

    public void onTouchEvent(MotionEvent event){
    }

    public void onOffsetsChanged(float x, float y, float z, float w, int i, int j){
    }

    /*********************************************************************************************/
    private void startRotation(float x, float y){
        xInicial = x;
        yInicial = y;
    }

    private void updateRotation(float x, float y){
        float difX = xInicial - x;
        float difY = yInicial - y;
        yaw= difX * gradosPorPixelYaw;
        pitch = difY * gradosPorPixelPitch;
        yawAcumulado+=yaw;
        pitchAcumulado+=pitch;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
            //starts or updates the rotation with the upcoming event x and y screen values
            Log.e("NUEVO_RENDERER","onScroll reached");
            if(!isRotating) {
                startRotation(event2.getX(), event2.getY());
                isRotating=true;
                return false;
            }else{
                isRotating = true;
                updateRotation(event2.getX(), event2.getY());
                return false;
            }
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{
        //zooms in or out according to the scale detector value
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            double fov = Math.max(30.0D, Math.min(54.0D, 45.0D * (1.0D / (double)detector.getScaleFactor())));
            getCurrentCamera().setFieldOfView(fov);
            return true;
        }

        //the zoom begins
        @Override
        public boolean onScaleBegin (ScaleGestureDetector detector) {
            isScaling = true;
            isRotating = false;
            return super.onScaleBegin(detector);
        }

        //the zoom ends
        @Override
        public void onScaleEnd (ScaleGestureDetector detector) {
            isRotating = false;
            isScaling = false;
        }
    }
    /*********************************************************************************************/
}

