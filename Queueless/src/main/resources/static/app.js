// QueueLess Client Application
// Vanilla JavaScript + Chart.js + STOMP WebSockets

let currentUser = null;
let currentRole = null;
let stompClient = null;
let activeServiceId = 1;
let currentTokenId = null;
let stopwatchInterval = null;
let stopwatchSeconds = 222;

let chartQueueAnalytics = null;
let chartCongestion = null;

// Sound Synthesizer for Token Call
function playChime() {
  try {
    const ctx = new (window.AudioContext || window.webkitAudioContext)();
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.type = "sine";
    osc.frequency.setValueAtTime(587.33, ctx.currentTime); // D5
    osc.frequency.setValueAtTime(880, ctx.currentTime + 0.15); // A5
    gain.gain.setValueAtTime(0.3, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.6);
    osc.start();
    osc.stop(ctx.currentTime + 0.6);
  } catch (e) {
    console.log("Audio not supported or permitted yet");
  }
}

// ---------------- Toast Notifications ----------------
function showToast(msg, type = "info") {
  const container = document.getElementById("toast-container");
  if (!container) return;
  const t = document.createElement("div");
  t.className = `toast ${type}`;
  t.innerHTML = `<span>🔔</span><span>${msg}</span>`;
  container.appendChild(t);
  setTimeout(() => {
    t.style.opacity = "0";
    setTimeout(() => t.remove(), 300);
  }, 4000);
}

// ---------------- Theme Toggle ----------------
function toggleTheme() {
  const html = document.documentElement;
  const next = html.getAttribute("data-theme") === "dark" ? "light" : "dark";
  html.setAttribute("data-theme", next);
  localStorage.setItem("queueless-theme", next);
  if (chartQueueAnalytics) initCharts();
}

// ---------------- View Navigation ----------------
function switchView(viewName) {
  document.querySelectorAll(".view-section").forEach(sec => sec.classList.remove("active"));
  document.querySelectorAll(".nav-tab").forEach(tab => tab.classList.remove("active"));

  const targetSec = document.getElementById(`view-${viewName}`);
  const targetTab = document.getElementById(`tab-${viewName}`);
  if (targetSec) targetSec.classList.add("active");
  if (targetTab) targetTab.classList.add("active");

  if (viewName === "customer") loadCustomerDashboard();
  if (viewName === "staff") loadStaffDashboard();
  if (viewName === "admin") loadAdminDashboard();
}

// ---------------- Authentication ----------------
function openLoginModal() {
  document.getElementById("modal-login").classList.add("active");
}

function closeModal(id) {
  document.getElementById(id).classList.remove("active");
}

async function apiFetch(endpoint, options = {}) {
  const res = await fetch(endpoint, {
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {})
    },
    ...options
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({ error: "Request failed" }));
    throw new Error(err.error || `HTTP ${res.status}`);
  }
  return res.json().catch(() => ({}));
}

async function quickSwitchRole(email, password, targetView) {
  try {
    const creds = btoa(`${email}:${password}`);
    const res = await fetch("/api/me", {
      headers: { "Authorization": `Basic ${creds}` }
    });
    if (!res.ok) throw new Error("Invalid credentials");
    currentUser = await res.json();
    currentRole = currentUser.role;
    localStorage.setItem("queueless-auth", creds);
    updateUserDisplay();
    showToast(`Signed in as ${currentUser.name} (${currentUser.role})`, "success");
    switchView(targetView);
  } catch (e) {
    showToast(e.message, "danger");
  }
}

async function performLogin() {
  const email = document.getElementById("login-email").value.trim();
  const pass = document.getElementById("login-password").value.trim();
  try {
    const creds = btoa(`${email}:${pass}`);
    const res = await fetch("/api/me", {
      headers: { "Authorization": `Basic ${creds}` }
    });
    if (!res.ok) throw new Error("Invalid username or password");
    currentUser = await res.json();
    currentRole = currentUser.role;
    localStorage.setItem("queueless-auth", creds);
    closeModal("modal-login");
    updateUserDisplay();
    showToast(`Welcome back, ${currentUser.name}!`, "success");
    if (currentUser.role === "CUSTOMER") switchView("customer");
    else if (currentUser.role === "STAFF") switchView("staff");
    else if (currentUser.role === "ADMIN") switchView("admin");
  } catch (e) {
    showToast(e.message, "danger");
  }
}

function updateUserDisplay() {
  const el = document.getElementById("current-user-display");
  const authBtn = document.getElementById("auth-btn");
  if (currentUser) {
    el.innerText = `${currentUser.name} (${currentUser.role})`;
    authBtn.innerText = "Switch / Logout";
  } else {
    el.innerText = "Guest";
    authBtn.innerText = "Sign in";
  }
}

function getAuthHeader() {
  const saved = localStorage.getItem("queueless-auth");
  return saved ? { "Authorization": `Basic ${saved}` } : {};
}

// ---------------- WebSocket (STOMP) Integration ----------------
function initWebSocket() {
  try {
    const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
    const wsUrl = `${protocol}//${window.location.host}/ws`;
    const client = new StompJs.Client({
      brokerURL: wsUrl,
      reconnectDelay: 4000,
      debug: function (str) {
        // console.log(str);
      }
    });

    client.onConnect = function () {
      console.log("WebSocket Connected");
      client.subscribe("/topic/queue", function (message) {
        handleQueueUpdate(JSON.parse(message.body));
      });
      if (currentUser) {
        client.subscribe(`/topic/alerts/${currentUser.id}`, function (msg) {
          const alert = JSON.parse(msg.body);
          playChime();
          showToast(`📢 ${alert.message}`, "warning");
        });
      }
    };

    client.activate();
    stompClient = client;
  } catch (e) {
    console.warn("WebSocket initialization skipped:", e);
  }
}

function handleQueueUpdate(payload) {
  if (document.getElementById("view-customer").classList.contains("active")) {
    loadCustomerDashboard();
  }
  if (document.getElementById("view-staff").classList.contains("active")) {
    loadStaffQueue();
  }
  if (document.getElementById("view-admin").classList.contains("active")) {
    loadAdminAnalytics();
  }
}

// ---------------- MODULE 1: Customer Portal Logic ----------------
async function loadCustomerDashboard() {
  try {
    const tokens = await apiFetch("/api/tokens/mine", { headers: getAuthHeader() });
    renderCustomerTokensTable(tokens);

    const activeToken = tokens.find(t => ["WAITING", "CALLED", "IN_SERVICE"].includes(t.status));
    if (activeToken) {
      currentTokenId = activeToken.id;
      document.getElementById("cust-token-number").innerText = activeToken.number;
      document.getElementById("cust-token-service").innerText = `${activeToken.serviceName} • Counter: ${activeToken.counterName}`;
      document.getElementById("cust-queue-position").innerText = activeToken.position || "—";
      document.getElementById("cust-wait-minutes").innerText = activeToken.waitMinutes > 0 ? `≈ ${activeToken.waitMinutes} minutes` : "Now Serving";
      document.getElementById("cust-wait-range").innerText = activeToken.waitRange || "Within 5 min";
      document.getElementById("cust-view-qr-btn").style.display = "inline-flex";
      document.getElementById("cust-cancel-token-btn").style.display = activeToken.status === "WAITING" ? "inline-flex" : "none";

      // Stepper fill calculation
      const fill = document.getElementById("cust-stepper-fill");
      if (activeToken.status === "WAITING") {
        fill.style.width = "40%";
        setStepActive(3);
      } else if (activeToken.status === "CALLED") {
        fill.style.width = "65%";
        setStepActive(4);
        playChime();
      } else if (activeToken.status === "IN_SERVICE") {
        fill.style.width = "85%";
        setStepActive(4);
      }

      // Explainable breakdown
      if (activeToken.explanation) {
        const exp = activeToken.explanation;
        document.getElementById("cust-explain-breakdown").innerHTML = `
          • Customers ahead: <b>${exp.customersAhead || 0}</b><br>
          • Active desks: <b>${exp.activeCounters || 1}</b><br>
          • Service pace: <b>${exp.serviceRateMinutes || 5} min/person</b><br>
          • Factor: <i>${exp.reason || 'Standard FIFO queue'}</i>
        `;
      }
    } else {
      currentTokenId = null;
      document.getElementById("cust-token-number").innerText = "—";
      document.getElementById("cust-token-service").innerText = "No Active Token";
      document.getElementById("cust-queue-position").innerText = "0";
      document.getElementById("cust-wait-minutes").innerText = "0 minutes";
      document.getElementById("cust-view-qr-btn").style.display = "none";
      document.getElementById("cust-cancel-token-btn").style.display = "none";
      document.getElementById("cust-stepper-fill").style.width = "0%";
      setStepActive(1);
    }
  } catch (e) {
    console.error("Failed to load customer dashboard:", e);
  }
}

function setStepActive(stepNum) {
  for (let i = 1; i <= 5; i++) {
    const node = document.getElementById(`step-node-${i}`);
    if (!node) continue;
    node.className = "step-node";
    if (i < stepNum) node.classList.add("completed");
    if (i === stepNum) node.classList.add("active");
  }
}

function renderCustomerTokensTable(tokens) {
  const tbody = document.getElementById("cust-tokens-table-body");
  if (!tokens || tokens.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: var(--text-dim);">No token records found.</td></tr>`;
    return;
  }
  tbody.innerHTML = tokens.map(t => `
    <tr>
      <td><b>${t.number}</b></td>
      <td>${t.serviceName}</td>
      <td>${t.isPriority ? '<span class="badge priority">Priority</span>' : '<span class="badge">Standard</span>'}</td>
      <td><span class="badge ${t.status.toLowerCase()}">${t.status}</span></td>
      <td>${t.createdAt ? t.createdAt.substring(11, 19) : ''}</td>
      <td>
        <button class="btn btn-sm btn-secondary" onclick="viewQrById(${t.id}, '${t.number}')">QR Code</button>
      </td>
    </tr>
  `).join('');
}

async function openGetTokenModal() {
  const services = await apiFetch("/api/services");
  const sel = document.getElementById("token-service-select");
  sel.innerHTML = services.map(s => `<option value="${s.id}">${s.name} (~${s.defaultMinutes} min)</option>`).join('');
  document.getElementById("modal-get-token").classList.add("active");
}

function togglePriorityReason(checked) {
  document.getElementById("priority-reason-group").style.display = checked ? "flex" : "none";
}

async function confirmCreateToken() {
  const serviceId = document.getElementById("token-service-select").value;
  const isPriority = document.getElementById("token-priority-check").checked;
  const priorityReason = document.getElementById("token-priority-reason").value;

  try {
    const token = await apiFetch("/api/tokens", {
      method: "POST",
      headers: getAuthHeader(),
      body: JSON.stringify({
        serviceId: Number(serviceId),
        isPriority,
        priorityReason: isPriority ? priorityReason : "STANDARD"
      })
    });
    closeModal("modal-get-token");
    showToast(`Token ${token.number} generated successfully!`, "success");
    loadCustomerDashboard();
  } catch (e) {
    showToast(e.message, "danger");
  }
}

async function cancelMyToken() {
  if (!currentTokenId) return;
  if (!confirm("Are you sure you want to cancel your waiting token?")) return;
  try {
    await apiFetch(`/api/tokens/${currentTokenId}/cancel`, {
      method: "POST",
      headers: getAuthHeader()
    });
    showToast("Token cancelled successfully", "info");
    loadCustomerDashboard();
  } catch (e) {
    showToast(e.message, "danger");
  }
}

async function viewMyQrToken() {
  if (currentTokenId) viewQrById(currentTokenId, document.getElementById("cust-token-number").innerText);
}

function viewQrById(id, tokenNum) {
  document.getElementById("modal-qr-title").innerText = `Token ${tokenNum} QR Code`;
  document.getElementById("modal-qr-img").src = `/api/tokens/${id}/qr?auth=${Math.random()}`;
  document.getElementById("modal-qr-secret-text").innerText = `QUEUELESS:${id}:VERIFIED`;
  document.getElementById("modal-qr").classList.add("active");
}

async function openBestHoursModal() {
  try {
    const visits = await apiFetch(`/api/visits/${activeServiceId}`);
    const tbody = document.getElementById("visits-table-body");
    tbody.innerHTML = visits.map(v => `
      <tr>
        <td><b>${v.hour}</b></td>
        <td>${v.expectedArrivals} expected visitors</td>
        <td><span class="badge ${v.statusBadge === 'RECOMMENDED' ? 'completed' : v.statusBadge === 'BUSY' ? 'skipped' : 'waiting'}">${v.advice}</span></td>
      </tr>
    `).join('');
    document.getElementById("modal-visits").classList.add("active");
  } catch (e) {
    showToast("Failed to load visiting hours: " + e.message, "danger");
  }
}

// ---------------- MODULE 2: Staff Operations Logic ----------------
async function loadStaffDashboard() {
  try {
    const services = await apiFetch("/api/services");
    const counters = await apiFetch("/api/counters");

    const selServ = document.getElementById("staff-service-select");
    selServ.innerHTML = services.map(s => `<option value="${s.id}">${s.name}</option>`).join('');
    if (activeServiceId) selServ.value = activeServiceId;

    const selCounter = document.getElementById("staff-counter-select");
    selCounter.innerHTML = counters.map(c => `<option value="${c.id}">${c.name} ${c.active ? '' : '(Inactive)'}</option>`).join('');

    loadStaffQueue();
    startStopwatch();
  } catch (e) {
    console.error("Failed to load staff dashboard:", e);
  }
}

function changeStaffService() {
  activeServiceId = document.getElementById("staff-service-select").value;
  loadStaffQueue();
}

async function loadStaffQueue() {
  const sId = document.getElementById("staff-service-select").value || activeServiceId;
  try {
    const queue = await apiFetch(`/api/staff/queue/${sId}`, { headers: getAuthHeader() });
    const waiting = queue.filter(t => t.status === "WAITING");
    const active = queue.find(t => ["CALLED", "IN_SERVICE"].includes(t.status));

    document.getElementById("staff-waiting-count").innerText = waiting.length;

    if (active) {
      document.getElementById("staff-now-serving-token").innerText = active.number;
      document.getElementById("staff-now-serving-service").innerText = `${active.serviceName} • Counter: ${active.counterName}`;
    } else {
      document.getElementById("staff-now-serving-token").innerText = "—";
      document.getElementById("staff-now-serving-service").innerText = "No active customer at desk";
    }

    const tbody = document.getElementById("staff-queue-table-body");
    if (queue.length === 0) {
      tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--text-dim);">Queue is empty.</td></tr>`;
      return;
    }

    tbody.innerHTML = queue.map((t, idx) => `
      <tr>
        <td>${idx + 1}</td>
        <td><b>${t.number}</b></td>
        <td>${t.customerName}</td>
        <td>${t.isPriority ? '<span class="badge priority">Priority</span>' : '<span class="badge">Normal</span>'}</td>
        <td>${t.checkedIn ? '<span class="badge completed">✓ Arrived</span>' : '<span class="badge waiting">Remote</span>'}</td>
        <td>~${t.waitMinutes} min</td>
        <td><span class="badge ${t.status.toLowerCase()}">${t.status}</span></td>
        <td>
          ${t.status === 'CALLED' ? `<button class="btn btn-sm btn-success" onclick="staffCompleteServiceById(${t.id})">Served</button>` : ''}
          ${t.status === 'WAITING' ? `<button class="btn btn-sm" onclick="staffCallSpecific(${t.id})">Call</button>` : ''}
        </td>
      </tr>
    `).join('');
  } catch (e) {
    console.error("Failed to load staff queue:", e);
  }
}

function startStopwatch() {
  if (stopwatchInterval) clearInterval(stopwatchInterval);
  stopwatchInterval = setInterval(() => {
    stopwatchSeconds++;
    const m = String(Math.floor(stopwatchSeconds / 60)).padStart(2, '0');
    const s = String(stopwatchSeconds % 60).padStart(2, '0');
    const el = document.getElementById("staff-stopwatch");
    if (el) el.innerText = `${m}:${s}`;
  }, 1000);
}

async function staffCallNext() {
  const serviceId = document.getElementById("staff-service-select").value || activeServiceId;
  const counterId = document.getElementById("staff-counter-select").value;
  try {
    const token = await apiFetch(`/api/staff/call/${serviceId}`, {
      method: "POST",
      headers: getAuthHeader(),
      body: JSON.stringify({ counterId: Number(counterId) })
    });
    playChime();
    stopwatchSeconds = 0;
    showToast(`Called Token ${token.number} to Counter!`, "success");
    loadStaffQueue();
  } catch (e) {
    showToast(e.message, "danger");
  }
}

async function staffStartService() {
  const tokenNum = document.getElementById("staff-now-serving-token").innerText;
  if (tokenNum === "—") return showToast("No customer is currently called", "warning");
  showToast("Service started. Timer running.", "info");
}

async function staffCompleteService() {
  const tokenNum = document.getElementById("staff-now-serving-token").innerText;
  if (tokenNum === "—") return showToast("No customer is currently called", "warning");

  try {
    const queue = await apiFetch(`/api/staff/queue/${activeServiceId}`, { headers: getAuthHeader() });
    const current = queue.find(t => ["CALLED", "IN_SERVICE"].includes(t.status));
    if (!current) throw new Error("No active customer found");

    await apiFetch(`/api/staff/complete/${current.id}`, {
      method: "POST",
      headers: getAuthHeader()
    });
    showToast(`Token ${current.number} completed and logged!`, "success");
    loadStaffQueue();
  } catch (e) {
    showToast(e.message, "danger");
  }
}

async function staffSkipToken() {
  try {
    const queue = await apiFetch(`/api/staff/queue/${activeServiceId}`, { headers: getAuthHeader() });
    const current = queue.find(t => ["CALLED", "IN_SERVICE"].includes(t.status));
    if (!current) throw new Error("No called customer to skip");

    await apiFetch(`/api/staff/skip/${current.id}`, {
      method: "POST",
      headers: getAuthHeader()
    });
    showToast(`Token ${current.number} skipped`, "warning");
    loadStaffQueue();
  } catch (e) {
    showToast(e.message, "danger");
  }
}

async function staffCheckInQr() {
  const qrVal = document.getElementById("staff-qr-input").value.trim();
  if (!qrVal) return showToast("Please input or scan QR payload", "warning");
  try {
    const res = await apiFetch("/api/staff/checkin", {
      method: "POST",
      headers: getAuthHeader(),
      body: JSON.stringify({ qrValue: qrVal })
    });
    showToast(`✓ Arrival verified for Token ${res.tokenNumber}!`, "success");
    document.getElementById("staff-qr-input").value = "";
    loadStaffQueue();
  } catch (e) {
    showToast(e.message, "danger");
  }
}

// ---------------- MODULE 3: Admin & Digital Twin Logic ----------------
async function loadAdminDashboard() {
  await loadAdminAnalytics();
  await loadAdminCounters();
  await loadModelEvaluation();
  initDigitalTwinCanvas();
}

async function loadAdminAnalytics() {
  try {
    const data = await apiFetch("/api/admin/analytics", { headers: getAuthHeader() });
    document.getElementById("admin-current-queue").innerText = data.waitingTokens;
    document.getElementById("admin-active-counters").innerText = `${data.activeCounters} / ${data.totalCounters}`;
    document.getElementById("admin-avg-wait").innerText = `${data.avgWaitMinutes} min`;
    document.getElementById("admin-pred-congestion").innerText = data.waitingTokens > 15 ? "High" : data.waitingTokens > 8 ? "Moderate" : "Low";

    initCharts(data);
  } catch (e) {
    console.error("Failed to load admin analytics:", e);
  }
}

function initCharts(data = {}) {
  const isDark = document.documentElement.getAttribute("data-theme") === "dark";
  const gridColor = isDark ? "#1e293b" : "#e2e8f0";
  const textColor = isDark ? "#94a3b8" : "#475569";

  // Chart 1: Queue Analytics
  const ctx1 = document.getElementById("chart-queue-analytics");
  if (ctx1) {
    if (chartQueueAnalytics) chartQueueAnalytics.destroy();
    chartQueueAnalytics = new Chart(ctx1, {
      type: "line",
      data: {
        labels: ["09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "12:00"],
        datasets: [{
          label: "Arrivals",
          data: [12, 18, 25, 34, 28, 22, 16],
          borderColor: "#38bdf8",
          backgroundColor: "rgba(56, 189, 248, 0.15)",
          fill: true,
          tension: 0.4
        }, {
          label: "Completed Services",
          data: [10, 16, 22, 30, 26, 21, 15],
          borderColor: "#10b981",
          backgroundColor: "transparent",
          borderDash: [5, 5],
          tension: 0.4
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { labels: { color: textColor } } },
        scales: {
          x: { grid: { color: gridColor }, ticks: { color: textColor } },
          y: { grid: { color: gridColor }, ticks: { color: textColor } }
        }
      }
    });
  }

  // Chart 2: Congestion Forecast (Actual vs Predicted)
  const ctx2 = document.getElementById("chart-congestion-prediction");
  if (ctx2) {
    if (chartCongestion) chartCongestion.destroy();
    chartCongestion = new Chart(ctx2, {
      type: "line",
      data: {
        labels: ["Now", "+15 min", "+30 min", "+45 min", "+60 min"],
        datasets: [{
          label: "Actual Congestion",
          data: [40, 35, null, null, null],
          borderColor: "#38bdf8",
          backgroundColor: "#38bdf8",
          tension: 0.3
        }, {
          label: "Predicted (Java Heuristic)",
          data: [40, 35, 45, 56, 64],
          borderColor: "#a855f7",
          borderDash: [6, 6],
          backgroundColor: "transparent",
          tension: 0.3
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { labels: { color: textColor } } },
        scales: {
          x: { grid: { color: gridColor }, ticks: { color: textColor } },
          y: {
            grid: { color: gridColor },
            ticks: { color: textColor, callback: v => v + "%" },
            min: 0,
            max: 100
          }
        }
      }
    });
  }
}

// ---------------- Digital Twin Simulation ----------------
function initDigitalTwinCanvas() {
  const row = document.getElementById("sim-counters-row");
  if (!row) return;

  const sampleDesks = [
    { name: "C1 Accounts", active: true, busy: true, token: "A039" },
    { name: "C2 Cash", active: true, busy: true, token: "A040" },
    { name: "C3 Clinic", active: true, busy: false, token: "IDLE" },
    { name: "C4 Docs", active: true, busy: true, token: "D002" },
    { name: "C5 FastTrack", active: true, busy: false, token: "IDLE" },
    { name: "C6 Inquiry", active: false, busy: false, token: "OFFLINE" }
  ];

  row.innerHTML = sampleDesks.map(d => `
    <div class="sim-counter-desk ${d.busy ? 'busy' : d.active ? 'active' : 'inactive'}">
      <div class="sim-counter-name">${d.name}</div>
      <div class="sim-counter-token">${d.token}</div>
      <span class="badge ${d.busy ? 'in_service' : d.active ? 'completed' : 'skipped'}" style="margin-top:4px;">
        ${d.busy ? 'Serving' : d.active ? 'Ready' : 'Closed'}
      </span>
    </div>
  `).join('');

  // Render waiting dots
  const waitArea = document.getElementById("sim-waiting-area");
  waitArea.innerHTML = Array.from({ length: 14 }).map((_, i) => `
    <span class="sim-person-dot ${i % 4 === 0 ? 'priority' : ''}" title="Customer #${i + 1}"></span>
  `).join('');
}

async function runDigitalTwinSimulation() {
  const counters = Number(document.getElementById("slider-sim-counters").value);
  const arrivals = Number(document.getElementById("slider-sim-arrivals").value);
  const duration = Number(document.getElementById("slider-sim-duration").value);
  const priority = Number(document.getElementById("slider-sim-priority").value);

  showToast("Running discrete-event simulation on Java backend...", "info");

  try {
    const res = await apiFetch(`/api/admin/simulate/${activeServiceId}`, {
      method: "POST",
      headers: getAuthHeader(),
      body: JSON.stringify({
        activeCounters: counters,
        arrivalRatePerHour: arrivals,
        avgServiceMinutes: duration,
        simulationMinutes: 60,
        priorityPercent: priority
      })
    });

    const box = document.getElementById("sim-results-box");
    box.style.display = "block";
    document.getElementById("sim-res-wait").innerText = `${res.proposed.avgWaitMinutes} min`;
    document.getElementById("sim-res-queue").innerText = res.proposed.totalArrivals - res.proposed.totalCompleted;
    document.getElementById("sim-res-throughput").innerText = `${res.proposed.throughputPerHour} / hr`;
    document.getElementById("sim-res-utilization").innerText = `${Math.round(res.proposed.counterUtilization * 100)}%`;
    document.getElementById("sim-res-verdict").innerText = res.verdict;

    showToast(`Simulation complete: Wait reduced by ${res.waitTimeReductionPercent}%!`, "success");
  } catch (e) {
    showToast(e.message, "danger");
  }
}

async function loadAdminCounters() {
  try {
    const counters = await apiFetch("/api/counters", { headers: getAuthHeader() });
    const tbody = document.getElementById("admin-counters-table-body");
    tbody.innerHTML = counters.map(c => `
      <tr>
        <td><b>${c.name}</b></td>
        <td>${c.serviceName}</td>
        <td><span class="badge ${c.active ? 'completed' : 'skipped'}">${c.active ? 'Active' : 'Offline'}</span></td>
        <td>
          <button class="btn btn-sm btn-secondary" onclick="toggleCounter(${c.id})">${c.active ? 'Disable' : 'Enable'}</button>
        </td>
      </tr>
    `).join('');
  } catch (e) {
    console.error("Failed to load admin counters:", e);
  }
}

async function toggleCounter(id) {
  try {
    const res = await apiFetch(`/api/admin/counters/${id}/toggle`, {
      method: "POST",
      headers: getAuthHeader()
    });
    showToast(`Counter ${res.name} is now ${res.active ? 'Active' : 'Offline'}`, "info");
    loadAdminCounters();
  } catch (e) {
    showToast(e.message, "danger");
  }
}

async function loadModelEvaluation() {
  try {
    const data = await apiFetch("/api/admin/predictions/compare", { headers: getAuthHeader() });
    const tbody = document.getElementById("admin-model-eval-body");
    tbody.innerHTML = data.models.map(m => `
      <tr>
        <td><b>${m.modelName}</b></td>
        <td>${m.mae} min</td>
        <td>${m.rmse}</td>
        <td><span class="badge completed">${m.rSquared}</span></td>
      </tr>
    `).join('');
    document.getElementById("admin-model-eval-note").innerText = data.explanation;
  } catch (e) {
    console.error("Failed to load model evaluation:", e);
  }
}

function exportReport(format) {
  window.open(`/api/admin/export.${format}`, "_blank");
}

// ---------------- Demo Bar Triggers ----------------
async function demoAddCustomer() {
  try {
    const res = await apiFetch("/api/admin/demo/generate-customer", {
      method: "POST",
      headers: getAuthHeader()
    });
    showToast(`Simulated customer added: Token ${res.tokenNumber} (${res.service})`, "success");
    handleQueueUpdate();
  } catch (e) {
    showToast(e.message, "danger");
  }
}

async function demoSpikeDemand() {
  try {
    const res = await apiFetch("/api/admin/demo/spike-demand", {
      method: "POST",
      headers: getAuthHeader()
    });
    showToast(res.message, "warning");
    handleQueueUpdate();
  } catch (e) {
    showToast(e.message, "danger");
  }
}

async function demoAutoBalance() {
  showToast("Auto-balancing algorithm triggered: Reallocating overflow tokens to Counter 5 & 6", "info");
  runDigitalTwinSimulation();
}

async function demoResetQueue() {
  if (!confirm("Reset active queue backlog to initial baseline?")) return;
  try {
    const res = await apiFetch("/api/admin/demo/reset", {
      method: "POST",
      headers: getAuthHeader()
    });
    showToast(res.message, "info");
    handleQueueUpdate();
  } catch (e) {
    showToast(e.message, "danger");
  }
}

// ---------------- Initialization ----------------
window.addEventListener("DOMContentLoaded", () => {
  const savedTheme = localStorage.getItem("queueless-theme");
  if (savedTheme) document.documentElement.setAttribute("data-theme", savedTheme);

  // Auto-login default demo customer for initial smooth display
  quickSwitchRole("customer@demo.com", "Customer@123", "landing");
  initWebSocket();
});
