import React, { useState, useEffect } from "react";
import { toast } from "react-toastify";
import {
  getAllRegions,
  getProvincesByRegion,
  getAreasByRegionAndProvince,
  transformRegionsToOptions,
  transformProvincesToOptions,
  transformAreasToOptions,
} from "services/locationService";

const AddAccountDialog = ({
  isOpen,
  onClose,
  onSave,
  account,
  loading,
  onFormEdit,
}) => {
  const [formData, setFormData] = useState({
    user_id: "",
    epf_num: "",
    user_name: "",
    user_cat: "",
    region_code: "",
    province_code: "",
    area_code: "",
  });
  const [initialFormData, setInitialFormData] = useState({});
  const [validationErrors, setValidationErrors] = useState({});
  const [regions, setRegions] = useState([]);
  const [provinces, setProvinces] = useState([]);
  const [areas, setAreas] = useState([]);
  const [locationLoading, setLocationLoading] = useState(false);
  const accountCategories = [
    { value: "Admin", label: "Admin" },
    { value: "Region User", label: "Region User" },
    { value: "Province User", label: "Province User" },
    { value: "Area User", label: "Area User" },
    { value: "Accountant Revenue", label: "Accountant Revenue" },
    { value: "Acc Assistance", label: "Acc Assistance" },
    { value: "Accountant Clark", label: "Accountant Clark" },
    { value: "Electrical Engineer", label: "Electrical Engineer (EE)" },
    { value: "Chief Engineer", label: "Chief Engineer (CE)" },
    { value: "Director", label: "Director" }
    ,
  ];
  // Load initial location data
  useEffect(() => {
    if (isOpen) {
      loadRegions();
    }
  }, [isOpen]);
  const loadRegions = async () => {
    try {
      setLocationLoading(true);
      const regionsData = await getAllRegions();
      const regionOptions = transformRegionsToOptions(regionsData);
      setRegions(regionOptions);
    } catch (error) {
      console.error("Error loading regions:", error);
      toast.error("Failed to load regions from database");
    } finally {
      setLocationLoading(false);
    }
  };
  const loadProvinces = async (regionCode) => {
    if (!regionCode) {
      setProvinces([]);
      setAreas([]);
      return;
    }
    try {
      setLocationLoading(true);
      const provincesData = await getProvincesByRegion(regionCode);
      const provinceOptions = transformProvincesToOptions(provincesData);
      setProvinces(provinceOptions);
      setAreas([]);
    } catch (error) {
      console.error("Error loading provinces:", error);
      toast.error("Failed to load provinces from database");
      setProvinces([]);
      setAreas([]);
    } finally {
      setLocationLoading(false);
    }
  };
  const loadAreas = async (regionCode, provinceCode) => {
    if (!regionCode || !provinceCode) {
      setAreas([]);
      return;
    }
    try {
      setLocationLoading(true);
      const areasData = await getAreasByRegionAndProvince(
        regionCode,
        provinceCode
      );
      const areaOptions = transformAreasToOptions(areasData);
      setAreas(areaOptions);
    } catch (error) {
      console.error("Error loading areas:", error);
      toast.error("Failed to load areas from database");
      setAreas([]);
    } finally {
      setLocationLoading(false);
    }
  };
  useEffect(() => {
    const initializeForm = async () => {
      if (account) {
        const rawCat = account.user_cat || "";
        const normalizedCat = (rawCat.toUpperCase() === "DIRECTOR" || rawCat.toUpperCase() === "DGM") ? "Director" : rawCat;
        const initialData = {
          user_id: account.user_id || "",
          epf_num: account.epf_num || "",
          user_name: account.user_name || "",
          user_cat: normalizedCat,
          region_code: account.region_code || "",
          province_code: account.province_code || "",
          area_code: account.area_code || "",
        };
        setFormData(initialData);
        setInitialFormData(initialData);
        try {
          if (account.region_code) {
            await loadProvinces(account.region_code);
            if (account.province_code) {
              await loadAreas(account.region_code, account.province_code);
            }
          }
        } catch (error) {
          console.error("Error loading location data for edit mode:", error);
        }
      } else {
        const initialData = {
          user_id: "",
          epf_num: "",
          user_name: "",
          user_cat: "",
          region_code: "",
          province_code: "",
          area_code: "",
        };
        setFormData(initialData);
        setInitialFormData(initialData);
        setProvinces([]);
        setAreas([]);
      }
      setValidationErrors({});
    };
    if (isOpen) {
      initializeForm();
    }
  }, [account, isOpen]);
  // Check if form has changed
  const hasFormChanged = () => {
    return JSON.stringify(formData) !== JSON.stringify(initialFormData);
  };
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prevState) => {
      const newState = {
        ...prevState,
        [name]: value,
      };
      if (name === "user_cat") {
        newState.region_code = "";
        newState.province_code = "";
        newState.area_code = "";
        setProvinces([]);
        setAreas([]);
      } else if (name === "region_code") {
        newState.province_code = "";
        newState.area_code = "";
        setAreas([]);
        if (value) {
          loadProvinces(value);
        } else {
          setProvinces([]);
        }
      } else if (name === "province_code") {
        newState.area_code = "";
        if (value && newState.region_code) {
          loadAreas(newState.region_code, value);
        } else {
          setAreas([]);
        }
      }
      return newState;
    });
    if (validationErrors[name]) {
      setValidationErrors((prev) => ({
        ...prev,
        [name]: "",
      }));
    }
    // Notify parent component about form edit
    if (onFormEdit && hasFormChanged()) {
      onFormEdit();
    }
  };
  const validateForm = () => {
    const errors = {};
    if (!account) {
      if (!formData.user_id.trim()) {
        errors.user_id = "User ID is required";
      } else if (formData.user_id.length < 3) {
        errors.user_id = "User ID must be at least 3 characters";
      } else if (formData.user_id.length > 10) {
        errors.user_id = "User ID must not exceed 10 characters";
      }
    }
    if (!formData.epf_num.trim()) {
      errors.epf_num = "EPF number is required";
    } else if (formData.epf_num.length < 3) {
      errors.epf_num = "EPF number must be at least 3 characters";
    } else if (formData.epf_num.length > 10) {
      errors.epf_num = "EPF number must not exceed 10 characters";
    }
    if (!formData.user_name.trim()) {
      errors.user_name = "User name is required";
    } else if (formData.user_name.length < 3) {
      errors.user_name = "User name must be at least 3 characters";
    } else if (formData.user_name.length > 20) {
      errors.user_name = "User name must not exceed 20 characters";
    }
    if (!formData.user_cat) {
      errors.user_cat = "User category is required";
    }
    if (formData.user_cat === "Region User") {
      if (!formData.region_code || formData.region_code.trim() === "") {
        errors.region_code = "Region is required for Region User";
      }
    } else if (
      formData.user_cat === "Province User" ||
      formData.user_cat === "Accountant Revenue" ||
      formData.user_cat === "Acc Assistance" ||
      formData.user_cat === "Accountant Clark"
    ) {
      if (!formData.region_code || formData.region_code.trim() === "") {
        errors.region_code = "Region is required for Province User";
      }
      if (!formData.province_code || formData.province_code.trim() === "") {
        errors.province_code = "Province is required for Province User";
      }
    } else if (formData.user_cat === "Area User") {
      if (!formData.region_code || formData.region_code.trim() === "") {
        errors.region_code = "Region is required for Area User";
      }
      if (!formData.province_code || formData.province_code.trim() === "") {
        errors.province_code = "Province is required for Area User";
      }
      if (!formData.area_code || formData.area_code.trim() === "") {
        errors.area_code = "Area is required for Area User";
      }
    }
    setValidationErrors(errors);
    if (Object.keys(errors).length > 0) {
      toast.error("Please fix the validation errors before submitting");
      return false;
    }
    return true;
  };
  const handleSubmit = (e) => {
    e.preventDefault();
    if (!validateForm()) {
      return;
    }
    const submitData = { ...formData };
    if (account) {
      delete submitData.user_id;
      delete submitData.epf_num;
    }
    if (submitData.user_cat === "Admin" ||
      submitData.user_cat === "Electrical Engineer" ||
      submitData.user_cat === "Chief Engineer" ||
      submitData.user_cat === "DGM" ||
      submitData.user_cat === "Director" ||
      submitData.user_cat === "DIRECTOR") {
      submitData.region_code = null;
      submitData.province_code = null;
      submitData.area_code = null;
    } else if (submitData.user_cat === "Region User") {
      submitData.province_code = null;
      submitData.area_code = null;
    } else if (
      submitData.user_cat === "Province User" ||
      submitData.user_cat === "Accountant Revenue" ||
      submitData.user_cat === "Acc Assistance" ||
      submitData.user_cat === "Accountant Clark"
    ) {
      submitData.region_code = submitData.region_code || null;
      submitData.province_code = submitData.province_code || null;
      submitData.area_code = null;
    }
    console.log("Submitting account data:", submitData);
    try {
      onSave(submitData);
    } catch (error) {
      console.error("Error submitting form:", error);
      toast.error("Failed to submit form data");
    }
  };
  const handleCancel = () => {
    // Check if there are unsaved changes
    if (hasFormChanged()) {
      if (
        window.confirm(
          "You have unsaved changes. Are you sure you want to cancel?"
        )
      ) {
        resetForm();
        onClose();
      }
    } else {
      resetForm();
      onClose();
    }
  };
  const resetForm = () => {
    setFormData({
      user_id: "",
      epf_num: "",
      user_name: "",
      user_cat: "",
      region_code: "",
      province_code: "",
      area_code: "",
    });
    setInitialFormData({});
    setValidationErrors({});
    setProvinces([]);
    setAreas([]);
  };
  if (!isOpen) return null;
  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-2 sm:p-4">
      <div className="ds-card w-full max-w-md mx-auto max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex justify-between items-center px-4 sm:px-6 py-4 border-b border-ink-200 sticky top-0 bg-white">
          <h3 className="ds-section-title">
            {account ? "Edit User Account" : "Add New User Account"}
            {hasFormChanged() && (
              <span className="ml-2 text-xs text-warning-600">
                (Unsaved changes)
              </span>
            )}
          </h3>
          <button
            onClick={handleCancel}
            disabled={loading}
            className="text-ink-400 hover:text-ink-600 disabled:text-ink-300 focus:outline-none"
          >
            <i className="fas fa-times text-xl"></i>
          </button>
        </div>
        {/* Form */}
        <form onSubmit={handleSubmit} className="px-4 sm:px-6 py-4">
          {/* User ID is immutable after account creation */}
          <div className="mb-4">
            <label className="block text-ink-600 text-sm font-medium mb-2">
              User ID {!account && <span className="text-critical-500">*</span>}
            </label>
            <input
              type="text"
              name="user_id"
              value={formData.user_id}
              onChange={handleChange}
              readOnly={Boolean(account)}
              disabled={Boolean(account) || loading}
              className={`w-full px-3 py-2 border rounded-md shadow-sm text-sm focus:outline-none focus:ring-2 focus:ring-success-500 focus:border-success-500 disabled:bg-ink-100 disabled:text-ink-500 disabled:cursor-not-allowed ${
                validationErrors.user_id ? "border-critical-500" : "border-ink-300"
              } ${account ? "bg-ink-100" : ""}`}
              placeholder="Enter user ID"
              maxLength="8"
              required={!account}
            />
            {validationErrors.user_id && (
              <p className="mt-1 text-xs text-critical-600">
                {validationErrors.user_id}
              </p>
            )}
          </div>
          {/* EPF Number */}
          <div className="mb-4">
            <label className="block text-ink-600 text-sm font-medium mb-2">
              EPF Number <span className="text-critical-500">*</span>
            </label>
            <input
              type="text"
              name="epf_num"
              value={formData.epf_num}
              onChange={handleChange}
              readOnly={Boolean(account)}
              disabled={Boolean(account) || loading}
              className={`w-full px-3 py-2 border rounded-md shadow-sm text-sm focus:outline-none focus:ring-2 focus:ring-success-500 focus:border-success-500 disabled:bg-ink-100 disabled:text-ink-500 disabled:cursor-not-allowed ${
                validationErrors.epf_num ? "border-critical-500" : "border-ink-300"
              }`}
              placeholder="Enter EPF number"
              maxLength="10"
              required
            />
            {validationErrors.epf_num && (
              <p className="mt-1 text-xs text-critical-600">
                {validationErrors.epf_num}
              </p>
            )}
          </div>
          {/* User Name */}
          <div className="mb-4">
            <label className="block text-ink-600 text-sm font-medium mb-2">
              User Name <span className="text-critical-500">*</span>
            </label>
            <input
              type="text"
              name="user_name"
              value={formData.user_name}
              onChange={handleChange}
              disabled={loading}
              className={`w-full px-3 py-2 border rounded-md shadow-sm text-sm focus:outline-none focus:ring-2 focus:ring-success-500 focus:border-success-500 disabled:bg-ink-100 ${
                validationErrors.user_name
                  ? "border-critical-500"
                  : "border-ink-300"
              }`}
              placeholder="Enter user name"
              maxLength="20"
              required
            />
            {validationErrors.user_name && (
              <p className="mt-1 text-xs text-critical-600">
                {validationErrors.user_name}
              </p>
            )}
          </div>
          {/* User Category */}
          <div className="mb-4">
            <label className="block text-ink-600 text-sm font-medium mb-2">
              User Category <span className="text-critical-500">*</span>
            </label>
            <select
              name="user_cat"
              value={formData.user_cat}
              onChange={handleChange}
              disabled={loading}
              className={`w-full px-3 py-2 border rounded-md shadow-sm text-sm focus:outline-none focus:ring-2 focus:ring-success-500 focus:border-success-500 disabled:bg-ink-100 ${
                validationErrors.user_cat ? "border-critical-500" : "border-ink-300"
              }`}
              required
            >
              <option value="">Select category</option>
              {accountCategories.map((category) => (
                <option key={category.value} value={category.value}>
                  {category.label}
                </option>
              ))}
            </select>
            {validationErrors.user_cat && (
              <p className="mt-1 text-xs text-critical-600">
                {validationErrors.user_cat}
              </p>
            )}
          </div>
          {/* Region selector - for Region, Province, and Area Users */}
          {(formData.user_cat === "Region User" ||
            formData.user_cat === "Province User" ||
            formData.user_cat === "Accountant Revenue" ||
            formData.user_cat === "Acc Assistance" ||
            formData.user_cat === "Accountant Clark" ||
            formData.user_cat === "Area User") && (
            <div className="mb-4">
              <label className="block text-ink-600 text-sm font-medium mb-2">
                Region <span className="text-critical-500">*</span>
              </label>
              <select
                name="region_code"
                value={formData.region_code}
                onChange={handleChange}
                disabled={loading || locationLoading}
                className={`w-full px-3 py-2 border rounded-md shadow-sm text-sm focus:outline-none focus:ring-2 focus:ring-success-500 focus:border-success-500 disabled:bg-ink-100 ${
                  validationErrors.region_code
                    ? "border-critical-500"
                    : "border-ink-300"
                }`}
                required
              >
                <option value="">
                  {locationLoading ? "Loading regions..." : "Select region"}
                </option>
                {regions.map((region) => (
                  <option key={region.value} value={region.value}>
                    {region.label}
                  </option>
                ))}
              </select>
              {validationErrors.region_code && (
                <p className="mt-1 text-xs text-critical-600">
                  {validationErrors.region_code}
                </p>
              )}
            </div>
          )}
          {/* Province selector - for Province and Area Users */}
          {(formData.user_cat === "Province User" ||
            formData.user_cat === "Accountant Revenue" ||
            formData.user_cat === "Acc Assistance" ||
            formData.user_cat === "Accountant Clark" ||
            formData.user_cat === "Area User") &&
            formData.region_code && (
              <div className="mb-4">
                <label className="block text-ink-600 text-sm font-medium mb-2">
                  Province <span className="text-critical-500">*</span>
                </label>
                <select
                  name="province_code"
                  value={formData.province_code}
                  onChange={handleChange}
                  disabled={loading || locationLoading}
                  className={`w-full px-3 py-2 border rounded-md shadow-sm text-sm focus:outline-none focus:ring-2 focus:ring-success-500 focus:border-success-500 disabled:bg-ink-100 ${
                    validationErrors.province_code
                      ? "border-critical-500"
                      : "border-ink-300"
                  }`}
                  required
                >
                  <option value="">
                    {locationLoading
                      ? "Loading provinces..."
                      : "Select province"}
                  </option>
                  {provinces.map((province) => (
                    <option key={province.value} value={province.value}>
                      {province.label}
                    </option>
                  ))}
                </select>
                {validationErrors.province_code && (
                  <p className="mt-1 text-xs text-critical-600">
                    {validationErrors.province_code}
                  </p>
                )}
              </div>
            )}
          {/* Area selector - for Area Users only */}
          {formData.user_cat === "Area User" && formData.province_code && (
            <div className="mb-4">
              <label className="block text-ink-600 text-sm font-medium mb-2">
                Area <span className="text-critical-500">*</span>
              </label>
              <select
                name="area_code"
                value={formData.area_code}
                onChange={handleChange}
                disabled={loading || locationLoading}
                className={`w-full px-3 py-2 border rounded-md shadow-sm text-sm focus:outline-none focus:ring-2 focus:ring-success-500 focus:border-success-500 disabled:bg-ink-100 ${
                  validationErrors.area_code
                    ? "border-critical-500"
                    : "border-ink-300"
                }`}
                required
              >
                <option value="">
                  {locationLoading ? "Loading areas..." : "Select area"}
                </option>
                {areas.map((area) => (
                  <option key={area.value} value={area.value}>
                    {area.label}
                  </option>
                ))}
              </select>
              {validationErrors.area_code && (
                <p className="mt-1 text-xs text-critical-600">
                  {validationErrors.area_code}
                </p>
              )}
            </div>
          )}
          {/* Buttons */}
          <div className="flex justify-end space-x-3 mt-6">
            <button
              type="button"
              onClick={handleCancel}
              disabled={loading}
              className="px-4 py-2 text-sm font-medium text-ink-700 bg-ink-100 hover:bg-ink-200 disabled:bg-ink-50 disabled:text-ink-400 rounded-md transition-colors duration-200 focus:outline-none"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading || locationLoading}
              className="px-4 py-2 text-sm font-medium text-white bg-success-500 hover:bg-success-600 disabled:bg-success-500 rounded-md transition-colors duration-200 flex items-center focus:outline-none"
            >
              {(loading || locationLoading) && (
                <div className="ds-spinner ds-spinner-sm ds-spinner-invert mr-2"></div>
              )}
              {account ? "Update User" : "Create User"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
export default AddAccountDialog;
