package com.batchmates.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batchmates.app.data.AppUser
import com.batchmates.app.data.Group
import com.batchmates.app.data.ReunionRepository
import com.batchmates.app.util.InviteCodes
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val repo: ReunionRepository, private val user: AppUser) : ViewModel() {

    /** null while loading. */
    val groups: StateFlow<List<Group>?> = repo.myGroups(user.uid)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val joining = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)
    private val _opened = Channel<String>(Channel.BUFFERED)
    val openGroup = _opened.receiveAsFlow()

    fun joinByCode(input: String) {
        if (!InviteCodes.isPlausible(input)) { error.value = "Invite codes have 6 letters/numbers"; return }
        if (joining.value) return
        joining.value = true
        error.value = null
        viewModelScope.launch {
            repo.joinByInviteCode(input, user)
                .onSuccess { _opened.send(it.id) }
                .onFailure { error.value = it.message ?: "Couldn't join with that code" }
            joining.value = false
        }
    }
}
