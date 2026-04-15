'use strict';

const { app, BrowserWindow, ipcMain, Notification } = require('electron');
const path = require('path');
const WebSocket = require('ws');

let mainWindow;
let ws = null;
let reconnectTimer = null;
let targetIpPort = null;

const RECONNECT_DELAY_MS = 5000;

function createWindow() {
    mainWindow = new BrowserWindow({
        width: 460,
        height: 340,
        resizable: false,
        webPreferences: {
            nodeIntegration: false,
            contextIsolation: true,
            preload: path.join(__dirname, 'preload.js')
        }
    });
    mainWindow.loadFile(path.join(__dirname, 'renderer', 'index.html'));
    mainWindow.on('closed', () => { mainWindow = null; });
}

function sendStatus(msg) {
    if (mainWindow && !mainWindow.isDestroyed()) {
        mainWindow.webContents.send('status', msg);
    }
}

function connect(ipPort) {
    targetIpPort = ipPort;
    ws = new WebSocket(`ws://${ipPort}`);

    ws.on('open', () => {
        clearTimeout(reconnectTimer);
        sendStatus('connected');
    });

    ws.on('message', (data) => {
        try {
            const payload = JSON.parse(data.toString());
            new Notification({
                title: `${payload.appName}: ${payload.title}`,
                body: payload.text
            }).show();
        } catch (e) {
            console.error('Failed to parse notification payload', e);
        }
    });

    ws.on('close', () => {
        sendStatus('disconnected');
        scheduleReconnect();
    });

    ws.on('error', (err) => {
        sendStatus(`error: ${err.message}`);
        ws.terminate();
        // 'close' fires after error; reconnect is scheduled there
    });
}

function scheduleReconnect() {
    if (!targetIpPort) return;
    reconnectTimer = setTimeout(() => {
        if (targetIpPort) connect(targetIpPort);
    }, RECONNECT_DELAY_MS);
}

ipcMain.on('connect', (_, ipPort) => {
    clearTimeout(reconnectTimer);
    if (ws) {
        ws.terminate();
        ws = null;
    }
    connect(ipPort);
});

ipcMain.on('disconnect', () => {
    targetIpPort = null;
    clearTimeout(reconnectTimer);
    if (ws) {
        ws.terminate();
        ws = null;
    }
    sendStatus('disconnected');
});

app.whenReady().then(() => {
    createWindow();
    app.on('activate', () => {
        if (BrowserWindow.getAllWindows().length === 0) createWindow();
    });
});

app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') app.quit();
});
