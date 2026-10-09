package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.model.NcreBillCycle;
import com.example.SPSProjectBackend.model.NcreDevTariffRate;
import com.example.SPSProjectBackend.repository.NcreBillCycleRepository;
import com.example.SPSProjectBackend.repository.NcreDevTariffRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

/**
 * Resolves the effective tariff rate for a developer's folio from
 * ncre_dev_year_tariff_rates, using the active bill month from ncre_bill_cycle.
 *
 * Rule:
 *   active bill month &lt;  month(tariff_changed) -> prv_tariff_rate
 *   active bill month &gt;= month(tariff_changed) -> cur_tariff_rate
 */
@Service
public class TariffRateResolver {

    @Autowired
    private NcreDevTariffRateRepository tariffRateRepository;

    @Autowired
    private NcreBillCycleRepository billCycleRepository;

    /**
     * @param folioNo   developer folio number (matches ncre_dev_year_tariff_rates.folio_no)
     * @param billCycle bill cycle number (used to look up its active bill month)
     * @return the resolved rate as a string, or null when no configuration exists
     */
    public String resolveRate(Short folioNo, String billCycle) {
        if (folioNo == null) {
            return null;
        }

        Optional<NcreDevTariffRate> rateOpt = tariffRateRepository.findByFolioNo(folioNo);
        if (!rateOpt.isPresent()) {
            return null;
        }
        NcreDevTariffRate rate = rateOpt.get();

        Short activeBillMonth = resolveActiveBillMonth(billCycle);
        Date tariffChanged = rate.getTariffChanged();

        if (activeBillMonth == null || tariffChanged == null) {
            // Cannot compare months - default to the current rate
            return toRateString(rate.getCurTariffRate());
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(tariffChanged);
        int changedMonth = cal.get(Calendar.MONTH) + 1;

        if (activeBillMonth < changedMonth) {
            return toRateString(rate.getPrvTariffRate());
        }
        return toRateString(rate.getCurTariffRate());
    }

    private Short resolveActiveBillMonth(String billCycle) {
        if (billCycle == null || billCycle.trim().isEmpty()) {
            return null;
        }
        try {
            Short cycle = Short.valueOf(billCycle.trim());
            return billCycleRepository.findById(cycle)
                    .map(NcreBillCycle::getBillMonth)
                    .orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String toRateString(BigDecimal rate) {
        return rate != null ? rate.setScale(2, RoundingMode.HALF_UP).toPlainString() : null;
    }
}
