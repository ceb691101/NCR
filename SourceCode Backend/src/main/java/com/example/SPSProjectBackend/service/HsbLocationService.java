package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.HsbAreaDTO;
import com.example.SPSProjectBackend.dto.HsbProvinceDTO;
import com.example.SPSProjectBackend.model.HsbArea;
import com.example.SPSProjectBackend.model.HsbProvince;
import com.example.SPSProjectBackend.repository.HsbAreaRepository;
import com.example.SPSProjectBackend.repository.HsbProvinceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class HsbLocationService {

    @Autowired
    private HsbProvinceRepository provinceRepository;

    @Autowired
    private HsbAreaRepository areaRepository;

    // Get all regions in ascending order
    public List<String> getAllRegions() {
        try {
//            return areaRepository.findAllRegions();
            List<String> regions = areaRepository.findAllRegions();
            System.out.println(regions);
            return regions;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve regions: " + e.getMessage(), e);
        }
    }

    // Get all provinces ordered by province code
    public List<HsbProvinceDTO> getAllProvinces() {
        try {
            List<HsbProvince> provinces = provinceRepository.findAllOrderByProvCode();
            return provinces.stream()
                    .map(this::convertProvinceToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve provinces: " + e.getMessage(), e);
        }
    }

    // Get provinces by region code
    public List<HsbProvinceDTO> getProvincesByRegion(String regionCode) {
        try {
            List<HsbProvince> provinces = provinceRepository.findByRegionCode(regionCode);
            return provinces.stream()
                    .map(province -> convertProvinceToDTO(province, regionCode))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve provinces for region " + regionCode + ": " + e.getMessage(), e);
        }
    }

    // Get all areas ordered by area code
    public List<HsbAreaDTO> getAllAreas() {
        try {
            List<Object[]> areasWithProvinces = areaRepository.findAreasWithProvinceNames();
            return areasWithProvinces.stream()
                    .map(this::convertToAreaDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve areas: " + e.getMessage(), e);
        }
    }

    // Get areas by region code
    public List<HsbAreaDTO> getAreasByRegion(String regionCode) {
        try {
            List<Object[]> areasWithProvinces = areaRepository.findAreasWithProvinceNamesByRegion(regionCode);
            return areasWithProvinces.stream()
                    .map(this::convertToAreaDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve areas for region " + regionCode + ": " + e.getMessage(), e);
        }
    }

    // Get areas by province code
    public List<HsbAreaDTO> getAreasByProvince(String provCode) {
        try {
            List<Object[]> areasWithProvinces = areaRepository.findAreasWithProvinceNamesByProvince(provCode);
            return areasWithProvinces.stream()
                    .map(this::convertToAreaDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve areas for province " + provCode + ": " + e.getMessage(), e);
        }
    }

    // Get areas by region and province
    public List<HsbAreaDTO> getAreasByRegionAndProvince(String regionCode, String provCode) {
        try {
            // Use trimmed version for lookup
            List<HsbArea> areas = areaRepository.findByRegionAndProvinceTrimmed(regionCode, provCode);
            return areas.stream()
                    .map(this::convertAreaToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve areas for region " + regionCode + " and province " + provCode + ": " + e.getMessage(), e);
        }
    }

    // Get province by code - USING TRIMMED VERSION
    public Optional<HsbProvinceDTO> getProvinceByCode(String provCode) {
        try {
            // Use trimmed version for lookup
            Optional<HsbProvince> province = provinceRepository.findByProvCodeTrimmed(provCode);
            return province.map(this::convertProvinceToDTO);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve province " + provCode + ": " + e.getMessage(), e);
        }
    }

    // Get area by code - USING TRIMMED VERSION
    public Optional<HsbAreaDTO> getAreaByCode(String areaCode) {
        try {
            // Use trimmed version for lookup
            Optional<HsbArea> area = areaRepository.findByAreaCodeTrimmed(areaCode);
            return area.map(this::convertAreaToDTO);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve area " + areaCode + ": " + e.getMessage(), e);
        }
    }

    // Get area by code with province name - USING TRIMMED VERSION
    public Optional<HsbAreaDTO> getAreaByCodeWithProvince(String areaCode) {
        try {
            // Use trimmed version for lookup
            Optional<HsbArea> areaOpt = areaRepository.findByAreaCodeTrimmed(areaCode);
            if (areaOpt.isPresent()) {
                HsbArea area = areaOpt.get();
                HsbAreaDTO areaDTO = convertAreaToDTO(area);
                
                // Get province name
                Optional<HsbProvince> provinceOpt = provinceRepository.findByProvCodeTrimmed(area.getProvCode());
                if (provinceOpt.isPresent()) {
                    areaDTO.setProvinceName(provinceOpt.get().getProvName());
                }
                
                return Optional.of(areaDTO);
            }
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve area with province " + areaCode + ": " + e.getMessage(), e);
        }
    }

    // Get multiple areas by codes - USING TRIMMED VERSIONS
    public List<HsbAreaDTO> getAreasByCodes(List<String> areaCodes) {
        try {
            List<HsbArea> areas = areaRepository.findByAreaCodeIn(areaCodes);
            return areas.stream()
                    .map(this::convertAreaToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve areas by codes: " + e.getMessage(), e);
        }
    }

    // Get areas with province names for a specific set of area codes
    public List<HsbAreaDTO> getAreasWithProvinceNamesByCodes(List<String> areaCodes) {
        try {
            if (areaCodes == null || areaCodes.isEmpty()) {
                return new ArrayList<>();
            }
            return areaRepository.findAreasWithProvinceNamesByCodes(areaCodes).stream()
                    .map(this::convertToAreaDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve areas by codes: " + e.getMessage(), e);
        }
    }

    // Get areas with province names by region and province
    public List<HsbAreaDTO> getAreasWithProvinceNamesByRegionAndProvince(String regionCode, String provCode) {
        try {
            return areaRepository.findAreasWithProvinceNamesByRegionAndProvince(regionCode, provCode).stream()
                    .map(this::convertToAreaDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve areas for region " + regionCode
                    + " and province " + provCode + ": " + e.getMessage(), e);
        }
    }

    // Look up a single area ignoring leading zeros in the CHAR(2) code
    public Optional<HsbAreaDTO> getAreaByCodeNormalized(String areaCode) {
        try {
            if (areaCode == null || areaCode.trim().isEmpty()) {
                return Optional.empty();
            }
            String clean = areaCode.trim();
            return areaRepository.findByAreaCodeNormalized(clean)
                    .or(() -> areaRepository.findByAreaCodeTrimmed(clean))
                    .map(this::convertAreaToDTO);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve area " + areaCode + ": " + e.getMessage(), e);
        }
    }

    // Helper method to convert Province entity to DTO
    private HsbProvinceDTO convertProvinceToDTO(HsbProvince province) {
        return new HsbProvinceDTO(
                province.getProvCode(),
                province.getProvName(),
                null // Region will be set separately when needed
        );
    }

    // Helper method to convert Province entity to DTO with region
    private HsbProvinceDTO convertProvinceToDTO(HsbProvince province, String region) {
        return new HsbProvinceDTO(
                province.getProvCode(),
                province.getProvName(),
                region
        );
    }

    // Helper method to convert Area entity to DTO
    private HsbAreaDTO convertAreaToDTO(HsbArea area) {
        HsbAreaDTO dto = new HsbAreaDTO();
        dto.setAreaCode(area.getAreaCode());
        dto.setProvCode(area.getProvCode());
        dto.setAreaName(area.getAreaName());
        dto.setRegion(area.getRegion());
        // Province name would need to be fetched separately or via join
        return dto;
    }

    // Helper method to convert query result to AreaDTO
    private HsbAreaDTO convertToAreaDTO(Object[] result) {
        // result: [areaCode, provCode, areaName, region, provinceName]
        return new HsbAreaDTO(
                (String) result[0], // area_code
                (String) result[1], // prov_code
                (String) result[2], // area_name
                (String) result[3], // region
                (String) result[4]  // province_name
        );
    }

    // Validation methods - USING TRIMMED VERSIONS
    public boolean isValidRegion(String regionCode) {
        try {
            List<String> regions = getAllRegions();
            return regions.contains(regionCode != null ? regionCode.trim() : null);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isValidProvince(String provCode) {
        try {
            return provinceRepository.existsByProvCodeTrimmed(provCode);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isValidArea(String areaCode) {
        try {
            return areaRepository.existsByAreaCodeTrimmed(areaCode);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isValidRegionProvinceCombo(String regionCode, String provCode) {
        try {
            List<HsbProvinceDTO> provinces = getProvincesByRegion(regionCode);
            return provinces.stream().anyMatch(p -> p.getProvCode().equals(provCode));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isValidRegionProvinceAreaCombo(String regionCode, String provCode, String areaCode) {
        try {
            List<HsbAreaDTO> areas = getAreasByRegionAndProvince(regionCode, provCode);
            return areas.stream().anyMatch(a -> a.getAreaCode().equals(areaCode));
        } catch (Exception e) {
            return false;
        }
    }

    // Get area name by code - USING TRIMMED VERSION
    public String getAreaNameByCode(String areaCode) {
        try {
            Optional<HsbAreaDTO> areaOpt = getAreaByCode(areaCode);
            return areaOpt.map(HsbAreaDTO::getAreaName).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    // Get province name by code - USING TRIMMED VERSION
    public String getProvinceNameByCode(String provCode) {
        try {
            Optional<HsbProvinceDTO> provinceOpt = getProvinceByCode(provCode);
            return provinceOpt.map(HsbProvinceDTO::getProvName).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Resolve the areas a user is allowed to see purely from the sec_info code hierarchy.
     * A NULL/blank code means "all of the level below", so user_cat is not consulted.
     */
    public List<HsbAreaDTO> getAreasForUser(String userCategory, String userRegion, String userProvince, String userArea) {
        try {
            String region = trimToNull(userRegion);
            String province = trimToNull(userProvince);
            String area = trimToNull(userArea);

            if (region == null) {
                return getAllAreas();
            }
            if (province == null) {
                return getAreasByRegion(region);
            }
            if (area == null) {
                return getAreasWithProvinceNamesByRegionAndProvince(region, province);
            }
            Optional<HsbAreaDTO> areaOpt = getAreaByCodeNormalized(area);
            return areaOpt.map(List::of).orElseGet(List::of);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get areas for user: " + e.getMessage(), e);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}