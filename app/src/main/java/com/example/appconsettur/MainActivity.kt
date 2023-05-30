package com.example.appconsettur

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import com.bxl.config.editor.BXLConfigLoader
import jpos.POSPrinter
import jpos.POSPrinterConst
import jpos.config.JposEntry
import java.util.UUID

const val REQUEST_ENABLE_BT = 1

class MainActivity : AppCompatActivity() {
    lateinit var bluetoothAdapter: BluetoothAdapter
    var mAddressDevices: ArrayAdapter<String>? = null
    var mNameDevices: ArrayAdapter<String>? = null
    private lateinit var posPrinter: POSPrinter
    var productName = BXLConfigLoader.PRODUCT_NAME_SPP_R200III
    var bxlConfigLoader: BXLConfigLoader? = null
    val ESCAPE_CHARACTERS = String(byteArrayOf(0x1b, 0x7c))


//    var posPrinter:POSPrinter?=null


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


        mAddressDevices = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        mNameDevices = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        var activeBt = findViewById<Button>(R.id.activeBt)
        var deactivateBt = findViewById<Button>(R.id.deactiveBt)
        var availableBt = findViewById<Button>(R.id.availableBt)
        var connect = findViewById<Button>(R.id.connect)
        var sendText = findViewById<Button>(R.id.sendText)
        var idSpinDisp = findViewById<Spinner>(R.id.spinner)
        var idTextOut = findViewById<EditText>(R.id.editTextText)
        var closePrint = findViewById<Button>(R.id.closePrint)
        var errorButton = findViewById<Button>(R.id.errorButton)
        var warningButton = findViewById<Button>(R.id.warningButton)
        var successButton = findViewById<Button>(R.id.successButton)

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
                this, "Bluetooth no es compatible en este dispositivo", Toast.LENGTH_SHORT
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
                        this, Manifest.permission.BLUETOOTH
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
            //NEW FILE IS IMPORTANT
            try{

                productName = BXLConfigLoader.PRODUCT_NAME_SPP_R200III
                bxlConfigLoader = BXLConfigLoader(this)
                bxlConfigLoader!!.openFile()
            }catch (JposException:Exception){
                JposException.printStackTrace()
                bxlConfigLoader?.newFile()

            }

            try {
//                if (m_bluetoothSocket == null || !m_isConnected) {
//                    val IntValSpin = idSpinDisp.selectedItemPosition
//                    m_address = mAddressDevices!!.getItem(IntValSpin).toString()
//                    Toast.makeText(this, "Conectando a $m_address", Toast.LENGTH_SHORT).show()
//                    bluetoothAdapter?.cancelDiscovery()
//                    val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(m_address)
//                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUid)
//                    m_bluetoothSocket!!.connect()
//                }
//                Toast.makeText(this, "Conectado a $m_address", Toast.LENGTH_SHORT).show()
//                Log.i("MainActivity", "Conectado a $m_address")
                for (entry in bxlConfigLoader?.entries!!) {
                    val config = entry as JposEntry
                    val logicalNames = config.logicalName
                    Log.i("MainActivity", logicalNames)

                    bxlConfigLoader!!.removeEntry(logicalNames)
                }

                var logicalName = "SPP-R200III_110362"
                bxlConfigLoader!!.addEntry(
                    logicalName,
                    BXLConfigLoader.DEVICE_CATEGORY_POS_PRINTER,
                    productName,
                    BXLConfigLoader.DEVICE_BUS_BLUETOOTH,
                    "74:F0:7D:A8:85:25"
                )
                bxlConfigLoader!!.saveFile()

                posPrinter = POSPrinter(this)

                posPrinter.open("SPP-R200III_110362")
                posPrinter.claim(5000 * 2)
                posPrinter.deviceEnabled = true
                Toast.makeText(this, "successful connect", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error al conectar a $m_address", Toast.LENGTH_SHORT).show()
                Log.i("MainActivity", "Error al conectar a $m_address")
            }
        }



        fun successSound() {
            val soundPool = SoundPool.Builder().build()
            val soundId = soundPool.load(
                this,
                R.raw.success2,
                1
            )
            soundPool.setOnLoadCompleteListener { _, _, _ ->
                soundPool.play(soundId, 1f, 1f, 1, 0, 2f)
            }
        }
        fun errorSound() {
            val soundPool = SoundPool.Builder().build()
            val soundId = soundPool.load(
                this,
                R.raw.error,
                1
            )
            soundPool.setOnLoadCompleteListener { _, _, _ ->
                soundPool.play(soundId, 1f, 1f, 1, 0, 2f)
            }
        }

        fun warningSound() {
            val soundPool = SoundPool.Builder().build()
            val soundId = soundPool.load(
                this,
                R.raw.warning,
                1
            )
            soundPool.setOnLoadCompleteListener { _, _, _ ->
                soundPool.play(soundId, 1f, 1f, 1, 0, 2f)
            }
        }
        fun printManifest(manifest: String) {
            try {
                posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, manifest)
//                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
//                } else {
//                    vibrator.vibrate(100)
//                }
                successSound()
                Toast.makeText(this, "Mensaje enviado", Toast.LENGTH_SHORT).show()
                Log.i("MainActivity", "Mensaje enviado")
            } catch (e: Exception) {

                e.printStackTrace()
                Toast.makeText(this, "Error al enviar mensaje", Toast.LENGTH_SHORT).show()
                errorSound()
                Log.i("MainActivity", "Error al enviar mensaje")
            }
        }
        closePrint.setOnClickListener {
            try {
                posPrinter.close()
                Toast.makeText(this, "Close Print", Toast.LENGTH_SHORT).show()
                warningSound()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error al cerrar a $m_address", Toast.LENGTH_SHORT).show()
                Log.i("MainActivity", "Error al cerrar a $m_address")
            }
        }
        sendText.setOnClickListener {
            val msg: String = "SMART_TESTS\n"
            //                    posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT,(ESCAPE_CHARACTERS + "uC"+ESCAPE_CHARACTERS + "cA")+ msg)

            val msgTest: String =
                "\u001B\u007CaM\u001B\u007CcA\u001B\u007c!uCTexto de ejemplo\n\u001B|bM\u001B|cA\u001B|!uCEXAMPLE TEST 2\n"
            val pasajerosString = """
    |Nombre          Edad  Destino 
    ----------------------------------------
    |Eduardo Mongomery   20    Machu Picchu
    |Marge Simpson        20    Machu Picchu
    |
"""
            printManifest(msgTest)
        }

        errorButton.setOnClickListener {
            errorSound()
        }
        successButton.setOnClickListener {
            successSound()
        }
        warningButton.setOnClickListener {
            warningSound()
        }





    }


}