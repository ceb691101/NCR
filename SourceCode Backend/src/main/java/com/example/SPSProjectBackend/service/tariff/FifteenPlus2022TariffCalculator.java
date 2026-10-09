package com.example.SPSProjectBackend.service.tariff;

import com.example.SPSProjectBackend.model.NcreDevTariffRate;
import com.example.SPSProjectBackend.repository.NcreDevTariffRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * TariffCalculator implementation for 15+ New (2022) tariffs (tariff_type = "15_2022").
 * 
 * Handles rate selection from ncre_dev_year_tariff_rates using folio_no,
 * safe YearMonth comparison between tariff_changed and billingMonth,
 * transition-month split calculation, generation loss deduction,
 * and cost of energy calculation.
 */
@Component("15_2022TariffCalculator")
public class FifteenPlus2022TariffCalculator implements TariffCalculator {

    private static final String TARIFF_TYPE_CODE = "15_2022";
    private static final String TARIFF_TYPE_DESC = "15+ New (2022)";

    private final NcreDevTariffRateRepository tariffRateRepository;

    @Autowired
    public FifteenPlus2022TariffCalculator(NcreDevTariffRateRepository tariffRateRepository) {
        this.tariffRateRepository = tariffRateRepository;
    }

    @Override
    public boolean supports(String tariffType) {
        if (tariffType == null) {
            return false;
        }
        String normalized = tariffType.trim();
        return TARIFF_TYPE_CODE.equalsIgnoreCase(normalized) || TARIFF_TYPE_DESC.equalsIgnoreCase(normalized);
    }

    @Override
    public TariffCalculationResult calculate(TariffCalculationContext context) {
        if (context == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TariffCalculationContext must not be null");
        }

        // 1. Validate required identifiers
        if (context.getTariffType() == null || context.getTariffType().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tariff type is required for tariff calculation.");
        }

        Short folioNo = context.getFolioNo();
        if (folioNo == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Folio number (folio_no) is required for tariff calculation.");
        }

        // 2. Fetch tariff rate configuration from ncre_dev_year_tariff_rates by folio_no
        NcreDevTariffRate rateRecord = tariffRateRepository.findByFolioNo(folioNo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No tariff rate configuration found for folio: " + folioNo));

        // 3. Validate tariff_changed date
        if (rateRecord.getTariffChanged() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tariff changed date is missing in tariff rate configuration for folio: " + folioNo);
        }

        // 4. Resolve Billing Month (Current Calendar Month - 1 Month)
        YearMonth billingMonth = context.getBillingMonth() != null
                ? context.getBillingMonth()
                : YearMonth.now().minusMonths(1);

        YearMonth tariffChangedMonth = toYearMonth(rateRecord.getTariffChanged());

        // 5. Deduct generation losses once on monthly energy sent to grid
        BigDecimal energySentToGrid = context.getTotalEnergySentToGrid() != null
                ? context.getTotalEnergySentToGrid()
                : BigDecimal.ZERO;

        BigDecimal lossKwh = calculateGenerationLossKwh(energySentToGrid, context);

        BigDecimal eligibleEnergy = energySentToGrid.subtract(lossKwh);
        if (eligibleEnergy.compareTo(BigDecimal.ZERO) < 0) {
            eligibleEnergy = BigDecimal.ZERO;
        }
        eligibleEnergy = eligibleEnergy.setScale(2, RoundingMode.HALF_UP);

        BigDecimal selectedRate;
        TariffRateType selectedRateType;
        BigDecimal costOfEnergy;
        List<TariffChargeLine> chargeLines = null;

        // 6. Rate Selection based on comparison direction:
        //    billingMonth > tariffChangedMonth  -> cur_tariff_rate
        //    billingMonth < tariffChangedMonth  -> prv_tariff_rate
        //    billingMonth == tariffChangedMonth -> TRANSITION MONTH SPLIT CALCULATION
        int cmp = billingMonth.compareTo(tariffChangedMonth);
        if (cmp > 0) {
            // CASE 1: current bill cycle month > change date month -> cur_tariff_rate
            if (rateRecord.getCurTariffRate() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Current tariff rate (cur_tariff_rate) is missing in configuration for folio: " + folioNo);
            }
            selectedRate = rateRecord.getCurTariffRate();
            selectedRateType = TariffRateType.CUR_TARIFF_RATE;
            costOfEnergy = eligibleEnergy.multiply(selectedRate).setScale(2, RoundingMode.HALF_UP);
        } else if (cmp < 0) {
            // CASE 2: current bill cycle month < change date month -> prv_tariff_rate
            if (rateRecord.getPrvTariffRate() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Previous tariff rate (prv_tariff_rate) is missing in configuration for folio: " + folioNo);
            }
            selectedRate = rateRecord.getPrvTariffRate();
            selectedRateType = TariffRateType.PRV_TARIFF_RATE;
            costOfEnergy = eligibleEnergy.multiply(selectedRate).setScale(2, RoundingMode.HALF_UP);
        } else {
            // CASE 3: current bill cycle month == change date month -> TRANSITION MONTH SPLIT
            if (rateRecord.getPrvTariffRate() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Previous tariff rate (prv_tariff_rate) is missing in configuration for folio: " + folioNo);
            }
            if (rateRecord.getCurTariffRate() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Current tariff rate (cur_tariff_rate) is missing in configuration for folio: " + folioNo);
            }

            LocalDate tariffChangedDate = toLocalDate(rateRecord.getTariffChanged());
            int totalDays = billingMonth.lengthOfMonth();
            int beforeDays = tariffChangedDate.getDayOfMonth() - 1;
            int afterDays = totalDays - beforeDays;

            if (beforeDays < 0 || afterDays < 0 || (beforeDays + afterDays != totalDays)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid transition date " + tariffChangedDate + " for billing month " + billingMonth);
            }

            BigDecimal beforeEligibleEnergy = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            BigDecimal afterEligibleEnergy = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

            if (eligibleEnergy.compareTo(BigDecimal.ZERO) > 0 && totalDays > 0) {
                if (beforeDays == 0) {
                    afterEligibleEnergy = eligibleEnergy;
                } else if (afterDays == 0) {
                    beforeEligibleEnergy = eligibleEnergy;
                } else {
                    beforeEligibleEnergy = eligibleEnergy
                            .multiply(BigDecimal.valueOf(beforeDays))
                            .divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP);
                    afterEligibleEnergy = eligibleEnergy.subtract(beforeEligibleEnergy);
                }
            }

            BigDecimal beforeCost = beforeEligibleEnergy.multiply(rateRecord.getPrvTariffRate()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal afterCost = afterEligibleEnergy.multiply(rateRecord.getCurTariffRate()).setScale(2, RoundingMode.HALF_UP);
            costOfEnergy = beforeCost.add(afterCost).setScale(2, RoundingMode.HALF_UP);

            // Effective rate for backward-compatible storage in invoices.rate_per_kwh
            BigDecimal effectiveRate = eligibleEnergy.compareTo(BigDecimal.ZERO) > 0
                    ? costOfEnergy.divide(eligibleEnergy, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

            selectedRate = effectiveRate;
            selectedRateType = TariffRateType.MULTI_RATE;

            chargeLines = new ArrayList<>();
            if (beforeDays > 0) {
                chargeLines.add(TariffChargeLine.builder()
                        .label("Before Tariff Change")
                        .periodStart(billingMonth.atDay(1))
                        .periodEnd(tariffChangedDate.minusDays(1))
                        .numberOfDays(beforeDays)
                        .energyKwh(beforeEligibleEnergy)
                        .ratePerKwh(rateRecord.getPrvTariffRate())
                        .costOfEnergy(beforeCost)
                        .rateType(TariffRateType.PRV_TARIFF_RATE)
                        .build());
            }
            if (afterDays > 0) {
                chargeLines.add(TariffChargeLine.builder()
                        .label("From Tariff Change Date")
                        .periodStart(tariffChangedDate)
                        .periodEnd(billingMonth.atEndOfMonth())
                        .numberOfDays(afterDays)
                        .energyKwh(afterEligibleEnergy)
                        .ratePerKwh(rateRecord.getCurTariffRate())
                        .costOfEnergy(afterCost)
                        .rateType(TariffRateType.CUR_TARIFF_RATE)
                        .build());
            }
        }

        return TariffCalculationResult.builder()
                .tariffType(context.getTariffType())
                .selectedTariffRate(selectedRate)
                .originalEnergySentToGrid(energySentToGrid)
                .generationLosses(lossKwh)
                .eligibleEnergy(eligibleEnergy)
                .costOfEnergy(costOfEnergy)
                .selectedRateType(selectedRateType)
                .chargeLines(chargeLines)
                .build();
    }

    /**
     * Calculates generation loss in kWh based on confirmed business rules.
     * If explicitLossKwh is given, it is used directly.
     * Otherwise, percentage from developer / agreement is applied:
     * - Fraction (e.g. 0.05 for 5%): loss = energy * 0.05
     * - Percentage (e.g. 5.0 for 5%): loss = energy * (5.0 / 100)
     */
    private BigDecimal calculateGenerationLossKwh(BigDecimal energySentToGrid, TariffCalculationContext context) {
        if (context.getExplicitLossKwh() != null) {
            return context.getExplicitLossKwh().setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal genLossVal = context.getGenerationLosses();
        if (genLossVal == null || genLossVal.compareTo(BigDecimal.ZERO) <= 0 || energySentToGrid.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal lossKwh;
        if (genLossVal.compareTo(BigDecimal.ONE) >= 0) {
            // Value is a percentage like 1.00, 2.00, 3.00, or 5.00 (%)
            lossKwh = energySentToGrid.multiply(genLossVal).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            // Value is a decimal fraction like 0.01, 0.02, 0.03, or 0.05 (representing 1%, 2%, 3%, 5%)
            lossKwh = energySentToGrid.multiply(genLossVal).setScale(2, RoundingMode.HALF_UP);
        }

        return lossKwh;
    }

    /**
     * Safely converts java.util.Date / java.sql.Date to LocalDate.
     */
    private LocalDate toLocalDate(Date date) {
        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Safely converts java.util.Date / java.sql.Date to YearMonth.
     */
    private YearMonth toYearMonth(Date date) {
        return YearMonth.from(toLocalDate(date));
    }
}
