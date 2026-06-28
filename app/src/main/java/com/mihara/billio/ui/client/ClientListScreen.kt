package com.mihara.billio.ui.client

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mihara.billio.R
import com.mihara.billio.data.db.entity.Client
import com.mihara.billio.ui.components.ConfirmDialog
import com.mihara.billio.ui.components.EmptyState
import com.mihara.billio.ui.components.SwipeAction
import com.mihara.billio.ui.components.SwipeableCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientListScreen(
    onOpen: (Long) -> Unit,
    onCreate: () -> Unit,
    viewModel: ClientListViewModel = hiltViewModel()
) {
    val clients by viewModel.clients.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var pendingDelete by remember { mutableStateOf<Client?>(null) }
    val cannotDeleteMsg = stringResource(R.string.cannot_delete_client)

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_clients)) }) },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreate) { Icon(Icons.Default.Add, null) }
        }
    ) { padding ->
        if (clients.isEmpty()) {
            EmptyState(emoji = "👥", title = stringResource(R.string.empty_clients), modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                items(clients, key = { it.id }) { client ->
                    SwipeableCard(
                        leftAction = SwipeAction(Icons.Default.Delete, Color(0xFFFF5252)) {
                            pendingDelete = client
                        }
                    ) {
                        ClientRow(client, onClick = { onOpen(client.id) })
                    }
                }
            }
        }
    }

    pendingDelete?.let { client ->
        ConfirmDialog(
            title = stringResource(R.string.confirm_delete_title),
            message = stringResource(R.string.confirm_delete_message),
            onConfirm = {
                viewModel.delete(client) { ok ->
                    if (!ok) scope.launch { snackbar.showSnackbar(cannotDeleteMsg) }
                }
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null }
        )
    }
}

@Composable
private fun ClientRow(client: Client, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                client.company?.takeIf { it.isNotBlank() } ?: client.name,
                style = MaterialTheme.typography.titleMedium
            )
            val subtitle = listOfNotNull(
                client.company?.takeIf { it.isNotBlank() }?.let { client.name },
                "${client.zip} ${client.city}".trim()
            ).filter { it.isNotBlank() }.joinToString(" · ")
            if (subtitle.isNotBlank()) {
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
