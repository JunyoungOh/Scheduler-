package com.cutener.raising.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothManager @Inject constructor(
    private val context: Context
) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    // Using a common UUID for the service
    private val APP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // SPP UUID
    private val APP_NAME = "PocketArena"

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private var socket: BluetoothSocket? = null

    sealed class ConnectionState {
        object Disconnected : ConnectionState()
        object Connecting : ConnectionState()
        object Connected : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }

    @SuppressLint("MissingPermission") // Permissions are handled in UI/Activity
    suspend fun startServer() {
        if (bluetoothAdapter == null) {
            _connectionState.value = ConnectionState.Error("Bluetooth not supported")
            return
        }

        withContext(Dispatchers.IO) {
            var serverSocket: BluetoothServerSocket? = null
            try {
                _connectionState.value = ConnectionState.Connecting
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, APP_UUID)
                Log.d("BluetoothManager", "Server started, waiting for connection...")

                // Blocking call, waiting for connection
                socket = serverSocket.accept()

                // Close server socket as we only accept one connection
                serverSocket.close()

                if (socket != null) {
                    _connectionState.value = ConnectionState.Connected
                    Log.d("BluetoothManager", "Client connected")
                }
            } catch (e: IOException) {
                Log.e("BluetoothManager", "Socket accept failed", e)
                _connectionState.value = ConnectionState.Error("Failed to start server: ${e.message}")
            }
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun connectToDevice(address: String) {
         if (bluetoothAdapter == null) {
            _connectionState.value = ConnectionState.Error("Bluetooth not supported")
            return
        }

        withContext(Dispatchers.IO) {
            try {
                _connectionState.value = ConnectionState.Connecting
                val device = bluetoothAdapter.getRemoteDevice(address)
                socket = device.createRfcommSocketToServiceRecord(APP_UUID)

                // Cancel discovery as it slows down connection
                bluetoothAdapter.cancelDiscovery()

                socket?.connect()
                _connectionState.value = ConnectionState.Connected
                Log.d("BluetoothManager", "Connected to server")
            } catch (e: IOException) {
                Log.e("BluetoothManager", "Connection failed", e)
                 _connectionState.value = ConnectionState.Error("Failed to connect: ${e.message}")
                try {
                    socket?.close()
                } catch (closeException: IOException) {
                    Log.e("BluetoothManager", "Could not close client socket", closeException)
                }
            }
        }
    }

    fun sendData(data: String) {
        if (socket == null) return
        try {
            socket?.outputStream?.write(data.toByteArray())
        } catch (e: IOException) {
            Log.e("BluetoothManager", "Error sending data", e)
            _connectionState.value = ConnectionState.Error("Error sending data")
        }
    }

    suspend fun receiveData(): String? {
        if (socket == null) return null
        return withContext(Dispatchers.IO) {
            try {
                val buffer = ByteArray(8192) // Increased buffer for battle logs
                val bytes = socket?.inputStream?.read(buffer)
                if (bytes != null && bytes > 0) {
                    String(buffer, 0, bytes)
                } else {
                    null
                }
            } catch (e: IOException) {
                Log.e("BluetoothManager", "Error receiving data", e)
                 _connectionState.value = ConnectionState.Error("Error receiving data")
                null
            }
        }
    }

    fun disconnect() {
        try {
            socket?.close()
            socket = null
            _connectionState.value = ConnectionState.Disconnected
        } catch (e: IOException) {
            Log.e("BluetoothManager", "Error closing socket", e)
        }
    }
}
