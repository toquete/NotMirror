package com.toquete.notmirror.model

import kotlinx.serialization.Serializable

@Serializable
data class NotificationPayload(
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val timestamp: Long
)
