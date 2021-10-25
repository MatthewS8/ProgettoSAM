package com.github.matthews8.placeswishlist.mainfragment

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
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
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.github.matthews8.placeswishlist.MainFragmentDirections
import com.github.matthews8.placeswishlist.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.ByteBuffer

class BluetoothReceiveDialog: DialogFragment(){
    val TAG = "BLUETOOTH_DIALOG"
    var btAdapter :BluetoothAdapter? = null
    val UUID = "df34b4da-2254-4983-8250-4e97453b4aa8"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bt_receive_dialog, container, false)

        val waitingProgressBar: ProgressBar = view.findViewById(R.id.waitingBar)

        btAdapter = BluetoothAdapter.getDefaultAdapter()
        if(btAdapter == null){
            Log.i(TAG, "onCreateView: adapter is null")
        } else {

            if(btAdapter!!.isEnabled()) Log.i(TAG, "onCreateView: bt enabled")

            val intentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            requireActivity().registerReceiver(discoverabilityReceiver, intentFilter)


            val intentFilter2 = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            requireActivity().registerReceiver(enableReceiver, intentFilter2)

//            val acceptThread = AcceptThread()
//            GlobalScope.launch(Dispatchers.IO) {
//                Log.i(TAG, "onCreateView: start server")
//                acceptThread.run()
//            }

        }
        return view
    }

    override fun onDestroy() {


        requireActivity().unregisterReceiver(discoverabilityReceiver)
        super.onDestroy()
    }
    private val discoverableCallback = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback {
            Log.i(TAG, "Discoverable Callback: code is ${it.resultCode} ")

            if(it.resultCode != Activity.RESULT_CANCELED){
//                val intentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
//                requireActivity().registerReceiver(discoverabilityReceiver, intentFilter)
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToBluetoothReceiveDialog())
                //startBtServer()
            }
            Toast.makeText(requireContext(), "SIAMO NELL discoverable result is ${it.resultCode}", Toast.LENGTH_SHORT).show()
        }
    )


    private val enableReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent?.action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                val mode =
                    intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                when(mode) {
                    BluetoothAdapter.STATE_ON -> {
                        Log.i(TAG, "state: BLUETOOTH STATE ON")
                        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 10) //5 min
                        discoverableCallback.launch(intent)

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


    private val discoverabilityReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent?.action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                val mode =
                    intent?.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR)
                when(mode) {
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE -> {
                        Log.i(TAG, "SCAN MODE: connectable")

                    }

                    BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE -> {
                        //connectable and discoverable
                        Log.i(TAG, "SCAN MODE: discoverable")
                    }
                    BluetoothAdapter.SCAN_MODE_NONE -> {
                        //Mot able to receive connection
                        //todo uscire in modo pulito
                        Log.i(TAG, "SCAN MODE: not connectable not discoverable")
                    }
                    BluetoothAdapter.STATE_CONNECTED -> {
                        Log.i(TAG, "SCAN MODE: connected")

                    }
                    else -> {
                        //BluetoothAdapter.STATE_CONNECTING || BluetoothAdapter.STATE_CONNECTED
                        Log.i(TAG, "SCAN MODE: error $mode")

                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private inner class AcceptThread : Thread() {


        private val serverSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            btAdapter?.listenUsingInsecureRfcommWithServiceRecord(
                getString(R.string.app_name),
                java.util.UUID.fromString(UUID)
            )
        }

        override fun run() {
            // Keep listening until exception occurs or a socket is returned.
            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    serverSocket?.accept()
                } catch (e: IOException) {
                    Log.e(TAG, "Socket's accept() method failed", e)
                    shouldLoop = false
                    null
                }
                socket?.also {
                    ConnectedThread(it).run()
                    serverSocket?.close()
                    shouldLoop = false
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        fun cancel() {
            try {
                serverSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }


    private inner class ConnectedThread(private val socket: BluetoothSocket): Thread(){
        val inputStream = socket.inputStream
        //TODO DA FINIRE

        override fun run() {
            TODO("Da finire")
            //lettura dimensione da ricevere
            var byteSizeBf = ByteArray(4)
            inputStream.read(byteSizeBf, 0, 4)
            val byteSize = ByteBuffer.wrap(byteSizeBf).int

            var byteRead = 0
            var read: Int
            val inputBuffer = ByteArray(1024)
            while(true){
                read = try {
                    inputStream.read(inputBuffer)
                } catch(e: IOException){
                    Log.d(TAG, "run: input stream disconnected")
                    break
                }
                if(read != -1){
                    byteRead += read

                } else{
                    break
                }
            }



        }
    }

}