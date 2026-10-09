package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.HsbAreaDTO;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import com.example.SPSProjectBackend.model.HsbArea;
import com.example.SPSProjectBackend.repository.HsbAreaRepository;
import com.example.SPSProjectBackend.util.SessionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * The area scope is driven purely by the region_code / province_code / area_code columns in
 * dbadmin.sec_info, where a NULL code widens the scope to the level above.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserAreaPermissionServiceTest {

    @Mock
    private HsbAreaRepository areaRepository;

    @Mock
    private SessionUtils sessionUtils;

    @InjectMocks
    private UserAreaPermissionService permissionService;

    private static HsbArea area(String code, String prov, String region) {
        HsbArea a = new HsbArea();
        a.setAreaCode(code);
        a.setProvCode(prov);
        a.setRegion(region);
        a.setAreaName("Area " + code);
        return a;
    }

    @BeforeEach
    void setUp() {
        // R1 -> province 1 ; R2 -> province 3
        when(areaRepository.findAreasWithProvinceNamesByRegion("R1"))
                .thenReturn(List.<Object[]>of(new Object[] {"01", "1", "Colombo North", "R1", "WP"}));
        when(areaRepository.findAreasWithProvinceNamesByRegionAndProvince("R1", "1"))
                .thenReturn(List.<Object[]>of(new Object[] {"01", "1", "Colombo North", "R1", "WP"}));
        when(areaRepository.findAreasWithProvinceNamesByCodes(List.of("01")))
                .thenReturn(List.<Object[]>of(new Object[] {"01", "1", "Colombo North", "R1", "WP"}));

        lenient().when(areaRepository.findByAreaCodeNormalized(anyString()))
                .thenAnswer(inv -> {
                    String code = inv.getArgument(0);
                    if ("01".equals(code) || "1".equals(code)) {
                        return Optional.of(area("01", "1", "R1"));
                    }
                    if ("55".equals(code) || "03".equals(code)) {
                        return Optional.of(area("55", "3", "R2"));
                    }
                    return Optional.empty();
                });
        lenient().when(areaRepository.findByAreaCodeTrimmed(anyString()))
                .thenAnswer(inv -> {
                    String code = inv.getArgument(0);
                    if ("01".equals(code)) {
                        return Optional.of(area("01", "1", "R1"));
                    }
                    if ("55".equals(code)) {
                        return Optional.of(area("55", "3", "R2"));
                    }
                    return Optional.empty();
                });
        lenient().when(areaRepository.findByRegionCodeTrimmed(anyString())).thenReturn(new ArrayList<>());
        lenient().when(areaRepository.findByRegionAndProvinceTrimmed(anyString(), anyString()))
                .thenReturn(new ArrayList<>());
    }

    @Test
    void nullRegionGrantsAllAreas() {
        assertTrue(permissionService.isAreaPermitted(null, null, null, "01"));
        assertTrue(permissionService.isAreaPermitted(null, null, null, "55"));
        assertEquals("ALL", permissionService.describeScope(null, null, null));
    }

    @Test
    void blankRegionIsTreatedAsNull() {
        // CHAR(2) padding must not look like a configured value.
        assertTrue(permissionService.isAreaPermitted("  ", "", "  ", "55"));
        assertEquals("ALL", permissionService.describeScope("  ", "", "  "));
    }

    @Test
    void regionOnlyGrantsEveryAreaInThatRegion() {
        assertTrue(permissionService.isAreaPermitted("R1", null, null, "01"));
        assertFalse(permissionService.isAreaPermitted("R1", null, null, "55"));
        assertEquals("REGION", permissionService.describeScope("R1", null, null));
    }

    @Test
    void regionAndProvinceGrantsEveryAreaInThatProvince() {
        assertTrue(permissionService.isAreaPermitted("R1", "1", null, "01"));
        assertFalse(permissionService.isAreaPermitted("R1", "1", null, "55"));
        assertEquals("PROVINCE", permissionService.describeScope("R1", "1", null));
    }

    @Test
    void fullyQualifiedCodesGrantOnlyThatArea() {
        assertTrue(permissionService.isAreaPermitted("R1", "1", "01", "01"));
        assertFalse(permissionService.isAreaPermitted("R1", "1", "01", "55"));
        assertEquals("AREA", permissionService.describeScope("R1", "1", "01"));
    }

    @Test
    void areaCodesMatchRegardlessOfLeadingZeros() {
        // sec_info stores CHAR(2) "01" while clients may send "1".
        assertTrue(permissionService.isAreaPermitted("R1", "1", "01", "1"));
        assertTrue(permissionService.isAreaPermitted("R1", "1", "1", "01"));
    }

    @Test
    void nullTargetAreaIsNeverPermitted() {
        assertFalse(permissionService.isAreaPermitted(null, null, null, null));
        assertFalse(permissionService.isAreaPermitted(null, null, null, "  "));
    }

    @Test
    void resolvePermittedAreasUsesHierarchyNotCategory() {
        List<HsbAreaDTO> permitted = permissionService.resolvePermittedAreas("R1", null, null);
        assertEquals(1, permitted.size());
        assertEquals("01", permitted.get(0).getAreaCode());
    }

    @Test
    void nullRegionResolvesEveryArea() {
        when(areaRepository.findAreasWithProvinceNames()).thenReturn(List.<Object[]>of(
                new Object[] {"01", "1", "Colombo North", "R1", "WP"},
                new Object[] {"55", "3", "Gampaha", "R2", "WP"}));
        assertEquals(2, permissionService.resolvePermittedAreas(null, null, null).size());
    }

    @Test
    void sanitizeKeepsOnlyPermittedAreas() {
        SecInfoLoginDTO.UserInfo userInfo = new SecInfoLoginDTO.UserInfo();
        userInfo.setRegionCode("R1");
        userInfo.setProvinceCode("1");
        when(sessionUtils.getUserLocationFromSession("s1", "u1")).thenReturn(Optional.of(userInfo));

        List<String> result = permissionService.sanitizeRequestedAreaCodes(
                "s1", "u1", List.of("01", "55"));

        assertEquals(List.of("01"), result);
    }

    @Test
    void sanitizeTreatsAllTokenAsEveryPermittedArea() {
        SecInfoLoginDTO.UserInfo userInfo = new SecInfoLoginDTO.UserInfo();
        userInfo.setRegionCode("R1");
        userInfo.setProvinceCode("1");
        when(sessionUtils.getUserLocationFromSession("s1", "u1")).thenReturn(Optional.of(userInfo));

        assertEquals(List.of("01"),
                permissionService.sanitizeRequestedAreaCodes("s1", "u1", List.of("ALL")));
        assertEquals(List.of("01"),
                permissionService.sanitizeRequestedAreaCodes("s1", "u1", new ArrayList<>()));
    }

    @Test
    void effectiveAreasDefaultToAllPermittedWhenNothingSelected() {
        SecInfoLoginDTO.UserInfo userInfo = new SecInfoLoginDTO.UserInfo();
        userInfo.setRegionCode("R1");
        userInfo.setProvinceCode("1");
        when(sessionUtils.getUserLocationFromSession("s1", "u1")).thenReturn(Optional.of(userInfo));

        assertEquals(List.of("01"), permissionService.resolveEffectiveAreaCodes("s1", "u1", null));
        assertEquals(List.of("01"), permissionService.resolveEffectiveAreaCodes("s1", "u1", "ALL"));
    }

    @Test
    void effectiveAreasNarrowToTheSelectedArea() {
        SecInfoLoginDTO.UserInfo userInfo = new SecInfoLoginDTO.UserInfo();
        userInfo.setRegionCode("R1");
        userInfo.setProvinceCode("1");
        when(sessionUtils.getUserLocationFromSession("s1", "u1")).thenReturn(Optional.of(userInfo));

        assertEquals(List.of("01"), permissionService.resolveEffectiveAreaCodes("s1", "u1", "01"));
        // A selection outside the permitted set yields no areas rather than leaking data.
        assertTrue(permissionService.resolveEffectiveAreaCodes("s1", "u1", "55").isEmpty());
    }

    @Test
    void isAnyAreaPermittedAllowsAllTokenAndRejectsForeignAreas() {
        SecInfoLoginDTO.UserInfo userInfo = new SecInfoLoginDTO.UserInfo();
        userInfo.setRegionCode("R1");
        userInfo.setProvinceCode("1");
        when(sessionUtils.getUserLocationFromSession("s1", "u1")).thenReturn(Optional.of(userInfo));

        assertTrue(permissionService.isAnyAreaPermitted("s1", "u1", List.of("ALL")));
        assertTrue(permissionService.isAnyAreaPermitted("s1", "u1", List.of("01")));
        assertFalse(permissionService.isAnyAreaPermitted("s1", "u1", List.of("55")));
        // An empty request means "everything permitted", which the caller already scoped.
        assertTrue(permissionService.isAnyAreaPermitted("s1", "u1", new ArrayList<>()));
    }

    @Test
    void invalidSessionDeniesEverything() {
        when(sessionUtils.getUserLocationFromSession("s1", "u1")).thenReturn(Optional.empty());
        assertFalse(permissionService.isAnyAreaPermitted("s1", "u1", List.of("01")));
    }
}
