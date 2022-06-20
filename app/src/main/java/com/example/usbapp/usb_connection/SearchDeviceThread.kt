package `in`.sunfox.android.spandanEngine.usb_connection

import android.hardware.usb.UsbManager
import com.example.usbapp.usb_connection.ConstantHelper


class SearchDeviceThread(private val usbManager: UsbManager) : Thread() {
    private var deviceFound = false
    override fun run() {
        try {
            val usbDevices = usbManager.deviceList
            if (usbDevices.isNotEmpty()) {
                for ((_, device) in usbDevices) {
                    val deviceVID = device.vendorId
                    val devicePID = device.productId
                    if (deviceVID == ConstantHelper.Devices.STM103_VID && devicePID == ConstantHelper.Devices.STM103_PID || deviceVID == ConstantHelper.Devices.STM302_VID && devicePID == ConstantHelper.Devices.STM302_PID) {
                        UsbConnectionHelper.setUsbDevice(device)
                        deviceFound = true
                        break
                    }
                }
            }
            if (!deviceFound) {
                UsbConnectionHelper.setUsbDevice(null)
            }
            UsbConnectionHelper.onSearchComplete()
            /*else {
                UsbConnectionHelper.setUsbDevice(null);
            }*/

            //UsbConnectionHelper.onSearchComplete();
        } catch (ie: Exception) {
            //UsbConnectionHelper.onSearchComplete();
        } finally {
            UsbConnectionHelper.onSearchComplete()
        }
    }
}