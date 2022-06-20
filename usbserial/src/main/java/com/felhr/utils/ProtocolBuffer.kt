package com.felhr.utils

import com.annimon.stream.IntStream
import com.annimon.stream.function.IntPredicate
import com.felhr.utils.ProtocolBuffer
import kotlin.jvm.Synchronized
import com.felhr.utils.ProtocolBuffer.SeparatorPredicate
import java.io.UnsupportedEncodingException
import java.lang.StringBuilder
import java.nio.charset.Charset
import java.util.*

// Thanks to Thomas Moorhead for improvements and suggestions
class ProtocolBuffer {
    private var mode: String
    private lateinit var rawBuffer: ByteArray
    private var bufferPointer = 0
    private lateinit var separator: ByteArray
    private var delimiter: String? = null
    private var stringBuffer: StringBuilder? = null
    private val commands: MutableList<String> = ArrayList()
    private val rawCommands: MutableList<ByteArray> = ArrayList()

    constructor(mode: String) {
        this.mode = mode
        if (mode == BINARY) {
            rawBuffer = ByteArray(DEFAULT_BUFFER_SIZE)
        } else {
            stringBuffer = StringBuilder(DEFAULT_BUFFER_SIZE)
        }
    }

    constructor(mode: String, bufferSize: Int) {
        this.mode = mode
        if (mode == BINARY) {
            rawBuffer = ByteArray(bufferSize)
        } else {
            stringBuffer = StringBuilder(bufferSize)
        }
    }

    fun setDelimiter(delimiter: String?) {
        this.delimiter = delimiter
    }

    fun setDelimiter(delimiter: ByteArray) {
        separator = delimiter
    }

    @Synchronized
    fun appendData(data: ByteArray) {
        // Ignore the frequent empty calls
        if (data.isEmpty()) return
        if (mode == TEXT) {
            try {
                val dataStr = String(data, Charsets.UTF_8)
                stringBuffer!!.append(dataStr)
                val buffer = stringBuffer.toString()
                var prevIndex = 0
                var index = buffer.indexOf(delimiter!!)
                while (index >= 0) {
                    val tempStr = buffer.substring(prevIndex, index + delimiter!!.length)
                    commands.add(tempStr)
                    prevIndex = index + delimiter!!.length
                    index = stringBuffer.toString().indexOf(delimiter!!, prevIndex)
                }
                if ( /*prevIndex < buffer.length() &&*/prevIndex > 0) {
                    val tempStr = buffer.substring(prevIndex, buffer.length)
                    stringBuffer!!.setLength(0)
                    stringBuffer!!.append(tempStr)
                }
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        } else if (mode == BINARY) {
            appendRawData(data)
        }
    }

    fun hasMoreCommands(): Boolean {
        return if (mode == TEXT) {
            commands.size > 0
        } else {
            rawCommands.size > 0
        }
    }

    fun nextTextCommand(): String? {
        return if (commands.size > 0) {
            commands.removeAt(0)
        } else {
            null
        }
    }

    fun nextBinaryCommand(): ByteArray? {
        return if (rawCommands.size > 0) {
            rawCommands.removeAt(0)
        } else {
            null
        }
    }

    private fun appendRawData(rawData: ByteArray) {
        System.arraycopy(rawData, 0, rawBuffer, bufferPointer, rawData.size)
        bufferPointer += rawData.size
        val predicate = SeparatorPredicate()
        val indexes = IntStream.range(0, bufferPointer)
            .filter(predicate)
            .toArray()
        var prevIndex = 0
        for (i in indexes) {
            val command = Arrays.copyOfRange(rawBuffer, prevIndex, i + separator.size)
            rawCommands.add(command)
            prevIndex = i + separator.size
        }
        if (prevIndex < rawBuffer.size
            && prevIndex > 0
        ) {
            val tempBuffer = Arrays.copyOfRange(rawBuffer, prevIndex, rawBuffer.size)
            bufferPointer = 0
            System.arraycopy(tempBuffer, 0, rawBuffer, bufferPointer, rawData.size)
            bufferPointer += rawData.size
        }
    }

    private inner class SeparatorPredicate : IntPredicate {
        override fun test(value: Int): Boolean {
            if (rawBuffer[value] == separator[0]) {
                for (i in 1..separator.size - 1) {
                    if (rawBuffer[value + i] != separator[i]) {
                        return false
                    }
                }
                return true
            }
            return false
        }
    }

    companion object {
        const val BINARY = "binary"
        const val TEXT = "text"
        private const val DEFAULT_BUFFER_SIZE = 16 * 1024
    }
}