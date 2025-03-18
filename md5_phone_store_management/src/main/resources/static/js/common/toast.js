document.addEventListener('DOMContentLoaded', function () {
    const toastElList = document.querySelectorAll('.toast');
    toastElList.forEach(toastEl => {
        toastEl.style.display = 'flex';

        setTimeout(() => {
            toastEl.classList.add('fade-out');
        }, 3000);

        setTimeout(() => {
            toastEl.style.display = 'none';
        }, 6000);

        toastEl.querySelector('.close-btn').addEventListener('click', () => {
            toastEl.style.display = 'none';
        });
    });
});