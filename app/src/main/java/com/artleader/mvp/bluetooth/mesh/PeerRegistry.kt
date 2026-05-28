package com.artleader.mvp.bluetooth.mesh

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PeerRegistry(
    private val stalePeerMillis: Long = DEFAULT_STALE_PEER_MILLIS
) {
    private val knownPeers = LinkedHashMap<Long, NearbyPeer>()
    private val _peers = MutableStateFlow<List<NearbyPeer>>(emptyList())
    val peers: StateFlow<List<NearbyPeer>> = _peers.asStateFlow()

    @Synchronized
    fun upsert(peer: NearbyPeer) {
        val current = knownPeers[peer.peerId]
        knownPeers[peer.peerId] = peer.copy(
            peerIdHex = peer.peerIdHex.ifBlank { peer.peerId.toString(16) },
            displayName = peer.displayName.ifBlank { current?.displayName ?: "Peer" },
            rssi = if (peer.rssi == Int.MIN_VALUE) current?.rssi ?: peer.rssi else peer.rssi,
            isOnline = true,
            lastSeenAt = peer.lastSeenAt
        )
        publish()
    }

    @Synchronized
    fun get(peerId: Long): NearbyPeer? = knownPeers[peerId]

    @Synchronized
    fun markOffline(peerId: Long) {
        val peer = knownPeers[peerId] ?: return
        knownPeers[peerId] = peer.copy(isOnline = false)
        publish()
    }

    @Synchronized
    fun pruneStale(now: Long = System.currentTimeMillis()) {
        var changed = false
        knownPeers.entries.forEach { entry ->
            val peer = entry.value
            if (peer.isOnline && now - peer.lastSeenAt > stalePeerMillis) {
                entry.setValue(peer.copy(isOnline = false))
                changed = true
            }
        }
        if (changed) publish()
    }

    private fun publish() {
        _peers.value = knownPeers.values.sortedWith(
            compareByDescending<NearbyPeer> { it.isOnline }.thenByDescending { it.lastSeenAt }
        )
    }

    companion object {
        const val DEFAULT_STALE_PEER_MILLIS = 45_000L
    }
}
