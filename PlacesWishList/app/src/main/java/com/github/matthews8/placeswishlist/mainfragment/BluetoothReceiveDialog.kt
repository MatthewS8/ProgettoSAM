package com.github.matthews8.placeswishlist.mainfragment

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
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
import okhttp3.internal.closeQuietly
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Integer.min
import java.lang.StringBuilder
import java.nio.ByteBuffer

//TODO BUG QUANDO SCADE IL DISCOVERABLE TIME L_APP CRASHA
class BluetoothReceiveDialog : DialogFragment() {
    val TAG = "BLUETOOTH_DIALOG"
    var btAdapter: BluetoothAdapter? = null
    val UUID = "df34b4da-2254-4983-8250-4e97453b4aa8"

    private lateinit var viewModel: MainFragmentViewModel

    lateinit var waitingProgressBar: ProgressBar
    lateinit var titleTv: TextView
    lateinit var receivingTv: TextView
    lateinit var nameET: EditText
    lateinit var button: Button
    lateinit var user: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bt_receive_dialog, container, false)
        val application = requireNotNull(this.activity).application
        val dataSource = FavPlacesDatabase.getInstance(application).favPlacesDatabaseDao
        val viewModelFactory = MainFragmentViewModelFactory(dataSource, application)
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(MainFragmentViewModel::class.java)

        waitingProgressBar = view.findViewById(R.id.waitingBar)
        titleTv = view.findViewById(R.id.titleTv)
        nameET = view.findViewById(R.id.nameET)
        receivingTv = view.findViewById(R.id.receivingTV)
        button = view.findViewById(R.id.edit_confirm_bt)


        btAdapter = BluetoothAdapter.getDefaultAdapter()
        if(btAdapter == null) {
            Log.i(TAG, "onCreateView: adapter is null")
        } else {

            if(btAdapter!!.isEnabled()) Log.i(TAG, "onCreateView: bt enabled")

            val intentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            requireActivity().registerReceiver(discoverabilityReceiver, intentFilter)


            val intentFilter2 = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            requireActivity().registerReceiver(enableReceiver, intentFilter2)

            val clientThread = ServerThread()
            GlobalScope.launch(Dispatchers.IO) {
                Log.i(TAG, "onCreateView: start server")
                clientThread.start()
            }

        }
        return view
    }

    override fun onDestroy() {

        requireActivity().unregisterReceiver(discoverabilityReceiver)
        requireActivity().unregisterReceiver(enableReceiver)
        super.onDestroy()
    }


    private val enableReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent?.action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                val mode =
                    intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                when(mode) {
                    BluetoothAdapter.STATE_ON -> {
                        Log.i(TAG, "state: BLUETOOTH STATE ON")
                        val inte = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                        inte.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300) //5 min

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
                        Log.e(TAG, "BLUETOOTH STATE: $mode ")

                    }
                }
            }
        }
    }


    private val discoverabilityReceiver = object : BroadcastReceiver() {
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
                        onStateConnected()

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
    private fun onStateConnected() {
        waitingProgressBar.visibility = View.GONE
        titleTv.text = getString(R.string.connection_estrablished)
        receivingTv.apply {
            text = getString(R.string.receiving)
            visibility = View.VISIBLE
        }
        button.visibility = View.GONE

    }

    private fun onListReceived() {
        onStateConnected()
        titleTv.text = getString(R.string.completed_bt)
        nameET.visibility = View.VISIBLE
        button.visibility = View.VISIBLE
        button.setOnClickListener {
            user = nameET.text.toString()
            Log.i(TAG, "buttonClicked user is $user")
            //TODO chiamare il viewModEL e inserire la lista
            dismiss()
        }
        receivingTv.apply {
            visibility = View.VISIBLE
            text = context.getString(R.string.tv_username)
        }
        waitingProgressBar.visibility = View.GONE
    }

    private fun onErrorState(msg: String) {
        titleTv.apply {
            text = "ERROR: $msg"
            visibility = View.VISIBLE
            setTextColor(Color.RED)
        }
        button.apply {
            visibility = View.VISIBLE
            text = context.getString(R.string.close)
            setOnClickListener {
                dismiss()
            }
        }

        nameET.visibility = View.GONE
        receivingTv.visibility = View.GONE
        waitingProgressBar.visibility = View.GONE
    }

    @SuppressLint("MissingPermission")
    private inner class ServerThread() : Thread() {

        private val serverSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            btAdapter?.listenUsingInsecureRfcommWithServiceRecord(
                getString(R.string.app_name),
                java.util.UUID.fromString(UUID)
            )
        }
        private var socket: BluetoothSocket? = null

        override fun run() {
            super.run()
            accept()
            if(socket != null) {
                handleConnection()
            } else {
                runBlocking(Dispatchers.Main) {
                    onErrorState("Failed to connect")
                }
            }
        }

        private fun accept() {
            // Keep listening until exception occurs or a socket is returned.
            var shouldLoop = true
            while(shouldLoop) {

                socket = try {
                    Log.i(TAG, "AcceptThread: accept ")
                    serverSocket?.accept()
                } catch(e: IOException) {
                    Log.e(TAG, "Socket's accept() method failed", e)
                    shouldLoop = false
                    runBlocking(Dispatchers.Main) {
                        onErrorState("Failed to connect")
                    }
                    null
                }

                Log.i(TAG, "AcceptThread: accepted ")

                socket?.also {
                    serverSocket?.close()
                    shouldLoop = false
                }
            }

            Log.i(TAG, "accept: finished -- socket is null? ${socket == null}")

        }

        fun handleConnection() {
            val inputStream: InputStream? = socket?.inputStream
            runBlocking(Dispatchers.Main) {
                onStateConnected()
            }
/*            lettura dimensione da ricevere
            Log.i(TAG, "ConnectedThread: inizio lettura dim ")
            val byteSizeRc = ByteArray(512)
            val PIPPO = inputStream?.read(byteSizeRc)
            Log.i(TAG, "ConnectedThread: pippo is $PIPPO ")
//            var byteDim = String(byteSizeRc).toInt()
            var byteDim = ByteBuffer.wrap(byteSizeRc).int
            Log.i(TAG, "dim: $byteDim")

            var byteDim = 4*/
            val byteSize = 1024
            var read: Int
            var inputBuffer = ByteArray(byteSize)
            var strRead = ""
            do {
                Log.i(TAG, "handleConnection: while")
                read = try {
                    inputStream?.read(inputBuffer) ?: -1
                } catch(e: IOException) {
                    Log.d(TAG, "run: input stream disconnected")
                    runBlocking(Dispatchers.Main) {
                        onErrorState("Connection interrupted")
                    }
                    -1
                }

                if(read > -1) {
/*                    if(byteDim == 4){
                        //first read
                        val dimension = String(inputBuffer)
                        if(dimension.contains('|')) {
                            byteDim = (dimension.substringBefore('|')).toInt()
                            strRead += dimension.substringAfter('|')
                            byteDim -= strRead.toByteArray().size
                            off = 0
                        } else {
                            off += 4
                        }
                    } else {
                        byteDim -= min(byteDim, byteSize)
                    }*/
                    Log.i(TAG, "handleConnection: read: $read, strRead $strRead")

                    if(read == byteSize){
                        strRead += String(inputBuffer)
                    } else {
                        val buff: ByteArray = inputBuffer.copyOfRange(0, read)
                        strRead += (String(buff))
                    }

                    Log.i(TAG, "handleConnection: AFter strRead $strRead")

                    inputBuffer = ByteArray(byteSize)
                }

            } while(read > 0)

            Log.i(TAG, "DOPO IL WHILE:  $strRead")
//            strRead += (String(inputBuffer).substringBeforeLast(']') + ']')

            val gson = Gson()
            val listReceived = gson.fromJson(strRead, Array<CityWithPlaces>::class.java)
            listReceived?.let {
                Log.i(TAG, "listReceived is: ${it.size}")
            }

            runBlocking(Dispatchers.Main) {
                onListReceived()
            }

/*            if(listReceived == null){
                Log.i(TAG, "handleConnection: List is null")
            }
            val lorem = "Ciao Elena ti amo troppo e queto [ un messaggio per vedere se questo cazzo di coso funziona ma a me pare che non stia funzionando affatto e non sso piu cosa scrivere in questo messaggio di prova potrei semplicemente ridurre la dimesnsione del bytearrau"
            Log.i(TAG, "ASSERT: ${lorem?.equals(strRead)}")
            Log.i(TAG, "I HAVE READ ${strRead}")
            Log.i(TAG, "I HAVE READ ${listReceived[0].city.name}")*/

            socket?.close()
//            sleep(2000)
        }

    }


/*    private inner class ConnectedThread(private val socket: BluetoothSocket): Thread(){
        val inputStream: InputStream = socket.inputStream
        //TODO DA FINIRE

        fun handleConnection() {
            runBlocking(Dispatchers.Main) {
                onStateConnected()
            }
            //lettura dimensione da ricevere
            Log.i(TAG, "ConnectedThread: inizio lettura dim ")
            val byteSizeRc = ByteArray(4)
            val pippo = inputStream.read(byteSizeRc, 0, 4)
            Log.i(TAG, "ConnectedThread: pippo is $pippo ")
            val byteSize = ByteBuffer.wrap(byteSizeRc).int
            Log.i(TAG, "dim: $byteSize")

            var byteRead = 0
            var read: Int
            val inputBuffer = ByteArray(1024)
            var i = 0
            while(true){
                Log.i(TAG, "ConnectedThread: in the while True iteration $i ")
                read = try {
                    inputStream.read(inputBuffer)
                } catch(e: IOException){
                    Log.d(TAG, "run: input stream disconnected")
                    break
                }
                Log.i(TAG, "ConnectedThread: letto ")
                i++
                if(read != -1){
                    byteRead += read
                    Log.i(TAG, "ConnectedThread: read $byteRead of $byteSize ")

                } else{
                    Log.i(TAG, "ConnectedThread: read returned -1 ")
                    onListReceived()
                    var str = String(inputBuffer)
                    val gson = Gson()
                    val listReceived = gson.fromJson<List<CityWithPlaces>>(str, CityWithPlaces::class.java)
                    Log.i(TAG, "completed:  ${listReceived.toString()}")
                    runBlocking(Dispatchers.Main) {
                        onListReceived()
                    }
                    break
                }
            }
            cancel()
            dismiss()
        }

        fun cancel(){
            try{
                socket.close()
            }catch(e: IOException) {
                Log.d(TAG, "run: stream disconnected")
            }
        }
    }*/

    private suspend fun runThread(t: Thread) {
        withContext(Dispatchers.IO) {
            t.start()
        }
    }

}