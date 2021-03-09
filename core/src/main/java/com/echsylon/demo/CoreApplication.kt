package com.echsylon.demo

import android.app.Application
import android.util.Log
import com.echsylon.demo.core.BuildConfig
import com.echsylon.demo.connection.BleConnection
import com.echsylon.demo.connection.Connection
import com.echsylon.demo.discovery.BleScanner
import com.echsylon.demo.discovery.Scanner
import org.jetbrains.annotations.TestOnly
import timber.log.Timber

import timber.log.Timber.DebugTree


open class CoreApplication : Application() {
    @TestOnly
    var customScanner: Scanner? = null

    @TestOnly
    var customConnection: Connection? = null

    private val defaultScanner: Scanner by lazy { BleScanner() }
    private val defaultConnection: Connection by lazy { BleConnection(this) }

    val scanner: Scanner
        get() = customScanner ?: defaultScanner

    val connection: Connection
        get() = customConnection ?: defaultConnection


    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        } else {
            Timber.plant(ProdTree())
        }
    }

    private class ProdTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            when (priority) {
                Log.WARN -> Unit    // Implement production worthy logging here
                Log.ERROR -> Unit   // Implement production worthy logging here
                Log.DEBUG -> Unit   // Don't log debug messages for production
                Log.VERBOSE -> Unit // Don't log verbose messages for production
            }
        }
    }
}