import React, { useState, useEffect } from 'react';
import { getNcreTariffDescriptions, getNcreTypes, getNcreAgreementTypes } from 'services/developerRegistrationService';
import {
  getAllProvinces,
  transformProvincesToOptions,
} from 'services/locationService';

const provinceOptions = [];

const initialFormState = {
  folioNumber: '',
  projectName: '',
  area: '',
  fileReferenceNo: '',
  province: '',
  loiIssued: '',
  sppaSignedDate: '',
  gridConnectionDate: '',
  referenceCode: '',
  region: '',
  srNo: '',
  type: '',
  gridSubstation: '',
  ncreType: '',
  initialTariff: '',
  tariffType: '',
  expirationDate: '',
  expirationExtensionDate: '',
  feederNo: '',
  commissionedYear: '',
  commissionedCapacityMw: '',
  sppaSignedCapacityMw: '',
  acExpirationWithExtension: '',
  exDate: '',
  acDate: '',
  flatDate: '',
  tttDate: '',
  firstTierDate: '',
  secondTierDate: '',
  thirdTierDate: '',
  newSppaSigned: '',
  validityStart: '',
  validityExpiry: '',
  initialTariffRevised: '',
  recommissionedOn: '',
  epExpired: '',
  glExpired: '',
  projectStatus: '',
  voltageLevelKv: '',
  addressLine1: '',
  addressLine2: '',
  addressLine3: '',
  contactPerson: '',
  reductions: '',
  agreementType: '',
  longitude: '',
  latitude: '',
};

const projectSections = [
  {
    title: 'Reference Information',
    fields: [
      { name: 'folioNumber', label: 'Folio Number', type: 'text', placeholder: 'Enter folio number' },
      { name: 'projectName', label: 'Project Name', type: 'text', placeholder: 'Enter project name' },
      { name: 'fileReferenceNo', label: 'File Reference No', type: 'text', placeholder: 'Enter file reference no' },
      { name: 'province', label: 'Province', type: 'select', options: provinceOptions, placeholder: 'Select province' },
      { name: 'loiIssued', label: 'LOI Issued', type: 'text', placeholder: 'Enter LOI issued value' },
      { name: 'sppaSignedDate', label: 'SPPA/PPA Signed Date', type: 'date' },
      { name: 'gridConnectionDate', label: 'Grid Connection Date/Commissioned Date', type: 'date' },
      { name: 'expirationDate', label: 'Expiration Date', type: 'date' },
      { name: 'referenceCode', label: 'Reference Code', type: 'text', placeholder: 'Enter reference code' },
      { name: 'type', label: 'Type', type: 'text', placeholder: 'Enter type' },
      { name: 'gridSubstation', label: 'Grid Substation', type: 'text', placeholder: 'Enter grid substation' },
      { name: 'ncreType', label: 'NCRE Type', type: 'select', options: [], placeholder: 'Select NCRE type' },
    ],
  },
  {
    title: 'Location',
    fields: [
      { name: 'area', label: 'Area', type: 'text', placeholder: 'Enter area' },
      { name: 'longitude', label: 'Longitude', type: 'number', step: '0.01', placeholder: 'Enter longitude' },
      { name: 'latitude', label: 'Latitude', type: 'number', step: '0.01', placeholder: 'Enter latitude' },
      { name: 'region', label: 'Region', type: 'select', options: regionOptions, placeholder: 'Select region' },
      { name: 'srNo', label: 'SR No', type: 'text', placeholder: 'Enter SR No' },
    ],
  },
  {
    title: 'Tariff and Grid Details',
    fields: [
      { name: 'initialTariff', label: 'Initial Tariff', type: 'text', placeholder: 'Enter initial tariff' },
      { name: 'tariffType', label: 'Tariff Type', type: 'select', options: [], placeholder: 'Select tariff type' },
      { name: 'expirationExtensionDate', label: 'Expiration Extension Date', type: 'date' },
      { name: 'feederNo', label: 'Feeder No', type: 'text', placeholder: 'Enter feeder no' },
      { name: 'commissionedYear', label: 'Commissioned Year', type: 'number', step: '1', min: '0', placeholder: 'Enter commissioned year' },
      { name: 'commissionedCapacityMw', label: 'Commissioned Capacity (MW)', type: 'number', step: '0.01', min: '0', placeholder: 'Enter commissioned capacity' },
      { name: 'sppaSignedCapacityMw', label: 'SPPA/PPA Signed Capacity (MW)', type: 'number', step: '0.01', min: '0', placeholder: 'Enter SPPA signed capacity' },

      { name: 'tttDate', label: 'TTT', type: 'date' },
      { name: 'firstTierDate', label: 'First Tier', type: 'date' },
      { name: 'secondTierDate', label: 'Second Tier', type: 'date' },
      { name: 'thirdTierDate', label: 'Third Tier', type: 'date' },
      { name: 'newSppaSigned', label: 'New SPPA Signed', type: 'text', placeholder: 'Enter new SPPA signed value' },

      { name: 'initialTariffRevised', label: 'Initial Tariff Revised', type: 'number', step: '0.01', placeholder: 'Enter revised tariff' },
      { name: 'recommissionedOn', label: 'Recommissioned On', type: 'date' },
      { name: 'epExpired', label: 'Energy Permit Expiration Date', type: 'date' },
      { name: 'glExpired', label: 'Generation License Expiration Date', type: 'date' },

      { name: 'voltageLevelKv', label: 'Voltage Level (KV)', type: 'number', step: '0.01', placeholder: 'Enter voltage level' },
    ],
  },
  {
    title: 'Commercial Details',
    fields: [
      { name: 'addressLine1', label: 'Address Line 1', type: 'text', placeholder: 'Enter address line 1' },
      { name: 'addressLine2', label: 'Address Line 2', type: 'text', placeholder: 'Enter address line 2' },
      { name: 'addressLine3', label: 'Address Line 3', type: 'text', placeholder: 'Enter address line 3' },
      { name: 'contactPerson', label: 'Contact Person', type: 'text', placeholder: 'Enter contact person' },
      { name: 'agreementType', label: 'Agreement Type', type: 'radio', options: [] },
    ],
  },
];

const inputClassName = 'ds-input';
const numericFieldNames = new Set(['commissionedYear', 'commissionedCapacityMw', 'sppaSignedCapacityMw', 'initialTariffRevised', 'voltageLevelKv', 'longitude', 'latitude']);

export default function ProjectCreationForm({ onSubmit }) {
  const [formData, setFormData] = useState(initialFormState);
  const [errors, setErrors] = useState({});
  const [tariffTypeOptions, setTariffTypeOptions] = useState([]);
  const [tariffsLoading, setTariffsLoading] = useState(true);
  const [tariffsError, setTariffsError] = useState(false);
  const [ncreTypeOptions, setNcreTypeOptions] = useState([]);
  const [ncreTypesLoading, setNcreTypesLoading] = useState(true);
  const [agreementTypeOptions, setAgreementTypeOptions] = useState([]);
  const [agreementTypesLoading, setAgreementTypesLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;
    const fetchAgreementTypes = async () => {
      try {
        const types = await getNcreAgreementTypes();
        if (isMounted) {
          setAgreementTypeOptions((types || []).map((t) => ({
            value: t.aggTypeId,
            label: `${t.aggTypeId}(${t.aggTypeName})`,
          })));
          setAgreementTypesLoading(false);
        }
      } catch (error) {
        console.error("Failed to load agreement types:", error);
        if (isMounted) {
          setAgreementTypeOptions([]);
          setAgreementTypesLoading(false);
        }
      }
    };
    fetchAgreementTypes();
    return () => {
      isMounted = false;
    };
  }, []);

  useEffect(() => {
    let isMounted = true;
    const fetchNcreTypes = async () => {
      try {
        const types = await getNcreTypes();
        if (isMounted) {
          setNcreTypeOptions((types || []).map((t) => ({
            value: t.typeId,
            label: t.typeName,
          })));
          setNcreTypesLoading(false);
        }
      } catch (error) {
        console.error("Failed to load NCRE types:", error);
        if (isMounted) {
          setNcreTypeOptions([]);
          setNcreTypesLoading(false);
        }
      }
    };
    fetchNcreTypes();
    return () => {
      isMounted = false;
    };
  }, []);

  const [provincesLoading, setProvincesLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;
    const fetchProvinces = async () => {
      setProvincesLoading(true);
      try {
        const provinces = await getAllProvinces();
        if (isMounted) {
          provinceOptions.length = 0;
          provinceOptions.push(...transformProvincesToOptions(provinces));
          setProvincesLoading(false);
        }
      } catch (error) {
        if (isMounted) {
          console.error('Failed to load provinces:', error);
          provinceOptions.length = 0;
          setProvincesLoading(false);
        }
      }
    };

    fetchProvinces();
    return () => {
      isMounted = false;
    };
  }, []);

  useEffect(() => {
    let isMounted = true;
    const fetchTariffs = async () => {
      setTariffsLoading(true);
      setTariffsError(false);
      try {
        const descriptions = await getNcreTariffDescriptions();
        if (isMounted) {
          setTariffTypeOptions(descriptions || []);
          setTariffsLoading(false);
        }
      } catch (error) {
        if (isMounted) {
          console.error("Failed to load tariff descriptions:", error);
          setTariffTypeOptions([]);
          setTariffsLoading(false);
          setTariffsError(true);
        }
      }
    };
    fetchTariffs();
    return () => {
      isMounted = false;
    };
  }, []);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setFormData((current) => ({
      ...current,
      [name]: value,
    }));

    if (errors[name]) {
      setErrors((current) => ({
        ...current,
        [name]: '',
      }));
    }
  };

  const validate = () => {
    const nextErrors = {};

    projectSections.forEach((section) => {
      section.fields.forEach((field) => {
        const value = String(formData[field.name] ?? '');

        if (!value.trim()) {
          nextErrors[field.name] = `${field.label} is required.`;
          return;
        }

        if (numericFieldNames.has(field.name)) {
          const numericValue = Number(value);

          if (Number.isNaN(numericValue)) {
            nextErrors[field.name] = `${field.label} must be a valid number.`;
            return;
          }

          if (field.name === 'commissionedYear' && numericValue < 0) {
            nextErrors[field.name] = 'Commissioned Year must be zero or greater.';
          }
        }
      });
    });

    return nextErrors;
  };

  const handleSubmit = (event) => {
    event.preventDefault();

    const nextErrors = validate();
    setErrors(nextErrors);

    if (Object.keys(nextErrors).length > 0) {
      return;
    }

    if (onSubmit) {
      onSubmit(formData);
    }
  };

  const renderField = (field) => {
    if (field.type === 'select') {
      const selectOptions = field.name === 'tariffType' ? tariffTypeOptions : (field.name === 'ncreType' ? ncreTypeOptions : (field.options || []));
      const placeholderText = field.name === 'tariffType'
        ? (tariffsLoading ? 'Loading tariff types...' : tariffsError ? 'Failed to load tariffs' : field.placeholder || 'Select tariff type')
        : field.name === 'ncreType'
          ? (ncreTypesLoading ? 'Loading NCRE types...' : field.placeholder || 'Select NCRE type')
          : (field.placeholder || `Select ${field.label ? field.label.toLowerCase() : 'an option'}`);

      return (
        <select
          id={field.name}
          name={field.name}
          value={formData[field.name]}
          onChange={handleChange}
          disabled={(field.name === 'tariffType' && tariffsLoading) || (field.name === 'ncreType' && ncreTypesLoading)}
          className={inputClassName}
        >
          <option value="">{placeholderText}</option>
          {selectOptions.map((option) => {
            const isObj = typeof option === 'object' && option !== null;
            const val = isObj ? option.value : option;
            const label = isObj ? option.label : option;
            return (
              <option key={val} value={val}>
                {label}
              </option>
            );
          })}
        </select>
      );
    }

    if (field.type === 'radio') {
      const radioOptions = field.name === 'agreementType' ? agreementTypeOptions : (field.options || []);
      return (
        <div className="flex flex-wrap gap-4 pt-1">
          {agreementTypesLoading && field.name === 'agreementType' && (
            <span className="text-sm text-ink-400">Loading agreement types...</span>
          )}
          {radioOptions.map((option) => {
            const isObject = typeof option === 'object' && option !== null;
            const optValue = isObject ? option.value : option;
            const optLabel = isObject ? option.label : option;
            return (
              <label key={optValue} className="inline-flex items-center gap-2 text-sm text-ink-700">
                <input
                  type="radio"
                  name={field.name}
                  value={optValue}
                  checked={formData[field.name] === optValue}
                  onChange={handleChange}
                  className="h-4 w-4 border-ink-300 text-navy-600 focus:ring-navy-500"
                />
                {optLabel}
              </label>
            );
          })}
        </div>
      );
    }

    return (
      <input
        id={field.name}
        name={field.name}
        type={field.type}
        step={field.step}
        min={field.min}
        value={formData[field.name]}
        onChange={handleChange}
        className={inputClassName}
        placeholder={field.placeholder}
      />
    );
  };

  return (
    <form className="rounded-lg border" onSubmit={handleSubmit} noValidate>

      <section className="rounded-lg border border-ink-100 bg-ink-50/70 p-4">
        <div className="mb-4">
          <h5 className="text-sm font-semibold text-ink-700">Project Information</h5>
          <p className="text-sm text-ink-500">Complete the project metadata below.</p>
        </div>

        <div className="space-y-6">
          {projectSections.map((section) => (
            <section key={section.title} className="rounded-lg border border-ink-100 bg-white p-4">
              <h6 className="mb-4 text-sm font-semibold text-ink-700">{section.title}</h6>
              <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
                {section.fields.map((field) => (
                  <div key={field.name} className={`flex flex-col justify-end h-full ${field.type === 'radio' ? 'md:col-span-2 xl:col-span-3' : ''}`}>
                    <label className="mb-1 block text-sm font-medium text-ink-700" htmlFor={field.type === 'radio' ? undefined : field.name}>
                      {field.label}
                    </label>
                    <div className={field.type !== 'radio' ? 'mt-auto' : ''}>
                      {renderField(field)}
                      {errors[field.name] && <p className="mt-1 text-xs text-critical-600">{errors[field.name]}</p>}
                    </div>
                  </div>
                ))}
              </div>
            </section>
          ))}
        </div>
      </section>

      <div className="mt-6 flex justify-end">
        <button
          type="submit"
          className="rounded-md bg-navy-500 px-5 py-2.5 text-sm font-medium text-white transition-colors duration-150 hover:bg-navy-600"
        >
          Create Project
        </button>
      </div>
    </form>
  );
}
