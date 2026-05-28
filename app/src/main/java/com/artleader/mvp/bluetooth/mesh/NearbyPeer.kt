package com.artleader.mvp.bluetooth.mesh

data class NearbyPeer(
    val peerId: Long,
    val peerIdHex: String = peerId.toString(16),
    val displayName: String,
    val rssi: Int = Int.MIN_VALUE,
    val isOnline: Boolean = true,
    val lastSeenAt: Long = System.currentTimeMillis()
)
