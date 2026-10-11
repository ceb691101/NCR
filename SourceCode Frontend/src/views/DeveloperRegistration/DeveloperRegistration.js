import React, { useState, useEffect } from "react";
import { useLocation } from "react-router-dom";
import { toast } from "react-toastify";
import Breadcrumb from "components/Breadcrumb/Breadcrumb";
import DeveloperRegistrationForm from "components/DeveloperRegistration/DeveloperRegistrationForm";
import DeveloperSearchAutocomplete from "components/DeveloperRegistration/DeveloperSearchAutocomplete";
import {
  getDeveloperById,
  getDeveloperBySearch,
} from "services/developerRegistrationService";

const topLevelButtons = [
  { key: "register", label: "Register Developer" },
  { key: "edit", label: "Edit Details" },
];

const formatDateForInput = (dateValue) => {
  if (!dateValue) return "";
  const d = new Date(dateValue);
  if (isNaN(d.getTime())) return "";
  return d.toISOString().split("T")[0];
};

const formatGenerationLosses = (val) => {
  if (val == null || val === '') return '';
  const num = Number(val);
  if (isNaN(num)) return '';
  
  if (num < 0) {
    toast.error(`Invalid negative Generation Losses (${num}%) detected. Generation losses percentage cannot be negative.`);
    return String(Math.abs(num));
  }
  
  if (num > 0 && num < 1) {
    return String(Math.round(num * 100 * 100) / 100);
  }
  
  return String(num);
};

const mapEntityToFormData = (entity) => {
  if (!entity) return {};
  const mappedAgreements = (entity.agreements && Array.isArray(entity.agreements) && entity.agreements.length > 0)
    ? entity.agreements.map((agg, idx) => ({
        id: agg.id || Date.now() + idx,
        initialTariff: agg.initialTariff != null ? String(agg.initialTariff) : (entity.initialTariff ?? ''),
        voltageLevelKv: agg.voltageLevelKv != null ? String(agg.voltageLevelKv) : (entity.voltageLevelKv != null ? String(entity.voltageLevelKv) : ''),
        generationLosses: formatGenerationLosses(agg.generationLosses != null ? agg.generationLosses : entity.generationLosses),
        paymentDeductions: Array.isArray(agg.paymentDeductions)
          ? agg.paymentDeductions.map(d => typeof d === 'object' && d !== null ? { type: d.type || d.deductionType || '', percentage: d.percentage != null ? String(d.percentage) : '' } : { type: String(d), percentage: '' })
          : (entity.paymentDeductions != null ? [{ type: "Payment Deduction", percentage: String(entity.paymentDeductions) }] : []),
        addendums: Array.isArray(agg.addendums)
          ? agg.addendums.map((add, aIdx) => ({
              id: add.id || Date.now() + aIdx,
              developerName: add.developerName || '',
              newSppaSignedDate: formatDateForInput(add.newSppaSignedDate || add.sppaSignedDate),
              initialTariffRevised: add.initialTariffRevised != null ? String(add.initialTariffRevised) : '',
              expirationExtensionDate: formatDateForInput(add.expirationExtensionDate),
              recommissionedOn: formatDateForInput(add.recommissionedOn),
              addendumFiles: Array.isArray(add.documents)
                ? add.documents.map(d => ({ name: d.fileName || d.documentName || d.name || 'Document', url: d.fileUrl || d.url || '' }))
                : (Array.isArray(add.addendumFiles) ? add.addendumFiles : [])
            }))
          : [],
        tariffFiles: Array.isArray(agg.tariffFiles)
          ? agg.tariffFiles
          : (Array.isArray(entity.documents) ? entity.documents.filter(d => d.documentType === 'TARIFF' || d.type === 'TARIFF').map(doc => ({ name: doc.fileName || doc.documentName || doc.name || 'Tariff File', url: doc.fileUrl || doc.url || '' })) : []),
        agreementFiles: Array.isArray(agg.agreementFiles)
          ? agg.agreementFiles
          : (Array.isArray(entity.documents) ? entity.documents.filter(d => d.documentType !== 'TARIFF' && d.type !== 'TARIFF').map(doc => ({ name: doc.fileName || doc.documentName || doc.name || 'Agreement File', url: doc.fileUrl || doc.url || '' })) : [])
      }))
    : [{
        id: Date.now(),
        initialTariff: entity.initialTariff ?? '',
        voltageLevelKv: entity.voltageLevelKv != null ? String(entity.voltageLevelKv) : '',
        generationLosses: formatGenerationLosses(entity.generationLosses),
        paymentDeductions: entity.paymentDeductions != null ? [{ type: "Payment Deduction", percentage: String(entity.paymentDeductions) }] : [],
        addendums: [],
        tariffFiles: Array.isArray(entity.documents) ? entity.documents.filter(d => d.documentType === 'TARIFF' || d.type === 'TARIFF').map(doc => ({ name: doc.fileName || doc.documentName || doc.name || 'Tariff File', url: doc.fileUrl || doc.url || '' })) : [],
        agreementFiles: Array.isArray(entity.documents) ? entity.documents.filter(d => d.documentType !== 'TARIFF' && d.type !== 'TARIFF').map(doc => ({ name: doc.fileName || doc.documentName || doc.name || 'Agreement File', url: doc.fileUrl || doc.url || '' })) : []
      }];

  const rawNcreType = (entity.ncreType || entity.type || entity.ncre_type || '').trim();
  const ncreType = rawNcreType === 'MSWP' ? 'WHP' : rawNcreType;

  return {
    developerName:             entity.developerName          ?? '',
    groupOfCompany:            entity.groupOfCompany         || entity.companyGroup        || '',
    email:                     entity.email                  ?? '',
    phone:                     entity.phone                  || entity.telephone           || '',
    folioNumber:               entity.folioNumber            != null ? String(entity.folioNumber) : (entity.folioNo != null ? String(entity.folioNo) : ''),
    accountNumber:             entity.accountNumber          || entity.accNbr              || '',
    projectName:               entity.projectName            || entity.facilityName        || '',
    responsibleEe:             (entity.responsibleEe || entity.responsible_ee || entity.responsble_ee || '').trim(),
    area:                      entity.area                   ?? '',
    fileReferenceNo:           entity.fileReferenceNo        || entity.fileRefNo           || '',
    province:                  entity.province               ?? '',
    loiIssued:                 formatDateForInput(entity.loiIssued),
    sppaSignedDate:            formatDateForInput(entity.sppaSignedDate || entity.sppaSigned),
    gridConnectionDate:        formatDateForInput(entity.gridConnectionDate),
    referenceCode:             entity.referenceCode          ?? '',
    region:                    entity.region                 ?? '',
    srNo:                      entity.srNo                   ?? '',
    gridSubstation:            entity.gridSubstation         ?? '',
    ncreType:                  ncreType,
    meterNo:                   entity.meterNo                || entity.mtrNbr              || '',
    initialTariff:             entity.initialTariff          ?? '',
    tariffType:                entity.tariffType             ?? '',
    expirationDate:            formatDateForInput(entity.expirationDate),
    expirationExtensionDate:   formatDateForInput(entity.expirationExtensionDate),
    feederNo:                  entity.feederNo               != null ? String(entity.feederNo) : '',
    commissionedYear:          entity.commissionedYear       != null ? String(entity.commissionedYear) : '',
    commissionedCapacityMw:    entity.commissionedCapacityMw != null ? String(entity.commissionedCapacityMw) : '',
    sppaSignedCapacityMw:      entity.sppaSignedCapacityMw   != null ? String(entity.sppaSignedCapacityMw) : (entity.sppaCapacityMw != null ? String(entity.sppaCapacityMw) : ''),
    acceptRu:                  entity.acceptRu               != null ? String(entity.acceptRu) : '',
    acExpirationWithExtension: formatDateForInput(entity.acExpirationWithExtension),
    exDate:                    formatDateForInput(entity.ex),
    acDate:                    formatDateForInput(entity.ac),
    flatDate:                  formatDateForInput(entity.flat),
    tttDate:                   formatDateForInput(entity.ttt),
    firstTierDate:             formatDateForInput(entity.firstTier),
    secondTierDate:            formatDateForInput(entity.secondTier),
    thirdTierDate:             formatDateForInput(entity.thirdTier),
    newSppaSigned:             formatDateForInput(entity.newSppaSigned),
    validityStart:             formatDateForInput(entity.validityStart),
    validityExpiry:            formatDateForInput(entity.validityExpiry),
    initialTariffRevised:      entity.initialTariffRevised   != null ? String(entity.initialTariffRevised) : '',
    recommissionedOn:          formatDateForInput(entity.recommissionedOn),
    epExpired:                 formatDateForInput(entity.epExpired),
    glExpired:                 formatDateForInput(entity.glExpired),
    projectStatus:             entity.projectStatus          ?? '',
    voltageLevelKv:            entity.voltageLevelKv         != null ? String(entity.voltageLevelKv) : '',
    addressLine1:              entity.addressLine1           ?? '',
    addressLine2:              entity.addressLine2           ?? '',
    addressLine3:              entity.addressLine3           ?? '',
    contactPerson:             entity.contactPerson          ?? '',
    reductions:                entity.reductions             ?? '',
    agreementType:             entity.agreementType          ?? '',
    longitude:                 entity.longitude              != null ? String(entity.longitude) : '',
    latitude:                  entity.latitude               != null ? String(entity.latitude) : '',
    status:                    entity.status                 || '2',
    generationLosses:          formatGenerationLosses(entity.generationLosses),
    paymentDeductions:         entity.paymentDeductions      != null ? String(entity.paymentDeductions) : '',
    agreements:                mappedAgreements,
  };
};

export default function DeveloperRegistration() {
  const location = useLocation();
  const [activeSection, setActiveSection] = useState("register");
  const [loading, setLoading] = useState(false);
  const [searchInput, setSearchInput] = useState("");
  const [searchField, setSearchField] = useState("folio_no");
  const [developerData, setDeveloperData] = useState(null);

  const checkEEAccess = (mappedData) => {
    const userCat = sessionStorage.getItem("user_category");
    const userId = sessionStorage.getItem("user_id");
    const isEE = userCat === "EE" || userCat === "Electrical Engineer";

    if (isEE && userId && mappedData) {
      const resp = (mappedData.responsibleEe || mappedData.responsible_ee || mappedData.responsble_ee || '').trim();
      if (resp) {
        const cleanId = (str) => (str || '').replace(/[^a-zA-Z0-9]/g, '').toLowerCase();
        const cResp = cleanId(resp);
        const cUser = cleanId(userId);
        const isMatch = cResp === cUser || cResp.includes(cUser) || cUser.includes(cResp);
        if (!isMatch) {
          toast.error("Access Denied: You do not have permission to view or edit projects assigned to another Electrical Engineer.");
          return false;
        }
      }
    }
    return true;
  };

  useEffect(() => {
    if (location.state && location.state.searchValue) {
      setActiveSection("edit");
      setSearchField(location.state.searchField || "folio_no");
      setSearchInput(location.state.searchValue);

      const autoSearch = async () => {
        setLoading(true);
        try {
          const data = await getDeveloperBySearch(
            location.state.searchField || "folio_no",
            location.state.searchValue
          );
          const mapped = mapEntityToFormData(data);
          if (checkEEAccess(mapped)) {
            setDeveloperData(mapped);
            toast.success("Developer found and loaded.");
          } else {
            setDeveloperData(null);
          }
        } catch (error) {
          toast.error(`Search failed: ${error.message}`);
          setDeveloperData(null);
        } finally {
          setLoading(false);
        }
      };
      autoSearch();

      // Clear state so it doesn't run again on refresh
      window.history.replaceState({}, document.title);
    }
  }, [location.state]);

  const breadcrumbItems = [
    { label: "Dashboard", href: "/admin/dashboard" },
    { label: "Developer Registration", href: null },
  ];

  const handleTabChange = (section) => {
    setActiveSection(section);
    if (section === "register") {
      setDeveloperData(null);
    }
  };

  const handleSelectDeveloper = async (selectedSummary) => {
    if (!selectedSummary) return;
    setLoading(true);
    try {
      let data;
      if (selectedSummary.id) {
        data = await getDeveloperById(selectedSummary.id);
      } else {
        const val =
          selectedSummary.folioNumber ||
          selectedSummary.accountNumber ||
          selectedSummary.developerName ||
          selectedSummary.projectName;
        data = await getDeveloperBySearch(searchField, val);
      }

      const mapped = mapEntityToFormData(data);
      if (checkEEAccess(mapped)) {
        setDeveloperData(mapped);
        toast.success("Developer details loaded for editing.");
      } else {
        setDeveloperData(null);
      }
    } catch (error) {
      console.error("Error loading developer details:", error);
      toast.error(`Failed to load developer details: ${error.message}`);
      setDeveloperData(null);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (field, inputVal) => {
    const val = inputVal != null ? inputVal : searchInput;
    const fld = field != null ? field : searchField;
    if (!val || !val.trim()) {
      toast.error("Please enter a search value.");
      return;
    }
    setLoading(true);
    try {
      const data = await getDeveloperBySearch(fld, val.trim());
      const mapped = mapEntityToFormData(data);
      if (checkEEAccess(mapped)) {
        setDeveloperData(mapped);
        toast.success("Developer found and loaded for editing.");
      } else {
        setDeveloperData(null);
      }
    } catch (error) {
      toast.error(`Search failed: ${error.message}`);
      setDeveloperData(null);
    } finally {
      setLoading(false);
    }
  };

  const [lastRegisteredDeveloper, setLastRegisteredDeveloper] = useState(null);

  const handleFormSubmit = (savedData, isEdit) => {
    if (isEdit) {
      setDeveloperData(null);
    } else {
      setLastRegisteredDeveloper(savedData || true);
    }
  };

  const handleCancel = () => {
    if (activeSection === "edit") {
      setDeveloperData(null);
      setSearchInput("");
      setSearchField("folio_no");
    }
  };

  const renderSection = () => {
    if (activeSection === "edit") {
      return (
        <div>
          {/* Autocomplete Typeahead Search Form */}
          <div className="mb-6 p-5 bg-ink-50/80 border border-ink-100 rounded-xl shadow-card">
            <h4 className="text-sm font-bold text-ink-800 mb-3">
              Search Developer (Typeahead / Autocomplete)
            </h4>
            <DeveloperSearchAutocomplete
              onSelectDeveloper={handleSelectDeveloper}
              onManualSearch={handleSearch}
              onFieldChange={setSearchField}
              initialField={searchField}
              initialValue={searchInput}
              outerLoading={loading}
            />
          </div>

          {/* Edit Form */}
          {developerData ? (
            <DeveloperRegistrationForm
              onSubmit={handleFormSubmit}
              loading={loading}
              initialData={developerData}
              isEditMode={true}
              onCancel={handleCancel}
            />
          ) : (
            <div className="rounded-2xl border border-dashed border-ink-300 bg-ink-50/50 p-6 text-ink-500">
              <p className="text-sm font-medium">
                Search for a developer to edit their details.
              </p>
            </div>
          )}
        </div>
      );
    }

    if (activeSection === "register" && lastRegisteredDeveloper) {
      return (
        <div className="p-8 bg-success-50/60 border border-success-200/80 rounded-2xl text-center space-y-4 shadow-sm">
          <div className="w-14 h-14 bg-success-500 text-white rounded-full flex items-center justify-center mx-auto text-2xl shadow-md">
            <i className="fas fa-check"></i>
          </div>
          <h4 className="text-xl font-extrabold text-ink-800">
            Developer Registered Successfully!
          </h4>
          <p className="text-sm font-medium text-ink-600 max-w-md mx-auto">
            The developer and project details have been safely recorded in the database.
          </p>

          {typeof lastRegisteredDeveloper === 'object' && (
            <div className="ds-panel p-ds-4 inline-block text-left text-body-sm space-y-2 max-w-md w-full my-2">
              <div className="flex justify-between border-b border-ink-100 pb-1.5">
                <span className="font-semibold text-ink-500">Folio Number:</span>
                <span className="font-bold text-ink-800">{lastRegisteredDeveloper.folioNo || lastRegisteredDeveloper.folioNumber || "N/A"}</span>
              </div>
              <div className="flex justify-between border-b border-ink-100 pb-1.5">
                <span className="font-semibold text-ink-500">Developer Name:</span>
                <span className="font-bold text-ink-800">{lastRegisteredDeveloper.developerName}</span>
              </div>
              <div className="flex justify-between">
                <span className="font-semibold text-ink-500">Project Name:</span>
                <span className="font-bold text-ink-800">{lastRegisteredDeveloper.facilityName || lastRegisteredDeveloper.projectName}</span>
              </div>
            </div>
          )}

          <div className="flex flex-col sm:flex-row justify-center gap-3 pt-3">
            <button
              onClick={() => {
                const mapped = mapEntityToFormData(lastRegisteredDeveloper);
                setDeveloperData(mapped);
                setActiveSection("edit");
                setLastRegisteredDeveloper(null);
              }}
              className="px-6 py-2.5 bg-navy-800 text-white rounded-xl font-bold hover:bg-navy-900 transition-all text-sm cursor-pointer shadow-md"
            >
              <i className="fas fa-edit mr-2"></i> View / Edit Details
            </button>
            <button
              onClick={() => setLastRegisteredDeveloper(null)}
              className="px-6 py-2.5 bg-white border border-ink-200 text-ink-700 rounded-xl font-bold hover:bg-ink-50 transition-all text-sm cursor-pointer shadow-xs"
            >
              <i className="fas fa-plus mr-2"></i> Register Another Developer
            </button>
          </div>
        </div>
      );
    }

    return (
      <div>
        <DeveloperRegistrationForm
          onSubmit={handleFormSubmit}
          loading={loading}
          onCancel={handleCancel}
        />
      </div>
    );
  };

  return (
    <div className="container mx-auto rounded-lg mb-20 px-2 sm:px-4">
      <div className="mb-8 mx-0 sm:mx-2 mt-5 rounded-lg">
        {/* Breadcrumb row */}
        <div className="mt-0 mb-4 p-3.5 bg-white border border-ink-100 rounded-2xl shadow-sm">
          <Breadcrumb items={breadcrumbItems} />
        </div>

        {/* Content Box */}
        <div className="p-6 bg-white border border-ink-100 rounded-2xl shadow-sm">
          <div className="mb-6">
            <h3 className="text-lg font-extrabold text-ink-800 mb-4">
              {activeSection === "register"
                ? "Register Developer"
                : "Edit Developer Details"}
            </h3>

            {/* Premium Tab Buttons */}
            <div className="flex flex-col sm:flex-row sm:space-x-4 space-y-3 sm:space-y-0 bg-ink-50/80 p-1.5 rounded-2xl border border-ink-100/50 mb-6">
              {topLevelButtons.map((button) => {
                const isActive = activeSection === button.key;
                return (
                  <button
                    key={button.key}
                    onClick={() => handleTabChange(button.key)}
                    className={`flex-1 px-4 py-2.5 rounded-xl text-sm font-bold transition-all duration-200 border cursor-pointer ${
                      isActive
                        ? "bg-navy-100 text-navy-800 border-navy-200/60 shadow-xs"
                        : "bg-white text-ink-600 hover:text-navy-800 hover:bg-ink-50 border-ink-200"
                    }`}
                    aria-label={button.label}
                  >
                    {button.label}
                  </button>
                );
              })}
            </div>
          </div>

          {renderSection()}
        </div>
      </div>
    </div>
  );
}