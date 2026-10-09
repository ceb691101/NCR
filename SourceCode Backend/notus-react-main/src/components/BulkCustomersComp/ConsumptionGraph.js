import React, { useState, useEffect, useRef } from 'react';
import { Line } from 'react-chartjs-2';
import {
    Chart as ChartJS,
    LineElement,
    PointElement,
    LinearScale,
    CategoryScale,
    Title,
    Tooltip,
    Legend,
    Filler
} from 'chart.js';

// Register ChartJS components
ChartJS.register(
    LineElement,
    PointElement,
    LinearScale,
    CategoryScale,
    Title,
    Tooltip,
    Legend,
    Filler
);

const ConsumptionGraph = ({ accountNumber, areaCode, customerName, folioNo }) => {
    const [consumptionData, setConsumptionData] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [cycleCount, setCycleCount] = useState(6); // Default 6 months
    const [viewOption, setViewOption] = useState('all'); // Default view: All
    const chartRef = useRef(null);

    useEffect(() => {
        if (accountNumber && areaCode) {
            loadConsumptionData();
        }
    }, [accountNumber, areaCode, cycleCount]);

    const loadConsumptionData = async () => {
        try {
            setLoading(true);
            setError(null);

            // Import the service
            const { getCustomerConsumptionHistory, transformConsumptionDataForChart } = await import('services/consumptionHistoryService');
            
            const response = await getCustomerConsumptionHistory(accountNumber, cycleCount, areaCode);
            
            if (response.success && response.consumption_history) {
                const chartData = transformConsumptionDataForChart(response.consumption_history);
                setConsumptionData(chartData);
            } else {
                setError(response.message || 'Failed to load consumption data');
            }
        } catch (err) {
            console.error('Error loading consumption data:', err);
            setError(err.message || 'Failed to load consumption data');
        } finally {
            setLoading(false);
        }
    };

    const handleCycleCountChange = (count) => {
        setCycleCount(count);
    };

    const handleViewOptionChange = (option) => {
        setViewOption(option);
    };

    // Filter datasets based on view option
    const getFilteredDatasets = () => {
        if (!consumptionData || !consumptionData.datasets) {
            return [];
        }

        if (viewOption === 'all') {
            return consumptionData.datasets;
        }

        // Filter based on selected view option
        return consumptionData.datasets.filter(dataset => {
            const label = dataset.label.toLowerCase();
            switch (viewOption) {
                case 'offpeak':
                    return label.includes('off peak');
                case 'day':
                    return label === 'day';
                case 'peak':
                    return label === 'peak';
                case 'kva':
                    return label === 'kva';
                default:
                    return true;
            }
        });
    };

    const chartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                position: 'top',
                labels: {
                    font: {
                        size: 12,
                        family: "'Inter', sans-serif"
                    },
                    padding: 20,
                    usePointStyle: true,
                    pointStyle: 'circle'
                }
            },
            tooltip: {
                mode: 'index',
                intersect: false,
                backgroundColor: 'rgba(0, 0, 0, 0.8)',
                titleFont: {
                    size: 12,
                    family: "'Inter', sans-serif"
                },
                bodyFont: {
                    size: 12,
                    family: "'Inter', sans-serif"
                },
                padding: 12,
                callbacks: {
                    label: function(context) {
                        const label = context.dataset.label || '';
                        const value = context.parsed.y;
                        
                        // Format the value with 2 decimal places
                        const formattedValue = typeof value === 'number' ? value.toFixed(2) : value;
                        
                        return `${label}: ${formattedValue} kWh`;
                    },
                    title: function(tooltipItems) {
                        const billCycle = consumptionData.billCycles[tooltipItems[0].dataIndex];
                        return `Bill Cycle: ${billCycle}`;
                    }
                }
            },
            // REMOVED title from here as we're moving it to the controls section
        },
        scales: {
            x: {
                title: {
                    display: true,
                    text: 'Bill Cycle',
                    font: {
                        size: 14,
                        weight: 'bold',
                        family: "'Inter', sans-serif"
                    }
                },
                grid: {
                    display: true,
                    color: 'rgba(0, 0, 0, 0.05)'
                },
                ticks: {
                    font: {
                        size: 12,
                        family: "'Inter', sans-serif"
                    }
                }
            },
            y: {
                title: {
                    display: true,
                    text: 'Consumption (kWh)',
                    font: {
                        size: 14,
                        weight: 'bold',
                        family: "'Inter', sans-serif"
                    }
                },
                beginAtZero: true,
                grid: {
                    display: true,
                    color: 'rgba(0, 0, 0, 0.05)'
                },
                ticks: {
                    font: {
                        size: 12,
                        family: "'Inter', sans-serif"
                    },
                    callback: function(value) {
                        return value.toLocaleString();
                    }
                }
            }
        },
        elements: {
            point: {
                radius: 5,
                hoverRadius: 8,
                backgroundColor: function(context) {
                    const colors = [
                        'rgba(255, 99, 132, 1)',
                        'rgba(54, 162, 235, 1)',
                        'rgba(255, 206, 86, 1)',
                        'rgba(75, 192, 192, 1)'
                    ];
                    return colors[context.datasetIndex % colors.length];
                },
                borderColor: 'white',
                borderWidth: 2
            },
            line: {
                tension: 0.3
            }
        },
        interaction: {
            intersect: false,
            mode: 'index'
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center h-96">
                <div className="text-center">
                    <div className="ds-spinner mx-auto"></div>
                    <p className="mt-3 text-ink-600">Loading consumption data...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="flex justify-center items-center h-96">
                <div className="text-center text-critical-600">
                    <i className="fas fa-exclamation-triangle text-lg text-warning-600"></i>
                    <p className="font-medium">Error loading consumption data</p>
                    <p className="text-sm mt-1">{error}</p>
                    <button
                        onClick={loadConsumptionData}
                        className="mt-4 bg-critical-500 hover:bg-critical-600 text-white px-4 py-2 rounded-md text-sm transition-colors duration-200"
                    >
                        Retry
                    </button>
                </div>
            </div>
        );
    }

    if (!consumptionData) {
        return (
            <div className="flex justify-center items-center h-96">
                <div className="text-center text-ink-500">
                    <i className="fas fa-chart-line text-lg text-ink-400"></i>
                    <p className="font-medium">No consumption data available</p>
                    <p className="text-sm mt-1">Consumption data will appear here once available</p>
                </div>
            </div>
        );
    }

    return (
        <div className="w-full bg-white rounded-lg shadow-md p-4">
            {/* Controls Section - MODIFIED with View dropdown */}
            <div className="flex flex-col md:flex-row md:items-center justify-between mb-6 p-4 bg-ink-50 rounded-lg">
                {/* Consumption History Text - Left side */}
                <div className="ds-section-title mb-3 md:mb-0">
                    Consumption History - {customerName || (folioNo ? `Folio ${folioNo}` : 'Developer')}
                </div>
                
                {/* Dropdowns - Right side, goes to next line on mobile */}
                <div className="flex flex-col sm:flex-row gap-3 sm:gap-4">
                    {/* Cycle Count Dropdown */}
                    <div className="flex items-center space-x-2">
                        <span className="text-sm text-ink-600">Show :</span>
                        <select
                            value={cycleCount}
                            onChange={(e) => handleCycleCountChange(parseInt(e.target.value))}
                            className="pr-8 border border-ink-300 rounded-md px-2 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-navy-500 focus:border-transparent min-w-[120px]"
                        >
                            <option value={3}>Last 3 cycles</option>
                            <option value={6}>Last 6 cycles</option>
                            <option value={12}>Last 12 cycles</option>
                        </select>
                    </div>
                    
                    {/* View Dropdown */}
                    <div className="flex items-center space-x-2">
                        <span className="text-sm text-ink-600">View :</span>
                        <select
                            value={viewOption}
                            onChange={(e) => handleViewOptionChange(e.target.value)}
                            className="pr-4 border border-ink-300 rounded-md px-2 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-navy-500 focus:border-transparent min-w-[125px]"
                        >
                            <option value="all">All Meters</option>
                            <option value="offpeak">Off Peak Only</option>
                            <option value="day">Day Only</option>
                            <option value="peak">Peak Only</option>
                            <option value="kva">KVA Only</option>
                        </select>
                    </div>
                </div>
            </div>

            {/* Chart Container */}
            <div className="relative h-96">
                <Line
                    ref={chartRef}
                    data={{
                        labels: consumptionData.labels,
                        datasets: getFilteredDatasets()
                    }}
                    options={chartOptions}
                />
            </div>
        </div>
    );
};

export default ConsumptionGraph;