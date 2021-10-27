package com.github.matthews8.placeswishlist.mainfragment

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.ColorInt
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.github.matthews8.placeswishlist.R
import com.github.matthews8.placeswishlist.database.FavPlacesDatabase
import com.github.matthews8.placeswishlist.database.User
import com.github.matthews8.placeswishlist.database.relations.CityWithPlaces
import com.github.matthews8.placeswishlist.utils.myColors
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList

class BluetoothReceiveDialog : DialogFragment(),  AdapterView.OnItemClickListener{
    val TAG = "BLUETOOTH_DIALOG"
    var btAdapter: BluetoothAdapter? = null
    val UUID = "df34b4da-2254-4983-8250-4e97453b4aa8"

    private lateinit var viewModel: MainFragmentViewModel
    var listReceived: Array<CityWithPlaces>? = null

    lateinit var waitingProgressBar: ProgressBar
    lateinit var titleTv: TextView
    lateinit var receivingTv: TextView
    lateinit var nameET: EditText
    lateinit var button: Button
    lateinit var user: User
    lateinit var colorPickerListView: ListView
    lateinit var colorListAdapter: ColorPickerListAdapter

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

        colorPickerListView = view.findViewById(R.id.colorPickerLV)
        colorPickerListView.setOnItemClickListener(this)


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
        button.apply {
            text = "Confirm"
            visibility = View.VISIBLE
            setOnClickListener {
                user = User(nameET.text.toString())
                Log.i(TAG, "buttonClicked user is ${user.username}")
                onColorPicking()
            }


        }
        receivingTv.apply {
            visibility = View.VISIBLE
            text = context.getString(R.string.tv_username)
        }
        waitingProgressBar.visibility = View.GONE
    }

    private fun onColorPicking() {
        titleTv.apply {
            text = "Pick a color for ${user.username}"
            visibility = View.VISIBLE
            setTextColor(Color.BLACK)
        }

        colorListAdapter = ColorPickerListAdapter(
            requireContext(), R.layout.color_list_item, myColors.colorArray
        )
        colorPickerListView.apply {
            adapter = colorListAdapter
            visibility = View.VISIBLE
        }

        button.visibility = View.GONE
        nameET.visibility = View.GONE
        receivingTv.visibility = View.GONE
        waitingProgressBar.visibility = View.GONE


    }

    private fun onStateCompleted() {
        titleTv.apply {
            text = "Done"
            setTextColor(Color.GREEN)
        }
        button.apply {
            text = getString(R.string.close)
            setOnClickListener {
                dismiss()
            }
            visibility = View.VISIBLE
        }
        
        colorPickerListView.visibility = View.GONE
        nameET.visibility = View.GONE
        receivingTv.visibility = View.GONE
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

                    Log.i(TAG, "handleConnection: read: $read, strRead $strRead")

                    strRead += if(read == byteSize){
                        String(inputBuffer)
                    } else {
                        val buff: ByteArray = inputBuffer.copyOfRange(0, read)
                        (String(buff))
                    }

                    Log.i(TAG, "handleConnection: after strRead $strRead")

                    inputBuffer = ByteArray(byteSize)
                }

            } while(read > 0)

            Log.i(TAG, "DOPO IL WHILE:  $strRead")

            val gson = Gson()
            listReceived = gson.fromJson(strRead, Array<CityWithPlaces>::class.java)
            listReceived?.let {
                Log.i(TAG, "listReceived is: ${it.size}")
            }

            runBlocking(Dispatchers.Main) {
                onListReceived()
            }


            socket?.close()
        }

    }


    class ColorPickerListAdapter(
        context: Context,
        resource: Int,
        private val colors: ArrayList<Float>
    ): ArrayAdapter<Float> (context, resource, colors as List<Float>) {

        private val layoutInflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        private val mViewResourceId: Int = resource

        private fun Drawable.overrideColor(@ColorInt colorInt: Int) {
            when (this) {
                is GradientDrawable -> setColor(colorInt)
                is ShapeDrawable -> paint.color = colorInt
                is ColorDrawable -> color = colorInt
            }
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val viewInf = this.layoutInflater.inflate(mViewResourceId, null)
            val colorCircle = viewInf.findViewById<View>(R.id.color_Circle) as ImageView
            val colorTv = viewInf.findViewById<View>(R.id.color_tv) as TextView
            colorCircle.drawable.overrideColor(myColors.markerColor(colors[position]))
            colorTv.text = myColors.colorMarker(colors[position])
            return viewInf
        }


    }

    override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        user.color_picked = myColors.colorArray[p2]
        GlobalScope.launch {
            Log.i(TAG, "onItemClick: launching coroutine")
            viewModel.saveUserPlaces(listReceived, user)
        }

        onStateCompleted()
    }

}


