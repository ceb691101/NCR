package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.model.PivAmount;
import com.example.SPSProjectBackend.model.PivAmountId;
import com.example.SPSProjectBackend.service.PivAmountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/piv-amounts")
public class PivAmountController {

    @Autowired
    private PivAmountService service;

    @GetMapping("/{deptId}/{pivNo}/{accountCode}")
    public Optional<PivAmount> getPivAmount(@PathVariable String deptId,
                                            @PathVariable String pivNo,
                                            @PathVariable String accountCode) {
        PivAmountId id = new PivAmountId();
        id.setDeptId(deptId);
        id.setPivNo(pivNo);
        id.setAccountCode(accountCode);
        return service.getPivAmount(id);
    }

    @PostMapping("/savepiv")
    public PivAmount createPivAmount(@RequestBody PivAmount pivAmount) {
        if (pivAmount.getDeptId() == null || pivAmount.getDeptId().isEmpty()) {
            throw new IllegalArgumentException("DeptId cannot be null or empty");
        }
        if (pivAmount.getPivNo() == null || pivAmount.getPivNo().isEmpty()) {
            throw new IllegalArgumentException("PivNo cannot be null or empty");
        }
        if (pivAmount.getAccountCode() == null || pivAmount.getAccountCode().isEmpty()) {
            throw new IllegalArgumentException("AccountCode cannot be null or empty");
        }
        if (pivAmount.getAmount() == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        return service.savePivAmount(pivAmount);

    }

    @DeleteMapping("/{deptId}/{pivNo}/{accountCode}")
    public void deletePivAmount(@PathVariable String deptId,
                                @PathVariable String pivNo,
                                @PathVariable String accountCode) {
        PivAmountId id = new PivAmountId();
        id.setDeptId(deptId);
        id.setPivNo(pivNo);
        id.setAccountCode(accountCode);
        service.deletePivAmount(id);
    }
}