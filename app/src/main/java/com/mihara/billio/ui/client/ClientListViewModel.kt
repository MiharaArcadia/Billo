package com.mihara.billio.ui.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mihara.billio.data.db.entity.Client
import com.mihara.billio.data.repository.ClientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientListViewModel @Inject constructor(
    private val repository: ClientRepository
) : ViewModel() {
    val clients: StateFlow<List<Client>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Invokes [onResult] with false if the client still has open invoices. */
    fun delete(client: Client, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        onResult(repository.delete(client))
    }
}
