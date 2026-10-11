import axios from 'axios';
import { getSelectedAreaCode, getBillCycleForArea } from './AreaAndBillService';

// IMPORTANT: Use localhost instead of 127.0.0.1 to avoid CORS issues
const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/HSB';

console.log('API Base URL:', API_BASE_URL);

const api = axios.create({
    baseURL: API_BASE_URL,
    withCredentials: false,
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
    },
    timeout: 20000, // 20 seconds timeout
});

// Basic auth interceptor
api.interceptors.request.use(
    (config) => {
        // Create base64 encoded credentials
        const username = 'user';
        const password = 'admin123';
        const credentials = btoa(`${username}:${password}`);

        config.headers.Authorization = `Basic ${credentials}`;
        console.log(`🌐 Making ${config.method.toUpperCase()} request to: ${config.baseURL}${config.url}`);
        return config;
    },
    (error) => {
        console.error('Request interceptor error:', error);
        return Promise.reject(error);
    }
);

// Response interceptor
api.interceptors.response.use(
    (response) => {
        console.log(`✅ Response from ${response.config.url}:`, response.status);
        return response;
    },
    (error) => {
        console.error('❌ Response error:', {
            code: error.code,
            message: error.message,
            url: error.config?.url,
            method: error.config?.method,
            timeout: error.config?.timeout,
        });

        if (error.code === 'ECONNABORTED') {
            error.userMessage = `Request timeout after ${error.config?.timeout}ms`;
            console.error('⚠️ Possible causes:');
            console.error('  1. Backend is not running');
            console.error('  2. Backend is slow to respond');
            console.error('  3. Network issues');
            console.error(`  4. Test URL: ${error.config?.baseURL}${error.config?.url}`);
        } else if (error.code === 'ERR_NETWORK') {
            error.userMessage = 'Network error - cannot reach backend';
        }

        return Promise.reject(error);
    }
);

export default {
    async testConnection() {
        try {
            console.log('🧪 Testing connection to backend...');
            // Use a short timeout for connection test
            const testAxios = axios.create({
                baseURL: API_BASE_URL,
                timeout: 5000,
            });

            const response = await testAxios.get('/api/journals/health');
            console.log('✅ Connection test successful:', response.status);
            return {
                success: true,
                status: response.status,
                data: response.data
            };
        } catch (error) {
            console.error('🔴 Connection test failed:', error.message);
            return {
                success: false,
                error: error.message,
                code: error.code,
                suggestion: 'Please check if Spring Boot backend is running'
            };
        }
    },

    async getAllJournals() {
        try {
            console.log('📋 Fetching journals for selected area and bill cycle...');

            // Get selected area code from storage
            const selectedAreaCode = getSelectedAreaCode();

            if (!selectedAreaCode) {
                console.warn('No area selected');
                return {
                    success: false,
                    message: 'Please select an area first',
                    data: []
                };
            }

            // Get bill cycle for selected area
            const selectedBillCycle = getBillCycleForArea(selectedAreaCode);

            if (!selectedBillCycle) {
                console.warn('No bill cycle found for selected area');
                return {
                    success: false,
                    message: 'No bill cycle found for selected area',
                    data: []
                };
            }

            console.log(`Fetching journals for area: ${selectedAreaCode}, bill cycle: ${selectedBillCycle}`);

            const response = await api.get('/api/journals', {
                params: {
                    area_code: selectedAreaCode,
                    bill_cycle: selectedBillCycle
                }
            });

            console.log('✅ Journals response received');
            return response.data;

        } catch (error) {
            console.error('❌ Error fetching journals:', error);

            // Don't wrap the error, just enhance it
            if (!error.userMessage) {
                error.userMessage = 'Failed to fetch journals from server';
            }

            throw error;
        }
    },

    async getJournalDetail(journal) {
        try {
            const id = typeof journal.jnlNo === 'string' ? parseInt(journal.jnlNo, 10) : journal.jnlNo;

            if (isNaN(id)) {
                throw new Error(`Invalid journal number: ${journal.jnlNo}`);
            }

            console.log(`🔍 Fetching journal details for ID: ${id}`, {
                accNbr: journal.accNbr,
                jnlType: journal.jnlType,
                adjustAmt: journal.adjustAmt
            });

            // Get selected area code from storage
            const selectedAreaCode = getSelectedAreaCode();
            const selectedBillCycle = getBillCycleForArea(selectedAreaCode);

            if (!selectedAreaCode || !selectedBillCycle) {
                throw new Error('Please select an area first');
            }

            const response = await api.get(`/api/journals/detail/${id}`, {
                params: {
                    accNbr: journal.accNbr,
                    jnlType: journal.jnlType,
                    adjustAmt: journal.adjustAmt,
                    area_code: selectedAreaCode,
                    bill_cycle: selectedBillCycle
                }
            });

            return response.data;

        } catch (error) {
            console.error(`❌ Error fetching journal detail:`, error);

            if (!error.userMessage) {
                error.userMessage = `Failed to fetch journal details`;
            }

            throw error;
        }
    },

    // ===================== NEW UPDATE METHOD =====================
    async updateJournal(journalData) {
        try {
            console.log('📝 Updating journal:', {
                jnlNo: journalData.jnlNo,
                accNbr: journalData.accNbr,
                jnlType: journalData.jnlType,
                adjustAmt: journalData.adjustAmt
            });

            // Prepare the update DTO
            const updateDTO = {
                jnlNo: journalData.jnlNo,
                accNbr: journalData.accNbr,
                areaCd: journalData.areaCd,
                addedBlcy: journalData.addedBlcy,
                jnlType: journalData.jnlType,
                adjustAmt: journalData.adjustAmt,
                adjustStat: journalData.adjustStat,
                authCode: journalData.authCode,
                docAttch: journalData.docAttch,
                confirmed: journalData.confirmed,
                userId: journalData.userId,
                editedUserId: 'USER' // You can set this dynamically based on logged-in user
            };

            const response = await api.put('/api/journals/update', updateDTO);

            console.log('✅ Journal updated successfully:', response.data);
            return response.data;

        } catch (error) {
            console.error('❌ Error updating journal:', error);

            // Log the detailed error response from backend
            if (error.response && error.response.data) {
                console.error('Backend error response:', error.response.data);
            }

            if (!error.userMessage) {
                error.userMessage = error.response?.data?.message || 'Failed to update journal';
            }

            throw error;
        }
    },

    async healthCheck() {
        return this.testConnection();
    },

    async getAllJournalTypes() {
        try {
            console.log('📋 Fetching journal types...');

            const response = await api.get('/api/journals/types');

            console.log('✅ Journal types response received');
            return response.data;

        } catch (error) {
            console.error('❌ Error fetching journal types:', error);

            if (!error.userMessage) {
                error.userMessage = 'Failed to fetch journal types from server';
            }

            throw error;
        }
    },

    async getAllJurnlAuth() {
        try {
            console.log('📋 Fetching jurnl_auth records...');

            const response = await api.get('/api/journals/jurnl-auth');

            console.log('✅ Jurnl_auth response received');
            return response.data;

        } catch (error) {
            console.error('❌ Error fetching jurnl_auth:', error);

            if (!error.userMessage) {
                error.userMessage = 'Failed to fetch jurnl_auth from server';
            }

            throw error;
        }
    },

    async createJournal(journalData) {
        try {
            console.log('📝 Creating new journal:', journalData);

            // Get session info from sessionStorage
            const sessionId = sessionStorage.getItem('session_id');
            const userId = sessionStorage.getItem('user_id');

            const response = await api.post('/api/journals/create', journalData, {
                headers: {
                    'X-Session-Id': sessionId,
                    'X-User-Id': userId
                }
            });

            console.log('✅ Journal created successfully:', response.data);
            return response.data;

        } catch (error) {
            console.error('❌ Error creating journal:', error);

            // Log the detailed error response from backend
            if (error.response && error.response.data) {
                console.error('Backend error response:', error.response.data);
            }

            if (!error.userMessage) {
                error.userMessage = error.response?.data?.message || 'Failed to create journal';
            }

            throw error;
        }
    },

    async getJournalsReport() {
        try {
            console.log('📊 Fetching journals report for user...');

            // Get session info from sessionStorage
            const sessionId = sessionStorage.getItem('session_id');
            const userId = sessionStorage.getItem('user_id');

            if (!sessionId || !userId) {
                throw new Error('Session information not found. Please log in again.');
            }

            // Get selected area code from storage
            const selectedAreaCode = getSelectedAreaCode();
            const selectedBillCycle = getBillCycleForArea(selectedAreaCode);

            if (!selectedAreaCode || !selectedBillCycle) {
                throw new Error('Please select an area first');
            }

            console.log('Selected area:', selectedAreaCode, 'Bill cycle:', selectedBillCycle);

            const response = await api.get('/api/journals/report', {
                params: {
                    session_id: sessionId,
                    user_id: userId,
                    area_code: selectedAreaCode,
                    bill_cycle: selectedBillCycle
                }
            });

            console.log('✅ Journals report received:', response.data.count + ' journals');
            return response.data;

        } catch (error) {
            console.error('❌ Error fetching journals report:', error);

            if (!error.userMessage) {
                error.userMessage = error.response?.data?.message || 'Failed to fetch journals report';
            }

            throw error;
        }
    },

    async getUnconfirmedJournals() {
        try {
            console.log('📊 Fetching unconfirmed journals...');

            // Get session info from sessionStorage
            const sessionId = sessionStorage.getItem('session_id');
            const userId = sessionStorage.getItem('user_id');

            if (!sessionId || !userId) {
                throw new Error('Session information not found. Please log in again.');
            }

            // Get selected area code from storage
            const selectedAreaCode = getSelectedAreaCode();
            const selectedBillCycle = getBillCycleForArea(selectedAreaCode);

            if (!selectedAreaCode || !selectedBillCycle) {
                throw new Error('Please select an area first');
            }

            console.log('Fetching unconfirmed journals for area:', selectedAreaCode, 'bill cycle:', selectedBillCycle);

            const response = await api.get('/api/journals/unconfirmed', {
                params: {
                    session_id: sessionId,
                    user_id: userId,
                    area_code: selectedAreaCode,
                    bill_cycle: selectedBillCycle
                }
            });

            console.log('✅ Unconfirmed journals received:', response.data.count + ' journals');
            return response.data;

        } catch (error) {
            console.error('❌ Error fetching unconfirmed journals:', error);

            if (!error.userMessage) {
                error.userMessage = error.response?.data?.message || 'Failed to fetch unconfirmed journals';
            }

            throw error;
        }
    },

    async confirmJournals(journals) {
        try {
            console.log('✅ Confirming journals:', journals.length);

            // Get session info from sessionStorage
            const sessionId = sessionStorage.getItem('session_id');
            const userId = sessionStorage.getItem('user_id');

            if (!sessionId || !userId) {
                throw new Error('Session information not found. Please log in again.');
            }

            const response = await api.post('/api/journals/confirm', journals, {
                headers: {
                    'X-Session-Id': sessionId,
                    'X-User-Id': userId
                }
            });

            console.log('✅ Journals confirmed successfully:', response.data);
            return response.data;

        } catch (error) {
            console.error('❌ Error confirming journals:', error);

            if (!error.userMessage) {
                error.userMessage = error.response?.data?.message || 'Failed to confirm journals';
            }

            throw error;
        }
    },

    async checkIfConfirmed(areaCode, billCycle) {
        try {
            console.log('✅ Checking if confirmed - Area:', areaCode, 'Bill Cycle:', billCycle);

            // Get session info from sessionStorage
            const sessionId = sessionStorage.getItem('session_id');
            const userId = sessionStorage.getItem('user_id');

            if (!sessionId || !userId) {
                throw new Error('Session information not found. Please log in again.');
            }

            const response = await api.get('/api/journals/check-confirmed', {
                params: {
                    areaCode,
                    billCycle
                },
                headers: {
                    'X-Session-Id': sessionId,
                    'X-User-Id': userId
                }
            });

            console.log('✅ Check confirmed response:', response.data);
            return response.data;

        } catch (error) {
            console.error('❌ Error checking confirmation status:', error);

            if (!error.userMessage) {
                error.userMessage = error.response?.data?.message || 'Failed to check confirmation status';
            }

            throw error;
        }
    },

    async updateJournal(journal) {
        try {
            console.log('✅ Updating journal:', journal);

            // Get session info from sessionStorage
            const sessionId = sessionStorage.getItem('session_id');
            const userId = sessionStorage.getItem('user_id');

            if (!sessionId || !userId) {
                throw new Error('Session information not found. Please log in again.');
            }

            const response = await api.put('/api/journals/update', journal, {
                headers: {
                    'X-Session-Id': sessionId,
                    'X-User-Id': userId
                }
            });

            console.log('✅ Journal updated successfully:', response.data);
            return response.data;

        } catch (error) {
            console.error('❌ Error updating journal:', error);

            if (!error.userMessage) {
                error.userMessage = error.response?.data?.message || 'Failed to update journal';
            }

            throw error;
        }
    }
};