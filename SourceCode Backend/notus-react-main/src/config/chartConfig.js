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

// Register ChartJS components once
export const registerChartJS = () => {
    if (!ChartJS.registry.get('line')) {
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
    }
};

// Default chart colors
export const CHART_COLORS = {
    offPeak: {
        backgroundColor: 'rgba(255, 99, 132, 0.2)',
        borderColor: 'rgba(255, 99, 132, 1)',
        pointBackgroundColor: 'rgba(255, 99, 132, 1)',
        pointBorderColor: 'white',
    },
    day: {
        backgroundColor: 'rgba(54, 162, 235, 0.2)',
        borderColor: 'rgba(54, 162, 235, 1)',
        pointBackgroundColor: 'rgba(54, 162, 235, 1)',
        pointBorderColor: 'white',
    },
    peak: {
        backgroundColor: 'rgba(255, 206, 86, 0.2)',
        borderColor: 'rgba(255, 206, 86, 1)',
        pointBackgroundColor: 'rgba(255, 206, 86, 1)',
        pointBorderColor: 'white',
    },
    kva: {
        backgroundColor: 'rgba(75, 192, 192, 0.2)',
        borderColor: 'rgba(75, 192, 192, 1)',
        pointBackgroundColor: 'rgba(75, 192, 192, 1)',
        pointBorderColor: 'white',
    },
};

// Default chart options
export const getChartOptions = (title) => ({
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
        },
        title: {
            display: !!title,
            text: title,
            font: {
                size: 16,
                weight: 'bold',
                family: "'Inter', sans-serif"
            },
            padding: {
                top: 10,
                bottom: 30
            }
        }
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
                    return value.toLocaleString() + ' kWh';
                }
            }
        }
    },
    elements: {
        point: {
            radius: 5,
            hoverRadius: 8,
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
});