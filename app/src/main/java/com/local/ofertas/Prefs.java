package com.local.ofertas;

import android.content.Context;
import android.content.SharedPreferences;

public final class Prefs {
    // Mantener el archivo para conservar metas que el usuario ya haya editado.
    private static final String NAME = "rentabilidad_config_v1";
    private Prefs() {}

    public static ProfitEngine.Config read(Context context) {
        SharedPreferences p = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        ProfitEngine.Config c = new ProfitEngine.Config();
        c.fuelPurchaseCop = get(p, "fuelPurchaseCop", c.fuelPurchaseCop);
        c.fuelPurchaseGallons = get(p, "fuelPurchaseGallons", c.fuelPurchaseGallons);
        c.kmPerGallon = get(p, "kmPerGallon", c.kmPerGallon);
        c.soatAnnual = get(p, "soatAnnual", c.soatAnnual);
        c.externalInsurancePayment = get(p, "externalInsurancePayment", c.externalInsurancePayment);
        c.externalInsuranceIntervalMonths = get(p, "externalInsuranceIntervalMonths", c.externalInsuranceIntervalMonths);
        c.inspectionAnnual = get(p, "inspectionAnnual", c.inspectionAnnual);
        c.tiresCost = get(p, "tiresCost", c.tiresCost);
        c.tireReplacementYears = get(p, "tireReplacementYears", c.tireReplacementYears);
        c.maintenanceAnnual = get(p, "maintenanceAnnual", c.maintenanceAnnual);
        c.annualKilometers = get(p, "annualKilometers", c.annualKilometers);
        c.commissionPercent = get(p, "commissionPercent", c.commissionPercent);
        c.timeCostPerHour = get(p, "timeCostPerHour", c.timeCostPerHour);
        c.minProfit = get(p, "minProfit", c.minProfit);
        c.minProfitPerKm = get(p, "minProfitPerKm", c.minProfitPerKm);
        c.minProfitPerHour = get(p, "minProfitPerHour", c.minProfitPerHour);
        c.marginTolerance = get(p, "marginTolerance", c.marginTolerance);
        return c;
    }

    public static boolean annualKmConfirmed(Context context) {
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getBoolean("annualKmConfirmed", true);
    }

    public static boolean insurancePeriodConfirmed(Context context) {
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getBoolean("insurancePeriodConfirmed", true);
    }

    private static double get(SharedPreferences p, String k, double v) {
        try { return Double.parseDouble(p.getString(k, Double.toString(v))); }
        catch (Exception ex) { return v; }
    }

    public static void write(Context context, ProfitEngine.Config c,
                             boolean confirmedMileage, boolean confirmedInsurance) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            .putString("fuelPurchaseCop", Double.toString(c.fuelPurchaseCop))
            .putString("fuelPurchaseGallons", Double.toString(c.fuelPurchaseGallons))
            .putString("kmPerGallon", Double.toString(c.kmPerGallon))
            .putString("soatAnnual", Double.toString(c.soatAnnual))
            .putString("externalInsurancePayment", Double.toString(c.externalInsurancePayment))
            .putString("externalInsuranceIntervalMonths", Double.toString(c.externalInsuranceIntervalMonths))
            .putString("inspectionAnnual", Double.toString(c.inspectionAnnual))
            .putString("tiresCost", Double.toString(c.tiresCost))
            .putString("tireReplacementYears", Double.toString(c.tireReplacementYears))
            .putString("maintenanceAnnual", Double.toString(c.maintenanceAnnual))
            .putString("annualKilometers", Double.toString(c.annualKilometers))
            .putString("commissionPercent", Double.toString(c.commissionPercent))
            .putString("timeCostPerHour", Double.toString(c.timeCostPerHour))
            .putString("minProfit", Double.toString(c.minProfit))
            .putString("minProfitPerKm", Double.toString(c.minProfitPerKm))
            .putString("minProfitPerHour", Double.toString(c.minProfitPerHour))
            .putString("marginTolerance", Double.toString(c.marginTolerance))
            .putBoolean("annualKmConfirmed", confirmedMileage)
            .putBoolean("insurancePeriodConfirmed", confirmedInsurance)
            .apply();
    }
}
