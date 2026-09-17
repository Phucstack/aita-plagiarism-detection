(() => {
    const root = document.getElementById('google-login');
    if (!root) return;
    const message = document.getElementById('google-login-message');
    const button = document.getElementById('google-login-btn');
    async function initialize() {
        button.disabled = true;
        message.textContent = 'Đang kết nối Google…';
        try {
            const response = await fetch(root.dataset.endpoint, {credentials: 'same-origin', cache: 'no-store'});
            if (!response.ok) throw new Error(response.status === 503
                ? 'Chưa có Google Client ID. Cần cấu hình GOOGLE_CLIENT_ID để đăng nhập.'
                : 'Không thể khởi tạo đăng nhập Google. Vui lòng thử lại.');
            const config = await response.json();
            if (!window.google?.accounts?.id) {
                await new Promise((resolve, reject) => {
                    const script = document.createElement('script');
                    script.src = 'https://accounts.google.com/gsi/client';
                    script.async = true;
                    script.onload = resolve;
                    script.onerror = () => reject(new Error('Không tải được Google Identity Services. Kiểm tra kết nối mạng.'));
                    document.head.appendChild(script);
                });
            }
            google.accounts.id.initialize({client_id: config.clientId, nonce: config.nonce,
                auto_select: false, callback: result => {
                    message.textContent = 'Đang xác minh tài khoản Google…';
                    const form = document.createElement('form');
                    form.method = 'POST'; form.action = root.dataset.endpoint;
                    for (const [name, value] of Object.entries({credential: result.credential, state: config.nonce})) {
                        const input = document.createElement('input');
                        input.type = 'hidden'; input.name = name; input.value = value; form.appendChild(input);
                    }
                    document.body.appendChild(form); form.submit();
                }});
            document.getElementById('google-identity-button').replaceChildren();
            google.accounts.id.renderButton(document.getElementById('google-identity-button'), {
                type: 'standard', theme: 'outline', size: 'large', text: 'signin_with', locale: 'vi'
            });
            button.hidden = true;
            message.textContent = 'Dùng email đã được cấp tài khoản trong hệ thống.';
        } catch (error) {
            message.textContent = error.message;
            button.textContent = 'Thử lại Google login'; button.disabled = false;
        }
    }
    button.addEventListener('click', initialize);
    initialize();
})();
