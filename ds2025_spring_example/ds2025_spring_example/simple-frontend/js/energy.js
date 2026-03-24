// Energy visualization functions
let energyChart = null;
let currentChartType = 'line';

function populateDeviceDropdown(devices) {
    const select = document.getElementById('deviceSelect');
    select.innerHTML = '<option value="">-- Select a device --</option>' + 
        devices.map(device => `<option value="${device.id}">${device.name}</option>`).join('');
}

async function loadEnergyData() {
    const deviceId = document.getElementById('deviceSelect').value;
    const selectedDate = document.getElementById('dateSelect').value;
    const errorDiv = document.getElementById('chartError');

    if (!deviceId || !selectedDate) {
        errorDiv.textContent = 'Please select both a device and a date';
        errorDiv.classList.remove('hidden');
        document.getElementById('statsGrid').style.display = 'none';
        if (energyChart) { 
            energyChart.destroy(); 
            energyChart = null; 
        }
        return;
    }

    errorDiv.classList.add('hidden');

    try {
        const startDate = `${selectedDate}T00:00:00`;
        const endDate = `${selectedDate}T23:59:59`;

        const response = await fetch(`/api/monitoring/devices/${deviceId}/hourly?start=${startDate}&end=${endDate}`);
        if (!response.ok) throw new Error(`Failed to load energy data: ${response.status}`);

        const data = await response.json();

        if (!data || data.length === 0) {
            errorDiv.textContent = 'No energy consumption data available for the selected date';
            errorDiv.classList.remove('hidden');
            document.getElementById('statsGrid').style.display = 'none';
            if (energyChart) { 
                energyChart.destroy(); 
                energyChart = null; 
            }
            return;
        }

        displayEnergyChart(data);
        displayStatistics(data);
    } catch (error) {
        errorDiv.textContent = 'Failed to load energy data: ' + error.message;
        errorDiv.classList.remove('hidden');
        document.getElementById('statsGrid').style.display = 'none';
    }
}

function displayEnergyChart(data) {
    const hours = Array.from({length: 24}, (_, i) => i);
    const consumptionMap = new Map();
    data.forEach(item => {
        const hour = new Date(item.hourTimestamp).getHours();
        consumptionMap.set(hour, item.totalConsumption || 0);
    });
    const consumptionValues = hours.map(hour => consumptionMap.get(hour) || 0);

    const ctx = document.getElementById('energyChart').getContext('2d');
    if (energyChart) energyChart.destroy();

    energyChart = new Chart(ctx, {
        type: currentChartType,
        data: {
            labels: hours.map(h => `${h}:00`),
            datasets: [{
                label: 'Energy Consumption (kWh)',
                data: consumptionValues,
                backgroundColor: currentChartType === 'bar' ? 'rgba(52, 152, 219, 0.6)' : 'rgba(52, 152, 219, 0.2)',
                borderColor: 'rgba(52, 152, 219, 1)',
                borderWidth: 2,
                fill: currentChartType === 'line',
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: true, position: 'top' },
                title: { display: true, text: 'Hourly Energy Consumption', font: { size: 16 } }
            },
            scales: {
                x: { title: { display: true, text: 'Hour of Day' } },
                y: { title: { display: true, text: 'Energy Consumption (kWh)' }, beginAtZero: true }
            }
        }
    });
}

function displayStatistics(data) {
    document.getElementById('statsGrid').style.display = 'grid';
    const totalConsumption = data.reduce((sum, item) => sum + (item.totalConsumption || 0), 0);
    const avgConsumption = data.length > 0 ? totalConsumption / data.length : 0;
    let peakItem = data.reduce((max, item) => (item.totalConsumption || 0) > (max.totalConsumption || 0) ? item : max, data[0] || {});
    const peakHour = peakItem ? new Date(peakItem.hourTimestamp).getHours() : 0;
    const peakValue = peakItem ? (peakItem.totalConsumption || 0) : 0;

    document.getElementById('totalConsumption').textContent = totalConsumption.toFixed(3);
    document.getElementById('avgConsumption').textContent = avgConsumption.toFixed(3);
    document.getElementById('peakHour').textContent = `${peakHour}:00`;
    document.getElementById('peakValue').textContent = peakValue.toFixed(3);
}

function changeChartType(type) {
    currentChartType = type;
    const deviceId = document.getElementById('deviceSelect').value;
    const selectedDate = document.getElementById('dateSelect').value;
    if (deviceId && selectedDate) loadEnergyData();
}
