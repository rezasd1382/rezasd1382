package com.example.myvpn

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            startVpnService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // فرض بر این است که در layout دو دکمه btnConnect و btnDisconnect دارید
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnConnect).setOnClickListener {
            prepareVpn()
        }

        findViewById<Button>(R.id.btnDisconnect).setOnClickListener {
            val intent = Intent(this, MyVpnService::class.java).apply {
                action = "STOP_VPN"
            }
            startService(intent)
        }
    }

    private fun prepareVpn() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            // کاربر قبلاً مجوز نداده، نمایش پنجره تایید اندروید
            vpnPermissionLauncher.launch(intent)
        } else {
            // مجوز قبلاً داده شده است
            startVpnService()
        }
    }

    private fun startVpnService() {
        val intent = Intent(this, MyVpnService::class.java)
        startService(intent)
    }
}
