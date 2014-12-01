package com.izv.webimage;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

public class Principal extends Activity {

    private EditText et_url, et_gc;
    private Button guardar;
    private ImageView imagen;
    private RadioButton rb_priv, rb_pub;
    private String extension;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        et_url = (EditText)findViewById(R.id.et_u);
        et_gc = (EditText)findViewById(R.id.et_g);
        guardar = (Button)findViewById(R.id.bt_guardar);
        imagen = (ImageView)findViewById(R.id.iv_imagen);
        rb_priv = (RadioButton)findViewById(R.id.rb_priv);
        rb_pub = (RadioButton)findViewById(R.id.rb_pub);
    }

    public void getBitmapFromURL(final String imageUrl) {
        Thread hilo = new Thread(){
            @Override
            public void run(){
            URL url = null;
                try {
                    url = new URL(imageUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    if(extensionImagen(imageUrl)){
                        Bitmap myBitmap = BitmapFactory.decodeStream(input);
                        guardarImagen(myBitmap);
                    }else{
                        Principal.this.runOnUiThread(new Runnable() {
                            public void run() {
                                tostada(getString(R.string.error_extension));
                            }
                        });
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    Principal.this.runOnUiThread(new Runnable() {
                        public void run() {
                            tostada(getString(R.string.error_url));
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    Principal.this.runOnUiThread(new Runnable() {
                        public void run() {
                            tostada(getString(R.string.error_io));
                        }
                    });
                }
            }
        };
        hilo.start();
    }

    public boolean extensionImagen(String uri){
        extension = uri.substring(uri.lastIndexOf("."));
        System.out.println(extension);
        if(extension.equals(".png") || extension.equals(".jpg") || extension.equals(".gif")) {
            return true;
        }else{
            return false;
        }
    }

    public void descargar(View view){
        imagen.setImageBitmap(null);
        getBitmapFromURL(et_url.getText().toString());
    }

    public void verImagen(View view){
        cargarImagen(et_gc.getText().toString());
    }

    private void cargarImagen(String nombre){
        String path = null;
        if (rb_priv.isChecked()) {
            path = getExternalFilesDir(null) + File.separator + nombre + extension;
        } else if (rb_pub.isChecked()) {
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + nombre + extension;
        }
        Bitmap cImagen = BitmapFactory.decodeFile(path);
        imagen.setImageBitmap(cImagen);
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private void guardarImagen (Bitmap imagen) throws IOException {
        if(isExternalStorageWritable()) {
            if (imagen != null) {
                File f = null;
                String archivo;
                archivo = et_gc.getText().toString() + extension;
                System.out.println(archivo);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                imagen.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                if (rb_priv.isChecked()) {
                    f = new File(getExternalFilesDir(null), archivo);
                } else if (rb_pub.isChecked()) {
                    f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), archivo);
                }
                f.createNewFile();
                FileOutputStream fo = new FileOutputStream(f);
                fo.write(bytes.toByteArray());
                fo.close();
            } else {
                Principal.this.runOnUiThread(new Runnable() {
                    public void run() {
                        tostada(getString(R.string.error_imagen));
                    }
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void tostada(String s){
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
