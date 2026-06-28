package com.mihara.billio.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mihara.billio.data.model.TaxMode
import com.mihara.billio.data.prefs.SettingsRepository
import com.mihara.billio.data.prefs.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _draft = MutableStateFlow(UserSettings())
    val draft: StateFlow<UserSettings> = _draft

    init {
        viewModelScope.launch { _draft.value = settingsRepository.settings.first() }
    }

    fun update(transform: (UserSettings) -> UserSettings) {
        _draft.value = transform(_draft.value)
    }

    val isValid: Boolean
        get() = with(_draft.value) {
            name.isNotBlank() && street.isNotBlank() && zip.isNotBlank() &&
                city.isNotBlank() && iban.isNotBlank() &&
                (taxMode == TaxMode.SMALL_BUSINESS || vatId.isNotBlank())
        }

    fun finish(onDone: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.save(_draft.value.copy(onboardingComplete = true))
            onDone()
        }
    }
}
