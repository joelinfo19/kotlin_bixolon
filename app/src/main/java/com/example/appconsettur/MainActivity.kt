package com.example.appconsettur

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import jpos.POSPrinter
import jpos.POSPrinterConst
import java.util.UUID

const val REQUEST_ENABLE_BT = 1

class MainActivity : AppCompatActivity() {
    lateinit var bluetoothAdapter: BluetoothAdapter
    var mAddressDevices: ArrayAdapter<String>? = null
    var mNameDevices: ArrayAdapter<String>? = null

    var posPrinter:POSPrinter?=null



    companion object {
        var m_myUid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        var m_isConnected: Boolean = false
        lateinit var m_address: String
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        posPrinter = POSPrinter(this)


        mAddressDevices = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        mNameDevices = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        var activeBt = findViewById<Button>(R.id.activeBt)
        var deactivateBt = findViewById<Button>(R.id.deactiveBt)
        var availableBt = findViewById<Button>(R.id.availableBt)
        var connect = findViewById<Button>(R.id.connect)
        var sendText = findViewById<Button>(R.id.sendText)
        var idSpinDisp = findViewById<Spinner>(R.id.spinner)
        var idTextOut = findViewById<EditText>(R.id.editTextText)

        var someActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == REQUEST_ENABLE_BT) {
                Toast.makeText(this, "Bluetooth activado", Toast.LENGTH_SHORT).show()
                // There are no request codes

            }
        }
        bluetoothAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

        if (bluetoothAdapter == null) {
            Toast.makeText(
                this,
                "Bluetooth no es compatible en este dispositivo",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        } else {
            Toast.makeText(this, "Bluetooth es compatible en este dispositivo", Toast.LENGTH_SHORT)
                .show()
        }
        activeBt.setOnClickListener {
            if (bluetoothAdapter.isEnabled) {
                Toast.makeText(this, "Bluetooth ya está activado", Toast.LENGTH_SHORT).show()
            } else {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.i("MainActivity", "ActivityCompat#requestPermissions")
                }
                someActivityResultLauncher.launch(enableBtIntent)
            }
        }
        deactivateBt.setOnClickListener {
            if (bluetoothAdapter.isEnabled) {
                bluetoothAdapter.disable()
                Toast.makeText(this, "Bluetooth desactivado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Bluetooth ya está desactivado", Toast.LENGTH_SHORT).show()
            }
        }
        availableBt.setOnClickListener {
            if (bluetoothAdapter.isEnabled) {
                val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
                mAddressDevices!!.clear()
                mNameDevices!!.clear()
                pairedDevices?.forEach { device ->
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address
                    mAddressDevices!!.add(deviceHardwareAddress)
                    mNameDevices!!.add(deviceName)
                }
                idSpinDisp.setAdapter(mNameDevices)
            } else {
                val noDevices = "No hay dispositivos"
                mAddressDevices!!.add(noDevices)
                mNameDevices!!.add(noDevices)
                Toast.makeText(this, "Bluetooth no está activado", Toast.LENGTH_SHORT).show()
            }
        }
        connect.setOnClickListener {
            try {
                if (m_bluetoothSocket == null || !m_isConnected) {
                    val IntValSpin = idSpinDisp.selectedItemPosition
                    m_address = mAddressDevices!!.getItem(IntValSpin).toString()
                    Toast.makeText(this, "Conectando a $m_address", Toast.LENGTH_SHORT).show()
                    bluetoothAdapter?.cancelDiscovery()
                    val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(m_address)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUid)
                    m_bluetoothSocket!!.connect()
                }
                Toast.makeText(this, "Conectado a $m_address", Toast.LENGTH_SHORT).show()
                Log.i("MainActivity", "Conectado a $m_address")
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error al conectar a $m_address", Toast.LENGTH_SHORT).show()
                Log.i("MainActivity", "Error al conectar a $m_address")
            }
        }
        //"|N|lA|aM|1hC|1vCBixolon Text Print!!
//        posPrinter!!.printNormal(POSPrinterConst.PTR_S_RECEIPT, strOption + data)

//        sendText.setOnClickListener {
////            posPrinter?.printNormal(POSPrinterConst.PTR_S_RECEIPT, "Hello World")
//
//
//                val msg: String = "Bixolon Text Print!!"
//                try {
////                    m_bluetoothSocket!!.outputStream.write(msg.toByteArray())
////                    Toast.makeText(this, "Mensaje enviado", Toast.LENGTH_SHORT).show()
////                    Log.i("MainActivity", "Mensaje enviado")
//                    posPrinter?.printNormal(POSPrinterConst.PTR_S_RECEIPT, "|N|lA|aM|1hC|1vC" + msg)
//
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    Toast.makeText(this, "Error al enviar mensaje", Toast.LENGTH_SHORT).show()
//                    Log.i("MainActivity", "Error al enviar mensaje")
//                }
//            }
//
//
//        }


    }}