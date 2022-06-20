package com.example.usbapp

import `in`.sunfox.android.spandanEngine.usb_connection.UsbConnectionContract
import `in`.sunfox.android.spandanEngine.usb_connection.UsbConnectionHelper
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity.TAG"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.start_test).setOnClickListener {
            UsbConnectionHelper.startTransmission()
        }


        UsbConnectionHelper.bind(object : UsbConnectionContract{
            override fun onDeviceDisconnected() {
                Log.d(TAG, "onDeviceDisconnected: ")
            }

            override fun onDeviceConnected() {
                Log.d(TAG, "onDeviceConnected: ")
            }

            override fun onUsbPermissionGranted() {
                Log.d(TAG, "onUsbPermissionGranted:")
            }

            override fun onReceiveData(data: String?) {
                Log.d(TAG, "onReceiveData: $data")
            }

            override fun onDeviceVerified() {
                Log.d(TAG, "onDeviceVerified: ")
//                findViewById<Button>(R.id.start_test).text = "verified"
            }

            override fun onVerificationTimeout() {
                Log.d(TAG, "onVerificationTimeout: ")
            }

            override fun onUsbPermissionDenied() {
                Log.d(TAG, "onUsbPermissionDenied: ")
            }

            override fun onDeviceReconnect() {
                Log.d(TAG, "onDeviceReconnect: ")
            }

        },this)
    }
}