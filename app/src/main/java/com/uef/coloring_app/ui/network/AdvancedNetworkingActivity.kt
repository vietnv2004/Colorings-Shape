package com.uef.coloring_app.ui.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.uef.coloring_app.R
import com.uef.coloring_app.core.network.NetworkManager
import com.uef.coloring_app.ui.common.BaseActivity
import kotlinx.coroutines.launch

class AdvancedNetworkingActivity : BaseActivity() {
    
    private lateinit var networkManager: NetworkManager
    
    // Views
    private lateinit var networkStatusText: android.widget.TextView
    private lateinit var networkInfoText: android.widget.TextView
    
    // Switches
    private lateinit var enableNetworkSwitch: Switch
    private lateinit var wifiOnlySwitch: Switch
    private lateinit var dataSavingSwitch: Switch
    private lateinit var autoSyncSwitch: Switch
    
    // Feature buttons
    private lateinit var wifiOptimizationButton: android.widget.Button
    private lateinit var dataCompressionButton: android.widget.Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advanced_networking)
        
        initViews()
        initNetworkManager()
        setupClickListeners()
        updateNetworkStatus()
    }
    
    private fun initViews() {
        networkStatusText = findViewById(R.id.networkStatusText)
        networkInfoText = findViewById(R.id.networkInfoText)
        
        enableNetworkSwitch = findViewById(R.id.enableNetworkSwitch)
        wifiOnlySwitch = findViewById(R.id.wifiOnlySwitch)
        dataSavingSwitch = findViewById(R.id.dataSavingSwitch)
        autoSyncSwitch = findViewById(R.id.autoSyncSwitch)
        
        wifiOptimizationButton = findViewById(R.id.wifiOptimizationButton)
        dataCompressionButton = findViewById(R.id.dataCompressionButton)
    }

    private fun initNetworkManager() {
        networkManager = NetworkManager(this)
    }
    
    private fun setupClickListeners() {
        // Network Control Buttons
        findViewById<android.widget.Button>(R.id.checkNetworkButton).setOnClickListener {
            checkNetworkStatus()
        }
        
        findViewById<android.widget.Button>(R.id.optimizeNetworkButton).setOnClickListener {
            optimizeNetwork()
        }
        
        // Switch Listeners
        enableNetworkSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleNetwork(isChecked)
        }
        
        wifiOnlySwitch.setOnCheckedChangeListener { _, isChecked ->
            setWifiOnly(isChecked)
        }
        
        dataSavingSwitch.setOnCheckedChangeListener { _, isChecked ->
            setDataSaving(isChecked)
        }
        
        autoSyncSwitch.setOnCheckedChangeListener { _, isChecked ->
            setAutoSync(isChecked)
        }
        
        // Feature Buttons
        wifiOptimizationButton.setOnClickListener {
            optimizeWifi()
        }
        
        dataCompressionButton.setOnClickListener {
            enableDataCompression()
        }
    }

    private fun updateNetworkStatus() {
        lifecycleScope.launch {
            val isConnected = networkManager.isNetworkAvailable()
            val networkName = networkManager.getNetworkName()
            
            networkStatusText.text = if (isConnected) {
                getString(R.string.network_ready)
            } else {
                getString(R.string.network_unavailable)
            }
            
            networkInfoText.text = if (isConnected) {
                getString(R.string.network_connecting, networkName)
            } else {
                getString(R.string.network_config_description)
            }
        }
    }

    private fun checkNetworkStatus() {
        lifecycleScope.launch {
            val isConnected = networkManager.isNetworkAvailable()
            val networkSpeed = networkManager.getNetworkSpeed()
            val latency = networkManager.getNetworkLatency()
            
            val message = if (isConnected) {
                "Mạng: Kết nối ✓\nTốc độ: $networkSpeed Mbps\nĐộ trễ: ${latency}ms"
            } else {
                "Mạng: Không kết nối ✗"
            }
            
            Toast.makeText(this@AdvancedNetworkingActivity, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun syncNetworkData() {
        lifecycleScope.launch {
            try {
                Toast.makeText(this@AdvancedNetworkingActivity, "Đang đồng bộ dữ liệu mạng...", Toast.LENGTH_SHORT).show()
                
                val success = networkManager.syncNetworkData()
                
                if (success) {
                    Toast.makeText(this@AdvancedNetworkingActivity, "Đồng bộ thành công!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@AdvancedNetworkingActivity, "Đồng bộ thất bại", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdvancedNetworkingActivity, "Lỗi đồng bộ: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun optimizeNetwork() {
        lifecycleScope.launch {
            try {
                Toast.makeText(this@AdvancedNetworkingActivity, "Đang tối ưu mạng...", Toast.LENGTH_SHORT).show()
                
                val result = networkManager.optimizeNetwork()
                
                Toast.makeText(this@AdvancedNetworkingActivity, "Tối ưu hoàn tất: $result", Toast.LENGTH_SHORT).show()
                updateNetworkStatus()
            } catch (e: Exception) {
                Toast.makeText(this@AdvancedNetworkingActivity, "Lỗi tối ưu: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleNetwork(isEnabled: Boolean) {
        lifecycleScope.launch {
            try {
                val success = networkManager.toggleNetwork(isEnabled)
                
                if (success) {
                    val message = if (isEnabled) {
                        "Đã bật mạng WiFi"
                    } else {
                        "Đã tắt mạng WiFi"
                    }
                    Toast.makeText(this@AdvancedNetworkingActivity, message, Toast.LENGTH_SHORT).show()
                    updateNetworkStatus()
                } else {
                    val message = if (isEnabled) {
                        "Không thể bật mạng WiFi (Android 10+ không hỗ trợ)"
        } else {
                        "Không thể tắt mạng WiFi (Android 10+ không hỗ trợ)"
                    }
                    Toast.makeText(this@AdvancedNetworkingActivity, message, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdvancedNetworkingActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setWifiOnly(isEnabled: Boolean) {
        lifecycleScope.launch {
            try {
                networkManager.setWifiOnly(isEnabled)
                
                Toast.makeText(this@AdvancedNetworkingActivity, 
                    if (isEnabled) "Chỉ sử dụng WiFi" else "Cho phép dữ liệu di động", 
                    Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@AdvancedNetworkingActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setDataSaving(isEnabled: Boolean) {
        lifecycleScope.launch {
            try {
                networkManager.setDataSaving(isEnabled)
                
                Toast.makeText(this@AdvancedNetworkingActivity, 
                    if (isEnabled) "Đã bật tiết kiệm dữ liệu" else "Đã tắt tiết kiệm dữ liệu", 
                    Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@AdvancedNetworkingActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setAutoSync(isEnabled: Boolean) {
        lifecycleScope.launch {
            try {
                networkManager.setAutoSync(isEnabled)
                
                Toast.makeText(this@AdvancedNetworkingActivity, 
                    if (isEnabled) "Đã bật đồng bộ tự động" else "Đã tắt đồng bộ tự động", 
                    Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@AdvancedNetworkingActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun optimizeWifi() {
        lifecycleScope.launch {
            try {
                Toast.makeText(this@AdvancedNetworkingActivity, "Đang tối ưu WiFi...", Toast.LENGTH_SHORT).show()
                
                val result = networkManager.optimizeWifi()
                
                Toast.makeText(this@AdvancedNetworkingActivity, "WiFi đã được tối ưu: $result", Toast.LENGTH_SHORT).show()
                wifiOptimizationButton.text = "ĐANG SỬ DỤNG"
                wifiOptimizationButton.setBackgroundColor(getColor(R.color.success_color))
            } catch (e: Exception) {
                Toast.makeText(this@AdvancedNetworkingActivity, "Lỗi tối ưu WiFi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun enableDataCompression() {
        lifecycleScope.launch {
            try {
                Toast.makeText(this@AdvancedNetworkingActivity, "Đang bật nén dữ liệu...", Toast.LENGTH_SHORT).show()
                
                val result = networkManager.enableDataCompression()
                
                Toast.makeText(this@AdvancedNetworkingActivity, "Nén dữ liệu: $result", Toast.LENGTH_SHORT).show()
                dataCompressionButton.text = "ĐANG SỬ DỤNG"
                dataCompressionButton.setBackgroundColor(getColor(R.color.success_color))
            } catch (e: Exception) {
                Toast.makeText(this@AdvancedNetworkingActivity, "Lỗi nén dữ liệu: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateNetworkStatus()
    }
}