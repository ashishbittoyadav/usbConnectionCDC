package `in`.sunfox.android.spandanEngine.usb_connection

interface UsbConnectionContract {
    fun onDeviceDisconnected()
    fun onDeviceConnected()
    fun onUsbPermissionGranted()
    fun onReceiveData(data: String?)
    fun onDeviceVerified()
    fun onVerificationTimeout()
    fun onUsbPermissionDenied()
    fun onDeviceReconnect()
}