package com.felhr.deviceids

import java.util.*

internal object Helpers {
    /**
     * Create a device id, since they are 4 bytes each, we can pack the pair in an long.
     */
    @JvmStatic
    fun createDevice(vendorId: Int, productId: Int): Long {
        return vendorId.toLong() shl 32 or (productId and 0xFFFFFFFFL.toInt()).toLong()
    }

    /**
     * Creates a sorted table.
     * This way, we can use binarySearch to find whether the entry exists.
     */
    @JvmStatic
    fun createTable(vararg entries: Long): LongArray {
        Arrays.sort(entries)
        return entries
    }

    @JvmStatic
    fun exists(devices: LongArray?, vendorId: Int, productId: Int): Boolean {
        return Arrays.binarySearch(devices, createDevice(vendorId, productId)) >= 0
    }
}