package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.BrandDTO;
import com.example.SPSProjectBackend.dto.MeterAmendmentDTO;
import com.example.SPSProjectBackend.dto.MeterAmendmentRequestDTO;
import com.example.SPSProjectBackend.dto.MeterTypeDataDTO;
import com.example.SPSProjectBackend.dto.MtrDetailDTO;
import com.example.SPSProjectBackend.dto.MtrReasonDTO;
import com.example.SPSProjectBackend.model.Brand;
import com.example.SPSProjectBackend.model.BulkCustomer;
import com.example.SPSProjectBackend.model.MtrReason;
import com.example.SPSProjectBackend.repository.BrandRepository;
import com.example.SPSProjectBackend.repository.BulkCustomerRepository;
import com.example.SPSProjectBackend.repository.MtrReasonRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MeterIntegrationService {

    @Autowired
    private BulkCustomerRepository bulkCustomerRepository;

    @Autowired
    private MtrDetailService mtrDetailService;

    @Autowired
    private MtrReasonRepository mtrReasonRepository;

    @Autowired
    private BrandRepository brandRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public Map<String, Object> getMetersByAccountNumber(String accNbr) {
        Optional<BulkCustomer> customerOpt = bulkCustomerRepository.findByAccNbrTrimmed(accNbr);
        if (customerOpt.isEmpty()) {
            throw new IllegalArgumentException("Customer not found for account number: " + accNbr);
        }

        BulkCustomer customer = customerOpt.get();
        String instId = customer.getInstId();
        List<MtrDetailDTO> meters = mtrDetailService.getAllByInstId(instId);

        Map<String, Object> response = new HashMap<>();
        response.put("acc_nbr", customer.getAccNbr());
        response.put("folio_no", customer.getFolioNo());
        response.put("inst_id", instId);
        response.put("bill_cycle", customer.getBillCycle());
        response.put("area_cd", customer.getAreaCd());
        response.put("meters", meters);
        return response;
    }

    public List<MtrReasonDTO> getActiveMeterReasons() {
        return mtrReasonRepository.findByStatus("A")
                .stream()
                .map(this::convertReasonToDTO)
                .collect(Collectors.toList());
    }

    public List<BrandDTO> getActiveBrands() {
        return brandRepository.findByStatus("A")
                .stream()
                .map(this::convertBrandToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Returns the latest billed reading (rdngs.rdn) per meter type for an account + sequence.
     */
    public Map<String, Integer> getLatestBilledReadings(String accNbr, Integer mtrSeq, String mtrNbr) {
        return getLatestBilledReadings(accNbr, mtrSeq, mtrNbr, null);
    }

    /**
     * Returns the latest billed reading (rdngs.rdn) per meter type for an account + sequence
     * as of an optional effective date.
     */
    public Map<String, Integer> getLatestBilledReadings(String accNbr, Integer mtrSeq, String mtrNbr, java.util.Date asOfDate) {
        if (isBlank(accNbr) || mtrSeq == null) {
            return Collections.emptyMap();
        }

        List<Object[]> rows = fetchLatestBilledRows(accNbr, mtrSeq, mtrNbr, asOfDate);

        Map<String, Integer> latestByType = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String meterType = toTrimmedStringOrNull(row[0]);
            Integer billedReading = toInteger(row[1]);
            if (meterType != null && !latestByType.containsKey(meterType)) {
                latestByType.put(meterType, billedReading != null ? billedReading : 0);
            }
        }

        return latestByType;
    }

    /**
     * Returns the latest billed rate (rdngs.rate) per meter type for an account + sequence
     * as of an optional effective date.
     */
    public Map<String, BigDecimal> getLatestBilledRates(String accNbr, Integer mtrSeq, String mtrNbr, java.util.Date asOfDate) {
        if (isBlank(accNbr) || mtrSeq == null) {
            return Collections.emptyMap();
        }

        List<Object[]> rows = fetchLatestBilledRows(accNbr, mtrSeq, mtrNbr, asOfDate);

        Map<String, BigDecimal> latestRatesByType = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String meterType = toTrimmedStringOrNull(row[0]);
            BigDecimal billedRate = toBigDecimal(row[2]);
            if (meterType != null && !latestRatesByType.containsKey(meterType)) {
                latestRatesByType.put(meterType, billedRate != null ? billedRate : BigDecimal.ZERO);
            }
        }

        return latestRatesByType;
    }

    private List<Object[]> fetchLatestBilledRows(String accNbr, Integer mtrSeq, String mtrNbr, java.util.Date asOfDate) {
        java.sql.Date asOfSqlDate = asOfDate != null ? new java.sql.Date(asOfDate.getTime()) : null;

        String sql = "SELECT TRIM(mtr_type) AS mtr_type, rdn, rate " +
                "FROM rdngs " +
                "WHERE TRIM(acc_nbr) = TRIM(:accNbr) " +
                "AND mtr_seq = :mtrSeq " +
                "AND (:mtrNbr IS NULL OR TRIM(mtr_nbr) = TRIM(:mtrNbr)) " +
                "AND (:asOfDate IS NULL OR rdng_date <= :asOfDate) " +
                "ORDER BY rdng_date DESC, entered_dtime DESC";

        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("accNbr", accNbr)
                .setParameter("mtrSeq", mtrSeq)
                .setParameter("mtrNbr", trimOrNull(mtrNbr))
                .setParameter("asOfDate", asOfSqlDate)
                .getResultList();
        return rows;
    }

    @Transactional
    public void createMeterAmendment(MeterAmendmentRequestDTO request) {
        validateRequest(request);

        // Validate that meter types are provided
        if (request.getMeterTypes() == null || request.getMeterTypes().isEmpty()) {
            throw new IllegalArgumentException("At least one meter type is required");
        }

        String sql = "INSERT INTO mtr_amnds (" +
                "acc_nbr, area_cd, added_blcy, effct_blcy, effct_date, type_chg, amd_type, " +
                "mtr_type, mtr_seq, mtrset_type, mtr_order, mtr_nbr, prsnt_rdn, prv_rdn, unts, " +
                "rate, amt, mtr_ratio, ct_ratio, m_factor, br_code, rsn_code, update_status, " +
                "user_id, entered_dtime" +
                ") VALUES (" +
                ":accNbr, :areaCd, :addedBlcy, :effctBlcy, :effctDate, :typeChg, :amdType, " +
                ":mtrType, :mtrSeq, :mtrsetType, :mtrOrder, :mtrNbr, :prsntRdn, :prvRdn, :unts, " +
                ":rate, :amt, :mtrRatio, :ctRatio, :mFactor, :brCode, :rsnCode, :updateStatus, " +
                ":userId, :enteredDtime" +
                ")";

        Timestamp enteredDtime = new Timestamp(System.currentTimeMillis());
        String typeChg = trimOrDefault(request.getTypeChg(), "C");

        // All meter types share the same physical meter number
        String oldMtrNbr = trimOrNull(request.getOldMtrNbr());
        String newMtrNbr = trimOrNull(request.getNewMtrNbr());
        Map<String, Integer> latestBilledReadings = getLatestBilledReadings(
            request.getAccNbr(),
            request.getMtrSeq(),
            oldMtrNbr
        );

        // For each meter type, create both 'R' (removed) and 'A' (added) entries
        for (MeterTypeDataDTO meterType : request.getMeterTypes()) {
            String meterTypeCode = trimOrNull(meterType.getMtrType());
            Integer mtrOrder = meterType.getMtrOrder() != null ? meterType.getMtrOrder() : (request.getMtrOrder() != null ? request.getMtrOrder().intValue() : null);

            Integer oldPreviousReading = firstNonNull(
                latestBilledReadings.get(meterTypeCode),
                meterType.getOldPrvRdn(),
                meterType.getPrvRdn(),
                0
            );

            Integer oldPresentReading = firstNonNull(
                meterType.getOldPrsntRdn(),
                meterType.getPrvRdn(),
                oldPreviousReading,
                0
            );

            BigDecimal oldUnits = firstNonNull(
                meterType.getOldUnts(),
                calculateUnits(oldPresentReading, oldPreviousReading),
                BigDecimal.ZERO
            );

            BigDecimal oldRate = firstNonNull(
                meterType.getOldRate(),
                meterType.getRate(),
                BigDecimal.ZERO
            );

            BigDecimal oldAmount = firstNonNull(
                meterType.getOldAmt(),
                BigDecimal.ZERO
            );

            Integer newPresentReading = firstNonNull(
                meterType.getNewPrsntRdn(),
                meterType.getPrsntRdn(),
                0
            );

            Integer newPreviousReading = firstNonNull(
                meterType.getNewPrvRdn(),
                oldPresentReading,
                meterType.getPrvRdn(),
                0
            );

            BigDecimal newUnits = firstNonNull(
                meterType.getNewUnts(),
                meterType.getUnts(),
                calculateUnits(newPresentReading, newPreviousReading),
                BigDecimal.ZERO
            );

            BigDecimal newRate = firstNonNull(
                meterType.getNewRate(),
                meterType.getRate(),
                BigDecimal.ZERO
            );

            BigDecimal newAmount = firstNonNull(
                meterType.getNewAmt(),
                meterType.getAmt(),
                BigDecimal.ZERO
            );

            // Ensure there is only one pending 'A' for this account + sequence + type.
            entityManager.createNativeQuery(
                    "UPDATE mtr_amnds SET amd_type = 'R' " +
                        "WHERE update_status = 0 " +
                        "AND TRIM(acc_nbr) = TRIM(:accNbr) " +
                        "AND mtr_seq = :mtrSeq " +
                        "AND TRIM(mtr_type) = TRIM(:mtrType) " +
                        "AND TRIM(amd_type) = 'A'"
                )
                .setParameter("accNbr", trimOrNull(request.getAccNbr()))
                .setParameter("mtrSeq", request.getMtrSeq())
                .setParameter("mtrType", meterTypeCode)
                .executeUpdate();

            // R row uses the OLD meter's ratio (captured at sequence selection, before user edits for new meter)
            String oldMtrRatio = trimOrDefault(request.getOldMtrRatio(), trimOrNull(request.getMtrRatio()));

            // Create 'R' (removed) entry for old meter
            entityManager.createNativeQuery(sql)
                    .setParameter("accNbr", trimOrNull(request.getAccNbr()))
                    .setParameter("areaCd", trimOrNull(request.getAreaCd()))
                    .setParameter("addedBlcy", trimOrNull(request.getAddedBlcy()))
                    .setParameter("effctBlcy", trimOrNull(request.getEffctBlcy()))
                    .setParameter("effctDate", request.getEffctDate())
                    .setParameter("typeChg", typeChg)
                    .setParameter("amdType", "R")
                    .setParameter("mtrType", meterTypeCode)
                    .setParameter("mtrSeq", request.getMtrSeq())
                    .setParameter("mtrsetType", request.getMtrsetType())
                    .setParameter("mtrOrder", mtrOrder)
                    .setParameter("mtrNbr", oldMtrNbr)
                    .setParameter("prsntRdn", oldPresentReading)
                    .setParameter("prvRdn", oldPreviousReading)
                    .setParameter("unts", oldUnits)
                    .setParameter("rate", oldRate)
                    .setParameter("amt", oldAmount)
                    .setParameter("mtrRatio", oldMtrRatio)
                    .setParameter("ctRatio", trimOrNull(request.getCtRatio()))
                    .setParameter("mFactor", request.getMFactor() != null ? request.getMFactor() : BigDecimal.ONE)
                    .setParameter("brCode", trimOrNull(request.getBrCode()))
                    .setParameter("rsnCode", trimOrNull(request.getRsnCode()))
                    .setParameter("updateStatus", 0)
                    .setParameter("userId", trimOrDefault(request.getUserId(), "SYSTEM"))
                    .setParameter("enteredDtime", enteredDtime)
                    .executeUpdate();

            // Create 'A' (added) entry for new meter.
            // prsnt_rdn = 0 (no billing done on new meter yet).
            // prv_rdn   = initial reading shown on new meter's dial at installation.
            // unts      = 0 (consumption is recorded once next billing cycle runs).
            entityManager.createNativeQuery(sql)
                    .setParameter("accNbr", trimOrNull(request.getAccNbr()))
                    .setParameter("areaCd", trimOrNull(request.getAreaCd()))
                    .setParameter("addedBlcy", trimOrNull(request.getAddedBlcy()))
                    .setParameter("effctBlcy", trimOrNull(request.getEffctBlcy()))
                    .setParameter("effctDate", request.getEffctDate())
                    .setParameter("typeChg", typeChg)
                    .setParameter("amdType", "A")
                    .setParameter("mtrType", meterTypeCode)
                    .setParameter("mtrSeq", request.getMtrSeq())
                    .setParameter("mtrsetType", request.getMtrsetType())
                    .setParameter("mtrOrder", mtrOrder)
                    .setParameter("mtrNbr", newMtrNbr)
                    .setParameter("prsntRdn", 0)               // no consumption yet
                    .setParameter("prvRdn", newPresentReading)  // initial reading on new meter
                    .setParameter("unts", BigDecimal.ZERO)      // billing happens in next rdngs cycle
                    .setParameter("rate", newRate)
                    .setParameter("amt", newAmount)
                    .setParameter("mtrRatio", trimOrNull(request.getMtrRatio()))
                    .setParameter("ctRatio", trimOrNull(request.getCtRatio()))
                    .setParameter("mFactor", request.getMFactor() != null ? request.getMFactor() : BigDecimal.ONE)
                    .setParameter("brCode", trimOrNull(request.getBrCode()))
                    .setParameter("rsnCode", trimOrNull(request.getRsnCode()))
                    .setParameter("updateStatus", 0)
                    .setParameter("userId", trimOrDefault(request.getUserId(), "SYSTEM"))
                    .setParameter("enteredDtime", enteredDtime)
                    .executeUpdate();
        }
    }

    private void validateRequest(MeterAmendmentRequestDTO request) {
        if (isBlank(request.getAccNbr())) {
            throw new IllegalArgumentException("acc_nbr is required");
        }
        if (isBlank(request.getAreaCd())) {
            throw new IllegalArgumentException("area_cd is required");
        }
        if (isBlank(request.getEffctBlcy())) {
            throw new IllegalArgumentException("effct_blcy is required");
        }
        if (request.getEffctDate() == null) {
            throw new IllegalArgumentException("effct_date is required");
        }
        if (request.getMtrSeq() == null) {
            throw new IllegalArgumentException("mtr_seq is required");
        }
    }

    private MtrReasonDTO convertReasonToDTO(MtrReason reason) {
        return new MtrReasonDTO(
                trimOrNull(reason.getRsnCode()),
                trimOrNull(reason.getRsnDesc()),
                trimOrNull(reason.getRsnStatus())
        );
    }

    private BrandDTO convertBrandToDTO(Brand brand) {
        return new BrandDTO(
                trimOrNull(brand.getBrCode()),
                trimOrNull(brand.getBrDesc()),
                trimOrNull(brand.getBrStatus())
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String trimOrDefault(String value, String fallback) {
        String trimmed = trimOrNull(value);
        return trimmed != null ? trimmed : fallback;
    }

    @SafeVarargs
    private final <T> T firstNonNull(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private BigDecimal calculateUnits(Integer present, Integer previous) {
        int prsnt = present != null ? present : 0;
        int prv = previous != null ? previous : 0;
        int units = prsnt - prv;
        return BigDecimal.valueOf(Math.max(units, 0));
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(value.toString().replace(",", "").trim());
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        try {
            return new BigDecimal(value.toString().replace(",", "").trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String toTrimmedStringOrNull(Object value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.toString().trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Timestamp toTimestamp(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Timestamp ts) {
            return ts;
        }
        if (value instanceof Date date) {
            return new Timestamp(date.getTime());
        }
        if (value instanceof java.util.Date date) {
            return new Timestamp(date.getTime());
        }
        return null;
    }

    /**
     * Get all pending meter amendments (update_status = 0)
     */
    public long getPendingMeterAmendmentsCount() {
        String countSql = "SELECT COUNT(*) FROM mtr_amnds WHERE update_status = 0";
        try {
            return ((Number) entityManager.createNativeQuery(countSql).getSingleResult()).longValue();
        } catch (Exception e) {
            return 0L;
        }
    }

    @SuppressWarnings("unchecked")
    public List<MeterAmendmentDTO> getPendingMeterAmendments() {
        return getPendingMeterAmendments(null, null);
    }

    @SuppressWarnings("unchecked")
    public List<MeterAmendmentDTO> getPendingMeterAmendments(Integer limit, Integer offset) {
        String sql = "SELECT " +
                "ma.acc_nbr, ma.area_cd, ma.added_blcy, ma.effct_blcy, ma.effct_date, " +
                "ma.type_chg, ma.amd_type, ma.mtr_type, ma.mtr_seq, ma.mtrset_type, ma.mtr_order, " +
                "ma.mtr_nbr, ma.prsnt_rdn, ma.prv_rdn, ma.unts, ma.rate, ma.amt, ma.mtr_ratio, " +
                "ma.ct_ratio, ma.m_factor, ma.br_code, ma.rsn_code, ma.update_status, ma.user_id, " +
                "ma.entered_dtime, COALESCE(mr.rsn_desc, '') as rsn_desc, dev.folio_no " +
                "FROM mtr_amnds ma " +
                "LEFT JOIN mtr_reason mr ON ma.rsn_code = TRIM(mr.rsn_code) " +
                "LEFT JOIN dbadmin.ncre_developers dev ON TRIM(ma.acc_nbr) = TRIM(dev.acc_nbr) " +
                "WHERE ma.update_status = 0 " +
                "ORDER BY ma.entered_dtime DESC";

        try {
            jakarta.persistence.Query query = entityManager.createNativeQuery(sql);
            
            // Apply pagination if specified
            if (limit != null && limit > 0) {
                query.setMaxResults(limit);
            }
            if (offset != null && offset >= 0) {
                query.setFirstResult(offset);
            }
            
            List<Object[]> results = query.getResultList();

            return results.stream().map(row -> {
                MeterAmendmentDTO dto = new MeterAmendmentDTO();
                dto.setAccNbr(toTrimmedStringOrNull(row[0]));
                dto.setAreaCd(toTrimmedStringOrNull(row[1]));
                dto.setAddedBlcy(toTrimmedStringOrNull(row[2]));
                dto.setEffctBlcy(toTrimmedStringOrNull(row[3]));
                dto.setEffctDate(toTimestamp(row[4]));
                dto.setTypeChg(toTrimmedStringOrNull(row[5]));
                dto.setAmdType(toTrimmedStringOrNull(row[6]));
                dto.setMtrType(toTrimmedStringOrNull(row[7]));
                dto.setMtrSeq(toInteger(row[8]));
                dto.setMtrsetType(toTrimmedStringOrNull(row[9]));
                dto.setMtrOrder(toInteger(row[10]));
                dto.setMtrNbr(toTrimmedStringOrNull(row[11]));
                dto.setPrsntRdn(toInteger(row[12]));
                dto.setPrvRdn(toInteger(row[13]));
                dto.setUnts(toBigDecimal(row[14]));
                dto.setRate(toBigDecimal(row[15]));
                dto.setAmt(toBigDecimal(row[16]));
                dto.setMtrRatio(toTrimmedStringOrNull(row[17]));
                dto.setCtRatio(toTrimmedStringOrNull(row[18]));
                dto.setmFactor(toBigDecimal(row[19]));
                dto.setBrCode(toTrimmedStringOrNull(row[20]));
                dto.setRsnCode(toTrimmedStringOrNull(row[21]));
                dto.setUpdateStatus(toInteger(row[22]));
                dto.setUserId(toTrimmedStringOrNull(row[23]));
                dto.setEnteredDtime(toTimestamp(row[24]));
                dto.setRsnDesc(toTrimmedStringOrNull(row[25]));
                if (row.length > 26 && row[26] != null) {
                    if (row[26] instanceof Number) {
                        dto.setFolioNo(((Number) row[26]).shortValue());
                    } else {
                        try {
                            dto.setFolioNo(Short.valueOf(row[26].toString().trim()));
                        } catch (Exception ignored) {}
                    }
                }
                return dto;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error fetching pending meter amendments: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch pending meter amendments", e);
        }
    }

    /**
     * Approve a meter amendment - applies changes to mtr_detail
     */
    @Transactional
    public void approveMeterAmendment(String accNbr, String amdType, String mtrNbr, String effctBlcy, Timestamp enteredDtime, String userId) {
        // First, get the amendment details using explicit column selection
        String selectSql = "SELECT " +
                "acc_nbr, area_cd, added_blcy, effct_blcy, effct_date, " +
                "type_chg, amd_type, mtr_type, mtr_seq, mtrset_type, mtr_order, " +
                "mtr_nbr, prsnt_rdn, prv_rdn, unts, rate, amt, mtr_ratio, " +
                "ct_ratio, m_factor, br_code, rsn_code, update_status, user_id, entered_dtime " +
                "FROM mtr_amnds WHERE update_status = 0 " +
                "AND TRIM(acc_nbr) = TRIM(:accNbr) " +
                "AND TRIM(amd_type) = TRIM(:amdType) " +
                "AND TRIM(mtr_nbr) = TRIM(:mtrNbr) " +
                "AND TRIM(effct_blcy) = TRIM(:effctBlcy) " +
                "AND entered_dtime = :enteredDtime";

        List<Object[]> results = entityManager.createNativeQuery(selectSql)
                .setParameter("accNbr", accNbr)
                .setParameter("amdType", amdType)
                .setParameter("mtrNbr", mtrNbr)
                .setParameter("effctBlcy", effctBlcy)
                .setParameter("enteredDtime", enteredDtime)
                .getResultList();

        if (results.isEmpty()) {
            throw new IllegalArgumentException("Amendment not found or already processed");
        }

        Object[] amendment = results.get(0);
        String amendmentType = toTrimmedStringOrNull(amendment[6]);
        String amendmentMeterNbr = toTrimmedStringOrNull(amendment[11]);

        // Step 1: Update amendment status to approved (1)
        String updateAmdSql = "UPDATE mtr_amnds SET update_status = 1, user_id = :userId " +
                "WHERE update_status = 0 " +
                "AND TRIM(acc_nbr) = TRIM(:accNbr) " +
                "AND TRIM(amd_type) = TRIM(:amdType) " +
                "AND TRIM(mtr_nbr) = TRIM(:mtrNbr) " +
                "AND TRIM(effct_blcy) = TRIM(:effctBlcy) " +
                "AND entered_dtime = :enteredDtime";
        entityManager.createNativeQuery(updateAmdSql)
                .setParameter("userId", trimOrNull(userId))
                .setParameter("accNbr", accNbr)
                .setParameter("amdType", amdType)
                .setParameter("mtrNbr", mtrNbr)
                .setParameter("effctBlcy", effctBlcy)
                .setParameter("enteredDtime", enteredDtime)
                .executeUpdate();

        // Step 2: Apply changes to mtr_detail based on amendment type
        if ("R".equals(amendmentType)) {
            // Remove: Delete the meter from mtr_detail
            System.out.println("Removing meter: " + amendmentMeterNbr);
            String deleteSql = "DELETE FROM mtr_detail WHERE TRIM(mtr_nbr) = TRIM(:mtrNbr)";
            int deleted = entityManager.createNativeQuery(deleteSql)
                    .setParameter("mtrNbr", amendmentMeterNbr)
                    .executeUpdate();
            System.out.println("Deleted " + deleted + " meter record(s)");
        } else if ("A".equals(amendmentType)) {
            // Add: Insert new meter into mtr_detail
            System.out.println("Adding meter: " + amendmentMeterNbr);
            insertMeterDetail(amendment);
        }
    }

    /**
     * Reject a meter amendment - sets status to -1
     */
    @Transactional
    public void rejectMeterAmendment(String accNbr, String amdType, String mtrNbr, String effctBlcy, Timestamp enteredDtime, String userId) {
        String sql = "UPDATE mtr_amnds SET update_status = -1, user_id = :userId " +
                "WHERE update_status = 0 " +
                "AND TRIM(acc_nbr) = TRIM(:accNbr) " +
                "AND TRIM(amd_type) = TRIM(:amdType) " +
                "AND TRIM(mtr_nbr) = TRIM(:mtrNbr) " +
                "AND TRIM(effct_blcy) = TRIM(:effctBlcy) " +
                "AND entered_dtime = :enteredDtime";
        int updated = entityManager.createNativeQuery(sql)
                .setParameter("userId", trimOrNull(userId))
                .setParameter("accNbr", accNbr)
                .setParameter("amdType", amdType)
                .setParameter("mtrNbr", mtrNbr)
                .setParameter("effctBlcy", effctBlcy)
                .setParameter("enteredDtime", enteredDtime)
                .executeUpdate();

        if (updated == 0) {
            throw new IllegalArgumentException("Amendment not found or already processed");
        }
    }

    /**
     * Helper method to insert meter detail from amendment
     */
    private void insertMeterDetail(Object[] amendment) {
        String insertSql = "INSERT INTO mtr_detail (" +
                "inst_id, added_blcy, mtr_type, mtr_seq, mtrset_type, mtr_order, " +
                "mtr_nbr, prsnt_rdn, mtr_ratio, ct_ratio, m_factor, br_code" +
                ") VALUES (" +
                ":instId, :addedBlcy, :mtrType, :mtrSeq, :mtrsetType, :mtrOrder, " +
                ":mtrNbr, :prsntRdn, :mtrRatio, :ctRatio, :mFactor, :brCode" +
                ")";

        // Get inst_id from customer table using acc_nbr
        String accNbr = toTrimmedStringOrNull(amendment[0]);
        Optional<BulkCustomer> customerOpt = bulkCustomerRepository.findByAccNbrTrimmed(accNbr);
        if (customerOpt.isEmpty()) {
            throw new IllegalArgumentException("Customer not found for account number: " + accNbr);
        }
        String instId = customerOpt.get().getInstId();

        // Map amendment columns to mtr_detail
        String mtrType = toTrimmedStringOrNull(amendment[7]);
        Integer mtrSeq = toInteger(amendment[8]);
        Integer mtrOrder = toInteger(amendment[10]);
        String mtrNbr = toTrimmedStringOrNull(amendment[11]);
        Integer prsntRdn = toInteger(amendment[12]);
        String mtrRatio = toTrimmedStringOrNull(amendment[17]);
        String ctRatio = toTrimmedStringOrNull(amendment[18]);
        BigDecimal mFactor = toBigDecimal(amendment[19]);
        String brCode = toTrimmedStringOrNull(amendment[20]);

        // Keep only one active meter row per installation + sequence + meter type.
        entityManager.createNativeQuery("DELETE FROM mtr_detail WHERE inst_id = :instId AND mtr_seq = :mtrSeq AND TRIM(mtr_type) = TRIM(:mtrType)")
            .setParameter("instId", instId)
            .setParameter("mtrSeq", mtrSeq)
            .setParameter("mtrType", mtrType)
            .executeUpdate();

        entityManager.createNativeQuery(insertSql)
                .setParameter("instId", instId)
                .setParameter("addedBlcy", toTrimmedStringOrNull(amendment[3]))
                .setParameter("mtrType", mtrType)
                .setParameter("mtrSeq", mtrSeq)
                .setParameter("mtrsetType", amendment[9])
                .setParameter("mtrOrder", mtrOrder)
                .setParameter("mtrNbr", mtrNbr)
                .setParameter("prsntRdn", prsntRdn != null ? prsntRdn : 0)
                .setParameter("mtrRatio", mtrRatio)
                .setParameter("ctRatio", ctRatio)
                .setParameter("mFactor", mFactor != null ? mFactor : BigDecimal.ONE)
                .setParameter("brCode", brCode)
                .executeUpdate();
        
        System.out.println("Inserted meter " + mtrNbr + " for customer " + accNbr);
    }
}
