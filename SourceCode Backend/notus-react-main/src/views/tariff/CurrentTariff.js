// FINAL AND COMPLETE FILE: src/views/tariff/CurrentTariff.js
// Purpose: Display LIVE tariffs from the 'tariff' table.

import React, { useState, useEffect } from 'react';
import { ChevronRight, ChevronDown } from 'lucide-react';
import tariffService from '../../services/tariffService';
import { getAllTariffStatuses } from '../../utils/tariffStatusStorage';

export default function CurrentTariff() {
  const [expandedTariffs, setExpandedTariffs] = useState(new Set());
  const [expandedCategories, setExpandedCategories] = useState(new Set());
  
  const [tariffData, setTariffData] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchLiveTariffs = async () => {
    try {
      setLoading(true);
      // Fetch ONLY active tariffs (to_date = NULL)
      const data = await tariffService.getActiveTariffs();
      console.log('Fetched ACTIVE LIVE tariff data:', data);

      const transformedData = transformToUIFormat(data);
      setTariffData(transformedData);
    } catch (error) {
      console.error('Error fetching active live tariffs:', error);
      alert('Failed to load live tariffs. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLiveTariffs();
  }, []);

  const formatDateToDDMMYYYY = (dateString) => {
    if (!dateString) return null;
    if (typeof dateString === 'string' && /^\d{2}\/\d{2}\/\d{4}$/.test(dateString)) {
      return dateString;
    }
    const date = new Date(dateString);
    if (isNaN(date.getTime())) {
      return null;
    }
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    return `${day}/${month}/${year}`;
  };

  const transformToUIFormat = (apiData) => {
    console.log('=== CURRENT TARIFF TRANSFORMATION START ===');
    console.log('Raw API data received:', apiData);
    const uiData = [];

    // 1. Domestic (tariff 11)
    const domestic = apiData.find(t => t.tariff === 11);
    if (domestic) {
      console.log('Domestic (11) - Raw data:', domestic);
      console.log('Domestic Block 1 Rate:', domestic.rate1, '(from rate1 column)');
      console.log('Domestic Block 1 Fixed Charge:', domestic.fixedCharge1, '(from fxdchg1 column)');
      console.log('Domestic Block 1 Limit:', domestic.limit1, '(from lmt1 column)');
      console.log('Domestic Min Charge:', domestic.minCharge, '(from min_charg column)');

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
              { id: 'block1', name: 'Block 1: 0-30 kWh', minCharge: domestic.minCharge, limit: domestic.limit1, rate: domestic.rate1, fixedCharge: domestic.fixedCharge1 },
              { id: 'block2', name: 'Block 2: 31-60 kWh', minCharge: domestic.minCharge, limit: domestic.limit2, rate: domestic.rate2, fixedCharge: domestic.fixedCharge2 }
            ]
          },
          {
            id: 'cat2',
            name: 'Consumption above 60kWh per month',
            blocks: [
              // { id: 'block3', name: 'Block 1: 0-60 kWh', minCharge: domestic.minCharge, limit: domestic.limit2, rate: domestic.rate2, fixedCharge: domestic.fixedCharge2 },
              { id: 'block3', name: 'Block 2: 61-90 kWh', minCharge: domestic.minCharge, limit: domestic.limit3, rate: domestic.rate3, fixedCharge: domestic.fixedCharge3 },
              { id: 'block4', name: 'Block 3: 91-120 kWh', minCharge: domestic.minCharge, limit: domestic.limit4, rate: domestic.rate4, fixedCharge: domestic.fixedCharge4 },
              { id: 'block5', name: 'Block 4: 121-180 kWh', minCharge: domestic.minCharge, limit: domestic.limit5, rate: domestic.rate5, fixedCharge: domestic.fixedCharge5 },
              { id: 'block6', name: 'Block 5: Above 180 kWh', minCharge: domestic.minCharge, limit: domestic.limit6, rate: domestic.rate6, fixedCharge: domestic.fixedCharge6 }
            ]
          }
        ]
      });
    }

    // 2. Domestic Time of Use (tariff 13, 14, 15)
    const peak = apiData.find(t => t.tariff === 13);
    const day = apiData.find(t => t.tariff === 14);
    const offPeak = apiData.find(t => t.tariff === 15);
    if (peak || day || offPeak) {
      console.log('Domestic TOU - Peak (13):', peak);
      console.log('Domestic TOU - Day (14):', day);
      console.log('Domestic TOU - Off Peak (15):', offPeak);
      console.log('Peak Rate:', peak?.rate1, '(from tariff 13 rate1)');
      console.log('Peak Fixed Charge:', peak?.fixedCharge1, '(from tariff 13 fxdchg1)');
      console.log('Day Rate:', day?.rate1, '(from tariff 14 rate1)');
      console.log('Day Fixed Charge:', day?.fixedCharge1, '(from tariff 14 fxdchg1)');
      console.log('Off Peak Rate:', offPeak?.rate1, '(from tariff 15 rate1)');
      console.log('Off Peak Fixed Charge:', offPeak?.fixedCharge1, '(from tariff 15 fxdchg1)');

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
              { id: 'block-peak', name: 'Peak [18:30 to 22:30] - 13', minCharge: peak?.minCharge, limit: peak?.limit1, rate: peak?.rate1, fixedCharge: peak?.fixedCharge1 },
              { id: 'block-day', name: 'Day [05:30 to 18:30] - 14', minCharge: day?.minCharge, limit: day?.limit1, rate: day?.rate1, fixedCharge: day?.fixedCharge1 },
              { id: 'block-offpeak', name: 'Off Peak [22:30 to 05:30] - 15', minCharge: offPeak?.minCharge, limit: offPeak?.limit1, rate: offPeak?.rate1, fixedCharge: offPeak?.fixedCharge1 }
            ]
          }
        ]
      });
    }
    
    // 3. Religious & Charitable (tariff 51)
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
            id: 'cat-religious',
            name: 'Religious & Charitable Blocks',
            blocks: [
              { id: 'block1', name: 'Block 1: 0-30 kWh', minCharge: religious.minCharge, limit: religious.limit1, rate: religious.rate1, fixedCharge: religious.fixedCharge1 },
              { id: 'block2', name: 'Block 2: 31-90 kWh', minCharge: religious.minCharge, limit: religious.limit2, rate: religious.rate2, fixedCharge: religious.fixedCharge2 },
              { id: 'block3', name: 'Block 3: 91-120 kWh', minCharge: religious.minCharge, limit: religious.limit3, rate: religious.rate3, fixedCharge: religious.fixedCharge3 },
              { id: 'block4', name: 'Block 4: 121-180 kWh', minCharge: religious.minCharge, limit: religious.limit4, rate: religious.rate4, fixedCharge: religious.fixedCharge4 },
              { id: 'block5', name: 'Block 5: Above 180 kWh', minCharge: religious.minCharge, limit: religious.limit5, rate: religious.rate5, fixedCharge: religious.fixedCharge5 }
            ]
          }
        ]
      });
    }

    // 4. Other Consumers
    const other21 = apiData.find(t => t.tariff === 21);
    const other22 = apiData.find(t => t.tariff === 22);
    const other31 = apiData.find(t => t.tariff === 31);
    const other32 = apiData.find(t => t.tariff === 32);
    const other33 = apiData.find(t => t.tariff === 33);
    const other34 = apiData.find(t => t.tariff === 34);
    const other41 = apiData.find(t => t.tariff === 41);
    const other42 = apiData.find(t => t.tariff === 42);

    const otherRef = other21 || other22 || other31 || other32 || other33 || other34 || other41 || other42;

    if (otherRef) {
      console.log('Other Consumers - Industrial (21, 22):', other21, other22);
      console.log('Industrial Energy Charge Rate:', other21?.rate1, '(from tariff 21 rate1)');
      console.log('Industrial Energy Charge Fixed:', other22?.rate2, '(from tariff 22 rate2)');
      console.log('Industrial Fixed Charge Rate:', other21?.fixedCharge1, '(from tariff 21 fxdchg1)');
      console.log('Industrial Fixed Charge Fixed:', other22?.fixedCharge2, '(from tariff 22 fxdchg2)');

        uiData.push({
            id: 4,
            tariffName: 'Other Consumers',
            tariffId: [21, 22, 41, 42, 31, 32, 33, 34],
            fromDate: formatDateToDDMMYYYY(otherRef.fromDate || otherRef.frmDate),
            toDate: formatDateToDDMMYYYY(otherRef.toDate) || null,
            noOfSlabs: 2,
            categories: [
                { id: 'cat-industrial', name: 'Industrial - 21, 22', subcategories: [
                    { id: 'sub-ind-1', name: 'Industrial 1', blocks: [
                        { id: 'block-ind1-1', name: 'Energy Charge (LKR/kWh)', rate: other21?.rate1, fixedCharge: other22?.rate2 },
                        { id: 'block-ind1-2', name: 'Fixed Charge (LKR/month)', rate: other21?.fixedCharge1, fixedCharge: other22?.fixedCharge2 }
                    ]}//,
                    // { id: 'sub-ind-2', name: 'Industrial 2', blocks: [
                    //     { id: 'block-ind2-1', name: 'Peak [18:30 to 22:30] (LKR/kWh)', rate: other22?.rate1, fixedCharge: other22?.fixedCharge1 },
                    //     { id: 'block-ind2-2', name: 'Day [05:30 to 18:30] (LKR/kWh)', rate: other22?.rate2, fixedCharge: other22?.fixedCharge2 },
                    //     { id: 'block-ind2-3', name: 'Off Peak [22:30 to 05:30] (LKR/kWh)', rate: other22?.rate3, fixedCharge: other22?.fixedCharge3 },
                    //     { id: 'block-ind2-4', name: 'Demand Charge (LKR/kVA)', rate: other22?.rate4, fixedCharge: other22?.fixedCharge4 },
                    //     { id: 'block-ind2-5', name: 'Fixed Charge (LKR/month)', rate: other22?.rate5, fixedCharge: other22?.fixedCharge5 }
                    // ]}
                ]},
                { id: 'cat-hotel', name: 'Hotel - 41, 42', subcategories: [
                     { id: 'sub-hot-1', name: 'Hotel 1', blocks: [
                        { id: 'block-hot1-1', name: 'Energy Charge (LKR/kWh)', rate: other41?.rate1, fixedCharge: other42?.rate2 },
                        { id: 'block-hot1-2', name: 'Fixed Charge (LKR/month)', rate: other41?.fixedCharge1, fixedCharge: other42?.fixedCharge2 }
                    ]}//,
                    // { id: 'sub-hot-2', name: 'Hotel 2', blocks: [
                    //     { id: 'block-hot2-1', name: 'Peak [18:30 to 22:30] (LKR/kWh)', rate: other42?.rate1, fixedCharge: other42?.fixedCharge1 },
                    //     { id: 'block-hot2-2', name: 'Day [05:30 to 18:30] (LKR/kWh)', rate: other42?.rate2, fixedCharge: other42?.fixedCharge2 },
                    //     { id: 'block-hot2-3', name: 'Off Peak [22:30 to 05:30] (LKR/kWh)', rate: other42?.rate3, fixedCharge: other42?.fixedCharge3 },
                    //     { id: 'block-hot2-4', name: 'Demand Charge (LKR/kVA)', rate: other42?.rate4, fixedCharge: other42?.fixedCharge4 },
                    //     { id: 'block-hot2-5', name: 'Fixed Charge (LKR/month)', rate: other42?.rate5, fixedCharge: other42?.fixedCharge5 }
                    // ]}
                ]},
                { id: 'cat-gp', name: 'General Purpose - 31, 32', subcategories: [
                     { id: 'sub-gp-1', name: 'General Purpose 1', blocks: [
                        { id: 'block-gp1-1', name: 'Energy Charge (LKR/kWh)', rate: other31?.rate1, fixedCharge: other32?.rate2 },
                        { id: 'block-gp1-2', name: 'Fixed Charge (LKR/month)', rate: other31?.fixedCharge1, fixedCharge: other32?.fixedCharge2 }
                    ]}//,
                    // { id: 'sub-gp-2', name: 'General Purpose 2', blocks: [
                    //     { id: 'block-gp2-1', name: 'Peak [18:30 to 22:30] (LKR/kWh)', rate: other32?.rate1, fixedCharge: other32?.fixedCharge1 },
                    //     { id: 'block-gp2-2', name: 'Day [05:30 to 18:30] (LKR/kWh)', rate: other32?.rate2, fixedCharge: other32?.fixedCharge2 },
                    //     { id: 'block-gp2-3', name: 'Off Peak [22:30 to 05:30] (LKR/kWh)', rate: other32?.rate3, fixedCharge: other32?.fixedCharge3 },
                    //     { id: 'block-gp2-4', name: 'Demand Charge (LKR/kVA)', rate: other32?.rate4, fixedCharge: other32?.fixedCharge4 },
                    //     { id: 'block-gp2-5', name: 'Fixed Charge (LKR/month)', rate: other32?.rate5, fixedCharge: other32?.fixedCharge5 }
                    // ]}
                ]},
                { id: 'cat-gov', name: 'Government - 33, 34', subcategories: [
                     { id: 'sub-gov-1', name: 'Government 1', blocks: [
                        { id: 'block-gov1-1', name: 'Energy Charge (LKR/kWh)', rate: other33?.rate1, fixedCharge: other34?.rate2 },
                        { id: 'block-gov1-2', name: 'Fixed Charge (LKR/month)', rate: other33?.fixedCharge1, fixedCharge: other34?.fixedCharge2 }
                    ]}//,
                    // { id: 'sub-gov-2', name: 'Government 2', blocks: [
                    //     { id: 'block-gov2-1', name: 'Peak [18:30 to 22:30] (LKR/kWh)', rate: other34?.rate1, fixedCharge: other34?.fixedCharge1 },
                    //     { id: 'block-gov2-2', name: 'Day [05:30 to 18:30] (LKR/kWh)', rate: other34?.rate2, fixedCharge: other34?.fixedCharge2 },
                    //     { id: 'block-gov2-3', name: 'Off Peak [22:30 to 05:30] (LKR/kWh)', rate: other34?.rate3, fixedCharge: other34?.fixedCharge3 },
                    //     { id: 'block-gov2-4', name: 'Demand Charge (LKR/kVA)', rate: other34?.rate4, fixedCharge: other34?.fixedCharge4 },
                    //     { id: 'block-gov2-5', name: 'Fixed Charge (LKR/month)', rate: other34?.rate5, fixedCharge: other34?.fixedCharge5 }
                    // ]}
                ]}
            ]
        });
    }

    // 5. Street Lighting (tariff 61)
    const street = apiData.find(t => t.tariff === 61);
    if (street) {
        uiData.push({
            id: 5,
            tariffName: 'Street Lighting',
            tariffId: 61,
            fromDate: formatDateToDDMMYYYY(street.fromDate || street.frmDate),
            toDate: formatDateToDDMMYYYY(street.toDate) || null,
            noOfSlabs: 1,
            categories: [{ id: 'cat-street', name: 'Street Lighting Charge', blocks: [
                { id: 'block-street-1', name: 'Energy Charge (LKR/kWh)', minCharge: street.minCharge, limit: street.limit1, rate: street.rate1, fixedCharge: street.fixedCharge1 }
            ]}]
        });
    }

    // 6. Agriculture (tariff 71, 72, 73)
    const agPeak = apiData.find(t => t.tariff === 71);
    const agDay = apiData.find(t => t.tariff === 72);
    const agOffPeak = apiData.find(t => t.tariff === 73);
    if (agPeak || agDay || agOffPeak) {
        uiData.push({
            id: 6,
            tariffName: 'Agriculture: Optional Time of Use',
            tariffId: [71, 72, 73],
            fromDate: formatDateToDDMMYYYY(agPeak?.fromDate || agDay?.fromDate || agOffPeak?.fromDate),
            toDate: formatDateToDDMMYYYY(agPeak?.toDate || agDay?.toDate || agOffPeak?.toDate) || null,
            noOfSlabs: 1,
            categories: [{ id: 'cat-agri', name: 'Agriculture Time of Use Tariff', blocks: [
                { id: 'block-ag-1', name: 'Peak [18:30 to 22:30] - 71', rate: agPeak?.rate1, fixedCharge: agPeak?.fixedCharge1 },
                { id: 'block-ag-2', name: 'Day [05:30 to 18:30] - 72', rate: agDay?.rate1, fixedCharge: agDay?.fixedCharge1 },
                { id: 'block-ag-3', name: 'Off Peak [22:30 to 05:30] - 73', rate: agOffPeak?.rate1, fixedCharge: agOffPeak?.fixedCharge1 }
            ]}]
        });
    }

    // 7. EV Charging Stations (tariff 93)
    // const evCharging = apiData.find(t => t.tariff === 93);
    // if (evCharging) {
    //     uiData.push({
    //         id: 7,
    //         tariffName: 'EV Charging Stations',
    //         tariffId: 93,
    //         fromDate: formatDateToDDMMYYYY(evCharging.fromDate || evCharging.frmDate),
    //         toDate: formatDateToDDMMYYYY(evCharging.toDate) || null,
    //         noOfSlabs: 3,
    //         categories: [{ id: 'cat-ev', name: 'EV Charging Stations Time', isEVCharging: true, blocks: [
    //             { id: 'block-ev-1', name: 'Peak (LKR/kWh)', dcFastCharging: evCharging.rate1, acLevel2Charging: evCharging.rate2 },
    //             { id: 'block-ev-2', name: 'Day (LKR/kWh)', dcFastCharging: evCharging.rate3, acLevel2Charging: evCharging.rate4 },
    //             { id: 'block-ev-3', name: 'Off Peak (LKR/kWh)', dcFastCharging: evCharging.rate5, acLevel2Charging: evCharging.rate6 }
    //         ]}]
    //     });
    // }

    console.log('=== CURRENT TARIFF TRANSFORMATION COMPLETE ===');
    console.log('Transformed UI data:', uiData);
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

  const handleEndAllTariffs = async () => {
    const allEnded = tariffData.every(t => t.toDate !== null && t.toDate !== '');

    if (allEnded) {
      const confirmReactivate = window.confirm('Are you sure you want to reactivate ALL live tariffs?');
      if (!confirmReactivate) return;

      try {
        setLoading(true);
        const response = await tariffService.reactivateAllTariffs();
        await fetchLiveTariffs();
        alert(`SUCCESS!\n\nReactivated: ${response.reactivatedCount} tariff(s)\nDeleted: ${response.deletedCount} empty tariff(s)`);
      } catch (error) {
        alert('Failed to reactivate tariffs. Error: ' + error.message);
        setLoading(false);
      }
    } else {
      // Read Active/Inactive status from localStorage
      console.log('=== END ALL TARIFFS: Reading Active/Inactive Status ===');
      const allStatuses = getAllTariffStatuses();
      console.log('All tariff statuses from localStorage:', allStatuses);

      // Extract all tariff IDs from tariffData
      const allTariffIds = [];
      tariffData.forEach(tariff => {
        if (Array.isArray(tariff.tariffId)) {
          // Multi-tariff category (e.g., [13, 14, 15])
          allTariffIds.push(...tariff.tariffId);
        } else {
          // Single tariff (e.g., 11)
          allTariffIds.push(tariff.tariffId);
        }
      });
      console.log('All tariff IDs from tariffData:', allTariffIds);

      // Filter by status
      const activeTariffIds = allTariffIds.filter(id => {
        const status = allStatuses[String(id)] || 'A'; // Default to Active if not in localStorage
        return status === 'A';
      });
      const inactiveTariffIds = allTariffIds.filter(id => {
        const status = allStatuses[String(id)] || 'A';
        return status === 'I';
      });

      console.log('Active tariff IDs (will be ended):', activeTariffIds);
      console.log('Inactive tariff IDs (will be skipped):', inactiveTariffIds);

      // Validate: If no Active tariffs, show warning and abort
      if (activeTariffIds.length === 0) {
        alert('All tariffs are currently marked as Inactive. No tariffs will be ended.');
        return;
      }

      // Show confirmation with counts
      const confirmMessage = `You are about to end tariffs:\n\n` +
        `✓ Active (will be ended): ${activeTariffIds.length} tariff(s)\n` +
        `✗ Inactive (will be skipped): ${inactiveTariffIds.length} tariff(s)\n\n` +
        `Active tariff IDs: ${activeTariffIds.join(', ')}\n` +
        `${inactiveTariffIds.length > 0 ? `Inactive tariff IDs: ${inactiveTariffIds.join(', ')}\n` : ''}\n` +
        `Are you sure you want to continue?`;

      const confirmAction = window.confirm(confirmMessage);
      if (!confirmAction) return;

      try {
        setLoading(true);
        console.log('=== Calling endSelectedTariffsAndCreateNew ===');
        console.log('Sending tariff IDs:', activeTariffIds);

        const response = await tariffService.endSelectedTariffsAndCreateNew(activeTariffIds);

        console.log('=== Response received ===');
        console.log('Response:', response);

        await fetchLiveTariffs();

        const successMessage = `SUCCESS!\n\n` +
          `Ended: ${response.endedCount} tariff(s)\n` +
          `Created: ${response.createdCount} new draft tariff(s)\n` +
          `Skipped (Inactive): ${inactiveTariffIds.length} tariff(s)`;

        alert(successMessage);
      } catch (error) {
        console.error('=== Error ending tariffs ===');
        console.error('Error:', error);
        alert('Failed to end tariffs. Error: ' + error.message);
        setLoading(false);
      }
    }
  };

  const handleTransferToSetup = async () => {
    const confirmTransfer = window.confirm(
      'Are you sure you want to transfer all Live Tariffs to Tariff Setup? This will replace any existing tariffs in Tariff Setup.'
    );
    if (!confirmTransfer) return;

    try {
      setLoading(true);
      const response = await tariffService.transferToTmpTariff();

      if (response.success) {
        alert(`SUCCESS! Transferred ${response.transferredCount} tariff(s) to Tariff Setup.`);
      } else {
        alert(`Transfer failed: ${response.message}`);
      }

      setLoading(false);
    } catch (error) {
      alert(`Transfer failed: ${error.message}`);
      setLoading(false);
    }
  };

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

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen bg-ink-100">
        <div>Loading live tariff data...</div>
      </div>
    );
  }

  return (
    <div className="flex justify-center items-start min-h-screen bg-ink-100 rounded-lg">
      <div className="ds-card p-ds-5 pb-ds-8 w-full max-w-7xl">
        
        <div className="flex justify-between items-center mb-4">
          <h2 className="ds-section-title">Current Tariff Records (Read Only)</h2>
          <div className="flex gap-3 items-center">
            <button
              onClick={handleTransferToSetup}
              className="px-4 py-2 bg-success-600 text-white rounded hover:bg-success-700 transition-colors duration-200 text-sm font-semibold"
            >
              Transfer to Setup
            </button>
            <button
              onClick={handleEndAllTariffs}
              className="ds-btn ds-btn-primary"
            >
              {tariffData.every(t => t.toDate !== null && t.toDate !== '') ? 'Reactivate All Tariffs' : 'End All Tariffs'}
            </button>
          </div>
        </div>

        <div className="overflow-x-auto rounded-lg shadow-sm">
          <table className="w-full border-collapse bg-white">
            <thead className="bg-navy-800 text-white">
              <tr>
                <th className="px-4 py-3 text-center font-semibold text-sm border-b-2 border-navy-800" style={{ fontSize: "13px", minWidth: "50px" }}></th>
                <th className="px-4 py-3 text-center font-semibold text-sm border-b-2 border-navy-800" style={{ fontSize: "13px", minWidth: "280px" }}>Tariff Name</th>
                <th className="px-4 py-3 text-center font-semibold text-sm border-b-2 border-navy-800" style={{ fontSize: "13px", minWidth: "230px" }}>Tariff ID</th>
                <th className="px-4 py-3 text-center font-semibold text-sm border-b-2 border-navy-800" style={{ fontSize: "13px", minWidth: "150px" }}>From Date</th>
                <th className="px-4 py-3 text-center font-semibold text-sm border-b-2 border-navy-800" style={{ fontSize: "13px", minWidth: "150px" }}>To Date</th>
                <th className="px-4 py-3 text-center font-semibold text-sm border-b-2 border-navy-800" style={{ fontSize: "13px", minWidth: "120px" }}>No of Slabs</th>
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
                    <td className="px-4 py-7 text-sm text-ink-700 align-middle text-center">{tariff.tariffName}</td>
                    <td className="px-4 py-7 text-sm text-ink-700 align-middle text-center">
                      {Array.isArray(tariff.tariffId) ? tariff.tariffId.join(', ') : tariff.tariffId}
                    </td>
                    <td className="px-4 py-7 text-sm text-ink-700 align-middle text-center">{tariff.fromDate}</td>
                    <td className="px-4 py-7 text-sm text-ink-700 align-middle text-center">{tariff.toDate || '-'}</td>
                    <td className="px-4 py-7 text-sm text-ink-700 align-middle text-center">{tariff.noOfSlabs}</td>
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
                                  {category.isEVCharging ? (
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
                                              <td className="px-3 py-2 text-sm text-ink-700 border-b">{block.name}</td>
                                              <td className="px-3 py-2 text-sm text-ink-700 border-b text-center">{block.dcFastCharging}</td>
                                              <td className="px-3 py-2 text-sm text-ink-700 border-b text-center">{block.acLevel2Charging}</td>
                                            </tr>
                                          ))}
                                        </tbody>
                                      </table>
                                    </div>
                                  ) : category.subcategories ? (
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
                                                  <th className="ds-th text-center px-3 py-2">Rate (Rs/kWh)</th>
                                                  <th className="ds-th text-center px-3 py-2">Fixed Charge (Rs)</th>
                                                </tr>
                                              </thead>
                                              <tbody>
                                                {subcategory.blocks.map((block, blockIdx) => (
                                                  <tr key={block.id} className={blockIdx % 2 === 0 ? 'bg-white' : 'bg-ink-50'}>
                                                    <td className="px-3 py-2 text-sm text-ink-700 border-b">{block.name}</td>
                                                    {!shouldHideMinChargeAndLimit(subcategory.name) && (
                                                      <td className="px-3 py-2 text-sm text-ink-700 border-b text-center">{block.minCharge}</td>
                                                    )}
                                                    {!shouldHideMinChargeAndLimit(subcategory.name) && (
                                                      <td className="px-3 py-2 text-sm text-ink-700 border-b text-center">{block.limit}</td>
                                                    )}
                                                    <td className="px-3 py-2 text-sm text-ink-700 border-b text-center">{block.rate}</td>
                                                    <td className="px-3 py-2 text-sm text-ink-700 border-b text-center">{block.fixedCharge}</td>
                                                  </tr>
                                                ))}
                                              </tbody>
                                            </table>
                                          </div>
                                        )}
                                      </div>
                                    ))
                                  ) : (
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
                                            <th className="ds-th text-center px-3 py-2">Rate (Rs/kWh)</th>
                                            <th className="ds-th text-center px-3 py-2">Fixed Charge (Rs)</th>
                                          </tr>
                                        </thead>
                                        <tbody>
                                          {category.blocks.map((block, blockIdx) => (
                                            <tr key={block.id} className={blockIdx % 2 === 0 ? 'bg-white' : 'bg-ink-50'}>
                                              <td className="px-3 py-2 text-sm text-ink-700 border-b">{block.name}</td>
                                              {!shouldHideMinChargeAndLimit(category.name) && (
                                                <td className="px-3 py-2 text-sm text-ink-700 border-b text-center">{block.minCharge}</td>
                                              )}
                                              {!shouldHideMinChargeAndLimit(category.name) && (
                                                <td className="px-3 py-2 text-sm text-ink-700 border-b text-center">{block.limit}</td>
                                              )}
                                              <td className="px-3 py-2 text-sm text-ink-700 border-b text-center">{block.rate}</td>
                                              <td className="px-3 py-2 text-sm text-ink-700 border-b text-center">{block.fixedCharge}</td>
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