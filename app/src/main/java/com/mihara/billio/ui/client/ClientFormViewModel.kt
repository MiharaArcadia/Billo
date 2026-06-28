package com.mihara.billio.ui.client

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mihara.billio.data.db.entity.Client
import com.mihara.billio.data.repository.ClientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientFormViewModel @Inject constructor(
    private val repository: ClientRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val id: Long = savedStateHandle.get<Long>("id") ?: 0L

    private val _client = MutableStateFlow(Client(name = ""))
    val client: StateFlow<Client> = _client

    init {
        if (id != 0L) viewModelScope.launch { repository.get(id)?.let { _client.value = it } }
    }

    fun update(transform: (Client) -> Client) { _client.value = transform(_client.value) }

    val isValid: Boolean get() = _client.value.name.isNotBlank()

    fun save(onSaved: () -> Unit) {
        if (!isValid) return
        viewModelScope.launch {
            repository.save(_client.value)
            onSaved()
        }
    }
}
