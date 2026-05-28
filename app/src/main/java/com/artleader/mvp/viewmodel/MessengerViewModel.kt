package com.artleader.mvp.viewmodel

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artleader.mvp.bluetooth.mesh.NearbyPeer
import com.artleader.mvp.bluetooth.mesh.PacketType
import com.artleader.mvp.data.local.entity.ConversationEntity
import com.artleader.mvp.data.local.entity.MessageEntity
import com.artleader.mvp.data.repository.MessengerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class MessengerUiState(
    val meshActive: Boolean   = false,
    val statusText: String    = "Инициализация…",
    val selectedChatId: String? = null,
    val error: String?        = null
)

class MessengerViewModel(
    private val repository: MessengerRepository,
    myLogin: String,
    myDisplayName: String
) : ViewModel() {

    private var myLogin: String = myLogin
    private var myDisplayName: String = myDisplayName

    private val _ui = MutableStateFlow(MessengerUiState())
    val ui: StateFlow<MessengerUiState> = _ui.asStateFlow()

    // Nearby peers come directly from BleMessengerService's PeerRegistry
    private val _nearbyPeers = MutableStateFlow<List<NearbyPeer>>(emptyList())
    val nearbyPeers: StateFlow<List<NearbyPeer>> = _nearbyPeers.asStateFlow()

    val chats = repository.observeConversations()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val messages: StateFlow<List<MessageEntity>> = _ui
        .flatMapLatest { state ->
            state.selectedChatId?.let { repository.messagesForChat(it) } ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun updateIdentity(login: String, displayName: String) {
        if (login.isNotBlank()) myLogin = login
        if (displayName.isNotBlank()) myDisplayName = displayName
    }

    // ── BLE Service binding ──────────────────────────────────────────────────
    private var bleService: BleMessengerService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder?) {
            bleService = BleMessengerService.instance
            _ui.value = _ui.value.copy(meshActive = true, statusText = "Поиск рядом…")
            observePeerRegistry()
            observeIncomingPackets()
        }
        override fun onServiceDisconnected(name: ComponentName) {
            bleService = null
            _ui.value = _ui.value.copy(meshActive = false, statusText = "Переподключение…")
        }
    }

    fun bindService(context: Context) {
        val intent = Intent(context, BleMessengerService::class.java).apply {
            putExtra(BleMessengerService.EXTRA_DISPLAY_NAME, myDisplayName)
            putExtra(BleMessengerService.EXTRA_PEER_ID, myLogin.hashCode().toLong())
        }
        context.startForegroundService(intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindService(context: Context) {
        try { context.unbindService(serviceConnection) } catch (_: Exception) {}
    }

    // ── Peer registry observation ────────────────────────────────────────────
    private fun observePeerRegistry() {
        viewModelScope.launch {
            bleService?.peerRegistry?.peers?.collect { peers ->
                _nearbyPeers.value = peers
                val count = peers.count { it.isOnline }
                _ui.value = _ui.value.copy(
                    statusText = if (count > 0) "$count рядом" else "Поиск рядом…"
                )
            }
        }
    }

    // ── Incoming packet observation ───────────────────────────────────────────
    private fun observeIncomingPackets() {
        viewModelScope.launch {
            bleService?.incomingPackets?.collect { packet ->
                if (packet.type != PacketType.MESSAGE) return@collect
                val text     = packet.payload.decodeToString()
                val isMine   = packet.senderId == myLogin.hashCode().toLong()
                val chatId   = deriveChatId(packet.senderId, myLogin.hashCode().toLong())
                val senderName = bleService?.peerRegistry?.get(packet.senderId)?.displayName
                    ?: "Peer"

                // Ensure conversation exists
                ensureConversation(chatId, senderName, packet.senderId)

                repository.saveMessage(
                    MessageEntity(
                        messageId        = UUID.randomUUID().toString(),
                        chatId           = chatId,
                        senderId         = packet.senderId.toString(),
                        senderName       = if (isMine) myDisplayName else senderName,
                        encryptedPayload = text,
                        deliveryState    = "delivered",
                        timestamp        = packet.timestamp,
                        isMine           = isMine,
                        packetId         = packet.packetId,
                        hopCount         = packet.hopCount.toInt()
                    )
                )
            }
        }
    }

    // ── Chat management ──────────────────────────────────────────────────────
    fun openPrivateChat(peer: NearbyPeer) = viewModelScope.launch {
        val chatId = deriveChatId(peer.peerId, myLogin.hashCode().toLong())
        ensureConversation(chatId, peer.displayName, peer.peerId)
        _ui.value = _ui.value.copy(selectedChatId = chatId)
    }

    fun selectChat(chat: ConversationEntity) {
        _ui.value = _ui.value.copy(selectedChatId = chat.conversationId)
    }

    fun onBackFromChat() {
        _ui.value = _ui.value.copy(selectedChatId = null, error = null)
    }

    // ── Sending ──────────────────────────────────────────────────────────────
    fun send(text: String) {
        val chatId = _ui.value.selectedChatId ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            // Persist locally immediately (optimistic)
            val entity = MessageEntity(
                messageId        = UUID.randomUUID().toString(),
                chatId           = chatId,
                senderId         = myLogin,
                senderName       = myDisplayName,
                encryptedPayload = text,
                deliveryState    = "sending",
                timestamp        = System.currentTimeMillis(),
                isMine           = true,
                packetId         = System.currentTimeMillis() // placeholder until BLE assigns one
            )
            repository.saveMessage(entity)
            repository.updateLastMessage(chatId, text, entity.timestamp)
        }

        // BLE broadcast (fire and forget — delivery is best-effort over mesh)
        bleService?.sendMessage(text) ?: run {
            _ui.value = _ui.value.copy(error = "Mesh не активен")
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    /**
     * Deterministic chat ID for a private conversation between two peers.
     * Always the same regardless of which side initiates.
     */
    private fun deriveChatId(peerId1: Long, peerId2: Long): String {
        val sorted = listOf(peerId1, peerId2).sorted()
        return "private_${sorted[0]}_${sorted[1]}"
    }

    private suspend fun ensureConversation(chatId: String, peerName: String, peerId: Long) {
        repository.ensureConversation(
            ConversationEntity(
                conversationId = chatId,
                title          = peerName,
                type           = "private",
                ownerId        = myLogin,
                participantIds = "$myLogin,${peerId}"
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
    }
}
