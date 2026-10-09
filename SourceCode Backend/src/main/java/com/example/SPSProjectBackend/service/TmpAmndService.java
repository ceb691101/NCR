package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.*;
import com.example.SPSProjectBackend.model.AmndType;
import com.example.SPSProjectBackend.model.TmpAmnd;
import com.example.SPSProjectBackend.model.TmpAmndPk;
import com.example.SPSProjectBackend.model.HistAmnd;
import com.example.SPSProjectBackend.model.HistAmndPk;
import com.example.SPSProjectBackend.repository.AmndTypeRepository;
import com.example.SPSProjectBackend.repository.TmpAmndRepository;
import com.example.SPSProjectBackend.repository.HistAmndRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TmpAmndService {
    @Autowired
    private TmpAmndRepository tmpAmndRepository;
    @Autowired
    private com.example.SPSProjectBackend.repository.NcreDeveloperRepository ncreDeveloperRepository;
    // NEW: Autowired dependencies for old value fetching
    @Autowired
    private AmndTypeRepository amndTypeRepository;
    @Autowired
    private BulkCustomerService bulkCustomerService;
    @Autowired
    private MonTotService monTotService;
    @Autowired
    private MtrDetailService mtrDetailService;
    @Autowired
    private InstInfoService instInfoService;
    @Autowired
    private LoanMasterService loanMasterService;
    @Autowired
    private NetmeterService netmeterService;
    @Autowired
    private HistAmndRepository histAmndRepository;
    // NEW: Field maps for each table (using DTO getters)
    private Map<String, Function<BulkCustomerDTO, Object>> customerFieldMap;
    private Map<String, Function<MonTotDTO, Object>> monTotFieldMap;
    private Map<String, Function<MtrDetailDTO, Object>> mtrDetailFieldMap;
    private Map<String, Function<InstInfoDTO, Object>> instInfoFieldMap;
    private Map<String, Function<LoanMasterDTO, Object>> loanMasterFieldMap;
    private Map<String, Function<NetmeterDTO, Object>> netmeterFieldMap;
    private Map<String, BiConsumer<BulkCustomerDTO, Object>> customerSetterMap;
    private Map<String, BiConsumer<MonTotDTO, Object>> monTotSetterMap;
    private Map<String, BiConsumer<MtrDetailDTO, Object>> mtrDetailSetterMap;
    private Map<String, BiConsumer<InstInfoDTO, Object>> instInfoSetterMap;
    private Map<String, BiConsumer<LoanMasterDTO, Object>> loanMasterSetterMap;
    private Map<String, BiConsumer<NetmeterDTO, Object>> netmeterSetterMap;
    @PostConstruct
    private void initFieldMaps() {
        // Customer fields
        customerFieldMap = new HashMap<>();
        customerFieldMap.put("AREA_CD", BulkCustomerDTO::getAreaCd);
        customerFieldMap.put("TARIFF", BulkCustomerDTO::getTariff);
        customerFieldMap.put("AGRMNT_NO", BulkCustomerDTO::getAgrmntNo);
        customerFieldMap.put("CST_ST", BulkCustomerDTO::getCstSt);
        customerFieldMap.put("NO_LOANS", BulkCustomerDTO::getNoLoans);
        customerFieldMap.put("TOT_SEC_DEP", BulkCustomerDTO::getTotSecDep);
        customerFieldMap.put("WLK_ORD", BulkCustomerDTO::getWlkOrd);
        customerFieldMap.put("DEPOSIT_AMT", BulkCustomerDTO::getDepositAmt);
        customerFieldMap.put("EST_AMNT", BulkCustomerDTO::getEstAmnt);
        customerFieldMap.put("ADD_DEP_AMT", BulkCustomerDTO::getAddDepAmt);
        customerFieldMap.put("IND_TYPE", BulkCustomerDTO::getIndType);
        customerFieldMap.put("TEL_NBR", BulkCustomerDTO::getTelNbr);
        customerFieldMap.put("DEP_DATE", BulkCustomerDTO::getDepDate);
        customerFieldMap.put("DEP_PIV_NBR", BulkCustomerDTO::getDepPivNbr);
        customerFieldMap.put("CUS_CAT", BulkCustomerDTO::getCusCat);
        customerFieldMap.put("GST_APL", BulkCustomerDTO::getGstApl);
        customerFieldMap.put("TAX_INV", BulkCustomerDTO::getTaxInv);
        customerFieldMap.put("AUTH_LETTER", BulkCustomerDTO::getAuthLetter);
        customerFieldMap.put("TAX_NUM", BulkCustomerDTO::getTaxNum);
        customerFieldMap.put("CUST_CD", BulkCustomerDTO::getCustCd);
        customerFieldMap.put("CUST_TYPE", BulkCustomerDTO::getCustType);
        customerFieldMap.put("NET_TYPE", BulkCustomerDTO::getNetType);
        customerFieldMap.put("CAT_CODE", BulkCustomerDTO::getCatCode);
        customerFieldMap.put("CNTR_DMND", BulkCustomerDTO::getCntrDmnd);
        // MON_TOT fields
        monTotFieldMap = new HashMap<>();
        monTotFieldMap.put("BF_BAL", MonTotDTO::getBfBal);
        monTotFieldMap.put("TOT_UNTSKWO", MonTotDTO::getTotUntsKwo);
        monTotFieldMap.put("TOT_UNTSKWD", MonTotDTO::getTotUntsKwd);
        monTotFieldMap.put("TOT_UNTSKWP", MonTotDTO::getTotUntsKwp);
        monTotFieldMap.put("TOT_KVA", MonTotDTO::getTotKva);
        monTotFieldMap.put("TOT_KWOCHG", MonTotDTO::getTotKwoChg);
        monTotFieldMap.put("TOT_KWDCHG", MonTotDTO::getTotKwdChg);
        monTotFieldMap.put("TOT_KWPCHG", MonTotDTO::getTotKwpChg);
        monTotFieldMap.put("TOT_KVACHG", MonTotDTO::getTotKvaChg);
        monTotFieldMap.put("TOT_CHARGE", MonTotDTO::getTotCharge);
        monTotFieldMap.put("FIXED_CHG", MonTotDTO::getFixedChg);
        monTotFieldMap.put("TOT_GST", MonTotDTO::getTotGst);
        monTotFieldMap.put("TOT_AMT", MonTotDTO::getTotAmt);
        monTotFieldMap.put("DEBT_TOT", MonTotDTO::getDebtTot);
        monTotFieldMap.put("CRDT_TOT", MonTotDTO::getCrdtTot);
        monTotFieldMap.put("PAY_TOT", MonTotDTO::getPayTot);
        monTotFieldMap.put("CRNT_BAL", MonTotDTO::getCrntBal);
        // MTR_DETAIL fields
        mtrDetailFieldMap = new HashMap<>();
        mtrDetailFieldMap.put("MTR_NBR", MtrDetailDTO::getMtrNbr);
        mtrDetailFieldMap.put("MTR_TYPE", MtrDetailDTO::getMtrType);
        mtrDetailFieldMap.put("NO_OF_PHASES", MtrDetailDTO::getNoOfPhases);
        mtrDetailFieldMap.put("PRSNT_RDN", MtrDetailDTO::getPrsntRdn);
        mtrDetailFieldMap.put("CT_RATIO", MtrDetailDTO::getCtRatio);
        mtrDetailFieldMap.put("MTR_RATIO", MtrDetailDTO::getMtrRatio);
        mtrDetailFieldMap.put("M_FACTOR", MtrDetailDTO::getMFactor);
        mtrDetailFieldMap.put("AVG_CNSP_3", MtrDetailDTO::getAvgCnsp3);
        mtrDetailFieldMap.put("AVG_CNSP_6", MtrDetailDTO::getAvgCnsp6);
        mtrDetailFieldMap.put("AVG_CNSP_12", MtrDetailDTO::getAvgCnsp12);
        // INST_INFO fields
        instInfoFieldMap = new HashMap<>();
        instInfoFieldMap.put("TR_CB", InstInfoDTO::getTrCb);
        instInfoFieldMap.put("TYPE_MET", InstInfoDTO::getTypeMet);
        instInfoFieldMap.put("TRPNL_VOLT", InstInfoDTO::getTrpnlVolt);
        instInfoFieldMap.put("TRPNL_AMPS", InstInfoDTO::getTrpnlAmps);
        instInfoFieldMap.put("MTR_SET", InstInfoDTO::getMtrSet);
        instInfoFieldMap.put("NBR_MET", InstInfoDTO::getNbrMet);
        // LOAN_MAS fields (note: table is LOAN_MASTER in code)
        loanMasterFieldMap = new HashMap<>();
        loanMasterFieldMap.put("LN_ST", LoanMasterDTO::getActiveSt);
        loanMasterFieldMap.put("INST_AMT", LoanMasterDTO::getMonPmnt);
        loanMasterFieldMap.put("LN_BAL", LoanMasterDTO::getLoanAmt);
        loanMasterFieldMap.put("LOAN_TYPE", LoanMasterDTO::getLoanType);
        loanMasterFieldMap.put("NO_MONTHS", LoanMasterDTO::getNoMonths);
        loanMasterFieldMap.put("ST_BILL_CYCLE", LoanMasterDTO::getStBillCycle);
        loanMasterFieldMap.put("END_BILL_CYCLE", LoanMasterDTO::getEndBillCycle);
        loanMasterFieldMap.put("INT_RATE", LoanMasterDTO::getIntRate);
        // NETMETER fields
        netmeterFieldMap = new HashMap<>();
        netmeterFieldMap.put("SETOFF", NetmeterDTO::getSetoff);
        netmeterFieldMap.put("SCHM", NetmeterDTO::getSchm);
        netmeterFieldMap.put("GEN_CAP", NetmeterDTO::getGenCap);
        netmeterFieldMap.put("RATE1", NetmeterDTO::getRate1);
        netmeterFieldMap.put("AGRMNT_DATE", NetmeterDTO::getAgrmntDate);
        netmeterFieldMap.put("BF_UNITS", NetmeterDTO::getBfUnits);
        netmeterFieldMap.put("AVG_IMP", NetmeterDTO::getAvgImp);
        netmeterFieldMap.put("AVG_EXP", NetmeterDTO::getAvgExp);
        netmeterFieldMap.put("NET_TYPE", NetmeterDTO::getNetType);
        netmeterFieldMap.put("RATE2", NetmeterDTO::getRate2);
        netmeterFieldMap.put("PERIOD1", NetmeterDTO::getPeriod1);
        netmeterFieldMap.put("PERIOD2", NetmeterDTO::getPeriod2);
        netmeterFieldMap.put("RATE3", NetmeterDTO::getRate3);
        // Customer setters (assuming DTO field types based on common usage; adjust if needed)
        customerSetterMap = new HashMap<>();
        customerSetterMap.put("AREA_CD", (dto, val) -> dto.setAreaCd((String) val));
        customerSetterMap.put("TARIFF", (dto, val) -> dto.setTariff((String) val));
        customerSetterMap.put("AGRMNT_NO", (dto, val) -> dto.setAgrmntNo((String) val));
        customerSetterMap.put("CST_ST", (dto, val) -> dto.setCstSt(Integer.parseInt((String) val)));
        customerSetterMap.put("NO_LOANS", (dto, val) -> dto.setNoLoans(((BigDecimal) val).intValue())); // Assuming Integer in DTO
        customerSetterMap.put("TOT_SEC_DEP", (dto, val) -> dto.setTotSecDep((BigDecimal) val));
        customerSetterMap.put("WLK_ORD", (dto, val) -> dto.setWlkOrd((String) val));
        customerSetterMap.put("DEPOSIT_AMT", (dto, val) -> dto.setDepositAmt((BigDecimal) val));
        customerSetterMap.put("EST_AMNT", (dto, val) -> dto.setEstAmnt((BigDecimal) val));
        customerSetterMap.put("ADD_DEP_AMT", (dto, val) -> dto.setAddDepAmt((BigDecimal) val));
        customerSetterMap.put("IND_TYPE", (dto, val) -> dto.setIndType((String) val));
        customerSetterMap.put("TEL_NBR", (dto, val) -> dto.setTelNbr((String) val));
        customerSetterMap.put("DEP_DATE", (dto, val) -> dto.setDepDate((java.util.Date) val)); // Date
        customerSetterMap.put("DEP_PIV_NBR", (dto, val) -> dto.setDepPivNbr((String) val));
        customerSetterMap.put("CUS_CAT", (dto, val) -> dto.setCusCat((String) val));
        customerSetterMap.put("GST_APL", (dto, val) -> dto.setGstApl((String) val));
        customerSetterMap.put("TAX_INV", (dto, val) -> dto.setTaxInv((String) val));
        customerSetterMap.put("AUTH_LETTER", (dto, val) -> dto.setAuthLetter((String) val));
        customerSetterMap.put("TAX_NUM", (dto, val) -> dto.setTaxNum((String) val));
        customerSetterMap.put("CUST_CD", (dto, val) -> dto.setCustCd((String) val));
        customerSetterMap.put("CUST_TYPE", (dto, val) -> dto.setCustType((String) val));
        customerSetterMap.put("NET_TYPE", (dto, val) -> dto.setNetType((String) val));
        customerSetterMap.put("CAT_CODE", (dto, val) -> dto.setCatCode((String) val));
        customerSetterMap.put("CNTR_DMND", (dto, val) -> dto.setCntrDmnd((BigDecimal) val));
        // MON_TOT setters (assuming BigDecimal for all numeric fields)
        monTotSetterMap = new HashMap<>();
        monTotSetterMap.put("BF_BAL", (dto, val) -> dto.setBfBal((BigDecimal) val));
        monTotSetterMap.put("TOT_UNTSKWO", (dto, val) -> dto.setTotUntsKwo((BigDecimal) val));
        monTotSetterMap.put("TOT_UNTSKWD", (dto, val) -> dto.setTotUntsKwd((BigDecimal) val));
        monTotSetterMap.put("TOT_UNTSKWP", (dto, val) -> dto.setTotUntsKwp((BigDecimal) val));
        monTotSetterMap.put("TOT_KVA", (dto, val) -> dto.setTotKva((BigDecimal) val));
        monTotSetterMap.put("TOT_KWOCHG", (dto, val) -> dto.setTotKwoChg((BigDecimal) val));
        monTotSetterMap.put("TOT_KWDCHG", (dto, val) -> dto.setTotKwdChg((BigDecimal) val));
        monTotSetterMap.put("TOT_KWPCHG", (dto, val) -> dto.setTotKwpChg((BigDecimal) val));
        monTotSetterMap.put("TOT_KVACHG", (dto, val) -> dto.setTotKvaChg((BigDecimal) val));
        monTotSetterMap.put("TOT_CHARGE", (dto, val) -> dto.setTotCharge((BigDecimal) val));
        monTotSetterMap.put("FIXED_CHG", (dto, val) -> dto.setFixedChg((BigDecimal) val));
        monTotSetterMap.put("TOT_GST", (dto, val) -> dto.setTotGst((BigDecimal) val));
        monTotSetterMap.put("TOT_AMT", (dto, val) -> dto.setTotAmt((BigDecimal) val));
        monTotSetterMap.put("DEBT_TOT", (dto, val) -> dto.setDebtTot((BigDecimal) val));
        monTotSetterMap.put("CRDT_TOT", (dto, val) -> dto.setCrdtTot((BigDecimal) val));
        monTotSetterMap.put("PAY_TOT", (dto, val) -> dto.setPayTot((BigDecimal) val));
        monTotSetterMap.put("CRNT_BAL", (dto, val) -> dto.setCrntBal((BigDecimal) val));
        // MTR_DETAIL setters
        mtrDetailSetterMap = new HashMap<>();
        mtrDetailSetterMap.put("MTR_NBR", (dto, val) -> dto.setMtrNbr((String) val));
        mtrDetailSetterMap.put("MTR_TYPE", (dto, val) -> dto.setMtrType((String) val));
        mtrDetailSetterMap.put("NO_OF_PHASES", (dto, val) -> dto.setNoOfPhases((String) val));
        mtrDetailSetterMap.put("PRSNT_RDN", (dto, val) -> dto.setPrsntRdn(Integer.parseInt((String) val)));
        mtrDetailSetterMap.put("CT_RATIO", (dto, val) -> dto.setCtRatio((String) val));
        mtrDetailSetterMap.put("MTR_RATIO", (dto, val) -> dto.setMtrRatio((String) val));
        mtrDetailSetterMap.put("M_FACTOR", (dto, val) -> dto.setMFactor((BigDecimal) val));
        mtrDetailSetterMap.put("AVG_CNSP_3", (dto, val) -> dto.setAvgCnsp3((BigDecimal) val));
        mtrDetailSetterMap.put("AVG_CNSP_6", (dto, val) -> dto.setAvgCnsp6((BigDecimal) val));
        mtrDetailSetterMap.put("AVG_CNSP_12", (dto, val) -> dto.setAvgCnsp12((BigDecimal) val));
        // INST_INFO setters
        instInfoSetterMap = new HashMap<>();
        instInfoSetterMap.put("TR_CB", (dto, val) -> dto.setTrCb((String) val));
        instInfoSetterMap.put("TYPE_MET", (dto, val) -> dto.setTypeMet((String) val));
        instInfoSetterMap.put("TRPNL_VOLT", (dto, val) -> dto.setTrpnlVolt((BigDecimal) val));
        instInfoSetterMap.put("TRPNL_AMPS", (dto, val) -> dto.setTrpnlAmps((BigDecimal) val));
        instInfoSetterMap.put("MTR_SET", (dto, val) -> dto.setMtrSet(Short.parseShort((String) val)));
        instInfoSetterMap.put("NBR_MET", (dto, val) -> dto.setNbrMet(((BigDecimal) val).shortValue()));
        // LOAN_MAS setters
        loanMasterSetterMap = new HashMap<>();
        loanMasterSetterMap.put("LN_ST", (dto, val) -> dto.setActiveSt((String) val));
        loanMasterSetterMap.put("INST_AMT", (dto, val) -> dto.setMonPmnt((BigDecimal) val));
        loanMasterSetterMap.put("LN_BAL", (dto, val) -> dto.setLoanAmt((BigDecimal) val));
        loanMasterSetterMap.put("LOAN_TYPE", (dto, val) -> dto.setLoanType((String) val));
        loanMasterSetterMap.put("NO_MONTHS", (dto, val) -> dto.setNoMonths(((BigDecimal) val).shortValue()));
        loanMasterSetterMap.put("ST_BILL_CYCLE", (dto, val) -> dto.setStBillCycle(((BigDecimal) val).shortValue()));
        loanMasterSetterMap.put("END_BILL_CYCLE", (dto, val) -> dto.setEndBillCycle(((BigDecimal) val).shortValue()));
        loanMasterSetterMap.put("INT_RATE", (dto, val) -> dto.setIntRate((BigDecimal) val));
        // NETMETER setters
        netmeterSetterMap = new HashMap<>();
        netmeterSetterMap.put("SETOFF", (dto, val) -> dto.setSetoff((String) val));
        netmeterSetterMap.put("SCHM", (dto, val) -> dto.setSchm((String) val));
        netmeterSetterMap.put("GEN_CAP", (dto, val) -> dto.setGenCap((BigDecimal) val));
        netmeterSetterMap.put("RATE1", (dto, val) -> dto.setRate1((BigDecimal) val));
        netmeterSetterMap.put("AGRMNT_DATE", (dto, val) -> dto.setAgrmntDate((java.util.Date) val)); // Date
        netmeterSetterMap.put("BF_UNITS", (dto, val) -> dto.setBfUnits(Integer.parseInt((String) val)));
        netmeterSetterMap.put("AVG_IMP", (dto, val) -> dto.setAvgImp(Integer.parseInt((String) val)));
        netmeterSetterMap.put("AVG_EXP", (dto, val) -> dto.setAvgExp(Integer.parseInt((String) val)));
        netmeterSetterMap.put("NET_TYPE", (dto, val) -> dto.setNetType((String) val));
        netmeterSetterMap.put("RATE2", (dto, val) -> dto.setRate2((BigDecimal) val));
        netmeterSetterMap.put("PERIOD1", (dto, val) -> dto.setPeriod1(((BigDecimal) val).shortValue()));
        netmeterSetterMap.put("PERIOD2", (dto, val) -> dto.setPeriod2(((BigDecimal) val).shortValue()));
        netmeterSetterMap.put("RATE3", (dto, val) -> dto.setRate3((BigDecimal) val));
    }
    // NEW: Method to get old value using single logic
    public String getOldValue(String accNbr, String amdType, String billCycle) {
        Optional<AmndType> amndTypeOpt = amndTypeRepository.findById(amdType);
        if (amndTypeOpt.isEmpty()) {
            throw new RuntimeException("Amendment type not found: " + amdType);
        }
        AmndType amndType = amndTypeOpt.get();
        String uptblName = amndType.getUptblName();
        String fieldName = amndType.getFieldName();
        Object value = null;
        switch (uptblName) {
            case "CUSTOMER":
                Optional<BulkCustomerDTO> custOpt = bulkCustomerService.getCustomerByAccNbr(accNbr);
                if (custOpt.isPresent()) {
                    Function<BulkCustomerDTO, Object> custGetter = customerFieldMap.get(fieldName);
                    if (custGetter != null) {
                        value = custGetter.apply(custOpt.get());
                    }
                }
                break;
            case "MON_TOT":
                if (billCycle == null || billCycle.trim().isEmpty()) {
                    throw new RuntimeException("Bill cycle is required for MON_TOT amendments");
                }
                if (fieldName.equals("BF_BAL")) {
                    // Special case for BF_BAL: Fetch crnt_bal from the latest existing MON_TOT record
                    List<MonTotDTO> monList = monTotService.getByAccNbr(accNbr);
                    if (!monList.isEmpty()) {
                        MonTotDTO latest = monList.stream()
                                .max(Comparator.comparing(dto -> Integer.parseInt(dto.getBillCycle())))
                                .orElse(null);
                        if (latest != null) {
                            Function<MonTotDTO, Object> getter = monTotFieldMap.get("CRNT_BAL");
                            if (getter != null) {
                                value = getter.apply(latest);
                            }
                        }
                    }
                } else {
                    Optional<MonTotDTO> monOpt = monTotService.getByAccNbrAndBillCycle(accNbr, billCycle);
                    if (monOpt.isPresent()) {
                        Function<MonTotDTO, Object> monGetter = monTotFieldMap.get(fieldName);
                        if (monGetter != null) {
                            value = monGetter.apply(monOpt.get());
                        }
                    }
                }
                break;
            case "MTR_DETAIL":
                Optional<BulkCustomerDTO> custOptMtr = bulkCustomerService.getCustomerByAccNbr(accNbr);
                if (custOptMtr.isPresent() && custOptMtr.get().getInstId() != null) {
                    String instId = custOptMtr.get().getInstId();
                    Optional<MtrDetailDTO> mtrOpt = mtrDetailService.getByInstId(instId);
                    if (mtrOpt.isPresent()) {
                        Function<MtrDetailDTO, Object> mtrGetter = mtrDetailFieldMap.get(fieldName);
                        if (mtrGetter != null) {
                            value = mtrGetter.apply(mtrOpt.get());
                        }
                    }
                }
                break;
            case "INST_INFO":
                Optional<BulkCustomerDTO> custOptInst = bulkCustomerService.getCustomerByAccNbr(accNbr);
                if (custOptInst.isPresent() && custOptInst.get().getInstId() != null) {
                    String instId = custOptInst.get().getInstId();
                    Optional<InstInfoDTO> instOpt = instInfoService.getByInstId(instId);
                    if (instOpt.isPresent()) {
                        Function<InstInfoDTO, Object> instGetter = instInfoFieldMap.get(fieldName);
                        if (instGetter != null) {
                            value = instGetter.apply(instOpt.get());
                        }
                    }
                }
                break;
            case "LOAN_MAS":
                Optional<LoanMasterDTO> loanOpt = loanMasterService.getLoanByAccNbr(accNbr);
                if (loanOpt.isPresent()) {
                    Function<LoanMasterDTO, Object> loanGetter = loanMasterFieldMap.get(fieldName);
                    if (loanGetter != null) {
                        value = loanGetter.apply(loanOpt.get());
                    }
                }
                break;
            case "NETMETER":
                Optional<NetmeterDTO> netOpt = netmeterService.getNetmeterByAccNbr(accNbr);
                if (netOpt.isPresent()) {
                    Function<NetmeterDTO, Object> netGetter = netmeterFieldMap.get(fieldName);
                    if (netGetter != null) {
                        value = netGetter.apply(netOpt.get());
                    }
                }
                break;
            default:
                throw new RuntimeException("Unsupported table for old value: " + uptblName);
        }
        return value != null ? value.toString() : null;
    }
    public TmpAmndDTO createAmendment(TmpAmndDTO dto) {
        // Validate required for PK
        if (dto.getAccNbr() == null || dto.getAccNbr().trim().isEmpty() ||
                dto.getAmdType() == null || dto.getAmdType().trim().isEmpty() ||
                dto.getEffctBlcy() == null) {
            throw new RuntimeException("accNbr, amdType, and effctBlcy are required");
        }
        TmpAmnd entity = convertToEntity(dto);
        entity.setStatus("1");
        TmpAmnd saved = tmpAmndRepository.save(entity);
        return convertToDTO(saved);
    }
    public TmpAmndDTO updateAmendment(String accNbr, String amdType, Short effctBlcy, TmpAmndDTO dto) {
        TmpAmndPk pk = new TmpAmndPk(accNbr, amdType, effctBlcy);
        Optional<TmpAmnd> optional = tmpAmndRepository.findById(pk);
        if (!optional.isPresent()) {
            throw new RuntimeException("Amendment not found with key: " + accNbr + ", " + amdType + ", " + effctBlcy);
        }
        TmpAmnd entity = optional.get();
        updateAmendmentFields(entity, dto);
        // NEW: Always reset status to 1 (pending) on update
        entity.setStatus("1");
        // Ensure edited timestamp and user are set (if not already via DTO)
        if (entity.getEditedUserId() == null || entity.getEditedUserId().trim().isEmpty()) {
            entity.setEditedUserId("SYSTEM");
        }
        if (entity.getEditedDtime() == null) {
            entity.setEditedDtime(new Timestamp(System.currentTimeMillis()));
        }
        TmpAmnd updated = tmpAmndRepository.save(entity);
        return convertToDTO(updated);
    }
    public List<TmpAmndDTO> getAmendmentsByAccNbr(String accNbr) {
        List<TmpAmnd> entities = tmpAmndRepository.findByIdAccNbr(accNbr);
        return entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    public void deleteAmendment(String accNbr, String amdType, Short effctBlcy) {
        TmpAmndPk pk = new TmpAmndPk(accNbr, amdType, effctBlcy);
        if (!tmpAmndRepository.existsById(pk)) {
            throw new RuntimeException("No amendment to delete for key: " + accNbr + ", " + amdType + ", " + effctBlcy);
        }
        tmpAmndRepository.deleteById(pk);
    }
    // UPDATED: Fetch only amendments with status == 1 (pending), optional filter by areaCd
    public List<TmpAmndDTO> getAllAmendments(String areaCd) {
        List<TmpAmnd> entities;
        if (areaCd != null && !areaCd.isEmpty()) {
            entities = tmpAmndRepository.findByStatusAndAreaCd("1", areaCd);
        } else {
            entities = tmpAmndRepository.findByStatus("1");
        }
        return entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    // UPDATED: Get all rejected amendments (status = 3), optional filter by areaCd
    public List<TmpAmndDTO> getRejectedAmendments(String areaCd) {
        List<TmpAmnd> entities;
        if (areaCd != null && !areaCd.isEmpty()) {
            entities = tmpAmndRepository.findByStatusAndAreaCd("3", areaCd);
        } else {
            entities = tmpAmndRepository.findByStatus("3");
        }
        return entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    // UPDATED: Get all posted amendments (status = 2), optional filter by areaCd
    public List<TmpAmndDTO> getPostedAmendments(String areaCd) {
        List<TmpAmnd> entities;
        if (areaCd != null && !areaCd.isEmpty()) {
            entities = tmpAmndRepository.findByStatusAndAreaCd("2", areaCd);
        } else {
            entities = tmpAmndRepository.findByStatus("2");
        }
        return entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    public void updateStatus(String accNbr, String amdType, Short effctBlcy, String status) {
        TmpAmndPk pk = new TmpAmndPk(accNbr, amdType, effctBlcy);
        Optional<TmpAmnd> optional = tmpAmndRepository.findById(pk);
        if (!optional.isPresent()) {
            throw new RuntimeException("Amendment not found with key: " + accNbr + ", " + amdType + ", " + effctBlcy);
        }
        TmpAmnd entity = optional.get();
        entity.setStatus(status);
        entity.setEditedUserId("SYSTEM");
        entity.setEditedDtime(new Timestamp(System.currentTimeMillis()));
        tmpAmndRepository.save(entity);
    }
    public void finalPost(String accNbr, String amdType, Short effctBlcy) {
        TmpAmndPk pk = new TmpAmndPk(accNbr, amdType, effctBlcy);
        Optional<TmpAmnd> optional = tmpAmndRepository.findById(pk);
        if (!optional.isPresent()) {
            throw new RuntimeException("Amendment not found with key: " + accNbr + ", " + amdType + ", " + effctBlcy);
        }
        TmpAmnd tmp = optional.get();
        if (!"2".equals(tmp.getStatus())) {
            throw new RuntimeException("Amendment must be in posted status (2) to archive to history.");
        }
        // Get AmndType and dt_type
        Optional<AmndType> amndTypeOpt = amndTypeRepository.findById(amdType);
        if (amndTypeOpt.isEmpty()) {
            throw new RuntimeException("Amendment type not found: " + amdType);
        }
        AmndType amndType = amndTypeOpt.get();
        String dtType = amndType.getDtType();
        // Get old value (use effctBlcy as billCycle if needed)
        String billCycle = effctBlcy != null ? effctBlcy.toString() : null;
        String oldValueStr = getOldValue(accNbr, amdType, billCycle);
        // Prepare old and new values based on dt_type
        BigDecimal oldNmrValue = null;
        String oldChrValue = null;
        BigDecimal newNmrValue = null;
        String newChrValue = null;
        if ("N".equals(dtType)) {
            oldNmrValue = oldValueStr != null ? new BigDecimal(oldValueStr) : null;
            newNmrValue = tmp.getNmrValue();
        } else {
            oldChrValue = oldValueStr;
            newChrValue = tmp.getChrValue();
        }
        // Create HistAmnd
        HistAmnd hist = new HistAmnd();
        HistAmndPk histPk = new HistAmndPk(accNbr, amdType, String.format("%03d", effctBlcy));
        hist.setId(histPk);
        hist.setAddedBlcy(tmp.getAddedBlcy() != null ? String.format("%03d", tmp.getAddedBlcy()) : null);
        hist.setEffctDate(tmp.getEffctDate());
        hist.setOldNmrValue(oldNmrValue);
        hist.setNewNmrValue(newNmrValue);
        hist.setOldChrValue(oldChrValue);
        hist.setNewChrValue(newChrValue);
        hist.setAmauthCode(tmp.getAmauthCode());
        hist.setUserId(tmp.getUserId());
        hist.setEnteredDtime(tmp.getEnteredDtime());
        hist.setEditedUserId(tmp.getEditedUserId());
        hist.setEditedDtime(tmp.getEditedDtime());
        // Save to history
        histAmndRepository.save(hist);
        // NEW: Update the target table with new value
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Object newVal;
        if ("N".equals(dtType)) {
            newVal = tmp.getNmrValue();
        } else if ("D".equals(dtType)) {
            try {
                newVal = sdf.parse(tmp.getChrValue());
            } catch (ParseException e) {
                throw new RuntimeException("Invalid date format for new value: " + tmp.getChrValue(), e);
            }
        } else {
            newVal = tmp.getChrValue();
        }
        // Normalize newVal to expected type
        if ("N".equals(dtType)) {
            if (!(newVal instanceof BigDecimal)) {
                newVal = new BigDecimal(newVal.toString());
            }
        } else if ("D".equals(dtType)) {
            if (!(newVal instanceof java.util.Date)) {
                try {
                    newVal = sdf.parse(newVal.toString());
                } catch (ParseException e) {
                    throw new RuntimeException("Invalid date format for new value: " + newVal, e);
                }
            }
        } else {
            newVal = newVal.toString();
        }
        String uptblName = amndType.getUptblName();
        String fieldName = amndType.getFieldName();
        switch (uptblName) {
            case "CUSTOMER":
                Optional<BulkCustomerDTO> custOpt = bulkCustomerService.getCustomerByAccNbr(accNbr);
                if (custOpt.isPresent()) {
                    BulkCustomerDTO custDto = custOpt.get();
                    BiConsumer<BulkCustomerDTO, Object> custSetter = customerSetterMap.get(fieldName);
                    if (custSetter != null) {
                        custSetter.accept(custDto, newVal);
                        bulkCustomerService.updateCustomer(accNbr, custDto);
                    } else {
                        throw new RuntimeException("No setter found for field: " + fieldName + " in CUSTOMER");
                    }
                } else {
                    throw new RuntimeException("Customer not found for accNbr: " + accNbr);
                }
                break;
            case "MON_TOT":
                String billCycleStr = String.format("%03d", effctBlcy);
                Optional<MonTotDTO> monOpt = monTotService.getByAccNbrAndBillCycle(accNbr, billCycleStr);
                if (monOpt.isPresent()) {
                    MonTotDTO monDto = monOpt.get();
                    BiConsumer<MonTotDTO, Object> monSetter = monTotSetterMap.get(fieldName);
                    if (monSetter != null) {
                        monSetter.accept(monDto, newVal);
                        monTotService.updateMonTot(accNbr, billCycleStr, monDto);
                    } else {
                        throw new RuntimeException("No setter found for field: " + fieldName + " in MON_TOT");
                    }
                } else {
                    throw new RuntimeException("MonTot not found for accNbr: " + accNbr + ", billCycle: " + billCycleStr);
                }
                break;
            case "MTR_DETAIL":
                Optional<BulkCustomerDTO> custOptMtr = bulkCustomerService.getCustomerByAccNbr(accNbr);
                if (custOptMtr.isPresent() && custOptMtr.get().getInstId() != null) {
                    String instId = custOptMtr.get().getInstId();
                    Optional<MtrDetailDTO> mtrOpt = mtrDetailService.getByInstId(instId);
                    if (mtrOpt.isPresent()) {
                        MtrDetailDTO mtrDto = mtrOpt.get();
                        BiConsumer<MtrDetailDTO, Object> mtrSetter = mtrDetailSetterMap.get(fieldName);
                        if (mtrSetter != null) {
                            mtrSetter.accept(mtrDto, newVal);
                            mtrDetailService.updateMtrDetail(instId, mtrDto);
                        } else {
                            throw new RuntimeException("No setter found for field: " + fieldName + " in MTR_DETAIL");
                        }
                    } else {
                        throw new RuntimeException("MtrDetail not found for instId: " + instId);
                    }
                } else {
                    throw new RuntimeException("Customer or instId not found for accNbr: " + accNbr);
                }
                break;
            case "INST_INFO":
                Optional<BulkCustomerDTO> custOptInst = bulkCustomerService.getCustomerByAccNbr(accNbr);
                if (custOptInst.isPresent() && custOptInst.get().getInstId() != null) {
                    String instId = custOptInst.get().getInstId();
                    Optional<InstInfoDTO> instOpt = instInfoService.getByInstId(instId);
                    if (instOpt.isPresent()) {
                        InstInfoDTO instDto = instOpt.get();
                        BiConsumer<InstInfoDTO, Object> instSetter = instInfoSetterMap.get(fieldName);
                        if (instSetter != null) {
                            instSetter.accept(instDto, newVal);
                            instInfoService.updateInstInfo(instId, instDto);
                        } else {
                            throw new RuntimeException("No setter found for field: " + fieldName + " in INST_INFO");
                        }
                    } else {
                        throw new RuntimeException("InstInfo not found for instId: " + instId);
                    }
                } else {
                    throw new RuntimeException("Customer or instId not found for accNbr: " + accNbr);
                }
                break;
            case "LOAN_MAS":
                Optional<LoanMasterDTO> loanOpt = loanMasterService.getLoanByAccNbr(accNbr);
                if (loanOpt.isPresent()) {
                    LoanMasterDTO loanDto = loanOpt.get();
                    BiConsumer<LoanMasterDTO, Object> loanSetter = loanMasterSetterMap.get(fieldName);
                    if (loanSetter != null) {
                        loanSetter.accept(loanDto, newVal);
                        loanMasterService.updateLoanMaster(accNbr, loanDto);
                    } else {
                        throw new RuntimeException("No setter found for field: " + fieldName + " in LOAN_MAS");
                    }
                } else {
                    throw new RuntimeException("LoanMaster not found for accNbr: " + accNbr);
                }
                break;
            case "NETMETER":
                Optional<NetmeterDTO> netOpt = netmeterService.getNetmeterByAccNbr(accNbr);
                if (netOpt.isPresent()) {
                    NetmeterDTO netDto = netOpt.get();
                    BiConsumer<NetmeterDTO, Object> netSetter = netmeterSetterMap.get(fieldName);
                    if (netSetter != null) {
                        netSetter.accept(netDto, newVal);
                        netmeterService.updateNetmeter(accNbr, netDto);
                    } else {
                        throw new RuntimeException("No setter found for field: " + fieldName + " in NETMETER");
                    }
                } else {
                    throw new RuntimeException("Netmeter not found for accNbr: " + accNbr);
                }
                break;
            default:
                throw new RuntimeException("Unsupported table for update: " + uptblName);
        }
        // Update tmp status to 4
        tmp.setStatus("4");
        tmp.setEditedUserId("SYSTEM");
        tmp.setEditedDtime(new Timestamp(System.currentTimeMillis()));
        tmpAmndRepository.save(tmp);
    }
    private TmpAmnd convertToEntity(TmpAmndDTO dto) {
        TmpAmnd amendment = new TmpAmnd();
        TmpAmndPk pk = new TmpAmndPk(dto.getAccNbr(), dto.getAmdType(), dto.getEffctBlcy());
        amendment.setId(pk);
        if (dto.getAreaCd() != null && !dto.getAreaCd().trim().isEmpty())
            amendment.setAreaCd(dto.getAreaCd());
        if (dto.getAddedBlcy() != null)
            amendment.setAddedBlcy(dto.getAddedBlcy());
        if (dto.getEffctDate() != null && !dto.getEffctDate().trim().isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            try {
                Date parsedDate = sdf.parse(dto.getEffctDate());
                amendment.setEffctDate(parsedDate);
            } catch (ParseException e) {
                throw new RuntimeException("Invalid effective date format: " + dto.getEffctDate() + ". Expected yyyy-MM-dd", e);
            }
        }
        // Only set one of these two; do not set the other explicitly to null here
        if (dto.getNmrValue() != null) {
            amendment.setNmrValue(dto.getNmrValue());
        } else if (dto.getChrValue() != null && !dto.getChrValue().trim().isEmpty()) {
            amendment.setChrValue(dto.getChrValue());
        }
        if (dto.getAmauthCode() != null && !dto.getAmauthCode().trim().isEmpty())
            amendment.setAmauthCode(dto.getAmauthCode());
        if (dto.getUserId() != null && !dto.getUserId().trim().isEmpty())
            amendment.setUserId(dto.getUserId());
        if (dto.getEditedUserId() != null && !dto.getEditedUserId().trim().isEmpty())
            amendment.setEditedUserId(dto.getEditedUserId());
        // NEW: Status - default to "1" if not provided
        if (dto.getStatus() != null) {
            amendment.setStatus(dto.getStatus());
        } else {
            amendment.setStatus("1"); // Default for create
        }
        return amendment;
    }
    private void updateAmendmentFields(TmpAmnd amendment, TmpAmndDTO dto) {
        if (dto.getAreaCd() != null && !dto.getAreaCd().trim().isEmpty())
            amendment.setAreaCd(dto.getAreaCd());
        if (dto.getAddedBlcy() != null)
            amendment.setAddedBlcy(dto.getAddedBlcy());
        if (dto.getEffctDate() != null && !dto.getEffctDate().trim().isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            try {
                Date parsedDate = sdf.parse(dto.getEffctDate());
                amendment.setEffctDate(parsedDate);
            } catch (ParseException e) {
                throw new RuntimeException("Invalid effective date format in update: " + dto.getEffctDate() + ". Expected yyyy-MM-dd", e);
            }
        }
        // On update, explicitly nullify the other value field to avoid stale mismatch
        if (dto.getNmrValue() != null) {
            amendment.setNmrValue(dto.getNmrValue());
            amendment.setChrValue(null);
        } else if (dto.getChrValue() != null && !dto.getChrValue().trim().isEmpty()) {
            amendment.setChrValue(dto.getChrValue());
            amendment.setNmrValue(null);
        }
        if (dto.getAmauthCode() != null && !dto.getAmauthCode().trim().isEmpty())
            amendment.setAmauthCode(dto.getAmauthCode());
        if (dto.getUserId() != null && !dto.getUserId().trim().isEmpty())
            amendment.setUserId(dto.getUserId());
        if (dto.getEditedUserId() != null && !dto.getEditedUserId().trim().isEmpty())
            amendment.setEditedUserId(dto.getEditedUserId());
        if (dto.getStatus() != null) {
            amendment.setStatus(dto.getStatus());
        }
    }
    private TmpAmndDTO convertToDTO(TmpAmnd entity) {
        TmpAmndDTO dto = new TmpAmndDTO();
        dto.setAccNbr(entity.getId().getAccNbr());
        if (entity.getId().getAccNbr() != null && ncreDeveloperRepository != null) {
            ncreDeveloperRepository.findByAccNbrTrimmed(entity.getId().getAccNbr().trim())
                    .ifPresent(dev -> dto.setFolioNo(dev.getFolioNo()));
        }
        dto.setAmdType(entity.getId().getAmdType());
        dto.setEffctBlcy(entity.getId().getEffctBlcy());
        dto.setAreaCd(entity.getAreaCd());
        dto.setAddedBlcy(entity.getAddedBlcy());
        if (entity.getEffctDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            dto.setEffctDate(sdf.format(entity.getEffctDate()));
        }
        dto.setNmrValue(entity.getNmrValue());
        dto.setChrValue(entity.getChrValue());
        dto.setAmauthCode(entity.getAmauthCode());
        dto.setUserId(entity.getUserId());
        dto.setEditedUserId(entity.getEditedUserId());
        dto.setEnteredDtime(entity.getEnteredDtime());
        dto.setEditedDtime(entity.getEditedDtime());
        dto.setStatus(entity.getStatus());
        return dto;
    }
    private HistAmndDTO convertToHistDTO(HistAmnd entity) {
        HistAmndDTO dto = new HistAmndDTO();
        dto.setAccNbr(entity.getId().getAccNbr());
        dto.setAmdType(entity.getId().getAmdType());
        dto.setAddedBlcy(entity.getAddedBlcy());
        dto.setEffctBlcy(entity.getId().getEffctBlcy());
        if (entity.getEffctDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            dto.setEffctDate(sdf.format(entity.getEffctDate()));
        }
        dto.setOldNmrValue(entity.getOldNmrValue());
        dto.setNewNmrValue(entity.getNewNmrValue());
        dto.setOldChrValue(entity.getOldChrValue());
        dto.setNewChrValue(entity.getNewChrValue());
        dto.setAmauthCode(entity.getAmauthCode());
        dto.setUserId(entity.getUserId());
        dto.setEnteredDtime(entity.getEnteredDtime());
        dto.setEditedUserId(entity.getEditedUserId());
        dto.setEditedDtime(entity.getEditedDtime());
        return dto;
    }
}