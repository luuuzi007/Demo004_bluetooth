package com.example.bluetooth.adapter

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetooth.R
import java.util.*


/**
 * @author luuuzi
 * @date 2020-06-19.
 * @description
 */
class BtDevAdapter(var mContext: Context, var listener: Listener) :
    RecyclerView.Adapter<BtDevAdapter.VH>() {
    private var mDevices: MutableList<BluetoothDevice?> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflate = LayoutInflater.from(parent.context).inflate(R.layout.item_dev, parent, false)
        return VH(inflate)
    }

    override fun getItemCount(): Int {
        return mDevices.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val device = mDevices[position]
        val name = device?.name
        val address = device?.address
        val bondState = device?.bondState//是否配对
        holder.tvName.text = "name:$name"
        holder.tvAddress.text = "address:" + address + if (bondState == 10) "[未配对[" else "[配对]"
    }

    public fun add(device: BluetoothDevice?) {
        if (!mDevices.contains(device)) {
            mDevices.add(device)
            notifyDataSetChanged()
        }
    }

    /**
     * 重新扫描
     */
    fun renewScan() {
        mDevices.clear()
        val bt = BluetoothAdapter.getDefaultAdapter()
        if (!bt.isDiscovering) {
            bt.startDiscovery()
        } else {
            Toast.makeText(mContext, "正在扫描", Toast.LENGTH_SHORT).show()
        }
        notifyDataSetChanged()
    }

    inner class VH(item: View) : RecyclerView.ViewHolder(item), View.OnClickListener {
        lateinit var tvName: AppCompatTextView
        lateinit var tvAddress: AppCompatTextView

        init {
            tvName = item.findViewById(R.id.tvName)
            tvAddress = item.findViewById(R.id.tvAddress)
            item.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            listener.onItemClick(mDevices[adapterPosition])
        }
    }

    public interface Listener {
        fun onItemClick(dev: BluetoothDevice?)
    }
}