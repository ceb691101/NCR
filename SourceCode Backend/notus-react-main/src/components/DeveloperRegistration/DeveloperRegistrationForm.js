import React, { useState, useEffect, useRef, useMemo } from "react";
import "./developer-registration.css";
import { toast } from "react-toastify";
import { getNcreTariffDescriptions, getNcreTypes, getNcreAgreementTypes, getGridSubstations, getPaymentDeductionTypes } from "services/developerRegistrationService";
import { apiPath, getAuthHeaders } from "../../config";
import {
  getAllRegions,
  getProvincesByRegion,
  getAreasByRegionAndProvince,
  transformRegionsToOptions,
  transformProvincesToOptions,
  transformAreasToOptions,
} from "services/locationService";

const initialAgreementState = {
  id: Date.now(),
  initialTariff: "",
  voltageLevelKv: "",
  generationLosses: "",
  paymentDeductions: [],
  addendums: [],
  tariffFiles: [],
  agreementFiles: [],
};

const initialFormState = {
  // Developer Information
  developerName: "",
  groupOfCompany: "",
  email: "",
  phone: "",
  addressLine1: "",
  addressLine2: "",
  addressLine3: "",
  contactPerson: "",
  // Project Information
  folioNumber: '', accountNumber: '', projectName: '', area: '',
  fileReferenceNo: '', province: '', loiIssued: '', sppaSignedDate: '',
  gridConnectionDate: '', expirationDate: '', referenceCode: '',
  region: '', srNo: '', gridSubstation: '', ncreType: '', feederNo: '', meterNo: '',
  longitude: '', latitude: '', reductions: '', responsibleEe: '',
  status: '2',
  // Tariff Details (Global Project Details)
  agreementType: "",
  tariffType: "",
  commissionedYear: "",
  commissionedCapacityMw: "",
  sppaSignedCapacityMw: "",
  acceptRu: "",
  epExpired: "",
  glExpired: "",
  firstTierDate: "",
  secondTierDate: "",
  thirdTierDate: "",

  // Agreements
  agreements: [{ ...initialAgreementState, id: Date.now() }],
};

const developerFields = [
  {
    name: "developerName",
    label: "Developer name",
    type: "text",
    placeholder: "Enter developer name",
  },
  {
    name: "groupOfCompany",
    label: "Group of company",
    type: "text",
    placeholder: "Enter group of company",
  },
  {
    name: "contactPerson",
    label: "Contact Person",
    type: "text",
    placeholder: "Enter contact person",
  },
  {
    name: "email",
    label: "Email",
    type: "email",
    placeholder: "Enter email address",
  },
  {
    name: "phone",
    label: "Phone",
    type: "tel",
    placeholder: "Enter phone number",
  },
  {
    name: "addressLine1",
    label: "Address Line 1",
    type: "text",
    placeholder: "Enter address line 1",
  },
  {
    name: "addressLine2",
    label: "Address Line 2",
    type: "text",
    placeholder: "Enter address line 2",
  },
  {
    name: "addressLine3",
    label: "Address Line 3",
    type: "text",
    placeholder: "Enter address line 3",
  },
];

const projectSections = [
  {
    title: "Reference Information",
    fields: [
      { name: 'folioNumber', label: 'Folio number', type: 'text', placeholder: 'Enter folio number' },
      { name: 'projectName', label: 'Name Of The Facility', type: 'text', placeholder: 'Enter facility name' },
      { name: 'accountNumber', label: 'Bulk Supply Account Number', type: 'text', placeholder: 'Enter account number' },
      { name: 'meterNo', label: 'Meter No', type: 'text', placeholder: 'Enter meter number' },
      { name: 'fileReferenceNo', label: 'File Reference No (RE&ESCP)', type: 'text', placeholder: 'Enter file reference no' },
      { name: 'loiIssued', label: 'LOI Issued', type: 'date' },
      { name: 'sppaSignedDate', label: 'SPPA/PPA Signed Date', type: 'date' },
      { name: 'gridConnectionDate', label: 'Grid Connection Date/Commissioned Date', type: 'date' },
      { name: 'expirationDate', label: 'Expiration Date', type: 'date' },
      { name: 'referenceCode', label: 'Reference Code', type: 'text', placeholder: 'Enter reference code' },
      { name: 'ncreType', label: 'NCRE Type', type: 'select', options: [], placeholder: 'Select NCRE type' },
      { name: 'feederNo', label: 'Feeder No', type: 'text', placeholder: 'Enter feeder no' },
      { name: 'responsibleEe', label: 'Responsible Electrical Engineer', type: 'select', options: [], placeholder: 'Select responsible EE' },
    ],
  },
  {
    title: "Location",
    fields: [
      {
        name: "region",
        label: "Region",
        type: "select",
        placeholder: "Select region",
      },
      {
        name: "province",
        label: "Province",
        type: "select",
        placeholder: "Select province",
      },
      {
        name: "area",
        label: "Area",
        type: "select",
        placeholder: "Select area",
      },
      {
        name: "gridSubstation",
        label: "Grid Substation",
        type: "select",
        placeholder: "Select grid substation",
      },
      {
        name: "longitude",
        label: "Longitude",
        type: "number",
        min: 79.5,
        max: 81.9,
        step: "any",
        placeholder: "Enter longitude",
      },
      {
        name: "latitude",
        label: "Latitude",
        type: "number",
        min: 5.9,
        max: 9.9,
        step: "any",
        placeholder: "Enter latitude",
      },
      {
        name: "srNo",
        label: "Region Serial Number",
        type: "text",
        placeholder: "Enter Region Serial No",
      },
    ],
  },
];

const requiredFieldNames = new Set([
  "developerName",
  "accountNumber",
  "folioNumber",
  "tariffType",
  "projectName",
  "meterNo",
  "gridConnectionDate",
  "ncreType",
  "commissionedCapacityMw",
  "sppaSignedCapacityMw",
  "acceptRu",
  "longitude",
  "latitude",
]);

const inputClassName =
  "w-full rounded-xl border border-ink-200 px-3.5 py-2.5 text-sm focus:border-navy-800 focus:outline-none focus:ring-2 focus:ring-navy-800/20 transition-all duration-200 shadow-xs placeholder-ink-400 font-medium text-ink-700 bg-white";

export default function DeveloperRegistrationForm({
  onSubmit,
  loading = false,
  initialData = {},
  isEditMode = false,
  onCancel,
}) {
  const [formData, setFormData] = useState(() => ({
    ...initialFormState,
    ...initialData,
    agreements: (initialData && initialData.agreements && initialData.agreements.length > 0)
      ? initialData.agreements
      : [{ ...initialAgreementState, id: Date.now() }]
  }));

  const prevInitialDataRef = useRef(initialData);

  useEffect(() => {
    if (initialData && Object.keys(initialData).length > 0 && initialData !== prevInitialDataRef.current) {
      prevInitialDataRef.current = initialData;
      setFormData({
        ...initialFormState,
        ...initialData,
        agreements: (initialData.agreements && initialData.agreements.length > 0)
          ? initialData.agreements
          : [{ ...initialAgreementState, id: Date.now() }]
      });
    }
  }, [initialData]);
  const [submitting, setSubmitting] = useState(false);
  const [tariffTypeOptions, setTariffTypeOptions] = useState([]);
  const [tariffsLoading, setTariffsLoading] = useState(true);
  const [tariffsError, setTariffsError] = useState(false);
  const [ncreTypeOptions, setNcreTypeOptions] = useState([]);
  const [ncreTypesLoading, setNcreTypesLoading] = useState(true);

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
            label: `${t.aggTypeId} (${t.aggTypeName})`,
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
  const [paymentDeductionOptions, setPaymentDeductionOptions] = useState([]);
  const [paymentDeductionTypesLoading, setPaymentDeductionTypesLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;
    const fetchPaymentDeductionTypes = async () => {
      try {
        const types = await getPaymentDeductionTypes();
        if (isMounted) {
          setPaymentDeductionOptions((types || []).map((t) => t.dedTypeNm));
          setPaymentDeductionTypesLoading(false);
        }
      } catch (error) {
        console.error("Failed to load payment deduction types:", error);
        if (isMounted) {
          setPaymentDeductionOptions([]);
          setPaymentDeductionTypesLoading(false);
        }
      }
    };
    fetchPaymentDeductionTypes();
    return () => {
      isMounted = false;
    };
  }, []);
  const [regionOptions, setRegionOptions] = useState([]);
  const [provinceOptions, setProvinceOptions] = useState([]);
  const [areaOptions, setAreaOptions] = useState([]);
  const [gridSubstationOptions, setGridSubstationOptions] = useState([]);
  const [gridSubstationsLoading, setGridSubstationsLoading] = useState(false);
  const [locationLoading, setLocationLoading] = useState(false);

  const loadRegions = async () => {
    setLocationLoading(true);
    try {
      const regions = await getAllRegions();
      setRegionOptions(transformRegionsToOptions(regions));
    } catch (error) {
      console.error("Failed to load regions:", error);
      toast.error("Failed to load regions from server");
      setRegionOptions([]);
    } finally {
      setLocationLoading(false);
    }
  };

  const loadProvinces = async (regionCode) => {
    if (!regionCode) {
      setProvinceOptions([]);
      setAreaOptions([]);
      return;
    }
    setLocationLoading(true);
    try {
      const provinces = await getProvincesByRegion(regionCode);
      setProvinceOptions(transformProvincesToOptions(provinces));
      setAreaOptions([]);
    } catch (error) {
      console.error("Failed to load provinces:", error);
      toast.error("Failed to load provinces from server");
      setProvinceOptions([]);
      setAreaOptions([]);
    } finally {
      setLocationLoading(false);
    }
  };

  const loadAreas = async (regionCode, provinceCode) => {
    if (!regionCode || !provinceCode) {
      setAreaOptions([]);
      return;
    }
    setLocationLoading(true);
    try {
      const areas = await getAreasByRegionAndProvince(regionCode, provinceCode);
      setAreaOptions(transformAreasToOptions(areas));
    } catch (error) {
      console.error("Failed to load areas:", error);
      toast.error("Failed to load areas from server");
      setAreaOptions([]);
    } finally {
      setLocationLoading(false);
    }
  };

  const loadGridSubstations = async (licenseCode) => {
    if (!licenseCode) {
      setGridSubstationOptions([]);
      setGridSubstationsLoading(false);
      return;
    }
    setGridSubstationsLoading(true);
    try {
      const substations = await getGridSubstations(licenseCode);
      setGridSubstationOptions((substations || []).map((s) => ({
        value: s.gssName,
        label: `${s.gssName} (${s.gssCode})`,
      })));
    } catch (error) {
      console.error("Failed to load grid substations:", error);
      toast.error("Failed to load grid substations from server");
      setGridSubstationOptions([]);
    } finally {
      setGridSubstationsLoading(false);
    }
  };

  useEffect(() => {
    loadRegions();
  }, []);

  useEffect(() => {
    const region = initialData && initialData.region ? initialData.region : "";
    const province = initialData && initialData.province ? initialData.province : "";
    if (region) {
      loadProvinces(region);
      loadGridSubstations(region);
      if (province) {
        loadAreas(region, province);
      }
    }
  }, [initialData]);

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
          toast.error("Failed to load tariff descriptions from server");
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

  const effectiveTariffOptions = useMemo(() => {
    const options = [...tariffTypeOptions];
    if (isEditMode && formData.tariffType && formData.tariffType.trim() !== "") {
      const exists = options.some((opt) => {
        const val = typeof opt === "object" && opt !== null ? opt.value : opt;
        return String(val).trim().toLowerCase() === String(formData.tariffType).trim().toLowerCase();
      });
      if (!exists) {
        options.push({
          value: formData.tariffType,
          label: `${formData.tariffType} (Inactive)`,
        });
      }
    }
    return options;
  }, [tariffTypeOptions, isEditMode, formData.tariffType]);

  const effectiveNcreTypeOptions = useMemo(() => {
    const options = [...ncreTypeOptions];
    if (isEditMode && formData.ncreType && formData.ncreType.trim() !== "") {
      const exists = options.some((opt) => {
        const val = typeof opt === "object" && opt !== null ? opt.value : opt;
        return String(val).trim().toLowerCase() === String(formData.ncreType).trim().toLowerCase();
      });
      if (!exists) {
        options.push({
          value: formData.ncreType,
          label: `${formData.ncreType} (Inactive)`,
        });
      }
    }
    return options;
  }, [ncreTypeOptions, isEditMode, formData.ncreType]);

  const effectiveAgreementTypeOptions = useMemo(() => {
    const options = [...agreementTypeOptions];
    if (isEditMode && formData.agreementType && formData.agreementType.trim() !== "") {
      const exists = options.some((opt) => {
        const val = typeof opt === "object" && opt !== null ? opt.value : opt;
        return String(val).trim().toLowerCase() === String(formData.agreementType).trim().toLowerCase();
      });
      if (!exists) {
        options.push({
          value: formData.agreementType,
          label: `${formData.agreementType} (Inactive)`,
        });
      }
    }
    return options;
  }, [agreementTypeOptions, isEditMode, formData.agreementType]);

  const [eeOptions, setEeOptions] = useState([]);

  useEffect(() => {
    const fetchEEs = async () => {
      try {
        const response = await fetch(
          apiPath(`/api/v1/accounts/category/${encodeURIComponent("Electrical Engineer")}`),
          {
            method: "GET",
            headers: getAuthHeaders(),
            credentials: "include",
          }
        );
        if (response.ok) {
          const data = await response.json();
          const fetchedOptions = data.map((account) => {
            const id = (account.user_id || account.userId || '').trim();
            const name = (account.user_name || account.userName || '').trim();
            return {
              value: id,
              label: name ? `${name} [${id}]` : id
            };
          });

          const optionMap = new Map();
          fetchedOptions.forEach(opt => {
            if (opt.value && !optionMap.has(opt.value)) {
              optionMap.set(opt.value, opt);
            }
          });

          setEeOptions(Array.from(optionMap.values()));
        } else {
          console.error("Failed to fetch EE accounts:", await response.text());
        }
      } catch (error) {
        console.error("Error fetching EE accounts:", error);
      }
    };
    fetchEEs();
  }, []);

  const handleChange = (event) => {
    const { name, value } = event.target;
    if (name === "region") {
      setFormData((current) => ({
        ...current,
        region: value,
        province: "",
        area: "",
        gridSubstation: "",
      }));
      setAreaOptions([]);
      setGridSubstationOptions([]);
      if (value) {
        loadProvinces(value);
        loadGridSubstations(value);
      } else {
        setProvinceOptions([]);
      }
      return;
    }
    if (name === "province") {
      setFormData((current) => ({
        ...current,
        province: value,
        area: "",
      }));
      if (value) {
        loadAreas(formData.region, value);
      } else {
        setAreaOptions([]);
      }
      return;
    }
    setFormData((current) => ({ ...current, [name]: value }));
  };

  useEffect(() => {
    // Clear tier dates if not TTT
    if (formData.tariffType && !formData.tariffType.includes("TTT")) {
      if (
        formData.firstTierDate !== "" ||
        formData.secondTierDate !== "" ||
        formData.thirdTierDate !== ""
      ) {
        setFormData((prev) => ({
          ...prev,
          firstTierDate: "",
          secondTierDate: "",
        }));
      }
    }
    // Auto-populate commissioned year from grid connection date
    if (formData.gridConnectionDate) {
      const gcd = new Date(formData.gridConnectionDate);
      if (!isNaN(gcd.getTime())) {
        const year = String(gcd.getFullYear());
        if (formData.commissionedYear !== year) {
          setFormData((prev) => ({
            ...prev,
            commissionedYear: year,
          }));
        }
      }
    }

    if (
      (formData.tariffType === "TTT (3 years)" ||
        formData.tariffType === "TTT (5 years)") &&
      formData.gridConnectionDate
    ) {
      const gcd = new Date(formData.gridConnectionDate);
      if (!isNaN(gcd.getTime())) {
        const d1 = new Date(gcd);
        d1.setFullYear(d1.getFullYear() + 8);
        d1.setDate(d1.getDate() - 1);

        const d2 = new Date(d1);
        d2.setFullYear(d2.getFullYear() + 7);

        const d3 = new Date(d2);
        d3.setFullYear(d3.getFullYear() + 5);

        const t1 = d1.toISOString().split("T")[0];
        const t2 = d2.toISOString().split("T")[0];
        const t3 = d3.toISOString().split("T")[0];

        if (
          formData.firstTierDate !== t1 ||
          formData.secondTierDate !== t2 ||
          formData.thirdTierDate !== t3
        ) {
          setFormData((prev) => ({
            ...prev,
            firstTierDate: t1,
            secondTierDate: t2,
            thirdTierDate: t3,
          }));
        }
      }
    }
  }, [
    formData.gridConnectionDate,
    formData.tariffType,
    formData.commissionedYear,
    formData.ncreType,
    formData.firstTierDate,
    formData.secondTierDate,
    formData.thirdTierDate,
  ]);

  const handleAgreementChange = (index, field, value) => {
    const newAgreements = [...formData.agreements];
    newAgreements[index][field] = value;
    setFormData({ ...formData, agreements: newAgreements });
  };

  const addAgreement = () => {
    setFormData({
      ...formData,
      agreements: [
        ...formData.agreements,
        { ...initialAgreementState, id: Date.now() },
      ],
    });
  };

  const removeAgreement = (index) => {
    const newAgreements = formData.agreements.filter((_, i) => i !== index);
    setFormData({ ...formData, agreements: newAgreements });
  };

  const addAddendum = (agreementIndex) => {
    const newAgreements = [...formData.agreements];
    newAgreements[agreementIndex].addendums.push({
      id: Date.now(),
      developerName: "",
      newSppaSignedDate: "",
      initialTariffRevised: "",
      expirationExtensionDate: "",
      recommissionedOn: "",
      addendumFiles: [],
    });
    setFormData({ ...formData, agreements: newAgreements });
  };

  const handleAddendumChange = (
    agreementIndex,
    addendumIndex,
    field,
    value
  ) => {
    const newAgreements = [...formData.agreements];
    newAgreements[agreementIndex].addendums[addendumIndex][field] = value;
    setFormData({ ...formData, agreements: newAgreements });
  };

  const removeAddendum = (agreementIndex, addendumIndex) => {
    const newAgreements = [...formData.agreements];
    newAgreements[agreementIndex].addendums = newAgreements[
      agreementIndex
    ].addendums.filter((_, i) => i !== addendumIndex);
    setFormData({ ...formData, agreements: newAgreements });
  };

  const handlePaymentDeductionToggle = (agreementIndex, deductionType) => {
    const newAgreements = [...formData.agreements];
    const deductions = newAgreements[agreementIndex].paymentDeductions;
    const exists = deductions.find((d) => d.type === deductionType);
    if (exists) {
      newAgreements[agreementIndex].paymentDeductions = deductions.filter(
        (d) => d.type !== deductionType
      );
    } else {
      const isOnePercent = deductionType.toLowerCase().includes("1%");
      newAgreements[agreementIndex].paymentDeductions.push({
        type: deductionType,
        percentage: isOnePercent ? "1" : "",
      });
    }
    setFormData({ ...formData, agreements: newAgreements });
  };

  const handlePaymentDeductionValChange = (
    agreementIndex,
    deductionType,
    value
  ) => {
    const newAgreements = [...formData.agreements];
    const deduction = newAgreements[agreementIndex].paymentDeductions.find(
      (d) => d.type === deductionType
    );
    if (deduction) {
      deduction.percentage = value;
    }
    setFormData({ ...formData, agreements: newAgreements });
  };

  const handleFileUpload = (agreementIndex, targetArray, e) => {
    const files = Array.from(e.target.files);
    const newAgreements = [...formData.agreements];
    newAgreements[agreementIndex][targetArray] = [
      ...newAgreements[agreementIndex][targetArray],
      ...files,
    ];
    setFormData({ ...formData, agreements: newAgreements });
  };

  const handleAddendumFileUpload = (agreementIndex, addendumIndex, e) => {
    const files = Array.from(e.target.files);
    const newAgreements = [...formData.agreements];
    newAgreements[agreementIndex].addendums[addendumIndex].addendumFiles = [
      ...newAgreements[agreementIndex].addendums[addendumIndex].addendumFiles,
      ...files,
    ];
    setFormData({ ...formData, agreements: newAgreements });
  };

  const handleRemoveFile = (agreementIndex, targetArray, fileIndex) => {
    const newAgreements = [...formData.agreements];
    newAgreements[agreementIndex][targetArray] = newAgreements[agreementIndex][
      targetArray
    ].filter((_, i) => i !== fileIndex);
    setFormData({ ...formData, agreements: newAgreements });
  };

  const handleRemoveAddendumFile = (
    agreementIndex,
    addendumIndex,
    fileIndex
  ) => {
    const newAgreements = [...formData.agreements];
    newAgreements[agreementIndex].addendums[addendumIndex].addendumFiles =
      newAgreements[agreementIndex].addendums[addendumIndex].addendumFiles.filter(
        (_, i) => i !== fileIndex
      );
    setFormData({ ...formData, agreements: newAgreements });
  };

  const renderField = (field, value, onChangeHandler) => {
    if (field.type === 'select') {
      const optionSources = {
        region: regionOptions,
        province: provinceOptions,
        area: areaOptions,
        gridSubstation: gridSubstationOptions,
        responsibleEe: eeOptions,
        ncreType: effectiveNcreTypeOptions,
      };
      const selectOptions = optionSources[field.name] || field.options || [];

      let isDisabled = !!field.disabled;
      let hintText = null;

      if (field.name === 'region') {
        const waiting = locationLoading && selectOptions.length === 0;
        isDisabled = isDisabled || waiting;
        hintText = waiting ? "Loading regions..." : null;
      } else if (field.name === 'province') {
        const waiting = locationLoading && selectOptions.length === 0;
        isDisabled = isDisabled || !formData.region || waiting;
        hintText = !formData.region
          ? "Select region first"
          : waiting
            ? "Loading provinces..."
            : null;
      } else if (field.name === 'area') {
        const waiting = locationLoading && selectOptions.length === 0;
        isDisabled = isDisabled || !formData.province || waiting;
        hintText = !formData.province
          ? "Select province first"
          : waiting
            ? "Loading areas..."
            : null;
      } else if (field.name === 'gridSubstation') {
        const waiting = gridSubstationsLoading && selectOptions.length === 0;
        isDisabled = isDisabled || !formData.region || waiting;
        hintText = !formData.region
          ? "Select region first"
          : waiting
            ? "Loading grid substations..."
            : null;
      }

      let selectedValue = value || "";
      if (field.name === 'responsibleEe' && value) {
        const cleanVal = String(value).replace(/[^a-zA-Z0-9]/g, '').toLowerCase();
        const directMatch = selectOptions.find(opt => {
          const optVal = typeof opt === 'object' ? opt.value : opt;
          return String(optVal).trim() === String(value).trim();
        });

        if (directMatch) {
          selectedValue = typeof directMatch === 'object' ? directMatch.value : directMatch;
        } else {
          const fuzzyMatch = selectOptions.find(opt => {
            const optVal = typeof opt === 'object' ? String(opt.value) : String(opt);
            const optLabel = typeof opt === 'object' ? String(opt.label) : String(opt);
            const cleanOptVal = optVal.replace(/[^a-zA-Z0-9]/g, '').toLowerCase();
            const cleanOptLabel = optLabel.replace(/[^a-zA-Z0-9]/g, '').toLowerCase();
            return cleanOptVal === cleanVal ||
              cleanOptLabel === cleanVal ||
              (cleanVal.length > 2 && cleanOptLabel.includes(cleanVal)) ||
              (cleanOptVal.length > 2 && cleanVal.includes(cleanOptVal));
          });

          if (fuzzyMatch) {
            selectedValue = typeof fuzzyMatch === 'object' ? fuzzyMatch.value : fuzzyMatch;
          }
        }
      }

      const existsInOptions = selectOptions.some(opt => {
        const optVal = typeof opt === 'object' ? opt.value : opt;
        return String(optVal) === String(selectedValue);
      });

      return (
        <select
          id={field.name}
          name={field.name}
          value={selectedValue}
          onChange={onChangeHandler}
          required={requiredFieldNames.has(field.name)}
          disabled={isDisabled || (field.name === 'ncreType' && ncreTypesLoading)}
          className={`${inputClassName} ${isDisabled ? "bg-ink-100 cursor-not-allowed" : ""}`}
        >
          <option value="">
            {hintText || (field.name === 'ncreType' && ncreTypesLoading
              ? 'Loading NCRE types...'
              : (field.placeholder || `Select ${field.label ? field.label.toLowerCase() : 'an option'}`))}
          </option>
          {selectedValue && !existsInOptions && (
            <option value={selectedValue}>{selectedValue}</option>
          )}
          {selectOptions.map((option) => {
            const isObject = typeof option === 'object' && option !== null;
            const val = isObject ? option.value : option;
            const label = isObject ? option.label : option;
            return <option key={val} value={val}>{label}</option>;
          })}
        </select>
      );
    }

    if (field.type === "radio") {
      const radioOptions = field.name === 'agreementType' ? effectiveAgreementTypeOptions : (field.options || []);
      return (
        <div className="flex flex-wrap gap-6 pt-2">
          {agreementTypesLoading && field.name === 'agreementType' && (
            <span className="text-sm text-ink-400">Loading agreement types...</span>
          )}
          {radioOptions.map((option) => {
            const isObject = typeof option === 'object' && option !== null;
            const optValue = isObject ? option.value : option;
            const optLabel = isObject ? option.label : option;
            return (
              <label
                key={optValue}
                className="inline-flex items-center gap-2.5 text-sm text-ink-700 font-medium cursor-pointer"
              >
                <input
                  type="radio"
                  name={field.name}
                  value={optValue}
                  checked={value === optValue}
                  onChange={onChangeHandler}
                  className="h-4.5 w-4.5 border-ink-300 text-navy-800 focus:ring-navy-800/20"
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
        value={value}
        onChange={onChangeHandler}
        required={requiredFieldNames.has(field.name)}
        min={field.min}
        max={field.max}
        step={field.step}
        placeholder={field.placeholder}
        className={inputClassName}
        onWheel={(e) => field.type === 'number' && e.target.blur()}
      />
    );
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (submitting) return;

    const requiredFields = [
      ["developerName", "Developer name"],
      ["accountNumber", "Bulk Supply Account Number"],
      ["folioNumber", "Folio number"],
      ["tariffType", "Tariff type"],
      ["projectName", "Name of the facility"],
      ["meterNo", "Meter no"],
      ["gridConnectionDate", "Grid connection date"],
      ["ncreType", "NCRE type"],
      ["commissionedCapacityMw", "Commissioned capacity (MW)"],
      ["sppaSignedCapacityMw", "SPPA/PPA signed capacity (MW)"],
      ["acceptRu", "Accept RU"],
      ["longitude", "Longitude"],
      ["latitude", "Latitude"],
    ];
    const missingFields = requiredFields
      .filter(([field]) => !String(formData[field] ?? "").trim())
      .map(([, label]) => label);
    if (missingFields.length > 0) {
      toast.error(`Required fields: ${missingFields.join(", ")}.`);
      return;
    }

    const folioNumber = Number(formData.folioNumber);
    if (!Number.isInteger(folioNumber) || folioNumber <= 0 || folioNumber % 4 !== 1) {
      toast.error("Folio number must match the pattern 1, 5, 9, 13, ...");
      return;
    }

    const longitude = formData.longitude === "" ? null : Number(formData.longitude);
    const latitude = formData.latitude === "" ? null : Number(formData.latitude);
    if ((longitude === null) !== (latitude === null)) {
      toast.error("Longitude and latitude must be provided together.");
      return;
    }
    if (longitude !== null && (longitude < 79.5 || longitude > 81.9)) {
      toast.error("Longitude must be within Sri Lanka (79.5 to 81.9).");
      return;
    }
    if (latitude !== null && (latitude < 5.9 || latitude > 9.9)) {
      toast.error("Latitude must be within Sri Lanka (5.9 to 9.9).");
      return;
    }

    // Validation guard: Energy Losses cannot be negative
    for (let i = 0; i < (formData.agreements || []).length; i++) {
      const gLoss = formData.agreements[i].generationLosses;
      if (gLoss != null && gLoss !== "" && Number(gLoss) < 0) {
        toast.error(`Agreement ${i + 1}: Energy Losses (%) cannot be a negative value.`);
        return;
      }
      for (const deduction of formData.agreements[i].paymentDeductions || []) {
        if (deduction.percentage !== "" && Number(deduction.percentage) < 0) {
          toast.error(`Agreement ${i + 1}: Payment deduction percentage cannot be negative.`);
          return;
        }
      }
    }

    console.log("Submitting formData:", formData);
    setSubmitting(true);

    try {
      // 1. Construct clean JSON payload mapping File objects to filenames
      const requestData = {
        ...formData,
        acceptRu: formData.acceptRu === "" || formData.acceptRu == null ? null : Number(formData.acceptRu),
        agreements: formData.agreements.map((agreement) => ({
          initialTariff: agreement.initialTariff,
          voltageLevelKv: agreement.voltageLevelKv,
          generationLosses: agreement.generationLosses,
          paymentDeductions: agreement.paymentDeductions,
          tariffFileNames: (agreement.tariffFiles || []).map((f) => f.name),
          agreementFileNames: (agreement.agreementFiles || []).map((f) => f.name),
          addendums: (agreement.addendums || []).map((addendum) => ({
            developerName: addendum.developerName,
            newSppaSignedDate: addendum.newSppaSignedDate,
            initialTariffRevised: addendum.initialTariffRevised,
            expirationExtensionDate: addendum.expirationExtensionDate,
            recommissionedOn: addendum.recommissionedOn,
            addendumFileNames: (addendum.addendumFiles || []).map((f) => f.name),
          })),
        })),
      };

      const dataPayload = new Blob([JSON.stringify(requestData)], {
        type: "application/json",
      });

      const multipartData = new FormData();
      multipartData.append("data", dataPayload);

      // 2. Extract actual File objects and append to multipartData
      formData.agreements.forEach((agreement) => {
        if (agreement.tariffFiles) {
          agreement.tariffFiles.forEach((file) => {
            multipartData.append("files", file);
          });
        }
        if (agreement.agreementFiles) {
          agreement.agreementFiles.forEach((file) => {
            multipartData.append("files", file);
          });
        }
        if (agreement.addendums) {
          agreement.addendums.forEach((addendum) => {
            if (addendum.addendumFiles) {
              addendum.addendumFiles.forEach((file) => {
                multipartData.append("files", file);
              });
            }
          });
        }
      });

      const endpoint = isEditMode
        ? apiPath("/api/v1/developers/update")
        : apiPath("/api/v1/developers/register");
      const httpMethod = isEditMode ? "PUT" : "POST";
      const authHeaders = getAuthHeaders();
      delete authHeaders["Content-Type"];

      const response = await fetch(endpoint, {
        method: httpMethod,
        body: multipartData,
        headers: authHeaders,
        credentials: "include",
      });

      if (response.ok) {
        toast.success(
          isEditMode
            ? "Developer Updated Successfully!"
            : "Developer Registered Successfully!"
        );
        const savedData = await response.json();
        console.log("Saved Developer Data:", savedData);
        if (onSubmit) {
          onSubmit(savedData, isEditMode);
        }
      } else {
        const errorText = await response.text();
        toast.error(
          (isEditMode ? "Update Failed: " : "Registration Failed: ") + errorText
        );
        console.error("Failed response:", errorText);
      }
    } catch (error) {
      console.error("Error submitting form:", error);
      toast.error("An error occurred during submission.");
    } finally {
      setSubmitting(false);
    }
  };

  const handleResetForm = () => {
    setFormData({
      ...initialFormState,
      agreements: [{ ...initialAgreementState, id: Date.now() }]
    });
  };

  return (
    <form className="space-y-6" onSubmit={handleSubmit}>
      {/* Developer Information Section */}
      <div className="bg-ink-50/50 p-6 rounded-2xl border border-ink-100/80">
        <h5 className="mb-4 text-sm font-extrabold text-ink-800 uppercase tracking-wider">
          Developer Information
        </h5>
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
          {developerFields.map((field) => (
            <div key={field.name} className="flex flex-col justify-end">
              <label className="ds-label-field">
                {field.label}
                {requiredFieldNames.has(field.name) && <span className="ml-1 text-critical-500" aria-hidden="true">*</span>}
              </label>
              {renderField(field, formData[field.name], handleChange)}
            </div>
          ))}
        </div>
      </div>

      {/* Project Information Section */}
      <div className="bg-ink-50/50 p-6 rounded-2xl border border-ink-100/80 space-y-6">
        <h5 className="text-sm font-extrabold text-ink-800 uppercase tracking-wider">
          Project Information
        </h5>
        <div className="space-y-6">
          {projectSections.map((section) => (
            <section
              key={section.title}
              className="rounded-2xl border border-ink-200/60 bg-white p-5 shadow-xs"
            >
              <h6 className="mb-4 text-sm font-bold text-ink-800">
                {section.title}
              </h6>
              <div className="grid grid-cols-1 gap-ds-4 md:grid-cols-2 xl:grid-cols-3">
                {section.fields.map((field) => (
                  <div key={field.name} className="flex flex-col justify-end h-full">
                    <label className="ds-label-field">
                      {field.label}
                      {requiredFieldNames.has(field.name) && <span className="ml-1 text-critical-500" aria-hidden="true">*</span>}
                    </label>
                    {renderField(field, formData[field.name], handleChange)}
                  </div>
                ))}
              </div>
            </section>
          ))}

          {/* Global Tariff Details Section */}
          <section className="rounded-2xl border border-ink-200/60 bg-white p-5 shadow-xs">
            <h6 className="mb-4 text-sm font-bold text-ink-800">
              Tariff Details
            </h6>
            <div className="grid grid-cols-1 gap-ds-4 md:grid-cols-2 xl:grid-cols-3">
              <div className="md:col-span-2 xl:col-span-3">
                <label className="ds-label-field">
                  Agreement Type
                </label>
                {renderField(
                  { name: "agreementType", type: "radio", options: agreementTypeOptions },
                  formData.agreementType,
                  handleChange
                )}
              </div>
              <div>
                <label className="ds-label-field">
                  Tariff Type
                  <span className="ml-1 text-critical-500" aria-hidden="true">*</span>
                </label>
                {renderField(
                  {
                    name: "tariffType",
                    type: "select",
                    options: effectiveTariffOptions,
                    placeholder: tariffsLoading
                      ? "Loading tariffs..."
                      : tariffsError
                        ? "Failed to load tariffs"
                        : "Select tariff type",
                    disabled: tariffsLoading,
                  },
                  formData.tariffType,
                  handleChange
                )}
              </div>
              {isEditMode && (
                <div>
                  <label className="ds-label-field">
                    Status
                  </label>
                  <select
                    name="status"
                    value={formData.status || "2"}
                    onChange={handleChange}
                    className={inputClassName}
                  >
                    <option value="2">Active</option>
                    <option value="3">Inactive</option>
                  </select>
                </div>
              )}
              <div>
                <label className="ds-label-field">
                  Commissioned Year
                </label>
                {renderField(
                  { name: "commissionedYear", type: "number" },
                  formData.commissionedYear,
                  handleChange
                )}
              </div>
              <div>
                <label className="ds-label-field">
                  Commissioned Capacity (MW)
                  <span className="ml-1 text-critical-500" aria-hidden="true">*</span>
                </label>
                {renderField(
                  { name: "commissionedCapacityMw", type: "number" },
                  formData.commissionedCapacityMw,
                  handleChange
                )}
              </div>
              <div>
                <label className="ds-label-field">
                  SPPA/PPA Signed Capacity (MW)
                  <span className="ml-1 text-critical-500" aria-hidden="true">*</span>
                </label>
                {renderField(
                  { name: "sppaSignedCapacityMw", type: "number" },
                  formData.sppaSignedCapacityMw,
                  handleChange
                )}
              </div>
              <div>
                <label className="ds-label-field">
                  Accepted Energy Difference (RU)
                  <span className="ml-1 text-critical-500" aria-hidden="true">*</span>
                </label>
                {renderField(
                  { name: "acceptRu", type: "number", min: 0 },
                  formData.acceptRu,
                  handleChange
                )}
              </div>
              <div>
                <label className="ds-label-field">
                  Energy Permit Expiration Date
                </label>
                {renderField(
                  { name: "epExpired", type: "date" },
                  formData.epExpired,
                  handleChange
                )}
              </div>
              <div>
                <label className="ds-label-field">
                  Generation License Expiration Date
                </label>
                {renderField(
                  { name: "glExpired", type: "date" },
                  formData.glExpired,
                  handleChange
                )}
              </div>
              {(formData.tariffType === "TTT (3 years)" ||
                formData.tariffType === "TTT (5 years)") && (
                  <>
                    <div>
                      <label className="ds-label-field">
                        1st Tier
                      </label>
                      {renderField(
                        { name: "firstTierDate", type: "date" },
                        formData.firstTierDate,
                        handleChange
                      )}
                    </div>
                    <div>
                      <label className="ds-label-field">
                        2nd Tier
                      </label>
                      {renderField(
                        { name: "secondTierDate", type: "date" },
                        formData.secondTierDate,
                        handleChange
                      )}
                    </div>
                    <div>
                      <label className="ds-label-field">
                        3rd Tier
                      </label>
                      {renderField(
                        { name: "thirdTierDate", type: "date" },
                        formData.thirdTierDate,
                        handleChange
                      )}
                    </div>
                  </>
                )}
            </div>
          </section>
        </div>
      </div>

      {/* Dynamic Agreements Section */}
      <div className="space-y-6">
        {formData.agreements.map((agreement, index) => (
          <div key={agreement.id} className="mb-6">
            {/* Agreement Title Outside the Box */}
            <div className="flex justify-between items-center mb-3 px-1">
              <h6 className="text-sm font-extrabold text-ink-800 uppercase tracking-wider">
                Agreement {index + 1}
              </h6>
              {formData.agreements.length > 1 && (
                <button
                  type="button"
                  onClick={() => removeAgreement(index)}
                  className="text-critical-500 hover:text-critical-700 text-xs font-bold cursor-pointer transition-colors"
                >
                  <i className="fas fa-trash mr-1.5"></i> Remove Agreement
                </button>
              )}
            </div>

            <div className="border border-ink-200 rounded-2xl p-6 bg-ink-50/80 shadow-xs relative">
              <div className="grid grid-cols-1 gap-ds-4 md:grid-cols-2 lg:grid-cols-3 mb-6 border-b border-ink-200/60 pb-6">
                <div>
                  <label className="ds-label-field">
                    Initial Tariff
                  </label>
                  {renderField(
                    { name: `initialTariff_${index}`, type: "text" },
                    agreement.initialTariff,
                    (e) => handleAgreementChange(index, "initialTariff", e.target.value)
                  )}

                  {/* Initial Tariff Document Upload */}
                  <div className="mt-3 bg-white p-3 rounded-xl border border-ink-200/50 shadow-xs">
                    <label className="text-[11px] font-bold text-ink-500 uppercase tracking-tight">
                      Attach Tariff Revision Docs
                    </label>
                    <input
                      type="file"
                      multiple
                      accept=".pdf,image/*"
                      onChange={(e) => handleFileUpload(index, "tariffFiles", e)}
                      className="block w-full text-xs text-ink-400 mt-1.5 file:mr-3 file:py-1 file:px-2.5 file:rounded-lg file:border-0 file:text-[11px] file:font-bold file:bg-ink-100 file:text-navy-800 hover:file:bg-ink-200/80 cursor-pointer file:cursor-pointer"
                    />
                    {agreement.tariffFiles.length > 0 && (
                      <ul className="mt-2.5 text-xs text-navy-800 space-y-1">
                        {agreement.tariffFiles.map((f, i) => (
                          <li
                            key={i}
                            className="flex justify-between items-center bg-ink-50 p-1.5 rounded-lg border border-ink-200/40"
                          >
                            <span className="truncate pr-2 font-medium">{f.name}</span>
                            <button
                              type="button"
                              onClick={() => handleRemoveFile(index, "tariffFiles", i)}
                              className="text-critical-500 hover:text-critical-700 cursor-pointer"
                            >
                              <i className="fas fa-times"></i>
                            </button>
                          </li>
                        ))}
                      </ul>
                    )}
                  </div>
                </div>
                <div>
                  <label className="ds-label-field">
                    Voltage Level (KV)
                  </label>
                  {renderField(
                    { name: `voltageLevelKv_${index}`, type: "number" },
                    agreement.voltageLevelKv,
                    (e) => handleAgreementChange(index, "voltageLevelKv", e.target.value)
                  )}
                </div>
                <div>
                  <label className="ds-label-field">
                    Energy Losses (%)
                  </label>
                  {renderField(
                    { name: `generationLosses_${index}`, type: "number", min: "0", step: "0.01" },
                    agreement.generationLosses,
                    (e) => handleAgreementChange(index, "generationLosses", e.target.value)
                  )}
                </div>
              </div>

              {/* Payment Deductions Multi-Select */}
              <div className="mb-6 bg-white p-4 rounded-xl border border-ink-200/80 shadow-xs">
                <label className="mb-3 block text-xs font-bold text-ink-600 uppercase tracking-tight">
                  Payment Deductions
                </label>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {paymentDeductionTypesLoading && paymentDeductionOptions.length === 0 ? (
                    <p className="text-sm text-ink-400 col-span-2">
                      Loading payment deduction types...
                    </p>
                  ) : (
                    paymentDeductionOptions.map((option) => {
                      const active = agreement.paymentDeductions.find(
                        (d) => d.type === option
                      );
                      return (
                        <div key={option} className="flex items-center space-x-3">
                          <input
                            type="checkbox"
                            checked={!!active}
                            onChange={() => handlePaymentDeductionToggle(index, option)}
                            className="h-4.5 w-4.5 text-navy-800 border-ink-300 rounded focus:ring-navy-800/20 cursor-pointer"
                          />
                          <label className="text-sm text-ink-700 font-medium w-1/2">
                            {option}
                          </label>
                          {active && (
                            <input
                              type="number"
                              min="0"
                              placeholder="Value (%)"
                              value={active.percentage}
                              disabled={option.toLowerCase().includes("1%")}
                              onChange={(e) =>
                                handlePaymentDeductionValChange(index, option, e.target.value)
                              }
                              onWheel={(e) => e.target.blur()}
                              className={`w-1/2 rounded-lg border border-slate-200 px-2 py-1 text-sm focus:border-[#002244] focus:outline-none ${
                                option.toLowerCase().includes("1%") ? "bg-slate-100 text-slate-500 cursor-not-allowed" : ""
                              }`}
                            />
                          )}
                        </div>
                      );
                    })
                  )}
                </div>
              </div>

              {/* Addendums Section */}
              <div className="mb-6 pl-4 border-l-2 border-navy-800/20">
                <div className="flex justify-between items-center mb-4">
                  <h6 className="text-xs font-bold text-navy-800 uppercase tracking-wider">
                    Addendums for Agreement {index + 1}
                  </h6>
                </div>

                {agreement.addendums.map((addendum, aIndex) => (
                  <div
                    key={addendum.id}
                    className="mb-4 bg-ink-100/60 p-4 rounded-xl border border-ink-200"
                  >
                    <div className="flex justify-between mb-3">
                      <span className="font-bold text-xs text-navy-800 uppercase">
                        Addendum {aIndex + 1}
                      </span>
                      <button
                        type="button"
                        onClick={() => removeAddendum(index, aIndex)}
                        className="text-critical-500 hover:text-critical-600 text-xs font-bold cursor-pointer"
                      >
                        <i className="fas fa-times"></i> Remove
                      </button>
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                      <div>
                        <label className="ds-label-field">
                          Developer Name
                        </label>
                        {renderField(
                          { name: `add_dev_${index}_${aIndex}`, type: "text" },
                          addendum.developerName,
                          (e) => handleAddendumChange(index, aIndex, "developerName", e.target.value)
                        )}
                      </div>
                      <div>
                        <label className="ds-label-field">
                          Addendum Signed Date
                        </label>
                        {renderField(
                          { name: `add_sppa_${index}_${aIndex}`, type: "date" },
                          addendum.newSppaSignedDate,
                          (e) => handleAddendumChange(index, aIndex, "newSppaSignedDate", e.target.value)
                        )}
                      </div>
                      <div>
                        <label className="ds-label-field">
                          Initial Tariff Revised
                        </label>
                        {renderField(
                          { name: `add_rev_${index}_${aIndex}`, type: "number" },
                          addendum.initialTariffRevised,
                          (e) => handleAddendumChange(index, aIndex, "initialTariffRevised", e.target.value)
                        )}
                      </div>
                      <div>
                        <label className="ds-label-field">
                          Expiration Extension Date
                        </label>
                        {renderField(
                          { name: `add_exp_${index}_${aIndex}`, type: "date" },
                          addendum.expirationExtensionDate,
                          (e) => handleAddendumChange(index, aIndex, "expirationExtensionDate", e.target.value)
                        )}
                      </div>
                      <div>
                        <label className="ds-label-field">
                          Recommissioned On
                        </label>
                        {renderField(
                          { name: `add_rec_${index}_${aIndex}`, type: "date" },
                          addendum.recommissionedOn,
                          (e) => handleAddendumChange(index, aIndex, "recommissionedOn", e.target.value)
                        )}
                      </div>
                    </div>

                    {/* Addendum Document Upload */}
                    <div className="mt-4 pt-4 border-t border-ink-200">
                      <label className="text-[11px] font-bold text-ink-500 uppercase tracking-tight mb-2 block">
                        Upload Addendum Documents (PDF/Image)
                      </label>
                      <input
                        type="file"
                        multiple
                        accept=".pdf,image/*"
                        onChange={(e) => handleAddendumFileUpload(index, aIndex, e)}
                        className="block w-full text-xs text-ink-400 file:mr-3 file:py-1 file:px-2.5 file:rounded-lg file:border-0 file:text-[11px] file:font-bold file:bg-ink-200/60 file:text-navy-800 hover:file:bg-ink-200 cursor-pointer file:cursor-pointer"
                      />
                      {addendum.addendumFiles.length > 0 && (
                        <ul className="mt-2.5 text-xs text-navy-800 space-y-1">
                          {addendum.addendumFiles.map((f, i) => (
                            <li
                              key={i}
                              className="flex justify-between items-center bg-white p-1.5 rounded-lg border border-ink-200/40"
                            >
                              <span className="truncate pr-2 font-medium">{f.name}</span>
                              <button
                                type="button"
                                onClick={() => handleRemoveAddendumFile(index, aIndex, i)}
                                className="text-critical-500 hover:text-critical-700 cursor-pointer"
                              >
                                <i className="fas fa-times"></i>
                              </button>
                            </li>
                          ))}
                        </ul>
                      )}
                    </div>
                  </div>
                ))}

                <button
                  type="button"
                  onClick={() => addAddendum(index)}
                  className="mt-2 text-xs font-bold text-navy-800 bg-white border border-ink-200 px-3.5 py-1.5 rounded-xl hover:bg-ink-50 transition-colors shadow-xs cursor-pointer"
                >
                  <i className="fas fa-plus mr-1.5"></i> Add Addendum
                </button>
              </div>

              {/* Agreement Document Upload */}
              <div className="mt-6 p-4 bg-white rounded-xl border border-dashed border-ink-300 shadow-xs">
                <label className="text-xs font-bold text-ink-600 uppercase tracking-tight block mb-1">
                  Upload General Agreement Documents
                </label>
                <p className="text-xs text-ink-400 mb-3">
                  Attach PDFs or images containing the full signed agreement or related commercial documents.
                </p>
                <input
                  type="file"
                  multiple
                  accept=".pdf,image/*,.doc,.docx"
                  onChange={(e) => handleFileUpload(index, "agreementFiles", e)}
                  className="block w-full text-xs text-ink-400 file:mr-3 file:py-1 file:px-2.5 file:rounded-lg file:border-0 file:text-[11px] file:font-bold file:bg-navy-100 file:text-navy-800 hover:file:bg-navy-100/80 cursor-pointer file:cursor-pointer"
                />
                {agreement.agreementFiles.length > 0 && (
                  <ul className="mt-3 text-xs text-navy-800 space-y-2">
                    {agreement.agreementFiles.map((f, i) => (
                      <li
                        key={i}
                        className="flex justify-between items-center bg-ink-50 p-2 rounded-xl border border-ink-200/50"
                      >
                        <span className="truncate pr-2 font-medium">{f.name}</span>
                        <button
                          type="button"
                          onClick={() => handleRemoveFile(index, "agreementFiles", i)}
                          className="text-critical-500 hover:text-critical-700 font-bold px-2 cursor-pointer"
                        >
                          <i className="fas fa-times"></i>
                        </button>
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            </div>
          </div>
        ))}

        <div className="flex justify-center mt-6">
          <button
            type="button"
            onClick={addAgreement}
            className="px-6 py-3 bg-ink-50 text-navy-800 font-bold rounded-xl border-2 border-dashed border-ink-300 hover:bg-navy-100 hover:border-navy-800/30 transition-all duration-200 cursor-pointer"
          >
            <i className="fas fa-plus mr-2"></i> Add Another Agreement
          </button>
        </div>
      </div>

      <div className="mt-8 pt-4 border-t border-ink-200/60 flex justify-end gap-3">
        {!isEditMode && (
          <button
            type="button"
            onClick={handleResetForm}
            className="rounded-xl px-5 py-2.5 text-sm font-bold text-ink-700 bg-white border border-ink-200 hover:bg-ink-50 transition-all duration-200 cursor-pointer shadow-xs"
          >
            <i className="fas fa-undo mr-1.5 text-xs text-ink-400"></i> Clear Form
          </button>
        )}
        <button
          type="button"
          onClick={onCancel}
          className="rounded-xl px-6 py-2.5 text-sm font-bold text-ink-700 bg-white border border-ink-200 hover:bg-ink-50 transition-all duration-200 cursor-pointer"
        >
          Cancel
        </button>
        <button
          type="submit"
          disabled={submitting}
          className="rounded-xl px-6 py-2.5 text-sm font-bold text-white bg-navy-800 hover:bg-navy-900 disabled:opacity-50 transition-all duration-200 shadow-md cursor-pointer"
        >
          {submitting
            ? "Saving..."
            : isEditMode
              ? "Update Developer"
              : "Register Developer"}
        </button>
      </div>
    </form>
  );
}