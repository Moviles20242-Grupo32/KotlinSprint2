package com.example.foodies.model

import android.util.LruCache

object LruCashingManager {

    // Instancia LRU Cache
    val lruCashing: LruCache<String, String> by lazy {
        val cacheSize = 4 * 1024 * 1024 // 4 MiB
        LruCache(cacheSize)
    }
}