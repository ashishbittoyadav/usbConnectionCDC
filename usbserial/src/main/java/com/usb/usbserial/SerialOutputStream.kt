package com.usb.usbserial

import java.io.OutputStream
import java.lang.IndexOutOfBoundsException

class SerialOutputStream(protected val device: UsbSerialInterface) : OutputStream() {
    private var timeout = 0
    override fun write(b: Int) {
        device.syncWrite(byteArrayOf(b.toByte()), timeout)
    }

    override fun write(b: ByteArray) {
        device.syncWrite(b, timeout)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        if (off < 0) {
            throw IndexOutOfBoundsException("Offset must be >= 0")
        }
        if (len < 0) {
            throw IndexOutOfBoundsException("Length must positive")
        }
        if (off + len > b.size) {
            throw IndexOutOfBoundsException("off + length greater than buffer length")
        }
        if (off == 0 && len == b.size) {
            write(b)
            return
        }
        device.syncWrite(b, off, len, timeout)
    }

    fun setTimeout(timeout: Int) {
        this.timeout = timeout
    }
}