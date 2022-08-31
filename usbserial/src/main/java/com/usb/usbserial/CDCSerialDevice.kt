package com.usb.usbserial

import kotlin.jvm.JvmOverloads
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbRequest
import com.usb.utils.SafeUsbRequest
import com.usb.usbserial.UsbSerialInterface.UsbCTSCallback
import com.usb.usbserial.UsbSerialInterface.UsbDSRCallback
import com.usb.usbserial.UsbSerialInterface.UsbBreakCallback
import com.usb.usbserial.UsbSerialInterface.UsbFrameCallback
import com.usb.usbserial.UsbSerialInterface.UsbOverrunCallback
import com.usb.usbserial.UsbSerialInterface.UsbParityCallback
import android.hardware.usb.UsbConstants
import android.util.Log
import com.usb.usbserial.UsbSerialInterface.Companion.DATA_BITS_5
import com.usb.usbserial.UsbSerialInterface.Companion.DATA_BITS_6
import com.usb.usbserial.UsbSerialInterface.Companion.DATA_BITS_7
import com.usb.usbserial.UsbSerialInterface.Companion.DATA_BITS_8
import com.usb.usbserial.UsbSerialInterface.Companion.PARITY_EVEN
import com.usb.usbserial.UsbSerialInterface.Companion.PARITY_MARK
import com.usb.usbserial.UsbSerialInterface.Companion.PARITY_NONE
import com.usb.usbserial.UsbSerialInterface.Companion.PARITY_ODD
import com.usb.usbserial.UsbSerialInterface.Companion.PARITY_SPACE
import com.usb.usbserial.UsbSerialInterface.Companion.STOP_BITS_1
import com.usb.usbserial.UsbSerialInterface.Companion.STOP_BITS_15
import com.usb.usbserial.UsbSerialInterface.Companion.STOP_BITS_2

class CDCSerialDevice @JvmOverloads constructor(
    device: UsbDevice,
    connection: UsbDeviceConnection?,
    iface: Int = -1
) : UsbSerialDevice(device, connection) {
    private val mInterface: UsbInterface
    private var inEndpoint: UsbEndpoint? = null
    private var outEndpoint: UsbEndpoint? = null
    private var cdcControl = 0
    var initialBaudRate1 = 0
        get() = this.initialBaudRate
        set(value) {
            field = this.initialBaudRate
        }
    private var controlLineState = CDC_CONTROL_LINE_ON

    override fun getCTS(ctsCallback: UsbCTSCallback?){

    }

    override fun getDSR(dsrCallback: UsbDSRCallback?) {
        TODO("Not yet implemented")
    }

    override fun getBreak(breakCallback: UsbBreakCallback?) {
        TODO("Not yet implemented")
    }

    override fun getFrame(frameCallback: UsbFrameCallback?) {
        TODO("Not yet implemented")
    }

    override fun getOverrun(overrunCallback: UsbOverrunCallback?) {
        TODO("Not yet implemented")
    }

    override fun getParity(parityCallback: UsbParityCallback?) {
        TODO("Not yet implemented")
    }

    override fun open(): Boolean {
        val ret = openCDC()
        return if (ret) {
            // Initialize UsbRequest
            val requestIN: UsbRequest = SafeUsbRequest()
            requestIN.initialize(connection, inEndpoint)

            // Restart the working thread if it has been killed before and  get and claim interface
            restartWorkingThread()
            restartWriteThread()

            // Pass references to the threads
            setThreadsParams(requestIN, outEndpoint)
            asyncMode = true
            isOpen = true
            true
        } else {
            isOpen = false
            false
        }
    }

    override fun close() {
        setControlCommand(CDC_SET_CONTROL_LINE_STATE, CDC_CONTROL_LINE_OFF, null)
        killWorkingThread()
        killWriteThread()
        if(connection!=null) {
            connection.releaseInterface(mInterface)
            connection.close()
        }
        isOpen = false
    }

    override fun syncOpen(): Boolean {
        val ret = openCDC()
        return if (ret) {
            setSyncParams(inEndpoint, outEndpoint)
            asyncMode = false
            isOpen = true

            // Init Streams
            serialInputStream = SerialInputStream(this)
            serialOutputStream = SerialOutputStream(this)
            true
        } else {
            isOpen = false
            false
        }
    }

    override fun syncClose() {
        setControlCommand(CDC_SET_CONTROL_LINE_STATE, CDC_CONTROL_LINE_OFF, null)
        if (connection!=null) {
            connection.releaseInterface(mInterface)
            connection.close()
        }
        isOpen = false
    }

    override fun setBaudRate(baudRate: Int) {
        val data = lineCoding
        data[0] = (baudRate and 0xff).toByte()
        data[1] = (baudRate shr 8 and 0xff).toByte()
        data[2] = (baudRate shr 16 and 0xff).toByte()
        data[3] = (baudRate shr 24 and 0xff).toByte()
        setControlCommand(CDC_SET_LINE_CODING, 0, data)
    }

    override fun setDataBits(dataBits: Int) {
        Log.d("UsbSerialDevice.TAG", "setDataBits: $dataBits $DATA_BITS_8 $DATA_BITS_7 $DATA_BITS_6 $DATA_BITS_5")
        val data = lineCoding
        when (dataBits) {
//            DATA_BITS_5 -> data[6] = 0x05
//            DATA_BITS_6 -> data[6] = 0x06
//            DATA_BITS_7 -> data[6] = 0x07
            DATA_BITS_8 -> data[6] = 0x08
            else -> return
        }
        setControlCommand(CDC_SET_LINE_CODING, 0, data)
    }

    override fun setStopBits(stopBits: Int) {
        Log.d("UsbSerialDevice.TAG", "setStopBits: $stopBits $STOP_BITS_1 $STOP_BITS_2 $STOP_BITS_15")
        val data = lineCoding
        when (stopBits) {
            STOP_BITS_1 -> data[4] = 0x00
//            STOP_BITS_15 -> data[4] = 0x01
//            STOP_BITS_2 -> data[4] = 0x02
            else -> return
        }
        setControlCommand(CDC_SET_LINE_CODING, 0, data)
    }

    override fun setParity(parity: Int) {
        Log.d("UsbSerialDevice.TAG", "setParity: $parity $PARITY_NONE $PARITY_ODD $PARITY_MARK $PARITY_EVEN $PARITY_SPACE")
        val data = lineCoding
        when (parity) {
            PARITY_NONE -> data[5] = 0x00
//            PARITY_ODD -> data[5] = 0x01
//            PARITY_EVEN -> data[5] = 0x02
//            PARITY_MARK -> data[5] = 0x03
//            PARITY_SPACE -> data[5] = 0x04
            else -> return
        }
        setControlCommand(CDC_SET_LINE_CODING, 0, data)
    }

    override fun setFlowControl(flowControl: Int) {
        // TODO Auto-generated method stub
    }

    override fun setBreak(state: Boolean) {
        //TODO
    }

    override fun setRTS(state: Boolean) {
        controlLineState =
            if (state) controlLineState or CDC_SET_CONTROL_LINE_STATE_RTS else controlLineState and CDC_SET_CONTROL_LINE_STATE_RTS.inv()
        setControlCommand(CDC_SET_CONTROL_LINE_STATE, controlLineState, null)
    }

    override fun setDTR(state: Boolean) {
        controlLineState =
            if (state) controlLineState or CDC_SET_CONTROL_LINE_STATE_DTR else controlLineState and CDC_SET_CONTROL_LINE_STATE_DTR.inv()
        setControlCommand(CDC_SET_CONTROL_LINE_STATE, controlLineState, null)
    }

    private fun openCDC(): Boolean {
        if (connection?.claimInterface(mInterface, true) == true) {
            Log.i(CLASS_ID, "Interface succesfully claimed")
        } else {
            Log.i(CLASS_ID, "Interface could not be claimed")
            return false
        }

        // Assign endpoints
        val numberEndpoints = mInterface.endpointCount
        for (i in 0 until numberEndpoints) {
            val endpoint = mInterface.getEndpoint(i)
            if (endpoint.type == UsbConstants.USB_ENDPOINT_XFER_BULK
                && endpoint.direction == UsbConstants.USB_DIR_IN
            ) {
                inEndpoint = endpoint
            } else if (endpoint.type == UsbConstants.USB_ENDPOINT_XFER_BULK
                && endpoint.direction == UsbConstants.USB_DIR_OUT
            ) {
                outEndpoint = endpoint
            }
        }
        if (outEndpoint == null || inEndpoint == null) {
            Log.i(CLASS_ID, "Interface does not have an IN or OUT interface")
            return false
        }

        // Default Setup
        setControlCommand(CDC_SET_LINE_CODING, 0, initialLineCoding)
        setControlCommand(CDC_SET_CONTROL_LINE_STATE, CDC_CONTROL_LINE_ON, null)
        return true
    }

    protected val initialLineCoding: ByteArray
        protected get() {
            val lineCoding: ByteArray
            val initialBaudRate = initialBaudRate1
            if (initialBaudRate > 0) {
                lineCoding = CDC_DEFAULT_LINE_CODING.clone()
                for (i in 0..3) {
                    lineCoding[i] = (initialBaudRate shr i * 8 and 0xFF).toByte()
                }
            } else {
                lineCoding = CDC_DEFAULT_LINE_CODING
            }
            return lineCoding
        }

    private fun setControlCommand(request: Int, value: Int, data: ByteArray?): Int {
        var dataLength = 0
        if (data != null) {
            dataLength = data.size
        }
        val response = connection?.controlTransfer(
            CDC_REQTYPE_HOST2DEVICE,
            request,
            value,
            cdcControl,
            data,
            dataLength,
            USB_TIMEOUT
        )
        Log.i(CLASS_ID, "Control Transfer Response: $response")
        return response?:0
    }

    private val lineCoding: ByteArray
        get() {
            val data = ByteArray(7)
            val response = connection?.controlTransfer(
                CDC_REQTYPE_DEVICE2HOST,
                CDC_GET_LINE_CODING,
                0,
                cdcControl,
                data,
                data.size,
                USB_TIMEOUT
            )
            Log.i(CLASS_ID, "Control Transfer Response: $response")
            return data
        }

    companion object {
        private val CLASS_ID = CDCSerialDevice::class.java.simpleName
        private const val CDC_REQTYPE_HOST2DEVICE = 0x21
        private const val CDC_REQTYPE_DEVICE2HOST = 0xA1
        private const val CDC_SET_LINE_CODING = 0x20
        private const val CDC_GET_LINE_CODING = 0x21
        private const val CDC_SET_CONTROL_LINE_STATE = 0x22
        private const val CDC_SET_CONTROL_LINE_STATE_RTS = 0x2
        private const val CDC_SET_CONTROL_LINE_STATE_DTR = 0x1

        /***
         * Default Serial Configuration
         * Baud rate: 115200
         * Data bits: 8
         * Stop bits: 1
         * Parity: None
         * Flow Control: Off
         */
        private val CDC_DEFAULT_LINE_CODING = byteArrayOf(
            0x00.toByte(),
            0xC2.toByte(),
            0x01.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x08.toByte()
        )
        private const val CDC_CONTROL_LINE_ON = 0x0003
        private const val CDC_CONTROL_LINE_OFF = 0x0000
        private fun findFirstCDC(device: UsbDevice): Int {
            val interfaceCount = device.interfaceCount
            for (iIndex in 0 until interfaceCount) {
                if (device.getInterface(iIndex).interfaceClass == UsbConstants.USB_CLASS_CDC_DATA) {
                    return iIndex
                }
            }
            Log.i(CLASS_ID, "There is no CDC class interface")
            return -1
        }

        private fun findFirstControl(device: UsbDevice): Int {
            val interfaceCount = device.interfaceCount
            for (iIndex in 0 until interfaceCount) {
                if (device.getInterface(iIndex).interfaceClass == UsbConstants.USB_CLASS_COMM) {
                    Log.i(CLASS_ID, "Using CDC control interface $iIndex")
                    return iIndex
                }
            }
            Log.i(CLASS_ID, "There is no CDC control interface")
            return 0
        }
    }

    init {
        cdcControl =
            findFirstControl(device) // Not sure how to find the control interface for others.
        mInterface = device.getInterface(if (iface >= 0) iface else findFirstCDC(device))
    }
}