import { MapContainer, TileLayer, Marker, Popup, Polyline, Tooltip, useMap, ZoomControl } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import './MapView.css';
import { useState, useEffect, useRef } from 'react';
import { fetchNCRECustomers, fetchNCREPlantStatuses } from '../../services/ncrePlantationService';
import { getSelectedAreaCode } from '../../services/AreaAndBillService';
import { createPlantMarkerIcon, normalizePlantType, UNIFIED_MARKER_COLOR, getLegendIconSvg, normalizeFolio } from './mapHelpers';

// Sri Lanka bounds with padding to prevent popup cropping at edges
const sriLankaBounds = [
  [2.0, 75.0],   // Southwest
  [15.0, 85.0],  // Northeast
];

// Category colors - single source of truth
const CATEGORY_COLORS = {
  'S': '#FFA500',   // Orange  - Solar (SPP)
  'W': '#9C27B0',   // Purple  - Wind Power (WPP)
  'M': '#0066CC',   // Blue    - Mini Hydro (MHP)
  'B': '#228B22',   // Green   - Biomass (BMP)
  'D': '#8B4513',   // Brown   - Dendro (DPP)
  'WHP': '#00A896',   // Teal    - Waste to Heat Power (WHP)
};

const CATEGORY_LABELS = {
  'B': 'BIOMASS',
  'M': 'MINI HYDRO',
  'S': 'SOLAR',
  'W': 'WIND POWER',
  'D': 'DENDRO',
  'WHP': 'WASTE TO HEAT POWER',
};

// ─── Co-location offset ───────────────────────────────────────────────────────
const OFFSET_RADIUS_DEG = 0.003;

const resolveCoLocated = (items) => {
  const buckets = {};
  items.forEach((item) => {
    const key = `${item.lat.toFixed(5)},${item.lng.toFixed(5)}`;
    if (!buckets[key]) buckets[key] = [];
    buckets[key].push(item);
  });
  const result = [];
  Object.values(buckets).forEach((group) => {
    if (group.length === 1) { result.push(group[0]); return; }
    group.forEach((item, idx) => {
      const angle = (2 * Math.PI * idx) / group.length;
      result.push({
        ...item,
        lat: item.lat + OFFSET_RADIUS_DEG * Math.sin(angle),
        lng: item.lng + OFFSET_RADIUS_DEG * Math.cos(angle),
        originalLat: item.lat,
        originalLng: item.lng,
        coLocatedCount: group.length,
        coLocatedIndex: idx + 1,
      });
    });
  });
  return result;
};

// ─── Transform backend data ───────────────────────────────────────────────────
const mapCategoryKey = (catStr) => {
  if (!catStr) return 'UNKNOWN';
  const u = catStr.trim().toUpperCase();
  if (u === 'WHP' || u.includes('WASTE TO HEAT') || u.includes('WASTE-TO-HEAT') || u.includes('WASTE HEAT') || u.includes('WASTE-HEAT') || u.includes('WIND HYDRO') || u.includes('WIND-HYDRO')) return 'WHP';
  if (u === 'S' || u === 'SPP' || u.includes('SOLAR')) return 'S';
  if (u === 'W' || u === 'WPP' || u.includes('WIND')) return 'W';
  if (u === 'M' || u === 'MHP' || u.includes('HYDRO')) return 'M';
  if (u === 'B' || u === 'BMP' || u.includes('BIOMASS')) return 'B';
  if (u === 'D' || u === 'DPP' || u.includes('DENDRO')) return 'D';
  return u;
};

const transformBackendData = (data) => {
  return data
    .filter((c) => c && c.latitude != null && c.longitude != null && !isNaN(Number(c.latitude)) && !isNaN(Number(c.longitude)) && Number(c.latitude) !== 0 && Number(c.longitude) !== 0)
    .map((c) => {
      const addressParts = [c.addressLine1, c.addressLine2, c.addressLine3]
        .filter(Boolean).map((s) => s.trim()).filter(Boolean);
      const fullAddress = addressParts.join(', ') || c.city || '';
      const rawCat = c.ncreCategory || c.type || c.ncre_category || '';
      return {
        accountNumber: c.accNbr || c.accountNumber || '',
        folioNo: c.folioNo != null ? c.folioNo : '',
        areaCode: c.areaCd || c.areaCode || c.area_code || '',
        area: c.area || c.city || '',
        name: c.name || '',
        developerName: c.developerName || c.name || '',
        facilityName: c.facilityName || c.projectName || '',
        address: fullAddress,
        ncreCategory: mapCategoryKey(rawCat),
        rawPlantType: rawCat,
        responsibleEe: (c.responsibleEe || c.responsible_ee || c.responsble_ee || '').trim(),
        lat: Number(c.latitude),
        lng: Number(c.longitude),
        originalLat: Number(c.latitude),
        originalLng: Number(c.longitude),
        coLocatedCount: 1,
        coLocatedIndex: 1,
      };
    });
};

// ─── Popup HTML ───────────────────────────────────────────────────────────────
const createPopupContent = (p, isOnline = false, statusLoaded = false) => {
  const categoryLabel = CATEGORY_LABELS[p.ncreCategory] || 'UNKNOWN';

  const coLocatedNote = p.coLocatedCount > 1
    ? `<div style="background:#fff3cd;border:1px solid #ffc107;border-radius:4px;
                   padding:6px 10px;margin-bottom:10px;font-size:11px;color:#856404;">
         ⚠ ${p.coLocatedCount} projects share this location · project ${p.coLocatedIndex} of ${p.coLocatedCount}
       </div>`
    : '';

  const statusHtml = !statusLoaded
    ? '<span style="color:#888;">Unavailable</span>'
    : (isOnline
      ? '<span style="color:#059669;font-weight:700;">Online</span>'
      : '<span style="color:#666;font-weight:600;">Offline</span>');

  const row = (label, value) => value
    ? `<div style="display:flex;justify-content:space-between;align-items:flex-start;
                   margin-bottom:8px;padding-bottom:8px;border-bottom:1px solid #f0f0f0;">
         <strong style="color:#667eea;font-size:11px;white-space:nowrap;margin-right:8px;">${label}</strong>
         <span style="color:#555;font-size:11px;text-align:right;word-break:break-word;">${value}</span>
       </div>`
    : '';
  return `
    <div style="font-family:'Segoe UI',Arial,sans-serif;width:340px;background:white;border-radius:10px;overflow:hidden;">
      <div style="background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);color:white;padding:12px 14px;">
        <h3 style="margin:0 0 2px 0;font-size:14px;font-weight:700;">${categoryLabel}</h3>
        <p style="margin:0;font-size:11px;opacity:0.85;">${p.facilityName || 'Project name not available'}</p>
      </div>
      <div style="padding:12px 14px;max-height:260px;overflow-y:auto;">
        ${coLocatedNote}
        ${row('Status', statusHtml)}
        ${row('Folio No', p.folioNo)}
        ${row('Developer Name', p.developerName || p.name)}
        ${row('Project / Facility', p.facilityName)}
        ${row('NCRE Category', categoryLabel)}
        ${row('Area Code', p.areaCode)}
        ${row('Area / Province', p.area)}
        ${row('Address', p.address)}
        ${p.coLocatedCount > 1
      ? row('GPS (shared)', `${p.originalLat?.toFixed(5)}, ${p.originalLng?.toFixed(5)}`)
      : row('GPS', `${p.lat?.toFixed(5)}, ${p.lng?.toFixed(5)}`)}
      </div>
    </div>`;
};

// ─── Legend ───────────────────────────────────────────────────────────────────
const Legend = ({
  showConnected, setShowConnected,
  selectedCategory, setSelectedCategory,
  statusFilter, setStatusFilter,
  onlineFolios, statusLoaded,
  plantations = []
}) => {
  const categoryCounts = {};
  plantations.forEach(p => {
    const cat = p.ncreCategory;
    if (cat) {
      categoryCounts[cat] = (categoryCounts[cat] || 0) + 1;
    }
  });

  const ALL_CATEGORIES = [
    { color: '#FFA500', label: 'Solar', key: 'S' },
    { color: '#9C27B0', label: 'Wind Power', key: 'W' },
    { color: '#0066CC', label: 'Mini Hydro', key: 'M' },
    { color: '#228B22', label: 'Biomass', key: 'B' },
    { color: '#8B4513', label: 'Dendro', key: 'D' },
    { color: '#00A896', label: 'Waste to Heat Power', key: 'WHP' },
  ];

  const availableCategories = ALL_CATEGORIES.filter(cat => (categoryCounts[cat.key] || 0) > 0);

  let onlineCount = 0;
  let offlineCount = 0;
  if (statusLoaded) {
    plantations.forEach(p => {
      const normFolio = normalizeFolio(p.folioNo);
      if (normFolio !== '' && onlineFolios.has(normFolio)) {
        onlineCount++;
      } else {
        offlineCount++;
      }
    });
  }

  return (
    <div style={{
      position: 'absolute', top: '16px', right: '16px',
      backgroundColor: 'white', padding: '15px', borderRadius: '8px',
      boxShadow: '0 2px 8px rgba(0,0,0,0.2)', zIndex: 699,
      fontFamily: 'Arial, sans-serif', minWidth: '220px',
    }}>
      <h4 style={{ margin: '0 0 12px 0', fontSize: '14px', fontWeight: 'bold', color: '#333', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span>NCRE Categories ({plantations.length})</span>
        {selectedCategory && (
          <span
            onClick={() => setSelectedCategory(null)}
            style={{ fontSize: '11px', color: '#667eea', cursor: 'pointer', fontWeight: 'normal' }}
          >
            Clear Filter
          </span>
        )}
      </h4>

      {availableCategories.length === 0 ? (
        <div style={{ fontSize: '12px', color: '#888', fontStyle: 'italic', marginBottom: '8px' }}>
          No plantations found.
        </div>
      ) : (
        availableCategories.map(({ color, label, key }) => {
          const count = categoryCounts[key] || 0;
          return (
            <div
              key={key}
              onClick={() => setSelectedCategory(selectedCategory === key ? null : key)}
              style={{
                display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '8px',
                cursor: 'pointer',
                opacity: selectedCategory && selectedCategory !== key ? 0.3 : 1,
                transition: 'all 0.2s ease',
                padding: '4px 6px',
                borderRadius: '4px',
                backgroundColor: selectedCategory === key ? '#f5f7ff' : 'transparent',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center' }}>
                <div
                  style={{ marginRight: '10px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                  dangerouslySetInnerHTML={{ __html: getLegendIconSvg(key) }}
                />
                <span style={{ fontSize: '13px', color: '#333', fontWeight: selectedCategory === key ? '600' : '500' }}>{label}</span>
              </div>
              <span style={{ fontSize: '12px', fontWeight: 'bold', color: '#667eea', marginLeft: '10px' }}>
                ({count})
              </span>
            </div>
          );
        })
      )}

      <hr style={{ margin: '12px 0', border: 'none', borderTop: '1px solid #ddd' }} />

      <div style={{ marginBottom: '12px' }}>
        <h5 style={{ margin: '0 0 8px 0', fontSize: '13px', fontWeight: 'bold', color: '#555' }}>Status Filter</h5>
        <div style={{ display: 'flex', gap: '8px', marginBottom: '4px' }}>
          <button
            onClick={() => setStatusFilter(statusFilter === 'ONLINE' ? 'ALL' : 'ONLINE')}
            style={{
              flex: 1, padding: '6px', fontSize: '11px', fontWeight: 'bold',
              border: statusFilter === 'ONLINE' ? '2px solid #059669' : '1px solid #ddd',
              backgroundColor: statusFilter === 'ONLINE' ? '#ecfdf5' : 'white',
              color: statusFilter === 'ONLINE' ? '#059669' : '#666',
              borderRadius: '4px', cursor: 'pointer', transition: 'all 0.2s'
            }}
          >
            Online {statusLoaded ? `(${onlineCount})` : ''}
          </button>
          <button
            onClick={() => setStatusFilter(statusFilter === 'OFFLINE' ? 'ALL' : 'OFFLINE')}
            style={{
              flex: 1, padding: '6px', fontSize: '11px', fontWeight: 'bold',
              border: statusFilter === 'OFFLINE' ? '2px solid #666' : '1px solid #ddd',
              backgroundColor: statusFilter === 'OFFLINE' ? '#f3f4f6' : 'white',
              color: '#666',
              borderRadius: '4px', cursor: 'pointer', transition: 'all 0.2s'
            }}
          >
            Offline {statusLoaded ? `(${offlineCount})` : ''}
          </button>
        </div>
      </div>

      <hr style={{ margin: '12px 0', border: 'none', borderTop: '1px solid #ddd' }} />
      <div style={{ fontSize: '11px', color: '#888', marginBottom: '12px', display: 'flex', alignItems: 'center', gap: '4px' }}>
        <span style={{ fontSize: '14px', fontWeight: 'bold' }}>+</span> badge = multiple projects at same location
      </div>
      <button
        onClick={() => setShowConnected(!showConnected)}
        style={{
          width: '100%', padding: '10px 12px',
          backgroundColor: showConnected ? '#667eea' : '#f0f0f0',
          color: showConnected ? 'white' : '#333',
          border: 'none', borderRadius: '6px',
          fontSize: '12px', fontWeight: 'bold',
          cursor: 'pointer', transition: 'all 0.3s ease',
          boxShadow: showConnected ? '0 2px 4px rgba(102,126,234,0.4)' : 'none'
        }}
      >
        {showConnected ? '✓ Connected View' : 'Show Connected Lines'}
      </button>
    </div>
  );
};

// ─── Auto-resize handler for map mount / tab switch ──────────────────────────
function MapViewController() {
  const map = useMap();

  useEffect(() => {
    if (!map) return;
    map.setView([7.75, 80.65], 10.0, { animate: false });
    const timer = setTimeout(() => {
      try {
        map.invalidateSize();
        map.setView([7.75, 80.65], 10.0, { animate: false });
      } catch (e) { }
    }, 150);

    return () => clearTimeout(timer);
  }, [map]);

  return null;
}

// ─── Main component ───────────────────────────────────────────────────────────
export default function MapView() {
  const [showConnected, setShowConnected] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [plantations, setPlantations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [drawnPaths, setDrawnPaths] = useState({});
  const [onlineFolios, setOnlineFolios] = useState(new Set());
  const [statusLoaded, setStatusLoaded] = useState(false);
  const intervalRef = useRef(null);
  const statusIntervalRef = useRef(null);

  useEffect(() => {
    loadPlantations();
    loadPlantStatuses();

    // 60-second periodic status refresh only
    statusIntervalRef.current = setInterval(() => {
      loadPlantStatuses();
    }, 60000);

    const handleAreaChange = () => {
      loadPlantations();
    };

    window.addEventListener("areaAndBill:changed", handleAreaChange);
    window.addEventListener("storage", handleAreaChange);

    return () => {
      if (statusIntervalRef.current) clearInterval(statusIntervalRef.current);
      window.removeEventListener("areaAndBill:changed", handleAreaChange);
      window.removeEventListener("storage", handleAreaChange);
    };
  }, []);

  const loadPlantStatuses = async () => {
    try {
      const res = await fetchNCREPlantStatuses();
      if (res && res.success && Array.isArray(res.onlineFolios)) {
        const set = new Set(res.onlineFolios.map(f => normalizeFolio(f)).filter(Boolean));
        setOnlineFolios(set);
        setStatusLoaded(true);
      }
    } catch (err) {
      console.error('Failed to load plant monitoring statuses:', err);
      // If a previous valid status set exists in the frontend during a temporary refresh failure,
      // keep the previous valid status until the next successful refresh.
    }
  };

  useEffect(() => {
    if (intervalRef.current) clearInterval(intervalRef.current);

    const visiblePlantations = plantations.filter(p => {
      if (selectedCategory && p.ncreCategory !== selectedCategory) return false;
      if (statusFilter !== 'ALL') {
        const normFolio = normalizeFolio(p.folioNo);
        const isOnline = statusLoaded && normFolio !== '' && onlineFolios.has(normFolio);
        if (statusFilter === 'ONLINE' && !isOnline) return false;
        if (statusFilter === 'OFFLINE' && isOnline) return false;
      }
      return true;
    });

    if (!showConnected || visiblePlantations.length === 0) { setDrawnPaths({}); return; }
    const groups = {};
    visiblePlantations.forEach((p) => {
      if (!groups[p.ncreCategory]) groups[p.ncreCategory] = [];
      groups[p.ncreCategory].push(p);
    });
    Object.keys(groups).forEach((cat) => { groups[cat].sort((a, b) => b.lat - a.lat); });
    const fullPaths = {};
    Object.entries(groups).forEach(([cat, points]) => {
      if (points.length < 2) return;
      fullPaths[cat] = points.map((p) => [p.lat, p.lng]);
    });
    if (Object.keys(fullPaths).length === 0) return;
    const maxLen = Math.max(...Object.values(fullPaths).map((p) => p.length));
    const init = {};
    Object.keys(fullPaths).forEach((cat) => { init[cat] = [fullPaths[cat][0]]; });
    setDrawnPaths(init);
    let step = 1;
    intervalRef.current = setInterval(() => {
      setDrawnPaths(() => {
        const next = {};
        Object.entries(fullPaths).forEach(([cat, pts]) => {
          next[cat] = pts.slice(0, Math.min(step + 1, pts.length));
        });
        return next;
      });
      step++;
      if (step >= maxLen) clearInterval(intervalRef.current);
    }, 800);
    return () => clearInterval(intervalRef.current);
  }, [showConnected, plantations, selectedCategory, statusFilter, statusLoaded, onlineFolios]);

  const loadPlantations = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await fetchNCRECustomers();
      let raw = transformBackendData(Array.isArray(data) ? data : []);

      const cleanId = (str) => (str || '').replace(/[^a-zA-Z0-9]/g, '').toLowerCase();
      const activeArea = getSelectedAreaCode() || sessionStorage.getItem('selected_area_code') || sessionStorage.getItem('area_code') || sessionStorage.getItem('user_area_code');

      if (activeArea && activeArea !== 'ALL') {
        const targetArea = cleanId(activeArea);
        const targetAreaNum = parseInt(targetArea, 10);
        raw = raw.filter(p => {
          const pAreaCd = cleanId(p.areaCode);
          const pArea = cleanId(p.area);

          const matchByCode = pAreaCd && (
            pAreaCd === targetArea ||
            (!isNaN(targetAreaNum) && parseInt(pAreaCd, 10) === targetAreaNum)
          );

          const matchByName = pArea && (
            pArea === targetArea ||
            pArea === `area${targetArea}` ||
            (!isNaN(targetAreaNum) && parseInt(pArea, 10) === targetAreaNum)
          );

          return Boolean(matchByCode || matchByName);
        });
      }

      const resolved = resolveCoLocated(raw);
      setPlantations(resolved);
    } catch (err) {
      setError('Failed to load locations');
      console.error('Error:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    // ⚠ No overflow:hidden here — that was clipping the popups
    <div style={{ position: 'relative', width: '100%', height: '100%' }}>

      {loading && (
        <div style={{
          position: 'absolute', top: '50%', left: '50%',
          transform: 'translate(-50%, -50%)',
          backgroundColor: 'white', padding: '20px',
          borderRadius: '8px', zIndex: 1500, textAlign: 'center',
        }}>
          <p>Loading locations…</p>
        </div>
      )}

      {error && (
        <div style={{
          position: 'absolute', top: '10px', left: '10px',
          backgroundColor: '#fee', color: '#c33',
          padding: '15px', borderRadius: '8px',
          zIndex: 1500, maxWidth: '300px',
        }}>
          {error}
        </div>
      )}

      <MapContainer
        center={[7.75, 80.65]} zoom={10.0}
        zoomControl={false}
        zoomSnap={0.1} zoomDelta={0.5}
        maxZoom={16} minZoom={6}
        maxBounds={sriLankaBounds} maxBoundsViscosity={1.0}
        style={{ width: '100%', height: '100%' }}
      >
        <MapViewController />
        <ZoomControl position="bottomright" />

        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          noWrap={true} bounds={sriLankaBounds}
        />

        {showConnected && Object.entries(drawnPaths).map(([cat, positions]) =>
          positions.length >= 2 ? (
            <Polyline
              key={cat} positions={positions}
              color={CATEGORY_COLORS[cat] || '#999999'}
              weight={2.5} opacity={0.85} dashArray="8, 4"
            />
          ) : null
        )}

        {(() => {
          const visiblePlantations = plantations.filter(p => {
            if (selectedCategory && p.ncreCategory !== selectedCategory) return false;
            if (statusFilter !== 'ALL') {
              const normFolio = normalizeFolio(p.folioNo);
              const isOnline = statusLoaded && normFolio !== '' && onlineFolios.has(normFolio);
              if (statusFilter === 'ONLINE' && !isOnline) return false;
              if (statusFilter === 'OFFLINE' && isOnline) return false;
            }
            return true;
          });

          return visiblePlantations.map((p, i) => {
            const isTop = p.lat > 8.0;
            const tooltipDir = isTop ? 'bottom' : 'top';
            const tooltipOffset = isTop ? [0, 10] : [0, -10];

            const normFolio = normalizeFolio(p.folioNo);
            const isOnline = statusLoaded && normFolio !== '' && onlineFolios.has(normFolio);
            const statusLabel = !statusLoaded ? 'Unavailable' : (isOnline ? 'Online' : 'Offline');

            return (
              <Marker
                key={`${p.accountNumber}-${i}`}
                position={[p.lat, p.lng]}
                icon={createPlantMarkerIcon(p, isTop, isOnline)}
              >
                <Tooltip permanent={false} direction={tooltipDir} offset={tooltipOffset} opacity={0.95}>
                  <div style={{ fontSize: '11px', lineHeight: '1.6' }}>
                    <strong>{CATEGORY_LABELS[p.ncreCategory] || 'UNKNOWN'}</strong>
                    {p.coLocatedCount > 1 && (
                      <span style={{ marginLeft: '6px', color: '#e67e22', fontWeight: 'bold' }}>
                        [{p.coLocatedIndex}/{p.coLocatedCount}]
                      </span>
                    )}
                    <br />
                    <strong>Status:</strong>{' '}
                    <span style={{ color: isOnline ? '#059669' : '#666', fontWeight: 'bold' }}>
                      {statusLabel}
                    </span>
                    <br />
                    <strong>Folio No:</strong> {p.folioNo}<br />
                    <strong>Developer:</strong> {(p.developerName || p.name).substring(0, 28)}<br />
                    {p.facilityName && <span><strong>Project:</strong> {p.facilityName.substring(0, 28)}<br /></span>}
                    <strong>Area:</strong> {p.areaCode} {p.area ? `· ${p.area}` : ''}
                  </div>
                </Tooltip>
                <Popup maxWidth={360}>
                  <div dangerouslySetInnerHTML={{ __html: createPopupContent(p, isOnline, statusLoaded) }} />
                </Popup>
              </Marker>
            );
          });
        })()}
      </MapContainer>

      <Legend
        showConnected={showConnected}
        setShowConnected={setShowConnected}
        selectedCategory={selectedCategory}
        setSelectedCategory={setSelectedCategory}
        statusFilter={statusFilter}
        setStatusFilter={setStatusFilter}
        onlineFolios={onlineFolios}
        statusLoaded={statusLoaded}
        plantations={plantations}
      />
    </div>
  );
}