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
    
    // Khai báo biến khác
    let cart = [];
    let currentCustomerPage = 0;
    let allCurrentPage = 0;
    let pageSize = 5;
    let customerSearchTimeout;
    let productSearchTimeout;
    let allCustomerSearchTimeout;
    
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
        
        // Tạo URL với tham số "keyword" (đây là điểm quan trọng)
        const url = `/api/sales/search-customers?keyword=${encodeURIComponent(searchInput)}&page=${currentCustomerPage}&size=${pageSize}&_=${new Date().getTime()}`;
        
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
            // Kiểm tra xem data có chứa nội dung không
            if (data && data.content) {
                console.log(`DEBUG: Tìm thấy ${data.content.length} khách hàng trên trang này`);
                console.log(`DEBUG: Tổng số khách hàng: ${data.totalElements}`);
                
                // Kiểm tra và log chi tiết về các khách hàng tìm được
                data.content.forEach((customer, index) => {
                    console.log(`DEBUG: Khách hàng #${index + 1}: ID=${customer.customerID || customer.id}, Tên=${customer.fullName}, SĐT=${customer.phone}, Email=${customer.email}`);
                });
                
                // Cập nhật bảng khách hàng
                updateCustomerTable(data.content);
                
                // Cập nhật phân trang
                let paginationInfo = {
                    content: data.content,
                    totalElements: data.totalElements, 
                    totalPages: data.totalPages,
                    number: data.number,
                    size: data.size,
                    numberOfElements: data.numberOfElements
                };
                
                updatePagination('customerPagination', paginationInfo, changeCustomerPage);
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
    
    // Chức năng tìm kiếm tất cả khách hàng
    function searchAllCustomers() {
        let searchInput = document.getElementById('allCustomerSearchInput').value.trim();
        console.log("DEBUG: Tìm kiếm tất cả khách hàng với từ khóa: " + searchInput);
        
        // Hiển thị đang tải
        const customerListElement = document.getElementById('customerListWithPurchaseCount');
        if (customerListElement) {
            customerListElement.innerHTML = '<tr><td colspan="6" class="text-center"><div class="spinner-border text-primary" role="status"></div></td></tr>';
        }
        
        // Tạo URL với tham số "keyword" (đây là điểm quan trọng) 
        const url = `/api/sales/search-all-customers?keyword=${encodeURIComponent(searchInput)}&page=${allCurrentPage}&size=${pageSize}&_=${new Date().getTime()}`;
        
        console.log(`DEBUG: Gọi API tìm kiếm tất cả khách hàng: ${url}`);
        
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
            console.log(`API tìm kiếm tất cả khách hàng trả về status: ${response.status}`);
            if (!response.ok) {
                throw new Error(`Lỗi HTTP: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log(`DEBUG: API trả về dữ liệu tất cả khách hàng:`, data);
            // Kiểm tra xem data có chứa nội dung không
            if (data && data.content) {
                console.log(`DEBUG: Tìm thấy ${data.content.length} khách hàng trên trang này`);
                console.log(`DEBUG: Tổng số khách hàng: ${data.totalElements}`);
                
                // Kiểm tra và log chi tiết về các khách hàng tìm được
                data.content.forEach((customer, index) => {
                    console.log(`DEBUG: Tất cả khách hàng #${index + 1}: ID=${customer.customerID || customer.id}, Tên=${customer.fullName}, SĐT=${customer.phone}, Email=${customer.email}`);
                });
                
                // Cập nhật bảng khách hàng
                updateAllCustomerTable(data.content);
                
                // Cập nhật phân trang
                let paginationInfo = {
                    content: data.content,
                    totalElements: data.totalElements, 
                    totalPages: data.totalPages,
                    number: data.number,
                    size: data.size,
                    numberOfElements: data.numberOfElements
                };
                
                updatePagination('allCustomerPagination', paginationInfo, changeAllCustomerPage);
            } else {
                console.error(`DEBUG: API trả về dữ liệu không hợp lệ:`, data);
                updateAllCustomerTable([]);
                // Ẩn phân trang nếu không có dữ liệu
                let paginationElement = document.getElementById('allCustomerPagination');
                if (paginationElement) {
                    paginationElement.innerHTML = '';
                    paginationElement.style.display = 'none';
                }
            }
        })
        .catch(error => {
            console.error(`Lỗi khi tìm kiếm tất cả khách hàng:`, error);
            if (customerListElement) {
                customerListElement.innerHTML = `<tr><td colspan="6" class="text-center text-danger">Lỗi khi tải dữ liệu khách hàng: ${error.message}</td></tr>`;
            }
            showToast('error', `Lỗi khi tìm kiếm khách hàng: ${error.message}`);
        });
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
        params.append('size', 5);

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
                updateProductTable(data, page);
            })
            .catch(error => {
                console.error(`Lỗi khi tìm kiếm sản phẩm:`, error);
                productListElement.innerHTML = `<tr><td colspan="4" class="text-center text-danger">Lỗi khi tải dữ liệu sản phẩm: ${error.message}</td></tr>`;
                showToast('error', `Lỗi khi tìm kiếm sản phẩm: ${error.message}`);
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
            console.log(`DEBUG: Xử lý tất cả khách hàng: ID=${customer.customerID || customer.id}, Tên=${customer.fullName}, SĐT=${customer.phone}`);
            
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
            
            // Xử lý phân trang
            const paginationElement = document.getElementById('productPagination');
            if (paginationElement) {
                let paginationInfo = {
                    content: data.content,
                    totalElements: data.totalElements,
                    totalPages: data.totalPages,
                    number: currentPage,
                    size: data.size,
                    numberOfElements: data.numberOfElements
                };
                
                updatePagination('productPagination', paginationInfo, function(page) {
                    searchProducts(page);
                });
            }
            
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
        
        // Chúng ta không cần phải cập nhật từng phần riêng lẻ vì đã đổ dữ liệu vào các input
        // Nếu trong tương lai cần hiển thị ra ngoài, có thể thêm code ở đây
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
            
            // Đóng modal
            const productModal = document.getElementById('productModal');
            if (productModal) {
                const modal = bootstrap.Modal.getInstance(productModal);
                if (modal) modal.hide();
            }
            
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
                    <td>${item.name}</td>
                    <td>${formatCurrency(item.price)}</td>
                    <td>
                        <div class="input-group input-group-sm">
                            <button class="btn btn-outline-secondary decrease-quantity" data-index="${index}">-</button>
                            <input type="number" class="form-control text-center quantity-input" value="${item.quantity}" data-index="${index}" min="1" max="${item.stock}">
                            <button class="btn btn-outline-secondary increase-quantity" data-index="${index}">+</button>
                        </div>
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
        document.querySelectorAll('.decrease-quantity').forEach(button => {
            button.addEventListener('click', function() {
                const index = parseInt(this.getAttribute('data-index'));
                updateCartItemQuantity(index, cart[index].quantity - 1);
            });
        });
        
        document.querySelectorAll('.increase-quantity').forEach(button => {
            button.addEventListener('click', function() {
                const index = parseInt(this.getAttribute('data-index'));
                updateCartItemQuantity(index, cart[index].quantity + 1);
            });
        });
        
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
    
    // Lưu khách hàng mới
    function saveNewCustomer() {
        const form = document.getElementById('newCustomerForm');
        if (!form) {
            console.error('Không tìm thấy form khách hàng mới!');
            return;
        }
        
        if (!form.checkValidity()) {
            form.classList.add('was-validated');
            return;
        }
        
        const formData = new FormData(form);
        
        // Thêm CSRF token vào formData
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
        
        // Hiển thị loading
        const saveBtn = document.getElementById('saveNewCustomerBtn');
        if (!saveBtn) {
            console.error('Không tìm thấy nút lưu khách hàng!');
            return;
        }
        
        const originalBtnText = saveBtn.innerHTML;
        saveBtn.disabled = true;
        saveBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Đang lưu...';
        
        // Chuẩn bị request
        let fetchOptions = {
            method: 'POST',
            body: formData,
            credentials: 'same-origin'
        };
        
        // Thêm CSRF header nếu có
        if (csrfToken && csrfHeader) {
            const headers = new Headers();
            headers.append(csrfHeader, csrfToken);
            fetchOptions.headers = headers;
        }
        
        // Gửi API request để tạo khách hàng mới
        fetch('/api/create-customer', fetchOptions)
        .then(response => {
            console.log('Status:', response.status);
            console.log('ContentType:', response.headers.get('content-type'));
            
            // LƯU Ý: Không redirect, chúng ta chỉ xử lý phản hồi JSON
            if (!response.ok) {
                throw new Error(`Lỗi ${response.status}: ${response.statusText}`);
            }
            
            return response.json();
        })
        .then(data => {
            // Xử lý thành công
            console.log('Data:', data);
            
            // Kiểm tra customerId có tồn tại trong response
            if (data.success === true && data.customerId) {
                console.log('Thêm khách hàng thành công với ID:', data.customerId);
                
                // Lấy thông tin từ form
                const fullName = document.getElementById('newCustomerName').value;
                const phone = document.getElementById('newCustomerPhone').value;
                const email = document.getElementById('newCustomerEmail').value || '';
                const address = document.getElementById('newCustomerAddress').value || '';
                
                // Chọn khách hàng và cập nhật form
                selectCustomer(
                    data.customerId,
                    fullName,
                    phone,
                    email,
                    address
                );
                
                // Đóng modal
                try {
                    const modalElement = document.getElementById('newCustomerModal');
                    if (modalElement) {
                        const modal = bootstrap.Modal.getInstance(modalElement);
                        if (modal) {
                            modal.hide();
                            console.log('Đã đóng modal');
                        } else {
                            console.error('Không tìm thấy instance bootstrap Modal');
                            // Thử phương pháp khác để đóng modal
                            const newModal = new bootstrap.Modal(modalElement);
                            newModal.hide();
                        }
                    } else {
                        console.error('Không tìm thấy modal element');
                    }
                } catch (error) {
                    console.error('Lỗi khi đóng modal:', error);
                    // Phương pháp cuối cùng nếu các phương pháp khác không thành công
                    $('#newCustomerModal').modal('hide');
                }
                
                // Thông báo thành công
                showToast('success', 'Đã thêm khách hàng' + data.fullName + 'thành công');
                
                // Reset form
                form.reset();
                form.classList.remove('was-validated');
            } else {
                console.error('Response không hợp lệ:', data);
                showToast('error', data.message || 'Lỗi khi thêm khách hàng mới');
            }
        })
        .catch(error => {
            console.error('Error creating customer:', error);
            showToast('error', error.message);
        })
        .finally(() => {
            // Khôi phục trạng thái nút
            saveBtn.disabled = false;
            saveBtn.innerHTML = originalBtnText;
        });
    }
    
    // Kiểm tra điều kiện để có thể checkout
    function validateCheckout() {
        console.log(`Trạng thái checkout: Có khách hàng=${customerId && customerId.value ? 'true' : 'false'}, Có sản phẩm=${cart.length > 0 ? 'true' : 'false'}`);
        
        if (!customerId || !customerId.value || cart.length === 0) {
            showToast('error', 'Vui lòng chọn khách hàng và thêm sản phẩm vào giỏ hàng');
            return;
        }
        
        if (!checkoutBtn) {
            console.error('Không tìm thấy nút thanh toán!');
            return;
        }
        
        // Kích hoạt hoặc vô hiệu hóa nút thanh toán
        checkoutBtn.disabled = !(customerId && customerId.value && cart.length > 0);
    }
    
    // Xử lý thanh toán
    function processCheckout() {
        const customerId = document.getElementById('customerId');
        const selectedCustomerInfo = document.getElementById('selectedCustomerInfo');
        const checkoutBtn = document.getElementById('checkoutBtn');
        const salesForm = document.getElementById('salesForm');
        const productInputs = document.getElementById('productInputs');
        const paymentMethodInput = document.getElementById('paymentMethodInput');
        
        console.log(`Trạng thái checkout: Có khách hàng=${customerId && customerId.value ? 'true' : 'false'}, Có sản phẩm=${cart.length > 0 ? 'true' : 'false'}`);
        
        if (!customerId || !customerId.value || cart.length === 0) {
            showToast('error', 'Vui lòng chọn khách hàng và thêm sản phẩm vào giỏ hàng');
            return;
        }
        
        if (!checkoutBtn) {
            console.error('Không tìm thấy nút thanh toán!');
            return;
        }
        
        if (!salesForm) {
            console.error('Không tìm thấy form thanh toán!');
            return;
        }
        
        if (!productInputs) {
            console.error('Không tìm thấy phần tử chứa inputs sản phẩm!');
            return;
        }
        
        // Xóa các input sản phẩm cũ (nếu có)
        productInputs.innerHTML = '';
        
        // Thêm thông tin sản phẩm vào form
        cart.forEach((item, index) => {
            // Tạo input cho productID
            const productIdInput = document.createElement('input');
            productIdInput.type = 'hidden';
            productIdInput.name = 'productID';
            productIdInput.value = item.productId;
            productInputs.appendChild(productIdInput);
            
            // Tạo input cho quantity
            const quantityInput = document.createElement('input');
            quantityInput.type = 'hidden';
            quantityInput.name = 'quantity';
            quantityInput.value = item.quantity;
            productInputs.appendChild(quantityInput);
        });
        
        // Cập nhật phương thức thanh toán
        const paymentMethodChecked = document.querySelector('input[name="paymentMethod"]:checked');
        if (!paymentMethodChecked) {
            showToast('error', 'Vui lòng chọn phương thức thanh toán');
            return;
        }
        paymentMethodInput.value = paymentMethodChecked.value;
        
        // Thêm tùy chọn in hóa đơn
        const printInvoice = document.getElementById('printInvoice');
        let printInvoiceInput = document.getElementById('printInvoiceInput');
        
        if (!printInvoiceInput) {
            printInvoiceInput = document.createElement('input');
            printInvoiceInput.type = 'hidden';
            printInvoiceInput.name = 'printInvoice';
            printInvoiceInput.id = 'printInvoiceInput';
            productInputs.appendChild(printInvoiceInput);
        }
        
        printInvoiceInput.value = printInvoice && printInvoice.checked ? 'true' : 'false';
        
        // Debug form
        console.log("Debug Form contents:");
        const formData = new FormData(salesForm);
        for (let pair of formData.entries()) {
            console.log(pair[0] + ': ' + pair[1]);
        }
        
        // Hiển thị loading
        checkoutBtn.disabled = true;
        checkoutBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Đang xử lý...';
        
        // Submit form
        salesForm.submit();
    }
    
    // Khởi tạo các modal và sự kiện
    function initModals() {
        console.log("DEBUG: Khởi tạo các modal...");
        
        // Modal Khách hàng đã từng mua
        const existingCustomerModal = document.getElementById('existingCustomerModal');
        if (existingCustomerModal) {
            console.log("DEBUG: Tìm thấy modal khách hàng đã mua, thiết lập sự kiện");
            
            // Kiểm tra và tạo phần tử phân trang nếu chưa có
            const customerPagination = document.getElementById('customerPagination');
            if (!customerPagination) {
                console.error("DEBUG: Không tìm thấy phần tử customerPagination, đang tạo mới");
                const customerModalBody = document.querySelector('#existingCustomerModal .modal-body');
                if (customerModalBody) {
                    const paginationDiv = document.createElement('div');
                    paginationDiv.id = 'customerPagination';
                    paginationDiv.className = 'd-flex justify-content-end mt-3';
                    customerModalBody.appendChild(paginationDiv);
                    console.log("DEBUG: Đã tạo phần tử customerPagination");
                }
            } else {
                console.log("DEBUG: Tìm thấy phần tử customerPagination");
            }
            
            // Thiết lập sự kiện cho modal khách hàng đã từng mua
            existingCustomerModal.addEventListener('shown.bs.modal', function () {
                console.log("DEBUG: Modal khách hàng đã mua được mở");
                // Reset trang về 0 và tìm kiếm
                currentCustomerPage = 0;
                // Tìm kiếm khách hàng ngay khi mở modal
                searchCustomers();
                // Focus vào ô tìm kiếm
                const searchInput = document.getElementById('customerSearchInput');
                if (searchInput) {
                    searchInput.focus();
                    console.log("DEBUG: Đã focus vào ô tìm kiếm khách hàng");
                }
            });
        } else {
            console.error("DEBUG: Không tìm thấy modal khách hàng đã mua");
        }
        
        // Modal Tất cả khách hàng
        const existingAllCustomerModal = document.getElementById('existingAllCustomerModal');
        if (existingAllCustomerModal) {
            console.log("DEBUG: Tìm thấy modal tất cả khách hàng, thiết lập sự kiện");
            
            const allCustomerPagination = document.getElementById('allCustomerPagination');
            if (!allCustomerPagination) {
                console.error("DEBUG: Không tìm thấy phần tử allCustomerPagination, đang tạo mới");
                const allCustomerModalBody = document.querySelector('#existingAllCustomerModal .modal-body');
                if (allCustomerModalBody) {
                    const paginationDiv = document.createElement('div');
                    paginationDiv.id = 'allCustomerPagination';
                    paginationDiv.className = 'd-flex justify-content-end mt-3';
                    allCustomerModalBody.appendChild(paginationDiv);
                    console.log("DEBUG: Đã tạo phần tử allCustomerPagination");
                }
            } else {
                console.log("DEBUG: Tìm thấy phần tử allCustomerPagination");
            }
            
            // Thiết lập sự kiện cho modal tất cả khách hàng
            existingAllCustomerModal.addEventListener('shown.bs.modal', function () {
                console.log("DEBUG: Modal tất cả khách hàng được mở");
                // Reset trang về 0 và tìm kiếm
                allCurrentPage = 0;
                // Tìm kiếm khách hàng ngay khi mở modal
                searchAllCustomers();
                // Focus vào ô tìm kiếm
                const searchInput = document.getElementById('allCustomerSearchInput');
                if (searchInput) {
                    searchInput.focus();
                    console.log("DEBUG: Đã focus vào ô tìm kiếm tất cả khách hàng");
                }
            });
        } else {
            console.error("DEBUG: Không tìm thấy modal tất cả khách hàng");
        }
        
        // Modal Sản phẩm
        const productModal = document.getElementById('productModal');
        if (productModal) {
            console.log("DEBUG: Tìm thấy modal sản phẩm, thiết lập sự kiện");
            
            // Thiết lập sự kiện cho modal sản phẩm
            productModal.addEventListener('shown.bs.modal', function () {
                console.log("DEBUG: Modal sản phẩm được mở");
                // Tìm kiếm với từ khóa mặc định
                searchProducts(0);
                // Focus vào ô tìm kiếm
                const searchInput = document.getElementById('productSearchInput');
                if (searchInput) {
                    searchInput.focus();
                    console.log("DEBUG: Đã focus vào ô tìm kiếm sản phẩm");
                }
            });
        } else {
            console.error("DEBUG: Không tìm thấy modal sản phẩm");
        }
        
        console.log("DEBUG: Khởi tạo modal hoàn tất, thiết lập sự kiện tìm kiếm...");
        
        // Thiết lập các sự kiện tìm kiếm cho các modal
        setupSearchListeners();
        
        console.log("DEBUG: Khởi tạo modal và sự kiện hoàn tất");
    }
    
    // Thiết lập người nghe sự kiện tìm kiếm
    function setupSearchListeners() {
        console.log("DEBUG: Thiết lập các sự kiện tìm kiếm...");

        // Tìm kiếm khách hàng đã mua khi nhập
        const customerSearchInput = document.getElementById('customerSearchInput');
        if (customerSearchInput) {
            console.log("DEBUG: Đã tìm thấy input customerSearchInput, thiết lập sự kiện");
            
            // Reset input và tìm kiếm ban đầu (để hiển thị tất cả)
            setTimeout(function() {
                customerSearchInput.value = '';
                // Tìm kiếm ngay lập tức khi khởi tạo, không cần timeout
                currentCustomerPage = 0;
                searchCustomers();
            }, 100);
            
            // Tìm kiếm khi gõ với độ trễ
            customerSearchInput.addEventListener('input', function() {
                console.log("DEBUG: Sự kiện input đã kích hoạt trên customerSearchInput");
                clearTimeout(customerSearchTimeout);
                customerSearchTimeout = setTimeout(function() {
                    currentCustomerPage = 0; // Reset về trang đầu tiên khi tìm kiếm mới
                    searchCustomers();
                }, 500); // Tăng độ trễ lên 500ms để tránh quá nhiều request
            });
            
            // Tìm kiếm khi nhấn Enter
            customerSearchInput.addEventListener('keydown', function(e) {
                if (e.key === 'Enter') {
                    console.log("DEBUG: Phím Enter đã được nhấn trong customerSearchInput");
                    e.preventDefault();
                    clearTimeout(customerSearchTimeout);
                    currentCustomerPage = 0;
                    searchCustomers();
                }
            });
        } else {
            console.error("DEBUG: Không tìm thấy input tìm kiếm khách hàng đã mua");
        }
        
        // Bắt sự kiện cho nút tìm kiếm khách hàng
        const searchCustomerBtn = document.getElementById('searchCustomerBtn');
        if (searchCustomerBtn) {
            console.log("DEBUG: Đã tìm thấy nút searchCustomerBtn, thiết lập sự kiện");
            searchCustomerBtn.addEventListener('click', function() {
                console.log("DEBUG: Nút tìm kiếm khách hàng đã được nhấn");
                clearTimeout(customerSearchTimeout);
                currentCustomerPage = 0;
                searchCustomers();
            });
        } else {
            console.error("DEBUG: Không tìm thấy nút tìm kiếm khách hàng đã mua");
        }
        
        // Tìm kiếm tất cả khách hàng khi nhập
        const allCustomerSearchInput = document.getElementById('allCustomerSearchInput');
        if (allCustomerSearchInput) {
            console.log("DEBUG: Đã tìm thấy input allCustomerSearchInput, thiết lập sự kiện");
            
            // Reset input và tìm kiếm ban đầu (để hiển thị tất cả)
            setTimeout(function() {
                allCustomerSearchInput.value = '';
                // Tìm kiếm ngay lập tức khi khởi tạo, không cần timeout
                allCurrentPage = 0;
                searchAllCustomers();
            }, 100);
            
            // Tìm kiếm khi gõ với độ trễ
            allCustomerSearchInput.addEventListener('input', function() {
                console.log("DEBUG: Sự kiện input đã kích hoạt trên allCustomerSearchInput");
                clearTimeout(allCustomerSearchTimeout);
                allCustomerSearchTimeout = setTimeout(function() {
                    allCurrentPage = 0; // Reset về trang đầu tiên khi tìm kiếm mới
                    searchAllCustomers();
                }, 500); // Tăng độ trễ lên 500ms để tránh quá nhiều request
            });
            
            // Tìm kiếm khi nhấn Enter
            allCustomerSearchInput.addEventListener('keydown', function(e) {
                if (e.key === 'Enter') {
                    console.log("DEBUG: Phím Enter đã được nhấn trong allCustomerSearchInput");
                    e.preventDefault();
                    clearTimeout(allCustomerSearchTimeout);
                    allCurrentPage = 0;
                    searchAllCustomers();
                }
            });
        } else {
            console.error("DEBUG: Không tìm thấy input tìm kiếm tất cả khách hàng");
        }
        
        // Bắt sự kiện cho nút tìm kiếm tất cả khách hàng
        const allSearchCustomerBtn = document.getElementById('allSearchCustomerBtn');
        if (allSearchCustomerBtn) {
            console.log("DEBUG: Đã tìm thấy nút allSearchCustomerBtn, thiết lập sự kiện");
            allSearchCustomerBtn.addEventListener('click', function() {
                console.log("DEBUG: Nút tìm kiếm tất cả khách hàng đã được nhấn");
                clearTimeout(allCustomerSearchTimeout);
                allCurrentPage = 0;
                searchAllCustomers();
            });
        } else {
            console.error("DEBUG: Không tìm thấy nút tìm kiếm tất cả khách hàng");
        }
        
        // Sự kiện tìm kiếm sản phẩm
        const productSearchInput = document.getElementById('productSearchInput');
        if (productSearchInput) {
            console.log("DEBUG: Đã tìm thấy input productSearchInput, thiết lập sự kiện");
            
            // Reset input và tìm kiếm ban đầu (để hiển thị tất cả)
            setTimeout(function() {
                productSearchInput.value = '';
                // Tìm kiếm ngay lập tức khi khởi tạo, không cần timeout
                searchProducts(0);
            }, 100);
            
            // Tìm kiếm khi gõ với độ trễ
            productSearchInput.addEventListener('input', function() {
                console.log("DEBUG: Sự kiện input đã kích hoạt trên productSearchInput");
                clearTimeout(productSearchTimeout);
                productSearchTimeout = setTimeout(function() {
                    searchProducts(0);
                }, 500); // Tăng độ trễ lên 500ms để tránh quá nhiều request
            });
            
            // Tìm kiếm khi nhấn Enter
            productSearchInput.addEventListener('keydown', function(e) {
                if (e.key === 'Enter') {
                    console.log("DEBUG: Phím Enter đã được nhấn trong productSearchInput");
                    e.preventDefault();
                    searchProducts(0);
                }
            });
        } else {
            console.error("DEBUG: Không tìm thấy input tìm kiếm sản phẩm");
        }
        
        // Nút tìm kiếm sản phẩm
        const searchProductBtn = document.getElementById('searchProductBtn');
        if (searchProductBtn) {
            console.log("DEBUG: Đã tìm thấy nút searchProductBtn, thiết lập sự kiện");
            searchProductBtn.addEventListener('click', function() {
                console.log("DEBUG: Nút tìm kiếm sản phẩm đã được nhấn");
                searchProducts(0);
            });
        } else {
            console.error("DEBUG: Không tìm thấy nút tìm kiếm sản phẩm");
        }
        
        console.log("DEBUG: Thiết lập sự kiện tìm kiếm hoàn tất");
    }
    
    // Thiết lập các event listeners cho giỏ hàng và thanh toán
    function setupCartAndCheckoutListeners() {
        console.log('Thiết lập các sự kiện giỏ hàng và thanh toán...');
        
        // Nút thanh toán
        const checkoutBtn = document.getElementById('checkoutBtn');
        if (checkoutBtn) {
            checkoutBtn.addEventListener('click', function() {
                processCheckout();
            });
        } else {
            console.warn('Không tìm thấy nút thanh toán (ID: checkoutBtn)');
        }
        
        console.log('Thiết lập sự kiện giỏ hàng và thanh toán hoàn tất');
    }
    
    // Khởi tạo các sự kiện khi trang được tải
    document.addEventListener('DOMContentLoaded', function() {
        console.log('Trang đã tải xong, bắt đầu khởi tạo...');
        
        try {
            // Khởi tạo các modal (sẽ khởi tạo cả 3 loại modal)
            initModals();
            
            // Thiết lập sự kiện tìm kiếm
            setupSearchListeners();
            
            // Thiết lập sự kiện lưu khách hàng mới (thực hiện trước các listener khác)
            setupNewCustomer();
            
            // Thiết lập các event listeners cho giỏ hàng và thanh toán
            setupCartAndCheckoutListeners();
            
            // Khởi tạo giỏ hàng
            updateCartUI();
            
            // Kiểm tra trạng thái thanh toán ban đầu
            validateCheckout();
            
            console.log('Khởi tạo trang hoàn tất');
        } catch (error) {
            console.error('Lỗi trong quá trình khởi tạo trang:', error);
        }
    });

    // Kiểm tra nếu chưa có hàm setupNewCustomer, thêm một hàm rỗng để tránh lỗi
    function setupNewCustomer() {
        console.log("DEBUG: Thiết lập sự kiện cho khách hàng mới");
        
        // Nút lưu khách hàng mới - xóa tất cả các event listener hiện có và thêm lại
        const saveCustomerBtn = document.getElementById('saveNewCustomerBtn');
        if (saveCustomerBtn) {
            console.log("DEBUG: Đã tìm thấy nút saveNewCustomerBtn, thiết lập sự kiện");
            
            // Xóa tất cả các event listener hiện có bằng cách clone và thay thế
            const newSaveBtn = saveCustomerBtn.cloneNode(true);
            saveCustomerBtn.parentNode.replaceChild(newSaveBtn, saveCustomerBtn);
            
            // Thêm event listener mới
            newSaveBtn.addEventListener('click', function() {
                console.log("DEBUG: Nút lưu khách hàng đã được nhấn");
                saveNewCustomer();
            });
        } else {
            console.error("DEBUG: Không tìm thấy nút lưu khách hàng mới (ID: saveNewCustomerBtn)");
        }
        
        console.log("DEBUG: Thiết lập sự kiện khách hàng mới hoàn tất");
    }

    // Cập nhật điều khiển phân trang
    function updatePagination(elementId, paginationInfo, pageChangeFunction) {
        const paginationElement = document.getElementById(elementId);
        if (!paginationElement) {
            console.error(`Không tìm thấy phần tử phân trang: ${elementId}`);
            return;
        }
        
        // Lấy thông tin phân trang từ dữ liệu API
        const totalElements = paginationInfo.totalElements || 0;
        const size = paginationInfo.size || 5;
        const totalPages = paginationInfo.totalPages || Math.ceil(totalElements / size);
        const currentPage = paginationInfo.number || 0;
        
        console.log(`DEBUG: Cập nhật phân trang ${elementId} - Tổng phần tử: ${totalElements}, Số trang: ${totalPages}, Trang hiện tại: ${currentPage}`);
        
        // Nếu tổng số phần tử ít hơn hoặc bằng số lượng hiển thị trên một trang, ẩn phân trang
        if (totalElements <= size) {
            console.log(`DEBUG: Ẩn phân trang ${elementId} vì có ít hơn hoặc bằng ${size} phần tử`);
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
        paginationContainer.className = 'pagination justify-content-end';
        
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
        searchCustomers();
    }

    // Hàm thay đổi trang cho tất cả khách hàng
    function changeAllCustomerPage(page) {
        allCurrentPage = page;
        searchAllCustomers();
    }

    // Hàm thay đổi trang cho sản phẩm
    function changeProductPage(page) {
        searchProducts(page);
    }
})();
