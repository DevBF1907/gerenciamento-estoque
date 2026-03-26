const API = '';

async function request(method, path, body) {
    const res = await fetch(API + path, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: body ? JSON.stringify(body) : undefined
    });
    if (res.status === 204) return true;
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Erro desconhecido');
    return data;
}

function toast(msg, type = 'success') {
    const wrap = document.getElementById('toasts');
    const t = document.createElement('div');
    t.className = `toast ${type}`;
    t.innerHTML = `<div class="toast-dot"></div>${msg}`;
    wrap.appendChild(t);
    setTimeout(() => t.remove(), 3500);
}

function nav(page, el) {
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
    document.getElementById('page-' + page).classList.add('active');
    el.classList.add('active');
    if (page === 'dashboard') loadDashboard();
    if (page === 'products') loadProducts();
    if (page === 'warehouses') loadWarehouses();
    if (page === 'inventory') loadInventoryPage();
    if (page === 'lowstock') loadLowStock();
}

// ---- DASHBOARD ----

async function loadDashboard() {
    try {
        const [products, warehouses, lowstock] = await Promise.all([
            request('GET', '/products'),
            request('GET', '/warehouses'),
            request('GET', '/stock/low')
        ]);

        document.getElementById('stat-products').textContent = products.length;
        document.getElementById('stat-warehouses').textContent = warehouses.length;
        document.getElementById('stat-lowstock').textContent = lowstock.length;
        document.getElementById('stat-api').textContent = 'online';
        document.getElementById('stat-api').style.color = 'var(--accent)';

        const tbody = document.getElementById('dashboard-table');
        if (!products.length) {
            tbody.innerHTML = '<tr><td colspan="4" class="empty">nenhum produto cadastrado</td></tr>';
            return;
        }

        const rows = await Promise.all(products.map(async p => {
            try {
                const stock = await request('GET', `/products/${p.id}/stock`);
                const total = stock.totalQuantity;
                const status = total === 0 ? 'sem estoque' : total < 5 ? 'baixo' : 'ok';
                const badge = total === 0 ? 'badge-red' : total < 5 ? 'badge-yellow' : 'badge-green';
                return `<tr>
          <td style="font-family:var(--mono);color:var(--muted)">#${p.id}</td>
          <td>${p.name}</td>
          <td style="font-family:var(--mono)">${total}</td>
          <td><span class="badge ${badge}">${status}</span></td>
        </tr>`;
            } catch {
                return `<tr>
          <td style="font-family:var(--mono);color:var(--muted)">#${p.id}</td>
          <td>${p.name}</td>
          <td style="font-family:var(--mono)">—</td>
          <td><span class="badge badge-red">sem dados</span></td>
        </tr>`;
            }
        }));
        tbody.innerHTML = rows.join('');
    } catch {
        document.getElementById('stat-api').textContent = 'offline';
        document.getElementById('stat-api').style.color = 'var(--danger)';
        toast('Não foi possível conectar à API', 'error');
    }
}

// ---- PRODUCTS ----

async function loadProducts() {
    const data = await request('GET', '/products').catch(() => []);
    const tbody = document.getElementById('products-table');
    tbody.innerHTML = data.length
        ? data.map(p => `<tr>
        <td style="font-family:var(--mono);color:var(--muted)">#${p.id}</td>
        <td>${p.name}</td>
      </tr>`).join('')
        : '<tr><td colspan="2" class="empty">nenhum produto cadastrado</td></tr>';
}

async function createProduct() {
    const name = document.getElementById('product-name').value.trim();
    if (!name) return toast('informe o nome do produto', 'error');
    try {
        await request('POST', '/products', { name });
        document.getElementById('product-name').value = '';
        toast(`produto "${name}" cadastrado`);
        loadProducts();
    } catch (e) {
        toast(e.message, 'error');
    }
}

// ---- WAREHOUSES ----

async function loadWarehouses() {
    const data = await request('GET', '/warehouses').catch(() => []);
    const tbody = document.getElementById('warehouses-table');
    tbody.innerHTML = data.length
        ? data.map(w => `<tr>
        <td style="font-family:var(--mono);color:var(--muted)">#${w.id}</td>
        <td>${w.name}</td>
      </tr>`).join('')
        : '<tr><td colspan="2" class="empty">nenhum armazém cadastrado</td></tr>';
}

async function createWarehouse() {
    const name = document.getElementById('warehouse-name').value.trim();
    if (!name) return toast('informe o nome do armazém', 'error');
    try {
        await request('POST', '/warehouses', { name });
        document.getElementById('warehouse-name').value = '';
        toast(`armazém "${name}" cadastrado`);
        loadWarehouses();
    } catch (e) {
        toast(e.message, 'error');
    }
}

// ---- INVENTORY ----

async function loadInventoryPage() {
    const [products, warehouses] = await Promise.all([
        request('GET', '/products').catch(() => []),
        request('GET', '/warehouses').catch(() => [])
    ]);

    const pOpts = products.length
        ? products.map(p => `<option value="${p.id}">${p.name}</option>`).join('')
        : '<option value="">nenhum produto cadastrado</option>';

    const wOpts = warehouses.length
        ? warehouses.map(w => `<option value="${w.id}">${w.name}</option>`).join('')
        : '<option value="">nenhum armazém cadastrado</option>';

    ['add-product', 'rem-product', 'query-product'].forEach(id => {
        document.getElementById(id).innerHTML = pOpts;
    });
    ['add-warehouse', 'rem-warehouse'].forEach(id => {
        document.getElementById(id).innerHTML = wOpts;
    });
}

async function addStock() {
    const productId = document.getElementById('add-product').value;
    const warehouseId = document.getElementById('add-warehouse').value;
    const quantity = parseInt(document.getElementById('add-qty').value);
    if (!productId || !warehouseId) return toast('selecione produto e armazém', 'error');
    if (!quantity || quantity < 1) return toast('quantidade deve ser maior que zero', 'error');
    try {
        await request('POST', `/products/${productId}/stock/add`, {
            warehouseId: parseInt(warehouseId),
            quantity
        });
        document.getElementById('add-qty').value = '';
        toast(`${quantity} unidade(s) adicionada(s)`);
    } catch (e) {
        toast(e.message, 'error');
    }
}

async function removeStock() {
    const productId = document.getElementById('rem-product').value;
    const warehouseId = document.getElementById('rem-warehouse').value;
    const quantity = parseInt(document.getElementById('rem-qty').value);
    if (!productId || !warehouseId) return toast('selecione produto e armazém', 'error');
    if (!quantity || quantity < 1) return toast('quantidade deve ser maior que zero', 'error');
    try {
        await request('POST', `/products/${productId}/stock/remove`, {
            warehouseId: parseInt(warehouseId),
            quantity
        });
        document.getElementById('rem-qty').value = '';
        toast(`${quantity} unidade(s) removida(s)`);
    } catch (e) {
        toast(e.message, 'error');
    }
}

async function queryProductStock() {
    const productId = document.getElementById('query-product').value;
    if (!productId) return;
    try {
        const data = await request('GET', `/products/${productId}/stock`);
        const tbody = document.getElementById('stock-table');
        tbody.innerHTML = data.byWarehouse.length
            ? data.byWarehouse.map(w => `<tr>
          <td>${w.warehouseName}</td>
          <td style="font-family:var(--mono)">${w.quantity}</td>
        </tr>`).join('')
            : '<tr><td colspan="2" class="empty">sem estoque em nenhum armazém</td></tr>';

        const total = document.getElementById('stock-total');
        total.style.display = 'block';
        total.textContent = `total: ${data.totalQuantity} unidade(s)`;
    } catch (e) {
        toast(e.message, 'error');
    }
}

// ---- LOW STOCK ----

async function loadLowStock() {
    const threshold = document.getElementById('threshold').value;
    const url = threshold ? `/stock/low?threshold=${threshold}` : '/stock/low';
    try {
        const data = await request('GET', url);
        const tbody = document.getElementById('lowstock-table');
        tbody.innerHTML = data.length
            ? data.map(p => `<tr>
          <td style="font-family:var(--mono);color:var(--muted)">#${p.productId}</td>
          <td>${p.productName}</td>
          <td style="font-family:var(--mono)">${p.totalQuantity}</td>
          <td><span class="badge ${p.totalQuantity === 0 ? 'badge-red' : 'badge-yellow'}">
            ${p.totalQuantity === 0 ? 'sem estoque' : 'baixo'}
          </span></td>
        </tr>`).join('')
            : '<tr><td colspan="4" class="empty">nenhum produto abaixo do limite</td></tr>';
    } catch (e) {
        toast(e.message, 'error');
    }
}

// ---- INIT ----
loadDashboard();