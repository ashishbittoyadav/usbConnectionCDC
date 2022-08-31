package com.usb.usbserial

import android.util.Log
import com.usb.utils.HexData.hexToString

object UsbSerialDebugger {
    private val CLASS_ID = UsbSerialDebugger::class.java.simpleName
    const val ENCODING = "UTF-8"
    fun printLogGet(src: ByteArray, verbose: Boolean) {
        if (!verbose) {
            Log.i(CLASS_ID, "Data obtained from write buffer: " + String(src))
        } else {
            Log.i(CLASS_ID, "Data obtained from write buffer: " + String(src))
            Log.i(CLASS_ID, "Raw data from write buffer: " + hexToString(src))
            Log.i(CLASS_ID, "Number of bytes obtained from write buffer: " + src.size)
        }
    }

    fun printLogPut(src: ByteArray, verbose: Boolean) {
        if (!verbose) {
            Log.i(CLASS_ID, "Data obtained pushed to write buffer: " + String(src))
        } else {
            Log.i(CLASS_ID, "Data obtained pushed to write buffer: " + String(src))
            Log.i(CLASS_ID, "Raw data pushed to write buffer: " + hexToString(src))
            Log.i(CLASS_ID, "Number of bytes pushed from write buffer: " + src.size)
        }
    }

    fun printReadLogGet(src: ByteArray, verbose: Boolean) {
        if (!verbose) {
            Log.i(CLASS_ID, "Data obtained from Read buffer: " + String(src))
        } else {
            Log.i(CLASS_ID, "Data obtained from Read buffer: " + String(src))
            Log.i(CLASS_ID, "Raw data from Read buffer: " + hexToString(src))
            Log.i(CLASS_ID, "Number of bytes obtained from Read buffer: " + src.size)
        }
    }

    fun printReadLogPut(src: ByteArray, verbose: Boolean) {
        if (!verbose) {
            Log.i(CLASS_ID, "Data obtained pushed to read buffer: " + String(src))
        } else {
            Log.i(CLASS_ID, "Data obtained pushed to read buffer: " + String(src))
            Log.i(CLASS_ID, "Raw data pushed to read buffer: " + hexToString(src))
            Log.i(CLASS_ID, "Number of bytes pushed from read buffer: " + src.size)
        }
    }
}