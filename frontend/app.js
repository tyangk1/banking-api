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
let stompClient = null;

// ============ INIT ============
document.addEventListener('DOMContentLoaded', () => {
    initForms();
    checkAuth();
    initTransferForm();
    initDepositForm();
});

// ============ AUTH ============
function checkAuth() {
    const token = localStorage.getItem(TOKEN_KEY);
    const userStr = localStorage.getItem(USER_KEY);
    if (token && userStr) {
        currentUser = JSON.parse(userStr);
        showPage('dashboard-page');
        loadDashboard();
        connectWebSocket();
    } else {
        showPage('login-page');
    }
}

function initForms() {
    document.getElementById('login-form').addEventListener('submit', handleLogin);
    document.getElementById('register-form').addEventListener('submit', handleRegister);
    document.getElementById('transfer-form').addEventListener('submit', handleTransfer);
    document.getElementById('create-account-form').addEventListener('submit', handleCreateAccount);
    document.getElementById('deposit-form').addEventListener('submit', handleDeposit);
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
        connectWebSocket();
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
        <div style="display:flex;justify-content:space-between;align-items:center">
            <span class="acc-status status-${acc.status}">${acc.status}</span>
            <button class="icon-btn" onclick="event.stopPropagation();showQrCode('${acc.id}','${acc.accountNumber}','${acc.ownerName || ''}')" title="Mã QR nhận tiền" style="color:var(--text-secondary);padding:4px">
                <span class="material-icons-outlined" style="font-size:20px">qr_code_2</span>
            </button>
        </div>
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
        // Load transactions from ALL accounts and merge
        const allTxPromises = accounts.map(acc =>
            apiGet(`/v1/transactions/account/${acc.id}?size=10`).catch(() => ({ data: { content: [] } }))
        );
        const results = await Promise.all(allTxPromises);
        const allTx = results.flatMap(r => r.data?.content || []);
        // Sort by date descending and take top 5
        allTx.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
        transactions = allTx.slice(0, 5);
        renderDashboardTransactions();
        document.getElementById('recent-tx-count').textContent = allTx.length;
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
    container.innerHTML = transactions.map(tx => {
        const isIncoming = isTxIncoming(tx);
        const sign = isIncoming ? '+' : '-';
        const cls = isIncoming ? 'positive' : 'negative';
        return `
    <div class="tx-item">
        <div class="tx-icon ${tx.type.toLowerCase()}">
            <span class="material-icons-outlined">${getTxIcon(tx.type)}</span>
        </div>
        <div class="tx-details">
            <div class="tx-type">${isIncoming ? getTxLabelIncoming(tx.type) : getTxLabel(tx.type)}</div>
            <div class="tx-date">${formatDate(tx.createdAt)} · ${tx.referenceNumber}</div>
        </div>
        <div class="tx-amount ${cls}">
            ${sign}${formatCurrency(tx.amount)}
        </div>
        <span class="tx-status-badge tx-status-${tx.status}">${tx.status}</span>
    </div>`;
    }).join('');
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

// ============ DEPOSIT ============
function initDepositForm() {
    const amountInput = document.getElementById('deposit-amount');
    if (amountInput) {
        amountInput.addEventListener('input', () => {
            const amount = parseFloat(amountInput.value) || 0;
            // Could add preview here
        });
    }
}

function populateDepositAccounts() {
    const select = document.getElementById('deposit-account');
    select.innerHTML = '<option value="">Ch\u1ECDn t\u00E0i kho\u1EA3n...</option>';
    accounts.filter(a => a.status === 'ACTIVE').forEach(acc => {
        select.innerHTML += `<option value="${acc.accountNumber}">${acc.accountNumber} \u2014 ${formatCurrency(acc.balance)} ${acc.currency}</option>`;
    });
}

async function handleDeposit(e) {
    e.preventDefault();
    const btn = document.getElementById('deposit-btn');
    const errorEl = document.getElementById('deposit-error');
    setLoading(btn, true);
    hideError(errorEl);

    try {
        const data = await apiPost('/v1/transactions/deposit', {
            accountNumber: document.getElementById('deposit-account').value,
            amount: parseFloat(document.getElementById('deposit-amount').value),
            description: document.getElementById('deposit-desc').value || 'N\u1EA1p ti\u1EC1n'
        });

        document.getElementById('deposit-form-card').classList.add('hidden');
        const resultCard = document.getElementById('deposit-result');
        resultCard.classList.remove('hidden');
        document.getElementById('deposit-result-ref').textContent = `M\u00E3: ${data.data.referenceNumber}`;
        document.getElementById('deposit-result-amount').textContent = formatCurrency(data.data.amount) + ' VND';

        showToast('N\u1EA1p ti\u1EC1n th\u00E0nh c\u00F4ng!', 'success');
        loadDashboard();
    } catch (err) {
        showError(errorEl, err.message || 'N\u1EA1p ti\u1EC1n th\u1EA5t b\u1EA1i');
    } finally {
        setLoading(btn, false);
    }
}

function resetDeposit() {
    document.getElementById('deposit-form-card').classList.remove('hidden');
    document.getElementById('deposit-result').classList.add('hidden');
    document.getElementById('deposit-form').reset();
    populateDepositAccounts();
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

    try {
        let txList;
        if (accountId) {
            // Load for specific account
            const data = await apiGet(`/v1/transactions/account/${accountId}?size=50`);
            txList = data.data?.content || [];
        } else {
            // Load from ALL accounts
            const allTxPromises = accounts.map(acc =>
                apiGet(`/v1/transactions/account/${acc.id}?size=50`).catch(() => ({ data: { content: [] } }))
            );
            const results = await Promise.all(allTxPromises);
            txList = results.flatMap(r => r.data?.content || []);
            // Deduplicate by ID (in case same tx appears for both source & dest)
            const seen = new Set();
            txList = txList.filter(tx => { if (seen.has(tx.id)) return false; seen.add(tx.id); return true; });
            txList.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
        }
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
    tbody.innerHTML = filtered.map(tx => {
        const isIncoming = isTxIncoming(tx);
        const sign = isIncoming ? '+' : '-';
        const color = isIncoming ? 'var(--accent-green)' : 'var(--accent-red)';
        return `
    <tr>
        <td>${formatDate(tx.createdAt)}</td>
        <td style="font-family:monospace;font-size:12px;color:var(--text-secondary)">${tx.referenceNumber}</td>
        <td>
            <span class="material-icons-outlined" style="font-size:16px;vertical-align:middle;color:${isIncoming ? 'var(--accent-green)' : 'var(--accent-blue)'}">
                ${getTxIcon(tx.type)}</span> ${isIncoming ? getTxLabelIncoming(tx.type) : getTxLabel(tx.type)}
        </td>
        <td class="${isIncoming ? 'positive' : 'negative'}" style="font-weight:600;color:${color}">
            ${sign}${formatCurrency(tx.amount)}
        </td>
        <td><span class="tx-status-badge tx-status-${tx.status}">${tx.status}</span></td>
    </tr>`;
    }).join('');
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
        if (pageId === 'deposit-page') { populateDepositAccounts(); resetDeposit(); }
        if (pageId === 'transfer-page') { populateSourceAccounts(); resetTransfer(); }
        if (pageId === 'history-page') { populateFilterAccounts(); loadHistory(); }
        if (pageId === 'analytics-page') { loadAnalytics(); }
        if (pageId === 'recurring-page') { populateRecurringAccounts(); loadRecurring(); }
        if (pageId === 'beneficiary-page') { loadBeneficiaries(); }
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

// ============ RECURRING TRANSFERS ============
function populateRecurringAccounts() {
    const sel = document.getElementById('rc-source');
    if (!sel) return;
    sel.innerHTML = accounts.map(a =>
        `<option value="${a.id}">${a.accountNumber} — ${formatCurrency(a.balance)} VND</option>`
    ).join('');
}

function toggleDayFields() {
    const freq = document.getElementById('rc-frequency').value;
    document.getElementById('rc-dow-group').style.display = freq === 'WEEKLY' ? '' : 'none';
    document.getElementById('rc-dom-group').style.display = freq === 'MONTHLY' ? '' : 'none';
}

async function loadRecurring() {
    try {
        const data = await apiGet('/v1/recurring-transfers');
        const list = data.data;
        const container = document.getElementById('recurring-list');

        if (!list || !list.length) {
            container.innerHTML = '<p class="empty-state">Chưa có lịch chuyển tiền tự động</p>';
            return;
        }

        const freqLabels = { DAILY: 'Hàng ngày', WEEKLY: 'Hàng tuần', MONTHLY: 'Hàng tháng' };
        const statusColors = { ACTIVE: '#22c55e', PAUSED: '#f59e0b', COMPLETED: '#6366f1', CANCELLED: '#ef4444' };
        const dowLabels = { 1:'T2', 2:'T3', 3:'T4', 4:'T5', 5:'T6', 6:'T7', 7:'CN' };

        container.innerHTML = list.map(r => {
            let scheduleText = freqLabels[r.frequency];
            if (r.frequency === 'WEEKLY') scheduleText += ` (${dowLabels[r.dayOfWeek] || ''})`;
            if (r.frequency === 'MONTHLY') scheduleText += ` (ngày ${r.dayOfMonth})`;

            const nextDate = r.nextExecution ? new Date(r.nextExecution).toLocaleDateString('vi-VN') : '—';
            const actions = r.status === 'ACTIVE'
                ? `<button class="btn-sm btn-warning" onclick="pauseRecurring('${r.id}')">⏸ Tạm dừng</button>
                   <button class="btn-sm btn-danger" onclick="cancelRecurring('${r.id}')">✕ Huỷ</button>`
                : r.status === 'PAUSED'
                ? `<button class="btn-sm btn-success" onclick="resumeRecurring('${r.id}')">▶ Tiếp tục</button>
                   <button class="btn-sm btn-danger" onclick="cancelRecurring('${r.id}')">✕ Huỷ</button>`
                : '';

            return `
            <div class="tx-item" style="flex-direction:column;align-items:stretch;gap:10px;">
                <div style="display:flex;align-items:center;gap:14px;">
                    <div class="tx-icon" style="background:${statusColors[r.status]}22;color:${statusColors[r.status]}">
                        <span class="material-icons-outlined">schedule_send</span>
                    </div>
                    <div style="flex:1">
                        <div style="font-weight:600;font-size:14px">${r.sourceAccountNumber} → ${r.destinationAccountNumber}</div>
                        <div style="font-size:12px;color:var(--text-muted)">${scheduleText} · Kế tiếp: ${nextDate} · Đã thực hiện: ${r.executionCount} lần</div>
                        ${r.lastError ? `<div style="font-size:11px;color:#ef4444;margin-top:2px">⚠ ${r.lastError}</div>` : ''}
                    </div>
                    <div style="text-align:right">
                        <div style="font-weight:700;color:var(--accent-blue)">${formatCurrency(r.amount)} VND</div>
                        <span style="font-size:11px;padding:2px 8px;border-radius:12px;background:${statusColors[r.status]}22;color:${statusColors[r.status]}">${r.status}</span>
                    </div>
                </div>
                <div style="display:flex;gap:8px;justify-content:flex-end">${actions}</div>
            </div>`;
        }).join('');
    } catch (err) {
        console.error('Failed to load recurring transfers:', err);
    }
}

async function createRecurring(e) {
    e.preventDefault();
    const errorDiv = document.getElementById('rc-error');
    errorDiv.classList.add('hidden');

    const freq = document.getElementById('rc-frequency').value;
    const body = {
        sourceAccountId: document.getElementById('rc-source').value,
        destinationAccountNumber: document.getElementById('rc-dest').value,
        amount: parseFloat(document.getElementById('rc-amount').value),
        frequency: freq,
        description: document.getElementById('rc-desc').value || null,
    };
    if (freq === 'WEEKLY') body.dayOfWeek = parseInt(document.getElementById('rc-dow').value);
    if (freq === 'MONTHLY') body.dayOfMonth = parseInt(document.getElementById('rc-dom').value);

    try {
        const token = localStorage.getItem(TOKEN_KEY);
        const res = await fetch(`${API_BASE}/v1/recurring-transfers`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
            body: JSON.stringify(body)
        });
        const data = await res.json();
        if (!res.ok || !data.success) throw new Error(data.message || 'Failed');

        showToast('✅ Đã tạo lịch chuyển tiền tự động!', 'success');
        document.getElementById('recurring-form').reset();
        toggleDayFields();
        loadRecurring();
    } catch (err) {
        errorDiv.textContent = err.message;
        errorDiv.classList.remove('hidden');
    }
}

async function pauseRecurring(id) {
    try {
        const token = localStorage.getItem(TOKEN_KEY);
        await fetch(`${API_BASE}/v1/recurring-transfers/${id}/pause`, {
            method: 'PATCH', headers: { 'Authorization': `Bearer ${token}` }
        });
        showToast('⏸ Đã tạm dừng lịch chuyển tiền', 'success');
        loadRecurring();
    } catch (err) { showToast('Lỗi: ' + err.message, 'error'); }
}

async function resumeRecurring(id) {
    try {
        const token = localStorage.getItem(TOKEN_KEY);
        await fetch(`${API_BASE}/v1/recurring-transfers/${id}/resume`, {
            method: 'PATCH', headers: { 'Authorization': `Bearer ${token}` }
        });
        showToast('▶ Đã kích hoạt lại lịch chuyển tiền', 'success');
        loadRecurring();
    } catch (err) { showToast('Lỗi: ' + err.message, 'error'); }
}

async function cancelRecurring(id) {
    if (!confirm('Bạn chắc chắn muốn huỷ lịch chuyển tiền này?')) return;
    try {
        const token = localStorage.getItem(TOKEN_KEY);
        await fetch(`${API_BASE}/v1/recurring-transfers/${id}`, {
            method: 'DELETE', headers: { 'Authorization': `Bearer ${token}` }
        });
        showToast('✕ Đã huỷ lịch chuyển tiền', 'success');
        loadRecurring();
    } catch (err) { showToast('Lỗi: ' + err.message, 'error'); }
}

// ============ PDF STATEMENT DOWNLOAD ============
async function downloadPdfStatement() {
    const filterAccount = document.getElementById('filter-account');
    let accountId = filterAccount ? filterAccount.value : '';

    // If no specific account selected, use first account
    if (!accountId && accounts.length > 0) {
        accountId = accounts[0].id;
    }

    if (!accountId) {
        showToast('Không tìm thấy tài khoản để xuất sao kê', 'error');
        return;
    }

    const btn = document.getElementById('btn-download-pdf');
    const btnText = btn.querySelector('.btn-text');
    const originalText = btnText.textContent;
    btnText.textContent = 'Đang tạo PDF...';
    btn.disabled = true;

    try {
        const token = localStorage.getItem(TOKEN_KEY);
        const res = await fetch(`${API_BASE}/v1/statements/${accountId}/pdf`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!res.ok) {
            const err = await res.json().catch(() => ({ message: 'Failed to generate PDF' }));
            throw new Error(err.message);
        }

        const blob = await res.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;

        // Extract filename from Content-Disposition header
        const disposition = res.headers.get('Content-Disposition');
        let filename = 'statement.pdf';
        if (disposition && disposition.includes('filename=')) {
            filename = disposition.split('filename=')[1].replace(/"/g, '');
        }

        a.download = filename;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);

        showToast('📄 Đã tải sao kê PDF thành công!', 'success');
    } catch (err) {
        console.error('PDF download failed:', err);
        showToast('Không thể tạo PDF: ' + err.message, 'error');
    } finally {
        btnText.textContent = originalText;
        btn.disabled = false;
    }
}

// ============ ANALYTICS ============
let chartIncome = null;
let chartCategory = null;

async function loadAnalytics() {
    try {
        const data = await apiGet('/v1/analytics/dashboard');
        const analytics = data.data;

        // Update stats
        document.getElementById('stat-income').textContent = formatCurrency(analytics.totalIncome);
        document.getElementById('stat-expense').textContent = formatCurrency(analytics.totalExpense);
        document.getElementById('stat-net').textContent = formatCurrency(analytics.netChange);
        document.getElementById('stat-count').textContent = analytics.totalTransactions;

        // Render charts
        renderIncomeExpenseChart(analytics.monthlySummaries);
        renderCategoryChart(analytics.categoryBreakdown);
        renderTopBeneficiaries(analytics.topBeneficiaries);
    } catch (err) {
        console.error('Failed to load analytics:', err);
    }
}

function renderIncomeExpenseChart(monthlySummaries) {
    const ctx = document.getElementById('chart-income-expense');
    if (!ctx) return;

    if (chartIncome) chartIncome.destroy();

    const labels = monthlySummaries.map(m => m.monthName || `T${m.month}`);
    const incomeData = monthlySummaries.map(m => m.totalIncome);
    const expenseData = monthlySummaries.map(m => m.totalExpense);

    chartIncome = new Chart(ctx, {
        type: 'bar',
        data: {
            labels,
            datasets: [
                {
                    label: 'Thu nhập',
                    data: incomeData,
                    backgroundColor: 'rgba(34, 197, 94, 0.7)',
                    borderColor: '#22c55e',
                    borderWidth: 1,
                    borderRadius: 6,
                    barPercentage: 0.6,
                },
                {
                    label: 'Chi tiêu',
                    data: expenseData,
                    backgroundColor: 'rgba(239, 68, 68, 0.7)',
                    borderColor: '#ef4444',
                    borderWidth: 1,
                    borderRadius: 6,
                    barPercentage: 0.6,
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { labels: { color: '#fff', font: { family: 'Inter' } } }
            },
            scales: {
                x: { ticks: { color: 'rgba(255,255,255,0.6)', font: { family: 'Inter' } }, grid: { color: 'rgba(255,255,255,0.05)' } },
                y: { ticks: { color: 'rgba(255,255,255,0.6)', font: { family: 'Inter' }, callback: v => formatCurrency(v) }, grid: { color: 'rgba(255,255,255,0.05)' } }
            }
        }
    });
}

function renderCategoryChart(categories) {
    const ctx = document.getElementById('chart-category');
    if (!ctx) return;

    if (chartCategory) chartCategory.destroy();

    const categoryColors = [
        '#3b82f6', '#8b5cf6', '#22c55e', '#ef4444', '#f59e0b',
        '#06b6d4', '#ec4899', '#14b8a6', '#f97316', '#6366f1'
    ];

    const categoryLabels = {
        OTHER: 'Khác', SALARY: 'Lương', RENT: 'Thuê nhà', SHOPPING: 'Mua sắm',
        FOOD: 'Ăn uống', TRANSFER: 'Chuyển khoản', BILLS: 'Hoá đơn',
        ENTERTAINMENT: 'Giải trí', HEALTH: 'Sức khoẻ', EDUCATION: 'Giáo dục'
    };

    chartCategory = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: categories.map(c => categoryLabels[c.category] || c.category),
            datasets: [{
                data: categories.map(c => c.totalAmount),
                backgroundColor: categories.map((_, i) => categoryColors[i % categoryColors.length]),
                borderColor: 'rgba(10, 22, 40, 0.8)',
                borderWidth: 3,
                hoverOffset: 8,
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '65%',
            plugins: {
                legend: {
                    position: 'right',
                    labels: {
                        color: '#fff',
                        font: { family: 'Inter', size: 12 },
                        padding: 12,
                        usePointStyle: true,
                        pointStyleWidth: 10,
                    }
                },
                tooltip: {
                    callbacks: {
                        label: (context) => {
                            const cat = categories[context.dataIndex];
                            return ` ${formatCurrency(cat.totalAmount)} VND (${cat.percentage}%)`;
                        }
                    }
                }
            }
        }
    });
}

function renderTopBeneficiaries(beneficiaries) {
    const container = document.getElementById('top-beneficiaries');
    if (!beneficiaries || !beneficiaries.length) {
        container.innerHTML = '<p class="empty-state">Chưa có dữ liệu chuyển khoản</p>';
        return;
    }

    const maxAmount = beneficiaries[0]?.totalAmount || 1;
    container.innerHTML = beneficiaries.map((b, i) => {
        const pct = (b.totalAmount / maxAmount * 100).toFixed(0);
        const colors = ['#3b82f6', '#8b5cf6', '#22c55e', '#f59e0b', '#06b6d4'];
        return `
    <div class="tx-item" style="flex-direction:column;align-items:stretch;gap:8px;">
        <div style="display:flex;align-items:center;gap:14px;">
            <div class="tx-icon" style="background:${colors[i]}22;color:${colors[i]}">
                <span class="material-icons-outlined">person</span>
            </div>
            <div style="flex:1">
                <div style="font-weight:600;font-size:14px">${b.accountNumber}</div>
                <div style="font-size:12px;color:var(--text-muted)">${b.transferCount} giao dịch</div>
            </div>
            <div style="font-weight:700;color:var(--accent-blue)">${formatCurrency(b.totalAmount)} VND</div>
        </div>
        <div style="height:6px;background:rgba(255,255,255,0.05);border-radius:3px;overflow:hidden">
            <div style="height:100%;width:${pct}%;background:${colors[i]};border-radius:3px;transition:width 0.5s"></div>
        </div>
    </div>`;
    }).join('');
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

function getTxLabelIncoming(type) {
    const labels = { DEPOSIT: 'Nạp tiền', TRANSFER: 'Nhận tiền', WITHDRAWAL: 'Rút tiền', FEE: 'Phí', INTEREST: 'Lãi suất', REFUND: 'Hoàn tiền' };
    return labels[type] || type;
}

function isTxIncoming(tx) {
    if (tx.type === 'DEPOSIT' || tx.type === 'INTEREST' || tx.type === 'REFUND') return true;
    if (tx.type === 'WITHDRAWAL' || tx.type === 'FEE') return false;
    // For TRANSFER: check if any of user's accounts is the destination
    const myAccountNumbers = accounts.map(a => a.accountNumber);
    if (tx.destinationAccountNumber && myAccountNumbers.includes(tx.destinationAccountNumber)) return true;
    return false;
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

// ============ WEBSOCKET NOTIFICATIONS ============
function connectWebSocket() {
    if (stompClient && stompClient.connected) return;
    if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
        console.warn('SockJS/STOMP not loaded — skipping WebSocket');
        return;
    }

    try {
        const socket = new SockJS('http://localhost:8080/api/ws');
        stompClient = Stomp.over(socket);
        stompClient.debug = null; // Suppress STOMP debug logs

        stompClient.connect({}, function(frame) {
            console.log('🔌 WebSocket connected:', frame);

            // Subscribe to broadcast notifications
            stompClient.subscribe('/topic/notifications', function(message) {
                handleWsNotification(JSON.parse(message.body));
            });

            console.log('✅ Subscribed to /topic/notifications');
        }, function(error) {
            console.warn('WebSocket connection error:', error);
            // Reconnect after 5 seconds
            setTimeout(connectWebSocket, 5000);
        });
    } catch (e) {
        console.warn('Failed to connect WebSocket:', e);
    }
}

function disconnectWebSocket() {
    if (stompClient && stompClient.connected) {
        stompClient.disconnect(() => console.log('🔌 WebSocket disconnected'));
        stompClient = null;
    }
}

function handleWsNotification(notification) {
    console.log('🔔 WS Notification:', notification);

    // Only show if the notification is for the current user
    if (notification.userId && currentUser) {
        const isForMe = notification.userId === currentUser.email || notification.userId === currentUser.id;
        if (!isForMe) return;
    }

    // Determine toast type
    let toastType = 'info';
    let icon = 'notifications';
    switch (notification.type) {
        case 'TRANSFER_RECEIVED':
            toastType = 'success';
            icon = '💰';
            break;
        case 'TRANSFER_SENT':
            toastType = 'info';
            icon = '📤';
            break;
        case 'DEPOSIT_RECEIVED':
            toastType = 'success';
            icon = '💳';
            break;
        case 'ACCOUNT_CREATED':
            toastType = 'success';
            icon = '🏦';
            break;
    }

    // Show enhanced toast
    showNotificationToast(icon, notification.title, notification.message, toastType);

    // Auto-refresh dashboard data
    if (['TRANSFER_RECEIVED', 'DEPOSIT_RECEIVED', 'BALANCE_UPDATED'].includes(notification.type)) {
        setTimeout(() => loadDashboard(), 1000);
    }
}

function showNotificationToast(icon, title, message, type) {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.style.cssText = 'flex-direction:column;align-items:flex-start;gap:4px;min-width:350px;cursor:pointer;';
    toast.innerHTML = `
        <div style="display:flex;align-items:center;gap:8px;font-weight:600;">
            <span style="font-size:20px">${icon}</span> ${title}
        </div>
        <div style="font-size:13px;opacity:0.85;padding-left:28px;">${message}</div>
    `;
    toast.onclick = () => { toast.remove(); loadDashboard(); };
    container.appendChild(toast);
    setTimeout(() => { toast.style.opacity = '0'; toast.style.transform = 'translateX(100%)'; setTimeout(() => toast.remove(), 300); }, 6000);
}

// ============ BENEFICIARY MANAGEMENT ============
let benSearchTimer = null;

async function loadBeneficiaries() {
    try {
        const data = await apiGet('/v1/beneficiaries?size=100');
        const page = data.data;
        const list = page.content || page;
        renderBeneficiaryList(list);
    } catch (err) { console.error('Failed to load beneficiaries:', err); }
}

function renderBeneficiaryList(list) {
    const container = document.getElementById('ben-list');
    const favContainer = document.getElementById('ben-favorites');
    const countEl = document.getElementById('ben-count');
    if (countEl) countEl.textContent = list.length;

    const favorites = list.filter(b => b.favorite);
    const others = list.filter(b => !b.favorite);

    // Render favorites
    if (favorites.length > 0) {
        favContainer.innerHTML = `
            <div style="font-size:12px;color:var(--accent-orange);font-weight:600;margin-bottom:8px">⭐ Y\u00eau th\u00edch (${favorites.length})</div>
            ${favorites.map(b => renderBenCard(b)).join('')}`;
    } else {
        favContainer.innerHTML = '';
    }

    // Render all
    if (others.length > 0) {
        container.innerHTML = others.map(b => renderBenCard(b)).join('');
    } else if (favorites.length === 0) {
        container.innerHTML = '<p class="empty-state">Ch\u01b0a c\u00f3 ng\u01b0\u1eddi nh\u1eadn n\u00e0o</p>';
    } else {
        container.innerHTML = '';
    }
}

function renderBenCard(b) {
    const initials = (b.accountHolderName || 'NA').split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase();
    const favIcon = b.favorite ? 'star' : 'star_outline';
    const favColor = b.favorite ? '#f59e0b' : 'var(--text-muted)';
    return `
    <div class="tx-item" style="gap:12px">
        <div class="tx-icon" style="background:var(--gradient-purple);color:#fff;font-size:13px;font-weight:700">
            ${initials}
        </div>
        <div style="flex:1;min-width:0">
            <div style="font-weight:600;font-size:14px">${b.nickname}</div>
            <div style="font-size:12px;color:var(--text-muted)">${b.accountNumber} \u2022 ${b.accountHolderName}</div>
            <div style="font-size:11px;color:var(--text-muted)">CK: ${b.transferCount} l\u1ea7n${b.verified ? ' \u2022 \u2705 X\u00e1c minh' : ''}</div>
        </div>
        <div style="display:flex;gap:6px;align-items:center">
            <button class="icon-btn" onclick="toggleFavorite('${b.id}',${!b.favorite},'${b.nickname}')" title="Y\u00eau th\u00edch" style="color:${favColor}">
                <span class="material-icons-outlined">${favIcon}</span>
            </button>
            <button class="btn-sm btn-success" onclick="quickTransfer('${b.accountNumber}')" title="Chuy\u1ec3n ti\u1ec1n nhanh">\u27A4</button>
            <button class="btn-sm btn-danger" onclick="deleteBeneficiary('${b.id}','${b.nickname}')" title="X\u00f3a">\u2715</button>
        </div>
    </div>`;
}

async function createBeneficiary(e) {
    e.preventDefault();
    const errorDiv = document.getElementById('ben-error');
    errorDiv.classList.add('hidden');
    try {
        const token = localStorage.getItem(TOKEN_KEY);
        const res = await fetch(`${API_BASE}/v1/beneficiaries`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
            body: JSON.stringify({
                nickname: document.getElementById('ben-nickname').value,
                accountNumber: document.getElementById('ben-account').value,
                accountHolderName: document.getElementById('ben-holder').value,
            })
        });
        const data = await res.json();
        if (!res.ok || !data.success) throw new Error(data.message || 'Failed');
        showToast('\u2705 \u0110\u00e3 l\u01b0u ng\u01b0\u1eddi nh\u1eadn m\u1edbi!', 'success');
        document.getElementById('ben-form').reset();
        loadBeneficiaries();
    } catch (err) {
        errorDiv.textContent = err.message;
        errorDiv.classList.remove('hidden');
    }
}

function searchBeneficiaries() {
    clearTimeout(benSearchTimer);
    benSearchTimer = setTimeout(async () => {
        const q = document.getElementById('ben-search').value.trim();
        if (!q) { loadBeneficiaries(); return; }
        try {
            const data = await apiGet(`/v1/beneficiaries/search?q=${encodeURIComponent(q)}`);
            const page = data.data;
            renderBeneficiaryList(page.content || page);
        } catch (err) { console.error('Search failed:', err); }
    }, 300);
}

async function toggleFavorite(id, newState, nickname) {
    try {
        const token = localStorage.getItem(TOKEN_KEY);
        await fetch(`${API_BASE}/v1/beneficiaries/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
            body: JSON.stringify({ nickname: nickname, favorite: newState })
        });
        showToast(newState ? '\u2b50 \u0110\u00e3 th\u00eam y\u00eau th\u00edch' : '\u0110\u00e3 b\u1ecf y\u00eau th\u00edch', 'success');
        loadBeneficiaries();
    } catch (err) { showToast('L\u1ed7i: ' + err.message, 'error'); }
}

async function deleteBeneficiary(id, name) {
    if (!confirm(`X\u00f3a "${name}" kh\u1ecfi danh b\u1ea1?`)) return;
    try {
        const token = localStorage.getItem(TOKEN_KEY);
        await fetch(`${API_BASE}/v1/beneficiaries/${id}`, {
            method: 'DELETE', headers: { 'Authorization': `Bearer ${token}` }
        });
        showToast('\u0110\u00e3 x\u00f3a ng\u01b0\u1eddi nh\u1eadn', 'success');
        loadBeneficiaries();
    } catch (err) { showToast('L\u1ed7i: ' + err.message, 'error'); }
}

function quickTransfer(accountNumber) {
    showPage('transfer-page');
    setTimeout(() => {
        const destInput = document.getElementById('transfer-dest');
        if (destInput) destInput.value = accountNumber;
    }, 100);
}

// ============ QR CODE ============
let qrScannerStream = null;
let qrScanAnimFrame = null;

function showQrCode(accountId, accountNumber, ownerName) {
    const modal = document.getElementById('qr-modal');
    const img = document.getElementById('qr-image');
    const info = document.getElementById('qr-account-info');
    const token = localStorage.getItem(TOKEN_KEY);

    img.src = `${API_BASE}/v1/qr/generate/${accountId}?size=400&token=${token}`;
    // Set auth header via fetch for the image
    fetch(`${API_BASE}/v1/qr/generate/${accountId}?size=400`, {
        headers: { 'Authorization': `Bearer ${token}` }
    }).then(res => res.blob()).then(blob => {
        img.src = URL.createObjectURL(blob);
    }).catch(() => {
        img.alt = 'Không thể tải QR';
    });

    info.innerHTML = `<strong>${ownerName}</strong><br><span style="font-size:18px;letter-spacing:2px;color:var(--accent-blue)">${accountNumber}</span>`;
    modal.classList.remove('hidden');
}

function closeQrModal() {
    document.getElementById('qr-modal').classList.add('hidden');
}

function openQrScanner() {
    const modal = document.getElementById('qr-scanner-modal');
    const video = document.getElementById('qr-video');
    const status = document.getElementById('qr-scan-status');
    status.textContent = 'Đang mở camera...';
    modal.classList.remove('hidden');

    navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } })
        .then(stream => {
            qrScannerStream = stream;
            video.srcObject = stream;
            video.play();
            status.textContent = 'Đưa mã QR vào khung hình...';
            requestAnimationFrame(scanQrFrame);
        })
        .catch(err => {
            status.textContent = '❌ Không thể truy cập camera: ' + err.message;
            // Fallback: manual input
            setTimeout(() => {
                const payload = prompt('Không thể mở camera.\nNhập nội dung QR code:');
                if (payload) handleQrDecoded(payload);
                closeQrScanner();
            }, 1000);
        });
}

function scanQrFrame() {
    const video = document.getElementById('qr-video');
    const canvas = document.getElementById('qr-canvas');
    const ctx = canvas.getContext('2d');

    if (video.readyState === video.HAVE_ENOUGH_DATA) {
        canvas.width = video.videoWidth;
        canvas.height = video.videoHeight;
        ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
        const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);

        if (typeof jsQR !== 'undefined') {
            const code = jsQR(imageData.data, imageData.width, imageData.height, { inversionAttempts: 'dontInvert' });
            if (code && code.data) {
                // QR detected!
                document.getElementById('qr-scan-status').textContent = '✅ Đã quét thành công!';
                handleQrDecoded(code.data);
                setTimeout(() => closeQrScanner(), 500);
                return;
            }
        }
    }

    qrScanAnimFrame = requestAnimationFrame(scanQrFrame);
}

function closeQrScanner() {
    document.getElementById('qr-scanner-modal').classList.add('hidden');
    if (qrScannerStream) {
        qrScannerStream.getTracks().forEach(t => t.stop());
        qrScannerStream = null;
    }
    if (qrScanAnimFrame) {
        cancelAnimationFrame(qrScanAnimFrame);
        qrScanAnimFrame = null;
    }
}

function handleQrDecoded(payload) {
    // Format: BANKQR|accountNumber|holderName|amount(optional)
    const parts = payload.split('|');
    if (parts.length < 3 || parts[0] !== 'BANKQR') {
        showToast('❌ Mã QR không hợp lệ', 'error');
        return;
    }

    const accountNumber = parts[1];
    const holderName = parts[2];
    const amount = parts.length > 3 ? parts[3] : '';

    showToast(`✅ Đã nhận diện: ${holderName} (${accountNumber})`, 'success');

    // Navigate to transfer page and fill in
    showPage('transfer-page');
    setTimeout(() => {
        const destInput = document.getElementById('transfer-dest');
        const amountInput = document.getElementById('transfer-amount');
        if (destInput) destInput.value = accountNumber;
        if (amountInput && amount) amountInput.value = amount;
    }, 200);
}
