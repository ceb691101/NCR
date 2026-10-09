// Updated Service: com.example.SPSProjectBackend.service.TmpCustomerService.java
// Removed loanCd mapping, renamed loanAmount to loanAmt, added loanType and lnStatus mappings
package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.TmpCustomerDTO;
import com.example.SPSProjectBackend.model.TmpCustomerNew;
import com.example.SPSProjectBackend.repository.TmpCustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TmpCustomerService {

    @Autowired
    private TmpCustomerRepository tmpCustomerRepository;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private TmpCustomerDTO mapToDTO(TmpCustomerNew customer) {
        if (customer == null) {
            return null;
        }
        TmpCustomerDTO dto = new TmpCustomerDTO();
        // Removed dto.setId() since no auto-id anymore
        dto.setJobNbr(customer.getJobNbr());
        dto.setAreaCd(customer.getAreaCd());
        dto.setCusCat(customer.getCusCat());
        dto.setNatSup(customer.getNatSup());
        dto.setName(customer.getName());
        dto.setAddressL1(customer.getAddressL1());
        dto.setAddressL2(customer.getAddressL2());
        dto.setCity(customer.getCity());
        dto.setTelNbr(customer.getTelNbr());
        dto.setIdNbr(customer.getIdNbr());
        dto.setIdType(customer.getIdType());
        dto.setIssuedDt(customer.getIssuedDt() != null ? DATE_FORMAT.format(customer.getIssuedDt()) : null);
        dto.setEstAmnt(customer.getEstAmnt());
        dto.setEspayDt(customer.getEspayDt() != null ? DATE_FORMAT.format(customer.getEspayDt()) : null);
        dto.setEstPivNbr(customer.getEstPivNbr());
        dto.setIndType(customer.getIndType());
        dto.setDepositAmt(customer.getDepositAmt());
        dto.setDepDate(customer.getDepDate() != null ? DATE_FORMAT.format(customer.getDepDate()) : null);
        dto.setDepPivNbr(customer.getDepPivNbr());
        dto.setAddDepAmt(customer.getAddDepAmt());
        dto.setTotSecDep(customer.getTotSecDep());  // New mapping added
        dto.setCntrDmnd(customer.getCntrDmnd());
        dto.setTariff(customer.getTariff());
        dto.setGstApl(customer.getGstApl());
        dto.setAgrmntNo(customer.getAgrmntNo());
        dto.setCnectDate(customer.getCnectDate() != null ? DATE_FORMAT.format(customer.getCnectDate()) : null);
        dto.setNoLoans(customer.getNoLoans());
        dto.setLoanAmt(customer.getLoanAmt());  // Renamed from loanAmount
        dto.setLoanType(customer.getLoanType());  // New mapping
        dto.setLnStatus(customer.getLnStatus());  // New mapping
        dto.setCstSt(customer.getCstSt());
        dto.setCustCd(customer.getCustCd());  // Fixed: use correct getter and setter with 't'
        dto.setRedCode(customer.getRedCode());
        dto.setDlyPack(customer.getDlyPack());
        dto.setWlkOrd(customer.getWlkOrd());
        dto.setNoOfPhases(customer.getNoOfPhases());
        dto.setTaxInv(customer.getTaxInv());
        dto.setTaxNum(customer.getTaxNum());
        dto.setUserId(customer.getUserId());
        return dto;
    }

    public List<TmpCustomerDTO> getAllTmpCustomers() {
        List<TmpCustomerNew> customers = tmpCustomerRepository.findAllWhereAccNbrIsNull();
        return customers.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public TmpCustomerDTO getTmpCustomerByJobNbr(String jobNbr) {
        Optional<TmpCustomerNew> optionalCustomer = tmpCustomerRepository.findById(jobNbr);
        if (optionalCustomer.isEmpty()) {
            throw new RuntimeException("TmpCustomer not found with job_nbr: " + jobNbr);
        }
        return mapToDTO(optionalCustomer.get());
    }

    public void generateAcc(String jobNbr) {
        Optional<TmpCustomerNew> optionalCustomer = tmpCustomerRepository.findById(jobNbr);
        if (optionalCustomer.isEmpty()) {
            throw new RuntimeException("TmpCustomer not found with job_nbr: " + jobNbr);
        }
        TmpCustomerNew customer = optionalCustomer.get();
        customer.setAccNbr("1");
        tmpCustomerRepository.save(customer);
    }
}