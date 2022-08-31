package com.usb.utils

import java.lang.StringBuilder
import kotlin.experimental.and

object HexData {
    private const val HEXES = "0123456789ABCDEF"
    private const val HEX_INDICATOR = "0x"
    private const val SPACE = " "
    @JvmStatic
    fun hexToString(data: ByteArray?): String? {
        return if (data != null) {
            val hex = StringBuilder(2 * data.size)
            for (element in data) {
                hex.append(HEX_INDICATOR)
                hex.append(HEXES[(element and 0xF0.toByte()).toInt() shr 4])
                    .append(HEXES[(element and 0x0F).toInt()])
                hex.append(SPACE)
            }
            hex.toString()
        } else {
            null
        }
    }

    fun stringTobytes(hexString: String): ByteArray {
        var stringProcessed = hexString.trim { it <= ' ' }.replace("0x".toRegex(), "")
        stringProcessed = stringProcessed.replace("\\s+".toRegex(), "")
        val data = ByteArray(stringProcessed.length / 2)
        var i = 0
        var j = 0
        while (i <= stringProcessed.length - 1) {
            val character = stringProcessed.substring(i, i + 2).toInt(16).toByte()
            data[j] = character
            j++
            i += 2
        }
        return data
    }

    fun hex4digits(id: String): String {
        if (id.length == 1) return "000$id"
        if (id.length == 2) return "00$id"
        return if (id.length == 3) "0$id" else id
    }
}