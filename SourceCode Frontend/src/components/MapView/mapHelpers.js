import L from 'leaflet';

/**
 * Plant Type Constants
 */
export const PLANT_TYPES = {
  WIND: 'Wind',
  HYDRO: 'MiniHydro',
  SOLAR: 'Solar',
  BIOMASS: 'Biomass',
  DENDRO: 'Dendro',
  WASTE_HEAT: 'WasteHeat',
  UNKNOWN: 'Solar',
};

export const PLANT_TYPE_LABELS = {
  [PLANT_TYPES.WIND]: 'Wind Generation',
  [PLANT_TYPES.HYDRO]: 'Mini Hydro',
  [PLANT_TYPES.SOLAR]: 'Solar Power',
  [PLANT_TYPES.BIOMASS]: 'Biomass',
  [PLANT_TYPES.DENDRO]: 'Dendro',
  [PLANT_TYPES.WASTE_HEAT]: 'Waste to Heat Power',
  [PLANT_TYPES.UNKNOWN]: 'Other / Generic Plant',
};

// Unified green brand color
export const PLANT_ICON_COLOR = '#059669';
export const UNIFIED_MARKER_COLOR = PLANT_ICON_COLOR;

/**
 * Supplied exact SVG definitions
 */
export const ICONS = {
  Solar: `<svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.8" stroke-linecap="round"> <circle cx="12" cy="12" r="4"/> <line x1="12" y1="2" x2="12" y2="5"/> <line x1="12" y1="19" x2="12" y2="22"/> <line x1="2" y1="12" x2="5" y2="12"/> <line x1="19" y1="12" x2="22" y2="12"/> <line x1="4.9" y1="4.9" x2="7" y2="7"/> <line x1="17" y1="17" x2="19.1" y2="19.1"/> <line x1="19.1" y1="4.9" x2="17" y2="7"/> <line x1="7" y1="17" x2="4.9" y2="19.1"/> </svg>`,
  Wind: `<svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"> <line x1="12" y1="22" x2="12" y2="10"/> <circle cx="12" cy="10" r="1.1" fill="white" stroke="none"/> <path d="M12 10 C 15 9, 17 6.5, 17 3"/> <path d="M12 10 C 9.5 12.5, 6.5 12.5, 4.3 10.8"/> <path d="M12 10 C 13.2 13, 12.8 16, 10.2 17.8"/> </svg>`,
  MiniHydro: `<svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"> <path d="M12 3 C 8.5 8, 6 11.5, 6 14.5 C 6 18 8.7 20 12 20 C 15.3 20 18 18 18 14.5 C 18 11.5 15.5 8 12 3 Z"/> <path d="M9 15 C 10 16.3 14 16.3 15 15" fill="none"/> </svg>`,
  Biomass: `<svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"> <path d="M6 20 C 5 13 8 6 18 4 C 17 12 13 18 6 20 Z"/> <path d="M7.5 18.5 C 10 14 12.5 10.5 16.5 6.5"/> </svg>`,
  Dendro: `<svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"> <line x1="12" y1="21" x2="12" y2="15.5"/> <path d="M12 15.5 L8.5 12 M12 15.5 L15.5 12"/> <path d="M12 4 L7 11 H17 Z"/> <path d="M12 7.2 L9 12 H15 Z"/> </svg>`,
  WasteHeat: `<svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"> <path d="M8.5 14.5A2.5 2.5 0 0 0 11 12c0-1.38-.5-2-1-3-1.072-2.143-.224-4.054 2-6 .5 2.5 2 4.9 4 6.5 2 1.6 3 3.5 3 5.5a7 7 0 1 1-14 0c0-1.153.433-2.294 1-3a2.5 2.5 0 0 0 2.5 2.5z"/> </svg>`
};

/**
 * Normalizes any raw plant type string into one of the supported visual icon categories.
 * Supported values:
 * - S, SPP, SOLAR -> Solar
 * - W, WPP, WIND -> Wind
 * - M, MHP, HYDRO, MINI HYDRO -> MiniHydro
 * - B, BMP, BIOMASS -> Biomass
 * - D, DPP, DENDRO -> Dendro
 * - WHP, WASTE TO HEAT POWER, WASTE HEAT -> WasteHeat
 */
export const normalizePlantType = (rawType) => {
  if (!rawType || typeof rawType !== 'string') return PLANT_TYPES.UNKNOWN;

  const clean = rawType
    .toLowerCase()
    .replace(/[-_/\\]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();

  if (!clean) return PLANT_TYPES.UNKNOWN;

  // 1. Dendro matching (Dendro, DPP, D) - kept distinct from Biomass
  if (
    clean === 'd' ||
    clean === 'dpp' ||
    clean.includes('dendro')
  ) {
    return PLANT_TYPES.DENDRO;
  }

  // 2. Biomass matching (BMP, B, Biomass)
  if (
    clean === 'b' ||
    clean === 'bmp' ||
    clean.includes('biomass') ||
    clean.includes('bio mass')
  ) {
    return PLANT_TYPES.BIOMASS;
  }

  // 3. Solar matching (Solar, SPP, S, PV)
  if (
    clean === 's' ||
    clean === 'spp' ||
    clean.includes('solar') ||
    clean.includes('pv')
  ) {
    return PLANT_TYPES.SOLAR;
  }

  // 4. Waste to Heat Power matching (WHP, Waste to Heat, Waste Heat)
  if (
    clean === 'whp' ||
    clean.includes('waste to heat') ||
    clean.includes('waste heat') ||
    clean.includes('waste-to-heat') ||
    clean.includes('waste-heat')
  ) {
    return PLANT_TYPES.WASTE_HEAT;
  }

  // 5. Wind matching (Wind, WPP, W)
  if (
    clean === 'w' ||
    clean === 'wpp' ||
    clean.includes('wind')
  ) {
    return PLANT_TYPES.WIND;
  }

  // 6. Mini Hydro matching (Mini Hydro, MHP, Hydro, Small Hydro, M)
  if (
    clean === 'm' ||
    clean === 'mhp' ||
    clean.includes('hydro')
  ) {
    return PLANT_TYPES.HYDRO;
  }

  return PLANT_TYPES.UNKNOWN;
};

/**
 * Normalizes a folio number for consistent matching.
 * Rules:
 * - Trim leading and trailing whitespace
 * - Handle case differences (convert to uppercase)
 * - Preserve leading zeros (no numeric conversion)
 * - Handle null safely (returns empty string)
 */
export const normalizeFolio = (folio) => {
  if (folio == null) return '';
  return String(folio).trim().toUpperCase();
};

/**
 * Builds the complete HTML for the map pin marker.
 */
export const createPlantMarkerSvg = (iconKey, isCoLocated = false, isOnline = false) => {
  const iconSvg = ICONS[iconKey] || ICONS.Solar;
  const onlineClass = isOnline ? 'plant-pin--online' : '';
  const badgeHtml = isCoLocated ? '<div class="plant-pin__badge">+</div>' : '';

  return `
    <div class="plant-pin-container">
      <div class="plant-pin ${onlineClass}">
        ${iconSvg}
      </div>
      ${badgeHtml}
    </div>
  `;
};

/**
 * Creates a Leaflet DivIcon for a power plant with proper classes and anchors.
 */
export const createPlantMarkerIcon = (plant, isTop = false, isOnline = false) => {
  const rawType = plant.ncreCategory || plant.type || plant.ncre_type || '';
  const normalizedType = normalizePlantType(rawType);
  const isCoLocated = (plant.coLocatedCount || 1) > 1;

  const html = createPlantMarkerSvg(normalizedType, isCoLocated, isOnline);

  return L.divIcon({
    html: html,
    iconSize: [36, 44],
    iconAnchor: [18, 44],
    popupAnchor: isTop ? [0, 8] : [0, -44],
    className: 'energy-plant-marker',
  });
};

/**
 * Generates an inline preview icon for the map legend.
 */
export const getLegendIconSvg = (rawType) => {
  const plantType = normalizePlantType(rawType);
  const iconSvg = ICONS[plantType] || ICONS.Solar;

  return `
    <div style="width:24px;height:24px;border-radius:6px;background:${PLANT_ICON_COLOR};display:flex;align-items:center;justify-content:center;box-shadow:0 1px 3px rgba(0,0,0,0.15);">
      <div style="width:16px;height:16px;display:flex;align-items:center;justify-content:center;">
        ${iconSvg}
      </div>
    </div>
  `;
};
