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
import java.util.Date;

/**
 * TariffCalculator implementation for FLAT rate tariffs.
 * 
 * Handles rate selection from ncre_dev_year_tariff_rates using folio_no,
 * safe YearMonth comparison against tariff_changed, generation-loss deduction,
 * and cost of energy calculation.
 */
@Component
public class FlatTariffCalculator implements TariffCalculator {

    private final NcreDevTariffRateRepository tariffRateRepository;

    @Autowired
    public FlatTariffCalculator(NcreDevTariffRateRepository tariffRateRepository) {
        this.tariffRateRepository = tariffRateRepository;
    }

    @Override
    public boolean supports(String tariffType) {
        if (tariffType == null) {
            return false;
        }
        String normalized = tariffType.trim().toUpperCase();
        return normalized.startsWith("FLAT") || normalized.contains("FLAT");
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

        // 5. Rate Selection based on YearMonth comparison
        BigDecimal selectedRate;
        TariffRateType selectedRateType;

        int cmp = billingMonth.compareTo(tariffChangedMonth);
        if (cmp <= 0) {
            // CASE 1: billingMonth <= tariffChangedMonth -> prv_tariff_rate
            if (rateRecord.getPrvTariffRate() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Previous tariff rate (prv_tariff_rate) is missing in configuration for folio: " + folioNo);
            }
            selectedRate = rateRecord.getPrvTariffRate();
            selectedRateType = TariffRateType.PRV_TARIFF_RATE;
        } else {
            // CASE 2: billingMonth > tariffChangedMonth -> cur_tariff_rate
            if (rateRecord.getCurTariffRate() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Current tariff rate (cur_tariff_rate) is missing in configuration for folio: " + folioNo);
            }
            selectedRate = rateRecord.getCurTariffRate();
            selectedRateType = TariffRateType.CUR_TARIFF_RATE;
        }

        // 6. Deduct Generation Losses
        // Confirmed unit: generation_losses in ncre_developers / ncre_agreements represents a PERCENTAGE (%).
        // Explicit loss kWh can also be provided directly via context.getExplicitLossKwh() (e.g. in test suites).
        BigDecimal energySentToGrid = context.getTotalEnergySentToGrid() != null
                ? context.getTotalEnergySentToGrid()
                : BigDecimal.ZERO;

        BigDecimal lossKwh = calculateGenerationLossKwh(energySentToGrid, context);

        BigDecimal eligibleEnergy = energySentToGrid.subtract(lossKwh);
        if (eligibleEnergy.compareTo(BigDecimal.ZERO) < 0) {
            eligibleEnergy = BigDecimal.ZERO;
        }
        eligibleEnergy = eligibleEnergy.setScale(2, RoundingMode.HALF_UP);

        // 7. Calculate Cost of Energy = Eligible Energy x Selected Tariff Rate
        BigDecimal costOfEnergy = eligibleEnergy.multiply(selectedRate).setScale(2, RoundingMode.HALF_UP);

        return TariffCalculationResult.builder()
                .tariffType(context.getTariffType())
                .selectedTariffRate(selectedRate)
                .originalEnergySentToGrid(energySentToGrid)
                .generationLosses(lossKwh)
                .eligibleEnergy(eligibleEnergy)
                .costOfEnergy(costOfEnergy)
                .selectedRateType(selectedRateType)
                .build();
    }

    /**
     * Calculates generation loss in kWh based on confirmed business rules.
     * If explicitLossKwh is given, it is used directly.
     * Otherwise, percentage from developer is applied:
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
     * Safely converts java.util.Date / java.sql.Date to YearMonth.
     */
    private YearMonth toYearMonth(Date date) {
        if (date instanceof java.sql.Date) {
            return YearMonth.from(((java.sql.Date) date).toLocalDate());
        }
        return YearMonth.from(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }
}
