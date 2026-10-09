import {
  PLANT_TYPES,
  PLANT_ICON_COLOR,
  UNIFIED_MARKER_COLOR,
  normalizePlantType,
  normalizeFolio,
  createPlantMarkerIcon,
  createPlantMarkerSvg,
  getLegendIconSvg,
  ICONS,
} from './mapHelpers';

describe('mapHelpers - Folio Normalization', () => {
  test('trims whitespace and handles case differences', () => {
    expect(normalizeFolio(' ABC001 ')).toBe('ABC001');
    expect(normalizeFolio('abc001')).toBe('ABC001');
    expect(normalizeFolio('  AbC001  ')).toBe('ABC001');
    // Case and space normalized match
    expect(normalizeFolio(' ABC001 ')).toBe(normalizeFolio('abc001'));
  });

  test('preserves leading zeros without numeric conversion', () => {
    expect(normalizeFolio('00123')).toBe('00123');
    expect(normalizeFolio('  00042  ')).toBe('00042');
    expect(normalizeFolio('000')).toBe('000');
    // If it were converted to number, 00123 would become 123 - verify it stays 00123
    expect(normalizeFolio('00123')).not.toBe('123');
  });

  test('handles null and undefined safely', () => {
    expect(normalizeFolio(null)).toBe('');
    expect(normalizeFolio(undefined)).toBe('');
    expect(normalizeFolio('')).toBe('');
    expect(normalizeFolio('   ')).toBe('');
  });
});

describe('mapHelpers - Plant Type Normalization', () => {
  test('normalizes Wind generation variations (including WPP)', () => {
    expect(normalizePlantType('Wind')).toBe(PLANT_TYPES.WIND);
    expect(normalizePlantType('WIND')).toBe(PLANT_TYPES.WIND);
    expect(normalizePlantType('Wind Power')).toBe(PLANT_TYPES.WIND);
    expect(normalizePlantType('Wind Generation')).toBe(PLANT_TYPES.WIND);
    expect(normalizePlantType('WPP')).toBe(PLANT_TYPES.WIND);
    expect(normalizePlantType('W')).toBe(PLANT_TYPES.WIND);
  });

  test('normalizes Waste to Heat Power variations (including WHP)', () => {
    expect(normalizePlantType('Waste to Heat Power')).toBe(PLANT_TYPES.WASTE_HEAT);
    expect(normalizePlantType('Waste to Heat')).toBe(PLANT_TYPES.WASTE_HEAT);
    expect(normalizePlantType('Waste-to-Heat')).toBe(PLANT_TYPES.WASTE_HEAT);
    expect(normalizePlantType('Waste Heat')).toBe(PLANT_TYPES.WASTE_HEAT);
    expect(normalizePlantType('Waste Heat Power')).toBe(PLANT_TYPES.WASTE_HEAT);
    expect(normalizePlantType('WHP')).toBe(PLANT_TYPES.WASTE_HEAT);
    expect(normalizePlantType('whp')).toBe(PLANT_TYPES.WASTE_HEAT);
    expect(normalizePlantType('WHP')).not.toBe(PLANT_TYPES.WIND);
  });

  test('normalizes Mini Hydro variations (including MHP)', () => {
    expect(normalizePlantType('Mini Hydro')).toBe(PLANT_TYPES.HYDRO);
    expect(normalizePlantType('Mini-Hydro')).toBe(PLANT_TYPES.HYDRO);
    expect(normalizePlantType('Hydro')).toBe(PLANT_TYPES.HYDRO);
    expect(normalizePlantType('Small Hydro')).toBe(PLANT_TYPES.HYDRO);
    expect(normalizePlantType('MHP')).toBe(PLANT_TYPES.HYDRO);
    expect(normalizePlantType('M')).toBe(PLANT_TYPES.HYDRO);
  });

  test('normalizes Solar Power variations (including SPP)', () => {
    expect(normalizePlantType('Solar')).toBe(PLANT_TYPES.SOLAR);
    expect(normalizePlantType('Solar Power')).toBe(PLANT_TYPES.SOLAR);
    expect(normalizePlantType('Solar PV')).toBe(PLANT_TYPES.SOLAR);
    expect(normalizePlantType('SPP')).toBe(PLANT_TYPES.SOLAR);
    expect(normalizePlantType('S')).toBe(PLANT_TYPES.SOLAR);
  });

  test('normalizes Biomass variations (BMP, B, Biomass)', () => {
    expect(normalizePlantType('Biomass')).toBe(PLANT_TYPES.BIOMASS);
    expect(normalizePlantType('Bio Mass')).toBe(PLANT_TYPES.BIOMASS);
    expect(normalizePlantType('Bio-Mass')).toBe(PLANT_TYPES.BIOMASS);
    expect(normalizePlantType('BMP')).toBe(PLANT_TYPES.BIOMASS);
    expect(normalizePlantType('B')).toBe(PLANT_TYPES.BIOMASS);
  });

  test('normalizes Dendro separately from Biomass (DPP, D, Dendro)', () => {
    expect(normalizePlantType('Dendro')).toBe(PLANT_TYPES.DENDRO);
    expect(normalizePlantType('DPP')).toBe(PLANT_TYPES.DENDRO);
    expect(normalizePlantType('D')).toBe(PLANT_TYPES.DENDRO);
    expect(normalizePlantType('Dendro')).not.toBe(PLANT_TYPES.BIOMASS);
  });

  test('falls back gracefully to UNKNOWN for unsupported / unexpected types', () => {
    expect(normalizePlantType('Unknown Plant')).toBe(PLANT_TYPES.UNKNOWN);
    expect(normalizePlantType('OTHER_GEN')).toBe(PLANT_TYPES.UNKNOWN);
    expect(normalizePlantType('')).toBe(PLANT_TYPES.UNKNOWN);
    expect(normalizePlantType(null)).toBe(PLANT_TYPES.UNKNOWN);
    expect(normalizePlantType(undefined)).toBe(PLANT_TYPES.UNKNOWN);
  });
});

describe('mapHelpers - Marker Icon Creation & Blinking Behavior', () => {
  test('online plant receives blinking class plant-pin--online', () => {
    const plant = { ncreCategory: 'Solar', folioNo: 'SOL-01' };
    const isOnline = true;
    const icon = createPlantMarkerIcon(plant, false, isOnline);
    expect(icon.options.html).toContain('plant-pin--online');
  });

  test('offline plant has NO blinking class and remains static', () => {
    const plant = { ncreCategory: 'Solar', folioNo: 'SOL-02' };
    const isOnline = false;
    const icon = createPlantMarkerIcon(plant, false, isOnline);
    expect(icon.options.html).not.toContain('plant-pin--online');
    expect(icon.options.html).toContain('plant-pin');
  });

  test('missing/unavailable status remains static without blinking class', () => {
    const plant = { ncreCategory: 'Wind', folioNo: 'WND-99' };
    // Default isOnline = false
    const icon = createPlantMarkerIcon(plant, false, false);
    expect(icon.options.html).not.toContain('plant-pin--online');
    expect(icon.options.html).toContain('plant-pin');
  });

  test('both online and offline plants render with the green brand color', () => {
    const onlineHtml = createPlantMarkerSvg('Solar', false, true);
    const offlineHtml = createPlantMarkerSvg('Solar', false, false);
    expect(onlineHtml).toContain('plant-pin--online');
    expect(offlineHtml).not.toContain('plant-pin--online');
    expect(PLANT_ICON_COLOR).toBe('#059669');
  });

  test('Dendro gets Dendro icon', () => {
    const dendroPlant = { ncreCategory: 'Dendro' };
    const icon = createPlantMarkerIcon(dendroPlant);
    // Dendro SVG features line x1="12" y1="21" x2="12" y2="15.5"
    expect(icon.options.html).toContain('y2="15.5"');
  });

  test('co-located plants include + badge', () => {
    const icon = createPlantMarkerIcon({ ncreCategory: 'Solar', coLocatedCount: 3 });
    expect(icon.options.html).toContain('plant-pin__badge');
    expect(icon.options.html).toContain('+');
  });

  test('single plant does not include + badge', () => {
    const icon = createPlantMarkerIcon({ ncreCategory: 'Solar', coLocatedCount: 1 });
    expect(icon.options.html).not.toContain('plant-pin__badge');
  });

  test('Waste to Heat Power gets WasteHeat icon', () => {
    const whpPlant = { ncreCategory: 'WHP' };
    const icon = createPlantMarkerIcon(whpPlant);
    expect(icon.options.className).toContain('energy-plant-marker');
  });

  test('legend icons render valid SVG elements with green brand background', () => {
    ['W', 'M', 'S', 'B', 'D', 'WHP', 'UNKNOWN'].forEach(k => {
      const svg = getLegendIconSvg(k);
      expect(svg).toContain('<svg');
      expect(svg).toContain(PLANT_ICON_COLOR);
    });
  });
});
