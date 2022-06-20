//package com.felhr.usbserial
//
//import kotlin.jvm.JvmOverloads
//import android.hardware.usb.UsbDevice
//import android.hardware.usb.UsbDeviceConnection
//import com.felhr.usbserial.UsbSerialDevice
//import com.felhr.usbserial.UsbSerialInterface.UsbCTSCallback
//import com.felhr.usbserial.UsbSerialInterface.UsbDSRCallback
//import android.hardware.usb.UsbInterface
//import android.hardware.usb.UsbEndpoint
//import com.felhr.usbserial.FTDISerialDevice.FTDIUtilities
//import com.felhr.usbserial.UsbSerialInterface.UsbParityCallback
//import com.felhr.usbserial.UsbSerialInterface.UsbFrameCallback
//import com.felhr.usbserial.UsbSerialInterface.UsbOverrunCallback
//import com.felhr.usbserial.UsbSerialInterface.UsbBreakCallback
//import android.hardware.usb.UsbRequest
//import com.felhr.utils.SafeUsbRequest
//import android.hardware.usb.UsbConstants
//import android.annotation.SuppressLint
//import android.os.Build
//import android.util.Log
//import java.util.*
//import kotlin.experimental.and
//
//class FTDISerialDevice @JvmOverloads constructor(
//    device: UsbDevice,
//    connection: UsbDeviceConnection?,
//    iface: Int = -1
//) : UsbSerialDevice(device, connection) {
//    private var currentSioSetData = 0x0000
//
//    /**
//     * Flow control variables
//     */
//    private var rtsCtsEnabled: Boolean
//    private var dtrDsrEnabled: Boolean
//    private var ctsState: Boolean
//    private var dsrState: Boolean
//    private var firstTime // with this flag we set the CTS and DSR state to the first value received from the FTDI device
//            : Boolean
//    private var ctsCallback: UsbCTSCallback? = null
//    private var dsrCallback: UsbDSRCallback? = null
//    private val mInterface: UsbInterface
//    private var inEndpoint: UsbEndpoint? = null
//    private var outEndpoint: UsbEndpoint? = null
//    var ftdiUtilities: FTDIUtilities
//    private var parityCallback: UsbParityCallback? = null
//    private var frameCallback: UsbFrameCallback? = null
//    private var overrunCallback: UsbOverrunCallback? = null
//    private var breakCallback: UsbBreakCallback? = null
//    override fun open(): Boolean {
//        val ret = openFTDI()
//        return if (ret) {
//            // Initialize UsbRequest
//            val requestIN: UsbRequest = SafeUsbRequest()
//            requestIN.initialize(connection, inEndpoint)
//
//            // Restart the working thread if it has been killed before and  get and claim interface
//            restartWorkingThread()
//            restartWriteThread()
//
//            // Pass references to the threads
//            setThreadsParams(requestIN, outEndpoint)
//            asyncMode = true
//            isOpen = true
//            true
//        } else {
//            isOpen = false
//            false
//        }
//    }
//
//    override fun close() {
//        setControlCommand(FTDI_SIO_MODEM_CTRL, FTDI_SET_MODEM_CTRL_DEFAULT3, 0)
//        setControlCommand(FTDI_SIO_MODEM_CTRL, FTDI_SET_MODEM_CTRL_DEFAULT4, 0)
//        currentSioSetData = 0x0000
//        killWorkingThread()
//        killWriteThread()
//        connection!!.releaseInterface(mInterface)
//        isOpen = false
//    }
//
//    override fun syncOpen(): Boolean {
//        val ret = openFTDI()
//        return if (ret) {
//            setSyncParams(inEndpoint, outEndpoint)
//            asyncMode = false
//
//            // Init Streams
//            serialInputStream = SerialInputStream(this)
//            serialOutputStream = SerialOutputStream(this)
//            isOpen = true
//            true
//        } else {
//            isOpen = false
//            false
//        }
//    }
//
//    override fun syncClose() {
//        setControlCommand(FTDI_SIO_MODEM_CTRL, FTDI_SET_MODEM_CTRL_DEFAULT3, 0)
//        setControlCommand(FTDI_SIO_MODEM_CTRL, FTDI_SET_MODEM_CTRL_DEFAULT4, 0)
//        currentSioSetData = 0x0000
//        connection!!.releaseInterface(mInterface)
//        isOpen = false
//    }
//
//    override fun setBaudRate(baudRate: Int) {
//        val encodedBaudRate = encodedBaudRate(baudRate)
//        if (encodedBaudRate != null) {
//            setEncodedBaudRate(encodedBaudRate)
//        } else {
//            setOldBaudRate(baudRate)
//        }
//    }
//
//    override fun setDataBits(dataBits: Int) {
//        when (dataBits) {
//            UsbSerialInterface.DATA_BITS_5 -> {
//                currentSioSetData = currentSioSetData or 1
//                currentSioSetData = currentSioSetData and (1 shl 1).inv()
//                currentSioSetData = currentSioSetData or (1 shl 2)
//                currentSioSetData = currentSioSetData and (1 shl 3).inv()
//                setControlCommand(FTDI_SIO_SET_DATA, currentSioSetData, 0)
//            }
//            UsbSerialInterface.DATA_BITS_6 -> {
//                currentSioSetData = currentSioSetData and 1.inv()
//                currentSioSetData = currentSioSetData or (1 shl 1)
//                currentSioSetData = currentSioSetData or (1 shl 2)
//                currentSioSetData = currentSioSetData and (1 shl 3).inv()
//                setControlCommand(FTDI_SIO_SET_DATA, currentSioSetData, 0)
//            }
//            UsbSerialInterface.DATA_BITS_7 -> {
//                currentSioSetData = currentSioSetData or 1
//                currentSioSetData = currentSioSetData or (1 shl 1)
//                currentSioSetData = currentSioSetData or (1 shl 2)
//                currentSioSetData = currentSioSetData and (1 shl 3).inv()
//                setControlCommand(FTDI_SIO_SET_DATA, currentSioSetData, 0)
//            }
//            UsbSerialInterface.DATA_BITS_8 -> {
//                currentSioSetData = currentSioSetData and 1.inv()
//                currentSioSetData = currentSioSetData and (1 shl 1).inv()
//                currentSioSetData = currentSioSetData and (1 shl 2).inv()
//                currentSioSetData = currentSioSetData or (1 shl 3)
//                setControlCommand(FTDI_SIO_SET_DATA, currentSioSetData, 0)
//            }
//            else -> {
//                currentSioSetData = currentSioSetData and 1.inv()
//                currentSioSetData = currentSioSetData and (1 shl 1).inv()
//                currentSioSetData = currentSioSetData and (1 shl 2).inv()
//                currentSioSetData = currentSioSetData or (1 shl 3)
//                setControlCommand(FTDI_SIO_SET_DATA, currentSioSetData, 0)
//            }
//        }
//    }
//
//    override fun setStopBits(stopBits: Int) {
//        when (stopBits) {
//            UsbSerialInterface.STOP_BITS_1 -> {
//                currentSioSetData = currentSioSetData and (1 shl 11).inv()
//                currentSioSetData = currentSioSetData and (1 shl 12).inv()
//                currentSioSetData = currentSioSetData and (1 shl 13).inv()
//                setControlCommand(FTDI_SIO_SET_DATA, currentSioSetData, 0)
//            }
//            UsbSerialInterface.STOP_BITS_15 -> {
//                currentSioSetData = currentSioSetData or (1 shl 11)
//                currentSioSetData = currentSioSetData and (1 shl 12).inv()
//                currentSioSetData = currentSioSetData and (1 shl 13).inv()
//                setControlCommand(FTDI_SIO_SET_DATA, currentSioSetData, 0)
//            }
//            UsbSerialInterface.STOP_BITS_2 -> {
//                currentSioSetData = currentSioSetData and (1 shl 11).inv()
//                currentSioSetData = currentSioSetData or (1 shl 12)
//                currentSioSetData = currentSioSetData and (1 shl 13).inv()
//                setControlCommand(FTDI_SIO_SET_DATA, currentSioSetData, 0)
//            }
//            else -> {
//                currentSioSetData = currentSioSetData and (1 shl 11).inv()
//                currentSioSetData = currentSioSetData and (1 shl 12).inv()
//                currentSioSetData = currentSioSetData and (1 shl 13).inv()
//                setControlCommand(FTDI_SIO_SET_DATA, currentSioSetData, 0)
//            }
//        }
//    }
//
//    override fun setParity(parity: Int) {
//        when (parity) {
//            UsbSerialInterface.PARITY_NONE -> {
//                currentSioSetData = currentSioSetData and (1 shl 8).inv()
//                currentSioSetData = currentSioSetData and (1 shl 9).inv()
//                currentSioSetData = currentSioSetData and (1 shl 10).inv()
//                setControlCommand(FTDI_SIO_SET_DATA, currentSioSetData, 0)
//            }
//            UsbSerialInterface.PARITY_ODD -> {
//                currentSioSetData = currentSioSetData or (1 shl 8)
//                currentSioSetData = currentSioSetData and (1 shl 9).inv()
//                currentSioSetData = currentSioSetData and (1 shl 10).inv()
//                setControlCommand(FTDI_SIO_SET_DATA, currentSioSetData, 0)
//            }
//            UsbSerialInterface.PARITY_EVEN -> {
//                currentSioSetData = currentSioSetData and (1 shl 8).inv()
//                currentSioSetData = currentSioSetData or (1 shl 9)
//                currentSioSetData = currentSioSetData and (1 shl 10).inv()
//                setControlCommand(FTDI_SIO_SET_DATA, currentSioSetData, 0)
//            }
//            UsbSerialInterface.PARITY_MARK -> {
//                currentSioSetData = currentSioSetData or (1 shl 8)
//                currentSioSetData = currentSioSetData or (1 shl 9)
//                currentSioSetData = currentSioSetData and (1 shl 10).inv()
//                setControlCommand(FTDI_SIO_SET_DATA, currentSioSetData, 0)
//            }
//            UsbSerialInterface.PARITY_SPACE -> {
//                currentSioSetData = currentSioSetData and (1 shl 8).inv()
//                currentSioSetData = currentSioSetData and (1 shl 9).inv()
//                currentSioSetData = currentSioSetData or (1 shl 10)
//                setControlCommand(FTDI_SIO_SET_DATA, currentSioSetData, 0)
//            }
//            else -> {
//                currentSioSetData = currentSioSetData and (1 shl 8).inv()
//                currentSioSetData = currentSioSetData and (1 shl 9).inv()
//                currentSioSetData = currentSioSetData and (1 shl 10).inv()
//                setControlCommand(FTDI_SIO_SET_DATA, currentSioSetData, 0)
//            }
//        }
//    }
//
//    override fun setFlowControl(flowControl: Int) {
//        when (flowControl) {
//            UsbSerialInterface.FLOW_CONTROL_OFF -> {
//                setControlCommand(FTDI_SIO_SET_FLOW_CTRL, FTDI_SET_FLOW_CTRL_DEFAULT, 0)
//                rtsCtsEnabled = false
//                dtrDsrEnabled = false
//            }
//            UsbSerialInterface.FLOW_CONTROL_RTS_CTS -> {
//                rtsCtsEnabled = true
//                dtrDsrEnabled = false
//                val indexRTSCTS = 0x0001
//                setControlCommand(FTDI_SIO_SET_FLOW_CTRL, FTDI_SET_FLOW_CTRL_DEFAULT, indexRTSCTS)
//            }
//            UsbSerialInterface.FLOW_CONTROL_DSR_DTR -> {
//                dtrDsrEnabled = true
//                rtsCtsEnabled = false
//                val indexDSRDTR = 0x0002
//                setControlCommand(FTDI_SIO_SET_FLOW_CTRL, FTDI_SET_FLOW_CTRL_DEFAULT, indexDSRDTR)
//            }
//            UsbSerialInterface.FLOW_CONTROL_XON_XOFF -> {
//                val indexXONXOFF = 0x0004
//                val wValue = 0x1311
//                setControlCommand(FTDI_SIO_SET_FLOW_CTRL, wValue, indexXONXOFF)
//            }
//            else -> setControlCommand(FTDI_SIO_SET_FLOW_CTRL, FTDI_SET_FLOW_CTRL_DEFAULT, 0)
//        }
//    }
//
//    /**
//     * BREAK on/off methods obtained from linux driver
//     * https://github.com/torvalds/linux/blob/master/drivers/usb/serial/ftdi_sio.c
//     */
//    override fun setBreak(state: Boolean) {
//        currentSioSetData = if (state) {
//            currentSioSetData or FTDI_SIO_SET_BREAK_ON
//        } else {
//            currentSioSetData and FTDI_SIO_SET_BREAK_ON.inv()
//        }
//        setControlCommand(FTDI_SIO_SET_DATA, currentSioSetData, 0)
//    }
//
//    override fun setRTS(state: Boolean) {
//        if (state) {
//            setControlCommand(FTDI_SIO_MODEM_CTRL, FTDI_SIO_SET_RTS_HIGH, 0)
//        } else {
//            setControlCommand(FTDI_SIO_MODEM_CTRL, FTDI_SIO_SET_RTS_LOW, 0)
//        }
//    }
//
//    override fun setDTR(state: Boolean) {
//        if (state) {
//            setControlCommand(FTDI_SIO_MODEM_CTRL, FTDI_SIO_SET_DTR_HIGH, 0)
//        } else {
//            setControlCommand(FTDI_SIO_MODEM_CTRL, FTDI_SIO_SET_DTR_LOW, 0)
//        }
//    }
//
//    override fun getCTS(ctsCallback: UsbCTSCallback?) {
//        this.ctsCallback = ctsCallback
//    }
//
//    override fun getDSR(dsrCallback: UsbDSRCallback?) {
//        this.dsrCallback = dsrCallback
//    }
//
//    override fun getBreak(breakCallback: UsbBreakCallback?) {
//        this.breakCallback = breakCallback
//    }
//
//    override fun getFrame(frameCallback: UsbFrameCallback?) {
//        this.frameCallback = frameCallback
//    }
//
//    override fun getOverrun(overrunCallback: UsbOverrunCallback?) {
//        this.overrunCallback = overrunCallback
//    }
//
//    override fun getParity(parityCallback: UsbParityCallback?) {
//        this.parityCallback = parityCallback
//    }
//
//    private fun openFTDI(): Boolean {
//        if (connection!!.claimInterface(mInterface, true)) {
//            Log.i(CLASS_ID, "Interface succesfully claimed")
//        } else {
//            Log.i(CLASS_ID, "Interface could not be claimed")
//            return false
//        }
//
//        // Assign endpoints
//        val numberEndpoints = mInterface.endpointCount
//        for (i in 0..numberEndpoints - 1) {
//            val endpoint = mInterface.getEndpoint(i)
//            if (endpoint.type == UsbConstants.USB_ENDPOINT_XFER_BULK
//                && endpoint.direction == UsbConstants.USB_DIR_IN
//            ) {
//                inEndpoint = endpoint
//            } else {
//                outEndpoint = endpoint
//            }
//        }
//
//        // Default Setup
//        firstTime = true
//        if (setControlCommand(FTDI_SIO_RESET, 0x00, 0) < 0) return false
//        if (setControlCommand(FTDI_SIO_SET_DATA, FTDI_SET_DATA_DEFAULT, 0) < 0) return false
//        currentSioSetData = FTDI_SET_DATA_DEFAULT
//        if (setControlCommand(
//                FTDI_SIO_MODEM_CTRL,
//                FTDI_SET_MODEM_CTRL_DEFAULT1,
//                0
//            ) < 0
//        ) return false
//        if (setControlCommand(
//                FTDI_SIO_MODEM_CTRL,
//                FTDI_SET_MODEM_CTRL_DEFAULT2,
//                0
//            ) < 0
//        ) return false
//        if (setControlCommand(
//                FTDI_SIO_SET_FLOW_CTRL,
//                FTDI_SET_FLOW_CTRL_DEFAULT,
//                0
//            ) < 0
//        ) return false
//        if (setControlCommand(FTDI_SIO_SET_BAUD_RATE, FTDI_BAUDRATE_9600, 0) < 0) return false
//
//        // Flow control disabled by default
//        rtsCtsEnabled = false
//        dtrDsrEnabled = false
//        return true
//    }
//
//    private fun setControlCommand(request: Int, value: Int, index: Int): Int {
//        val dataLength = 0
//        val response = connection!!.controlTransfer(
//            FTDI_REQTYPE_HOST2DEVICE,
//            request,
//            value,
//            mInterface.id + 1 + index,
//            null,
//            dataLength,
//            USB_TIMEOUT
//        )
//        Log.i(CLASS_ID, "Control Transfer Response: $response")
//        return response
//    }
//
//    inner class FTDIUtilities {
//        // Special treatment needed to FTDI devices
//        fun adaptArray(ftdiData: ByteArray): ByteArray {
//            val length = ftdiData.size
//            return if (length > 64) {
//                var n = 1
//                var p = 64
//                // Precalculate length without FTDI headers
//                while (p < length) {
//                    n++
//                    p = n * 64
//                }
//                val realLength = length - n * 2
//                val data = ByteArray(realLength)
//                copyData(ftdiData, data)
//                data
//            } else {
//                Arrays.copyOfRange(ftdiData, 2, length)
//            }
//        }
//
//        fun checkModemStatus(data: ByteArray) {
//            if (data.size == 0) // Safeguard for zero length arrays
//                return
//            val cts = (data[0] and 0x10).toInt() == 0x10
//            val dsr = (data[0] and 0x20).toInt() == 0x20
//            if (firstTime) // First modem status received
//            {
//                ctsState = cts
//                dsrState = dsr
//                if (rtsCtsEnabled && ctsCallback != null) ctsCallback!!.onCTSChanged(ctsState)
//                if (dtrDsrEnabled && dsrCallback != null) dsrCallback!!.onDSRChanged(dsrState)
//                firstTime = false
//                return
//            }
//            if (rtsCtsEnabled && cts != ctsState && ctsCallback != null) //CTS
//            {
//                ctsState = !ctsState
//                ctsCallback!!.onCTSChanged(ctsState)
//            }
//            if (dtrDsrEnabled && dsr != dsrState && dsrCallback != null) //DSR
//            {
//                dsrState = !dsrState
//                dsrCallback!!.onDSRChanged(dsrState)
//            }
//            if (parityCallback != null) // Parity error checking
//            {
//                if ((data[1] and 0x04).toInt() == 0x04) {
//                    parityCallback!!.onParityError()
//                }
//            }
//            if (frameCallback != null) // Frame error checking
//            {
//                if ((data[1] and 0x08).toInt() == 0x08) {
//                    frameCallback!!.onFramingError()
//                }
//            }
//            if (overrunCallback != null) // Overrun error checking
//            {
//                if ((data[1] and 0x02).toInt() == 0x02) {
//                    overrunCallback!!.onOverrunError()
//                }
//            }
//            if (breakCallback != null) // Break interrupt checking
//            {
//                if ((data[1] and 0x10).toInt() == 0x10) {
//                    breakCallback!!.onBreakInterrupt()
//                }
//            }
//        }
//    }
//
//    override fun syncRead(buffer: ByteArray?, timeout: Int): Int {
//        val beginTime = System.currentTimeMillis()
//        val stopTime = beginTime + timeout
//        if (asyncMode) {
//            return -1
//        }
//        if (buffer == null) {
//            return 0
//        }
//        var n = buffer.size / 62
//        if (buffer.size % 62 != 0) {
//            n++
//        }
//        val tempBuffer = ByteArray(buffer.size + n * 2)
//        var readen = 0
//        do {
//            var timeLeft = 0
//            if (timeout > 0) {
//                timeLeft = (stopTime - System.currentTimeMillis()).toInt()
//                if (timeLeft <= 0) {
//                    break
//                }
//            }
//            val numberBytes =
//                connection!!.bulkTransfer(inEndpoint, tempBuffer, tempBuffer.size, timeLeft)
//            if (numberBytes > 2) // Data received
//            {
//                val newBuffer = ftdiUtilities.adaptArray(tempBuffer)
//                System.arraycopy(newBuffer, 0, buffer, 0, buffer.size)
//                var p = numberBytes / 64
//                if (numberBytes % 64 != 0) {
//                    p++
//                }
//                readen = numberBytes - p * 2
//            }
//        } while (readen <= 0)
//        return readen
//    }
//
//    override fun syncRead(buffer: ByteArray?, offset: Int, length: Int, timeout: Int): Int {
//        val beginTime = System.currentTimeMillis()
//        val stopTime = beginTime + timeout
//        if (asyncMode) {
//            return -1
//        }
//        if (buffer == null) {
//            return 0
//        }
//        var n = length / 62
//        if (length % 62 != 0) {
//            n++
//        }
//        val tempBuffer = ByteArray(length + n * 2)
//        var readen = 0
//        do {
//            var timeLeft = 0
//            if (timeout > 0) {
//                timeLeft = (stopTime - System.currentTimeMillis()).toInt()
//                if (timeLeft <= 0) {
//                    break
//                }
//            }
//            val numberBytes =
//                connection!!.bulkTransfer(inEndpoint, tempBuffer, tempBuffer.size, timeLeft)
//            if (numberBytes > 2) // Data received
//            {
//                val newBuffer = ftdiUtilities.adaptArray(tempBuffer)
//                System.arraycopy(newBuffer, 0, buffer, offset, length)
//                var p = numberBytes / 64
//                if (numberBytes % 64 != 0) {
//                    p++
//                }
//                readen = numberBytes - p * 2
//            }
//        } while (readen <= 0)
//        return readen
//    }
//
//    /**
//     * This method avoids creation of garbage by reusing the same
//     * array instance for skipping header bytes and running
//     * [UsbDeviceConnection.bulkTransfer]
//     * directly.
//     */
//    @SuppressLint("NewApi")
//    private fun readSyncJelly(buffer: ByteArray, timeout: Int, stopTime: Long): Int {
//        var read = 0
//        do {
//            var timeLeft = 0
//            if (timeout > 0) {
//                timeLeft = (stopTime - System.currentTimeMillis()).toInt()
//                if (timeLeft <= 0) {
//                    break
//                }
//            }
//            var numberBytes = connection!!.bulkTransfer(inEndpoint, skip, skip.size, timeLeft)
//            if (numberBytes > 2) // Data received
//            {
//                numberBytes = connection.bulkTransfer(inEndpoint, buffer, read, 62, timeLeft)
//                read += numberBytes
//            }
//        } while (read <= 0)
//        return read
//    }
//
//    // https://stackoverflow.com/questions/47303802/how-is-androids-string-usbdevice-getversion-encoded-from-word-bcddevice
//    private val bcdDevice: Short
//        private get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//            val descriptors = connection!!.rawDescriptors
//            ((descriptors[13].toInt() shl 8) + descriptors[12]).toShort()
//        } else {
//            -1
//        }
//    private val iSerialNumber: Byte
//        private get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//            val descriptors = connection!!.rawDescriptors
//            descriptors[16]
//        } else {
//            -1
//        }
//
//    private fun isBaudTolerated(speed: Long, target: Long): Boolean {
//        return speed >= target * 100 / 103 &&
//                speed <= target * 100 / 97
//    }
//
//    // Encoding baudrate as freebsd driver:
//    // https://github.com/freebsd/freebsd/blob/1d6e4247415d264485ee94b59fdbc12e0c566fd0/sys/dev/usb/serial/uftdi.c
//    private fun encodedBaudRate(baudRate: Int): ShortArray? {
//        var isFT232A = false
//        var clk12MHz = false
//        var hIndex = false
//        val ret = ShortArray(2)
//        val clk: Int
//        var divisor: Int
//        val fastClk: Int
//        var frac: Int
//        val hwSpeed: Int
//        val encodedFraction = byteArrayOf(
//            0, 3, 2, 4, 1, 5, 6, 7
//        )
//        val roundoff232a = byteArrayOf(
//            0, 1, 0, 1, 0, -1, 2, 1,
//            0, -1, -2, -3, 4, 3, 2, 1
//        )
//        val bcdDevice = bcdDevice
//        if (bcdDevice.toInt() == -1) {
//            return null
//        }
//        if (bcdDevice.toInt() == 0x200 && iSerialNumber.toInt() == 0) {
//            isFT232A = true
//        }
//        if (bcdDevice.toInt() == 0x500 || bcdDevice.toInt() == 0x700 || bcdDevice.toInt() == 0x800 || bcdDevice.toInt() == 0x900 || bcdDevice.toInt() == 0x1000) {
//            hIndex = true
//        }
//        if (bcdDevice.toInt() == 0x700 || bcdDevice.toInt() == 0x800 || bcdDevice.toInt() == 0x900) {
//            clk12MHz = true
//        }
//        if (baudRate >= 1200 && clk12MHz) {
//            clk = 12000000
//            fastClk = 1 shl 17
//        } else {
//            clk = 3000000
//            fastClk = 0
//        }
//        if (baudRate < clk shr 14 || baudRate > clk) {
//            return null
//        }
//        divisor = (clk shl 4) / baudRate
//        if (divisor and 0xf == 1) {
//            divisor = divisor and -0x8
//        } else if (isFT232A) {
//            divisor += roundoff232a[divisor and 0x0f]
//        } else {
//            divisor += 1 /* Rounds odd 16ths up to next 8th. */
//        }
//        divisor = divisor shr 1
//        hwSpeed = (clk shl 3) / divisor
//        if (!isBaudTolerated(hwSpeed.toLong(), baudRate.toLong())) {
//            return null
//        }
//        frac = divisor and 0x07
//        divisor = divisor shr 3
//        if (divisor == 1) {
//            if (frac == 0) {
//                divisor = 0 /* 1.0 becomes 0.0 */
//            } else {
//                frac = 0 /* 1.5 becomes 1.0 */
//            }
//        }
//        divisor = divisor or (encodedFraction[frac].toInt() shl 14 or fastClk)
//        ret[0] = divisor.toShort() //loBits
//        ret[1] =
//            if (hIndex) (divisor shr 8 and 0xFF00 or mInterface.id + 1).toShort() else (divisor shr 16).toShort() //hiBits
//        return ret
//    }
//
//    private fun setEncodedBaudRate(encodedBaudRate: ShortArray) {
//        connection!!.controlTransfer(
//            FTDI_REQTYPE_HOST2DEVICE,
//            FTDI_SIO_SET_BAUD_RATE,
//            encodedBaudRate[0].toInt(),
//            encodedBaudRate[1].toInt(),
//            null,
//            0,
//            USB_TIMEOUT
//        )
//    }
//
//    private fun setOldBaudRate(baudRate: Int) {
//        var value = 0
//        value =
//            if (baudRate >= 0 && baudRate <= 300) FTDI_BAUDRATE_300 else if (baudRate > 300 && baudRate <= 600) FTDI_BAUDRATE_600 else if (baudRate > 600 && baudRate <= 1200) FTDI_BAUDRATE_1200 else if (baudRate > 1200 && baudRate <= 2400) FTDI_BAUDRATE_2400 else if (baudRate > 2400 && baudRate <= 4800) FTDI_BAUDRATE_4800 else if (baudRate > 4800 && baudRate <= 9600) FTDI_BAUDRATE_9600 else if (baudRate > 9600 && baudRate <= 19200) FTDI_BAUDRATE_19200 else if (baudRate > 19200 && baudRate <= 38400) FTDI_BAUDRATE_38400 else if (baudRate > 19200 && baudRate <= 57600) FTDI_BAUDRATE_57600 else if (baudRate > 57600 && baudRate <= 115200) FTDI_BAUDRATE_115200 else if (baudRate > 115200 && baudRate <= 230400) FTDI_BAUDRATE_230400 else if (baudRate > 230400 && baudRate <= 460800) FTDI_BAUDRATE_460800 else if (baudRate > 460800 && baudRate <= 921600) FTDI_BAUDRATE_921600 else if (baudRate > 921600) FTDI_BAUDRATE_921600 else FTDI_BAUDRATE_9600
//        setControlCommand(FTDI_SIO_SET_BAUD_RATE, value, 0)
//    }
//
//    companion object {
//        private val CLASS_ID = FTDISerialDevice::class.java.simpleName
//        private const val FTDI_SIO_RESET = 0
//        private const val FTDI_SIO_MODEM_CTRL = 1
//        private const val FTDI_SIO_SET_FLOW_CTRL = 2
//        private const val FTDI_SIO_SET_BAUD_RATE = 3
//        private const val FTDI_SIO_SET_DATA = 4
//        private const val FTDI_REQTYPE_HOST2DEVICE = 0x40
//
//        /**
//         * RTS and DTR values obtained from FreeBSD FTDI driver
//         * https://github.com/freebsd/freebsd/blob/70b396ca9c54a94c3fad73c3ceb0a76dffbde635/sys/dev/usb/serial/uftdi_reg.h
//         */
//        private const val FTDI_SIO_SET_DTR_MASK = 0x1
//        private const val FTDI_SIO_SET_DTR_HIGH = 1 or (FTDI_SIO_SET_DTR_MASK shl 8)
//        private const val FTDI_SIO_SET_DTR_LOW = 0 or (FTDI_SIO_SET_DTR_MASK shl 8)
//        private const val FTDI_SIO_SET_RTS_MASK = 0x2
//        private const val FTDI_SIO_SET_RTS_HIGH = 2 or (FTDI_SIO_SET_RTS_MASK shl 8)
//        private const val FTDI_SIO_SET_RTS_LOW = 0 or (FTDI_SIO_SET_RTS_MASK shl 8)
//
//        /**
//         * BREAK on/off values obtained from linux driver
//         * https://github.com/torvalds/linux/blob/master/drivers/usb/serial/ftdi_sio.h
//         */
//        private const val FTDI_SIO_SET_BREAK_ON = 1 shl 14
//        private const val FTDI_SIO_SET_BREAK_OFF = 0 shl 14
//        const val FTDI_BAUDRATE_300 = 0x2710
//        const val FTDI_BAUDRATE_600 = 0x1388
//        const val FTDI_BAUDRATE_1200 = 0x09c4
//        const val FTDI_BAUDRATE_2400 = 0x04e2
//        const val FTDI_BAUDRATE_4800 = 0x0271
//        const val FTDI_BAUDRATE_9600 = 0x4138
//        const val FTDI_BAUDRATE_19200 = 0x809c
//        const val FTDI_BAUDRATE_38400 = 0xc04e
//        const val FTDI_BAUDRATE_57600 = 0x0034
//        const val FTDI_BAUDRATE_115200 = 0x001a
//        const val FTDI_BAUDRATE_230400 = 0x000d
//        const val FTDI_BAUDRATE_460800 = 0x4006
//        const val FTDI_BAUDRATE_921600 = 0x8003
//
//        /***
//         * Default Serial Configuration
//         * Baud rate: 9600
//         * Data bits: 8
//         * Stop bits: 1
//         * Parity: None
//         * Flow Control: Off
//         */
//        private const val FTDI_SET_DATA_DEFAULT = 0x0008
//        private const val FTDI_SET_MODEM_CTRL_DEFAULT1 = 0x0101
//        private const val FTDI_SET_MODEM_CTRL_DEFAULT2 = 0x0202
//        private const val FTDI_SET_MODEM_CTRL_DEFAULT3 = 0x0100
//        private const val FTDI_SET_MODEM_CTRL_DEFAULT4 = 0x0200
//        private const val FTDI_SET_FLOW_CTRL_DEFAULT = 0x0000
//        private val EMPTY_BYTE_ARRAY = byteArrayOf()
//
//        // Special treatment needed to FTDI devices
//        @JvmStatic
//        fun adaptArray(ftdiData: ByteArray): ByteArray {
//            val length = ftdiData.size
//            return if (length > 64) {
//                var n = 1
//                var p = 64
//                // Precalculate length without FTDI headers
//                while (p < length) {
//                    n++
//                    p = n * 64
//                }
//                val realLength = length - n * 2
//                val data = ByteArray(realLength)
//                copyData(ftdiData, data)
//                data
//            } else if (length == 2) // special case optimization that returns the same instance.
//            {
//                EMPTY_BYTE_ARRAY
//            } else {
//                Arrays.copyOfRange(ftdiData, 2, length)
//            }
//        }
//
//        // Copy data without FTDI headers
//        private fun copyData(src: ByteArray, dst: ByteArray) {
//            var srcPos = 2
//            var dstPos = 0
//            while (srcPos - 2 <= src.size - 64) {
//                System.arraycopy(src, srcPos, dst, dstPos, 62)
//                srcPos += 64
//                dstPos += 62
//            }
//            val remaining = src.size - srcPos + 2
//            if (remaining > 0) {
//                System.arraycopy(src, srcPos, dst, dstPos, remaining - 2)
//            }
//        }
//
//        private val skip = ByteArray(2)
//    }
//
//    init {
//        ftdiUtilities = FTDIUtilities()
//        rtsCtsEnabled = false
//        dtrDsrEnabled = false
//        ctsState = true
//        dsrState = true
//        firstTime = true
//        mInterface = device.getInterface(if (iface >= 0) iface else 0)
//    }
//}