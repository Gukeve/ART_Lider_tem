package com.artleader.mvp.bluetooth.mesh

import android.util.Base64
import org.json.JSONObject
import java.util.UUID

enum class PacketType { HELLO, MESSAGE, ACK }

data class MeshPacket(
    val senderId: Long,
    val targetId: Long? = null,
    val type: PacketType,
    val payload: ByteArray,
    val timestamp: Long = System.currentTimeMillis(),
    val ttl: Int = DEFAULT_TTL,
    val packetId: Long = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE,
    val hopCount: Int = 0
) {
    fun shouldRelay(maxHopCount: Int = DEFAULT_TTL): Boolean = ttl > 0 && hopCount < maxHopCount

    fun forRelay(): MeshPacket = copy(ttl = (ttl - 1).coerceAtLeast(0), hopCount = hopCount + 1)

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
        const val DEFAULT_TTL = 6

        fun fromJson(json: String): MeshPacket {
            val obj = JSONObject(json)
            return MeshPacket(
                senderId = obj.getLong("senderId"),
                targetId = obj.optLongOrNull("targetId"),
                type = PacketType.valueOf(obj.getString("type")),
                payload = Base64.decode(obj.getString("payload"), Base64.NO_WRAP),
                timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                ttl = obj.optInt("ttl", DEFAULT_TTL),
                packetId = obj.optLong("packetId", UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE),
                hopCount = obj.optInt("hopCount", 0)
            )
        }
    }
}

private fun JSONObject.optLongOrNull(name: String): Long? =
    if (has(name) && !isNull(name)) getLong(name) else null
