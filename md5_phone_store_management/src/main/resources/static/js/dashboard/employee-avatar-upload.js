// File xử lý upload avatar nhân viên
document.addEventListener('DOMContentLoaded', function() {
    const avatarInput = document.getElementById('avatarFile');
    const avatarPreview = document.querySelector('.avatar-preview');
    
    if (!avatarInput) return;
    
    avatarInput.addEventListener('change', function(e) {
        const file = e.target.files[0];
        if (!file) return;
        
        // Kiểm tra kích thước file (tối đa 10MB)
        if (file.size > 10 * 1024 * 1024) {
            showToast('error', 'Kích thước file quá lớn. Tối đa 10MB');
            avatarInput.value = '';
            return;
        }
        
        // Kiểm tra định dạng file
        if (!file.type.match('image.*')) {
            showToast('error', 'Chỉ chấp nhận file ảnh (JPEG, PNG, GIF)');
            avatarInput.value = '';
            return;
        }
        
        // Tạo overlay đang tải
        createLoadingOverlay(avatarPreview);
        
        // Hiển thị ảnh xem trước
        const reader = new FileReader();
        reader.onload = function(event) {
            // Tạo một đối tượng Image để xác nhận rằng ảnh đã tải xong
            const img = new Image();
            img.onload = function() {
                // Xóa overlay đang tải sau khi ảnh đã tải xong
                setTimeout(removeLoadingOverlay, 500); // Thêm độ trễ nhỏ để hiệu ứng rõ ràng hơn
            };
            img.src = event.target.result;
            
            // Cập nhật ảnh xem trước
            avatarPreview.setAttribute('src', event.target.result);
            
            // Hiển thị thông báo thành công
            showToast('success', 'Ảnh đại diện đã được tải lên thành công');
        };
        
        reader.onerror = function() {
            removeLoadingOverlay();
            showToast('error', 'Lỗi khi đọc file. Vui lòng thử lại');
        };
        
        reader.readAsDataURL(file);
    });
    
    // Hàm tạo overlay đang tải
    function createLoadingOverlay(element) {
        // Kiểm tra nếu đã có overlay thì không tạo mới
        if (document.querySelector('.loading-overlay')) {
            return;
        }
        
        // Tạo overlay
        const overlay = document.createElement('div');
        overlay.className = 'loading-overlay';
        overlay.style.position = 'absolute';
        overlay.style.top = '0';
        overlay.style.left = '0';
        overlay.style.width = '100%';
        overlay.style.height = '100%';
        overlay.style.backgroundColor = 'rgba(255, 255, 255, 0.7)';
        overlay.style.display = 'flex';
        overlay.style.alignItems = 'center';
        overlay.style.justifyContent = 'center';
        overlay.style.zIndex = '9999';
        
        // Tạo spinner
        const spinner = document.createElement('div');
        spinner.className = 'spinner-border text-primary';
        spinner.setAttribute('role', 'status');
        
        // Tạo text
        const loadingText = document.createElement('span');
        loadingText.className = 'ms-2 text-primary';
        loadingText.textContent = 'Đang tải ảnh...';
        
        // Tạo container cho spinner và text
        const container = document.createElement('div');
        container.className = 'd-flex flex-column align-items-center';
        container.appendChild(spinner);
        container.appendChild(loadingText);
        
        overlay.appendChild(container);
        
        // Thêm overlay vào container của ảnh
        const parent = element.parentElement;
        parent.style.position = 'relative';
        parent.appendChild(overlay);
    }
    
    // Hàm xóa overlay đang tải
    function removeLoadingOverlay() {
        const overlay = document.querySelector('.loading-overlay');
        if (overlay) {
            overlay.remove();
        }
    }
    
    // Hàm hiển thị thông báo
    function showToast(type, message) {
        if (typeof window.showToastMessage === 'function') {
            window.showToastMessage(type, message);
        } else {
            console.log(`[${type}] ${message}`);
        }
    }
}); 