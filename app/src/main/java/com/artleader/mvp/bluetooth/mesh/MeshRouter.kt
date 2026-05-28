package com.artleader.mvp.bluetooth.mesh

class MeshRouter(
    private val localPeerId: Long,
    private val onRelay: (MeshPacket) -> Unit,
    private val onDeliver: (MeshPacket) -> Unit
) {
    private val seenPackets = LinkedHashSet<Long>()

    fun handle(packet: MeshPacket) {
        if (!seenPackets.add(packet.packetId)) return
        if (packet.senderId == localPeerId) return

        if (packet.isBroadcast || packet.targetId == localPeerId) {
            onDeliver(packet)
        }
        if (packet.shouldRelay) {
            onRelay(packet.relayed())
        }
    }
}
