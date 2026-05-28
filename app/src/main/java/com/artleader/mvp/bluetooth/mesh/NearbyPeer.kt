package com.artleader.mvp.bluetooth.mesh

data class NearbyPeer(
    val peerId: Long,
    val displayName: String,
    val isOnline: Boolean = true,
    val lastSeenAt: Long = System.currentTimeMillis()
)
