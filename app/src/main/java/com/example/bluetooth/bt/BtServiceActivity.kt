package com.example.bluetooth.bt

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
import com.example.bluetooth.R
import kotlinx.android.synthetic.main.activity_bt_client.*


/**
 * @author luuuzi
 * @date 2020-06-19.
 * @description bt服务端
 */
class BtServiceActivity : AppCompatActivity(), BtManager.Listener, View.OnClickListener {
    private lateinit var tvStatus: AppCompatTextView
    private lateinit var tvLog: AppCompatTextView
    private lateinit var mServer: BtServer
    private var mHandler: Handler = Handler()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bt_service)
        initView()
        initData()
    }

    private fun initView() {
        tvStatus = findViewById(R.id.tvStatus)
        tvLog = findViewById(R.id.tvLog)

        findViewById<AppCompatEditText>(R.id.etMsg)
        findViewById<AppCompatEditText>(R.id.etCmd)
        findViewById<AppCompatEditText>(R.id.etFile)

        findViewById<AppCompatButton>(R.id.btnSendMsg).setOnClickListener(this)
        findViewById<AppCompatButton>(R.id.btnSendCmd).setOnClickListener(this)
        findViewById<AppCompatButton>(R.id.btnSendFile).setOnClickListener(this)

    }

    private fun initData() {
        mServer = BtServer(this)
    }

    override fun socketNotify(state: Int, obj: Any?) {
        mHandler.post {
            if (isDestroyed) return@post
            var msg: String? = null
            when (state) {
                BtManager.Listener.CONNECTED -> {
                    val dev = obj as BluetoothDevice
                    msg = String.format("与%s(%s)连接成功", dev.name, dev.address)
                    tvStatus.text = msg
                }
                BtManager.Listener.DISCONNECTED -> {
                    mServer.listen()
                    msg = "连接断开,正在重新监听..."
                    tvStatus.text = msg
                }
                BtManager.Listener.SEND_MSG -> {
                    tvLog.append(java.lang.String.format("\n%s", obj))
                }
                BtManager.Listener.RECEIVED_MSG -> {
                    tvLog.append(java.lang.String.format("\n%s", obj))
                }
            }
        }
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.btnSendMsg -> {//发送消息
                if (!mServer.isConnected(null)){
                    Toast.makeText(this, "没有连接", Toast.LENGTH_SHORT).show()
                    return
                }
                val msg = etMsg.text.toString()
                if (TextUtils.isEmpty(msg)) {
                    Toast.makeText(this, "请输入消息", Toast.LENGTH_SHORT).show()
                } else {
                    mServer.sendMsg(msg)
                }
            }
            R.id.btnSendCmd -> {//发送命令
                if (!mServer.isConnected(null)){
                    Toast.makeText(this, "没有连接", Toast.LENGTH_SHORT).show()
                    return
                }
                val cmd = etCmd.text.toString()
                if (TextUtils.isEmpty(cmd)) {
                    Toast.makeText(this, "请输入命令", Toast.LENGTH_SHORT).show()
                } else {
                    mServer.sendCmd(cmd .toByteArray())
                }
            }
            R.id.btnSendFile -> {//发送文件
                if (!mServer.isConnected(null)){
                    Toast.makeText(this, "没有连接", Toast.LENGTH_SHORT).show()
                    return
                }
                val path = etFile.text.toString()
                if (TextUtils.isEmpty(path)) {
                    Toast.makeText(this, "请选择文件", Toast.LENGTH_SHORT).show()
                } else {
                    mServer.sendFile(path)
                }
            }
        }
    }
}