package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.HsbAreaDTO;
import com.example.SPSProjectBackend.dto.HsbProvinceDTO;
import com.example.SPSProjectBackend.service.HsbLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/locations")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class HsbLocationController {

    @Autowired
    private HsbLocationService locationService;

    // Get all regions in ascending order (R1, R2, R3, R4)
    @GetMapping("/regions")
    public ResponseEntity<?> getAllRegions() {
        try {
            List<String> regions = locationService.getAllRegions();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Regions retrieved successfully");
            response.put("regions", regions);
            response.put("count", regions.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve regions");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get all provinces with their names
    @GetMapping("/provinces")
    public ResponseEntity<?> getAllProvinces() {
        try {
            List<HsbProvinceDTO> provinces = locationService.getAllProvinces();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Provinces retrieved successfully");
            response.put("provinces", provinces);
            response.put("count", provinces.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve provinces");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get provinces by region code
    @GetMapping("/provinces/region/{regionCode}")
    public ResponseEntity<?> getProvincesByRegion(@PathVariable String regionCode) {
        try {
            List<HsbProvinceDTO> provinces = locationService.getProvincesByRegion(regionCode);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Provinces retrieved successfully for region " + regionCode);
            response.put("region_code", regionCode);
            response.put("provinces", provinces);
            response.put("count", provinces.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve provinces for region " + regionCode);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get all areas with their names and province names
    @GetMapping("/areas")
    public ResponseEntity<?> getAllAreas() {
        try {
            List<HsbAreaDTO> areas = locationService.getAllAreas();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Areas retrieved successfully");
            response.put("areas", areas);
            response.put("count", areas.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve areas");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get areas by region code
    @GetMapping("/areas/region/{regionCode}")
    public ResponseEntity<?> getAreasByRegion(@PathVariable String regionCode) {
        try {
            List<HsbAreaDTO> areas = locationService.getAreasByRegion(regionCode);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Areas retrieved successfully for region " + regionCode);
            response.put("region_code", regionCode);
            response.put("areas", areas);
            response.put("count", areas.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve areas for region " + regionCode);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get areas by province code
    @GetMapping("/areas/province/{provCode}")
    public ResponseEntity<?> getAreasByProvince(@PathVariable String provCode) {
        try {
            List<HsbAreaDTO> areas = locationService.getAreasByProvince(provCode);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Areas retrieved successfully for province " + provCode);
            response.put("province_code", provCode);
            response.put("areas", areas);
            response.put("count", areas.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve areas for province " + provCode);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get areas by region and province
    @GetMapping("/areas/region/{regionCode}/province/{provCode}")
    public ResponseEntity<?> getAreasByRegionAndProvince(@PathVariable String regionCode, @PathVariable String provCode) {
        try {
            List<HsbAreaDTO> areas = locationService.getAreasByRegionAndProvince(regionCode, provCode);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Areas retrieved successfully for region " + regionCode + " and province " + provCode);
            response.put("region_code", regionCode);
            response.put("province_code", provCode);
            response.put("areas", areas);
            response.put("count", areas.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve areas for region " + regionCode + " and province " + provCode);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get province by code
    @GetMapping("/province/{provCode}")
    public ResponseEntity<?> getProvinceByCode(@PathVariable String provCode) {
        try {
            Optional<HsbProvinceDTO> province = locationService.getProvinceByCode(provCode);
            
            if (province.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Province retrieved successfully");
                response.put("province", province.get());
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Province not found");
                error.put("message", "Province with code " + provCode + " not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve province");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get area by code
    @GetMapping("/area/{areaCode}")
    public ResponseEntity<?> getAreaByCode(@PathVariable String areaCode) {
        try {
            Optional<HsbAreaDTO> area = locationService.getAreaByCode(areaCode);
            
            if (area.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Area retrieved successfully");
                response.put("area", area.get());
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Area not found");
                error.put("message", "Area with code " + areaCode + " not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve area");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Validation endpoints
    @GetMapping("/validate/region/{regionCode}")
    public ResponseEntity<?> validateRegion(@PathVariable String regionCode) {
        try {
            boolean isValid = locationService.isValidRegion(regionCode);
            
            Map<String, Object> response = new HashMap<>();
            response.put("region_code", regionCode);
            response.put("is_valid", isValid);
            response.put("message", isValid ? "Region is valid" : "Region is invalid");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to validate region");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/validate/province/{provCode}")
    public ResponseEntity<?> validateProvince(@PathVariable String provCode) {
        try {
            boolean isValid = locationService.isValidProvince(provCode);
            
            Map<String, Object> response = new HashMap<>();
            response.put("province_code", provCode);
            response.put("is_valid", isValid);
            response.put("message", isValid ? "Province is valid" : "Province is invalid");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to validate province");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/validate/area/{areaCode}")
    public ResponseEntity<?> validateArea(@PathVariable String areaCode) {
        try {
            boolean isValid = locationService.isValidArea(areaCode);
            
            Map<String, Object> response = new HashMap<>();
            response.put("area_code", areaCode);
            response.put("is_valid", isValid);
            response.put("message", isValid ? "Area is valid" : "Area is invalid");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to validate area");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Validate region-province combination
    @GetMapping("/validate/region/{regionCode}/province/{provCode}")
    public ResponseEntity<?> validateRegionProvince(@PathVariable String regionCode, @PathVariable String provCode) {
        try {
            boolean isValid = locationService.isValidRegionProvinceCombo(regionCode, provCode);
            
            Map<String, Object> response = new HashMap<>();
            response.put("region_code", regionCode);
            response.put("province_code", provCode);
            response.put("is_valid", isValid);
            response.put("message", isValid ? "Region-Province combination is valid" : "Region-Province combination is invalid");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to validate region-province combination");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Validate region-province-area combination
    @GetMapping("/validate/region/{regionCode}/province/{provCode}/area/{areaCode}")
    public ResponseEntity<?> validateRegionProvinceArea(@PathVariable String regionCode, @PathVariable String provCode, @PathVariable String areaCode) {
        try {
            boolean isValid = locationService.isValidRegionProvinceAreaCombo(regionCode, provCode, areaCode);
            
            Map<String, Object> response = new HashMap<>();
            response.put("region_code", regionCode);
            response.put("province_code", provCode);
            response.put("area_code", areaCode);
            response.put("is_valid", isValid);
            response.put("message", isValid ? "Region-Province-Area combination is valid" : "Region-Province-Area combination is invalid");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to validate region-province-area combination");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}