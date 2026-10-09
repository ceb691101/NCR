/**
 * Unit Tests for tariffStatusStorage utility
 */

import {
  initializeTariffStatus,
  getTariffStatus,
  setTariffStatus,
  getAllTariffStatuses,
} from '../tariffStatusStorage';

// Mock localStorage
const localStorageMock = (() => {
  let store = {};

  return {
    getItem: (key) => store[key] || null,
    setItem: (key, value) => {
      store[key] = value.toString();
    },
    clear: () => {
      store = {};
    },
    removeItem: (key) => {
      delete store[key];
    },
  };
})();

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
});

describe('tariffStatusStorage', () => {
  beforeEach(() => {
    // Clear localStorage before each test
    localStorage.clear();
  });

  describe('initializeTariffStatus', () => {
    test('should initialize tariff statuses with default "A" when storage is empty', () => {
      const tariffIds = [11, 13, 14, 15];
      initializeTariffStatus(tariffIds);

      const statuses = getAllTariffStatuses();
      expect(statuses).toEqual({
        '11': 'A',
        '13': 'A',
        '14': 'A',
        '15': 'A',
      });
    });

    test('should not overwrite existing statuses', () => {
      // Set initial status
      setTariffStatus(11, 'I');

      // Initialize with more tariff IDs
      initializeTariffStatus([11, 13, 14]);

      const statuses = getAllTariffStatuses();
      expect(statuses['11']).toBe('I'); // Should remain "I"
      expect(statuses['13']).toBe('A'); // New one should be "A"
      expect(statuses['14']).toBe('A'); // New one should be "A"
    });

    test('should handle empty array gracefully', () => {
      initializeTariffStatus([]);
      const statuses = getAllTariffStatuses();
      expect(statuses).toEqual({});
    });

    test('should handle non-array input gracefully', () => {
      initializeTariffStatus(null);
      const statuses = getAllTariffStatuses();
      expect(statuses).toEqual({});
    });
  });

  describe('getTariffStatus', () => {
    test('should retrieve correct status for existing tariff', () => {
      setTariffStatus(11, 'I');
      expect(getTariffStatus(11)).toBe('I');
    });

    test('should return default "A" for non-existent tariff', () => {
      expect(getTariffStatus(999)).toBe('A');
    });

    test('should handle string tariff IDs', () => {
      setTariffStatus('13', 'I');
      expect(getTariffStatus('13')).toBe('I');
      expect(getTariffStatus(13)).toBe('I'); // Should work with number too
    });
  });

  describe('setTariffStatus', () => {
    test('should update status for a tariff', () => {
      setTariffStatus(11, 'A');
      expect(getTariffStatus(11)).toBe('A');

      setTariffStatus(11, 'I');
      expect(getTariffStatus(11)).toBe('I');
    });

    test('should persist status in localStorage', () => {
      setTariffStatus(11, 'I');

      // Retrieve directly from localStorage
      const stored = JSON.parse(localStorage.getItem('tariff_status_map'));
      expect(stored['11']).toBe('I');
    });

    test('should reject invalid status values', () => {
      const consoleWarnSpy = jest.spyOn(console, 'warn').mockImplementation();

      setTariffStatus(11, 'INVALID');

      expect(consoleWarnSpy).toHaveBeenCalledWith(
        'Invalid status "INVALID". Must be "A" or "I"'
      );

      consoleWarnSpy.mockRestore();
    });

    test('should handle string and number tariff IDs', () => {
      setTariffStatus(11, 'A');
      setTariffStatus('13', 'I');

      expect(getTariffStatus(11)).toBe('A');
      expect(getTariffStatus('13')).toBe('I');
    });
  });

  describe('getAllTariffStatuses', () => {
    test('should return all statuses as object', () => {
      setTariffStatus(11, 'A');
      setTariffStatus(13, 'I');
      setTariffStatus(14, 'A');

      const statuses = getAllTariffStatuses();
      expect(statuses).toEqual({
        '11': 'A',
        '13': 'I',
        '14': 'A',
      });
    });

    test('should return empty object when no statuses exist', () => {
      const statuses = getAllTariffStatuses();
      expect(statuses).toEqual({});
    });

    test('should handle corrupted localStorage data gracefully', () => {
      const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation();

      // Set invalid JSON in localStorage
      localStorage.setItem('tariff_status_map', 'INVALID_JSON');

      const statuses = getAllTariffStatuses();
      expect(statuses).toEqual({});
      expect(consoleErrorSpy).toHaveBeenCalled();

      consoleErrorSpy.mockRestore();
    });
  });

  describe('Integration tests', () => {
    test('should handle complete workflow: initialize, get, set, toggle', () => {
      // Initialize tariffs
      initializeTariffStatus([11, 13, 14]);

      // Verify all initialized to "A"
      expect(getTariffStatus(11)).toBe('A');
      expect(getTariffStatus(13)).toBe('A');
      expect(getTariffStatus(14)).toBe('A');

      // Toggle tariff 13 to Inactive
      setTariffStatus(13, 'I');
      expect(getTariffStatus(13)).toBe('I');

      // Verify others remain Active
      expect(getTariffStatus(11)).toBe('A');
      expect(getTariffStatus(14)).toBe('A');

      // Toggle tariff 13 back to Active
      setTariffStatus(13, 'A');
      expect(getTariffStatus(13)).toBe('A');

      // Verify all statuses
      const allStatuses = getAllTariffStatuses();
      expect(allStatuses).toEqual({
        '11': 'A',
        '13': 'A',
        '14': 'A',
      });
    });
  });
});
