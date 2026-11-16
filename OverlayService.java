package com.local.ofertas;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class OverlayService extends Service {
    private static final String CHANNEL="ofertas_activas";
    private static final int NOTIFICATION_ID=44;
    private WindowManager wm;
    private WindowManager.LayoutParams params;
    private View overlay;
    private EditText fare, pickup, trip, minutes;
    private TextView result;
    private TextView compactVerdict;
    private boolean minimized = false;
    private LinearLayout details;
    private ScrollView scroll;
    private Button collapse;
    private BroadcastReceiver receiver;
    private String lastOffer = "";

    @Override public void onCreate() {
        super.onCreate();
        NotificationManager nm=getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel(CHANNEL,"Analizador activo",NotificationManager.IMPORTANCE_LOW));
        Intent open=new Intent(this,MainActivity.class);
        PendingIntent pi=PendingIntent.getActivity(this,0,open,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        Notification n=new Notification.Builder(this,CHANNEL)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("Analizador de ofertas activo")
            .setContentText("Ventana flotante disponible. Toca para configurar.")
            .setContentIntent(pi).setOngoing(true).build();
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIFICATION_ID,n,android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIFICATION_ID,n);
        }
        if(!Settings.canDrawOverlays(this)) {stopSelf();return;}
        buildOverlay();
        receiver=new BroadcastReceiver() {
            @Override public void onReceive(Context c, Intent i) {
                if(CaptureService.ACTION_VISIBILITY.equals(i.getAction())) {
                    if(overlay!=null) overlay.setVisibility(i.getBooleanExtra(CaptureService.EXTRA_VISIBLE,true)?View.VISIBLE:View.INVISIBLE);
                    return;
                }
                if(CaptureService.ACTION_OCR_STARTED.equals(i.getAction())) {
                    setMinimized(true);
                    compactVerdict.setText("Buscando oferta visible...");
                    compactVerdict.setTextColor(Ui.NAVY);
                    return;
                }
                if(CaptureService.ACTION_RESULT.equals(i.getAction())) onRead(i);
            }
        };
        IntentFilter filter=new IntentFilter();
        filter.addAction(CaptureService.ACTION_RESULT);
        filter.addAction(CaptureService.ACTION_VISIBILITY);
        filter.addAction(CaptureService.ACTION_OCR_STARTED);
        if(Build.VERSION.SDK_INT>=33) registerReceiver(receiver,filter,Context.RECEIVER_NOT_EXPORTED);
        else registerReceiver(receiver,filter);
    }
    private EditText field(LinearLayout parent, String name, String value) {
        parent.addView(Ui.label(this,name,12,true));
        EditText e=Ui.decimal(this,value); e.setTextSize(14);
        parent.addView(e,new LinearLayout.LayoutParams(-1,Ui.dp(this,42)));
        return e;
    }
    private void buildOverlay() {
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        LinearLayout panel=Ui.column(this,12);
        panel.setBackground(Ui.background(Color.WHITE,14,this));
        panel.setElevation(Ui.dp(this,12));
        LinearLayout heading=new LinearLayout(this);heading.setOrientation(LinearLayout.HORIZONTAL);
        TextView title=Ui.label(this,"  Evaluar oferta  ≡",17,true);
        heading.addView(title,new LinearLayout.LayoutParams(0,Ui.dp(this,44),1));
        collapse=Ui.button(this,"−"); heading.addView(collapse,new LinearLayout.LayoutParams(Ui.dp(this,50),Ui.dp(this,44)));
        panel.addView(heading);
        compactVerdict=Ui.label(this,"Esperando oferta visible",15,true);
        compactVerdict.setPadding(Ui.dp(this,8),Ui.dp(this,4),Ui.dp(this,8),Ui.dp(this,9));
        panel.addView(compactVerdict);
        details=Ui.column(this,0);
        TextView explain=Ui.label(this,"Ingresa los datos visibles de la oferta",12,false);
        details.addView(explain);
        fare=field(details,"Tarifa ofrecida (COP)","15000");
        pickup=field(details,"Distancia a recogida (km)","2");
        trip=field(details,"Distancia del viaje (km)","6");
        minutes=field(details,"Duración total (min, incluida recogida)","25");
        Button calculate=Ui.button(this,"Analizar rentabilidad");
        details.addView(calculate);
        result=Ui.label(this,"Completa los datos y pulsa Analizar",14,true);
        result.setPadding(Ui.dp(this,9),Ui.dp(this,12),Ui.dp(this,9),Ui.dp(this,12));
        details.addView(result);
        Button close=Ui.button(this,"Cerrar ventana");details.addView(close);
        scroll=new ScrollView(this);scroll.setFillViewport(false);
        scroll.addView(details);
        int screenHeight=getResources().getDisplayMetrics().heightPixels;
        scroll.setLayoutParams(new LinearLayout.LayoutParams(-1,Math.min(Ui.dp(this,520),(int)(screenHeight*0.68f))));
        panel.addView(scroll);
        calculate.setOnClickListener(v -> analyze());
        close.setOnClickListener(v -> stopSelf());
        collapse.setOnClickListener(v -> setMinimized(!minimized));
        params=new WindowManager.LayoutParams(
            Ui.dp(this,310), WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT);
        params.gravity=Gravity.TOP|Gravity.START;
        params.x=Ui.dp(this,12);params.y=Ui.dp(this,90);
        overlay=panel;
        title.setOnTouchListener(new View.OnTouchListener() {
            int initX,initY;float startX,startY;
            @Override public boolean onTouch(View v,MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN) {
                    initX=params.x;initY=params.y;startX=event.getRawX();startY=event.getRawY();return true;
                }
                if(event.getAction()==MotionEvent.ACTION_MOVE) {
                    params.x=initX+(int)(event.getRawX()-startX);
                    params.y=initY+(int)(event.getRawY()-startY);
                    if(overlay!=null) wm.updateViewLayout(overlay,params);
                    return true;
                }
                return event.getAction()==MotionEvent.ACTION_UP;
            }
        });
        wm.addView(overlay,params);
    }
    /** El OCR muestra una tarjeta compacta que no roba el foco a inDrive. */
    private void setMinimized(boolean compact) {
        minimized = compact;
        if (scroll != null) scroll.setVisibility(compact ? View.GONE : View.VISIBLE);
        if (collapse != null) collapse.setText(compact ? "+" : "−");
        if (params != null && overlay != null && wm != null) {
            if (compact) params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            else params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            try { wm.updateViewLayout(overlay, params); } catch (Exception ignored) {}
        }
    }
    private void onRead(Intent i) {
        if(!i.getBooleanExtra("complete",false)) {
            result.setText("OCR: lectura incompleta. Comprueba o ingresa datos manualmente.");
            result.setTextColor(Ui.AMBER);
            compactVerdict.setText("Lectura parcial: comprobar");
            compactVerdict.setTextColor(Ui.AMBER);
            return;
        }
        double f=i.getDoubleExtra("fare",0), t=i.getDoubleExtra("trip",0), m=i.getDoubleExtra("minutes",0);
        boolean hasPickup=i.hasExtra("pickup");
        double k=i.getDoubleExtra("pickup",0);
        String fingerprint=f+"|"+t+"|"+m+"|"+k;
        if(fingerprint.equals(lastOffer)) return;
        lastOffer=fingerprint;
        fare.setText(Ui.one(f));trip.setText(Ui.one(t));minutes.setText(Ui.one(m));
        if(hasPickup) pickup.setText(Ui.one(k));
        else pickup.setText("");
        if(!hasPickup) {
            result.setText("OCR parcial: falta distancia a recogida. Introducela para evitar subestimar costos.");
            result.setTextColor(Ui.AMBER);
        } else analyze();
    }
    private void analyze() {
        double p=Ui.number(fare), k=Ui.number(pickup), t=Ui.number(trip), m=Ui.number(minutes);
        if(!Ui.valid(p)||!Ui.valid(k)||!Ui.valid(t)||!Ui.valid(m)|| p<=0 || t<=0 || m<=0) {
            Toast.makeText(this,"Ingresa tarifa, trayecto y duración válidos",Toast.LENGTH_LONG).show();return;
        }
        ProfitEngine.Offer o=new ProfitEngine.Offer();o.fare=p;o.pickupKm=k;o.tripKm=t;o.totalMinutes=m;
        ProfitEngine.Result r=ProfitEngine.calculate(Prefs.read(this),o);
        int color=r.verdict.equals("RENTABLE")?Ui.GREEN:r.verdict.equals("MARGINAL")?Ui.AMBER:Ui.RED;
        result.setText("● " + r.verdict + "\n" +
            "Utilidad estimada: " + Ui.money(r.profit) + "\n" +
            "Utilidad/km total: " + Ui.money(r.profitPerKm) + "\n" +
            "Utilidad/hora: " + Ui.money(r.profitPerHour) + "\n" +
            "Recorrido: " + Ui.one(r.totalKm) + " km\n" +
            "Combustible: " + Ui.money(r.fuelCost) + "\n" +
            "Mantenimiento: " + Ui.money(r.maintenanceCost) + "\n" +
            "SOAT, seguros, revisión y llantas: " + Ui.money(r.otherFixedCost) + "\n" +
            "Costo operativo por km: " + Ui.money(r.annualCostPerKm + Prefs.read(this).fuelCostPerKm()) + "\n" +
            "Tarifa mínima con metas: " + Ui.money(r.minimumFareForTargets) + "\n" +
            "Comisión: " + Ui.money(r.commission) + "\n" +
            "Costo de tiempo: " + Ui.money(r.timeCost));
        result.setTextColor(color);
        compactVerdict.setText("● "+r.verdict+" · "+Ui.money(r.profit)+" de utilidad estimada");
        compactVerdict.setTextColor(color);
        result.setBackground(Ui.background(Color.rgb(245,247,249),9,this));
    }
    @Override public int onStartCommand(Intent i,int flags,int startId) {return START_STICKY;}
    @Override public void onDestroy() {
        if(receiver!=null) {try {unregisterReceiver(receiver);} catch(Exception ignored) {}}
        if(wm!=null && overlay!=null) {try {wm.removeView(overlay);}catch(Exception ignored) {}}
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent i) {return null;}
}
