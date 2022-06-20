package com.felhr.deviceids

import com.felhr.deviceids.CH34xIds

object CH34xIds {
    private val ch34xDevices = Helpers.createTable(
        Helpers.createDevice(0x4348, 0x5523),
        Helpers.createDevice(0x1a86, 0x7523),
        Helpers.createDevice(0x1a86, 0x5523),
        Helpers.createDevice(0x1a86, 0x0445)
    )

    fun isDeviceSupported(vendorId: Int, productId: Int): Boolean {
        return Helpers.exists(ch34xDevices, vendorId, productId)
    }
}