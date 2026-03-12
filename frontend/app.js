/* ========================================
   BANKING DASHBOARD — JavaScript Application
   ======================================== */

// ============ CONFIG ============
const API_BASE = 'http://localhost:8080/api';
const TOKEN_KEY = 'banking_access_token';
const REFRESH_KEY = 'banking_refresh_token';
const USER_KEY = 'banking_user';

// ============ STATE ============
let currentUser = null;
let accounts = [];
let transactions = [];

// ============ INIT ============
document.addEventListener('DOMContentLoaded', () => {
    initForms();
    checkAuth();
    initTransferForm();
});

// ============ AUTH ============
function checkAuth() {
    const token = localStorage.getItem(TOKEN_KEY);
    const userStr = localStorage.getItem(USER_KEY);
    if (token && userStr) {
        currentUser = JSON.parse(userStr);
        showPage('dashboard-page');
        loadDashboard();
    } else {
        showPage('login-page');
    }
}

function initForms() {
    document.getElementById('login-form').addEventListener('submit', handleLogin);
    document.getElementById('register-form').addEventListener('submit', handleRegister);
    document.getElementById('transfer-form').addEventListener('submit', handleTransfer);
    document.getElementById('create-account-form').addEventListener('submit', handleCreateAccount);
}

async function handleLogin(e) {
    e.preventDefault();
    const btn = document.getElementById('login-btn');
    const errorEl = document.getElementById('login-error');
    setLoading(btn, true);
    hideError(errorEl);

    try {
        const data = await apiPost('/v1/auth/login', {
            email: document.getElementById('login-email').value,
            password: document.getElementById('login-password').value
        });

        localStorage.setItem(TOKEN_KEY, data.data.accessToken);
        localStorage.setItem(REFRESH_KEY, data.data.refreshToken);
        localStorage.setItem(USER_KEY, JSON.stringify(data.data.user));
        currentUser = data.data.user;

        showToast('Đăng nhập thành công!', 'success');
        showPage('dashboard-page');
        loadDashboard();
    } catch (err) {
        showError(errorEl, err.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại email và mật khẩu.');
    } finally {
        setLoading(btn, false);
    }
}

async function handleRegister(e) {
    e.preventDefault();
    const btn = document.getElementById('register-btn');
    const errorEl = document.getElementById('register-error');
    setLoading(btn, true);
    hideError(errorEl);

    try {
        const data = await apiPost('/v1/auth/register', {
            firstName: document.getElementById('reg-firstName').value,
            lastName: document.getElementById('reg-lastName').value,
            email: document.getElementById('reg-email').value,
            phoneNumber: document.getElementById('reg-phone').value,
            password: document.getElementById('reg-password').value
        });

        localStorage.setItem(TOKEN_KEY, data.data.accessToken);
        localStorage.setItem(REFRESH_KEY, data.data.refreshToken);
        localStorage.setItem(USER_KEY, JSON.stringify(data.data.user));
        currentUser = data.data.user;

        showToast('Đăng ký thành công! Chào mừng bạn đến với Premium Banking.', 'success');
        showPage('dashboard-page');
        loadDashboard();
    } catch (err) {
        showError(errorEl, err.message || 'Đăng ký thất bại. Vui lòng thử lại.');
    } finally {
        setLoading(btn, false);
    }
}

function logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_KEY);
    localStorage.removeItem(USER_KEY);
    currentUser = null;
    accounts = [];
    transactions = [];
    showPage('login-page');
    showToast('Đã đăng xuất', 'info');
}

// ============ DASHBOARD ============
async function loadDashboard() {
    if (!currentUser) return;
    updateUserUI();
    try {
        accounts = await apiGet('/v1/accounts');
        accounts = accounts.data || [];
        renderDashboardStats();
        renderDashboardAccounts();
        loadAccountTransactions();
    } catch (err) {
        console.error('Failed to load dashboard:', err);
    }
}

function updateUserUI() {
    const fullName = currentUser.fullName || `${currentUser.firstName} ${currentUser.lastName}`;
    const initials = (currentUser.firstName?.[0] || '') + (currentUser.lastName?.[0] || '');

    document.getElementById('greeting').textContent = `Xin chào, ${fullName} 👋`;

    // Update all sidebar user sections
    document.querySelectorAll('.user-name').forEach(el => el.textContent = fullName);
    document.querySelectorAll('.user-avatar').forEach(el => el.textContent = initials.toUpperCase());
}

function renderDashboardStats() {
    const totalBalance = accounts.reduce((sum, a) => sum + (a.balance || 0), 0);
    const activeCount = accounts.filter(a => a.status === 'ACTIVE').length;

    document.getElementById('total-balance').textContent = formatCurrency(totalBalance);
    document.getElementById('account-count').textContent = accounts.length;
    document.getElementById('active-accounts').textContent = activeCount;
}

function renderDashboardAccounts() {
    const container = document.getElementById('dashboard-accounts');
    if (!accounts.length) {
        container.innerHTML = '<p class="empty-state">Chưa có tài khoản. <a href="#" onclick="showCreateAccountModal()">Tạo ngay</a></p>';
        return;
    }
    container.innerHTML = accounts.slice(0, 3).map(acc => renderAccountCard(acc)).join('');
}

function renderAccountCard(acc) {
    return `
    <div class="account-card">
        <span class="acc-status status-${acc.status}">${acc.status}</span>
        <div class="acc-type">${acc.accountType} ACCOUNT</div>
        <div class="acc-number">${formatAccountNumber(acc.accountNumber)}</div>
        <div class="acc-bottom">
            <div class="acc-name">${acc.accountName || acc.ownerName}</div>
            <div class="acc-balance">
                <span>${acc.currency || 'VND'}</span>
                <strong>${formatCurrency(acc.balance)}</strong>
            </div>
        </div>
    </div>`;
}

async function loadAccountTransactions() {
    if (!accounts.length) return;
    try {
        const data = await apiGet(`/v1/transactions/account/${accounts[0].id}?size=5`);
        transactions = data.data?.content || [];
        renderDashboardTransactions();
        document.getElementById('recent-tx-count').textContent = transactions.length;
    } catch (err) {
        console.log('No transactions yet');
    }
}

function renderDashboardTransactions() {
    const container = document.getElementById('dashboard-transactions');
    if (!transactions.length) {
        container.innerHTML = '<p class="empty-state">Chưa có giao dịch nào</p>';
        return;
    }
    container.innerHTML = transactions.map(tx => `
    <div class="tx-item">
        <div class="tx-icon ${tx.type.toLowerCase()}">
            <span class="material-icons-outlined">${getTxIcon(tx.type)}</span>
        </div>
        <div class="tx-details">
            <div class="tx-type">${getTxLabel(tx.type)}</div>
            <div class="tx-date">${formatDate(tx.createdAt)} · ${tx.referenceNumber}</div>
        </div>
        <div class="tx-amount ${tx.type === 'DEPOSIT' ? 'positive' : 'negative'}">
            ${tx.type === 'DEPOSIT' ? '+' : '-'}${formatCurrency(tx.amount)}
        </div>
        <span class="tx-status-badge tx-status-${tx.status}">${tx.status}</span>
    </div>`).join('');
}

// ============ ACCOUNTS ============
function renderAccountsPage() {
    const container = document.getElementById('accounts-list');
    if (!accounts.length) {
        container.innerHTML = '<p class="empty-state">Chưa có tài khoản. Tạo tài khoản mới để bắt đầu!</p>';
        return;
    }
    container.innerHTML = accounts.map(acc => renderAccountCard(acc)).join('');
}

function showCreateAccountModal() {
    document.getElementById('create-account-modal').classList.remove('hidden');
}

function closeModal(id) {
    document.getElementById(id).classList.add('hidden');
}

async function handleCreateAccount(e) {
    e.preventDefault();
    const errorEl = document.getElementById('create-acc-error');
    hideError(errorEl);

    try {
        const data = await apiPost('/v1/accounts', {
            accountName: document.getElementById('acc-name').value,
            accountType: document.getElementById('acc-type').value,
            currency: 'VND'
        });
        showToast('Tạo tài khoản thành công!', 'success');
        closeModal('create-account-modal');
        document.getElementById('create-account-form').reset();
        loadDashboard();
    } catch (err) {
        showError(errorEl, err.message);
    }
}

// ============ TRANSFER ============
function initTransferForm() {
    const amountInput = document.getElementById('transfer-amount');
    amountInput.addEventListener('input', () => {
        const amount = parseFloat(amountInput.value) || 0;
        const summary = document.getElementById('transfer-summary');
        if (amount > 0) {
            summary.style.display = 'block';
            document.getElementById('sum-amount').textContent = formatCurrency(amount);
            document.getElementById('sum-total').textContent = formatCurrency(amount);
        } else {
            summary.style.display = 'none';
        }
    });
}

function populateSourceAccounts() {
    const select = document.getElementById('source-account');
    select.innerHTML = '<option value="">Chọn tài khoản...</option>';
    accounts.filter(a => a.status === 'ACTIVE').forEach(acc => {
        select.innerHTML += `<option value="${acc.accountNumber}">${acc.accountNumber} — ${formatCurrency(acc.balance)} ${acc.currency}</option>`;
    });
}

async function handleTransfer(e) {
    e.preventDefault();
    const btn = document.getElementById('transfer-btn');
    const errorEl = document.getElementById('transfer-error');
    setLoading(btn, true);
    hideError(errorEl);

    try {
        const data = await apiPost('/v1/transactions/transfer', {
            sourceAccountNumber: document.getElementById('source-account').value,
            destinationAccountNumber: document.getElementById('dest-account').value,
            amount: parseFloat(document.getElementById('transfer-amount').value),
            description: document.getElementById('transfer-desc').value
        });

        document.querySelector('.transfer-form-card').classList.add('hidden');
        const resultCard = document.getElementById('transfer-result');
        resultCard.classList.remove('hidden');
        document.getElementById('result-ref').textContent = `Mã: ${data.data.referenceNumber}`;
        document.getElementById('result-amount').textContent = formatCurrency(data.data.amount) + ' VND';

        showToast('Chuyển tiền thành công!', 'success');
        loadDashboard();
    } catch (err) {
        showError(errorEl, err.message || 'Chuyển tiền thất bại');
    } finally {
        setLoading(btn, false);
    }
}

function resetTransfer() {
    document.querySelector('.transfer-form-card').classList.remove('hidden');
    document.getElementById('transfer-result').classList.add('hidden');
    document.getElementById('transfer-form').reset();
    document.getElementById('transfer-summary').style.display = 'none';
    populateSourceAccounts();
}

// ============ HISTORY ============
function populateFilterAccounts() {
    const select = document.getElementById('filter-account');
    select.innerHTML = '<option value="">Tất cả tài khoản</option>';
    accounts.forEach(acc => {
        select.innerHTML += `<option value="${acc.id}">${acc.accountNumber} (${acc.accountType})</option>`;
    });
}

async function loadHistory() {
    const accountId = document.getElementById('filter-account').value;
    if (!accountId && !accounts.length) return;

    const targetId = accountId || (accounts[0]?.id);
    if (!targetId) return;

    try {
        const data = await apiGet(`/v1/transactions/account/${targetId}?size=50`);
        const txList = data.data?.content || [];
        renderHistoryTable(txList);
    } catch (err) {
        console.error('Failed to load history:', err);
    }
}

function renderHistoryTable(txList) {
    const filterRef = (document.getElementById('filter-ref')?.value || '').toLowerCase();
    const filtered = filterRef ? txList.filter(tx => tx.referenceNumber.toLowerCase().includes(filterRef)) : txList;

    const tbody = document.getElementById('history-body');
    if (!filtered.length) {
        tbody.innerHTML = '<tr><td colspan="5" class="empty-state">Không tìm thấy giao dịch</td></tr>';
        return;
    }
    tbody.innerHTML = filtered.map(tx => `
    <tr>
        <td>${formatDate(tx.createdAt)}</td>
        <td style="font-family:monospace;font-size:12px;color:var(--text-secondary)">${tx.referenceNumber}</td>
        <td>
            <span class="material-icons-outlined" style="font-size:16px;vertical-align:middle;color:${tx.type === 'DEPOSIT' ? 'var(--accent-green)' : 'var(--accent-blue)'}">
                ${getTxIcon(tx.type)}</span> ${getTxLabel(tx.type)}
        </td>
        <td class="${tx.type === 'DEPOSIT' ? 'positive' : 'negative'}" style="font-weight:600;color:${tx.type === 'DEPOSIT' ? 'var(--accent-green)' : 'var(--accent-red)'}">
            ${tx.type === 'DEPOSIT' ? '+' : '-'}${formatCurrency(tx.amount)}
        </td>
        <td><span class="tx-status-badge tx-status-${tx.status}">${tx.status}</span></td>
    </tr>`).join('');
}

// ============ PAGE NAVIGATION ============
function showPage(pageId) {
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    document.getElementById(pageId).classList.add('active');

    // Clone sidebar for app pages
    if (pageId !== 'login-page' && pageId !== 'register-page') {
        cloneSidebar(pageId);
        updateNavActive(pageId);

        // Load page-specific data
        if (pageId === 'dashboard-page') loadDashboard();
        if (pageId === 'accounts-page') renderAccountsPage();
        if (pageId === 'transfer-page') { populateSourceAccounts(); resetTransfer(); }
        if (pageId === 'history-page') { populateFilterAccounts(); loadHistory(); }
    }
}

function cloneSidebar(pageId) {
    if (pageId === 'dashboard-page') return;
    const source = document.querySelector('#dashboard-page .sidebar');
    const target = document.querySelector(`#${pageId} .sidebar`);
    if (source && target) target.innerHTML = source.innerHTML;
}

function updateNavActive(pageId) {
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.remove('active');
        if (item.dataset.page === pageId) item.classList.add('active');
    });
}

// ============ API HELPERS ============
async function apiPost(path, body) {
    const headers = { 'Content-Type': 'application/json' };
    const token = localStorage.getItem(TOKEN_KEY);
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const res = await fetch(API_BASE + path, { method: 'POST', headers, body: JSON.stringify(body) });
    const data = await res.json();
    if (!res.ok || !data.success) throw new Error(data.message || `HTTP ${res.status}`);
    return data;
}

async function apiGet(path) {
    const headers = {};
    const token = localStorage.getItem(TOKEN_KEY);
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const res = await fetch(API_BASE + path, { headers });
    const data = await res.json();
    if (!res.ok || !data.success) throw new Error(data.message || `HTTP ${res.status}`);
    return data;
}

// ============ UTILITIES ============
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN').format(amount || 0);
}

function formatAccountNumber(num) {
    if (!num) return '';
    return num.replace(/(.{4})/g, '$1 ').trim();
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}

function getTxIcon(type) {
    const icons = { DEPOSIT: 'south_west', TRANSFER: 'swap_horiz', WITHDRAWAL: 'north_east', FEE: 'receipt', INTEREST: 'percent', REFUND: 'undo' };
    return icons[type] || 'receipt_long';
}

function getTxLabel(type) {
    const labels = { DEPOSIT: 'Nạp tiền', TRANSFER: 'Chuyển khoản', WITHDRAWAL: 'Rút tiền', FEE: 'Phí', INTEREST: 'Lãi suất', REFUND: 'Hoàn tiền' };
    return labels[type] || type;
}

function setLoading(btn, loading) {
    const text = btn.querySelector('.btn-text');
    const loader = btn.querySelector('.btn-loader');
    if (loading) {
        btn.disabled = true;
        if (text) text.classList.add('hidden');
        if (loader) loader.classList.remove('hidden');
    } else {
        btn.disabled = false;
        if (text) text.classList.remove('hidden');
        if (loader) loader.classList.add('hidden');
    }
}

function showError(el, msg) {
    el.textContent = msg;
    el.classList.remove('hidden');
}

function hideError(el) {
    el.classList.add('hidden');
}

function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    const icon = type === 'success' ? 'check_circle' : type === 'error' ? 'error' : 'info';
    toast.innerHTML = `<span class="material-icons-outlined">${icon}</span>${message}`;
    container.appendChild(toast);
    setTimeout(() => { toast.style.opacity = '0'; toast.style.transform = 'translateX(100%)'; setTimeout(() => toast.remove(), 300); }, 4000);
}
