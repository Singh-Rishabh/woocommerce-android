package com.woocommerce.android.ui.woopos.home.items.variations

import android.util.LruCache
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class VariationsLRUCache<K, V> @Inject constructor(maxSize: Int) {

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
}
