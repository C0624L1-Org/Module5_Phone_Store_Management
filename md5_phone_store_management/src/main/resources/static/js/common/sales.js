(function() {
    // Khai báo các biến DOM
    const customerSearchInput = document.getElementById('customerSearchInput');
    const productSearchInput = document.getElementById('productSearchInput');
    const customerSearchBtn = document.getElementById('searchCustomerBtn');
    const productSearchBtn = document.getElementById('searchProductBtn');
    const customerList = document.getElementById('customerList');
    const productList = document.getElementById('productList');
    const emptyCart = document.getElementById('emptyCart');
    const cartItems = document.getElementById('cartItems');
    const totalItems = document.getElementById('totalItems');
    const totalAmount = document.getElementById('totalAmount');
    const customerId = document.getElementById('customerId');
    const selectedCustomerInfo = document.getElementById('selectedCustomerInfo');
    const saveCustomerBtn = document.getElementById('saveNewCustomerBtn');
    const checkoutBtn = document.getElementById('checkoutBtn');

    // Đồng bộ hóa dữ liệu purchaseCount khi tải trang
    (function synchronizePurchaseCounts() {
        console.log("Đang đồng bộ hóa dữ liệu purchaseCount...");
        
        fetch('/api/sales/sync-purchase-counts')
            .then(response => response.json())
            .then(data => {
                console.log("Kết quả đồng bộ hóa:", data);
                if (data.success) {
                    console.log("Đã đồng bộ hóa thành công: " + data.message);
                    
                    // Tự động tải lại danh sách khách hàng sau khi đồng bộ hóa thành công
                    console.log("Đang tải lại danh sách khách hàng sau khi đồng bộ hóa...");
                    setTimeout(function() {
                        // Tải lại danh sách khách hàng đã mua
                        searchCustomers();
                        // Tải lại danh sách tất cả khách hàng
                        searchAllCustomers();
                    }, 500);
                } else {
                    console.error("Lỗi khi đồng bộ hóa: " + data.message);
                }
            })
            .catch(error => {
                console.error("Lỗi kết nối khi đồng bộ hóa: ", error);
            });
    })();

    // Khai báo biến khác
    let cart = [];
    let currentCustomerPage = 0;
    let allCurrentPage = 0;
    let pageSize = 5;
    let customerSearchTimeout;
    let productSearchTimeout;
    let allCustomerSearchTimeout;
    
    // Cache dữ liệu khách hàng để tránh mất dữ liệu
    let allCustomersCache = [];
    let purchasedCustomersCache = []; // Thêm cache cho khách hàng đã mua hàng

    // Định dạng tiền tệ
    function formatCurrency(amount) {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND',
            minimumFractionDigits: 0
        }).format(amount);
    }

    // Thêm hàm fetchWithFallback ngay sau hàm formatCurrency
    function fetchWithFallback(url, options = {}) {
        return fetch(url, options)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`Lỗi HTTP: ${response.status}`);
                }
                return response.json();
            });
    }

    // Hiển thị toast thông báo
    function showToast(type, message) {
        // Kiểm tra nếu có hàm showToastMessage (từ toast.js)
        if (typeof window.showToastMessage === 'function') {
            window.showToastMessage(type, message);
        }
    }

    // Chức năng tìm kiếm khách hàng đã từng mua hàng
    function searchCustomers() {
        let searchInput = document.getElementById('customerSearchInput').value.trim();
        console.log("DEBUG: Tìm kiếm khách hàng đã mua hàng với từ khóa: " + searchInput);

        // Hiển thị đang tải
        const customerListElement = document.getElementById('customerList');
        if (customerListElement) {
            customerListElement.innerHTML = '<tr><td colspan="6" class="text-center"><div class="spinner-border text-primary" role="status"></div></td></tr>';
        }

        // Khi tìm kiếm với từ khóa mới, luôn reset về trang đầu tiên
        if (searchInput) {
            currentCustomerPage = 0;
        }

        // Nếu có dữ liệu cache và chỉ là điều hướng phân trang (không tìm kiếm), sử dụng cache
        if (purchasedCustomersCache.length > 0 && currentCustomerPage > 0 && !searchInput) {
            console.log("DEBUG: Sử dụng dữ liệu cache cho phân trang khách hàng đã mua");
            displayCachedPurchasedCustomers(searchInput);
            return;
        }

        // Tạo URL với tham số "keyword", trang hiện tại và timestamp để tránh cache
        const timestamp = new Date().getTime();
        const url = `/api/sales/search-customers?keyword=${encodeURIComponent(searchInput)}&page=0&size=100&_=${timestamp}`;

        console.log(`DEBUG: Gọi API tìm kiếm khách hàng đã mua: ${url}`);

        // Gọi API với method GET và thêm headers để tránh cache
        fetch(url, {
            method: 'GET',
            headers: {
                'Cache-Control': 'no-cache, no-store, must-revalidate',
                'Pragma': 'no-cache',
                'Expires': '0'
            },
            credentials: 'same-origin'
        })
        .then(response => {
            console.log(`API tìm kiếm khách hàng đã mua trả về status: ${response.status}`);
            if (!response.ok) {
                throw new Error(`Lỗi HTTP: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log(`DEBUG: API trả về dữ liệu khách hàng đã mua:`, data);
            
            // Thêm debug để kiểm tra chi tiết về dữ liệu trả về
            console.log(`DEBUG: Cấu trúc data:`, {
                hasContent: !!data.content,
                contentLength: data.content ? data.content.length : 0,
                contentType: data.content ? typeof data.content : 'không có',
                isArray: data.content ? Array.isArray(data.content) : false,
                totalElements: data.totalElements,
                totalPages: data.totalPages,
                numberOfElements: data.numberOfElements
            });
            
            if (data.content) {
                // Lưu vào bộ nhớ cache
                purchasedCustomersCache = [...data.content];
                console.log(`DEBUG: Đã lưu ${purchasedCustomersCache.length} khách hàng đã mua vào cache`);
                
                // Hiển thị dữ liệu từ cache
                displayCachedPurchasedCustomers(searchInput);
            } else {
                console.error(`DEBUG: API trả về dữ liệu không hợp lệ:`, data);
                updateCustomerTable([]);
                
                // Ẩn phân trang nếu không có dữ liệu
                let paginationElement = document.getElementById('customerPagination');
                if (paginationElement) {
                    paginationElement.innerHTML = '';
                    paginationElement.style.display = 'none';
                }
            }
        })
        .catch(error => {
            console.error(`Lỗi khi tìm kiếm khách hàng đã mua:`, error);
            if (customerListElement) {
                customerListElement.innerHTML = `<tr><td colspan="6" class="text-center text-danger">Lỗi khi tải dữ liệu khách hàng: ${error.message}</td></tr>`;
            }
            showToast('error', `Lỗi khi tìm kiếm khách hàng: ${error.message}`);
        });
    }

    // Hiển thị dữ liệu khách hàng đã mua từ cache
    function displayCachedPurchasedCustomers(searchInput) {
        // Lọc dữ liệu theo từ khóa nếu cần
        let filteredCustomers = purchasedCustomersCache;
        
        if (searchInput) {
            const searchLower = searchInput.toLowerCase();
            filteredCustomers = purchasedCustomersCache.filter(customer => {
                const nameMatch = (customer.fullName || '').toLowerCase().includes(searchLower);
                const phoneMatch = (customer.phone || '').includes(searchInput);
                const emailMatch = (customer.email || '').toLowerCase().includes(searchLower);
                return nameMatch || phoneMatch || emailMatch;
            });
            console.log(`DEBUG: Sau khi lọc với từ khóa "${searchInput}": ${filteredCustomers.length} khách hàng đã mua`);
        }
        
        // Đảm bảo currentCustomerPage không vượt quá số trang tối đa
        const totalPages = Math.ceil(filteredCustomers.length / pageSize);
        if (totalPages > 0 && currentCustomerPage >= totalPages) {
            currentCustomerPage = totalPages - 1;
            console.log(`DEBUG: Điều chỉnh lại trang hiện tại của khách hàng đã mua = ${currentCustomerPage} vì chỉ có ${totalPages} trang`);
        }
        
        // Tính toán phân trang
        const start = currentCustomerPage * pageSize;
        const end = Math.min(start + pageSize, filteredCustomers.length);
        const customersOnPage = filteredCustomers.slice(start, end);
        
        console.log(`DEBUG: Hiển thị ${customersOnPage.length} khách hàng đã mua từ cache (trang ${currentCustomerPage + 1}, từ ${start} đến ${end-1})`);
        
        // Nếu trang hiện tại không có dữ liệu, quay về trang đầu tiên
        if (customersOnPage.length === 0 && filteredCustomers.length > 0) {
            currentCustomerPage = 0;
            const newStart = 0;
            const newEnd = Math.min(pageSize, filteredCustomers.length);
            const newCustomersOnPage = filteredCustomers.slice(newStart, newEnd);
            console.log(`DEBUG: Quay về trang đầu tiên vì trang hiện tại không có dữ liệu. Hiển thị ${newCustomersOnPage.length} khách hàng đã mua.`);
            
            // Cập nhật bảng với dữ liệu trang đầu tiên
            updateCustomerTable(newCustomersOnPage);
            
            // Cập nhật phân trang với trang đầu tiên
            const paginationData = {
                content: newCustomersOnPage,
                totalElements: filteredCustomers.length,
                totalPages: totalPages,
                size: pageSize,
                number: currentCustomerPage
            };
            
            updatePagination('customerPagination', paginationData, changeCustomerPage);
            return;
        }
        
        // Cập nhật bảng với dữ liệu trang hiện tại
        updateCustomerTable(customersOnPage);
        
        // Cập nhật phân trang
        const paginationData = {
            content: customersOnPage,
            totalElements: filteredCustomers.length,
            totalPages: totalPages,
            size: pageSize,
            number: currentCustomerPage
        };
        
        updatePagination('customerPagination', paginationData, changeCustomerPage);
    }

    // Chức năng tìm kiếm tất cả khách hàng
    function searchAllCustomers() {
        let searchInput = document.getElementById('allCustomerSearchInput').value.trim();
        console.log("DEBUG: Tìm kiếm tất cả khách hàng với từ khóa: " + searchInput);

        // Hiển thị đang tải
        const customerListElement = document.getElementById('customerListWithPurchaseCount');
        if (customerListElement) {
            customerListElement.innerHTML = '<tr><td colspan="6" class="text-center"><div class="spinner-border text-primary" role="status"></div></td></tr>';
        }

        // Khi tìm kiếm với từ khóa mới, luôn reset về trang đầu tiên
        if (searchInput) {
            allCurrentPage = 0;
        }

        // Tạo URL với tham số "keyword" và timestamp để tránh cache
        const timestamp = new Date().getTime();

        // Nếu có dữ liệu cache và chỉ là điều hướng phân trang (không tìm kiếm), sử dụng cache
        if (allCustomersCache.length > 0 && allCurrentPage > 0 && !searchInput) {
            console.log("DEBUG: Sử dụng dữ liệu cache cho phân trang");
            displayCachedCustomers(searchInput);
            return;
        }

        // Luôn làm mới dữ liệu khi tìm kiếm từ khóa mới
        const allCustomersUrl = `/api/sales/search-all-customers?keyword=${encodeURIComponent(searchInput)}&page=0&size=100&_=${timestamp}`;
        const purchasedCustomersUrl = `/api/sales/search-customers?keyword=${encodeURIComponent(searchInput)}&page=0&size=50&_=${timestamp}`;

        console.log(`DEBUG: Gọi API tìm kiếm tất cả khách hàng: ${allCustomersUrl}`);
        console.log(`DEBUG: Gọi API tìm kiếm khách hàng đã mua: ${purchasedCustomersUrl}`);

        // Sử dụng Promise.all để tải cả hai danh sách cùng lúc
        Promise.all([
            // Tải tất cả khách hàng
            fetch(allCustomersUrl, {
            method: 'GET',
            headers: {
                'Cache-Control': 'no-cache, no-store, must-revalidate',
                'Pragma': 'no-cache',
                'Expires': '0'
            },
            credentials: 'same-origin'
            }).then(response => {
                if (!response.ok) throw new Error(`Lỗi HTTP: ${response.status}`);
                return response.json();
            }),
            
            // Tải khách hàng đã mua
            fetch(purchasedCustomersUrl, {
                method: 'GET',
                headers: {
                    'Cache-Control': 'no-cache, no-store, must-revalidate',
                    'Pragma': 'no-cache',
                    'Expires': '0'
                },
                credentials: 'same-origin'
            }).then(response => {
                if (!response.ok) throw new Error(`Lỗi HTTP: ${response.status}`);
                return response.json();
            })
        ])
        .then(([allCustomersData, purchasedCustomersData]) => {
            console.log("DEBUG: Đã nhận dữ liệu từ cả hai API:");
            console.log("- Tất cả khách hàng:", allCustomersData);
            console.log("- Khách hàng đã mua:", purchasedCustomersData);
            
            // Kiểm tra nếu API trả về mảng rỗng
            if (!allCustomersData || !allCustomersData.content || allCustomersData.content.length === 0) {
                console.log("CẢNH BÁO: API tất cả khách hàng trả về mảng rỗng");
                allCustomersCache = [];
                displayCachedCustomers(searchInput);
                return;
            }
            
            // Log chi tiết số lượng khách hàng từ mỗi API
            console.log(`DEBUG: API tất cả khách hàng trả về ${allCustomersData.content.length} khách hàng`);
            console.log(`DEBUG: API khách hàng đã mua trả về ${purchasedCustomersData.content ? purchasedCustomersData.content.length : 0} khách hàng`);

            // Tạo map cho khách hàng đã mua để dễ tìm kiếm
            const purchasedMap = new Map();
            if (purchasedCustomersData.content && purchasedCustomersData.content.length) {
                purchasedCustomersData.content.forEach(customer => {
                    const id = customer.customerID || customer.id;
                    if (id) {
                        purchasedMap.set(id.toString(), customer);
                    }
                });
            }
            
            // Dùng trực tiếp dữ liệu từ API tất cả khách hàng
            let customers = [...allCustomersData.content];
            
            // Log danh sách khách hàng trước khi kết hợp
            console.log(`DEBUG: Danh sách tất cả khách hàng trước khi kết hợp: ${customers.length} khách hàng`);
            customers.forEach((customer, index) => {
                if (index < 5) { // Chỉ hiển thị 5 khách hàng đầu tiên để tránh log quá dài
                    console.log(`  - Khách hàng #${index + 1}: ID=${customer.customerID || customer.id}, Tên=${customer.fullName}, PurchaseCount=${customer.purchaseCount || 0}`);
                }
            });
            
            // Thêm các khách hàng đã mua không có trong danh sách ban đầu
            if (purchasedCustomersData.content) {
                for (const customer of purchasedCustomersData.content) {
                    const id = customer.customerID || customer.id;
                    if (id) {
                        // Kiểm tra xem đã có trong danh sách chưa
                        const existing = customers.find(c => (c.customerID || c.id) == id);
                        
                        if (!existing) {
                            console.log(`DEBUG: Thêm khách hàng đã mua ID=${id} vào danh sách tổng hợp`);
                            customers.push(customer);
                } else {
                            // Cập nhật purchaseCount từ dữ liệu khách hàng đã mua
                            existing.purchaseCount = customer.purchaseCount;
                        }
                    }
                }
            }
            
            // Log danh sách khách hàng sau khi kết hợp
            console.log(`DEBUG: Danh sách sau khi kết hợp: ${customers.length} khách hàng`);
            
            // Lọc theo từ khóa nếu cần
            if (searchInput) {
                const searchLower = searchInput.toLowerCase();
                customers = customers.filter(customer => {
                    const nameMatch = (customer.fullName || '').toLowerCase().includes(searchLower);
                    const phoneMatch = (customer.phone || '').includes(searchInput);
                    const emailMatch = (customer.email || '').toLowerCase().includes(searchLower);
                    return nameMatch || phoneMatch || emailMatch;
                });
                console.log(`DEBUG: Sau khi lọc với từ khóa "${searchInput}": ${customers.length} khách hàng`);
            }
            
            // Sắp xếp khách hàng theo ID
            customers.sort((a, b) => {
                const idA = a.customerID || a.id || 0;
                const idB = b.customerID || b.id || 0;
                return idA - idB;
            });
            
            // Lưu vào cache
            allCustomersCache = customers;
            
            // Hiển thị dữ liệu
            displayCachedCustomers(searchInput);
            })
            .catch(error => {
                console.error(`Lỗi khi tìm kiếm tất cả khách hàng:`, error);
                if (customerListElement) {
                    customerListElement.innerHTML = `<tr><td colspan="6" class="text-center text-danger">Lỗi khi tải dữ liệu khách hàng: ${error.message}</td></tr>`;
                }
                showToast('error', `Lỗi khi tìm kiếm khách hàng: ${error.message}`);
            });
    }
    
    // Hiển thị dữ liệu khách hàng từ cache
    function displayCachedCustomers(searchInput) {
        // Lọc dữ liệu theo từ khóa nếu cần
        let filteredCustomers = allCustomersCache;
        
        if (searchInput) {
            const searchLower = searchInput.toLowerCase();
            filteredCustomers = allCustomersCache.filter(customer => {
                const nameMatch = (customer.fullName || '').toLowerCase().includes(searchLower);
                const phoneMatch = (customer.phone || '').includes(searchInput);
                const emailMatch = (customer.email || '').toLowerCase().includes(searchLower);
                return nameMatch || phoneMatch || emailMatch;
            });
            console.log(`DEBUG: Sau khi lọc với từ khóa "${searchInput}": ${filteredCustomers.length} khách hàng`);
        }
        
        // Đảm bảo allCurrentPage không vượt quá số trang tối đa
        const totalPages = Math.ceil(filteredCustomers.length / pageSize);
        if (totalPages > 0 && allCurrentPage >= totalPages) {
            allCurrentPage = totalPages - 1;
            console.log(`DEBUG: Điều chỉnh lại trang hiện tại = ${allCurrentPage} vì chỉ có ${totalPages} trang`);
        }
        
        // Tính toán phân trang
        const start = allCurrentPage * pageSize;
        const end = Math.min(start + pageSize, filteredCustomers.length);
        const customersOnPage = filteredCustomers.slice(start, end);
        
        console.log(`DEBUG: Hiển thị ${customersOnPage.length} khách hàng từ cache (trang ${allCurrentPage + 1}, từ ${start} đến ${end-1})`);
        
        // Nếu trang hiện tại không có dữ liệu, quay về trang đầu tiên
        if (customersOnPage.length === 0 && filteredCustomers.length > 0) {
            allCurrentPage = 0;
            const newStart = 0;
            const newEnd = Math.min(pageSize, filteredCustomers.length);
            const newCustomersOnPage = filteredCustomers.slice(newStart, newEnd);
            console.log(`DEBUG: Quay về trang đầu tiên vì trang hiện tại không có dữ liệu. Hiển thị ${newCustomersOnPage.length} khách hàng.`);
            
            // Cập nhật bảng với dữ liệu trang đầu tiên
            updateAllCustomerTable(newCustomersOnPage);
            
            // Cập nhật phân trang với trang đầu tiên
            const paginationData = {
                content: newCustomersOnPage,
                totalElements: filteredCustomers.length,
                totalPages: totalPages,
                size: pageSize,
                number: allCurrentPage
            };
            
            updatePagination('allCustomerPagination', paginationData, changeAllCustomerPage);
            return;
        }
        
        // Cập nhật bảng với dữ liệu trang hiện tại
        updateAllCustomerTable(customersOnPage);
        
        // Cập nhật phân trang
        const paginationData = {
            content: customersOnPage,
            totalElements: filteredCustomers.length,
            totalPages: totalPages,
            size: pageSize,
            number: allCurrentPage
        };
        
        updatePagination('allCustomerPagination', paginationData, changeAllCustomerPage);
    }

    // Tìm kiếm sản phẩm
    function searchProducts(page = 0) {
        // Đảm bảo trang là số không âm
        page = Math.max(0, page);

        // Lấy phần tử danh sách sản phẩm
        const productListElement = document.getElementById('productList');

        // Hiển thị biểu tượng đang tải
        if (productListElement) {
            productListElement.innerHTML = '<tr><td colspan="4" class="text-center"><div class="spinner-border text-primary" role="status"></div></td></tr>';
        } else {
            console.error('Không tìm thấy phần tử ID: productList!');
            return;
        }

        // Lấy từ khóa tìm kiếm
        const searchInput = document.getElementById('productSearchInput');
        const keyword = searchInput ? searchInput.value.trim() : '';
        console.log('Từ khóa tìm kiếm sản phẩm:', keyword);

        const params = new URLSearchParams();
        params.append('keyword', keyword);
        params.append('page', page);
        params.append('size', pageSize);
        params.append('_', new Date().getTime()); // Thêm timestamp để tránh cache

        // Sử dụng endpoint chính xác đã cung cấp
        const url = `/api/sales/search-products?${params.toString()}`;

        console.log(`DEBUG: Gọi API tìm kiếm sản phẩm: ${url}`);

        // Gọi API với method GET
        fetch(url)
            .then(response => {
                console.log(`API tìm kiếm sản phẩm trả về status: ${response.status}`);
                if (!response.ok) {
                    throw new Error(`Lỗi HTTP: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                // Debug dữ liệu trả về từ API
                console.log('Dữ liệu API sản phẩm:', data);
                
                // Cập nhật bảng sản phẩm
                updateProductTable(data, page);
                
                // Cập nhật phân trang cho sản phẩm
                updatePagination('productPagination', data, changeProductPage);
            })
            .catch(error => {
                console.error(`Lỗi khi tìm kiếm sản phẩm:`, error);
                productListElement.innerHTML = `<tr><td colspan="4" class="text-center text-danger">Lỗi khi tải dữ liệu sản phẩm: ${error.message}</td></tr>`;
                showToast('error', `Lỗi khi tìm kiếm sản phẩm: ${error.message}`);
                
                // Ẩn phân trang khi có lỗi
                const paginationElement = document.getElementById('productPagination');
                if (paginationElement) {
                    paginationElement.innerHTML = '';
                    paginationElement.style.display = 'none';
                }
            });
    }

    // Cập nhật bảng khách hàng đã từng mua
    function updateCustomerTable(customers) {
        const customerListElement = document.getElementById('customerList');
        if (!customerListElement) {
            console.error('Không tìm thấy phần tử ID: customerList');
            return;
        }

        console.log(`DEBUG: Cập nhật bảng khách hàng với ${customers.length} bản ghi`);

        if (!customers || customers.length === 0) {
            customerListElement.innerHTML = '<tr><td colspan="6" class="text-center">Không tìm thấy khách hàng nào</td></tr>';
            return;
        }

        let html = '';
        customers.forEach(customer => {
            // Log để debug
            console.log(`DEBUG: Xử lý khách hàng: ID=${customer.customerID || customer.id}, Tên=${customer.fullName}, SĐT=${customer.phone}`);

            html += `<tr>
                <td>${customer.fullName || ''}</td>
                <td>${customer.phone || ''}</td>
                <td>${customer.email || ''}</td>
                <td>${customer.address || ''}</td>
                <td>${customer.purchaseCount || 0}</td>
                <td>
                    <button type="button" class="btn btn-primary btn-sm select-customer" 
                        data-customer-id="${customer.customerID || customer.id || ''}"
                        data-customer-name="${customer.fullName || ''}" 
                        data-customer-phone="${customer.phone || ''}"
                        data-customer-email="${customer.email || ''}"
                        data-customer-address="${customer.address || ''}"
                        data-customer-purchase-count="${customer.purchaseCount || 0}">
                        Chọn
                    </button>
                </td>
            </tr>`;
        });

        customerListElement.innerHTML = html;

        // Thêm sự kiện cho nút "Chọn"
        customerListElement.querySelectorAll('.select-customer').forEach(button => {
            button.addEventListener('click', function() {
                const customerId = this.getAttribute('data-customer-id');
                const customerName = this.getAttribute('data-customer-name');
                const customerPhone = this.getAttribute('data-customer-phone');
                const customerEmail = this.getAttribute('data-customer-email');
                const customerAddress = this.getAttribute('data-customer-address');

                console.log(`DEBUG: Đã chọn khách hàng - ID=${customerId}, Tên=${customerName}, SĐT=${customerPhone}`);

                // Sử dụng hàm selectCustomer để chọn khách hàng
                selectCustomer(customerId, customerName, customerPhone, customerEmail, customerAddress);
            });
        });
    }

    // Cập nhật bảng tất cả khách hàng
    function updateAllCustomerTable(customers) {
        const customerListElement = document.getElementById('customerListWithPurchaseCount');
        if (!customerListElement) {
            console.error('Không tìm thấy phần tử ID: customerListWithPurchaseCount');
            return;
        }

        console.log(`DEBUG: Cập nhật bảng tất cả khách hàng với ${customers.length} bản ghi`);

        if (!customers || customers.length === 0) {
            customerListElement.innerHTML = '<tr><td colspan="6" class="text-center">Không tìm thấy khách hàng nào</td></tr>';
            return;
        }

        let html = '';
        customers.forEach(customer => {
            // Log để debug
            console.log(`DEBUG: Xử lý tất cả khách hàng: ID=${customer.customerID || customer.id}, Tên=${customer.fullName}, SĐT=${customer.phone}, PurchaseCount=${customer.purchaseCount || 0}`);

            html += `<tr>
                <td>${customer.fullName || ''}</td>
                <td>${customer.phone || ''}</td>
                <td>${customer.email || ''}</td>
                <td>${customer.address || ''}</td>
                <td>${customer.purchaseCount || 0}</td>
                <td>
                    <button type="button" class="btn btn-primary btn-sm select-all-customer" 
                        data-customer-id="${customer.customerID || customer.id || ''}"
                        data-customer-name="${customer.fullName || ''}" 
                        data-customer-phone="${customer.phone || ''}"
                        data-customer-email="${customer.email || ''}"
                        data-customer-address="${customer.address || ''}"
                        data-customer-purchase-count="${customer.purchaseCount || 0}">
                        Chọn
                    </button>
                </td>
            </tr>`;
        });

        customerListElement.innerHTML = html;

        // Thêm sự kiện cho nút "Chọn"
        customerListElement.querySelectorAll('.select-all-customer').forEach(button => {
            button.addEventListener('click', function() {
                const customerId = this.getAttribute('data-customer-id');
                const customerName = this.getAttribute('data-customer-name');
                const customerPhone = this.getAttribute('data-customer-phone');
                const customerEmail = this.getAttribute('data-customer-email');
                const customerAddress = this.getAttribute('data-customer-address');

                console.log(`DEBUG: Đã chọn từ tất cả khách hàng - ID=${customerId}, Tên=${customerName}, SĐT=${customerPhone}`);

                // Sử dụng hàm selectCustomer để chọn khách hàng
                selectCustomer(customerId, customerName, customerPhone, customerEmail, customerAddress);
            });
        });
    }

    // Cập nhật bảng sản phẩm
    function updateProductTable(data, currentPage) {
        const productListElement = document.getElementById('productList');
        if (!productListElement) {
            console.error('Không tìm thấy phần tử ID: productList!');
            return;
        }

        if (data.content && data.content.length > 0) {
            let html = '';
            data.content.forEach(product => {
                try {
                    // Tránh log toàn bộ đối tượng sản phẩm gây vòng lặp vô hạn
                    console.log(`Xử lý sản phẩm ID: ${product.productID || product.id}, Tên: ${product.name || product.productName}`);

                    // Kiểm tra số lượng tồn kho
                    const isOutOfStock = product.stockQuantity <= 0;
                    const stockStatus = isOutOfStock ?
                        '<span class="badge bg-danger">Hết hàng</span>' :
                        `<span class="badge bg-success">${product.stockQuantity} trong kho</span>`;

                    // Xử lý ảnh sản phẩm - KHÔNG truy cập trực tiếp đối tượng image
                    let productImage = '/img/no-image.png'; // Đường dẫn ảnh mặc định

                    // Tạo bản sao cạn của đối tượng images để tránh vòng lặp
                    if (product.images && Array.isArray(product.images)) {
                        // Chỉ lấy imageUrl từ phần tử đầu tiên trong mảng
                        if (product.images.length > 0 && product.images[0] && product.images[0].imageUrl) {
                            productImage = product.images[0].imageUrl;
                        }
                    }

                    // Thêm đoạn code này để ngừng vòng lặp vô hạn khi ảnh không tồn tại
                    const handleImageError = "this.onerror=null; this.src='/img/no-image.png';";

                    // Xử lý giá sản phẩm
                    let productPrice = 0;
                    if (typeof product.sellingPrice === 'number') {
                        productPrice = product.sellingPrice;
                    } else if (typeof product.price === 'number') {
                        productPrice = product.price;
                    } else if (product.sellingPrice) {
                        // Trường hợp BigDecimal được chuyển thành string
                        try {
                            productPrice = parseFloat(String(product.sellingPrice).replace(/[^\d.-]/g, ''));
                        } catch (e) {
                            console.error('Lỗi khi chuyển đổi giá bán:', e);
                        }
                    }

                    html += `
                        <tr>
                            <td>
                                <div class="d-flex align-items-center">
                                    <div class="product-img me-3" style="width: 50px; height: 50px;">
                                        <img src="${productImage}" 
                                            class="img-fluid rounded" 
                                            alt="${product.name || 'Sản phẩm'}"
                                            onerror="${handleImageError}">
                                    </div>
                                    <div>
                                        <h6 class="mb-0">${product.name || ''}</h6>
                                        <small class="text-muted"></small>
                                    </div>
                                </div>
                            </td>
                            <td><strong>${formatCurrency(productPrice)}</strong></td>
                            <td>${stockStatus}</td>
                            <td>
                                <button class="btn btn-sm btn-primary select-product" 
                                        data-id="${product.productID}" 
                                        data-name="${product.name || ''}" 
                                        data-price="${productPrice}"
                                        data-stock="${product.stockQuantity || 0}"
                                        ${isOutOfStock ? 'disabled' : ''}>
                                    <i class="fas fa-plus"></i>
                                </button>
                            </td>
                        </tr>
                    `;
                } catch (error) {
                    console.error('Lỗi khi xử lý sản phẩm:', error);
                }
            });
            productListElement.innerHTML = html;

            // Thêm event listener cho các nút "Chọn"
            productListElement.querySelectorAll('.select-product').forEach(button => {
                button.addEventListener('click', function() {
                    const productId = this.getAttribute('data-id');
                    const productName = this.getAttribute('data-name');
                    const productPrice = parseFloat(this.getAttribute('data-price') || 0);
                    const stockQuantity = parseInt(this.getAttribute('data-stock') || 0);

                    // Thêm sản phẩm vào giỏ hàng
                    addToCart(productId, productName, productPrice, stockQuantity);

                    // Đóng modal
                    const modal = bootstrap.Modal.getInstance(document.getElementById('productModal'));
                    if (modal) modal.hide();
                });
            });
        } else {
            productListElement.innerHTML = '<tr><td colspan="4" class="text-center">Không tìm thấy sản phẩm</td></tr>';
        }
    }

    // Thêm hàm cập nhật thông tin hiển thị khách hàng
    function updateCustomerDisplayInfo(name, phone, email, address) {
        // Chúng ta sẽ sử dụng giá trị từ các input này để hiển thị
        console.log(`DEBUG: Cập nhật hiển thị thông tin khách hàng: ${name}, ${phone}, ${email}, ${address}`);
    }

    // Chọn khách hàng
    function selectCustomer(id, name, phone, email, address) {
        console.log(`DEBUG: Chọn khách hàng với ID: ${id}, Tên: ${name}`);

        const customerIdInput = document.getElementById('customerId');
        if (!customerIdInput) {
            console.error('Không tìm thấy input customerId!');
            return;
        }

        // Điền thông tin khách hàng vào form ẩn
        customerIdInput.value = id;

        const customerNameInput = document.getElementById('customerName');
        const customerPhoneInput = document.getElementById('customerPhone');
        const customerEmailInput = document.getElementById('customerEmail');
        const customerAddressInput = document.getElementById('customerAddress');

        if (customerNameInput) customerNameInput.value = name || '';
        if (customerPhoneInput) customerPhoneInput.value = phone || '';
        if (customerEmailInput) customerEmailInput.value = email || '';
        if (customerAddressInput) customerAddressInput.value = address || '';

        // Hiển thị phần thông tin khách hàng
        const selectedCustomerInfo = document.getElementById('selectedCustomerInfo');
        if (selectedCustomerInfo) {
            selectedCustomerInfo.classList.remove('d-none');
        }

        // Cập nhật giao diện hiển thị thông tin
        updateCustomerDisplayInfo(name, phone, email, address);

        // Đóng modal nếu cần
        const existingCustomerModal = document.getElementById('existingCustomerModal');
        if (existingCustomerModal) {
            const modal = bootstrap.Modal.getInstance(existingCustomerModal);
            if (modal) modal.hide();
        }

        const existingAllCustomerModal = document.getElementById('existingAllCustomerModal');
        if (existingAllCustomerModal) {
            const modal = bootstrap.Modal.getInstance(existingAllCustomerModal);
            if (modal) modal.hide();
        }

        // Kiểm tra nếu có thể checkout
        validateCheckout();

        console.log(`Đã chọn khách hàng: ${name} (ID: ${id})`);
        showToast('success', `Đã chọn khách hàng: ${name}`);
    }

    // Thêm sản phẩm vào giỏ hàng
    function addToCart(id, name, price, stock) {
        // Kiểm tra sản phẩm đã có trong giỏ hàng chưa
        const existingItemIndex = cart.findIndex(item => item.productId === id);

        if (existingItemIndex !== -1) {
            // Nếu đã có, tăng số lượng nếu còn trong kho
            const newQuantity = cart[existingItemIndex].quantity + 1;

            if (newQuantity <= stock) {
                cart[existingItemIndex].quantity = newQuantity;
                cart[existingItemIndex].total = newQuantity * price;

                // Cập nhật UI
                updateCartUI();
                showToast('success', 'Đã cập nhật số lượng sản phẩm trong giỏ hàng');
            } else {
                showToast('error', 'Số lượng sản phẩm trong kho không đủ');
            }
        } else {
            // Nếu chưa có, thêm mới
            cart.push({
                productId: id,
                name: name,
                price: price,
                quantity: 1,
                stock: stock,
                total: price
            });

            // Cập nhật UI
            updateCartUI();

            // Thông báo thành công
            showToast('success', 'Đã thêm sản phẩm vào giỏ hàng');
        }

        // Kiểm tra nếu có thể checkout
        validateCheckout();

        console.log(`Đã thêm sản phẩm: ${name} (ID: ${id}, Giá: ${price}) vào giỏ hàng`);
    }

    // Cập nhật UI giỏ hàng
    function updateCartUI() {
        if (!cartItems || !emptyCart || !totalItems || !totalAmount) {
            console.error('Không tìm thấy một hoặc nhiều phần tử giỏ hàng!');
            return;
        }

        if (cart.length === 0) {
            emptyCart.classList.remove('d-none');
            cartItems.innerHTML = '';
            totalItems.textContent = '0';
            totalAmount.textContent = '0 VND';
            return;
        }

        // Ẩn thông báo giỏ hàng trống
        emptyCart.classList.add('d-none');

        // Tạo HTML cho các sản phẩm trong giỏ hàng
        let html = '';
        let cartTotal = 0;
        let itemCount = 0;

        cart.forEach((item, index) => {
            const itemTotal = item.price * item.quantity;
            cartTotal += itemTotal;
            itemCount += item.quantity;

            html += `
                <tr>
                    <td>${index + 1}</td>
                    <td>${item.name}</td>
                    <td>${formatCurrency(item.price)}</td>
                    <td>
                            <input type="number" class="form-control text-center quantity-input" value="${item.quantity}" data-index="${index}" min="1" max="${item.stock}">
                    </td>
                    <td>${formatCurrency(itemTotal)}</td>
                    <td>
                        <button class="btn btn-sm btn-danger remove-from-cart" data-index="${index}">
                            <i class="fas fa-trash"></i>
                        </button>
                    </td>
                </tr>
            `;
        });

        // Cập nhật nội dung giỏ hàng
        cartItems.innerHTML = html;

        // Cập nhật tổng số lượng và tổng tiền
        totalItems.textContent = itemCount;
        totalAmount.textContent = formatCurrency(cartTotal);

        // Thêm event listeners cho các nút trong giỏ hàng
        document.querySelectorAll('.quantity-input').forEach(input => {
            input.addEventListener('change', function() {
                const index = parseInt(this.getAttribute('data-index'));
                updateCartItemQuantity(index, parseInt(this.value) || 1);
            });
        });

        document.querySelectorAll('.remove-from-cart').forEach(button => {
            button.addEventListener('click', function() {
                const index = parseInt(this.getAttribute('data-index'));
                removeFromCart(index);
            });
        });
    }

    // Cập nhật số lượng sản phẩm trong giỏ hàng
    function updateCartItemQuantity(index, newQuantity) {
        if (index < 0 || index >= cart.length) return;

        const item = cart[index];

        // Kiểm tra số lượng hợp lệ
        if (newQuantity < 1) {
            removeFromCart(index);
            return;
        }

        // Kiểm tra số lượng trong kho
        if (newQuantity > item.stock) {
            showToast('error', 'Số lượng sản phẩm trong kho không đủ');
            return;
        }

        // Cập nhật số lượng và tổng tiền
        item.quantity = newQuantity;
        item.total = newQuantity * item.price;

        // Cập nhật UI
        updateCartUI();

        // Kiểm tra nếu có thể checkout
        validateCheckout();
    }

    // Xóa sản phẩm khỏi giỏ hàng
    function removeFromCart(index) {
        if (index < 0 || index >= cart.length) return;

        // Xóa sản phẩm
        cart.splice(index, 1);

        // Cập nhật UI
        updateCartUI();

        // Thông báo
        showToast('success', 'Đã xóa sản phẩm khỏi giỏ hàng');

        // Kiểm tra nếu có thể checkout
        validateCheckout();
    }

    // Kiểm tra điều kiện để có thể checkout
    function validateCheckout() {
        console.log(`Trạng thái checkout: Có khách hàng=${customerId && customerId.value ? 'true' : 'false'}, Có sản phẩm=${cart.length > 0 ? 'true' : 'false'}`);

        if (!checkoutBtn) {
            console.error('Không tìm thấy nút thanh toán!');
            return;
        }

        // Kích hoạt hoặc vô hiệu hóa nút thanh toán
        checkoutBtn.disabled = !(customerId && customerId.value && cart.length > 0);
    }

    // Cập nhật điều khiển phân trang
    function updatePagination(elementId, paginationInfo, pageChangeFunction) {
        const paginationElement = document.getElementById(elementId);
        if (!paginationElement) {
            console.error(`Không tìm thấy phần tử phân trang: ${elementId}`);
            return;
        }

        // Lấy thông tin phân trang từ dữ liệu API
        const size = paginationInfo.size || pageSize;
        const currentPage = paginationInfo.number || 0;
        
        // FIX: Nếu totalElements hoặc totalPages không tồn tại hoặc không đáng tin cậy, đặt giá trị mặc định
        let totalElements = paginationInfo.totalElements;
        let totalPages = paginationInfo.totalPages;
        
        // Nếu đang ở trang khác trang đầu tiên, đảm bảo thông tin phân trang được duy trì
        if (currentPage > 0) {
            // Nếu đang ở trang > 0, đảm bảo ít nhất có totalPages = currentPage + 1
            if (!totalPages || totalPages <= currentPage) {
                totalPages = currentPage + 1;
                console.log(`DEBUG: Đang ở trang ${currentPage}, đặt tổng số trang = ${totalPages}`);
            }
            
            // Nếu không có totalElements, ước tính từ totalPages và size
            if (!totalElements) {
                totalElements = totalPages * size;
                console.log(`DEBUG: Ước tính tổng số phần tử = ${totalElements} từ ${totalPages} trang`);
            }
        }
        
        // Đảm bảo luôn có ít nhất 2 trang nếu đang ở trang thứ 2
        if (currentPage === 1 && (!totalPages || totalPages < 2)) {
            totalPages = 2;
            console.log(`DEBUG: Ở trang 2, đảm bảo ít nhất có 2 trang`);
        }
        
        console.log(`DEBUG: Cập nhật phân trang ${elementId} - Tổng phần tử: ${totalElements}, Số trang: ${totalPages}, Trang hiện tại: ${currentPage}`);

        // FIX: Thay đổi logic kiểm tra khi nào ẩn phân trang
        // Chỉ ẩn phân trang khi:
        // 1. Đang ở trang đầu tiên (currentPage = 0)
        // 2. VÀ (totalElements rõ ràng nhỏ hơn hoặc bằng size HOẶC totalPages = 1)
        if (currentPage === 0 && (
            (totalElements !== undefined && totalElements <= size) || 
            (totalPages !== undefined && totalPages <= 1)
        )) {
            console.log(`DEBUG: Ẩn phân trang ${elementId} vì đang ở trang đầu và chỉ có 1 trang`);
            paginationElement.innerHTML = '';
            paginationElement.style.display = 'none';
            return;
        }

        // Hiển thị phân trang nếu có nhiều trang
        paginationElement.style.display = 'flex';

        // Xóa tất cả event listeners cũ
        paginationElement.innerHTML = '';

        // Tạo container phân trang
        const paginationContainer = document.createElement('ul');
        paginationContainer.className = 'pagination justify-content-end mb-0';

        // Nút trang trước
        const prevBtn = document.createElement('li');
        prevBtn.className = `page-item ${currentPage <= 0 ? 'disabled' : ''}`;

        const prevLink = document.createElement('a');
        prevLink.className = 'page-link';
        prevLink.href = '#';
        prevLink.setAttribute('aria-label', 'Previous');
        prevLink.innerHTML = '<span aria-hidden="true">&laquo;</span>';

        if (currentPage > 0) {
            prevLink.addEventListener('click', function(e) {
                e.preventDefault();
                pageChangeFunction(currentPage - 1);
            });
        }

        prevBtn.appendChild(prevLink);
        paginationContainer.appendChild(prevBtn);

        // Số trang tối đa hiển thị
        const maxPages = 5;
        let startPage = Math.max(0, currentPage - Math.floor(maxPages / 2));
        let endPage = Math.min(totalPages - 1, startPage + maxPages - 1);

        // Điều chỉnh phạm vi trang nếu cần
        if (endPage - startPage + 1 < maxPages) {
            startPage = Math.max(0, endPage - maxPages + 1);
        }

        // Tạo các nút trang
        for (let i = startPage; i <= endPage; i++) {
            const pageItem = document.createElement('li');
            pageItem.className = `page-item ${i === currentPage ? 'active' : ''}`;

            const pageLink = document.createElement('a');
            pageLink.className = 'page-link';
            pageLink.href = '#';
            pageLink.textContent = i + 1;

            pageLink.addEventListener('click', function(e) {
                e.preventDefault();
                pageChangeFunction(i);
            });

            pageItem.appendChild(pageLink);
            paginationContainer.appendChild(pageItem);
        }

        // Nút trang sau
        const nextBtn = document.createElement('li');
        nextBtn.className = `page-item ${currentPage >= totalPages - 1 ? 'disabled' : ''}`;

        const nextLink = document.createElement('a');
        nextLink.className = 'page-link';
        nextLink.href = '#';
        nextLink.setAttribute('aria-label', 'Next');
        nextLink.innerHTML = '<span aria-hidden="true">&raquo;</span>';

        if (currentPage < totalPages - 1) {
            nextLink.addEventListener('click', function(e) {
                e.preventDefault();
                pageChangeFunction(currentPage + 1);
            });
        }

        nextBtn.appendChild(nextLink);
        paginationContainer.appendChild(nextBtn);

        // Thêm vào DOM
        paginationElement.appendChild(paginationContainer);
    }

    // Hàm thay đổi trang cho khách hàng đã mua
    function changeCustomerPage(page) {
        currentCustomerPage = page;
                
        // Kiểm tra nếu có dữ liệu cache
        if (purchasedCustomersCache.length > 0) {
            console.log("DEBUG: Đổi trang khách hàng đã mua sử dụng cache");
            displayCachedPurchasedCustomers(document.getElementById('customerSearchInput').value.trim());
        } else {
            // Nếu không có cache, gọi API
            searchCustomers();
        }
    }

    // Hàm thay đổi trang cho tất cả khách hàng
    function changeAllCustomerPage(page) {
        allCurrentPage = page;
        
        // Kiểm tra nếu có dữ liệu cache
        if (allCustomersCache.length > 0) {
            displayCachedCustomers(document.getElementById('allCustomerSearchInput').value.trim());
        } else {
            // Nếu không có cache, gọi API
            searchAllCustomers();
        }
    }

    // Hàm thay đổi trang cho sản phẩm
    function changeProductPage(page) {
        searchProducts(page);
    }
    
    // Thêm event listeners cho các modals và tìm kiếm
    document.addEventListener('DOMContentLoaded', function() {
        // Thêm event listeners cho các input tìm kiếm
        if (customerSearchInput) {
            customerSearchInput.addEventListener('keyup', function(e) {
                // Nếu nhấn Enter, tìm kiếm ngay
                if (e.key === 'Enter') {
                    searchCustomers();
                    return;
                }
                
                // Nếu không, đợi một chút rồi mới tìm kiếm (debounce)
                clearTimeout(customerSearchTimeout);
                customerSearchTimeout = setTimeout(function() {
                searchCustomers();
                }, 500); // Đợi 500ms sau khi người dùng ngừng gõ
            });
        }
        
        if (document.getElementById('allCustomerSearchInput')) {
            document.getElementById('allCustomerSearchInput').addEventListener('keyup', function(e) {
                // Nếu nhấn Enter, tìm kiếm ngay
                if (e.key === 'Enter') {
                    searchAllCustomers();
                    return;
                }
                
                // Nếu không, đợi một chút rồi mới tìm kiếm (debounce)
                clearTimeout(allCustomerSearchTimeout);
                allCustomerSearchTimeout = setTimeout(function() {
                searchAllCustomers();
                }, 500); // Đợi 500ms sau khi người dùng ngừng gõ
            });
        }

        if (productSearchInput) {
            productSearchInput.addEventListener('keyup', function(e) {
                // Nếu nhấn Enter, tìm kiếm ngay
                if (e.key === 'Enter') {
                searchProducts(0);
                    return;
                }

                // Nếu không, đợi một chút rồi mới tìm kiếm (debounce)
                clearTimeout(productSearchTimeout);
                productSearchTimeout = setTimeout(function() {
                    searchProducts(0);
                }, 500); // Đợi 500ms sau khi người dùng ngừng gõ
            });
        }
        
        // Thêm event listeners cho các nút tìm kiếm
        if (customerSearchBtn) {
            customerSearchBtn.addEventListener('click', function() {
                searchCustomers();
            });
        }
        
        if (document.getElementById('searchAllCustomerBtn')) {
            document.getElementById('searchAllCustomerBtn').addEventListener('click', function() {
                searchAllCustomers();
            });
        }
        
        if (productSearchBtn) {
            productSearchBtn.addEventListener('click', function() {
                searchProducts(0);
            });
        }
        
        // Xử lý sự kiện lưu khách hàng mới
        if (saveCustomerBtn) {
            saveCustomerBtn.addEventListener('click', function() {
                saveNewCustomer();
            });
        }
        
        // Lắng nghe sự kiện hiển thị modal khách hàng đã mua
        const existingCustomerModal = document.getElementById('existingCustomerModal');
        if (existingCustomerModal) {
            existingCustomerModal.addEventListener('shown.bs.modal', function() {
                console.log("Modal khách hàng đã mua được mở, tự động tìm kiếm...");
                // Tự động tìm kiếm khách hàng khi modal mở
                searchCustomers();
            });
        }
        
        // Lắng nghe sự kiện hiển thị modal tất cả khách hàng
        const existingAllCustomerModal = document.getElementById('existingAllCustomerModal');
        if (existingAllCustomerModal) {
            existingAllCustomerModal.addEventListener('shown.bs.modal', function() {
                console.log("Modal tất cả khách hàng được mở, tự động tìm kiếm...");
                // Tự động tìm kiếm tất cả khách hàng khi modal mở
                searchAllCustomers();
            });
        }
        
        // Lắng nghe sự kiện hiển thị modal sản phẩm
        const productModal = document.getElementById('productModal');
        if (productModal) {
            productModal.addEventListener('shown.bs.modal', function() {
                console.log("Modal sản phẩm được mở, tự động tìm kiếm...");
                // Tự động tìm kiếm sản phẩm khi modal mở
                searchProducts(0);
            });
        }

        // Thêm sự kiện cho nút thanh toán
        if (checkoutBtn) {
            checkoutBtn.addEventListener('click', function() {
                console.log("Nút thanh toán được nhấn");
                
                // Lấy phương thức thanh toán đã chọn
                const paymentMethod = document.querySelector('input[name="paymentMethod"]:checked').value;
                console.log("Phương thức thanh toán đã chọn:", paymentMethod);
                
                // Cập nhật input ẩn cho phương thức thanh toán
                document.getElementById('paymentMethodInput').value = paymentMethod;
                
                // Tạo input ẩn cho sản phẩm trong giỏ hàng
                const productInputsContainer = document.getElementById('productInputs');
                productInputsContainer.innerHTML = ''; // Xóa các input cũ
                
                // Thêm input ẩn cho mỗi sản phẩm trong giỏ hàng
                cart.forEach((item, index) => {
                    const productIdInput = document.createElement('input');
                    productIdInput.type = 'hidden';
                    productIdInput.name = 'productID';
                    productIdInput.value = item.productId;
                    productInputsContainer.appendChild(productIdInput);
                    
                    const quantityInput = document.createElement('input');
                    quantityInput.type = 'hidden';
                    quantityInput.name = 'quantity';
                    quantityInput.value = item.quantity;
                    productInputsContainer.appendChild(quantityInput);
                });
                
                // Kiểm tra nếu cần in hóa đơn
                const printInvoice = document.getElementById('printInvoice');
                if (printInvoice && printInvoice.checked) {
                    const printInvoiceInput = document.createElement('input');
                    printInvoiceInput.type = 'hidden';
                    printInvoiceInput.name = 'printInvoice';
                    printInvoiceInput.value = 'true';
                    productInputsContainer.appendChild(printInvoiceInput);
                }

                // Lấy form thanh toán và submit
                const salesForm = document.getElementById('salesForm');
                if (salesForm) {
                    console.log("Đang gửi form thanh toán...");
                    salesForm.submit();
        } else {
                    console.error("Không tìm thấy form thanh toán!");
                    showToast('error', 'Lỗi khi gửi form thanh toán!');
                }
            });
        }
    });

    // Hàm lưu khách hàng mới
    function saveNewCustomer() {
        console.log("Đang lưu khách hàng mới...");

        // Lấy form và dữ liệu
        const form = document.getElementById('newCustomerForm');
        if (!form) {
            console.error('Không tìm thấy form khách hàng mới!');
            return;
        }

        // Lấy dữ liệu từ form
        const fullName = document.getElementById('newCustomerName').value.trim();
        const phone = document.getElementById('newCustomerPhone').value.trim();
        const email = document.getElementById('newCustomerEmail').value.trim();
        const address = document.getElementById('newCustomerAddress').value.trim();
        const gender = document.getElementById('newCustomerGender').value;

        // Xóa thông báo lỗi cũ
        document.getElementById('fullNameError').textContent = '';
        document.getElementById('phoneError').textContent = '';
        document.getElementById('emailError').textContent = '';
        document.getElementById('addressError').textContent = '';

        // Validate theo ràng buộc từ model Customer
        let isValid = true;

        // Validate fullName
        if (!fullName) {
            document.getElementById('fullNameError').textContent = 'Họ và tên không được để trống!';
            isValid = false;
        } else if (fullName.length > 50) {
            showToast('error', 'Họ và tên không được vượt quá 50 ký tự!');
            document.getElementById('fullNameError').textContent = 'Họ và tên không được vượt quá 50 ký tự!';
            isValid = false;
        } else if (!/^[\p{L} ]+$/u.test(fullName)) {
            showToast('error', 'Họ và tên chỉ được chứa chữ cái và khoảng trắng!');
            document.getElementById('fullNameError').textContent = 'Họ và tên chỉ được chứa chữ cái và khoảng trắng!';
            isValid = false;
        }

        // Validate phone
        if (!phone) {
            document.getElementById('phoneError').textContent = 'Số điện thoại không được để trống!';
            isValid = false;
        } else if (!/^\d{10,15}$/.test(phone)) {
            showToast('error', 'Số điện thoại phải chứa từ 10 đến 15 chữ số!');
            document.getElementById('phoneError').textContent = 'Số điện thoại phải chứa từ 10 đến 15 chữ số!';
            isValid = false;
        }

        // Validate email
        if (!email) {
            document.getElementById('emailError').textContent = 'Email không được để trống!';
            isValid = false;
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            document.getElementById('emailError').textContent = 'Định dạng email không hợp lệ!';
            isValid = false;
        } else if (email.length > 50) {
            document.getElementById('emailError').textContent = 'Email không được vượt quá 50 ký tự!';
            isValid = false;
        }

        // Validate address
        if (!address) {
            document.getElementById('addressError').textContent = 'Địa chỉ không được để trống!';
            isValid = false;
        } else if (address.length > 500) {
            showToast('error', 'Địa chỉ không được vượt quá 500 ký tự!');
            document.getElementById('addressError').textContent = 'Địa chỉ không được vượt quá 500 ký tự!';
            isValid = false;
        }

        // Nếu có lỗi, dừng lại
        if (!isValid) {
            return;
        }

        // Chuẩn bị dữ liệu gửi lên server
        const customerData = {
            fullName: fullName,
            phone: phone,
            email: email,
            address: address,
            gender: gender,
            dob: "2000-01-01"
        };

        console.log("Dữ liệu khách hàng:", customerData);

        // Hiển thị trạng thái đang xử lý
        saveCustomerBtn.disabled = true;
        saveCustomerBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Đang lưu...';

        // Gửi yêu cầu POST đến API
        fetch('/api/sales/add-customer', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(customerData)
        })
            .then(response => response.json())
            .then(data => {
                console.log("Kết quả lưu khách hàng:", data);

                // Khôi phục nút
                saveCustomerBtn.disabled = false;
                saveCustomerBtn.innerHTML = '<i class="fas fa-save me-2"></i>Lưu khách hàng';

                if (data.success) {
                    // Hiển thị thông báo thành công
                    showToast('success', data.message || 'Đã thêm khách hàng thành công!');

                    // Đóng modal
                    const modal = bootstrap.Modal.getInstance(document.getElementById('newCustomerModal'));
                    if (modal) modal.hide();

                    // Chọn khách hàng vừa tạo
                    selectCustomer(
                        data.customerId,
                        data.fullName,
                        customerData.phone,
                        customerData.email,
                        customerData.address
                    );

                    // Xóa form
                    form.reset();
                } else {
                    // Xử lý các loại lỗi khác nhau
                    if (data.errors) {
                        // Hiển thị lỗi validation cho từng trường
                        if (data.errors.fullName) {
                            document.getElementById('fullNameError').textContent = data.errors.fullName;
                        }
                        if (data.errors.phone) {
                            document.getElementById('phoneError').textContent = data.errors.phone;
                        }
                        if (data.errors.email) {
                            document.getElementById('emailError').textContent = data.errors.email;
                        }
                        if (data.errors.address) {
                            document.getElementById('addressError').textContent = data.errors.address;
                        }

                        // Hiển thị toast với thông báo lỗi chung
                        showToast('error', 'Vui lòng kiểm tra lại thông tin khách hàng!');
                    } else if (data.message) {
                        // Xử lý lỗi nghiệp vụ (như trùng số điện thoại hoặc email)
                        if (data.message.includes("Số điện thoại đã tồn tại")) {
                            document.getElementById('phoneError').textContent = 'Số điện thoại đã tồn tại trong hệ thống!';
                            showToast('error', 'Số điện thoại đã tồn tại!');
                        } else if (data.message.includes("Email đã tồn tại")) {
                            document.getElementById('emailError').textContent = 'Email đã tồn tại trong hệ thống!';
                            showToast('error', 'Email đã tồn tại!');
                        } else {
                            // Hiển thị thông báo lỗi khác
                            showToast('error', data.message);
                        }
                    } else {
                        // Hiển thị thông báo lỗi nếu không có chi tiết lỗi từ server
                        showToast('error', 'Đã xảy ra lỗi khi tạo khách hàng mới');
                    }
                }
            })
            .catch(error => {
                console.error("Lỗi khi thêm khách hàng:", error);

                // Khôi phục nút
                saveCustomerBtn.disabled = false;
                saveCustomerBtn.innerHTML = '<i class="fas fa-save me-2"></i>Lưu khách hàng';

                // Hiển thị thông báo lỗi
                showToast('error', 'Lỗi kết nối khi thêm khách hàng: ' + error.message);
            });
    }
})();
