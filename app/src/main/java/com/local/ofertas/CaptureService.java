package com.local.ofertas;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.google.mlkit.vision.text.TextRecognizer;
import java.nio.ByteBuffer;

/** Captura explicita de pantalla + OCR local. Nunca guarda imagenes ni envia datos. */
public class CaptureService extends Service {
    public static final String ACTION_RESULT="com.local.ofertas.OCR_RESULT";
    public static final String ACTION_OCR_STARTED="com.local.ofertas.OCR_STARTED";
    public static final String ACTION_VISIBILITY="com.local.ofertas.OVERLAY_VISIBILITY";
    public static final String EXTRA_VISIBLE="visible";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private MediaProjection projection;
    private VirtualDisplay display;
    private ImageReader reader;
    private TextRecognizer recognizer;
    private boolean busy=false, running=false;
    private final Runnable loop=new Runnable() {
        @Override public void run() {
            if (!running) return;
            // Quitar brevemente el overlay para evitar OCR de sus propios valores.
            Intent hide=new Intent(ACTION_VISIBILITY).setPackage(getPackageName());
            hide.putExtra(EXTRA_VISIBLE,false);sendBroadcast(hide);
            handler.postDelayed(() -> { captureFrame(); if(running) handler.postDelayed(this,2600); },350);
        }
    };
    @Override public void onCreate() {
        super.onCreate();
        NotificationManager nm=getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel("captura_ofertas","Lectura de pantalla",NotificationManager.IMPORTANCE_LOW));
        recognizer=TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    }
    @Override public int onStartCommand(Intent intent,int flags,int startId) {
        if(intent==null || !intent.hasExtra("resultCode") || !intent.hasExtra("resultData")) {stopSelf();return START_NOT_STICKY;}
        Notification n=new Notification.Builder(this,"captura_ofertas")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentTitle("Lectura de ofertas activa")
            .setContentText("Se analiza la pantalla localmente. Puedes detener desde la app.")
            .setOngoing(true).build();
        if(Build.VERSION.SDK_INT>=29) startForeground(45,n,android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
        else startForeground(45,n);
        if(projection!=null) return START_NOT_STICKY;
        MediaProjectionManager m=(MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
        projection=m.getMediaProjection(intent.getIntExtra("resultCode",Activity.RESULT_CANCELED),
            (Intent)intent.getParcelableExtra("resultData"));
        if(projection==null) {stopSelf();return START_NOT_STICKY;}
        projection.registerCallback(new MediaProjection.Callback() {
            @Override public void onStop(){stopSelf();}
        },handler);
        WindowManager wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        DisplayMetrics metrics=new DisplayMetrics();wm.getDefaultDisplay().getRealMetrics(metrics);
        int width=metrics.widthPixels,height=metrics.heightPixels;
        reader=ImageReader.newInstance(width,height,PixelFormat.RGBA_8888,2);
        display=projection.createVirtualDisplay("analisis_local",width,height,metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,reader.getSurface(),null,handler);
        running=true;
        sendBroadcast(new Intent(ACTION_OCR_STARTED).setPackage(getPackageName()));
        handler.post(loop);
        return START_NOT_STICKY;
    }
    private void showOverlay() {
        Intent show=new Intent(ACTION_VISIBILITY).setPackage(getPackageName());
        show.putExtra(EXTRA_VISIBLE,true);sendBroadcast(show);
    }
    private void captureFrame() {
        if(reader==null || busy) {showOverlay();return;}
        Image image=null;
        try {
            image=reader.acquireLatestImage();
            if(image==null) {showOverlay();return;}
            Image.Plane plane=image.getPlanes()[0];
            int pixelStride=plane.getPixelStride(),rowStride=plane.getRowStride();
            ByteBuffer buffer=plane.getBuffer();
            Bitmap padded=Bitmap.createBitmap(image.getWidth()+(rowStride-pixelStride*image.getWidth())/pixelStride,image.getHeight(),Bitmap.Config.ARGB_8888);
            padded.copyPixelsFromBuffer(buffer);
            Bitmap bitmap=Bitmap.createBitmap(padded,0,0,image.getWidth(),image.getHeight());
            padded.recycle();
            image.close();image=null;
            showOverlay();
            busy=true;
            recognizer.process(InputImage.fromBitmap(bitmap,0)).addOnSuccessListener(text -> {
                OfferParser.Parsed parsed=OfferParser.parse(text.getText());
                if(parsed.fare != null || parsed.tripKm != null) {
                    Intent result=new Intent(ACTION_RESULT).setPackage(getPackageName());
                    if(parsed.fare!=null) result.putExtra("fare",parsed.fare);
                    if(parsed.pickupKm!=null) result.putExtra("pickup",parsed.pickupKm);
                    if(parsed.tripKm!=null) result.putExtra("trip",parsed.tripKm);
                    if(parsed.minutes!=null) result.putExtra("minutes",parsed.minutes);
                    result.putExtra("complete",parsed.complete());
                    result.putExtra("warning",parsed.warning);
                    sendBroadcast(result);
                }
            }).addOnCompleteListener(task -> {bitmap.recycle();busy=false;});
        } catch(Exception ignored) {showOverlay();}
        finally {if(image!=null)image.close();}
    }
    @Override public void onDestroy() {
        running=false;handler.removeCallbacksAndMessages(null);
        showOverlay();
        if(display!=null)display.release();
        if(reader!=null)reader.close();
        if(projection!=null)projection.stop();
        if(recognizer!=null)recognizer.close();
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent i){return null;}
}
