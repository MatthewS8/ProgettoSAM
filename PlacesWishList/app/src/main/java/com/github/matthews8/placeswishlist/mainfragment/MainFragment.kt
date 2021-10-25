package com.github.matthews8.placeswishlist

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.view.ActionMode
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.*
import androidx.recyclerview.widget.RecyclerView
import com.github.matthews8.placeswishlist.database.FavPlacesDatabase
import com.github.matthews8.placeswishlist.databinding.FragmentMainBinding
import com.github.matthews8.placeswishlist.mainfragment.*

class MainFragment : Fragment() {

    /* TODO
     *  #1 La notifica di refresh della lista avviene tramite l'observer
     *  di citiesList. Quindi se devo rimuovere qualcosa va tolto da city list
     *  e questo trigghera il cambiamento oppure alla rimozione del database
     *  essendo una LiveData potrebbe essere aggiornato automaticamente
     *  ------------------------------------------------------------------------
     *  #2 quando aggiorni l'elemento della recycler view bisogna ricordarsi
     *  di azzerare i cambiamenti eg se l'elemento che esce ha l'omino verde
     *  sllora quello che lo sostituisce avra l'omino verde e non [ detto che sia giusto
     *  ------------------------------------------------------------------------
     *  BLUETOOTH
     *  -verificare che il bluetooth sia presente sul dispositivo e chiedere i permessi
     *      all-utilizzo CHECK
     *  -Richiedere l'attivazione del bluetooth CHECK
     *  -Se ho pigiato il tasto bluetooth_receive rendere discoverable il dispositivo e
     *   aspettare che mi arrivi la richiesta di accoppiamento
     *  -Se ho pigiato, invece, su share allora avviare la ricerca del-altro dispositivo
     *  -Una volta che l-accoppiamento [ avvenuto inviare/riceve i dati
     *  -Salvare su database i file ricevuti mosytando prima il color-picker oppure
     *   mostrare messaggio di "inviato con successo"
     *  ------------------------------------------------------------------------
     *  inserire stringhe delle toast nel string.xml
     */

    private lateinit var viewModel: MainFragmentViewModel
    var bluetoothAdapter: BluetoothAdapter? = null
    var isPresent = false

    // mi serve per sapere se voglio ricevere (0) o inviare (1)
    var mBluetoothAction: Int? = null

    val TAG = "OptionsMenuDebug"


    private var actionMode: ActionMode? = null
    private lateinit var selectionTracker: SelectionTracker<Long> //todo qui si perdono i dati
    private lateinit var selectionObserver: CitySelectionObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_fragment_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        Log.i(TAG, "onPrepareOptionsMenu: isPresent is $isPresent")
        super.onPrepareOptionsMenu(menu)
        val bluetoothItem = menu.findItem(R.id.action_bluetooth_receive)
        bluetoothItem.isVisible = isPresent
    }

    private fun updateBluetoothItem(){
        Log.i(TAG, "updateBluetoothItem: isPresent is $isPresent")
        requireActivity().invalidateOptionsMenu()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_bluetooth_receive -> {
                mBluetoothAction = 0
                if(checkPermissions()) {
                    Log.i(
                        TAG,
                        "onOptionsItemSelected: permissionGranted: true calling discoverable"
                    )
                    requestDiscoverable()
                } else {
                    Log.i(
                        TAG,
                        "onOptionsItemSelected: permissionGranted: false making a toast"
                    )
                    Toast.makeText(
                        context,
                        getString(R.string.bluetooth_request_denied),
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
                true
            }
            R.id.oB_alpha -> {
                viewModel.orderByName()
                true
            }
            R.id.oB_timeAdded -> {
                viewModel.orderById()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkPermissions(): Boolean{
        val permissions = mutableListOf<String>()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            permissions.addAll(
                listOf(
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            )
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        for(permission in permissions) {
            if(ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsResultCallback.launch(permissions.toTypedArray())
                return false
            }
        }
        return true
    }

    private val permissionsResultCallback = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val granted = permissions.entries.all {
            Log.i(TAG, "verify each permission: ${it.key} -> ${it.value} ")
            it.value == true
        }
        if(granted){
            Log.i(TAG, "permissionCallback: granted calling discoverable ")
            if(mBluetoothAction == 0){
                //voglio ricevere
                requestDiscoverable()
            } else {
                //voglio inviare
                TODO("Not yet implemented")
            }
        }
        Log.i(TAG, "returning from callback  ")
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<FragmentMainBinding>(
            inflater, R.layout.fragment_main, container, false)

        //viewModel
        val application = requireNotNull(this.activity).application
        val dataSource = FavPlacesDatabase.getInstance(application).favPlacesDatabaseDao
        val viewModelFactory = MainFragmentViewModelFactory(dataSource, application)
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(MainFragmentViewModel::class.java)
        binding.mainFragmentViewModel = viewModel


        binding.addButton.setOnClickListener {
            it.findNavController().navigate(MainFragmentDirections.actionMainFragmentToMapsFragment(true))
            Toast.makeText(
                this.context,
                "Button clicked",
                Toast.LENGTH_SHORT
            )
                .show()
        }

        binding.setLifecycleOwner(this)

        val adapter = CityListAdapter(CityListener(
            cityClickListener = { cityId ->
                viewModel.onCityClicked(cityId) },
            iconClickListener =   { cityId ->
                viewModel.onIconClicked(cityId) }
        )
        )

        viewModel.orderBy.observe(viewLifecycleOwner, {
            it?.let {
                viewModel.citiesList.observe(viewLifecycleOwner, {
                    it?.let {
                        adapter.submitList(it)
                    }
                })
            }
        })

        binding.citiesList.adapter = adapter
        var rv = binding.citiesList
        selectionTracker = SelectionTracker.Builder<Long>(
            MainFragment::class.java.name,
            rv,
            CityItemKeyProvider(rv),
            CityDetailsLookup(rv),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

        adapter.tracker = selectionTracker

        selectionObserver = CitySelectionObserver(selectionTracker) { count ->
            onSelectionChanged(count)
        }
        selectionTracker.addObserver(selectionObserver)

        viewModel.citiesList.observe(viewLifecycleOwner, Observer {
            for(e in it)
                Log.i(TAG, "onCreateView: pre submitList -- la lista è: $e")
            it?.let {
                Log.i(TAG, "onCreateView: called submitList")
                adapter.submitList(it)
            }
        })

        viewModel.navigateToPlaceList.observe(viewLifecycleOwner, Observer {
            it?.let{ cityId ->
                this.findNavController().navigate(MainFragmentDirections.actionMainFragmentToPlaceFragment(cityId))
                viewModel.onPlaceListNavigated()
            }
        })

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(bluetoothAdapter == null){
            isPresent = false
            Toast.makeText(context, "NO BLUETOOTH FOUND", Toast.LENGTH_LONG).show()
        } else{
            isPresent = true
        }
        updateBluetoothItem()
    }

    private fun enableBt(){
        //TODO ---- se devo attivare la discoverability questo passaggio non serve
        // ie posso usare questa funzione per avviare la ricerca dei dispostivi
        //  mentre per ricevere chiamo direttamente requestDiscoverable()
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        Log.i(TAG, "enableBt: launching callback ENABLE")
        enableResultCallback.launch(intent)
    }

    private val enableResultCallback = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback {
            if(it.resultCode != Activity.RESULT_OK){
                Toast.makeText(requireContext(), getString(R.string.bluetooth_enable_fail), Toast.LENGTH_LONG).show()
            }
        }
    )

    private fun requestDiscoverable() {
        Log.i(TAG, "requestDiscoverable: called now")
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300) //5 min
        discoverableCallback.launch(intent)
        //TODO devo registrare un broadcast receiver per sapere quando il dispositivo non è piu discoverable
        //val discoverabilityIntent = IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
        //requireActivity().registerReceiver(discoverabilityReceiver, discoverabilityIntent)
        // e poi bisogna trovare il momento migliore per fare
        //unregister receiver
    }

    private val discoverableCallback = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback {
            Log.i(TAG, "Discoverable Callback: code is ${it.resultCode} ")

            if(it.resultCode != Activity.RESULT_CANCELED){
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToBluetoothReceiveDialog())
                //startBtServer()
            }
            Toast.makeText(requireContext(), "SIAMO NELL discoverable result is ${it.resultCode}", Toast.LENGTH_SHORT).show()
        }
    )

    fun onSelectionChanged(selectedCount: Int){
        if(selectedCount == 0){
            actionMode?.let{
                it.finish()
                actionMode = null
            }
        } else{
            (requireActivity() as? AppCompatActivity)?.let { activity ->
                if(actionMode == null){
                    actionMode = activity.startSupportActionMode(CityActionModeCallback())
                }
            }
            actionMode?.title = "Selected ${selectedCount}"
        }
    }
    private class CitySelectionObserver(
        private val selectionTracker: SelectionTracker<Long>,
        private val onSelectionChangedListener: (Int) -> Unit
    ) : SelectionTracker.SelectionObserver<Long>() {

        override fun onSelectionChanged() {
            super.onSelectionChanged()
            onSelectionChangedListener(selectionTracker.selection?.size() ?: 0)
        }
    }
    
    private inner class CityActionModeCallback: ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            Log.i(TAG, "onCreateActionMode: infalting menu")
            val menuInflater = requireActivity().menuInflater
            menuInflater.inflate(R.menu.main_action_mode_menu, menu)
            return true
            
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            Log.i(TAG, "onPrepareActionMode: return false")
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem) =
            when(item.itemId) {
                R.id.action_remove -> {
                    val itemsToRemove = selectionTracker.selection.toList().also {
                        selectionTracker.clearSelection()
                    }
                    viewModel.deleteSelectedCities(itemsToRemove)
                    true
                }
                else -> false
            }

        override fun onDestroyActionMode(mode: ActionMode?) {
            Log.i(TAG, "onDestroyActionMode: destroy action mode")
            selectionTracker.clearSelection()
        }
    }

    private class CityItemKeyProvider(private val rv: RecyclerView):
        ItemKeyProvider<Long>(SCOPE_MAPPED){
        override fun getKey(position: Int): Long? {
            return rv.adapter?.getItemId(position)
        }

        override fun getPosition(key: Long): Int {
            val viewHolder = rv.findViewHolderForItemId(key)
            return viewHolder?.layoutPosition ?: RecyclerView.NO_POSITION
        }
    }


}