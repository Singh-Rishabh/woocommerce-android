package com.woocommerce.android.ui.woopos.home.items.variations

import android.util.LruCache
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class VariationsLRUCache<K, V>(private val maxSize: Int) {

    private val cache = LruCache<K, V>(maxSize)
    private val mutex = Mutex()

    suspend fun get(key: K): V? {
        return mutex.withLock {
            cache.get(key)
        }
    }

    suspend fun put(key: K, value: V) {
        mutex.withLock {
            cache.put(key, value)
        }
    }

    suspend fun remove(key: K) {
        mutex.withLock {
            cache.remove(key)
        }
    }

    suspend fun clear() {
        mutex.withLock {
            cache.evictAll()
        }
    }

    suspend fun containsKey(key: K): Boolean {
        return mutex.withLock {
            cache.get(key) != null
        }
    }
}
