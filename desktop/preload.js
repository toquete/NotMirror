'use strict';

const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('notmirrorAPI', {
    connect: (ipPort) => ipcRenderer.send('connect', ipPort),
    disconnect: () => ipcRenderer.send('disconnect'),
    onStatus: (cb) => ipcRenderer.on('status', (_, msg) => cb(msg))
});
