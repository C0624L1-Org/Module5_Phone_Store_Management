// File xử lý upload ảnh sản phẩm
document.addEventListener('DOMContentLoaded', function() {
    const imageInput = document.getElementById('imgProducts');

    if (!imageInput) return;

    // Biến để kiểm tra form đã được submit hay chưa
    let isSubmitting = false;

    // Tìm form
    const form = imageInput.closest('form');
    if (form) {
        // Ghi đè lên phương thức submit của form để ngăn submit nhiều lần
        const originalSubmit = form.submit;
        form.submit = function() {
            console.log("Form submit override gọi");
            if (isSubmitting) {
                console.log("Đã submit rồi, bỏ qua");
                return false;
            }

            console.log("Submit form");
            isSubmitting = true;

            // Disable tất cả các nút submit
            disableAllSubmitButtons();

            // Call original form submit
            return originalSubmit.apply(this, arguments);
        };

        // Xử lý tất cả các nút submit
        const confirmCreateButton = document.getElementById('confirmCreate');
        const confirmUpdateButton = document.getElementById('confirmUpdate');

        if (confirmCreateButton) {
            console.log("Đã tìm thấy nút confirmCreate");
            const originalClickHandler = confirmCreateButton.onclick;
            confirmCreateButton.onclick = function(e) {
                if (isSubmitting) {
                    console.log("Đang submit, ngăn chặn click thêm");
                    e.preventDefault();
                    e.stopPropagation();
                    return false;
                }

                console.log("Nhấn nút tạo mới");
                isSubmitting = true;
                disableAllSubmitButtons();

                // Thêm delay để đảm bảo trang web có đủ thời gian phản ứng
                setTimeout(function() {
                    if (typeof originalClickHandler === 'function') {
                        console.log("Gọi handler gốc");
                        originalClickHandler.call(confirmCreateButton, e);
                    } else {
                        console.log("Gọi submit trực tiếp");
                        form.submit();
                    }
                }, 10);
            };
        }

        if (confirmUpdateButton) {
            console.log("Đã tìm thấy nút confirmUpdate");
            const originalClickHandler = confirmUpdateButton.onclick;
            confirmUpdateButton.onclick = function(e) {
                if (isSubmitting) {
                    console.log("Đang submit, ngăn chặn click thêm");
                    e.preventDefault();
                    e.stopPropagation();
                    return false;
                }

                console.log("Nhấn nút cập nhật");
                isSubmitting = true;
                disableAllSubmitButtons();

                // Thêm delay để đảm bảo trang web có đủ thời gian phản ứng
                setTimeout(function() {
                    if (typeof originalClickHandler === 'function') {
                        console.log("Gọi handler gốc");
                        originalClickHandler.call(confirmUpdateButton, e);
                    } else {
                        console.log("Gọi submit trực tiếp");
                        form.submit();
                    }
                }, 10);
            };
        }
    }

    function disableAllSubmitButtons() {
        document.querySelectorAll('button[type="submit"], button#confirmCreate, button#confirmUpdate, button[data-submit-form]').forEach(button => {
            button.disabled = true;
            if (button.classList.contains('btn-success')) {
                button.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Đang xử lý...';
            }
        });
    }

    // Tạo container cho ảnh xem trước nếu chưa có
    let previewContainer = document.getElementById('imagePreviewContainer');
    if (!previewContainer) {
        previewContainer = document.createElement('div');
        previewContainer.id = 'imagePreviewContainer';
        previewContainer.className = 'd-flex flex-wrap gap-2 mt-3';
        previewContainer.style.position = 'relative';

        const parentElement = imageInput.parentElement;
        parentElement.appendChild(previewContainer);
    }

    // Tạo thông báo số ảnh đã chọn
    let imageCountInfo = document.getElementById('imageCountInfo');
    if (!imageCountInfo) {
        imageCountInfo = document.createElement('div');
        imageCountInfo.id = 'imageCountInfo';
        imageCountInfo.className = 'mt-2 small text-info';
        imageCountInfo.style.display = 'none';

        const parentElement = imageInput.parentElement;
        parentElement.insertBefore(imageCountInfo, previewContainer);
    }

    // Kiểm tra nếu đang ở trang cập nhật sản phẩm
    const isUpdatePage = window.location.href.includes('/update-form/');
    let currentImagesContainer = null;

    if (isUpdatePage) {
        // Tìm container hiển thị ảnh hiện tại
        currentImagesContainer = imageInput.parentElement.querySelector('.d-flex.flex-wrap');

        // Thêm tiêu đề cho xem trước ảnh mới
        const previewTitle = document.createElement('h6');
        previewTitle.className = 'fw-bold mt-3 mb-2';
        previewTitle.textContent = 'Ảnh mới (xem trước):';

        const parentElement = imageInput.parentElement;
        parentElement.insertBefore(previewTitle, previewContainer);
    }

    imageInput.addEventListener('change', function(e) {
        const files = e.target.files;
        if (!files || files.length === 0) return;

        // Hiển thị số ảnh đã chọn
        imageCountInfo.textContent = `Đã chọn ${files.length} ảnh`;
        imageCountInfo.style.display = 'block';

        // Nếu là trang cập nhật, thêm thông báo cảnh báo
        if (isUpdatePage && currentImagesContainer) {
            const warningMsg = document.getElementById('imageReplaceWarning');
            if (!warningMsg) {
                const warning = document.createElement('div');
                warning.id = 'imageReplaceWarning';
                warning.className = 'alert alert-warning mt-2';
                warning.innerHTML = '<i class="fas fa-exclamation-triangle me-2"></i>Lưu ý: Ảnh mới sẽ <strong>thay thế hoàn toàn</strong> ảnh cũ khi bạn cập nhật.';

                const parentElement = imageInput.parentElement;
                parentElement.insertBefore(warning, previewContainer.previousSibling);
            }
        }

        // Xóa tất cả ảnh xem trước cũ
        previewContainer.innerHTML = '';

        // Tạo overlay đang tải
        createLoadingOverlay(previewContainer);

        // Số ảnh đã tải
        let loadedImages = 0;
        let errorImages = 0;

        // Xử lý từng file ảnh
        Array.from(files).forEach(file => {
            // Kiểm tra kích thước file (tối đa 10MB)
            if (file.size > 10 * 1024 * 1024) {
                errorImages++;
                updateLoadingStatus(files.length, loadedImages, errorImages);
                return;
            }

            // Kiểm tra định dạng file
            if (!file.type.match('image.*')) {
                errorImages++;
                updateLoadingStatus(files.length, loadedImages, errorImages);
                return;
            }

            // Tạo thumbnail cho mỗi ảnh
            const reader = new FileReader();
            reader.onload = function(event) {
                // Tạo container cho thumbnail
                const imgContainer = document.createElement('div');
                imgContainer.className = 'image-preview-item';
                imgContainer.style.width = '150px';
                imgContainer.style.height = '150px';
                imgContainer.style.overflow = 'hidden';
                imgContainer.style.border = '1px solid #dee2e6';
                imgContainer.style.borderRadius = '0.25rem';
                imgContainer.style.position = 'relative';

                // Tạo ảnh thumbnail
                const img = document.createElement('img');
                img.className = 'img-thumbnail w-100 h-100';
                img.style.objectFit = 'cover';
                img.alt = file.name;

                // Xử lý sự kiện khi ảnh tải xong
                img.onload = function() {
                    loadedImages++;
                    updateLoadingStatus(files.length, loadedImages, errorImages);

                    // Nếu tất cả ảnh đã tải xong, xóa overlay
                    if (loadedImages + errorImages === files.length) {
                        setTimeout(function() {
                            removeLoadingOverlay();
                            showToast('success', `Đã tải lên ${loadedImages} ảnh thành công` +
                                     (errorImages > 0 ? ` (${errorImages} ảnh lỗi)` : ''));
                        }, 500);
                    }
                };

                img.src = event.target.result;

                // Thêm vào container
                imgContainer.appendChild(img);
                previewContainer.appendChild(imgContainer);
            };

            reader.onerror = function() {
                errorImages++;
                updateLoadingStatus(files.length, loadedImages, errorImages);
                showToast('error', 'Lỗi khi đọc file: ' + file.name);
            };

            reader.readAsDataURL(file);
        });
    });

    // Hàm cập nhật trạng thái đang tải
    function updateLoadingStatus(total, loaded, errors) {
        const overlay = document.querySelector('.loading-overlay');
        if (overlay) {
            const loadingText = overlay.querySelector('span');
            if (loadingText) {
                loadingText.textContent = `Đang tải ảnh... ${loaded + errors}/${total}`;
            }
        }
    }

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
        overlay.style.minHeight = '150px';

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

        // Thêm overlay vào container
        element.style.position = 'relative';
        element.appendChild(overlay);
    }

    // Hàm xóa overlay đang tải
    function removeLoadingOverlay() {
        const overlay = document.querySelector('.loading-overlay');
        if (overlay) {
            overlay.remove();
        }
    }

    // Hàm hiển thị thông báo (nếu không có sẵn hàm showToast)
    function showToast(type, message) {
        if (typeof window.showToastMessage === 'function') {
            window.showToastMessage(type, message);
        } else {
            console.log(`[${type}] ${message}`);
        }
    }
});