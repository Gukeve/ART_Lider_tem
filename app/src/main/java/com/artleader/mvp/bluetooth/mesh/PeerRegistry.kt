package com.artleader.mvp.bluetooth.mesh

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PeerRegistry {
    private val knownPeers = LinkedHashMap<Long, NearbyPeer>()
    private val _peers = MutableStateFlow<List<NearbyPeer>>(emptyList())
    val peers: StateFlow<List<NearbyPeer>> = _peers.asStateFlow()

    fun upsert(peer: NearbyPeer) {
        knownPeers[peer.peerId] = peer
        _peers.value = knownPeers.values.sortedByDescending { it.lastSeenAt }
    }

    fun get(peerId: Long): NearbyPeer? = knownPeers[peerId]
}
