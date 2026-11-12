package com.batchmates.app.ui.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batchmates.app.data.AppUser
import com.batchmates.app.data.Group
import com.batchmates.app.data.Member
import com.batchmates.app.data.Message
import com.batchmates.app.data.MissingPerson
import com.batchmates.app.data.Reunion
import com.batchmates.app.data.ReunionRepository
import com.batchmates.app.util.Prompts
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

    private fun <T> Flow0(flow: kotlinx.coroutines.flow.Flow<T>, initial: T): StateFlow<T> =
        flow.catch { emit(initial) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)

    val group: StateFlow<Group?> = Flow0(repo.group(groupId), null)
    val members: StateFlow<List<Member>> = Flow0(repo.members(groupId), emptyList())
    val messages: StateFlow<List<Message>> = Flow0(repo.messages(groupId), emptyList())
    val missing: StateFlow<List<MissingPerson>> = Flow0(repo.missing(groupId), emptyList())
    val reunion: StateFlow<Reunion?> = Flow0(repo.reunion(groupId), null)

    val sending = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)
    /** Current throwback question on the wall; 🎲 rolls a new one. */
    val prompt = MutableStateFlow(Prompts.random())

    fun send(text: String, onSent: () -> Unit) {
        if (text.isBlank() || sending.value) return
        sending.value = true
        viewModelScope.launch {
            repo.sendMessage(groupId, text, user).onSuccess { onSent() }.onFailure { error.value = it.message }
            sending.value = false
        }
    }

    fun rollPrompt() { prompt.value = Prompts.random(except = prompt.value) }

    fun answerPrompt(answer: String, onSent: () -> Unit) {
        if (answer.isBlank()) return
        viewModelScope.launch {
            repo.sendPrompt(groupId, prompt.value, answer, user)
                .onSuccess { onSent(); rollPrompt() }
                .onFailure { error.value = it.message }
        }
    }

    fun toggleReaction(m: Message, emoji: String) {
        val on = user.uid !in m.reactions[emoji].orEmpty()
        viewModelScope.launch { repo.react(groupId, m.id, emoji, user, on) }
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

    // ---- people ----

    fun saveCard(city: String, now: String, memory: String) {
        viewModelScope.launch { repo.updateMyCard(groupId, user, city, now, memory).onFailure { error.value = it.message } }
    }

    fun sharedInvite() { viewModelScope.launch { repo.recordShare(groupId, user) } }

    fun addMissing(name: String, note: String) {
        if (name.trim().length < 2) return
        viewModelScope.launch { repo.addMissing(groupId, name, note, user).onFailure { error.value = it.message } }
    }

    fun invitedMissing(p: MissingPerson) {
        viewModelScope.launch { repo.markInvited(groupId, p.id); repo.recordShare(groupId, user) }
    }

    fun removeMissing(p: MissingPerson) { viewModelScope.launch { repo.removeMissing(groupId, p.id) } }

    // ---- reunion ----

    fun proposeReunion(place: String, dates: List<Long>) {
        if (dates.isEmpty()) { error.value = "Pick at least one date"; return }
        viewModelScope.launch { repo.proposeReunion(groupId, place, dates.distinct().sorted(), user).onFailure { error.value = it.message } }
    }

    fun vote(i: Int) { viewModelScope.launch { repo.vote(groupId, i, user) } }
    fun rsvp(status: String) { viewModelScope.launch { repo.rsvp(groupId, status, user) } }
    fun lock(date: Long?) { viewModelScope.launch { repo.lockReunion(groupId, date) } }
}
