package com.local.ofertas;

/** Cuentas locales de rentabilidad. Sin dependencias Android para probarlas por separado. */
public final class ProfitEngine {
    private ProfitEngine() {}

    public static final class Config {
        // Datos declarados por el conductor (16/11/2026).
        public double fuelPurchaseCop = 140000;
        public double fuelPurchaseGallons = 8.4;
        public double kmPerGallon = 40;
        public double soatAnnual = 750000;
        public double externalInsurancePayment = 89000;
        // Confirmado: el seguro de 89.000 COP se paga una vez al año.
        public double externalInsuranceIntervalMonths = 12;
        public double inspectionAnnual = 400000;
        public double tiresCost = 650000;
        public double tireReplacementYears = 2;
        public double maintenanceAnnual = 2000000;
        // Recorrido anual aproximado confirmado por el conductor.
        public double annualKilometers = 17000;
        public double commissionPercent = 10.5;
        // Sin valor confirmado del tiempo; se deja 0. Hay una meta neta/h aparte.
        public double timeCostPerHour = 0;
        // OBJETIVOS PROVISIONALES: editables a criterio del conductor.
        public double minProfit = 5000;
        public double minProfitPerKm = 900;
        public double minProfitPerHour = 12000;
        public double marginTolerance = 0.15;

        public double fuelPricePerGallon() {
            return Math.max(0, fuelPurchaseCop) / Math.max(0.001, fuelPurchaseGallons);
        }
        public double fuelCostPerKm() {
            return fuelPricePerGallon() / Math.max(0.001, kmPerGallon);
        }
        public double annualInsuranceExternal() {
            return Math.max(0, externalInsurancePayment) * 12.0 /
                Math.max(0.001, externalInsuranceIntervalMonths);
        }
        public double annualTireCost() {
            return Math.max(0, tiresCost) / Math.max(0.001, tireReplacementYears);
        }
        public double annualOtherFixedCosts() {
            return Math.max(0, soatAnnual) + annualInsuranceExternal() +
                Math.max(0, inspectionAnnual) + annualTireCost();
        }
        public double annualTotalCosts() {
            return annualOtherFixedCosts() + Math.max(0, maintenanceAnnual);
        }
        public double annualCostPerKm() {
            return annualTotalCosts() / Math.max(1, annualKilometers);
        }
    }

    public static final class Offer {
        public double fare, pickupKm, tripKm, totalMinutes;
    }

    public static final class Result {
        public double totalKm, fuelCost, maintenanceCost, otherFixedCost, commission;
        public double timeCost, profit, profitPerKm, profitPerHour, revenue;
        public double annualCostPerKm, annualTotalCosts, operatingProfit;
        public double minimumFareForTargets;
        public String verdict;
    }

    public static Result calculate(Config c, Offer o) {
        Result r = new Result();
        r.totalKm = Math.max(0, o.pickupKm) + Math.max(0, o.tripKm);
        double fare = Math.max(0, o.fare);
        r.fuelCost = r.totalKm * c.fuelCostPerKm();
        double yearlyKm = Math.max(1, c.annualKilometers);
        r.maintenanceCost = r.totalKm * Math.max(0, c.maintenanceAnnual) / yearlyKm;
        r.otherFixedCost = r.totalKm * c.annualOtherFixedCosts() / yearlyKm;
        r.annualTotalCosts = c.annualTotalCosts();
        r.annualCostPerKm = c.annualCostPerKm();
        double commissionRate = Math.min(1, Math.max(0, c.commissionPercent / 100.0));
        r.commission = fare * commissionRate;
        r.timeCost = Math.max(0, o.totalMinutes) / 60.0 * Math.max(0, c.timeCostPerHour);
        r.revenue = fare - r.commission;
        r.operatingProfit = r.revenue - r.fuelCost - r.maintenanceCost - r.otherFixedCost;
        r.profit = r.operatingProfit - r.timeCost;
        r.profitPerKm = r.totalKm > 0 ? r.profit / r.totalKm : 0;
        r.profitPerHour = o.totalMinutes > 0 ? r.profit * 60.0 / o.totalMinutes : 0;
        double profitTarget = Math.max(0, c.minProfit);
        double perKmTarget = Math.max(0, c.minProfitPerKm);
        double perHourTarget = Math.max(0, c.minProfitPerHour);
        boolean profitable = r.profit >= profitTarget && r.profitPerKm >= perKmTarget &&
                r.profitPerHour >= perHourTarget;
        double tolerance = Math.min(0.9, Math.max(0, c.marginTolerance));
        boolean marginal = r.profit >= profitTarget * (1 - tolerance) &&
                r.profitPerKm >= perKmTarget * (1 - tolerance) &&
                r.profitPerHour >= perHourTarget * (1 - tolerance);
        r.verdict = profitable ? "RENTABLE" : (marginal ? "MARGINAL" : "NO RENTABLE");

        double neededProfit = Math.max(profitTarget,
                Math.max(r.totalKm * perKmTarget, Math.max(0, o.totalMinutes) * perHourTarget / 60.0));
        double needsBeforeCommission = neededProfit + r.fuelCost + r.maintenanceCost + r.otherFixedCost + r.timeCost;
        r.minimumFareForTargets = commissionRate >= 1 ? Double.POSITIVE_INFINITY :
                needsBeforeCommission / (1.0 - commissionRate);
        return r;
    }
}
