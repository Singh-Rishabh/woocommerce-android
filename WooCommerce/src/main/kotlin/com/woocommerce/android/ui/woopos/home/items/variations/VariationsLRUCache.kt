package com.woocommerce.android.ui.woopos.home.items.variations

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class VariationsLRUCache<K, V>(private val maxSize: Int) {

    companion object {
        private const val LOAD_FACTOR = 0.75f
    }
    private val cache = object : LinkedHashMap<K, V>(maxSize, LOAD_FACTOR, true) {
        override fun removeEldestEntry(eldest: Map.Entry<K, V>): Boolean {
            return size > maxSize
        }
    }

    private val mutex = Mutex()

    suspend fun get(key: K): V? {
        return mutex.withLock {
            cache[key]
        }
    }

    suspend fun put(key: K, value: V) {
        mutex.withLock {
            cache[key] = value
        }
    }

    suspend fun remove(key: K) {
        mutex.withLock {
            cache.remove(key)
        }
    }

    suspend fun clear() {
        mutex.withLock {
            cache.clear()
        }
    }

    suspend fun containsKey(key: K): Boolean {
        return mutex.withLock {
            cache.containsKey(key)
        }
    }
}
