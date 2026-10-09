package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.StatusCountDTO;
import com.example.SPSProjectBackend.model.Spstdesthmt;
import com.example.SPSProjectBackend.model.SpstdesthmtId;
import com.example.SPSProjectBackend.service.SpstdesthmtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spstdesthmt")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class SpstdesthmtController {

    private final SpstdesthmtService service;

    @PostMapping
    public ResponseEntity<Spstdesthmt> create(@RequestBody Spstdesthmt estimate) {
        return ResponseEntity.ok(service.create(estimate));
    }

    @GetMapping("/{appNo}/{stdNo}/{deptId}")
    public ResponseEntity<Spstdesthmt> getById(
            @PathVariable String appNo,
            @PathVariable String stdNo,
            @PathVariable String deptId) {

        SpstdesthmtId id = new SpstdesthmtId();
        id.setAppNo(appNo);
        id.setStdNo(stdNo);
        id.setDeptId(deptId);

        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Spstdesthmt>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/appno/{appNo}")
    public ResponseEntity<Spstdesthmt> getByAppNo(@PathVariable String appNo) {
        return service.getByAppNo(appNo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/applicant/{appNo}")
    public ResponseEntity<?> getApplicantByAppNo(@PathVariable String appNo) {
        return service.getApplicantByAppNo(appNo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status-counts")
    public ResponseEntity<List<StatusCountDTO>> getStatusCounts() {
        List<StatusCountDTO> data = service.getStatusCounts();
        return ResponseEntity.ok(data);
    }


    @PutMapping
    public ResponseEntity<Spstdesthmt> update(@RequestBody Spstdesthmt estimate) {
        return ResponseEntity.ok(service.update(estimate));
    }

    @DeleteMapping("/{appNo}/{stdNo}/{deptId}")
    public ResponseEntity<Void> delete(
            @PathVariable String appNo,
            @PathVariable String stdNo,
            @PathVariable String deptId) {

        SpstdesthmtId id = new SpstdesthmtId();
        id.setAppNo(appNo);
        id.setStdNo(stdNo);
        id.setDeptId(deptId);

        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
