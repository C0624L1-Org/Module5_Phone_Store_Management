/**
 * Script xử lý tìm kiếm cho thanh navbar
 * Hỗ trợ tìm kiếm theo thời gian thực hiển thị kết quả ngay khi người dùng gõ
 */
document.addEventListener('DOMContentLoaded', function() {
    // Lấy tham chiếu đến các phần tử DOM
    const searchInput = document.getElementById('search-input');
    const searchResults = document.getElementById('search-results');
    const searchButton = document.getElementById('search-button');
    const searchForm = document.getElementById('search-form');
    
    console.log("DOM loaded, search elements:", {
        searchInput: searchInput ? true : false,
        searchResults: searchResults ? true : false,
        searchButton: searchButton ? true : false
    });

    // Kiểm tra xem có phần tử cần thiết không
    if (!searchInput || !searchResults || !searchButton) {
        console.error("Không tìm thấy các phần tử cần thiết cho chức năng tìm kiếm");
        return;
    }

    // Thiết lập debounce để tránh gọi API quá nhiều lần
    let debounceTimer;
    const DEBOUNCE_DELAY = 300; // milliseconds

    // Force ẩn kết quả tìm kiếm khi trang mới load
    searchResults.style.display = 'none';
    searchInput.value = '';
    
    // Ngăn không cho form submit khi nhấn Enter
    if (searchForm) {
        searchForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const keyword = searchInput.value.trim();
            if (keyword) {
                fetchSearchResults(keyword);
            } else {
                hideSearchResults();
            }
        });
    }

    // XỬ LÝ KHI NGƯỜI DÙNG NHẬP TỪ KHÓA
    searchInput.addEventListener('input', function() {
        const keyword = this.value.trim();
        console.log("Input event, keyword:", keyword);
        
        // Xóa bỏ timer trước đó
        clearTimeout(debounceTimer);

        // Ẩn kết quả nếu từ khóa rỗng
        if (!keyword) {
            hideSearchResults();
            return;
        }

        // Hiển thị trạng thái đang tải
        searchResults.innerHTML = '<div class="search-loading">Đang tìm kiếm...</div>';
        searchResults.style.display = 'block';

        // Debounce để tránh gọi API quá nhiều
        debounceTimer = setTimeout(() => {
            fetchSearchResults(keyword);
        }, DEBOUNCE_DELAY);
    });

    // XỬ LÝ KHI CLICK NÚT TÌM KIẾM
    searchButton.addEventListener('click', function() {
        const keyword = searchInput.value.trim();
        if (keyword) {
            fetchSearchResults(keyword);
        } else {
            hideSearchResults();
        }
    });

    // XỬ LÝ KHI NHẤN ENTER
    searchInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            const keyword = this.value.trim();
            if (keyword) {
                fetchSearchResults(keyword);
            } else {
                hideSearchResults();
            }
        }
    });

    // XỬ LÝ KHI RỜI FOCUS KHỎI Ô TÌM KIẾM
    searchInput.addEventListener('blur', function(e) {
        // Trì hoãn để cho phép click vào kết quả
        setTimeout(() => {
            // Kiểm tra xem focus có đang ở trong kết quả tìm kiếm không
            if (!searchResults.contains(document.activeElement)) {
                // Kiểm tra xem ô input có trống không
                if (!searchInput.value.trim()) {
                    hideSearchResults();
                }
            }
        }, 200);
    });

    // XỬ LÝ KHI CLICK BẤT KỲ NƠI NÀO TRÊN TRANG
    document.addEventListener('click', function(e) {
        // Nếu click không phải vào vùng tìm kiếm
        if (!searchInput.contains(e.target) && 
            !searchResults.contains(e.target) && 
            !searchButton.contains(e.target)) {
            hideSearchResults();
        }
    });

    // HÀM ẨN KẾT QUẢ TÌM KIẾM
    function hideSearchResults() {
        console.log("Ẩn kết quả tìm kiếm");
        searchResults.classList.add('force-hide');
    }

    // HÀM GỌI API TÌM KIẾM
    async function fetchSearchResults(keyword) {
        // Nếu từ khóa rỗng, ẩn kết quả và dừng
        if (!keyword) {
            hideSearchResults();
            return;
        }
        
        console.log("Đang tìm kiếm cho từ khoá:", keyword);
        try {
            // Thêm timestamp để tránh cache
            const timestamp = new Date().getTime();
            const response = await fetch(`/api/search/products?keyword=${encodeURIComponent(keyword)}&_=${timestamp}`);
            
            // Kiểm tra nếu từ khóa đã thay đổi khi API đang xử lý
            if (searchInput.value.trim() !== keyword) {
                if (!searchInput.value.trim()) {
                    hideSearchResults();
                }
                return;
            }
            
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            
            // Đọc response
            const text = await response.text();
            if (!text || text.trim() === '') {
                throw new Error("Empty response from server");
            }
            
            // Parse JSON
            let data;
            try {
                data = JSON.parse(text);
            } catch (jsonError) {
                console.error("JSON parse error:", jsonError);
                throw new Error("Invalid JSON response");
            }
            
            // Kiểm tra lại từ khóa hiện tại
            if (searchInput.value.trim() !== keyword) {
                if (!searchInput.value.trim()) {
                    hideSearchResults();
                }
                return;
            }
            
            // Hiển thị kết quả
            displaySearchResults(data, keyword);
            
        } catch (error) {
            console.error('Error fetching search results:', error);
            // Chỉ hiển thị lỗi nếu từ khóa vẫn chưa thay đổi
            if (searchInput.value.trim() === keyword) {
                searchResults.innerHTML = `<div class="search-no-results">Lỗi: ${error.message}</div>`;
                searchResults.style.display = 'block';
            } else if (!searchInput.value.trim()) {
                hideSearchResults();
            }
        }
    }

    // HÀM HIỂN THỊ KẾT QUẢ TÌM KIẾM
    function displaySearchResults(results, keyword) {
        // Kiểm tra lại từ khóa hiện tại
        if (searchInput.value.trim() !== keyword) {
            if (!searchInput.value.trim()) {
                hideSearchResults();
            }
            return;
        }
        
        // Kiểm tra kết quả trống
        if (!results || results.length === 0) {
            searchResults.innerHTML = '<div class="search-no-results">Không tìm thấy sản phẩm phù hợp</div>';
            searchResults.classList.remove('force-hide');
            return;
        }

        let html = '';

        // Xử lý từng sản phẩm trong kết quả
        results.forEach(product => {
            // Đánh dấu từ khóa tìm kiếm trong tên sản phẩm
            const highlightedName = highlightKeyword(product.name, keyword);

            // Định dạng giá tiền
            const formattedPrice = formatCurrency(product.retailPrice);

            // Hiển thị thông tin cấu hình nếu có
            let specsInfo = '';
            if (product.cpu || product.storage) {
                specsInfo = `<div class="search-result-specs">`;
                if (product.cpu) specsInfo += `CPU: ${product.cpu} `;
                if (product.storage) specsInfo += `| Bộ nhớ: ${product.storage}`;
                specsInfo += `</div>`;
            }

            // Đảm bảo productId là đúng (thử tất cả các trường có thể)
            const productId = product.productId || product.productID || '';

            html += `
                <div class="search-result-item" data-product-id="${productId}">
                    <img src="${product.imageUrl || '/img/no-image-product.png'}" alt="${product.name}" class="search-result-image">
                    <div class="search-result-info">
                        <div class="search-result-name">${highlightedName}</div>
                        ${specsInfo}
                        <div>
                            <span class="search-result-price">${formattedPrice}</span>                      
                        </div>
                    </div>
                </div>
            `;
        });

        // Thêm nút xem tất cả kết quả
        html += `<div class="search-view-all">Xem tất cả sản phẩm</div>`;

        // Cập nhật DOM và hiển thị kết quả
        searchResults.innerHTML = html;
        searchResults.classList.remove('force-hide');

        // THÊM SỰ KIỆN CHO CÁC KẾT QUẢ
        
        // Thêm sự kiện click vào item
        document.querySelectorAll('.search-result-item').forEach(item => {
            item.addEventListener('click', function() {
                const productId = this.getAttribute('data-product-id');
                if (productId) {
                    window.location.href = `/dashboard/products/detail/${productId}`;
                }
            });
        });

        // Thêm sự kiện xem tất cả kết quả
        const viewAllBtn = document.querySelector('.search-view-all');
        if (viewAllBtn) {
            viewAllBtn.addEventListener('click', function() {
                window.location.href = `/dashboard/products/list`;
            });
        }
    }

    // CÁC HÀM TIỆN ÍCH

    // Hàm đánh dấu từ khóa trong văn bản
    function highlightKeyword(text, keyword) {
        if (!keyword || !text) return text;
        const regex = new RegExp(`(${escapeRegExp(keyword)})`, 'gi');
        return text.replace(regex, '<span class="search-highlight">$1</span>');
    }

    // Hàm định dạng tiền tệ
    function formatCurrency(amount) {
        if (!amount) return '0₫';
        return new Intl.NumberFormat('vi-VN').format(amount) + ' VND';
    }

    // Hàm escape các ký tự đặc biệt trong regex
    function escapeRegExp(string) {
        return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    }
});

// Xóa sự kiện cũ (nếu có)
document.getElementById('search-input').outerHTML = document.getElementById('search-input').outerHTML;

// Gán sự kiện mới
setTimeout(() => {
    const searchInput = document.getElementById('search-input');
    const searchResults = document.getElementById('search-results');
    
    // Tạo phần tử search-results nếu không tồn tại
    if (!searchResults) {
        const div = document.createElement('div');
        div.id = 'search-results';
        div.className = 'search-results force-hide';
        div.style.position = 'absolute';
        div.style.zIndex = '9999';
        div.style.background = 'white';
        div.style.width = '100%';
        div.style.boxShadow = '0 0 10px rgba(0,0,0,0.2)';
        searchInput.parentNode.appendChild(div);
    }
    
    // Hàm ẩn kết quả tìm kiếm
    function hideSearchResults() {
        searchResults.classList.add('force-hide');
    }
    
    searchInput.addEventListener('input', function() {
        const keyword = this.value.trim();
        if (keyword.length < 2) {
            hideSearchResults();
            return;
        }
        
        searchResults.innerHTML = '<div style="padding:10px;">Đang tìm kiếm...</div>';
        searchResults.classList.remove('force-hide');
        
        fetch(`/api/search/products?keyword=${encodeURIComponent(keyword)}`)
            .then(response => response.json())
            .then(data => {
                console.log('Kết quả:', data);
                
                // Kiểm tra lại từ khóa hiện tại, nếu đã xóa thì ẩn kết quả
                if (searchInput.value.trim() === '') {
                    hideSearchResults();
                    return;
                }
                
                if (!data || data.length === 0) {
                    searchResults.innerHTML = '<div style="padding:10px;">Không tìm thấy kết quả</div>';
                    return;
                }
                
                let html = '';
                data.forEach(item => {
                    html += `
                    <div style="padding:10px; border-bottom:1px solid #eee; display:flex; align-items:center; cursor:pointer;" class="search-result-item" data-product-id="${item.productId || ''}">
                        <img src="${item.imageUrl}" alt="" style="width:40px; height:40px; margin-right:10px;">
                        <div>
                            <div style="font-weight:bold;">${item.name}</div>
                            <div style="color:#d82027;">${new Intl.NumberFormat('vi-VN').format(item.retailPrice)} VND</div>
                        </div>
                    </div>`;
                });
                
                searchResults.innerHTML = html;
                
                // Thêm sự kiện click vào kết quả tìm kiếm
                document.querySelectorAll('.search-result-item').forEach(item => {
                    item.addEventListener('click', function() {
                        const productId = this.getAttribute('data-product-id');
                        if (productId) {
                            window.location.href = `/dashboard/products/detail/${productId}`;
                        }
                    });
                });
            })
            .catch(error => {
                console.error('Lỗi:', error);
                searchResults.innerHTML = '<div style="padding:10px;">Lỗi khi tìm kiếm</div>';
            });
    });
    
    // Xử lý khi blur khỏi ô input
    searchInput.addEventListener('blur', function() {
        // Trì hoãn để cho phép click vào kết quả
        setTimeout(() => {
            // Kiểm tra xem focus có đang ở trong kết quả tìm kiếm không
            if (!searchResults.contains(document.activeElement)) {
                hideSearchResults();
            }
        }, 200);
    });
    
    // Xử lý click ra ngoài
    document.addEventListener('click', function(e) {
        // Nếu click không phải vào vùng tìm kiếm
        if (!searchInput.contains(e.target) &&
            !searchResults.contains(e.target)) {
            hideSearchResults();
        }
    });
}, 1000);
