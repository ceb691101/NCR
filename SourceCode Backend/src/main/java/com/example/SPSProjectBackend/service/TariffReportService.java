package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Service
public class TariffReportService {

    @Autowired
    private TmpTariffService tmpTariffService;

    // Cache compiled templates to avoid recompiling on every request
    private static final Map<String, JasperReport> templateCache = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Get or compile a JasperReport template from cache
     */
    private JasperReport getOrCompileTemplate(String templatePath) throws Exception {
        return templateCache.computeIfAbsent(templatePath, path -> {
            try (InputStream is = new ClassPathResource(path).getInputStream()) {
                System.out.println("Compiling template: " + path);
                return JasperCompileManager.compileReport(is);
            } catch (Exception e) {
                throw new RuntimeException("Failed to compile template: " + path, e);
            }
        });
    }

    /**
     * Generate PDF report and return as byte array
     * Story 1.6: Enhanced categorized structure
     */
    public byte[] generateTariffPDFBytes() throws Exception {
        System.out.println("=== Starting Categorized Tariff PDF Generation (Story 1.6) ===");

        // 1. Fetch active tariffs
        List<TariffDTO> activeTariffs = tmpTariffService.getActiveTmpTariffs();
        System.out.println("Found " + activeTariffs.size() + " active tariffs");

        // 2. Categorize tariffs by section
        Map<CategoryType, List<TariffDTO>> categorizedTariffs = categorizeActiveTariffs(activeTariffs);
        System.out.println("Categorized into " + categorizedTariffs.size() + " sections");

        // 3. Create CategorySection objects (AC 16: omit empty sections)
        List<CategorySection> categorySections = new ArrayList<>();

        // Add sections in specific order (AC 2)
        if (categorizedTariffs.containsKey(CategoryType.DOMESTIC)) {
            CategorySection section = createDomesticSection(categorizedTariffs.get(CategoryType.DOMESTIC));
            if (section != null) categorySections.add(section);
        }

        if (categorizedTariffs.containsKey(CategoryType.DOMESTIC_TOU)) {
            CategorySection section = createDomesticTOUSection(categorizedTariffs.get(CategoryType.DOMESTIC_TOU));
            if (section != null) categorySections.add(section);
        }

        if (categorizedTariffs.containsKey(CategoryType.RELIGIOUS)) {
            CategorySection section = createReligiousSection(categorizedTariffs.get(CategoryType.RELIGIOUS));
            if (section != null) categorySections.add(section);
        }

        if (categorizedTariffs.containsKey(CategoryType.OTHER_CONSUMERS)) {
            CategorySection section = createOtherConsumersSection(categorizedTariffs.get(CategoryType.OTHER_CONSUMERS));
            if (section != null) categorySections.add(section);
        }

        if (categorizedTariffs.containsKey(CategoryType.STREET_LIGHTING)) {
            CategorySection section = createStreetLightingSection(categorizedTariffs.get(CategoryType.STREET_LIGHTING));
            if (section != null) categorySections.add(section);
        }

        if (categorizedTariffs.containsKey(CategoryType.AGRICULTURE_TOU)) {
            CategorySection section = createAgricultureTOUSection(categorizedTariffs.get(CategoryType.AGRICULTURE_TOU));
            if (section != null) categorySections.add(section);
        }

        System.out.println("Created " + categorySections.size() + " category sections for report");

        // 4. Load and compile main JRXML template (using cache)
        JasperReport jasperReport = getOrCompileTemplate("reports/tariff_categorized_report.jrxml");
        System.out.println("Main categorized template ready");

        // 5. Compile subreports (using cache for better performance)
        // Story 1.7: Compile detail subreports for nested table rows
        JasperReport blocksDetailJasper = getOrCompileTemplate("reports/domestic_blocks_detail.jrxml");
        JasperReport tariffsDetailJasper = getOrCompileTemplate("reports/other_consumers_tariffs_detail.jrxml");
        JasperReport domesticJasper = getOrCompileTemplate("reports/domestic_subreport.jrxml");
        JasperReport touJasper = getOrCompileTemplate("reports/tou_subreport.jrxml");
        JasperReport simpleBlocksJasper = getOrCompileTemplate("reports/simple_blocks_subreport.jrxml");
        JasperReport otherConsumersJasper = getOrCompileTemplate("reports/other_consumers_subreport.jrxml");
        System.out.println("All subreport templates ready");

        // 6. Prepare parameters
        Map<String, Object> params = new HashMap<>();
        params.put("REPORT_DATE", new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
        params.put("BLOCKS_DETAIL_SUBREPORT", blocksDetailJasper);  // Story 1.7: Pass blocks detail subreport
        params.put("TARIFFS_DETAIL_SUBREPORT", tariffsDetailJasper);  // Story 1.7: Pass tariffs detail subreport
        params.put("DOMESTIC_SUBREPORT", domesticJasper);
        params.put("TOU_SUBREPORT", touJasper);
        params.put("SIMPLE_BLOCKS_SUBREPORT", simpleBlocksJasper);
        params.put("OTHER_CONSUMERS_SUBREPORT", otherConsumersJasper);

        // 7. Create data source with category sections
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(categorySections);

        // 8. Fill report
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);
        System.out.println("Report filled successfully");

        // 9. Export to PDF
        byte[] pdfBytes;
        try (ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream()) {
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(pdfOutputStream));
            exporter.exportReport();
            System.out.println("PDF exported successfully");

            // 10. Return PDF as byte array
            pdfBytes = pdfOutputStream.toByteArray();
        }

        System.out.println("=== Categorized PDF Generation Completed: " + pdfBytes.length + " bytes ===");
        return pdfBytes;
    }

    /**
     * Generate PDF report for all active tariffs (where to_date IS NULL)
     * Legacy method that writes directly to HttpServletResponse
     * @deprecated Use generateTariffPDFBytes() instead
     */
    @Deprecated
    public void generateTariffPDF(HttpServletResponse response) throws Exception {
        byte[] pdfBytes = generateTariffPDFBytes();
        String filename = "tariff_setup_" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".pdf";
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }

    /**
     * Transform TariffDTO list to TariffReportData structure for JasperReports
     */
    private List<TariffReportData> transformTariffData(List<TariffDTO> tariffs) {
        List<TariffReportData> reportDataList = new ArrayList<>();

        for (TariffDTO tariff : tariffs) {
            TariffReportData reportData = new TariffReportData();

            // Basic tariff info
            reportData.setTariffId(tariff.getTariff());
            reportData.setTariffName(getTariffName(tariff.getTariff()));
            reportData.setFromDate(tariff.getFromDate());
            reportData.setToDate(tariff.getToDate());
            reportData.setNumberOfSlabs(tariff.getNumberOfSlabs());
            reportData.setMinCharge(tariff.getMinCharge());

            // Extract blocks based on numberOfSlabs
            List<TariffBlock> blocks = extractBlocks(tariff);
            reportData.setBlocks(blocks);

            reportDataList.add(reportData);
        }

        return reportDataList;
    }

    /**
     * Extract tariff blocks from TariffDTO based on numberOfSlabs
     */
    private List<TariffBlock> extractBlocks(TariffDTO tariff) {
        List<TariffBlock> blocks = new ArrayList<>();
        int numberOfSlabs = tariff.getNumberOfSlabs() != null ? tariff.getNumberOfSlabs() : 0;

        for (int i = 1; i <= numberOfSlabs && i <= 9; i++) {
            TariffBlock block = new TariffBlock();

            // Use reflection to get rate and fixed charge for each slab
            BigDecimal rate = getSlabField(tariff, "rate", i);
            BigDecimal fixedCharge = getSlabField(tariff, "fixedCharge", i);
            Short limit = getSlabLimit(tariff, i);

            if (rate != null || fixedCharge != null) {
                block.setBlockNumber(i);
                block.setBlockName(getBlockName(tariff.getTariff(), i, limit));
                block.setRate(rate);
                block.setFixedCharge(fixedCharge);
                block.setLimit(limit);
                blocks.add(block);
            }
        }

        return blocks;
    }

    /**
     * Get slab field value using reflection
     */
    private BigDecimal getSlabField(TariffDTO tariff, String fieldPrefix, int slabNumber) {
        try {
            String methodName = "get" + capitalizeFirst(fieldPrefix) + slabNumber;
            java.lang.reflect.Method method = TariffDTO.class.getMethod(methodName);
            return (BigDecimal) method.invoke(tariff);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get slab limit value
     */
    private Short getSlabLimit(TariffDTO tariff, int slabNumber) {
        try {
            String methodName = "getLimit" + slabNumber;
            java.lang.reflect.Method method = TariffDTO.class.getMethod(methodName);
            return (Short) method.invoke(tariff);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Generate block name based on tariff type and block number
     */
    private String getBlockName(Short tariffId, int blockNumber, Short limit) {
        // For Time of Use tariffs (13, 14, 15)
        if (tariffId == 13 || tariffId == 14 || tariffId == 15) {
            if (blockNumber == 1) return "Peak";
            if (blockNumber == 2) return "Day";
            if (blockNumber == 3) return "Off-Peak";
        }

        // Default: Block X (0-Y kWh)
        if (limit != null) {
            int prevLimit = blockNumber == 1 ? 0 : (limit - 30);
            return "Block " + blockNumber + " (" + prevLimit + "-" + limit + " kWh)";
        }

        return "Block " + blockNumber;
    }

    /**
     * Get tariff display name based on tariff ID
     */
    private String getTariffName(Short tariffId) {
        if (tariffId == null) return "Unknown";

        switch (tariffId) {
            case 11: return "Domestic";
            case 13: return "Domestic TOU - Peak";
            case 14: return "Domestic TOU - Day";
            case 15: return "Domestic TOU - Off-Peak";
            case 21: return "Industrial - LV (<300 kWh)";
            case 22: return "Industrial - LV (>300 kWh)";
            case 31: return "Hotel - LV (<300 kWh)";
            case 32: return "Hotel - LV (>300 kWh)";
            case 33: return "Hotel - MV (<300 kWh)";
            case 34: return "Hotel - MV (>300 kWh)";
            case 41: return "General Purpose - LV (<300 kWh)";
            case 42: return "General Purpose - LV (>300 kWh)";
            case 51: return "Religious & Charitable";
            case 61: return "Street Lighting";
            case 71: return "Agriculture - Peak";
            case 72: return "Agriculture - Day";
            case 73: return "Agriculture - Off-Peak";
            case 93: return "EV Charging";
            default: return "Tariff " + tariffId;
        }
    }

    /**
     * Capitalize first letter of string
     */
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    // ================================================================
    // NEW CATEGORIZATION METHODS FOR STORY 1.6
    // ================================================================

    /**
     * Format rate value: 0 for zero/null, 2 decimal places for non-zero
     */
    private String formatRateValue(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) == 0) {
            return "0";
        }
        return String.format("%.2f", value);
    }

    /**
     * Determine category type based on tariff ID
     */
    private CategoryType determineCategoryType(Short tariffId) {
        if (tariffId == null) return null;

        if (tariffId == 11) return CategoryType.DOMESTIC;
        if (tariffId >= 13 && tariffId <= 15) return CategoryType.DOMESTIC_TOU;
        if (tariffId == 51) return CategoryType.RELIGIOUS;
        if (tariffId == 61) return CategoryType.STREET_LIGHTING;
        if (tariffId >= 71 && tariffId <= 73) return CategoryType.AGRICULTURE_TOU;
        if (Arrays.asList(21, 22, 31, 32, 33, 34, 41, 42).contains(tariffId.intValue())) {
            return CategoryType.OTHER_CONSUMERS;
        }
        return null;
    }

    /**
     * Categorize active tariffs by section
     */
    private Map<CategoryType, List<TariffDTO>> categorizeActiveTariffs(List<TariffDTO> activeTariffs) {
        Map<CategoryType, List<TariffDTO>> categories = new EnumMap<>(CategoryType.class);

        for (TariffDTO tariff : activeTariffs) {
            CategoryType category = determineCategoryType(tariff.getTariff());
            if (category != null) {
                categories.computeIfAbsent(category, k -> new ArrayList<>()).add(tariff);
            }
        }

        return categories;
    }

    /**
     * Create Domestic section with consumption range grouping
     * AC 4: [0–60 kWh / Month] blocks 1-2, [Above 60 kWh] blocks 3-6
     */
    private CategorySection createDomesticSection(List<TariffDTO> tariffs) {
        if (tariffs.isEmpty()) return null;

        TariffDTO tariff = tariffs.get(0); // Domestic has only one tariff (11)
        Date fromDate = tariff.getFromDate();

        List<ConsumptionGroup> groups = new ArrayList<>();

        // Group 1: [0–60 kWh / Month] - blocks 1-2
        ConsumptionGroup lowConsumption = new ConsumptionGroup();
        lowConsumption.setRangeName("[0–60 kWh / Month]");
        List<TariffBlockDisplay> lowBlocks = new ArrayList<>();
        lowBlocks.add(new TariffBlockDisplay("0-30 kWh",
            formatRateValue(tariff.getRate1()),
            formatRateValue(tariff.getFixedCharge1())));
        lowBlocks.add(new TariffBlockDisplay("31-60 kWh",
            formatRateValue(tariff.getRate2()),
            formatRateValue(tariff.getFixedCharge2())));
        lowConsumption.setBlocks(lowBlocks);
        groups.add(lowConsumption);

        // Group 2: [Above 60 kWh] - blocks 3-6
        ConsumptionGroup highConsumption = new ConsumptionGroup();
        highConsumption.setRangeName("[Above 60 kWh]");
        List<TariffBlockDisplay> highBlocks = new ArrayList<>();
        highBlocks.add(new TariffBlockDisplay("61-90 kWh",
            formatRateValue(tariff.getRate3()),
            formatRateValue(tariff.getFixedCharge3())));
        highBlocks.add(new TariffBlockDisplay("91-120 kWh",
            formatRateValue(tariff.getRate4()),
            formatRateValue(tariff.getFixedCharge4())));
        highBlocks.add(new TariffBlockDisplay("121-180 kWh",
            formatRateValue(tariff.getRate5()),
            formatRateValue(tariff.getFixedCharge5())));
        highBlocks.add(new TariffBlockDisplay("Above 180 kWh",
            formatRateValue(tariff.getRate6()),
            formatRateValue(tariff.getFixedCharge6())));
        highConsumption.setBlocks(highBlocks);
        groups.add(highConsumption);

        List<Object> tariffData = new ArrayList<>(groups);
        return new CategorySection("Domestic (Tariff 11)", fromDate, CategoryType.DOMESTIC, tariffData);
    }

    /**
     * Create Domestic TOU section with time ranges
     * AC 5: Display Peak (18:30–22:30), Day (05:30–18:30), Off Peak (22:30–05:30)
     */
    private CategorySection createDomesticTOUSection(List<TariffDTO> tariffs) {
        if (tariffs.isEmpty()) return null;

        Date earliestDate = tariffs.stream()
            .map(TariffDTO::getFromDate)
            .min(Date::compareTo)
            .orElse(null);

        List<TOUTariffDisplay> touTariffs = new ArrayList<>();

        for (TariffDTO tariff : tariffs) {
            Short tariffId = tariff.getTariff();
            String periodLabel = "";
            String timeRange = "";

            if (tariffId == 13) {
                periodLabel = "Peak";
                timeRange = "18:30–22:30";
            } else if (tariffId == 14) {
                periodLabel = "Day";
                timeRange = "05:30–18:30";
            } else if (tariffId == 15) {
                periodLabel = "Off Peak";
                timeRange = "22:30–05:30";
            }

            touTariffs.add(new TOUTariffDisplay(
                periodLabel,
                timeRange,
                tariffId,
                formatRateValue(tariff.getRate1()),
                formatRateValue(tariff.getFixedCharge1())
            ));
        }

        List<Object> tariffData = new ArrayList<>(touTariffs);
        return new CategorySection("Domestic Time of Use", earliestDate, CategoryType.DOMESTIC_TOU, tariffData);
    }

    /**
     * Create Religious & Charitable section with simple block listing
     */
    private CategorySection createReligiousSection(List<TariffDTO> tariffs) {
        if (tariffs.isEmpty()) return null;

        TariffDTO tariff = tariffs.get(0);
        List<TariffBlockDisplay> blocks = new ArrayList<>();

        int numberOfSlabs = tariff.getNumberOfSlabs() != null ? tariff.getNumberOfSlabs() : 0;
        int previousLimit = 0; // Track previous limit for cumulative range calculation

        for (int i = 1; i <= numberOfSlabs && i <= 9; i++) {
            BigDecimal rate = getSlabField(tariff, "rate", i);
            BigDecimal fixedCharge = getSlabField(tariff, "fixedCharge", i);
            Short limit = getSlabLimit(tariff, i);

            if (rate != null || fixedCharge != null) {
                String rangeLabel;
                if (limit != null) {
                    // Create cumulative range with proper boundaries
                    // First block: 0-30, subsequent blocks: 31-90, 91-120, etc.
                    int startRange = (previousLimit == 0) ? 0 : previousLimit + 1;
                    rangeLabel = startRange + "-" + limit + " kWh";
                    previousLimit = limit; // Update for next iteration
                } else {
                    // Last block with no limit: "Above X kWh"
                    rangeLabel = "Above " + previousLimit + " kWh";
                }

                blocks.add(new TariffBlockDisplay(rangeLabel,
                    formatRateValue(rate),
                    formatRateValue(fixedCharge)));
            }
        }

        List<Object> tariffData = new ArrayList<>(blocks);
        return new CategorySection("Religious & Charitable (Tariff 51)",
            tariff.getFromDate(), CategoryType.RELIGIOUS, tariffData);
    }

    /**
     * Create Street Lighting section
     */
    private CategorySection createStreetLightingSection(List<TariffDTO> tariffs) {
        if (tariffs.isEmpty()) return null;

        TariffDTO tariff = tariffs.get(0);
        List<TariffBlockDisplay> blocks = new ArrayList<>();

        blocks.add(new TariffBlockDisplay("Energy Charge",
            formatRateValue(tariff.getRate1()),
            formatRateValue(tariff.getFixedCharge1())));

        List<Object> tariffData = new ArrayList<>(blocks);
        return new CategorySection("Street Lighting (Tariff 61)",
            tariff.getFromDate(), CategoryType.STREET_LIGHTING, tariffData);
    }

    /**
     * Create Agriculture TOU section
     * AC 5: Similar to Domestic TOU with time ranges
     */
    private CategorySection createAgricultureTOUSection(List<TariffDTO> tariffs) {
        if (tariffs.isEmpty()) return null;

        Date earliestDate = tariffs.stream()
            .map(TariffDTO::getFromDate)
            .min(Date::compareTo)
            .orElse(null);

        List<TOUTariffDisplay> touTariffs = new ArrayList<>();

        for (TariffDTO tariff : tariffs) {
            Short tariffId = tariff.getTariff();
            String periodLabel = "";
            String timeRange = "";

            if (tariffId == 71) {
                periodLabel = "Peak";
                timeRange = "18:30–22:30";
            } else if (tariffId == 72) {
                periodLabel = "Day";
                timeRange = "05:30–18:30";
            } else if (tariffId == 73) {
                periodLabel = "Off Peak";
                timeRange = "22:30–05:30";
            }

            touTariffs.add(new TOUTariffDisplay(
                periodLabel,
                timeRange,
                tariffId,
                formatRateValue(tariff.getRate1()),
                formatRateValue(tariff.getFixedCharge1())
            ));
        }

        List<Object> tariffData = new ArrayList<>(touTariffs);
        return new CategorySection("Agriculture Optional TOU", earliestDate, CategoryType.AGRICULTURE_TOU, tariffData);
    }

    /**
     * Create Other Consumers section with sub-grouping
     * AC 6, 7: Sub-group by Industrial, Hotel, General Purpose, Government
     */
    private CategorySection createOtherConsumersSection(List<TariffDTO> tariffs) {
        if (tariffs.isEmpty()) return null;

        Date earliestDate = tariffs.stream()
            .map(TariffDTO::getFromDate)
            .min(Date::compareTo)
            .orElse(null);

        List<SubGroupSection> subGroups = new ArrayList<>();

        // Industrial (21, 22)
        List<TariffDTO> industrial = tariffs.stream()
            .filter(t -> t.getTariff() == 21 || t.getTariff() == 22)
            .collect(Collectors.toList());
        if (!industrial.isEmpty()) {
            subGroups.add(createSubGroup("Industrial", industrial, 300));
        }

        // Hotel (41, 42)
        List<TariffDTO> hotel = tariffs.stream()
            .filter(t -> t.getTariff() == 41 || t.getTariff() == 42)
            .collect(Collectors.toList());
        if (!hotel.isEmpty()) {
            subGroups.add(createSubGroup("Hotel", hotel, 300));
        }

        // General Purpose (31, 32)
        List<TariffDTO> generalPurpose = tariffs.stream()
            .filter(t -> t.getTariff() == 31 || t.getTariff() == 32)
            .collect(Collectors.toList());
        if (!generalPurpose.isEmpty()) {
            subGroups.add(createSubGroup("General Purpose", generalPurpose, 180));
        }

        // Government (33, 34)
        List<TariffDTO> government = tariffs.stream()
            .filter(t -> t.getTariff() == 33 || t.getTariff() == 34)
            .collect(Collectors.toList());
        if (!government.isEmpty()) {
            subGroups.add(createSubGroup("Government", government, 180));
        }

        List<Object> tariffData = new ArrayList<>(subGroups);
        return new CategorySection("Other Consumers", earliestDate, CategoryType.OTHER_CONSUMERS, tariffData);
    }

    /**
     * Helper to create sub-group with threshold labels
     */
    private SubGroupSection createSubGroup(String subGroupName, List<TariffDTO> tariffs, int threshold) {
        List<SimpleTariffDisplay> simpleTariffs = new ArrayList<>();

        // Sort by tariff ID to ensure consistent ordering
        tariffs.sort(Comparator.comparing(TariffDTO::getTariff));

        for (int i = 0; i < tariffs.size(); i++) {
            TariffDTO tariff = tariffs.get(i);
            String thresholdLabel = (i == 0) ? "<" + threshold + " kWh" : ">" + threshold + " kWh";

            simpleTariffs.add(new SimpleTariffDisplay(
                thresholdLabel,
                formatRateValue(tariff.getRate1()),
                formatRateValue(tariff.getFixedCharge1())
            ));
        }

        return new SubGroupSection(subGroupName, simpleTariffs);
    }

    // ================================================================
    // INNER CLASSES FOR JASPERREPORTS DATA STRUCTURE
    // ================================================================

    /**
     * Main data structure for tariff report
     */
    public static class TariffReportData {
        private Short tariffId;
        private String tariffName;
        private Date fromDate;
        private Date toDate;
        private Short numberOfSlabs;
        private BigDecimal minCharge;
        private List<TariffBlock> blocks;

        // Getters and Setters
        public Short getTariffId() { return tariffId; }
        public void setTariffId(Short tariffId) { this.tariffId = tariffId; }

        public String getTariffName() { return tariffName; }
        public void setTariffName(String tariffName) { this.tariffName = tariffName; }

        public Date getFromDate() { return fromDate; }
        public void setFromDate(Date fromDate) { this.fromDate = fromDate; }

        public Date getToDate() { return toDate; }
        public void setToDate(Date toDate) { this.toDate = toDate; }

        public Short getNumberOfSlabs() { return numberOfSlabs; }
        public void setNumberOfSlabs(Short numberOfSlabs) { this.numberOfSlabs = numberOfSlabs; }

        public BigDecimal getMinCharge() { return minCharge; }
        public void setMinCharge(BigDecimal minCharge) { this.minCharge = minCharge; }

        public List<TariffBlock> getBlocks() { return blocks; }
        public void setBlocks(List<TariffBlock> blocks) { this.blocks = blocks; }
    }

    /**
     * Data structure for individual tariff blocks
     */
    public static class TariffBlock {
        private Integer blockNumber;
        private String blockName;
        private BigDecimal rate;
        private BigDecimal fixedCharge;
        private Short limit;

        // Getters and Setters
        public Integer getBlockNumber() { return blockNumber; }
        public void setBlockNumber(Integer blockNumber) { this.blockNumber = blockNumber; }

        public String getBlockName() { return blockName; }
        public void setBlockName(String blockName) { this.blockName = blockName; }

        public BigDecimal getRate() { return rate; }
        public void setRate(BigDecimal rate) { this.rate = rate; }

        public BigDecimal getFixedCharge() { return fixedCharge; }
        public void setFixedCharge(BigDecimal fixedCharge) { this.fixedCharge = fixedCharge; }

        public Short getLimit() { return limit; }
        public void setLimit(Short limit) { this.limit = limit; }
    }
}
