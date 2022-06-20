package com.felhr.usbserial

import android.util.Log
import com.felhr.usbserial.UsbSerialDebugger.printReadLogGet
import com.felhr.usbserial.UsbSerialDebugger.printLogPut
import com.felhr.usbserial.UsbSerialDebugger.printLogGet
import com.felhr.usbserial.SerialBuffer.SynchronizedBuffer
import com.felhr.usbserial.UsbSerialDebugger
import kotlin.jvm.Synchronized
import com.felhr.usbserial.SerialBuffer
import okio.Buffer
import java.io.EOFException
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class SerialBuffer(version: Boolean) {
    private var readBuffer: ByteBuffer? = null
    private val writeBuffer: SynchronizedBuffer = SynchronizedBuffer()
    lateinit var bufferCompatible : ByteArray
    private var debugging = false

    /*
     * Print debug messages
     */
    fun debug(value: Boolean) {
        debugging = value
    }

    fun getReadBuffer(): ByteBuffer? {
        synchronized(this) { return readBuffer }
    }

    val dataReceived: ByteArray
        get() {
            synchronized(this) {
                val dst = ByteArray(readBuffer!!.position())
                readBuffer!!.position(0)
                readBuffer!![dst, 0, dst.size]
                Log.d("UsbSerialDevice.TAG", "data received ${String(dst)}")
                if (debugging) printReadLogGet(dst, true)
                return dst
            }
        }

    fun clearReadBuffer() {
        synchronized(this) { readBuffer!!.clear() }
    }

    fun getWriteBuffer(): ByteArray {
        return writeBuffer.get()
    }

    fun putWriteBuffer(data: ByteArray?) {
        writeBuffer.put(data)
    }

    fun getDataReceivedCompatible(numberBytes: Int): ByteArray {
        return bufferCompatible.copyOfRange(0, numberBytes)
    }

    private inner class SynchronizedBuffer internal constructor() {
        private val buffer: Buffer = Buffer()

        private val lock = ReentrantLock()
        private val condition = lock.newCondition()


        fun put(src: ByteArray?) {
            lock.withLock {
                if (src == null || src.isEmpty()) return
                if (debugging) printLogPut(src, true)
                buffer.write(src)
                condition.signal()
            }
        }


        fun get(): ByteArray {
            lock.withLock {
                if (buffer.size == 0L) {
                    try {
                        condition.await()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                        Thread.currentThread().interrupt()
                    }
                }
                val dst: ByteArray = if (buffer.size <= MAX_BULK_BUFFER) {
                    buffer.readByteArray()
                } else {
                    try {
                        buffer.readByteArray(MAX_BULK_BUFFER.toLong())
                    } catch (e: EOFException) {
                        e.printStackTrace()
                        return ByteArray(0)
                    }
                }
                if (debugging) printLogGet(dst, true)
                return dst
            }
            }
    }

    companion object {
        const val DEFAULT_READ_BUFFER_SIZE = 16 * 1024
        const val MAX_BULK_BUFFER = 16 * 1024
    }

    init {
        if (version) {
            readBuffer = ByteBuffer.allocate(DEFAULT_READ_BUFFER_SIZE)
        } else {
            bufferCompatible = ByteArray(DEFAULT_READ_BUFFER_SIZE)
        }
    }
}