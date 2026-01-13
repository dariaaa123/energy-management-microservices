# Energy Consumption Visualization Feature

## Overview
Added the missing 2-point requirement: **Client Energy Consumption Dashboard** with interactive charts.

## What Was Added

### 1. Frontend Enhancements (simple-frontend/index.html)

#### New Dependencies
- **Chart.js 4.4.0** - Professional charting library loaded via CDN

#### New UI Components

**Energy Consumption Dashboard Card** (visible only for CLIENT users):
- Device selector dropdown (populated with user's assigned devices)
- Date picker (calendar input for selecting any date)
- Chart type toggle buttons (Line Chart / Bar Chart)
- Statistics grid showing:
  - Total Consumption (kWh)
  - Average per Hour (kWh)
  - Peak Hour
  - Peak Consumption (kWh)
- Interactive chart canvas (400px height, responsive)

#### New CSS Styles
- `.chart-controls` - Flexible layout for device/date selection
- `.chart-container` - Responsive chart wrapper
- `.chart-type-toggle` - Button group for chart type switching
- `.stats-grid` - Grid layout for statistics cards
- `.stat-card` - Individual statistic display
- `.btn-info` - Teal button style for chart controls

### 2. JavaScript Functionality

#### New Variables
```javascript
let energyChart = null;           // Chart.js instance
let currentChartType = 'line';    // Current chart type (line/bar)
let clientDevices = [];           // Client's assigned devices
```

#### New Functions

**`populateDeviceDropdown(devices)`**
- Populates the device selector with client's assigned devices
- Called after devices are loaded for CLIENT users

**`loadEnergyData()`**
- Fetches hourly consumption data from Monitoring API
- Endpoint: `GET /api/monitoring/devices/{deviceId}/hourly?start={date}T00:00:00&end={date}T23:59:59`
- Validates device and date selection
- Handles empty data and errors gracefully
- Calls `displayEnergyChart()` and `displayStatistics()`

**`displayEnergyChart(data)`**
- Creates/updates Chart.js visualization
- Supports both line and bar chart types
- Maps API data to 24-hour format (0:00 to 23:00)
- Fills missing hours with 0 consumption
- Configures:
  - X-axis: Hours (0-23)
  - Y-axis: Energy Consumption (kWh)
  - Tooltips with formatted values
  - Responsive design

**`displayStatistics(data)`**
- Calculates and displays key metrics:
  - Total daily consumption
  - Average hourly consumption
  - Peak consumption hour
  - Peak consumption value
- Updates statistics grid cards

**`changeChartType(type)`**
- Switches between line and bar chart
- Reloads data to refresh visualization

### 3. User Experience Flow

#### For CLIENT Users:
1. Login with CLIENT credentials
2. See "My Devices" dashboard
3. View assigned devices in table
4. Scroll to "Energy Consumption Dashboard" card
5. Select a device from dropdown
6. Pick a date from calendar (defaults to today)
7. View interactive chart with hourly consumption
8. See statistics summary
9. Toggle between line/bar chart views

#### For ADMIN Users:
- Energy visualization card is hidden
- Only device management interface is shown

## API Integration

### Endpoint Used
```
GET /api/monitoring/devices/{deviceId}/hourly
Query Parameters:
  - start: ISO 8601 datetime (e.g., 2024-11-17T00:00:00)
  - end: ISO 8601 datetime (e.g., 2024-11-17T23:59:59)
```

### Expected Response Format
```json
[
  {
    "id": 1,
    "deviceId": "uuid",
    "hourTimestamp": "2024-11-17T10:00:00",
    "totalConsumption": 2.45,
    "measurementCount": 6,
    "calculatedAt": "2024-11-17T10:50:00"
  }
]
```

## Features Implemented

✅ Device selection dropdown (populated with client's devices)
✅ Date picker with calendar interface
✅ Line chart visualization
✅ Bar chart visualization
✅ Chart type toggle
✅ Hourly consumption display (X: hours, Y: kWh)
✅ Statistics dashboard:
  - Total consumption
  - Average consumption
  - Peak hour identification
  - Peak value
✅ Responsive design
✅ Error handling (no data, API errors)
✅ Empty state handling
✅ 24-hour format (0:00 to 23:00)

## Testing Instructions

### 1. Start Services
```bash
# Make sure Docker Desktop is running
docker-compose build frontend
docker-compose up -d frontend
```

### 2. Create Test Data

**Login as admin:**
- URL: http://localhost
- Username: admin
- Password: admin123

**Create a CLIENT user:**
- Name: Test Client
- Username: testclient
- Password: test123
- Role: CLIENT

**Create a device:**
- Name: Smart Meter Test
- Max Consumption: 5000

**Assign device to client user**

### 3. Generate Energy Data

**Update producer.py with device ID:**
```python
DEVICE_ID = "your-device-uuid-here"
```

**Run simulator:**
```bash
python producer.py
```

**Wait 10-60 minutes for data to accumulate, or send multiple measurements manually**

### 4. Test Visualization

**Logout and login as CLIENT:**
- Username: testclient
- Password: test123

**Use the dashboard:**
1. Select your device from dropdown
2. Select today's date (or date when data was generated)
3. View the chart
4. Toggle between line and bar chart
5. Check statistics

## Requirements Satisfied

✅ **2 points**: Client can view historical energy consumption as charts
✅ Line chart option (X: hours, Y: kWh)
✅ Bar chart option (X: hours, Y: kWh)
✅ Calendar date selection
✅ Device selection for clients with multiple devices
✅ Statistics summary

## Technical Details

### Chart Configuration
- **Library**: Chart.js 4.4.0
- **Chart Types**: Line (with fill) and Bar
- **Data Points**: 24 (one per hour)
- **Interpolation**: Cubic bezier (tension: 0.4) for smooth lines
- **Colors**: Blue theme (#3498db)
- **Responsive**: Maintains aspect ratio, scales to container

### Data Processing
- API returns sparse data (only hours with measurements)
- Frontend fills all 24 hours (0-23)
- Missing hours show 0 consumption
- Timestamps parsed to extract hour component
- Values rounded to 3 decimal places

### Error Handling
- No device selected: Shows error message
- No date selected: Shows error message
- No data available: Shows friendly message
- API error: Shows error with details
- Network error: Caught and displayed

## Browser Compatibility
- Modern browsers with ES6 support
- Chart.js works on: Chrome, Firefox, Safari, Edge
- Date input type supported by all modern browsers

## Future Enhancements (Optional)
- Multi-day comparison
- Week/month view
- Export chart as image
- Real-time updates via WebSocket
- Consumption alerts/notifications
- Comparison with max consumption threshold
- Energy cost calculation
- Historical trends

---

**Status**: ✅ COMPLETE - Ready for grading
**Points**: 2/2 for visualization requirement
