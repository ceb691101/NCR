import axios from "axios";

// Change this line to match your actual backend URL
const API_BASE_URL = "http://10.128.1.59:8080/HSB/api/tariffs";

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

const tariffService = {
  // Get all tariffs
  getAllTariffs: async () => {
    try {
      console.log("Fetching tariffs from:", API_BASE_URL);
      const response = await axios.get(API_BASE_URL, getAuthConfig());
      console.log("Tariffs fetched successfully:", response.data);
      return response.data;
    } catch (error) {
      console.error("Error fetching tariffs:", error);
      throw error;
    }
  },

  // Get only active tariffs (to_date = NULL)
  getActiveTariffs: async () => {
    try {
      console.log("Fetching active tariffs from:", `${API_BASE_URL}/active`);
      const response = await axios.get(
        `${API_BASE_URL}/active`,
        getAuthConfig()
      );
      console.log("Active tariffs fetched successfully:", response.data);
      return response.data;
    } catch (error) {
      console.error("Error fetching active tariffs:", error);
      throw error;
    }
  },

  // Get only ended tariffs (to_date != NULL)
  getEndedTariffs: async () => {
    try {
      console.log("Fetching ended tariffs from:", `${API_BASE_URL}/ended`);
      const response = await axios.get(
        `${API_BASE_URL}/ended`,
        getAuthConfig()
      );
      console.log("Ended tariffs fetched successfully:", response.data);
      return response.data;
    } catch (error) {
      console.error("Error fetching ended tariffs:", error);
      throw error;
    }
  },

  getTariffById: async (tariffId) => {
    try {
      console.log("Fetching tariff:", tariffId);
      const response = await axios.get(
        `${API_BASE_URL}/${tariffId}`,
        getAuthConfig()
      );
      console.log("Tariff fetched:", response.data);
      return response.data;
    } catch (error) {
      console.error("Error fetching tariff:", error);
      throw error;
    }
  },

  updateTariff: async (tariffId, tariffData) => {
    try {
      console.log("Updating tariff:", tariffId, tariffData);
      const response = await axios.patch(
        `${API_BASE_URL}/${tariffId}`,
        tariffData,
        getAuthConfig()
      );
      console.log("Tariff updated:", response.data);
      return response.data;
    } catch (error) {
      console.error("Error updating tariff:", error);
      throw error;
    }
  },

  createTariff: async (tariffData) => {
    try {
      console.log("Creating tariff:", tariffData);
      const response = await axios.post(
        `${API_BASE_URL}/save`,
        tariffData,
        getAuthConfig()
      );
      console.log("Tariff created:", response.data);
      return response.data;
    } catch (error) {
      console.error("Error creating tariff:", error);
      throw error;
    }
  },

  // Alias for createTariff - used by TariffNew.js
  saveTariff: async (tariffData) => {
    try {
      console.log("Saving new tariff:", tariffData);
      const response = await axios.post(
        `${API_BASE_URL}/save`,
        tariffData,
        getAuthConfig()
      );
      console.log("Tariff saved successfully:", response.data);
      return response.data;
    } catch (error) {
      console.error("Error saving tariff:", error);
      throw error;
    }
  },

  deleteTariff: async (tariffId) => {
    try {
      console.log("Deleting tariff:", tariffId);
      await axios.delete(`${API_BASE_URL}/${tariffId}`, getAuthConfig());
      console.log("Tariff deleted");
    } catch (error) {
      console.error("Error deleting tariff:", error);
      throw error;
    }
  },

  // NEW: End all tariffs and create new ones
  endAllTariffsAndCreateNew: async () => {
    try {
      console.log("=== ENDING ALL TARIFFS AND CREATING NEW ===");
      console.log(
        "Calling endpoint:",
        `${API_BASE_URL}/end-all-and-create-new`
      );

      const response = await axios.post(
        `${API_BASE_URL}/end-all-and-create-new`,
        {},
        getAuthConfig()
      );

      console.log("=== END ALL TARIFFS SUCCESS ===");
      console.log("Response:", response.data);

      return response.data;
    } catch (error) {
      console.error("=== END ALL TARIFFS ERROR ===");
      console.error("Error:", error);
      throw error;
    }
  },

  // NEW: Transfer live tariffs to tmp_tariff (Tariff Setup)
  transferToTmpTariff: async () => {
    try {
      console.log("=== TRANSFERRING TO TMP_TARIFF ===");
      console.log("Calling endpoint:", `${API_BASE_URL}/transfer-to-tmp`);

      const response = await axios.post(
        `${API_BASE_URL}/transfer-to-tmp`,
        {},
        getAuthConfig()
      );

      console.log("=== TRANSFER SUCCESS ===");
      console.log("Response:", response.data);

      return response.data;
    } catch (error) {
      console.error("=== TRANSFER ERROR ===");
      console.error("Error:", error);
      throw error;
    }
  },

  // NEW: End selected tariffs and create new ones (for Active/Inactive status)
  endSelectedTariffsAndCreateNew: async (tariffIds) => {
    try {
      console.log("=== ENDING SELECTED TARIFFS AND CREATING NEW ===");
      console.log("Tariff IDs to end:", tariffIds);
      console.log(
        "Calling endpoint:",
        `${API_BASE_URL}/end-selected-and-create-new`
      );

      const response = await axios.post(
        `${API_BASE_URL}/end-selected-and-create-new`,
        { tariffIds },
        getAuthConfig()
      );

      console.log("=== END SELECTED TARIFFS SUCCESS ===");
      console.log("Response:", response.data);

      return response.data;
    } catch (error) {
      console.error("=== END SELECTED TARIFFS ERROR ===");
      console.error("Error:", error);
      throw error;
    }
  },
};

export default tariffService;
