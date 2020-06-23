package com.example.bluetooth.bt

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.util.Log
import java.util.concurrent.Executors


/**
 * @author luuuzi
 * @date 2020-06-19.
 * @description bt服务端控制
 */
class BtServer(lisenter: Listener) : BtManager(lisenter) {
    private var mSSocket: BluetoothServerSocket? = null

    init {
        listen()
    }

    fun listen() {
        Log.i(tag, "开始监听")
        try {
            val adapter = BluetoothAdapter.getDefaultAdapter()
//            mSSocket = adapter.listenUsingRfcommWithServiceRecord(TAG, SPP_UUID); //加密传输，Android强制执行配对，弹窗显示配对码
            mSSocket =
                adapter.listenUsingInsecureRfcommWithServiceRecord(tag, SPP_UUID) //明文传输(不安全)，无需配对
            // 开启子线程
            Executors.newCachedThreadPool().execute(Runnable {
                try {
                    val socket = mSSocket!!.accept() // 监听连接
                    mSSocket!!.close() // 关闭监听，只连接一个设备
                    loopRead(socket) // 循环读取
                } catch (e: Throwable) {
                    close()
                }
            })
        } catch (e: Throwable) {
            close()
        }
    }
}