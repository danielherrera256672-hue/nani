package com.local.ofertas;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** Parámetros del vehículo y metas de utilidad completamente editables en el teléfono. */
public class SettingsActivity extends Activity {
    private static final String[] NAMES = {
        "Costo de la compra de combustible (COP)",
        "Galones de esa compra",
        "Rendimiento real (km/galón)",
        "Kilómetros recorridos al año",
        "SOAT (COP/año)",
        "Seguro adicional (COP por pago)",
        "Periodicidad seguro adicional (meses entre pagos)",
        "Tecnomecánica (COP/año)",
        "Costo del juego de llantas (COP)",
        "Duración estimada llantas (años)",
        "Mantenimiento (COP/año)",
        "Comisión de plataforma (%)",
        "Costo de oportunidad del tiempo (COP/hora; 0 si no se descuenta)",
        "Utilidad mínima por viaje (COP)",
        "Utilidad mínima por km total (COP/km)",
        "Utilidad mínima por hora (COP/h)",
        "Tolerancia de zona marginal (0 a 0,9)"
    };
    private final EditText[] fields = new EditText[NAMES.length];
    private CheckBox insuranceConfirmed;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        ProfitEngine.Config c = Prefs.read(this);
        double[] values = {
            c.fuelPurchaseCop, c.fuelPurchaseGallons, c.kmPerGallon, c.annualKilometers,
            c.soatAnnual, c.externalInsurancePayment, c.externalInsuranceIntervalMonths,
            c.inspectionAnnual, c.tiresCost, c.tireReplacementYears,
            c.maintenanceAnnual, c.commissionPercent, c.timeCostPerHour,
            c.minProfit, c.minProfitPerKm, c.minProfitPerHour, c.marginTolerance
        };
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = Ui.column(this, 18);
        root.addView(Ui.label(this,"Costos y metas de rentabilidad",23,true));
        root.addView(Ui.label(this,
            "Los valores se almacenan únicamente en tu teléfono. Los 17.000 km anuales y los demás costos fueron suministrados por ti. Los mínimos de utilidad son metas provisionales y puedes modificarlos.",14,false));
        for (int i = 0; i < fields.length; i++) {
            if (i == 0) root.addView(Ui.label(this,"COMBUSTIBLE Y RECORRIDO",17,true));
            if (i == 4) root.addView(Ui.label(this,"GASTOS ANUALES Y PERIÓDICOS",17,true));
            if (i == 11) root.addView(Ui.label(this,"COMISIÓN Y OBJETIVOS",17,true));
            root.addView(Ui.label(this,NAMES[i],13,true));
            fields[i] = Ui.decimal(this, Double.toString(values[i]));
            root.addView(fields[i]);
        }
        insuranceConfirmed = new CheckBox(this);
        insuranceConfirmed.setText("Seguro adicional de $89.000: pago anual confirmado");
        insuranceConfirmed.setChecked(Prefs.insurancePeriodConfirmed(this));
        root.addView(insuranceConfirmed);
        TextView note=Ui.label(this,
            "El seguro adicional fue confirmado como pago ANUAL (12 meses). " +
            "Si cambia la frecuencia de pago, ajusta los meses para actualizar el costo por km.",13,false);
        root.addView(note);
        android.widget.Button save = Ui.button(this,"Guardar parámetros");
        root.addView(save);
        save.setOnClickListener(v -> {
            double[] n = new double[fields.length];
            for(int i = 0; i < n.length; i++) {
                n[i] = Ui.number(fields[i]);
                if (!Ui.valid(n[i])) {
                    Toast.makeText(this,"Valor inválido: " + NAMES[i],Toast.LENGTH_LONG).show();
                    return;
                }
            }
            if (n[1] <= 0 || n[2] <= 0 || n[3] <= 0 || n[6] <= 0 || n[9] <= 0 ||
                n[11] >= 100 || n[16] > 0.9) {
                Toast.makeText(this,
                    "Revisa galones, rendimiento, km anuales, meses, años, comisión (<100) y tolerancia (<=0,9)",
                    Toast.LENGTH_LONG).show();
                return;
            }
            ProfitEngine.Config x = new ProfitEngine.Config();
            x.fuelPurchaseCop=n[0];x.fuelPurchaseGallons=n[1];x.kmPerGallon=n[2];x.annualKilometers=n[3];
            x.soatAnnual=n[4];x.externalInsurancePayment=n[5];x.externalInsuranceIntervalMonths=n[6];
            x.inspectionAnnual=n[7];x.tiresCost=n[8];x.tireReplacementYears=n[9];
            x.maintenanceAnnual=n[10];x.commissionPercent=n[11];x.timeCostPerHour=n[12];
            x.minProfit=n[13];x.minProfitPerKm=n[14];x.minProfitPerHour=n[15];x.marginTolerance=n[16];
            Prefs.write(this,x,true,insuranceConfirmed.isChecked());
            Toast.makeText(this,"Costos y metas actualizados",Toast.LENGTH_SHORT).show();
            finish();
        });
        scroll.addView(root);
        setContentView(scroll);
    }
}
