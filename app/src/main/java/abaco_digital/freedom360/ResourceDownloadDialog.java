package abaco_digital.freedom360;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Autor: Sandra Malpica Mallo
 *
 * Fecha: 29/06/2015
 *
 * Clase: ResourceDownloadDialog.java
 *
 * Comments: creates a fragment dialog that manages the video downloads of the user
 */
public class ResourceDownloadDialog extends DialogFragment{

    @Override
    public Dialog onCreateDialog(Bundle savedInstance){
        super.onCreateDialog(savedInstance);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        //inflate builder with custom view
        builder.setView(inflater.inflate(R.layout.popup,null))
                .setTitle(R.string.titulo_popup)    //set dialog title
                //set cancel button
                .setNegativeButton(R.string.atras, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                    //set download button and download the video
                }).setPositiveButton(R.string.confirmar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText enlace = (EditText) getActivity().findViewById(R.id.enlaceDescarga);
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(enlace, InputMethodManager.SHOW_IMPLICIT);
                        String path = enlace.getText().toString();
                        try{
                            //method for download found in http://www.insdout.com/snippets/descargar-archivos-desde-una-url-en-nuestra-aplicacion-android.htm
                            URL url = new URL(path);
                            //establish connection
                            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                            //configuration
                            urlConnection.setRequestMethod("GET");
                            urlConnection.setDoOutput(true);
                            urlConnection.connect();
                            //download and get the video
                            File f = auxiliar.directorio;
                            File file = new File(f,path);
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
                                builder.setMessage("descargando... "+porcentaje+"%");
                            }
                            //close
                            fileOutput.close();
                        }catch(MalformedURLException ex){
                            builder.setMessage("Video not found. Bad URL");
                        }catch(IOException ex){
                            builder.setMessage("Bad connection");
                        }
                    }
                });
        return builder.create();
    }
}
