package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.DifferenceStatisticsDTO.AccountDifferenceDTO;
import com.example.SPSProjectBackend.dto.DifferenceStatisticsDTO.DifferenceResponse;
import com.example.SPSProjectBackend.dto.HsbAreaDTO;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import com.example.SPSProjectBackend.model.NcreDeveloper;
import com.example.SPSProjectBackend.model.NcreInvRdngs;
import com.example.SPSProjectBackend.repository.BillCycleConfigRepository;
import com.example.SPSProjectBackend.repository.DifferenceStatisticsRepository;
import com.example.SPSProjectBackend.repository.NcreDeveloperRepository;
import com.example.SPSProjectBackend.repository.NcreInvRdngsRepository;
import com.example.SPSProjectBackend.util.SessionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DifferenceStatisticsServiceTest {

    @Mock
    private DifferenceStatisticsRepository differenceStatisticsRepository;

    @Mock
    private NcreInvRdngsRepository ncreInvRdngsRepository;

    @Mock
    private NcreDeveloperRepository ncreDeveloperRepository;

    @Mock
    private BillCycleConfigRepository billCycleConfigRepository;

    @Mock
    private com.example.SPSProjectBackend.repository.NcreBillCycleRepository ncreBillCycleRepository;

    @Mock
    private SessionUtils sessionUtils;

    @Mock
    private HsbLocationService locationService;

    @Mock
    private UserAreaPermissionService areaPermissionService;

    @InjectMocks
    private DifferenceStatisticsService differenceStatisticsService;

    private SecInfoLoginDTO.UserInfo adminUser;
    private SecInfoLoginDTO.UserInfo regionUser;
    private SecInfoLoginDTO.UserInfo provinceUser;
    private SecInfoLoginDTO.UserInfo areaUser;

    @BeforeEach
    void setUp() {
        // A NULL code widens the user's scope, so these four differ only by how far
        // down the hierarchy their codes are set.
        adminUser = new SecInfoLoginDTO.UserInfo();
        adminUser.setUserCategory("Admin");
        adminUser.setRegionCode(null);
        adminUser.setProvinceCode(null);
        adminUser.setAreaCode(null);

        regionUser = new SecInfoLoginDTO.UserInfo();
        regionUser.setUserCategory("Region User");
        regionUser.setRegionCode("R1");
        regionUser.setProvinceCode(null);
        regionUser.setAreaCode(null);

        provinceUser = new SecInfoLoginDTO.UserInfo();
        provinceUser.setUserCategory("Province User");
        provinceUser.setRegionCode("R1");
        provinceUser.setProvinceCode("P1");
        provinceUser.setAreaCode(null);

        areaUser = new SecInfoLoginDTO.UserInfo();
        areaUser.setUserCategory("Area User");
        areaUser.setRegionCode("R1");
        areaUser.setProvinceCode("P1");
        areaUser.setAreaCode("A1");

        // A1 lives in region R1 / province P1. B2 lives in region R2 / province P2.
        lenient().when(areaPermissionService.isAreaPermitted(any(), any(), any(), anyString()))
                .thenAnswer(inv -> {
                    String region = inv.getArgument(0);
                    String province = inv.getArgument(1);
                    String area = inv.getArgument(2);
                    String target = inv.getArgument(3);

                    boolean inR1 = "A1".equals(target);
                    boolean inP1 = "A1".equals(target);
                    if (region == null) return true;
                    if (!region.equals(inR1 ? "R1" : "R2")) return false;
                    if (province == null) return true;
                    if (!province.equals(inP1 ? "P1" : "P2")) return false;
                    return area == null || area.equals(target);
                });

        // "A1" is the area the fixtures live in; anything else is outside the scope.
        lenient().when(areaPermissionService.isAnyAreaPermitted(anyString(), anyString(), any()))
                .thenAnswer(inv -> {
                    List<String> requested = inv.getArgument(2);
                    if (requested == null || requested.isEmpty()) return true;
                    return requested.stream().anyMatch("A1"::equals);
                });
    }

    private NcreDeveloper createDeveloper(String accNbr, Short folioNo, String facilityName, Short acceptRu) {
        NcreDeveloper dev = new NcreDeveloper();
        dev.setAccNbr(accNbr);
        dev.setFolioNo(folioNo);
        dev.setFacilityName(facilityName);
        dev.setAcceptRu(acceptRu);
        return dev;
    }

    private NcreInvRdngs createReading(String accNbr, String areaCd, String cycle,
                                      BigDecimal tot, BigDecimal r1, BigDecimal r2, BigDecimal r3) {
        NcreInvRdngs r = new NcreInvRdngs();
        r.setAccNbr(accNbr);
        r.setAreaCd(areaCd);
        r.setAddedBlcy(cycle);
        r.setKwhTot(tot);
        r.setKwhR1(r1);
        r.setKwhR2(r2);
        r.setKwhR3(r3);
        return r;
    }

    @Test
    void testFormula_PositiveDifference() {
        // Current: tot=2000, r1=600, r2=500, r3=800
        // Previous: tot=1000, r1=300, r2=250, r3=400
        // Units: tot=1000, r1=300, r2=250, r3=400 -> diff = 1000 - 950 = 50
        NcreInvRdngs curr = createReading("1234567890", "A1", "820",
                new BigDecimal("2000"), new BigDecimal("600"), new BigDecimal("500"), new BigDecimal("800"));
        NcreInvRdngs prev = createReading("1234567890", "A1", "819",
                new BigDecimal("1000"), new BigDecimal("300"), new BigDecimal("250"), new BigDecimal("400"));
        NcreDeveloper dev = createDeveloper("1234567890", (short) 101, "ABC Hydro Power Plant", (short) 24);

        when(sessionUtils.getUserLocationFromSession("session1", "user1")).thenReturn(Optional.of(adminUser));
        when(ncreDeveloperRepository.findAll()).thenReturn(List.of(dev));
        when(ncreInvRdngsRepository.findAllByAreaCdAndAddedBlcyTrimmed("A1", "820")).thenReturn(List.of(curr));
        when(ncreInvRdngsRepository.findAllByAreaCdAndAddedBlcyTrimmed("A1", "819")).thenReturn(List.of(prev));

        DifferenceResponse response = differenceStatisticsService.getAreaDifferences("session1", "user1", "A1", "820");

        assertTrue(response.getSuccess());
        assertEquals("A1", response.getAreaCode());
        assertEquals("820", response.getActiveBillCycle());
        assertEquals(1, response.getDifferences().size());
        AccountDifferenceDTO dto = response.getDifferences().get(0);
        assertEquals("1234567890", dto.getAccountNumber());
        assertEquals("101", dto.getFolioNo());
        assertEquals("ABC Hydro Power Plant", dto.getFacilityName());
        assertEquals(new BigDecimal("50"), dto.getDifference());
        assertEquals((short) 24, dto.getAcceptRu());
    }

    @Test
    void testFormula_NegativeDifference() {
        // Current: tot=1900, r1=600, r2=600, r3=750
        // Previous: tot=1000, r1=300, r2=300, r3=400
        // Units: tot=900, r1=300, r2=300, r3=350 -> diff = 900 - 950 = -50
        NcreInvRdngs curr = createReading("1234567891", "A1", "820",
                new BigDecimal("1900"), new BigDecimal("600"), new BigDecimal("600"), new BigDecimal("750"));
        NcreInvRdngs prev = createReading("1234567891", "A1", "819",
                new BigDecimal("1000"), new BigDecimal("300"), new BigDecimal("300"), new BigDecimal("400"));
        NcreDeveloper dev = createDeveloper("1234567891", (short) 102, "XYZ Solar Facility", (short) 12);

        when(sessionUtils.getUserLocationFromSession("session1", "user1")).thenReturn(Optional.of(adminUser));
        when(ncreDeveloperRepository.findAll()).thenReturn(List.of(dev));
        when(ncreInvRdngsRepository.findAllByAreaCdAndAddedBlcyTrimmed("A1", "820")).thenReturn(List.of(curr));
        when(ncreInvRdngsRepository.findAllByAreaCdAndAddedBlcyTrimmed("A1", "819")).thenReturn(List.of(prev));

        DifferenceResponse response = differenceStatisticsService.getAreaDifferences("session1", "user1", "A1", "820");

        assertTrue(response.getSuccess());
        assertEquals(1, response.getDifferences().size());
        assertEquals("102", response.getDifferences().get(0).getFolioNo());
        assertEquals(new BigDecimal("-50"), response.getDifferences().get(0).getDifference());
        assertEquals((short) 12, response.getDifferences().get(0).getAcceptRu());
    }

    @Test
    void testFormula_AllAccountsReturned() {
        NcreInvRdngs curr1 = createReading("ACC1", "A1", "820", new BigDecimal("1010"), new BigDecimal("500"), new BigDecimal("500"), BigDecimal.ZERO);
        NcreInvRdngs prev1 = createReading("ACC1", "A1", "819", new BigDecimal("1000"), new BigDecimal("500"), new BigDecimal("500"), BigDecimal.ZERO);
        NcreDeveloper dev1 = createDeveloper("ACC1", (short) 101, "Facility 1", (short) 10);

        NcreInvRdngs curr2 = createReading("ACC2", "A1", "820", new BigDecimal("990"), new BigDecimal("500"), new BigDecimal("500"), BigDecimal.ZERO);
        NcreInvRdngs prev2 = createReading("ACC2", "A1", "819", new BigDecimal("1000"), new BigDecimal("500"), new BigDecimal("500"), BigDecimal.ZERO);
        NcreDeveloper dev2 = createDeveloper("ACC2", (short) 102, "Facility 2", (short) 20);

        when(sessionUtils.getUserLocationFromSession("session1", "user1")).thenReturn(Optional.of(adminUser));
        when(ncreDeveloperRepository.findAll()).thenReturn(List.of(dev1, dev2));
        when(ncreInvRdngsRepository.findAllByAreaCdAndAddedBlcyTrimmed("A1", "820")).thenReturn(List.of(curr1, curr2));
        when(ncreInvRdngsRepository.findAllByAreaCdAndAddedBlcyTrimmed("A1", "819")).thenReturn(List.of(prev1, prev2));

        DifferenceResponse response = differenceStatisticsService.getAreaDifferences("session1", "user1", "A1", "820");

        assertTrue(response.getSuccess());
        assertEquals(2, response.getDifferences().size());
    }

    @Test
    void testAutomaticActiveBillCycleResolution() {
        when(sessionUtils.getUserLocationFromSession("session1", "user1")).thenReturn(Optional.of(adminUser));
        when(ncreBillCycleRepository.findCurrentBillCycleNumber()).thenReturn(Optional.of(825));
        when(ncreDeveloperRepository.findAll()).thenReturn(Collections.emptyList());
        when(ncreInvRdngsRepository.findAllByAreaCdAndAddedBlcyTrimmed("A1", "825")).thenReturn(Collections.emptyList());
        when(ncreInvRdngsRepository.findAllByAreaCdAndAddedBlcyTrimmed("A1", "824")).thenReturn(Collections.emptyList());

        DifferenceResponse response = differenceStatisticsService.getAreaDifferences("session1", "user1", "A1", null);

        assertTrue(response.getSuccess());
        assertEquals("825", response.getActiveBillCycle());
        assertEquals(0, response.getDifferences().size());
    }

    @Test
    void testMissingBillCycle_ErrorResponse() {
        when(sessionUtils.getUserLocationFromSession("session1", "user1")).thenReturn(Optional.of(adminUser));
        when(ncreBillCycleRepository.findCurrentBillCycleNumber()).thenReturn(Optional.empty());

        DifferenceResponse response = differenceStatisticsService.getAreaDifferences("session1", "user1", "A1", null);

        assertFalse(response.getSuccess());
        assertTrue(response.getMessage().contains("No active bill cycle found"));
    }

    @Test
    void testSecurity_AreaUserAccessDenied() {
        when(sessionUtils.getUserLocationFromSession("session1", "areaUser1")).thenReturn(Optional.of(areaUser));

        DifferenceResponse response = differenceStatisticsService.getAreaDifferences("session1", "areaUser1", "A2", "820");

        assertFalse(response.getSuccess());
        assertTrue(response.getMessage().contains("Access denied"));
        verifyNoInteractions(differenceStatisticsRepository);
    }

    @Test
    void testSecurity_CodeHierarchyDefinesScope() {
        // A NULL region grants every area; R1 grants its own areas only.
        assertTrue(differenceStatisticsService.hasAreaAccessBasedOnUserCategory(adminUser, "A1"));
        assertTrue(differenceStatisticsService.hasAreaAccessBasedOnUserCategory(adminUser, "B2"));

        assertTrue(differenceStatisticsService.hasAreaAccessBasedOnUserCategory(regionUser, "A1"));
        assertFalse(differenceStatisticsService.hasAreaAccessBasedOnUserCategory(regionUser, "B2"));

        // A NULL province grants every area in the region; P1 narrows it further.
        assertTrue(differenceStatisticsService.hasAreaAccessBasedOnUserCategory(provinceUser, "A1"));
        assertFalse(differenceStatisticsService.hasAreaAccessBasedOnUserCategory(provinceUser, "B2"));

        // Fully qualified codes grant only that single area.
        assertTrue(differenceStatisticsService.hasAreaAccessBasedOnUserCategory(areaUser, "A1"));
        assertFalse(differenceStatisticsService.hasAreaAccessBasedOnUserCategory(areaUser, "A9"));
    }
}
