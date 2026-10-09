import axios from "axios";
import { apiPath, getAuthHeaders } from "config";

const endpoint = apiPath("/api/tariff-setup");

const yearTariffSetupService = {
  getRows: () => axios.get(endpoint, { headers: getAuthHeaders() }),
  update: (folioNo, data) => axios.patch(`${endpoint}/${folioNo}`, data, { headers: getAuthHeaders() }),
  submit: (folioNo) => axios.post(`${endpoint}/${folioNo}/submit`, {}, { headers: getAuthHeaders() }),
  decide: (folioNo, approved) => axios.post(`${endpoint}/${folioNo}/decision?approved=${approved}`, {}, { headers: getAuthHeaders() }),
};

export default yearTariffSetupService;
