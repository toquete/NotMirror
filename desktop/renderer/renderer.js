'use strict';

const ipInput      = document.getElementById('ip');
const portInput    = document.getElementById('port');
const statusEl     = document.getElementById('status');
const dotEl        = document.getElementById('dot');
const connectBtn   = document.getElementById('connect-btn');
const disconnectBtn = document.getElementById('disconnect-btn');

connectBtn.addEventListener('click', () => {
    const ip   = ipInput.value.trim();
    const port = portInput.value.trim();
    if (!ip) {
        setStatus('Enter an IP address', 'error');
        return;
    }
    setStatus('Connecting…', '');
    window.notmirrorAPI.connect(`${ip}:${port}`);
});

disconnectBtn.addEventListener('click', () => {
    window.notmirrorAPI.disconnect();
});

window.notmirrorAPI.onStatus((msg) => {
    if (msg === 'connected') {
        setStatus('Connected', 'connected');
    } else if (msg === 'disconnected') {
        setStatus('Disconnected — retrying…', '');
    } else {
        setStatus(msg, 'error');
    }
});

function setStatus(text, state) {
    statusEl.textContent = text;
    dotEl.className = 'dot' + (state ? ` ${state}` : '');
}
