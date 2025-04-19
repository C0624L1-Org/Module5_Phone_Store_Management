const revenueByTimeData = /*[[${revenueByTime}]]*/ [];
const dayLabels = revenueByTimeData.map(item => item[0]);
const dayRevenues = revenueByTimeData.map(item => item[1]);

// Hàm để tạo biểu đồ đường
function createLineChart(canvasId, labels, data, label, color) {
    const canvas = document.getElementById(canvasId);
    if (canvas) {
        new Chart(canvas, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: label,
                    data: data,
                    borderColor: color,
                    borderWidth: 2,
                    fill: false
                }]
            },
            options: {
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    }
}

// Tạo biểu đồ doanh thu theo ngày (mặc định hiển thị khi tab "Theo Ngày" active)
createLineChart('dayRevenueChart', dayLabels, dayRevenues, 'Doanh Thu (VNĐ)', 'rgba(54, 162, 235, 1)');

// Giả định dữ liệu doanh thu theo tháng và 3 năm gần đây (bạn cần điều chỉnh controller để cung cấp dữ liệu này)
const monthlyRevenueData = /*[[${monthlyRevenueByTime}]]*/ []; // Cần controller trả về
const monthlyLabels = monthlyRevenueData.map(item => item[0]);
const monthlyRevenues = monthlyRevenueData.map(item => item[1]);
createLineChart('monthlyRevenueChart', monthlyLabels, monthlyRevenues, 'Doanh Thu (VNĐ)', 'rgba(255, 99, 132, 1)');

const multiYearRevenueData = /*[[${multiYearRevenueByTime}]]*/ []; // Cần controller trả về
const multiYearLabels = multiYearRevenueData.map(item => item[0]);
const multiYearRevenues = multiYearRevenueData.map(item => item[1]);
createLineChart('multiYearRevenueChart', multiYearLabels, multiYearRevenues, 'Doanh Thu (VNĐ)', 'rgba(75, 192, 192, 1)');

// Cập nhật tổng doanh thu theo ngày (nếu bạn muốn hiển thị)
const dayTotalRevenueElement = document.getElementById('dayTotalRevenue');
if (dayTotalRevenueElement && dayRevenues.length > 0) {
    const totalDayRevenue = dayRevenues.reduce((sum, revenue) => sum + revenue, 0);
    dayTotalRevenueElement.textContent = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(totalDayRevenue);
}

// Cập nhật tổng doanh thu theo tháng (nếu bạn muốn hiển thị)
const monthlyTotalRevenueElement = document.getElementById('monthlyTotalRevenue');
if (monthlyTotalRevenueElement && monthlyRevenues.length > 0) {
    const totalMonthlyRevenue = monthlyRevenues.reduce((sum, revenue) => sum + revenue, 0);
    monthlyTotalRevenueElement.textContent = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(totalMonthlyRevenue);
}

// Cập nhật tổng doanh thu 3 năm gần đây (nếu bạn muốn hiển thị)
const multiYearTotalRevenueElement = document.getElementById('multiYearTotalRevenue');
if (multiYearTotalRevenueElement && multiYearRevenues.length > 0) {
    const totalMultiYearRevenue = multiYearRevenues.reduce((sum, revenue) => sum + revenue, 0);
    multiYearTotalRevenueElement.textContent = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(totalMultiYearRevenue);
}

// --- Logic cho navigation (nếu bạn muốn tải lại dữ liệu khi chuyển đổi) ---
document.getElementById('prevWeek').addEventListener('click', function() {
    // Gọi API hoặc cập nhật dữ liệu cho tháng trước
    console.log('Tháng trước');
});

document.getElementById('currentWeek').addEventListener('click', function() {
    // Gọi API hoặc cập nhật dữ liệu cho tháng hiện tại
    console.log('Tháng hiện tại');
});

document.getElementById('nextWeek').addEventListener('click', function() {
    // Gọi API hoặc cập nhật dữ liệu cho tháng sau
    console.log('Tháng sau');
});

document.getElementById('prevMonth').addEventListener('click', function() {
    // Gọi API hoặc cập nhật dữ liệu cho năm trước (theo tháng)
    console.log('Năm trước');
});

document.getElementById('currentMonth').addEventListener('click', function() {
    // Gọi API hoặc cập nhật dữ liệu cho năm hiện tại (theo tháng)
    console.log('Năm hiện tại');
});

document.getElementById('nextMonth').addEventListener('click', function() {
    // Gọi API hoặc cập nhật dữ liệu cho năm sau (theo tháng)
    console.log('Năm sau');
});

document.getElementById('prevYear').addEventListener('click', function() {
    // Gọi API hoặc cập nhật dữ liệu cho năm trước
    console.log('Năm trước');
});

document.getElementById('currentYear').addEventListener('click', function() {
    // Gọi API hoặc cập nhật dữ liệu cho năm hiện tại
    console.log('Năm hiện tại');
});

document.getElementById('nextYear').addEventListener('click', function() {
    // Gọi API hoặc cập nhật dữ liệu cho năm sau
    console.log('Năm sau');
});

// Cập nhật label ngày hiện tại (nếu bạn muốn)
const dayLabelElement = document.getElementById('dayLabel');
if (dayLabelElement) {
    const today = new Date();
    const formattedDate = `${today.toLocaleDateString('vi-VN')}`;
    dayLabelElement.textContent = `Ngày hiện tại: ${formattedDate}`;
}

// Cập nhật label tháng hiện tại (nếu bạn muốn)
const monthLabelElement = document.getElementById('monthLabel');
if (monthLabelElement) {
    const today = new Date();
    const formattedMonth = `${today.toLocaleDateString('vi-VN', { month: '2-digit', year: 'numeric' })}`;
    monthLabelElement.textContent = `Tháng ${formattedMonth}`;
}

// Cập nhật label năm hiện tại (nếu bạn muốn)
const yearLabelElement = document.getElementById('yearLabel');
if (yearLabelElement) {
    const today = new Date();
    const formattedYear = `${today.getFullYear()}`;
    yearLabelElement.textContent = `Năm ${formattedYear}`;
}