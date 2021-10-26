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
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.github.matthews8.placeswishlist.R
import com.github.matthews8.placeswishlist.database.FavPlacesDatabase
import com.github.matthews8.placeswishlist.database.relations.CityWithPlaces
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.IOException
import java.io.OutputStream
import java.lang.Integer.min
import kotlin.math.max

class BluetoothSendDialog: DialogFragment(), AdapterView.OnItemClickListener{
    val TAG = "BLUETOOTH_DIALOG_S"
    val btAdapter = BluetoothAdapter.getDefaultAdapter()
    val UUID = "df34b4da-2254-4983-8250-4e97453b4aa8"

    private lateinit var viewModel: MainFragmentViewModel

    val btDevices = ArrayList<BluetoothDevice>()
    private lateinit var deviceListAdapter: DeviceListAdapter
    private lateinit var listView: ListView
    private lateinit var button: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var title: TextView


    private lateinit var clientThread: ClientThread

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bt_send_dialog, container, false)
        val application = requireNotNull(this.activity).application
        val dataSource = FavPlacesDatabase.getInstance(application).favPlacesDatabaseDao
        val viewModelFactory = MainFragmentViewModelFactory(dataSource, application)
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(MainFragmentViewModel::class.java)

        button = view.findViewById(R.id.bt_discover_button)
        title = view.findViewById(R.id.titleTvSend)
        progressBar = view.findViewById(R.id.progressBar)

        listView = view.findViewById<ListView>(R.id.list_view)
        listView.setOnItemClickListener(this)


        actionDiscover()
        if(btAdapter.isDiscovering){
            button.setText("Restart")
        }
        button.setOnClickListener{
            actionDiscover()
        }

        val intentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        requireActivity().registerReceiver(enableReceiver, intentFilter)

        return view
    }


    private fun onDeviceSelected(){
        listView.visibility = View.GONE
        title.text = getString(R.string.connecting)
        progressBar.visibility = View.VISIBLE
        button.isEnabled = false

    }

    private fun onSelectionPrepare() {
        button.visibility = View.GONE
        listView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        title.text = getString(R.string.title_send_wait)
    }

    private fun onSelectionReady() {
        button.visibility = View.GONE
        listView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        title.text = getString(R.string.sending_list)
    }

    private fun onErrorState(msg: String) {
        title.apply {
            text = "Error $msg"
            setTextColor(Color.RED)
            visibility = View.VISIBLE
        }
        button.apply {
            text = "Close"
            setOnClickListener{
                dismiss()
            }
            visibility = View.VISIBLE
        }
        listView.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    override fun onDestroy() {

        Log.i(TAG, "onDestroy: dialog")
        if(viewModel.dbCoroutine?.isActive ?: false){
            Log.i(TAG, "onDestroy: isActive")
            viewModel.dbCoroutine?.cancel()
            Log.i(TAG, "onDestroy: cancelled")
        }
        viewModel.selectionToSend = null
        requireActivity().unregisterReceiver(enableReceiver)
        requireActivity().unregisterReceiver(btDiscover)
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
                        onErrorState("Bluetooth OFF")
                    }

                    else -> {
                        Log.e(TAG, "BLUETOOTH STATE: $mode " )

                    }
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    private inner class ClientThread(val device: BluetoothDevice): Thread(){

        var btSocket: BluetoothSocket? = null

        override fun run(){
            super.run()
            connect()
            sendList()
        }

        fun connect() {
            btAdapter.cancelDiscovery()
            btSocket = device.createRfcommSocketToServiceRecord(
                java.util.UUID.fromString(UUID)
            )
            try {
                btSocket?.let{
                    Log.i(TAG, "try connect ")
                    it.connect()
                    Log.i(TAG, "connected")
                }
            }catch(e: IOException){
                try {
                    btSocket?.close()
                }catch(e: IOException){

                }
                Log.e(TAG, "connect: $e ")
            }
        }

        fun sendList() {
            val oStream: OutputStream? = btSocket?.outputStream
            runBlocking(Dispatchers.Main) {
                onSelectionPrepare()
            }
            var selectedPlacesToSend: List<CityWithPlaces>? = null
            selectedPlacesToSend = viewModel.database.getCitiesWithPlaces(viewModel.selectedList!!)
            val gson = Gson()
            val jsonStr = gson.toJson(selectedPlacesToSend)
            Log.i(TAG, "JSON: $jsonStr")
            viewModel.selectionToSend = jsonStr.toByteArray()

            runBlocking(Dispatchers.Main) {
                onSelectionReady()
            }

            /*if(viewModel.selectionToSend == null){
                Log.e(TAG, "sendList: selection is null", )
                runBlocking(Dispatchers.Main) {
                    onSelectionPrepare()
                }
                Log.i(TAG, "sendList: before join")
                runBlocking {viewModel.dbCoroutine!!.join()}
                Log.i(TAG, "sendList: after join")
            }
            Log.i(TAG, "CONNECTEdThread: cancelDiscovery ")
*/
            if(btAdapter.isDiscovering)
                btAdapter.cancelDiscovery()

           /* if(viewModel.dbCoroutine == null)
                Log.i(TAG, "sendList: DB COURUTINE IS NULL")

            if(viewModel.dbCoroutine?.isActive == true) {
                Log.i(TAG, "sendList: VIewModel is active")
                runBlocking(Dispatchers.Main) {
                    onSelectionPrepare()
                }
                Log.i(TAG, "sendList: before join")
                runBlocking {viewModel.dbCoroutine!!.join()}
                Log.i(TAG, "sendList: after join")
                Log.i( TAG,
                    "sendList: viewModel Coroutine is completed ${viewModel.dbCoroutine?.isCompleted}"
                )
            } else {
                Log.i(TAG, "sendList: IN the else before if ${viewModel.dbCoroutine?.isCompleted == false}")

                if(viewModel.dbCoroutine?.isCompleted == false) {
                    Log.i(TAG, "sendList: isNOTcompleted")
                    cancel()
                    runBlocking(Dispatchers.Main) {
                        onErrorState("Failed to load the list")
                    }
                }
                val gson = Gson()
                val str = viewModel.selectionToSend?.let {String(it)}
                Log.i(TAG, "SEND IS: $str ")

                val listReceived = gson.fromJson(str, Array<CityWithPlaces>::class.java)
                listReceived?.let {
                    Log.i(TAG, "listReceived is: ${it.size}")
                }
//                Log.i(TAG, "SEND IS:  ${listReceived[0]}")
            }

            Log.i(TAG, "CONNECTEdThread: viewModel.selectionToSend is ready")
            runBlocking(Dispatchers.Main) {
                onSelectionReady()
            }*/

//            Log.i(TAG, "CONNECTEdThread: dim initializing ")

//            val dim = viewModel.selectionToSend!!.size

/*            var dimens = "$dim|".toByteArray()
            Log.i(TAG, "CONNECTEdThread: dim is $dim")
            try {
//                oStream?.write(dim.toString().toByteArray(), 0, 4)
                oStream?.write(dimens)
            } catch(e: IOException) {
                Log.d(TAG, "run: stream disconnected")
                cancel()
            }

            Log.i(TAG, "CONNECTEdThread: size sent")*/

            //val lorem = "Ciao Elena ti amo troppo e queto [ un messaggio per vedere se questo cazzo di coso funziona ma a me pare che non stia funzionando affatto e non sso piu cosa scrivere in questo messaggio di prova potrei semplicemente ridurre la dimesnsione del bytearrau"
            viewModel.selectionToSend?.let {
                val byte = 1024
                var left = it.size
                var off = 0
                while(left > 0) {
                    try {
                        oStream?.write(viewModel.selectionToSend, off, min(left, byte))
//                oStream?.write(lorem.toByteArray())
                    } catch(e: IOException) {
                        Log.d(TAG, "CONNECTEdThread: stream disconnected")
                        cancel()
                        onErrorState("Failed to send")
                    }

                    off += min(left, byte)
                    left -= min(left, byte)
                }

            }
            runBlocking(Dispatchers.Main) {
                Toast.makeText(requireContext(), "List sent", Toast.LENGTH_LONG).show()
            }

            cancel()
            dismiss()
        }

        fun cancel(){
            try{
                btSocket?.close()
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

        private val layoutInflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        private val mViewResourceId: Int = resource

        @SuppressLint("MissingPermission")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val viewInf = this.layoutInflater.inflate(mViewResourceId, null)
            val device = devices[position]
            val deviceName = viewInf.findViewById<View>(R.id.device_name) as TextView
            val deviceAddress = viewInf.findViewById<View>(R.id.device_address) as TextView
            deviceName.text = device.name
            deviceAddress.text = device.address

            return viewInf
        }


    }

    @SuppressLint("MissingPermission")
    override fun onItemClick(av: AdapterView<*>?, view: View?, i: Int, l: Long) {
        val deviceAddress = btDevices.get(i)
        Log.i(TAG, "onItemClick: ${deviceAddress.toString()}")
        onDeviceSelected()
        clientThread = ClientThread(deviceAddress)

        runBlocking {
            launch {
                runThread(clientThread)
            }
        }

    }

    private suspend fun runThread(t: Thread){
        withContext(Dispatchers.IO){
            t.start()
        }
    }
}