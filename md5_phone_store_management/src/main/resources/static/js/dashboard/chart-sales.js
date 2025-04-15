// Các biến toàn cục
let dayChart = null;
let monthlyChart = null;
let yearlyChart = null;
let multiYearChart = null;

// Biến toàn cục để theo dõi thông tin sản phẩm và thời gian hiện tại
let currentMonth = new Date().getMonth() + 1;  // Tháng hiện tại (1-12)
let currentYear = new Date().getFullYear();    // Năm hiện tại
let currentProductId = null;                   // Mã sản phẩm hiện tại (null = tất cả sản phẩm)
let currentProductName = null;                 // Tên sản phẩm hiện tại

// Định dạng tiền tệ
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', { style: 'decimal' }).format(amount) + ' VND';
}

// Hàm bắt đầu khi tài liệu đã tải xong
document.addEventListener('DOMContentLoaded', function() {
    // Khởi tạo tất cả biểu đồ
    initDayChart();
    initMonthlyChart();
    initMultiYearChart();

    // Gắn sự kiện cho các nút điều hướng
    setupNavigationButtons();
});

// Cập nhật biểu đồ theo thời gian từ backend
function updateChartDateTime(month, year, productId, productName) {
    // Cập nhật biến toàn cục
    currentMonth = month;
    currentYear = year;
    currentProductId = productId;
    currentProductName = productName;

    console.log(`Đang cập nhật biểu đồ theo ngày: tháng=${month}, năm=${year}, sản phẩm=${productId ? productId : 'tất cả'}`);

    // Cập nhật label và dữ liệu biểu đồ
    updateDayLabel();
    loadDayChartData(currentMonth, currentYear);
}

// Cập nhật biểu đồ theo năm từ backend
function updateChartYear(year, productId, productName) {
    // Cập nhật biến toàn cục
    currentYear = year;
    currentProductId = productId;
    currentProductName = productName;

    console.log(`Đang cập nhật biểu đồ theo tháng: năm=${year}, sản phẩm=${productId ? productId : 'tất cả'}`);

    // Cập nhật label và dữ liệu biểu đồ
    updateMonthLabel();
    loadMonthlyChartData(currentYear);
}

// Khởi tạo biểu đồ doanh thu theo ngày
function initDayChart() {
    const ctx = document.getElementById('dayRevenueChart').getContext('2d');

    // Cập nhật label hiển thị tháng và năm
    updateDayLabel();

    // Tạo biểu đồ trống trước
    dayChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: [],
            datasets: [{
                label: 'Doanh Thu (VNĐ)',
                backgroundColor: 'rgba(78, 115, 223, 0.7)',
                borderColor: 'rgba(78, 115, 223, 1)',
                borderWidth: 1,
                data: []
            }, {
                label: 'Số Hóa Đơn',
                backgroundColor: 'rgba(255, 159, 64, 0.7)',
                borderColor: 'rgba(255, 159, 64, 1)',
                borderWidth: 1,
                data: [],
                yAxisID: 'y1'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Doanh Thu (VNĐ)'
                    }
                },
                y1: {
                    beginAtZero: true,
                    position: 'right',
                    grid: {
                        drawOnChartArea: false
                    },
                    title: {
                        display: true,
                        text: 'Số Hóa Đơn'
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: 'Ngày trong tháng'
                    }
                }
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            let label = context.dataset.label || '';
                            if (label) {
                                label += ': ';
                            }
                            if (context.dataset.yAxisID === 'y1') {
                                label += context.parsed.y;
                            } else {
                                label += formatCurrency(context.parsed.y);
                            }
                            return label;
                        }
                    }
                }
            }
        }
    });

    // Tải dữ liệu
    loadDayChartData(currentMonth, currentYear);
}

// Tải dữ liệu biểu đồ theo ngày
function loadDayChartData(month, year) {
    let apiUrl = `/api/chart/day?month=${month}&year=${year}`;

    // Thêm mã sản phẩm vào URL nếu có
    if (currentProductId) {
        apiUrl += `&productId=${currentProductId}`;
    }

    fetch(apiUrl)
        .then(response => response.json())
        .then(data => {
            if (Object.keys(data).length === 0) {
                // Không có dữ liệu
                updateChartWithNoData(dayChart);
                document.getElementById('dayTotalRevenue').textContent = formatCurrency(0);
                return;
            }

            // Tạo mảng đầy đủ các ngày trong tháng
            const daysInMonth = getDaysInMonth(month, year);
            const allDays = Array.from({length: daysInMonth}, (_, i) => i + 1);
            const revenueData = new Array(daysInMonth).fill(0);
            const invoiceCountData = new Array(daysInMonth).fill(0);

            // Điền dữ liệu vào đúng vị trí trong mảng
            if (data.days && data.days.length > 0) {
                data.days.forEach((day, index) => {
                    const dayIndex = day - 1; // Chuyển từ ngày (1-31) sang chỉ số mảng (0-30)
                    if (dayIndex >= 0 && dayIndex < daysInMonth) {
                        revenueData[dayIndex] = data.revenueSums[index] || 0;
                        invoiceCountData[dayIndex] = data.invoiceCounts[index] || 0;
                    }
                });
            }

            // Nếu API trả về thông tin sản phẩm, cập nhật biến toàn cục nếu chưa được đặt
            if (data.productId && !currentProductId) {
                currentProductId = data.productId;
            }

            // Cập nhật tiêu đề biểu đồ nếu có sản phẩm
            if (currentProductId) {
                const productTitle = currentProductName
                    ? `Doanh thu sản phẩm: ${currentProductName} (ID: ${currentProductId})`
                    : `Doanh thu sản phẩm ID: ${currentProductId}`;

                dayChart.options.plugins.title = {
                    display: true,
                    text: productTitle,
                    font: {
                        size: 16,
                        weight: 'bold'
                    }
                };
            } else {
                if (dayChart.options.plugins.title) {
                    dayChart.options.plugins.title.display = false;
                }
            }

            // Cập nhật biểu đồ với dữ liệu mới
            dayChart.data.labels = allDays;
            dayChart.data.datasets[0].data = revenueData;
            dayChart.data.datasets[1].data = invoiceCountData;
            dayChart.update();

            // Tính tổng doanh thu
            const totalRevenue = revenueData.reduce((sum, current) => sum + current, 0);
            document.getElementById('dayTotalRevenue').textContent = formatCurrency(totalRevenue);
        })
        .catch(error => {
            console.error("Lỗi khi tải dữ liệu biểu đồ theo ngày:", error);
            updateChartWithNoData(dayChart);
            document.getElementById('dayTotalRevenue').textContent = formatCurrency(0);
        });
}

// Tính số ngày trong tháng
function getDaysInMonth(month, year) {
    // Tháng trong JavaScript được đánh số từ 0-11
    return new Date(year, month, 0).getDate();
}

// Khởi tạo biểu đồ doanh thu theo tháng
function initMonthlyChart() {
    const ctx = document.getElementById('monthlyRevenueChart').getContext('2d');

    // Cập nhật label hiển thị năm
    updateMonthLabel();

    // Tạo biểu đồ trống trước
    monthlyChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: [],
            datasets: [{
                label: 'Doanh Thu (VNĐ)',
                backgroundColor: 'rgba(40, 167, 69, 0.7)',
                borderColor: 'rgba(40, 167, 69, 1)',
                borderWidth: 1,
                data: []
            }, {
                label: 'Số Hóa Đơn',
                backgroundColor: 'rgba(255, 193, 7, 0.7)',
                borderColor: 'rgba(255, 193, 7, 1)',
                borderWidth: 1,
                data: [],
                yAxisID: 'y1'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Doanh Thu (VNĐ)'
                    }
                },
                y1: {
                    beginAtZero: true,
                    position: 'right',
                    grid: {
                        drawOnChartArea: false
                    },
                    title: {
                        display: true,
                        text: 'Số Hóa Đơn'
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: 'Tháng'
                    }
                }
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            let label = context.dataset.label || '';
                            if (label) {
                                label += ': ';
                            }
                            if (context.dataset.yAxisID === 'y1') {
                                label += context.parsed.y;
                            } else {
                                label += formatCurrency(context.parsed.y);
                            }
                            return label;
                        }
                    }
                }
            }
        }
    });

    // Tải dữ liệu
    loadMonthlyChartData(currentYear);
}

// Tải dữ liệu biểu đồ theo tháng
function loadMonthlyChartData(year) {
    let apiUrl = `/api/revenue/monthly?year=${year}`;

    // Thêm mã sản phẩm vào URL nếu có
    if (currentProductId) {
        apiUrl += `&productId=${currentProductId}`;
    }

    fetch(apiUrl)
        .then(response => response.json())
        .then(data => {
            if (Object.keys(data).length === 0) {
                // Không có dữ liệu
                updateChartWithNoData(monthlyChart);
                document.getElementById('monthlyTotalRevenue').textContent = formatCurrency(0);
                return;
            }

            // Nếu API trả về thông tin sản phẩm, cập nhật biến toàn cục
            if (data.productId && !currentProductId) {
                currentProductId = data.productId;
            }

            // Chuyển đổi số tháng sang tên tháng
            const monthNames = [
                'Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6',
                'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12'
            ];

            // Tạo mảng dữ liệu cho 12 tháng (tất cả đều là 0 ban đầu)
            const revenueData = new Array(12).fill(0);
            const invoiceCountData = new Array(12).fill(0);

            // Điền dữ liệu vào đúng vị trí trong mảng
            if (data.months && data.months.length > 0) {
                data.months.forEach((month, index) => {
                    const monthIndex = month - 1; // Chuyển từ tháng (1-12) sang chỉ số mảng (0-11)
                    if (monthIndex >= 0 && monthIndex < 12) {
                        revenueData[monthIndex] = data.revenueSums[index] || 0;
                        invoiceCountData[monthIndex] = data.invoiceCounts[index] || 0;
                    }
                });
            }

            // Cập nhật tiêu đề biểu đồ nếu có sản phẩm
            if (currentProductId) {
                const productTitle = currentProductName
                    ? `Doanh thu sản phẩm: ${currentProductName} (ID: ${currentProductId})`
                    : `Doanh thu sản phẩm ID: ${currentProductId}`;

                monthlyChart.options.plugins.title = {
                    display: true,
                    text: productTitle,
                    font: {
                        size: 16,
                        weight: 'bold'
                    }
                };
            } else {
                if (monthlyChart.options.plugins.title) {
                    monthlyChart.options.plugins.title.display = false;
                }
            }

            // Cập nhật biểu đồ với dữ liệu mới
            monthlyChart.data.labels = monthNames;
            monthlyChart.data.datasets[0].data = revenueData;
            monthlyChart.data.datasets[1].data = invoiceCountData;
            monthlyChart.update();

            // Tính tổng doanh thu
            const totalRevenue = revenueData.reduce((sum, current) => sum + current, 0);
            document.getElementById('monthlyTotalRevenue').textContent = formatCurrency(totalRevenue);
        })
        .catch(error => {
            console.error("Lỗi khi tải dữ liệu biểu đồ theo tháng:", error);
            updateChartWithNoData(monthlyChart);
            document.getElementById('monthlyTotalRevenue').textContent = formatCurrency(0);
        });
}

// Khởi tạo biểu đồ doanh thu 3 năm gần đây
function initMultiYearChart() {
    const ctx = document.getElementById('multiYearRevenueChart').getContext('2d');

    // Tạo biểu đồ trống trước
    multiYearChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: [],
            datasets: [{
                label: 'Doanh Thu (VNĐ)',
                backgroundColor: 'rgba(23, 162, 184, 0.7)',
                borderColor: 'rgba(23, 162, 184, 1)',
                borderWidth: 1,
                data: []
            }, {
                label: 'Số Hóa Đơn',
                backgroundColor: 'rgba(220, 53, 69, 0.7)',
                borderColor: 'rgba(220, 53, 69, 1)',
                borderWidth: 1,
                data: [],
                yAxisID: 'y1'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Doanh Thu (VNĐ)'
                    }
                },
                y1: {
                    beginAtZero: true,
                    position: 'right',
                    grid: {
                        drawOnChartArea: false
                    },
                    title: {
                        display: true,
                        text: 'Số Hóa Đơn'
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: 'Năm'
                    }
                }
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            let label = context.dataset.label || '';
                            if (label) {
                                label += ': ';
                            }
                            if (context.dataset.yAxisID === 'y1') {
                                label += context.parsed.y;
                            } else {
                                label += formatCurrency(context.parsed.y);
                            }
                            return label;
                        }
                    }
                }
            }
        }
    });

    // Tải dữ liệu
    loadMultiYearChartData();
}

// Tải dữ liệu biểu đồ 3 năm gần đây
function loadMultiYearChartData() {
    let apiUrl = '/api/revenue/yearly';

    // Thêm mã sản phẩm vào URL nếu có
    if (currentProductId) {
        apiUrl += `?productId=${currentProductId}`;
    }

    fetch(apiUrl)
        .then(response => response.json())
        .then(data => {
            if (Object.keys(data).length === 0) {
                // Không có dữ liệu
                updateChartWithNoData(multiYearChart);
                document.getElementById('multiYearTotalRevenue').textContent = formatCurrency(0);
                return;
            }

            // Nếu API trả về thông tin sản phẩm, cập nhật biến toàn cục
            if (data.productId && !currentProductId) {
                currentProductId = data.productId;
            }

            // Cập nhật tiêu đề biểu đồ nếu có sản phẩm
            if (currentProductId) {
                const productTitle = currentProductName
                    ? `Doanh thu sản phẩm: ${currentProductName} (ID: ${currentProductId})`
                    : `Doanh thu sản phẩm ID: ${currentProductId}`;

                multiYearChart.options.plugins.title = {
                    display: true,
                    text: productTitle,
                    font: {
                        size: 16,
                        weight: 'bold'
                    }
                };
            } else {
                if (multiYearChart.options.plugins.title) {
                    multiYearChart.options.plugins.title.display = false;
                }
            }

            // Cập nhật biểu đồ với dữ liệu mới
            multiYearChart.data.labels = data.years;
            multiYearChart.data.datasets[0].data = data.revenueSums;
            multiYearChart.data.datasets[1].data = data.invoiceCounts;
            multiYearChart.update();

            // Tính tổng doanh thu
            const totalRevenue = data.revenueSums.reduce((sum, current) => sum + current, 0);
            document.getElementById('multiYearTotalRevenue').textContent = formatCurrency(totalRevenue);
        })
        .catch(error => {
            console.error("Lỗi khi tải dữ liệu biểu đồ 3 năm gần đây:", error);
            updateChartWithNoData(multiYearChart);
            document.getElementById('multiYearTotalRevenue').textContent = formatCurrency(0);
        });
}

// Cập nhật biểu đồ khi không có dữ liệu
function updateChartWithNoData(chart) {
    chart.data.labels = ['Không có dữ liệu'];
    chart.data.datasets.forEach(dataset => {
        dataset.data = [0];
    });
    chart.update();
}

// Cập nhật label thời gian
function updateDayLabel() {
    const months = [
        'Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6',
        'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12'
    ];

    let labelText = `<i class="far fa-calendar-alt me-2"></i> Biểu đồ doanh thu ${months[currentMonth - 1]} năm ${currentYear}`;

    // Thêm thông tin sản phẩm nếu có
    if (currentProductId && currentProductName) {
        labelText += ` - ${currentProductName} (ID: ${currentProductId})`;
    }

    document.getElementById('dayLabel').innerHTML = labelText;
}

function updateMonthLabel() {
    let labelText = `<i class="far fa-calendar-alt me-2"></i> Biểu đồ doanh thu năm ${currentYear}`;

    // Thêm thông tin sản phẩm nếu có
    if (currentProductId && currentProductName) {
        labelText += ` - ${currentProductName} (ID: ${currentProductId})`;
    }

    document.getElementById('monthLabel').innerHTML = labelText;
}

// Thiết lập các nút điều hướng
function setupNavigationButtons() {
    // Nút điều hướng theo ngày
    document.getElementById('prevWeek').addEventListener('click', function() {
        currentMonth--;
        if (currentMonth < 1) {
            currentMonth = 12;
            currentYear--;
        }
        updateDayLabel();
        loadDayChartData(currentMonth, currentYear);
    });

    document.getElementById('nextWeek').addEventListener('click', function() {
        currentMonth++;
        if (currentMonth > 12) {
            currentMonth = 1;
            currentYear++;
        }
        updateDayLabel();
        loadDayChartData(currentMonth, currentYear);
    });

    document.getElementById('currentWeek').addEventListener('click', function() {
        currentMonth = new Date().getMonth() + 1;
        currentYear = new Date().getFullYear();
        updateDayLabel();
        loadDayChartData(currentMonth, currentYear);
    });

    // Nút điều hướng theo tháng
    document.getElementById('prevMonth').addEventListener('click', function() {
        currentYear--;
        updateMonthLabel();
        loadMonthlyChartData(currentYear);
    });

    document.getElementById('nextMonth').addEventListener('click', function() {
        currentYear++;
        updateMonthLabel();
        loadMonthlyChartData(currentYear);
    });

    document.getElementById('currentMonth').addEventListener('click', function() {
        currentYear = new Date().getFullYear();
        updateMonthLabel();
        loadMonthlyChartData(currentYear);
    });

    // Nút reset bộ lọc sản phẩm (nếu có)
    const resetButtons = document.querySelectorAll('.reset-product-filter');
    if (resetButtons && resetButtons.length > 0) {
        resetButtons.forEach(button => {
            button.addEventListener('click', function() {
                resetProductFilter();
            });
        });
    }
}

// Hàm reset sản phẩm về mặc định (tất cả sản phẩm)
function resetProductFilter() {
    currentProductId = null;
    currentProductName = null;

    // Cập nhật lại các biểu đồ
    updateDayLabel();
    loadDayChartData(currentMonth, currentYear);

    updateMonthLabel();
    loadMonthlyChartData(currentYear);

    loadMultiYearChartData();
}
