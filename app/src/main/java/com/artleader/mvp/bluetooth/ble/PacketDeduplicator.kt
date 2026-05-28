package com.artleader.mvp.bluetooth.ble

class PacketDeduplicator(
    private val maxEntries: Int = DEFAULT_MAX_ENTRIES,
    private val entryTtlMillis: Long = DEFAULT_ENTRY_TTL_MILLIS
) {
    private val seen = LinkedHashMap<Long, Long>()

    @Synchronized
    fun isDuplicate(packetId: Long, now: Long = System.currentTimeMillis()): Boolean {
        prune(now)
        if (seen.containsKey(packetId)) return true
        seen[packetId] = now
        trimToMaxEntries()
        return false
    }

    @Synchronized
    fun pruneStale(now: Long = System.currentTimeMillis()) {
        prune(now)
    }

    private fun prune(now: Long) {
        val iterator = seen.iterator()
        while (iterator.hasNext()) {
            if (now - iterator.next().value > entryTtlMillis) iterator.remove()
        }
    }

    private fun trimToMaxEntries() {
        val iterator = seen.iterator()
        while (seen.size > maxEntries && iterator.hasNext()) {
            iterator.next()
            iterator.remove()
        }
    }

    companion object {
        const val DEFAULT_MAX_ENTRIES = 1_024
        const val DEFAULT_ENTRY_TTL_MILLIS = 10 * 60 * 1_000L
    }
}
