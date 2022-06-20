package com.felhr.usbserial

import java.io.IOException
import java.io.InputStream
import java.lang.IndexOutOfBoundsException
import kotlin.Throws
import kotlin.experimental.and
import kotlin.Int

class SerialInputStream : InputStream {
    private var timeout = 0
    private var maxBufferSize = 16 * 1024
    private val buffer: ByteArray
    private var pointer: Int
    private var bufferSize: Int
    protected val device: UsbSerialInterface

    constructor(device: UsbSerialInterface) {
        this.device = device
        buffer = ByteArray(maxBufferSize)
        pointer = 0
        bufferSize = -1
    }

    constructor(device: UsbSerialInterface, maxBufferSize: Int) {
        this.device = device
        this.maxBufferSize = maxBufferSize
        buffer = ByteArray(this.maxBufferSize)
        pointer = 0
        bufferSize = -1
    }

    override fun read(): Int {
        val value = checkFromBuffer()
        if (value >= 0) return value
        val ret = device.syncRead(buffer, timeout)
        return if (ret >= 0) ({
            bufferSize = ret
            buffer[pointer++] and 0xff.toByte()
        }).toString().toInt() else {
            -1
        }
    }

    override fun read(b: ByteArray): Int {
        return device.syncRead(b, timeout)
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (off < 0) {
            throw IndexOutOfBoundsException("Offset must be >= 0")
        }
        if (len < 0) {
            throw IndexOutOfBoundsException("Length must positive")
        }
        if (len > b.size - off) {
            throw IndexOutOfBoundsException("Length greater than b.length - off")
        }
        return if (off == 0 && len == b.size) {
            read(b)
        } else device.syncRead(b, off, len, timeout)
    }

    @Throws(IOException::class)
    override fun available(): Int {
        return if (bufferSize > 0) bufferSize - pointer else 0
    }

    fun setTimeout(timeout: Int) {
        this.timeout = timeout
    }

    private fun checkFromBuffer(): Int {
        return if (bufferSize > 0 && pointer < bufferSize) ({
            buffer[pointer++] and 0xff.toByte()
        }).toString().toInt() else {
            pointer = 0
            bufferSize = -1
            -1
        }
    }
}