package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.HsbAreaDTO;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import com.example.SPSProjectBackend.model.HsbArea;
import com.example.SPSProjectBackend.repository.HsbAreaRepository;
import com.example.SPSProjectBackend.util.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Resolves the set of areas a user is allowed to see, based purely on the
 * region_code / province_code / area_code values stored in dbadmin.sec_info.
 *
 * Permission hierarchy (a NULL / blank code means "all of the level below"):
 *   region_code NULL                          -> every area in every region
 *   region_code set, province_code NULL       -> every area in that region
 *   region + province set, area_code NULL     -> every area in that province
 *   region + province + area set              -> only that single area
 *
 * The user's category (user_cat) is intentionally NOT used to derive area
 * permissions; it only decides EE specific row filtering elsewhere.
 */
@Service
@Transactional(readOnly = true)
public class UserAreaPermissionService {

    /** Marker used when the user has not narrowed the scope to a single area. */
    public static final String ALL_AREAS = "ALL";

    @Autowired
    private HsbAreaRepository areaRepository;

    @Autowired
    @Lazy
    private SessionUtils sessionUtils;

    // ------------------------------------------------------------------
    // Normalisation helpers
    // ------------------------------------------------------------------

    /** Treats null / blank as "not set" so CHAR(2) padding does not look like a value. */
    public static String normalizeCode(String code) {
        if (code == null) {
            return null;
        }
        String trimmed = code.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** Area codes are CHAR(2) ("01") but arrive from clients as "1" - compare on the numeric value. */
    public static String normalizeAreaCode(String areaCode) {
        String code = normalizeCode(areaCode);
        if (code == null) {
            return null;
        }
        return code.replaceFirst("^0+(?!$)", "");
    }

    public static boolean isAllAreasToken(String areaCode) {
        String code = normalizeCode(areaCode);
        return code != null && ALL_AREAS.equalsIgnoreCase(code);
    }

    // ------------------------------------------------------------------
    // Scope description
    // ------------------------------------------------------------------

    /**
     * Human readable description of what the codes in sec_info mean for this user.
     */
    public String describeScope(String regionCode, String provinceCode, String areaCode) {
        String region = normalizeCode(regionCode);
        String province = normalizeCode(provinceCode);
        String area = normalizeCode(areaCode);

        if (region == null) {
            return "ALL";
        }
        if (province == null) {
            return "REGION";
        }
        if (area == null) {
            return "PROVINCE";
        }
        return "AREA";
    }

    // ------------------------------------------------------------------
    // Permitted area resolution
    // ------------------------------------------------------------------

    /**
     * Resolves every area the user is permitted to access from the raw sec_info codes.
     */
    public List<HsbAreaDTO> resolvePermittedAreas(String regionCode, String provinceCode, String areaCode) {
        String region = normalizeCode(regionCode);
        String province = normalizeCode(provinceCode);
        String area = normalizeCode(areaCode);

        try {
            if (region == null) {
                return toAreaDTOs(areaRepository.findAreasWithProvinceNames());
            }

            if (province == null) {
                return toAreaDTOs(areaRepository.findAreasWithProvinceNamesByRegion(region));
            }

            if (area == null) {
                return toAreaDTOs(areaRepository.findAreasWithProvinceNamesByRegionAndProvince(region, province));
            }

            // Fully qualified permission: still resolve through the join so the
            // province name is populated for display purposes.
            List<Object[]> rows = areaRepository.findAreasWithProvinceNamesByCodes(List.of(area));
            if (!rows.isEmpty()) {
                return toAreaDTOs(rows);
            }
            return areaRepository.findByAreaCodeTrimmed(area)
                    .map(found -> List.of(toAreaDTO(found)))
                    .orElseGet(List::of);
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve permitted areas: " + e.getMessage(), e);
        }
    }

    /** Resolves the permitted areas for the currently logged in user. */
    public List<HsbAreaDTO> resolvePermittedAreasForUser(String sessionId, String userId) {
        return sessionUtils.getUserLocationFromSession(sessionId, userId)
                .map(userInfo -> resolvePermittedAreas(userInfo.getRegionCode(),
                        userInfo.getProvinceCode(), userInfo.getAreaCode()))
                .orElseGet(List::of);
    }

    /** Convenience wrapper returning the permitted area codes in stable order. */
    public List<String> resolvePermittedAreaCodes(String sessionId, String userId) {
        Set<String> codes = new LinkedHashSet<>();
        for (HsbAreaDTO area : resolvePermittedAreasForUser(sessionId, userId)) {
            String code = normalizeCode(area.getAreaCode());
            if (code != null) {
                codes.add(code);
            }
        }
        return new ArrayList<>(codes);
    }

    // ------------------------------------------------------------------
    // Access checks
    // ------------------------------------------------------------------

    /**
     * True when the user may read data for the given area, derived from the code hierarchy.
     */
    public boolean isAreaPermitted(String regionCode, String provinceCode, String areaCode,
                                   String targetAreaCode) {
        String region = normalizeCode(regionCode);
        String province = normalizeCode(provinceCode);
        String area = normalizeCode(areaCode);
        String target = normalizeAreaCode(targetAreaCode);

        if (target == null) {
            return false;
        }
        if (region == null) {
            return true;
        }
        if (province == null) {
            return isAreaInRegion(target, region);
        }
        if (area == null) {
            return isAreaInProvince(target, region, province);
        }
        return normalizeAreaCode(area).equals(target);
    }

    /**
     * True when the user may read data for any of the supplied areas.
     * An empty / "ALL" selection means every permitted area.
     */
    public boolean isAnyAreaPermitted(String sessionId, String userId, List<String> requestedAreaCodes) {
        Optional<SecInfoLoginDTO.UserInfo> userInfoOpt = sessionUtils.getUserLocationFromSession(sessionId, userId);
        if (userInfoOpt.isEmpty()) {
            return false;
        }
        SecInfoLoginDTO.UserInfo userInfo = userInfoOpt.get();

        if (requestedAreaCodes == null || requestedAreaCodes.isEmpty()) {
            return true;
        }
        for (String requested : requestedAreaCodes) {
            if (isAllAreasToken(requested)) {
                return true;
            }
            if (isAreaPermitted(userInfo.getRegionCode(), userInfo.getProvinceCode(),
                    userInfo.getAreaCode(), requested)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates a requested selection against the user's permitted areas.
     * Returns the permitted subset (empty selection is expanded to every permitted area).
     */
    public List<String> sanitizeRequestedAreaCodes(String sessionId, String userId, List<String> requestedAreaCodes) {
        List<String> permitted = resolvePermittedAreaCodes(sessionId, userId);
        Set<String> permittedNormalized = new LinkedHashSet<>();
        for (String code : permitted) {
            permittedNormalized.add(normalizeAreaCode(code));
        }

        if (requestedAreaCodes == null || requestedAreaCodes.isEmpty()) {
            return permitted;
        }

        boolean allRequested = requestedAreaCodes.stream().anyMatch(UserAreaPermissionService::isAllAreasToken);
        if (allRequested) {
            return permitted;
        }

        List<String> result = new ArrayList<>();
        for (String requested : requestedAreaCodes) {
            String normalized = normalizeAreaCode(requested);
            if (normalized != null && permittedNormalized.contains(normalized)) {
                String original = permitted.stream()
                        .filter(code -> normalizeAreaCode(code).equals(normalized))
                        .findFirst()
                        .orElse(requested.trim());
                if (!result.contains(original)) {
                    result.add(original);
                }
            }
        }
        return result;
    }

    /**
     * The areas whose data should be loaded for this request: the selected area when the user
     * narrowed the scope, otherwise every permitted area.
     */
    public List<String> resolveEffectiveAreaCodes(String sessionId, String userId, String selectedAreaCode) {
        List<String> permitted = resolvePermittedAreaCodes(sessionId, userId);

        if (isAllAreasToken(selectedAreaCode)) {
            return permitted;
        }
        String normalizedSelection = normalizeAreaCode(selectedAreaCode);
        if (normalizedSelection == null) {
            return permitted;
        }

        return permitted.stream()
                .filter(code -> normalizeAreaCode(code).equals(normalizedSelection))
                .findFirst()
                .map(List::of)
                .orElseGet(Collections::emptyList);
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    private boolean isAreaInRegion(String targetAreaCode, String regionCode) {
        return findArea(targetAreaCode)
                .map(area -> regionCode.equals(normalizeCode(area.getRegion())))
                .orElseGet(() -> areaExistsWithRegion(targetAreaCode, regionCode));
    }

    private boolean isAreaInProvince(String targetAreaCode, String regionCode, String provinceCode) {
        return findArea(targetAreaCode)
                .map(area -> regionCode.equals(normalizeCode(area.getRegion()))
                        && provinceCode.equals(normalizeCode(area.getProvCode())))
                .orElseGet(() -> areaExistsWithRegionAndProvince(targetAreaCode, regionCode, provinceCode));
    }

    private Optional<com.example.SPSProjectBackend.model.HsbArea> findArea(String areaCode) {
        String code = normalizeCode(areaCode);
        if (code == null) {
            return Optional.empty();
        }
        return areaRepository.findByAreaCodeNormalized(code)
                .or(() -> areaRepository.findByAreaCodeTrimmed(code));
    }

    private boolean areaExistsWithRegion(String targetAreaCode, String regionCode) {
        for (HsbArea area : areaRepository.findByRegionCodeTrimmed(regionCode)) {
            if (normalizeAreaCode(area.getAreaCode()).equals(targetAreaCode)) {
                return true;
            }
        }
        return false;
    }

    private boolean areaExistsWithRegionAndProvince(String targetAreaCode, String regionCode, String provinceCode) {
        for (HsbArea area : areaRepository.findByRegionAndProvinceTrimmed(regionCode, provinceCode)) {
            if (normalizeAreaCode(area.getAreaCode()).equals(targetAreaCode)) {
                return true;
            }
        }
        return false;
    }

    private List<HsbAreaDTO> toAreaDTOs(List<Object[]> rows) {
        List<HsbAreaDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new HsbAreaDTO((String) row[0], (String) row[1], (String) row[2],
                    (String) row[3], (String) row[4]));
        }
        return result;
    }

    private HsbAreaDTO toAreaDTO(com.example.SPSProjectBackend.model.HsbArea area) {
        return new HsbAreaDTO(area.getAreaCode(), area.getProvCode(), area.getAreaName(), area.getRegion(), null);
    }
}
