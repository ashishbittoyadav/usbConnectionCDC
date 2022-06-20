package com.felhr.usbserial

import kotlin.jvm.Volatile

abstract class AbstractWorkerThread : Thread() {
    @JvmField
    var firstTime = true

    @Volatile
    private var keep = true

    @Volatile
    private var workingThread: Thread? = null
    fun stopThread() {
        keep = false
        if (workingThread != null) {
            workingThread!!.interrupt()
        }
    }

    override fun run() {
        if (!keep) {
            return
        }
        workingThread = currentThread()
        while (keep && !(workingThread as Thread).isInterrupted) {
            doRun()
        }
    }

    abstract fun doRun()
}