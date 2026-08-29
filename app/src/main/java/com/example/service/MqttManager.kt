package com.example.service

import android.content.Context
import android.util.Log
import com.example.data.model.MqttConnectionStatus
import com.example.data.model.MqttDeviceState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject

class MqttManager(private val context: Context) {

    companion object {
        private const val TAG = "LuminaMqttManager"
        const val DEFAULT_BROKER_URL = "tcp://broker.emqx.io:1883"
        const val DEFAULT_TOPIC_COMMAND = "lumina/home/command"
        const val DEFAULT_TOPIC_STATUS = "lumina/home/status"
    }

    private var mqttClient: MqttAsyncClient? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _connectionStatus = MutableStateFlow(MqttConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<MqttConnectionStatus> = _connectionStatus.asStateFlow()

    private val _devices = MutableStateFlow<Map<String, Boolean>>(
        mapOf(
            "Living Room Light" to false,
            "AC Cool 22°C" to true,
            "Desk RGB Strip" to false,
            "Ceiling Fan" to true
        )
    )
    val devices: StateFlow<Map<String, Boolean>> = _devices.asStateFlow()

    private val _lastLog = MutableStateFlow("MQTT Gateway Initialized")
    val lastLog: StateFlow<String> = _lastLog.asStateFlow()

    fun connect(brokerUrl: String = DEFAULT_BROKER_URL, clientId: String = "Lumina_App_${System.currentTimeMillis() % 10000}") {
        scope.launch {
            try {
                _connectionStatus.value = MqttConnectionStatus.CONNECTING
                _lastLog.value = "Connecting to broker: $brokerUrl"

                mqttClient?.disconnect()
                mqttClient = MqttAsyncClient(brokerUrl, clientId, MemoryPersistence())

                val options = MqttConnectOptions().apply {
                    isCleanSession = true
                    connectionTimeout = 10
                    keepAliveInterval = 30
                    isAutomaticReconnect = true
                }

                mqttClient?.setCallback(object : MqttCallback {
                    override fun connectionLost(cause: Throwable?) {
                        _connectionStatus.value = MqttConnectionStatus.DISCONNECTED
                        _lastLog.value = "Connection lost: ${cause?.localizedMessage}"
                    }

                    override fun messageArrived(topic: String?, message: MqttMessage?) {
                        val payload = message?.toString() ?: return
                        _lastLog.value = "Received on $topic: $payload"
                        parseIncomingTelemetry(payload)
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {
                        Log.d(TAG, "Delivery complete")
                    }
                })

                mqttClient?.connect(options, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        _connectionStatus.value = MqttConnectionStatus.CONNECTED
                        _lastLog.value = "Connected to MQTT broker ($brokerUrl)"
                        subscribeToTopics()
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        _connectionStatus.value = MqttConnectionStatus.ERROR
                        _lastLog.value = "Connection failed: ${exception?.localizedMessage}"
                    }
                })
            } catch (e: Exception) {
                _connectionStatus.value = MqttConnectionStatus.ERROR
                _lastLog.value = "MQTT Error: ${e.localizedMessage}"
            }
        }
    }

    private fun subscribeToTopics() {
        try {
            mqttClient?.subscribe(DEFAULT_TOPIC_STATUS, 1, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    _lastLog.value = "Subscribed to $DEFAULT_TOPIC_STATUS"
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    _lastLog.value = "Subscription failed: ${exception?.localizedMessage}"
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error subscribing to topics", e)
        }
    }

    fun toggleDevice(deviceName: String) {
        val current = _devices.value.toMutableMap()
        val currentState = current[deviceName] ?: false
        val newState = !currentState
        current[deviceName] = newState
        _devices.value = current

        val relayMap = mapOf(
            "Living Room Light" to 1,
            "AC Cool 22°C" to 2,
            "Desk RGB Strip" to 3,
            "Ceiling Fan" to 4
        )
        val relay = relayMap[deviceName] ?: 1
        publishCommand(deviceName, relay, newState)
    }

    fun setDeviceState(deviceName: String, state: Boolean) {
        val current = _devices.value.toMutableMap()
        current[deviceName] = state
        _devices.value = current

        val relayMap = mapOf(
            "Living Room Light" to 1,
            "AC Cool 22°C" to 2,
            "Desk RGB Strip" to 3,
            "Ceiling Fan" to 4
        )
        val relay = relayMap[deviceName] ?: 1
        publishCommand(deviceName, relay, state)
    }

    private fun publishCommand(deviceName: String, relay: Int, state: Boolean) {
        scope.launch {
            try {
                val payload = JSONObject().apply {
                    put("device", deviceName)
                    put("relay", relay)
                    put("state", if (state) "ON" else "OFF")
                    put("timestamp", System.currentTimeMillis())
                }.toString()

                if (mqttClient?.isConnected == true) {
                    val message = MqttMessage(payload.toByteArray()).apply {
                        qos = 1
                    }
                    mqttClient?.publish(DEFAULT_TOPIC_COMMAND, message)
                    _lastLog.value = "Published: $payload"
                } else {
                    _lastLog.value = "Local state changed ($deviceName = $state), MQTT offline"
                }
            } catch (e: Exception) {
                _lastLog.value = "Publish error: ${e.localizedMessage}"
            }
        }
    }

    private fun parseIncomingTelemetry(payload: String) {
        try {
            val json = JSONObject(payload)
            if (json.has("device") && json.has("state")) {
                val device = json.getString("device")
                val state = json.getString("state").equals("ON", ignoreCase = true)
                val current = _devices.value.toMutableMap()
                current[device] = state
                _devices.value = current
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing telemetry JSON", e)
        }
    }

    fun disconnect() {
        try {
            mqttClient?.disconnect()
            _connectionStatus.value = MqttConnectionStatus.DISCONNECTED
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting MQTT", e)
        }
    }
}
