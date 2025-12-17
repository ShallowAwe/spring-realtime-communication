// State management
const state = {
    websocket: { active: false, count: 0, socket: null },
    sse: { active: false, count: 0, eventSource: null },
    shortPoll: { active: false, count: 0, interval: null },
    longPoll: { active: false, count: 0, lastValue: 0, polling: false }
};

// Utility: Add log entry
function addLog(protocol, message, type = 'info') {
    const logsContainer = document.getElementById('connectionLogs');
    const emptyState = logsContainer.querySelector('.empty-state');
    if (emptyState) emptyState.remove();

    const logEntry = document.createElement('div');
    logEntry.className = `log-entry ${type}`;
    
    const now = new Date();
    const time = now.toLocaleTimeString();
    
    const protocolLabels = {
        'websocket': { class: 'ws', text: 'WS' },
        'sse': { class: 'sse', text: 'SSE' },
        'short': { class: 'sp', text: 'SP' },
        'long': { class: 'lp', text: 'LP' }
    };
    
    const label = protocolLabels[protocol] || { class: 'info', text: protocol.toUpperCase() };
    
    logEntry.innerHTML = `
        <span class="log-time">${time}</span>
        <span class="log-protocol ${label.class}">${label.text}</span>
        ${message}
    `;
    
    logsContainer.insertBefore(logEntry, logsContainer.firstChild);
    
    // Keep only last 100 logs
    while (logsContainer.children.length > 100) {
        logsContainer.removeChild(logsContainer.lastChild);
    }
}

// Utility: Add data entry
function addData(protocol, data) {
    const dataContainer = document.getElementById('receivedData');
    const emptyState = dataContainer.querySelector('.empty-state');
    if (emptyState) emptyState.remove();

    const dataEntry = document.createElement('div');
    
    const protocolLabels = {
        'websocket': { class: 'ws', text: 'WebSocket' },
        'sse': { class: 'sse', text: 'SSE' },
        'short': { class: 'sp', text: 'Short Poll' },
        'long': { class: 'lp', text: 'Long Poll' }
    };
    
    const label = protocolLabels[protocol] || { class: 'ws', text: protocol };
    dataEntry.className = `data-entry ${label.class}`;
    
    const now = new Date();
    const time = now.toLocaleTimeString();
    
    let formattedData;
    try {
        const parsed = typeof data === 'string' ? JSON.parse(data) : data;
        formattedData = JSON.stringify(parsed, null, 2);
    } catch {
        formattedData = typeof data === 'string' ? data : JSON.stringify(data);
    }
    
    dataEntry.innerHTML = `
        <div class="data-header">
            <span class="data-protocol ${label.class}">${label.text}</span>
            <span class="data-time">${time}</span>
        </div>
        <pre>${formattedData}</pre>
    `;
    
    dataContainer.insertBefore(dataEntry, dataContainer.firstChild);
    
    // Keep only last 50 data entries
    while (dataContainer.children.length > 50) {
        dataContainer.removeChild(dataContainer.lastChild);
    }
}

// Utility: Update status
function updateStatus(protocol, active, count) {
    const statusMap = {
        'websocket': 'ws',
        'sse': 'sse',
        'short': 'sp',
        'long': 'lp'
    };
    
    const prefix = statusMap[protocol];
    const statusBadge = document.getElementById(`${prefix}-status`);
    const countElement = document.getElementById(`${prefix}-count`);
    
    if (statusBadge) {
        statusBadge.className = `status-badge ${active ? 'active' : 'inactive'}`;
        statusBadge.textContent = active ? 'Active' : 'Inactive';
    }
    
    if (countElement) {
        countElement.textContent = count;
    }
}

// Protocol toggle
function toggleProtocol(protocol) {
    const btn = document.getElementById(`btn-${protocol}`);
    const isActive = btn.classList.contains('active');
    
    if (isActive) {
        stopProtocol(protocol);
    } else {
        startProtocol(protocol);
    }
}

// Start protocol
function startProtocol(protocol) {
    const btn = document.getElementById(`btn-${protocol}`);
    btn.classList.add('active');
    
    switch(protocol) {
        case 'websocket':
            wsConnect();
            break;
        case 'sse':
            sseConnect();
            break;
        case 'short':
            shortPollStart();
            break;
        case 'long':
            longPollStart();
            break;
    }
}

// Stop protocol
function stopProtocol(protocol) {
    const btn = document.getElementById(`btn-${protocol}`);
    btn.classList.remove('active');
    
    switch(protocol) {
        case 'websocket':
            wsDisconnect();
            break;
        case 'sse':
            sseDisconnect();
            break;
        case 'short':
            shortPollStop();
            break;
        case 'long':
            longPollStop();
            break;
    }
}

// ============= WebSocket Implementation =============
function wsConnect() {
    if (state.websocket.socket && state.websocket.socket.readyState === WebSocket.OPEN) {
        addLog('websocket', 'Already connected', 'warning');
        return;
    }

    addLog('websocket', `Connecting to ${WS_URL}...`, 'info');
    
    state.websocket.socket = new WebSocket(WS_URL);

    state.websocket.socket.onopen = () => {
        state.websocket.active = true;
        updateStatus('websocket', true, state.websocket.count);
        addLog('websocket', 'Connected successfully', 'success');
    };

    state.websocket.socket.onmessage = (event) => {
        state.websocket.count++;
        updateStatus('websocket', true, state.websocket.count);
        addData('websocket', event.data);
    };

    state.websocket.socket.onerror = (error) => {
        addLog('websocket', `Error: ${error.message || 'Connection failed'}`, 'error');
    };

    state.websocket.socket.onclose = (event) => {
        state.websocket.active = false;
        updateStatus('websocket', false, state.websocket.count);
        addLog('websocket', `Disconnected (code: ${event.code})`, 'warning');
    };
}

function wsDisconnect() {
    if (state.websocket.socket) {
        addLog('websocket', 'Manually disconnecting...', 'info');
        state.websocket.socket.close(1000, 'User requested disconnect');
        state.websocket.socket = null;
        state.websocket.active = false;
        updateStatus('websocket', false, state.websocket.count);
    }
}

// ============= SSE Implementation =============
function sseConnect() {
    if (state.sse.eventSource) {
        addLog('sse', 'Already connected', 'warning');
        return;
    }

    addLog('sse', `Connecting to ${SSE_URL}...`, 'info');
    
    state.sse.eventSource = new EventSource(SSE_URL);

    state.sse.eventSource.onopen = () => {
        state.sse.active = true;
        updateStatus('sse', true, state.sse.count);
        addLog('sse', 'Connected successfully', 'success');
    };

    state.sse.eventSource.onmessage = (event) => {
        state.sse.count++;
        updateStatus('sse', true, state.sse.count);
        addData('sse', event.data);
    };

    state.sse.eventSource.onerror = (error) => {
        state.sse.active = false;
        updateStatus('sse', false, state.sse.count);
        addLog('sse', 'Connection error occurred', 'error');
        sseDisconnect();
    };
}

function sseDisconnect() {
    if (state.sse.eventSource) {
        addLog('sse', 'Disconnecting...', 'info');
        state.sse.eventSource.close();
        state.sse.eventSource = null;
        state.sse.active = false;
        updateStatus('sse', false, state.sse.count);
    }
}

// ============= Short Polling Implementation =============
function shortPollStart() {
    if (state.shortPoll.interval) {
        addLog('short', 'Already polling', 'warning');
        return;
    }

    addLog('short', 'Starting short polling (2s interval)...', 'info');
    state.shortPoll.active = true;
    updateStatus('short', true, state.shortPoll.count);

    const poll = async () => {
        try {
            const response = await fetch(SHORT_POLL_URL);
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            
            const data = await response.json();
            state.shortPoll.count++;
            updateStatus('short', true, state.shortPoll.count);
            addData('short', data);
        } catch (error) {
            addLog('short', `Error: ${error.message}`, 'error');
        }
    };

    // Initial poll
    poll();
    
    // Poll every 2 seconds
    state.shortPoll.interval = setInterval(poll, 2000);
}

function shortPollStop() {
    if (state.shortPoll.interval) {
        addLog('short', 'Stopping short polling...', 'info');
        clearInterval(state.shortPoll.interval);
        state.shortPoll.interval = null;
        state.shortPoll.active = false;
        updateStatus('short', false, state.shortPoll.count);
    }
}

// ============= Long Polling Implementation =============
function longPollStart() {
    if (state.longPoll.polling) {
        addLog('long', 'Already polling', 'warning');
        return;
    }

    addLog('long', 'Starting long polling...', 'info');
    state.longPoll.active = true;
    state.longPoll.polling = true;
    updateStatus('long', true, state.longPoll.count);

    longPoll();
}

async function longPoll() {
    if (!state.longPoll.polling) return;

    try {
        const url = `${LONG_POLL_URL}?lastValue=${state.longPoll.lastValue}`;
        const response = await fetch(url);
        
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        
        const data = await response.json();
        state.longPoll.count++;
        state.longPoll.lastValue = data.counter || state.longPoll.lastValue;
        updateStatus('long', true, state.longPoll.count);
        addData('long', data);
        
        // Continue polling
        if (state.longPoll.polling) {
            setTimeout(longPoll, 100);
        }
    } catch (error) {
        addLog('long', `Error: ${error.message}`, 'error');
        if (state.longPoll.polling) {
            setTimeout(longPoll, 2000); // Retry after 2s on error
        }
    }
}

function longPollStop() {
    if (state.longPoll.polling) {
        addLog('long', 'Stopping long polling...', 'info');
        state.longPoll.polling = false;
        state.longPoll.active = false;
        updateStatus('long', false, state.longPoll.count);
    }
}

// ============= Global Controls =============
function startAll() {
    addLog('global', 'Starting all protocols...', 'info');
    startProtocol('websocket');
    startProtocol('sse');
    startProtocol('short');
    startProtocol('long');
}

function stopAll() {
    addLog('global', 'Stopping all protocols...', 'info');
    stopProtocol('websocket');
    stopProtocol('sse');
    stopProtocol('short');
    stopProtocol('long');
}

function clearAllLogs() {
    document.getElementById('connectionLogs').innerHTML = `
        <div class="empty-state">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
            </svg>
            <p>No connection logs yet</p>
        </div>
    `;
}

function clearAllData() {
    document.getElementById('receivedData').innerHTML = `
        <div class="empty-state">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z"></path>
            </svg>
            <p>No data received yet</p>
        </div>
    `;
}

// Initialize on page load
window.addEventListener('load', () => {
    addLog('global', 'Dashboard initialized successfully', 'success');
    addLog('global', `WebSocket URL: ${WS_URL}`, 'info');
    addLog('global', `SSE URL: ${SSE_URL}`, 'info');
    addLog('global', `Short Polling URL: ${SHORT_POLL_URL}`, 'info');
    addLog('global', `Long Polling URL: ${LONG_POLL_URL}`, 'info');
});