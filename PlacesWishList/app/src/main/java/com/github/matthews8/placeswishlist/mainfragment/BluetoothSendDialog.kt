package com.github.matthews8.placeswishlist.mainfragment

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.github.matthews8.placeswishlist.MainActivity
import com.github.matthews8.placeswishlist.MainFragmentDirections
import com.github.matthews8.placeswishlist.R
import kotlinx.coroutines.*
import java.io.IOException
import java.nio.ByteBuffer


class BluetoothSendDialog: DialogFragment(), AdapterView.OnItemClickListener{
    val TAG = "BLUETOOTH_DIALOG_S"
    val btAdapter = BluetoothAdapter.getDefaultAdapter()
    val UUID = "df34b4da-2254-4983-8250-4e97453b4aa8"

    val btDevices = ArrayList<BluetoothDevice>()
    private lateinit var deviceListAdapter: DeviceListAdapter
    private lateinit var listView: ListView
    private lateinit var connectThread: ConnectThread
    private lateinit var connectedThread: ConnectedThread

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bt_send_dialog, container, false)

        listView = view.findViewById<ListView>(R.id.list_view)
        listView.setOnItemClickListener(this)
        val btDiscoverButton: Button = view.findViewById(R.id.bt_discover_button)

        actionDiscover()
        btDiscoverButton.setOnClickListener{
            actionDiscover()
        }

        val intentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        requireActivity().registerReceiver(enableReceiver, intentFilter)

        return view
    }

    override fun onDestroy() {


        requireActivity().unregisterReceiver(enableReceiver)
        super.onDestroy()
    }

    @SuppressLint("MissingPermission")
    fun actionDiscover(){
        Log.i(TAG, "actionDiscover: isDiscovering ${btAdapter.isDiscovering}")
        if(btAdapter.isDiscovering){
            btAdapter.cancelDiscovery()
        }

        Log.i(TAG, "actionDiscover: startDiscovery")
        val discovering = btAdapter.startDiscovery()

        Log.i(TAG, "actionDiscover: discovering ${discovering}")


        val intentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        requireActivity().registerReceiver(btDiscover, intentFilter)
    }

    private val btDiscover = object: BroadcastReceiver(){
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            Log.i(TAG, "ACTION FOUND: before if")
            if(action.equals((BluetoothDevice.ACTION_FOUND))){
                val device: BluetoothDevice? =
                    intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                Log.i(TAG, "ACTION FOUND:  device is ${device?.name}")
                device?.let {
                    btDevices.add(it)
                    deviceListAdapter =
                        DeviceListAdapter(requireContext(), R.layout.device_adapter_view, btDevices)
                    listView.adapter = deviceListAdapter
                    Log.i(TAG, "ACTION FOUND: list adapter called ")
                }
            }
        }
    }

    private val enableReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent?.action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                val mode =
                    intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                when(mode) {
                    BluetoothAdapter.STATE_ON -> {
                        Log.i(TAG, "state: BLUETOOTH STATE ON")
                    }

                    BluetoothAdapter.STATE_TURNING_ON -> {
                        Log.i(TAG, "state: BLUETOOTH STATE TURNING ON")
                    }

                    BluetoothAdapter.STATE_TURNING_OFF -> {
                        Log.i(TAG, "state: BLUETOOTH STATE TURNING OFF")
                    }

                    BluetoothAdapter.STATE_OFF -> {
                        Log.i(TAG, "state: BLUETOOTH STATE OFF")
                    }

                    else -> {
                        Log.e(TAG, "BLUETOOTH STATE: $mode " )

                    }
                }
            }
        }
    }



    @SuppressLint("MissingPermission")
    private inner class ConnectThread(val device: BluetoothDevice) : Thread() {


        var bt_socket: BluetoothSocket? = null
        override fun run() {
            bt_socket =
                device.createRfcommSocketToServiceRecord(java.util.UUID.fromString(UUID))
            try {
                bt_socket?.let{
                    it.connect()
                    connectedThread = ConnectedThread(it)
                    runBlocking {
                        runThread(connectedThread)
                    }
                }
            }catch(e: IOException){
                cancel()
                Log.e(TAG, "connect: $e ")
            }
        }

        fun cancel(){
            try {
                bt_socket?.close()
            } catch (e: IOException) {
                Toast.makeText(requireContext(),
                    "Something went wrong during connection", Toast.LENGTH_LONG)
                    .show()
                Log.e(TAG, "cancel: $e ")
            }

        }
    }
//        private val serverSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
//            btAdapter?.listenUsingInsecureRfcommWithServiceRecord(
//                getString(R.string.app_name),
//                java.util.UUID.fromString(UUID)
//            )
//        }
//
//        override fun run() {
//            // Keep listening until exception occurs or a socket is returned.
//            var shouldLoop = true
//            while (shouldLoop) {
//                val socket: BluetoothSocket? = try {
//                    serverSocket?.accept()
//                } catch (e: IOException) {
//                    Log.e(TAG, "Socket's accept() method failed", e)
//                    shouldLoop = false
//                    null
//                }
//                socket?.also {
//                    ConnectedThread(it).run()
//                    serverSocket?.close()
//                    shouldLoop = false
//                }
//            }
//        }
//
//        // Closes the connect socket and causes the thread to finish.
//        fun cancel() {
//            try {
//                serverSocket?.close()
//            } catch (e: IOException) {
//                Log.e(TAG, "Could not close the connect socket", e)
//            }
//        }
//    }


    private inner class ConnectedThread(private val socket: BluetoothSocket): Thread(){
        val oStream = socket.outputStream
        //TODO DA FINIRE
        //val viewModel.listToSend

        val listToSend: String = "Lorem ipsum ipsum solem"

        override fun run() {
            //lettura dimensione da ricevere
            var list = listToSend.toByteArray()
            val dim = list.size

            try {
                oStream.write(dim)
            } catch(e: IOException){
                Log.d(TAG, "run: stream disconnected")
                cancel()
            }
            try {
                oStream.write(list)
            } catch(e: IOException){
                Log.d(TAG, "run: stream disconnected")
                cancel()
            }
            runBlocking(Dispatchers.Main)
            {Toast.makeText(requireContext(), "INVIO EFFETTUATO", Toast.LENGTH_LONG).show()}
            dismiss()
        }

        fun cancel(){
            try{
                socket.close()
            }catch(e: IOException) {
                Log.d(TAG, "run: stream disconnected")
            }
        }

    }


    class DeviceListAdapter(
        context: Context,
        resource: Int,
        private val devices: ArrayList<BluetoothDevice>
    ) :
        ArrayAdapter<BluetoothDevice?>(context, resource, devices as List<BluetoothDevice?>) {
        private val mLayoutInflater: LayoutInflater
        private val mViewResourceId: Int
        @SuppressLint("MissingPermission")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val viewInf = this.mLayoutInflater.inflate(mViewResourceId, null)
            val device = devices[position]
            val deviceName = viewInf.findViewById<View>(R.id.device_name) as TextView
            val deviceAddress = viewInf.findViewById<View>(R.id.device_address) as TextView
            deviceName.text = device.name
            deviceAddress.text = device.address

            return viewInf
        }

        init {
            mLayoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            mViewResourceId = resource
        }
    }

    @SuppressLint("MissingPermission")
    override fun onItemClick(av: AdapterView<*>?, view: View?, i: Int, l: Long) {
        btAdapter.cancelDiscovery()
        val deviceAddress = btDevices.get(i)
        connectThread = ConnectThread(deviceAddress)

        runBlocking {
            launch {
                runThread(connectThread)
            }
        }

    }

    private suspend fun runThread(t: Thread){
        withContext(Dispatchers.IO){
            t.start()
        }
    }
}