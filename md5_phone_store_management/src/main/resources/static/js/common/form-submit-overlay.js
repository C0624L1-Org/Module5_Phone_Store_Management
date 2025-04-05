// File xử lý hiển thị overlay loading khi submit form
document.addEventListener('DOMContentLoaded', function() {
    // Tìm tất cả các form cần xử lý
    const forms = document.querySelectorAll('form[data-loading-overlay="true"]');
    
    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            // Không hiển thị overlay nếu form không hợp lệ
            if (!form.checkValidity()) {
                return;
            }
            
            // Đóng tất cả các modal đang mở
            closeAllModals();
            
            // Hiển thị overlay loading
            showSubmitLoadingOverlay();
        });
        
        // Xử lý các nút submit custom (như các nút xác nhận trong modal)
        const formId = form.id || '';
        const customSubmitButtons = document.querySelectorAll(`[data-submit-form="${formId}"]`);
        
        customSubmitButtons.forEach(button => {
            button.addEventListener('click', function() {
                // Kiểm tra form hợp lệ trước khi hiển thị overlay
                if (form.checkValidity()) {
                    // Đóng tất cả các modal đang mở
                    closeAllModals();
                    
                    // Hiển thị overlay loading
                    showSubmitLoadingOverlay();

                    setTimeout(() => {
                        form.submit();
                    }, 100);
                } else {
                    // Kích hoạt validation của form
                    const submitEvent = new Event('submit', {
                        bubbles: true,
                        cancelable: true
                    });
                    form.dispatchEvent(submitEvent);
                }
            });
        });
    });
    
    // Hàm đóng tất cả các modal
    function closeAllModals() {
        // Tìm tất cả các modal đang mở
        const openModals = document.querySelectorAll('.modal.show');
        
        openModals.forEach(modal => {
            if (typeof bootstrap !== 'undefined') {
                const bsModal = bootstrap.Modal.getInstance(modal);
                if (bsModal) {
                    bsModal.hide();
                }
            } else {
                // Thêm class 'closing' để áp dụng hiệu ứng đóng
                modal.classList.add('closing');
                
                // Xóa class 'show' và 'closing' sau khi đã hoàn tất transition
                setTimeout(() => {
                    modal.classList.remove('show');
                    modal.classList.remove('closing');
                    
                    // Xóa modal backdrop nếu có
                    const backdrop = document.querySelector('.modal-backdrop');
                    if (backdrop) {
                        backdrop.remove();
                    }
                    
                    // Đặt lại style của body
                    document.body.style.overflow = '';
                    document.body.classList.remove('modal-open');
                }, 150);
            }
        });
    }
    
    // Hàm hiển thị overlay loading
    function showSubmitLoadingOverlay() {
        // Kiểm tra nếu đã có overlay thì không tạo mới
        if (document.querySelector('.submit-loading-overlay')) {
            return;
        }

        // Tạo style cho animations
        const styleElement = document.createElement('style');
        styleElement.textContent = `
            @keyframes spin {
                0% { transform: rotate(0deg); }
                100% { transform: rotate(360deg); }
            }
            @keyframes pulse {
                0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(0, 123, 255, 0.5); }
                70% { transform: scale(1); box-shadow: 0 0 0 15px rgba(0, 123, 255, 0); }
                100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(0, 123, 255, 0); }
            }
            @keyframes gradient {
                0% { background-position: 0% 50%; }
                50% { background-position: 100% 50%; }
                100% { background-position: 0% 50%; }
            }
            @keyframes shimmer {
                0% { background-position: -500px 0; }
                100% { background-position: 500px 0; }
            }
            .submit-loading-container {
                animation: pulse 2s infinite;
            }
            .loading-spinner {
                animation: spin 1s linear infinite;
            }
            .gradient-progress {
                background: linear-gradient(90deg, #007bff, #00c3ff, #00e4ff, #00c3ff, #007bff);
                background-size: 500% 100%;
                animation: gradient 3s ease infinite;
            }
            .shimmer-overlay {
                position: absolute;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background: linear-gradient(90deg, 
                    rgba(255,255,255,0) 0%, 
                    rgba(255,255,255,0.2) 50%, 
                    rgba(255,255,255,0) 100%);
                background-size: 200% 100%;
                animation: shimmer 2s infinite;
            }
        `;
        document.head.appendChild(styleElement);
        
        // Tạo overlay chính
        const overlay = document.createElement('div');
        overlay.className = 'submit-loading-overlay';
        overlay.style.position = 'fixed';
        overlay.style.top = '0';
        overlay.style.left = '0';
        overlay.style.width = '100%';
        overlay.style.height = '100%';
        overlay.style.backgroundColor = 'rgba(0, 0, 0, 0.7)';
        overlay.style.display = 'flex';
        overlay.style.alignItems = 'center';
        overlay.style.justifyContent = 'center';
        overlay.style.zIndex = '9999';
        overlay.style.backdropFilter = 'blur(5px)';
        
        // Tạo container cho spinner và text
        const container = document.createElement('div');
        container.className = 'submit-loading-container d-flex flex-column align-items-center bg-white p-4 rounded-lg shadow-lg';
        container.style.minWidth = '250px';
        container.style.maxWidth = '350px';
        container.style.borderRadius = '15px';
        container.style.background = 'rgba(255, 255, 255, 0.95)';
        container.style.backdropFilter = 'blur(10px)';
        container.style.boxShadow = '0 10px 25px rgba(0, 0, 0, 0.2)';
        container.style.border = '1px solid rgba(255, 255, 255, 0.3)';
        container.style.position = 'relative';
        container.style.overflow = 'hidden';
        
        // Tạo hiệu ứng shimmer overlay
        const shimmerEffect = document.createElement('div');
        shimmerEffect.className = 'shimmer-overlay';
        container.appendChild(shimmerEffect);
        
        // Tạo icon
        const iconContainer = document.createElement('div');
        iconContainer.style.width = '80px';
        iconContainer.style.height = '80px';
        iconContainer.style.borderRadius = '50%';
        iconContainer.style.display = 'flex';
        iconContainer.style.alignItems = 'center';
        iconContainer.style.justifyContent = 'center';
        iconContainer.style.background = 'linear-gradient(145deg, #ffffff, #f0f0f0)';
        iconContainer.style.boxShadow = '8px 8px 16px #d1d1d1, -8px -8px 16px #ffffff';
        iconContainer.style.margin = '0 auto 15px auto';
        
        // Tạo spinner
        const spinner = document.createElement('div');
        spinner.className = 'loading-spinner';
        spinner.style.width = '50px';
        spinner.style.height = '50px';
        spinner.style.border = '5px solid rgba(0, 123, 255, 0.2)';
        spinner.style.borderRadius = '50%';
        spinner.style.borderTop = '5px solid #007bff';
        
        iconContainer.appendChild(spinner);
        
        // Tạo text
        const loadingText = document.createElement('div');
        loadingText.className = 'mt-3 fw-bold text-center';
        loadingText.textContent = 'Đang xử lý dữ liệu...';
        loadingText.style.fontSize = '1.2rem';
        loadingText.style.color = '#2c3e50';
        loadingText.style.marginBottom = '15px';
        loadingText.style.fontWeight = '600';
        
        // Tạo subtext
        const loadingSubtext = document.createElement('div');
        loadingSubtext.className = 'text-center mb-3';
        loadingSubtext.textContent = 'Vui lòng đợi trong giây lát';
        loadingSubtext.style.fontSize = '0.9rem';
        loadingSubtext.style.color = '#7f8c8d';
        
        // Thêm progress bar
        const progressContainer = document.createElement('div');
        progressContainer.className = 'w-100 mt-2';
        progressContainer.style.height = '6px';
        progressContainer.style.backgroundColor = 'rgba(0, 123, 255, 0.1)';
        progressContainer.style.borderRadius = '10px';
        progressContainer.style.overflow = 'hidden';
        progressContainer.style.margin = '10px 0';
        
        const progressBar = document.createElement('div');
        progressBar.className = 'gradient-progress';
        progressBar.style.height = '100%';
        progressBar.style.width = '0%';
        progressBar.style.transition = 'width 1s cubic-bezier(0.65, 0, 0.35, 1)';
        
        progressContainer.appendChild(progressBar);
        
        // Tạo thời gian xử lý
        const processingTime = document.createElement('div');
        processingTime.className = 'text-center mb-1 processing-time';
        processingTime.textContent = '00:00';
        processingTime.style.fontSize = '0.85rem';
        processingTime.style.color = '#95a5a6';
        processingTime.style.fontFamily = 'monospace';
        
        // Ghép các thành phần lại với nhau
        container.appendChild(iconContainer);
        container.appendChild(loadingText);
        container.appendChild(loadingSubtext);
        container.appendChild(progressContainer);
        container.appendChild(processingTime);
        overlay.appendChild(container);
        
        // Thêm overlay vào body
        document.body.appendChild(overlay);
        
        // Hiệu ứng progress bar tăng dần
        let progress = 0;
        let seconds = 0;
        
        // Hiệu ứng progress ban đầu tăng dến 30%
        setTimeout(() => {
            progressBar.style.width = '30%';
        }, 200);
        
        // Sau đó tiếp tục tăng đến 60%
        setTimeout(() => {
            progressBar.style.width = '60%';
        }, 800);
        
        // Cuối cùng tăng đến 85%
        setTimeout(() => {
            progressBar.style.width = '85%';
        }, 2000);
        
        // Cập nhật thời gian xử lý
        const timer = setInterval(() => {
            seconds++;
            const mins = Math.floor(seconds / 60);
            const secs = seconds % 60;
            processingTime.textContent = `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
            
            // Nếu thời gian xử lý quá 10 giây, hiển thị thông báo bổ sung
            if (seconds === 10) {
                loadingSubtext.textContent = 'Đang xử lý dữ liệu lớn...';
                loadingSubtext.style.color = '#e67e22';
            }
        }, 1000);
        
        // Lưu timer vào attribute của overlay để xóa khi cần
        overlay.setAttribute('data-timer', timer);
    }
    
    // Thêm phương thức để xóa overlay khi cần
    window.removeSubmitLoadingOverlay = function() {
        const overlay = document.querySelector('.submit-loading-overlay');
        if (overlay) {
            // Dừng timer
            clearInterval(overlay.getAttribute('data-timer'));
            // Xóa overlay
            overlay.remove();
        }
    };
}); 