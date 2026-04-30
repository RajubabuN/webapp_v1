// Cart JavaScript functionality
(function() {
    'use strict';

    // CSRF token for AJAX requests
    const csrfToken = document.querySelector('input[name="_csrf"]')?.value;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';

    // Simple functions without loading indicators
    function showLoading() {
        hideMessages();
    }

    function hideLoading() {
    }

    function hideMessages() {
    }

    // Success message function (referenced in other functions)
    function showSuccess(message) {
        console.log('Success:', message);
    }

    // AJAX cart update function
    async function updateCartAjax(productId, operator) {
        showLoading();

        const formData = new FormData();
        formData.append('productId', productId);
        formData.append('operator', operator);
        if (csrfToken) {
            formData.append('_csrf', csrfToken);
        }

        try {
            const response = await fetch('/updatecart', {
                method: 'POST',
                body: formData,
                headers: {
                    ...(csrfToken && { [csrfHeader]: csrfToken })
                }
            });

            if (response.ok) {
                // Reload the cart content
                await reloadCartContent();
                showSuccess('Cart updated successfully!');
            } else {
                throw new Error(`Server error: ${response.status}`);
            }
        } catch (error) {
            console.error('Error updating cart:', error);
        } finally {
            hideLoading();
        }
    }

    // AJAX remove from cart function
    async function removeCartAjax(productId) {
        showLoading();

        const formData = new FormData();
        formData.append('productId', productId);
        formData.append('operator', 'remove');
        if (csrfToken) {
            formData.append('_csrf', csrfToken);
        }

        try {
            const response = await fetch('/updatecart', {
                method: 'POST',
                body: formData,
                headers: {
                    ...(csrfToken && { [csrfHeader]: csrfToken })
                }
            });

            if (response.ok) {
                // Reload the cart content
                await reloadCartContent();
                showSuccess('Item removed from cart!');
            } else {
                throw new Error(`Server error: ${response.status}`);
            }
        } catch (error) {
            console.error('Error removing item:', error);
        } finally {
            hideLoading();
        }
    }

    // AJAX complete order function
    async function completeOrderAjax() {
        showLoading();

        const formData = new FormData();
        if (csrfToken) {
            formData.append('_csrf', csrfToken);
        }

        try {
            const response = await fetch('/completecart', {
                method: 'POST',
                body: formData,
                headers: {
                    ...(csrfToken && { [csrfHeader]: csrfToken })
                }
            });

            if (response.ok) {
                // Reload the cart content
                await reloadCartContent();
                showSuccess('Order completed successfully!');
            } else {
                throw new Error(`Server error: ${response.status}`);
            }
        } catch (error) {
            console.error('Error completing order:', error);
        } finally {
            hideLoading();
        }
    }

    // Reload cart content via AJAX
    async function reloadCartContent() {
        try {
            const response = await fetch('/cart', {
                method: 'GET',
                headers: {
                    'Accept': 'text/html',
                    ...(csrfToken && { [csrfHeader]: csrfToken })
                }
            });

            if (response.ok) {
                const html = await response.text();
                const parser = new DOMParser();
                const doc = parser.parseFromString(html, 'text/html');

                // Extract cart content from the response
                const newCartContent = doc.querySelector('#cart-content');
                if (newCartContent) {
                    document.getElementById('cart-content').innerHTML = newCartContent.innerHTML;
                }

                // Update cart count in header
                const newCartSize = doc.querySelector('.cartcount div');
                const currentCartCount = document.querySelector('.cartcount div');
                if (newCartSize && currentCartCount) {
                    currentCartCount.textContent = newCartSize.textContent;
                }
            }
        } catch (error) {
            console.error('Error reloading cart content:', error);
        }
    }

    // Fallback functions for non-AJAX users
    function updatecart(productId, operator) {
        // Fallback to form submission if AJAX fails
        document.getElementById('productId').value = productId;
        document.getElementById('operator').value = operator;
        document.getElementById('updatecart').submit();
    }

    function removecart(productId) {
        // Fallback to form submission if AJAX fails
        document.getElementById('productId').value = productId;
        document.getElementById('operator').value = 'remove';
        document.getElementById('updatecart').submit();
    }

    function completecart() {
        // Fallback to form submission if AJAX fails
        document.getElementById('completecart').submit();
    }

    // Add smooth animations for cart items
    function initCartAnimations() {
        // Add fade-in animation to cart items
        const cartItems = document.querySelectorAll('.cart-item');
        cartItems.forEach((item, index) => {
            item.style.opacity = '0';
            item.style.transform = 'translateY(20px)';
            setTimeout(() => {
                item.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
                item.style.opacity = '1';
                item.style.transform = 'translateY(0)';
            }, index * 100);
        });
    }

    // Initialize when DOM is loaded
    document.addEventListener('DOMContentLoaded', function() {
        initCartAnimations();
    });

    // Make functions globally available
    window.updateCartAjax = updateCartAjax;
    window.removeCartAjax = removeCartAjax;
    window.completeOrderAjax = completeOrderAjax;
    window.updatecart = updatecart;
    window.removecart = removecart;
    window.completecart = completecart;

})();