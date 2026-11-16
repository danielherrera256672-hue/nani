import com.local.ofertas.ProfitEngine;
import com.local.ofertas.OfferParser;

public final class FinancialTest {
    private static void checkNear(String label, double actual, double expected, double tol) {
        if(Math.abs(actual-expected)>tol) throw new AssertionError(label+": "+actual+" vs "+expected);
    }
    public static void main(String[] args) {
        ProfitEngine.Config c=new ProfitEngine.Config();
        checkNear("km por año",c.annualKilometers,17000,0.00001);
        checkNear("Combustible COP/km",c.fuelCostPerKm(),416.6666667,0.0001);
        checkNear("Gasto anual",c.annualTotalCosts(),3564000,0.0001);
        checkNear("Otros COP/km",c.annualCostPerKm(),209.6470588,0.0001);
        ProfitEngine.Offer o=new ProfitEngine.Offer();
        o.fare=21800;o.pickupKm=2;o.tripKm=8.6;o.totalMinutes=37;
        ProfitEngine.Result r=ProfitEngine.calculate(c,o);
        checkNear("Comisión",r.commission,2289,0.0001);
        checkNear("Combustible total",r.fuelCost,4416.666667,0.0001);
        checkNear("Gastos prorrateados",r.maintenanceCost+r.otherFixedCost,2222.258824,0.0001);
        checkNear("Utilidad",r.profit,12872.07451,0.0001);
        checkNear("km",r.totalKm,10.6,0.0001);
        checkNear("Tarifa mínima",r.minimumFareForTargets,18077.012,0.002);
        if(!"RENTABLE".equals(r.verdict)) throw new AssertionError("Semáforo esperado rentable");
        c.externalInsuranceIntervalMonths=1;
        ProfitEngine.Result moreCost=ProfitEngine.calculate(c,o);
        if(moreCost.profit>=r.profit)throw new AssertionError("Seguro mensual debe bajar utilidad");
        String text="Solicitud de viaje\n12 min\n2,0 km\n25 min\n8,6 km\nTen cuidado en esta ruta\nCOL$ 21.800\nPrecio justo\nAceptar por COL$ 21.800\nOfrece tu tarifa\nCOL$ 22.800\nCOL$ 24.000\nCerrar";
        OfferParser.Parsed p=OfferParser.parse(text);
        if(!p.complete())throw new AssertionError("OCR parse incompleto: "+p.warning);
        checkNear("Tarifa OCR",p.fare,21800,0.01);
        checkNear("Pickup OCR",p.pickupKm,2,0.01);
        checkNear("Trayecto OCR",p.tripKm,8.6,0.01);
        checkNear("Minutos OCR",p.minutes,37,0.01);
        System.out.println("OK: combustible, anualizaciones, rentabilidad, minima, mensual vs anual, parser");
    }
}
