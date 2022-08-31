package com.example.usbapp.usb_connection

import android.util.Log
import com.usb.usbserial.UsbSerialDevice
import com.usb.usbserial.UsbSerialInterface
import com.usb.usbserial.UsbSerialInterface.UsbReadCallback
import java.nio.charset.StandardCharsets


class SerialDeviceDataReceiveThread(private val serialPort: UsbSerialDevice?) : Thread() {
    private var validData = ""
    var data1: String? = null
    var i = 0
    override fun run() {
        if (serialPort != null) {
            Log.d("STMDEVICE", "Serial Port not null!")
            if (serialPort.open()) {
                Log.d("STMDEVICE", "Serial Port OPENED!")
                serialPort.setBaudRate(115200)
                serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8)
                serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
                serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1)
                serialPort.setParity(UsbSerialInterface.PARITY_NONE)
                serialPort.read(object : UsbReadCallback {
                    override fun onReceivedData(data: ByteArray?) {
                        /*try {*/
                        data1 = String(data!!, StandardCharsets.UTF_8)
                        if (i < 10) {
                            i++
                            Log.d("jggg.hfg.hcg", ": onData: $data")
                        }
                        if ((data1)!!.contains(ConstantHelper.DATA_DELIMITTER)) {
                            //Log.d("STMDEVICE","Data Recieved!");
                            while (data1!!.contains(ConstantHelper.DATA_DELIMITTER)) {
                                val tempData = data1!!.substring(
                                    0,
                                    data1!!.indexOf(ConstantHelper.DATA_DELIMITTER) + 1
                                )
                                validData += tempData
                                Log.d("STMDEVICE", "data: $validData")
                                //  Log.d("jggg.hfg.hcg", "onReceivedData: "+validData+"\n");
                                try {
                                    UsbConnectionHelper.onReceiveRawData(validData)
                                } catch (e: Exception) {
                                    Log.d("STMDEVICE", "data: ${e.message}")
                                }
                                validData = ""
                                if (data1!!.indexOf(ConstantHelper.DATA_DELIMITTER) + 1 >= data1!!.length) data1 =
                                    "" else data1 =
                                    data1!!.substring(data1!!.indexOf(ConstantHelper.DATA_DELIMITTER) + 1)
                            }
                            validData = data1 as String
                        } else if (data1!!.matches("[0-9]+".toRegex())) {
                            validData += data
                        } else {
                        }
                    }
                } /* catch (Exception e) {
                                        Log.d("STMDEVICE", "EXCEPTION: "+e.getMessage());
                                        e.printStackTrace();
                                    }*/ // }
                )
            }
        } else {
            Log.d("STMDEVICE", "SERIAL PORT IS NULL!!!!!")
        }
    }
}
