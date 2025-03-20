document.addEventListener('DOMContentLoaded', function() {
    // Handle direct payment with VNPAY
    const handleVnpayDirectPayment = async (orderId, amount, orderInfo) => {
        try {
            const response = await fetch('/api/vnpay/create-payment', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ orderId, amount, orderInfo })
            });

            if (response.ok) {
                const paymentUrl = await response.text();
                window.location.href = paymentUrl;
            } else {
                alert('Lỗi khi tạo yêu cầu thanh toán VNPAY');
            }
        } catch (error) {
            console.error('Error creating payment:', error);
            alert('Lỗi khi xử lý thanh toán: ' + error.message);
        }
    };

    window.handleVnpayPayment = handleVnpayDirectPayment;
});
