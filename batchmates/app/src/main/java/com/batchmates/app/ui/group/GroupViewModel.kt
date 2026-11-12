package com.batchmates.app.ui.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batchmates.app.data.AppUser
import com.batchmates.app.data.Group
import com.batchmates.app.data.Member
import com.batchmates.app.data.Message
import com.batchmates.app.data.ReunionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GroupViewModel(
    private val repo: ReunionRepository,
    val user: AppUser,
    private val groupId: String,
) : ViewModel() {

    val group: StateFlow<Group?> = repo.group(groupId).catch { emit(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val members: StateFlow<List<Member>> = repo.members(groupId).catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val messages: StateFlow<List<Message>> = repo.messages(groupId).catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val sending = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)

    fun send(text: String, onSent: () -> Unit) {
        if (text.isBlank() || sending.value) return
        sending.value = true
        viewModelScope.launch {
            repo.sendMessage(groupId, text, user)
                .onSuccess { onSent() }
                .onFailure { error.value = it.message }
            sending.value = false
        }
    }

    fun setWhatsapp(link: String) {
        val clean = link.trim()
        if (clean.isNotEmpty() && !clean.startsWith("https://chat.whatsapp.com/")) {
            error.value = "Paste a WhatsApp group invite link (https://chat.whatsapp.com/…)"; return
        }
        viewModelScope.launch { repo.setWhatsappLink(groupId, clean).onFailure { error.value = it.message } }
    }

    fun leave(onDone: () -> Unit) {
        viewModelScope.launch { repo.leaveGroup(groupId, user).onSuccess { onDone() }.onFailure { error.value = it.message } }
    }
}
