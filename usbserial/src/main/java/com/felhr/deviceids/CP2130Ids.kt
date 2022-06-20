package com.felhr.deviceids

import com.felhr.deviceids.CP2130Ids

object CP2130Ids {
    private val cp2130Devices = Helpers.createTable(
        Helpers.createDevice(0x10C4, 0x87a0)
    )

    fun isDeviceSupported(vendorId: Int, productId: Int): Boolean {
        return Helpers.exists(cp2130Devices, vendorId, productId)
    }
}