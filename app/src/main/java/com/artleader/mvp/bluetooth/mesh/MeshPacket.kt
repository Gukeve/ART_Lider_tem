package com.artleader.mvp.bluetooth.mesh

import android.util.Base64
import org.json.JSONObject
import java.util.UUID

/**
 * Wire-level packet used by the BLE mesh transport.
 *
 * The format is intentionally compact JSON encoded as UTF-8 bytes so packets can
 * be written directly to the Nordic UART RX characteristic and reconstructed by
 * intermediate relays without any Room/UI dependencies.
 */
enum class PacketType { ANNOUNCE, MESSAGE, PING, PONG, HELLO, ACK }

data class MeshPacket(
    val senderId: Long,
    val targetId: Long = BROADCAST_TARGET,
    val type: PacketType,
    val payload: ByteArray = ByteArray(0),
    val timestamp: Long = System.currentTimeMillis(),
    val ttl: Int = DEFAULT_TTL,
    val packetId: Long = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE,
    val hopCount: Int = 0
) {
    val isBroadcast: Boolean
        get() = targetId == BROADCAST_TARGET

    val shouldRelay: Boolean
        get() = ttl > 0 && hopCount < DEFAULT_TTL

    fun relayed(): MeshPacket = copy(
        ttl = (ttl - 1).coerceAtLeast(0),
        hopCount = hopCount + 1
    )

    fun forRelay(): MeshPacket = relayed()

    fun toBytes(): ByteArray = toJson().encodeToByteArray()

    fun toJson(): String = JSONObject()
        .put("packetId", packetId)
        .put("senderId", senderId)
        .put("targetId", targetId)
        .put("type", type.name)
        .put("payload", Base64.encodeToString(payload, Base64.NO_WRAP))
        .put("timestamp", timestamp)
        .put("ttl", ttl)
        .put("hopCount", hopCount)
        .toString()

    companion object {
        const val BROADCAST_TARGET: Long = -1L
        const val DEFAULT_TTL = 6
        private const val MAX_WIRE_BYTES = 4_096

        fun create(
            type: PacketType,
            senderId: Long,
            targetId: Long = BROADCAST_TARGET,
            payload: ByteArray = ByteArray(0),
            ttl: Int = DEFAULT_TTL
        ): MeshPacket = MeshPacket(
            senderId = senderId,
            targetId = targetId,
            type = type,
            payload = payload,
            ttl = ttl
        )

        fun fromBytes(bytes: ByteArray): MeshPacket? {
            if (bytes.isEmpty() || bytes.size > MAX_WIRE_BYTES) return null
            return runCatching { fromJson(bytes.decodeToString()) }.getOrNull()
        }

        fun fromJson(json: String): MeshPacket {
            val obj = JSONObject(json)
            return MeshPacket(
                senderId = obj.getLong("senderId"),
                targetId = obj.optLong("targetId", BROADCAST_TARGET),
                type = PacketType.valueOf(obj.getString("type")),
                payload = Base64.decode(obj.optString("payload", ""), Base64.NO_WRAP),
                timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                ttl = obj.optInt("ttl", DEFAULT_TTL),
                packetId = obj.optLong(
                    "packetId",
                    UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE
                ),
                hopCount = obj.optInt("hopCount", 0)
            )
        }
    }
}
