package `in`.sunfox.android.spandanEngine.usb_connection

import com.example.usbapp.usb_connection.ConstantHelper.Intents.ACTION_USB_PERMISSION
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.usbapp.usb_connection.ConstantHelper
import com.felhr.usbserial.UsbSerialDevice

object UsbConnectionHelper {

    private val TAG = "UsbConnectionHelper"

    private lateinit var usbManager: UsbManager
    private lateinit var usbConnectionContract: UsbConnectionContract
    private lateinit var activity: AppCompatActivity
    private lateinit var serialPort: UsbSerialDevice
    private var usbDevice: UsbDevice? = null
    private var isDeviceVerified = false
    private var serialDeviceDataReceiveThread: SerialDeviceDataReceiveThread? = null
    private var searchDeviceThread: SearchDeviceThread? = null


    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val action = intent.action
            if (action != null) {
                when (action) {
                    ConstantHelper.Intents.ACTION_USB_CONNECTED -> {
                        usbConnectionContract.onDeviceConnected()
                        searchDeviceThread = SearchDeviceThread(usbManager)
                        searchDeviceThread!!.name = "Search Device thread"
                        searchDeviceThread!!.start()
                    }
                    ConstantHelper.Intents.ACTION_USB_DISCONNECTED -> {
                        usbConnectionContract.onDeviceDisconnected()
                        isDeviceVerified = false
                        if (searchDeviceThread != null) {
                            searchDeviceThread!!.interrupt()
                            searchDeviceThread = null
                        }
                        if (serialDeviceDataReceiveThread != null) {
                            serialDeviceDataReceiveThread!!.interrupt()
                            serialDeviceDataReceiveThread = null
                        }
                    }
                    ACTION_USB_PERMISSION -> {
                        //Toast.makeText(context,"Usb Permission",Toast.LENGTH_SHORT).show()
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            // Toast.makeText(context,"Usb Permission inside",Toast.LENGTH_SHORT).show()
                            usbConnectionContract.onUsbPermissionGranted()
                            setUp()
                            //isDeviceVerified = true
                        } else {
                            usbConnectionContract.onUsbPermissionDenied()

                        }
                    }
                }
            }
        }
    }

    fun getSerialPort(): UsbSerialDevice? {
        return serialPort
    }

    fun isDeviceConnected(): Boolean {
        return isDeviceVerified
    }

    fun setDeviceConnected(connected: Boolean) {
        isDeviceVerified = connected
    }

    fun getUsbDevice(): UsbDevice? {
        return usbDevice
    }

    fun startTransmission() {
        if (UsbConnectionHelper::serialPort.isInitialized)
            serialPort.write("1".toByteArray())
        else
            usbConnectionContract.onDeviceReconnect()
    }

    fun stopTransmission() {
        if (::serialPort.isInitialized)
            serialPort.write("0".toByteArray())
    }

    fun bind(usbConnectionContract: UsbConnectionContract, activity: Context) {
        (activity as AppCompatActivity).registerReceiver(
            broadcastReceiver,
            getIntentFilter()
        )
        usbManager =
            (activity).getSystemService(
                Context.USB_SERVICE
            ) as UsbManager
        UsbConnectionHelper.usbConnectionContract = usbConnectionContract
        UsbConnectionHelper.activity = activity
    }

    fun unBind(context: Context) {
//        isDeviceVerified = false
        (context as AppCompatActivity).unregisterReceiver(broadcastReceiver)
    }

    private fun getIntentFilter(): IntentFilter? {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ConstantHelper.Intents.ACTION_USB_CONNECTED)
        intentFilter.addAction(ConstantHelper.Intents.ACTION_USB_DISCONNECTED)
        intentFilter.addAction(ACTION_USB_PERMISSION)
        return intentFilter
    }

    private fun setUp() {
        Log.d("RawData.DATA", "Setup()")

        if (usbDevice == null) {
            usbConnectionContract.onVerificationTimeout()
            return
        }
        serialPort = UsbSerialDevice.createUsbSerialDevice(
            usbDevice!!,
            usbManager.openDevice(usbDevice)
        )!!
        serialDeviceDataReceiveThread =
            SerialDeviceDataReceiveThread(serialPort)
        serialDeviceDataReceiveThread!!.name = "SerialDeviceDataReceiveThread"
        serialDeviceDataReceiveThread!!.start()
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            serialPort.write("c".toByteArray())
            Log.d("RawData.DATA", "Writing 'c'")
        }, 3000)
        val handler1 = Handler(Looper.getMainLooper())
        handler1.postDelayed({
            if (!isDeviceVerified) {
                usbConnectionContract.onVerificationTimeout()
            }
        }, 8000)
    }

    fun onReceiveRawData(data: String) {
        Log.d("RawData.DATA", data)
//        Log.d("RawData.DATA", "Is Verified: "+ isDeviceVerified)
//        Toast.makeText(,"Received",Toast.LENGTH_SHORT).show()
        val finalData = data.substring(0, data.length - 1)
        if (finalData == "spdn" && !isDeviceVerified) {
            isDeviceVerified = true
            usbConnectionContract.onDeviceVerified()
        } else usbConnectionContract.onReceiveData(finalData)
    }


    fun setUsbDevice(device: UsbDevice?) {
        Log.d(TAG, "setUsbDevice: $device")
        usbDevice = device
        if (device == null) {
            usbConnectionContract.onDeviceDisconnected()
        } else {
            val permissionIntent = PendingIntent.getBroadcast(
                activity as Context?,
                0,
                Intent(ACTION_USB_PERMISSION),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
            )
            if (!usbManager.hasPermission(usbDevice))
                usbManager.requestPermission(
                    usbDevice,
                    permissionIntent
                )
        }
    }

    fun onSearchComplete() {
        /*if(usbDevice==null){
            isDeviceVerified = false;
            usbConnectionContract.onDeviceDisconnected();
           // ((AppCompatActivity)usbConnectionContract).sendBroadcast(new Intent(ConstantsHelper.Intents.ACTION_USB_DISCONNECTED));
        }*/
        if (searchDeviceThread != null) {

            /*if (usbDevice == null) {
                //SpandanToast.showToast(((AppCompatActivity) usbConnectionContract), "Null");
                isDeviceVerified = false;
                usbConnectionContract.onDeviceDisconnected();
                // ((AppCompatActivity)usbConnectionContract).sendBroadcast(new Intent(ConstantsHelper.Intents.ACTION_USB_DISCONNECTED));
            }*/
            searchDeviceThread!!.interrupt()
            searchDeviceThread = null
        }
    }
}