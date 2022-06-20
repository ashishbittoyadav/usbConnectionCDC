package com.felhr.deviceids

import com.felhr.deviceids.Helpers.createTable
import com.felhr.deviceids.Helpers.createDevice
import com.felhr.deviceids.Helpers.exists
import com.felhr.deviceids.XdcVcpIds

object XdcVcpIds {
    /*
	 * Werner Wolfrum (w.wolfrum@wolfrum-elektronik.de)
	 */
    /* Different products and vendors of XdcVcp family
    */
    private val xdcvcpDevices = createTable(
        createDevice(0x264D, 0x0232),  // VCP (Virtual Com Port)
        createDevice(0x264D, 0x0120),  // USI (Universal Sensor Interface)
        createDevice(0x0483, 0x5740) //CC3D (STM)
    )

    fun isDeviceSupported(vendorId: Int, productId: Int): Boolean {
        return exists(xdcvcpDevices, vendorId, productId)
    }
}