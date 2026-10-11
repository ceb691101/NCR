// File path: src/views/tariff/TariffArchive.js
// UPDATED: Now reads from tmp_tariff table (ended tariffs)
// FIXED: Using React Router v5 (useHistory instead of useNavigate)

import React, { useState, useEffect } from 'react';
import { ChevronRight, ChevronDown } from 'lucide-react';
import { useHistory } from 'react-router-dom';  // CHANGED: v5 hook
import tmpTariffService from '../../services/tmpTariffService';

export default function TariffArchive() {
  const history = useHistory();  // CHANGED: v5 hook
  const [expandedTariffs, setExpandedTariffs] = useState(new Set());
  const [expandedCategories, setExpandedCategories] = useState(new Set());
  const [tariffData, setTariffData] = useState([]);
  const [loading, setLoading] = useState(true);

  // Fetch ended tariffs from tmp_tariff table
  useEffect(() => {
    const fetchEndedTariffs = async () => {
      try {
        setLoading(true);
        console.log('Fetching ended tariffs from tmp_tariff table...');
        
        const data = await tmpTariffService.getEndedTmpTariffs();
        console.log('Fetched ended tariffs:', data);
        
        if (data && data.length > 0) {
          // Transform API data to UI format
          const transformedData = transformToUIFormat(data);
          setTariffData(transformedData);
        } else {
          console.log('No ended tariffs found');
          setTariffData([]);
        }
      } catch (error) {
        console.error('Error fetching ended tariffs:', error);
        alert('Failed to load ended tariffs. Please try again.');
        setTariffData([]);
      } finally {
        setLoading(false);
      }
    };

    fetchEndedTariffs();
  }, []);

  // Date formatting function
  const formatDateToDDMMYYYY = (dateString) => {
    if (!dateString) return null;
    
    if (typeof dateString === 'string' && /^\d{2}\/\d{2}\/\d{4}$/.test(dateString)) {
      return dateString;
    }
    
    let date;
    
    if (typeof dateString === 'string' && dateString.includes('-')) {
      const [year, month, day] = dateString.split('-');
      date = new Date(year, month - 1, day);
    } else {
      date = new Date(dateString);
    }
    
    if (isNaN(date.getTime())) {
      console.warn('Invalid date:', dateString);
      return dateString;
    }
    
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    
    return `${day}/${month}/${year}`;
  };

  // Transform database data to UI format
  const transformToUIFormat = (apiData) => {
    const uiData = [];
    
    // 1. Domestic (tariff 11)
    const domestic = apiData.find(t => t.tariff === 11);
    if (domestic) {
      uiData.push({
        id: 1,
        tariffName: 'Domestic',
        tariffId: 11,
        fromDate: formatDateToDDMMYYYY(domestic.fromDate) || '06/12/2025',
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
                limit: domestic.limit1 || 30,
                rate: domestic.rate1 || 4.50,
                fixedCharge: domestic.fixedCharge1 || 80,
              },
              {
                id: 'block2',
                name: 'Block 2: 31-60 kWh',
                minCharge: domestic.minCharge || 0,
                limit: domestic.limit2 || 60,
                rate: domestic.rate2 || 8,
                fixedCharge: domestic.fixedCharge2 || 210,
              }
            ]
          },
          {
            id: 'cat2',
            name: 'Consumption above 60kWh per month',
            blocks: [
              {
                id: 'block3',
                name: 'Block 1: 0-60 kWh',
                minCharge: domestic.minCharge || 0,
                limit: domestic.limit2 || 60,
                rate: domestic.rate2 || 12.75,
                fixedCharge: domestic.fixedCharge2 || 0,
              },
              {
                id: 'block4',
                name: 'Block 2: 61-90 kWh',
                minCharge: domestic.minCharge || 0,
                limit: domestic.limit3 || 90,
                rate: domestic.rate3 || 18.50,
                fixedCharge: domestic.fixedCharge3 || 400,
              },
              {
                id: 'block5',
                name: 'Block 3: 91-120 kWh',
                minCharge: domestic.minCharge || 0,
                limit: domestic.limit4 || 120,
                rate: domestic.rate4 || 24,
                fixedCharge: domestic.fixedCharge4 || 1000,
              },
              {
                id: 'block6',
                name: 'Block 4: 121-180 kWh',
                minCharge: domestic.minCharge || 0,
                limit: domestic.limit5 || 180,
                rate: domestic.rate5 || 41,
                fixedCharge: domestic.fixedCharge5 || 1500,
              },
              {
                id: 'block7',
                name: 'Block 5: Above 180 kWh',
                minCharge: domestic.minCharge || 0,
                limit: domestic.limit6 || 0,
                rate: domestic.rate6 || 61,
                fixedCharge: domestic.fixedCharge6 || 2100,
              }
            ]
          }
        ]
      });
    }

    // 2. Domestic Time of Use (tariffs 13, 14, 15)
    const peak = apiData.find(t => t.tariff === 13);
    const day = apiData.find(t => t.tariff === 14);
    const offPeak = apiData.find(t => t.tariff === 15);

    if (peak || day || offPeak) {
      uiData.push({
        id: 2,
        tariffName: 'Domestic Time of Use',
        tariffId: [13, 14, 15],
        fromDate: formatDateToDDMMYYYY(peak?.fromDate || day?.fromDate || offPeak?.fromDate) || '06/12/2025',
        toDate: formatDateToDDMMYYYY(peak?.toDate || day?.toDate || offPeak?.toDate) || null,
        noOfSlabs: 1,
        categories: [
          {
            id: 'cat3',
            name: 'Time of Use',
            type: 'tou',
            blocks: [
              {
                id: 'block-tou-peak',
                name: 'Peak',
                rate: peak?.rate1 || 67,
                fixedCharge: peak?.fixedCharge1 || 2100,
              },
              {
                id: 'block-tou-day',
                name: 'Day',
                rate: day?.rate1 || 35,
                fixedCharge: day?.fixedCharge1 || 0,
              },
              {
                id: 'block-tou-offpeak',
                name: 'Off-Peak',
                rate: offPeak?.rate1 || 21,
                fixedCharge: offPeak?.fixedCharge1 || 0,
              }
            ]
          }
        ]
      });
    }

    // Add more tariff types as needed (Religious, Industrial, Hotel, etc.)
    // Similar to TariffNew.js transformToUIFormat function

    return uiData;
  };

  // Toggle tariff expansion
  const toggleTariff = (tariffId) => {
    const newExpanded = new Set(expandedTariffs);
    if (newExpanded.has(tariffId)) {
      newExpanded.delete(tariffId);
    } else {
      newExpanded.add(tariffId);
    }
    setExpandedTariffs(newExpanded);
  };

  // Toggle category expansion
  const toggleCategory = (categoryId) => {
    const newExpanded = new Set(expandedCategories);
    if (newExpanded.has(categoryId)) {
      newExpanded.delete(categoryId);
    } else {
      newExpanded.add(categoryId);
    }
    setExpandedCategories(newExpanded);
  };

  // Navigate back to active tariffs
  const goToActiveTariffs = () => {
    history.push('/tariff/new');  // CHANGED: v5 syntax
  };

  if (loading) {
    return (
      <div className="flex min-h-[50vh] items-center justify-center">
        <div className="text-center space-y-2">
          <span className="ds-spinner" aria-hidden="true"></span>
          <p className="ds-body-sm">Loading ended tariffs...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-ds-5">
      <section className="ds-card-refined w-full overflow-hidden">
        <div className="ds-card-header">
          <div>
            <h1 className="ds-page-title">Tariff History</h1>
            <p className="ds-card-subtitle mt-1">Previous tariffs that have ended</p>
          </div>
          <button
            onClick={goToActiveTariffs}
            className="ds-btn ds-btn-secondary ds-btn-sm"
            type="button"
          >
            <i className="fas fa-arrow-left mr-2" aria-hidden="true"></i>
            Active Tariffs
          </button>
        </div>

        <div className="ds-card-body">
          {tariffData.length === 0 ? (
          <div className="ds-empty">
            <span className="w-11 h-11 rounded-lg bg-ink-100 border border-ink-200 text-ink-400 flex items-center justify-center text-lg flex-none">
              <i className="fas fa-archive"></i>
            </span>
            <p className="ds-section-title mt-1">No ended tariffs found</p>
            <p className="ds-caption">
              Ended tariffs will appear here after using &quot;End All Tariffs&quot;
            </p>
          </div>
          ) : (
            <div className="ds-table-wrap w-full overflow-x-auto">
              <table className="ds-table w-full min-w-[560px]">
                <thead>
                  <tr>
                    <th className="ds-th">
                      Tariff Name
                    </th>
                    <th className="ds-th">
                      From Date
                    </th>
                    <th className="ds-th">
                      To Date
                    </th>
                    <th className="ds-th">
                      Status
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {tariffData.map((tariff) => (
                    <React.Fragment key={tariff.id}>
                      <tr 
                        onClick={() => toggleTariff(tariff.id)}
                        className="cursor-pointer hover:bg-ink-50 transition-colors"
                      >
                        <td className="ds-td whitespace-nowrap">
                          <div className="flex items-center">
                            {expandedTariffs.has(tariff.id) ? (
                              <ChevronDown size={16} className="mr-2 text-warning-700" />
                            ) : (
                              <ChevronRight size={16} className="mr-2 text-ink-400" />
                            )}
                            <span className="font-semibold text-ink-800">
                              {tariff.tariffName}
                            </span>
                          </div>
                        </td>
                        <td className="ds-td whitespace-nowrap">
                          {tariff.fromDate}
                        </td>
                        <td className="ds-td whitespace-nowrap">
                          <span className="text-critical-700 font-semibold">
                            {tariff.toDate || 'N/A'}
                          </span>
                        </td>
                        <td className="ds-td whitespace-nowrap">
                          <span className="ds-badge ds-badge-critical">
                            Ended
                          </span>
                        </td>
                      </tr>

                      {/* Expanded Content */}
                      {expandedTariffs.has(tariff.id) && (
                        <tr>
                          <td colSpan="4" className="bg-ink-50 px-4 py-4">
                            <div className="space-y-4">
                              {tariff.categories.map((category) => (
                                <div key={category.id} className="ds-panel bg-white p-4">
                                  <div 
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      toggleCategory(category.id);
                                    }}
                                    className="cursor-pointer flex items-center justify-between mb-2"
                                  >
                                    <h4 className="font-semibold text-ink-700 flex items-center">
                                      {expandedCategories.has(category.id) ? (
                                        <ChevronDown size={16} className="mr-2 text-warning-700" />
                                      ) : (
                                        <ChevronRight size={16} className="mr-2 text-ink-400" />
                                      )}
                                      {category.name}
                                    </h4>
                                  </div>

                                  {expandedCategories.has(category.id) && (
                                    <div className="ds-table-wrap mt-3 overflow-x-auto">
                                      <table className="ds-table w-full min-w-[420px] text-sm">
                                        <thead>
                                          <tr>
                                            <th className="ds-th px-3 py-2">Block Name</th>
                                            <th className="ds-th text-center px-3 py-2">Rate (Rs/kWh)</th>
                                            <th className="ds-th text-center px-3 py-2">Fixed Charge (Rs)</th>
                                          </tr>
                                        </thead>
                                        <tbody>
                                          {category.blocks.map((block, idx) => (
                                            <tr key={block.id} className={idx % 2 === 0 ? 'bg-white' : 'bg-ink-50'}>
                                              <td className="ds-td">{block.name}</td>
                                              <td className="ds-td text-center">{block.rate}</td>
                                              <td className="ds-td text-center">{block.fixedCharge}</td>
                                            </tr>
                                          ))}
                                        </tbody>
                                      </table>
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
            )}
        </div>
        <div className="ds-card-footer">
          <p className="ds-caption">Showing {tariffData.length} ended tariff group(s)</p>
        </div>
      </section>
    </div>
  );
}