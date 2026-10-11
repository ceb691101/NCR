import React, { useState, useEffect, useRef } from 'react';
import { Line } from 'react-chartjs-2';
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend,
    Filler
} from 'chart.js';

ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend,
    Filler
);

const GenerationGraph = ({ accNbr, areaCd }) => {
    const [generationData, setGenerationData] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [cycleCount, setCycleCount] = useState(3);
    const chartRef = useRef(null);

    useEffect(() => {
        if (accNbr && areaCd) {
            loadGenerationData();
        }
    }, [accNbr, areaCd, cycleCount]);

    const loadGenerationData = async () => {
        try {
            setLoading(true);
            setError(null);

            const { getGenerationHistory, transformGenerationDataForChart } = await import('services/generationHistoryService');
            
            const historyList = await getGenerationHistory(accNbr, areaCd, cycleCount);
            
            if (historyList && historyList.length > 0) {
                const chartData = transformGenerationDataForChart(historyList);
                setGenerationData(chartData);
            } else {
                setError('No generation data found for this developer.');
            }
        } catch (err) {
            console.error('Error loading generation data:', err);
            setError(err.message || 'Failed to load generation data');
        } finally {
            setLoading(false);
        }
    };

    const chartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                position: 'top',
                labels: {
                    usePointStyle: true,
                    pointStyle: 'circle',
                    padding: 20,
                    font: {
                        size: 12,
                        family: "'Inter', sans-serif"
                    }
                }
            },
            tooltip: {
                mode: 'index',
                intersect: false,
                callbacks: {
                    label: function(context) {
                        const label = context.dataset.label || '';
                        const value = context.parsed.y;
                        const formattedValue = typeof value === 'number' ? value.toFixed(2) : value;
                        return `${label}: ${formattedValue} kWh`;
                    }
                }
            }
        },
        scales: {
            x: {
                grid: {
                    color: 'rgba(0, 0, 0, 0.05)',
                },
                title: {
                    display: true,
                    text: 'Bill Cycle',
                    font: {
                        size: 14,
                        weight: 'bold',
                        family: "'Inter', sans-serif"
                    },
                    color: '#666'
                }
            },
            y: {
                grid: {
                    color: 'rgba(0, 0, 0, 0.05)',
                },
                title: {
                    display: true,
                    text: 'Energy Sent to Grid (kWh)',
                    font: {
                        size: 14,
                        weight: 'bold',
                        family: "'Inter', sans-serif"
                    },
                    color: '#666'
                },
                beginAtZero: true
            }
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center h-96 bg-white rounded-lg shadow-md p-4">
                <div className="text-center">
                    <div className="ds-spinner mx-auto"></div>
                    <p className="mt-3 text-ink-600">Loading generation data...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="flex justify-center items-center h-96 bg-white rounded-lg shadow-md p-4">
                <div className="text-center text-critical-600">
                    <i className="fas fa-exclamation-triangle text-lg text-warning-600"></i>
                    <p className="font-medium">Error loading generation data</p>
                    <p className="text-sm mt-1">{error}</p>
                    <button
                        onClick={loadGenerationData}
                        className="mt-4 bg-critical-500 hover:bg-critical-600 text-white px-4 py-2 rounded-md text-sm transition-colors duration-200"
                    >
                        Retry
                    </button>
                </div>
            </div>
        );
    }

    if (!generationData) {
        return (
            <div className="flex justify-center items-center h-96 bg-white rounded-lg shadow-md p-4">
                <div className="text-center text-ink-500">
                    <i className="fas fa-chart-line text-lg text-ink-400"></i>
                    <p className="font-medium">No generation data available</p>
                </div>
            </div>
        );
    }

    return (
        <div className="w-full bg-white rounded-lg shadow-md p-4 mt-4">
            <div className="flex items-center justify-between mb-4">
                <h3 className="ds-section-title">
                    Energy Sent to Grid {cycleCount === -1 ? '(All Cycles)' : `(Latest ${cycleCount} Cycles)`}
                </h3>
                <div className="flex items-center space-x-2">
                    <label htmlFor="cycleFilter" className="text-base font-semibold text-ink-700">Show :</label>
                    <select
                        id="cycleFilter"
                        value={cycleCount}
                        onChange={(e) => setCycleCount(Number(e.target.value))}
                        className="bg-ink-50 border border-ink-300 text-ink-800 text-base font-medium rounded-lg focus:ring-navy-500 focus:border-navy-500 block w-48 px-4 py-2 shadow-sm cursor-pointer transition duration-150 ease-in-out hover:border-navy-400"
                    >
                        <option value={3}>Last 3 cycles</option>
                        <option value={6}>Last 6 cycles</option>
                        <option value={12}>Last 12 cycles</option>
                        <option value={-1}>All cycles</option>
                    </select>
                </div>
            </div>
            <div className="relative h-96">
                <Line ref={chartRef} data={generationData} options={chartOptions} />
            </div>
        </div>
    );
};

export default GenerationGraph;
