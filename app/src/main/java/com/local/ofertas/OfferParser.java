package com.local.ofertas;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Interpreta una solicitud de inDrive con la estructura observada en Vivo/Funtouch 14.
 * No selecciona precios de contraoferta como si fueran la tarifa del pasajero.
 */
public final class OfferParser {
    private OfferParser() {}
    public static final class Parsed {
        public Double fare, pickupKm, tripKm, minutes;
        public String warning = "";
        public boolean complete() { return fare != null && pickupKm != null && tripKm != null && minutes != null; }
    }
    private static final Pattern MONEY = Pattern.compile("(?i)(?:COL\\s*\\$|COP\\s*\\$?|\\$)\\s*([0-9][0-9., ]{2,12})");
    private static final Pattern DIST = Pattern.compile("(?i)([0-9]+(?:[.,][0-9]+)?)\\s*k\\s*m\\b");
    private static final Pattern MIN = Pattern.compile("(?i)([0-9]+)\\s*(?:min(?:utos?)?|mins?)\\b");
    private static double decimal(String s) { return Double.parseDouble(s.trim().replace(',', '.')); }
    private static double amount(String s) {
        s=s.replace(" ","");
        if(s.contains(",") && s.contains(".")) s=s.replace(".","").replace(",",".");
        else if(s.contains(".")) {int i=s.lastIndexOf('.');if(s.length()-i-1==3)s=s.replace(".","");}
        else if(s.contains(",")) {int i=s.lastIndexOf(',');if(s.length()-i-1==3)s=s.replace(",","");else s=s.replace(',','.');}
        return Double.parseDouble(s);
    }
    public static Parsed parse(String visibleText) {
        Parsed p=new Parsed();
        if(visibleText==null || visibleText.trim().isEmpty()) {p.warning="Sin texto reconocible";return p;}
        // Los botones de contraoferta quedan bajo este encabezado y deben descartarse.
        Matcher offerActions=Pattern.compile("(?i)ofre(?:ce|cer)?\\s+tu\\s+tarifa|prop[oó]n\\s+tu\\s+tarifa").matcher(visibleText);
        String primary=offerActions.find()?visibleText.substring(0,offerActions.start()):visibleText;
        List<Double> fares=new ArrayList<>(), distances=new ArrayList<>(), durations=new ArrayList<>();
        Matcher m=MONEY.matcher(primary);
        while(m.find()) try {double v=amount(m.group(1));if(v>=1500 && v<=3000000) fares.add(v);}catch(Exception ignored){}
        // Prioridad: tarifa principal antes de botones "Aceptar por" (no confundir sugerencias).
        if(!fares.isEmpty())p.fare=fares.get(0);
        // Distancias del mapa arriba de "Ten cuidado en esta ruta" o de la tarifa.
        // Primero 2,0 km recogida (A), segundo 8,6 km trayecto (A -> B).
        int mapLimit=primary.length();
        Matcher safety=Pattern.compile("(?i)ten\\s+cuidado\\s+en\\s+esta\\s+ruta").matcher(primary);
        if(safety.find())mapLimit=safety.start();
        else {
            Matcher fareAt=MONEY.matcher(primary);
            if(fareAt.find())mapLimit=fareAt.start();
        }
        String map=primary.substring(0,mapLimit);
        m=DIST.matcher(map);
        while(m.find())try {double v=decimal(m.group(1));if(v>=0 && v<=500)distances.add(v);}catch(Exception ignored){}
        m=MIN.matcher(map);
        while(m.find())try {double v=decimal(m.group(1));if(v>0 && v<=600)durations.add(v);}catch(Exception ignored){}
        if(distances.size()==2){p.pickupKm=distances.get(0);p.tripKm=distances.get(1);}
        if(durations.size()==2)p.minutes=durations.get(0)+durations.get(1);
        if(!p.complete()) p.warning="Lectura incompleta: revisar tarifa, 2 distancias y 2 tiempos del mapa.";
        return p;
    }
}
