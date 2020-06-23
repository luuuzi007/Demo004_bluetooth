package com.example.bluetooth.bt

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.util.*
import java.util.concurrent.Executors


/**
 * @author luuuzi
 * @date 2020-06-19.
 * @description 蓝牙管理
 */
open class BtManager(listener: Listener) {
    companion object {
        val tag: String = "BtManager"
        //#蓝牙串口服务uuid
        val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        val FLAG_MSG = 0;  //消息标记
        val FLAG_FILE = 1; //文件标记

        val COM_READ_SN2 = byteArrayOf(
            0XAA.toByte(),
            0X55,
            0X00,
            0X00,
            0X00,
            0X05,
            0XFF.toByte(),
            0XFA.toByte(),
            0X00,
            0X00,
            0X02,
            0X00,
            0X00,
            0XFD.toByte(),
            0XAA.toByte(),
            0X55,
            0X00,
            0X00,
            0X00,
            0X68,
            0XFF.toByte(),
            0X97.toByte(),
            0X01,
            0X00,
            0X00,
            0X64,
            0X01,
            0X02,
            0X03,
            0X04,
            0X05,
            0X06,
            0X07,
            0X08,
            0X09,
            0X0A.toByte(),
            0X0B.toByte(),
            0X0C.toByte()
            ,
            0X0D.toByte(),
            0X0E.toByte(),
            0X0F.toByte(),
            0X10,
            0X11,
            0X1,
            0X13,
            0X14,
            0X15,
            0X16,
            0X17,
            0X18,
            0X19,
            0X1A.toByte(),
            0X1B.toByte(),
            0X1C.toByte()
            ,
            0X1D.toByte(),
            0X1E.toByte(),
            0X1F.toByte(),
            0X20,
            0X21,
            0X22,
            0X23,
            0X24,
            0X25,
            0X26,
            0X27,
            0X28,
            0X29,
            0X2A.toByte(),
            0X2B.toByte(),
            0X2C.toByte(),
            0X2D.toByte(),
            0X2E.toByte(),
            0X2F.toByte(),
            0X30,
            0X31,
            0X32,
            0X33,
            0X34,
            0X35,
            0X36,
            0X37,
            0X38,
            0X39,
            0X3A.toByte(),
            0X3B.toByte(),
            0X3C.toByte(),
            0X3D.toByte(),
            0X3E.toByte(),
            0X3F.toByte(),
            0X40,
            0X41,
            0X42,
            0X43,
            0X44,
            0X45,
            0X46,
            0X47,
            0X48,
            0X49,
            0X4A.toByte(),
            0X4B.toByte(),
            0X4C.toByte(),
            0X4D.toByte(),
            0X4E.toByte(),
            0X4F.toByte(),
            0X50,
            0X51,
            0X52,
            0X53,
            0X54,
            0X55,
            0X56,
            0X57,
            0X58,
            0X59,
            0X5A.toByte(),
            0X5B.toByte(),
            0X5C.toByte(),
            0X5D.toByte(),
            0X5E.toByte(),
            0X5F.toByte(),
            0X60,
            0X61,
            0X62,
            0X63,
            0X64,
            0XFE.toByte()
        )
    }

    //读取标记
    private var isRead: Boolean = false
    private var isSending = false
    private var startTime: Long = 0
    private var mSocket: BluetoothSocket? = null
    lateinit var mOuts: DataOutputStream
    private val stringBuilder = StringBuilder(78)
    private var listener: Listener? = listener
    /**
     * 配对并连接
     */
    public fun connect(dev: BluetoothDevice?) {
        close()
        try {
            val socket = dev?.createInsecureRfcommSocketToServiceRecord(SPP_UUID)
            Executors.newCachedThreadPool().execute { loopRead(socket) }
        } catch (e: Throwable) {
            close()
            e.printStackTrace()
        }
    }

    //循环读取
    public fun loopRead(socket: BluetoothSocket?) {
        mSocket = socket
        try {
            if (!mSocket!!.isConnected) {
                mSocket!!.connect()
            }
            notifyUi(Listener.CONNECTED, mSocket!!.getRemoteDevice());//连接成功回调
            val ins = DataInputStream(mSocket!!.inputStream)
            mOuts = DataOutputStream(mSocket!!.outputStream)
            isRead = true
            val bytes = ByteArray(1024 * 4)
            var count: Int
            while (isRead) {
                Arrays.fill(bytes, 0)
                Log.i(tag, "开始读。。。")
                while ((ins.read(bytes).also { count = it }) != -1) {
//                    val str = bytes2HexString(bytes, count)
                    val str=String(bytes,0,count)
                    notifyUi(Listener.RECEIVED_MSG, "接收到数据：$str");
                    break

//                    Log.i(tag, "读取内容：$str")
//                    if (str!!.indexOf("B9") != -1) {
//                        sendCmd(COM_READ_SN2)
//                    }
                }
            }
        } catch (e: Throwable) {
            Log.i(tag, "读取错误:${e.message}")
            close()
        }
    }

    /**
     * 与指定设备是否已连接
     */
    public fun isConnected(dev: BluetoothDevice?): Boolean {
        val connect = mSocket != null && mSocket!!.isConnected
        if (dev == null) {
            return connect
        }
        //mSocket!!.remoteDevice获取当前socket所属的BluetoothDevice
        return connect && mSocket!!.remoteDevice.equals(dev)
    }

    /**
     * 发送命令
     */
    open fun sendCmd(cmd: ByteArray?) {
        if (checkSend()) {
            return
        }
        isSending = true
        try {
            Log.i(tag, "发送命令:" + bytes2HexString(cmd!!, cmd.size))
            notifyUi(Listener.SEND_MSG, "发送命令：${bytes2HexString(cmd!!, cmd.size)}")
            startTime = System.currentTimeMillis()
            mOuts.write(cmd)
            mOuts.flush()

        } catch (e: Throwable) {
            close()
        }
        isSending = false
    }

    /**
     * 发送短消息
     */
    fun sendMsg(msg: String) {
        if (checkSend()) {
            return
        }
        isSending = true
        try {

            notifyUi(Listener.SEND_MSG, "发送消息：$msg")
            mOuts.writeInt(FLAG_MSG)//消息标记
            mOuts.writeUTF(msg)
            mOuts.flush()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        isSending = false
    }

    /**发送文件
     * @param path 文件路径
     */
    fun sendFile(fliePath: String) {
        if (checkSend()) {
            return
        }
        isSending=true
        Executors.newCachedThreadPool().execute {
            try {
                val ins = FileInputStream(fliePath)
                val file = File(fliePath)
                mOuts.writeInt(FLAG_FILE)//文件标记
                mOuts.writeUTF(file.name)//文件名
                mOuts.writeLong(file.length())//文件长度
                var r: Int
                val b = ByteArray(1024 * 4)
                val startTime = System.currentTimeMillis()
                notifyUi(Listener.SEND_MSG, "正在发送文件($fliePath)请稍后。。。")
                while ((ins.read(b).also { r = it }) != -1) {
                    mOuts.write(b, 0, r)
                }
                mOuts.flush()
                val endTime = System.currentTimeMillis()
                notifyUi(
                    Listener.SEND_MSG,
                    String.format(
                        Locale.getDefault(),
                        "用时：${(endTime - startTime) / 1000}s,文件发送完成${file.name}"
                    )
                )

            } catch (e: Throwable) {
                close()
            }
            isSending=false

        }
    }
    fun unListener(){//释放引用，防止内容泄漏
        listener=null
    }
    protected fun close() {
        try {
            isRead = false
            mSocket?.close()
            notifyUi(Listener.DISCONNECTED, null);
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun checkSend(): Boolean {
        if (isSending) {
//            APP.toast("正在发送其它数据,请稍后再发...", 0)
            return true
        }
        return false
    }

    fun notifyUi(status: Int, any: Any?) {
        if (listener != null) {
            listener?.socketNotify(status, any)
        }
    }

    //byte[]转换成hexString字符串,无符号。每个Byte之间无空格分隔。字母小写
    private fun bytes2HexString(b: ByteArray, size: Int): String? {
        var ret = ""
        for (i in 0 until size) {
            var hex = Integer.toHexString(b[i].toInt() and 0xFF)
            if (hex.length == 1) {
                hex = "0$hex"
            }
            ret += hex.toUpperCase() //转换大写
        }
        return ret
    }

    interface Listener {
        fun socketNotify(state: Int, obj: Any?)

        companion object {
            const val DISCONNECTED = 0
            const val CONNECTED = 1
            const val SEND_MSG = 2
            const val RECEIVED_MSG = 3
            const val DATA_ERROR = 4
        }
    }
}