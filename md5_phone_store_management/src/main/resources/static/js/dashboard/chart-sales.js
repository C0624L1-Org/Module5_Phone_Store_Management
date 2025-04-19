// Các biến toàn cục cho biểu đồ
let dayChart = null;
let monthlyChart = null;
let yearlyChart = null; // Note: yearlyChart is not used in your current code, multiYearChart is. Let's stick to multiYearChart.
let multiYearChart = null;

// Biến toàn cục để theo dõi thông tin thời gian hiện tại cho các biểu đồ
let currentMonth = new Date().getMonth() + 1;  // Tháng hiện tại (1-12)
let currentYear = new Date().getFullYear();    // Năm hiện tại

// Định dạng tiền tệ
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', { style: 'decimal' }).format(amount) + ' VND';
}

// Hàm bắt đầu khi tài liệu đã tải xong
document.addEventListener('DOMContentLoaded', function() {
    // Khởi tạo tất cả biểu đồ
    // Load data will happen inside init functions by calling loadChartData
    initDayChart();
    initMonthlyChart();
    initMultiYearChart(); // Changed from yearlyChart

    // Gắn sự kiện cho các nút điều hướng
    setupNavigationButtons();

    // Có thể thêm sự kiện cho nút "Tạo báo cáo" của form chính
    // Nếu bạn muốn biểu đồ cập nhật ngay khi người dùng bấm nút "Tạo báo cáo"
    // mà không load lại trang (cần ngăn chặn submit mặc định của form)
    const reportForm = document.querySelector('.search-form');
    if (reportForm) {
        reportForm.addEventListener('submit', function(event) {
            // Ngăn chặn form submit theo cách truyền thống (load lại trang)
            event.preventDefault();

            // Cập nhật lại các biểu đồ với bộ lọc mới từ form
            // Các hàm loadChartData sẽ tự đọc bộ lọc mới nhất từ form
            loadDayChartData(currentMonth, currentYear); // Load lại biểu đồ ngày với tháng/năm hiện tại và bộ lọc mới
            loadMonthlyChartData(currentYear);         // Load lại biểu đồ tháng với năm hiện tại và bộ lọc mới
            loadMultiYearChartData();                 // Load lại biểu đồ năm với bộ lọc mới

            // Nếu bạn có logic hiển thị báo cáo dạng bảng ở phần khác của trang,
            // bạn cũng cần kích hoạt nó ở đây thông qua AJAX POST hoặc GET tùy cách bạn xử lý.
            // Đoạn code này chỉ tập trung vào việc cập nhật biểu đồ.
        });
    }
});

// --- Hàm đọc giá trị bộ lọc từ form ---
function getFilterParamsFromForm() {
    const paymentMethod = document.getElementById('paymentMethod').value;
    const employeeName = document.getElementById('employeeName').value;
    const productName = document.getElementById('productName').value;

    const params = new URLSearchParams();

    if (paymentMethod !== '') { // Chỉ thêm nếu không phải 'Tất cả'
        params.append('paymentMethod', paymentMethod);
    }
    if (employeeName.trim() !== '') {
        params.append('employeeName', employeeName.trim());
    }
    if (productName.trim() !== '') {
        params.append('productName', productName.trim());
    }

    return params;
}

// --- Hàm cập nhật label hiển thị thời gian và bộ lọc ---
// Cập nhật label thời gian cho biểu đồ ngày
function updateDayLabel() {
    const months = [
        'Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6',
        'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12'
    ];
    document.getElementById('dayLabel').innerHTML = `<i class="far fa-calendar-alt me-2"></i> Biểu đồ doanh thu ${months[currentMonth - 1]} năm ${currentYear}`;
}

// Cập nhật label thời gian cho biểu đồ tháng
function updateMonthLabel() {
    document.getElementById('monthLabel').innerHTML = `<i class="far fa-calendar-alt me-2"></i> Biểu đồ doanh thu năm ${currentYear}`;
}

// --- Hàm khởi tạo biểu đồ (chỉ tạo canvas, load data sau) ---

// Khởi tạo biểu đồ doanh thu theo ngày
function initDayChart() {
    const ctx = document.getElementById('dayRevenueChart').getContext('2d');
    updateDayLabel(); // Cập nhật label ban đầu

    dayChart = new Chart(ctx, {
        type: 'bar',
        data: { labels: [], datasets: [] }, // Dữ liệu rỗng ban đầu
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: { beginAtZero: true, title: { display: true, text: 'Doanh Thu (VNĐ)' } },
                y1: { beginAtZero: true, position: 'right', grid: { drawOnChartArea: false }, title: { display: true, text: 'Số Hóa Đơn' } },
                x: { title: { display: true, text: 'Ngày trong tháng' } }
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            let label = context.dataset.label || '';
                            if (label) { label += ': '; }
                            if (context.dataset.yAxisID === 'y1') {
                                label += context.parsed.y;
                            } else {
                                label += formatCurrency(context.parsed.y);
                            }
                            return label;
                        }
                    }
                },
                title: { // Thêm cấu hình title ở đây, sẽ được cập nhật sau khi load data
                    display: false, // Mặc định ẩn, chỉ hiển thị khi có bộ lọc sản phẩm
                    text: '',
                    font: { size: 16, weight: 'bold' }
                }
            }
        }
    });
    loadDayChartData(currentMonth, currentYear); // Tải dữ liệu sau khi tạo biểu đồ
}

// Khởi tạo biểu đồ doanh thu theo tháng
function initMonthlyChart() {
    const ctx = document.getElementById('monthlyRevenueChart').getContext('2d');
    updateMonthLabel(); // Cập nhật label ban đầu

    monthlyChart = new Chart(ctx, {
        type: 'bar',
        data: { labels: [], datasets: [] }, // Dữ liệu rỗng ban đầu
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: { beginAtZero: true, title: { display: true, text: 'Doanh Thu (VNĐ)' } },
                y1: { beginAtZero: true, position: 'right', grid: { drawOnChartArea: false }, title: { display: true, text: 'Số Hóa Đơn' } },
                x: { title: { display: true, text: 'Tháng' } }
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            let label = context.dataset.label || '';
                            if (label) { label += ': '; }
                            if (context.dataset.yAxisID === 'y1') {
                                label += context.parsed.y;
                            } else {
                                label += formatCurrency(context.parsed.y);
                            }
                            return label;
                        }
                    }
                },
                title: { // Thêm cấu hình title
                    display: false,
                    text: '',
                    font: { size: 16, weight: 'bold' }
                }
            }
        }
    });
    loadMonthlyChartData(currentYear); // Tải dữ liệu sau khi tạo biểu đồ
}

// Khởi tạo biểu đồ doanh thu 3 năm gần đây
function initMultiYearChart() { // Changed from initYearlyChart
    const ctx = document.getElementById('multiYearRevenueChart').getContext('2d'); // Changed ID

    multiYearChart = new Chart(ctx, {
        type: 'bar',
        data: { labels: [], datasets: [] }, // Dữ liệu rỗng ban đầu
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: { beginAtZero: true, title: { display: true, text: 'Doanh Thu (VNĐ)' } },
                y1: { beginAtZero: true, position: 'right', grid: { drawOnChartArea: false }, title: { display: true, text: 'Số Hóa Đơn' } },
                x: { title: { display: true, text: 'Năm' } }
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            let label = context.dataset.label || '';
                            if (label) { label += ': '; }
                            if (context.dataset.yAxisID === 'y1') {
                                label += context.parsed.y;
                            } else {
                                label += formatCurrency(context.parsed.y);
                            }
                            return label;
                        }
                    }
                },
                title: { // Thêm cấu hình title
                    display: false,
                    text: '',
                    font: { size: 16, weight: 'bold' }
                }
            }
        }
    });
    loadMultiYearChartData(); // Tải dữ liệu sau khi tạo biểu đồ
}


// --- Hàm tải dữ liệu biểu đồ từ API ---

// Tải dữ liệu biểu đồ theo ngày
function loadDayChartData(month, year) {
    const filterParams = getFilterParamsFromForm();
    filterParams.append('month', month);
    filterParams.append('year', year);

    const apiUrl = `/api/chart/day?${filterParams.toString()}`;

    fetch(apiUrl)
        .then(response => response.json())
        .then(data => {
            if (!data || Object.keys(data).length === 0 || !data.days || data.days.length === 0) {
                // Không có dữ liệu
                updateChartWithNoData(dayChart, 'Không có dữ liệu cho khoảng thời gian và bộ lọc này.');
                document.getElementById('dayTotalRevenue').textContent = formatCurrency(0);
                // Cập nhật title dựa trên bộ lọc hiện tại
                updateChartTitle(dayChart, filterParams.get('productName'));
                return;
            }

            // Tạo mảng đầy đủ các ngày trong tháng
            const daysInMonth = getDaysInMonth(month, year);
            const allDays = Array.from({length: daysInMonth}, (_, i) => i + 1);
            const revenueData = new Array(daysInMonth).fill(0);
            const invoiceCountData = new Array(daysInMonth).fill(0);

            // Điền dữ liệu vào đúng vị trí trong mảng
            data.days.forEach((day, index) => {
                const dayIndex = day - 1; // Chuyển từ ngày (1-31) sang chỉ số mảng (0-30)
                if (dayIndex >= 0 && dayIndex < daysInMonth) {
                    revenueData[dayIndex] = data.revenueSums[index] || 0;
                    invoiceCountData[dayIndex] = data.invoiceCounts[index] || 0;
                }
            });

            // Cập nhật tiêu đề biểu đồ dựa trên bộ lọc sản phẩm hiện tại
            updateChartTitle(dayChart, filterParams.get('productName'));


            // Cập nhật biểu đồ với dữ liệu mới
            dayChart.data.labels = allDays;
            dayChart.data.datasets = [{
                label: 'Doanh Thu (VNĐ)',
                backgroundColor: 'rgba(78, 115, 223, 0.7)',
                borderColor: 'rgba(78, 115, 223, 1)',
                borderWidth: 1,
                data: revenueData
            }, {
                label: 'Số Hóa Đơn',
                backgroundColor: 'rgba(255, 159, 64, 0.7)',
                borderColor: 'rgba(255, 159, 64, 1)',
                borderWidth: 1,
                data: invoiceCountData,
                yAxisID: 'y1'
            }];
            dayChart.update();

            // Tính tổng doanh thu
            const totalRevenue = revenueData.reduce((sum, current) => sum + current, 0);
            document.getElementById('dayTotalRevenue').textContent = formatCurrency(totalRevenue);
        })
        .catch(error => {
            console.error("Lỗi khi tải dữ liệu biểu đồ theo ngày:", error);
            updateChartWithNoData(dayChart, 'Đã xảy ra lỗi khi tải dữ liệu.');
            document.getElementById('dayTotalRevenue').textContent = formatCurrency(0);
            updateChartTitle(dayChart, filterParams.get('productName'));
        });
}

// Tải dữ liệu biểu đồ theo tháng
function loadMonthlyChartData(year) {
    const filterParams = getFilterParamsFromForm();
    filterParams.append('year', year);

    const apiUrl = `/api/revenue/monthly?${filterParams.toString()}`;

    fetch(apiUrl)
        .then(response => response.json())
        .then(data => {
            if (!data || Object.keys(data).length === 0 || !data.months || data.months.length === 0) {
                // Không có dữ liệu
                updateChartWithNoData(monthlyChart, 'Không có dữ liệu cho khoảng thời gian và bộ lọc này.');
                document.getElementById('monthlyTotalRevenue').textContent = formatCurrency(0);
                updateChartTitle(monthlyChart, filterParams.get('productName'));
                return;
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

            // Cập nhật tiêu đề biểu đồ dựa trên bộ lọc sản phẩm hiện tại
            updateChartTitle(monthlyChart, filterParams.get('productName'));


            // Cập nhật biểu đồ với dữ liệu mới
            monthlyChart.data.labels = monthNames;
            monthlyChart.data.datasets = [{
                label: 'Doanh Thu (VNĐ)',
                backgroundColor: 'rgba(40, 167, 69, 0.7)',
                borderColor: 'rgba(40, 167, 69, 1)',
                borderWidth: 1,
                data: revenueData
            }, {
                label: 'Số Hóa Đơn',
                backgroundColor: 'rgba(255, 193, 7, 0.7)',
                borderColor: 'rgba(255, 193, 7, 1)',
                borderWidth: 1,
                data: invoiceCountData,
                yAxisID: 'y1'
            }];
            monthlyChart.update();

            // Tính tổng doanh thu
            const totalRevenue = revenueData.reduce((sum, current) => sum + current, 0);
            document.getElementById('monthlyTotalRevenue').textContent = formatCurrency(totalRevenue);
        })
        .catch(error => {
            console.error("Lỗi khi tải dữ liệu biểu đồ theo tháng:", error);
            updateChartWithNoData(monthlyChart, 'Đã xảy ra lỗi khi tải dữ liệu.');
            document.getElementById('monthlyTotalRevenue').textContent = formatCurrency(0);
            updateChartTitle(monthlyChart, filterParams.get('productName'));
        });
}

// Tải dữ liệu biểu đồ 3 năm gần đây
function loadMultiYearChartData() { // Changed from loadYearlyChartData
    const filterParams = getFilterParamsFromForm();

    const apiUrl = `/api/revenue/yearly?${filterParams.toString()}`; // Changed URL

    fetch(apiUrl)
        .then(response => response.json())
        .then(data => {
            if (!data || Object.keys(data).length === 0 || !data.years || data.years.length === 0) {
                // Không có dữ liệu
                updateChartWithNoData(multiYearChart, 'Không có dữ liệu cho khoảng thời gian và bộ lọc này.');
                document.getElementById('multiYearTotalRevenue').textContent = formatCurrency(0); // Changed ID
                updateChartTitle(multiYearChart, filterParams.get('productName'));
                return;
            }

            // Cập nhật tiêu đề biểu đồ dựa trên bộ lọc sản phẩm hiện tại
            updateChartTitle(multiYearChart, filterParams.get('productName'));


            // Cập nhật biểu đồ với dữ liệu mới
            multiYearChart.data.labels = data.years;
            multiYearChart.data.datasets = [{
                label: 'Doanh Thu (VNĐ)',
                backgroundColor: 'rgba(23, 162, 184, 0.7)',
                borderColor: 'rgba(23, 162, 184, 1)',
                borderWidth: 1,
                data: data.revenueSums
            }, {
                label: 'Số Hóa Đơn',
                backgroundColor: 'rgba(220, 53, 69, 0.7)',
                borderColor: 'rgba(220, 53, 69, 1)',
                borderWidth: 1,
                data: data.invoiceCounts,
                yAxisID: 'y1'
            }];
            multiYearChart.update();

            // Tính tổng doanh thu
            const totalRevenue = data.revenueSums.reduce((sum, current) => sum + current, 0);
            document.getElementById('multiYearTotalRevenue').textContent = formatCurrency(totalRevenue); // Changed ID
        })
        .catch(error => {
            console.error("Lỗi khi tải dữ liệu biểu đồ 3 năm gần đây:", error);
            updateChartWithNoData(multiYearChart, 'Đã xảy ra lỗi khi tải dữ liệu.');
            document.getElementById('multiYearTotalRevenue').textContent = formatCurrency(0); // Changed ID
            updateChartTitle(multiYearChart, filterParams.get('productName'));
        });
}

// Tính số ngày trong tháng
function getDaysInMonth(month, year) {
    // Tháng trong JavaScript được đánh số từ 0-11, Date(year, month, 0) trả về ngày cuối cùng của tháng trước đó.
    // Date(year, month, 0) với month là tháng 1-12 sẽ cho ngày cuối của tháng đó.
    return new Date(year, month, 0).getDate();
}


// Cập nhật biểu đồ khi không có dữ liệu hoặc lỗi
function updateChartWithNoData(chart, message = 'Không có dữ liệu') {
    chart.data.labels = [message]; // Hiển thị thông báo trên trục x
    chart.data.datasets = []; // Xóa bỏ các dataset
    chart.update();
}

// Cập nhật tiêu đề biểu đồ dựa trên bộ lọc sản phẩm
function updateChartTitle(chart, productNameFilter) {
    if (productNameFilter && productNameFilter.trim() !== '') {
        chart.options.plugins.title.display = true;
        chart.options.plugins.title.text = `Doanh thu theo bộ lọc sản phẩm: "${productNameFilter.trim()}"`;
    } else {
        chart.options.plugins.title.display = false;
    }
    chart.update(); // Cập nhật biểu đồ để hiển thị hoặc ẩn title
}


// Thiết lập các nút điều hướng
function setupNavigationButtons() {
    // Nút điều hướng theo ngày (biểu đồ ngày)
    document.getElementById('prevWeek').addEventListener('click', function() { // Tên ID không phù hợp (Week/Day), nên đổi thành prevDayChartMonth
        currentMonth--;
        if (currentMonth < 1) {
            currentMonth = 12;
            currentYear--;
        }
        updateDayLabel();
        loadDayChartData(currentMonth, currentYear);
    });

    document.getElementById('nextWeek').addEventListener('click', function() { // Tên ID không phù hợp (Week/Day), nên đổi thành nextDayChartMonth
        currentMonth++;
        if (currentMonth > 12) {
            currentMonth = 1;
            currentYear++;
        }
        updateDayLabel();
        loadDayChartData(currentMonth, currentYear);
    });

    document.getElementById('currentWeek').addEventListener('click', function() { // Tên ID không phù hợp (Week/Day), nên đổi thành currentDayChartMonth
        currentMonth = new Date().getMonth() + 1;
        currentYear = new Date().getFullYear();
        updateDayLabel();
        loadDayChartData(currentMonth, currentYear);
    });

    // Nút điều hướng theo tháng (biểu đồ tháng)
    document.getElementById('prevMonth').addEventListener('click', function() { // Tên ID không phù hợp (Month/Year), nên đổi thành prevMonthChartYear
        currentYear--;
        updateMonthLabel();
        loadMonthlyChartData(currentYear);
    });

    document.getElementById('nextMonth').addEventListener('click', function() { // Tên ID không phù hợp (Month/Year), nên đổi thành nextMonthChartYear
        currentYear++;
        updateMonthLabel();
        loadMonthlyChartData(currentYear);
    });

    document.getElementById('currentMonth').addEventListener('click', function() { // Tên ID không phù hợp (Month/Year), nên đổi thành currentMonthChartYear
        currentYear = new Date().getFullYear();
        updateMonthLabel();
        loadMonthlyChartData(currentYear);
    });

    // Các nút reset bộ lọc sản phẩm (nếu có) đã được xử lý ngầm khi form submit
    // Nếu bạn muốn nút reset filter chỉ ảnh hưởng đến biểu đồ mà không load lại trang,
    // bạn cần thêm logic vào đây để xóa giá trị trong các input filter,
    // sau đó gọi lại các hàm loadChartData.
    // Hiện tại, nút "Xóa báo cáo" trong form làm việc này bằng cách load lại trang /sales-report
    // mà không có params, dẫn đến filter rỗng.
    const resetButtons = document.querySelectorAll('.reset-product-filter'); // ID/Class này không thấy trong HTML bạn cung cấp
    if (resetButtons && resetButtons.length > 0) {
        resetButtons.forEach(button => {
            button.addEventListener('click', function() {
                // Giả định có các input/select với ID 'paymentMethod', 'employeeName', 'productName'
                document.getElementById('paymentMethod').value = ''; // Reset dropdown
                document.getElementById('employeeName').value = ''; // Clear input
                document.getElementById('productName').value = ''; // Clear input

                // Sau khi reset form, load lại dữ liệu biểu đồ
                // Các hàm loadChartData sẽ tự đọc giá trị rỗng vừa reset
                loadDayChartData(currentMonth, currentYear);
                loadMonthlyChartData(currentYear);
                loadMultiYearChartData();
            });
        });
    }
}

