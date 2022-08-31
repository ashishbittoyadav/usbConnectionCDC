package com.usb.usbserial

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import com.usb.usbserial.UsbSerialInterface.UsbReadCallback
import android.hardware.usb.UsbRequest
import android.hardware.usb.UsbConstants
import kotlin.jvm.JvmOverloads
import android.os.Build
import android.util.Log
import java.util.*

abstract class UsbSerialDevice(
    protected val device: UsbDevice,
    protected val connection: UsbDeviceConnection?
) : UsbSerialInterface {

    private val TAG = "UsbSerialDevice.TAG"

    protected var serialBuffer: SerialBuffer? = SerialBuffer(mr1Version)
    protected var workerThread: WorkerThread? = null
    protected var writeThread: WriteThread? = null
//    protected var readThread: ReadThread? = null

    // Endpoints for synchronous read and write operations
    private var inEndpoint: UsbEndpoint? = null
    private var outEndpoint: UsbEndpoint? = null

    // InputStream and OutputStream (only for sync api)
    protected var serialInputStream: SerialInputStream? = null
    protected var serialOutputStream: SerialOutputStream? = null
    protected var asyncMode = true
    var portName = ""
    var isOpen = false
        protected set

    // Common Usb Serial Operations (I/O Asynchronous)
    abstract override fun open(): Boolean
//    override fun write(buffer: ByteArray) {
//        if (asyncMode) serialBuffer!!.putWriteBuffer(buffer)
//    }

    override fun write(buffer: ByteArray?) {
        if (asyncMode) serialBuffer!!.putWriteBuffer(buffer)
    }
    /**
     * Classes that do not implement [.setInitialBaudRate] should always return -1
     *
     * @return initial baud rate used when initializing the serial connection
     */// this class does not implement initialBaudRate
    /**
     *
     *
     * Use this setter **before** calling [.open] to override the default baud rate defined in this particular class.
     *
     *
     *
     *
     * This is a workaround for devices where calling [.setBaudRate] has no effect once [.open] has been called.
     *
     *
     * @param initialBaudRate baud rate to be used when initializing the serial connection
     */
    var initialBaudRate: Int = 0
        get() = -1

//    override fun read(mCallback: UsbReadCallback): Int {
//        if (!asyncMode) return -1
//        if (mr1Version) {
//            if (workerThread != null) {
//                workerThread!!.setCallback(mCallback)
//                workerThread!!.usbRequest!!.queue(
//                    serialBuffer!!.getReadBuffer(),
//                    SerialBuffer.DEFAULT_READ_BUFFER_SIZE
//                )
//            }
//        } else {
//            readThread!!.setCallback(mCallback)
//            //readThread.start();
//        }
//        return 0
//    }

    override fun read(mCallback: UsbReadCallback?): Int {
        Log.d("TestingFlow.TAG", "read: reading data start transmission")
        if (!asyncMode) return -1
        if (mr1Version) {
            Log.d(TAG, "read: $mr1Version")
            if (workerThread != null) {
                workerThread!!.setCallback(mCallback)
                workerThread!!.usbRequest!!.queue(
                    serialBuffer!!.getReadBuffer(),SerialBuffer.DEFAULT_READ_BUFFER_SIZE
                )
            }
        } else {
//            readThread!!.setCallback(mCallback)
            //readThread.start();
        }
        return 0
    }

    abstract override fun close()

    // Common Usb Serial Operations (I/O Synchronous)
    abstract override fun syncOpen(): Boolean
    abstract override fun syncClose()

    override fun syncWrite(buffer: ByteArray?, timeout: Int): Int {
//        return if (!asyncMode) {
//            if (buffer == null) 0 else connection?.bulkTransfer(
//                outEndpoint,
//                buffer,
//                buffer.size,
//                timeout
//            )?:0
//        } else {
//            -1
//        }
        return -1
    }

    override fun syncRead(buffer: ByteArray?, timeout: Int): Int {
//        if (asyncMode) {
//            return -1
//        }
//        return if (buffer == null) 0 else connection?.bulkTransfer(
//            inEndpoint,
//            buffer,
//            buffer.size,
//            timeout
//        )?:0
        return 0
    }

    override fun syncWrite(buffer: ByteArray?, offset: Int, length: Int, timeout: Int): Int {
//        return if (!asyncMode) {
//            if (buffer == null) 0 else connection?.bulkTransfer(
//                outEndpoint,
//                buffer,
//                offset,
//                length,
//                timeout
//            )?:0
//        } else {
//            -1
//        }
        return -1
    }


    override fun syncRead(buffer: ByteArray?, offset: Int, length: Int, timeout: Int): Int {
//        if (asyncMode) {
//            return -1
//        }
//        return if (buffer == null) 0 else connection?.bulkTransfer(
//            inEndpoint,
//            buffer,
//            offset,
//            length,
//            timeout
//        )?:0
        return 0
    }

    // Serial port configuration
    abstract override fun setBaudRate(baudRate: Int)
    abstract override fun setDataBits(dataBits: Int)
    abstract override fun setStopBits(stopBits: Int)
    abstract override fun setParity(parity: Int)
    abstract override fun setFlowControl(flowControl: Int)
    abstract override fun setBreak(state: Boolean)
    fun getInputStream(): SerialInputStream? {
        check(!asyncMode) {
            """
                InputStream only available in Sync mode. 
                Open the port with syncOpen()
                """.trimIndent()
        }
        return serialInputStream
    }

    fun getOutputStream(): SerialOutputStream? {
        check(!asyncMode) {
            """
                OutputStream only available in Sync mode. 
                Open the port with syncOpen()
                """.trimIndent()
        }
        return serialOutputStream
    }

    fun getVid(): Int {
        return device.vendorId
    }

    fun getPid(): Int {
        return device.productId
    }

    fun getDeviceId(): Int {
        return device.deviceId
    }

    //Debug options
    fun debug(value: Boolean) {
        if (serialBuffer != null) serialBuffer!!.debug(value)
    }

    private fun isFTDIDevice(): Boolean {
//        return this is FTDISerialDevice
        return false
    }

    /*
     * WorkerThread waits for request notifications from IN endpoint
     */
    protected inner class WorkerThread() :
        AbstractWorkerThread() {
        private var callback: UsbReadCallback? = null
        var usbRequest: UsbRequest? = null
        override fun doRun() {
            val request = connection?.requestWait()
            if (request != null && request.endpoint.type == UsbConstants.USB_ENDPOINT_XFER_BULK && request.endpoint.direction == UsbConstants.USB_DIR_IN) {
                val data = serialBuffer!!.dataReceived
                    // Clear buffer, execute the callback
                    serialBuffer!!.clearReadBuffer()
                    onReceivedData(data)
                // Queue a new request
                usbRequest!!.queue(
                    serialBuffer!!.getReadBuffer(),
                    SerialBuffer.DEFAULT_READ_BUFFER_SIZE
                )
            }
        }

        fun setCallback(callback: UsbReadCallback?) {
            this.callback = callback
        }

        private fun onReceivedData(data: ByteArray) {
            if (callback != null) callback!!.onReceivedData(data)
        }
    }

    inner class WriteThread : AbstractWorkerThread() {
        private var outEndpoint: UsbEndpoint? = null
        override fun doRun() {
            Log.d(TAG, "doRun: 1 - bulkTransfer $serialBuffer")
            val data = serialBuffer!!.getWriteBuffer()
            if (data.isNotEmpty()) connection?.bulkTransfer(outEndpoint, data, data.size, USB_TIMEOUT)
        }

        fun setUsbEndpoint(outEndpoint: UsbEndpoint?) {
            this.outEndpoint = outEndpoint
        }
    }

    protected fun setSyncParams(inEndpoint: UsbEndpoint?, outEndpoint: UsbEndpoint?) {
        this.inEndpoint = inEndpoint
        this.outEndpoint = outEndpoint
    }

    protected fun setThreadsParams(request: UsbRequest, endpoint: UsbEndpoint?) {
        writeThread!!.setUsbEndpoint(endpoint)
        if (mr1Version) {
            workerThread!!.usbRequest = request
        } else {
//            readThread!!.setUsbEndpoint(request.endpoint)
        }
    }

    /*
     * Kill workingThread; This must be called when closing a device
     */
    protected fun killWorkingThread() {
        if (mr1Version && workerThread != null) {
            workerThread!!.stopThread()
            workerThread = null
        }
    }

    /*
     * Restart workingThread if it has been killed before
     */
    protected fun restartWorkingThread() {
        if (mr1Version && workerThread == null) {
            workerThread = WorkerThread()
            workerThread!!.start()
            while (!workerThread!!.isAlive) {
            } // Busy waiting
        }
    }

    protected fun killWriteThread() {
        if (writeThread != null) {
            writeThread!!.stopThread()
            writeThread = null
        }
    }

    protected fun restartWriteThread() {
        if (writeThread == null) {
            writeThread = WriteThread()
            writeThread!!.start()
            while (!writeThread!!.isAlive) {
            } // Busy waiting
        }
    }

    companion object {
        const val CDC = "cdc"
        const val CH34x = "ch34x"
        const val CP210x = "cp210x"
        const val FTDI = "ftdi"
        const val PL2303 = "pl2303"
        const val COM_PORT = "COM "

        // Android version < 4.3 It is not going to be asynchronous read operations
        val mr1Version = Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1
        const val USB_TIMEOUT = 0
        @JvmOverloads
        fun createUsbSerialDevice(
            device: UsbDevice,
            connection: UsbDeviceConnection?,
            iface: Int = -1
        ): UsbSerialDevice? {
            /*
		 * It checks given vid and pid and will return a custom driver or a CDC serial driver.
		 * When CDC is returned open() method is even more important, its response will inform about if it can be really
		 * opened as a serial device with a generic CDC serial driver
		 */
            val vid = device.vendorId
            val pid = device.productId
            return if (isCdcDevice(device)) CDCSerialDevice(
                device,
                connection,
                iface
            ) else null
        }

        private fun isCdcDevice(device: UsbDevice): Boolean {
            val iIndex = device.interfaceCount
            for (i in 0 until iIndex) {
                val iface = device.getInterface(i)
                if (iface.interfaceClass == UsbConstants.USB_CLASS_CDC_DATA) return true
            }
            return false
        }
    }

}