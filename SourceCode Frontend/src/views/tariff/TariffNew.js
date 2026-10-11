// File path: src/views/tariff/TariffNew.js

import React, { useState, useEffect } from 'react';
import { ChevronRight, ChevronDown } from 'lucide-react';
import axios from 'axios';
// ✅ CHANGED: Using tmpTariffService now
import tmpTariffService from 'services/tmpTariffService';
import StatusBadge from 'components/StatusBadge/StatusBadge';
import { apiPath } from 'config';
import {
  initializeTariffStatus,
  getTariffStatus,
  setTariffStatus,
  getAllTariffStatuses,
  clearAllTariffStatuses,
} from 'utils/tariffStatusStorage';

export default function TariffNew() {
  const [expandedTariffs, setExpandedTariffs] = useState(new Set());
  const [expandedCategories, setExpandedCategories] = useState(new Set());
  const [tariffData, setTariffData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [tariffStatuses, setTariffStatuses] = useState({});

  // Fetch tariffs from API using the new service
  useEffect(() => {
    const fetchTariffs = async () => {
      try {
        setLoading(true);
        // ✅ CHANGED: Calling tmpTariffService to get active draft tariffs
        const data = await tmpTariffService.getAllTmpTariffs();
        console.log('Fetched tmp_tariff data:', data);

        const transformedData = transformToUIFormat(data);
        setTariffData(transformedData);

        // ✅ NEW: If no tariff records exist, clear all Active/Inactive statuses
        if (!transformedData || transformedData.length === 0) {
          console.log('No tariff records found - clearing all tariff statuses');
          clearAllTariffStatuses();
          setTariffStatuses({});
        } else {
          // ✅ SYNC DATABASE RECORD_STATUS TO STATE AND LOCALSTORAGE
          console.log('Syncing tariff statuses from database...');
          const statusMap = {};

          // Extract record_status from database response
          data.forEach((tariff) => {
            const tariffId = tariff.tariff;
            const recordStatus = tariff.recordStatus || 'A'; // Default to Active if missing
            statusMap[tariffId] = recordStatus;

            // Update localStorage to match database (database is source of truth)
            setTariffStatus(tariffId, recordStatus);
          });

          console.log('Database statuses synced:', statusMap);

          // Set React state from database values
          setTariffStatuses(statusMap);
        }
      } catch (error) {
        console.error('Error fetching tmp_tariffs:', error);
        alert('Failed to load draft tariffs. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    fetchTariffs();
  }, []);

  const formatDateToDDMMYYYY = (dateString) => {
    if (!dateString) return null;
    if (typeof dateString === 'string' && /^\d{2}\/\d{2}\/\d{4}$/.test(dateString)) {
      return dateString;
    }
    let date;
    if (typeof dateString === 'string' && dateString.includes('-')) {
      const parts = dateString.split('T')[0].split('-');
      date = new Date(parts[0], parts[1] - 1, parts[2]);
    } else {
      date = new Date(dateString);
    }
    if (isNaN(date.getTime())) {
      console.warn('Invalid date:', dateString);
      return null;
    }
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    return `${day}/${month}/${year}`;
  };

  // ===================================================================
// UPDATED transformToUIFormat FUNCTION - CORRECT DATABASE MAPPINGS
// ===================================================================
// Replace your existing transformToUIFormat function (around line 59-392) with this

function transformToUIFormat(apiData) {
  console.log('=== TARIFF SETUP TRANSFORMATION START ===');
  console.log('Raw tmp_tariff API data received:', apiData);
  const uiData = [];

  // ============================================================
  // 1. DOMESTIC (tariff 11)
  // ============================================================
  const domestic = apiData.find(t => t.tariff === 11);
  if (domestic) {
    console.log('Domestic (11) - Raw tmp_tariff data:', domestic);
    console.log('Domestic Block 1 Rate:', domestic.rate1, '(from tmp_tariff.rate1 column)');
    console.log('Domestic Block 1 Fixed Charge:', domestic.fixedCharge1, '(from tmp_tariff.fxdchg1 column)');

    uiData.push({
      id: 1,
      tariffName: 'Domestic',
      tariffId: 11,
      fromDate: formatDateToDDMMYYYY(domestic.fromDate || domestic.frmDate),
      toDate: formatDateToDDMMYYYY(domestic.toDate) || null,
      noOfSlabs: domestic.numberOfSlabs || 6,
      categories: [
        {
          id: 'cat1',
          name: 'Consumption 0-60 kWh Per Month',
          blocks: [
            { 
              id: 'block1', 
              name: 'Block 1: 0-30 kWh', 
              minCharge: domestic.minCharge || 0, 
              limit: domestic.limit1 || 0, 
              rate: domestic.rate1 || 0, 
              fixedCharge: domestic.fixedCharge1 || 0, 
              dbField: 'limit1,rate1,fixedCharge1'  // ✅ Maps to: lmt1, rate1, fxdchg1
            },
            { 
              id: 'block2', 
              name: 'Block 2: 31-60 kWh', 
              minCharge: domestic.minCharge || 0, 
              limit: domestic.limit2 || 0, 
              rate: domestic.rate2 || 0, 
              fixedCharge: domestic.fixedCharge2 || 0, 
              dbField: 'limit2,rate2,fixedCharge2'  // ✅ Maps to: lmt2, rate2, fxdchg2
            }
          ]
        }, 
        {
          id: 'cat2',
          name: 'Consumption above 60kWh per month',
          blocks: [
            // ⚠️ NOTE: Block 3 uses same fields as block 2 (you mentioned you'll decide on this later)
            // { 
            //   id: 'block3', 
            //   name: 'Block 1: 0-60 kWh', 
            //   minCharge: domestic.minCharge || 0, 
            //   limit: domestic.limit2 || 0, 
            //   rate: domestic.rate2 || 0, 
            //   fixedCharge: domestic.fixedCharge2 || 0, 
            //   dbField: 'limit2,rate2,fixedCharge2'  
            // },
            { 
              id: 'block3', 
              name: 'Block 2: 61-90 kWh', 
              minCharge: domestic.minCharge || 0, 
              limit: domestic.limit3 || 0, 
              rate: domestic.rate3 || 0, 
              fixedCharge: domestic.fixedCharge3 || 0, 
              dbField: 'limit3,rate3,fixedCharge3'  // ✅ Maps to: lmt3, rate3, fxdchg3
            },
            { 
              id: 'block4', 
              name: 'Block 3: 91-120 kWh', 
              minCharge: domestic.minCharge || 0, 
              limit: domestic.limit4 || 0, 
              rate: domestic.rate4 || 0, 
              fixedCharge: domestic.fixedCharge4 || 0, 
              dbField: 'limit4,rate4,fixedCharge4'  // ✅ Maps to: lmt4, rate4, fxdchg4
            },
            { 
              id: 'block5', 
              name: 'Block 4: 121-180 kWh', 
              minCharge: domestic.minCharge || 0, 
              limit: domestic.limit5 || 0, 
              rate: domestic.rate5 || 0, 
              fixedCharge: domestic.fixedCharge5 || 0, 
              dbField: 'limit5,rate5,fixedCharge5'  // ✅ Maps to: lmt5, rate5, fxdchg5
            },
            { 
              id: 'block6', 
              name: 'Block 5: Above 180 kWh', 
              minCharge: domestic.minCharge || 0, 
              limit: domestic.limit6 || 0, 
              rate: domestic.rate6 || 0, 
              fixedCharge: domestic.fixedCharge6 || 0, 
              dbField: 'limit6,rate6,fixedCharge6'  // ✅ Maps to: lmt6, rate6, fxdchg6
            }
          ]
        }
      ]
    });
  }

  // ============================================================
  // 2. DOMESTIC TIME OF USE (tariffs 13, 14, 15)
  // ============================================================
  const peak = apiData.find(t => t.tariff === 13);
  const day = apiData.find(t => t.tariff === 14);
  const offPeak = apiData.find(t => t.tariff === 15);

  if (peak || day || offPeak) {
    console.log('Domestic TOU - Peak (13) tmp_tariff:', peak);
    console.log('Domestic TOU - Day (14) tmp_tariff:', day);
    console.log('Domestic TOU - Off Peak (15) tmp_tariff:', offPeak);
    console.log('Day Fixed Charge:', day?.fixedCharge1, '(from tmp_tariff tariff 14 fxdchg1)');
    console.log('Off Peak Fixed Charge:', offPeak?.fixedCharge1, '(from tmp_tariff tariff 15 fxdchg1)');

    uiData.push({
      id: 2,
      tariffName: 'Domestic Time of Use',
      tariffId: [13, 14, 15],
      fromDate: formatDateToDDMMYYYY(peak?.fromDate || peak?.frmDate || day?.fromDate || day?.frmDate || offPeak?.fromDate || offPeak?.frmDate),
      toDate: formatDateToDDMMYYYY(peak?.toDate || day?.toDate || offPeak?.toDate) || null,
      noOfSlabs: 1,
      categories: [
        {
          id: 'cat-tou',
          name: 'Time of Use Tariff',
          blocks: [
            {
              id: 'block-peak',
              name: 'Peak [18:30 to 22:30] - 13',
              minCharge: peak?.minCharge || 0,
              limit: peak?.limit1 || 0,
              rate: peak?.rate1 || 0,
              fixedCharge: peak?.fixedCharge1 || 0,
              tariffId: 13,
              dbField: 'rate1,fixedCharge1'  // ✅ Tariff 13: rate1, fxdchg1
            },
            {
              id: 'block-day',
              name: 'Day [05:30 to 18:30] - 14',
              minCharge: day?.minCharge || 0,
              limit: day?.limit1 || 0,
              rate: day?.rate1 || 0,
              fixedCharge: day?.fixedCharge1 || 0,
              tariffId: 14,
              dbField: 'rate1,fixedCharge1'  // ✅ Tariff 14: rate1, fxdchg1
            },
            {
              id: 'block-offpeak',
              name: 'Off Peak [22:30 to 05:30] - 15',
              minCharge: offPeak?.minCharge || 0,
              limit: offPeak?.limit1 || 0,
              rate: offPeak?.rate1 || 0,
              fixedCharge: offPeak?.fixedCharge1 || 0,
              tariffId: 15,
              dbField: 'rate1,fixedCharge1'  // ✅ Tariff 15: rate1, fxdchg1
            }
          ]
        }
      ]
    });
  }

  // ============================================================
  // 3. RELIGIOUS & CHARITABLE (tariff 51)
  // ============================================================
  const religious = apiData.find(t => t.tariff === 51);
  if (religious) {
    uiData.push({
      id: 3,
      tariffName: 'Religious & Charitable',
      tariffId: 51,
      fromDate: formatDateToDDMMYYYY(religious.fromDate || religious.frmDate),
      toDate: formatDateToDDMMYYYY(religious.toDate) || null,
      noOfSlabs: 5,
      categories: [
        {
          id: 'cat1',
          name: 'Religious & Charitable Blocks',
          blocks: [
            { 
              id: 'block1', 
              name: 'Block 1: 0-30 kWh', 
              minCharge: religious.minCharge || 0, 
              limit: religious.limit1 || 0, 
              rate: religious.rate1 || 0, 
              fixedCharge: religious.fixedCharge1 || 0, 
              dbField: 'limit1,rate1,fixedCharge1'  // ✅ Tariff 51: lmt1, rate1, fxdchg1
            },
            { 
              id: 'block2', 
              name: 'Block 2: 31-90 kWh', 
              minCharge: religious.minCharge || 0, 
              limit: religious.limit2 || 0, 
              rate: religious.rate2 || 0, 
              fixedCharge: religious.fixedCharge2 || 0, 
              dbField: 'limit2,rate2,fixedCharge2'  // ✅ Tariff 51: lmt2, rate2, fxdchg2
            },
            { 
              id: 'block3', 
              name: 'Block 3: 91-120 kWh', 
              minCharge: religious.minCharge || 0, 
              limit: religious.limit3 || 0, 
              rate: religious.rate3 || 0, 
              fixedCharge: religious.fixedCharge3 || 0, 
              dbField: 'limit3,rate3,fixedCharge3'  // ✅ Tariff 51: lmt3, rate3, fxdchg3
            },
            { 
              id: 'block4', 
              name: 'Block 4: 121-180 kWh', 
              minCharge: religious.minCharge || 0, 
              limit: religious.limit4 || 0, 
              rate: religious.rate4 || 0, 
              fixedCharge: religious.fixedCharge4 || 0, 
              dbField: 'limit4,rate4,fixedCharge4'  // ✅ Tariff 51: lmt4, rate4, fxdchg4
            },
            { 
              id: 'block5', 
              name: 'Block 5: Above 180 kWh', 
              minCharge: religious.minCharge || 0, 
              limit: religious.limit5 || 0, 
              rate: religious.rate5 || 0, 
              fixedCharge: religious.fixedCharge5 || 0, 
              dbField: 'limit5,rate5,fixedCharge5'  // ✅ Tariff 51: lmt5, rate5, fxdchg5
            }
          ]
        }
      ]
    });
  }

  // ============================================================
  // 4. OTHER CONSUMERS (Industrial, Hotel, General Purpose, Government)
  // ============================================================
  const ind21 = apiData.find(t => t.tariff === 21);
  const ind22 = apiData.find(t => t.tariff === 22);
  const hotel41 = apiData.find(t => t.tariff === 41);
  const hotel42 = apiData.find(t => t.tariff === 42);
  const gp31 = apiData.find(t => t.tariff === 31);
  const gp32 = apiData.find(t => t.tariff === 32);
  const gov33 = apiData.find(t => t.tariff === 33);
  const gov34 = apiData.find(t => t.tariff === 34);

  if (ind21 || ind22 || hotel41 || hotel42 || gp31 || gp32 || gov33 || gov34) {
    console.log('Other Consumers tmp_tariff - Industrial (21, 22):', ind21, ind22);
    console.log('Industrial Energy Rate:', ind21?.rate1, '(from tmp_tariff 21 rate1)');
    console.log('Industrial Energy Fixed:', ind22?.rate2, '(from tmp_tariff 22 rate2)');
    console.log('Industrial Fixed Rate:', ind21?.fixedCharge1, '(from tmp_tariff 21 fxdchg1)');
    console.log('Industrial Fixed Fixed:', ind22?.fixedCharge2, '(from tmp_tariff 22 fxdchg2)');

    const otherRef = ind21 || ind22 || hotel41 || hotel42 || gp31 || gp32 || gov33 || gov34;
    uiData.push({
      id: 4,
      tariffName: 'Other Consumers',
      tariffId: [21, 22, 41, 42, 31, 32, 33, 34],
      fromDate: formatDateToDDMMYYYY(otherRef.fromDate || otherRef.frmDate),
      toDate: formatDateToDDMMYYYY(otherRef.toDate) || null,
      noOfSlabs: 2,
      categories: [
        {
          id: 'cat1',
          name: 'Industrial  -  21, 22',
          subcategories: [
            {
              id: 'cat1-1',
              name: 'Industrial 1',
              blocks: [
                { 
                  id: 'block1', 
                  name: 'Energy Charge (LKR/kWh)', 
                  rate: ind21?.rate1 || 0, 
                  fixedCharge: ind22?.rate2 || 0, 
                  tariffId: 21,  // ✅ First value from tariff 21
                  dbField: 'rate1,rate2'  // ✅ Tariff 21: rate1, Tariff 22: rate2
                },
                { 
                  id: 'block2', 
                  name: 'Fixed Charge (LKR/month)', 
                  rate: ind21?.fixedCharge1 || 0, 
                  fixedCharge: ind22?.fixedCharge2 || 0, 
                  tariffId: 21,  // ✅ First value from tariff 21
                  dbField: 'fixedCharge1,fixedCharge2'  // ✅ Tariff 21: fxdchg1, Tariff 22: fxdchg2
                }
              ]
            }//,
            // {
            //   id: 'cat1-2',
            //   name: 'Industrial 2',
            //   blocks: [
            //     // ⚠️ TODO: You need to provide the exact database field mappings for Industrial 2
            //     { id: 'block3', name: 'Peak [18:30 to 22:30] (LKR/kWh)', rate: 28, fixedCharge: 28, dbField: 'rate1,fixedCharge1' },
            //     { id: 'block4', name: 'Day [05:30 to 18:30] (LKR/kWh)', rate: 15, fixedCharge: 15, dbField: 'rate2,fixedCharge2' },
            //     { id: 'block5', name: 'Off Peak [22:30 to 05:30] (LKR/kWh)', rate: 12, fixedCharge: 12, dbField: 'rate3,fixedCharge3' },
            //     { id: 'block6', name: 'Demand Charge (LKR/kVA)', rate: 1400, fixedCharge: 1400, dbField: 'rate4,fixedCharge4' },
            //     { id: 'block7', name: 'Fixed Charge (LKR/month)', rate: 5000, fixedCharge: 5000, dbField: 'rate5,fixedCharge5' }
            //   ]
            // },
            // {
            //   id: 'cat1-3',
            //   name: 'Industrial 3',
            //   blocks: [
            //     // ⚠️ TODO: You need to provide the exact database field mappings for Industrial 3
            //     { id: 'block8', name: 'Peak [18:30 to 22:30] (LKR/kWh)', rate: 27, fixedCharge: 27, dbField: 'rate1,fixedCharge1' },
            //     { id: 'block9', name: 'Day [05:30 to 18:30] (LKR/kWh)', rate: 14, fixedCharge: 14, dbField: 'rate2,fixedCharge2' },
            //     { id: 'block10', name: 'Off Peak [22:30 to 05:30] (LKR/kWh)', rate: 11, fixedCharge: 11, dbField: 'rate3,fixedCharge3' },
            //     { id: 'block11', name: 'Demand Charge (LKR/kVA)', rate: 1350, fixedCharge: 1350, dbField: 'rate4,fixedCharge4' },
            //     { id: 'block12', name: 'Fixed Charge (LKR/month)', rate: 5000, fixedCharge: 5000, dbField: 'rate5,fixedCharge5' }
            //   ]
            // }
          ]
        },
        {
          id: 'cat2',
          name: 'Hotel  -  41, 42',
          subcategories: [
            {
              id: 'cat2-1',
              name: 'Hotel 1',
              blocks: [
                { 
                  id: 'block13', 
                  name: 'Energy Charge (LKR/kWh)', 
                  rate: hotel41?.rate1 || 0, 
                  fixedCharge: hotel42?.rate2 || 0, 
                  tariffId: 41,  // ✅ First value from tariff 41
                  dbField: 'rate1,rate2'  // ✅ Tariff 41: rate1, Tariff 42: rate2
                },
                { 
                  id: 'block14', 
                  name: 'Fixed Charge (LKR/month)', 
                  rate: hotel41?.fixedCharge1 || 0, 
                  fixedCharge: hotel42?.fixedCharge2 || 0, 
                  tariffId: 41,  // ✅ First value from tariff 41
                  dbField: 'fixedCharge1,fixedCharge2'  // ✅ Tariff 41: fxdchg1, Tariff 42: fxdchg2
                }
              ]
            }//,
            // {
            //   id: 'cat2-2',
            //   name: 'Hotel 2',
            //   blocks: [
            //     // ⚠️ TODO: You need to provide the exact database field mappings for Hotel 2
            //     { id: 'block15', name: 'Peak [18:30 to 22:30] (LKR/kWh)', rate: 28, fixedCharge: 28, dbField: 'rate1,fixedCharge1' },
            //     { id: 'block16', name: 'Day [05:30 to 18:30] (LKR/kWh)', rate: 15, fixedCharge: 15, dbField: 'rate2,fixedCharge2' },
            //     { id: 'block17', name: 'Off Peak [22:30 to 05:30] (LKR/kWh)', rate: 12, fixedCharge: 12, dbField: 'rate3,fixedCharge3' },
            //     { id: 'block18', name: 'Demand Charge (LKR/kVA)', rate: 1400, fixedCharge: 1400, dbField: 'rate4,fixedCharge4' },
            //     { id: 'block19', name: 'Fixed Charge (LKR/month)', rate: 5000, fixedCharge: 5000, dbField: 'rate5,fixedCharge5' }
            //   ]
            // },
            // {
            //   id: 'cat2-3',
            //   name: 'Hotel 3',
            //   blocks: [
            //     // ⚠️ TODO: You need to provide the exact database field mappings for Hotel 3
            //     { id: 'block20', name: 'Peak [18:30 to 22:30] (LKR/kWh)', rate: 27, fixedCharge: 27, dbField: 'rate1,fixedCharge1' },
            //     { id: 'block21', name: 'Day [05:30 to 18:30] (LKR/kWh)', rate: 14, fixedCharge: 14, dbField: 'rate2,fixedCharge2' },
            //     { id: 'block22', name: 'Off Peak [22:30 to 05:30] (LKR/kWh)', rate: 11, fixedCharge: 11, dbField: 'rate3,fixedCharge3' },
            //     { id: 'block23', name: 'Demand Charge (LKR/kVA)', rate: 1350, fixedCharge: 1350, dbField: 'rate4,fixedCharge4' },
            //     { id: 'block24', name: 'Fixed Charge (LKR/month)', rate: 5000, fixedCharge: 5000, dbField: 'rate5,fixedCharge5' }
            //   ]
            // }
          ]
        },
        {
          id: 'cat3',
          name: 'General Purpose  -  31, 32',
          subcategories: [
            {
              id: 'cat3-1',
              name: 'General Purpose 1',
              blocks: [
                { 
                  id: 'block25', 
                  name: 'Energy Charge (LKR/kWh)', 
                  rate: gp31?.rate1 || 0, 
                  fixedCharge: gp32?.rate2 || 0, 
                  tariffId: 31,  // ✅ First value from tariff 31
                  dbField: 'rate1,rate2'  // ✅ Tariff 31: rate1, Tariff 32: rate2
                },
                { 
                  id: 'block26', 
                  name: 'Fixed Charge (LKR/month)', 
                  rate: gp31?.fixedCharge1 || 0, 
                  fixedCharge: gp32?.fixedCharge2 || 0, 
                  tariffId: 31,  // ✅ First value from tariff 31
                  dbField: 'fixedCharge1,fixedCharge2'  // ✅ Tariff 31: fxdchg1, Tariff 32: fxdchg2
                }
              ]
            }//,
            // {
            //   id: 'cat3-2',
            //   name: 'General Purpose 2',
            //   blocks: [
            //     // ⚠️ TODO: You need to provide the exact database field mappings for General Purpose 2
            //     { id: 'block27', name: 'Peak [18:30 to 22:30] (LKR/kWh)', rate: 47, fixedCharge: 47, dbField: 'rate1,fixedCharge1' },
            //     { id: 'block28', name: 'Day [05:30 to 18:30] (LKR/kWh)', rate: 41, fixedCharge: 41, dbField: 'rate2,fixedCharge2' },
            //     { id: 'block29', name: 'Off Peak [22:30 to 05:30] (LKR/kWh)', rate: 31, fixedCharge: 31, dbField: 'rate3,fixedCharge3' },
            //     { id: 'block30', name: 'Demand Charge (LKR/kVA)', rate: 1500, fixedCharge: 1500, dbField: 'rate4,fixedCharge4' },
            //     { id: 'block31', name: 'Fixed Charge (LKR/month)', rate: 5000, fixedCharge: 5000, dbField: 'rate5,fixedCharge5' }
            //   ]
            // },
            // {
            //   id: 'cat3-3',
            //   name: 'General Purpose 3',
            //   blocks: [
            //     // ⚠️ TODO: You need to provide the exact database field mappings for General Purpose 3
            //     { id: 'block32', name: 'Peak [18:30 to 22:30] (LKR/kWh)', rate: 46, fixedCharge: 46, dbField: 'rate1,fixedCharge1' },
            //     { id: 'block33', name: 'Day [05:30 to 18:30] (LKR/kWh)', rate: 39.5, fixedCharge: 39.5, dbField: 'rate2,fixedCharge2' },
            //     { id: 'block34', name: 'Off Peak [22:30 to 05:30] (LKR/kWh)', rate: 30, fixedCharge: 30, dbField: 'rate3,fixedCharge3' },
            //     { id: 'block35', name: 'Demand Charge (LKR/kVA)', rate: 1450, fixedCharge: 1450, dbField: 'rate4,fixedCharge4' },
            //     { id: 'block36', name: 'Fixed Charge (LKR/month)', rate: 5000, fixedCharge: 5000, dbField: 'rate5,fixedCharge5' }
            //   ]
            // }
          ]
        },
        {
          id: 'cat4',
          name: 'Government  -  33, 34',
          subcategories: [
            {
              id: 'cat4-1',
              name: 'Government 1',
              blocks: [
                { 
                  id: 'block37', 
                  name: 'Energy Charge (LKR/kWh)', 
                  rate: gov33?.rate1 || 0, 
                  fixedCharge: gov34?.rate2 || 0, 
                  tariffId: 33,  // ✅ First value from tariff 33
                  dbField: 'rate1,rate2'  // ✅ Tariff 33: rate1, Tariff 34: rate2
                },
                { 
                  id: 'block38', 
                  name: 'Fixed Charge (LKR/month)', 
                  rate: gov33?.fixedCharge1 || 0, 
                  fixedCharge: gov34?.fixedCharge2 || 0, 
                  tariffId: 33,  // ✅ First value from tariff 33
                  dbField: 'fixedCharge1,fixedCharge2'  // ✅ Tariff 33: fxdchg1, Tariff 34: fxdchg2
                }
              ]
            }//,
            // {
            //   id: 'cat4-2',
            //   name: 'Government 2',
            //   blocks: [
            //     // ⚠️ TODO: You need to provide the exact database field mappings for Government 2
            //     { id: 'block39', name: 'Peak [18:30 to 22:30] (LKR/kWh)', rate: 47, fixedCharge: 47, dbField: 'rate1,fixedCharge1' },
            //     { id: 'block40', name: 'Day [05:30 to 18:30] (LKR/kWh)', rate: 41, fixedCharge: 41, dbField: 'rate2,fixedCharge2' },
            //     { id: 'block41', name: 'Off Peak [22:30 to 05:30] (LKR/kWh)', rate: 31, fixedCharge: 31, dbField: 'rate3,fixedCharge3' },
            //     { id: 'block42', name: 'Demand Charge (LKR/kVA)', rate: 1500, fixedCharge: 1500, dbField: 'rate4,fixedCharge4' },
            //     { id: 'block43', name: 'Fixed Charge (LKR/month)', rate: 5000, fixedCharge: 5000, dbField: 'rate5,fixedCharge5' }
            //   ]
            // },
            // {
            //   id: 'cat4-3',
            //   name: 'Government 3',
            //   blocks: [
            //     // ⚠️ TODO: You need to provide the exact database field mappings for Government 3
            //     { id: 'block44', name: 'Peak [18:30 to 22:30] (LKR/kWh)', rate: 46, fixedCharge: 46, dbField: 'rate1,fixedCharge1' },
            //     { id: 'block45', name: 'Day [05:30 to 18:30] (LKR/kWh)', rate: 39.5, fixedCharge: 39.5, dbField: 'rate2,fixedCharge2' },
            //     { id: 'block46', name: 'Off Peak [22:30 to 05:30] (LKR/kWh)', rate: 30, fixedCharge: 30, dbField: 'rate3,fixedCharge3' },
            //     { id: 'block47', name: 'Demand Charge (LKR/kVA)', rate: 1450, fixedCharge: 1450, dbField: 'rate4,fixedCharge4' },
            //     { id: 'block48', name: 'Fixed Charge (LKR/month)', rate: 5000, fixedCharge: 5000, dbField: 'rate5,fixedCharge5' }
            //   ]
            // }
          ]
        }
      ]
    });
  }

  // ============================================================
  // 5. STREET LIGHTING (tariff 61)
  // ============================================================
  const street = apiData.find(t => t.tariff === 61);
  if (street) {
    uiData.push({
      id: 5,
      tariffName: 'Street Lighting',
      tariffId: 61,
      fromDate: formatDateToDDMMYYYY(street.fromDate || street.frmDate),
      toDate: formatDateToDDMMYYYY(street.toDate) || null,
      noOfSlabs: 1,
      categories: [
        {
          id: 'cat-street',
          name: 'Street Lighting Charge',
          blocks: [
            {
              id: 'block-street-1',
              name: 'Energy Charge (LKR/kWh)',
              minCharge: street.minCharge || 0,
              limit: street.limit1 || 0,
              rate: street.rate1 || 0,
              fixedCharge: street.rate1 || 0,  // ✅ Both use rate1
              dbField: 'rate1,rate1'  // ✅ Tariff 61: rate1 for both fields
            }
          ]
        }
      ]
    });
  }

  // ============================================================
  // 6. AGRICULTURE (tariffs 71, 72, 73)
  // ============================================================
  const agPeak = apiData.find(t => t.tariff === 71);
  const agDay = apiData.find(t => t.tariff === 72);
  const agOffPeak = apiData.find(t => t.tariff === 73);

  if (agPeak || agDay || agOffPeak) {
    console.log('Agriculture TOU tmp_tariff - Peak (71):', agPeak);
    console.log('Agriculture TOU tmp_tariff - Day (72):', agDay);
    console.log('Agriculture TOU tmp_tariff - Off Peak (73):', agOffPeak);
    console.log('Peak Rate:', agPeak?.rate1, '(from tmp_tariff 71 rate1)');
    console.log('Peak Fixed Charge:', agPeak?.fixedCharge1, '(from tmp_tariff 71 fxdchg1)');
    console.log('Day Rate:', agDay?.rate1, '(from tmp_tariff 72 rate1)');
    console.log('Day Fixed Charge:', agDay?.fixedCharge1, '(from tmp_tariff 72 fxdchg1)');
    console.log('Off Peak Rate:', agOffPeak?.rate1, '(from tmp_tariff 73 rate1)');
    console.log('Off Peak Fixed Charge:', agOffPeak?.fixedCharge1, '(from tmp_tariff 73 fxdchg1)');

    uiData.push({
      id: 6,
      tariffName: 'Agriculture: Optional Time of Use',
      tariffId: [71, 72, 73],
      fromDate: formatDateToDDMMYYYY(agPeak?.fromDate || agPeak?.frmDate || agDay?.fromDate || agDay?.frmDate || agOffPeak?.fromDate || agOffPeak?.frmDate),
      toDate: formatDateToDDMMYYYY(agPeak?.toDate || agDay?.toDate || agOffPeak?.toDate) || null,
      noOfSlabs: 1,
      categories: [
        {
          id: 'cat-agri',
          name: 'Agriculture Time of Use Tariff',
          blocks: [
            {
              id: 'block-ag-1',
              name: 'Peak [18:30 to 22:30] - 71',
              rate: agPeak?.rate1 || 0,
              fixedCharge: agPeak?.fixedCharge1 || 0,  // ✅ Tariff 71: rate1, fxdchg1
              tariffId: 71,
              dbField: 'rate1,fixedCharge1'  // ✅ Tariff 71: rate1, fxdchg1
            },
            {
              id: 'block-ag-2',
              name: 'Day [05:30 to 18:30] - 72',
              rate: agDay?.rate1 || 0,
              fixedCharge: agDay?.fixedCharge1 || 0,  // ✅ Tariff 72: rate1, fxdchg1
              tariffId: 72,
              dbField: 'rate1,fixedCharge1'  // ✅ Tariff 72: rate1, fxdchg1
            },
            {
              id: 'block-ag-3',
              name: 'Off Peak [22:30 to 05:30] - 73',
              rate: agOffPeak?.rate1 || 0,
              fixedCharge: agOffPeak?.fixedCharge1 || 0,  // ✅ Tariff 73: rate1, fxdchg1
              tariffId: 73,
              dbField: 'rate1,fixedCharge1'  // ✅ Tariff 73: rate1, fxdchg1
            }
          ]
        }
      ]
    });
  }

  // ============================================================
  // 7. EV CHARGING STATIONS (tariff 93)
  // ============================================================
  // const evCharging = apiData.find(t => t.tariff === 93);
  // if (evCharging) {
  //   uiData.push({
  //     id: 7,
  //     tariffName: 'EV Charging Stations',
  //     tariffId: 93,
  //     fromDate: formatDateToDDMMYYYY(evCharging.fromDate || evCharging.frmDate),
  //     toDate: formatDateToDDMMYYYY(evCharging.toDate) || null,
  //     noOfSlabs: 3,
  //     categories: [
  //       {
  //         id: 'cat-ev',
  //         name: 'EV Charging Stations Time',
  //         isEVCharging: true,
  //         blocks: [
  //           { 
  //             id: 'block-ev-1', 
  //             name: 'Peak (LKR/kWh)', 
  //             dcFastCharging: evCharging.rate1 || 0,  // ✅ DC Fast = rate1
  //             acLevel2Charging: evCharging.fixedCharge1 || 0,  // ✅ AC Level 2 = fxdchg1
  //             dbField: 'rate1,fixedCharge1'  // ✅ Tariff 93: rate1, fxdchg1
  //           },
  //           { 
  //             id: 'block-ev-2', 
  //             name: 'Day (LKR/kWh)', 
  //             dcFastCharging: evCharging.rate2 || 0,  // ✅ DC Fast = rate2
  //             acLevel2Charging: evCharging.fixedCharge2 || 0,  // ✅ AC Level 2 = fxdchg2
  //             dbField: 'rate2,fixedCharge2'  // ✅ Tariff 93: rate2, fxdchg2
  //           },
  //           { 
  //             id: 'block-ev-3', 
  //             name: 'Off Peak (LKR/kWh)', 
  //             dcFastCharging: evCharging.rate3 || 0,  // ✅ DC Fast = rate3
  //             acLevel2Charging: evCharging.fixedCharge3 || 0,  // ✅ AC Level 2 = fxdchg3
  //             dbField: 'rate3,fixedCharge3'  // ✅ Tariff 93: rate3, fxdchg3
  //           }
  //         ]
  //       }
  //     ]
  //   });
  // }

  console.log('=== TARIFF SETUP TRANSFORMATION COMPLETE ===');
  console.log('Transformed tmp_tariff UI data:', uiData);
  return uiData;
};

  const toggleTariff = (tariffId) => {
    const newExpanded = new Set(expandedTariffs);
    if (newExpanded.has(tariffId)) {
      newExpanded.delete(tariffId);
    } else {
      newExpanded.add(tariffId);
    }
    setExpandedTariffs(newExpanded);
  };

  const toggleCategory = (categoryId) => {
    const newExpanded = new Set(expandedCategories);
    if (newExpanded.has(categoryId)) {
      newExpanded.delete(categoryId);
    } else {
      newExpanded.add(categoryId);
    }
    setExpandedCategories(newExpanded);
  };

  // Handle tariff status toggle - NOW PERSISTS TO DATABASE
  const handleStatusToggle = async (tariffId) => {
    const currentStatus = tariffStatuses[tariffId] || 'A';
    const newStatus = currentStatus === 'A' ? 'I' : 'A';

    try {
      // ✅ Call API to update database
      console.log(`Updating tariff ${tariffId} status to: ${newStatus}`);
      await tmpTariffService.updateRecordStatus(tariffId, newStatus);
      console.log(`✅ Successfully updated tariff ${tariffId} in database`);

      // ✅ Update localStorage AFTER successful API call
      setTariffStatus(tariffId, newStatus);

      // ✅ Update React state for UI re-render
      setTariffStatuses((prevStatuses) => ({
        ...prevStatuses,
        [tariffId]: newStatus,
      }));
    } catch (error) {
      console.error(`❌ Failed to update tariff ${tariffId} status:`, error);
      alert(`Failed to update tariff status. Please try again.`);
    }
  };

  // ✅ CHANGED: This function now respects Active/Inactive status from localStorage
  const handleEndAllTariffs = async () => {
    try {
      console.log('=== STARTING END ALL TARIFFS WITH STATUS FILTERING ===');

      // Step 1: Read all tariff statuses from localStorage
      const allStatuses = getAllTariffStatuses();
      console.log('All tariff statuses from localStorage:', allStatuses);

      // Step 2: Extract all tariff IDs from tariffData
      const allTariffIds = [];
      tariffData.forEach((tariff) => {
        if (Array.isArray(tariff.tariffId)) {
          // Multi-tariff category (e.g., 13, 14, 15)
          allTariffIds.push(...tariff.tariffId);
        } else {
          // Single tariff category
          allTariffIds.push(tariff.tariffId);
        }
      });
      console.log('All tariff IDs from tariffData:', allTariffIds);

      // Step 3: Filter by status to separate Active and Inactive tariffs
      const activeTariffIds = allTariffIds.filter(id => {
        const status = allStatuses[id] || 'A'; // Default to Active if not found
        return status === 'A';
      });

      const inactiveTariffIds = allTariffIds.filter(id => {
        const status = allStatuses[id] || 'A';
        return status === 'I';
      });

      console.log('Active tariffs (to end):', activeTariffIds);
      console.log('Inactive tariffs (to skip):', inactiveTariffIds);

      // Step 4: Validate - if no Active tariffs, show warning and abort
      if (activeTariffIds.length === 0) {
        alert(
          '⚠️ NO ACTIVE TARIFFS\n\n' +
          'All tariffs are currently marked as Inactive.\n' +
          'Please mark at least one tariff as Active to proceed.'
        );
        return;
      }

      // Step 5: Show confirmation dialog with counts
      const confirmAction = window.confirm(
        '⚠️ EDIT ACTIVE TARIFFS?\n\n' +
        `Active tariffs (will be ended): ${activeTariffIds.length}\n` +
        `Inactive tariffs (will be the same): ${inactiveTariffIds.length}\n\n` +
        'This will:\n' +
        `1. Set to_date = today on ${activeTariffIds.length} Active tariff(s)\n` +
        `2. Create ${activeTariffIds.length} new empty tariff record(s)\n` +
        `3. Leave ${inactiveTariffIds.length} Inactive tariff(s) unchanged\n` +
        '4. Refresh the page with the new data\n\n' +
        'Do you want to continue?'
      );

      if (!confirmAction) return;

      // Step 6: Call service with only Active tariff IDs
      setLoading(true);
      console.log('=== CALLING END SELECTED TMP TARIFFS ===');
      console.log('Sending tariff IDs:', activeTariffIds);

      const response = await tmpTariffService.endSelectedTariffsAndCreateNew(activeTariffIds);
      console.log('✅ TmpTariff backend response:', response);

      // Step 7: Wait for database transaction
      console.log('⏳ Waiting for database transaction...');
      await new Promise(resolve => setTimeout(resolve, 1500));

      // Step 8: Fetch fresh data and update UI
      console.log('🔄 Fetching fresh tmp_tariff data...');
      const freshData = await tmpTariffService.getAllTmpTariffs();

      const transformedData = transformToUIFormat(freshData);
      setTariffData(transformedData);

      // Step 9: Re-initialize tariff statuses for new records
      // ✅ NEW: If no tariff records exist after ending, clear all statuses
      if (!transformedData || transformedData.length === 0) {
        console.log('No tariff records after ending - clearing all tariff statuses');
        clearAllTariffStatuses();
        setTariffStatuses({});
      } else {
        const newTariffIds = [];
        transformedData.forEach((tariff) => {
          if (Array.isArray(tariff.tariffId)) {
            newTariffIds.push(...tariff.tariffId);
          } else {
            newTariffIds.push(tariff.tariffId);
          }
        });
        initializeTariffStatus(newTariffIds);
        setTariffStatuses(getAllTariffStatuses());
      }

      setLoading(false);

      // Step 10: Show success message with counts
      alert(
        `✅ SUCCESS!\n\n` +
        `Ended: ${response.endedCount} Active tariff(s)\n` +
        `Skipped: ${inactiveTariffIds.length} Inactive tariff(s)\n` +
        `Created: ${response.createdCount} new tariff(s)\n\n` +
        `✔️ Inactive tariffs remain unchanged\n` +
        `✔️ New empty Active tariffs are ready for editing`
      );

    } catch (error) {
      setLoading(false);
      console.error('❌ Error ending draft tariffs:', error);
      console.error('Error details:', error.response?.data);
      alert('❌ Failed to end draft tariffs.\n\nError: ' + (error.response?.data?.message || error.message));
    }
  };

  // Handle PDF export
  const handleExportPDF = async () => {
    try {
      console.log('=== STARTING PDF EXPORT ===');
      setLoading(true);

      const response = await axios.get(
        apiPath('/api/tariff-reports/export-pdf'),
        {
          responseType: 'blob',
          headers: {
            'Authorization': 'Basic ' + btoa('user:admin123')
          }
        }
      );

      console.log('PDF response received, size:', response.data.size);

      // Create download link
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      const filename = `tariff_setup_${new Date().toISOString().slice(0,10)}.pdf`;
      link.setAttribute('download', filename);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);

      setLoading(false);
      console.log('✅ PDF export successful:', filename);
    } catch (error) {
      setLoading(false);
      console.error('❌ PDF export failed:', error);
      console.error('Error details:', error.response?.data);
      alert('❌ Failed to export PDF.\n\nError: ' + (error.message || 'Unknown error'));
    }
  };

  const handleTariffFieldChange = (tariffId, field, value) => {
    setTariffData(prevData =>
      prevData.map(tariff =>
        tariff.id === tariffId
          ? { ...tariff, [field]: value }
          : tariff
      )
    );
  };

  // ===================================================================
// UPDATED handleBlockInputChange FUNCTION
// ===================================================================
// Replace your existing handleBlockInputChange function (around line 472) with this

const handleBlockInputChange = async (tariff, block, field, value) => {
  try {
    // Validate fromDate
    if (!tariff.fromDate || !/^\d{2}\/\d{2}\/\d{4}$/.test(tariff.fromDate)) {
      alert('Error: Invalid or missing "From Date" for this tariff.');
      return;
    }

    // Convert DD/MM/YYYY to YYYY-MM-DD for backend
    const [day, month, year] = tariff.fromDate.split('/');
    const formattedFromDate = `${year}-${month}-${day}`;

    // Parse the dbField to get the correct database column names
    const dbFieldParts = block.dbField.split(',');

    // Map UI field to database field based on dbField length
    let fieldMapping;
    if (dbFieldParts.length === 3) {
      // Blocks with limit column (Domestic, Religious): limit, rate, fixedCharge
      fieldMapping = {
        minCharge: 'minCharge',
        limit: dbFieldParts[0],
        rate: dbFieldParts[1],
        fixedCharge: dbFieldParts[2]
      };
    } else {
      // Blocks without limit column (Other Consumers, TOU): rate, fixedCharge
      fieldMapping = {
        minCharge: 'minCharge',
        limit: 'limit1',
        rate: dbFieldParts[0],
        fixedCharge: dbFieldParts[1]
      };
    }

    const dbField = fieldMapping[field];
    if (!dbField) {
      console.error(`Invalid field "${field}" for block`, block);
      return;
    }

    // Determine the correct tariff ID to update
    // For dual-tariff blocks (Other Consumers), fixedCharge updates the second tariff
    let tariffIdToUpdate;
    const secondTariffId = getSecondTariffId(tariff.tariffName, block.id);

    if (secondTariffId && field === 'fixedCharge') {
      // For Other Consumers, fixedCharge is stored in the second tariff
      tariffIdToUpdate = secondTariffId;
    } else {
      // All other cases use the first/primary tariff
      tariffIdToUpdate = block.tariffId || (Array.isArray(tariff.tariffId) ? tariff.tariffId[0] : tariff.tariffId);
    }

    // Parse and prepare the value
    const parsedValue = parseFloat(value);
    const updateData = { [dbField]: isNaN(parsedValue) ? 0 : parsedValue };

    console.log('Updating tmp_tariff:', {
      tariffId: tariffIdToUpdate,
      fromDate: formattedFromDate,
      field: dbField,
      value: updateData[dbField],
      isDualTariff: !!secondTariffId,
      originalField: field
    });

    // Call the backend service
    await tmpTariffService.updateTmpTariff(tariffIdToUpdate, formattedFromDate, updateData);

    console.log('✅ Updated tmp_tariff successfully');

  } catch (error) {
    console.error('Error updating tmp_tariff block:', error);
    alert('Failed to update tariff. Please try again.');
  }
};

// Helper function to determine the second tariff ID for dual-tariff blocks
const getSecondTariffId = (tariffName, blockId) => {
  // For "Other Consumers" category, determine the second tariff
  if (tariffName === 'Other Consumers') {
    // Industrial 1: tariff 21 and 22
    if (blockId === 'block1' || blockId === 'block2') {
      return 22;
    }
    // Hotel 1: tariff 41 and 42
    if (blockId === 'block13' || blockId === 'block14') {
      return 42;
    }
    // General Purpose 1: tariff 31 and 32
    if (blockId === 'block25' || blockId === 'block26') {
      return 32;
    }
    // Government 1: tariff 33 and 34
    if (blockId === 'block37' || blockId === 'block38') {
      return 34;
    }
  }
  return null;
};

  const handleEVChargingInputChange = (tariffId, blockId, field, value) => {
    setTariffData(prevData =>
      prevData.map(tariff => {
        if (tariff.id === tariffId) {
          return {
            ...tariff,
            categories: tariff.categories.map(category => ({
              ...category,
              blocks: category.blocks.map(block =>
                block.id === blockId
                  ? { ...block, [field]: parseFloat(value) || 0 }
                  : block
              )
            }))
          };
        }
        return tariff;
      })
    );
  };

  // Helper function to check if category should hide min charge and limit columns
  const shouldHideMinChargeAndLimit = (categoryName) => {
    const categoriesToHide = [
      'Industrial 1', 'Industrial 2', 'Industrial 3',
      'Hotel 1', 'Hotel 2', 'Hotel 3',
      'General Purpose 1', 'General Purpose 2', 'General Purpose 3',
      'Government 1', 'Government 2', 'Government 3',
      'Agriculture Time of Use Tariff'
    ];
    return categoriesToHide.includes(categoryName);
  };

  // Helper function to check if category should use editable threshold headers
  const shouldUseEditableHeaders = (categoryName) => {
    return [
      'Industrial 1', 'Industrial 2', 'Industrial 3',
      'Hotel 1', 'Hotel 2', 'Hotel 3',
      'General Purpose 1', 'General Purpose 2', 'General Purpose 3',
      'Government 1', 'Government 2', 'Government 3'
    ].includes(categoryName);
  };

  // Helper function to get default threshold values based on category
  const getThresholdValues = (categoryName) => {
    if (['Industrial 1', 'Industrial 2', 'Industrial 3', 'Hotel 1', 'Hotel 2', 'Hotel 3'].includes(categoryName)) {
      return { lower: '<300', upper: '>300' };
    } else if (['General Purpose 1', 'General Purpose 2', 'General Purpose 3', 'Government 1', 'Government 2', 'Government 3'].includes(categoryName)) {
      return { lower: '<180', upper: '>180' };
    }
    return { lower: '', upper: '' };
  };

  // Helper function to get tariff IDs for Other Consumer subcategories
  const getSubcategoryTariffIds = (categoryName) => {
    const mapping = {
      'Industrial 1': { first: 21, second: 22 },
      'Hotel 1': { first: 41, second: 42 },
      'General Purpose 1': { first: 31, second: 32 },
      'Government 1': { first: 33, second: 34 }
    };
    return mapping[categoryName] || null;
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen bg-ink-100">
        <div className="text-ink-500">Loading draft tariff data...</div>
      </div>
    );
  }

  return (
    <div className="flex justify-center items-start min-h-screen bg-ink-100 rounded-lg">
      <div className="ds-card p-ds-5 pb-ds-8 w-full max-w-7xl">
        
        <div className="flex justify-between items-center mb-4">
          <h2 className="ds-section-title">Tariff Records</h2>
          <div className="flex gap-2">
            <button
              onClick={handleExportPDF}
              disabled={loading}
              className="px-4 py-2 bg-navy-600 text-white rounded hover:bg-navy-700 disabled:bg-ink-400 disabled:cursor-not-allowed transition-colors duration-200 text-sm font-semibold"
            >
              {loading ? 'Exporting...' : 'Export to PDF'}
            </button>
            <button
              onClick={handleEndAllTariffs}
              className="ds-btn ds-btn-primary"
            >
              Edit All Tariffs
            </button>
          </div>
        </div>

        <div className="overflow-x-auto rounded-lg shadow-sm">
          <table className="w-full border-collapse bg-white">
            <thead className="bg-brandred text-white">
              <tr>
                <th className="px-4 py-3 text-center font-semibold text-sm border-b-2 border-brandred-dark" style={{ fontSize: "13px", minWidth: "50px" }}></th>
                <th className="px-4 py-3 text-center font-semibold text-sm border-b-2 border-brandred-dark" style={{ fontSize: "13px", minWidth: "280px" }}>Tariff Name</th>
                <th className="px-4 py-3 text-center font-semibold text-sm border-b-2 border-brandred-dark" style={{ fontSize: "13px", minWidth: "230px" }}>Tariff ID</th>
                <th className="px-4 py-3 text-center font-semibold text-sm border-b-2 border-brandred-dark" style={{ fontSize: "13px", minWidth: "150px" }}>From Date</th>
                <th className="px-4 py-3 text-center font-semibold text-sm border-b-2 border-brandred-dark" style={{ fontSize: "13px", minWidth: "150px" }}>To Date</th>
                <th className="px-4 py-3 text-center font-semibold text-sm border-b-2 border-brandred-dark" style={{ fontSize: "13px", minWidth: "120px" }}>No of Slabs</th>
              </tr>
            </thead>
            <tbody className="bg-white">
              {tariffData.map((tariff, idx) => (
                <React.Fragment key={tariff.id}>
                  <tr className={`transition-colors duration-200 border-b-2 border-ink-300 ${idx % 2 === 0 ? 'bg-white hover:bg-ink-50' : 'bg-ink-50 hover:bg-ink-100'}`}>
                    <td className="px-0 py-7 text-center text-sm text-ink-700 align-middle">
                      {tariff.categories && tariff.categories.length > 0 ? (
                        <button
                          onClick={() => toggleTariff(tariff.id)}
                          className="text-brandred hover:text-brandred-dark transition-colors"
                        >
                          {expandedTariffs.has(tariff.id) ? (
                            <ChevronDown className="w-5 h-5" />
                          ) : (
                            <ChevronRight className="w-5 h-5" />
                          )}
                        </button>
                      ) : null}
                    </td>
                    <td className="px-4 py-7 text-sm text-ink-700 align-middle">
                      <div className="flex items-center justify-center gap-2">
                        <input
                          type="text"
                          value={tariff.tariffName}
                          readOnly
                          className="flex-1 px-1.5 py-1 border border-transparent rounded text-sm bg-ink-100 cursor-not-allowed"
                          style={{ textAlign: 'center' }}
                        />
                        {/* Show badge only for single-tariff categories */}
                        {!Array.isArray(tariff.tariffId) && (
                          <StatusBadge
                            status={tariffStatuses[tariff.tariffId] || 'A'}
                            onToggle={() => handleStatusToggle(tariff.tariffId)}
                          />
                        )}
                      </div>
                    </td>
                    <td className="px-4 py-7 text-sm text-ink-700 align-middle">
                      <input
                        type="text"
                        value={Array.isArray(tariff.tariffId) ? tariff.tariffId.join(', ') : tariff.tariffId}
                        readOnly
                        className="w-full px-1.5 py-1 border border-transparent rounded text-sm bg-ink-100 cursor-not-allowed"
                        style={{ textAlign: 'center' }}
                      />
                    </td>
                    <td className="px-4 py-7 text-sm text-ink-700 align-middle">
                      <input
                        type="text"
                        value={tariff.fromDate}
                        onChange={(e) => handleTariffFieldChange(tariff.id, 'fromDate', e.target.value)}
                        className="w-full px-1.5 py-1 border border-transparent rounded text-sm bg-transparent transition-all duration-200 hover:bg-ink-50 hover:border-ink-300 focus:outline-none focus:border-brandred focus:bg-white focus:shadow-sm focus:ring-2 focus:ring-brandred focus:ring-opacity-10"
                        style={{ textAlign: 'center' }}
                      />
                    </td>
                    <td className="px-4 py-7 text-sm text-ink-700 align-middle">
                      <input
                        type="text"
                        value={tariff.toDate || ''}
                        onChange={(e) => handleTariffFieldChange(tariff.id, 'toDate', e.target.value)}
                        placeholder="-"
                        className="w-full px-1.5 py-1 border border-transparent rounded text-sm bg-transparent transition-all duration-200 hover:bg-ink-50 hover:border-ink-300 focus:outline-none focus:border-brandred focus:bg-white focus:shadow-sm focus:ring-2 focus:ring-brandred focus:ring-opacity-10"
                        style={{ textAlign: 'center' }}
                      />
                    </td>
                    <td className="px-4 py-7 text-sm text-ink-700 align-middle">
                      <input
                        type="number"
                        value={tariff.noOfSlabs}
                        onChange={(e) => handleTariffFieldChange(tariff.id, 'noOfSlabs', e.target.value)}
                        className="w-full px-1.5 py-1 border border-transparent rounded text-sm bg-transparent transition-all duration-200 hover:bg-ink-50 hover:border-ink-300 focus:outline-none focus:border-brandred focus:bg-white focus:shadow-sm focus:ring-2 focus:ring-brandred focus:ring-opacity-10"
                        style={{ textAlign: 'center' }}
                      />
                    </td>
                  </tr>

                  {expandedTariffs.has(tariff.id) && tariff.categories && tariff.categories.length > 0 && (
                    <tr>
                      <td colSpan="6" className="bg-ink-50 p-4 border-b-2 border-ink-300">
                        <div className="space-y-4">
                          {tariff.categories.map((category) => (
                            <div key={category.id} className="ds-card overflow-hidden mb-ds-4">
                              <button
                                onClick={() => toggleCategory(category.id)}
                                className="w-full px-4 py-3 flex items-center justify-between hover:bg-ink-50 transition-colors bg-ink-50"
                              >
                                <span className="font-semibold text-brandred text-sm">{category.name}</span>
                                {expandedCategories.has(category.id) ? (
                                  <ChevronDown className="w-4 h-4 text-brandred" />
                                ) : (
                                  <ChevronRight className="w-4 h-4 text-brandred" />
                                )}
                              </button>

                              {expandedCategories.has(category.id) && (
                                <div className="pl-4 pr-4 pb-4">
                                  {/* Check if this is EV Charging category */}
                                  {category.isEVCharging ? (
                                    /* Special rendering for EV Charging with 2 columns */
                                    <div className="overflow-x-auto">
                                      <table className="w-full border-collapse">
                                        <thead className="bg-ink-100">
                                          <tr>
                                            <th className="ds-th px-3 py-2">Time Period</th>
                                            <th className="ds-th text-center px-3 py-2">DC Fast Charging (LKR/kWh)</th>
                                            <th className="ds-th text-center px-3 py-2">AC Level 2 Charging (LKR/kWh)</th>
                                          </tr>
                                        </thead>
                                        <tbody>
                                          {category.blocks.map((block, blockIdx) => (
                                            <tr key={block.id} className={blockIdx % 2 === 0 ? 'bg-white' : 'bg-ink-50'}>
                                              <td className="px-3 py-2 text-sm text-ink-700 border-b">
                                                <div className="flex items-center gap-2">
                                                  <span>{block.name}</span>
                                                  {/* Show badge for multi-tariff blocks */}
                                                  {block.tariffId && (
                                                    <StatusBadge
                                                      status={tariffStatuses[block.tariffId] || 'A'}
                                                      onToggle={() => handleStatusToggle(block.tariffId)}
                                                    />
                                                  )}
                                                </div>
                                              </td>
                                              <td className="px-3 py-2 text-sm text-ink-700 border-b">
                                                <input
                                                  type="number"
                                                  step="0.01"
                                                  value={block.dcFastCharging}
                                                  onChange={(e) => handleEVChargingInputChange(tariff.id, block.id, 'dcFastCharging', e.target.value)}
                                                  className="ds-input px-2 py-1.5 text-center"
                                                />
                                              </td>
                                              <td className="px-3 py-2 text-sm text-ink-700 border-b">
                                                <input
                                                  type="number"
                                                  step="0.01"
                                                  value={block.acLevel2Charging}
                                                  onChange={(e) => handleEVChargingInputChange(tariff.id, block.id, 'acLevel2Charging', e.target.value)}
                                                  className="ds-input px-2 py-1.5 text-center"
                                                />
                                              </td>
                                            </tr>
                                          ))}
                                        </tbody>
                                      </table>
                                    </div>
                                  ) : category.subcategories ? (
                                    /* Render subcategories if they exist */
                                    category.subcategories.map((subcategory) => (
                                      <div key={subcategory.id} className="bg-ink-50 rounded-lg border border-ink-200 overflow-hidden mb-3 mt-3">
                                        <button
                                          onClick={() => toggleCategory(subcategory.id)}
                                          className="w-full px-4 py-2 flex items-center justify-between hover:bg-ink-100 transition-colors"
                                        >
                                          <span className="font-semibold text-ink-700 text-sm">{subcategory.name}</span>
                                          {expandedCategories.has(subcategory.id) ? (
                                            <ChevronDown className="w-4 h-4 text-brandred" />
                                          ) : (
                                            <ChevronRight className="w-4 h-4 text-brandred" />
                                          )}
                                        </button>
                                        
                                        {expandedCategories.has(subcategory.id) && (
                                          <div className="overflow-x-auto">
                                            <table className="w-full border-collapse">
                                              <thead className="bg-ink-100">
                                                <tr>
                                                  <th className="ds-th px-3 py-2">Block Name</th>
                                                  {!shouldHideMinChargeAndLimit(subcategory.name) && (
                                                    <th className="ds-th text-center px-3 py-2">Min Charge (Rs)</th>
                                                  )}
                                                  {!shouldHideMinChargeAndLimit(subcategory.name) && (
                                                    <th className="ds-th text-center px-3 py-2">Limit (kWh)</th>
                                                  )}
                                                  {shouldUseEditableHeaders(subcategory.name) ? (
                                                    <>
                                                      <th className="ds-th text-center px-3 py-2">
                                                        <div className="flex flex-col items-center gap-1">
                                                          <input
                                                            type="text"
                                                            defaultValue={getThresholdValues(subcategory.name).lower}
                                                            className="w-20 px-2 py-1 border border-ink-300 rounded text-sm text-center font-semibold focus:outline-none focus:border-brandred focus:ring-1 focus:ring-brandred bg-white"
                                                          />
                                                          <div className="text-xs font-normal">(kWh/month)</div>
                                                          {(() => {
                                                            const tariffIds = getSubcategoryTariffIds(subcategory.name);
                                                            return tariffIds ? (
                                                              <StatusBadge
                                                                status={tariffStatuses[tariffIds.first] || 'A'}
                                                                onToggle={() => handleStatusToggle(tariffIds.first)}
                                                              />
                                                            ) : null;
                                                          })()}
                                                        </div>
                                                      </th>
                                                      <th className="ds-th text-center px-3 py-2">
                                                        <div className="flex flex-col items-center gap-1">
                                                          <input
                                                            type="text"
                                                            defaultValue={getThresholdValues(subcategory.name).upper}
                                                            className="w-20 px-2 py-1 border border-ink-300 rounded text-sm text-center font-semibold focus:outline-none focus:border-brandred focus:ring-1 focus:ring-brandred bg-white"
                                                          />
                                                          <div className="text-xs font-normal">(kWh/month)</div>
                                                          {(() => {
                                                            const tariffIds = getSubcategoryTariffIds(subcategory.name);
                                                            return tariffIds ? (
                                                              <StatusBadge
                                                                status={tariffStatuses[tariffIds.second] || 'A'}
                                                                onToggle={() => handleStatusToggle(tariffIds.second)}
                                                              />
                                                            ) : null;
                                                          })()}
                                                        </div>
                                                      </th>
                                                    </>
                                                  ) : (
                                                    <>
                                                      <th className="ds-th text-center px-3 py-2">Rate (Rs/kWh)</th>
                                                      <th className="ds-th text-center px-3 py-2">Fixed Charge (Rs)</th>
                                                    </>
                                                  )}
                                                </tr>
                                              </thead>
                                              <tbody>
                                                {subcategory.blocks.map((block, blockIdx) => (
                                                  <tr key={block.id} className={blockIdx % 2 === 0 ? 'bg-white' : 'bg-ink-50'}>
                                                    <td className="px-3 py-2 text-sm text-ink-700 border-b">
                                                      <div className="flex items-center gap-2">
                                                        <span>{block.name}</span>
                                                        {/* Show badge for multi-tariff blocks */}
                                                        {block.tariffId && !shouldUseEditableHeaders(subcategory.name) && (
                                                          <StatusBadge
                                                            status={tariffStatuses[block.tariffId] || 'A'}
                                                            onToggle={() => handleStatusToggle(block.tariffId)}
                                                          />
                                                        )}
                                                      </div>
                                                    </td>
                                                    {!shouldHideMinChargeAndLimit(subcategory.name) && (
                                                      <td className="px-3 py-2 text-sm text-ink-700 border-b">
                                                        <input
                                                          type="number"
                                                          step="0.01"
                                                          defaultValue={block.minCharge}
                                                          onChange={(e) => handleBlockInputChange(tariff, block, 'minCharge', e.target.value)}
                                                          className="ds-input px-2 py-1.5 text-center"
                                                        />
                                                      </td>
                                                    )}
                                                    {!shouldHideMinChargeAndLimit(subcategory.name) && (
                                                      <td className="px-3 py-2 text-sm text-ink-700 border-b">
                                                        <input
                                                          type="number"
                                                          defaultValue={block.limit}
                                                          onChange={(e) => handleBlockInputChange(tariff, block, 'limit', e.target.value)}
                                                          className="ds-input px-2 py-1.5 text-center"
                                                        />
                                                      </td>
                                                    )}
                                                    <td className="px-3 py-2 text-sm text-ink-700 border-b">
                                                      <input
                                                        type="number"
                                                        step="0.01"
                                                        defaultValue={block.rate}
                                                        onChange={(e) => handleBlockInputChange(tariff, block, 'rate', e.target.value)}
                                                        className="ds-input px-2 py-1.5 text-center"
                                                      />
                                                    </td>
                                                    <td className="px-3 py-2 text-sm text-ink-700 border-b">
                                                      <input
                                                        type="number"
                                                        step="0.01"
                                                        defaultValue={block.fixedCharge}
                                                        onChange={(e) => handleBlockInputChange(tariff, block, 'fixedCharge', e.target.value)}
                                                        className="ds-input px-2 py-1.5 text-center"
                                                      />
                                                    </td>
                                                  </tr>
                                                ))}
                                              </tbody>
                                            </table>
                                          </div>
                                        )}
                                      </div>
                                    ))
                                  ) : (
                                    /* Render blocks directly if no subcategories */
                                    <div className="overflow-x-auto">
                                      <table className="w-full border-collapse">
                                        <thead className="bg-ink-100">
                                          <tr>
                                            <th className="ds-th px-3 py-2" style={{ minWidth: "230px" }}>Block Name</th>
                                            {!shouldHideMinChargeAndLimit(category.name) && (
                                              <th className="ds-th text-center px-3 py-2">Min Charge (Rs)</th>
                                            )}
                                            {!shouldHideMinChargeAndLimit(category.name) && (
                                              <th className="ds-th text-center px-3 py-2">Limit (kWh)</th>
                                            )}
                                            {shouldUseEditableHeaders(category.name) ? (
                                              <>
                                                <th className="ds-th text-center px-3 py-2">
                                                  <input
                                                    type="text"
                                                    defaultValue={getThresholdValues(category.name).lower}
                                                    className="w-20 px-2 py-1 border border-ink-300 rounded text-sm text-center font-semibold focus:outline-none focus:border-brandred focus:ring-1 focus:ring-brandred bg-white"
                                                  />
                                                  <div className="text-xs font-normal mt-1">(kWh/month)</div>
                                                </th>
                                                <th className="ds-th text-center px-3 py-2">
                                                  <input
                                                    type="text"
                                                    defaultValue={getThresholdValues(category.name).upper}
                                                    className="w-20 px-2 py-1 border border-ink-300 rounded text-sm text-center font-semibold focus:outline-none focus:border-brandred focus:ring-1 focus:ring-brandred bg-white"
                                                  />
                                                  <div className="text-xs font-normal mt-1">(kWh/month)</div>
                                                </th>
                                              </>
                                            ) : (
                                              <>
                                                <th className="ds-th text-center px-3 py-2">Rate (Rs/kWh)</th>
                                                <th className="ds-th text-center px-3 py-2">Fixed Charge (Rs)</th>
                                              </>
                                            )}
                                          </tr>
                                        </thead>
                                        <tbody>
                                          {category.blocks.map((block, blockIdx) => (
                                            <tr key={block.id} className={blockIdx % 2 === 0 ? 'bg-white' : 'bg-ink-50'}>
                                              <td className="px-3 py-2 text-sm text-ink-700 border-b">
                                                <div className="flex items-center gap-2">
                                                  <span>{block.name}</span>
                                                  {/* Show badge for multi-tariff blocks */}
                                                  {block.tariffId && (
                                                    <StatusBadge
                                                      status={tariffStatuses[block.tariffId] || 'A'}
                                                      onToggle={() => handleStatusToggle(block.tariffId)}
                                                    />
                                                  )}
                                                </div>
                                              </td>
                                              {!shouldHideMinChargeAndLimit(category.name) && (
                                                <td className="px-3 py-2 text-sm text-ink-700 border-b">
                                                  <input
                                                    type="number"
                                                    step="0.01"
                                                    defaultValue={block.minCharge}
                                                    onChange={(e) => handleBlockInputChange(tariff, block, 'minCharge', e.target.value)}
                                                    className="ds-input px-2 py-1.5 text-center"
                                                  />
                                                </td>
                                              )}
                                              {!shouldHideMinChargeAndLimit(category.name) && (
                                                <td className="px-3 py-2 text-sm text-ink-700 border-b">
                                                  <input
                                                    type="number"
                                                    defaultValue={block.limit}
                                                    onChange={(e) => handleBlockInputChange(tariff, block, 'limit', e.target.value)}
                                                    className="ds-input px-2 py-1.5 text-center"
                                                  />
                                                </td>
                                              )}
                                              <td className="px-3 py-2 text-sm text-ink-700 border-b">
                                                <input
                                                  type="number"
                                                  step="0.01"
                                                  defaultValue={block.rate}
                                                  onChange={(e) => handleBlockInputChange(tariff, block, 'rate', e.target.value)}
                                                  className="ds-input px-2 py-1.5 text-center"
                                                />
                                              </td>
                                              <td className="px-3 py-2 text-sm text-ink-700 border-b">
                                                <input
                                                  type="number"
                                                  step="0.01"
                                                  defaultValue={block.fixedCharge}
                                                  onChange={(e) => handleBlockInputChange(tariff, block, 'fixedCharge', e.target.value)}
                                                  className="ds-input px-2 py-1.5 text-center"
                                                />
                                              </td>
                                            </tr>
                                          ))}
                                        </tbody>
                                      </table>
                                    </div>
                                  )}
                                </div>
                              )}
                            </div>
                          ))}
                        </div>
                      </td>
                    </tr>
                  )}
                </React.Fragment>
              ))}
            </tbody>
          </table>
        </div>

        <div className="flex justify-between items-center mt-6 mb-8">
          <div>
            <span className="text-sm text-ink-600">
              Showing {tariffData.length} of {tariffData.length} entries
            </span>
          </div>
        </div>
      </div>
    </div>
  );
}