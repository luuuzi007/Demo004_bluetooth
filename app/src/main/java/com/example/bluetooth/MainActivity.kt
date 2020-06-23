package com.example.bluetooth

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.bluetooth.bt.BtClientActivity
import com.example.bluetooth.bt.BtServiceActivity

/**
 * @author luuuzi
 * @description 蓝牙demo测试
 */
class MainActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_CODE = 101
        val tag: String = MainActivity::class.java.simpleName
    }

    lateinit var mContext: Context
    private var permissionFlag = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mContext = this
        permissionManager()
    }

    /**
     * 经典蓝牙客户端
     */
    fun btClient(view: View) {
        Log.i(tag, "permissionFlag：$permissionFlag")
        if (permissionFlag) {
            startActivity(Intent(this, BtClientActivity::class.java))
        } else {
            Toast.makeText(this, "没有权限", Toast.LENGTH_SHORT).show()
        }
    }

    fun btService(view: View) {
        if (permissionFlag) {
            startActivity(Intent(this, BtServiceActivity::class.java))
        } else {
            Toast.makeText(this, "没有权限", Toast.LENGTH_SHORT).show()
        }
    }

    fun bleCenter(view: View) {}
    fun blePeriphery(view: View) {}
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(tag, "获取到权限")
                permissionFlag = true
            } else {
                Toast.makeText(this, "没有权限", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 申请权限
     */
    private fun permissionManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val checkCallingOrSelfPermission =
                checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            if (checkCallingOrSelfPermission != PackageManager.PERMISSION_GRANTED) {//申请权限
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE)
            } else {
                Log.i(tag, "有权限")
                permissionFlag = true
            }
        }
    }

}
