package com.local.ofertas;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ScrollView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private TextView costSummary;
    private static final int CAPTURE_CODE=200;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = Ui.column(this, 22);
        root.addView(Ui.label(this,"Analizador de viajes",26,true));
        root.addView(Ui.label(this,"Evaluacion local de ofertas de viaje con semaforo flotante, captura de pantalla autorizada y reconocimiento de texto (OCR).",16,false));
        costSummary=Ui.label(this,"",15,true);
        root.addView(costSummary);
        root.addView(Ui.label(this,"1. Ajusta tus costos y metas.\n2. Activa la ventana flotante.\n3. Inicia la lectura y autoriza Android a compartir la pantalla completa.\n4. Abre inDrive y comprueba los datos antes de decidir.",15,false));
        android.widget.Button config = Ui.button(this,"Editar parametros");
        android.widget.Button start = Ui.button(this,"Activar ventana flotante");
        android.widget.Button capture = Ui.button(this,"Iniciar lectura automatica (OCR)");
        android.widget.Button stop = Ui.button(this,"Detener lectura y ventana flotante");
        root.addView(config);root.addView(start);root.addView(capture);root.addView(stop);
        root.addView(Ui.label(this,"Privacidad: la captura de pantalla solo se procesa en el telefono. La app no se conecta a servidores ni acepta viajes. El OCR puede confundir datos; confirma tarifa, distancias y minutos. Para evitar errores, se exige una lectura completa antes de calificar automaticamente. No manipules el telefono mientras conduces.",13,false));
        config.setOnClickListener(v -> startActivity(new Intent(this,SettingsActivity.class)));
        start.setOnClickListener(v -> startOverlay());
        capture.setOnClickListener(v -> {
            if(!startOverlay()) return;
            MediaProjectionManager mgr=(MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
            startActivityForResult(mgr.createScreenCaptureIntent(),CAPTURE_CODE);
        });
        stop.setOnClickListener(v -> {
            stopService(new Intent(this,CaptureService.class));
            stopService(new Intent(this,OverlayService.class));
            Toast.makeText(this,"Analisis desactivado",Toast.LENGTH_SHORT).show();
        });
        scroll.addView(root);setContentView(scroll);
    }
    @Override protected void onResume() {
        super.onResume();
        if(costSummary!=null) {
            ProfitEngine.Config c=Prefs.read(this);
            String insuranceInfo=Prefs.insurancePeriodConfirmed(this)?"":"\nSeguro adicional: revisar periodicidad.";
            costSummary.setText("Recorrido anual: "+Ui.money(c.annualKilometers).replace("$ ","")+" km"+
                "\nGasolina/km: "+Ui.money(c.fuelCostPerKm())+
                " · Gastos anuales/km: "+Ui.money(c.annualCostPerKm())+
                "\nCosto total/km: "+Ui.money(c.fuelCostPerKm()+c.annualCostPerKm())+
                " · Comisión: "+Ui.one(c.commissionPercent)+" %"+insuranceInfo);
        }
    }
    private boolean startOverlay() {
        if(!Settings.canDrawOverlays(this)) {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));
            Toast.makeText(this,"Autoriza mostrar encima de otras aplicaciones",Toast.LENGTH_LONG).show();
            return false;
        }
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},101);
        }
        startForegroundService(new Intent(this,OverlayService.class));
        return true;
    }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==CAPTURE_CODE && resultCode==RESULT_OK && data!=null) {
            Intent service=new Intent(this,CaptureService.class);
            service.putExtra("resultCode",resultCode);
            service.putExtra("resultData",data);
            startForegroundService(service);
            Toast.makeText(this,"Captura autorizada; abre una oferta de inDrive",Toast.LENGTH_LONG).show();
        }
    }
}
