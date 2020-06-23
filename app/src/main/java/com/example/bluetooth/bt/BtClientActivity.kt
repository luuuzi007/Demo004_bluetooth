package com.example.bluetooth.bt

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetooth.R
import com.example.bluetooth.Receiver.BluetoothReceiver
import com.example.bluetooth.adapter.BtDevAdapter
import kotlinx.android.synthetic.main.activity_bt_client.*


/**
 * @author luuuzi
 * @date 2020-06-17.
 * @description 经典蓝牙
 */
class BtClientActivity : AppCompatActivity(), BluetoothReceiver.Listener, View.OnClickListener,
    BtDevAdapter.Listener, BtManager.Listener {
    companion object {
        val tag: String = BtClientActivity::class.java.simpleName
    }

    private lateinit var tvStatus: AppCompatTextView
    private var mBluetoothAdapter: BluetoothAdapter? = null
    lateinit var mBluetoothReceiver: BluetoothReceiver
    lateinit var mAdapter: BtDevAdapter
    private var mHandler: Handler = Handler()
    private var btManager: BtManager = BtManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bt_client)
        initView()
        initData()

    }

    private fun initView() {
        findViewById<AppCompatEditText>(R.id.etMsg)
        findViewById<AppCompatEditText>(R.id.etCmd)
        findViewById<AppCompatEditText>(R.id.etFile)

        findViewById<AppCompatButton>(R.id.btnOpenBt).setOnClickListener(this)
        findViewById<AppCompatButton>(R.id.btnScanBt).setOnClickListener(this)
        findViewById<AppCompatButton>(R.id.btnSendMsg).setOnClickListener(this)
        findViewById<AppCompatButton>(R.id.btnSendCmd).setOnClickListener(this)
        findViewById<AppCompatButton>(R.id.btnSendFile).setOnClickListener(this)

        tvStatus = findViewById(R.id.tvStatus)
        val rlvDevices = findViewById<RecyclerView>(R.id.rlvDevices)

        rlvDevices.layoutManager = LinearLayoutManager(this)
        mAdapter = BtDevAdapter(this, this)
        rlvDevices.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rlvDevices.adapter = mAdapter
    }

    private fun initData() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        mBluetoothReceiver = BluetoothReceiver(this, this)
        if (supperBluetooth()) return
        //查询已配对设备
        val bondedDevices = mBluetoothAdapter!!.bondedDevices
        bondedDevices.forEach {
            mAdapter.add(it)
        }
//扫描
        if (!mBluetoothAdapter!!.isEnabled) {
            Toast.makeText(this, "请先开启蓝牙", Toast.LENGTH_SHORT).show()
            return
        }
        if (!mBluetoothAdapter!!.isDiscovering) {
            mBluetoothAdapter!!.startDiscovery()//扫描经典蓝牙和低功耗蓝牙(时间12s)
        } else {
            Toast.makeText(this, "正在扫描", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.btnOpenBt -> {//打开蓝牙
                supperBluetooth()
                if (!mBluetoothAdapter!!.isEnabled) {
                    //方式1
//            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            startActivity(intent)
                    //方式2
                    mBluetoothAdapter!!.enable()
                } else {
                    Toast.makeText(this, "蓝牙已打开", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.btnScanBt -> {//重新扫描
                supperBluetooth()
                mAdapter.renewScan()
            }
            R.id.btnSendMsg -> {//发送消息
                if (!btManager.isConnected(null)) {
                    Toast.makeText(this, "没有连接", Toast.LENGTH_SHORT).show()
                    return
                }
                val msg = etMsg.text.toString()
                if (TextUtils.isEmpty(msg)) {
                    Toast.makeText(this, "请输入消息", Toast.LENGTH_SHORT).show()
                } else {
                    btManager.sendMsg(msg)
                }
            }
            R.id.btnSendCmd -> {//发送命令
                if (!btManager.isConnected(null)) {
                    Toast.makeText(this, "没有连接", Toast.LENGTH_SHORT).show()
                    return
                }
                val cmd = etCmd.text.toString()
                if (TextUtils.isEmpty(cmd)) {
                    Toast.makeText(this, "请输入命令", Toast.LENGTH_SHORT).show()
                } else {
                    btManager.sendCmd(cmd.toByteArray())
                }
            }
            R.id.btnSendFile -> {//发送文件
                if (!btManager.isConnected(null)) {
                    Toast.makeText(this, "没有连接", Toast.LENGTH_SHORT).show()
                    return
                }
                val path = etFile.text.toString()
                if (TextUtils.isEmpty(path)) {
                    Toast.makeText(this, "请选择文件", Toast.LENGTH_SHORT).show()
                } else {
                    btManager.sendFile(path)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mBluetoothReceiver)
        btManager.unListener()
    }

    /**
     * 设备是否支持蓝牙
     */
    private fun supperBluetooth(): Boolean {
        return if (mBluetoothAdapter == null) {
            Toast.makeText(this, "当前设备不支持蓝牙", Toast.LENGTH_SHORT).show()
            true
        } else {
            false
        }
    }

    override fun foundev(dev: BluetoothDevice?) {
        mAdapter.add(dev)
    }

    override fun scanFinish() {
        Toast.makeText(this, "扫描结束", Toast.LENGTH_SHORT).show()
    }

    /**
     * item点击事件:连接蓝牙
     */
    override fun onItemClick(dev: BluetoothDevice?) {

        if (btManager.isConnected(dev)) {
            Toast.makeText(this, "已经连接了", Toast.LENGTH_SHORT).show()
            return
        }
        btManager.connect(dev)
        Toast.makeText(this, "正在连接", Toast.LENGTH_SHORT).show()
        tvStatus.text = "正在连接"
    }

    //回调
    private lateinit var msg: String

    override fun socketNotify(state: Int, obj: Any?) {
        mHandler.post(object : Runnable {
            override fun run() {
                if (isDestroyed) {
                    return
                }
                when (state) {
                    BtManager.Listener.CONNECTED -> {
                        val dev = obj as BluetoothDevice
                        msg = String.format("与%s(%s)连接成功", dev.name, dev.address)
                        tvStatus.text = msg
                    }
                    BtManager.Listener.DISCONNECTED -> {
                        msg = "连接断开"
                        tvStatus.text = msg
                    }
                    BtManager.Listener.SEND_MSG -> {
                        msg = obj as String
                        tvStatus.append(msg + "\n")
                    }
                    BtManager.Listener.RECEIVED_MSG -> {//接收
                        Toast.makeText(this@BtClientActivity, "接收到数据:$obj", Toast.LENGTH_SHORT)
                            .show()
                        msg = obj as String
                        tvStatus.append(msg + "\n")
                    }
                }
            }
        })

    }

}