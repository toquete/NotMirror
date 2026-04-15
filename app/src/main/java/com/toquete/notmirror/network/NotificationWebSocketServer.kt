package com.toquete.notmirror.network

import android.util.Log
import com.toquete.notmirror.model.NotificationPayload
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArraySet

private const val TAG = "NotificationWSServer"

class NotificationWebSocketServer(port: Int = 8765) : WebSocketServer(InetSocketAddress(port)) {

    private val clients = CopyOnWriteArraySet<WebSocket>()

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
        clients.add(conn)
        Log.d(TAG, "Client connected: ${conn.remoteSocketAddress}")
    }

    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
        clients.remove(conn)
        Log.d(TAG, "Client disconnected: ${conn.remoteSocketAddress} ($reason)")
    }

    override fun onMessage(conn: WebSocket, message: String) {
        // Server only sends; incoming messages are ignored
    }

    override fun onError(conn: WebSocket?, ex: Exception) {
        Log.e(TAG, "WebSocket error on ${conn?.remoteSocketAddress}", ex)
        conn?.let { clients.remove(it) }
    }

    override fun onStart() {
        Log.d(TAG, "WebSocket server started on port ${port}")
    }

    fun broadcast(payload: NotificationPayload) {
        val json = Json.encodeToString(payload)
        clients.forEach { if (it.isOpen) it.send(json) }
    }
}
