import React, { useState, useRef } from "react";
import PropTypes from "prop-types";
import { MapContainer, TileLayer, Marker, useMapEvents, useMap } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";
import iconUrl from "leaflet/dist/images/marker-icon.png";
import iconShadow from "leaflet/dist/images/marker-shadow.png";

const DefaultIcon = L.icon({
  iconUrl,
  shadowUrl: iconShadow,
  iconAnchor: [12, 41],
});
L.Marker.prototype.options.icon = DefaultIcon;

const SRI_LANKA_CENTER = [7.8731, 80.7718];
const SRI_LANKA_BOUNDS = [
  [5.85, 79.4],   // Southwest
  [9.85, 82.2],   // Northeast
];

function LocationMarker({ position, setPosition }) {
  useMapEvents({
    click(e) {
      setPosition([e.latlng.lat, e.latlng.lng]);
    },
  });
  return position ? <Marker position={position} /> : null;
}

const MapPickerModal = ({ isOpen, onClose, onSave, initialPosition }) => {
  const [position, setPosition] = useState(initialPosition || null);
  const [latInput, setLatInput] = useState(initialPosition ? initialPosition[0].toFixed(6) : "");
  const [lngInput, setLngInput] = useState(initialPosition ? initialPosition[1].toFixed(6) : "");
  const [inputError, setInputError] = useState("");
  const mapRef = useRef();

  const handleMapClick = (lat, lng) => {
    setPosition([lat, lng]);
    setLatInput(lat.toFixed(6));
    setLngInput(lng.toFixed(6));
    setInputError("");
  };

  const isValidLatLng = (lat, lng) => {
    return (
      !isNaN(lat) && !isNaN(lng) &&
      lat >= -90 && lat <= 90 &&
      lng >= -180 && lng <= 180
    );
  };

  const handleSeeLocation = () => {
    const lat = parseFloat(latInput);
    const lng = parseFloat(lngInput);
    if (!isValidLatLng(lat, lng)) {
      setInputError("Please enter valid latitude and longitude values.");
      return;
    }
    setPosition([lat, lng]);
    setInputError("");
    if (mapRef.current) {
      mapRef.current.setView([lat, lng], 14, { animate: true });
    }
  };

  const handleSave = () => {
    const lat = parseFloat(latInput);
    const lng = parseFloat(lngInput);
    if (!isValidLatLng(lat, lng)) {
      setInputError("Please enter valid latitude and longitude values.");
      return;
    }
    setInputError("");
    onSave([lat, lng]);
  };

  function MapWithRef() {
    const map = useMap();
    React.useEffect(() => {
      mapRef.current = map;
    }, [map]);
    return null;
  }

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Overlay */}
      <div className="fixed inset-0 bg-ink-900 opacity-60" aria-hidden="true"></div>

      {/* Modal */}
      <div className="ds-modal max-w-4xl" style={{ maxHeight: '95vh' }}>
        {/* Scrollable content */}
        <div className="px-4 pt-5 pb-2 sm:p-6 sm:pb-2 flex-1 overflow-y-auto">
          <h3 className="text-lg font-semibold mb-2 text-ink-800">Select Location on Map</h3>
          <div className="mb-2 text-sm text-ink-600">Click on the map to select the exact location for this customer, or enter coordinates below.</div>
          {/* Manual input fields */}
          <div className="flex flex-col sm:flex-row sm:items-end gap-2 mb-4">
            <div className="flex flex-col">
              <label className="text-xs text-ink-700 mb-1">Latitude</label>
              <input
                type="text"
                className="border border-ink-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-2 focus:ring-navy-200"
                value={latInput}
                onChange={e => setLatInput(e.target.value)}
                placeholder="Latitude"
              />
            </div>
            <div className="flex flex-col">
              <label className="text-xs text-ink-700 mb-1">Longitude</label>
              <input
                type="text"
                className="border border-ink-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-2 focus:ring-navy-200"
                value={lngInput}
                onChange={e => setLngInput(e.target.value)}
                placeholder="Longitude"
              />
            </div>
            <button
              type="button"
              onClick={handleSeeLocation}
              className="bg-navy-500 hover:bg-navy-600 text-white px-3 py-2 rounded text-xs font-medium transition-colors duration-200 focus:outline-none mt-4 sm:mt-0 sm:ml-2"
            >
              See Location
            </button>
          </div>
          {inputError && <div className="text-xs text-critical-600 mb-2">{inputError}</div>}
          <div className="w-full" style={{ height: '380px', borderRadius: '0.5rem', overflow: 'hidden' }}>
            <MapContainer
              center={position || SRI_LANKA_CENTER}
              zoom={position ? 14 : 7}
              minZoom={7}
              maxBounds={SRI_LANKA_BOUNDS}
              style={{ height: "100%", width: "100%" }}
              scrollWheelZoom={true}
              whenCreated={mapInstance => { mapRef.current = mapInstance; }}
            >
              <MapWithRef />
              <TileLayer
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                attribution="&copy; OpenStreetMap contributors"
              />
              <LocationMarker
                position={position}
                setPosition={(pos) => {
                  handleMapClick(pos[0], pos[1]);
                }}
              />
            </MapContainer>
          </div>
          {position && (
            <div className="mt-3 text-xs text-ink-700">
              <strong>Selected:</strong> Latitude: {position[0].toFixed(6)}, Longitude: {position[1].toFixed(6)}
            </div>
          )}
        </div>

        {/* Buttons always visible at bottom */}
        <div className="bg-ink-50 px-4 py-3 sm:px-6 flex flex-row-reverse gap-2 rounded-b-lg border-t border-ink-200 flex-shrink-0">
          <button
            type="button"
            onClick={handleSave}
            className="inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-navy-600 text-base font-medium text-white hover:bg-navy-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-navy-500 sm:w-auto sm:text-sm disabled:opacity-50"
            disabled={!latInput || !lngInput}
          >
            Save Location
          </button>
          <button
            type="button"
            onClick={onClose}
            className="inline-flex justify-center rounded-md border border-ink-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-ink-700 hover:bg-ink-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-navy-500 sm:w-auto sm:text-sm"
          >
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
};

MapPickerModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  onSave: PropTypes.func.isRequired,
  initialPosition: PropTypes.array,
};

export default MapPickerModal;