package abaco_digital.freedom360;
/**
 * Autor: Sandra Malpica Mallo
 *
 * Fecha: 11/09/2015
 *
 * Clase: CRenderer.java
 *
 * Comments: Cardboard renderer, used in gyroscope and cardboard mode. It sets the scene up.
 * Sensor events are managed internally with Google Cardboard sdk.
 */
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.PowerManager;
import android.util.Log;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.StreamingTexture;
import org.rajawali3d.primitives.Sphere;

import java.io.File;
import java.io.IOException;

public class CRenderer extends RajawaliVRRenderer{
    private int mode;                   //actual playback mode
    private int timeSet;                //time where the video has to start playing
    public int pausedPosition;          //video length in ms
    private final int cardboardMode=2;  //cardboard mode.
    private final int gyroMode=1;       //gyro mode. the sphere is rotated using the devices sensors
    private Context context;            //activity's context
    private MediaPlayer mMediaPlayer;   //mediaPLayer that holds the video
    private Sphere earthSphere;         //sphere where the video will be displayed
    StreamingTexture video;             //video texture to project on the sphere
    private String path;            //path del video
    private String titulo;          //titulo del video

    /**
     * Class constructor. Sets up internal parameters.
     * @param context
     * @param time
     * @param p
     * @param t
     */
    public CRenderer(Context context, int time, String p, String t){
        super(context);
        this.context=context;
        this.timeSet=time;
        mode=gyroMode;                  //initial mode: gyro mode
        path = p;
        titulo = t;
    }

    /**
     * Inits the scene. A sphere is placed on the world's origin. Inside of it, a video is
     * applied as a streaming texture.
     */
    @Override
    public void initScene(){
        //create a 100 segment sphere
        earthSphere = new Sphere(1, 100, 100);

        //try to set the mediaPLayer data source
        mMediaPlayer = new MediaPlayer();
        try{
            if(path==null){          //case: default app video
                int id;
                if(titulo.equalsIgnoreCase("predef1")){
                    id=context.getResources().getIdentifier("agua","raw",context.getPackageName());
                }else{
                    id=context.getResources().getIdentifier("helico","raw",context.getPackageName());
                }
                mMediaPlayer.setDataSource(context, Uri.parse("android.resource://" + context.getPackageName() + "/" + id));
            }else{                  //case: downloaded video
                mMediaPlayer.setDataSource(Uri.fromFile(new File(path)).getPath());
            }
        }catch(IOException ex){
            Log.e("ERROR", "couldn attach data source to the media player");
        }
        mMediaPlayer.setLooping(true);  //enable video looping
        video = new StreamingTexture("pyrex",mMediaPlayer); //create video texture
        mMediaPlayer.prepareAsync();    //prepare the player (asynchronous)
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //start the player only when it is prepared
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
        earthSphere.setScaleX(11.15);
        earthSphere.setScaleY(11.15);
        earthSphere.setScaleZ(-11.15);
        getCurrentCamera().setPosition(0,0,0.5);
    }

    /**
     * Renders the scene
     * @param elapsedTime
     * @param deltaTime
     */
    @Override
    public void onRender(final long elapsedTime, final double deltaTime){
        super.onRender(elapsedTime, deltaTime);
        if (video != null) {
            video.update();
        }
        //if the screen is off, pause the mediaPlayer and store the current position for later restoring
        PowerManager mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (!mPowerManager.isScreenOn()){
            if (mMediaPlayer!= null && mMediaPlayer.isPlaying())
            {    mMediaPlayer.pause();
                pausedPosition=mMediaPlayer.getCurrentPosition();
            }
        }
    }

    //returns this renderer media player
    public MediaPlayer getMediaPlayer(){
        return this.mMediaPlayer;
    }
}

