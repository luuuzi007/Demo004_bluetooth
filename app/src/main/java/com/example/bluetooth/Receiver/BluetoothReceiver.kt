package com.example.bluetooth.Receiver

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log

/**
 * @author luuuzi
 * @date 2020-06-18.
 * @description
 */
class BluetoothReceiver(context: Context, var mListener: Listener) : BroadcastReceiver() {
    companion object {
        val tag: String = BluetoothReceiver::class.java.simpleName
    }

    init {
        Log.i(tag, "构造方法初始化")
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)//蓝牙开关改变状态
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)//蓝牙开始扫描
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)//蓝牙扫描结束

        filter.addAction(BluetoothDevice.ACTION_FOUND)//蓝牙发现新设备(未配对的设备)
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)//在系统弹出配对框之前(确认/输入配对码)
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)//设备配对状态改变
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)//最底层连接建立
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED) //BluetoothAdapter连接状态

        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED) //BluetoothHeadset连接状态
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED) //BluetoothA2dp连接状态
        filter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED) //BluetoothA2dp连接状态

        context.registerReceiver(this, filter)
    }

    override fun onReceive(p0: Context?, p1: Intent?) {
        val action = p1!!.action ?: return
        Log.i(tag, "action=$action")
        val dev = p1.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
        if(dev!=null) {
            Log.i(tag, "name:${dev.name},address:${dev.address}")
        }
        when (action) {
            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                val intExtra = p1.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)
                //关闭值为13,10；开启为11,12
                Log.i(tag, "蓝牙开关状态:$intExtra")
            }
            BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                Log.i(tag, "蓝牙开始扫描")
            }
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                Log.i(tag, "蓝牙扫描结束")
                mListener.scanFinish()
            }
            BluetoothDevice.ACTION_FOUND -> {//蓝牙发现新设备(未配对的设备)

                mListener.foundev(dev)
            }
        }
    }

    interface Listener {
        fun foundev(dev: BluetoothDevice?)
        fun scanFinish()
    }
}