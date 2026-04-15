package com.toquete.notmirror.ui

import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import com.toquete.notmirror.data.AppInfo

@Composable
fun AppListItem(app: AppInfo, onToggle: (Boolean) -> Unit) {
    ListItem(
        headlineContent = {
            Text(text = app.appName, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Text(
                text = app.packageName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        trailingContent = {
            Switch(
                checked = app.isAllowed,
                onCheckedChange = onToggle
            )
        }
    )
}
