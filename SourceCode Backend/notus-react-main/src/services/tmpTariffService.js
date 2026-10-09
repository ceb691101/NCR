// src/services/tmpTariffService.js

import axios from "axios";

const API_BASE_URL = "http://10.128.1.59:8080/HSB/api/tmp-tariffs";

// Helper function to get auth config for axios requests
const getAuthConfig = () => {
  const sessionId = sessionStorage.getItem("session_id") || localStorage.getItem("session_id");
  const headers = {
    "Content-Type": "application/json",
    Authorization: "Basic " + btoa("user:admin123"),
  };
  if (sessionId) {
    headers["X-Session-Id"] = sessionId;
  }
  return { headers };
};

const tmpTariffService = {
  getAllTmpTariffs: async () => {
    try {
      const response = await axios.get(API_BASE_URL, getAuthConfig());
      return response.data;
    } catch (error) {
      console.error("Error fetching tmp tariffs:", error);
      throw error;
    }
  },

  getEndedTmpTariffs: async () => {
    try {
      const response = await axios.get(
        `${API_BASE_URL}/ended`,
        getAuthConfig()
      );
      return response.data;
    } catch (error) {
      console.error("Error fetching ended tmp tariffs:", error);
      throw error;
    }
  },

  updateTmpTariff: async (tariffId, fromDate, tariffData) => {
    try {
      const response = await axios.patch(
        `${API_BASE_URL}/${tariffId}?fromDate=${fromDate}`,
        tariffData,
        getAuthConfig()
      );
      return response.data;
    } catch (error) {
      console.error("Error updating tmp tariff:", error);
      throw error;
    }
  },

  saveTmpTariff: async (tariffData) => {
    try {
      const response = await axios.post(
        `${API_BASE_URL}/save`,
        tariffData,
        getAuthConfig()
      );
      return response.data;
    } catch (error) {
      console.error("Error saving tmp tariff:", error);
      throw error;
    }
  },

  endAllTariffsAndCreateNew: async () => {
    try {
      const response = await axios.post(
        `${API_BASE_URL}/end-all-and-create-new`,
        {},
        getAuthConfig()
      );
      return response.data;
    } catch (error) {
      console.error("Error ending all tmp tariffs:", error);
      throw error;
    }
  },

  endSelectedTariffsAndCreateNew: async (tariffIds) => {
    try {
      console.log("Ending selected tariffs:", tariffIds);
      const response = await axios.post(
        `${API_BASE_URL}/end-selected-and-create-new`,
        { tariffIds },
        getAuthConfig()
      );
      console.log("End selected tariffs response:", response.data);
      return response.data;
    } catch (error) {
      console.error("Error ending selected tmp tariffs:", error);
      throw error;
    }
  },

  reactivateAll: async () => {
    try {
      const response = await axios.post(
        `${API_BASE_URL}/reactivate-all`,
        {},
        getAuthConfig()
      );
      return response.data;
    } catch (error) {
      console.error("Error reactivating all tmp tariffs:", error);
      throw error;
    }
  },

  // Update record_status for a tariff (Active/Inactive toggle)
  updateRecordStatus: async (tariffId, recordStatus) => {
    try {
      console.log(
        `API call: PATCH ${API_BASE_URL}/${tariffId}/status?recordStatus=${recordStatus}`
      );
      const response = await axios.patch(
        `${API_BASE_URL}/${tariffId}/status?recordStatus=${recordStatus}`,
        {},
        getAuthConfig()
      );
      console.log("Update record status response:", response.data);
      return response.data;
    } catch (error) {
      console.error(
        `Error updating record status for tariff ${tariffId}:`,
        error
      );
      throw error;
    }
  },
};

export default tmpTariffService;
