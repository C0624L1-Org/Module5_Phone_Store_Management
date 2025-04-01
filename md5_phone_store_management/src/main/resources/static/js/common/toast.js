// Hàm hiển thị toast message dùng template từ toast.html
window.showToastMessage = function(type, message) {
    // Tìm container
    const container = document.querySelector('.toast-container');
    if (!container) return;
    
    // Tìm và clone template
    const template = document.getElementById('toast-template');
    if (!template) return;
    
    const toast = template.cloneNode(true);
    toast.id = '';
    toast.classList.remove('d-none');
    toast.classList.add('show', type);
    
    // Đặt icon
    const icon = toast.querySelector('.toast-icon');
    if (icon) {
        if (type === 'success') {
            icon.className = 'fa-solid fa-circle-check toast-icon';
        } else {
            icon.className = 'fa-solid fa-circle-xmark toast-icon';
        }
    }
    
    // Đặt tiêu đề
    const title = toast.querySelector('.toast-title');
    if (title) {
        title.innerHTML = `<strong>${type === 'success' ? 'Thành Công' : 'Lỗi'}</strong>`;
    }
    
    // Đặt nội dung
    const messageEl = toast.querySelector('.toast-message');
    if (messageEl) {
        messageEl.textContent = message;
    }
    
    // Thêm vào container
    container.appendChild(toast);
    
    // Xử lý nút đóng
    const closeBtn = toast.querySelector('.close-btn');
    if (closeBtn) {
        closeBtn.addEventListener('click', function() {
            toast.remove();
        });
    }
    
    // Tự động xóa sau 3 giây
    setTimeout(() => {
        toast.remove();
    }, 3000);
};

// Ghi đè hàm showToast trong sales.js
if (typeof showToast === 'function') {
    const originalShowToast = showToast;
    window.showToast = function(type, message) {
        if (window.showToastMessage) {
            window.showToastMessage(type, message);
        } else {
            originalShowToast(type, message);
        }
    };
}